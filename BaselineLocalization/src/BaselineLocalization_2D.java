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

import java.util.ArrayList;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;

public class BaselineLocalization_2D extends BaselineLocalizationAbstract implements PlugIn {
	
	public static void main(String args[]) {
		chooseImage();
		new BaselineLocalization_2D().run("");
	}

	@Override
	public void run(String arg) {
		doDialog(Mode.D2, "Baseline Localization 2D");
	}
	
	@Override
	public ArrayList<BaselineLocalizationParticle> process(ImagePlus imp) {
		ArrayList<BaselineLocalizationParticle> all = new ArrayList<BaselineLocalizationParticle>();
		BaselineLocalizationOperators op = new BaselineLocalizationOperators(radiusPix, minSignal, minSNR, pixelsize, verbose, display);
		for (int frame = 1; frame <= framesMax; frame++) {
			FloatProcessor ip = imp.getStack().getProcessor(frame).convertToFloatProcessor();
			imp.setSlice(frame);
			FloatProcessor fip = op.filterDoG(ip, frame);
			ArrayList<int[]> candidates = op.detect(ip, fip, false);
			ArrayList<BaselineLocalizationParticle> particles = op.localize(candidates, ip, frame);
			all.addAll(particles);
			log(frame, candidates, particles);
		}
		return all;
	}
	
	@Override
	public double[] calibrate(ImagePlus imp, int fmin, int fmax, double zmin, double zmax, double zstep, Roi roi) {
		// no calibration for the 2D case
		return new double[] {1.0, 0.0};
	}
}
