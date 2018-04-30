package smlms.sample;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.StackWindow;
import ij.io.FileSaver;
import imageware.Builder;
import imageware.ImageWare;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import smlms.tools.NormalizedVariable;
import smlms.tools.Point3D;
import smlms.tools.PsRandom;

public class Reticulum2T_Generator_2016 {

	private PsRandom	rand	                 = new PsRandom(13234);

	private int	      nx	                     = 640;
	private int	      ny	                     = 640;
	private int	      nz	                     = 150;	                 
	private double	  pixelsize	                 = 10;
	
	private double	  radiusTube	             = 150;
	private double	  thickTube	                 = 6;

	private ImageWare	substrat;
	ArrayList<Point3D> anchors = new ArrayList<Point3D>();


	public static void main(String args[]) {
		Reticulum2T_Generator_2016 mt = new Reticulum2T_Generator_2016();
		mt.run(1807, -10);
		mt.run(2709, +10);
	}

	public Reticulum2T_Generator_2016() {
	}
	
	public void run(long random, double rotation) {
		
		rand = new PsRandom(random);
		String outfile = "/Users/sage/Desktop/SampleMT-" + random + ".txt";
		
		substrat = createSubstrat();
		ImagePlus imp = new ImagePlus("Substrat-"+ random + "-" + rotation, substrat.buildImageStack());
		imp.show();
		
		ArrayList<ArrayList<Point3D>> lists = build(rotation);
		IJ.log("Number of tubes: " + lists.size());
		new MTCanvas(imp, lists, anchors);

		Sample sample = new Sample("name", (int)(nx * pixelsize), (int) (ny * pixelsize), (int) (nz * pixelsize));
		for (int i = 0; i < lists.size(); i++) {
			NormalizedVariable radius = new NormalizedVariable(radiusTube);
			NormalizedVariable thickness = new NormalizedVariable(thickTube);
			
				
			ArrayList<Point3D> nodes = new ArrayList<Point3D>();
			ArrayList<Point3D> list = lists.get(i);
			for (int k = 0; k < list.size(); k++) {
				Point3D p = list.get(k);
				nodes.add(new Point3D(p.x * pixelsize, p.y * pixelsize, p.z * pixelsize + 750));
			}
			Tube tube = new Tube("tube " + i, nodes, radius, thickness, 1);
			sample.add(tube);
		}
		sample.save(outfile);
	}

	private ArrayList<ArrayList<Point3D>> build(double rotation) {

		ArrayList<Point3D> my1 = new ArrayList<Point3D>();
		Point3D a1 = new Point3D(r(400, 420), r(-30, -40), r(-24, -27));
		Point3D b1 = new Point3D(r(200, 240), r(320, 340), r(-13, -18));
		Point3D c1 = new Point3D(r(180, 200), r(450, 460), r(13, 18));
		Point3D d1 = new Point3D(r(50, 80), r(700, 720), r(22, 25));
		
		my1.add(a1);
		my1.add(b1);
		my1.add(c1);
		my1.add(d1);
		
		ArrayList<Point3D> my3 = new ArrayList<Point3D>();
		Point3D a3 = new Point3D(d1.x+r(160, 170), r(690, 700), r(-15, -17));
		Point3D b3 = new Point3D(c1.x-r(80, 100), r(350, 360), r(-7, -8));
		Point3D c3 = new Point3D(b1.x+r(100,110), r(150, 170), r(9, 11));
		Point3D d3 = new Point3D(r(680, 700), r(90, 120), r(15, 18));

		my3.add(a3);
		my3.add(b3);
		my3.add(c3);
		my3.add(d3);

		ArrayList<Point3D> myss1 = smooth(perturbe(attract(my1), r(0.15, 0.25)));
		ArrayList<Point3D> py1 = perturbe(attract(my3), r(0.15, 0.25));
		ArrayList<Point3D> py2 = perturbe(py1, r(0.45, 0.55));
		ArrayList<Point3D> py3 = perturbe(py2, r(0.70, 0.72));
		
		ArrayList<Point3D> myss3 = smooth(perturbe(py3, r(0.75, 0.75)));
		
		rotate(myss1, rotation);
		rotate(myss3, rotation);
		ArrayList<ArrayList<Point3D>> lists = new ArrayList<ArrayList<Point3D>>();
		lists.add(myss1);
		lists.add(myss3);
	
		
		return lists;
	}
	
