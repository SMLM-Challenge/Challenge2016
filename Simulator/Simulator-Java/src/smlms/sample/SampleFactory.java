package smlms.sample;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import ij.IJ;
import imageware.Builder;
import imageware.ImageWare;

import java.util.ArrayList;
import java.util.Random;

import smlms.tools.NormalizedVariable;
import smlms.tools.Point3D;
import smlms.tools.PsRandom;
import additionaluserinterface.WalkBar;

public class SampleFactory {

	private PsRandom	     rand	 = new PsRandom(1234);
	private Sample	         sample;
	private WalkBar	         walk;
	private Belong[][][]	 belongs;
	private Tube[]	         tubes;
	private int	             mx;
	private int	             my;
	private int	             mz;
	private double	         pixelsize;
	private ArrayList<int[]>	list	= new ArrayList<int[]>();

	public SampleFactory(Point3D dim, WalkBar walk, Sample sample, double pixelsize) {
		this.walk = walk;
		this.sample = sample;
		this.pixelsize = pixelsize;
		mx = (int) Math.ceil(dim.x / pixelsize);
		my = (int) Math.ceil(dim.y / pixelsize);
		mz = (int) Math.ceil(dim.z / pixelsize);
		IJ.log("\nMask size " + mx + " " + my + " " + mz);
		// belongs = new Belong[mx][my][mz];
	}

	public void createPositionsStep(int nbfluos, double timeout, double step, double sigma) {
		int count = 0;
		double chrono = System.nanoTime();
		tubes = sample.getTubes();
		int nt = tubes.length;
		
		for (int i = 0; i<nt; i++) {
			//
			//NormalizedVariable radiusMT = new NormalizedVariable(12);
			//NormalizedVariable thicknessMT = new NormalizedVariable(5);
			
			NormalizedVariable radiusER = new NormalizedVariable(150);
			NormalizedVariable thicknessER = new NormalizedVariable(6);
			radiusER.addLinear(5-i, -35+2*i);
			radiusER.addCosine(15, 2.5+i, 0.1);
			thicknessER.addCosine(1, 4.5+i, 0.5);

			tubes[i].radius = radiusER;
			tubes[i].thickness = thicknessER;
		}

		double cumul[] = new double[nt];
		double total = 0;
		for (int i = 0; i<nt; i++) {
			Point3D nodes[] = tubes[i].axis.getNodes();
			for(int k=0; k<nodes.length; k++)
				if (k%100==0)
					System.out.println("SampleFactory >>>>>>>>>>>>>>>> node avent init  tube = "  + i + " " + nodes[k]);

			tubes[i].initStep(step, sigma);
			
			for (int j = 0; j<20; j++)
				System.out.println("SampleFactory list tube=" + i + " " + j + " " + tubes[i].list.get(j));

			double rmin = tubes[i].innerMin;
			double rmax = tubes[i].outerMax;
			double vol = tubes[i].list.size() * Math.PI * (rmax*rmax - rmin*rmin) * tubes[i].density;
			IJ.log("Tube " + i + " size = " + tubes[i].list.size() + " rmin=" + rmin + " rmax=" + rmax + " vol=" + vol );
			cumul[i] = (i == 0 ? vol : vol + cumul[i-1]);
			IJ.log("Init step: nb_list=" + tubes[i].list.size() + " vol " + IJ.d2s(vol * 1e-9, 4) + " um3 // innerRadius: " + tubes[i].innerMin + " outer " + tubes[i].outerMax);
			total += vol;
			/*
			for(int k=0; k<tubes[i].list.size(); k++)
				sample.fluos.add(tubes[i].list.get(k));

			for(Point3D node : tubes[i].nodes)
			for(int r=0; r<20; r++)
				sample.fluos.add(new Point3D(node.x, node.y+r, node.z));
			*/
		}
	
		
		IJ.log("CREATION nt = " + nt);
		
		do {
			double rvol = rand.nextDouble(total - 1);
			int tub = 0;
			for(tub=0; tub<nt; tub++)
				if (rvol < cumul[tub]) {
					break;
				}
	
			Tube tube = tubes[tub];
			int s = rand.nextInteger(tube.list.size() - 1);
			double radius = rand.nextDouble(tube.inner[s], tube.outer[s]);
			Point3D pa = tube.list.get(Math.max(0, s - 1));
			Point3D po = tube.list.get(s);
			Point3D pb = tube.list.get(Math.min( tube.list.size() - 1, s + 1));
			Point3D p = getPointNormalCircle(rand, pa, po, pb, radius);
			sample.fluos.add(p);
			count++;
			if (count % 1000 == 0)
				walk.progress("Fine: " + count, (100.0 * count) / nbfluos);
			if (count % 4000 == 0)
				IJ.log("" + count + " tub=" + tub + " rvol=" + rvol + " total_vol=" + total);
		}	
		while( count < nbfluos && (System.nanoTime() - chrono) * 1e-9 < timeout);
		

	}

