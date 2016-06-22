package smlms.simulation;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.io.FileSaver;
import ij.io.Opener;
import imageware.Builder;
import imageware.ImageWare;

import java.io.File;
import java.util.ArrayList;

import smlms.file.Description;
import smlms.file.Fluorophores;
import smlms.tools.Tools;
import smlms.tools.Zip;

public class SequenceFactory_Batch_2016 {

	public static String pathRoot = System.getProperty("user.home") + "/Desktop/activation-final/";
	public static String pathPSF  = System.getProperty("user.home") + "/Desktop/activation-final/psf/";
	
	public String[] psfsAll = new String[] {"2D-Exp", "AS-Exp", "DH-Exp", "BP-250", "BP+250"};
	public String[] psfs3D  = new String[] {"AS-Exp", "DH-Exp", "BP-250", "BP+250"};
	public String[] psf2D  = new String[] {"2D-Exp"};
	public String[] psfAS  = new String[] {"AS-Exp"};
	public String[] psfDH  = new String[] {"DH-Exp"};
	public String[] psfBP  = new String[] {"BP-250", "BP+250"};

	public static void main(String args[]) {
		SequenceFactory_Batch_2016 sf = new SequenceFactory_Batch_2016();
		String[] psfs = new String[] {"2D-Exp", "AS-Exp", "DH-Exp", "BP-000-Exp", "BP-500-Exp"};
		sf.run(pathRoot, "FP0.N1", 2000, 100, "activations.csv", true, true, true, true, "");
	}
	
	public SequenceFactory_Batch_2016() {
		
		GenericDialog dlg = new GenericDialog("Batch");
		dlg.addStringField("Dataset", "MT0.N1.HD");
		dlg.addNumericField("Number of Frames", 20000, 0);
		dlg.addNumericField("AUtofluorescence", 10, 1);
		dlg.addCheckbox("2D", true);
		dlg.addCheckbox("AS", true);
		dlg.addCheckbox("DH", true);
		dlg.addCheckbox("BP", true);
		
		dlg.showDialog();
		if (dlg.wasCanceled())
			return;
		
		String dataset = dlg.getNextString();
		int nframes = (int)dlg.getNextNumber();
		double autofluo = dlg.getNextNumber();
		boolean d2d= dlg.getNextBoolean();
		boolean das = dlg.getNextBoolean();
		boolean ddh = dlg.getNextBoolean();
		boolean dbp = dlg.getNextBoolean();
		
		run(pathRoot, dataset, nframes, autofluo, "activations.csv", d2d, das, ddh, dbp, "BP-Exp");
		
		//run(pathData, "ER0.N1", 0.7, "activations-20000-frames.csv", psfs3Db, "");
		//run(pathData, "ER0.N2", 1.1, "activations-20000-frames.csv", psfs3Db, "");
		
		//run(pathData, "MT2.N1", 0.7, "activations-10000-frames.csv", psfs2D, "");	
		//run(pathData, "MT2.N2", 1.1, "activations-10000-frames.csv", psfs2D, "");
		
		//run(pathData, "MT2.N1", 0.7, "activations-20000-frames.csv", psfs3Db, "");
		//run(pathData, "MT2.N2", 1.1, "activations-20000-frames.csv", psfs3Db, "");


	}
	
