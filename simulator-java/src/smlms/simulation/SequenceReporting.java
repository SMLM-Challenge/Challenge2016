package smlms.simulation;

import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.io.Opener;
import imageware.ImageWare;

import java.io.File;
import java.util.ArrayList;

import smlms.tools.Tools;
import smlms.tools.Zip;

public class SequenceReporting {

	private SequenceFactoryDialog dialog;
	
	public SequenceReporting(SequenceFactoryDialog dialog) {
		this.dialog = dialog;
	}
	
	public void reportWeb(String dataset, String pathMain, String pathOracle, String pathSequence, PSFModule psf) {
		
		(new File(pathOracle)).mkdir();
		String parent = new File(pathMain).getParent() + File.separator;
		Tools.copyFile(parent + "index.html", pathOracle + "index.html");	
		//Zip.zipFolder(pathPSF, pathOracle + "one-bead-100nm-" + psf.getName() + "-10x10x10-as-list.zip");
		reportParameters(pathOracle);
		reportExcerpt(pathSequence, pathOracle);
		reportProjection(pathOracle);
		reportPSF(pathOracle, psf);
		double c = dialog.spnOversamplingConvolve.get() * dialog.spnOversamplingWorking.get();
		double pixelsize =  (dialog.spnPixelsizeCamera.get()/c);
		ImageWare psfim = psf.test(100, pixelsize, dialog.spnFocalPlaneTI.get());	

		String pathPSF = pathMain + File.separator + "psf" + File.separator ;
		new File(pathPSF).mkdir();
		
		psf.storeIllustration(psfim, pathOracle, 0.5);
		psf.storeSlices(psfim, pathPSF);
		
		String pathData = parent + "Data" + File.separator;
		(new File(pathData)).mkdir();
		
		Zip.zipFolder(pathSequence, pathData + "sequence-" + dataset + "-" + psf.getName() + "-as-list.zip");

		String pathStack = pathMain + "sequence-as-stack" + File.separator;	
		
		String stack = pathData + "sequence-" + dataset + "-" + psf.getName() + "-as-stack.zip";
		IJ.log("ZIP " + pathData + " to " + stack);
		Zip.zipFolder(pathStack, stack);
	}
	
	/**
	 * Report all elements.
	 */
	protected void reportAll(String path) {
		(new File(path)).mkdir();
		try {
			String params	= Tools.readFile(path + "sequence-parameters.html");
			String exceprt	= Tools.readFile(path + "sequence-excerpt.html");
			String accuracy	= Tools.readFile(path + "sequence-accuracy.html");	
			ReportHTML out = new ReportHTML(path, "sequence.html");
			out.print("\n<table cellpadding=0 style=\"border:0px solid #FFFFFF\"><tr><td valign=top  style=\"width:480px\">\n");
			out.print(params);
			out.print(accuracy);
			out.print("\n</td><td style=\"width:20px\"></td><td valign=top  style=\"width:480px\">");
			out.print(exceprt);
			out.print("\n</td></tr></table>\n");
			out.close();
			ReportHTML outc = new ReportHTML(path, "acquisition_parameters.html");
			reportChallenge(outc, "");
			outc.close();
		}
		catch(Exception ex) {
			IJ.error("" + ex);
		}
	}

