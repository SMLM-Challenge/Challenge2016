package smlms.tools;

import imageware.Builder;
import imageware.ImageWare;
import smlms.file.Fluorophore;

public class Volume {

	public double x1;
	public double x2;
	public double y1;
	public double y2;
	public double z1;
	public double z2;
	
	public Volume(double x1, double x2, double y1, double y2, double z1, double z2) {
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
		this.z1 = z1;
		this.z2 = z2;
	}

	public Volume(Point3D origin, Point3D dim) {
		this.x1 = origin.x;
		this.x2 = origin.x + dim.x;
		this.y1 = origin.y;
		this.y2 = origin.y + dim.y;
		this.z1 = origin.z;
		this.z2 = origin.z + dim.z;
	}

	public boolean contains(Fluorophore point) {
		if (point.x < x1)
			return false;
		if (point.x > x2)
			return false;
		if (point.y < y1)
			return false;
		if (point.y > y2)
			return false;
		if (point.z < z1)
			return false;
		if (point.z > z2)
			return false;
		return true;
	}

	public boolean contains(Point3D point) {
		if (point.x < x1)
			return false;
		if (point.x > x2)
			return false;
		if (point.y < y1)
			return false;
		if (point.y > y2)
			return false;
		if (point.z < z1)
			return false;
		if (point.z > z2)
			return false;
		return true;
	}
	
	public ImageWare allocate3D(double pixelsize) {
		int nx = (int)Math.ceil((x2-x1)/pixelsize);
		int ny = (int)Math.ceil((y2-y1)/pixelsize);
		int nz = (int)Math.ceil((z2-z1)/pixelsize);
		return Builder.create(nx, ny, nz, ImageWare.FLOAT);
	}

	public ImageWare allocate2D(double pixelsize) {
		int nx = (int)Math.ceil((x2-x1)/pixelsize);
		int ny = (int)Math.ceil((y2-y1)/pixelsize);
		return Builder.create(nx, ny, 1, ImageWare.FLOAT);
	}

	public String toString() {
		return "Volume (" + x1 + ", " + y1 + ", " + z1 + ") " + " (" + x2 + ", " + y2 + ", " + z2 + ") "; 
	}
}
