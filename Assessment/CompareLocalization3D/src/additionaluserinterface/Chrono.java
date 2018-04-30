//=========================================================================================
//
// Project: AdditionalUserInterface - Providing GUI for ImageJ plugin
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
package additionaluserinterface;

import java.text.DecimalFormat;

/**
 * This class provides static methods to measures the elapsed time.
 * It is a equivalent to the function tic and toc of Matlab. 
 * 
 * @author Daniel Sage, Biomedical Imaging Group, EPFL, Lausanne, Switzerland.
 *
 */  
public class Chrono {

	static private double chrono = 0;
	
	/**
	* Register the current time.
	*/
	public static void tic() {
		chrono = System.currentTimeMillis();
	}
	
	/**
	* Returns a string that indicates the elapsed time since the last tic() call.
	*/
	public static String toc() {
		return toc("");
	}

	/**
	* Returns a string that indicates the elapsed time since the last tic() call.
	*
	* @param msg	message to print
	*/
	public static String toc(String msg) {
		double te = System.currentTimeMillis()-chrono;
		String s = msg + " ";
		DecimalFormat df = new DecimalFormat("####.##"); 
		if (te < 3000.0) 
			return s + df.format(te) + " ms";
		te /= 1000;
		if (te < 600.1)
			return s + df.format(te) + " s";
		te /= 60;
		if (te < 240.1)
			return s + df.format(te) + " min.";
		te /= 24;
		return s + df.format(te) + " h.";
	}
 }
