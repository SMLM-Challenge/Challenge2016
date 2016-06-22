//=========================================================================================
//
// Project: Localization Microscopy
//
// Author : Daniel Sage, Biomedical Imaging Group (BIG), http://bigwww.epfl.ch/sage/
//
// Organization: Ecole Polytechnique Federale de Lausanne (EPFL), Lausanne, Switzerland
//
// Conditions of use: You'll be free to use this software for research purposes, but you 
// should not redistribute it without our consent. In addition, we expect you to include a
// citation or acknowledgment whenever you present or publish results that are based on it.
//
//=========================================================================================
package smlms.tools;

public class ArrayOperations {

	public static void main(String args[]) {
		double max[] = ArrayOperations.init(3, -Double.MAX_VALUE);
		double min[] = ArrayOperations.init(3, Double.MAX_VALUE);
		double mean[] = ArrayOperations.init(3, 0);
		double var[] = ArrayOperations.init(3, 0);
		double count[] = ArrayOperations.init(3, 0);
		PsRandom rand = new PsRandom();
		for(int i=0; i<10000; i++) {
			double a[] = new double[] {rand.nextDouble(3, 5), rand.nextGaussian(3, 5), rand.nextPoissonian(4)};
			ArrayOperations.inc(count);
			ArrayOperations.mean(count, mean, a);
			ArrayOperations.min(min, a);
			ArrayOperations.max(max, a);
			ArrayOperations.variance(count, var, mean, a);
		}
		for(int i=0; i<min.length; i++)
			System.out.println("Min: " + min[i]);
		for(int i=0; i<min.length; i++)
			System.out.println("Mean: " + mean[i]);
		for(int i=0; i<min.length; i++)
			System.out.println("stdev: " + Math.sqrt(var[i]));
		for(int i=0; i<min.length; i++)
			System.out.println("max: " + max[i]);
	}

	public static String[] merge(String arr1[], String arr2[], int begin, int end) {
		String[] arr = new String[arr1.length + (end-begin) + 1];
		for(int i=0; i<arr1.length; i++)
			arr[i] = arr1[i];
		for(int i=begin; i<=end; i++)
			arr[i+arr1.length-begin] = arr2[i];
		return arr;
	}
	public static double[] ramp(int n) {
		 double array[] = new double[n];
		 for(int i=0; i<n; i++)
			 array[i] = i;
		 return array;
	}
	
	public static int getIndexMaximum(double[] array) {
		 int n = array.length;
		 double max = -Double.MAX_VALUE;
		 int imax = 0;
		 for(int i=0; i<n; i++) {
			 if (array[i] > max) {
				 max = array[i];
				 imax = i;
			 }
		 }
		 return imax;
	}

	public static double[][] transpose(double[][] array) {
		 int ni = array.length;
		 int nj = array[0].length;
		 double out[][] = new double[nj][ni];
		 for(int i=0; i<ni; i++) 
			 for(int j=0; j<nj; j++)
				 out[j][i] = array[i][j];
		 return out;
	}

	public static double getMaximum(double[] array) {
		 int n = array.length;
		 double max = -Double.MAX_VALUE;
		 for(int i=0; i<n; i++) {
			 if (array[i] > max) {
				 max = array[i];
			 }
		 }
		 return max;
	}

	public static int getFWMH(double[] array, double max, int posmax) { 	
		int n = array.length;
		int i1 = 0;
		for(i1=posmax; i1<n; i1++) {
			if (max*0.5 > array[i1])
				break;
		}
		int i2 = 0;
		for(i2=posmax; i2>=0; i2--) {
			if (max*0.5 > array[i2])
				break;
		}
		return i1-i2;	
	} 
	
	public static float getMean(float[][] array) {
		int n = array.length;
		int m = array[0].length;
		float mean = 0f;
		for (int i = 0; i < n; i++)
		for (int j = 0; j < m; j++)
			mean += array[i][j];
		return mean / (n*m);	
	}

	public static float getNorm(float[][] array) {
		int n = array.length;
		int m = array[0].length;
		float mean = 0f;
		for (int i = 0; i < n; i++)
		for (int j = 0; j < m; j++)
			mean += array[i][j]*array[i][j];
		return (float)Math.sqrt(mean / (n*m));	
	}

	public static void fill(float[][] array, float value) {
		int n = array.length;
		int m = array[0].length;
		for (int i = 0; i < n; i++)
		for (int j = 0; j < m; j++)
			array[i][j] = value;
	}

