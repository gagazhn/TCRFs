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

import optimization.LBFGS;
import template.TemplateQueue;
import type.Feature;
import type.FeatureSet;
import type.Instance;
import type.InstanceList;
import type.Label;
import type.LabelSet;

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
	
	private final double SIGMA = 10;
	
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
		System.out.println("Compile to gain features: " + mFeatureSet.getFeatureSize());
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
			System.out.println("Trainning...");
			for (int i = 0; i < iter; i++) {
				L = computeGravity(lambda, gravity, i);
				LBFGS.lbfgs(lambda.length, 6, lambda, L, gravity, false, dragond, iprint, 0.001, 1.0e-16, iflag);
				
				if (iflag[0] == 0) {
					break;
				}
				
				System.out.println("ITER: " + i);
				System.out.println("likelihood: " + L);
				
				// 训练过程中进行评测
				Evaluation eval = new Evaluation(mFeatureSet, mLabelSet);
				
				for (Instance instance : mTestInsList) {
					Path path = mInference.exec(instance, mFeatureSet, mLabelSet);
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
		}
	}
	
	/**
	 * 测试文本
	 * @param instanceList 文本
	 * @param templateQueue 模板队列
	 */
	public void test(InstanceList instanceList) {
		mTemplateQueue.compile(instanceList, mFeatureSet, mLabelSet, false);
		
		Evaluation eval = new Evaluation(mFeatureSet, mLabelSet);
		for (Instance instance : instanceList) {
			Path path = mInference.exec(instance, mFeatureSet, mLabelSet);
			
			for (int i = 0; i < path.path.length; i++) {
				String lString = instance.getTimestampSequence().get(i).getLabel();
				String fString = instance.getTimestampSequence().get(i).getFeatures().toString();
				int labelIndex = mLabelSet.labelIndex(lString);
				Label label = mLabelSet.labelByIndex(path.path[i]);
//				System.out.println(fString + " " + lString + " " + label.value());
				eval.statstic(labelIndex, path.path[i]);
				System.out.println(fString + " >> " + lString + " " + label.value() + (lString.equals(label.value()) ? "" : " $$$$$$") + " " + path.score);
			}
			System.out.println();
		}
		eval.info();
	}
	
	/**
	 * 计算梯度并返回likelihood
	 * 
	 */
	private double computeGravity(double[] lambda, double[] gravity, int iter) {
		double L = 0.0;
		int labelSize = mLabelSet.getLabelSize();
		int featureSize = mFeatureSet.getFeatureSize();
		
		// p_theta(s|o)
		double[] expF = new double[featureSize];
		double Z = 0;
		double[][] zz;
		
		// the last term
		for (int i = 0; i < featureSize; i++) {
			gravity[i] = -1.0 * lambda[i] / (SIGMA * SIGMA);
			L -= lambda[i] * lambda[i] / (2 * SIGMA * SIGMA);
		}
		
		// 计算p_theta(s|o)
		for (Instance instance : mInstanceList) {
			int length = instance.getTimestampSequence().size();
			//!!!!!性能
			zz = new double[labelSize - 1][length];
			double zs = 0;
			double scale = 0;
			int[] sCount = new int[featureSize];
			double Zx = 0;
			double[] expFF = new double[featureSize];
			
			// s
			for (int i = 0; i < length; i++) {
				for (Feature feature : instance.getFeatureSequence().get(i)) {
					zs += lambda[feature.getIndex()];
					sCount[feature.getIndex()]++;
				}
			}
			scale = zs;
//			Zx += Math.exp(zx - scale); == 1
			Zx += 1;
			for (int i = 0; i < featureSize; i++) {
				expFF[i] += sCount[i];
			}
			
			// pos: 在第pos个位置异化
			for (int pos = 0; pos < length; pos++) {
				for (int gLabel = 0; gLabel < labelSize - 1; gLabel++) {
					// f_k的数量
					int[] fCount = new int[featureSize];
					
					// 一次将一个异化槽填满
					for (int i = 0; i < length; i++) {
						for (Feature feature : instance.getFeatureSequence().get(i)) {
							if (i == pos) {
								String fString = feature.getValue();
								int lIndex = feature.getLabel().getIndex();
								String preLString = feature.getPreLabel() == null ? null : feature.getPreLabel().value();
								String lString = mLabelSet.labelByIndex((lIndex + 1 + gLabel) % labelSize).value();
								Feature f = mFeatureSet.lookupFeature(fString, preLString, lString);
								if (f != null) {
									zz[gLabel][pos] += lambda[f.getIndex()];
									fCount[f.getIndex()]++;
								}
							} else if (instance.graph.parent(i) == pos && feature.type == Feature.TYPE_EDGE) {
								int lIndex = feature.getLabel().getIndex();
								String fString = feature.getValue();
								String preLString = mLabelSet.labelByIndex((lIndex + 1 + gLabel) % labelSize).value();
								String lString = feature.getLabel().value();
								Feature f = mFeatureSet.lookupFeature(fString, preLString, lString);
								if (f != null) {
									zz[gLabel][pos] += lambda[f.getIndex()];
									fCount[f.getIndex()]++;
								}
							} else {
								zz[gLabel][pos] += lambda[feature.getIndex()];
								fCount[feature.getIndex()]++;
							}
						}
					}
					
					zz[gLabel][pos] = Math.exp(zz[gLabel][pos] - scale);
					Zx += zz[gLabel][pos];
					//!!
					if (zz[gLabel][pos] > 1) {
						
					}
					for (int i = 0; i < featureSize; i++) {
						expFF[i] += zz[gLabel][pos] * fCount[i];
					}
				}
			}
			
			for (int i = 0; i < featureSize; i++) {
				expFF[i] = expFF[i] / Zx;
			}
			for (int i = 0; i < featureSize; i++) {
				expF[i] += expFF[i];
			}
			Z += Math.log(Zx);
		}
		
		L -= Z;

		// 梯度的前两个terms
		for (int i = 0;  i < gravity.length; i++) {
			gravity[i] -= expF[i];
			gravity[i] += mFeatureSet.getFeatureByIndex(i).getFreq();
		}
		
		//!!
//		System.out.println("Likelihood: " + L);
		
		// 因为要求最小值，将L与G反转
		L *= -1.0;
		for (int i = 0; i < gravity.length; i++) {
			gravity[i] *= -1.0;
		}
		
		return L;
	}
	
	public FeatureSet features() {
		return mFeatureSet;
	}
}
