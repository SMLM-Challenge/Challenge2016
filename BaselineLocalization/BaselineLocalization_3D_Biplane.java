// =========================================================================================
//
// Single-Molecule Localization Microscopy Challenge 2016
// http://bigwww.epfl.ch/smlm/
//
// Author:
// Daniel Sage, http://bigwww.epfl.ch/sage/
// Biomedical Imaging Group (BIG)
// Ecole Polytechnique Federale de Lausanne (EPFL), CH-1015 Lausanne,
// Switzerland
//
// Reference:
// D. Sage, H. Kirshner, T. Pengo, N. Stuurman, J. Min, S. Manley, M. Unser
// Quantitative Evaluation of Software Packages for Single-Molecule Localization
// Microscopy
// Nature Methods 12, August 2015.
//
// Conditions of use:
// You'll be free to use this software for research purposes, but you should not 
// redistribute it without our consent. In addition, we expect you to include a 
// citation or acknowledgment whenever you present or publish results that are based on it.
//
// =========================================================================================


import ij.ImagePlus;
import ij.gui.Plot;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;

import java.util.ArrayList;
import java.util.Collections;

public class BaselineLocalization_3D_Biplane extends BaselineLocalizationAbstract implements PlugIn {

	public static void main(String args[]) {
		chooseImage();
		new BaselineLocalization_3D_Biplane().run("");
	}

	@Override
	public void run(String arg) {
		doDialog(Mode.BP, "Baseline Localization 3D Biplane");
	}
	
	@Override
	public ArrayList<BaselineLocalizationParticle> process(ImagePlus imp) {
		ArrayList<BaselineLocalizationParticle> all = new ArrayList<BaselineLocalizationParticle>();
		BaselineLocalizationOperators op = new BaselineLocalizationOperators(radiusPix, minSignal, minSNR, pixelsize, verbose, display);
		for (int frame = 1; frame <= framesMax; frame++) {
			FloatProcessor ip[] = split(imp.getStack().getProcessor(frame).convertToFloatProcessor());
			imp.setSlice(frame);
			ArrayList<int[]> candidates0 = op.detect(ip[0], op.filterDoG(ip[0], frame), false);
			ArrayList<int[]> candidates1 = op.detect(ip[1], op.filterDoG(ip[1], frame), false);
			ArrayList<BaselineLocalizationParticle> particles0 = op.localize(candidates0, ip[0], frame);
			ArrayList<BaselineLocalizationParticle> particles1 = op.localize(candidates1, ip[1], frame);
			ArrayList<BaselineLocalizationParticle> particles = pairingBiplane(particles0, particles1, imp.getWidth());
			all.addAll(particles);
			candidates0.addAll(candidates1);
			log(frame, candidates0, particles);
		}
		return all;
	}
	
	@Override
	public double[] calibrate(ImagePlus imp, int fmin, int fmax, double zmin, double zmax, double zstep, Roi roi) {
		double param[] = new double[fmax-fmin+1];
		double axial[] = new double[fmax-fmin+1];
		BaselineLocalizationOperators op = new BaselineLocalizationOperators(radiusPix, minSignal, minSNR, pixelsize, verbose, display);
		for (int z = 0; z <param.length; z++) {
			FloatProcessor ip[] = split(imp.getStack().getProcessor(z+fmin).convertToFloatProcessor());
			imp.setSlice(z+fmin);
			ip[0].setRoi(roi);
			ip[0] = (FloatProcessor)ip[0].crop();
			ip[1].setRoi(roi);
			ip[1] = (FloatProcessor)ip[1].crop();
			imp.setSlice(z+1);
			FloatProcessor fip0 = op.filterDoG(ip[0], z+fmin);
			FloatProcessor fip1 = op.filterDoG(ip[1], z+fmin);
			ArrayList<int[]> candidates0 = op.detect(ip[0], fip0, true);
			ArrayList<int[]> candidates1 = op.detect(ip[1], fip1, true);
			ArrayList<BaselineLocalizationParticle> particles0 = op.localize(candidates0, ip[0], z+fmin);
			ArrayList<BaselineLocalizationParticle> particles1 = op.localize(candidates1, ip[1], z+fmin);
			if (particles0.size() >= 1 && particles1.size() >= 1) {
				BaselineLocalizationParticle p0 = particles0.get(0);
				BaselineLocalizationParticle p1 = particles1.get(0);
				double w0 = (p0.sigmaX + p0.sigmaY) * 0.5;
				double w1 = (p1.sigmaX + p1.sigmaY) * 0.5;
				param[z] = w0 - w1;
				axial[z] = z*zstep + zmin;
			}
		}
		new Plot("Calibration Biplane", "angle", "Z in nm",  param, axial).show();	
		return linearRegression(param, axial);
	}
	
