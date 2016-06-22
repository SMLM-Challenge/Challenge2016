package bpalm.simulator;

import ij.IJ;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import smlms.simulation.PSFModule;
import bpalm.model.KirchhoffDiffractionSimpson;


/**
 * This class manages all aspects of the algorithm for generating the data.
 * 
 * @author Hagai Kirshner, Biomedical Imaging Group, Ecole Polytechnique Federale de Lausanne (EPFL)
 */
public class BiplaneAlgorithm {
	
	public BPALMParameters p;
	public double[][][]  zStack;
	public double effRadius;
	static int OVER_SAMPLING = 2;
	
	static boolean verbose = false;
	
	SliceOfPSF[] psfSlice;
	private boolean columnwise;

	public BiplaneAlgorithm(PSFModule module) {
		columnwise = module.bpalm.orientation.equals("Columns");
		this.p = new BPALMParameters(module.bpalm); // Making a copy of the Parameters
	}
			
	public int getSliceIndex(Particle particle, BPALMParameters p) {
		int i = (int)(Math.ceil(particle.t*p.noFrames)-1);
		
		return (i<0)?0:(i<=p.noFrames)?i:p.noFrames-1;
	}
	
	public void renderSequence(Particle[] particle, float image[][]) {
		p.calculateConstants();
		int nx = image.length;
		int ny = image[0].length;
		// Determine the effective radius by the most out of focus particle.
		effRadius = getEffectiveRadius(nx, ny);
IJ.log(" DEBUG renderSequence line 56 " + nx + " " + ny + " nframes " + p.noFrames + " " + p.doSequence + effRadius);	
		// generate the psf pattern of the various particles

		psfSlice = new SliceOfPSF[particle.length*(p.doSplit?2:1)];
		double tmp1 = p.delta_z;
		double tmp2 = p.delta_z2;
		
		if (p.doSplit) {
			Particle[] particle2 = generateSecondPlaneParticles(particle, nx, ny);
			p.delta_z = tmp1; 
			p.calculateConstants();
			for (int n=0;n<particle2.length;n++)
				psfSlice[particle.length+n] = new SliceOfPSF(image, p, getSliceIndex(particle2[n],p), particle2[n], 3.0*effRadius);
			p.delta_z = tmp2;
			p.calculateConstants();
			for (int n=0;n<particle.length;n++)
				psfSlice[n] = new SliceOfPSF(image, p, getSliceIndex(particle[n],p), particle[n], 3.0*effRadius);
		} 
		else {
			for (int n=0;n<particle.length;n++)
				psfSlice[n] = new SliceOfPSF(image, p, getSliceIndex(particle[n],p), particle[n],3.0*effRadius);
		}
		// Threading the slices
		boolean multithread = true;
		if (multithread) {
			ExecutorService executor = Executors.newFixedThreadPool(
					Runtime.getRuntime().availableProcessors());
			for(SliceOfPSF s : psfSlice)
				executor.execute(s);
			executor.shutdown();
			while (!executor.isTerminated()) {}
		}
		else
			for(SliceOfPSF s : psfSlice)
				s.run();

		
		// Making sure the pixels are within the required range
		//normalize(p.maxValue);
		
		// Scaling the pixel values
		//intensityScale();
	
	}
	
	public Particle[] generateSecondPlaneParticles(Particle[] particle, int nx, int ny) {
		Particle[] particle2 = new Particle[particle.length];
		for (int i=0; i<particle.length; i++) 
			particle2[i] = new Particle(particle[i]);
		
		double rotRadians = p.rotation/180.0*Math.PI;
		double cosa = Math.cos(rotRadians) * p.scale ;
		double sina = Math.cos(rotRadians) * p.scale;
		if (columnwise) { // COLUMNWISE
			for (int n=0;n<particle.length; n++) {
				double dx = p.dx/p.pixelSize + nx/2.0;
				double dy = p.dy/p.pixelSize;
				particle[n].x =  particle[n].x/2.0; 
				particle2[n].x =   particle[n].x*cosa + particle[n].y*sina + dx;
				particle2[n].y =  -particle[n].x*sina + particle[n].y*cosa + dy;
			}
		}
		else { // ROWISE
			for (int n=0;n<particle.length; n++) {
				double dx = p.dx/p.pixelSize;
				double dy = p.dy/p.pixelSize + ny/2.0;
				particle[n].y =  particle[n].y/2.0;
				particle2[n].x =  particle[n].x*cosa + particle[n].y*sina + dx;
				particle2[n].y =  -particle[n].x*sina + particle[n].y*cosa + dy;
			}
		}
	
		return particle2;
	}
	double getEffectiveRadius(int nx, int ny) {
		// The return value is in units of [pixels]
		
		double x0 = (nx-1)/2, y0 = (ny-1)/2; 
		int maxRadius = ((int) Math.round(Math.sqrt((nx-x0)*(nx-x0)+(ny-y0)*(ny-y0))))+1;
		double[] r = new double[maxRadius*OVER_SAMPLING];
		double[] h = new double[r.length];
		double sum=0.0, sigma2=0.0;
		
		p.ti = p.ti0;
		p.ts = p.thick;
		p.delta_z = 0;
		p.calculateConstants();
		KirchhoffDiffractionSimpson I = new KirchhoffDiffractionSimpson(p,p.accuracy);
		for (int n=0; n<r.length; n++) {
			r[n] = ((double)n)/((double)OVER_SAMPLING);
			h[n] = I.calculate(r[n]*p.pixelSize*p.M);
			sum += h[n];
			sigma2 += h[n]*r[n]*r[n];
		}

		return Math.sqrt(sigma2/sum); 
	}
	

}