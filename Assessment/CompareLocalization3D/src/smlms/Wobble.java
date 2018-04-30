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
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class Wobble {

	private double	dx[];
	private double	dy[];
	private double	z[];
	private int		on	= 0;	// 0 No 1 On, 2 Err
	private String	filename;
	private double dx0 = 0;	// shift in he focal plane
	private double dy0 = 0;	// shift in he focal plane
	
	public Wobble(String filename) {
		this.filename = filename;
		if (filename != null) {
			try {
				BufferedReader buffer = new BufferedReader(new FileReader(filename));
				String line = buffer.readLine();
				ArrayList<double[]> rows = new ArrayList<double[]>();
				while (line != null) {
					String items[] = line.split("[,;\t]");
					if (items.length == 3) {
						try {
							double x = Double.parseDouble(items[0]);
							double y = Double.parseDouble(items[1]);
							double z = Double.parseDouble(items[2]);
							rows.add(new double[] { x, y, z });
						}
						catch(Exception ex) {}
					}
					line = buffer.readLine();
				}
				if (rows.size() >= 2) {
					dx = new double[rows.size()];
					dy = new double[rows.size()];
					z = new double[rows.size()];
					int index = 0;
					double min = Double.MAX_VALUE;
					for (int i = 0; i < rows.size(); i++) {
						dx[i] = rows.get(i)[0];
						dy[i] = rows.get(i)[1];
						z[i] = rows.get(i)[2];
						if (Math.abs(z[i]) < min) {
							min = Math.abs(z[i]);
							index = i;
						}		
					}
					on = 1;
					dx0 = dx[index];
					dy0 = dy[index];
					System.out.println("Bias at the focal plane " + dx0 + " " + dy0);
				}
				else {
					on = 2;
				}
				buffer.close();
			}
			catch (Exception e) {
				on = 2;
			}
		}
		else
			on = 0;
	}
	
	public double[] getCorrectionAt0() {
		return new double[] {dx0, dy0};
	}
	
	/*
	 * Corrected the systematic bias at the focal plane
	 */
	public void bias1(Fluorophores original) {
		if (on == 1) {
			for(Fluorophore fluo : original) {
				fluo.xnano = fluo.xnano - dx0;
				fluo.ynano = fluo.ynano - dy0;
			}
		}
	}

	public void correctionTest1(FluorophorePairs pairs) {
		if (on == 1) {
			for(FluorophorePair pair : pairs) {
				Fluorophore ref = pair.ref;
				int index = -1;
				double min = Double.MAX_VALUE;
				for (int i = 0; i < z.length; i++) {
					if (min > Math.abs(ref.znano - z[i])) {
						index = i;
						min = Math.abs(ref.znano - z[i]);
					}
				}
				if (index >= 0) {
					Fluorophore test = pair.test;
					test.xnano = test.xnano + dx0 - dx[index];
					test.ynano = test.ynano + dy0 - dy[index];
				}
			}		
		}
	}
	
	public Fluorophore wobble(Fluorophore fluo) {
		if (on == 1) {
			Fluorophore fluw = fluo.duplicate();
			int index = -1;
			double min = Double.MAX_VALUE;
			for (int i = 0; i < z.length; i++) {
				if (min > Math.abs(fluw.znano - z[i])) {
					index = i;
					min = Math.abs(fluw.znano - z[i]);
				}
			}
			if (index >= 0) {
				fluw.xnano = fluw.xnano - dx[index];
				fluw.ynano = fluw.ynano - dy[index];
			}
			return fluw;
		}
		return fluo;
	}

	public boolean on() {
		return on == 1;
	}
	
	
	public String getName() {
		if (on == 0)
			return "NO";
		else if (on == 1)
			return new File(filename).getName();
		else
			return "ERR";
	}
	
}
