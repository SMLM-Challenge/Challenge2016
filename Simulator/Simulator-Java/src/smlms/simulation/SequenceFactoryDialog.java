package smlms.simulation;

import ij.IJ;
import ij.gui.GUI;
import imageware.ImageWare;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import smlms.file.FluorophoreComponent;
import smlms.file.Fluorophores;
import smlms.simulation.defocussed2dfunction.ZFunction;
import smlms.simulation.gl.PSFParameters;
import smlms.tools.Chrono;
import smlms.tools.Point3D;
import smlms.tools.PsRandom;
import smlms.tools.Tools;
import smlms.tools.Verbose;
import additionaluserinterface.GridPanel;
import additionaluserinterface.GridToolbar;
import additionaluserinterface.Settings;
import additionaluserinterface.SpinnerDouble;
import additionaluserinterface.SpinnerInteger;
import additionaluserinterface.WalkBar;
import bpalm.simulator.BPALMParameters;

public class SequenceFactoryDialog extends JDialog implements ActionListener, WindowListener, ChangeListener, Runnable {

	protected WalkBar walk				= new WalkBar("(c) 2010 EPFL, BIG", false, false, true);
	protected Settings settings			= new Settings("localization-microscopy", IJ.getDirectory("plugins") + "localization-microscopy.txt");
	protected Thread thread				= null;
	protected JButton bnRun				= new JButton("Run");
	private JButton bnTestAutofluo		= new JButton("Test");
	private JButton bnTestNoise			= new JButton("Test");
	private JButton bnTestBeadPSF		= new JButton("Generate PSF");
	private JButton bnSavePSF			= new JButton("Save");
	private JButton bnTestAccuracy		= new JButton("Test");
	private JButton bnReportParameters	= new JButton("Parameters");
	private JButton bnReportAccuracy	= new JButton("Accuracy");
	private JButton bnReportPSF			= new JButton("PSF");
	private JButton bnReportAll			= new JButton("All");
	
	private JButton bnSaveParams		= new JButton("Store");
	private JButton bnLoadParams		= new JButton("Load");
	private JButton	bnBrowsePSF[]		= new JButton[] {new JButton("Browse"), new JButton("Browse"), new JButton("Browse")};
 	private JButton	bnLoadPSF[]			= new JButton[] {new JButton("Load"), new JButton("Load"), new JButton("Load")};

	protected JButton job				= bnRun;
	
	protected SpinnerDouble		spnFluoPixelsize		= new SpinnerDouble(15, 0, 100000, 1);
	protected JCheckBox 		chkFluoAllFrames		= new JCheckBox("All Frames", true);
	public	 JComboBox			cmbThreading			= new JComboBox(SequenceFactory.names);
	
	protected SpinnerInteger	spnSeedRandom			= new SpinnerInteger(123, 0, 10000, 1);
	protected SpinnerDouble 	spnFluoAmplitude 		= new SpinnerDouble(2000, 0, 10000000, 100);

//	protected SpinnerInteger 	spnPixelsizeStorePSF	= new SpinnerInteger(10, 1, 100, 1);
	protected SpinnerInteger 	spnOversamplingConvolve = new SpinnerInteger(2, 1, 100, 1);
	protected SpinnerInteger	spnOversamplingWorking	= new SpinnerInteger(2, 1, 100, 1);
	
	protected SpinnerDouble		spnQuantumEfficiency	= new SpinnerDouble(2, 0, 10000000, 1);
	protected SpinnerDouble		spnCameraGain			= new SpinnerDouble(2, 0, 10000000, 1);
	protected SpinnerDouble		spnCameraOffset			= new SpinnerDouble(2, -100000, 10000000, 1);

	protected SpinnerInteger	spnCameraResolution		= new SpinnerInteger(256, 0, 100000, 1);
	protected SpinnerDouble 	spnPixelsizeCamera		= new SpinnerDouble(150, 0, 100000, 1);
	protected JComboBox 		cmbCameraQuantization 	= new JComboBox(CameraModule.quantizationNames);
	protected SpinnerDouble		spnCameraSaturation		= new SpinnerDouble(100000, -10000000, 10000000, 1);
	protected JComboBox 		cmbCameraFileFormat		= new JComboBox(CameraModule.names);
		
	protected SpinnerDouble		spnBaseline				= new SpinnerDouble(0, -10000000, 10000000, 1);
	protected JCheckBox 		chkNoises[] 			= new JCheckBox[NoiseModule.names.length];
	protected SpinnerDouble 	spnNoises[]				= new SpinnerDouble[NoiseModule.names.length];
		
	protected JComboBox 		cmbVerbose				= new JComboBox(Verbose.names);	

	protected JComboBox 		cmbAutofluoMode			= new JComboBox(AutofluorescenceModule.evolutions);	
	protected SpinnerDouble 	spnAutofluoDefocus		= new SpinnerDouble(1000, 0, 100000, 100);
	protected SpinnerDouble 	spnAutofluoChange 		= new SpinnerDouble(100, 0, 100, 100);
	protected SpinnerDouble 	spnAutofluoDiffusion	= new SpinnerDouble(100, 0, 100000, 100);
	protected SpinnerDouble 	spnAutofluoDispl		= new SpinnerDouble(100, 0, 100000, 100);
	protected JComboBox 		cmbAutofluoSources		= new JComboBox(AutofluorescenceModule.names);	
	protected SpinnerDouble 	spnAutofluoGain			= new SpinnerDouble(100, -100000, 100000, 100);
	protected SpinnerDouble 	spnAutofluoOffsetMean	= new SpinnerDouble(100, -100000, 100000, 100);
	protected SpinnerDouble 	spnAutofluoOffsetStdv	= new SpinnerDouble(0, 0, 100000, 100);
	protected SpinnerInteger 	spnAutofluoNbSources	= new SpinnerInteger(20, 0, 100000, 1);
	protected SpinnerInteger 	spnAutofluoNbScale		= new SpinnerInteger(20, 0, 100000, 1);
	protected SpinnerDouble 	spnAutofluoSize			= new SpinnerDouble(100, 100, 100000, 100);

	protected JCheckBox 		chkStats				= new JCheckBox("Stats");
	protected JCheckBox 		chkProjection			= new JCheckBox("Projection");
	protected JCheckBox 		chkReport				= new JCheckBox("Report");
	protected JComboBox 		cmbMode					= new JComboBox(SequenceFactory.modes);
	
