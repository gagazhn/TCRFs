/**
 * 
 */
package util;

/**
 * @author gagazhn
 *
 */
public class Matrix {
	public static void mult(double[] x, double[] y) {
		for (int i = 0; i < x.length; i++) {
			x[i] = x[i] * y[i];
			
			if (x[i] == 0) {
				System.out.println("hehe");
			}
		}
	}
	
	public static void matric_mult(int size, double[] x, double[][] A, double[] y, boolean T) {
		// for beta
		if (!T) {
			for (int i = 0; i < size; i++) {
				x[i] = 0;
				
				for (int j = 0;  j < size; j++) {
					x[i] += A[i][j] * y[j];
				}
			}
		} else {
			for (int i = 0; i < size; i++) {
				x[i] = 0;
				
				for (int j = 0;  j < size; j++) {
					x[i] += y[j] * A[j][i];
				}
			}
		}
	}
	
	public static double sum(double[] x) {
		double sum = 0;
		
		for (int i = 0; i < x.length; i++) {
			sum += x[i];
		}
		
		return sum;
	}
	
	public static double[] copy(double[] x) {
		double[] copy = new double[x.length];
		
		for (int i = 0; i < x.length; i++) {
			copy[i] = x[i];
		}
		
		return copy;
	}
	
	public static double[] initVector(int n, int value) {
		double[] vector = new double[n];
		for (int i = 0; i < n; i++) {
			vector[i] = value;
		}
		return vector;
	}
	
	public static void scale(double[] x, double scale) {
		for (int i = 0; i < x.length; i++) {
			x[i] = x[i] / scale;
		}
	}
}
