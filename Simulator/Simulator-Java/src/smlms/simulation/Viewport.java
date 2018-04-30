package smlms.simulation;

import ij.IJ;

import java.io.PrintStream;

import smlms.file.Fluorophore;
import smlms.tools.Point3D;
import smlms.tools.Tools;


public class Viewport {

	private double fovx;		// nano
	private double fovy;		// nano
	private double thickness;	// nano
	private double pixelsize;	// nano
	private Point3D origin;		// nano
	
	public Viewport(Point3D origin, double fovx, double fovy, double thickness, double pixelsize) {
		this.origin = origin;
		this.fovx = fovx;
		this.fovy = fovy;
		this.thickness = thickness;
		this.pixelsize = pixelsize;
	}
	
	public Viewport(Point3D origin, Point3D size, double pixelsize) {
		this.origin = origin;
		this.fovx = size.x;
		this.fovy = size.y;
		this.thickness = size.z;
		this.pixelsize = pixelsize;
	}
	
	public double getSurface() {
		return fovx*fovy;
	}
	
	public boolean inside(Point3D pt) {
		if (pt.x < origin.x)
			return false;
		if (pt.y < origin.y)
			return false;
		if (pt.z < origin.z)
			return false;
		if (pt.x > origin.x + fovx)
			return false;
		if (pt.y > origin.y + fovy)
			return false;
		if (pt.z > origin.z + thickness)
			return false;
		return true;
	}

	public boolean insideXY(Fluorophore fluo) {
		if (fluo.x < origin.x)
			return false;
		if (fluo.y < origin.y)
			return false;
		if (fluo.x > origin.x + fovx)
			return false;
		if (fluo.y > origin.y + fovy)
			return false;
		return true;
	}
	
	public String getInfo() {
		String sx = "X/" + IJ.d2s(origin.x) + " ... " + IJ.d2s(origin.x+fovx) + " ";
		String sy = "Y/" + IJ.d2s(origin.y) + " ... " + IJ.d2s(origin.y+fovy) + " ";
		String sz = "Z/" + IJ.d2s(origin.z) + " ... " + IJ.d2s(origin.z+thickness) + " ";
		return sx + sy + sz;
	}

	public void setThicknessNano(double thickness) {
		this.thickness = thickness;
	}

	public double getThicknessNano() {
		return thickness;
	}

	public double getFoVXNano() {
		return fovx;
	}

	public double getFoVYNano() {
		return fovy;
	}

	public int convertIntegerPixel(double anm) {
		return Tools.round(anm / pixelsize);
	}

	public double convertPixel(double anm) {
		return anm / pixelsize;
	}
	
	public double convertNano(double apix) {
		return apix * pixelsize;
	}


	public double screenX(double xnm) {
		return (xnm - origin.x) / pixelsize;
	}

	public double screenY(double ynm) {
		return (ynm - origin.y) / pixelsize;
	}

	public int screenZ(double znm) {
		return Tools.round((znm - origin.z) / pixelsize);
	}

	public Point3D screenPoint(Point3D pnm) {
		return pnm.translate(origin.negate()).scale(1.0/pixelsize);
	}
	
	public double getPixelsize() {
		return pixelsize;
	}

	public Point3D getCornerMinNano() {
		double x1 = origin.x;
		double y1 = origin.y;
		double z1 = origin.z;
		return (new Point3D(x1, y1, z1));
	}

	public Point3D getCornerMaxNano() {
		double x1 = origin.x + fovx;
		double y1 = origin.y + fovy;
		double z1 = origin.z + thickness;
		return (new Point3D(x1, y1, z1));
	}

	public Point3D getCornerPixel() {
		return getCornerMinNano().scale(1.0/pixelsize);
	}

	public int getFoVXPixel() {
		return (int)Math.round(fovx / pixelsize);
	}

	public int getFoVYPixel() {
		return (int)Math.round(fovy / pixelsize);
	}

	public int getThicknessPixel() {
		return (int)Math.ceil(thickness / pixelsize);
	}

	public Point3D getSizePixel() {
		return getSizeNano().scale(1.0/pixelsize);
	}
	
	public Point3D getSizeNano() {
		return (new Point3D(fovx, fovy, thickness));
	}

	public Point3D getCornerMinPixel() {
		return getCornerPixel();
	}
	
	public Point3D getOrigin() {
		return origin;
	}
	
	public Point3D getCornerMaxPixel() {
		double x2 = origin.x + fovx;
		double y2 = origin.y + fovy;
		double z2 = origin.z  + thickness;
		return (new Point3D(x2, y2, z2)).scale(1.0/pixelsize);
	}
	
	public String toString() {
		return "Vol (" + fovx + "x" + fovy + "x" + thickness + ") at " + pixelsize + " nm";
	}
	
	public void report(PrintStream out) {	
		out.print("<h2>Viewport</h2>");
		out.print("<table cellpadding=5>");			
		out.print("<tr><td>Sample</td><td>Field of view</td><td>" + getFoVXNano() + "x" +  getFoVYNano() + "</td><td>nm</td></tr>");
		out.print("<tr><td></td><td>Field of view</td><td>" + getFoVXPixel() + "x" +  getFoVYPixel() + "</td><td>pixel</td></tr>");
		out.print("<tr><td></td><td>Thickness</td><td>" + getThicknessNano() + "</td><td>nm</td></tr>");
		out.print("<tr><td></td><td>Thickness</td><td>" + getThicknessPixel() + "</td><td>slices</td></tr>");
		out.print("</table>");
	}


}