	protected SpinnerInteger 	spnNbFluorophores		= new SpinnerInteger(10000, 1, 10000000, 10000);
	protected SpinnerInteger 	spnFirstFrame			= new SpinnerInteger(1, 1, 999999, 1);
	protected SpinnerInteger 	spnLastFrame	 		= new SpinnerInteger(10, 1, 999999, 1);
	protected SpinnerInteger 	spnIntervalFrame 		= new SpinnerInteger(10, 1, 1000, 1);

	protected SpinnerDouble 	spnBiplaneDepth			= new SpinnerDouble(0, -9999, 9999, 100);
	protected SpinnerDouble 	spnBiplaneDeltaZ1		= new SpinnerDouble(0, -9999, 9999, 1);
	protected SpinnerDouble 	spnBiplaneDeltaZ2		= new SpinnerDouble(100, -9999, 9999, 100);
	protected SpinnerDouble 	spnBiplaneNI			= new SpinnerDouble(1, 0, 9999, 0.1);
	protected SpinnerDouble 	spnBiplaneNS			= new SpinnerDouble(1, 0, 9999, 0.1);
	protected SpinnerDouble 	spnBiplaneTI			= new SpinnerDouble(150, 0, 9999, 0.1);
	protected SpinnerDouble 	spnBiplaneDX			= new SpinnerDouble(100, 0, 9999, 0.1);
	protected SpinnerDouble 	spnBiplaneDY			= new SpinnerDouble(100, 0, 9999, 0.1);
	protected SpinnerDouble 	spnBiplaneRotation		= new SpinnerDouble(0, -9999, 9999, 0.1);
	protected SpinnerDouble 	spnBiplaneScale			= new SpinnerDouble(1, -9999, 9999, 0.1);
	
	protected JComboBox 		cmbBiplaneOrientation	= new JComboBox(new String[] {"Rows", "Columns", "One channel"});

	protected SpinnerDouble 	spnWavelength	 		= new SpinnerDouble(500, 200, 10000, 10);
	protected SpinnerDouble 	spnNS	 				= new SpinnerDouble(1, 0.1, 100, 0.1);
	protected SpinnerDouble 	spnNI	 				= new SpinnerDouble(1, 0.1, 100, 0.1);
	protected SpinnerDouble 	spnNA	 				= new SpinnerDouble(1, 0.1, 100, 0.1);
	protected SpinnerDouble		spnFWHMFactor			= new SpinnerDouble(1, 0, 100, 1);
	protected SpinnerInteger	spnOversamplingLateral	= new SpinnerInteger(1, 1, 1000, 1);
	protected SpinnerInteger	spnOversamplingAxial	= new SpinnerInteger(1, 1, 1000, 1);
	protected SpinnerDouble		spnDeltaZ				= new SpinnerDouble(20, -10000, 10000, 1);
	protected SpinnerDouble		spnBeadSize				= new SpinnerDouble(0, 0, 10000, 10);

	protected SpinnerDouble 	spnThickness			= new SpinnerDouble(500, 0, 100000, 10);
	
	protected SpinnerDouble 	spnFocalPlaneTI			= new SpinnerDouble(500, -100000, 100000, 10);
	protected SpinnerDouble 	spnDefocusPlane			= new SpinnerDouble(1000, -100000, 100000, 10);

	protected JComboBox 		cmbPSFModelXY			= new JComboBox(PSFModule.namesXY);	
	protected JComboBox 		cmbPSFModelXYZ			= new JComboBox(PSFModule.namesXYZ);	
	protected JComboBox 		cmbPSFModelZ			= new JComboBox(ZFunction.names);
	
	private JLabel 				lblAccuracy_stats		= new JLabel("FWHM");
	private JLabel 				lblAccuracy_quant		= new JLabel("Pixelsize");
	private JLabel 				lblAccuracy_back		= new JLabel("Background");
	private JLabel 				lblAccuracy_N			= new JLabel("Nb photons");
	private JLabel 				lblAccuracy_Thomson		= new JLabel("Accuracy");
	
	protected JLabel 			lblDiffractionLimit		= new JLabel("000 nm");
	protected JLabel 			lblFoV					= new JLabel("Field of view x Depth");
	protected JLabel 			lblFWHM					= new JLabel("500 nm");
	protected JLabel 			lblSummaryPlane			= new JLabel("Double FWHM");
	protected JLabel 			lblOversampling 		= new JLabel("Oversampling");
 	protected JTextField		txtPSFFile[]			= new JTextField[] 
 			{new JTextField("-", 20), new JTextField("-", 20),  new JTextField("-", 20), new JTextField("-", 20), new JTextField("-", 20), new JTextField("-", 20), new JTextField("-", 20)};
 	protected JLabel 			lblPSFFile[]			= new JLabel[] 	
 			{new JLabel(), new JLabel(), new JLabel(), new JLabel(), new JLabel(), new JLabel(), new JLabel()};
	
 	protected JTabbedPane 		tabPSF 					= new JTabbedPane();
 	protected JLabel 			lblConvolveInfo			= new JLabel("------------------");
	protected PsRandom 			psrand					= new PsRandom();
	public String 				path;

	protected FluorophoreComponent 	panel					= new FluorophoreComponent(settings, "Activations");
	
