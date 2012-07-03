package type;

import java.io.Serializable;

/**
 * 特征/函数 对象，即CRFs模型中的lumbda_k * f(s', s, o)，包括边特征与点特征。
 * 	更多特征描述参见FeatureSet类
 *
 * @author gagazhn
 *
 */
public class Feature implements Serializable{
	private static final long serialVersionUID = 6932071868234821500L;
	
	public static final int TYPE_EDGE = 0;
	public static final int TYPE_STATE = 1;
	
	public final int type;
	
	private String value;
	private Label label;
	private Label preLabel;
	private int freq;
	
	int index;
	
	/**
	 * @param index 特征的索引号，对应与权重数组lambda[i]中的i
	 * @param type 特征类型:TYPE_EDGE边类型，TYPE_STATE点类型
	 * @param fString 特征文本
	 * @param label 标签
	 */
	public Feature(int index, int type, String fString, Label preLabel, Label label) {
		this.index = index;
		this.preLabel = preLabel;
		this.label = label;
		this.type = type;
		value = fString;
		freq = 0;
	}
	
	public void setPreLabel(Label label) {
		if (type == TYPE_EDGE) {
			this.preLabel = label;
		} else {
			System.out.println("STATE hasn't preLabel");
		}
	}
	
	public Label getPreLabel() {
		return this.preLabel;
	}
	
	public String getValue() {
		return value;
	}
	
	public Label getLabel() {
		return label;
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
		return value + "<" + label.value() + ">";
	}
}
