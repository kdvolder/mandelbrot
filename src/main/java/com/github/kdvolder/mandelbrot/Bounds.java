package com.github.kdvolder.mandelbrot;

public class Bounds {
	
	public static Bounds withCenterAndWidth(Point center, double width) {
		double aspect = full_bounds.getAspect();
		double heigth = width / aspect;
		Bounds bounds = new Bounds();
		bounds.lowx = center.x - width / 2;
		bounds.lowy = center.y - heigth / 2;
		bounds.highx = bounds.lowx + width;
		bounds.highy = bounds.lowy + heigth;
		return bounds;
	}
	
	public static Bounds full_bounds = new Bounds();
	
	double lowy = -1.25;
	double highy = +1.25;
	double lowx = -3.0;
	double highx = lowx + (highy-lowy)*1920/1080;
	
	public Bounds flyTowards(double centerx, double centery, double aspect) {
		double heigth = (highy - lowy) * 0.99;
		double width = heigth * aspect;
		
		Bounds target = new Bounds();
		target.lowx = centerx - width / 2;
		target.highx = target.lowx + width;
		target.lowy = centery - heigth / 2;
		target.highy = target.lowy + heigth;
		
		if (target.lowx < lowx) {
			target.lowx = lowx;
			target.highx = target.lowx + width;
		} else if (target.highx > highx) {
			target.highx = highx;
			target.lowx = target.highx - width;
		}
		
		if (target.lowy < lowy) {
			target.lowy = lowy;
			target.highy = target.lowy + heigth;
		} else if (target.highy > highy) {
			target.highy = highy;
			target.lowy = target.highy - heigth;
		}
		return target;
	}

	public Bounds zoomOut(Bounds target) {
		Bounds zoomed = new Bounds();
		double cx = (lowx + highx) / 2;
		double cy = (lowy + highy) / 2;
		double ratio = target.getAspect();
		double width = getWidth() * 1.01;
		if (width > target.getWidth()) {
			return target;
		}
		double heigth = width / ratio;
		zoomed.lowx = cx - width / 2;
		zoomed.highx = zoomed.lowx + width;
		zoomed.lowy = cy - heigth / 2;
		zoomed.highy = zoomed.lowy + heigth;
		
		if (zoomed.lowx < target.lowx) {
			zoomed.lowx = target.lowx;
			zoomed.highx = zoomed.lowx + width;
		} else if (zoomed.highx > target.highx) {
			zoomed.highx = target.highx;
			zoomed.lowx = target.highx - width;
		}
		if (zoomed.lowy < target.lowy) {
			zoomed.lowy = target.lowy;
			zoomed.highy = zoomed.lowy + heigth;
		} else if (zoomed.highy > target.highy) {
			zoomed.highy = target.highy;
			zoomed.lowy = zoomed.highy - heigth;
		} 
		return zoomed;
	}

	private double getAspect() {
		return getWidth() / getHeigth();
	}

	public double getWidth() {
		return highx - lowx;
	}

	public double getHeigth() {
		return highy - lowy;
	}

	public Bounds zoomToDimension(double targetWidth, double targetHeight) {
		double centerx = (lowx + highx)/2;
		double centery = (lowy + highy)/2;
		Bounds zoomed = new Bounds();
		zoomed.lowx = centerx - targetWidth/2; 
		zoomed.lowy = centery - targetHeight/2;
		zoomed.highx = zoomed.lowx + targetWidth;
		zoomed.highy = zoomed.lowy + targetHeight;
		return zoomed;
	}

	public double getCenterX() {
		return (lowx + highx)/2;
	}

	public double getCenterY() {
		return (lowy + highy)/2;
	}

	@Override
	public String toString() {
		return "Bounds [x=" + Double.toString(lowx) + " ->x+ " + Double.toString(getWidth()) + ", y=" + Double.toString(lowy) + " ->y+ " + getHeigth() + "]";
	}

	public Bounds zoomAround(double center_x, double center_y, double zoom_factor) {
		double ratio = full_bounds.getWidth() / full_bounds.getHeigth();
		Bounds zoomed = new Bounds();
		double target_width = getWidth() / zoom_factor;
		double target_heigth = target_width / ratio;
		zoomed.lowx = center_x - target_width / 2; zoomed.highx = zoomed.lowx + target_width;
		zoomed.lowy = center_y - target_heigth / 2; zoomed.highy = zoomed.lowy + target_heigth;
		return zoomed;
	}
}