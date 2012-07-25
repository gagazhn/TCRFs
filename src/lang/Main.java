/**
 * 
 */
package lang;

/**
 * @author gagazhn
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		TreeCRFTui.main("--train tree ./output/model.hit.G.ser.gz utf-8 ./data/train.G.data ./data/test.G.data".split(" "));
		TreeCRFTui.main("--test ./output/model.COAE.tree.ser.gz utf-8 ./data/~.data".split(" "));
	}

}
