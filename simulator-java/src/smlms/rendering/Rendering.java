package smlms.rendering;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ColorProcessor;
import imageware.ImageWare;

import java.util.ArrayList;

import smlms.file.Fluorophore;
import smlms.tools.Point3D;
import smlms.tools.Volume;
import additionaluserinterface.WalkBar;

public class Rendering {

	private WalkBar 		walk;
	private double 			pixelsize;
	private double 			timeout;
	
	public enum Method {GAUSSIAN, HISTO, TRIANGLE};
	public enum Amplitude {NONE, UNIT, PHOTONS, FRAME, X, Y, Z};
	
	private double minFrame = -Double.MAX_VALUE;
	private double maxFrame = -Double.MAX_VALUE;
	private double minPhotons = -Double.MAX_VALUE;
	private double maxPhotons = -Double.MAX_VALUE;

	public Rendering(WalkBar walk, double pixelsize, double timeout) {
		this.walk = walk;
		this.pixelsize = pixelsize;
		this.timeout = timeout;
	}

	public void setLimitFrames(double minFrame, double maxFrame) {
		this.minFrame = minFrame;
		this.maxFrame = maxFrame;
	}

	public void setLimitPhotons(double minPhotons, double maxPhotons) {
		this.minPhotons = minPhotons;
		this.maxPhotons = maxPhotons;
	}

	public ImageWare render(ArrayList<Fluorophore> fluos, Method method, Amplitude amplitude, Volume volume, double fwhm) {
		ImageWare image = volume.allocate3D(pixelsize);
		
		IJ.log("Rendering at pixelsize: " + pixelsize);
		IJ.log("Fluos:" + fluos.size());
		IJ.log("Method:" + method.name() + " Amplitude:" + amplitude.name() + " FWHM:" + fwhm);
		IJ.log("Volume:" + volume.toString());
		
		double r = 1.0/pixelsize;
		int n = fluos.size();
		walk.reset();
		double count = 0;
		double chrono = System.nanoTime();
		double a = 1;
		for(Fluorophore fluo : fluos) {
			if (fluo.frame >= minFrame)
			if (fluo.frame <= maxFrame)
			if (fluo.photons >= minPhotons)
			if (fluo.photons <= maxPhotons)
				if (volume.contains(fluo)) {
				count++;
				double x = (fluo.x-volume.x1)*r;
				double y = (fluo.y-volume.y1)*r;
				double z = (fluo.z-volume.z1)*r;
				if (count % 100 == 0)
					walk.progress("Render " + count, 100.0*count/n);
				if (amplitude == Amplitude.PHOTONS)
					a = fluo.photons;
				else if (amplitude == Amplitude.FRAME)
					a = fluo.frame;
				else if (amplitude == Amplitude.X)
					a = fluo.x;
				else if (amplitude == Amplitude.Y)
					a = fluo.y;
				else if (amplitude == Amplitude.Z)
					a = fluo.z;
				
				if (method == Method.GAUSSIAN)
					addGaussian(image, fwhm/pixelsize, x, y, z, a);
				else if (method == Method.TRIANGLE)
					addTriangle(image, x, y, z, a);
				else 
					image.putPixel((int)x, (int)y, (int)z, a + image.getPixel((int)x, (int)y, (int)z));
			}
			if ((System.nanoTime() - chrono) * 10e-9 > timeout) {
				walk.progress("TIMEOUT " + count, 100.0*count/n);
				break;
			}
		}

		return image;
	}

	public ImageWare projection(ArrayList<Fluorophore> fluos, Method method, Amplitude amplitude, Volume volume, double fwmh) {
		ImageWare image = volume.allocate2D(pixelsize);
		double r = 1.0/pixelsize;
		int n = fluos.size();
		if (walk != null)
			walk.reset();
		double count = 0;
		double chrono = System.nanoTime();
		double a = 1;
	
		for(Fluorophore fluo : fluos) {
			if (volume.contains(fluo)) {
				count++;
				double x = (fluo.x-volume.x1)*r;
				double y = (fluo.y-volume.y1)*r;
				if (walk != null)
				if (count % 100 == 0)
					walk.progress("Projection " + count, count/n);
				if (amplitude == Amplitude.PHOTONS)
					a = fluo.photons;
				else if (amplitude == Amplitude.FRAME)
					a = fluo.frame;
				else if (amplitude == Amplitude.X)
					a = fluo.x;
				else if (amplitude == Amplitude.Y)
					a = fluo.y;
				else if (amplitude == Amplitude.Z)
					a = fluo.z;
				
				a = Math.min(1, Math.max(0, a));
				if (method == Method.GAUSSIAN)
					addGaussian(image, fwmh/pixelsize, x, y, a);
				else if (method == Method.TRIANGLE)
					addTriangle(image, x, y, a);
				else
					image.putPixel((int)x, (int)y, 0, a + image.getPixel((int)x, (int)y, 0));	
			}
			if ((System.nanoTime() - chrono) * 10e-9 > timeout) {
				walk.progress("TIMEOUT " + count, 100.0*count/n);
				break;
			}
		}
		return image;
	}

