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
