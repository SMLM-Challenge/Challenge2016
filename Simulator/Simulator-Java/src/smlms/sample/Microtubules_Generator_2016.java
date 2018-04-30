package smlms.sample;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.StackWindow;
import imageware.Builder;
import imageware.ImageWare;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import smlms.tools.NormalizedVariable;
import smlms.tools.Point3D;
import smlms.tools.PsRandom;

public class Microtubules_Generator_2016 {

	private PsRandom	rand	                 = new PsRandom(123);
	/******
	 * MT0-1 private int margin = 100; private int nx = 320 + margin + margin;
	 * // 320 * 40 = 12800nm private int ny = 320 + margin + margin; // 320 * 40
	 * = 12800nm private int nz = 25; // 25 * 40 = 1000nm private double
	 * pixelsize = 40; private double lateralRandomExtremity = 10; private
	 * double jitterExtremityAngleRadian = 0.02; private int nbExtremity = 12;
	 * private int rangeExtremityOpposition = 2; private double lambdaBackground
	 * = 1; private double lambdaDirection = 1; private double lambdaRandom =
	 * 0.1; private double elevationRange = Math.toRadians(5); private double
	 * azimuthRange = Math.toRadians(10); private double elementLength = 6;
	 * private int nbTubes = 100; private double radiusTube = 12; private double
	 * thickTube = 6;
	 ***/

	private int	      margin	                 = 100;
	private int	      nx	                     = 320 + margin + margin;
	private int	      ny	                     = 320 + margin + margin;
	private int	      nz	                     = 75;	                 
	private double	  pixelsize	                 = 20;
	private double	  lateralRandomExtremity	 = 10;
	private double	  jitterExtremityAngleRadian	= 0.02;
	private int	      nbExtremity	             = 12;
	private int	      rangeExtremityOpposition	 = 2;
	private double	  lambdaBackground	         = 1;
	private double	  lambdaDirection	         = 1;
	private double	  lambdaRandom	             = 0.4;
	private double	  elevationRange	         = Math.toRadians(4);
	private double	  azimuthRange	             = Math.toRadians(10);
	private double	  elementLength	             = 6;
	private int	      nbTubes	                 = 100;
	private double	  radiusTube	             = 12;
	private double	  thickTube	                 = 6;
	private double	  density	                 = 1.0;

	private ImageWare	collision;
	private ImageWare	background;

	private String	  outfile	                 = "/Users/sage/Desktop/SampleMT2.txt";

	public static void main(String args[]) {
		new Microtubules_Generator_2016();
	}

	public Microtubules_Generator_2016() {

		collision = Builder.create(nx, ny, nz, ImageWare.BYTE);

		background = Builder.create(nx, ny, nz, ImageWare.FLOAT);
		for (int i = 0; i < nx; i++)
			for (int j = 0; j < ny; j++)
				for (int k = 0; k < nz; k++) {
					background.putPixel(i, j, k, rand.nextDouble(-20, 20));
				}

		for (int i = 0; i < 15; i++) {
			int sign = i % 2 == 0 ? -1 : 1;
			int x = rand.nextInteger(10, nx - 10);
			int y = rand.nextInteger(10, nx - 10);
			int z = rand.nextInteger(1, nz - 1);
			for (int j = 0; j < 1000000; j++) {
				x += rand.nextInteger(-2, 2);
				y += rand.nextInteger(-1, 1);
				z += rand.nextInteger(-1, 1) * (rand.nextDouble(1) > 0.9 ? 1 : 0);
				background.putPixel(x, y, z, sign * 20);
			}
		}
		background.smoothGaussian(6, 6, 1);
		ImagePlus imp = new ImagePlus("background", background.buildImageStack());
		imp.show();

		Point3D extremities[] = createExtremity();

		ArrayList<ArrayList<Point3D>> lists = new ArrayList<ArrayList<Point3D>>();

		for (int i = 0; i < nbTubes; i++) {
			ArrayList<Point3D> list = build(i, createTubeExtremity(extremities));
			if (list != null) {
				boolean flag = true;
				for (Point3D p : list)
					if (p.z <= 0) {
						IJ.log("" + i + " > " + p.z);
						flag = false;
						break;
					}
				for (Point3D p : list)
					if (p.z <= 0) {
						IJ.log("" + i + " > " + p.z);
						flag = false;
						break;
					}

				if (list.size() < nx / 10) {
					IJ.log("" + i + " > n = " + list.size());
					flag = false;
				}

				if (flag) {
					draw(list);
					lists.add(list);
				}
			}
		}
		IJ.log("Number of tubes: " + lists.size());
		new MTCanvas(imp, lists, nz, extremities);

		Sample sample = new Sample("name", (int) ((nx - 2 * margin) * pixelsize), (int) ((ny - 2 * margin) * pixelsize), (int) ((nz) * pixelsize));
		for (int i = 0; i < lists.size(); i++) {
			NormalizedVariable radius = new NormalizedVariable(radiusTube);
			NormalizedVariable thickness = new NormalizedVariable(thickTube);
			ArrayList<Point3D> nodes = new ArrayList<Point3D>();
			ArrayList<Point3D> list = lists.get(i);
			for (int k = 0; k < list.size(); k++) {
				Point3D p = list.get(k);
				nodes.add(new Point3D((p.x - margin) * pixelsize, (p.y - margin) * pixelsize, p.z * pixelsize));
			}
			Tube tube = new Tube("tube " + i, nodes, radius, thickness, 1);
			sample.add(tube);
		}
		collision.show();
		sample.save(outfile);
	}

