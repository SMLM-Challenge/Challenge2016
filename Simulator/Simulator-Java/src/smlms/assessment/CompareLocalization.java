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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import smlms.assessment.hungarian.MaximumWeightedMatching;
import smlms.assessment.hungarian.WeightedEdge;
import smlms.file.Fluorophore;
import smlms.file.Fluorophores;
import additionaluserinterface.WalkBar;

public class CompareLocalization implements Runnable {

	public static final int			ALGO_GLOBAL_SORT_NEAREST_NEIGHBORHOOR	= 0;
	public static final int			ALGO_NEAREST_NEIGHBORHOOR				= 1;
	public static final int			ALGO_HUNGARIAN							= 2;

	private Fluorophores[]			refs;
	private Fluorophores[]			tests;
	private double					tolerance								= Double.MAX_VALUE;
	private CompareResult			results[];			
	private CompareResult			result;				
														
	private Vector<FluorophorePair>	pairs[];
	private int						algo									= ALGO_NEAREST_NEIGHBORHOOR;
	private double					pixelsize;
	private WalkBar					walk;
	
	private int						frameBegin = 0;
	private int						frameEnd 	= Integer.MAX_VALUE;
	private String					dataset;
	private String					software;
	private String					logging;
	
	public CompareLocalization(WalkBar walk, String dataset, String software, String logging, Fluorophores[]	refs, Fluorophores[] tests, int algo, double tolerance, double pixelsize) {
		this.walk = walk;
		this.dataset = dataset;
		this.software = software;
		this.refs = refs;
		this.tests = tests;
		this.algo = algo;
		this.tolerance = tolerance;
		this.pixelsize = pixelsize;
		this.logging = logging;
	}

	public void setTolerance(double tolerance) {
		this.tolerance = tolerance;
	}

	public void setFrameRange(int frameBegin, int frameEnd) {
		this.frameBegin = frameBegin;
		this.frameEnd = frameEnd;
	}

	public CompareResult getResult() {
		return result;
	}

	public CompareResult[] getResults() {
		return results;
	}
	
	public double[] getJaccard() {
		double[] jaccard = new double[results.length];
		for(int i=0; i<results.length; i++)
			jaccard[i] = results[i].jaccard / 100.0;
		return jaccard;
	}

	public double[] getPrecision() {
		double[] precision = new double[results.length];
		for(int i=0; i<results.length; i++)
			precision[i] = results[i].precision;
		return precision;
	}

	public double[] getRecall() {
		double[] recall = new double[results.length];
		for(int i=0; i<results.length; i++)
			recall[i] = results[i].recall;
		return recall;
	}

	public double[] getRMSE() {
		double[] rmse = new double[results.length];
		for(int i=0; i<results.length; i++)
			rmse[i] = results[i].rmseLateral;
		return rmse;
	}

	public double[] getFrames() {
		double[] frames = new double[results.length];
		for(int i=0; i<results.length; i++)
			frames[i] = i+1;
		return frames;
	}

	public Vector<FluorophorePair>[] getPairs() {
		return pairs;
	}

	public void run() {

		if (refs == null)
			return;
		if (tests == null)
			return;
		
		int nbframes = Math.min(refs.length, tests.length);
		double timeTotal = System.nanoTime();
		pairs = new Vector[nbframes];
		int ntest = 0;
		int nref = 0;
		boolean talk = logging.equals("Talk");
		boolean verbose = logging.equals("Verbose");
		
		int f1 = Math.max(0, frameBegin);
		int f2 = Math.min(nbframes, frameEnd);
		results = new CompareResult[f2];
		result = new CompareResult(dataset, software, algo, tolerance, pixelsize);
		walk.reset();
		for (int f = f1; f < f2; f++) {
			double time = System.nanoTime();
			Fluorophores ref = refs[f];
			Fluorophores test = tests[f];

			ntest += test.size();
			nref += ref.size();
			
			walk.progress("Frame " + f + "(" + ref.size() + ":" + test.size() + ")", (100 * f) / (f2 - f1 + 1));
			//if (talk || verbose)
			//	IJ.log("Frame: " + f + " nref:" + ref.size() + " ntest:" + test.size());

			for (int i = 0; i < ref.size(); i++)
				ref.get(i).matching = false;
			for (int i = 0; i < test.size(); i++)
				test.get(i).matching = false;

			switch (algo) {
			case ALGO_GLOBAL_SORT_NEAREST_NEIGHBORHOOR:
				pairs[f] = matchNNAlgo(ref, test, tolerance, true);
				break;
			case ALGO_NEAREST_NEIGHBORHOOR:
				pairs[f] = matchNNAlgo(ref, test, tolerance, false);
				break;
			case ALGO_HUNGARIAN:
				pairs[f] = matchHungarianAlgo(f, ref, test, tolerance);
				break;
			}

			results[f] = new CompareResult("Dataset", "Software", algo, tolerance, pixelsize);
			for (int i = 0; i < pairs[f].size(); i++) {
				results[f].add(pairs[f].get(i));
				result.add(pairs[f].get(i));
			}
			time = (System.nanoTime() - time) * 10e-9;
			results[f].compute(ref.size(), test.size(), time);
		}
		
		timeTotal = (System.nanoTime() - timeTotal) * 10e-9;
		result.compute(nref, ntest, timeTotal);

		walk.finish("Comparison Localization");
	}

