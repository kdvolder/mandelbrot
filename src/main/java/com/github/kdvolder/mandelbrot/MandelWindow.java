package com.github.kdvolder.mandelbrot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.swing.JFrame;

public class MandelWindow {
	
	private static final int VIDEO_LENGTH = 20 * 60;
	private static final int FPS = 24;
	private static final int MAX_FRAMES = VIDEO_LENGTH * FPS;
	
	static class Bounds {
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

		private double getWidth() {
			return highx - lowx;
		}

		private double getHeigth() {
			return highy - lowy;
		}
	}
	
	static Bounds full_bounds = new Bounds();
	
	public static class Painter implements Runnable, Closeable {
		
		private static final int iter_bump_limit = 1000;

		private static final File VIDEO_FILE = new File("saved-videos/capture-"+System.currentTimeMillis()+".mp4");

		private ColorGrid canvas;

		private Random rnd = new Random();

		private Color[] initialColorMap = {
				Color.BLACK,
				new Color(255, 0, 0),
				new Color(255, 255, 0),
				new Color(0, 255, 0),
				new Color(0, 255, 255),
				new Color(0, 0, 255),
				new Color(255, 0, 255),
				Color.WHITE,
				Color.BLACK
		};
		
//		private Color[] initialColorMap = {
//				Color.BLACK,
//				Color.BLUE,
//				Color.GREEN,
//				Color.RED, 
//				Color.YELLOW, 
//				Color.WHITE,
//				Color.BLACK
//		};

		private Color[] colorMap;

		private boolean zoom_out = false;
		
		private boolean fly_towards = false;
		private double fly_towards_x = rnd.nextDouble()*4-2.0;
		private double fly_towards_y = rnd.nextDouble()*4-2.0;

		private int max_iter = 1;
		private int max_iter_bump = 0;
		int expected_iter_bumps = 0;

		private boolean closeRequested;
		
		public Painter(ColorGrid canvas) {
			this.colorMap = initialColorMap;
			expandColorMap(400);
			this.canvas = canvas;
		}
		
		private int mandel(double x, double y) {
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
						int holdfor = fly_towards ? 150 : 1;
						if (expected_iter_bumps>0 && (rnd.nextInt(holdfor*expected_iter_bumps)==0)) {
							fly_towards = true;
							fly_towards_x = x;
							fly_towards_y = y;
						}
					}
					return iter % colorMap.length;
				}
				// (zr + zi * i) * (zr + zi * i) + x + y * i
				// zr^2 + 2 zr * zi * i - zi^2 + x + y * i
				// zr^2 - zi^2 + x + (2 zr * zi + y) * i
				double tmp = zr_square - zi_square + x;
				zi = 2 * zr * zi + y;
				zr = tmp;
				iter ++;
			}
			return iter % colorMap.length;
		}

		public int christmass(double x, double y, double scale) {
			double z = scale * y * (x * x - y * y);
			return (int) Math.floorMod((long)z, colorMap.length);
		}
		
		@Override
		public void run() {
			long beginningOfTime = System.currentTimeMillis();
			try {
				int w = canvas.getWidth();
				int h = canvas.getHeigth();
				double aspect = (double)w / h;
				
				Bounds target = new Bounds();
	
				try (VideoFileWriter video = new VideoFileWriter(VIDEO_FILE, w, h, FPS)) {
					for (int i = 1; i <= MAX_FRAMES && !closeRequested; i++) {
						double lowx = target.lowx;
						double highx = target.highx;
						double lowy = target.lowy;
						double highy = target.highy;
						double xfactor = (highx - lowx) / canvas.getWidth();
						double yfactor = (highy - lowy) / canvas.getHeigth();
						long start = System.currentTimeMillis();
						expected_iter_bumps = max_iter_bump;
						max_iter_bump = 0;
						double lastx = 1000;
						double lasty = 1000;
						for (int k = 0; k < w; k++) {
							double x = lowx + k * xfactor;
							if (lastx==x) {
								zoom_out = true;
							}
							lastx = x;
							for (int r = 0; r < h; r++) {
								double y = lowy + r * yfactor;
								Color c = colorMap[mandel(x, y)];
								canvas.put(k,r,c);
								if (lasty==y) {
									zoom_out = true;
								}
								lasty = y;
							}
						}
						if (zoom_out) {
							target = target.zoomOut(full_bounds);
							if (target.getWidth() >= full_bounds.getWidth()) {
								zoom_out = false;
								fly_towards = false;
							}
						} else if (fly_towards) {
							target = target.flyTowards(fly_towards_x, fly_towards_y, aspect);
							double xmark = (fly_towards_x - lowx) / (highx - lowx) * canvas.getWidth();
							double ymark = (fly_towards_y - lowy) / (highy - lowy) * canvas.getHeigth();
							canvas.setMarker(Math.round(xmark), Math.round(ymark));
						}
						canvas.repaint();
						if (max_iter_bump < iter_bump_limit/2) {
							max_iter--;
						}
						if (max_iter > 1500) {
							zoom_out = true;
						}
						video.addFrame(canvas.getImage());
						if (xfactor < 1E-14) {
							zoom_out = true;
						}
						double completion = ((double)i)/MAX_FRAMES;
						double computed_f_pre_min =  1000.0 * 60 * i / (System.currentTimeMillis() - beginningOfTime); 
						double eta = (MAX_FRAMES - i) / computed_f_pre_min;
						System.out.println(
								"frame = " + i +
								" mib = "+max_iter_bump +
								" mit = "+max_iter +
								" xfactor = "+ xfactor  +
								" took "+(System.currentTimeMillis() - start)+" ms " +
								" "+completion*100+"% " +
								"  "+ eta + " min"
						);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.exit(0);
		}
		
		public void expandColorMap(int numColors) {
			Color[] initial = this.colorMap;
			Color[] expanded = new Color[numColors];
			for (int t = 0; t < expanded.length; t++) {
				// if t == colorMap.length - 1 => s =  initial.length - 1
				double s = ((double)t) * (initial.length - 1) / (expanded.length - 1);
				int s0 = (int)Math.floor(s);
				int s1 = (int)Math.ceil(s);
				if (s0==s1) {
					expanded[t] = initial[s0]; 
				} else {
					Color c1 = initial[s0];
					Color c2 = initial[s1];
					double interpol = s - s0;
					expanded[t] = new Color(
							interpol(interpol, c1.getRed(), c2.getRed()),
							interpol(interpol, c1.getGreen(), c2.getGreen()),
							interpol(interpol, c1.getBlue(), c2.getBlue())
					);
				}
			}
			this.colorMap = expanded;
		}

		private int interpol(double interpol, int a, int b) {
			return (int) Math.round(interpol * b + (1.0 - interpol) * a);
		}

		@Override
		public void close() {
			closeRequested = true;
		}
	}

	public static void main(String[] args) {
		new MandelWindow().run();
	}


	private void run() {
		JFrame frame = new JFrame("Mandelbrot");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		
		ColorGrid mandelbrot = new ColorGrid(1920, 1080); 
//		Label mandelbrot = new Label("Image comding soon");
		 
		frame.getContentPane().add(mandelbrot.getWidget(), BorderLayout.CENTER);
		
		Painter painter = new Painter(mandelbrot);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				painter.close();
			}
		});
		new Thread(painter).start();

		frame.pack();
		frame.setVisible(true);
		// TODO Auto-generated method stub
		
	}
	
}
