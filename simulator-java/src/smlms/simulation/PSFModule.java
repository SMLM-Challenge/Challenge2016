package smlms.simulation;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.process.ImageProcessor;
import imageware.Builder;
import imageware.ImageWare;

import java.io.File;

import smlms.file.Fluorophore;
import smlms.file.Fluorophores;
import smlms.simulation.defocussed2dfunction.Airy;
import smlms.simulation.defocussed2dfunction.Astigmatism;
import smlms.simulation.defocussed2dfunction.Cosine;
import smlms.simulation.defocussed2dfunction.Defocussed2DFunction;
import smlms.simulation.defocussed2dfunction.DoubleHelix;
import smlms.simulation.defocussed2dfunction.ElongatedGaussian;
import smlms.simulation.defocussed2dfunction.Gaussian;
import smlms.simulation.defocussed2dfunction.Linear;
import smlms.simulation.defocussed2dfunction.Lorentz;
import smlms.simulation.defocussed2dfunction.Rectangle;
import smlms.simulation.defocussed2dfunction.ZFunction;
import smlms.simulation.gl.PSFParameters;
import smlms.simulation.gl.PSFValue;
import smlms.tools.Chrono;
import smlms.tools.Point3D;
import smlms.tools.Tools;
import smlms.tools.Verbose;
import bpalm.simulator.BPALMParameters;
import bpalm.simulator.BiplaneAlgorithm;
import bpalm.simulator.Particle;

public class PSFModule {
	
	static public int GAUSSIAN		 	= 0;
	static public int LORENTZ 			= 1;
	static public int AIRY 				= 2;
	static public int COSINE 			= 3;
	static public int LINEAR		 	= 4;
	static public int RECTANGLE 		= 5;
	static public int PIXELWISE			= 6;
	static public int ASTIGMATISM	 	= 7;
	static public int ROTATED_GAUSSIAN 	= 8;
	static public int DOUBLE_HELIX	 	= 9;
	static public int GIBSON_LANNI 		= 10;
	static public int BIPLANE		 	= 11;
	static public int FILE		 		= 12;
	
	static public String[] namesXY 	 = new String[] { "Gaussian", "Lorentz", "Airy", "Cosine", "Linear", "Rectangle", "Pixelwise", "Astigmatism", "Elongated Gaussian (Steer)", "Double Helix (Steer)"};
	static public String[] namesXYZ  = new String[] {"Gibson and Lanni"};

	private int psf;
	private ZFunction zfunction;
	private double fwhm;
	private Viewport viewport;
	
	private PSFParameters pgl;	// gibson-lanni
	private double lineGL[][];
	
	private ImageWare psfArray;
	private String name = "Noname";
	private double intDensityPSF = 1.0;
	
	public BPALMParameters bpalm;
	public boolean biplaneAffine = false;
	
	public double affineDX = 0;
	public double affineDY = 0;
	public double affineScale = 0;
	public double affineRotation = 0;
	
	public PSFModule(double fwhm, Viewport viewport) {
		this.name = "Point";
		this.psf = PIXELWISE;
		this.fwhm = fwhm;
		this.viewport = viewport; 
	}

	public PSFModule(double fwhm, Viewport viewport, ZFunction zfunction, int psf) {
		this.name = "Point";
		this.psf = psf;
		this.zfunction = zfunction; 
		this.fwhm = fwhm;
		this.viewport = viewport; 
		this.name = namesXY[psf] + "-" + zfunction.getName();
	}

	public PSFModule(double fwhm, Viewport viewport, PSFParameters pgl) {
		this.name = "G&L";
		this.psf = GIBSON_LANNI;
		this.fwhm = fwhm;
		this.pgl = pgl;
		this.viewport = viewport; 
		lineGL = (psf == GIBSON_LANNI ? initPSF_GibsonLanni(viewport): null);
	}

	public PSFModule(double fwhm, Viewport viewport, BPALMParameters bpalm) {
		this.name = "Biplane";
		this.psf = BIPLANE;
		this.fwhm = fwhm;
		this.bpalm = bpalm;
		this.viewport = viewport;
	}

