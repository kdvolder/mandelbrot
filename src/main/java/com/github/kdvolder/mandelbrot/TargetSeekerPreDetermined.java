package com.github.kdvolder.mandelbrot;

public class TargetSeekerPreDetermined implements TargetSeeker {
	
	Point[] targets = {
			new Point(-0.21583418609788452, 0.6535461043577115),
	};
	
	int nextTarget = 0;

	@Override
	public Bounds find(double desiredWidth) {
		return Bounds.withCenterAndWidth(targets[nextTarget++ % targets.length], desiredWidth);
	}

	// centerX = -0.21583418609788452
	// centerY = 0.6535461043577115
	
}
