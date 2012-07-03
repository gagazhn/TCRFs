/**
 * 
 */
package template;

import java.io.Serializable;

import type.FeatureSet;
import type.Instance;
import type.LabelSet;

/**
 * @author gagazhn
 *
 */
public abstract class AbstractTemplate implements Serializable {
	private static final long serialVersionUID = -1952989856229445569L;
	
	public abstract void compile(Instance instance, FeatureSet featureSet, LabelSet labelSet, boolean create);
}
