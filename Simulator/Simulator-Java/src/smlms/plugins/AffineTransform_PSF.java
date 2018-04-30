package smlms.plugins;

import ij.IJ;

public class AffineTransform_PSF {

	public AffineTransform_PSF() {
		IJ.selectWindow("BP-000.tif");
		IJ.run("TransformJ Scale", "x-factor=1.0 y-factor=1.0 z-factor=1.0 interpolation=[cubic B-spline]");
		IJ.run("TransformJ Rotate", "z-angle=3 y-angle=0.0 x-angle=0.0 interpolation=[cubic B-spline] background=0.0 anti-alias");
		IJ.run("TransformJ Translate", "x-translation=50 y-translation=0.0 z-translation=0.0 interpolation=[cubic convolution] background=0.0");
		IJ.selectWindow("BP-500.tif");
		IJ.run("TransformJ Scale", "x-factor=1.05 y-factor=1.05 z-factor=1.0 interpolation=[cubic B-spline]");
		IJ.run("TransformJ Rotate", "z-angle=-3 y-angle=0.0 x-angle=0.0 interpolation=[cubic B-spline] background=0.0 anti-alias");
		IJ.run("TransformJ Translate", "x-translation=-50 y-translation=0.0 z-translation=0.0 interpolation=[cubic convolution] background=0.0");
	}
}
