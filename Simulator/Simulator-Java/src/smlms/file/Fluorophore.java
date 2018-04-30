// =========================================================================================
//
// Project: Localization Microscopy
//
// Author : Daniel Sage, Biomedical Imaging Group (BIG),
// http://bigwww.epfl.ch/sage/
//
// Organization: Ecole Polytechnique F�d�rale de Lausanne (EPFL), Lausanne,
// Switzerland
//
// Conditions of use: You'll be free to use this software for research purposes,
// but you
// should not redistribute it without our consent. In addition, we expect you to
// include a
// citation or acknowledgment whenever you present or publish results that are
// based on it.
//
// =========================================================================================
package smlms.file;

import ij.IJ;
import smlms.tools.Point3D;

public class Fluorophore {

	public int		id				= 0;
	public double	x				= 0.0;
	public double	y				= 0.0;
	public double	z				= 0.0;
	public int		frame			= 0;
	public double	photons			= 1;
	public int		channel			= 0;
	public int		frameon			= 0;
	public double	total			= 1;
	public double	backgroundMean	= 0.0;
	public double	backgroundStdev	= 0.0;
	public double	signalMean		= 0.0;
	public double	signalStdev		= 0.0;
	public double	signalPeak		= 0.0;
	public double	sigmax			= 0.0;
	public double	sigmay			= 0.0;
	public double	sigmaz			= 0.0;
	public double	uncertainty		= 0.0;
	public int		closestID		= 0;
	public double 	closestDistance = 0.0;
	public int		closestCount	= 0;
	public double	cnr				= 0.0;
	public double	snr				= 0.0;
	public double	psnr			= 0.0;
	public double	unknown			= 0.0;
	public boolean 	matching 		= false;

	public Fluorophore() {

	}

	public Fluorophore(int id, double x, double y, double z, int frame, double photons) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.z = z;
		this.frame = frame;
		this.photons = photons;
	}

	public void setSNR(double backgroundMean, double backgroundStdev, double signalPeak, double signalMean, double signalStdev) {
		this.backgroundMean = backgroundMean;
		this.backgroundStdev = backgroundStdev;
		this.signalPeak = signalPeak;
		this.signalMean = signalMean;
		this.signalStdev = signalStdev;
		this.psnr = backgroundStdev == 0 ? 0 : (signalPeak / backgroundStdev);
		this.cnr = backgroundStdev == 0 ? 0 : (signalPeak - backgroundMean) / backgroundStdev;
		double sb = Math.sqrt(backgroundStdev * backgroundStdev + signalStdev * signalStdev);
		this.snr = sb == 0 ? 0 : (signalPeak - backgroundMean) / sb;
	}

	public double[] vectorValues() {
		return new double[] { id, x, y, z, frame, photons, channel, frameon, total, backgroundMean, backgroundStdev, signalMean, signalStdev, signalPeak, sigmax, sigmay, sigmaz, uncertainty, closestID, closestDistance, closestCount, cnr, snr, psnr, unknown };
	}

	public static int vectorSize() {
		return vectorNames().length;
	}

	public static String[] vectorNames() {
		return new String[] { "ID", "X", "Y", "Z", "Frame", "Photons", "Channel", "Frame ON", "Total", "Background Mean", "Background Stdev", "Signal Mean", "Signal Stdev", "Signal Peak", "Sigma X", "Sigma Y", "Sigma Z", "Uncertainty", "Closest ID", "Closest Distance", "Closest Count", "CNR", "SNR", "PSNR", "Unknown" };
	}

	public String vectorValuesAsString() {
		double[] values = vectorValues();
		String s = "";
		for (double v : values)
			s += "" + v + ", ";
		return s + "\n";
	}

	public double[] getXYZFramePhotons() {
		return new double[] { x, y, z, frame, photons };
	}

	public double distance(Point3D p) {
		return Math.sqrt((p.x - x) * (p.x - x) + (p.y - y) * (p.y - y) + (p.z - z) * (p.z - z));
	}

	public double distance(Fluorophore f) {
		return Math.sqrt((f.x - x) * (f.x - x) + (f.y - y) * (f.y - y) + (f.z - z) * (f.z - z));
	}
	
	public double deltaX(Fluorophore fluo) {
		return fluo.x - x;
	}

	public double deltaY(Fluorophore fluo) {
		return fluo.y - y;
	}

	public double deltaZ(Fluorophore fluo) {
		return fluo.z - z;
	}

	public double differenceIntensity(Fluorophore fluo) {
		return fluo.photons - photons;
	}

	public int distanceFrame(Fluorophore fluo) {
		return Math.abs(fluo.frame - frame);
	}

	public double distanceLateral(Fluorophore fluo) {
		return Math.sqrt((fluo.x - x) * (fluo.x - x) + (fluo.y - y) * (fluo.y - y));
	}

	public double distanceAxial(Fluorophore fluo) {
		return Math.abs(fluo.z - z);
	}



	public Point3D scale(double scale) {
		return new Point3D(x * scale, y * scale, z * scale);
	}

	public Point3D negate() {
		return new Point3D(-x, -y, -z);
	}

	public void translate(double dx, double dy, double dz) {
		x += dx;
		y += dy;
		z += dz;
	}

	public void subtract(Point3D delta) {
		x = x - delta.x;
		y = y - delta.y;
		z = z - delta.z;
	}

	public Point3D translate(Point3D delta) {
		return new Point3D(x + delta.x, y + delta.y, z + delta.z);
	}

	public static Point3D read(String line) {
		String[] tokens = line.split("[,]");
		if (tokens.length == 3) {
			double x = Double.parseDouble(tokens[0]);
			double y = Double.parseDouble(tokens[1]);
			double z = Double.parseDouble(tokens[2]);
			return new Point3D(x, y, z);
		}
		else {
			return new Point3D(0, 0, 0);
		}
	}

	public String toString() {
		return " (" + IJ.d2s(x) + ",  " + IJ.d2s(y) + ",  " + IJ.d2s(z) + ") A=" + photons;
	}

}
