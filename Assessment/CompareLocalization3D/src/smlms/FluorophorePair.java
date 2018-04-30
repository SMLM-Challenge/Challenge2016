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

public class FluorophorePair implements Comparable<FluorophorePair> {
    public Fluorophore ref;
    public Fluorophore test;
    public double cost = 0;
    
    public FluorophorePair(Fluorophore ref, Fluorophore test, double cost) {
        this.ref = ref;
        this.test = test;
        this.cost = cost;
    }
    
    public int compareTo(FluorophorePair pair) {
        if (cost > pair.cost)
            return 1;
        if (cost < pair.cost)
            return -1;
        return 0;
    }
    
    public String toString() {
        String a = ref.xnano + ", " + ref.ynano;
        String b = test.xnano + ", " + test.ynano;
        return a + " // " + b;
    }
}
