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
	
	private boolean exactly;
	
	
	public Evaluation(FeatureSet featureSet, LabelSet labelSet, boolean exactly) {
		mFeatureSet = featureSet;
		mLabelSet = labelSet;
		
		this.exactly = exactly;
		
		mLabelSize = mLabelSet.getLabelSize();
		mRaw = new int[mLabelSize];
		mPredict = new int[mLabelSize];
		mHit = new int[mLabelSize];
		
		df = new DecimalFormat("##.00%");
	}
	
/*	public void statstic(int rawLabel, int predictLabel) {
		mRaw[rawLabel]++;
		mPredict[predictLabel]++;
		
		if (rawLabel == predictLabel) {
			mHit[predictLabel]++;
		}
	}*/
	
	public void staticic(int[] answer, int[] guess) {
		if (answer.length != guess.length) {
			System.err.println("answer's size != guess's size");
			System.exit(0);
		}
		
		if (exactly) {
			int answerHead = -1;
			int guessHead = -1;
			int a = -1;
			int b = -1;
			for (int t = 0; t < answer.length; t++) {
				if (t == 0 || answer[t - 1] != answer[t]) {
					answerHead = t;
				}
				
				if (t == 0 || guess[t - 1] != guess[t]) {
					guessHead = t;
				}
				
				if (t >= answer.length - 1 || answer[t + 1] != answer[t]) {
					mRaw[answer[t]]++;
					a = answer[t];
				}
				
				if (t >= answer.length - 1 || guess[t + 1] != guess[t] ) {
					mPredict[guess[t]]++;
					b = guess[t];
				}
				
				if ((t >= answer.length - 1 || answer[t + 1] != answer[t]) && answerHead == guessHead && a == b) {
					mHit[answer[t]]++;
				}
			}
		} else {
			for (int t = 0; t < answer.length; t++) {
				mRaw[answer[t]]++;
				mPredict[guess[t]]++;
				
				if (answer[t] == guess[t]) {
					mHit[answer[t]]++;
				}	
			}
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
		System.err.println("===========================================================================");
		System.err.println();
		System.err.println("Label\t\tHIT\tRAW\tPRED\t\tPRE%\tREC%\tF%");
		for (int i = 0; i < mLabelSize; i++	) {
			String lString = mLabelSet.labelByIndex(i).value();
			String gap = lString.length() >= 8 ? "\t" : "\t\t";
			double p = 1.0 * mHit[i] / mPredict[i];
			double r = 1.0 * mHit[i] / mRaw[i];
			double f = (BETA * BETA + 1) * p * r / (BETA * p + r);
			System.err.println(lString + gap + mHit[i] + "\t" + mRaw[i] + "\t" + mPredict[i] + "\t\t" + df.format(p) + "\t" + df.format(r) + "\t" + df.format(f));
		}
		System.err.println();
		System.err.println("===========================================================================");
		System.err.println();
	}
}