	public void run(String pathData, String dataset, int nframes, double autofluo, String filename, boolean d2d, boolean das, boolean ddh, boolean dbp, String bpname) {
		
		ArrayList<String> list = new ArrayList<String>();
		if (d2d)
			list.add(psf2D[0]);
		if (das)
			list.add(psfAS[0]);
		if (ddh)
			list.add(psfDH[0]);
		if (dbp)
			list.add(psfBP[0]);
		if (dbp)
			list.add(psfBP[1]);
		
		String[] psfs = new String[list.size()];
		for(int i=0; i<list.size(); i++)
			psfs[i] = list.get(i);
		
		String path = pathData + "/" + dataset + "/";
		filename = path + filename;
		SequenceFactoryDialog dialog = new SequenceFactoryDialog();
		dialog.loadParams();
		dialog.settings.loadRecordedItems();
		dialog.path = path;
		dialog.panel.txtFile.setText(filename);
		Fluorophores fluos = Fluorophores.load( filename, new Description("activations"), dialog.panel.lblInfo);
		dialog.panel.setFluorophores(fluos);
		dialog.panel.getFluorophoresPerFrames();
		dialog.chkProjection.setSelected(true);
		dialog.chkStats.setSelected(true);
		dialog.chkReport.setSelected(true);
		IJ.log(" " + fluos.size());
		fluos.computeStats();
		
		double chrono = System.nanoTime();  // 1 ou 2 (join)
		dialog.tabPSF.setSelectedIndex(3);
		for(int i=0; i<dialog.txtPSFFile.length; i++)
			dialog.txtPSFFile[i].setText("");
		for(int i=0; i<psfs.length; i++)
			dialog.txtPSFFile[i].setText(pathPSF + psfs[i] + ".tif");
		
		dialog.cmbCameraQuantization.setSelectedIndex(5); 	// 16-bits
		dialog.spnCameraSaturation.set(65535);			// 16-bits
		dialog.cmbCameraFileFormat.setSelectedIndex(1); 	// 16-bits
		
		dialog.spnBiplaneDX.set(77.11);
		dialog.spnBiplaneDY.set(17.07);
		dialog.spnBiplaneRotation.set(1.9);
		dialog.spnBiplaneScale.set(1);
		
		dialog.chkNoises[0].setSelected(true);
		dialog.chkNoises[1].setSelected(false);
		dialog.chkNoises[2].setSelected(false);
		dialog.chkNoises[3].setSelected(true);
		dialog.chkNoises[4].setSelected(true);
		dialog.chkNoises[5].setSelected(false);
		
		dialog.spnNoises[0].set(74.4);
		dialog.spnNoises[1].set(0);
		dialog.spnNoises[2].set(0);
		dialog.spnNoises[3].set(300);
		dialog.spnNoises[4].set(0.002);
		
		dialog.spnQuantumEfficiency.set(0.9);
		dialog.spnBaseline.set(100);
		dialog.spnCameraGain.set(45);
		
		dialog.spnAutofluoOffsetMean.set(1);
		dialog.spnAutofluoOffsetStdv.set(autofluo);
		dialog.cmbAutofluoMode.setSelectedIndex(1);
		dialog.cmbAutofluoSources.setSelectedIndex(0);
		dialog.spnAutofluoGain.set(0);
			
		dialog.spnCameraResolution.set(150);
		dialog.spnNA.set(1.49);
		dialog.cmbThreading.setSelectedIndex(0);
		dialog.spnThickness.set(1500);
		
		dialog.spnLastFrame.set(nframes);
		dialog.run();
		
		IJ.log("End Batch " + ((System.nanoTime() - chrono)*1e-9));

		String pathbp = path + File.separator + bpname ;
		(new File(pathbp)).mkdir();
		
		String pathdata = pathRoot + dataset + File.separator + "Data" + File.separator;
		(new File(pathdata)).mkdir();
		Tools.copyFile(pathRoot + "data.html", pathdata + "data.html");	

		String pathOracle = pathRoot + dataset + File.separator + psfs[0] + File.separator + "oracle" + File.separator;
		Tools.copyFile(pathOracle + "sequence-parameters.html", pathdata + "parameters.html");	

		merge(dataset, path+"BP-250/sequence/", path+"BP+250/sequence/", pathdata);
		
		IJ.log("end of " + filename);
	}
	
	public void merge(String dataset, String path1, String path2, String pathout) {
		IJ.log("Path1 "  + path1);
		IJ.log("Path2 "  + path2);
		String list1[] = new File(path1).list();
		String list2[] = new File(path2).list();
		new File(pathout).mkdir();
		new File(pathout + "/sequence").mkdir();
		Opener opener = new Opener();
		IJ.log("Number of files " + list1.length + " in "  + path1);
		IJ.log("Number of files " + list2.length + " in "  + path2);
		ImageStack stack = null;
	
		for(int i=0; i<Math.min(list1.length, list2.length); i++) {
			IJ.log(" " + list1[i]);
			ImagePlus imp1 = opener.openImage(path1 + list1[i]);
			ImagePlus imp2 = opener.openImage(path2 + list2[i]);
			ImageWare image1 = Builder.wrap(imp1);
			ImageWare image2 = Builder.wrap(imp2);
			int nx1 = image1.getSizeX();
			int nx2 = image1.getSizeX();
			int ny1 = image2.getSizeX();
			ImageWare image = Builder.create(nx1+nx2, ny1, 1, image1.getType());
			image.putXY(0,  0,  0, image1);
			image.putXY(nx1,  0,  0, image2);
			ImagePlus imp = new ImagePlus("", image.buildImageStack());
			(new FileSaver(imp)).saveAsTiff(pathout + "/sequence/" + File.separator + list1[i]);
			if (stack == null) 
				stack = new ImageStack(nx1+nx2, ny1);
			stack.addSlice("", image.buildImageStack().getProcessor(1));
		}
		
		String pathdata = pathRoot + dataset + File.separator + "Data" + File.separator;
		Zip.zipFolder(pathout + "/sequence/", pathdata + "sequence-" + dataset + "-BP-Exp-as-list.zip");
		
		ImagePlus imps = new ImagePlus("as-stack", stack);
		imps.show();
		String pathStack = pathout + "/sequence-as-stack" + File.separator;	
		new File(pathStack).mkdir();
		(new FileSaver(imps)).saveAsTiffStack(pathStack + "/sequence-" + dataset + "-BP-Exp-as-stack.tif");	


		Zip.zipFolder(pathStack, pathdata + "sequence-" + dataset + "-BP-Exp-as-stack.zip");
		
		new File(pathStack).delete();
		new File(pathout + "/sequence/").delete();

	}

}
