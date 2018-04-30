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

public class ElongatedGaussian extends Defocussed2DFunction {

	private double sigma;
	private double elongation = 2.0;
	
	private double cosa;
	private double sina;
	private double kgaussU;
	private double kgaussV;
	
	private double defocusFactor = 1.0;
	
	public ElongatedGaussian(double sigma, double defocusFactor) {
		super();
		this.sigma = sigma;
		this.kgaussU = 1.0 / (sigma * sigma * elongation);
		this.kgaussV = 1.0 / (sigma * sigma / elongation);
		this.cosa = Math.cos(defocusFactor);
		this.sina = Math.sin(defocusFactor);
	}

	public double eval(double x, double y) {
		double u = x * cosa + y * sina;
		double v = -x * sina + y * cosa;
		u = u * u * kgaussU;
		v = v * v * kgaussV;
		return Math.exp(-(u + v));
	}
	
	public int getSupport() {
		return 4*Tools.round(sigma*elongation*2)+1;
	}

}
