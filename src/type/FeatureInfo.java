/**
 * 
 */
package type;

import gcrfs.Model;

import java.io.File;


import lang.TreeCRFTui;

/**
 * @author gagazhn
 *
 */
public class FeatureInfo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Model model = (Model)TreeCRFTui.readGzippedObject(new File("./output/model.COAE.tree.ser.gz"));
		info(model.features());
	}
	
	public static void info(FeatureSet featureSet) {
		double[] lambda = featureSet.getLambda();
		int featureSize = featureSet.getFeatureSize();
		System.err.println("Feature Size: " + featureSize);
		
		
		
		int L = 1000;
		
		double[] weights = new double[L];
		int[] indexs = new int[L];
		int size = 0;
		
		// 冒泡排序，前1000 权重×Freq
		for (int i = 0; i < featureSize; i++) {
			Feature featrue = featureSet.getFeatureByIndex(i);
			
			for (int j = weights.length - 1; j >= 0 && Math.abs(lambda[i] * featrue.getFreq()) > Math.abs(weights[j]); j--) {
				if (j == weights.length - 1) {
					weights[j] = lambda[i] * featrue.getFreq();
					indexs[j] = i;
				} else {
					double tw = weights[j];
					int ti = indexs[j];
					weights[j] = lambda[i] * featrue.getFreq();
					indexs[j] = i;
					weights[j + 1] = tw;
					indexs[j + 1] = ti;
				}
			}
			
			size = i;
		}
		
		
		// 打印
		for (int i = 0; i < L && i <= size; i++) {
			Feature feature = featureSet.getFeatureByIndex(indexs[i]);
			String preLString = feature.getPreLabel() == null ? "ANY" : feature.getLabel().value();
			String lString = feature.getLabel().value();
			System.err.println(feature.getValue() + " " + feature.getFreq() + " " + preLString + "=>" + lString + " " + featureSet.mLambda[indexs[i]]);
		}
		System.err.println();
		
		for (int i = 0; i < featureSize; i++) {
			Feature feature = featureSet.getFeatureByIndex(i);
			if (feature.type == Feature.TYPE_EDGE && feature.getValue().equals("TRANS")) {
				System.err.println(feature.getValue() + " " + feature.getPreLabel().value() + "=>" + feature.getLabel().value() + " " + featureSet.mLambda[i]);
			}
		}
	}
}
