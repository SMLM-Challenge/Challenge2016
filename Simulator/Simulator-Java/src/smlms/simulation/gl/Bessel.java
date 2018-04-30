package smlms.simulation.gl;

//import ij.IJ;

/*
* This class evaluates the Bessel function J0(x).
* It uses the polynomial approximations on p. 369-70 of Abramowitz & Stegun.
* The error in J0 is supposed to be less than or equal to 5 x 10^-8.
* The error in J1 is supposed to be less than or equal to 4 x 10^-8, relative to the value of x.
* The error in J2 depends on the error of J0 and J1, as J2 = 2*J1/x + J0.
* 
*/

public class Bessel {
	
	/** Constants for Bessel function approximation according Abramowitz & Stegun */
	private static double[] tJ0 = {1.0, -2.2499997, 1.2656208, -0.3163866, 0.0444479, -0.0039444, 0.0002100};
	private static double[] pJ0 = {-.78539816, -.04166397, -.00003954, 0.00262573, -.00054125, -.00029333, .00013558};
	private static double[] fJ0 = {.79788456, -0.00000077, -.00552740, -.00009512, 0.00137237, -0.00072805, 0.00014476};
	
	private static double[] tJ1 = {0.5, -0.56249985, 0.21093573, -0.03954289, 0.0443319, -0.00031761, 0.0001109};
	private static double[] pJ1 = {-2.35619449, 0.12499612, 0.00005650, -0.00637879, 0.00074348, 0.00079824, -0.00029166};
	private static double[] fJ1 = {0.79788456, 0.00000156, 0.01689667, 0.00017105, -0.00249511, 0.00113653, -0.00020033};
	
	
	/**
	 * Returns the value of the Bessel function of the first kind of order zero (J0) at x.
	 */
	public static double J0(double x) {
		if (x < 0.0)  
			x *= -1.0;
		if (x <= 3.0) {
			double y = x*x/9.0;
			return tJ0[0] + y*(tJ0[1] + y*(tJ0[2] + y*(tJ0[3] + y*(tJ0[4] + y*(tJ0[5] + y*tJ0[6])))));
		} 
		
		double y = 3.0/x;
		double theta0 = x + pJ0[0] + y*(pJ0[1] + y*(pJ0[2] + y*(pJ0[3] + y*(pJ0[4] + y*(pJ0[5] + y*pJ0[6])))));
		double f0 = fJ0[0] + y*(fJ0[1] + y*(fJ0[2] + y*(fJ0[3] + y*(fJ0[4] + y*(fJ0[5] + y*fJ0[6])))));
		return Math.sqrt(1.0/x)*f0*Math.cos(theta0);
	}
	
	/**
	 * Returns the value of the Bessel function of the first kind of order one (J1) at x.
	 */
	public static double J1(double x) {
		
		int sign=1;
		
		if (x < 0.0) {  
			x *= -1.0;
			sign = -1;
		}
		
		if (x <= 3.0) {
			double y = x*x/9.0;
			return sign*x*(tJ1[0] + y*(tJ1[1] + y*(tJ1[2] + y*(tJ1[3] + y*(tJ1[4] + y*(tJ1[5] + y*tJ1[6]))))));
		} 
		
		double y = 3.0/x;
		double theta1 = x + pJ1[0] + y*(pJ1[1] + y*(pJ1[2] + y*(pJ1[3] + y*(pJ1[4] + y*(pJ1[5] + y*pJ1[6])))));
		double f1 = fJ1[0] + y*(fJ1[1] + y*(fJ1[2] + y*(fJ1[3] + y*(fJ1[4] + y*(fJ1[5] + y*fJ1[6])))));
		return sign*Math.sqrt(1.0/x)*f1*Math.cos(theta1);
	}
	
	/**
	 * Returns the value of the Bessel function of the first kind of order two (J2) at x.
	 */
	public static double J2(double x) {
		
		double value0 = J0(x);
		double value1 = J1(x);
		if (x==0.0) return 0.0;
		else return 2.0*value1/x + value0;	
	}	
}