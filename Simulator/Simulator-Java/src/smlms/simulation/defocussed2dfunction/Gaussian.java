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

public class Gaussian extends Defocussed2DFunction {

	private double sigma;
	private double k;
	private double defocusFactor = 1.0;
	
	public Gaussian(double radius, double defocusFactor) {
		super();
		this.defocusFactor = defocusFactor;
		sigma = radius;
		this.k = 1.0/(defocusFactor*defocusFactor*sigma*sigma*2.0);
	}

	public double eval(double x, double y) {
		double r = x*x + y*y;
		return Math.exp(-r*k);
	}
	
	public int getSupport() {
		return 4*Tools.round(sigma*defocusFactor*2)+1;
	}
}
