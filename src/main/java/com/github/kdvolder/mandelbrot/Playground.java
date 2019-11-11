package com.github.kdvolder.mandelbrot;

public class Playground {

	public static void main(String[] args) {
		for (int j = 1; j<50; j++) {
			int i = 1 << j;
			System.out.println(j+": "+i);
		}
		System.out.println(Runtime.getRuntime().availableProcessors());
	}
	
}
