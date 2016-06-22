package smlms.rendering;

import ij.ImagePlus;

public class RenderingParameters {

	public String name;
	public ImagePlus imp;
	
	public double pixelsize;
	public double fwhm;
	public String method;
	
	public double min[];	// x, y, z, frame, photons
	public double max[];	// x, y, z, frame, photons
	
	public RenderingParameters(String name, String method, double pixelsize, double fwhm, double min[], double max[]) {
		this.name = name;
		this.method = method;
		this.pixelsize = pixelsize;
		this.fwhm = fwhm;
		this.min = min;
		this.max = max;
	}
	
	public Object[] getObjectAsArray() {
		return new Object[] { 
				name, method, pixelsize, fwhm, 
				min[0], max[0],
				min[1], max[1],
				min[2], max[2],
				min[3], max[3],
				min[4], max[4] };

	}
	
	
}