	private void show(ImageWare image[], String name) {
		
		if (image.length == 1) {
			image[0].show(name);
		}
		else {
			image[0].clip();
			image[1].clip();
			image[2].clip();
			ImageWare rb = image[0].convert(ImageWare.BYTE);
			ImageWare gb = image[1].convert(ImageWare.BYTE);
			ImageWare bb = image[2].convert(ImageWare.BYTE);
			int nx = image[0].getSizeX();
			int ny = image[0].getSizeY();
			int nz = image[0].getSizeZ();
			ImageStack stack = new ImageStack(nx, ny);
			for(int z=0; z<nz; z++) {
				byte r[] = rb.getSliceByte(z);
				byte g[] = gb.getSliceByte(z);
				byte b[] = bb.getSliceByte(z);
				ColorProcessor cp = new ColorProcessor(nx, ny);
				cp.setRGB(r, g, b);
				stack.addSlice(cp);
			}
			ImagePlus imp = new ImagePlus(name, stack);
			imp.show();
		}

	}
	public ImageWare renderVolHisto(ArrayList<Point3D> points, Volume volume, double pixelsize) {
		ImageWare image = volume.allocate3D(pixelsize);
		double r = 1.0/pixelsize;
		int n = points.size();
		walk.reset();
		double count = 0;
		for(Point3D point : points) {
			if (volume.contains(point)) {
				count++;
				if (count % 100 == 0)
					walk.progress("Render " + count, count/n);
				int i = (int)Math.round((point.x-volume.x1)*r);
				int j = (int)Math.round((point.y-volume.y1)*r);
				int k = (int)Math.round((point.z-volume.z1)*r);
				image.putPixel(i, j, k, 1 + image.getPixel(i, j, k));
			}
		}
		walk.finish("Render " + n);
		return image;
	}

	public ImageWare renderVolGaussian(ArrayList<Point3D> points, Volume volume, double pixelsize, double fwmh) {
		ImageWare image = volume.allocate3D(pixelsize);
		double r = 1.0/pixelsize;
		int n = points.size();
		walk.reset();
		double count = 0;
		for(Point3D point : points) {
			if (volume.contains(point)) {
				count++;
				if (count % 100 == 0)
					walk.progress("Render " + count, count/n);
				addGaussian(image, fwmh/pixelsize, (point.x-volume.x1)*r, (point.y-volume.y1)*r, (point.z-volume.z1)*r, 100);

			}
		}
		walk.finish("Render " + n);
		return image;
	}

	private void addGaussian(ImageWare image, double sigma, double x, double y, double z, double photons) {
		int xi = (int)Math.round(x);
		int yi = (int)Math.round(y);
		int zi = (int)Math.round(z);
	
		int size = (int) Math.ceil(3 * sigma) + 1;
		double coefXY = 1.0 / (sigma * sigma * 2.0);
		for (int i = xi - size; i <= xi + size; i++)
		for (int j = yi - size; j <= yi + size; j++)
		for (int k = zi - size; k <= zi + size; k++) {
			double v = photons * Math.exp(-coefXY * ((i-x) * (i-x) + (j-y) * (j-y) + (k-z) * (k-z)));
			image.putPixel(i, j, k, v + image.getPixel(i, j, k));
		}
	}

	private void addGaussian(ImageWare image, double sigma, double x, double y, double photons) {
		int xi = (int)Math.round(x);
		int yi = (int)Math.round(y);
	
		int size = (int) Math.ceil(3 * sigma) + 1;
		double coefXY = 1.0 / (sigma * sigma * 2.0);
		for (int i = xi - size; i <= xi + size; i++)
		for (int j = yi - size; j <= yi + size; j++) {
			double v = photons * Math.exp(-coefXY * ((i-x) * (i-x) + (j-y) * (j-y) ));
			image.putPixel(i, j, 0, v + image.getPixel(i, j, 0));
		}
	}
	
	private void addTriangle(ImageWare image, double x, double y, double z, double photons) {
		int xi = (int)Math.round(x);
		int yi = (int)Math.round(y);
		int zi = (int)Math.round(z);
		for (int i = xi - 1; i <= xi + 1; i++)
		for (int j = yi - 1; j <= yi + 1; j++)
		for (int k = zi - 1; k <= zi + 1; k++) {
			double v = photons * Math.sqrt((i-x) * (i-x) + (j-y) * (j-y) + (k-z) * (k-z));
			image.putPixel(i, j, k, v + image.getPixel(i, j, k));
		}
	}

	private void addTriangle(ImageWare image, double x, double y, double photons) {
		int xi = (int)Math.round(x);
		int yi = (int)Math.round(y);

		for (int i = xi - 1; i <= xi + 1; i++)
		for (int j = yi - 1; j <= yi + 1; j++) {
			double v = photons * Math.sqrt((i-x) * (i-x) + (j-y) * (j-y) );
			image.putPixel(i, j, 0, v + image.getPixel(i, j, 0));
		}
	}
}
