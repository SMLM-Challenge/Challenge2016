package smlms.tools;

import ij.IJ;

public class Progress {

	private double chrono;
	
	public Progress() {
		chrono = System.nanoTime();
	}
	
	public void print(String message) {
		double c = (System.nanoTime() - chrono);
		if (c < 1e3)
			IJ.log(String.format("%3.1f ns // ", c) + message);
		else if (c < 1e6)
			IJ.log(String.format("%3.1f us // ", (c*1e-3)) + message);
		else if (c < 1e9)
			IJ.log(String.format("%3.1f ms // ", (c*1e-6)) + message);
		else if (c < 1e12)
			IJ.log(String.format("%3.1f  s // ", (c*1e-9)) + message);
		else if (c < 1e14)
			IJ.log(String.format("%3.1f mn // ", (c*1e-9)/60.0) + message);
		else if (c < 1e16)
			IJ.log(String.format("%3.1f  h // ", (c*1e-9)/3600.0) + message);
	}
}
