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

import java.util.ArrayList;

public class Matching {
	public ArrayList<WeightedEdge>	cTEdges;
	public double					weight;

	public Matching(ArrayList<WeightedEdge> cTEdges, double weight) {
		this.cTEdges = cTEdges;
		this.weight = weight;
	}
}