	public int createBelongs(double factorDistanceMask, double timeout, int nbsamples) {
		int count = 0;
		walk.reset();
		tubes = sample.getTubes();
		for (Tube tube : tubes) {
			tube.init(nbsamples);
			IJ.log("INIT nbsamples " + nbsamples);
		}
		double n = mx * my * mz;
		ImageWare belongsImage = Builder.create(mx, my, mz, ImageWare.FLOAT);
		ArrayList<double[]> shifts = new ArrayList<double[]>();
		for (double i = 0; i < 1; i += .36)
			for (double j = 0; j < 1; j += .36)
				for (double k = 0; k < 1; k += .36)
					shifts.add(new double[] { i, j, k });
		double chrono = System.nanoTime();

		for (int tub = 0; tub < tubes.length; tub++) {
			for (int i = 0; i < mx; i++)
				for (int j = 0; j < my; j++)
					for (int k = 0; k < mz; k++) {
						for (double[] shift : shifts) {
							Point3D p = new Point3D((i + shift[0]) * pixelsize, (j + shift[1]) * pixelsize, (k + shift[2]) * pixelsize);
							int index = tubes[tub].inside(p, factorDistanceMask);
							if (index > 0) {
								// if (belongs[i][j][k] == null)
								// belongs[i][j][k] = new Belong();
								count++;
								belongsImage.putPixel(i, j, k, list.size());
								list.add(new int[] { i, j, k, tub, index });
								break;
							}
						}
						if (count % 100 == 0)
							walk.progress("Count: " + tub + " " + count, 100.0 * tubes.length * (i * mz * my + j * mz + k) / n);
						if ((System.nanoTime() - chrono) * 1e-9 > timeout)
							break;

					}
		}
		belongsImage.show("belongsImage " + count);
		return count;
	}

	public class ListPoint extends ArrayList<Point3D> {

	}

	public class Belong extends ArrayList<int[]> {

	}

	public void createPositions(int nbfluos, double timeout) {
		int count = 0;
		IJ.log("\nCreate Position : Mask size " + mx + " " + my + " " + mz);
		walk.reset();
		IJ.log("Fine size " + mx + " " + my + " " + mz);

		sample.fluos.clear();
		count = 0;
		int bad = 0;
		int good = 0;
		double chrono = System.nanoTime();
		double time = 0.0;
		int nitems = list.size() - 1;
		do {
			int item[] = list.get(rand.nextInteger(nitems));
			double x = rand.nextDouble(pixelsize);
			double y = rand.nextDouble(pixelsize);
			double z = rand.nextDouble(pixelsize);
			Point3D point = new Point3D(x + item[0] * pixelsize, y + item[1] * pixelsize, z + item[2] * pixelsize);
			int index1 = item[4] - 4;
			int index2 = item[4] + 4;
			int index = tubes[item[3]].inside(point, index1, index2);
			good++;
			if (index > 0) {
				sample.fluos.add(new Point3D(point.x, point.y, point.z));
				count++;
			}
			else {
				bad++;
			}
			if (count % 1000 == 0)
				walk.progress("Fine: " + count, (100.0 * count) / nbfluos);
			time = (System.nanoTime() - chrono) * 1e-9;
		}
		while (count < nbfluos && time < timeout);
		IJ.log(" Count " + count + " fluos");
		IJ.log(" Bad voxels " + bad + " fluos");
		IJ.log(" Good voxels " + good + " fluos");
	}