	private ArrayList<Point3D> build(int tube, Point3D extremity[]) {

		int n = nx;
		double dx = (extremity[1].x - extremity[0].x) / n;
		double dy = (extremity[1].y - extremity[0].y) / n;
		double dz = (extremity[1].z - extremity[0].z) / n;
		double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
		dx = dx / d;
		dy = dy / d;
		dz = dz / d;
		double x = extremity[0].x;
		double y = extremity[0].y;
		double z = extremity[0].z;
		double azimuth = Math.atan2(dy, dx);
		double elevation = Math.acos(dz / d);
		ArrayList<Point3D> list = new ArrayList<Point3D>();
		for (int p = 0; p < n; p++) {
			double max = -Double.MAX_VALUE;
			double amax = 0;
			double imax = 0;
			double er = (rand.nextDouble(1) > 0.1 ? elevationRange : elevationRange * 6);
			for (double i = elevation - er; i <= elevation + er; i += er * 0.5) {
				double sini = Math.sin(i);
				double oz = Math.cos(i);
				for (double a = azimuth - azimuthRange; a <= azimuth + azimuthRange; a += azimuthRange * 0.5) {
					double ox = Math.cos(a) * sini;
					double oy = Math.sin(a) * sini;
					double xn = x + elementLength * ox;
					double yn = y + elementLength * oy;
					double zn = z + elementLength * oz;
					double v = 0.0;
					v += lambdaBackground * background.getInterpolatedPixel(xn, yn, zn);
					v += lambdaDirection * (dx * ox + dy * oy + dz * oz);
					v += lambdaRandom * rand.nextGaussian(0, 2);
					if (v > max) {
						max = v;
						amax = a;
						imax = i;
					}
				}
			}
			azimuth = amax;
			x = x + elementLength * Math.cos(amax) * Math.sin(imax);
			y = y + elementLength * Math.sin(amax) * Math.sin(imax);
			z = z + elementLength * Math.cos(imax);
			if (collision.getInterpolatedPixel(x, y, z) > 0.5) {
				return null;
			}
			list.add(new Point3D(x, y, z));
		}

		double x1 = margin * 0.5;
		double y1 = margin * 0.5;
		double x2 = nx - margin * 0.5;
		double y2 = ny - margin * 0.5;
		ArrayList<Point3D> inside = new ArrayList<Point3D>();

		for (int i = 0; i < list.size(); i++) {
			Point3D p = list.get(i);
			if (p.x > x1 && p.x < x2)
				if (p.y > y1 && p.y < y2) {
					inside.add(p);
				}
		}

		return inside;
	}

