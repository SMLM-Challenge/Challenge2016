package smlms.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.io.Opener;
import imageware.Builder;
import imageware.ImageWare;

import java.io.File;

public class Normalize_PSF_Batch {

	public static String path = "/Users/sage/Desktop/activation/psf/";
		
	public static void main(String args[]) {
		new Normalize_PSF_Batch();
	}

	public Normalize_PSF_Batch() {	
		
		String list[] = new File(path).list();
		for(String psf : list) {
			if (psf.endsWith(".tif")) {
				ImagePlus imp = (new Opener()).openImage(path + File.separator + psf);
				ImagePlus out = normalize(imp, psf);
				FileSaver saver = new FileSaver(out);
				saver.saveAsTiffStack(path + File.separator + psf + "-max.tif");
			}
		}
	}
	
	private ImagePlus normalize(ImagePlus imp, String psfname) {
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
		Builder.create(att).show("Windowing map " + psfname);
		
		for(int x=0; x<nx; x++)
		for(int y=0; y<ny; y++) 
		for(int z=0; z<nz; z++)
			psf.putPixel(x, y, z, psf.getPixel(x, y, z) * att[x][y]);

		double max = -Double.MAX_VALUE;
		int xmax = 0;
		int ymax = 0;
		int zmax = 0;
		for(int x=0; x<nx; x++)
		for(int y=0; y<ny; y++) 
		for(int z=0; z<nz; z++) 
			if (psf.getPixel(x, y, z) > max) {
				max = psf.getPixel(x, y, z);
				xmax = x;
				ymax = y;
				zmax = z;
			}
		
		IJ.log(" Max " + psfname + ": "+ max + " at (" + xmax + " " + ymax + " " + zmax + ") == middle (" + (nx/2) + " " + (ny/2) + " "  + (nz/2) +")");
	
		double f = 0.5;
		double hmax = (psfname.startsWith("BP") ? max * f * 0.5 : max * f);
		
		int swhm = 0;
		double sum = 0.0;
		for(int x=0; x<nx; x++)
		for(int y=0; y<ny; y++) 
			if (psf.getPixel(x, y, zmax) > hmax) {
				sum += psf.getPixel(x, y, zmax);
				swhm++;
			}
		IJ.log(" Surface HMax:" + swhm + " fwhm "+ Math.sqrt((swhm/Math.PI)*2) + " norm: " + sum);
		

		double total = 1.0 / sum;
		psf.multiply(total);
		psf.show("" + psf.getTotal());
			
		ImagePlus out = new ImagePlus("", psf.buildImageStack()); 
		
			
		/*	
		for(int z=0; z<nz; z++) {
			double s = 0.0;
			for(int x=0; x<nx; x++)
			for(int y=0; y<ny; y++) 
				s += psf.getPixel(x, y, z);
			IJ.log(" slice z=" + z + " " + s);
		}
		*/
		return out;
	}

}
