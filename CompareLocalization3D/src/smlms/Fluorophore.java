//=========================================================================================
//
// Single-Molecule Localization Microscopy Challenge 2016
// http://bigwww.epfl.ch/smlm/
//
// Author: 
// Daniel Sage, http://bigwww.epfl.ch/sage/
// Biomedical Imaging Group (BIG)
// Ecole Polytechnique Federale de Lausanne (EPFL), CH-1015 Lausanne, Switzerland
//
// Reference: 
// D. Sage, H. Kirshner, T. Pengo, N. Stuurman, J. Min, S. Manley, M. Unser
// Quantitative Evaluation of Software Packages for Single-Molecule Localization Microscopy 
// Nature Methods 12, August 2015.
// 
// Conditions of use: 
// You'll be free to use this software for research purposes, but you 
// should not redistribute it without our consent. In addition, we expect you to include a
// citation or acknowledgment whenever you present or publish results that are based on it.
//
//=========================================================================================

package smlms;

public class Fluorophore implements Comparable<Fluorophore> {

	public double	xnano;
	public double	ynano;
	public double	znano;
	public boolean	matching	= false;
	public int		frame;
	public double	photons;
	public boolean	activation	= false;

	public Fluorophore(double xnano, double ynano, double znano, int frame, double photons) {
		this.xnano = xnano;
		this.ynano = ynano;
		this.znano = znano;
		this.frame = frame;
		this.photons = photons;
		this.activation = false;
	}

	public Fluorophore duplicate() {
		Fluorophore fluo = new Fluorophore(xnano, ynano, znano, frame, photons);
		fluo.activation = activation;
		fluo.matching = matching;
		return fluo;
	}
	public boolean getActivation() {
		return activation;
	}

	public int getFrame() {
		return frame;
	}

	public double getPhotons() {
		return photons;
	}

	public boolean isSamePosition(Fluorophore fluo) {
		if (fluo.xnano != xnano)
			return false;
		if (fluo.ynano != ynano)
			return false;
		if (fluo.znano != znano)
			return false;
		return true;
	}

	public void resetActivation() {
		activation = false;
	}

	public boolean isActivated() {
		return activation;
	}

	public double deltaX(Fluorophore fluo) {
		return fluo.xnano - xnano;
	}

	public double deltaY(Fluorophore fluo) {
		return fluo.ynano - ynano;
	}

	public double deltaZ(Fluorophore fluo) {
		return fluo.znano - znano;
	}

	public double differenceIntensity(Fluorophore fluo) {
		return fluo.photons - photons;
	}

	public int distanceFrame(Fluorophore fluo) {
		return Math.abs(fluo.frame - frame);
	}

	public double distanceLateral(Fluorophore fluo) {
		return Math.sqrt((fluo.xnano - xnano) * (fluo.xnano - xnano) + (fluo.ynano - ynano) * (fluo.ynano - ynano));
	}

	public double distanceAxial(Fluorophore fluo) {
		return Math.abs(fluo.znano - znano);
	}

	public double distance(Fluorophore fluo) {
		return Math.sqrt((fluo.xnano - xnano) * (fluo.xnano - xnano) + (fluo.ynano - ynano) * (fluo.ynano - ynano) + (fluo.znano - znano) * (fluo.znano - znano));
	}

	public String toString() {
		String s = " (" + xnano + ", " + ynano + ", " + znano + ") [" + frame + "] " + photons;
		return s;
	}

	public void log() {
		System.out.println(toString());
	}

	public int compareTo(Fluorophore fluorophore) {
		return (fluorophore.frame > frame ? 0 : 1);
	}
}
