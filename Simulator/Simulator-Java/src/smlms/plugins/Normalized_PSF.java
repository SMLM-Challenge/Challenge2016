package smlms.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.FileSaver;
import imageware.Builder;
import imageware.ImageWare;

public class Normalized_PSF {
	
	public static void main(String args[]) {
		new Normalized_PSF();
	}

	public Normalized_PSF() {	
		//String filename = "/Users/sage/Desktop/GL-20nm.tif";
		//String filename = "/Users/sage/Desktop/DH-20nm.tif";
		//String filename = "/Users/sage/Desktop/psf.tif";
		//ImagePlus imp = new Opener().openImage(filename);
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp == null)
			return;
		ImageWare psf = Builder.wrap(imp);
		double min = psf.getMinimum();

		psf.subtract(min);
		int nx = psf.getWidth();
		int ny = psf.getHeight();
		int nz = psf.getSizeZ();
		double att[][] = new double[nx][ny];
		double xc = nx * 0.5;
		double yc = ny * 0.5;
		for(int x=0; x<nx; x++)
		for(int y=0; y<ny; y++) {
			double d = 1 - Math.sqrt((x-xc)*(x-xc) + (y-yc)*(y-yc)) / (nx);
			att[x][y] = 1.0 / (1.0 + Math.exp(-(d-0.5)/0.02));
		}
		
		Builder.create(att).show("att");

		for(int x=0; x<nx; x++)
		for(int y=0; y<ny; y++) 
		for(int z=0; z<nz; z++) 
			psf.putPixel(x, y, z, psf.getPixel(x, y, z) * att[x][y]);

		int zo = nz/2;
		double sum = 0.0;
		for(int x=0; x<nx; x++)
		for(int y=0; y<ny; y++) 
			sum += psf.getPixel(x, y, zo);

		double total = 1.0 / sum;
		psf.multiply(total);
		psf.show("" + psf.getTotal());
		
		ImagePlus out = new ImagePlus("", psf.buildImageStack());  
		FileSaver saver = new FileSaver(out);
		saver.saveAsTiffStack("/Users/sage/Desktop/" + imp.getTitle() + "-norm.tif");
		
		
		for(int z=0; z<nz; z++) {
			double s = 0.0;
			for(int x=0; x<nx; x++)
				for(int y=0; y<ny; y++) 
					s += psf.getPixel(x, y, z);
			IJ.log(" slice z=" + z + " " + s);
		}

	}
}
