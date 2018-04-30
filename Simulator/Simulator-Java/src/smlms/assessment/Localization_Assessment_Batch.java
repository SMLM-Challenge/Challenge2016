package smlms.assessment;

import ij.IJ;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JLabel;

import smlms.file.Description;
import smlms.file.Fluorophores;
import additionaluserinterface.WalkBar;

public class Localization_Assessment_Batch {

	public static String groundtruth  = "/Users/sage/Desktop/beads/MT1.0/activations.csv";
	public static String path = "/Users/sage/Desktop/beads/MT /results/";
	private WalkBar	walk = new WalkBar("(c) 2016 EPFL, BIG", false, false, true);

	public Localization_Assessment_Batch() {
			
		String dataset = "MT0-6";
		double pixelsize = 100;
		double radius = 250;
		int algo = 0;
		String logging = "Talk";
		
		File dir = new File(path);
		String files[] = dir.list();
		int n = files.length;
		Fluorophores refsall = Fluorophores.load( groundtruth, new Description("activations"), new JLabel());
		Fluorophores[] refs = refsall.reshapeInFrames();
		
		ArrayList<CompareResult> results = new ArrayList<CompareResult>();
		for(int i=0; i<n; i++) {
			
			if (files[i].endsWith("csv") || files[i].endsWith("xls")) {
				String filename = path + File.separator + files[i];
				String software = files[i];
				IJ.log(" \n\n" + files[i]);
				String[] items = files[i].split("[.]");
				IJ.log(" " + items.length);
				if (items.length >= 2) {
					Description desc = new Description(items[0]);
					Fluorophores testsall = Fluorophores.load(filename, desc, new JLabel());
					Fluorophores[] tests = testsall.reshapeInFrames();
					CompareLocalization cl = new CompareLocalization(walk, dataset, software, logging, refs, tests, algo, radius, pixelsize);
					cl.run();
					results.add(cl.getResult());
				}
			}
		}
		
		
		AssessmentTable table = new AssessmentTable(CompareResult.getResultsHeader());
		table.update(results);
		table.show(1000, 100);
	
	}
	
}