	public PSFModule(String filename, Viewport viewport) {
		if (!(new File(filename).exists()))
			IJ.log("" + filename );
		this.name = new File(filename).getName();
		if (name.endsWith(".tif"))
			name = name.substring(0, name.length()-4);
		this.intDensityPSF = 0;
		this.psf = FILE;
		this.viewport = viewport;
		ImagePlus imp = new Opener().openImage(filename);
		psfArray = null;
		if (imp != null) {
			int nx = imp.getWidth();
			int ny = imp.getHeight();
			int nz = imp.getStackSize();
			psfArray = Builder.create(nx, ny, nz, ImageWare.FLOAT);
			for(int k=0; k<nz; k++) {
				ImageProcessor ip = imp.getStack().getProcessor(k+1);
				for(int i=0; i<nx; i++)
				for(int j=0; j<ny; j++) {
					double v = ip.getPixelValue(i, j);
					intDensityPSF += v;
					psfArray.putPixel(i, j, k, v);
				}
			}
		}

		if (name.startsWith("BP500")) {
			biplaneAffine = true;
			IJ.log("affineDX " + affineDX);
			IJ.log("affineDY " + affineDY);
			IJ.log("affineRotation " + affineRotation);
			IJ.log("affineScale " + affineScale);
		}
		IJ.log("Integral Density of " + getName() + " : "+ intDensityPSF );
	}
	
	public String getInfoArrayPSF() {
		return "" + psfArray.getWidth() + " " + psfArray.getHeight() + " " + psfArray.getDepth();
	}
	
	public double getFWHM() {
		return fwhm;
	}

	public String getName() {
		return name;
	}
	
	public Fluorophores convolve(Fluorophores fluorophores, float[][] image, double fwhm) {
		Fluorophores fluorophoresProcessed = new Fluorophores();
		Fluorophores fluos = new Fluorophores();
		if (biplaneAffine) {
			double rotRadians = affineRotation/180.0*Math.PI;
			double cosa = Math.cos(rotRadians) * affineScale;
			double sina = Math.sin(rotRadians) * affineScale;
			for(Fluorophore fluo : fluorophores) {
				Fluorophore a = new Fluorophore(0, fluo.x, fluo.y, fluo.z, fluo.frame, fluo.photons);
				a.x =  fluo.x*cosa + fluo.y*sina + affineDX;
				a.y = -fluo.x*sina + fluo.y*cosa + affineDY;
				fluos.add(a);
			}
			//IJ.log("affineDX " + affineDX + "  affineDY:" + affineDY + " affineRotation:" + affineRotation + " affineScale:" + affineScale + " " + cosa + " " + sina);
		}
		else {
			for(Fluorophore fluo : fluorophores)
				fluos.add(fluo);
		}
		Verbose.talk("Convolve (biplane:" + (biplaneAffine) + ") nb:" + fluos.size() + " fluos with PSF: " + getName() +  (fluos.size() > 0 ? " frame:" + fluos.get(0).frame : ""));
		
		for(Fluorophore fluo : fluos) {
			if (viewport.insideXY(fluo)) {
				fluorophoresProcessed.add(fluo);
				if (psf == PIXELWISE)
					convolvePSF_Pixelwise(fluo, viewport, image);	
				else if (psf == GIBSON_LANNI)
					convolvePSF_GibsonLanni(fluo, viewport, image);
				else if (psf == FILE)
					convolvePSF_File(fluo, viewport, image, fwhm);
				else if (psf == BIPLANE)
					fluorophoresProcessed.add(fluo);					
				else 
					convolveDefocussed2DFunction(fluo, viewport, image);
			}
		}	
		//if (psf == BIPLANE)
		//	convolveBPALM(fluorophores, viewport, image);

		return fluorophoresProcessed;
	}
	
