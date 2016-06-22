package smlms.simulation;

import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;

import java.io.File;

public class ProjectionModule {
	
	private FloatProcessor maxPR;	// Working resolution
	private FloatProcessor maxCR;	// Camera resolution
	private FloatProcessor avgPR;
	private FloatProcessor avgCR;
	private double pixelsizeWorking;
	private double pixelsizeCamera;
	
	public ProjectionModule(Viewport viewportCamera, double pixelsizeWorking) {
		pixelsizeCamera = viewportCamera.getPixelsize();
		this.pixelsizeWorking = pixelsizeWorking;
		int cx = viewportCamera.getFoVXPixel();
		int cy = viewportCamera.getFoVYPixel();
		int px = (int)Math.ceil(cx * pixelsizeWorking);
		int py = (int)Math.ceil(cy * pixelsizeWorking);
		maxPR = new FloatProcessor(px, py);
		maxCR = new FloatProcessor(cx, cy);
		avgPR = new FloatProcessor(px, py);
		avgCR = new FloatProcessor(cx, cy);
	}
	
	public void projectAtWorkingResolution(float[][] image) {
		if (maxPR == null)
			return;
		if (avgPR == null)
			return;
		float[] maxpix = (float[])maxPR.getPixels();
		float[] avgpix = (float[])avgPR.getPixels();
		int n = image.length;
		int index;
		for(int x=0; x<n; x++)
		for(int y=0; y<n; y++) {
			float value = image[x][y];
			index = x+y*n;
			maxpix[x+y*n] = Math.max(value, maxpix[x+y*n]);
			avgpix[index] = value + avgpix[index];	
		}
	}

	public void projectAtCameraResolution(float[][] image) {
		if (maxCR == null)
			return;
		if (avgCR == null)
			return;
		float[] maxpix = (float[])maxCR.getPixels();
		float[] avgpix = (float[])avgCR.getPixels();
		int n = image.length;
		int index;
		for(int x=0; x<n; x++)
		for(int y=0; y<n; y++) {
			float value = image[x][y];
			index = x+y*n;
			maxpix[x+y*n] = Math.max(value, maxpix[x+y*n]);
			avgpix[index] = value + avgpix[index];	
		}
	}
	
	public void show() {
		(new ImagePlus("max-" + IJ.d2s(pixelsizeWorking,1) + "-nm", maxPR)).show();
		(new ImagePlus("max-" + IJ.d2s(pixelsizeCamera,1)  + "-nm", maxCR)).show();
		(new ImagePlus("avg-" + IJ.d2s(pixelsizeWorking,1) + "-nm", avgPR)).show();
		(new ImagePlus("avg-" + IJ.d2s(pixelsizeCamera,1)  + "-nm", avgCR)).show();
	}
	
	public void store(String path, String format) {
		new File(path).mkdir();
		store(maxPR, path + "max-high-resolution", format);
		store(maxCR, path + "max-camera-resolution", format);
		store(avgPR, path + "avg-high-resolution", format);
		store(avgCR, path + "avg-camera-resolution", format);
	}
	
	private void store(FloatProcessor fp, String filename, String format) {
		ImagePlus imp = new ImagePlus("", fp);
		if (format.equals(CameraModule.names[0]))
			(new ImageConverter(imp)).convertToGray8();
		if (format.equals(CameraModule.names[1]))
			(new ImageConverter(imp)).convertToGray16();
		if (format.equals(CameraModule.names[2]))
			(new ImageConverter(imp)).convertToGray8();
		
		(new FileSaver(imp)).saveAsJpeg(filename + ".jpg");
		(new FileSaver(imp)).saveAsTiff(filename + ".tif");	
	}
}