	/**
	 * Report Parameters
	 */
	protected void reportParameters(String path) {
		int first = dialog.spnFirstFrame.get();
		int last = dialog.spnLastFrame.get();
		int interval = dialog.spnIntervalFrame.get();
		String frames = "From " + first + " to " + last;
		double pxc = dialog.spnPixelsizeCamera.get();
		(new File(path)).mkdir();
		try {
			ReportHTML out = new ReportHTML(path, "sequence-parameters.html");
			out.printSection("Parameters");
			out.print("<table class=\"report_table\" style=\"width:480px\">");			
			out.printHeader("Camera", 3);
			out.printParam("Photon converter factor or Quantum efficiency (QE)", dialog.spnQuantumEfficiency.get(), 2, "e<sup>-</sup>/Ph.");
			out.printParam("Resolution", dialog.spnCameraResolution.get(), 0, "pixels");
			out.printParam("Pixelsize", dialog.spnPixelsizeCamera.get(), 2, "nm");
			out.printParam("Field of view", dialog.spnCameraResolution.get() * dialog.spnPixelsizeCamera.get(), 2, "nm");
	
			out.printHeader("Optics", 3);
			out.printParam("Wavelength", dialog.spnWavelength.get(), 2, "nm");
			out.printParam("Numerical aperture (NA)", dialog.spnNA.get(), 2, "");

			out.printHeader("Autofluorescence", 3);
			out.printParam("Background level (Gain)", dialog.spnAutofluoOffsetMean.get(), 2, "");
			out.printParam("Background level (Poisson distribution)", dialog.spnAutofluoOffsetStdv.get(), 2, "");
			if (dialog.cmbAutofluoSources.getSelectedIndex() != AutofluorescenceModule.NONE) {
				out.printParam("Location of the sources", (String) dialog.cmbAutofluoSources.getSelectedItem(), "");
				out.printParam("Number of sources", dialog.spnAutofluoNbSources.get(), 0, "");
				out.printParam("Gain of sources", dialog.spnAutofluoGain.get(), 0, "");
				out.printParam("Size of sources", dialog.spnAutofluoSize.get(), 0, "nm");
				out.printParam("Percentage of change", dialog.spnAutofluoChange.get(), 0, "%");
			}

			out.printHeader("Camera Noise", 3);
			out.printParam("Distribution: " + NoiseModule.distribution[0], "" + dialog.spnNoises[0].get(), "");
			out.printParam("Distribution: " + NoiseModule.distribution[3], "" + dialog.spnNoises[3].get(), "");
			out.printParam("Distribution: " + NoiseModule.distribution[4], "" + dialog.spnNoises[4].get(), "");
			int c = dialog.spnOversamplingConvolve.get();
			int a = dialog.spnOversamplingWorking.get();
			
			out.printHeader("Analog Digital Conversion", 3);
			out.printParam("Electron conversion e<sup>-</sup> per ADU", dialog.spnCameraGain.get(), 2, "DN/e<sup>-</sup>");
			out.printParam("Baseline", dialog.spnBaseline.get(), 2, "DN");
			out.printParam("Saturation", dialog.spnCameraSaturation.get(), 2, "DN");
			out.printParam("Quantization", (String) dialog.cmbCameraQuantization.getSelectedItem(), "");
			
			out.printHeader("Computational Parameters", 3);
			out.printParam("Thickness", dialog.spnThickness.get(), 2, "nm");
			out.printParam("Frames (interval:" + interval +")", frames, "");
			out.printParam("Multithreading", (String) dialog.cmbThreading.getSelectedItem(), "");
			out.printParam("Pixelsize to PSF convolution", (pxc / (c*a)), 1, "nm");
			out.printParam("Pixelsize for autofluorescence", (pxc / (a)), 1, "nm");
			out.printParam("Pixelsize of the camera", dialog.spnPixelsizeCamera.get(), 1, "nm");
			out.printParam("File format", (String) dialog.cmbCameraFileFormat.getSelectedItem(), "");

			out.print("</table>");
			out.close();
		}
		catch (Exception e) {
			IJ.error("" + e);
		}
	}