	public void removeSteric(double steric) {
		
		int n = sample.fluos.size();
		walk.reset();
		// Allocation in voxels
		ListPoint a[][][] = new ListPoint[mx][my][mz];
		int count = 0;
		for(Point3D fluo : sample.fluos) {
			int i = (int)(fluo.x / pixelsize);
			if (i >= 0 && i < mx) {
				int j = (int)(fluo.y / pixelsize);
				if (j >= 0 && j < my) {
					int k = (int)(fluo.z / pixelsize);
					if (k >= 0 && k < mz) {
						if (a[i][j][k] == null)
							a[i][j][k] = new ListPoint();
						a[i][j][k].add(fluo);
						count++;
					}
				}
			}
			if (count % 100 == 0)
				walk.progress("Fluo allocation " + count, 100.0 * count / n);
		}

		ArrayList<Point3D> fluos = new ArrayList<Point3D>();
		for(Point3D fluo : sample.fluos)
			fluos.add(fluo);
		sample.fluos.clear();

		int nbins = (int)Math.ceil(pixelsize/steric)+1 ;
		byte[][][] test = new byte[nbins][nbins][nbins];
		walk.reset();
		for(int i=0; i<mx; i++) {
			walk.progress("column " + i, (100.0*i)/mx);
			for(int j=0; j<my; j++)
			for(int k=0; k<mz; k++) {
				if (a[i][j][k] != null) {
					for(int x=0; x<nbins; x++)
					for(int y=0; y<nbins; y++)
					for(int z=0; z<nbins; z++) {
						test[x][y][z] = 0;
					}						
					ListPoint list = a[i][j][k];
					int na = list.size();
					int removed[] = new int[na];
					for(int u=0; u<na; u++) {
						Point3D p = list.get(u);
						int xp = (int)((p.x - i*pixelsize)/steric);
						int yp = (int)((p.y - j*pixelsize)/steric);
						int zp = (int)((p.z - k*pixelsize)/steric);
						if (test[xp][yp][zp] == 0)
							test[xp][yp][zp] = 1;
						else 
							removed[u]++;
					}					
					for(int u=0; u<na; u++) {
						if (removed[u] == 0)
							sample.fluos.add(list.get(u));
					}
				}
			}
		}
		IJ.log(" Count: " + sample.fluos.size());
	}
	
	public void crop() {
		
		int n = sample.fluos.size();
		walk.reset();

		ArrayList<Point3D> fluos = new ArrayList<Point3D>();
		for(Point3D fluo : sample.fluos)
			fluos.add(fluo);
		sample.fluos.clear();
		int count = 0;
		for(Point3D fluo : fluos) {
			boolean in = false;
			if (fluo.x > 0)
			if (fluo.y > 0)
			if (fluo.z > -sample.nz)
			if (fluo.x < sample.nx)
			if (fluo.y < sample.ny)
			if (fluo.z < sample.nz) 
				in = true;
			if (in)
				sample.fluos.add(fluo);
			else 
				IJ.log( "Erased: " + fluo.toString());
			
			count++;
			if (count % 100 == 0)
				walk.progress("Fluo allocation " + count, 100.0 * count / n);

		}
		IJ.log(" Count: " + sample.fluos.size());
	}


	public void createPositions_grid(int nbfluos, double timeout) {
		int count = 0;
		IJ.log("\nMask size " + mx + " " + my + " " + mz);
		walk.reset();
		PsRandom rand = new PsRandom(1234);
		walk.reset();
		IJ.log("Fine size " + mx + " " + my + " " + mz);

		sample.fluos.clear();
		count = 0;
		int bad = 0;
		int good = 0;
		double chrono = System.nanoTime();
		double time = 0.0;
		do {
			double x = rand.nextDouble(0, mx);
			double y = rand.nextDouble(0, my);
			double z = rand.nextDouble(0, mz);
			int i = (int) x;
			int j = (int) y;
			int k = (int) z;
			if (belongs[i][j][k] != null) {
				good++;
				for (int[] belong : belongs[i][j][k]) {
					Point3D point = new Point3D(x * pixelsize, y * pixelsize, z * pixelsize);
					int index1 = belong[1] - 40;
					int index2 = belong[1] + 40;
					int index = tubes[belong[0]].inside(point, index1, index2);
					if (index > 0) {
						sample.fluos.add(new Point3D(point.x, point.y, point.z));
						count++;
					}
				}
			}
			else {
				bad++;
			}
			if (count % 1000 == 0)
				walk.progress("Fine: " + count, (100.0 * count) / nbfluos);
			time = (System.nanoTime() - chrono) * 1e-9;
		}
		while (count < nbfluos && time < timeout);
		IJ.log(" Count " + count + " fluos");
		IJ.log(" Bad voxels " + bad + " fluos");
		IJ.log(" Good voxels " + good + " fluos");
	}