	private void rotate(ArrayList<Point3D> points, double angle) {
		double cosa = Math.cos(Math.toRadians(angle));
		double sina = Math.sin(Math.toRadians(angle));
		for(Point3D point : points) {
			double x = point.x - nx/2;
			double y = point.y - ny/2;
			point.x = cosa * x + sina * y + nx/2;
			point.y = -sina * x + cosa * y + ny/2;
		}
	}
	
	private ArrayList<Point3D> perturbe(ArrayList<Point3D> in, double pos) {
		int k = (int)(pos*in.size());
		double dx = in.get(k-1).x -  in.get(k+1).x;
		double dy = in.get(k-1).y -  in.get(k+1).y;
		
		double d = Math.sqrt(dy*dy+dx*dx);
		double r = rand.nextDouble(-1, 1) < 0 ? r(-5, -6) : r(3 , 5);
		in.get(k).x += -r*dy/d;
		in.get(k).y += +r*dx/d;
			
		return in;
	}
	
	private ArrayList<Point3D> smooth(ArrayList<Point3D> in) {
		CurveSpline spline1 = new CurveSpline(in, 1);
		return spline1.getSamplesInterval(1);
	}
	
	private ArrayList<Point3D> attract(ArrayList<Point3D> in) {
		CurveSpline spline1 = new CurveSpline(in, 1);
		ArrayList<Point3D> s = spline1.getSamplesInterval(50);
	
		ArrayList<Point3D> out = new ArrayList<Point3D>();
		out.add(s.get(0));
		for(int k=1; k<s.size()-1; k++) {
			int x = (int)s.get(k).x;
			int y = (int)s.get(k).y;
			
			double dx = s.get(k-1).x -  s.get(k+1).x;
			double dy = s.get(k-1).y -  s.get(k+1).y;
			double min = Double.MAX_VALUE;
			int dirx = dx <= dy ? 0 : 1;
			int diry = dx >= dy ? 1 : 0;
			
			int imin = 0;
			for(int i=-50; i<50; i++) {
				double cost = Math.abs(i) + substrat.getPixel(x + dirx*i, y + diry*i, 0);
				if (cost < min) {
					imin = i;
					min = cost;
				}
				
			}
			out.add(new Point3D(s.get(k).x + dirx*imin, s.get(k).y + diry*imin, s.get(k).z));
			
		}
		out.add(s.get(s.size()-1));
		return out;
	}
	
