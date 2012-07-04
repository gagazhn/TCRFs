/**   
 * @Package perform
 * @Description: TODO
 * @author gagazhn
 * @date May 15, 2012 2:52:59 PM
 * @version 1.0   
 */
package inference;

import gcrfs.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import type.Feature;
import type.FeatureSequence;
import type.FeatureSet;
import type.FeatureVector;
import type.Instance;
import type.LabelSet;

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
}
