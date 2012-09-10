/**   
 * @Package type
 * @Description: TODO
 * @author gagazhn
 * @date May 9, 2012 3:55:30 PM
 * @version 1.0   
 */
package type;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * 标签 字典。所有编译的标签都会被记录在字典中。在训练阶段，通过提供
 * 	lString来编译特征，且标签只能在训练阶段被记录，在测试阶段中只能在字典
 * 	中进行查询。
 *
 * @author gagazhn
 */
public class LabelSet implements Serializable {
	private static final long serialVersionUID = -3065254086737670769L;

	private HashMap<String, Label> mDict;
	private ArrayList<Label> mList;
	private int mSeek;
	
	public LabelSet() {
		mDict = new HashMap<String, Label>();
		mList = new ArrayList<Label>();
		mSeek = 0;
	}
	
	public Label putAndGetLabel(String lString) {
		Label label = mDict.get(lString);
		if (label == null) {
			label = new Label(mSeek++, lString);
			mDict.put(lString, label);
			mList.add(label);
		}
		
		label.addFreq();
		return label;
	}
	
	public int labelIndex(String lString) {
		Label label = mDict.get(lString);
		if (label != null) {
			return label.getIndex();
		} else {
			return -1;
		}
	}
	
	public int getLabelSize() {
		return mDict.size();
	}
	
	public Label labelByIndex(int index) {
		return mList.get(index);
	}
	
	public ArrayList<Label> getLabelList() {
		return mList;
	}
}
