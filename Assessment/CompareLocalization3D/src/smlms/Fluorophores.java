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

import java.util.ArrayList;

public class Fluorophores extends ArrayList<Fluorophore> {

	public static Fluorophores[] crop(Fluorophores fluos[], double x1, double x2, double y1, double y2) {
		Fluorophores[] fluorophores = new Fluorophores[fluos.length];
		int count1 = 0;
		int count2 = 0;
		for (int f = 0; f < fluos.length; f++) {
			fluorophores[f] = new Fluorophores();
			for (Fluorophore fluo : fluos[f]) {
				if (fluo.xnano >= x1)
					if (fluo.ynano >= y1)
						if (fluo.xnano <= x2)
							if (fluo.ynano <= y2)
								fluorophores[f].add(fluo);
			}
			count1 += fluos[f] .size();
			count2 += fluorophores[f] .size();
			
		}
		System.out.println(" Original Number of Points " + count1);
		System.out.println(" Cropped Number of Points " + count2);
		return fluorophores;
	}
	
	public static Fluorophores correctionWooble(Wobble wobble, Fluorophores fluos) {
		if (wobble == null)
			return fluos;
		Fluorophores b = new Fluorophores();
		for(Fluorophore fluo : fluos)
			b.add(wobble.wobble(fluo));
		return b;
	}

}