	/**
	 * Create a 3D PSF at the resolution pixelsize from z=0 to z=thickness
	 * @param pixelsize	Resolution, pixelsize in nm
	 * @param thickness	in nm
	 * @param focal	in nm
	 */
	public ImageWare test(double beadSizeNM, double pixelsize, double focalPlaneNM) {
		if (psf == FILE)
			return psfArray;	
		int radiusPixel = (int)( (0.5*beadSizeNM) / pixelsize) + 1;
		IJ.log(" \n radiusPixel" + radiusPixel);
		double supportnm = 50 * radiusPixel * pixelsize;
		double thickness = viewport.getThicknessNano();
		Viewport viewportTest = new Viewport(new Point3D(0, 0, 0), supportnm, supportnm, thickness, pixelsize);
		int nx = viewportTest.getFoVXPixel();
		int ny = viewportTest.getFoVYPixel();
		int nz = viewportTest.getThicknessPixel();
		int nt = nz;
		ImageWare vol = Builder.create(nx, ny, nz, ImageWare.FLOAT);	
		Chrono.reset();
		Fluorophores fluorophores[] = new Fluorophores[nt];
		double stepNM = radiusPixel * pixelsize * 0.333;
		IJ.log(" \n radiusPixel" + radiusPixel + "  stepNM " + stepNM );
		
		for(int t=0; t<nt; t++) {
			fluorophores[t] = new Fluorophores();
			for(int i=-3; i<=3; i++)
			for(int j=-3; j<=3; j++)
			for(int k=-3; k<=3; k++) {
				double d = Math.sqrt(i*i + j*j + k*k) * stepNM;
				if (d <= beadSizeNM + 1) {
					double znano = focalPlaneNM + k*stepNM + t*pixelsize;
					Fluorophore fluo = new Fluorophore(fluorophores[t].size()+1, supportnm*0.5 + i*stepNM, supportnm*0.5+j*stepNM, znano, 0, 1);
					fluorophores[t].add(fluo);
				}
			}
		}
		for(int z=0; z<nz; z++) {
			float[][] image = new float[nx][nx];
			convolve(fluorophores[z], image, 25);
			vol.putXY(0, 0, z, image);
		}
		Chrono.print(getName());
		return vol;
	}

	private void convolveBPALM1(Fluorophores fluorophoresProcessed, Viewport viewport, float[][] image) {
		// read the parameters from all tabs.
		double px = viewport.getPixelsize();
		
		Particle particle[] = new Particle[fluorophoresProcessed.size()];
		
		for(int i=0; i<fluorophoresProcessed.size(); i++) {
			particle[i] = new Particle();
			Fluorophore fluo = fluorophoresProcessed.get(i); 
			particle[i].x = fluo.x / px;
			particle[i].y = fluo.y / px;
			particle[i].z = fluo.z * 1e-9;
		}
			
		bpalm.calculateConstants();
		bpalm.doSequence = true;
		bpalm.calculateConstants();
		BiplaneAlgorithm ma = new BiplaneAlgorithm(this);
		
		ma.renderSequence(particle, image);
	}


	private void add(int i, int j, float [][] image, float value) {
		if (i<0)
			return;
		if (j<0)
			return;
		if (i>=image.length)
			return;
		if (j>=image[0].length)
			return;
		image[i][j] += value;
	}

	
	/**
	 * Dummy convolve with a dirac, impulse to display the fluorophores
	 */
	public void convolvePSF_Pixelwise(Fluorophore fluo, Viewport viewport, float[][] image) {
		int n = image.length-1;
		Verbose.prolix("Fluorophore Pixelwise " + fluo.toString());
		double A = fluo.photons;
		double xpix = viewport.screenX(fluo.x);
		double ypix = viewport.screenY(fluo.y);
		int xi = Tools.round(xpix);
		int yi = Tools.round(ypix);
		if (xi > 0 && yi > 0 && xi < n && yi < n)
			image[xi][yi] += A; 
	}

	/**
	 */
	public void convolvePSF_File(Fluorophore fluo, Viewport viewport, float[][] image, double fwhm) {
		int nx = psfArray.getWidth();
		int ny = psfArray.getHeight();
		int nz = psfArray.getDepth();
		int hx = nx / 2;
		int hy = ny / 2;
		Verbose.prolix("Fluorophore Pixelwise " + fluo.toString() + " " + hx  + " " + hy);
		
		
		
		double norm = 1.0;
		
		double A = fluo.photons * norm;
		double xpix = viewport.screenX(fluo.x) - hx - 0.5;
		double ypix = viewport.screenY(fluo.y) - hy - 0.5;
		double zpix = viewport.screenZ(fluo.z);		
		int xi = (int)(xpix);
		int yi =  (int)(ypix);
		if (zpix>=0 && zpix<nz) {
			for(int i=xi; i<xi+nx; i++)
				for(int j=yi; j<yi+ny; j++) {
					double v = psfArray.getInterpolatedPixel(-xpix+i, -ypix+j, zpix, ImageWare.MIRROR);
					add(i, j, image, (float)(v*A));
				}
		}
	}

