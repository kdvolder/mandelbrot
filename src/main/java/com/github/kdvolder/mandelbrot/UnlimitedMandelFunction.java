package com.github.kdvolder.mandelbrot;

/**
 * Mandelfunction without a 'max_iter'. Instead it detects
 * whether the iteration hits a cycle.
 */
public class UnlimitedMandelFunction {

	private final int LIMIT; // = 1 << 20;

	public static final int NO_ESCAPE = -1;
	
	private int max_no_escape_iter = 0;
	public int min_iter = Integer.MAX_VALUE;
	private int max_iter = 0;
	
	public UnlimitedMandelFunction(int hardIterLimit) {
		this.LIMIT = hardIterLimit;
	}

	public int mandel(double x, double y) {
//		System.out.println("----------------");
		int iter = 0;
		double zr = x;
		double zi = y;
		double cycle_r = 0;
		double cycle_i = 0;
		long deadline = 1;
//		System.out.println("deadline = "+deadline);
//		double shortest = 4.0;
		while (zr != cycle_r || zi != cycle_i) {
			double zr_square = zr*zr;
			double zi_square = zi*zi;
			double len_squared = zr_square + zi_square;
			if (len_squared > 4.0) {
				min_iter = Math.min(min_iter, iter);
				max_iter = Math.max(max_iter, iter);
				return iter;
//			} else if (len_squared < shortest) {
//				//System.out.println(len_squared);
//				cycle_i = zi;
//				cycle_r = zr;
//				shortest = len_squared;
			}
			if (iter>=deadline) {
				deadline *= 2;
				//System.out.println("deadline = "+deadline);
				cycle_i = zi;
				cycle_r = zr;
//				shortest = len_squared;
			}
			// (zr + zi * i) * (zr + zi * i) + x + y * i
			// zr^2 + 2 zr * zi * i - zi^2 + x + y * i
			// zr^2 - zi^2 + x + (2 zr * zi + y) * i
			double tmp = zr_square - zi_square + x;
			zi = 2 * zr * zi + y;
			zr = tmp;
			iter ++;
			if (iter> LIMIT) { 
//				System.out.println("HARD x = "+x +" y = "+y);
				max_no_escape_iter = Math.max(max_no_escape_iter, iter);
				return NO_ESCAPE;
			}
		}
		max_no_escape_iter = Math.max(max_no_escape_iter, iter);
		return NO_ESCAPE;
	}

	public void startSession() {
		max_no_escape_iter = 0;
		min_iter = Integer.MAX_VALUE;
		max_iter = 0;
	}
	public void endSession() {
	}

	public String sessionStats() {
		return "mnei = "+max_no_escape_iter +" min="+min_iter+" max="+max_iter;
	}

}
