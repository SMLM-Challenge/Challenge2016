//=========================================================================================
//
// Project: Single-Molecule Localization Microscopy
//			Benchmarking of Localization Microscopy Software for Super-resolution Imaging
//
// Author : Daniel Sage, Biomedical Imaging Group (BIG), http://bigwww.epfl.ch/sage/
//
// Organization: Ecole Polytechnique Federale de Lausanne (EPFL), Lausanne, Switzerland
//
// Reference: paper submitted, 2013
//
// Conditions of use: You'll be free to use this software for research purposes, but you 
// should not redistribute it without our consent. In addition, we expect you to include a
// citation or acknowledgment whenever you present or publish results that are based on it.
//
//=========================================================================================

package smlms.assessment.hungarian;

public class WeightedEdge {

	public int		source;
	public int		destination;
	public double	weight;
	boolean			matched;

	public WeightedEdge(int s, int d, double w) {
		source = s;
		destination = d;
		weight = w;
	}

	public boolean equals(WeightedEdge cTEdge) {
		return (cTEdge.source == source && cTEdge.destination == destination);
	}

	@Override
	public String toString() {
		return new String("s=" + source + ", d=" + destination + ", w=" + weight);
	}

}