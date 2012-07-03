/**
 * 
 */
package gcrfs;

import java.text.DecimalFormat;

import type.FeatureSet;
import type.LabelSet;

/**
 * @author gagazhn
 *
 */
public class Evaluation {
	private FeatureSet mFeatureSet;
	private LabelSet mLabelSet;
	
	private final double BETA = 1;
	
	private DecimalFormat df;
	
	private int mLabelSize;
	private int[] mRaw;
	private int[] mPredict;
	private int[] mHit;
	
	
	public Evaluation(FeatureSet featureSet, LabelSet labelSet) {
		mFeatureSet = featureSet;
		mLabelSet = labelSet;
		
		mLabelSize = mLabelSet.getLabelSize();
		mRaw = new int[mLabelSize];
		mPredict = new int[mLabelSize];
		mHit = new int[mLabelSize];
		
		df = new DecimalFormat("##.00%");
	}
	
	public void statstic(int rawLabel, int predictLabel) {
		mRaw[rawLabel]++;
		mPredict[predictLabel]++;
		
		if (rawLabel == predictLabel) {
			mHit[predictLabel]++;
		}
	}
	
	public void clear() {
		for (int i = 0; i < mLabelSize; i++) {
			mRaw[i] = 0;
			mPredict[i] = 0;
			mHit[i] = 0;
		}
	}
	
	public void info() {
		System.out.println("===========================================================================");
		System.out.println();
		System.out.println("Label\t\tHIT\tRAW\tPRED\t\tPRE%\tREC%\tF%");
		for (int i = 0; i < mLabelSize; i++	) {
			String lString = mLabelSet.labelByIndex(i).value();
			String gap = lString.length() >= 8 ? "\t" : "\t\t";
			double p = 1.0 * mHit[i] / mPredict[i];
			double r = 1.0 * mHit[i] / mRaw[i];
			double f = (BETA * BETA + 1) * p * r / (BETA * p + r);
			System.out.println(lString + gap + mHit[i] + "\t" + mRaw[i] + "\t" + mPredict[i] + "\t\t" + df.format(p) + "\t" + df.format(r) + "\t" + df.format(f));
		}
		System.out.println();
		System.out.println("===========================================================================");
		System.out.println();
	}
}
