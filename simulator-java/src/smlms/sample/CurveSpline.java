//=========================================================================================
//
// Project: Localization Microscopy
//
// Author : Daniel Sage, Biomedical Imaging Group (BIG), http://bigwww.epfl.ch/sage/
//
// Organization: Ecole Polytechnique Federale de Lausanne (EPFL), Lausanne, Switzerland
//
// Conditions of use: You'll be free to use this software for research purposes, but you 
// should not redistribute it without our consent. In addition, we expect you to include a
// citation or acknowledgment whenever you present or publish results that are based on it.
//
//=========================================================================================
package smlms.sample;

import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Overlay;

import java.awt.Color;
import java.util.ArrayList;

import smlms.tools.Point3D;
import smlms.tools.Vector3D;

public class CurveSpline {

	private static double		C0			= 6.0;
	private static double		A			= Math.sqrt(3.0) - 2.0;
	private static double		accuracy	= 0.001;

	private Point3D				nodes[];

	private ArrayList<Point3D>	samples		= new ArrayList<Point3D>();

	public CurveSpline(Point3D nodes[], double sampling) {
		this.nodes = nodes;
		this.samples = getSamplesInterval(sampling);
	}

	public CurveSpline(ArrayList<Point3D> vnodes, double sampling) {
		int n = vnodes.size();
		this.nodes = new Point3D[n];
		for (int i = 0; i < n; i++)
			this.nodes[i] = vnodes.get(i);
		this.samples = getSamplesInterval(sampling);
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
		return getSamplesInterval(step);
	}

	public ArrayList<Point3D> getSamplesInterval(double step) {
		Point3D coef[] = doSymmetricalExponentialFilter(extend(nodes));
		ArrayList<Point3D> points = new ArrayList<Point3D>();
		Point3D current = nodes[0];
		points.add(current);
		double len = 0;
		for (int k = 1; k < coef.length - 2; k++) {
			for (double s = 0.0; s <= 1.0; s += accuracy) {
				Point3D p = getInterpolatedCubicValue(coef, k + s);
				len += p.distance(current);
				if (len >= step) {
					points.add(p);
					len = 0;
				}
				current = p;
			}
		}
		points.add(nodes[nodes.length - 1]);
		return points;
	}

	public ArrayList<Vector3D> getNormalsInterval(double step, double unit) {
		Point3D coef[] = doSymmetricalExponentialFilter(extend(nodes));
		ArrayList<Vector3D> normals = new ArrayList<Vector3D>();
		Point3D current = nodes[0];

		double len = 0;
		for (int k = 1; k < coef.length - 2; k++) {
			for (double s = 0.0; s <= 1.0; s += accuracy) {
				Point3D p = getInterpolatedCubicValue(coef, k + s);
				len += p.distance(current);
				if (len >= step) {
					Point3D p1 = getInterpolatedQuadraticValue(coef, k + s - 0.5);
					Point3D p2 = getInterpolatedQuadraticValue(coef, k + s + 0.5);
					double dx = p1.y - p2.y;
					double dy = p1.x - p2.x;
					double dz = p1.z - p2.z;
					double d = unit / Math.sqrt(dx * dx + dy * dy + dz * dz);
					if (d > 0)
						normals.add(new Vector3D(p, new Point3D(dx * d, dy * d, dz * d)));
					else
						normals.add(new Vector3D(p, p));
					len = 0;
				}
				current = p;
			}
		}
		return normals;
	}
	
	public Point3D[] prefilter() {
		return doSymmetricalExponentialFilter(extend(nodes));
	}
	
	public Point3D getClosestPoint(Point3D[] coef, Point3D a, double tolerance) {
		Point3D best = a;
		double min = Double.MAX_VALUE;
		for (int k = 1; k < coef.length-1; k++) {
			for (double s=0; s<1; s+=tolerance) {
				Point3D p = getInterpolatedCubicValue(coef, k+s);
				double d = p.distance(a);
				if (d < min) {
					min = d;
					best = p;
				}
			}
		}
		return best;
	}
	
	public ArrayList<Point3D> getSample(double tolerance) {
		Point3D[] coef = doSymmetricalExponentialFilter(extend(nodes));
		ArrayList<Point3D> samples = new ArrayList<Point3D>();
		for (int k = 1; k < coef.length-1; k++)
			for (double s=0; s<1; s+=tolerance)
				samples.add(getInterpolatedCubicValue(coef, k+s));
		return samples;
	}
	