	public SequenceFactoryDialog() {
		super(new Frame(), "Sequence Factory");
		for(int i=0; i<NoiseModule.names.length; i++) {
			chkNoises[i] = new JCheckBox(NoiseModule.names[i]);
			spnNoises[i] = new SpinnerDouble(0, -100000, 10000000, 100);	
		}
		loadSettings();
		
		int tabpsf = settings.loadValue("tabpsf", 1);

			// Camera
	 	lblFoV.setBorder(BorderFactory.createEtchedBorder());
	 	lblFoV.setBackground(new Color(150, 150, 192, 10));
		GridToolbar pnCamera = new GridToolbar("Camera");
		pnCamera.place(0, 0, new JLabel("Quantum Efficiency"));
		pnCamera.place(0, 1, spnQuantumEfficiency);
		pnCamera.place(0, 2, new JLabel("<html>Ph. to e<sup>-</sup>"));
		pnCamera.place(2, 0, new JLabel("Thickness"));
		pnCamera.place(2, 1, spnThickness);
		pnCamera.place(2, 2, new JLabel("nm"));
		pnCamera.place(3, 0, new JLabel("Resolution"));
		pnCamera.place(3, 1, spnCameraResolution);
		pnCamera.place(3, 2, new JLabel("pixels"));
		pnCamera.place(4, 0, new JLabel("Pixelsize"));
		pnCamera.place(4, 1, spnPixelsizeCamera);
		pnCamera.place(4, 2, new JLabel("nm/pix"));
		pnCamera.place(5, 0, new JLabel("FoV / DoF"));
		pnCamera.place(5, 1, lblFoV);
		pnCamera.place(5, 2, new JLabel("nm"));

		// Optic
	 	lblDiffractionLimit.setBorder(BorderFactory.createEtchedBorder());
	 	lblDiffractionLimit.setBackground(new Color(150, 150, 192, 10));
		GridToolbar pnOptics = new GridToolbar("Optics");
		pnOptics.place(1, 0, new JLabel("Wavelength"));
		pnOptics.place(1, 1, spnWavelength);
		pnOptics.place(1, 2, new JLabel("nm"));
		pnOptics.place(2, 0, new JLabel("Numerical Aperture (NA)"));
		pnOptics.place(2, 1, spnNA);
		pnOptics.place(2, 2, lblDiffractionLimit);
		pnOptics.place(4, 0, new JLabel("Focal Plane (TI)"));
		pnOptics.place(4, 1, spnFocalPlaneTI);
		pnOptics.place(4, 2, new JLabel("nm"));


		// Acquistion
		GridToolbar pnAcquisition = new GridToolbar(false);
		pnAcquisition.place(5, 0, pnCamera);		
		pnAcquisition.place(7, 0, pnOptics);

		// ADC
		GridToolbar pnConverter = new GridToolbar("Electron conversion");
		pnConverter.place(0, 0, 3, 1, new JLabel(""));
		pnConverter.place(1, 0, new JLabel("e- per ADU"));
		pnConverter.place(1, 1, spnCameraGain);
		pnConverter.place(1, 2, new JLabel("<html>e<sup>-</sup> to DN</html>"));
		pnConverter.place(3, 0, new JLabel("Offset"));
		pnConverter.place(3, 1, spnCameraOffset);
		pnConverter.place(3, 2, new JLabel("<html>DN</html>"));
		pnConverter.place(4, 0, new JLabel("Baseline"));
		pnConverter.place(4, 1, spnBaseline);
		pnConverter.place(4, 2, new JLabel("<html>DN</html>"));
		
		GridToolbar pnQuantization = new GridToolbar("Digitalization");
		pnQuantization.place(6, 0, new JLabel("Saturation"));
		pnQuantization.place(6, 1, spnCameraSaturation);
		pnQuantization.place(7, 0, new JLabel("Quantization"));
		pnQuantization.place(7, 1, 2, 1, cmbCameraQuantization);
		pnQuantization.place(8, 0, new JLabel("File Format"));
		pnQuantization.place(8, 1, cmbCameraFileFormat);

		GridToolbar pnADC = new GridToolbar(false);
		pnADC.place(5, 0, pnConverter);		
		pnADC.place(7, 0, pnQuantization);
		
		// FocalPlane
		GridPanel pnPSFSave = new GridPanel(false);
		pnPSFSave.place(5, 0, new JLabel("Size (nm)"));
		pnPSFSave.place(5, 1, spnBeadSize);
		pnPSFSave.place(5, 2, bnTestBeadPSF);
		
		//
		GridToolbar pnPSF0 = new GridToolbar(false);
		pnPSF0.place(1, 0, new JLabel("Dirac function"));
		pnPSF0.place(2, 0, new JLabel("Only a point source"));
		pnPSF0.place(3, 0, new JLabel("No axial dependency"));
				
		//
		GridToolbar pnPSF1 = new GridToolbar(false);
	 	lblFWHM.setBorder(BorderFactory.createEtchedBorder());
	 	lblFWHM.setBackground(new Color(150, 150, 192, 10));
	 	lblSummaryPlane.setBorder(BorderFactory.createEtchedBorder());
	 	lblSummaryPlane.setBackground(new Color(150, 150, 192, 10));
		pnPSF1.place(0, 0, new JLabel("XY Function"));
		pnPSF1.place(0, 1, 2, 1, cmbPSFModelXY);
		pnPSF1.place(1, 0, new JLabel("FWHM factor"));
		pnPSF1.place(1, 1, spnFWHMFactor);
		pnPSF1.place(2, 0, new JLabel("FWHM"));
		pnPSF1.place(2, 1, lblFWHM);
		pnPSF1.place(2, 2, new JLabel("nm"));
		pnPSF1.place(3, 0, new JLabel("Z Function"));
		pnPSF1.place(3, 1, 2, 1, cmbPSFModelZ);
		pnPSF1.place(4, 2, new JLabel("nm"));
		pnPSF1.place(5, 0, new JLabel("Defocus Plane"));
		pnPSF1.place(5, 1, spnDefocusPlane);
		pnPSF1.place(5, 2, new JLabel("nm"));
		pnPSF1.place(6, 0, 3, 1, lblSummaryPlane);
	
		//
		GridToolbar pnPSF2 = new GridToolbar(false);
		pnPSF2.place(0, 0, 4, 1, new JComboBox(PSFModule.namesXYZ));
		pnPSF2.place(1, 0, 4, 1, new JLabel("Refractive Index"));
		pnPSF2.place(2, 0, new JLabel("ns"));
		pnPSF2.place(2, 1, spnNS);
		pnPSF2.place(2, 2, new JLabel("ni"));
		pnPSF2.place(2, 3, spnNI);		
		pnPSF2.place(3, 0, 4, 1, new JLabel("Oversampling - Accurary vs speed"));
		pnPSF2.place(4, 0, new JLabel("Lateral"));
		pnPSF2.place(4, 1, spnOversamplingLateral);
		pnPSF2.place(4, 2, new JLabel("Axial"));
		pnPSF2.place(4, 3, spnOversamplingAxial);
		pnPSF2.place(5, 0, new JLabel("DeltaZ"));
		pnPSF2.place(5, 1, spnDeltaZ);

		GridPanel pnPSF3 = new GridPanel(false, 5);
		for(int i=0; i<3; i++) {
			lblPSFFile[i].setBorder(BorderFactory.createEtchedBorder());
			pnPSF3.place(0+i*2, 0, txtPSFFile[i]);
			pnPSF3.place(0+i*2, 1, bnBrowsePSF[i]);
			pnPSF3.place(1+i*2, 1, bnLoadPSF[i]);
			pnPSF3.place(1+i*2, 0, lblPSFFile[i]);
		}
		GridPanel pnPSF4 = new GridPanel(false, 5);
		pnPSF4.place(0, 0, 3, 1, cmbBiplaneOrientation);
		pnPSF4.place(1, 0, new JLabel("Delta Plane 1/2"));
		pnPSF4.place(1, 1, spnBiplaneDeltaZ1);
		pnPSF4.place(1, 2, spnBiplaneDeltaZ2);
		pnPSF4.place(3, 0, new JLabel("Depth / dist (ti)"));
		pnPSF4.place(3, 1, spnBiplaneDepth);
		pnPSF4.place(3, 2, spnBiplaneTI);
		pnPSF4.place(4, 0, new JLabel("Index ni/ns"));
		pnPSF4.place(4, 1, spnBiplaneNI);
		pnPSF4.place(4, 2, spnBiplaneNS);
		pnPSF4.place(5, 0, new JLabel("Trans  dx /dx"));
		pnPSF4.place(5, 1, spnBiplaneDX);
		pnPSF4.place(5, 2, spnBiplaneDY);
		pnPSF4.place(6, 0, new JLabel("Rotation/Scale"));
		pnPSF4.place(6, 1, spnBiplaneRotation);
		pnPSF4.place(6, 2, spnBiplaneScale);

		tabPSF.add("PSF Point", pnPSF0);
		tabPSF.add("PSF 2Dz", pnPSF1);
     	tabPSF.add("PSF G&L", pnPSF2);
    	tabPSF.add("PSF File", pnPSF3);
       	tabPSF.add("Biplane", pnPSF4);
  
		tabPSF.setSelectedIndex(tabpsf);
       
		GridToolbar pnPSF = new GridToolbar(false, 1);
		pnPSF.place(3, 0, pnPSFSave);		
		pnPSF.place(4, 0, tabPSF);

 		// Autofluo
		GridToolbar pnAutofluo1 = new GridToolbar("Background");
		pnAutofluo1.place(1, 0, new JLabel("Gain/Poisson"));
		pnAutofluo1.place(1, 1, spnAutofluoOffsetMean);
		pnAutofluo1.place(1, 2, spnAutofluoOffsetStdv);

		GridToolbar pnAutofluo2 = new GridToolbar("Sources");
		pnAutofluo2.place(0, 0, cmbAutofluoSources);
		pnAutofluo2.place(0, 1, new JLabel("Nb Sources", JLabel.RIGHT));
		pnAutofluo2.place(0, 2, spnAutofluoNbSources);
		
		pnAutofluo2.place(2, 0, new JLabel("Gain/Size (nm)"));
		pnAutofluo2.place(2, 1, spnAutofluoGain);
		pnAutofluo2.place(2, 2, spnAutofluoSize);
		
		pnAutofluo2.place(3, 0, new JLabel("Move/Diff (nm"));
		pnAutofluo2.place(3, 1, spnAutofluoDispl);
		pnAutofluo2.place(3, 2, spnAutofluoDiffusion);
		
		pnAutofluo2.place(4, 0, new JLabel("Defocus (nm)/Scale"));
		pnAutofluo2.place(4, 1, this.spnAutofluoDefocus);
		pnAutofluo2.place(4, 2, this.spnAutofluoNbScale);

		pnAutofluo2.place(5, 0, new JLabel("Dynamics"));
		pnAutofluo2.place(5, 1, this.spnAutofluoChange);
		pnAutofluo2.place(5, 2, new JLabel("% of change"));

		GridPanel pnAutofluo = new GridPanel(false);
		pnAutofluo.place(7, 0, new JLabel("Evolution"));
		pnAutofluo.place(7, 1, cmbAutofluoMode);
		pnAutofluo.place(7, 2, bnTestAutofluo);
		pnAutofluo.place(8, 0, 3, 1, pnAutofluo1);
		pnAutofluo.place(9, 0, 3, 1, pnAutofluo2);

		// Noise
		GridToolbar pnNoise = new GridToolbar(false);
		int row = 1;
		for(int i=0; i<NoiseModule.names.length; i++) {
			pnNoise.place(row, 0, chkNoises[i]);
			pnNoise.place(row, 1, spnNoises[i]);
			pnNoise.place(row, 3, new JLabel(NoiseModule.distribution[i]));
			row++;
		}
		pnNoise.place(row, 0, bnTestNoise);
 
        // Sequencer
	 	GridPanel pnSequencer = new GridPanel(false);
	 	lblConvolveInfo.setBorder(BorderFactory.createEtchedBorder());
	 	pnSequencer.place(1, 0, 2, 1, lblConvolveInfo);
	 	pnSequencer.place(1, 2, cmbMode);
	 	pnSequencer.place(4, 0, chkProjection);
	 	pnSequencer.place(4, 1, chkStats);
	 	pnSequencer.place(4, 2, chkReport);
	 	pnSequencer.place(5, 0, spnFirstFrame);
	 	pnSequencer.place(5, 1, spnLastFrame);
	 	pnSequencer.place(5, 2, spnIntervalFrame);
	 	pnSequencer.place(6, 0, cmbThreading);
	 	pnSequencer.place(6, 1, cmbVerbose);
	 	pnSequencer.place(6, 2, bnRun);

	 	// Computation
	 	GridPanel pnComputation = new GridPanel(false);
	 	lblOversampling.setBorder(BorderFactory.createEtchedBorder());
	 	pnComputation.place(0, 0, 2, 1, lblOversampling);
	 	pnComputation.place(0, 2, new JLabel("Random Seed"));
	 	pnComputation.place(0, 3, spnSeedRandom);
	 	pnComputation.place(1, 0, new JLabel("PSF Convolve"));
	 	pnComputation.place(1, 1, spnOversamplingConvolve);
	 	pnComputation.place(1, 2, new JLabel("Working"));
	 	pnComputation.place(1, 3, spnOversamplingWorking);
	
	 	// Report
	 	GridPanel pnReport = new GridPanel(false);
	 	pnReport.place(0, 0, bnReportAccuracy);
	 	pnReport.place(0, 2, bnReportPSF);
	 	pnReport.place(1, 0, bnReportParameters);
	 	pnReport.place(1, 1, bnReportAll);
	 	pnReport.place(5, 0, bnSaveParams);
	 	pnReport.place(5, 1, bnLoadParams);

	 	// Report
	 	GridPanel pnAccuracy = new GridPanel(false);
		lblAccuracy_stats.setBorder(BorderFactory.createEtchedBorder());
		lblAccuracy_quant.setBorder(BorderFactory.createEtchedBorder());
		lblAccuracy_back.setBorder(BorderFactory.createEtchedBorder());
		lblAccuracy_N.setBorder(BorderFactory.createEtchedBorder());
		lblAccuracy_Thomson.setBorder(BorderFactory.createEtchedBorder());
	 	pnAccuracy.place(0, 0, new JLabel("Stat"));
	 	pnAccuracy.place(0, 1, lblAccuracy_stats);
	 	pnAccuracy.place(0, 2, new JLabel("Quan"));
	 	pnAccuracy.place(0, 3, lblAccuracy_quant);
	 	pnAccuracy.place(1, 0, new JLabel("Back"));
	 	pnAccuracy.place(1, 1, lblAccuracy_back);
	 	pnAccuracy.place(1, 2, new JLabel("Npht"));
	 	pnAccuracy.place(1, 3, lblAccuracy_N);
	 	pnAccuracy.place(3, 0, 2, 1, new JLabel("Thomson Acc."));
	 	pnAccuracy.place(3, 2, 1, 1, lblAccuracy_Thomson);
	 	pnAccuracy.place(3, 3, 1, 1, bnTestAccuracy);
	 	
		JTabbedPane tab2 = new JTabbedPane();
		tab2.add("Camera", pnAcquisition);
		tab2.add("PSF", pnPSF);
		tab2.add("Autofluo.", pnAutofluo);
		tab2.add("Noise", pnNoise);
		tab2.add("ADC", pnADC);

		JTabbedPane tab3 = new JTabbedPane();
		tab3.add("Sequencer", pnSequencer);
		tab3.add("Compute", pnComputation);
		tab3.add("Accuracy", pnAccuracy);
		tab3.add("Report", pnReport);

		// Main
		GridPanel pnMain = new GridPanel(false, 1);	
		pnMain.place(0, 0, panel);
		pnMain.place(2, 0, tab2);
		pnMain.place(4, 0, tab3);
		pnMain.place(5, 0, walk);

		addWindowListener(this);
		spnNA.addChangeListener(this);
		spnWavelength.addChangeListener(this);
		
		spnThickness.addChangeListener(this);
		spnCameraResolution.addChangeListener(this);
		spnPixelsizeCamera.addChangeListener(this);
		spnFWHMFactor.addChangeListener(this);
		cmbCameraQuantization.addActionListener(this);
		cmbPSFModelXY.addActionListener(this);
		spnDefocusPlane.addChangeListener(this);
		spnFocalPlaneTI.addChangeListener(this);
		spnCameraResolution.addChangeListener(this);
	 	bnReportAccuracy.addActionListener(this);
	 	bnReportPSF.addActionListener(this);
	 	bnReportAll.addActionListener(this);
	 	bnReportParameters.addActionListener(this);
		walk.getButtonClose().addActionListener(this);
		bnTestAutofluo.addActionListener(this);
		bnTestNoise.addActionListener(this);
		bnTestBeadPSF.addActionListener(this);
		bnSavePSF.addActionListener(this);
		bnTestAccuracy.addActionListener(this);
		bnRun.addActionListener(this);
		bnSaveParams.addActionListener(this);
		bnLoadParams.addActionListener(this);
		for(int i=0; i<bnLoadPSF.length; i++)
			bnLoadPSF[i].addActionListener(this);
		for(int i=0; i<bnLoadPSF.length; i++)
			bnBrowsePSF[i].addActionListener(this);
		spnOversamplingConvolve.addChangeListener(this);
		spnOversamplingWorking.addChangeListener(this);
		tabPSF.addChangeListener(this);
		add(pnMain);

		pack();
		setResizable(true);
		GUI.center(this);
		setVisible(true);
		updateInterface();
	}

