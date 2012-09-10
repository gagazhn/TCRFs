/**   
 * @Package type
 * @Description: TODO
 * @author gagazhn
 * @date May 15, 2012 9:35:24 AM
 * @version 1.0   
 */
package type;

import java.io.Serializable;

/**
 * 标签，即模型需要进行学习与预测的对象
 */
public class Label implements Serializable {
	private static final long serialVersionUID = -4955971313089246713L;
	
	private String value;
	private int freq;
	private int index;
	
	public Label(int index, String lString) {
		this.index = index;
		this.value = lString;
		freq = 0;
	}
	
	public String value() {
		return value;
	}
	
	public void addFreq() {
		freq++;
	}
	
	public int getFreq() {
		return freq;
	}

	public int getIndex() {
		return index;
	}
	
	public String toString() {
		return value + ":" + freq;
	}
}
