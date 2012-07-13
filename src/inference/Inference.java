/**
 * 
 */
package inference;

import gcrfs.Model;

import java.io.Serializable;

import type.FeatureSet;
import type.Instance;
import type.InstanceList;
import type.LabelSet;

/**
 * @author gagazhn
 *
 */
public abstract class Inference implements Serializable{
	private static final long serialVersionUID = -3536997894514191700L;

	public abstract Path exec(Instance instance, FeatureSet feautureSet, LabelSet labelSet);
	
	public class Path {
		public int[] path;
		public double score;
		
		public Path(int[] path, double score) {
			this.path = path;
			this.score = score;
		}
	}
	
	public abstract double computeGravity(Model model, double[] lambda, double[] gravity, int iter);
}
