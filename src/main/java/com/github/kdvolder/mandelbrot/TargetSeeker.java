package com.github.kdvolder.mandelbrot;

import java.util.Random;

/**
 * Seeks for a interesting region of space within a given bounds.
 *
 * @author kdvolder
 *
 */
public class TargetSeeker {
	
	private final Random rnd = new Random();
	
	private final Bounds initialBounds = new Bounds();
	
	/**
	 * Search for a interesting region of space with a given width.
	 * @param mandel 
	 */
	public Bounds find(double desiredWidth, MandelFunction mandel) {
		mandel.max_iter = 1;
		int required_streak_length = 2000;
		
		double cand_x = 0, cand_y = 0;
		Bounds bounds = initialBounds;
		while (bounds.getWidth() > desiredWidth) {
			//Repeatedly pick an 'interesting' point and zoom towards it.
			
			//A point is interesting if it has a high iteration count *and* escapes.
			//What constitutes a high iteration count is relative w.r.t to the current
			//search bounds. We only consider a iter value high if it is 'hard' to
			//find another point within that space that has a higher count.
			int i = 0;
			while (i++ < required_streak_length) {
				double x = bounds.lowx + rnd.nextDouble() * bounds.getWidth();
				double y = bounds.lowy + rnd.nextDouble() * bounds.getHeigth();
				int m = mandel.mandel(x, y);
				if (m == mandel.max_iter-1) {
					cand_x = x;
					cand_y = y;
					i = 0; // reset streak counter
					mandel.max_iter ++; // search for higher max iter
//					System.out.println(mandel.max_iter);
				}
			}
			bounds = bounds.zoomAround(cand_x, cand_y, 2.0);
			System.out.println(bounds + " w = "+ bounds.getWidth());
		}
		return bounds.zoomToDimension(desiredWidth, desiredWidth * Bounds.full_bounds.getHeigth() / Bounds.full_bounds.getWidth());
	}

	/**
	 * Search for a point that takes exactly the given number of iterations to escape.
	 */
	public Point findPointWithValue(int wanted_iterations) {
		double x0, y0, x1, y1;
		
		MandelFunction mandel = new MandelFunction(wanted_iterations+1);
		Bounds bounds = initialBounds;
		//First pick a random point that has lower mandel value than desirable.
		x0 = rnd.nextDouble() * bounds.getWidth() + bounds.lowx;
		y0 = rnd.nextDouble() * bounds.getHeigth() + bounds.lowy;
		int mandel0;
		do {
			mandel0 = mandel.mandel(x0, y0);
		} while (mandel0 > wanted_iterations);
		if (mandel0==wanted_iterations) {
			//done already!
			return new Point(x0, y0);
		}
		
		//Now pick a second random point that needs at least the wanted number of iterations
		int mandel1;
		do {
			x1 = rnd.nextDouble() * bounds.getWidth() + bounds.lowx;
			y1 = rnd.nextDouble() * bounds.getHeigth() + bounds.lowy;
			mandel1 = mandel.mandel(x1, y1);
		} while (mandel1<wanted_iterations);
		
		if (mandel1==wanted_iterations) {
			//done already!
			return new Point(x1, y1);
		}
		
		// At this point we know that
		// mandel(x0, y0) < wanted_iterations
		// mandel(x1, y1) > wanted_iterations
		// Number of iterations don't skip steps therefore, somewhere in between these two points must exist some points
		// that have exactly the wanted number of iterations.
		
		double xmid, ymid;
		int mandel_mid;
		double last_epsilon = 0.0;
		double epsilon = 0.0;
		do {
			xmid = (x0 + x1) / 2;
			ymid = (y0 + y1) / 2;
			mandel_mid = mandel.mandel(xmid, ymid);
			if (mandel_mid<wanted_iterations) {
				x0 = xmid; y0 = ymid;
			} else if (mandel_mid > wanted_iterations) {
				x1 = xmid; y1 = ymid;
			}
			last_epsilon = epsilon;
			epsilon = Math.max(Math.abs(x0-x1), Math.abs(y0-y1));
		} while (mandel_mid!=wanted_iterations && last_epsilon!=epsilon);
		if (mandel_mid!=wanted_iterations) {
			//bailed out because, presumably the double precision is too small to find point of desired iterations
			findPointWithValue(wanted_iterations);
		}
		return new Point(xmid, ymid);
	}
}
