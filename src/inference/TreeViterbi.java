/**   
 * @Package perform
 * @Description: TODO
 * @author gagazhn
 * @date May 15, 2012 2:52:59 PM
 * @version 1.0   
 */
package inference;

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
	
	public Path exec(Instance instance, FeatureSet featureSet, LabelSet labelSet) {
		double[] lambda = featureSet.getLambda();
		int labelSize = labelSet.getLabelSize();
		FeatureSequence featureSequence = instance.getFeatureSequence();
		Graph graph = instance.graph;
		
		int length = featureSequence.size();
		conformSpace(length, labelSize);
		
		// 记录最终要返回的最终路径和概率
		int[] finalPath = new int[length];
		double finalScore = 0.0;
		
		// 初始化
		for (int t = 0; t < length; t++) {
			for (int i = 0; i < labelSize; i++) {
				for (int j = 0; j < labelSize; j++) {
					mTransp[t][i][j] = 0;
				}
			}
		}
		
		// 扫瞄所有时间点,生成转移概率
		for (int t = 0; t < length ; t++) {
			FeatureVector featureVector = instance.getFeatureSequence().get(t);
			
			for (Feature feature : featureVector) {
				String fString = feature.getValue();
				if (feature.type == Feature.TYPE_EDGE) {
					for (int i = 0; i < labelSize; i++) {
						for (int j = 0; j < labelSize; j++) {
							String preLString = labelSet.labelByIndex(j).value();
							String lString = labelSet.labelByIndex(i).value();
							Feature f = featureSet.lookupFeature(fString, preLString, lString);
							if (f != null) {
								mTransp[t][j][i] += lambda[f.getIndex()];
							}
						}
					}
				} else {
					for (int i = 0; i < labelSize; i++) {
						String lString = labelSet.labelByIndex(i).value();
						Feature f = featureSet.lookupFeature(fString, null, lString);
						if (f != null) {
							for (int j = 0; j < labelSize; j++) {
								mTransp[t][j][i] += lambda[f.getIndex()];
							}
						}
					}
				}
			}
		}
		//!!! + *
//		norm(mTransp, length, labelSize);
		
		// 先广扫描，记录所有的中间节点(//!!先广扫描是因为想实现动态归化算法，不过现在
		// 没有实现。
		LinkedList<Integer> list = new LinkedList<Integer>();
		ArrayList<Integer> cross = new ArrayList<Integer>();
		list.add(-1);
		
		while(!list.isEmpty()) {
			int t = list.poll();
			if (graph.isCross(t)) {
				cross.add(t);
			}
			
			for (int child : graph.childrenList(t)) {
				list.addLast(child);
			}
		}
		
		// 枚举中间节点的取值
		int[][] enumers = enumerate(cross, labelSize);
		
		// viterbi算法,对所有的中间节点进行计算,取最大值
		for (int e = 0; e < enumers.length; e++) {
			int[] assignments = enumers[e];
			HashMap<Integer, Integer> assignment = new HashMap<Integer, Integer>();
			for (int i = 0; i < cross.size(); i++) {
				int node = cross.get(i);
				int v = assignments[i];
				assignment.put(node, v);
			}
			
			int[] resultPath = new int[length];
			visit(graph, Graph.ROOT, length, labelSize, assignment, mTransp, mV, mPath);
			double p = getPath(graph, length, labelSize, assignment, mTransp, mV, mPath, resultPath);
			
			if (p > finalScore) {
				finalScore = p;
				finalPath = resultPath;
			}
		}
		
		return new Path(finalPath, Math.pow(finalScore, 1.0 / (enumers[0].length + 1)));
	}
	
	private static void norm(double[][][] transp, int length, int labelSize) {
		for (int t = 0; t < length; t++) {
			double z = 0.0;
			
			for (int i = 0; i < labelSize; i++) {
				for (int j = 0; j < labelSize; j++) {
					transp[t][i][j] = Math.exp(transp[t][i][j]);
					z += transp[t][i][j];
				}
			}
			
			for (int i = 0; i < labelSize; i++) {
				for (int j = 0; j < labelSize; j++) {
					transp[t][i][j] /= z;
				}
			}
		}
	}
	
	public static int[][] enumerate(ArrayList<Integer> cross, int labelSize) {
		int times = cross.size();
		int size = (int)Math.pow(labelSize, times);
		int[][] list = new int[size][times];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < times; j++) {
				list[i][j] = (i / (int)Math.pow(labelSize, times - j - 1)) % labelSize;
			}
		}
		return list;
	}
	
	private void visit(Graph graph, int t, int length, int labelSize, HashMap<Integer, Integer> assignment, double[][][] transp, double[][] V, int[][] path) {
		// 初始化根节点的V
		if (graph.parent(t) == -1) {
			for (int i = 0; i < labelSize; i++) {
				mV[t][i] = mTransp[t][0][i];
			}
		}
		
		// 不进行遍历的节点:虚根节点-1,根节点,无dep关系的孤立点
		if (t != -1 && graph.parent(t) != -1 && t != graph.parent(t)) {
			int parent = graph.parent(t);
			for (int i = 0; i < labelSize; i++) {
				double maxP = 0.0;
				
				// cross 的值被固定了
				if (graph.isCross(parent)) {
					//!!! + *
//					double p = V[parent][assignment.get(parent)] * transp[t][assignment.get(parent)][i];
					double p = V[parent][assignment.get(parent)] + transp[t][assignment.get(parent)][i];
					path[t][i] = assignment.get(parent);
					V[t][i] = p;
				}  else {
					for (int j = 0; j < labelSize; j++) { // j 是preLabelIndex
						double p = 	0.0;
						
						//!!!! + *
//						p = V[parent][j] * transp[t][j][i];
						p = V[parent][j] + transp[t][j][i];
						
						if (p > maxP) {
							maxP = p;
							
							path[t][i] = j;
						}
					}
					
					V[t][i] = maxP;
				}
				
			}
		}
		
		for (int child : graph.childrenList(t)) {
			visit(graph, child, length, labelSize, assignment, transp, V, path);
		}
	}
	
	private double getPath(Graph graph, int length, int labelSize, HashMap<Integer, Integer> assignment, double[][][] transp, double[][] V, int[][] path, int[] finalPath) {
		// 以每个叶子为始,向根扫描(有多余计算)
		double max = 1;
		for (int t = 0; t < length; t++) {
			// 所有不是孤立的叶子
			if (graph.isLeaf(t) && graph.parent(t) != t) {
				double maxP = 0;
				int labelIndex = 0;
				for (int i = 0; i < labelSize; i++) {
					if (V[t][i] > maxP) {
						maxP = V[t][i];
						labelIndex = i;
					}
				}
				
				//!!!! + *
//				max *= maxP;
				max += maxP;
				int temp = t;
				
				while (temp != Graph.ROOT) {
					finalPath[temp] = labelIndex;
					labelIndex = path[temp][labelIndex];
					
					temp = graph.parent(temp);
				}
			}
		
			// 孤立点
			if (graph.parent(t) == t) {
				double maxP = 0;
				int labelIndex = 0;
				for (int i = 0; i < labelSize; i++) {
					if (mTransp[t][0][i] > maxP) {
						maxP = mTransp[t][0][i];
						labelIndex = i;
						finalPath[t] = labelIndex;
					}
				}
			}
		}
		
		return max;
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
			Graph graph = instance.graph;
			int length = instance.getTimestampSequence().size();
			
			double[][] beta = new double[length][labelSize];
			double[][] alpha = new double[length][labelSize];
			double[] scale = new double[length];
			
			// init
			for (int i = 0; i < featureSize; i++) {
				ExpF[i] = 0;
			}
			
			// backward
			LinkedList<Integer> queue = new LinkedList<Integer>();
			for (int t = 0; t < length; t++) {
				if (graph.isLeaf(t) && graph.parent(t) != t) {
					queue.add(t);
					
					for (int i = 0; i < labelSize; i++) {
						beta[t][i] = 1;
					}
					scale[t] = labelSize;
					Matrix.scale(beta[t], scale[t]);
				}
			}
			
			short[] betaFlag = new short[length];
			while (!queue.isEmpty()) {
				int t = queue.removeFirst();
				int parent = graph.parent(t);
				
				// 当整句只有一个词时
				if (parent == Graph.ROOT) {
					continue;
				}
				
				model.compute_log_Mi(instance, t, Mi, Vi, true);
				double[] temp = Matrix.copy(beta[t]);
				Matrix.mult(temp, Vi);
				Matrix.matric_mult(labelSize, temp, Mi, Matrix.copy(temp), false);
//				add(beta[parent], beta[parent], temp);
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
					assert false;
					scale[parent] = Matrix.sum(beta[parent]);
					Matrix.scale(beta[parent], scale[parent]);
				}
			}
			
			// forward
			double seq_logli = 0;
			queue.clear();
			queue.add(graph.childrenList(Graph.ROOT).get(0));
			while(!queue.isEmpty()) {
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
					nextAlpha = Matrix.copy(Vi);
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
//						add(nextAlpha, nextAlpha, temp);
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
//							String preLString = mLabelSet.labelByIndex(i).value();
							String lString = model.labels().labelByIndex(i).value();
							
							Feature f = model.features().lookupFeature(fString, null, lString);
							if (f != null) {
									ExpF[f.getIndex()] += nextAlpha[i] * beta[t][i];
							}
						}
					} else if (feature.type == Feature.TYPE_EDGE) {
						for (int i = 0; i < labelSize; i++) {
							for (int j = 0; j < labelSize; j++) {
								String preLString = model.labels().labelByIndex(j).value();
								String lString = model.labels().labelByIndex(i).value();
								
								Feature f = model.features().lookupFeature(fString, preLString, lString);
								if (f != null) {
									ExpF[f.getIndex()] += preAlpha[j] * Vi[i] * Mi[j][i] * beta[t][i];
								}
							}
						}
					}
				}
				
				alpha[t] = Matrix.copy(nextAlpha);
				// scale
				Matrix.scale(alpha[t], scale[t]);
			}
			
			int root = graph.childrenList(Graph.ROOT).get(0);
			model.compute_log_Mi(instance, root, Mi, Vi, true);
			Matrix.mult(beta[root], Vi);
			double Zx = Matrix.sum(beta[root]);
			
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
		}
		
		// 因为要求最小值，将L与G反转
		logli *= -1.0;
		for (int i = 0; i < gravity.length; i++) {
			gravity[i] *= -1.0;
		}
		
		return logli;
	}
}
