package smlms.sample;

import ij.IJ;

import java.util.ArrayList;

import smlms.tools.NormalizedVariable;
import smlms.tools.Point3D;

public class Tube extends Item {

	//private CurveSpline axis;
	public CurveSegment axis;
	public ArrayList<Point3D> nodes;
	public NormalizedVariable radius;
	public NormalizedVariable thickness;
	public ArrayList<Point3D> list;
	public double innerMin;
	public double outerMax;
	public double density;
	public double inner[];
	public double outer[];
	
	public Tube(String name, ArrayList<Point3D> nodes, NormalizedVariable radius, NormalizedVariable thickness, double density) {
		super(name);
		this.radius = radius;
		this.thickness = thickness;
		this.density = density;
		this.nodes = nodes;
		
		axis = new CurveSegment(nodes, 0.1, 0);
		//axis = new CurveSpline(nodes, 4);
	}
	
	@Override
	public String getInfo() {
		String s = "\nTUBE " + getName() + "\n";
		s += "radius:" + radius.write() + "\n";
		s += "thickness:" + thickness.write() + "\n";
		s += "density:" + density + "\n";
		for(Point3D node : nodes)
		    s += "node:" + node.x + "," + node.y + "," + node.z + "\n";
		return s;
	}
	
	public ArrayList<Point3D> getSamplesInterval(double step, double sigma) {
		return axis.getSamplesInterval(step, sigma);
	}
	
	public Point3D[] getNodes() {
		return axis.getNodes();
	}

	public void initStep(double step, double sigma) {
		
		list = axis.getSamplesInterval(step, sigma);
		double n = list.size();
		inner = new double[list.size()];
		outer = new double[list.size()];
		innerMin =  1000000.0;
		outerMax = -1000000.0;
		for(int i=0; i<list.size(); i++) {
			inner[i] = radius.get(i/n) - thickness.get(i/n);
			outer[i] = radius.get(i/n);
			innerMin = Math.min(inner[i], innerMin);
			outerMax = Math.max(outer[i], outerMax);
		}
	}

	public void init(int nsamples) {
		list = axis.getSamplesNumber(nsamples);
		double n = list.size();
		inner = new double[list.size()];
		outer = new double[list.size()];
		innerMin =  1000000.0;
		outerMax = -1000000.0;
		for(int i=0; i<list.size(); i++) {
			inner[i] = radius.get(i/n) - thickness.get(i/n);
			outer[i] = radius.get(i/n);
			innerMin = Math.min(inner[i], innerMin);
			outerMax = Math.max(outer[i], outerMax);
		}
		IJ.log("Init nb samples: " + list.size() + " innerMin " + innerMin+ " outerMax " + outerMax);
	}

	public double[] closestDistance(Point3D p) {
		double distance = Double.MAX_VALUE;
		double n = list.size();
		int index = 0;
		for(int i=0; i<n; i++) {
			Point3D a = list.get(i);
			double d = (p.x-a.x)*(p.x-a.x) + (p.y-a.y)*(p.y-a.y) + (p.z-a.z)*(p.z-a.z);
			if (d < distance) {
				distance = d;
				index = i;
			}
		}
		return new double[] {Math.sqrt(distance), index};
	}
	
	@Override
    public boolean contains(Point3D p) {
		double[] closest = closestDistance(p);
		double min = closest[0];
		int imin = (int)closest[1];
		if (imin == 0)
			return false;
		if (imin == list.size()-1)
			return false;
		if (min < inner[imin])
			return false;
		if (min > outer[imin]) 
			return false;
		return true;
    }

	public int inside(Point3D p, int k1, int k2) {
		double min = Double.MAX_VALUE;
		int i1 = Math.max(1,  k1);
		int i2 = Math.min(list.size()-2,  k2);
		int index = 0;
		for(int i=i1; i<=i2; i++) {
			Point3D a = list.get(i);
			double d = (p.x-a.x)*(p.x-a.x) + (p.y-a.y)*(p.y-a.y) + (p.z-a.z)*(p.z-a.z);
			if (d < min) {
				min = d;
				index = i;
			}
		}
		min = Math.sqrt(min);
		if (min < inner[index])
			return -1;
		if (min > outer[index]) 
			return -1;
		return index;
	}

	public int inside(Point3D p, double resizeFactor) {
		double[] closest = closestDistance(p);
		int index = (int)closest[1];
		if (index <= 0)
			return -1;
		if (index >= list.size()-1)
			return -1;
		if (closest[0] < inner[index]/resizeFactor)
			return -1;
		if (closest[0] > outer[index]*resizeFactor) 
			return -1;
		return index;
	}
	
	public static Tube load(ArrayList<String> lines) {
		String name = "noname";
		ArrayList<Point3D> nodes = new ArrayList<Point3D>();
		NormalizedVariable radius = null;
		NormalizedVariable thickness = null;
		double density = 1;
		for(String line : lines) {
			String[] tokens = line.split("[:]");
			for (int i=0; i<tokens.length; i++) {
				if (tokens[i].startsWith("name"))
					name = tokens[i+1];
				if (tokens[i].startsWith("radius"))
					radius = NormalizedVariable.read(tokens[i+1]);
				if (tokens[i].startsWith("thickness"))
					thickness = NormalizedVariable.read(tokens[i+1]);
				if (tokens[i].startsWith("density"))
					density = Double.parseDouble(tokens[i+1]);
				if (tokens[i].startsWith("node"))
					nodes.add(Point3D.read(tokens[i+1]));
			}
		}
		return new Tube(name, nodes, radius, thickness, density);
	}

	public String save() {
		String s = "<TUBE>";
		s += "name:" + getName() + "\n";
		s += "radius:" + radius.write() + "\n";
		s += "thickness:" + thickness.write() + "\n";
		s += "density:" + density + "\n";
		for(Point3D node : nodes)
		    s += "node:" + node.x + "," + node.y + "," + node.z + "\n";
		return s + "</TUBE>\n";
	}

}
