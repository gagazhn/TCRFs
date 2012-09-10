/**   
 * @Package learning
 * @Description: TODO
 * @author gagazhn
 * @date May 10, 2012 10:55:23 AM
 * @version 1.0   
 */
package gcrfs;

import gagazhn.time.Statistic;
import inference.Inference;
import inference.Inference.Path;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

import optimization.LBFGS;
import template.TemplateQueue;
import type.Feature;
import type.FeatureSet;
import type.Instance;
import type.InstanceList;
import type.Label;
import type.LabelSet;
import util.Matrix;

/**
 * CRFs模型的核心对象。
 * 
 * @author gagazhn
 */
public class Model implements Serializable {
	private static final long serialVersionUID = -2201091313958992790L;
	
	private transient InstanceList mInstanceList;
	private transient InstanceList mTestInsList;
	
	private TemplateQueue mTemplateQueue;
	private boolean isInit;
	private FeatureSet mFeatureSet;
	private LabelSet mLabelSet;
	private Inference mInference;
	
	public static final double SIGMA = 10;
	
	public static final int DEBUG_OUTPUT = 0;
	public static final int CLEAN_OUTPUT = 1;
	
	/**
	 * @param instanceList 训练数据
	 * @param testInsList 训练阶段评测数据
	 * @param templateQueue 模板队列
	 * @param inference 演算对象
	 */
	public Model(InstanceList instanceList, InstanceList testInsList, TemplateQueue templateQueue, Inference inference) {
		this.mInstanceList = instanceList;
		this.mTestInsList = testInsList;
		this.mTemplateQueue = templateQueue;
		this.isInit = false;
		this.mLabelSet = new LabelSet();
		this.mFeatureSet = new FeatureSet(mLabelSet);
		this.mInference = inference;
	}
	
	private void init() {
		this.isInit = true;
		
		mTemplateQueue.compile(mInstanceList, mFeatureSet, mLabelSet, true);
		System.err.println("Compile to gain features: " + mFeatureSet.getFeatureSize());
		
		mFeatureSet.tight(mInstanceList);
		System.err.println("Tight to gain features: " + mFeatureSet.getFeatureSize());
		
		mTemplateQueue.compile(mTestInsList, mFeatureSet, mLabelSet, false);
	}
	