	private void draw(ArrayList<Point3D> list) {
		for (int p = 1; p < list.size(); p++) {
			int xp = (int) list.get(p).x;
			int yp = (int) list.get(p).y;
			int zp = (int) list.get(p).z;
			int t = 3;
			for (int k = 0; k < elementLength; k++) {
				double dx = list.get(p).x - list.get(p - 1).x;
				double dy = list.get(p).y - list.get(p - 1).y;
				double dz = list.get(p).z - list.get(p - 1).z;
				int xo = (int) Math.round(list.get(p - 1).x + k * dx / elementLength);
				int yo = (int) Math.round(list.get(p - 1).y + k * dy / elementLength);
				int zo = (int) Math.round(list.get(p - 1).z + k * dz / elementLength);
				int nc = 5;
				for(int x1=-nc; x1<=nc; x1++) 
					for(int y1=-nc; y1<=nc; y1++)
						for(int z1=-nc; z1<=nc; z1++) {
							collision.putPixel(xo+x1, yo+y1, zo+z1, 1);
				}
			}

			for (int i = -t; i <= t; i++)
				for (int j = -t; j <= t; j++)
					for (int k = -t; k <= t; k++)
						background.putPixel(xp + i, yp + j, zp + k, background.getPixel(xp + i, yp + j, zp + k) - 12);

		}
	}

	private Point3D[] createTubeExtremity(Point3D[] extremities) {
		int next = extremities.length;
		int ext1 = (int) Math.abs(Math.round(rand.nextDouble(0, next - 1)));
		ext1 = ext1 % next;
		int ext2 = ext1 + next / 2 + (int) Math.abs(Math.round(rand.nextDouble(-rangeExtremityOpposition, rangeExtremityOpposition)));
		ext2 = ext2 % next;
		Point3D e1 = new Point3D(extremities[ext1].x, extremities[ext1].y, extremities[ext1].z);
		Point3D e2 = new Point3D(extremities[ext2].x, extremities[ext2].y, extremities[ext2].z);
		e1.x += rand.nextDouble(-lateralRandomExtremity, lateralRandomExtremity);
		e1.y += rand.nextDouble(-lateralRandomExtremity, lateralRandomExtremity);
		e1.z += rand.nextDouble(-lateralRandomExtremity, lateralRandomExtremity);
		e2.x += rand.nextDouble(-lateralRandomExtremity, lateralRandomExtremity);
		e2.y += rand.nextDouble(-lateralRandomExtremity, lateralRandomExtremity);
		e2.z += rand.nextDouble(-lateralRandomExtremity, lateralRandomExtremity);
		return new Point3D[] { e1, e2 };
	}

	private Point3D[] createExtremity() {
		int xc = nx / 2;
		int yc = ny / 2;
		Point3D extremities[] = new Point3D[nbExtremity];
		for (int a = 0; a < nbExtremity; a++) {
			double r = a * 2 * Math.PI / nbExtremity + rand.nextDouble(-jitterExtremityAngleRadian, jitterExtremityAngleRadian);
			double xp = xc + nx * Math.cos(r) * 0.49;
			double yp = yc + ny * Math.sin(r) * 0.49;
			double zp = nz * 0.5 + nz * rand.nextDouble(-0.4, 0.4);
			extremities[a] = new Point3D(xp, yp, zp);
			IJ.log("Extremity " + extremities[a].x + " " + extremities[a].y + " " + extremities[a].z);
		}
		return extremities;
	}

	public class MTCanvas extends ImageCanvas {
		private ArrayList<ArrayList<Point3D>>	tubes;
		private int		                      nz;
		private Point3D		                  extremities[];

		public MTCanvas(ImagePlus imp, ArrayList<ArrayList<Point3D>> tubes, int nz, Point3D extremities[]) {
			super(imp);
			this.tubes = tubes;
			this.extremities = extremities;
			this.nz = nz;
			if (imp.getStackSize() > 1)
				imp.setWindow(new StackWindow(imp, this));
			else
				imp.setWindow(new ImageWindow(imp, this));
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			for (Point3D extremity : extremities) {
				g.setColor(Color.WHITE);
				g.fillOval(screenXD(extremity.x) - 3, screenYD(extremity.y) - 3, 7, 7);

			}

			for (ArrayList<Point3D> tube : tubes) {
				Color c = Color.getHSBColor((float) (tube.get(0).z / nz), 1, 1);
				g.setColor(c);
				g.fillOval(screenXD(tube.get(0).x) - 3, screenYD(tube.get(0).y) - 3, 7, 7);

				for (int p = 1; p < tube.size(); p++) {
					Point3D prev = tube.get(p - 1);
					Point3D curr = tube.get(p);
					g.drawLine(screenXD(prev.x), screenYD(prev.y), screenXD(curr.x), screenYD(curr.y));
				}
			}
		}
	}
}
