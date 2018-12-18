package com.github.kdvolder.mandelbrot;

/**
 * Computes 'mandelbrot' function. Maintains some internal state to
 * track the number of pixels that are exactly hitting the maximum iter
 * value and still escape. This is used to dynamically adjust the
 * max_iter value to keep this number low.
 * 
 * @author Kris De Volder
 */
public class OldMandelFunction {

	private static final int NOT_ESCAPED = 0;
	private int iter_bump_limit;
	private int max_iter;
	private int max_iter_bump;
	int expected_iter_bumps = 0;

	
	public OldMandelFunction(int max_iter, int iter_bump_limit) {
		this.max_iter = max_iter;
		this.iter_bump_limit = iter_bump_limit;
	}

	public void startSession() {
		expected_iter_bumps = max_iter_bump;
		max_iter_bump = 0;
	}

	public void endSession() {
		int reduce_iter_threshold = iter_bump_limit/2;
		while (max_iter_bump < reduce_iter_threshold) {
			max_iter--;
			reduce_iter_threshold /=  2;
		}
	}

	public int mandel(double x, double y) {
		int iter = 0;
		double zr = x;
		double zi = y;
		while (iter < max_iter ) {
			double zr_square = zr*zr;
			double zi_square = zi*zi;
			if (zr_square + zi_square > 4.0) {
				if (iter + 1 >= max_iter) {
					if (++max_iter_bump%iter_bump_limit==0) { 
						//System.out.println("max_iter = "+max_iter);
						max_iter++;
					}
				}
				return iter; //escaped at this iteration
			}
			// (zr + zi * i) * (zr + zi * i) + x + y * i
			// zr^2 + 2 zr * zi * i - zi^2 + x + y * i
			// zr^2 - zi^2 + x + (2 zr * zi + y) * i
			double tmp = zr_square - zi_square + x;
			zi = 2 * zr * zi + y;
			zr = tmp;
			iter ++;
		}
		return NOT_ESCAPED;
	}

	public String sessionStats() {
		return "mib = "+max_iter_bump +
		" mit = "+max_iter;
	}
}
