//=========================================================================================
//
// Project: Localization Microscopy
//
// Author : Daniel Sage, Biomedical Imaging Group (BIG), http://bigwww.epfl.ch/sage/
//
// Organization: Ecole Polytechnique F�d�rale de Lausanne (EPFL), Lausanne, Switzerland
//
// Conditions of use: You'll be free to use this software for research purposes, but you 
// should not redistribute it without our consent. In addition, we expect you to include a
// citation or acknowledgment whenever you present or publish results that are based on it.
//
//=========================================================================================
package smlms.tools;

import ij.IJ;

public class Chrono {

	static private double chrono;
	static private double[] chronos = new double[10];
	
	static public void reset() {
		chrono = System.nanoTime();
	}
	
	static public void reset(int number) {
		if (number < 0)
			chrono = System.nanoTime();
		else if (number >= 10)
			chrono = System.nanoTime();
		else	
			chronos[number] = System.nanoTime();
	}

	static public String string() {
		return string("", -1);
	}
	
	static public String string(String message) {
		return string(message, -1);
	}

	static public String string(int number) {
		return string("", number);
	}
	
	static public String string(String message, int number) {
		double t = 0;
		if (number < 0)
			t = System.nanoTime() - chrono;
		else if (number >= 10)
			t = System.nanoTime() - chrono;
		else
			t = System.nanoTime() - chronos[number];
		String sn = (number < 0 ? " " : " [" + number + "] ");
		if (t > 1000000000.0)
			return message + " Chrono"  + sn + IJ.d2s(t/1000000000.0) + " s";
		else if (t > 1000000.0)
			return message + " Chrono"  + sn + IJ.d2s(t/1000000.0) + " ms";
		else if (t > 1000.0)
			return message + " Chrono"  + sn + IJ.d2s(t/1000.0) + " us";
		else 
			return message + " Chrono"  + sn + IJ.d2s(t/1000000000.0) + " ns";
	}
	
	static public void print() {
		IJ.log(string("", -1));
	}
	
	static public void print(String message) {
		IJ.log(string(message, -1));
	}
	
	static public void print(int number) {
		IJ.log(string("" , number));
	}
	
	static public void print(String message, int number) {
		IJ.log(string(message, number));
	}


}