	/*
	 * public ImageWare createMask(Point3D dim, double pixelsize, int nbsamples,
	 * double factorDistanceMask) { int count = 0; int mx = (int)
	 * Math.ceil(dim.x / pixelsize); int my = (int) Math.ceil(dim.y /
	 * pixelsize); int mz = (int) Math.ceil(dim.z / pixelsize);
	 * IJ.log("\nMask size " + mx + " " + my + " " + mz); ImageWare image =
	 * Builder.create(mx, my, mz, ImageWare.BYTE); ImageWare distance =
	 * Builder.create(mx, my, mz, ImageWare.FLOAT); walk.reset(); double n = mx
	 * * my * mz; for(Item item : sample) { if (item instanceof Tube) { Tube
	 * tube = (Tube)item; tube.init(nbsamples); } }
	 * 
	 * Tube[] tubes = sample.getTubes();
	 * 
	 * ArrayList<double[]> voxels1 = new ArrayList<double[]>(); for (int i = 0;
	 * i < mx; i++) for (int j = 0; j < my; j++) for (int k = 0; k < mz; k++) {
	 * Point3D p = new Point3D(i * pixelsize, j * pixelsize, k * pixelsize);
	 * double min = Double.MAX_VALUE; for(int tub=0; tub<tubes.length; tub++) {
	 * double[] inside = tubes[tub].inside(p, factorDistanceMask); if (inside[0]
	 * < min) { min = inside[0]; } if (inside[2] > 0) { image.putPixel(i, j, k,
	 * 255); voxels1.add(new double[] {i, j, k, tub, inside[1]}); count++; } if
	 * (count % 100 == 0) walk.progress("Count: " + count, 100.0 * (i * mz * my
	 * + j * mz + k) / n); } distance.putPixel(i, j, k, min); }
	 * distance.show("Distance"); IJ.log("Number of voxels Step 1: " +
	 * voxels1.size());
	 * 
	 * int n1 = voxels1.size(); walk.progress("2nd step" + voxels1.size(), 0);
	 * int vcount2 = 0; ArrayList<double[]> voxels2 = new ArrayList<double[]>();
	 * double f2 = factorDistanceMask*0.1; for(int v=0; v<n1; v++) { double[]
	 * voxel = voxels1.get(v); int tub = (int)voxel[3]; Tube tube = tubes[tub];
	 * if (v % 10 == 0) walk.progress("2nd Step " + vcount2, (100.0*v) / n1);
	 * int k1 = (int)voxel[4] - 2; int k2 = (int)voxel[4] + 2; for (double i =
	 * voxel[0]-1; i <= voxel[0]+1; i+=0.1) for (double j = voxel[1]-1; j <=
	 * voxel[1]+1; j+=0.1) for (double k = voxel[2]-1; k <= voxel[2]+1; k+=0.1)
	 * { Point3D p = new Point3D(i * pixelsize, j * pixelsize, k * pixelsize);
	 * double[] inside = tube.inside(p, f2, k1, k2); if (inside[2] > 0) {
	 * voxels2.add(new double[] {i, j, k, tub, inside[1]}); vcount2++; } } } int
	 * n2 = voxels2.size(); IJ.log("Number of voxels end of step2 : " + n2);
	 * 
	 * sample.voxels.clear(); for(double[] voxel : voxels2) {
	 * sample.voxels.add(new Point3D(voxel[0]*pixelsize, voxel[1]*pixelsize,
	 * voxel[2]*pixelsize)); sample.fluos.add(new Point3D(voxel[0]*pixelsize,
	 * voxel[1]*pixelsize, voxel[2]*pixelsize)); } return image; }
	 */
	public void createPositions1(int nbfluos, double timeout) {
		double chrono = System.nanoTime();
		Tube[] tubes = sample.getTubes();
		sample.voxels.clear();
		sample.fluos.clear();
		for (Tube tube : tubes) {
			ArrayList<Point3D> ps = tube.getSamplesInterval(1, 0);
			for (Point3D p : ps) {
				sample.voxels.add(p);
				sample.fluos.add(p);
			}
		}
		IJ.log("Count: " + sample.voxels.size() + " fluos in time:" + ((System.nanoTime() - chrono) * 1e-9) + " s");
	}

