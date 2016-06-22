package smlms.plugins;

import smlms.tools.Chart;

public class Test_JFreeChart {

	public static void main(String args[]) {
		new Test_JFreeChart();
	}
	
	public Test_JFreeChart() {
		
		int N = 1000;
		double x[] = new double[N];
		double y[] = new double[N];
		for(int i=0; i<N; i++) {
			x[i] = Math.random();
			y[i] = Math.random();
		}
			
		Chart chart = new Chart("", "x", y);
		chart.show("test", 500, 500);
	}
}