	/*
	private ArrayList<Point3D> build(Point3D a, Point3D b) {

		int n = nx;
		double dx = (b.x - a.x) / n;
		double dy = (b.y - a.y) / n;
		double d = Math.sqrt(dx * dx + dy * dy);
		dx = dx / d;
		dy = dy / d;
		double x = a.x;
		double y = a.y;
		double azimuth = Math.atan2(dy, dx);
		ArrayList<Point3D> list = new ArrayList<Point3D>();
		boolean flag = true;
		double diprev = Double.MAX_VALUE;
		for (int p = 0; p < n & flag; p++) {
			double max = -Double.MAX_VALUE;
			double amax = 0;
			for (double an = azimuth - azimuthRange; an <= azimuth + azimuthRange; an += azimuthRange * 0.5) {
				double ox = Math.cos(an);
				double oy = Math.sin(an);
				double xn = x + elementLength * ox;
				double yn = y + elementLength * oy;
				
				double v = 0.0;
				double goal = 1000  -b.distanceLateral(new Point3D(xn, yn, 0));
				v += lambdaBackground * substrat.getInterpolatedPixel(xn, yn, 0);
				//v += lambdaDirection * (dx * ox + dy * oy );
				v += lambdaRandom * rand.nextGaussian(0, 2);
				v += lambdaDirection*goal;
				if (v > max) {
					max = v;
					amax = an;
				}
			}
			azimuth = amax;
			x = x + elementLength * Math.cos(amax);
			y = y + elementLength * Math.sin(amax);
			Point3D pc = new Point3D(x, y, 0);
			double di = b.distanceLateral(pc);
			System.out.println("Distanace to b " + di);
			if (di < 10) {
				list.add(b);
				break;
			}
			if (diprev < di) {
				list.add(b);
				break;
			}
			list.add(pc);
			diprev = di;
		}

		ArrayList<Point3D> flat = new ArrayList<Point3D>();
		
		for (int i = 0; i < list.size(); i++) {
			Point3D p = list.get(i);
				if (p.x > 0 && p.x < nx)
					if (p.y > 0 && p.y < ny) {
						flat.add(p);
					}
		}
	
		double dz = (b.z - a.z) / flat.size();
		for (int i = 0; i < flat.size(); i++)
			list.get(i).z = a.z + i*dz;

		ArrayList<Point3D> inside = new ArrayList<Point3D>();
			
		for (int i = 0; i < flat.size(); i++) {
			Point3D p = flat.get(i);
			if (p.x > -nz/2 && p.z < nz/2)
				inside.add(p);
		}
		System.out.println("nbpoints " + list.size() + " > " + " > " + flat.size() + " > " + inside.size());
		
		return inside;
	}
	*/
	private double r(double bottom, double top) {
		if (bottom < top)
			return rand.nextDouble(bottom, top);
		else
			return rand.nextDouble(top, bottom);
	}
	
	public ImageWare createSubstrat() {
		ImageWare substrat = Builder.create(nx, ny, 1, ImageWare.FLOAT);
		for (int i = 0; i < nx; i++)
			for (int j = 0; j < ny; j++)
				{
					substrat.putPixel(i, j, 0, rand.nextDouble(-20, 20));
				}

		for (int i = 0; i < 15; i++) {
			int sign = i % 2 == 0 ? -1 : 1;
			int x = rand.nextInteger(10, nx - 10);
			int y = rand.nextInteger(10, nx - 10);
			for (int j = 0; j < 1000000; j++) {
				x += rand.nextInteger(-2, 2);
				y += rand.nextInteger(-1, 1);
				substrat.putPixel(x, y, 0, sign * 20);
			}
		}
		substrat.smoothGaussian(6);
		return substrat;
	}
	
	public class MTCanvas extends ImageCanvas {
		private ArrayList<ArrayList<Point3D>>	tubes;
		private ArrayList<Point3D> points;
		
		public MTCanvas(ImagePlus imp, ArrayList<ArrayList<Point3D>> tubes, ArrayList<Point3D> points) {
			super(imp);
			this.tubes = tubes;
			this.points = points;
			if (imp.getStackSize() > 1)
				imp.setWindow(new StackWindow(imp, this));
			else
				imp.setWindow(new ImageWindow(imp, this));
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			for (Point3D point : points) {
				g.setColor(Color.ORANGE);
				g.fillOval(screenXD(point.x) - 3, screenYD(point.y) - 3, 7, 7);
			}
			
			for (ArrayList<Point3D> tube : tubes) {
				Color c = Color.getHSBColor((float) (tube.get(0).z / nz), 1, 1);
				g.setColor(c);
				for (int p = 1; p < tube.size(); p++) {
					Point3D prev = tube.get(p - 1);
					Point3D curr = tube.get(p);
					g.drawLine(screenXD(prev.x), screenYD(prev.y), screenXD(curr.x), screenYD(curr.y));
				}
			}
		}
	}
}