	public synchronized void actionPerformed(ActionEvent e) {
		Verbose.setLevel(cmbVerbose.getSelectedIndex());
		
		if (e.getSource() == walk.getButtonClose()) {
			settings.storeValue("tabpsf", tabPSF.getSelectedIndex());
			settings.storeRecordedItems();
			dispose();
			return;
		}

		for(int i=0; i<bnBrowsePSF.length; i++)
			if (e.getSource() == bnBrowsePSF[i]) {
				JFileChooser fc = new JFileChooser();
				if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
					txtPSFFile[i].setText(fc.getSelectedFile().getAbsolutePath());
			}
		
		for(int i=0; i<bnLoadPSF.length; i++)
			if (e.getSource() == bnLoadPSF[i]) {
				ArrayList<PSFModule> psfs = createPSFModule();
				if (psfs.get(i) != null)
					lblPSFFile[i].setText(psfs.get(i).getInfoArrayPSF());
			}
		
		if (e.getSource() == bnSavePSF)
			new SequenceReporting(this).reportPSF(path);

		if (e.getSource() == bnSaveParams)
			saveParams();
		
		if (e.getSource() == bnLoadParams)
			loadParams();
		
		if (e.getSource() == cmbPSFModelXY || e.getSource() == cmbCameraQuantization) {
			updateInterface();
			return;
		}

		if (e.getSource() == bnTestAccuracy)
			testAccuracy();
		
		if (e.getSource() == bnReportParameters)
			new SequenceReporting(this).reportParameters(path);
		
		if (e.getSource() == bnReportAccuracy)
			new SequenceReporting(this).reportAccuracy(path);

		if (e.getSource() == bnReportPSF)
			new SequenceReporting(this).reportPSF(path);

		if (e.getSource() == bnReportAll)
			new SequenceReporting(this).reportAll(path);

		job = null;
		if (e.getSource() instanceof JButton)
			job = (JButton)e.getSource();
			
		if (job != null) {
			if (thread == null) {
				thread = new Thread(this);
				thread.setPriority(Thread.MIN_PRIORITY);
				thread.start();
			}
		}
	}

