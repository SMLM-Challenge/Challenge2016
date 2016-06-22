package smlms.sample;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.StackWindow;
import ij.process.FloatProcessor;
import imageware.ImageWare;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import smlms.tools.NormalizedVariable;
import smlms.tools.Point3D;
import smlms.tools.PsRandom;

public class Microtubules_Generator_Training {

	private PsRandom	rand	                 = new PsRandom(123);

	private int	      margin	                 = 0;
	private int	      nx	                     = 320 + margin + margin;
	private int	      ny	                     = 320 + margin + margin;
	private int	      nz	                     = 75;	                 
	private double	  pixelsize	                 = 10;
	private double	  lateralRandomExtremity	 = 10;
	private double	  jitterExtremityAngleRadian	= 0.02;
	private int	      nbExtremity	             = 12;
	private int	      rangeExtremityOpposition	 = 2;
	private double	  elementLength	             = 6;
	private double	  radiusTube	             = 12;
	private double	  thickTube	                 = 6;

	private ImageWare	collision;
	private ImageWare	background;

	private String	  outfile	                 = "/Users/sage/Desktop/SampleMT-Training.txt";

	public static void main(String args[]) {
		new Microtubules_Generator_Training();
	}

	public Microtubules_Generator_Training() {
		System.out.println("Microtubule Training Reticulum");
		
		ImagePlus imp = new ImagePlus("background", new FloatProcessor(640, 640));
		imp.show();

		Point3D extremities[] = createExtremity();
		
		ArrayList<ArrayList<Point3D>> lists = build();
	
		IJ.log("Number of tubes: " + lists.size());
		new MTCanvas(imp, lists, nz,  extremities);
		
		Sample sample = new Sample("name", 6400, 6400, 1500);
		for(int i=0; i<lists.size(); i++) {
			NormalizedVariable radius = new NormalizedVariable(radiusTube);
			NormalizedVariable thickness = new NormalizedVariable(thickTube);
			ArrayList<Point3D> nodes = new ArrayList<Point3D>();
			ArrayList<Point3D> list = lists.get(i);
			for(int k=0; k<list.size(); k++) {
				Point3D p = list.get(k);
				nodes.add(new Point3D((p.x-margin)*pixelsize, (p.y-margin)*pixelsize, p.z*pixelsize));
			}
			Tube tube = new Tube("tube " + i, nodes, radius, thickness, 1);
			IJ.log(" list: " + i + " " + list.size() + " first " + list.get(0));
			IJ.log(" Radius 0.0: " + radius.get(0.0));
			IJ.log(" Radius 0.5: " + radius.get(0.5));
			IJ.log(" Radius 1.0: " + radius.get(1.0));
			sample.add(tube);
		}
		sample.save(outfile);	}

