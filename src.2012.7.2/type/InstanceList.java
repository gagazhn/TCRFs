package type;

import java.util.LinkedList;

/**
 * 文本集合，程序处理的主要对象
 * 
 * @author gagazhn
 *
 */
public class InstanceList extends LinkedList<Instance>{
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (Instance instance : this) {
			TimestampSequence featureSequence = instance.getTimestampSequence();
			for (Timestamp featureVector : featureSequence) {
				for (String feature : featureVector.getFeatures()) {
					sb.append(feature + " ");
				}
				sb.append("\n");
			}
			sb.append("\n\n");
		}
		
		return sb.toString();
	}
}
