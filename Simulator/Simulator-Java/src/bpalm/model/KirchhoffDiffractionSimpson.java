package bpalm.model;



/**
 * Kirchhoff Diffraction integral formula for the Gibson and Lanni PSF model
 * 
 * @author Hagai Kirshner, Biomedical Imaging Group, Ecole Polytechnique Federale de Lausanne (EPFL)
 */
public class KirchhoffDiffractionSimpson{

	// Gibson & Lanni parameters of the acquisition
	private GibsonLanniParameters p;
	
	// Stopping conditions:
	// Difference between consecutive Riemann approximations.
	double TOL = 1E-1;
	// The number of consecutive approximations that meet the TOL criterion
	int K;
	

	// Constructor
	public KirchhoffDiffractionSimpson(GibsonLanniParameters p, int accuracy) {
		this.p = new GibsonLanniParameters(p);
		this.p.calculateConstants();
		if (accuracy == 0) K = 5;
		else if (accuracy == 1) K = 7;
		else if (accuracy == 2) K = 9;
		else K = 3;
	}
	
	// calculate()
	// Simpson approximation for the Kirchhoff diffraction integral
	// 'r' is the radial distance of the detector relative to the optical axis.
	public double calculate(double r) {
		
		// Lower and upper limits of the integral
		double a = 0.0;
		double b=Math.min(1, p.ns/p.NA); //1.0
		int N;						// number of sub-intervals
		int k; 						// number of consecutive successful approximations
		double del;					// integration interval
		int iteration;				// number of iterations.
		double curDifference; 		// Stopping criterion
		
		double realSum, imagSum, rho;
		double[] sumOddIndex = new double[2], sumEvenIndex = new double[2];
		double[] valueX0 = new double[2], valueXn = new double[2];
		double[] value = new double[2];
		
		double curI = 0.0, prevI = 0.0;
		
		// Initialization of the Simpson sum (first iteration)
		N=2;
		del=(b-a)/2.0;
		k=0;
		iteration = 1;
		rho = (b-a)/2.0;
		sumOddIndex = this.integrand(rho,r);
		sumEvenIndex[0] = 0.0; sumEvenIndex[1] = 0.0;
		
		valueX0 = this.integrand(a,r);
		valueXn = this.integrand(b,r);
		
		realSum = valueX0[0] + 2.0*sumEvenIndex[0] + 4.0*sumOddIndex[0] + valueXn[0];
		imagSum = valueX0[1] + 2.0*sumEvenIndex[1] + 4.0*sumOddIndex[1] + valueXn[1];
		curI = (realSum*realSum+imagSum*imagSum)*del*del;
		
		prevI=curI;
		curDifference = TOL;

		boolean convergingErrorGiven = false;
		// Finer sampling grid until we meet the TOL value with the specified number of repetitions, K
		while(k<K) {
			iteration++;
			N *= 2;
			del = del * 0.5;
			sumEvenIndex[0] = sumEvenIndex[0] + sumOddIndex[0];
			sumEvenIndex[1] = sumEvenIndex[1] + sumOddIndex[1];
			sumOddIndex[0] = 0.0;
			sumOddIndex[1] = 0.0;
			for(int n=1; n<N; n=n+2) {
				rho = n*del;
				value = this.integrand(rho,r);
				sumOddIndex[0] += value[0]; 
				sumOddIndex[1] += value[1];
			}
			realSum = valueX0[0] + 2.0*sumEvenIndex[0] + 4.0*sumOddIndex[0] + valueXn[0];
			imagSum = valueX0[1] + 2.0*sumEvenIndex[1] + 4.0*sumOddIndex[1] + valueXn[1];
			curI = (realSum*realSum+imagSum*imagSum)*del*del;
	
			// Relative error between consecutive approximations
			if (prevI==0.0) 
				curDifference = Math.abs((prevI-curI)/1E-5);
			else 
				curDifference = Math.abs((prevI-curI)/curI);
			
			if (curDifference<=TOL)  
				k++;
			else 
				k = 0;
			
			prevI=curI;
			
			if (!convergingErrorGiven && iteration>15) {
				System.err.println("Integral not converging at r="+r+" after "+iteration+"iterations. Optical parameters?");
				convergingErrorGiven = true;
			}
		}
		
		return curI;
}
	
	// integrand()
double[] integrand(double rho, double r) {
		
		// 'rho' is the integration parameter.
		// 'r' is the radial distance of the detector relative to the optical axis in the DETECTOR.
		// The return value is a complex number that is described by an array
		// The relevant equations in the paper are are (4) and (5)
		
		double BesselValueRho = Bessel.J0(p.k_a_over_zd*r*rho)*rho;
		//double BesselValueRho = Bessel.J0(p.k0*p.NA*r*rho/p.M)*rho;
		
		double OPD_real, OPD_imag, OPD1_real, OPD1_imag, OPD2_real, OPD2_imag, OPD3;	// Optical path differences
		double[] I = new double[2];
		
		// Phase term due to immersion layer thickness
		double X = 1-p.NA_over_ni_squared*rho*rho;
		if (X>=0) {
			OPD1_real = p.ni*(p.ti-p.ti0)*Math.sqrt(X);
			OPD1_imag = 0;
		} else { 
			OPD1_real = 0;
			OPD1_imag = p.ni*(p.ti-p.ti0)*Math.sqrt(-X);
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
		
		//double Dz = (p.ti-p.ti0) + p.ni*p.ts/p.ns;
		//OPD_real = -p.NA*p.NA*Dz*rho*rho/(2.0*p.ni)-p.NA*p.NA*p.NA*p.NA*Dz*rho*rho*rho*rho/(8.0*p.ni*p.ni*p.ni);
		//OPD_real = -p.NA*p.NA*rho*rho*(((p.ti-p.ti0)/p.ni)+p.ts/p.ns)/2.0-p.NA*p.NA*p.NA*p.NA*rho*rho*rho*rho*p.ti/(8.0*p.ni*p.ni*p.ni)-p.NA*p.NA*p.NA*p.NA*rho*rho*rho*rho*p.ts/(8.0*p.ns*p.ns*p.ns);
		//OPD_imag = 0.0;

		double W = p.k0*OPD_real;
		
		// The real part
		I[0] = BesselValueRho*Math.cos(W)*Math.exp(-p.k0*OPD_imag); // See eq 4.9, pag 53, Gibson thesis

		// The imaginary part
		I[1] = BesselValueRho*Math.sin(W)*Math.exp(-p.k0*OPD_imag);
		
		return I;
	}
}