	/**
	 * 训练入口
	 * 
	 * @param iter 最大迭代次数
	 */
	public void train(int iter) {
		if (!this.isInit) {
			init();
		}
		
		double[] lambda = mFeatureSet.getLambda();
		// 梯度
		double[] gravity = new double[lambda.length];
		// likelihood
		double L = 0.0;
		
		// 调LBFGS方法对权重lambda[]进行更新
		int[] iprint = {-1, 0};
		int[] iflag = {0};
		double[] dragond = new double[lambda.length];
		try {
			// 迭代开始
			System.err.println("Trainning...");
			for (int i = 0; i < iter; i++) {
				System.err.println("ITER: " + i);
				
				L = mInference.computeGravity(this, lambda, gravity, iter);
				
				System.err.println("log-likelihood: " + (-L));
				
				// 更新权重，LBFGS
				LBFGS.lbfgs(lambda.length, 6, lambda, L, gravity, false, dragond, iprint, 0.001, 1.0e-16, iflag);
				
				if (iflag[0] == 0) {
					break;
				}
				
				System.err.println("update...");
				
				// 训练过程中进行评测, 训练过程中使用overlaping测试
				Evaluation eval = new Evaluation(mFeatureSet, mLabelSet, false);
				
				for (Instance instance : mTestInsList) {
					Path path = mInference.exec(this, instance, mFeatureSet, mLabelSet);
					int[] answer = new int[path.path.length];
					for (int t = 0; t < path.path.length; t++) {
						String lString = instance.getTimestampSequence().get(t).getLabel();
						int labelIndex = mLabelSet.labelIndex(lString);
						answer[t] = labelIndex;
					}
					
					eval.staticic(answer, path.path);
				}
				eval.info();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	/**
	 * 测试文本
	 * @param instanceList 文本
	 * @param templateQueue 模板队列
	 */
	public void test(InstanceList instanceList, int outputType) {
		mTemplateQueue.compile(instanceList, mFeatureSet, mLabelSet, false);
		
		Evaluation eval = new Evaluation(mFeatureSet, mLabelSet, true);
		for (Instance instance : instanceList) {
			Path path = mInference.exec(this, instance, mFeatureSet, mLabelSet);
			int[] answer = new int[path.path.length];
			for (int t = 0; t < path.path.length; t++) {
				String lString = instance.getTimestampSequence().get(t).getLabel();
				int labelIndex = mLabelSet.labelIndex(lString);
				answer[t] = labelIndex;
			}
			eval.staticic(answer, path.path);
			
			for (int i = 0; i < path.path.length; i++) {
				String lString = instance.getTimestampSequence().get(i).getLabel();

				ArrayList<String> q = instance.getTimestampSequence().get(i).getFeatures();
				String fString = "{";
				for (int j = 0; j < q.size(); j++) {
					fString = fString + " " + q.get(j);
				}
				fString = fString + "}";
				int labelIndex = mLabelSet.labelIndex(lString);
				Label label = mLabelSet.labelByIndex(path.path[i]);

				if (outputType == Model.DEBUG_OUTPUT) {
					//!! for test
					if ("KEY:OP".equals(lString)) {
						lString = "KEY:  OP";
					}
					String ll = label.value();
					if ("KEY:OP".equals(ll)) {
						ll = "KEY:  OP";
					}
				System.out.println(lString + " " + ll + "\t" + (lString.equals(ll) ? "   " : " $ ") + fString);
//				System.out.println(lString + " " + label.value() + "\t" + (lString.equals(label.value()) ? "   " : " $ ") + fString);
				} else if (outputType == Model.CLEAN_OUTPUT) {
					System.out.println(label.value());
				}
			}
			System.out.println();
		}
		
		eval.info();
	}
	
	public void compute_log_Mi(Instance instance, int t, double[][] Mi, double[] Vi, boolean E) {
		int labelSize = mLabelSet.getLabelSize();
		double[] lambda = mFeatureSet.getLambda();
		
		for (int i = 0; i < labelSize; i++) {
			Vi[i] = 0;
			
			for (int j = 0; j < labelSize; j++) {
				Mi[i][j] = 0;
			}
		}
		
		for (Feature feature : instance.getFeatureSequence().get(t)) {
			String fString = feature.getValue();
			
			if (feature.type == Feature.TYPE_STATE) {
				for (int i = 0; i < labelSize; i++) {
					String lString = mLabelSet.labelByIndex(i).value();
					
					Feature f = mFeatureSet.lookupFeature(fString, null, lString);
					if (f != null) {
						Vi[i] += lambda[f.getIndex()];
					}
				}
			} else if (feature.type == Feature.TYPE_EDGE) {
				for (int i = 0; i < labelSize; i++) {
					for (int j = 0; j < labelSize; j++) {
						String preLString = mLabelSet.labelByIndex(j).value();
						String lString = mLabelSet.labelByIndex(i).value();
						
						Feature f = mFeatureSet.lookupFeature(fString, preLString, lString);
						if (f != null) {
							Mi[j][i] += lambda[f.getIndex()];
						}
					}
				}
			}
		}
		
		if (E) {
			for (int i = 0; i < labelSize; i++) {
				Vi[i] = Math.exp(Vi[i]);
				
				for (int j = 0; j < labelSize; j++) {
					Mi[i][j] = Math.exp(Mi[i][j]);
				}
			}
		}
	}
	
	
	
	public FeatureSet features() {
		return mFeatureSet;
	}
	
	public LabelSet labels() {
		return mLabelSet;
	}
	
	public InstanceList trainInstances() {
		return mInstanceList;
	}
}
