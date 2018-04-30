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

package smlms.assessment;

import smlms.file.Fluorophore;

public class FluorophorePair implements Comparable<FluorophorePair> {
	public Fluorophore ref;
	public Fluorophore test;
	public double cost = 0;

	public FluorophorePair(Fluorophore ref, Fluorophore test) {
		this.ref = ref;
		this.test = test;
		this.cost = ref.distance(test);
	}

	public int compareTo(FluorophorePair pair) {
		return (cost > pair.cost ? 1 : 0);
	}

	public String toString() {
		String a = ref.x + ", " + ref.y;
		String b = test.x + ", " + test.y;
		return a + ", " + b;
	}
}