	public static void multiply(float[][] array, float multiply) {
		int n = array.length;
		int m = array[0].length;
		for (int i = 0; i < n; i++)
		for (int j = 0; j < m; j++)
			array[i][j] *= multiply;
	}

	public static void copy(float[][] source, float[][] destination) {
		int n = source.length;
		for (int i = 0; i < n; i++)
			System.arraycopy(source[i], 0, destination[i], 0, source[i].length);
	}

	public static void max(float[][] array, float[][] additiveTerm) {
		int n = array.length;
		int m = array[0].length;
		for (int i = 0; i < n; i++)
		for (int j = 0; j < m; j++) {
			array[i][j] = Math.max(array[i][j], additiveTerm[i][j]);
		}
	}

	public static void add(float[][] array, float[][] additiveTerm) {
		int n = array.length;
		int m = array[0].length;
		for (int i = 0; i < n; i++)
		for (int j = 0; j < m; j++) {
			array[i][j] += additiveTerm[i][j];
		}
	}

	public static void linear(float[][] array, float gain, float offset) {
		int n = array.length;
		int m = array[0].length;
		for (int i = 0; i < n; i++)
		for (int j = 0; j < m; j++) {
			array[i][j] = gain *array[i][j] + offset;
		}
	}

	public static void normalize(float[][] array, float norm) {
		int n = array.length;
		int m = array[0].length;
		float max = 0.0f;
		for (int i = 0; i < n; i++)
		for (int j = 0; j < m; j++) {
			if (array[i][j] > max)
				max = array[i][j];
		}
		multiply(array, norm/max);
	}
	
	public static void normalize(float[][] array, float norm, float offset) {
		int n = array.length;
		int m = array[0].length;
		float max = 0.0f;
		for (int i = 0; i < n; i++)
		for (int j = 0; j < m; j++) {
			if (array[i][j] > max)
				max = array[i][j];
		}
		float a = norm/max;
		for (int i = 0; i < n; i++)
		for (int j = 0; j < m; j++) {
				array[i][j] = a * array[i][j] + offset;
		}
	}


	public static void increment(float[][] array, float[][] additiveTerm) {
		int n = array.length;
		int m = array[0].length;
		for (int i = 0; i < n; i++)
		for (int j = 0; j < m; j++) {
			array[i][j] += additiveTerm[i][j];
		}
	}


	public static void increment(float[][] array, float[] additiveTerm) {
		int n = array.length;
		int m = array[0].length;
		for (int i = 0; i < n; i++)
		for (int j = 0; j < m; j++) {
			array[i][j] += additiveTerm[i+j*n];
		}
	}

	public static void increment(float[][] array, float value) {
		int n = array.length;
		int m = array[0].length;
		for (int i = 0; i < n; i++)
			for (int j = 0; j < m; j++)
				array[i][j] += value;
	}
	
	public static double[] init(int n, double value) {
		double[] array = new double[n];
		for(int i=0; i<array.length; i++)
			array[i] = value;	
		return array;
	}

	public static void setValue(double[] array, double value) {
		for(int i=0; i<array.length; i++)
			array[i] = value;	
	}

	public static void min(double[] minimun, double operand[]) {
		for(int i=0; i<operand.length; i++)
			minimun[i] = Math.min(minimun[i], operand[i]);	
	}
	
	public static void max(double[] maximun, double operand[]) {
		for(int i=0; i<operand.length; i++)
			maximun[i] = Math.max(maximun[i], operand[i]);	
	}
	
	public static void sum(double[] out, double operand[]) {
		for(int i=0; i<operand.length; i++)
			out[i] += operand[i];	
	}
	
	public static void inc(double[] out) {
		for(int i=0; i<out.length; i++)
			out[i]++;	
	}

	public static void mean(double count[], double[] currentMean, double update[]) {
		for(int i=0; i<update.length; i++)
			currentMean[i] = (currentMean[i]*count[i] + update[i]) / (count[i] + 1.0);	
	}
	
	public static void variance(double count[], double currentVariance[], double currentMean[], double update[]) {
		for(int i=0; i<update.length; i++) {
			if (count[i] > 1.0) {
				double a = currentVariance[i] * (count[i]-2.0) / (count[i]-1.0);
				double b = (update[i] - currentMean[i]) * (update[i] - currentMean[i]) / (count[i]+1.0);	
				currentVariance[i] = a + b;
			}
			else {
				currentVariance[i] = (update[i] - currentMean[i]) * (update[i] - currentMean[i]) ;
			}
		}
	}


}
