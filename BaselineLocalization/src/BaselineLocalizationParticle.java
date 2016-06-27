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

import java.awt.Color;

import ij.IJ;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Overlay;

public class BaselineLocalizationParticle {

	public int frame;
	public double x;
	public double y;
	public double z;
	public double signal;
	public double snr;
	public double sigmaX;
	public double sigmaY;
	public double angleDH;

	public boolean bispot = false;
	public double xy1[] = new double[2];
	public double xy2[] = new double[2];;

	public BaselineLocalizationParticle(int frame, double mxy[], double signal, double snr) {
		this.frame = frame;
		this.x = mxy[0];
		this.y = mxy[1];
		this.sigmaX = mxy[2];
		this.sigmaY = mxy[3];
		this.signal = signal;
		this.snr = snr;
	}
	
	public void setBiSpot(double xy1[], double xy2[]) {
		this.xy1 = xy1;
		this.xy2 = xy2;
		this.bispot = true;
	}
	
	public void draw(Overlay overlay, Color c, double pixelsize) {
		double sx = Math.max(1, sigmaX);
		double sy = Math.max(1, sigmaY);
		if (bispot) {
			OvalRoi roi1 = new OvalRoi(xy1[0]/pixelsize - sx, xy1[1]/pixelsize - sy, sx*2, sy*2);
			roi1.setPosition(frame);
			roi1.setStrokeColor(c);
			roi1.setFillColor(c);
			overlay.add(roi1);
			OvalRoi roi2 = new OvalRoi(xy2[0]/pixelsize - sx, xy2[1]/pixelsize - sy, sx*2, sy*2);
			roi2.setPosition(frame);
			roi2.setStrokeColor(c);
			roi2.setFillColor(c);
			overlay.add(roi2);
			Line line = new Line(xy1[0]/pixelsize, xy1[1]/pixelsize, xy2[0]/pixelsize, xy2[1]/pixelsize);
			line.setPosition(frame);
			line.setStrokeColor(c);
			line.setFillColor(c);
			overlay.add(line);
		}
		else {
			double pi = x/pixelsize - sx;
			double pj = y/pixelsize - sy;
			OvalRoi roi = new OvalRoi(pi, pj, sx*2, sy*2);
			roi.setPosition(frame);
			roi.setStrokeColor(c);
			roi.setFillColor(c);
			overlay.add(roi);
		}
	}
	public String toString() {
		return "" + frame + ", " + IJ.d2s(x, 2) + ", " + IJ.d2s(y, 2) + ", " + IJ.d2s(z, 2) + ", " + 
				IJ.d2s(signal, 5) + ", " + IJ.d2s(snr, 5) + ", " + IJ.d2s(sigmaX, 5) + ", " + IJ.d2s(sigmaY, 5);

	}
}
