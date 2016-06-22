//=========================================================================================
//
// Project: Single-Molecule Localization Microscopy
//			Benchmarking of Localization Microscopy Software for Super-resolution Imaging
//
// Author : Daniel Sage, Biomedical Imaging Group (BIG), http://bigwww.epfl.ch/sage/
//
// Organization: Ecole Polytechnique Federale de Lausanne (EPFL), Lausanne, Switzerland
//
// Reference: paper submitted, 2013
//
// Conditions of use: You'll be free to use this software for research purposes, but you 
// should not redistribute it without our consent. In addition, we expect you to include a
// citation or acknowledgment whenever you present or publish results that are based on it.
//
//=========================================================================================

package smlms.assessment;


public class CompareResult {
	
	private String algo;
	private double tolerance;
	private double time = 0;
	public int intersection = 0;
	public double rmseLateral = 0;
	public double rmseAxial = 0;
	public double deltaX = 0;
	public double deltaY = 0;
	public double deltaZ = 0;
	public double mdIntensity = 0;
	public double precision = 0;
	public double recall = 0;
	public double jaccard = 0;
	public double fscore = 0;
	public double snr = 0;
	public double frc = 0;	// Fourier Ring Correlation
	private int ntest;
	private double pixelsize = 100;
	
	public String dataset;
	public String software;
	
	public CompareResult(String dataset, String software, int algo, double tolerance, double pixelsize) {
		this.algo = (algo == 0 ? "GS-NN" : (algo == 1 ? "NN" : "Hungarian"));
		this.dataset = dataset;
		this.software = software;
		this.tolerance = tolerance;
		this.pixelsize = pixelsize;
	}
	
	public void add(FluorophorePair pair) {
		intersection++;
		deltaX += pair.ref.deltaX(pair.test);
		deltaY += pair.ref.deltaY(pair.test);
		deltaZ += pair.ref.deltaZ(pair.test);
		mdIntensity += pair.ref.differenceIntensity(pair.test);
		rmseLateral += pair.ref.distanceLateral(pair.test) * pair.ref.distanceLateral(pair.test);
		rmseAxial += pair.ref.distanceAxial(pair.test) * pair.ref.distanceAxial(pair.test);
	}
	
	public void compute(int nref, int ntest, double time) {
		this.ntest = ntest;
		this.time = time;
		int TP = intersection;
		double FP = ntest - TP;
		double FN = nref - TP;
		precision = TP / (TP + FP);
		recall = TP / (TP + FN);
		double union = nref + ntest - intersection;
		if (intersection > 0) {
			deltaX /= intersection;
			deltaY /= intersection;
			deltaZ /= intersection;
			mdIntensity /= intersection;
			rmseLateral = Math.sqrt(rmseLateral / intersection);
			rmseAxial = Math.sqrt(rmseAxial / intersection);
			jaccard = (100.0 * intersection) / union;
			fscore = 2.0 * (precision * recall) / (precision + recall);
		}		
	}

	public String[] getResultsValues(int num) {
		String line[] = new String[20];
		for (int i = 0; i < line.length; i++)
			line[i] = "-";
		
		line[ 0] = "" + num;
		line[ 1] = dataset;
		line[ 2] = software;
		line[ 3] = algo;
		line[ 4] = String.format("%1.3f", time);
		line[ 5] = String.format("%3.1f", tolerance);
		line[ 6] = String.format("%d",  ntest);
		line[ 7] = String.format("%d",  intersection);
		line[ 8] = String.format("%2.3f", jaccard);
		line[ 9] = String.format("%1.3f", fscore);
		line[10] = String.format("%1.3f", precision);
		line[11] = String.format("%1.3f", recall);
		line[12] = String.format("%3.3f", rmseLateral);
		line[13] = String.format("%3.3f", rmseAxial);
		line[14] = String.format("%3.3f", deltaX);
		line[15] = String.format("%3.3f", deltaY);
		line[16] = String.format("%3.3f", deltaZ);
		line[17] = String.format("%5.3f", mdIntensity);
		line[18] = String.format("%5.3f", snr);
		line[19] = String.format("%5.3f", frc);
		return line;
	}

	public static String[] getResultsHeader() {	
		return new String[] { "no", "Dataset", "Software", "Algo", "Time", "Tolerance", "#Fluos", "#Intersection", "Jaccard", "F-Score", "Precision",
			"Recall", "RMSE Lateral", "RMSE Axial", "Delta X", "Delta Y", "Delta Z", "MD Intensity", "SNR", "Resolution FRC" };
	}

	public String[] getResultsUnit() {	
		return new String[] { "", "", "", "", "s.", "nm", "", "", "%", "", "", "", "nm", "nm", "nm", "nm", "nm", "", "dB", "nm" };
	}

	
	public String[] getSummaryValues(String dataset, String softname) {
		String line[] = new String[5];
		for (int i = 0; i < line.length; i++)
			line[i] = "-";
		
		line[0] = dataset;
		line[1] = softname;
		line[2] = String.format("%2.3f", jaccard);
		line[3] = String.format("%3.3f", rmseLateral);
		line[4] = String.format("%5.3f", snr);
		return line;
	}
	
	public String[] getSummaryHeader() {	
		return new String[] { "Dataset", "Software", "Jaccard", "RMSE Lateral", "SNR" };
	}
	
	public String[] getHighlightTableHeader() {
		return new String[] {"Feature", "Value", "Unit"};
	}
	
	public String[][] getHighlightTableValues() {
		String table[][] = new String[4][3];		
		table[0] = new String[] {"Number of fluorophores", String.format("%d", ntest), ""};
		table[1] = new String[] {"Jaccard", String.format("%3.2f", jaccard), "%"};
		table[2] = new String[] {"Accuracy", String.format("%3.2f", rmseLateral), "nm"};
		table[3] = new String[] {"Image-based SNR", String.format("%3.2f", snr), "dB"};
		return table;
	}

	public String[] getCompleteTableHeader() {
		return new String[] {"Protocol of assessment", "Value", "Localization of particles", "Results", "Accuracy of localization", "Results"};
	}
	
	public String[][] getCompleteTableValues() {
		String table[][] = new String[4][6];		
		table[0] = new String[] {
				"Radius of tolerance", String.format("%3.1f nm", tolerance), 
				"Jaccard", String.format("%3.2f", jaccard) + "",
				"RMSE", String.format("%3.2f nm", rmseLateral)
		};
		
		table[1] = new String[] {
				"Number of fluorophores", String.format("%d", ntest),
				"F-Score", String.format("%3.2f", fscore),
				"Delta in X axis", String.format("%3.3f nm", deltaX)
		};
		
		table[2] = new String[] {
				"Number of matching", String.format("%d", intersection),
				"Precision", String.format("%3.2f", precision),
				"Delta in Y axis", String.format("%3.3f nm", deltaY)
				};
		
		table[3] = new String[] {
				"Pixelsize", String.format("%3.0f nm", pixelsize),
				"Recall", String.format("%3.3f", recall), 
				"Image-based SNR", String.format("%3.2f dB", snr)};
		return table;
	}

	public String[] getNumberOfFluorophores() {
		String table[] = new String[]  {"Number of fluorophores", String.format("%d", ntest), ""};
		return table;
	}
}
