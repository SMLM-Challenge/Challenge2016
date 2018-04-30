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

import java.util.ArrayList;
import java.util.Arrays;

public class BellmanFord {
	public static double			NOVAL			= Double.POSITIVE_INFINITY;

	private ArrayList<WeightedEdge>	cTEdges			= null;
	private double[]				distance		= null;
	private int[]					predecessor		= null;
	private int						numNodes		= 0;
	private int						sourceNodeIndex	= 0;

	public BellmanFord(ArrayList<WeightedEdge> cTEdges, int Nnodes, int source) {
		this.cTEdges = cTEdges;
		this.numNodes = Nnodes;
		this.sourceNodeIndex = source;
	}

	public void computeSpanningTree() {
		distance = new double[numNodes];
		predecessor = new int[numNodes];
		Arrays.fill(distance, NOVAL);
		Arrays.fill(predecessor, -1);
		distance[sourceNodeIndex] = 0.0;

		for (int i = 0; i < numNodes; i++) {
			// relax every edge in 'edges'
			for (WeightedEdge e : cTEdges) {
				if (distance[e.source] == NOVAL) {
					continue;
				}
				double newDistance = distance[e.source] + e.weight;
				if (newDistance < distance[e.destination]) {
					distance[e.destination] = newDistance;
					predecessor[e.destination] = e.source;
				}
			}
		}
	}

	public int[] getPredecessor() {
		return predecessor;
	}

	public double[] getTotalWeight() {
		return distance;
	}
}