	public void run() {
		psrand.setSeed(spnSeedRandom.get());
		double pxc = spnPixelsizeCamera.get();
		try {
 			// Output
			Chrono.reset(9);
			Viewport vwCamera = createViewport(pxc);

			if (job == bnTestAutofluo) {
				Fluorophores fluorophores[] = panel.getFluorophoresPerFrames();
				createAutofluorescenceModule().test(5, fluorophores[0]);
			}
			
			else if (job == bnTestNoise) 
				createNoiseModule().test(vwCamera);
			
			else if (job == bnTestBeadPSF) {
				ArrayList<PSFModule> psfs = createPSFModule();
				double c = spnOversamplingConvolve.get()*spnOversamplingWorking.get();
				double size = spnBeadSize.get();
				ImageWare psf = psfs.get(0).test(size, pxc/c, this.spnFocalPlaneTI.get());
				psf.show("PSF-" + size + "-" + psfs.get(0).toString());
			}

			else if (job == bnRun)  {
				int first 		= spnFirstFrame.get();
				int last 		= spnLastFrame.get();
				int interval 	= spnIntervalFrame.get();
				double fwhm 	= getDiffractionLimit() * spnFWHMFactor.get();
				int upC 		= spnOversamplingConvolve.get();
				int upW 		= spnOversamplingWorking.get();
				ArrayList<PSFModule> psfs 	= createPSFModule();
				CameraModule 	camera 	= createCameraModule();
				NoiseModule 	noise 	= createNoiseModule();
				AutofluorescenceModule 	autofluo = createAutofluorescenceModule();
				path = panel.txtFile.getText();
				IJ.log(" path " + path);
				IJ.log(" parent " + (new File(path).getParent()));
				String dataset = new File((new File(path).getParent())).getName();
				IJ.log(" dataset " + dataset);
				
				SequenceFactory sequencer = new SequenceFactory(dataset, first, last, interval, camera, psfs, noise, autofluo, createViewport(pxc), upC, upW);
				int multithread = cmbThreading.getSelectedIndex();
				int mode  = cmbMode.getSelectedIndex();
				SequenceReporting reporting = (chkReport.isSelected() ? new SequenceReporting(this) : null);
				Fluorophores fluorophores[] = panel.getFluorophoresPerFrames();
				sequencer.generate(path, multithread, fluorophores, fwhm, mode, chkProjection.isSelected(), chkStats.isSelected(), reporting);
			}
			
		}
		catch(Exception ex) {
			Verbose.exception(ex);
		}
		thread = null;
	}

