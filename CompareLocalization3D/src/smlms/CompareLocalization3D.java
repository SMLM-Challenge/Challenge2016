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
import java.util.Collections;

import smlms.hungarian.MaximumWeightedMatching;
import smlms.hungarian.WeightedEdge;
import additionaluserinterface.WalkBar;

public class CompareLocalization3D {

	public static final int	ALGO_GLOBAL_SORT_NEAREST_NEIGHBORHOOR	= 0;
	public static final int	ALGO_NEAREST_NEIGHBORHOOR	          = 1;
	public static final int	ALGO_HUNGARIAN	                      = 2;

	static public void main(String args[]) {
		new CompareLocalization3DDialog();
	}

	private Fluorophores	 	setRef[];
	private String	         	nameRef	= "";

	public CompareLocalization3D(String nameRef, Fluorophores setRef[]) {
		this.nameRef = nameRef;
		this.setRef = setRef;
	}

	public String[] run(WalkBar walk, String nameTest, Fluorophores setTest[], int num, String dataset, int algo, boolean dim3D, Wobble wobble, double minPhotons, double toleranceXY, double toleranceZ) {

		walk.reset();

		int nbframes = Math.min(setRef.length, setTest.length);
		double time = System.nanoTime();
		int intersection = 0;
		int union = 0;
		double rmseLateral = 0.0;
		double rmseAxial = 0.0;
		double jaccard = 0.0;
		double fscore = 0.0;
		double deltaX = 0.0;
		double deltaY = 0.0;
		double deltaZ = 0.0;
		double mdIntensity = 0.0;
		int ntest = 0;
		int nref = 0;

		int f2 = nbframes;

		for (int f = 0; f < nbframes; f++) {
			walk.progress("Frame " + f, (100.0 * f / f2));
			Fluorophores ref = Fluorophores.correctionWooble(wobble, setRef[f]);
			
			Fluorophores test = setTest[f];
  
			ntest += test.size();
			nref += ref.size();
			for (int i = 0; i < ref.size(); i++)
				ref.get(i).matching = false;
			for (int i = 0; i < test.size(); i++)
				test.get(i).matching = false;

			double tz = toleranceZ;
			if (!dim3D)
				tz = Double.MAX_VALUE;

			FluorophorePairs pairs = new FluorophorePairs();
			switch (algo) {
			case ALGO_NEAREST_NEIGHBORHOOR:
				pairs = matchNNAlgo(ref, test, dim3D, minPhotons, toleranceXY, tz, false);
				break;
			case ALGO_HUNGARIAN:
				pairs = matchHungarianAlgo(ref, test, dim3D, minPhotons, toleranceXY, tz);
				break;
			case ALGO_GLOBAL_SORT_NEAREST_NEIGHBORHOOR:
				pairs = matchNNAlgo(ref, test, dim3D, minPhotons, toleranceXY, tz, true);
				break;
			}
	/*		
			if (wobble != null)
				wobble.correctionTest(pairs);
	*/
			for (int i = 0; i < pairs.size(); i++) {
				FluorophorePair pair = pairs.get(i);
				intersection++;
				deltaX += pair.ref.deltaX(pair.test);
				deltaY += pair.ref.deltaY(pair.test);
				deltaZ += pair.ref.deltaZ(pair.test);
				mdIntensity += pair.ref.differenceIntensity(pair.test);
				rmseLateral += pair.ref.distanceLateral(pair.test) * pair.ref.distanceLateral(pair.test);
				rmseAxial += pair.ref.distanceAxial(pair.test) * pair.ref.distanceAxial(pair.test);
			}
		}
		time = (System.nanoTime() - time) * 10e-9;
		int TP = intersection;
		double FP = ntest - TP;
		double FN = nref - TP;
		double precision = TP / (TP + FP);
		double recall = TP / (TP + FN);
		union = nref + ntest - intersection;
		if (intersection > 0) {
			deltaX /= intersection;
			deltaY /= intersection;
			deltaZ /= intersection;
			mdIntensity /= intersection;
			rmseLateral = Math.sqrt(rmseLateral / intersection);
			rmseAxial = Math.sqrt(rmseAxial / intersection);
			jaccard = (100.0 * intersection) / union;
			fscore = 2.0 * (precision * recall) / (precision + recall);
		}
		
		String[] results = new String[24];
		int i = 0;
		results[i++] = "" + num;
		results[i++] = dataset;
		results[i++] = (algo == 0 ? "GS-NN" : (algo == 1 ? "NN" : "Hungarian"));
		results[i++] = "" + toleranceXY;
		results[i++] = "" + toleranceZ;
		results[i++] = String.format("%3.3f", time);
		results[i++] = nameRef;
		results[i++] = "" + nref;
		results[i++] = nameTest;
		results[i++] = "" + ntest;
		results[i++] = (dim3D ? "XYZ (cylinder)" : "XY (disk)");
		results[i++] = (wobble == null ? "NO" : wobble.getName());
		results[i++] = "" + minPhotons;
		results[i++] = "" + intersection;
		results[i++] = String.format("%3.3f", jaccard);
		results[i++] = String.format("%3.3f", fscore);
		results[i++] = String.format("%3.3f", precision);
		results[i++] = String.format("%3.3f", recall);
		results[i++] = String.format("%3.3f", rmseLateral);
		results[i++] = (dim3D ? String.format("%3.3f", rmseAxial) : "");
		results[i++] = String.format("%3.3f", deltaX);
		results[i++] = String.format("%3.3f", deltaY);
		results[i++] = (dim3D ? String.format("%3.3f", deltaZ) : "");
		results[i++] = String.format("%3.3f", mdIntensity);
		return results;
	}

