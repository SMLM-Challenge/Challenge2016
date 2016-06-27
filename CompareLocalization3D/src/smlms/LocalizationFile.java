//=========================================================================================
//
// Single-Molecule Localization Microscopy Challenge 2016
// http://bigwww.epfl.ch/smlm/
//
// Author: 
// Daniel Sage, http://bigwww.epfl.ch/sage/
// Biomedical Imaging Group (BIG)
// Ecole Polytechnique Federale de Lausanne (EPFL), CH-1015 Lausanne, Switzerland
//
// Reference: 
// D. Sage, H. Kirshner, T. Pengo, N. Stuurman, J. Min, S. Manley, M. Unser
// Quantitative Evaluation of Software Packages for Single-Molecule Localization Microscopy 
// Nature Methods 12, August 2015.
// 
// Conditions of use: 
// You'll be free to use this software for research purposes, but you 
// should not redistribute it without our consent. In addition, we expect you to include a
// citation or acknowledgment whenever you present or publish results that are based on it.
//
//=========================================================================================

package smlms;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class LocalizationFile {

	private int nbErrors = 0;
	
	public static Fluorophores[] load(Description desc, String filename) {
		LocalizationFile loc = new LocalizationFile();
		Fluorophores[] fluos = loc.read(desc, filename);
		return fluos;
	}

	public Fluorophores[] read(Description desc, String filename) {
		int firstrow = desc.firstRow;
	
		int lastFrame = 1;
		ArrayList<Fluorophore> fluos = new ArrayList<Fluorophore>();
		try {
			BufferedReader buffer = new BufferedReader(new FileReader(filename));

			for (int i = 0; i < firstrow; i++)
				readLine(buffer);
			int lineNumber = 0;
			Fluorophore fluorophore = null;
			String line = readLine(buffer);
			nbErrors = 0;
			while (line != null) {
				try {
					String items[] = line.split("[,;\t]");
					int frame = 0;
					double xnano = 0.0, ynano = 0.0, znano = 0.0, intensity = 0.0;
					for (int col = 0; col < items.length; col++) {
						String field = items[col].trim();
						if (col == desc.colX)
							xnano = (Double.parseDouble(field) + desc.shiftX) * desc.pixelsize;
						if (col == desc.colY)
							ynano = (Double.parseDouble(field) + desc.shiftY) * desc.pixelsize;
						if (col == desc.colZ)
							znano = (Double.parseDouble(field) + desc.shiftZ) * desc.zstep;
						if (col == desc.colFrame)
							frame = (int)Math.round(Double.parseDouble(field) + desc.shiftFrame);
						if (col == desc.colIntensity)
							intensity = Double.parseDouble(field);
						lastFrame = Math.max(frame, lastFrame);
					}
					Fluorophore fluo = new Fluorophore(xnano, ynano, znano, frame, intensity);
					fluos.add(fluo);
				}
				catch (Exception e) {
					System.out.println("Error in line number:" + lineNumber + "\n " + line + " at file " + filename);
					nbErrors++;
				}
				line = readLine(buffer);
				lineNumber++;
			}
		}
		catch (Exception ex) {
			
		}

		int n = lastFrame+1;
		Fluorophores[] fluorophores = new Fluorophores[n];
		for(int t=0; t<n; t++)
			fluorophores[t] = new Fluorophores();
		for (Fluorophore fluo : fluos) {
			if (fluo.frame >= 0 && fluo.frame < n) 
				fluorophores[fluo.frame].add(fluo);
		}

		return fluorophores;

	}

	public int getNbErrors() {
		return nbErrors;
	}
	
	private String readLine(BufferedReader buffer) {
		try {
			return buffer.readLine();
		}
		catch (Exception e) {
			return null;
		}
	}
}
