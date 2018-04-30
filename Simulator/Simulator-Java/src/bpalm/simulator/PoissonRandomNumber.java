package bpalm.simulator;

import java.util.Random;

public class PoissonRandomNumber extends Random {
	
	public PoissonRandomNumber() {
		super();
	}
	
	public double nextPoisson(double lambda) {
	
		if (lambda==0) return 0.0;
		else if (lambda<100) {
			//  Knuth algorithm
			double L = Math.exp(-lambda);
			int k = 0;
			double p = 1;
		
			do {
				k++;
				p *= nextDouble();
			} while (p >= L);
			//IJ.log("(lambda,k)=("+lambda + ", " + (k-1) + ")");
			return (double)(k - 1);
		} else {
			// Gussian distribution which approximates the Poisson one for large lambda values
			double value = (nextGaussian()*Math.sqrt(lambda))+lambda;
			//IJ.log("(lambda,value)=("+ lambda + ", " + value + ")");
			return value;
		}
	}
}
