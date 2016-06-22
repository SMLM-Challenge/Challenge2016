package smlms.sample;

import ij.IJ;

import java.util.ArrayList;

import smlms.file.PositionFile;
import smlms.tools.Point3D;
import smlms.tools.PsRandom;
import additionaluserinterface.WalkBar;

public class Beads_Generator {
	
	private int 	nx = 6400; 	// 320 * 20 = 6400nm
	private int 	ny = 6400; 	// 320 * 20 = 6400nm
	private int 	nz = 1500; 						// 25 * 20 = 1500nm

	private double rmax = 50;
	private double rmin = 200;
	private double tolerance = 80;
	private double zoff = 750;
	
	private String filename = "/Users/sage/Desktop/positions-BD.csv";
	private int nbeads = 44;
	private int nfluos = 120000;
	
	private PsRandom rand = new PsRandom(1234);
	private WalkBar				walk			= new WalkBar("(c) 2016 EPFL, BIG", false, false, true);
	
	public class Bead {
		public double x;
		public double y;
		public double z;
		public double r;
		
		public Bead(double x, double y, double z, double r) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.r = r;
		}
		
		public double distance(Bead bead) {
			double d = (x-bead.x)*(x-bead.x) + (y-bead.y)*(y-bead.y) + (z-bead.z)*(z-bead.z);
			return Math.sqrt(d) - r - bead.r;
		}
		
		public String toString() {
			return "Bead " + x + " " + y + " " + z + " " + r;
		}
	}
	
	public static void main(String args[]) {
		new Beads_Generator();
	}
	
	public Beads_Generator() {
		
		ArrayList<Bead> beads = new ArrayList<Bead>();
		
		beads.add(create());
		
		for (int i=0; i<nbeads; i++) {
			Bead bead = (rand.nextDouble() < 0.6 ? create() : create(beads.get(beads.size()-1)));
			boolean close = false;
			for(int b=0; b<beads.size(); b++) 
				if (bead.distance(beads.get(b)) < tolerance)
					close = true;
			if (!close)
				beads.add(bead);
		}
		
		int count = 0;
		double volume[] = new double[beads.size()];
		for(Bead bead : beads) {
			volume[count] = (count == 0 ? 0.0 : volume[count-1]) + 4.0*Math.PI/3.0*bead.r*bead.r*bead.r;
			IJ.log("" + (count) + " / "+ bead.toString() + " / " + volume[count]);
			count++;
		}
		double max = volume[count-1];
		
		ArrayList<Point3D> positions = new ArrayList<Point3D>();
		for(int i=0; i<nfluos; i++) {
			double v = rand.nextDouble(0, max);
			int k = 0;
			for(k=0; k<volume.length; k++)
				if (v < volume[k])
					break;
			Bead bead = beads.get(k);
			double r = bead.r;
			double x = rand.nextDouble(bead.x - r, bead.x + r);
			double y = rand.nextDouble(bead.y - r, bead.y + r);
			double z = rand.nextDouble(bead.z - r, bead.z + r);
			double d = Math.sqrt((x-bead.x)*(x-bead.x) + (y-bead.y)*(y-bead.y) + (z-bead.z)*(z-bead.z));
			if (d < r)
				positions.add(new Point3D(x, y, z-zoff));
		}
		
		new PositionFile(walk, filename).save(positions);
	
	}
	
	public Bead create() {
		double r = rand.nextDouble(rmin, rmax);
		double x = rand.nextDouble(r+8*tolerance, nx-r-8*tolerance);
		double y = rand.nextDouble(r+8*tolerance, ny-r-8*tolerance);
		double z = rand.nextDouble(r+2*tolerance, nz-r-2*tolerance);
		return new Bead(x, y, z, r);
	}

	public Bead create(Bead bead) {
		double x = bead.x + rand.nextDouble(-bead.r*1.5, bead.r*1.5);
		double y = bead.y + rand.nextDouble(-bead.r*1.5, bead.r*1.5);
		double r = rand.nextDouble(rmin, rmax*0.5);
		double z = rand.nextDouble(r+tolerance, nz-r-tolerance);
		return new Bead(x, y, z, r);
	}

}