	/**
	 * Create StructuralAutofluorescenceModule
	 */
	private AutofluorescenceModule createAutofluorescenceModule() {
		int upC 	= spnOversamplingConvolve.get();
		int upW 	= spnOversamplingWorking.get();
		Viewport viewport = createViewport(spnPixelsizeCamera.get() / (upC * upW));
		int mode 	= cmbAutofluoMode.getSelectedIndex();
		int type 	= cmbAutofluoSources.getSelectedIndex();
		int nbScale = spnAutofluoNbScale.get();
		int nbSources = spnAutofluoNbSources.get();
		double diffusion = spnAutofluoDiffusion.get();
		double displacement = spnAutofluoDispl.get();
		double defocus = spnAutofluoDefocus.get();
		double change = spnAutofluoChange.get();
		double gain = spnAutofluoGain.get();
		double size = spnAutofluoSize.get();
		AutofluorescenceModule autofluo = new AutofluorescenceModule(psrand, viewport, mode);
		autofluo.setBackground(spnAutofluoOffsetMean.get(), spnAutofluoOffsetStdv.get());
		autofluo.setSources(type, nbSources, nbScale, defocus, diffusion, displacement, size, change, gain);
		return autofluo;
	}
	
	/**
	 * Create NoiseModule
	 */
	protected NoiseModule createNoiseModule() {
		double gain		= spnCameraGain.get();
		double offset	= spnCameraOffset.get();
		double baseline	= spnBaseline.get();
		int n = chkNoises.length;
		boolean[] enable = new boolean[n];
		double[] param = new double[n];
		double qe = spnQuantumEfficiency.get();
		for(int i=0; i<n; i++) {
			enable[i] = chkNoises[i].isSelected();
			param[i] = spnNoises[i].get();
		}
		return new NoiseModule(psrand, gain, offset, baseline, qe, enable, param);
	}
	
	/**
	 * Create CameraModule
	 */
	protected CameraModule createCameraModule() {
		double saturation	= spnCameraSaturation.get();
		String format 		= (String)cmbCameraFileFormat.getSelectedItem();
		String  quantiz 	= (String)cmbCameraQuantization.getSelectedItem();
		return new CameraModule(quantiz, saturation, format);
	}
	
	/**
	 * Create PSFModule
	 */
	protected ArrayList<PSFModule> createPSFModule() {
		double pxc = spnPixelsizeCamera.get();
		double fwhm = getDiffractionLimit() * spnFWHMFactor.get();
		double c = spnOversamplingConvolve.get()*spnOversamplingWorking.get();

		Viewport vwPSF = createViewport(pxc/c);

		ArrayList<PSFModule> modules = new ArrayList<PSFModule>();
		int tab = tabPSF.getSelectedIndex();
		if (tab == 0) {
			modules.add(new PSFModule(fwhm, vwPSF));
		}
		else if (tab == 1) {
			double zfocal 			= spnFocalPlaneTI.get();
			double zdefocus			= spnDefocusPlane.get();
			int zfunc 				= cmbPSFModelZ.getSelectedIndex();		
			ZFunction zfunction = new ZFunction(zfunc,  zdefocus, zfocal);
			modules.add(new PSFModule(fwhm, vwPSF, zfunction, cmbPSFModelXY.getSelectedIndex()));
		}
		else if (tab == 2) {
			PSFParameters paramGL = new PSFParameters();
			paramGL.ni = spnNI.get();
			paramGL.ns = spnNS.get();
			paramGL.delta_ti = -spnFocalPlaneTI.get() * 1E-9;		// Focal Axial
			paramGL.delta_z = spnDeltaZ.get() * 1E-9;
			paramGL.lambda = spnWavelength.get() * 1E-9;
			paramGL.NA = spnNA.get();		
			paramGL.pixelSize = spnOversamplingConvolve.get() * pxc * 1E-9; 
			paramGL.oversamplingAxial = spnOversamplingAxial.get();
			paramGL.oversamplingLateral = spnOversamplingLateral.get();
			paramGL.calculateConstants();
			modules.add(new PSFModule(fwhm, vwPSF, paramGL));
		}
		else if (tab == 3) {
			for(int i=0; i<txtPSFFile.length; i++) {
				if (new File(txtPSFFile[i].getText()).exists()) {
					String filename = txtPSFFile[i].getText();
					PSFModule module = new PSFModule(txtPSFFile[i].getText(), vwPSF);
					module.biplaneAffine 	= filename.contains("BP500");
					module.affineRotation 	= spnBiplaneRotation.get();
					module.affineScale 		= spnBiplaneScale.get();
					module.affineDX			= spnBiplaneDX.get();
					module.affineDY			= spnBiplaneDY.get();
					modules.add(module);
				}
			}
		}
		else {
			double px = spnPixelsizeCamera.get()*1e-9;
			BPALMParameters p1 = new BPALMParameters();

			p1.doSplit 	= cmbBiplaneOrientation.getSelectedIndex() != 2;
			p1.orientation = (String)cmbBiplaneOrientation.getSelectedItem();
			p1.depth 	= spnBiplaneDepth.get()*1e-9;
			p1.delta_z 	= spnBiplaneDeltaZ1.get()*1e-9;
			p1.delta_z2 = spnBiplaneDeltaZ2.get()*1e-9;
			p1.ni 		= spnBiplaneNI.get();
			p1.ns 		= spnBiplaneNS.get();
			p1.NA 		= spnNA.get();
			p1.M 		= 100.0;
			p1.ti0 		= spnBiplaneTI.get()*1E-6;
			p1.lambda 	= spnWavelength.get()*1E-9;
			p1.thick 	= spnThickness.get()*1E-9;
			p1.pixelSize 		= px;
			p1.axialResolution 	= px;
			p1.zd_star 	= 0.2;	
			p1.rotation = spnBiplaneRotation.get();
			p1.scale 	= spnBiplaneScale.get();
			p1.dx 		= spnBiplaneDX.get()*1e-9;
			p1.dy 		= spnBiplaneDY.get()*1e-9;
			p1.border 	= 0;
			modules.add(new PSFModule(fwhm, vwPSF, p1));

		}
		return modules;
	}
	