	/**
	 * Convolution in the plane domain with a circular function.
	 */
	public void convolveDefocussed2DFunction(Fluorophore fluo, Viewport viewport, float[][] image) {
		int n = image.length;

		Chrono.reset();
		double defocusFactor = zfunction.getDefocusFactor(fluo.z);
		// XY, 2*sqrt(2*ln(2)) = 2.35482005, fwmh = 2*sqrt(2*ln(2)) * sigma	
		double radiusPix = viewport.convertPixel(fwhm) / 2.35482005;
		Defocussed2DFunction func = null;
		if (psf == GAUSSIAN)
			func = new Gaussian(radiusPix, defocusFactor);
		else if (psf == LORENTZ)
			func = new Lorentz(radiusPix, defocusFactor);	
		else if (psf == AIRY)
			func = new Airy(radiusPix, defocusFactor);		
		else if (psf == COSINE)
			func = new Cosine(radiusPix, defocusFactor);		
		else if (psf == LINEAR)
			func = new Linear(radiusPix, defocusFactor);		
		else if (psf == RECTANGLE)
			func = new Rectangle(radiusPix, defocusFactor);		
		else if (psf == ASTIGMATISM)
			func = new Astigmatism(radiusPix, defocusFactor);
		else if (psf == ROTATED_GAUSSIAN)
			func = new ElongatedGaussian(radiusPix, defocusFactor);
		else if (psf == DOUBLE_HELIX)
			func = new DoubleHelix(radiusPix, defocusFactor);
		else 
			return;	
		int support = func.getSupport();
		if (support < 1)
			return;
		double xpix = viewport.screenX(fluo.x);
		double ypix = viewport.screenY(fluo.y);

		int xi = Tools.round(xpix);
		int yi = Tools.round(ypix);
		
		double xc = xpix - xi - 0.5;
		double yc = ypix - yi - 0.5;
		int h = support/2;
		
		if (h > n) {
			h = n;
		}
		double array[][] = new double[2*h+1][2*h+1];
		double sum = 1.0;
	
		for(int i=-h; i<=h; i++)
		for(int j=-h; j<=h; j++) {
			double v = func.eval(xc-i, yc-j);
			sum += v;
			array[i+h][j+h] = v;
		}
		
		if (sum > 0) {
			double norm = fluo.photons / sum;
			for(int i=-h; i<=h; i++)
			for(int j=-h; j<=h; j++)
				add(xi+i, yi+j, image, (float)(array[i+h][j+h]*norm));
		}
	}	

	public double[][] initPSF_GibsonLanni(Viewport viewport) {
		PSFValue psfvalue = new PSFValue(pgl);
		double radiusPix = 10.0*viewport.convertPixel(fwhm);
		double pixelsize = viewport.getPixelsize() * 1E-9;
		int np = (int)Math.ceil((radiusPix*Math.sqrt(2)+1)*pgl.oversamplingLateral);
		int nz = viewport.getThicknessPixel() * pgl.oversamplingAxial + 1;
		double[] r = new double[np];
		for (int nn=0; nn<np; nn++)
			r[nn] = nn/pgl.oversamplingLateral;
		
		double[][] lineGL = new double[np][nz];
		for(int z=0; z<nz; z++) {
			psfvalue.p.zp = z * viewport.getPixelsize() * 1E-9 / pgl.oversamplingAxial;
			psfvalue.p.yd = 0;
			for (int nn=0; nn<r.length; nn++) {
				psfvalue.p.xd = r[nn] * pixelsize;
				lineGL[nn][z] = psfvalue.calculate();
			}
		}
		return lineGL;
	}

