package com.github.kdvolder.mandelbrot;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ColorGrid {

	private class Widget extends Canvas {
		@Override
		public void update(Graphics g) {
			paint(g);
		}
		
		@Override
		public void paint(Graphics g) {
			g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
			g.setColor(Color.WHITE);
			g.fillRect(xmark, ymark, 5, 5);
		}
		
		private static final long serialVersionUID = 1L;
		@Override
		public Dimension getMinimumSize() {
			return new Dimension(w, h);
		}

		@Override
		public Dimension getPreferredSize() {
			return getMinimumSize();
		}
		
		@Override
		public Dimension getMaximumSize() {
			return new Dimension(w, h);
		}
	}

	private Widget widget = new Widget();
	
	public void put(int x, int y, Color c) {
		g.setColor(c);
		g.fillRect(x, y, 1, 1);
	}
	public void repaint() {
		widget.repaint();
	}
		
	private int w;
	private int h;
	private BufferedImage image;
	private Graphics2D g;
	private int xmark;
	private int ymark;

	public ColorGrid(int w, int h) {
		this.w = w;
		this.h = h;
		this.image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		this.g = (Graphics2D) image.getGraphics();
	}

	public Component getWidget() {
		return widget;
	}

	public int getWidth() {
		return w;
	}

	public int getHeigth() {
		return h;
	}
	public void setMarker(long l, long m) {
		this.xmark = (int) l;
		this.ymark = (int) m;
	}
}
