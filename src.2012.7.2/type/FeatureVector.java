/**   
 * @Package type
 * @Description: TODO
 * @author gagazhn
 * @date May 10, 2012 4:19:05 PM
 * @version 1.0   
 */
package type;

import java.util.ArrayList;

/**
 * @Description: 
 *
 */
public class FeatureVector extends ArrayList<Feature>{
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Feature feature : this) {
			sb.append(feature + " ");
		}
		
		return sb.toString();
	}
}
