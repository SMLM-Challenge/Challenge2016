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

public class Astigmatism extends Defocussed2DFunction {

	private double sigma;
	private double kx;
	private double ky;
	
	public Astigmatism(double radius, double defocusFactor) {
		super();
		this.sigma = 0.8493218*radius; //1/sqrt(-2*log(0.5))
		double dy = defocusFactor * 1.5 - 1;
		double dx = 1.0 / dy;
		this.kx = 1.0/(dx*dx*sigma*sigma*2.0);
		this.ky = 1.0/(dy*dy*sigma*sigma*2.0);
	}

	public double eval(double x, double y) {
		return Math.exp(-x*x*kx - y*y*ky);
	}
	
	public int getSupport() {
		return 4*Tools.round(sigma*2)+1;
	}
}


