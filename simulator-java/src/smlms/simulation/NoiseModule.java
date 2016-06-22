package smlms.simulation;
import ij.IJ;
import imageware.Builder;
import imageware.ImageWare;

import java.io.PrintStream;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;

import smlms.tools.ArrayOperations;
import smlms.tools.Chrono;
import smlms.tools.PsRandom;

public class NoiseModule {
	
	public static String[] names = new String[] { "Read-out noise", "Dark noise", "Shot noise", "EM Gain", "Spurious (CIC)", "Dead pixel"};
	public static String[] distribution = new String[] { "Gaussian (stdev)", "Poisson", "Poisson", "Gamma", "add poisson", "/10e6 of pixel"};

	public static int READOUT 		= 0;
	public static int DARK	 		= 1;
	public static int SHOT	 		= 2;
	public static int EMCCD 		= 3;
	public static int SPURIOUS_CIC	= 4;
	public static int DEADPIXELS	= 5;
	private double quantumEfficiency;
	private boolean noiseEnable[];
	private double noiseParam[];

	private float		gain		 	= 1f;
	private float		offset		 	= 0f;
	private float		baseline	 	= 0f;

	private PsRandom psrand;
	
	public NoiseModule(PsRandom psrand, double gain, double offset, double baseline, double quantumEfficiency, boolean noiseEnable[], double noiseParam[]) {
		this.gain 			= (float)gain;
		this.offset		  	= (float)offset;
		this.baseline 		= (float)baseline;
		this.psrand	  	  	= psrand;
		this.noiseEnable  	= noiseEnable;
		this.noiseParam  	= noiseParam;
		this.quantumEfficiency  = quantumEfficiency;
	}

	public void test(Viewport viewport) {
		int nx = viewport.getFoVXPixel();
		int ny = viewport.getFoVXPixel();
		ImageWare stack = Builder.create(nx, ny, 1, ImageWare.FLOAT);
		for(int f=0; f<1; f++) {
			Chrono.reset();
			float[][] frame = new float[nx][ny];
			for(int i=0; i<nx; i++) {
				for(int j=0; j<ny; j++)
				frame[i][j] = j;
			}
			for(int i=nx/4; i<3*nx/4; i++) {
				for(int j=ny/2-20; j<ny/2; j++)
					frame[i][j] = 100;
				for(int j=ny/2; j<ny/2+20; j++)
					frame[i][j] = 400;
				for(int j=ny/2+20; j<ny/2+40; j++)
					frame[i][j] = 0;
			}
			add_old(frame);
			stack.putXY(0, 0,  f, frame);
			Chrono.print("add noise " + f);
		}
		stack.show("Test Noise SHOT " + noiseParam[SHOT] + " EMCCD " + noiseParam[EMCCD]);
	}
	
	public void add(float[][] frame, double autofluo) {
		
		//double QE=0.9; // Evolve quantum efficiency @700 nm
		double EMgain 	= noiseParam[EMCCD];	// 300
		double readout 	= noiseParam[READOUT];	// 74.4 measured rms electrons for my Evolve
		double c		= noiseParam[SPURIOUS_CIC]; //0.002; //manufacturer quoted spurious charge (CIC only, dark counts negligible) for my Evolve
		double e_per_edu = gain; 
		int nx = frame.length;
		int ny = frame[0].length;
		//float im1[][] = new float[nx][ny];
		//float im2[][] = new float[nx][ny];
		//float im3[][] = new float[nx][ny];
		//float im4[][] = new float[nx][ny];
		//float im5[][] = new float[nx][ny];
		
		for(int i=0; i<nx; i++)
		for(int j=0; j<ny; j++) {
			double photons = frame[i][j];
			//im1[i][j] = (float)photons; 
			double n_ie = noiseEnable[SPURIOUS_CIC] ? poisson(quantumEfficiency*(photons+autofluo) + c) : quantumEfficiency*photons;
			//im2[i][j] = (float)n_ie; 
			double n_oe = noiseEnable[EMCCD] ? gamma(n_ie, EMgain) : n_ie;
			//im3[i][j] = (float)n_oe; 
			n_oe += noiseEnable[READOUT] ? gaussian(readout) : 0;
			//im4[i][j] = (float)n_oe; 
			double ADU_out = (int)(n_oe / e_per_edu) + baseline;
			//im5[i][j] = (float)ADU_out; 
			frame[i][j] = (float)ADU_out;
		}
		
		//Builder.create(im1).show("in-photons");
		//Builder.create(im2).show("n_ie");
		//Builder.create(im3).show("n_oe");
		//Builder.create(im4).show("n_oe + gaussian");
		//Builder.create(im5).show("ADU_out");
		
		
	}

