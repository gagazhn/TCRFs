/**
 * 
 */
package template;

import gcrfs.Graph;
import type.Feature;
import type.FeatureSequence;
import type.FeatureSet;
import type.FeatureVector;
import type.Instance;
import type.Label;
import type.LabelSet;
import type.Timestamp;
import type.TimestampSequence;

/**
 * @author gagazhn
 *
 */
public class TreeTemplate extends AbstractTemplate {
	/** 
	 * 将测试文本进行编译，考虑依存句法的树形属性与特征。
	 * 	枚举，用以估计每一个时间点中的样例可能的Label。
	 * @param instance 测试文本
	 */
	public void compile(Instance instance, FeatureSet featureSet, LabelSet labelSet, boolean create) {
		FeatureSequence featureSequence = new FeatureSequence();
		TimestampSequence timestampSequence = instance.getTimestampSequence();
		int length = timestampSequence.size();
		instance.graph = new Graph(instance, featureSet, labelSet);
		
		for (int i = 0; i < length; i++) {
			Timestamp timestamp = timestampSequence.get(i);
			FeatureVector featureVector = new FeatureVector();
			int parent = timestamp.getParent();
			String lString = timestamp.getLabel();
			for (String fString : timestamp.getFeatures()) {
				if (fString.startsWith("//")) {
					
				} else if (fString.startsWith("#")) {
					if (parent < 0) {
						continue;
					}
					
					String preLString = timestampSequence.get(parent).getLabel();
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
			
			//!!! parent == -1
			if (parent >= 0) {
				String preLString = timestampSequence.get(parent).getLabel();
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
			
			instance.graph.addNode(parent, i);
			featureSequence.add(featureVector);
		}
		
		instance.setFeatureSequence(featureSequence);
	}
}