	private ArrayList<ArrayList<Point3D>> build() {

		ArrayList<Point3D> my1 = new ArrayList<Point3D>();
		my1.add(new Point3D(	-20	,	266.5	,	10.0 + 75	));
		my1.add(new Point3D(	74	,	268.3	,	11.7 + 75	));
		my1.add(new Point3D(	124	,	262.5	,	12.8 + 75	));
		my1.add(new Point3D(	184	,	240.4	,	13.9 + 75	));
		my1.add(new Point3D(	228	,	256.5	,	14.0 + 75	));
		my1.add(new Point3D(	284	,	267.5	,	15.1 + 75));
		my1.add(new Point3D(	324	,	298.5	,	16.2 + 75	));
		my1.add(new Point3D(	380	,	302.6	,	16.3 + 75	));
		my1.add(new Point3D(	446	,	313.5	,	16.4 + 75	));
		my1.add(new Point3D(	506	,	327.7	,	16.5 + 75	));
		my1.add(new Point3D(	561	,	340.5	,	16.0 + 75	));
		my1.add(new Point3D(	619	,	325.5	,	15.7 + 75	));
		my1.add(new Point3D(	650	,	305.5	,	15.8 + 75	));

		CurveSpline c1 = new CurveSpline(my1, 1);
		ArrayList<Point3D> mys1 = c1.getSamplesInterval(5);

		ArrayList<Point3D> my2 = new ArrayList<Point3D>(); // green
		my2.add(new Point3D(	-20	,	401.5	,	-70.6 + 75	));
		my2.add(new Point3D(	73	,	409.5	,	-65.7 + 75	));
		my2.add(new Point3D(	97	,	403.5	,	-60.8 + 75	));
		my2.add(new Point3D(	120	,	383.5	,	-52.9 + 75	));
		my2.add(new Point3D(	138	,	363.5	,	-42.0 + 75	));
		my2.add(new Point3D(	201	,	327.5	,	-38.1 + 75	));
		my2.add(new Point3D(	270	,	292.5	,	-28.2 + 75	));
		my2.add(new Point3D(	381	,	308.5	,	-23.3 + 75	));
		my2.add(new Point3D(	405	,	315.5	,	-21.4 + 75	));
		my2.add(new Point3D(	495	,	325.5	,	-21.5 + 75	));
		my2.add(new Point3D(	609	,	335.5	,	-22.5 + 75	));
		my2.add(new Point3D(	641	,	345.5	,	-23.4 + 75	));
		CurveSpline c2 = new CurveSpline(my2, 1);
		ArrayList<Point3D> mys2 = c2.getSamplesInterval(5);
								
		ArrayList<Point3D> my3 = new ArrayList<Point3D>();	// top, horiz
		my3.add(new Point3D(	89	,	-20	,	20.0 + 75	));
		my3.add(new Point3D(	80	,	105.5	,	21.2 + 75	));
		my3.add(new Point3D(	102	,	143.5	,	24.0 + 75	));
		my3.add(new Point3D(	142	,	216.5	,	24.0 + 75	));
		my3.add(new Point3D(	205	,	236.5	,	26.2 + 75	));
		my3.add(new Point3D(	239	,	269.5	,	28.0 + 75	));
	
		my3.add(new Point3D(	200	,	330.5	,	29.0 + 75	));

		my3.add(new Point3D(	207	,	385.5	,	32.1 + 75	));//
		my3.add(new Point3D(	244	,	405.5	,	52.6 + 75	));
		my3.add(new Point3D(	260	,	513.5	,	61.0 + 75	));
		my3.add(new Point3D(	305	,	609.5	,	63.5 + 75	));
		my3.add(new Point3D(	385	,	669.5	,	70.4 + 75	));
		CurveSpline c3 = new CurveSpline(my3, 1);
		ArrayList<Point3D> mys3 = c3.getSamplesInterval(5);
								
		
		ArrayList<ArrayList<Point3D>> lists = new ArrayList<ArrayList<Point3D>>();
		lists.add(mys1);
		lists.add(mys2);
		lists.add(mys3);
		
		for ( ArrayList<Point3D> list : lists)
		for (Point3D p : list) {
			double t = p.x; p.x = p.y; p.y = t; 
		}
	/*	
		IJ.log(" \n");
		int er = 0;
		NormalizedVariable[] z = new NormalizedVariable[3];
		double n[] = new double[3];
		// 1
		n[er] = lists.get(er).size();
		z[er] = new NormalizedVariable(68);
		z[er].addLinear(00, -32);
		z[er].addCosine(3, 2.5, 0.3);
		z[er].addUniformRandom(0, 3);
		for (int k=0; k<n[er]; k++) { // 1
			lists.get(er).get(k).z = z[er].get(k/(n[er]-1));
			IJ.log(" ER" + (er+1) + " " + lists.get(er).get(k).z);
		}

		IJ.log(" \n");
		er++;
		// 2
		n[er] = lists.get(er).size();
		z[er] = new NormalizedVariable(0);
		z[er].addLinear(170, -50);
		z[er].addCosine(5, 0.3, 1.57);
		z[er].addUniformRandom(0, 3);
		for (int k=0; k<n[er]; k++) { // 1
			lists.get(er).get(k).z = z[er].get(k/(n[er]-1));
			IJ.log(" ER" + (er+1) + " " + lists.get(er).get(k).z);
		}

		IJ.log(" \n");
		er++;
		// 3
		n[er] = lists.get(er).size();
		z[er] = new NormalizedVariable(18);
		z[er].addCosine(19, 1.1, 1.57);
		z[er].addUniformRandom(0, 3);
		for (int k=0; k<n[er]; k++) { // 1
			lists.get(er).get(k).z = z[er].get(k/(n[er]-1));
			IJ.log(" ER" + (er+1) + " " + lists.get(er).get(k).z);
		}
		
		*/
		return lists;
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
