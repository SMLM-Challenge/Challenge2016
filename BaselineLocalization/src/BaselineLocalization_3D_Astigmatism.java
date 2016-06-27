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

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Plot;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;

public class BaselineLocalization_3D_Astigmatism extends BaselineLocalizationAbstract implements PlugIn {
	
	public static void main(String args[]) {
		chooseImage();
		new BaselineLocalization_3D_Astigmatism().run("");
	}

	@Override
	public void run(String arg) {
		doDialog(Mode.AS, "Baseline Localization 3D Astigmatism");
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
			for (BaselineLocalizationParticle particle : particles)
				particle.z = AS_A * (particle.sigmaX-particle.sigmaY) + AS_B;
			all.addAll(particles);
			log(frame, candidates, particles);
		}
		return all;
	}
	
	@Override
	public double[] calibrate(ImagePlus imp, int fmin, int fmax, double zmin, double zmax, double zstep, Roi roi) {
		double param[] = new double[fmax-fmin+1];
		double axial[] = new double[fmax-fmin+1];
		BaselineLocalizationOperators op = new BaselineLocalizationOperators(radiusPix, minSignal, minSNR, pixelsize, verbose, display);
		for (int z = 0; z <param.length; z++) {
			FloatProcessor ip = imp.getStack().getProcessor(z+fmin).convertToFloatProcessor();
			imp.setSlice(z+fmin);
			ip.setRoi(roi);
			ip = (FloatProcessor)ip.crop();
			FloatProcessor fip = op.filterDoG(ip, z+fmin);
			ArrayList<int[]> candidates = op.detect(ip, fip, true);
			ArrayList<BaselineLocalizationParticle> particles = op.localize(candidates, ip, z+fmin);
			if (particles.size() == 1) {
				BaselineLocalizationParticle p = particles.get(0);
				param[z] = p.sigmaX - p.sigmaY;
				axial[z] = z*zstep + zmin;
			}
		}
		new Plot("Calibration Astimagtism", "sigmaX - sigmaY", "Z in nm", param, axial).show();	
		return linearRegression(param, axial);
	}
}
