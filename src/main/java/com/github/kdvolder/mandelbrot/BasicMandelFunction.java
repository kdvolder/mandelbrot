package com.github.kdvolder.mandelbrot;

public class BasicMandelFunction {

	public int max_iter = 2000;
	
	public BasicMandelFunction(int max_iter) {
		this.max_iter = max_iter;
	}

	public int mandel(double x, double y) {
		int iter = 0;
		double zr = x;
		double zi = y;
		while (iter < max_iter ) {
			double zr_square = zr*zr;
			double zi_square = zi*zi;
			if (zr_square + zi_square > 4.0) {
				return iter;
			}
			// (zr + zi * i) * (zr + zi * i) + x + y * i
			// zr^2 + 2 zr * zi * i - zi^2 + x + y * i
			// zr^2 - zi^2 + x + (2 zr * zi + y) * i
			double tmp = zr_square - zi_square + x;
			zi = 2 * zr * zi + y;
			zr = tmp;
			iter ++;
		}
		return iter;
	}

	public boolean isMandel(double x, double y) {
		return mandel(x, y) >= max_iter;
	}

}