	private double gamma(double shape, double scale) {
		shape = Math.max(1E-6, shape);
		return new GammaDistribution(shape, scale).sample();
	}
	private int poisson(double lambda) {
		lambda = Math.max(1E-6, lambda);
		return new PoissonDistribution(lambda).sample();
	}
	private double binomial(double p, int ntrials) {
		return psrand.nextBinomial(p, ntrials);
	}
	private double gaussian(double stdev) {
		return psrand.nextGaussian(0, stdev);
	}


	public void add_old(float[][] frame) {
		int n = frame.length;
		int m = frame[0].length;
		
		
		ArrayOperations.multiply(frame, (float)quantumEfficiency);
		// signal
		if (noiseEnable[SHOT]) {
			//float norm = ArrayOperations.getNorm(frame);
			float factor = 1f / (float)(noiseParam[SHOT]);
			float norm = 1f;
			ArrayOperations.multiply(frame, factor/norm);
			poissonNoise(frame);
			//float norm1 = ArrayOperations.getNorm(frame);
			//ArrayOperations.multiply(frame, norm/norm1);
			ArrayOperations.multiply(frame, (float)(noiseParam[SHOT]));
		}
				
		if (noiseEnable[EMCCD]) {
			ArrayOperations.linear(frame, (float)noiseParam[EMCCD]*gain, offset);
		}
		else {
			ArrayOperations.linear(frame, gain, offset);
		}

		// dark current
		float dark[][] = new float[n][m];
		if (noiseEnable[READOUT])
			gaussianNoise(dark, baseline, noiseParam[READOUT]);
		else
			ArrayOperations.fill(dark, baseline);
		
		if (noiseEnable[DARK]) {
			//float norm = ArrayOperations.getNorm(dark);
			float norm = 1f;
			float factor = 1f / (float)(noiseParam[DARK]);
			ArrayOperations.multiply(dark, factor/norm);
			poissonNoise(dark);
			//float norm1 = ArrayOperations.getNorm(dark);
			//ArrayOperations.multiply(dark, norm/norm1);
			ArrayOperations.multiply(dark, (float)(noiseParam[DARK]));
		}
		ArrayOperations.increment(frame, dark);
		

	}
		
	// y = a * (x-b)
	// y/a + b = x
	// x = (1/a) * y + a *(1/a) * (b)
	// x = (1/a) * (y + a * b)
	/*
	public void rescale(float[][] arr, float a, float b) {
		int n = arr.length;
		for(int y=0;y<n;y++)
		for(int x=0;x<n;x++)
			arr[x][y] = a*(arr[x][y]-b);
	}
	*/
	public void poissonNoise(float frame[][]) {
		int n = frame.length;
		for(int y=0;y<n;y++)
		for(int x=0;x<n;x++) {
			frame[x][y] = (float)nextPoisson(frame[x][y]); 
		}
	}
	
	public void gaussianNoise(float frame[][], double mean, double stdev) {
		int n = frame.length;
		for(int y=0;y<n;y++)
		for(int x=0;x<n;x++)
			frame[x][y] += (float)psrand.nextGaussian(mean, stdev); 
	}

	public void gaussianNoisePositive(float frame[][], double mean, double stdev) {
		int n = frame.length;
		double g = 0;
		for(int y=0;y<n;y++)
		for(int x=0;x<n;x++) {
			g = (float)psrand.nextGaussian(mean, stdev);
			frame[x][y] += (g > 0 ? g : 0.0);
		}
	}

	public double nextPoisson(double lambda) {
		if (lambda==0) 
			return 0.0;
		
		if (lambda<100) {
			//  Knuth algorithm
			double L = Math.exp(-lambda);
			int k = 0;
			double p = 1;
			do {
				k++;
				p *= psrand.nextDouble();
			} while (p >= L);
			return (double)(k - 1);
		} 
		else {
			// Gaussian distribution which approximates the Poisson one for large lambda values
			double value = (psrand.nextGaussian()*Math.sqrt(lambda))+lambda;
			return value;
		}
	}

	public void report(PrintStream out) {	
		out.print("<h2>Noise</h2>");
		out.print("<table cellpadding=5>");	
		for(int i=0; i<names.length; i++) {
			String param = " " + noiseParam[i];
			if (noiseEnable[i])
				out.print("<tr><td></td><td>" + names[i] + 
					"</td><td> Enable &bull;" + param + " (" + distribution[i] + ")</td><td></td></tr>");  
			else
				out.print("<tr><td></td><td>" + names[i] + 
						"</td><td> None</td><td></td></tr>");  
				
		}
		out.print("</table>");
	}

}
