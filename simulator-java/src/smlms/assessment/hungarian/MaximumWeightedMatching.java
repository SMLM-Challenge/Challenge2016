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
import java.util.concurrent.Callable;

public class MaximumWeightedMatching implements Callable {
	private int						Unodes;
	private int						Nnodes;
	private ArrayList<WeightedEdge>	maximumWeightedMatching;

	private ArrayList<WeightedEdge>	G;
	private boolean					terminated	= false;

	public MaximumWeightedMatching(ArrayList<WeightedEdge> G, int Unodes, int Wnodes) {
		this.Unodes = Unodes;
		this.Nnodes = Unodes + Wnodes;
		this.G = G;
	}

	public Boolean call() {
		try {
			ArrayList<Matching> Mset = new ArrayList<Matching>();

			ArrayList<WeightedEdge> GM = new ArrayList<WeightedEdge>();
			ArrayList<WeightedEdge> M = new ArrayList<WeightedEdge>();
			ArrayList<WeightedEdge> path = new ArrayList<WeightedEdge>();

			boolean[] matched = new boolean[Nnodes];
			Arrays.fill(matched, false);
			do {
				for (WeightedEdge e : G) {
					boolean found = false;
					for (WeightedEdge e2 : M) {
						if (e.equals(e2)) {
							found = true;
							matched[e.source] = true;
							matched[e.destination] = true;
							GM.add(new WeightedEdge(e.destination, e.source, e.weight));
						}
						if (found) {
							break;
						}
					}
					if (!found) {
						GM.add(new WeightedEdge(e.source, e.destination, -e.weight));
					}
				}

				path = findBestAugmentingPath(GM, matched);
				GM.clear();
				if (path.size() != 0) {

					for (WeightedEdge e : path) {
						WeightedEdge removableEdge = null;
						for (WeightedEdge e2 : M) {
							if (e.equals(e2)) {
								removableEdge = e2;
								break;
							}

						}
						if (removableEdge != null) {
							M.remove(removableEdge);
						}
						else {
							M.add(new WeightedEdge(e.source, e.destination, e.weight));
							matched[e.source] = true;
							matched[e.destination] = true;
						}
					}
				}

				ArrayList<WeightedEdge> MClone = new ArrayList<WeightedEdge>(M.size());
				for (WeightedEdge e : M) {
					MClone.add(e);
				}
				Mset.add(new Matching(MClone, matchingWeight(M)));
			}
			while (path.size() != 0);

			// identify best matching
			Matching bestMatching = Mset.get(0);
			for (Matching m : Mset) {
				double currentWeight = m.weight;
				if (currentWeight > bestMatching.weight) {
					bestMatching = m;
				}
			}
			maximumWeightedMatching = bestMatching.cTEdges;
			terminated = true;
			return true;
		}
		catch (Exception ex) {
			return false;
		}
	}

	public boolean isTerminated() {
		return terminated;
	}

	public ArrayList<WeightedEdge> getMaximumWeightMatching() {
		return maximumWeightedMatching;
	}

	private double matchingWeight(ArrayList<WeightedEdge> M) {
		double w = 0;
		for (WeightedEdge e : M) {
			w += Math.abs(e.weight);
		}
		return w;
	}

	private ArrayList<WeightedEdge> findBestAugmentingPath(ArrayList<WeightedEdge> GM, boolean[] matched) {

		// count unmatched
		int numMatched = 0;
		for (int i = 0; i < matched.length; i++) {
			if (matched[i]) {
				numMatched++;
			}
		}

		// cloning GM
		ArrayList<WeightedEdge> G = new ArrayList<WeightedEdge>(GM.size() + (matched.length - numMatched + 1));
		for (WeightedEdge e : GM) {
			G.add(e);
		}

		for (int i = 0; i < Nnodes; i++) {
			if (!matched[i]) {
				if (i < Unodes) {
					G.add(new WeightedEdge(Nnodes, i, 0.0));
				}
				else {
					G.add(new WeightedEdge(i, Nnodes + 1, 0.0));
				}
			}
		}

		BellmanFord trackerBellmanFord = new BellmanFord(G, Nnodes + 2, Nnodes);
		trackerBellmanFord.computeSpanningTree();

		int[] predecessor = trackerBellmanFord.getPredecessor();
		double[] weights = trackerBellmanFord.getTotalWeight();

		ArrayList<WeightedEdge> path = new ArrayList<WeightedEdge>();

		int n1 = predecessor[Nnodes + 1];

		while (n1 != Nnodes && n1 != -1) {
			int n2 = predecessor[n1];
			if (predecessor[n2] != -1) {
				if (n1 < n2) {
					path.add(new WeightedEdge(n1, n2, Math.abs(weights[n1] - weights[n2])));
				}
				else {
					path.add(new WeightedEdge(n2, n1, Math.abs(weights[n1] - weights[n2])));
				}
			}
			n1 = n2;
		}
		return path;
	}
}
