package lang;

import java.util.ArrayList;

import optimization.LBFGS;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final int LENGTH = 2;
		
		int[] iprint = {0, 0};
		int[] iflag = {0};
		double[] dragond = new double[LENGTH];
		
		double[] x = new double[LENGTH];
		double[] g = new double[LENGTH];
		double y = 0.0;
		
		try {
			int ITER = 1000;
			for (int i = 0; i < ITER; i++) {
				System.out.println("ITER: " + i);
				y = y(x);
				g(x, g);
				LBFGS.lbfgs(x.length, 5, x, y, g, false, dragond, iprint, 1.0e-5, 1.0e-160, iflag);
				
				if (iflag[0] < 0) {
					System.out.println("ERROR with code: " + iflag[0]);
					return;
				} else if (iflag[0] == 0) {
					System.out.println("Finish.");
					System.out.println("Result: " +  x[0] + " " + x[1]);
					break;
				} else if (iflag[0] == 1) {

					System.out.println(y);
				} else if (iflag[0] == 2) {
					System.out.println("dragond?");
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void g(double[] x, double[] g) {
		for (int i = 0; i < x.length; i++) {
			g[i] = 2 * x[i] - 100;
		}
	}
	
	private static double y(double[] x) {
		double y = 0.0;
		for (int i = 0; i < x.length; i++) {
			y = Math.pow((50 - x[i]), 2);
		}
		
		return y;
	}
}