	/*
	public Point3D getClosestPoint(Point3D[] coef, Point3D a, Tolerance tolerance) {
		Point3D best = a;
		double pos = 0;
		double min = Double.MAX_VALUE;
		for (int k = 1; k < coef.length-1; k++) {
			for (double s=0; s<1; s+=0.05) {
				Point3D p = getInterpolatedCubicValue(coef, k+s);
				double d = p.distance(a);
				if (d < min) {
					min = d;
					best = p;
					pos = k+s;
				}
			}
		}
		
		if (tolerance == Tolerance.HUNDREDTH || tolerance == Tolerance.THOUSANDTH) {
			for (double s=pos-0.05; s<=pos+0.05; s+=0.005) {
				Point3D p = getInterpolatedCubicValue(coef, s);
				double d = p.distance(a);
				if (d < min) {
					min = d;
					best = p;
					pos = s;
				}
			}
		}
		
		if (tolerance ==  Tolerance.THOUSANDTH) {
			for (double s=pos-0.005; s<=pos+0.005; s+=0.0005) {
				Point3D p = getInterpolatedCubicValue(coef, s);
				double d = p.distance(a);
				if (d < min) {
					min = d;
					best = p;
					pos = s;
				}
			}
		}
		if (pos <= 1)
			return new Point3D(Double.MAX_VALUE, Double.MAX_VALUE, 0);
		if (pos >= coef.length-2)
			return new Point3D(Double.MAX_VALUE, Double.MAX_VALUE, 0);

		//IJ.log("S:" + ((System.nanoTime()-chrono)/1000) + " " + pos + " " + best + " " + min);
		return best;
	}
	*/

