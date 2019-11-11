package com.github.kdvolder.mandelbrot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomColorMap {
	
	private static Random rnd = new Random();
	
	private static boolean[] booleans = { false, true };
	
	public static Color[] generate() {
		List<Color> _colors = new ArrayList<>();
		for (boolean red : booleans) {
			for (boolean green : booleans) {
				for (boolean blue : booleans) {
					_colors.add(new Color(red?255:0, green?255:0, blue?255:0));
				}
			}
		}
		_colors.add(_colors.get(0));
		Color[] colors = _colors.toArray(new Color[_colors.size()]);
		shuffle(colors, 1, colors.length-1);
		return colors;
	}

	private static void shuffle(Color[] colors, int low, int high) {
		if (high-low>1) {
			int numAvailableColors = high-low;
			int chosen = low + rnd.nextInt(numAvailableColors);
			Color tmp = colors[low];
			colors[low] = colors[chosen];
			colors[chosen] = tmp;
			shuffle(colors, low+1, high);
		}
	}
}
