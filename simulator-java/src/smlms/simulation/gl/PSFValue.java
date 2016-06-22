package smlms.simulation.gl;

/**
 * Calculating values of the Gibson and Lanni PSF model
 * 
 **/
public class PSFValue {

	// Gibson & Lanni parameters of the acquisition
	public PSFParameters p;
	
	// Gibson and Lanni parameters
	public double NA = 1.4;
	public double LAMBDA = 500E-9;
	public int MAGNIFICATION = 100;
	public double ZD_STAR = 200E-3;
	public double NI = 1.5;
	public double NS = 1.33;
	public double PIXEL_SIZE = 150E-9;
	public double DELTA_TI = 0;

	public double RELATIVE_DIFFERENCE_SIMPSON = 0.1;
	public int CONSECUTIVE_SUCCESIVE_ITERATIONS = 2;
	
	// Constructor
	public PSFValue(PSFParameters p) { this.p = new PSFParameters(p); }
	
	// calculate()
	// Simpson approximation for the Kirchhoff diffraction integral
	// The real and imaginary parts are approximated separately
	// 'r' is the radial distance of the detector relative to the optical axis in [m].
	// 'zp' is the particle depth relative to the coverslip in [m]
	public double calculate() {
		
		p.calculateConstants();
		
		// Lower and upper limits of the integral
		double a = 0.0;
		double b=Math.min(1, p.ns/p.NA); //1.0
		int N;						// number of sub-intervals
		int k; 						// number of consecutive successful approximations
		double del;					// integration interval
		int iteration;				// number of iterations.
		double curDifference; 		// Stopping criterion
		
		double rho;
		double[] sum = new double[2];
		double[] sumOddIndex = new double[2], sumEvenIndex = new double[2];
		double[] valueX0 = new double[2], valueXn = new double[2];
		double[] value = new double[2];
		
		double curValue = 0.0, prevValue = 0.0;
		
		// Initialization of the Simpson sum (first iteration)
		N=2;
		del=(b-a)/2.0;
		k=0;
		iteration = 1;
		rho = (b-a)/2.0;
		sumOddIndex = this.integrand(rho);
		sumEvenIndex[0] = 0.0; sumEvenIndex[1] = 0.0;
		
		valueX0 = this.integrand(a);
		valueXn = this.integrand(b);
		
		sum[0] = valueX0[0] + 2.0*sumEvenIndex[0] + 4.0*sumOddIndex[0] + valueXn[0];
		sum[1] = valueX0[1] + 2.0*sumEvenIndex[1] + 4.0*sumOddIndex[1] + valueXn[1];
		curValue = (sum[0]*sum[0]+sum[1]*sum[1])*del*del;
		
		prevValue = curValue;

		// Finer sampling grid until we meet the RELATIVE_DIFFERENCE_SIMPSON value with the specified number of repetitions, K
		while(k<CONSECUTIVE_SUCCESIVE_ITERATIONS) {
			iteration++;
			N *= 2;
			del = del/2;
			sumEvenIndex[0] = sumEvenIndex[0] + sumOddIndex[0];
			sumEvenIndex[1] = sumEvenIndex[1] + sumOddIndex[1];
			sumOddIndex[0] = 0.0;
			sumOddIndex[1] = 0.0;
			for(int n=1; n<N; n=n+2) {
				rho = n*del;
				value = this.integrand(rho);
				sumOddIndex[0] += value[0]; 
				sumOddIndex[1] += value[1];
			}
			sum[0] = valueX0[0] + 2.0*sumEvenIndex[0] + 4.0*sumOddIndex[0] + valueXn[0];
			sum[1] = valueX0[1] + 2.0*sumEvenIndex[1] + 4.0*sumOddIndex[1] + valueXn[1];
			curValue = (sum[0]*sum[0]+sum[1]*sum[1])*del*del;
			
			// Relative error between consecutive approximations
			if (prevValue==0.0) curDifference = Math.abs((prevValue-curValue)/1E-5);
			else curDifference = Math.abs((prevValue-curValue)/curValue);
			
			if (curDifference<=RELATIVE_DIFFERENCE_SIMPSON)  k++;
			else k = 0;
			
			prevValue=curValue;
			
			if (iteration>15) {
				System.err.println("Integral not converging after "+iteration+"iterations. The optical parameters are: " + p.toString());
				return curValue;
			}
		}
		
		return curValue;
	}
	
	double[] integrand(double rho) {
		
		// 'rho' is the integration parameter.
		// 'r' is the radial distance of the detector relative to the optical axis in the DETECTOR (i.e. after magnification)
		// The return value is a complex number that is described by an array
		// The relevant equations in the paper are (4) and (5)
		
		double BesselValueRho = Bessel.J0(p.k_a_over_zd*p.r*rho)*rho;
		//double BesselValueRho = Bessel.J0(p.k0*p.NA*r*rho/p.M)*rho;
		
		double OPD_real, OPD_imag, OPD1_real, OPD1_imag, OPD2_real, OPD2_imag, OPD3;	// Optical path differences
		double[] I = new double[2];
		
		// Phase term due to immersion layer thickness
		double X = 1-p.NA_over_ni_squared*rho*rho;
		if (X>=0) {
			OPD1_real = p.ni*(p.delta_ti)*Math.sqrt(X);
			OPD1_imag = 0;
		} else { 
			OPD1_real = 0;
			OPD1_imag = p.ni*(p.delta_ti)*Math.sqrt(-X);
		}
		
		
		// Phase term due to point source depth
		X = 1-p.NA_over_ns_squared*rho*rho;
		
		if (X>0) {
			OPD2_real = p.ns_ts*Math.sqrt(X);
			OPD2_imag = 0;
		} else {
			OPD2_real = 0;
			OPD2_imag = p.ns_ts*Math.sqrt(-X);
		}
				
		
		// Defocus in image plane due to imaging plane displacement
		OPD3 = p.const2*rho*rho;
		
		// See equation page 50, bottom, Gibson thesis and defocus equation page 70, top
		OPD_real = OPD1_real+OPD2_real+OPD3;
		OPD_imag = OPD1_imag+OPD2_imag;
		
		double W = p.k0*OPD_real;
		
		// The real part
		I[0] = BesselValueRho*Math.cos(W)*Math.exp(-p.k0*OPD_imag); // See eq 4.9, pag 53, Gibson thesis

		// The imaginary part
		I[1] = BesselValueRho*Math.sin(W)*Math.exp(-p.k0*OPD_imag);
		
		return I;
	}
}