	public double getLength() {
		Point3D coef[] = doSymmetricalExponentialFilter(extend(nodes));
		Point3D current = nodes[0];
		double len = 0;
		for (int k = 1; k < coef.length - 2; k++) {
			for (double s = 0; s <= 1; s += accuracy) {
				Point3D p = getInterpolatedCubicValue(coef, k + s);
				len += p.distance(current);
				current = p;
			}
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

	/*
	 * public void overlayDepthColorized(Vector<CurvePoint> points, Viewport
	 * viewport, Overlay overlay, int stroke) { double z1 =
	 * viewport.getCornerMinNano().z; double z2 = viewport.getCornerMaxNano().z;
	 * double thickness = z2 - z1; for (int s = 1; s<points.size(); s++) {
	 * CurvePoint pnm = points.get(s); //Point p1 =
	 * points.get(s-1).scale(resolution); //Point p2 = pnm.scale(resolution);
	 * CurvePoint p1 = viewport.screenPoint(points.get(s)); CurvePoint p2 =
	 * viewport.screenPoint(points.get(s-1)); //p1.translate(zero);
	 * //p2.translate(zero); Line line = new Line(p1.x, p1.y, p2.x, p2.y);
	 * line.setStrokeWidth(stroke); if (pnm.z < z1)
	 * line.setStrokeColor(Color.gray); else if (pnm.z > z2)
	 * line.setStrokeColor(Color.white); else { float h =
	 * (float)(pnm.z/thickness) + 0.5f; line.setStrokeColor(Color.getHSBColor(h,
	 * 1f, 1f)); } overlay.add(line); //OvalRoi roi = new OvalRoi(x2-2, y2-2, 4,
	 * 4); //roi.setStrokeColor(Color.GREEN); //overlay.add(roi); } }
	 */

	private Point3D getInterpolatedQuadraticValue(Point3D coef[], double s) {
		double[] cx = new double[coef.length];
		double[] cy = new double[coef.length];
		double[] cz = new double[coef.length];
		for (int i = 0; i < coef.length; i++) {
			cx[i] = coef[i].x;
			cy[i] = coef[i].y;
			cz[i] = coef[i].z;
		}
		double px = getInterpolatedQuadraticValue(cx, s);
		double py = getInterpolatedQuadraticValue(cy, s);
		double pz = getInterpolatedQuadraticValue(cz, s);
		return new Point3D(px, py, pz);
	}

	private Point3D getInterpolatedCubicValue(Point3D coef[], double s) {
		double[] cx = new double[coef.length];
		double[] cy = new double[coef.length];
		double[] cz = new double[coef.length];
		for (int i = 0; i < coef.length; i++) {
			cx[i] = coef[i].x;
			cy[i] = coef[i].y;
			cz[i] = coef[i].z;
		}
		double px = getInterpolatedCubicValue(cx, s);
		double py = getInterpolatedCubicValue(cy, s);
		double pz = getInterpolatedCubicValue(cz, s);
		return new Point3D(px, py, pz);
	}

	public double getInterpolatedQuadraticValue(double[] coef, double x) {
		int n = coef.length;
		int i = (int) Math.round(x);
		double t = x - i;

		double v0 = ((t - 0.5) * (t - 0.5)) / 2.0;
		double v2 = ((t + 0.5) * (t + 0.5)) / 2.0;
		double v1 = 1.0 - v0 - v2;

		int i0 = i - 1;
		if (i0 < 0)
			i0 = n - 2 - i0;

		int i1 = i;
		if (i1 >= n)
			i1 = i1 - n;

		int i2 = i + 1;
		if (i2 >= n)
			i2 = i2 - n;

		int i3 = i + 2;
		if (i3 >= n)
			i3 = i3 - n;

		return v0 * coef[i0] + v1 * coef[i1] + v2 * coef[i2];
	}

	/**
	 * Return an interpolated value (cubic) from B-spline coefficients at the
	 * position x. x should be in the range [0..n] n is the number of
	 * coefficients. Periodic conditions are applied.
	 */
	private double getInterpolatedCubicValue(double[] coef, double x) {
		int n = coef.length;
		int i = (int) Math.floor(x);
		double t = x - i;
		double t1 = 1.0 - t;
		double v0 = t1 * t1 * t1;
		double v1 = 4.0 + 3.0 * t * t * (t - 2.0);
		double v3 = t * t * t;

		int i0 = i - 1;
		if (i0 < 0)
			i0 = n - 2 - i0;

		int i1 = i;
		if (i1 >= n)
			i1 = i1 - n;

		int i2 = i + 1;
		if (i2 >= n)
			i2 = i2 - n;

		int i3 = i + 2;
		if (i3 >= n)
			i3 = i3 - n;

		return (v0 * coef[i0] + v1 * coef[i1] + (6.0 - v0 - v1 - v3) * coef[i2] + v3 * coef[i3]) / 6.0;
	}

	private Point3D[] doSymmetricalExponentialFilter(Point3D[] s) {
		double[] sx = new double[s.length];
		double[] sy = new double[s.length];
		double[] sz = new double[s.length];
		for (int i = 0; i < s.length; i++) {
			sx[i] = s[i].x;
			sy[i] = s[i].y;
			sz[i] = s[i].z;
		}
		doSymmetricalExponentialFilter(sx);
		doSymmetricalExponentialFilter(sy);
		doSymmetricalExponentialFilter(sz);

		Point3D[] coef = new Point3D[s.length];
		for (int i = 0; i < s.length; i++) {
			coef[i] = new Point3D(sx[i], sy[i], sz[i]);
		}
		return coef;
	}

	/**
	 * Performs the 1D symmetrical exponential filtering.
	 */
	private void doSymmetricalExponentialFilter(double s[]) {
		int n = s.length;
		double cn[] = new double[n];
		double cp[] = new double[n];
		cp[0] = computeInitialValueCausalMirror(s);

		for (int k = 1; k < n; k++)
			cp[k] = s[k] + A * cp[k - 1];
		cn[n - 1] = computeInitialValueAntiCausalMirror(cp);
		for (int k = n - 2; k >= 0; k--)
			cn[k] = A * (cn[k + 1] - cp[k]);
		for (int k = 0; k < n; k++)
			s[k] = C0 * cn[k];
	}

	/**
	 * Returns the initial value for the causal filter using the mirror boundary
	 * conditions.
	 */
	private double computeInitialValueCausalMirror(double signal[]) {
		double epsilon = 1e-6; // desired level of precision
		int k0 = (int) Math.ceil(Math.log(epsilon) / Math.log(Math.abs(A)));
		k0 = Math.min(k0, signal.length);
		double polek = A;
		double v = signal[0];

		for (int k = 1; k < k0; k++) {
			v = v + polek * signal[k];
			polek = polek * A;
		}
		return v;
	}

	/**
	 * Returns the initial value for the anti-causal filter using the mirror
	 * boundary conditions.
	 */
	private double computeInitialValueAntiCausalMirror(double signal[]) {
		int n = signal.length;
		double v = (A / (A * A - 1.0)) * (signal[n - 1] + A * signal[n - 2]);
		return v;
	}
}
