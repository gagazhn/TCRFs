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
//		TreeCRFTui.main("--train chain model.test.ser.gz utf-8 test.data test.data".split(" "));
		TreeCRFTui.main("--train tree model.test.ser.gz utf-8 trainG.data testG.data".split(" "));
	}

}