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

public class Verbose {
	
	static public String[] names = new String[] {"Mute", "Talk", "Prolix"};
	
	private static int level = 1;
	
	public static void setLevel(int verboseLevel) {
		level = verboseLevel;
	}
	
	public static void talk(String msg) {
		if (level >= 1)
			IJ.log(msg);
	}

	public static void prolix(String msg) {
		if (level >= 2)
			IJ.log(msg);
	}

	public static void exception(Exception ex) {
		StackTraceElement elements[] = ex.getStackTrace();
		for(int i=0; i<elements.length; i++)
			IJ.log("Trace Exception " + elements[i].toString());
		IJ.log("" + ex);
	}
}