	/**
	 * Matching two sets of fluorophores using the Nearest-Neighborhood
	 * algorithm. The version with the global sorting of the candidates is much
	 * better.
	 */
	private Vector<FluorophorePair> matchNNAlgo(Fluorophores ref, Fluorophores test, double tolerance, boolean globalSort) {
		Vector<FluorophorePair> candidates = new Vector<FluorophorePair>();
		for (int i = 0; i < ref.size(); i++) {
			Fluorophore a = ref.get(i);
			for (int j = 0; j < test.size(); j++) {
				if (a.distanceLateral(test.get(j)) <= tolerance)
					candidates.add(new FluorophorePair(a, test.get(j)));
			}
		}
		if (globalSort)
			Collections.sort(candidates);

		Vector<FluorophorePair> pairsFrame = new Vector<FluorophorePair>();
		for (int i = 0; i < candidates.size(); i++) {
			FluorophorePair pair = candidates.get(i);
			if (pair.ref.matching == false)
				if (pair.test.matching == false) {
					pair.ref.matching = true;
					pair.test.matching = true;
					pairsFrame.add(pair);
				}
		}
		

			return pairsFrame;
	}

	/**
	 * Matching two sets of fluorophores using the Hungarian algorithm.
	 */
	private Vector<FluorophorePair> matchHungarianAlgo(int frame, Fluorophores ref, Fluorophores test, double tolerance) {
		int M = ref.size();
		int N = test.size();
		ArrayList<WeightedEdge> graph = new ArrayList<WeightedEdge>();
		for (int i = 0; i < M; i++) {
			Fluorophore a = ref.get(i);
			for (int j = 0; j < N; j++) {
				double d = a.distanceLateral(test.get(j));
				if (d <= tolerance) {
					graph.add(new WeightedEdge(i, M + j, -d));
				}
			}
		}
		Vector<FluorophorePair> pairsFrame = new Vector<FluorophorePair>();

		MaximumWeightedMatching maximumWeightedMatching = new MaximumWeightedMatching(graph, M, N);
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Collection<Callable<MaximumWeightedMatching>> arr = new ArrayList<Callable<MaximumWeightedMatching>>();
		arr.add(maximumWeightedMatching);
		try {
			executor.invokeAll(arr, 10, TimeUnit.SECONDS);
			if (maximumWeightedMatching.isTerminated()) {
				ArrayList<WeightedEdge> matching = maximumWeightedMatching.getMaximumWeightMatching();
				for (int i = 0; i < matching.size(); i++) {
					Fluorophore r = ref.get(matching.get(i).source);
					Fluorophore d = test.get(matching.get(i).destination - M);
					FluorophorePair pair = new FluorophorePair(r, d);
					pairsFrame.add(pair);
				}
			}
			else {
				System.out.println("frame: " + frame + " ERROR not terminated ");
			}
		}
		catch (Exception e) {
			System.out.println("Error " + e);
			for (int i = 0; i < e.getStackTrace().length; i++)
				System.out.println("Error " + e.getStackTrace()[i]);

		}
		return pairsFrame;
	}

}
