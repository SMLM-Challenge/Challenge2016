package smlms.plugins;

import java.io.File;
import java.util.ArrayList;

import smlms.file.Fluorophore;
import smlms.file.Fluorophores;
import smlms.simulation.CameraModule;
import smlms.simulation.PSFModule;
import smlms.simulation.SequenceFactory;
import smlms.simulation.Viewport;
import smlms.tools.Point3D;

public class Generate_Beads {

	double pixelsize = 100;
	
	public Generate_Beads() {
		
		String psf = "/Users/sage/Desktop/beads/PSF/2D-Exp.tif";
		String path = "/Users/sage/Desktop/beads/PSF/2D-Exp/";
		int fmax = 151;
		Fluorophores[] fluos = new Fluorophores[fmax];
		for(int i=0; i <fmax; i++) {
			fluos[i] = new Fluorophores();
			Fluorophore fluo = new Fluorophore(i, 1000, 1000, 10*i-750, i+1, 1000000000.0);
			fluos[i].add(fluo);
		}
		generate(path, psf, fluos, fmax);
	}
	
	private void generate(String path, String psf, Fluorophores[] fluos, int fmax) {
		int fmin = 0;
		int upC = 100;
		int upW = 1;
		double fwhm = 250;
		new File(path).mkdir();
		CameraModule 	camera 	= createCameraModule();
		Viewport vwPSF = createViewport(pixelsize/(upC * upW));	
		PSFModule module = new PSFModule(psf, vwPSF);
		if (psf.startsWith("BP500")) {
			module.biplaneAffine 	= true;
			module.affineRotation 	= 2;
			module.affineScale 		= 1;
			module.affineDX			= 120;
			module.affineDY			= 70;
		}
		ArrayList<PSFModule> psfs = new ArrayList<PSFModule>();
		psfs.add(module);
		String dataset = "Beads in " + psf;
		SequenceFactory sequencer = new SequenceFactory(dataset, fmin, fmax, 1, camera, psfs, null, null, createViewport(pixelsize), upW, upC);
		sequencer.generate(path, 2, fluos, fwhm, 0, true, true, null);
	}
	
	protected Viewport createViewport(double pixelsize) {
		Point3D origin = new Point3D(0, 0, 0);
		double fovNano = 64 * pixelsize;
		double thickness = 1500;
		return new Viewport(origin, fovNano, fovNano, thickness, pixelsize);
	}
	
	protected CameraModule createCameraModule() {
		double saturation	= 65535;
		String format 		= "TIFF 32-bits";
		String  quantiz 	= CameraModule.quantizationNames[5];
		return new CameraModule(quantiz, saturation, format);
	}

}
