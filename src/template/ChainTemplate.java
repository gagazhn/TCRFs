/**   
 * @Package template
 * @Description: TODO
 * @author gagazhn
 * @date May 10, 2012 3:32:51 PM
 * @version 1.0   
 */
package template;

import gcrfs.Graph;
import type.Feature;
import type.FeatureSequence;
import type.FeatureSet;
import type.FeatureVector;
import type.Instance;
import type.LabelSet;
import type.Timestamp;
import type.TimestampSequence;

/**
 * 特征的处理模板，定义了创建特征的方式
 */
public class ChainTemplate extends AbstractTemplate {
	/**
	 * @param feautureSet
	 * @param labelSet
	 */
	/** 
	 * 将文本进行编译. 只考虑链式特征.
	 * @param instance 测试文本
	 */
	public void compile(Instance instance, FeatureSet featureSet, LabelSet labelSet, boolean create) {
		FeatureSequence featureSequence = new FeatureSequence();
		int length = instance.getTimestampSequence().size();
		instance.graph = new Graph(instance, featureSet, labelSet);
		TimestampSequence timestampSequence = instance.getTimestampSequence();
		for (int i = 0; i < length; i++) {
			Timestamp timestamp = timestampSequence.get(i);
			FeatureVector featureVector = new FeatureVector();
			String lString = timestamp.getLabel();
			for (String fString : timestamp.getFeatures()) {
				if (fString.startsWith("#")) {
					if (i <= 0) {
						continue;
					}
					
					String preLString = timestampSequence.get(i - 1).getLabel();
					if (create) {
						Feature feature = featureSet.putAndGetFeature(fString, preLString, lString, Feature.TYPE_EDGE);
						featureVector.add(feature);
					} else {
						Feature feature = featureSet.lookupFeature(fString, preLString, lString);
						if (feature != null) {
							featureVector.add(feature);
						}
					}
				} else {
					if (create) {
						Feature feature = featureSet.putAndGetFeature(fString, null, lString, Feature.TYPE_STATE);
						featureVector.add(feature);
					} else {
						Feature feature = featureSet.lookupFeature(fString, null, lString);
						if (feature != null) {
							featureVector.add(feature);
						}
					}
				}
			}
			
			// 不考虑图，只是用chain-crfs
			if (i > 0) {
				String preLString = timestampSequence.get(i - 1).getLabel();
				if (create) {
					Feature feature = featureSet.putAndGetFeature("TRANS", preLString, lString, Feature.TYPE_EDGE);
					featureVector.add(feature);
				} else {
					Feature feature = featureSet.lookupFeature("TRANS", preLString, lString);
					if (feature != null) {
						featureVector.add(feature);
					}
				}
			}
			
			instance.graph.addNode(i - 1, i);
			featureSequence.add(featureVector);
		}
		
		instance.setFeatureSequence(featureSequence);
	}
}
