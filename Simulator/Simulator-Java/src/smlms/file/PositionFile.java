package smlms.file;

import ij.IJ;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import smlms.tools.Point3D;
import additionaluserinterface.WalkBar;

public class PositionFile {

	private String path;
	private String filename;
	private WalkBar walk;
	
	public PositionFile(WalkBar walk, String filename) {
		this.walk = walk;
		this.filename = filename;
		this.path = new File(filename).getParent() + File.separator;
	}
	public PositionFile(WalkBar walk, String path, String name) {
		this.walk = walk;
		filename = path  + File.separator + name;
	}
	
	public void save(ArrayList<Point3D> positions) {
		(new File(path)).mkdir();
		File file = new File(filename);
		if (walk != null)
			walk.reset();
		try {
			BufferedWriter buffer = new BufferedWriter(new FileWriter(file));
			double n = positions.size();
			int count = 0;
			for(Point3D position : positions) {
				String s = "";
				s += IJ.d2s(position.x, 5) + ", "; 
				s += IJ.d2s(position.y, 5) + ", "; 
				s += IJ.d2s(position.z, 5); 
				buffer.write(s + "\n");
				count++;
				if (walk != null & count % 100 == 0)
					walk.progress("" + count, 100.0*count/n);
			}
			IJ.log("Number of saved fluorophores: " + count + "  into " + filename);
			buffer.close();
		}
		catch (IOException ex) {
			IJ.error("IOException");
		}
			walk.finish();
	}
}
