package com.github.kdvolder.mandelbrot;

import static com.github.kdvolder.mandelbrot.Bounds.full_bounds;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Closeable;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JFrame;

public class MandelWindow {
	
	private static final int NUM_VIDEOS = 8;
	public static final boolean benchmark = false;
	private static final double minIterDrag = 0.9;
	
	private static final double numColors = 2.2;
	private static final double colorSpeed = 6.0;
	
	private static final int VIDEO_LENGTH = (3 * 60 + 37) * NUM_VIDEOS;
	private static final int FPS = 24;
	private static final int MAX_FRAMES = VIDEO_LENGTH * FPS;
	
	public static TargetSeeker targetSeeker = new TargetSeeker();
	
	public static class Painter implements Runnable, Closeable {
		
		private ExecutorService executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		private static final double MIN_PIXEL_SIZE = 1E-14;
//		private static final double MIN_PIXEL_SIZE = 1E-3;
		

		private static final int NO_ESCAPE_COLOR = Color.BLACK.getRGB();

//		private static final int LINEARBOUND = 25;

		private ColorGrid canvas;

		private Color[] initialColorMap;
			
//			{ 
//				Color.BLACK, 
//				new Color(255,   0,   0), 
//				new Color(255, 255,   0),
//				new Color(0,     0, 255), 
//				new Color(0, 255, 0), 
//				new Color(255, 0, 255), 
//				new Color(255, 255, 255), 
//				Color.BLACK 
//			};

//		private Color[] initialColorMap = { 
//				Color.BLACK, 
//				new Color(255, 0, 0), 
//				new Color(255, 255, 0),
//				new Color(0, 255, 0), 
//				new Color(0, 255, 255), 
//				new Color(0, 0, 255), 
//				new Color(255, 0, 255), 
////				Color.WHITE,
//				Color.BLACK 
//			};
//
		// private Color[] initialColorMap = {
		// Color.BLACK,
		// Color.BLUE,
		// Color.GREEN,
		// Color.RED,
		// Color.YELLOW,
		// Color.WHITE,
		// Color.BLACK
		// };

		private int[] colorMap;
		private TargetSeeker targetSeeker = new TargetSeeker();

		private boolean zoom_out = true;

		private boolean fly_towards = false;
		private double fly_towards_x = 0;// rnd.nextDouble()*4-2.0;
		private double fly_towards_y = 0; // rnd.nextDouble()*4-2.0;

		// private OldMandelFunction mandel = new OldMandelFunction(100, 100);
		private UnlimitedMandelFunction mandel;
		
		private boolean closeRequested;

		public Painter(ColorGrid canvas) {
			initializeColorMap();
			this.canvas = canvas;
		}

		private void initializeColorMap() {
			this.initialColorMap = RandomColorMap.generate();
			this.colorMap = new int[initialColorMap.length];
			for (int i = 0; i < colorMap.length; i++) {
				colorMap[i] = initialColorMap[i].getRGB();
			}
			expandColorMap((initialColorMap.length - 1) * 256 + 1);
		}