	private void reportChallenge(ReportHTML out, String title) {
		out.print("\n<table cellpadding=0 style=\"border:0px solid #FFFFFF\"><tr><td valign=top>\n");
		out.printSection("Acquisition Parameters");

		// Left column
		out.print("<table class=\"report_table\" style=\"width:480px\"cellpadding=3>\n");			
	
		out.printHeader("Camera", 3);
		out.printParam("Photon converter factor or Quantum efficiency (QE)", dialog.spnQuantumEfficiency.get(), 2, "e<sup>-</sup>/Ph.");
		out.print("<td valign=\"top\" class=\"report_param\" colspan=3><i>QE = QE-Gain x QE-interacting [<a href=\"#ref1\">Ref. 1</a>]</i></td>\n");
		out.printParam("Resolution", dialog.spnCameraResolution.get(), 0, "pixels");
		out.printParam("Pixelsize", dialog.spnPixelsizeCamera.get(), 2, "nm");
		out.printParam("Field of view", dialog.spnCameraResolution.get() * dialog.spnPixelsizeCamera.get(), 2, "nm");

		out.printHeader("Optics", 3);
		out.printParam("Wavelength", dialog.spnWavelength.get(), 2, "nm");
		out.printParam("Numerical aperture (NA)", dialog.spnNA.get(), 2, "");

		out.printHeader("Analog Digital Conversion", 3);
		out.printParam("Electron conversion - Gain", dialog.spnCameraGain.get(), 2, "DN/e<sup>-</sup>");
		out.printParam("Electron conversion - Offset", dialog.spnCameraOffset.get(), 2, "DN");
		out.printParam("Baseline", dialog.spnBaseline.get(), 2, "DN");
		out.printParam("Saturation", dialog.spnCameraSaturation.get(), 2, "DN");
		out.printParam("Quantization", (String) dialog.cmbCameraQuantization.getSelectedItem(), "");
		
		out.print("</table>");
		out.print("<a name=\"ref1\"></a><p><?php include '../../html/reference_qe.html' ?></p>");
		
		out.print("\n</td><td width=\"20px\"></td><td valign=\"top\">\n");
		
		// Right column
		out.print("\n<table class=\"report_table\" style=\"width:480px\">\n");
		out.printSection("Point-Spread Function (PSF)");
		out.printHeader("Physical Model", 3);
		out.printParam("Wavelength", dialog.spnWavelength.get(), 2, "nm");
		out.printParam("Numerical aperture (NA)", dialog.spnNA.get(), 2, "");
		out.printParam("Offset focal plane ", dialog.getFocalPlane(), 0, "nm");
		if (dialog.tabPSF.getSelectedIndex() == 1) {
			out.printParam("XY function", "<b>" + (String) dialog.cmbPSFModelXY.getSelectedItem() + "</b>", "");
			out.printParam("Z function", (String) dialog.cmbPSFModelZ.getSelectedItem(), "");
			out.printParam("Focal plane", dialog.getFocalPlaneDescription(), "");
			out.printParam("Defocus plane", dialog.getDefocusPlaneDescription(), "");
		}
		if (dialog.tabPSF.getSelectedIndex() == 2) {
			out.printParam("Model", "<b>" + PSFModule.namesXYZ[0] + "</b>", "");
			out.printParam("Refractive index sample", dialog.spnNS.get(), 2, "");
			out.printParam("Refractive index immersion", dialog.spnNI.get(), 2, "");
			out.printParam("Offest working distance", dialog.spnFocalPlaneTI.get(), 2, "nm");
		}
		out.print("</td><tr></table>\n");
		out.print("</td></tr></table>\n");
	}

	private String[] getDirectoryList(String path) {
		File dir = new File(path);
		if (dir != null)
		if (dir.exists())
		if (dir.isDirectory()) {
			return dir.list();
		}
		return new String []{""};
	}

	protected void reportPSF(String path) {
		ArrayList<PSFModule> psfModules = dialog.createPSFModule();
		for(PSFModule psfModule : psfModules) {
			reportPSF(path + File.separator + psfModule.getName() + File.separator, psfModule);
		}
	}
	