	/**
	 * Gibson and Lanni PSF convolution.
	 */
	public void convolvePSF_GibsonLanni(Fluorophore fluo, Viewport viewport, float[][] image) {
		
		PSFValue psfvalue = new PSFValue(pgl);
		double A[][] = pgl.affineTransformA;
		double B[] = pgl.affineTransformB;
		
		double xc = viewport.getFoVXPixel() * 0.5;
		double yc = viewport.getFoVYPixel() * 0.5;
		double xa = viewport.convertPixel(fluo.x - B[0]) - xc;
		double ya = viewport.convertPixel(fluo.y - B[1]) - yc;
		
		double xt = viewport.convertNano(A[0][0]*xa + A[1][0]*ya + xc);
		double yt = viewport.convertNano(A[0][1]*xa + A[1][1]*ya + yc);

		psfvalue.p.xp = xt * 1E-9;
		psfvalue.p.yp = yt * 1E-9;
		psfvalue.p.zp = fluo.z * 1E-9;

		double radiusPix = 10.0*viewport.convertPixel(fwhm);
			
		int nx = image.length;
		int ny = image[0].length;
		
		int nz = viewport.getThicknessPixel() * pgl.oversamplingAxial + 1;
		int zi = (int)Math.floor(nz * fluo.z * pgl.oversamplingAxial / viewport.getThicknessNano());
				
		double xp = viewport.screenX(xt-viewport.getPixelsize()*0.5);
		double yp = viewport.screenY(yt-viewport.getPixelsize()*0.5);
		
		int np = lineGL.length;
		
		double[] r = new double[np];
		for (int nn=0; nn<np; nn++)
			r[nn] = nn/pgl.oversamplingLateral;
		
		//IJ.log(" zi " + zi + " " + lineGL[0].length +  " " + Math.max(0, Math.min(zi, lineGL[0].length-2)));
		zi = Math.max(0, Math.min(zi, lineGL[0].length-2));
/*
		int xLow = Math.max(0,(int)Math.ceil(xp-radiusPix)); 
		int xHigh = Math.min(nx-1,(int)Math.floor(xp+radiusPix));
		int yLow = Math.max(0,(int)Math.ceil(yp-radiusPix)); 
		int yHigh = Math.min(ny-1,(int)Math.floor(yp+radiusPix));
		for (int x=xLow; x<xHigh; x++)
		for (int y=yLow; y<yHigh; y++) {
			double rPixel = Math.sqrt((x-xp)*(x-xp)+(y-yp)*(y-yp));	// radius of the current pixel in units of [pixels]
			int index = (int)(rPixel*pgl.oversamplingLateral);
			image[x][y] += lineGL[index][zi] + (lineGL[index+1][zi]-lineGL[index][zi])*(rPixel-r[index])*pgl.oversamplingLateral;	// Interpolated value.
		}
*/
		int x1 = Math.max(0,(int)Math.ceil(xp-radiusPix)); 
		int x2 = Math.min(nx-1,(int)Math.floor(xp+radiusPix));
		int px = x2 - x1;
		int y1 = Math.max(0,(int)Math.ceil(yp-radiusPix)); 
		int y2 = Math.min(ny-1,(int)Math.floor(yp+radiusPix));
		int py = y2 - y1;
		double array[][] = new double[px][py];
		double sum = 0;
		for (int x=0; x<px; x++)
		for (int y=0; y<py; y++) {
			double rpixel = Math.sqrt((x+x1-xp)*(x+x1-xp)+(y+y1-yp)*(y+y1-yp));
			int index = (int)(rpixel*pgl.oversamplingLateral);
			array[x][y] = lineGL[index][zi] + (lineGL[index+1][zi]-lineGL[index][zi])*(rpixel-r[index])*pgl.oversamplingLateral;	// Interpolated value.
			sum += array[x][y];
		}
		if (sum > 0) {
			double norm = fluo.photons / sum;
			for (int x=0; x<px; x++)
			for (int y=0; y<py; y++) {
				add(x1+x, y1+y, image, (float)(array[x][y]*norm));
			}
		}
		
		/*
		int n = image.length;
		
		for (int x=-h; x<=h; x++)
		for (int y=-h; y<=h; y++) {
			double rPixel = Math.sqrt((x-xp)*(x-xp)+(y-yp)*(y-yp));	// radius of the current pixel in units of [pixels]
			int index = (int)(rPixel*pgl.oversamplingLateral);
			try{double value = lineGL[index][zi] + (lineGL[index+1][zi]-lineGL[index][zi])*(rPixel-r[index])*pgl.oversamplingLateral;	// Interpolated value.
			add(xi+x, yi+y, n, image, (float)(value));} 
			catch(Exception e) {IJ.log("" + x + " " + y + " " + index + " " + zi + r.length);}
			
		}
	*/
	}	