	/**
	 * Create Viewport at the pixelsize resolution
	 */
	protected Viewport createViewport(double pixelsize) {
		double thickness = spnThickness.get();
		Point3D origin = new Point3D(0, 0, -thickness/2);
		double fovNano = spnCameraResolution.get() * spnPixelsizeCamera.get();
		return new Viewport(origin, fovNano, fovNano, thickness, pixelsize);
	}
				 		
	public void stateChanged(ChangeEvent e) {
		updateInterface();
	}
	
	/**
	 * Test Accuracy according the Thompson rules.
	 */
	protected double[] testAccuracy() {
		double pxc 	= spnPixelsizeCamera.get();
		double fwhm = getDiffractionLimit() * spnFWHMFactor.get();
		int upC 	= spnOversamplingConvolve.get();
		int upW 	= spnOversamplingWorking.get();
		ArrayList<PSFModule> psfs	= createPSFModule();
		CameraModule 	camera 	= createCameraModule();
		NoiseModule 	noise 	= createNoiseModule();
		AutofluorescenceModule 	autofluo	= createAutofluorescenceModule();
		String dataset = new File((new File(path).getParent())).getName();

		SequenceFactory sequencer = new SequenceFactory(dataset, 1, 3, 1, camera, psfs, noise, autofluo, createViewport(pxc), upC, upW);
/*		sequencer.generateFrames(0, fluorophores, fwhm, true, true, true);
		double N = test.getMaximum();
		double a = spnPixelsizeCamera.get();
		double s = 0.5 * spnWavelength.get() / spnNA.get();
		double b = test.getMean();
		double s4 = s*s*s*s;
		double accStats = Math.sqrt((s*s)/N);
		double accQuant = Math.sqrt((a*a/12.0)/N);
		double accBack = Math.sqrt((8*Math.PI*s4*b*b)/(N*N*a*a));
		double accThomson = Math.sqrt(accStats*accStats + accQuant*accQuant + accBack*accBack);
		lblAccuracy_N.setText(IJ.d2s(N));
		lblAccuracy_back.setText(IJ.d2s(accBack));
		lblAccuracy_stats.setText(IJ.d2s(accStats));
		lblAccuracy_quant.setText(IJ.d2s(accQuant));
		lblAccuracy_Thomson.setText(IJ.d2s(accThomson));
		return new double[] {N, a, s, b, accStats, accQuant, accBack, accThomson};
		*/
		return new double[] {0,0,0,0, 0, 0,0,0,};
	}
	
	/**
	 * Update Interface.
	 */
	private void updateInterface() {
		int tab = tabPSF.getSelectedIndex();
		double pxc = spnPixelsizeCamera.get();
		int fov = (int)Math.round(pxc * spnCameraResolution.get());
		CameraModule camera = createCameraModule();
		spnCameraSaturation.set(camera.getSaturation());
		lblFoV.setText("" + fov + "x" + fov + "x" + spnThickness.get());
		double diffractionLimit = getDiffractionLimit();
		lblDiffractionLimit.setText(IJ.d2s(diffractionLimit) + " nm");
		lblFWHM.setText(IJ.d2s(diffractionLimit*spnFWHMFactor.get()));
		lblSummaryPlane.setText(getDefocusPlaneDescription() + " | " + getFocalPlaneDescription());
		int psf = cmbPSFModelXY.getSelectedIndex();
		if (psf <= PSFModule.RECTANGLE)
			cmbPSFModelZ.setSelectedItem(ZFunction.names[ZFunction.ZFUNC_EXPO]);
		else if (psf >= PSFModule.ROTATED_GAUSSIAN)
			cmbPSFModelZ.setSelectedItem(ZFunction.names[ZFunction.ZFUNC_ANGLE]);
		else if (psf == PSFModule.ASTIGMATISM)
			cmbPSFModelZ.setSelectedItem(ZFunction.names[ZFunction.ZFUNC_EXPO2]); 
		bnSavePSF.setEnabled( tab== 2 || tab == 1);
		int c = spnOversamplingConvolve.get();
		int w = spnOversamplingWorking.get();
		
		String psfName = (String)cmbPSFModelXY.getSelectedItem();
		String p = IJ.d2s((pxc / (c*w)), 2);
		if (tab == 2)
			psfName =" G&L";
		if (tab == 3)
			psfName = new File(txtPSFFile[0].getText()).getName();
		lblOversampling.setText("Px convolve " + p + " nm");
		lblConvolveInfo.setText("" + p + ">" + IJ.d2s((pxc / (c*w)), 2) + ">"  + pxc + " " + psfName);
	}

	protected double getDiffractionLimit() {
		return 0.5 * spnWavelength.get() / spnNA.get();
	}
	
	protected double getFocalPlane() {
		double oz = spnFocalPlaneTI.get();
		if (this.tabPSF.getSelectedIndex() == 2) {
			oz = oz * spnNI.get() / spnNS.get();
		}
		return oz;
	}
	
	protected String getDefocusPlaneDescription() {
		int psf = cmbPSFModelXY.getSelectedIndex();
		if (psf <= PSFModule.RECTANGLE)
			return "2 x FWHM at " + spnDefocusPlane.get() + " nm";
		if (psf >= PSFModule.ROTATED_GAUSSIAN)
			return "90 degrees at " + spnDefocusPlane.get() + " nm";
		if (psf == PSFModule.ASTIGMATISM)
			return "Vertical at " + spnDefocusPlane.get() + " nm";
		return "";
	}
	
	protected String getFocalPlaneDescription() {
		int psf = cmbPSFModelXY.getSelectedIndex();
		if (psf <= PSFModule.RECTANGLE)
			return "1 x FWHM at " + getFocalPlane() + " nm";
		if (psf >= PSFModule.ROTATED_GAUSSIAN)
			return "0 degrees at " + getFocalPlane() + " nm";
		if (psf == PSFModule.ASTIGMATISM)
			return "Horizontal at " + getFocalPlane() + " nm";
		return "";
	}