	/**
	 * Report PSF.
	 * 
	 * Show some frames of the sequence.
	 */
	private void reportPSF(String path, PSFModule psfModule) {
		(new File(path)).mkdir();
		try {
			double c = dialog.spnOversamplingConvolve.get() * dialog.spnOversamplingWorking.get();
			    
			ReportHTML out = new ReportHTML(path, "psf.html");
			out.printSection("PSF");
			out.print("<table><tr><td valign=\"top\">\n");
			
			// Left column
			out.print("\n<table class=\"report_table\" style=\"width:480px\">\n");
			out.printHeader("Physical Model", 3);
			out.printParam("Wavelength", dialog.spnWavelength.get(), 2, "nm");
			out.printParam("Numerical aperture (NA)", dialog.spnNA.get(), 2, "");
			out.printParam("Offset focal plane ", dialog.getFocalPlane(), 0, "nm");
			if (dialog.tabPSF.getSelectedIndex() == 1) {
				out.printParam("XY function", "<b>" + (String) dialog.cmbPSFModelXY.getSelectedItem() + "</b>", "");
				out.printParam("Z function", (String) dialog.cmbPSFModelZ.getSelectedItem(), "");
				out.printParam("Focal plane", dialog.getFocalPlaneDescription(), "");
				out.printParam("Defocus plane", dialog.getDefocusPlaneDescription(), "");
			}
			if (dialog.tabPSF.getSelectedIndex() == 2) {
				out.printParam("Model", "<b>" + PSFModule.namesXYZ[0] + "</b>", "");
				out.printParam("Refractive index sample", dialog.spnNS.get(), 2, "");
				out.printParam("Refractive index immersion", dialog.spnNI.get(), 2, "");
				out.printParam("Offset working distance", dialog.spnFocalPlaneTI.get(), 2, "nm");
			}		
			if (dialog.tabPSF.getSelectedIndex() == 3) {
				out.printParam("File", "<b>" +  psfModule.getName() + "</b>", "");
			}		
			if (dialog.tabPSF.getSelectedIndex() == 4) {
				out.printParam("Depth", dialog.spnBiplaneDepth.get(), 2, "");
				out.printParam("ni", dialog.spnBiplaneNI.get(), 2, "");
				out.printParam("ns", dialog.spnBiplaneNS.get(), 2, "");
				out.printParam("Delta Z 1", dialog.spnBiplaneDeltaZ1.get(), 2, "");
				out.printParam("Delta Z 2", dialog.spnBiplaneDeltaZ2.get(), 2, "");
				out.printParam("Orientation", (String)dialog.cmbBiplaneOrientation.getSelectedItem(), "Rows");
				out.printParam("spnBiplaneTI", dialog.spnBiplaneTI.get(), 2, "");
				out.printParam("spnBiplaneDX", dialog.spnBiplaneDX.get(), 2, "");
				out.printParam("spnBiplaneDY", dialog.spnBiplaneDY.get(), 2, "");
			}		
			out.printParam("Depth of the PSF", dialog.spnThickness.get(), 1, "nm");
			if (dialog.tabPSF.getSelectedIndex() == 2) {
				out.printParam("Oversampling in lateral", dialog.spnOversamplingLateral.get(), 0, "");
				out.printParam("Oversampling in axial", dialog.spnOversamplingAxial.get(), 0, "");
			}
			out.print("\n</table>");
			out.print("\n<p><span class=\"button toggleintro\" style=\"margin-top:50px;width:400px\">");
			out.print("\n<a href=\"one-bead-100nm-" + psfModule.getName() + "-10x10x10-as-list.zip\">");
			out.print("\none-bead-100nm-" + psfModule.getName() + "-10x10x10-as-list.zip</a><p>");
	
			out.print("\n</td><td width=\"20px\"></td><td valign=\"top\">\n");
			
			// Right column
			out.print("<table class=\"report_table\" style=\"width:480px\">\n");
			out.print("<img src=\"illustration.png\" style=\"width:480px; float:right;padding-left:10px\">\n");
			out.print("<h4>Orthogonal Section of the PSF</h4>\n");
			out.print("<p>Resolution: " + (dialog.spnPixelsizeCamera.get()/c) + " nm per voxel</p>\n");
			out.print("</tr><td></table></td></tr></table>\n");
			out.close();
		}
		catch(Exception e) {
			IJ.error("" + e);	
		}
	}
	/**
	 * Report Excerpt.
	 * 
	 * Show some frames of the sequence.
	 */
	protected void reportExcerpt(String pathSequence, String pathOracle) {
		int numberOfRandomFrames = 2;
		(new File(pathOracle)).mkdir();
		try {
			ReportHTML out = new ReportHTML(pathOracle, "sequence-excerpt.html");
			out.printSection("Excerpt");
			String frames[] = getDirectoryList(pathSequence);
			int n = frames.length;
			out.print("\n<table class=\"report_table\" style=\"width:480px\">");
			IJ.log("" +  " " + 0 + " " + frames[0]);
			reportFrame(pathSequence, pathOracle, out, "First Frame: ", frames[0]);
			for(int i=0; i<numberOfRandomFrames; i++) {
				int n1 = Math.max(0,  Math.min(n-1, (int)(Math.random() * (n-2)) + 1));
				reportFrame(pathSequence, pathOracle, out, "Random Frame: ", frames[n1]);
			}
			reportFrame(pathSequence, pathOracle, out, "Last Frame: ", frames[n-1]);
			out.print("</table>");
			out.close();
		}
		catch(Exception e) {
			IJ.error("" + e);	
		}
	}

	protected void reportFrame(String pathSequence, String pathOracle, ReportHTML out, String prefix, String frameExt) {	
		String pathimg = pathOracle + "images";
		(new File(pathimg)).mkdir();
		String frame = frameExt.substring(0, frameExt.length()-4);
		tif2jpeg(pathSequence, pathimg, frame);
		String imageHTML = "images/" + frame + ".jpg";
		out.print("<tr>");
		out.printHeader(prefix + frameExt, 1);
		out.print("</tr><tr><td>");
		out.print("<p><img src=\"" + imageHTML + "\"><p>\n");
		out.print("</tr>");
	}