	/**
	 * Gibson and Lanni PSF convolution.
	 */
	public void convolvePSF_GibsonLanni_ContinuousZ(Fluorophore particle, Viewport viewport, float[][] image) {
		
		PSFValue psfvalue = new PSFValue(pgl);
		psfvalue.p.xp = particle.x * 1E-9;
		psfvalue.p.yp = particle.y * 1E-9;
		psfvalue.p.zp = particle.z * 1E-9;
		
		//int nz = viewport.getThicknessPixel();
		//int zi = (int)Math.floor(nz * particle.znano / viewport.getThicknessNano());
	
		double radiusPix = 8*viewport.convertPixel(fwhm);
		double pixelsize = viewport.getPixelsize() * 1E-9;
		
		int nx = image.length;
		int ny = image[0].length;
		int np = (int)Math.ceil((radiusPix*Math.sqrt(2)+1)*pgl.oversamplingLateral);
		
		psfvalue.p.yd = psfvalue.p.yp;
		double[] r = new double[np];
		double[] h = new double[np];
		for (int nn=0; nn<np; nn++) {
			r[nn] = nn/pgl.oversamplingLateral;
			psfvalue.p.xd = psfvalue.p.xp + r[nn] * pixelsize;
			h[nn] = psfvalue.calculate();
		}
				
		double xp = viewport.screenX(particle.x);
		double yp = viewport.screenY(particle.y);
		
		int xLow = Math.max(0,(int)Math.ceil(xp-radiusPix)); 
		int xHigh = Math.min(nx-1,(int)Math.floor(xp+radiusPix));
		int yLow = Math.max(0,(int)Math.ceil(yp-radiusPix)); 
		int yHigh = Math.min(ny-1,(int)Math.floor(yp+radiusPix));
		for (int x=xLow; x<xHigh; x++)
		for (int y=yLow; y<yHigh; y++) {
			double rPixel = Math.sqrt((x-xp)*(x-xp)+(y-yp)*(y-yp));	// radius of the current pixel in units of [pixels]
			int index = (int)(rPixel*pgl.oversamplingLateral);	
			image[x][y] += h[index] + (h[index+1]-h[index])*(rPixel-r[index])*pgl.oversamplingLateral;	// Interpolated value.	
		}
	}	

	public String toString() {
		return getName() + " FWHM:" + IJ.d2s(fwhm);
	}

	public void storeIllustration(ImageWare psf, String path, double zfocalnm) {
		int nx = psf.getWidth();
		int ny = psf.getHeight();
		int nz = psf.getDepth();
		double max = psf.getMaximum();
		int zfocal = (int)Math.floor(nz*zfocalnm); 
		
		ImageWare image = Builder.create(nx+4, ny + nz + 6, 1, ImageWare.FLOAT);
		image.fillConstant(max);
		
		for(int i=0; i<nx; i++)
		for(int j=0; j<ny; j++)
			image.putPixel(i+2, j+2,  0, psf.getPixel(i, j, zfocal));
		for(int i=0; i<nx; i++)
		for(int k=0; k<nz; k++)
			image.putPixel(i+2, k+ny+4,  0, psf.getPixel(i, ny/2, k));
		
		ImagePlus imp = new ImagePlus("PSF XY and YZ", image.buildImageStack());
		imp.show();
	    WindowManager.setTempCurrentImage(imp); 
	    IJ.run("Fire");
		
		new FileSaver(imp).saveAsPng(path + "illustration.png");
	}
	
	public void storeSlices(ImageWare psf, String path) {
		int nz = psf.getDepth();
		ImagePlus imp = new ImagePlus("",  psf.buildImageStack());
		for(int z=0; z<nz; z++)
			new FileSaver(new ImagePlus("", imp.getStack().getProcessor(z+1))).saveAsTiff(path + "z-" + Tools.format(z) + ".tif");
	}

}
