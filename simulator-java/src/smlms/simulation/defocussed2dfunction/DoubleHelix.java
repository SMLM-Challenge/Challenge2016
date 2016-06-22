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

public class DoubleHelix extends Defocussed2DFunction {

	private double radius;
	private double sigma;
	private double kgauss;
	private double cosa;
	private double sina;
	
	public DoubleHelix(double radius, double defocusFactor) {
		super();
		this.radius = radius;
		this.sigma = 0.25*radius;
		this.kgauss = 1.0 / (sigma * sigma * 2.0);
		this.cosa = Math.cos(defocusFactor);
		this.sina = Math.sin(defocusFactor);
	}
	
	public double eval(double x, double y) {
		double u = x * cosa + y * sina;
		double v = -x * sina + y * cosa;
		double u1 = (u-radius*0.5);
		double u2 = (u+radius*0.5);
		return Math.exp(-((u1*u1+v*v)*kgauss)) + Math.exp(-((u2*u2+v*v)*kgauss));
	}
	
	public int getSupport() {
		return 4*Tools.round(sigma*3)+1;
	}
	
}