		@Override
		public void run() {
			switchToFlyTowards();
			long beginningOfTime = System.currentTimeMillis();
			int w = canvas.getWidth();
			int h = canvas.getHeigth();
			double aspect = (double) w / h;
			Bounds target;

			// start fully zoomed out:
			target = new Bounds();

			// { //Start fully zoomed in using iterative zoom search for interesting region
			// of space.
			// MandelFunction mandel = new MandelFunction(max_iter);
			// target = targetSeeker.find(MIN_PIXEL_SIZE * canvas.getWidth(), mandel);
			// //target = targetSeeker.find(full_bounds.getWidth() / 20, mandel);
			// max_iter = mandel.max_iter;
			// }

			VideoFileWriter video = null;
			Future<?>[] futures = new Future<?>[h];
			try {
				double minIter = 0;
				for (int i = 1; !closeRequested; i++) {
					if (video == null) {
						if (zoom_out) {
							video = new VideoFileWriter(newVideoFile("zoom-out"), w, h, FPS);
							video.writeCompanionTextFile(target.getCenterX(), target.getCenterY());
						} else if (fly_towards) {
							video = new VideoFileWriter(newVideoFile("zoom-in-out"), w, h, FPS);
							video.writeCompanionTextFile(fly_towards_x, fly_towards_y);
						}
					}
					double lowx = target.lowx;
					double highx = target.highx;
					double lowy = target.lowy;
					double highy = target.highy;
					double xfactor = (highx - lowx) / canvas.getWidth();
					double yfactor = (highy - lowy) / canvas.getHeigth();
					long start = System.currentTimeMillis();
					mandel.startSession();
					for (int r = 0; r < h; r++) {
						futures[r] = paintLine(w, lowx, lowy, xfactor, yfactor, r, minIter);
					}
					for (int r = 0; r < futures.length; r++) {
						futures[r].get();
					}
					if (video != null) {
						video.addFrame(canvas.getImage());
					}
					canvas.repaint();
					mandel.endSession();
					minIter = minIter * minIterDrag + mandel.min_iter * (1 - minIterDrag);
					if (zoom_out) {
						target = target.zoomOut(full_bounds);
						if (target.getWidth() >= full_bounds.getWidth()) {
							switchToFlyTowards();
							closeRequested |= i >= MAX_FRAMES;
							if (video != null) {
								video.close();
								video = null;
								initializeColorMap();
							}
						}
					} else if (fly_towards) {
						target = target.flyTowards(fly_towards_x, fly_towards_y, aspect);
						double xmark = (fly_towards_x - lowx) / (highx - lowx) * canvas.getWidth();
						double ymark = (fly_towards_y - lowy) / (highy - lowy) * canvas.getHeigth();
						canvas.setMarker(Math.round(xmark), Math.round(ymark));
					}
					if (xfactor < MIN_PIXEL_SIZE) {
						zoom_out = true;
					}
					double completion = ((double) i) / MAX_FRAMES;
					double computed_f_per_min = 1000.0 * 60 * i / (System.currentTimeMillis() - beginningOfTime);
					double eta = (MAX_FRAMES - i) / computed_f_per_min;
					System.out.println("frame = " + i + " " + mandel.sessionStats() + " xfactor = " + xfactor + " took "
							+ (System.currentTimeMillis() - start) + " ms" + " " + completion * 100 + "% " + "  " + eta
							+ " min" + " " + target);
					if (benchmark && i % 100 == 0) {
						long totalTime = System.currentTimeMillis() - beginningOfTime;
						System.out.println("=== benchmark results ===");
						System.out.println("Fly towards [x=" + fly_towards_x + ", y=" + fly_towards_y + "]");
						System.out.println("100 frames took " + totalTime / 1000.0 + " seconds");
						System.out.println("Average fpm " + computed_f_per_min + " seconds");
						closeRequested = true;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (video != null) {
					video.close();
				}
			}
			System.exit(0);
		}

		private void switchToFlyTowards() {
			zoom_out = false;
			fly_towards = true;
			{ // Set fly_towards target.
				MandelFunction mandel = new MandelFunction(0);
				Bounds zoomTarget = targetSeeker.find(MIN_PIXEL_SIZE * canvas.getWidth(), mandel);
				this.mandel = new UnlimitedMandelFunction(1<<20 /*mandel.max_iter+5_000*/);
				fly_towards_x = zoomTarget.getCenterX();
				fly_towards_y = zoomTarget.getCenterY();
			}
		}

		private Future<?> paintLine(int w, double lowx, double lowy, double xfactor, double yfactor, int r, double minIter) {
			return executors.submit(() -> {
//				final int tmi = (int)(targetSeeker.finalMaxIter * 2);
				final int cml = colorMap.length;
				double y = lowy + r * yfactor;
				for (int k = 0; k < w; k++) {
					double x = lowx + k * xfactor;
					int c;
					int m = mandel.mandel(x, y);
					if (m==UnlimitedMandelFunction.NO_ESCAPE) {
						c = NO_ESCAPE_COLOR;
					} else {
						double colorRange = numColors * 255;
						double overBase = m - minIter;
						double color = minIter * colorSpeed + colorRange - (colorRange * colorRange) / (overBase * colorSpeed + colorRange);
	//					m = m * cml / tmi;
						c = colorMap[(int) Math.floorMod(Math.round(color), cml)];
					}
					canvas.put(k,r,c);
	//							canvas.setMarker(Math.round(k), Math.round(r));
	//							canvas.repaint();
				}
			});
		}
		
		private File newVideoFile(String describe) {
			return new File("saved-videos/"+describe+"-"+System.currentTimeMillis()+".mp4");
		}

		public void expandColorMap(int numColors) {
			Color[] initial = this.initialColorMap;
			int[] expanded = new int[numColors];
			for (int t = 0; t < expanded.length; t++) {
				// if t == colorMap.length - 1 => s =  initial.length - 1
				double s = ((double)t) * (initial.length - 1) / (expanded.length - 1);
				//double s = (1.0 - 1.0 / (t/300.0 + 1.0)) * (initial.length - 1);
				int s0 = (int)Math.floor(s);
				int s1 = (int)Math.ceil(s);
				if (s0==s1) {
					expanded[t] = initial[s0].getRGB(); 
				} else {
					Color c1 = initial[s0];
					Color c2 = initial[s1];
					double interpol = s - s0;
					expanded[t] = new Color(
							interpol(interpol, c1.getRed(), c2.getRed()),
							interpol(interpol, c1.getGreen(), c2.getGreen()),
							interpol(interpol, c1.getBlue(), c2.getBlue())
					).getRGB();
				}
			}
			this.colorMap = expanded;
			System.out.println("last-color = " + new Color(this.colorMap[colorMap.length-1]));
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
		frame.setResizable(false);
		
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
	}
	
}
