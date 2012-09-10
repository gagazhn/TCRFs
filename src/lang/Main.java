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
		TreeCRFTui.main("--train tree ./output/model.COAE.PURE.tree.ser.gz utf-8 ./data/COAE.PURE.train.data ./data/COAE.PURE.test.data".split(" "));
	}

}
