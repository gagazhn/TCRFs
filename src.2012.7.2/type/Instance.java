package type;

import gcrfs.Graph;

/**
 * Instance表示文本中的一句话
 * 
 * @author gagazhn
 *
 */
public class Instance {
	private TimestampSequence timestampSequence;
	private FeatureSequence featureSequence;
	
	public Graph graph;
	
	public void setFeatureSequence(FeatureSequence featureSequence) {
		this.featureSequence = featureSequence;
	}
	
	public FeatureSequence getFeatureSequence() {
		return this.featureSequence;
	}

	/**
	 * @return 时间点序列
	 */
	public TimestampSequence getTimestampSequence() {
		return timestampSequence;
	}
	
	public void setTimestampSequence(TimestampSequence timestampSequence) {
		this.timestampSequence = timestampSequence;
	}
	
	public String toString() {
		return timestampSequence.toString();
	}
}
