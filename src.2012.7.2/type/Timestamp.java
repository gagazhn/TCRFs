package type;

import java.util.ArrayList;

/**
 * 时间点，即在t时刻的输入
 */
public class Timestamp {
	private ArrayList<String> features;
	private String label;
	private int parent;
	
	public Timestamp(int parent, String label) {
		this.features = new ArrayList<String>();
		this.label = label;
		this.parent = parent;
	}
	
	public ArrayList<String> getFeatures() {
		return features;
	}
	public void setFeatures(ArrayList<String> features) {
		this.features = features;
	}
	public String getLabel() {
		return label;
	}
	public int getParent() {
		return parent;
	}
	
	public String toString() {
		return "@" + parent + " " + features.toString() + ":" + this.label;
	}
}