	public void createPositions1(int nbfluos, double timeout, double pixelsize, int nbsamples) {

		ArrayList<Point3D> mask = sample.voxels;

		Random rand = new Random(1234);
		int count = 0;
		int nmask = mask.size() - 1;
		sample.fluos.clear();
		for (Item item : sample) {
			if (item instanceof Tube) {
				Tube tube = (Tube) item;
				tube.init(nbsamples);
			}
		}

		int ntubes = 0;
		for (Item item : sample)
			if (item instanceof Tube)
				ntubes++;
		Tube tubes[] = new Tube[ntubes];
		ntubes = 0;
		for (Item item : sample)
			tubes[ntubes++] = (Tube) item;

		double chrono = System.nanoTime();
		double time = 0.0;
		do {
			int index = (int) (rand.nextDouble() * nmask);
			int inx = (int) mask.get(index).x;
			int iny = (int) mask.get(index).y;
			int inz = (int) mask.get(index).z;
			double x = inx;// + (rand.nextDouble()-0.5)*pixelsize;
			double y = iny;// + (rand.nextDouble()-0.5)*pixelsize;
			double z = inz;// + (rand.nextDouble()-0.5)*pixelsize;
			Point3D p = new Point3D(x, y, z);
			for (Tube tube : tubes) {
				if (tube.contains(p)) {
					sample.fluos.add(p);
					count++;
				}
			}
			if (count % 100 == 0)
				walk.progress("Count: " + count, 100.0);

			time = (System.nanoTime() - chrono) * 1e-9;
		}
		while (count < nbfluos && time < timeout);
		IJ.log("Count: " + count + " fluos in time:" + ((System.nanoTime() - chrono) * 1e-9) + " s");
	}

	/**
	 * http://www.groupsrv.com/computers/about216280.html N Normal, (U,V) Plane
	 * of the circle // X(t) = C + r*(cos(t)*U + sin(t)*V) for 0 <= t < 2*pi. If
	 * N = (nx,ny,nz), U = (ux,uy,uz), and V = (vx,vy,vz), then
	 */
	private Point3D getPointNormalCircle(PsRandom psrand, Point3D pa, Point3D po, Point3D pb, double radiusPix) {

		double dab = pa.distance(pb);
		// Normal
		double nx = (pa.x - pb.x) / dab;
		double ny = (pa.y - pb.y) / dab;
		double nz = (pa.z - pb.z) / dab;

		nx += (rand.nextDouble() -0.5) * 0.1;
		ny += (rand.nextDouble() -0.5) * 0.1;
		nz += (rand.nextDouble() -0.5) * 0.1;
		// Plane circle
		double ux, uy, uz;
		double vx, vy, vz;

		if (abs(nx) >= abs(ny)) {
			double invLength = 1.0 / sqrt(nx * nx + ny * ny);
			ux = -nz * invLength;
			uy = 0;
			uz = +nx * invLength;
			vx = ny * uz;
			vy = nz * ux - nx * uz;
			vz = -ny * ux;
		}
		else {
			double invLength = 1.0 / sqrt(ny * ny + nz * nz);
			ux = 0;
			uy = +nz * invLength;
			uz = -ny * invLength;
			vx = ny * uz - nz * uy;
			vy = -nx * uz;
			vz = nx * uy;
		}

		double r = radiusPix;
		double a = psrand.nextDouble() * 2.0 * PI;
			
		double normu = Math.sqrt(ux * ux + uy * uy + uz * uz);
		double normv = Math.sqrt(vx * vx + vy * vy + vz * vz);

		ux /= normu;
		uz /= normu;
		ux /= normu;

		vx /= normv;
		vz /= normv;
		vx /= normv;
		
		double cosa = Math.cos(a);
		double sina = Math.sin(a);
		double x = po.x + r * (cosa * ux + sina * vx);
		double y = po.y + r * (cosa * uy + sina * vy);
		double z = po.z + r * (cosa * uz + sina * vz);

		Point3D out = new Point3D(x, y, z);
		return out;
	}
}
