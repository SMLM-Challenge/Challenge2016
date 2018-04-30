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
import java.util.Collections;

import ij.ImagePlus;
import ij.gui.Plot;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;

public class BaselineLocalization_3D_DoubleHelix extends BaselineLocalizationAbstract implements PlugIn {

	public static void main(String args[]) {
		chooseImage();
		new BaselineLocalization_3D_DoubleHelix().run("");
	}

	@Override
	public void run(String arg) {
		doDialog(Mode.DH, "Baseline Localization 3D DoubleHelix");
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
			ArrayList<BaselineLocalizationParticle> spots = op.localize(candidates, ip, frame);
			ArrayList<BaselineLocalizationParticle> particles = pairingDoubleHelix(spots);
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
			ip.setRoi(roi);
			ip = (FloatProcessor)ip.crop();
			imp.setSlice(z+fmin);
			FloatProcessor fip = op.filterDoG(ip, z+fmin);
			ArrayList<int[]> candidates = op.detect(ip, fip, true);
			ArrayList<BaselineLocalizationParticle> particles = op.localize(candidates, ip, z+fmin);
			if (particles.size() >= 1) {
				BaselineLocalizationParticle p = particles.get(0);
				ArrayList<int[]> candq = searchCloseLocalMax(fip, p, z, pixelsize);
				ArrayList<BaselineLocalizationParticle> particles2 = op.localize(candq, ip, z+1);
				if (particles2.size() >= 1) {
					BaselineLocalizationParticle q  = particles2.get(0);
					double angle = Math.toDegrees(Math.atan((p.x-q.x)/(p.y-q.y)));
					param[z] = angle;
					axial[z] = z*zstep + zmin;
				}
			}
		}
		new Plot("Calibration Double-Helix", "dx", "Z in nm",  param, axial).show();	
		return linearRegression(param, axial);
	}
	
	private ArrayList<int[]> searchCloseLocalMax(FloatProcessor fip, BaselineLocalizationParticle p, int z, double pixelsize) {
		int nx = fip.getWidth();
		int ny = fip.getHeight();
		double max = -Double.MAX_VALUE;
		int imax = nx/2;
		int jmax = ny/2;
		int h = 2;
		// Find the local max
		for (int i = h; i < nx - h; i++)
		for (int j = h; j < ny - h; j++) {
			double x = i*pixelsize;
			double y = j*pixelsize;
			double d = Math.sqrt((p.x-x)*(p.x-x) + (p.y-y)*(p.y-y));
			if (d > 200 && d < 2000) {
				float v = fip.getf(i, j);
				if (v > fip.getf(i - 1, j))
				if (v > fip.getf(i + 1, j)) 
				if (v > fip.getf(i - 1, j - 1))
				if (v > fip.getf(i, j - 1))
				if (v > fip.getf(i + 1, j - 1))
				if (v > fip.getf(i - 1, j + 1)) 
				if (v > fip.getf(i, j + 1))
				if (v > fip.getf(i + 1, j + 1))
				if (v > max ) {
					imax = i;
					jmax = j;
					max = v;
				}
			}
		}
		ArrayList<int[]> cand = new ArrayList<int[]>();
		cand.add(new int[] {imax, jmax});
		return cand;
	}
		
	private ArrayList<BaselineLocalizationParticle> pairingDoubleHelix(ArrayList<BaselineLocalizationParticle> spots) {
		int n = spots.size();
		ArrayList<Pair> pairs = new ArrayList<Pair>();
		for(int i=0; i<n; i++) {
			BaselineLocalizationParticle p = spots.get(i);
			for(int j=0; j<n; j++) {
				BaselineLocalizationParticle q = spots.get(j);
				double dist = Math.sqrt((p.x-q.x)*(p.x-q.x) + (p.y-q.y)*(p.y-q.y));
				if (dist >= DH_distMinPairing && dist <= DH_distMaxPairing) {
					double diffSignal = Math.abs(p.signal-q.signal);
					double x = (p.x + q.x)*0.5;
					double y = (p.y + q.y)*0.5;
					double angle = Math.toDegrees(Math.atan((p.x-q.x)/(p.y-q.y)));
					double z = DH_A * angle + DH_B;
					double signal = (p.signal+q.signal)*0.5;
					double snr = (p.snr+q.snr)*0.5;
					pairs.add(new Pair(p.frame, i, j, x, y, z, signal, snr, p.sigmaX, p.sigmaY, diffSignal, angle));
				}
			}
		}
		Collections.sort(pairs);
		boolean taken[] = new boolean[n];
		for(int i=0; i<n; i++)
			taken[i] = false;
		
		ArrayList<BaselineLocalizationParticle> particles = new ArrayList<BaselineLocalizationParticle>();
		for(int i=0; i<pairs.size(); i++) {
			Pair pair = pairs.get(i);
			if (taken[pair.k1] == false && taken[pair.k2] == false) {
				taken[pair.k1] = true;
				taken[pair.k2] = true;
				double[] mxy = new double[] {pair.x, pair.y, pair.sigmax, pair.sigmay};
				BaselineLocalizationParticle particle = new BaselineLocalizationParticle(pair.frame, mxy, pair.signal, pair.snr);
				particle.z = pair.z;
				particle.angleDH = pair.angle;
				BaselineLocalizationParticle p1 = spots.get(pair.k1);
				BaselineLocalizationParticle p2 = spots.get(pair.k2);
				particle.setBiSpot(new double[] {p1.x, p1.y}, new double[] {p2.x, p2.y});
				particles.add(particle);
			}
		}
		return particles;		
	}
	
	private class Pair implements Comparable<Pair> {
		public int frame;
		public double x;
		public double y;
		public double z;
		public int k1;
		public int k2;
		public double signal;
		public double diffSignal;
		public double snr;
		public double sigmax;
		public double sigmay;
		public double angle;
	
		public Pair(int frame, int k1, int k2, double x, double y, double z, double signal, double snr, double sigmax, double sigmay, double diffSignal, double angle) {
			this.frame = frame;
			this.k1 = k1;
			this.k2 = k2;
			this.x = x;
			this.y = y;
			this.z = z;
			this.signal = signal;
			this.snr = snr;
			this.diffSignal = diffSignal;
			this.sigmax = sigmax;
			this.sigmay = sigmay;
			this.angle = angle;
		}
		
		public int compareTo(Pair pair) {
			return diffSignal > pair.diffSignal ? 1 : -1;
		}
	}
}
