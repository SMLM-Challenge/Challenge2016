package smlms.file;

import ij.IJ;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JLabel;

public class Fluorophores extends ArrayList<Fluorophore> {

	private Statistics[] stats;

	public Fluorophores() {
		super();
	}
	
	static public Fluorophores load(String filename, Description desc, JLabel lblNotification) {
		Fluorophores fluorophores = new Fluorophores();
		IJ.log("Load:" + filename);
		int readingErrors = 0;
		try {
			BufferedReader buffer = new BufferedReader(new FileReader(filename));
			HashMap<Integer, Fields> fields = desc.getFields();
			for(int i=1; i<fields.size()+1; i++)
				IJ.log("Column " + i + " > " + fields.get(i));
			int lineNumber = 0;
			String line = readLine(buffer);
			for(int i=0; i<desc.numberOfHeadingRows; i++)
				line = readLine(buffer);
			while (line != null) {
				try {
					String words[] = line.trim().split("[,:;\t]");
					Fluorophore fluo = new Fluorophore();
					for(int i=0; i<words.length; i++) {
						Fields f = fields.get(i+1);
						if (f == Fields.X)
							fluo.x = desc.axisX.transform(Double.parseDouble(words[i]));
						if (f == Fields.Y)
							fluo.y = desc.axisY.transform(Double.parseDouble(words[i]));
						if (f == Fields.Z)
							fluo.z = desc.axisZ.transform(Double.parseDouble(words[i]));
						if (f == Fields.PHOTONS)
							fluo.photons = Double.parseDouble(words[i]);
						if (f == Fields.FRAME)
							fluo.frame = (int)desc.axisT.transform((int)Double.parseDouble(words[i]));
						if (f == Fields.ID)
							fluo.id = (int)Double.parseDouble(words[i]);
					}
					fluorophores.add(fluo);
				}
				catch (Exception e) {
					IJ.log("Error in line number:" + lineNumber + " <" + line + "> " );
					readingErrors++;
				}
				line = readLine(buffer);
				lineNumber++;
				if (lblNotification != null && lineNumber % 100 == 0)
					lblNotification.setText("fluos:" + fluorophores.size() + " error:" + readingErrors);
			}
			if (lblNotification != null)
				lblNotification.setText("fluos:" + fluorophores.size() + " error:" + readingErrors);
		}
		
		catch (Exception ex) {
			IJ.log(ex.toString());
		}
		IJ.log("End of reading:" + fluorophores.size() + " fluos");

		return fluorophores;
	}

	public void save(String filename) {
		(new File(filename)).getParentFile().mkdir();
		File file = new File(filename);
		try {
			BufferedWriter buffer = new BufferedWriter(new FileWriter(file));
			int count = 0;
			for(Fluorophore fluo : this) {
				String s = "";
				s += "" + (++count) + ", "; 
				s += IJ.d2s(fluo.frame, 0) + ", "; 
				s += IJ.d2s(fluo.x, 5) + ", "; 
				s += IJ.d2s(fluo.y, 5) + ", "; 
				s += IJ.d2s(fluo.z, 5) + ", ";
				s += IJ.d2s(fluo.photons, 5);
				buffer.write(s + "\n");
			}
			buffer.close();
		}
		catch (IOException ex) {
		}
	}
	/*
	public void save(ArrayList<Point3D> positions) {
		(new File(filename)).getParentFile().mkdir();
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
			IJ.log("Number of saved fluorophores: " + count);
			buffer.close();
		}
		catch (IOException ex) {
			IJ.error("IOException");
		}
			walk.finish();
	}
	*/
	public Statistics[] getStats() {
		if (stats == null)
			computeStats();
		return stats;
	}
	
	public int getFrameMax() {
		if (stats == null)
			computeStats();
		return (int)Math.ceil(stats[4].max);
	}
	
	public double getXMax(int round) {
		if (stats == null)
			computeStats();
		return (int)Math.ceil((stats[1].max*round))/round;
	}
	
	public double getYMax(int round) {
		if (stats == null)
			computeStats();
		return (int)Math.ceil((stats[2].max*round))/round;
	}
	
	public double getZMax(int round) {
		if (stats == null)
			computeStats();
		return (int)Math.ceil((stats[2].max*round))/round;	
	}

	public void computeStats() {
		String[] names = Fluorophore.vectorNames();
		int n = Fluorophore.vectorSize();
		stats = new Statistics[n];
		int nfluo = size();
		for(int i=0; i<n; i++)
			stats[i] = new Statistics(names[i]);
			
		for(Fluorophore fluo : this) {
			double[] vect = fluo.vectorValues();
			for(int i=0; i<n; i++) {
				stats[i].count++;
				stats[i].min = Math.min(stats[i].min, vect[i]);
				stats[i].max = Math.max(stats[i].max, vect[i]);
				stats[i].mean += vect[i];
				stats[i].stdev += vect[i] * vect[i];
			}
		}
		
		if (nfluo > 0) {
			for(int i=0; i<n; i++) {
				stats[i].mean /= nfluo; 
				stats[i].stdev = Math.sqrt(stats[i].stdev*n - stats[i].mean*stats[i].mean)/nfluo;
			}
			for(Fluorophore fluo : this) {
				double[] vect = fluo.vectorValues();
				for(int i=0; i<n; i++) {
					stats[i].addHisto(vect[i]);
				}
			}
		}
		
		int nframes = getFrameMax() + 1;
		int count[] = new int[nframes];
		for(int i=0; i<n; i++)
			stats[i].evolution = new double[nframes];	
		
		for(Fluorophore fluo : this) {
			double[] vect = fluo.vectorValues();
			int frame = (int)vect[4];
			if (frame >= 0 && frame < nframes)
				for(int i=0; i<n; i++) {
					stats[i].evolution[frame] += vect[i];
					count[frame]++;
				}
		}
		for(int f=0; f<nframes; f++) {
			if (count[f] > 0)
				for(int i=0; i<n; i++) {
					stats[i].evolution[f] /= count[f];
				}
		}
				
	}

	public Fluorophores[] reshapeInFrames() {
		int nframes = getFrameMax() + 1; 
		Fluorophores[] fluorophores = new Fluorophores[nframes];
		for(int i=0; i<nframes; i++)
			fluorophores[i] = new Fluorophores();
		for(Fluorophore fluo : this) {
			int frame = fluo.frame;
			if (frame >= 0 && frame < nframes)
				fluorophores[frame].add(fluo);
		}
		return fluorophores;
	}
	
	static public String getInfo(Fluorophores[] fluorophores) {
		int count = 0;
		for(Fluorophores ff : fluorophores) 
			count += ff.size();
		return "" + count + " fluos " + fluorophores.length + " frames";
	}

	private static String readLine(BufferedReader buffer) {
		try {
			return buffer.readLine();
		}
		catch (Exception e) {
			return null;
		}
	}

}
