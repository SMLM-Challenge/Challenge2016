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

public class Lorentz extends Defocussed2DFunction {

	private double radius;
	private double klorentz;
	
	private double defocusFactor = 1.0;
	
	public Lorentz(double radius, double defocusFactor) {
		super();
		this.defocusFactor = defocusFactor;
		this.radius = radius;
		klorentz = Math.sqrt(0.5)/(defocusFactor*radius);
		klorentz = klorentz * klorentz;
	}

	public double eval(double x, double y) {
		double r = x*x + y*y;
		return 1.0 / (1.0 + r * klorentz);	
	}
	
	public int getSupport() {
		return 12*Tools.round(radius*defocusFactor)+1;
	}

}
