/**   
 * @Package perform
 * @Description: TODO
 * @author gagazhn
 * @date May 15, 2012 2:52:59 PM
 * @version 1.0   
 */
package inference;

import gagazhn.time.Statistic;
import gcrfs.Graph;
import gcrfs.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import type.Feature;
import type.FeatureSequence;
import type.FeatureSet;
import type.FeatureVector;
import type.Instance;
import type.InstanceList;
import type.LabelSet;
import util.Matrix;

/**
 * Viterbi算法
 * 
 * @author gagazhn
 */
public class TreeViterbi extends Inference {
	private double[][] mV;
	private double[][][] mTransp;
	private int[][] mPath;

	private void conformSpace(int length, int labelSize) {
		if (mV == null) {
			mV = new double[length][labelSize];
			mTransp = new double[length][labelSize][labelSize];
			mPath = new int[length][labelSize];
		} else {
			if (mV.length < length) {
				mV = new double[length][labelSize];
				mTransp = new double[length][labelSize][labelSize];
				mPath = new int[length][labelSize];
			}
		}
	}
	
	public Path exec(Model model, Instance instance, FeatureSet featureSet, LabelSet labelSet) {
		int labelSize = labelSet.getLabelSize();
		int length = instance.getTimestampSequence().size();
		Graph graph = instance.graph;
		
		conformSpace(length, labelSize);
		
		double[][] beta = new double[length][labelSize];
		
		int[] finalPath = new int[length];
		double finalScore = 0.0;
		
		// 初始化概率数组
		for (int t = 0; t < length; t++) {
			model.compute_log_Mi(instance, t, mTransp[t], mV[t], false);
		}
		
		// 独立点
		for (int t = 0; t < length; t++) {
			if (graph.parent(t) == t) {
				double maxP = 0;
				int maxI = 0;
				for (int i = 0; i < labelSize; i++) {
					if (maxP < mV[t][i]) {
						maxP = mV[t][i];
						maxI = i;
					}
				}
				
				finalPath[t] = maxI;
			}
		}
		
		LinkedList<Integer> queue = new LinkedList<Integer>();
		for (int t = 0; t < length; t++	) {
			if (graph.isLeaf(t) && graph.parent(t) != t) {
				queue.addFirst(t);
			}
		}
		
		int t = -2;
		short[] count = new short[length];
		while (!queue.isEmpty()) {
			t = queue.removeFirst();
			int parent = graph.parent(t);
			
			if (graph.isLeaf(t)) {
				for (int i = 0; i < labelSize; i++) {
					beta[t][i] = mV[t][i];
				}
			} else if (graph.isCross(t)) {
				for (int i = 0; i < labelSize; i++) {
					for (int child : graph.childrenList(t)) {
						double maxP = 0.0;
						for (int j = 0; j < labelSize; j++) {
							double p = beta[child][j] + mTransp[child][i][j] + mV[t][i];
							if (maxP < p) {
								maxP = p;
								
								mPath[child][i] = j;
							}
						}
						beta[t][i] += maxP;
					}
				}
			} else {
				int child = graph.childrenList(t).get(0);
				for (int i = 0; i < labelSize; i++) {
					double maxP = 0.0;
					for (int j = 0; j < labelSize; j++) {
						double p = beta[child][j] + mTransp[child][i][j] + mV[t][i];
						if (maxP < p) {
							maxP = p;
							beta[t][i] = p;
							
							mPath[child][i] = j;
						}
					}
				}
			}
			
			if (parent != Graph.ROOT) {
				count[parent]++;
				
				if (count[parent] >= graph.childrenList(parent).size()) {
					queue.addLast(parent);
				}
			}
		}
		
		if (t != -2) {
			int label = 0;
			double maxP = 0;
			for (int i = 0; i < labelSize; i++) {
				if (maxP < beta[t][i]) {
					maxP = beta[t][i];
					label = i;
				}
			}
			
			finalPath[t] = label;
			queue.clear();
			queue.add(t);
			while (!queue.isEmpty()) {
				t = queue.removeFirst();
				label = finalPath[t];
				
				for (int child : graph.childrenList(t)) {
					queue.addLast(child);
					
					finalPath[child] = mPath[child][label];
				}
			}
		} else {
			
		}
		
		return new Path(finalPath, finalScore);
	}
	