	public void windowActivated(WindowEvent e) 		{}
	public void windowClosed(WindowEvent e) 		{}
	public void windowDeactivated(WindowEvent e) 	{}
	public void windowDeiconified(WindowEvent e)	{}
	public void windowIconified(WindowEvent e)		{}
	public void windowOpened(WindowEvent e)			{}			
	public void windowClosing(WindowEvent e) 		{dispose();}
	
	private void loadSettings() {
		for(int i=0; i<NoiseModule.names.length; i++) {
			settings.record("spnNoise-" + NoiseModule.names[i], spnNoises[i], "0");
			settings.record("chkNoise-" + NoiseModule.names[i], chkNoises[i], false);
		}

		settings.record("cmbMode", cmbMode, SequenceFactory.modes[0]);
		settings.record("chkProjection", chkProjection, true);
		settings.record("chkStats", chkStats, true);
		settings.record("chkReport", chkReport, true);
		
		settings.record("spnOversamplingConvolve", spnOversamplingConvolve, "2");
		settings.record("spnOversamplingWorking", spnOversamplingWorking, "2");
		settings.record("spnZThickness", spnThickness, "500");
		settings.record("spnFocalPlane", spnFocalPlaneTI, "0");
		settings.record("spnDefocusPlane", spnDefocusPlane, "500");
		
		settings.record("spnCameraResolution", spnCameraResolution, "256");
		settings.record("spnPixelsizeCamera", spnPixelsizeCamera, "100");
		settings.record("cmbCameraQuantization", cmbCameraQuantization, "14");
		settings.record("spnCameraSaturation", spnCameraSaturation, "1000000");
		settings.record("spnBaseline", spnBaseline, "100");
		settings.record("cmbCameraFileFormat", cmbCameraFileFormat, CameraModule.names[0]);

		settings.record("spnQuantumEfficiency", spnQuantumEfficiency, "2");		
		settings.record("spnCameraGain", spnCameraGain, "2");		
		settings.record("spnCameraOffset", spnCameraOffset, "2");		
		
		settings.record("spnFluoPixelsize", spnFluoPixelsize, "15");
		settings.record("chkFluoAllFrames", chkFluoAllFrames, true);
		settings.record("spnSeedRandom", spnSeedRandom, "123");
		settings.record("spnNbFluorophores", spnNbFluorophores, "1");

		settings.record("cmbPSFModelXY", cmbPSFModelXY, PSFModule.namesXY[0]);		
		settings.record("cmbPSFModelXYZ", cmbPSFModelXYZ, PSFModule.namesXYZ[0]);		
		settings.record("cmbPSFModelZ", cmbPSFModelZ, ZFunction.names[0]);
		
		settings.record("spnFirstFrame", spnFirstFrame, "1");
		settings.record("spnLastFrame", spnLastFrame, "1");
		settings.record("spnIntervalFrame", spnIntervalFrame, "1");
		
		settings.record("cmbAutofluoMode", cmbAutofluoMode, AutofluorescenceModule.evolutions[0]);
		settings.record("spnAutofluoDiffusion", spnAutofluoDiffusion, "100");	
		settings.record("spnAutofluoDefocus", spnAutofluoDefocus, "1000");	
		settings.record("spnAutofluoChange", spnAutofluoChange, "50");	
		settings.record("spnAutofluoNbScale", spnAutofluoNbScale, "10");	
		settings.record("spnAutofluoDispl", spnAutofluoDispl, "100");	
		settings.record("cmbAutofluoSources", cmbAutofluoSources, AutofluorescenceModule.names[0]);	
		settings.record("spnAutofluoGain", spnAutofluoGain, "100");
		settings.record("spnAutofluoOffsetMean", spnAutofluoOffsetMean, "100");
		settings.record("spnAutofluoOffsetStdv", spnAutofluoOffsetStdv, "0");
		settings.record("spnAutofluoNbSources", spnAutofluoNbSources, "20");
		settings.record("spnAutofluoSize", spnAutofluoSize, "200");
	
		settings.record("spnWavelength", spnWavelength, "500");
		settings.record("spnNA", spnNA, "1.4");
		settings.record("spnNS", spnNS, "1.0");
		settings.record("spnNI", spnNI, "1.0");
		settings.record("spnFWHMFactor", spnFWHMFactor, "1");
		settings.record("spnOversamplingLateral", spnOversamplingLateral, "1");
		settings.record("spnOversamplingAxial", spnOversamplingAxial, "1");
		settings.record("spnDeltaZ", spnDeltaZ, "0");
		settings.record("spnBeadSize", spnBeadSize, "0");

		settings.record("spnFluoAmplitude", spnFluoAmplitude, "1000");
		settings.record("cmbVerbose", cmbVerbose, "Verbose");
		settings.record("cmbThreading", cmbThreading, "Off-1 Thread");
		
		for(int i=0; i<3; i++)
			settings.record("txtFilePSF-"+i, txtPSFFile[i], "-");

		settings.record("spnBiplaneDepth1", spnBiplaneDepth, "0");
		settings.record("spnBiplaneDeltaZ1", spnBiplaneDeltaZ1, "0");
		settings.record("spnBiplaneDeltaZ2", spnBiplaneDeltaZ2, "500");
		settings.record("spnBiplaneNI", spnBiplaneNI, "1");
		settings.record("spnBiplaneNI", spnBiplaneNI, "1");
		settings.record("cmbBiplaneOrientation", cmbBiplaneOrientation, "Rows");
		settings.record("spnBiplaneTI", spnBiplaneTI, "100");
		settings.record("spnBiplaneDX", spnBiplaneDX, "0");
		settings.record("spnBiplaneDY", spnBiplaneDY, "0");
		settings.record("spnBiplaneRotation", spnBiplaneRotation, "0");
		settings.record("spnBiplaneScale", spnBiplaneScale, "1");


		settings.loadRecordedItems();
	}
	
	public void loadParams() {	
		File destFile = new File(IJ.getDirectory("plugins") + "localization-microscopy.txt");
		File sourceFile = new File(path + "localization-microscopy.txt");
		try {
			Tools.copyFile(sourceFile, destFile);
			settings.loadRecordedItems();
		}
		catch(Exception ex) {}
	}
	
	public void saveParams() {
		settings.storeRecordedItems();
		File sourceFile = new File(IJ.getDirectory("plugins") + "localization-microscopy.txt");
		File destFile = new File(path + "localization-microscopy.txt");
		try {
			Tools.copyFile(sourceFile, destFile);
		}
		catch(Exception ex) {}

	}
	
}

