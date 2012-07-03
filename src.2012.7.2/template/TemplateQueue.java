/**   
 * @Package template
 * @Description: TODO
 * @author gagazhn
 * @date May 10, 2012 4:08:41 PM
 * @version 1.0   
 */
package template;

import java.util.ArrayList;

import type.FeatureSet;
import type.Instance;
import type.InstanceList;
import type.LabelSet;

/**
 * @Description: 
 *
 */
public class TemplateQueue extends ArrayList<AbstractTemplate> {

	public void compile(InstanceList instanceList, FeatureSet featureSet, LabelSet labelSet, boolean create) {
		for (Instance instance : instanceList) {
			for (AbstractTemplate template : this) {
				template.compile(instance, featureSet, labelSet, create);
			}
		}
	}
}
