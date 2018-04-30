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
package smlms.simulation.defocussed2dfunction;

import smlms.tools.Tools;

public class Cosine extends Defocussed2DFunction {

	private double freq = Math.PI * 0.5;
	
	private double size = 1.0;
	
	public Cosine(double radius, double defocusFactor) {
		super();
		this.size = radius * defocusFactor;
	}

	public double eval(double x, double y) {
		if (size < 0.0000001)
			return 0;
		double r = Math.sqrt(x*x + y*y) / size;
		return Math.max(0, Math.cos(r * freq));	
	}
	
	public int getSupport() {
		return 2*Tools.round(size)+1;
	}
}
