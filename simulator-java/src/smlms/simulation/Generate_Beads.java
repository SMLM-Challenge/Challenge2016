package smlms.simulation;

import ij.IJ;
import smlms.file.Description;
import smlms.file.Fluorophores;

public class Generate_Beads {

	public static String path = "/Users/sage/Desktop/beads/beads6/";
	public static String pathPSF = "/Users/sage/Desktop/beads/psf/";
	public static String filename = path + "activations.csv";

	public static String[] psfs = new String[] {"2D-Exp", "AS-Exp", "DH-Exp", "BP000-Exp", "BP500-Exp"};
	
	public static void main(String args[]) {
		Generate_Beads sf = new Generate_Beads();
		sf.run(filename);
	}
	
	public Generate_Beads() {
		run(filename);
	}
	
	public void run(String path) {
		SequenceFactoryDialog dialog = new SequenceFactoryDialog();
		dialog.loadParams();
		dialog.settings.loadRecordedItems();
		dialog.path = path;
		dialog.panel.txtFile.setText(filename);
		Fluorophores fluos = Fluorophores.load(filename, new Description("activations"), dialog.panel.lblInfo);
		dialog.panel.setFluorophores(fluos);
		dialog.panel.getFluorophoresPerFrames();
		dialog.chkProjection.setSelected(true);
		dialog.chkStats.setSelected(true);
		dialog.chkReport.setSelected(true);
		fluos.computeStats();
		
		double chrono = System.nanoTime();  // 1 ou 2 (join)
		dialog.tabPSF.setSelectedIndex(3);
		for(int i=0; i<dialog.txtPSFFile.length; i++)
			dialog.txtPSFFile[i].setText("");
		for(int i=0; i<psfs.length; i++)
			dialog.txtPSFFile[i].setText(pathPSF + psfs[i] + ".tif");
		
		dialog.spnCameraResolution.set(64);
		dialog.spnLastFrame.set(151);
		dialog.cmbThreading.setSelectedIndex(0);
		dialog.cmbCameraQuantization.setSelectedIndex(5); 	// 16-bits
		dialog.spnCameraSaturation.set(65535); 				// 16-bits
		dialog.cmbCameraFileFormat.setSelectedIndex(1); 	// 16-bits
		
		dialog.spnBiplaneDX.set(77.11);
		dialog.spnBiplaneDY.set(17.07);
		dialog.spnBiplaneRotation.set(1.9);
		dialog.spnBiplaneScale.set(1);
		
		dialog.chkNoises[0].setSelected(true);
		dialog.chkNoises[1].setSelected(true);
		dialog.chkNoises[2].setSelected(true);
		dialog.chkNoises[3].setSelected(true);
		dialog.chkNoises[4].setSelected(false);
		dialog.chkNoises[5].setSelected(false);
		dialog.spnNoises[0].set(0.5);
		dialog.spnNoises[1].set(0.5);
		dialog.spnNoises[2].set(0.5);
		dialog.spnNoises[3].set(1.41);
		dialog.run();
		IJ.log("End Batch " + ((System.nanoTime() - chrono)*1e-9));
	}

}
