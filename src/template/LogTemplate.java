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
public class LogTemplate extends AbstractTemplate {
	/** 
	 * 将文本进行编译. 只考虑链式特征.
	 * @param instance 测试文本
	 */
	public void compile(Instance instance, FeatureSet featureSet, LabelSet labelSet, boolean create) {
		FeatureSequence featureSequence = new FeatureSequence();
		int length = instance.getTimestampSequence().size();
		instance.graph = new Graph(instance, featureSet, labelSet);
		TimestampSequence timestampSequence = instance.getTimestampSequence();
		for (int t = 0; t < length; t++) {
			Timestamp timestamp = timestampSequence.get(t);
			FeatureVector featureVector = new FeatureVector();
			String lString = timestamp.getLabel();
			for (String fString : timestamp.getFeatures()) {
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
			
			instance.graph.addNode(t - 1, t);
			featureSequence.add(featureVector);
		}
		
		instance.setFeatureSequence(featureSequence);
	}
}
