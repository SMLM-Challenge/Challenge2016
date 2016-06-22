package smlms.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.OvalRoi;
import ij.gui.Plot;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import imageware.Builder;
import imageware.ImageWare;

public class Format_PSF {

	public Format_PSF() {	
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp == null) {
			IJ.error("No open image");
			return;
		}
			
		GenericDialog dlg = new GenericDialog("Format PSF");
		dlg.addCheckbox("Zeroing", true);
		dlg.addNumericField("Percentage of histogram", 0.1, 1);
		
		dlg.addCheckbox("Gaussian in Z", true);
		dlg.addNumericField("SigmaZ", 1, 1);

		dlg.addCheckbox("Attenuation Lateral", true);
		dlg.addNumericField("Sigma of sigmoid window", 0.02, 3);
		
		dlg.addCheckbox("Normalization sum of middle slice = N", true);
		dlg.addNumericField("N", 1, 1);
		
		dlg.addCheckbox("Surface normalization (centered disk)", true);
		dlg.addNumericField("Radius in pixel", 100, 3);

		dlg.showDialog();
		if (dlg.wasCanceled())
			return;
		boolean zero = dlg.getNextBoolean();
		double percentage = dlg.getNextNumber();
		
		boolean gaussianZ = dlg.getNextBoolean();
		double sigmaZ = dlg.getNextNumber();
		
		boolean attenuation = dlg.getNextBoolean();
		double att = dlg.getNextNumber();
	
		boolean normalization = dlg.getNextBoolean();
		double norm = dlg.getNextNumber();
		
		boolean disk = dlg.getNextBoolean();
		double radius = dlg.getNextNumber();

		new ImageConverter(imp).convertToGray32();
		ImagePlus imp1 = zero 			? zeroing(imp, percentage) 		: imp.duplicate();
		ImagePlus imp2 = gaussianZ 		? gaussianZ(imp1, sigmaZ) 		: imp1.duplicate();
		ImagePlus imp3 = attenuation 	? attenuation(imp2, att) : imp2.duplicate();
		ImagePlus imp4 = normalization 	? normalization(imp3, norm, disk, (int)radius) 	: imp3.duplicate();

