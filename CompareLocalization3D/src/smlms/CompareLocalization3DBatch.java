package smlms;

import java.io.File;
import java.util.ArrayList;

import additionaluserinterface.WalkBar;

public class CompareLocalization3DBatch {

	private double border = 320;
	private double toleranceXY = 320;
	private double toleranceZ  = 320;
	private int algo = 0; //CompareLocalization3D.ALGO_GLOBAL_SORT_NEAREST_NEIGHBORHOOR;
	private String woobleFilename = " ";
	private int nx = 6400;
	private int ny = 6400;
	
	static public void main(String args[]) {
		new CompareLocalization3DBatch();
	}
	
	public CompareLocalization3DBatch() {
		
		String dataset = "MT0.N2.HD";
		String fileRef = "/Users/sage/Desktop/test-final/activations-MT0.N1.LD.csv";
		String fileTst = "/Users/sage/Desktop/test-final/QuickPALMresults_MT0.N2-LD-2D.csv";
		
		if (!(new File(fileRef).exists())) {
			System.out.println("File not found: " + fileRef);
			return;
		}
		
		if (!(new File(fileTst).exists())){
			System.out.println("File not found: " + fileTst);
			return;
		}
		Wobble wobble = new Wobble(woobleFilename);
		
		Description desca = new Description("Ground-truth", 2, 3, 4, 1, 5, 1.0, 1.0);
		Fluorophores[] ar = new LocalizationFile().read(desca, fileRef);
		Fluorophores[] a = Fluorophores.crop(ar, border, nx-border, border, ny-border);
		
		//Description descb = new Description("ThunderSTORM", 2, 3, 4, 1, 7, 1.0, 1.0);
		//Description descb = new Description("EasyDH", 2, 3, 4, 0, 8, 1.0, 1.0);
		//Description descb = new Description("Tested", 2, 3, 4, 1, 5, 1.0, 1.0);
		
		Description descb = new Description("QuickPALM", 4, 5, 6, 1, 14, 1.0, 1.0);
		Fluorophores[] b = new LocalizationFile().read(descb, fileTst);	
			
		for(Fluorophore fluo : b[1])
			System.out.println("Check Frame 0 " + fluo.toString() );
		ArrayList<String[]> results = new ArrayList<String[]>();
		results.add(CompareLocalization3D.getHeaders());
		
		compare2(dataset, wobble, desca, a, descb, b, results, 0);	

		CompareTable table = new CompareTable(results, CompareLocalization3D.getHeaders(), false);
		table.show(1200, 200, "Compare " + desca.name + " vs. " + descb.name);
		walk.finish("" + desca.name + " vs " + descb.name);
				
	}
	
	private void compare2(String dataset, Wobble wobble, Description desca, Fluorophores[] a, Description descb, Fluorophores[] b, ArrayList<String[]> results, double minPhotons) {
		CompareLocalization3D comparator = new CompareLocalization3D(desca.name, a);	
		String res1[] = comparator.run(walk, descb.name, b, 1, dataset, algo, true , null, minPhotons, toleranceXY, toleranceZ);
		String res2[] = comparator.run(walk, descb.name, b, 2, dataset, algo, false, null, minPhotons, toleranceXY, toleranceZ);
		results.add(res1);
		results.add(res2);
	}
	
	private WalkBar walk = new WalkBar("(c) Biomedical Imaging Group, EPFL 2016", false, false, false);

}
