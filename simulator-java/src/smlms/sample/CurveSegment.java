package smlms.sample;

//=========================================================================================
//
//Project: Localization Microscopy
//
//Author : Daniel Sage, Biomedical Imaging Group (BIG), http://bigwww.epfl.ch/sage/
//
//Organization: Ecole Polytechnique Federale de Lausanne (EPFL), Lausanne, Switzerland
//
//Conditions of use: You'll be free to use this software for research purposes, but you 
//should not redistribute it without our consent. In addition, we expect you to include a
//citation or acknowledgment whenever you present or publish results that are based on it.
//
//=========================================================================================

import ij.IJ;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import imageware.Builder;
import imageware.ImageWare;

import java.awt.Color;
import java.util.ArrayList;

import smlms.tools.Point3D;

public class CurveSegment {

	private static double		accuracy	= 0.001;

	private Point3D				nodes[];

	private ArrayList<Point3D>	samples		= new ArrayList<Point3D>();

	public CurveSegment(Point3D nodes[], double sampling, double sigma) {
		this.nodes = nodes;
		this.samples = getSamplesInterval(sampling, sigma);
	}

	public CurveSegment(ArrayList<Point3D> vnodes, double sampling, double sigma) {
		int n = vnodes.size();
		this.nodes = new Point3D[n];
		for (int i = 0; i < n; i++)
			this.nodes[i] = vnodes.get(i);
		this.samples = getSamplesInterval(sampling, sigma);
	}

	public Point3D[] getNodes() {
		return nodes;
	}

	public Point3D getFirstNode() {
		return nodes[0];
	}

	public ArrayList<Point3D> getInitialSamples() {
		return samples;
	}

	public ArrayList<Point3D> getSamplesNumber(int nsamples) {
		double len = getLength();
		double step = len / (nsamples - 1);
		return getSamplesInterval(step, 0);
	}

	public ArrayList<Point3D> getSamplesInterval(double step, double sigma) {
		ArrayList<Point3D> points = new ArrayList<Point3D>();
		for (int k = 1; k < nodes.length; k++) {
			double d = nodes[k-1].distance(nodes[k]);
			if (d > step*0.1) {
				int n = (int)(d/step);
				double stepCorr = d / n;
				double nn = d / stepCorr;
				double dx = (nodes[k].x - nodes[k-1].x) / nn;
				double dy = (nodes[k].y - nodes[k-1].y) / nn;
				double dz = (nodes[k].z - nodes[k-1].z) / nn;
				
				for (int i = 0; i<=n; i++) {
					Point3D p = new Point3D(dx * i + nodes[k-1].x, dy * i + nodes[k-1].y, dz * i + nodes[k-1].z);
					points.add(p);
				}
				
			}
		}
		IJ.log(" CurveSegment : nodes= " + nodes.length + " len:" +getLength() + " step=" + step + " nbsamples=" + points.size());
		
		int n = points.size();
		double x[] = new double[n];
		double y[] = new double[n];
		double z[] = new double[n];
		for(int i=0; i<n; i++) {
			Point3D p = points.get(i);
			x[i] = p.x;
			y[i] = p.y;
			z[i] = p.z;
		}
			
		ImageWare ix = Builder.create(x);
		ImageWare iy = Builder.create(y);
		ImageWare iz = Builder.create(z);
System.out.print(" ---------------------------------------------- SMOOTH " + sigma);
		if (sigma > 0.1) {
			ix.smoothGaussian(sigma);
			iy.smoothGaussian(sigma);
			iz.smoothGaussian(sigma);
		}
		ArrayList<Point3D> spoints = new ArrayList<Point3D>();
		for(int i=0; i<n; i++) {
			spoints.add(new Point3D(ix.getPixel(i, 0, 0), iy.getPixel(i, 0, 0), iz.getPixel(i, 0, 0)));
		}

		return spoints;
	}

		
	public Point3D getClosestPoint(ArrayList<Point3D> list, Point3D a) {
		Point3D best = a;
		double min = Double.MAX_VALUE;
		for (Point3D p : list) {
			double d = p.distance(a);
			if (d < min) {
				min = d;
				best = p;
			}
		}
		return best;
	}
	
	public double getLength() {
		double len = 0.0;
		for (int k = 1; k < nodes.length; k++) {
			len += nodes[k-1].distance(nodes[k]);
		}
		return len;
	}

	private Point3D[] extend(Point3D nodes[]) {
		int nnodes = nodes.length;
		Point3D extendedNodes[] = new Point3D[nnodes + 2];
		extendedNodes[0] = nodes[0];
		for (int i = 0; i < nnodes; i++)
			extendedNodes[i + 1] = nodes[i];
		extendedNodes[nnodes + 1] = nodes[nnodes - 1];
		return extendedNodes;
	}

	public void overlayNodes(Overlay overlay) {
		int nnodes = nodes.length;
		for (int k = 0; k < nnodes; k++) {
			int x = (int) Math.round(nodes[k].x);
			int y = (int) Math.round(nodes[k].y);
			OvalRoi roi = new OvalRoi(x - 5, y - 5, 10, 10);
			roi.setStrokeColor(Color.RED);
			overlay.add(roi);
		}
	}

	public void overlayInitialSamples(Overlay overlay, Color color, int radiusSample, Color colorPoint) {
		overlaySamples(samples, overlay, color, radiusSample, colorPoint);
	}

	public void overlaySamples(ArrayList<Point3D> points, Overlay overlay, Color color, int radiusSample, Color colorPoint) {
		for (int s = 1; s < points.size(); s++) {
			int x1 = (int) Math.round(points.get(s - 1).x);
			int y1 = (int) Math.round(points.get(s - 1).y);
			int x2 = (int) Math.round(points.get(s).x);
			int y2 = (int) Math.round(points.get(s).y);
			Line line = new Line(x1, y1, x2, y2);
			line.setStrokeColor(color);
			overlay.add(line);
			if (radiusSample > 0) {
				OvalRoi roi = new OvalRoi(x2 - radiusSample, y2 - radiusSample, 2 * radiusSample, 2 * radiusSample);
				roi.setStrokeColor(colorPoint);
				overlay.add(roi);
			}
		}
		if (radiusSample > 0) {
			int x2 = (int) Math.round(points.get(0).x);
			int y2 = (int) Math.round(points.get(0).y);
			OvalRoi roi = new OvalRoi(x2 - radiusSample, y2 - radiusSample, 2 * radiusSample, 2 * radiusSample);
			roi.setStrokeColor(colorPoint);
			overlay.add(roi);
		}
	}
}
