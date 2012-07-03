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
		Model model = (Model)TreeCRFTui.readGzippedObject(new File("./model.G.ser.gz"));
		info(model.features());
	}
	
	public static void info(FeatureSet featureSet) {
		featureSet.getLambda();
		System.out.println("Feature Size: " + featureSet.mDict.size());
		
		int ll = 4000;
		
		double[] weights = new double[ll];
		int[] indexs = new int[ll];
		int size = 0;
		
		for (int i = 0; i < featureSet.mLambda.length; i++) {
			for (int j = weights.length - 1; j >= 0 && Math.abs(featureSet.mLambda[i]) > Math.abs(weights[j]); j--) {
				if (j == weights.length - 1) {
					weights[j] = featureSet.mLambda[i];
					indexs[j] = i;
				} else {
					double tw = weights[j];
					int ti = indexs[j];
					weights[j] = featureSet.mLambda[i];
					indexs[j] = i;
					weights[j + 1] = tw;
					indexs[j + 1] = ti;
				}
			}
			
			size = i;
		}
		
		for (int i = 0; i < indexs.length && i <= size; i++) {
			Feature feature = featureSet.getFeatureByIndex(indexs[i]);
			System.out.println(feature.getValue() + " " + feature.getFreq() + " " + feature.getLabel().value() + " " + featureSet.mLambda[indexs[i]]);
//			System.out.println(feature.getLabel().value() + " " + mLambda[indexs[i]]);
		}
		
		System.out.println();
		for (int i = 0; i < featureSet.mLambda.length; i++) {
			if (featureSet.getFeatureByIndex(i).type == Feature.TYPE_EDGE) {
				Feature feature = featureSet.getFeatureByIndex(i);
				System.out.println(feature.getValue() + " " + feature.getPreLabel().value() + " " + feature.getLabel().value() + " " + featureSet.mLambda[i]);
			}
		}
	}
}
