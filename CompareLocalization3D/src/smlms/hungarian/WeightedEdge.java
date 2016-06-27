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

package smlms.hungarian;

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