	/**
	 * Report Accuracy
	 * 
	 * Accuracy is defined according the Thompson rules
	 */
	protected void reportAccuracy(String path) {	
		(new File(path)).mkdir();
		try {
			ReportHTML out = new ReportHTML(path, "sequence-accuracy.html");
			double res[] = dialog.testAccuracy();
			// return new double[] {N, a, s, b, accStats, accQuant, accBack, accThomson};
			out.printSection("Estimation of the Accuracy");
			out.print("<table class=\"report_table\" style=\"width:480px\">");
			out.printHeader("Accuracy defined by Thomson", 3);
			out.printParam("N, number of photons (max. of the frame)", res[0], 2, "");
			out.printParam("a, pixelsize of the camera", res[1], 2, "nm");
			out.printParam("s, FWHM of the PSF", res[2], 2, "nm");
			out.printParam("b, background (average of the frame)", res[3], 2, "");
			out.printParam("Accuracy term - Localization", res[4], 2, "nm");
			out.printParam("Accuracy term - Quantization", res[5], 2, "nm");
			out.printParam("Accuracy term - Background", res[6], 2, "nm");
			out.printParam("Accuracy Localization", res[7], 2, "nm");
			out.print("</table>");
			out.print("<p><?php include '../../html/reference_accuracy.html' ?></p>");
			out.close();
		}
		catch(Exception e) {
			IJ.error("" + e);	
		}
	}


	protected void reportProjection(String path) {
		(new File(path)).mkdir();
		try {
			ReportHTML out = new ReportHTML(path, "projection.html");
			String imageCMax = "max-camera-resolution";
			String imageCAvg = "avg-camera-resolution";
			String imageWMax = "max-high-resolution";
			String imageWAvg = "avg-high-resolution";
			double pxc = dialog.spnPixelsizeCamera.get();
			
			out.printSection("Time projection");
			out.print("<table><tr><td valign=top  style=\"width:480px\">\n");
				out.print("<table class=\"report_table\"><tr>");
				out.printTH("Average Intensity Projection");
				out.print("</tr><tr>");
				out.printTD(""+
				"<p>Camera Resolution: " + pxc + "nm/pixel (Original size)</p>\n" +
				"<p><img src=\"projection/" + imageCAvg + ".jpg\"></p>\n" + 
				"<p>Download the original <a href=\"projection/" + imageCAvg + ".tif\">image</a><p>\n" + 
				"<p>&nbsp;</p><p>High Resolution: " + (pxc / dialog.spnOversamplingConvolve.get()) + "nm/pixel <a href=\"projection/" + imageWAvg + ".jpg\"> (Enlarge this image)</a></p>\n" +
				"<p><a href=\"projection/" + imageWAvg + ".jpg\"><img src=\"projection/" + imageWAvg + ".jpg\" style=\"width:476px;height:475px\"></a></p>\n"+
				"<p>Download the original <a href=\"projection/" + imageWAvg + ".tif\">image</a><p>\n");
				out.print("</tr></table>");
				out.print("\n</td><td style=\"width:20px\"></td><td valign=top  style=\"width:480px\">");
				out.print("<table class=\"report_table\"><tr>");
				out.printTH("Maximum Intensity Projection");
				out.print("</tr><tr>");
				out.printTD(""+
				"<p>Camera Resolution: " + dialog.spnPixelsizeCamera.get() + "nm/pixel (Original size)</p>\n" +
				"<p><img src=\"projection/" + imageCMax + ".jpg\"></p>\n" + 
				"<p>Download the original <a href=\"projection/" + imageCMax + ".tif\">image</a><p>\n" + 
				"<p>&nbsp;</p><p>High Resolution: " + (pxc / dialog.spnOversamplingConvolve.get()) + "nm/pixel<a href=\"projection/" + imageWMax + ".jpg\"> (Enlarge this image)</a></p>\n" +
				"<p><a href=\"projection/" + imageWMax + ".jpg\"><img src=\"projection/" + imageWMax + ".jpg\" style=\"width:476px;height:475px\"></a></p>\n"+
				"<p>Download the original <a href=\"projection/" + imageWMax + ".tif\">image</a><p>\n");
				out.print("</tr></table>");

			out.print("</td></tr></table>\n");
			out.close();
		}
		catch(Exception ex) {
				IJ.error("reportProjection:" + ex + "(" + path + ")");
		}
	}
	
	private void tif2jpeg(String srcpath, String dstpath, String name) {
		String jpgname = dstpath + File.separator + name + ".jpg";
		String tifname = srcpath + File.separator + name + ".tif";
		Opener opener = new Opener();
		ImagePlus imp = opener.openImage(tifname);
		if (imp == null) {
			IJ.error(" tif2jpeg : File not found " + tifname);
			return;
		}
		(new FileSaver(imp)).saveAsJpeg(jpgname);
	}
	

}
