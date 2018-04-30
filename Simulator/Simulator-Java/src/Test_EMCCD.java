
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;

import smlms.tools.PsRandom;

public class Test_EMCCD {

	private PsRandom psrand = new PsRandom(1234);
	
	public static void main(String[] arg) {
		new Test_EMCCD();
	}

	public Test_EMCCD() {
		for(double i=1; i<10000; i*=Math.sqrt(2)) {
			double[] v = test(i);
			System.out.println("" + v[0] + "\t" + v[1] + "\t stdev=" + v[2]);
		}
	}
	
	private double[] test(double x) {
		int N = 1000;
		double[] values = new double[N];
		
		for(int i=0; i<N; i++) {
			int xp = poisson(x);
			values[i] = value(xp);
		}
		double mean = 0.0;
		for(int i=0; i<N; i++)
			mean += values[i];
		mean = mean / N;
		double stdev = 0.0;
		for(int i=0; i<N; i++)
			stdev += (values[i]-mean)*(values[i]-mean);
		stdev = Math.sqrt(stdev / N);
		return new double[] {x, mean, stdev};
	}

	private double value(double photons_on_pixel) {			
		// Pseudo-code noise model  for LM challenge 2016.
		// This assumes all input light is fluorescence (background or signal)
		// and thus follows poisson statistics
		// And that the camera is an EMCCD, Photometrics Evolve Delta 512 sitting in my lab (Seamus)
		//for each pixel, n input electrons n_ie input
		// *** gives total system gain G=0.9*300/30 =9
		//model quantization noise not an issue as noted by lots of astronomers and e.g. [1] Hirsch et al, PLoS One 2015
		// poisson noise including shot noise and spurious charge plus binomial quantum efficiency conversion is just a poisson distribution as per [1] 
		// ADC conversion has arbitary value within similar magnitude to typical manufacturer values 
		// EMCCD model, shape param k=n_ie, scale param theta=EMgain after Basden et al Mon Not R Astron Soc 2003
		double QE=0.9; // Evolve quantum efficiency @700 nm
		double readout = 74.4;//maunfacturer measured rms electrons for my Evolve
		double c=0.002; //manufacturer quoted spurious charge (CIC only, dark counts negligible) for my Evolve
		double e_per_edu = 0.01; 
		double baseline = 100;
		double EMgain = 300;
		double n_ie = poisson(QE*photons_on_pixel + c);
		double n_oe = gamma(n_ie, EMgain);
		n_oe = n_oe + gaussian(baseline, readout);
		double ADU_out = (int)(n_oe * e_per_edu) + baseline;
		double saturation = Math.pow(2, 16);
		int dn = (int)Math.floor(Math.min(saturation, Math.max(0, ADU_out)));
		return dn;
		
	}

private double value_old(double photons_on_pixel) {	
	double darknoise = 10;
	double background = 100.0;
	double EMgain = 1.41;
	double QE = 0.921;
	double baseline = 100;
	double readout = 5;
	double saturation = Math.pow(2, 16); // Quantization on 16 bits
	double gainADC = 1.0;
	double offsetADC = 0.0;
	int photons_back = (int)photons_on_pixel + poisson(background);
	double e 		= binomial(QE, photons_back);
	double e_dark 	= e + poisson(darknoise);
	double e_em 	= gamma(e_dark, EMgain);
	double e_read 	= e_em + gaussian(baseline, readout);
	double adc 		= e_read * gainADC + offsetADC;
	int dn 			= (int)Math.floor(Math.min(saturation, Math.max(0, adc)));
	return dn;
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
private double gaussian(double mean, double stdev) {
	return psrand.nextGaussian(mean, stdev);
}




	/*
	System.out.println("Photons on a pixel: " + photons_on_pixel);
	System.out.println("Photons and dark noise: " + photons_back);
	System.out.println("Conversion photon to e: " + e);
	System.out.println("e + dark: " + e_dark);
	System.out.println("e * em: " + e_em);
	System.out.println("e + read: " + e_read);
	System.out.println("Digital Number: " + dn);
	*/

}