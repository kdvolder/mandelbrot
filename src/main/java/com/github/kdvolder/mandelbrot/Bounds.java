package com.github.kdvolder.mandelbrot;

public class Bounds {
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
}