	private ArrayList<BaselineLocalizationParticle> pairingBiplane(ArrayList<BaselineLocalizationParticle> particles0, ArrayList<BaselineLocalizationParticle> particles1, int nx) {
		int n0 = particles0.size();
		int n1 = particles1.size();

		ArrayList<Pair> pairs = new ArrayList<Pair>();
		for (int i = 0; i < n0; i++) {
			BaselineLocalizationParticle p0 = particles0.get(i);
			for (int j = 0; j < n1; j++) {
				BaselineLocalizationParticle p1 = particles1.get(j);
				double dist = Math.sqrt((p0.x - p1.x) * (p0.x - p1.x) + (p0.y - p1.y) * (p0.y - p1.y));
				if (dist <= BP_distMaxPairing) {
					double x = p0.x;
					double y = p0.y;
					double w0 = (p0.sigmaX + p0.sigmaY) * 0.5;
					double w1 = (p1.sigmaX + p1.sigmaY) * 0.5;
					double z = BP_A * (w0-w1) + BP_B;
					double signal = p0.signal + p1.signal;
					double snr = (p0.snr + p1.snr) * 0.5;
					pairs.add(new Pair(p0.frame, i, j, x, y, z, signal, snr, dist));
				}
			}
		}
		Collections.sort(pairs);
		boolean taken0[] = new boolean[n0];
		for (int i = 0; i < n0; i++)
			taken0[i] = false;
		boolean taken1[] = new boolean[n1];
		for (int i = 0; i < n1; i++)
			taken1[i] = false;
		ArrayList<BaselineLocalizationParticle> particles = new ArrayList<BaselineLocalizationParticle>();
		for (int i = 0; i < pairs.size(); i++) {
			Pair pair = pairs.get(i);
			if (taken0[pair.k1] == false && taken1[pair.k2] == false) {
				taken0[pair.k1] = true;
				taken1[pair.k2] = true;
				double[] mxy = new double[] {pair.x, pair.y, 0.0, 0.0};
				BaselineLocalizationParticle particle = new BaselineLocalizationParticle(pair.frame, mxy, pair.signal, pair.snr);
				particle.z = pair.z;
				BaselineLocalizationParticle p1 = particles0.get(pair.k1);
				BaselineLocalizationParticle p2 = particles1.get(pair.k2);
				particle.setBiSpot(new double[] {p1.x, p1.y}, new double[] {p2.x + nx/2*pixelsize, p2.y});
				particles.add(particle);
			}
		}
		return particles;
	}

	private FloatProcessor[] split(FloatProcessor source) {
		int nx = source.getWidth();
		int ny = source.getHeight();
		FloatProcessor ipLeft = new FloatProcessor(nx/2, ny);
		for(int i=0; i<nx/2; i++)
			for(int j=0; j<ny; j++)
				ipLeft.putPixelValue(i, j, source.getPixelValue(i, j));
		FloatProcessor ipRight = new FloatProcessor(nx/2, ny);
		for(int i=0; i<nx/2; i++)
			for(int j=0; j<ny; j++)
				ipRight.putPixelValue(i, j, source.getPixelValue(i+nx/2, j));
		return new FloatProcessor[] {ipLeft , ipRight};
	}

	private class Pair implements Comparable<Pair> {
		public int		frame;
		public double	x;
		public double	y;
		public double	z;
		public int		k1;
		public int		k2;
		public double	signal;
		public double	dist;
		public double	snr;

		public Pair(int frame, int k1, int k2, double x, double y, double z, double signal, double snr, double dist) {
			this.frame = frame;
			this.k1 = k1;
			this.k2 = k2;
			this.x = x;
			this.y = y;
			this.z = z;
			this.signal = signal;
			this.snr = snr;
			this.dist = dist;
		}

		public int compareTo(Pair pair) {
			return dist > pair.dist ? 1 : -1;
		}
	}

}
