package smlms.plugins;

import smlms.tools.PsRandom;

public class Test_Random_1 {

	public static void main(String args[]) {
		double mx = 125;
		double my = 125;
		double mz = 11;
		double chrono = System.nanoTime();
		for(int i=0; i<1000; i++) {
			PsRandom rand = new PsRandom();
			double p = rand.nextDouble()*mx*my*mz;
			double x = p / (my*mz);
			double y = p / (mx*mz);
			double z = p / (mx*my);
			System.out.println("x= " + x + " y=" + y + " z=" + z);
		}
		System.out.println(" " + (System.nanoTime() - chrono));
		
		double a = 0.9551002;
		
		int e = (int)Math.log10(a);
		double m = a / Math.pow(10, e);
		int mi = ((int)Math.round(m*100));
		double ai = mi * Math.pow(10, e-2);
		System.out.println("a= " + a + " e=" + e + " m=" + m + " " + mi + " " + ai);
		
		int N = 1000;
		double x[] = new double[N];
		for(int i=0; i<N; i++)
			x[i] = Math.random();

		double mean = 0.0;
		for(int i=0; i<N; i++)
			mean += x[i];
		mean = mean / N;

		double sd = 0.0;
		for(int i=0; i<N; i++)
			sd += (x[i]-mean)*(x[i]-mean);
		sd = Math.sqrt(sd / N);
		
		double sd2 = 0.0;
		double xx = 0.0;
		double xs = 0.0;
		for(int i=0; i<N; i++) {
			xs += x[i];
			xx += x[i]*x[i];
		}
		sd2= Math.sqrt(xx*N - xs*xs)/N;
		System.out.println(" mean " + mean + " " + sd + " =?= " + sd2);
	}
}
