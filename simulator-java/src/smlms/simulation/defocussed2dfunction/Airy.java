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

public class Airy extends Defocussed2DFunction {

	private double cycle;
	private double size = 1.0;
	
	public Airy(double radius, double defocusFactor) {
		super();
		this.size = radius * defocusFactor;
		this.cycle = Math.PI * defocusFactor * defocusFactor;
		this.size = radius*defocusFactor;
	}
	
	public double eval(double x, double y) {
		double r = x*x + y*y;
		if (r > size*size)
			return 0;
		return Math.max(0, (Math.cos(r * cycle)+0.5)*(1.0-r));
	}
	
	public int getSupport() {
		return 8*Tools.round(size)+1;
	}
}

