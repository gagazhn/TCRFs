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
		
		for (int t = 0; t < length; t++) {
			Timestamp timestamp = timestampSequence.get(t);
			FeatureVector featureVector = new FeatureVector();
			int parent = timestamp.getParent();
			String lString = timestamp.getLabel();
			for (String fString : timestamp.getFeatures()) {
				if (fString.startsWith("//")) {
					continue;
				} else if (fString.startsWith("#") && parent != t) {
					if (parent < 0) {
						continue;
					}
					
					//!!
					if (timestampSequence.size() <= parent) {
						System.out.println("hehe");
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
				} else if (!fString.startsWith("#")) {
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
			if (parent != Graph.ROOT && parent != t) {
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
			
			instance.graph.addNode(parent, t);
			featureSequence.add(featureVector);
		}
		
		instance.setFeatureSequence(featureSequence);
	}
}
