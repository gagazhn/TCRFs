/**   
 * @Package learning
 * @Description: TODO
 * @author gagazhn
 * @date May 10, 2012 10:55:23 AM
 * @version 1.0   
 */
package gcrfs;

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
		mFeatureSet.tight(mInstanceList);
		mTemplateQueue.compile(mTestInsList, mFeatureSet, mLabelSet, false);
		System.err.println("Compile to gain features: " + mFeatureSet.getFeatureSize());
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
				
//				L = computeGravityG(lambda, gravity, i);
//				L = computeGravity2(lambda, gravity, i);
				L = mInference.computeGravity(this, lambda, gravity, iter);
				
				System.err.println("log-likelihood: " + (-L));
				
				LBFGS.lbfgs(lambda.length, 6, lambda, L, gravity, false, dragond, iprint, 0.001, 1.0e-16, iflag);
				
				if (iflag[0] == 0) {
					break;
				}
				
				
				// 训练过程中进行评测
				Evaluation eval = new Evaluation(mFeatureSet, mLabelSet);
				
				for (Instance instance : mTestInsList) {
					Path path = mInference.exec(this, instance, mFeatureSet, mLabelSet);
					for (int t = 0; t < path.path.length; t++) {
						String lString = instance.getTimestampSequence().get(t).getLabel();
						int labelIndex = mLabelSet.labelIndex(lString);
						eval.statstic(labelIndex, path.path[t]);
					}
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
		
		Evaluation eval = new Evaluation(mFeatureSet, mLabelSet);
		for (Instance instance : instanceList) {
			Path path = mInference.exec(this, instance, mFeatureSet, mLabelSet);
			
			for (int i = 0; i < path.path.length; i++) {
				String lString = instance.getTimestampSequence().get(i).getLabel();
				ArrayList<String> q = instance.getTimestampSequence().get(i).getFeatures();
				String fString = "{";
				for (int j = 0; j < q.size() && j < 4; j++) {
					fString = fString + q.get(j);
				}
				fString = fString + "}";
				int labelIndex = mLabelSet.labelIndex(lString);
				Label label = mLabelSet.labelByIndex(path.path[i]);
				eval.statstic(labelIndex, path.path[i]);
				
				if (outputType == Model.DEBUG_OUTPUT) {
//				System.out.println(fString + " >> " + lString + " " + label.value() + (lString.equals(label.value()) ? "" : " $$$$$$") + " " + path.score);
				System.out.println(fString + " >> " + lString + " " + label.value() + (lString.equals(label.value()) ? "" : " $$$$$$"));
				} else if (outputType == Model.CLEAN_OUTPUT) {
					System.out.println(label.value());
				}
			}
			System.out.println();
		}
		
		eval.info();
	}
	
	/**
	 * 计算梯度并返回likelihood
	 * 
	private double computeGravity(double[] lambda, double[] gravity, int iter) {
		double L = 0.0;
		int labelSize = mLabelSet.getLabelSize();
		int featureSize = mFeatureSet.getFeatureSize();
		
		// p_theta(s|o)
		double[] expF = new double[featureSize];
		
		// the last term
		for (int i = 0; i < featureSize; i++) {
			gravity[i] = -1.0 * lambda[i] / (SIGMA * SIGMA);
			L -= lambda[i] * lambda[i] / (2 * SIGMA * SIGMA);
		}
		
		// For each instance:
		for (Instance instance : mInstanceList) {
			int length = instance.getTimestampSequence().size();
			
			double scale = 0;
			double[] expF_s = new double[featureSize];
			double Z_o = 0;
			
			// 计算scale,防止指数过大
			for (int i = 0; i < length; i++) {
				for (Feature feature : instance.getFeatureSequence().get(i)) {
					scale += lambda[feature.getIndex()];
				}
			}
			
			// pos: 在第pos个位置异化
			// gLabel: 在pos处的异化值
			// For each label sequence S:
			for (int pos = 0; pos < length; pos++) {
				for (int gLabel = 0; gLabel < labelSize - 1; gLabel++) {
					
					double P_s_o = 0.0;
					
					// f_k的数量
					int[] C_s_i = new int[featureSize];
					
					// 一次将一个异化槽填满
					for (int t = 0; t < length; t++) {
						for (Feature feature : instance.getFeatureSequence().get(t)) {
							if (t == pos) {
								String fString = feature.getValue();
								int lIndex = feature.getLabel().getIndex();
								String preLString = feature.getPreLabel() == null ? null : feature.getPreLabel().value();
								String lString = mLabelSet.labelByIndex((lIndex + 1 + gLabel) % labelSize).value();
								Feature f = mFeatureSet.lookupFeature(fString, preLString, lString);
								if (f != null) {
									P_s_o += lambda[f.getIndex()];
									C_s_i[f.getIndex()]++;
								}
							} else if (instance.graph.parent(t) == pos && feature.type == Feature.TYPE_EDGE) {
								int lIndex = feature.getLabel().getIndex();
								String fString = feature.getValue();
								String preLString = mLabelSet.labelByIndex((lIndex + 1 + gLabel) % labelSize).value();
								String lString = feature.getLabel().value();
								Feature f = mFeatureSet.lookupFeature(fString, preLString, lString);
								if (f != null) {
									P_s_o += lambda[f.getIndex()];
									C_s_i[f.getIndex()]++;
								}
							} else {
								P_s_o += lambda[feature.getIndex()];
								C_s_i[feature.getIndex()]++;
							}
						}
					}
					
					P_s_o = Math.exp(P_s_o - scale);
					Z_o += P_s_o;

					for (int i = 0; i < featureSize; i++) {
						expF_s[i] += P_s_o * C_s_i[i];
					}
				}
			}
			// S end
			
			// loop for the last time
			double p = 0;
			int[] c = new int[featureSize];
			for (int t = 0; t < length; t++) {
				for (Feature feature : instance.getFeatureSequence().get(t)) {
					p += lambda[feature.getIndex()];
					c[feature.getIndex()]++;
					
					gravity[feature.getIndex()]++;
				}
			}
			L += p - scale;
			p = Math.exp(p - scale);
			Z_o += p;
			
			for (int i = 0; i < featureSize; i++) {
				expF_s[i] += p * c[i];
			}
			// loop end
			
			for (int i = 0; i < featureSize; i++) {
				expF_s[i] = expF_s[i] / Z_o;
				expF[i] += expF_s[i];
			}
			
			L -= Math.log(Z_o);
		}
		// Instance End
		
		// 梯度的前两个terms
		for (int i = 0;  i < gravity.length; i++) {
			gravity[i] -= expF[i];
		}
		
		// 因为要求最小值，将L与G反转
		L *= -1.0;
		for (int i = 0; i < gravity.length; i++) {
			gravity[i] *= -1.0;
		}
		
		return L;
	}
	 */
	
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
