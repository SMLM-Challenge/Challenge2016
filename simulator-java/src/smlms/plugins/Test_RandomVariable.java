package smlms.plugins;

import ij.gui.Plot;
import smlms.tools.NormalizedVariable;

public class Test_RandomVariable {

	public static void main(String args[]) {
		double xmin = -100;
		double xmax = 900;
		int n = (int)(xmax - xmin);
		double x[] = new double[n];
		for(int i=0; i<n; i++) 
			x[i] = xmin + i;
		NormalizedVariable var1 = new NormalizedVariable(10);
		var1.addSigmoid(100, 0.5, 0.1);
		var1.addCosine(20, 2, 200);
		var1.addPoissonRandom(10);
		double v1[] = new double[n];
		for(int i=0; i<n; i++) 
			v1[i] = var1.get(x[i]/1000.0);
		
		Plot plot = new Plot("Variable", "x", "value", x, v1);
		plot.show();
		
		double array[][][] = new double[2][3][4];
		System.out.println( " " + array.length + " " + array[0].length + " " + array[0][0].length);
	}

}
