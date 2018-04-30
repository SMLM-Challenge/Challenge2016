//=========================================================================================
//
// Project: Localization Microscopy
//
// Author : Daniel Sage, Biomedical Imaging Group (BIG), http://bigwww.epfl.ch/sage/
//
// Organization: Ecole Polytechnique F�d�rale de Lausanne (EPFL), Lausanne, Switzerland
//
// Conditions of use: You'll be free to use this software for research purposes, but you 
// should not redistribute it without our consent. In addition, we expect you to include a
// citation or acknowledgment whenever you present or publish results that are based on it.
//
//=========================================================================================
package smlms.tools;

import ij.IJ;

public class Point3D {
	
	public double x;
	public double y;
	public double z;
		
	public Point3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Point3D(double coord[]) {
		this.x = coord[0];
		this.y = coord[1];
		this.z = coord[2];
	}
	
	public double distance(Point3D p) {
		return Math.sqrt((p.x-x)*(p.x-x) + (p.y-y)*(p.y-y) + (p.z-z)*(p.z-z));
	}
	
	public double distanceLateral(Point3D p) {
		return Math.sqrt((p.x-x)*(p.x-x) + (p.y-y)*(p.y-y));
	}

	public Point3D scale(double scale) {
		return new Point3D(x*scale, y*scale, z*scale);
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
		return " (" + IJ.d2s(x) + ",  " + IJ.d2s(y) + ",  " + IJ.d2s(z) + ")";
	}

}
