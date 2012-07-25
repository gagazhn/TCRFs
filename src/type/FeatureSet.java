package type;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * 特征/函数 字典。所有编译的特征都会被记录在字典中。在训练阶段，通过提供
 * 	fString来编译特征，且特征只能在训练阶段被记录，在测试阶段中只能在字典
 * 	中进行查询。另外权重数组lambda[i]被保存在字典中。
 *
 */
public class FeatureSet implements Serializable {
	private static final long serialVersionUID = 2541523872304703462L;

	HashMap<String, Feature> mDict;
	ArrayList<Feature> mList;
	private int mSeek;
	private LabelSet mLabelSet;
	
	/**
	 * 特征权重数组
	 */
	double[] mLambda;
	
	public FeatureSet(LabelSet labelSet) {
		mDict = new HashMap<String, Feature>();
		mList = new ArrayList<Feature>();
		mLabelSet = labelSet;
		mSeek = 0;
	}
	
	/**
	 * @return 字典中实例的数量
	 */
	public int getFeatureSize() {
		return mDict.size();
	}
	
	
	/**
	 * 将特征文本编译为特征，并将其记录在字典里。如果已经存在，将特征的频率属性加1。提供了fString与lString后，
	 * 	特征使组织成‘fString_lString’的形式。训练中使用。
	 * 
	 * @param fString 特征文本
	 * @param lString 标签文本
	 * @param type 特征类型：边/点
	 * @return 特征对象
	 */
	public Feature putAndGetFeature(String fString, String preLString, String lString, int type) {
		// value[preLabel>>label]
		// value[null>>label]
		String query = query(fString, preLString, lString);
		Feature feature = mDict.get(query);
		if (feature == null) {
			Label label = mLabelSet.putAndGetLabel(lString);
			Label preLabel = null;
			if (type == Feature.TYPE_EDGE) {
				if (preLString == null) {
					System.out.println("ERROR on FeatureSet: preLString null.");
					System.exit(0);
				}
				
				preLabel = mLabelSet.putAndGetLabel(preLString);
			}
			feature = new Feature(mSeek++, type, fString, preLabel, label);
			mDict.put(query, feature);
			mList.add(feature);
		}
		
		feature.addFreq();
		return feature;
	}
	
	/**
	 * 查询而不创建特征，测试中使用
	 * 
	 * @param fString 特征文本
	 * @param lString 标签文本
	 * @return 特征对象;null如果特征不存在。
	 */
	public Feature lookupFeature(String fString, String preLString, String lString) {
		// value[preLabel>>label]
		// value[null>>label]
		return mDict.get(query(fString, preLString, lString));
	}
	
	private String query(String fString, String preLString, String lString) {
		return fString + "[" + preLString + ">>" + lString +"]";
	}
	
	
	/**
	 * 索引特征
	 * 
	 * @param index 特征索引号
	 * @return 特征
	 */
	public Feature getFeatureByIndex(int index) {
		return mList.get(index);
	}
	
	public double[] getLambda() {
		if (mLambda == null || mLambda.length != this.getFeatureSize()) {
			mLambda = new double[this.getFeatureSize()];
		}
		
		return mLambda;
	}
	
	// 压缩特征
	public void tight(InstanceList instanceList) {
		HashMap<String, Feature> tempM = new HashMap<String, Feature>();
		ArrayList<Feature> tempL = new ArrayList<Feature>();
		
		int threshold = 4;
		mSeek = 0;
		int labelSize = mLabelSet.getLabelSize();
		
		for (Feature feature : mList) {
			String fString = feature.getValue();
			String preLString = feature.getPreLabel() == null ? null : feature.getPreLabel().value();
			String lString = feature.getLabel().value();
			
			if (feature.type == Feature.TYPE_EDGE) {
				int fre = 0;
				for (int i = 0; i < labelSize; i++) {
					for (int j = 0; j < labelSize; j++) {
						String labelString = mLabelSet.labelByIndex(i).value();
						String preLabelString = mLabelSet.labelByIndex(j).value();
						Feature f = lookupFeature(fString, preLabelString, labelString);
						if (f != null) {
							fre += f.getFreq();
						}
					}
				}
				
				if (fre >= threshold) {
					tempL.add(feature);
					tempM.put(query(fString, preLString, lString), feature);
					feature.index = mSeek++;
				} else {
					feature.index = -1;
				}
			} else {
				int fre = 0;
				for (int i = 0; i < labelSize; i++) {
					String labelString = mLabelSet.labelByIndex(i).value();
					Feature f = lookupFeature(fString, null, labelString);
					if (f != null) {
						fre += f.getFreq();
					}
				}
				
				if (fre >= threshold) {
					tempL.add(feature);
					tempM.put(query(fString, preLString, lString), feature);
					feature.index = mSeek++;
				} else {
					feature.index = -1;
				}
			}
		}
		
		mDict = tempM;
		mList = tempL;
		
		for (Instance instance : instanceList) {
			for (int t = 0; t < instance.getFeatureSequence().size(); t++) {
				FeatureVector featureVector = instance.getFeatureSequence().get(t);
				FeatureVector newVector = new FeatureVector();
				for (Feature feature : featureVector) {
					if (feature.index != -1) {
						newVector.add(feature);
					}
				}
				instance.getFeatureSequence().set(t, newVector);
			}
		}
	}
	
	/**
	 *  打印特征信息，权重大到小前100个
	 */
	public void featureInfo() {
		getLambda();
		System.out.println("Feature Size: " + mDict.size());
		
		double[] weights = new double[100];
		int[] indexs = new int[100];
		int size = 0;
		
		for (int i = 0; i < this.mLambda.length; i++) {
			for (int j = weights.length - 1; j >= 0 && mLambda[i] > weights[j]; j--) {
				if (j == weights.length - 1) {
					weights[j] = mLambda[i];
					indexs[j] = i;
				} else {
					double tw = weights[j];
					int ti = indexs[j];
					weights[j] = mLambda[i];
					indexs[j] = i;
					weights[j + 1] = tw;
					indexs[j + 1] = ti;
				}
			}
			
			size = i;
		}
		
		for (int i = 0; i < indexs.length && i <= size; i++) {
			Feature feature = getFeatureByIndex(indexs[i]);
			System.out.println(feature.getValue() + " " + feature.getLabel().value() + " " + mLambda[indexs[i]]);
//			System.out.println(feature.getLabel().value() + " " + mLambda[indexs[i]]);
		}
	}
}