		imp4.show();
	}

	
	private ImagePlus zeroing(ImagePlus imp, double percentage) {
		ImageWare psf = Builder.wrap(imp);
		double max = psf.getMaximum();
		double min = psf.getMinimum();
		
		int nbins = 1000000;
		int histo[] = new int[nbins];		
		int nx = psf.getWidth();
		int ny = psf.getHeight();
		int nz = psf.getSizeZ();

		for(int x=0; x<nx; x++)
		for(int y=0; y<ny; y++)
		for(int z=0; z<nz; z++) {
			double v = (psf.getPixel(x, y, z) - min) / (max-min) * nbins;
			int h = (int)(Math.max(0, Math.min(nbins-1, v)));
			histo[h]++;
		}
	
		double stop = percentage * nx *ny * ny / 100.0;
		double cumul = 0;
		int h = 0;
		while (cumul < stop && h < nbins-1) {
			cumul += histo[h];
			h++;
		}
		double T = min + h * (max-min) / nbins;
		IJ.log("zero at T " + T + " h:" + h + " for " +stop + " cumul:" + cumul);
		
		ImagePlus out = imp.duplicate();
		for(int z=0; z<nz; z++) {
			ImageProcessor in = imp.getStack().getProcessor(z+1);
			ImageProcessor ou = out.getStack().getProcessor(z+1);
			for(int x=0; x<nx; x++)
			for(int y=0; y<ny; y++) 
				ou.putPixelValue(x, y, in.getPixelValue(x, y) < T ? 0.0 : in.getPixelValue(x, y)-T);
		}
		out.setTitle("After Zeroing");
		out.setSlice(nz/2);
		out.show();
		return out;
	}

	private ImagePlus gaussianZ(ImagePlus imp, double sigma) {
		ImageWare psf = Builder.wrap(imp);
		int nz = imp.getStackSize();
		psf.smoothGaussian(0, 0, sigma);
		ImagePlus out = new ImagePlus("", psf.buildImageStack());
		out.setTitle("After Gaussian");
		out.setSlice(nz/2);
		out.show();
		return out;
	}
	
	private ImagePlus attenuation(ImagePlus imp, double factorXY) {
		int nx = imp.getWidth();
		int ny = imp.getHeight();
		int nz = imp.getStackSize();
		double att[][] = new double[nx][ny];
		double xc = nx * 0.5;
		double yc = ny * 0.5;
		for(int x=0; x<nx; x++)
		for(int y=0; y<ny; y++) {
			double d = 1 - Math.sqrt((x-xc)*(x-xc) + (y-yc)*(y-yc)) / (nx);
			att[x][y] = 1.0 / (1.0 + Math.exp(-(d-0.5)/factorXY));
		}
		Builder.create(att).show("Windowing map " + factorXY);
		
		ImagePlus out = imp.duplicate();
		for(int z=0; z<nz; z++) {
			ImageProcessor in = imp.getStack().getProcessor(z+1);
			ImageProcessor ou = out.getStack().getProcessor(z+1);
			for(int x=0; x<nx; x++)
			for(int y=0; y<ny; y++)
				ou.putPixelValue(x, y, in.getPixelValue(x, y) * att[x][y]);
		}
		out.setTitle("After Attenuation");
		out.setSlice(nz/2);
		out.show();
		return out;
	}

	private ImagePlus normalization(ImagePlus imp, double norm, boolean disk, int radius) {
		ImageWare psf = Builder.create(imp);
		int nx = psf.getWidth();
		int ny = psf.getHeight();
		int nz = psf.getSizeZ();
		int xc = nx/2;
		int yc = ny/2;
		
		int zo = nz/2;
		double sum = 0.0;
		for(int x=0; x<nx; x++)
		for(int y=0; y<ny; y++) {
			if (disk == true) {
				double r = (xc-x)*(xc-x) + (yc-y)*(yc-y);
				if (Math.sqrt(r) < radius)
					sum += psf.getPixel(x, y, zo);
			}
			else {
				sum += psf.getPixel(x, y, zo);
			}
		}
		
		double total = norm / sum;
		IJ.log(" Normalization sum=" + String.format("%13.3f", sum) + " disk " + disk + " > normalize factor " + String.format("%3.15f", total));
		psf.multiply(total);
	
		double zz[] = new double[nz];
		double val[] = new double[nz];
		for(int z=0; z<nz; z++) {
			zz[z] = z;
			for(int x=0; x<nx; x++)
			for(int y=0; y<ny; y++) 
				val[z] += psf.getPixel(x, y, z);
		}	
		new Plot("Z integral", "z", "sum", zz, val).show();
		ImagePlus out = new ImagePlus("", psf.buildImageStack());  
		out.setTitle("After Normalization norm=" + total);
		out.setSlice(nz/2);
		out.show();
		if (disk)
			out.setRoi(new OvalRoi(xc-radius, yc-radius, radius*2, radius*2));
		return out;

	}

	/*
	private ImagePlus normalization(ImagePlus imp, double norm) {
		ImageWare psf = Builder.wrap(imp);
		int nx = psf.getWidth();
		int ny = psf.getHeight();
		int nz = psf.getSizeZ();

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
		
		IJ.log(" Max " + max + " at (" + xmax + " " + ymax + " " + zmax + ") == middle (" + (nx/2) + " " + (ny/2) + " "  + (nz/2) +")");
	
		double hmax = max * norm;
		
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
			
		ImagePlus out = new ImagePlus("", psf.buildImageStack()); 
		double zz[] = new double[nz];
		double val[] = new double[nz];
		
		for(int z=0; z<nz; z++) {
			for(int x=0; x<nx; x++)
			for(int y=0; y<ny; y++) 
				val[z] += psf.getPixel(x, y, z);
		}
		
		new Plot("Z integral", "z", "sum", zz, val).show();
		out.setTitle("After Normalization");
		out.setSlice(nz/2);
		out.show();
		return out;
	}

 */
}