	public static String[] getHeaders() {
		String[] results = new String[24];
		int i = 0;
		results[i++] = "ID";
		results[i++] = "Dataset";
		results[i++] = "Algorithm";
		results[i++] = "Tolerance XY";
		results[i++] = "Tolerance Z";
		results[i++] = "Time";
		results[i++] = "Reference";
		results[i++] = "# fluos";
		results[i++] = "Test";
		results[i++] = "# fluos";
		results[i++] = "Evaluation";
		results[i++] = "Wooble";
		results[i++] = "Min. Photons in Ref";
		results[i++] = "Intersection";
		results[i++] = "Jaccard";
		results[i++] = "Fscore";
		results[i++] = "Precision";
		results[i++] = "Recall";
		results[i++] = "RMSE Lateral";
		results[i++] = "RMSE Axial";
		results[i++] = "DeltaX";
		results[i++] = "DeltaY";
		results[i++] = "DeltaZ";
		results[i++] = "md Intensity";
		return results;
	}

	private FluorophorePairs matchNNAlgo(Fluorophores ref, Fluorophores test, boolean dim3D, double minPhotons, double toleranceXY, double toleranceZ, boolean globalSort) {
		ArrayList<FluorophorePair> candidates = new ArrayList<FluorophorePair>();
		for (int i = 0; i < ref.size(); i++) {
			Fluorophore a = ref.get(i);
			if (a.getPhotons() >= minPhotons) {
				for (int j = 0; j < test.size(); j++) {
					Fluorophore b = test.get(j);
					double dxy = a.distanceLateral(b);
					double dz = a.distanceAxial(b);
					if (dxy <= toleranceXY && dz <= toleranceZ) {
						candidates.add(new FluorophorePair(a, b, dim3D ? a.distance(b) : dxy));
					}
				}
			}
		}
		if (globalSort)
			Collections.sort(candidates);

		FluorophorePairs pairsFrame = new FluorophorePairs();
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

	private FluorophorePairs matchHungarianAlgo(Fluorophores ref, Fluorophores test, boolean dim3D, double minPhotons, double toleranceXY, double toleranceZ) {
		int M = ref.size();
		int N = test.size();
		ArrayList<WeightedEdge> graph = new ArrayList<WeightedEdge>();
		for (int i = 0; i < M; i++) {
			Fluorophore a = ref.get(i);
			if (a.getPhotons() >= minPhotons) {
				for (int j = 0; j < N; j++) {
					Fluorophore b = test.get(j);
					double dxy = a.distanceLateral(b);
					double dz = a.distanceAxial(b);
					if (dxy <= toleranceXY && dz <= toleranceZ) {
						graph.add(new WeightedEdge(i, M + j, dim3D ? -a.distance(b) : -dxy));
					}
				}
			}
		}
		FluorophorePairs pairsFrame = new FluorophorePairs();
		MaximumWeightedMatching maximumWeightedMatching = new MaximumWeightedMatching(graph, M, N);
		ArrayList<WeightedEdge> matching = maximumWeightedMatching.getMaximumWeightMatching();
		for (int i = 0; i < matching.size(); i++) {
			Fluorophore r = ref.get(matching.get(i).source);
			Fluorophore d = test.get(matching.get(i).destination - M);
			FluorophorePair pair = new FluorophorePair(r, d, dim3D ? r.distance(d) : r.distanceLateral(d));
			pairsFrame.add(pair);
		}
		return pairsFrame;
	}
}
