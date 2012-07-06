/**   
 * @Package perform
 * @Description: TODO
 * @author gagazhn
 * @date May 15, 2012 2:52:59 PM
 * @version 1.0   
 */
package inference;

import gcrfs.Evaluation;

import java.util.LinkedList;

import type.Feature;
import type.FeatureSet;
import type.FeatureVector;
import type.Instance;
import type.InstanceList;
import type.Label;
import type.LabelSet;

/**
 * Viterbi算法
 * 
 * @author gagazhn
 */
public class Viterbi extends Inference {
	public Path exec(Instance instance, FeatureSet featureSet,
			LabelSet labelSet) {
		double[] lambda = featureSet.getLambda();
		int labelSize = labelSet.getLabelSize();
		int length = instance.getFeatureSequence().size();
		int[] finalPath = new int[length];
		int[][] path = new int[labelSize][length];
		double[][] V = new double[labelSize][length];
		double[][] transp = new double[labelSize][labelSize];

		// 初始化V[][]
		for (Feature feature : instance.getFeatureSequence().get(0)) {
			String fString = feature.getValue();

			if (feature.type == Feature.TYPE_EDGE) {
				for (int i = 0; i < labelSize; i++) {
					for (int j = 0; j < labelSize; j++) {
						String preLString = labelSet.labelByIndex(j).value();
						String lString = labelSet.labelByIndex(i).value();
						Feature f = featureSet.lookupFeature(fString, preLString, lString);
						if (f != null) {
							int preLabelIndex = f.getPreLabel().getIndex();
							int labelIndex = f.getLabel().getIndex();
							int featureIndex = f.getIndex();
							transp[preLabelIndex][labelIndex] += lambda[featureIndex];
						}
					}
				}
			} else {
				for (int i = 0; i < labelSize; i++) {
					String lString = labelSet.labelByIndex(i).value();
					Feature f = featureSet.lookupFeature(fString, null, lString);
					if (f != null) {
						int labelIndex = f.getLabel().getIndex();
						int featureIndex = f.getIndex();
						for (int j = 0; j < labelSize; j++) {
							transp[j][labelIndex] += lambda[featureIndex];
						}
					}
				}
			}
		}
		//!!
		norm(transp);
		for (int i = 0; i < labelSize; i++) {
			V[i][0] += transp[0][i];
			
			path[i][0] = i;
		}
		
		for (int t = 1; t < path[0].length; t++) {
			FeatureVector featureVector = instance.getFeatureSequence().get(t);
			
			for (int i = 0; i < labelSize; i++) {
				for (int j = 0; j < labelSize; j++) {
					transp[i][j] = 0;
				}
			}
			for (Feature feature : featureVector) {
				String fString = feature.getValue();

				if (feature.type == Feature.TYPE_EDGE) {
					for (int i = 0; i < labelSize; i++) {
						for (int j = 0; j < labelSize; j++) {
							String preLString = labelSet.labelByIndex(j).value();
							String lString = labelSet.labelByIndex(i).value();
							Feature f = featureSet.lookupFeature(fString, preLString, lString);
							if (f != null) {
								int preLabelIndex = f.getPreLabel().getIndex();
								int labelIndex = f.getLabel().getIndex();
								int featureIndex = f.getIndex();
								transp[preLabelIndex][labelIndex] += lambda[featureIndex];
							}
						}
					}
				} else {
					for (int i = 0; i < labelSize; i++) {
						String lString = labelSet.labelByIndex(i).value();
						Feature f = featureSet.lookupFeature(fString, null, lString);
						if (f != null) {
							int labelIndex = f.getLabel().getIndex();
							int featureIndex = f.getIndex();
							for (int j = 0; j < labelSize; j++) {
								transp[j][labelIndex] += lambda[featureIndex];
							}
						}
					}
				}
				
			}
			//!!
			norm(transp);
			
			for (int i = 0; i < labelSize; i++) {
				double maxProbe = 0.0;
				for (int j = 0; j < labelSize; j++) {
					double p = V[j][t - 1] * transp[j][i];

					if (p > maxProbe) {
						maxProbe = p;

						path[i][t] = j;
					}
				}

				V[i][t] = maxProbe;
			}
		}

		int maxI = 0;
		double maxP = 0.0;
		for (int i = 0; i < V.length; i++) {
			double p = V[i][instance.getTimestampSequence().size() - 1];
			if (maxP < p) {
				maxP = p;
				maxI = i;
			}
		}

		int r = maxI;
		finalPath[length - 1] = r;
		for (int i = path[0].length - 1; i > 0; i--) {
			finalPath[i - 1] = path[r][i];
			r = path[r][i];
		}

		return new Path(finalPath, maxP);
	}

	public void printPath() {
		// for (int i = 0; i < list.size(); i++) {
		// int labelIndex = list.get(i);
		// Label label = labelSet.labelByIndex(labelIndex);
		// String lString = instance.getTimestampSequence().get(i).getLabel();
		// String fString =
		// instance.getTimestampSequence().get(i).getFeatures().toString();
		// eval.statstic(labelSet.labelIndex(lString), labelIndex);
		// // System.out.println(fString + " " + lString + " " + label.value());
		// System.out.println(lString + " " + label.value() +
		// (lString.equals(label.value()) ? "" : " $$$$$$"));
		// }
		// System.out.println();
	}
	
//	private static void exp(double[][] transp) {
//		
//	}

	private static void norm(double[][] transp) {
		int row = transp.length;
		int col = transp[0].length;
		double z = 0.0;

		for (int i = 0; i < row; i++) {
			for (int j = 0; j < row; j++) {
				transp[i][j] = Math.exp(transp[i][j]);
				z += transp[i][j];
			}
		}

		for (int i = 0; i < row; i++) {
			for (int j = 0; j < row; j++) {
				transp[i][j] /= z;
			}
		}
	}
}
