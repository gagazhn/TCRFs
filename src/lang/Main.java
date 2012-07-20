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
//		TreeCRFTui.main("--train chain model.test.ser.gz utf-8 train.G.data test.G.data".split(" "));
		TreeCRFTui.main("--train tree model.test.G.ser.gz utf-8 train.G.data test.G.data".split(" "));
//		TreeCRFTui.main("--train tree model.test.G.ser.gz utf-8 trainG.data testG.data".split(" "));
//		TreeCRFTui.main("--train tree model.liu.test.G.ser.gz utf-8 liu.G.train.data liu.G.test.data".split(" "));
//		TreeCRFTui.main("--test model.test.G.ser.gz utf-8 testG.data".split(" "));
	}

}