	public double computeGravity(Model model, double[] lambda, double[] gravity, int iter) {
		double logli = 0;
		
		int labelSize = model.labels().getLabelSize();
		int featureSize = model.features().getFeatureSize();
		
		// temp zoom
		double[][] Mi = new double[labelSize][labelSize];
		double[] Vi = new double[labelSize];
		double[] preAlpha = new double[labelSize];
		double[] nextAlpha = new double[labelSize];
		double[] ExpF = new double[featureSize];
		// temp zoom
		
		// the last term
		for (int i = 0; i < featureSize; i++) {
			gravity[i] = -1.0 * lambda[i] / (Model.SIGMA * Model.SIGMA);
			logli -= lambda[i] * lambda[i] / (2 * Model.SIGMA * Model.SIGMA);
		}
		
		for (Instance instance : model.trainInstances()) {
			
			//!!
//			Statistic.start("TOTAL");
//			Statistic.start("INIT");
			
			Graph graph = instance.graph;
			int length = instance.getTimestampSequence().size();
			
			double[][] beta = new double[length][labelSize];
			double[][] alpha = new double[length][labelSize];
			double[] scale = new double[length];
			
			// init
			for (int i = 0; i < featureSize; i++) {
				ExpF[i] = 0;
			}
			
			//!!
//			Statistic.note(false, "INIT");
//			Statistic.start("BACK");
			
			// backward
			LinkedList<Integer> queue = new LinkedList<Integer>();
			LinkedList<Integer> isolation = new LinkedList<Integer>();
			for (int t = 0; t < length; t++) {
				if (graph.isLeaf(t) && graph.parent(t) != t) {
					queue.add(t);
					
					for (int i = 0; i < labelSize; i++) {
						beta[t][i] = 1;
					}
					scale[t] = labelSize;
					Matrix.scale(beta[t], scale[t]);
				}
				
				if (graph.isLeaf(t) && graph.parent(t) == t) {
					isolation.add(t);
				}
			}
			
			short[] betaFlag = new short[length];
			while (!queue.isEmpty()) {
				int t = queue.removeFirst();
				int parent = graph.parent(t);
				
				if (parent == Graph.ROOT) {
					continue;
				}
				
				model.compute_log_Mi(instance, t, Mi, Vi, true);
				double[] temp = Matrix.copy(beta[t]);
				Matrix.mult(temp, Vi);
				Matrix.matric_mult(labelSize, temp, Mi, Matrix.copy(temp), false);
				
				if (betaFlag[parent] == 0) {
					beta[parent] = temp;
				} else {
					Matrix.mult(beta[parent], temp);
				}
				
				// 记录本节点已经计算完毕
				betaFlag[parent]++;
				
				// 父节点的所有孩子是否都已经计算完毕
				if (betaFlag[parent] >= graph.childrenList(parent).size()) {
					if (graph.parent(parent) != Graph.ROOT) {
						queue.addLast(parent);
					}

					// scale
					scale[parent] = Matrix.sum(beta[parent]);
					Matrix.scale(beta[parent], scale[parent]);
				}
			}
			
			//!!
//			Statistic.note(false, "BACK");
//			Statistic.start("FORW");
			
			// forward
			double seq_logli = 0;
			queue.clear();
			
			Matrix.fileWith(nextAlpha, 1);
			double[] lastAlpha = null;
			int root = 0;
			if (graph.childrenList(Graph.ROOT).size() > 0) {
				root = graph.childrenList(Graph.ROOT).get(0);

				queue.add(root);
				while (!queue.isEmpty()) {
					int t = queue.removeFirst();
					int parent = graph.parent(t);

					for (int child : graph.childrenList(t)) {
						queue.addLast(child);
					}

					// 3种情况：
					// 1：根
					// 2：t节点为交点的子节点
					// 3：普通
					if (parent == Graph.ROOT) {
						model.compute_log_Mi(instance, t, Mi, Vi, true);
						Matrix.mult(nextAlpha, Vi);
					} else if (graph.isCross(parent)) {
						nextAlpha = Matrix.initVector(labelSize, 1);
						for (int child : graph.childrenList(parent)) {
							if (child == t) {
								continue;
							}

							model.compute_log_Mi(instance, child, Mi, Vi, true);
							double[] temp = Matrix.copy(beta[child]);
							Matrix.mult(temp, Vi);
							Matrix.matric_mult(labelSize, temp, Mi, Matrix.copy(temp), false);

							Matrix.mult(nextAlpha, temp);
						}
						
						Matrix.mult(nextAlpha, alpha[parent]);
						preAlpha = Matrix.copy(nextAlpha);

						model.compute_log_Mi(instance, t, Mi, Vi, true);
						Matrix.matric_mult(labelSize, nextAlpha, Mi, Matrix.copy(nextAlpha), true);
						Matrix.mult(nextAlpha, Vi);
					} else {
						model.compute_log_Mi(instance, t, Mi, Vi, true);
						preAlpha = alpha[parent];
						Matrix.matric_mult(labelSize, nextAlpha, Mi, alpha[parent], true);
						Matrix.mult(nextAlpha, Vi);
					}


					for (Feature feature : instance.getFeatureSequence().get(t)) {
						String fString = feature.getValue();

						gravity[feature.getIndex()]++;
						seq_logli += lambda[feature.getIndex()];

						if (feature.type == Feature.TYPE_STATE) {
							for (int i = 0; i < labelSize; i++) {
								// String preLString =
								// mLabelSet.labelByIndex(i).value();
								String lString = model.labels().labelByIndex(i).value();

								Feature f = model.features().lookupFeature(fString, null, lString);
								if (f != null) {
									ExpF[f.getIndex()] += nextAlpha[i]
											* beta[t][i];
								}
							}
						} else if (feature.type == Feature.TYPE_EDGE) {
							for (int i = 0; i < labelSize; i++) {
								for (int j = 0; j < labelSize; j++) {
									String preLString = model.labels().labelByIndex(j).value();
									String lString = model.labels().labelByIndex(i).value();

									Feature f = model.features().lookupFeature(fString, preLString, lString);
									if (f != null) {
										ExpF[f.getIndex()] += preAlpha[j]
												* Vi[i] * Mi[j][i] * beta[t][i];
									}
								}
							}
						}
					}

					// scale
					alpha[t] = Matrix.copy(nextAlpha);
					Matrix.scale(alpha[t], scale[t]);
					lastAlpha = alpha[t];
				}
				
				//!!
//				Statistic.note(false, "FORW");
				
				double Zx = Matrix.sum(lastAlpha);
				
				seq_logli -= Math.log(Zx);
				
				// scale
				for (int i = 0; i < length; i++) {
					if (scale[i] != 0) {
						seq_logli -= Math.log(scale[i]);
					}
				}
				
				logli += seq_logli;
				
				for (int i = 0; i < featureSize; i++) {
					gravity[i] -= ExpF[i] / Zx;
				}
				
				//!!
//				Statistic.note(false, "TOTAL");
			} else {
				// instance只有孤立点时
				// 计算上孤立点
//				double[] isoAlpha = Matrix.initVector(labelSize, 1);
//				for (int iso : isolation) {
//					model.compute_log_Mi(instance, iso, Mi, Vi, true);
//					Matrix.mult(isoAlpha, Vi);
//				}
				
			}

		}
		
		// 因为要求最小值，将L与G反转
		logli *= -1.0;
		for (int i = 0; i < gravity.length; i++) {
			gravity[i] *= -1.0;
		}
		
		//!!
//		System.out.println(Statistic.getTime("TOTAL"));
//		System.out.println(Statistic.getTime("INIT"));
//		System.out.println(Statistic.getTime("BACK"));
//		System.out.println(Statistic.getTime("FORW"));
		
		return logli;
	}
}
