package smlms.sample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;

import smlms.tools.Point3D;
import smlms.tools.Progress;

public class Sample extends ArrayList<Item> {

	public String name;
	public int nx;
	public int ny;
	public int nz;
	public ArrayList<Point3D> voxels = new  ArrayList<Point3D>();
	public ArrayList<Point3D> fluos = new  ArrayList<Point3D>();
	
	public Sample(String name, int nx, int ny, int nz) {
		this.name = name;
		this.nx = nx;
		this.ny = ny;
		this.nz = nz;
	}

	public String getInfo() {
		return name + " [" + nx + ", " + ny + ", " + nz + "]";
	}
	
	public static Sample load(String filename) {
		Progress progress = new Progress();
		progress.print("Load Sample " + filename);
		File file = new File(filename);
		if (!file.exists())
			return null;

		try {
			BufferedReader input = new BufferedReader(new FileReader(filename));
			String line = input.readLine();
			String[] tokens = line.split("[,:]");

			if (tokens[0].equals("SAMPLE")) {
				String name = "untitled";
				int nx = 1;
				int ny = 1;
				int nz = 1;
				for (int i=0; i<tokens.length; i++) {
					if (tokens[i].equals("name"))
						name = tokens[i+1];
					if (tokens[i].equals("size")) {
						nx = Integer.parseInt(tokens[i+1]);
						ny = Integer.parseInt(tokens[i+2]);
						nz = Integer.parseInt(tokens[i+3]);
					}
				}
				Sample sample = new Sample(name, nx, ny, nz);
				ArrayList<String> lines = new ArrayList<String>();
				while ((line = input.readLine()) != null) {
					if (lines.size() % 100 == 0)
						progress.print(" line " + line);
					if (line.startsWith("<TUBE>"))
						lines.clear();
					if (line.startsWith("</TUBE>"))		
						sample.add(Tube.load(lines));
					lines.add(line);
				}
				progress.print("Load " + sample.size() + " items.");
				input.close();
				return sample;
			}
			else {
				progress.print("Format error in reading " + filename);
			}
			input.close();
		}
		catch (Exception ex) {
			progress.print(ex.toString());
		}
		return null;
	}

	public Tube[] getTubes() {
		int ntubes = 0;
		for (Item item : this)
			if (item instanceof Tube) 
				ntubes++;
		Tube tubes[] = new Tube[ntubes];
		ntubes = 0;
		for (Item item : this)
			tubes[ntubes++] = (Tube) item;
		return tubes;
	}
	
	public void save(String filename) {
		Progress progress = new Progress();
		File file = new File(filename);
		File parent = new File(file.getParent());
		if (!parent.exists()) {
			parent.mkdir();
		}
		try {
			PrintStream out = new PrintStream(new FileOutputStream(filename));
			progress.print("Save Sample " + filename);
			out.print("SAMPLE,name:" + name + ",size:" + nx + "," + ny + "," + nz + "\n");
			for (Item item : this) {
				String s = item.save();
				out.print(s);
				//progress.print(s);
			}
			out.close();
		}
		catch (Exception ex) {
			progress.print(filename + " " + ex.toString());
		}

	}
}
