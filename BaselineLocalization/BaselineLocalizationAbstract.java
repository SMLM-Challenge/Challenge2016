// =========================================================================================
//
// Single-Molecule Localization Microscopy Challenge 2016
// http://bigwww.epfl.ch/smlm/
//
// Author:
// Daniel Sage, http://bigwww.epfl.ch/sage/
// Biomedical Imaging Group (BIG)
// Ecole Polytechnique Federale de Lausanne (EPFL), CH-1015 Lausanne,
// Switzerland
//
// Reference:
// D. Sage, H. Kirshner, T. Pengo, N. Stuurman, J. Min, S. Manley, M. Unser
// Quantitative Evaluation of Software Packages for Single-Molecule Localization
// Microscopy
// Nature Methods 12, August 2015.
//
// Conditions of use:
// You'll be free to use this software for research purposes, but you should not 
// redistribute it without our consent. In addition, we expect you to include a 
// citation or acknowledgment whenever you present or publish results that are based on it.
//
// =========================================================================================

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.io.Opener;

import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public abstract class BaselineLocalizationAbstract implements ActionListener {

	protected enum Mode {
		D2, DH, AS, BP
	};

	// All
	protected double	pixelsize			= 250;
	protected double	fwhm				= 250;
	protected String	logging				= "Progression";
	protected double	minSignal			= 10;
	protected double	minSNR				= 1;
	protected String	path				= System.getProperty("user.home");

	// DH: Double-Helix
	protected double	DH_distMinPairing	= 200;
	protected double	DH_distMaxPairing	= 700;
	protected double	DH_A				= 10;
	protected double	DH_B				= 0;

	// AS: Astigmatism
	protected double	AS_A				= 100;
	protected double	AS_B				= 0;

	// BP: Biplan
	protected double	BP_distMaxPairing	= 700;
	protected double	BP_A				= 10;
	protected double	BP_B				= 0;

	protected String	title				= "Untitled";
	protected int		framesMax			= 50000;
	protected Mode		mode				= Mode.D2;
	protected boolean	mute				= false;
	protected boolean	display				= false;
	protected boolean	verbose				= false;
	protected float		radiusPix			= 1;
	protected double	chrono				= 0;
	
	// Calibration panel
	private Button help 	= new Button("Help");
	private Button calibrate 	= new Button("Calibrate");
	private TextField txtCalZ 	= new TextField("1 > -750; 151 > 750");
	private Choice cmbBeads		= new Choice();
	private Choice cmbSource 	= new Choice();
	
	private GenericDialog dlg;

	protected abstract double[] calibrate(ImagePlus imp, int fmin, int fmax, double zmin, double zmax, double zstep, Roi roi);
	protected abstract ArrayList<BaselineLocalizationParticle> process(ImagePlus imp);

	protected void doDialog(Mode mode, String title) {
		this.mode = mode;
		this.title = title;

		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp == null) {
			IJ.error("No open image.");
			return;
		}
		
		if (imp.getStackSize() < 2) {
			IJ.error("No open stack of images.");
			return;
			
		}
		framesMax = imp.getStackSize();

		getParameters();

		dlg = new GenericDialog(title);
		calibrate.addActionListener(this);
		help.addActionListener(this);
		addInfo(dlg);
		
		Panel pnCalibration = createCalibrationPanel(txtCalZ, help, calibrate);
		if (mode == Mode.DH || mode == Mode.AS || mode == Mode.BP) {
			dlg.addPanel(pnCalibration, GridBagConstraints.NORTH, new Insets(10, 10, 10, 10));
		}
		addLine(dlg, "General parameters");
		dlg.addNumericField("FWHM of PSF [nm]", fwhm, 1);
		dlg.addNumericField("Pixelsize [nm]", pixelsize, 1);
		dlg.addNumericField("Signal - Minimal value", minSignal, 1);
		dlg.addNumericField("SNR - Minimal value", minSNR, 1);

		if (mode == Mode.DH) {
			addLine(dlg, "Specific parameters for double-helix");
			dlg.addNumericField("A_dh z=A*angle+B", DH_A, 1);
			dlg.addNumericField("B_dh z=A*angle+B", DH_B, 1);
			dlg.addNumericField("Mindist pairing [nm]", DH_distMinPairing, 1);
			dlg.addNumericField("Maxdist pairing [nm]", DH_distMaxPairing, 1);
		}
		if (mode == Mode.AS) {
			addLine(dlg, "Specific parameters for astigmatism");
			dlg.addNumericField("A_as z=A*(wx-wy)+B", AS_A, 1);
			dlg.addNumericField("B_as z=A*(wx-wy)+B", AS_B, 1);
		}
		if (mode == Mode.BP) {
			addLine(dlg, "Specific parameters for biplane");
			dlg.addNumericField("A_bp z=A*(w1-w2)+B", BP_A, 1);
			dlg.addNumericField("B_bp z=A*(w1-w2)+B", BP_B, 1);
			dlg.addNumericField("Maxdist pairing [nm]", BP_distMaxPairing, 1);
		}
		addLine(dlg, "Runtime parameters");
		
		dlg.addNumericField("Frames to process", framesMax, 0);
		dlg.addStringField("Path to store output", path, 25);
		dlg.addChoice("Logging message", new String[] { "Progression", "Display prefilter", "Verbose", "Mute" }, "Progression");
		Panel pnSequence = createSequencePanel(imp);
		dlg.addPanel(pnSequence, GridBagConstraints.NORTH, new Insets(10, 10, 10, 10));
		
		dlg.showDialog();

		if (dlg.wasCanceled())
			return;

		fwhm 		= dlg.getNextNumber();
		pixelsize 	= dlg.getNextNumber();
		minSignal 	= dlg.getNextNumber();
		minSNR 		= dlg.getNextNumber();
		
		if (mode == Mode.DH) {
			DH_A = dlg.getNextNumber();
			DH_B = dlg.getNextNumber();
			DH_distMinPairing = dlg.getNextNumber();
			DH_distMaxPairing = dlg.getNextNumber();
		}
		if (mode == Mode.AS) {
			AS_A = dlg.getNextNumber();
			AS_B = dlg.getNextNumber();
		}
		if (mode == Mode.BP) {
			BP_A = dlg.getNextNumber();
			BP_B = dlg.getNextNumber();
			BP_distMaxPairing = dlg.getNextNumber();
		}
		framesMax 	= (int) dlg.getNextNumber();
		path 		= dlg.getNextString();
		logging 	= dlg.getNextChoice();	
		verbose 	= logging.equals("Verbose");
		mute 		= logging.equals("Mute");
		display 	= logging.equals("Display prefilter");
		try {
			(new File(path)).mkdir();
		}
		catch (Exception ex) {
		}
		imp.setSlice(1);
		framesMax = Math.min(framesMax, imp.getStackSize());
		runProcess();
	}

	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == calibrate) 
			runCalibration();
		if (e.getSource() == help) {
			JFrame frame = new JFrame("Calibration");
			JTextPane text = new JTextPane();
			text.setContentType("text/html");
			text.setEditable(false);
			text.setBorder(BorderFactory.createEtchedBorder());
			text.setMargin(new Insets(4, 4 ,4, 4));
			text.setText("<html><body style=\"font-family:arial\""+
				"<b>Axial Calibration for 3D SMLM</b><br>"+
				"<p>The calibration is performed on one specific bead. " + 
				"You have to select a z-stack images in the field <b>Stack of bead</b> and provide the parameters" +
				" of localization <b>FWHM</b>, <b>Pixelsize</b>, <b>Mininal Signal</b>, and <b>Mininmal SNR</b>. " +
				" Select also a rectangle (region-of-interest) around a 'good' bead (for the biplane case, only select a bead in left image)." +
				"</p><p>"+
				"<p>This simple calibration tool relies a linear relation between z and a shape factor of the bead. " + 
				"The goal of the calibration is to identify <i>A</i> and <i>B</i> in the equation: z = <i>A</i> s + <i>B</i>, " +
				" where z is the axial position in nm, and s in a shape factor that depends of the 3D modalities:" +
				"<br>&bull; (as) Astigmatism: s = w<sub>x</sub> - w<sub>x</sub>, " + 
				" where w<sub>x</sub> is the estimated size of the bright spot in X" +
				" where w<sub>y</sub> is the estimated size of the bright spot in Y." +
				"<br>&bull; (dh) Double-Helix: s = angle, " + 
				" where angle is the estimated angle formed by the closest bright spots." +
				"<br>&bull; (bp) Biplane: s = w<sub>1</sub> - w<sub>2</sub>, " + 
				" where w<sub>1</sub> and w<sub>2</sub> are the eestimated size, respectively in the left and the right image."+
				"</p><p>"+
				"<p>The calibration requires to knowledge of the correspondence between the slice number and the axial position (z) in nm." +
				"The syntax is the following: " +
				"<br><i>start_slice > axial_postion_nm; end_slice > axial_postion_nm;</i>" + 
				"<br>For example if the slice 25 corresponds to -500nm and the slice 125 corresponds to +500nm "  + 
				"you have to enter 25 > -500; 125 > 500; in the <b>Calibration</b> field."
							);

			JScrollPane scroll = new JScrollPane(text);
			scroll.setPreferredSize(new Dimension(550, 550));
			frame.add(scroll);
			frame.pack();
			frame.setVisible(true);
			
		}
	}
	
	public void runProcess() {
		ImagePlus imp = WindowManager.getImage(cmbSource.getSelectedItem());
		if (imp == null) {
			IJ.error("Invalid image source");
			return;
		}
		
		radiusPix = (float) (fwhm / pixelsize);
		if (!mute)
			IJ.log("Starting to process " + framesMax + " frames in " + title + "(fwhm=" + radiusPix + " pix)");
		chrono = System.nanoTime();
		
		ArrayList<BaselineLocalizationParticle> particles = process(imp);
		
		int nt = imp.getStackSize();
		Overlay overlay = new Overlay();
		double zmax = -Double.MAX_VALUE;
		double zmin = Double.MAX_VALUE;
		for(BaselineLocalizationParticle particle : particles) {
			zmax = Math.max(zmax, particle.z);
			zmin = Math.min(zmin, particle.z);
		}
		
		for(BaselineLocalizationParticle particle : particles) {
			int frame = particle.frame;
			if (frame >= 1 && frame <= nt) {
				imp.setSlice(frame);
				double h = (particle.z - zmin)/(zmax-zmin);
				Color zc = Color.getHSBColor((float)(h*0.5), 1f, 1f);
				Color c = new Color(zc.getRed(), zc.getGreen(), zc.getBlue(), 150);
				particle.draw(overlay, c, pixelsize);
			}
		}
		imp.setOverlay(overlay);
		
		String filename = path + File.separator + imp.getTitle() + "-" + framesMax + "-" + minSNR + ".csv";
		if (!mute) IJ.log("Store into " + filename);
		File file = new File(filename);
		int count = 0;
		try {
			BufferedWriter buffer = new BufferedWriter(new FileWriter(file));
			buffer.write(title + ", frame, xnano, ynano, znano, intensity, snr, width_x, width_y\n");
			for (BaselineLocalizationParticle particle : particles)
				buffer.write("" + (++count) + ", " + particle.toString() + "\n");
			buffer.close();
		}
		catch (Exception ex) {
			IJ.log(ex.toString());
		}

		if (!mute)
			IJ.log("End of processing " + framesMax + " frames, number of particles: " + count);
		imp.setSlice(1);
		setParameters();
	}

	private void runCalibration() {
		ImagePlus imp = WindowManager.getImage(cmbBeads.getSelectedItem());
		if (imp == null)
			return;
		String items[] = txtCalZ.getText().split("[>;]");
		if (items.length < 4) {
			IJ.error("Syntax error: enter something like '10 > -600; 100 > 600"); 
			return;	
		}
		try {
			
			Vector numericFields = dlg.getNumericFields();
			Object txt0 = numericFields.get(0);
			if (txt0 instanceof TextField)
				fwhm = Double.parseDouble(((TextField)txt0).getText());
			Object txt1 = numericFields.get(1);
			if (txt1 instanceof TextField)
				pixelsize = Double.parseDouble(((TextField)txt1).getText());
			Object txt2 = numericFields.get(2);
			if (txt2 instanceof TextField)
				minSignal = Double.parseDouble(((TextField)txt2).getText());
			Object txt3 = numericFields.get(3);
			if (txt3 instanceof TextField)
				minSNR = Double.parseDouble(((TextField)txt3).getText());

			int fmin = Integer.parseInt(items[0].trim());
			double zmin = Double.parseDouble(items[1].trim());
			int fmax = Integer.parseInt(items[2].trim());
			double zmax = Double.parseDouble(items[3].trim());
			double zstep = (zmax-zmin) / (fmax-fmin);
			Roi roi = imp.getRoi();
			if (roi == null) {
				IJ.error("Select a Roi around a bead"); 
				return;
			}
			radiusPix = (float) (fwhm / pixelsize);
			if (!mute)
				IJ.log("Starting to process " + framesMax + " frames in " + title + "(fwhm=" + radiusPix + " pix)");
			chrono = System.nanoTime();
			double[] params = calibrate(imp, fmin, fmax, zmin, zmax, zstep, roi);
			
			if (mode == Mode.AS) {
				AS_A = params[0];
				AS_B = params[1];
			}
			if (mode == Mode.DH) {
				DH_A = params[0];
				DH_B = params[1];
			}
			if (mode == Mode.BP) {
				BP_A = params[0];
				BP_B = params[1];
			}
			
			Object txtA = numericFields.get(4);
			if (txtA instanceof TextField)
				((TextField)txtA).setText(""+params[0]);
			Object txtB = numericFields.get(5);
			if (txtB instanceof TextField)
				((TextField)txtB).setText(""+params[1]);	

		}
		catch(Exception ex) {
			IJ.error("Syntax error: e.g '10 > -600; 100 > 600"); 
			return;
		}
	}

	private ArrayList<String> findOpenImages() {
		int id[] = WindowManager.getIDList();
		ArrayList<String> list = new ArrayList<String>();
		for(int i=0; i<id.length; i++) {
			ImagePlus imp = WindowManager.getImage(id[i]);
			if (imp.getStackSize() > 1)
				list.add(imp.getTitle());
		}
		return list;
	}
	
	private Panel createSequencePanel(ImagePlus imp) {
		Panel pnSequence = new Panel();
		cmbSource.setPreferredSize(new Dimension(200, 20));
		ArrayList<String> list = findOpenImages();
		for(String name : list)
			cmbSource.addItem(name);
		pnSequence.add(cmbSource);
		cmbSource.select(imp.getTitle());
		return pnSequence;
	}
	
	private Panel createCalibrationPanel(TextField txtCalZ, Button help, Button calibrate) {
		Panel pnCalibration = new Panel();
		cmbBeads.setPreferredSize(new Dimension(200, 20));
		ArrayList<String> list = findOpenImages();
		for(String name : list)
			cmbBeads.addItem(name);
		JPanel pn = new JPanel();
		pn.setLayout(new GridBagLayout());
		GridBagConstraints cbs = new GridBagConstraints();
		pn.setBorder(BorderFactory.createTitledBorder("Axial calibration on a bead (Z)"));
		cbs.anchor = GridBagConstraints.NORTHWEST;
		cbs.insets = new Insets(1, 1, 1, 1);
		cbs.fill = GridBagConstraints.HORIZONTAL;
		cbs.gridx = 0; cbs.gridy = 0; pn.add(new Label("Stack of bead (Roi)"), cbs);
		cbs.gridx = 1; cbs.gridy = 0; pn.add(cmbBeads, cbs);
		cbs.gridx = 0; cbs.gridy = 1; pn.add(new Label("Calibration"), cbs);
		cbs.gridx = 1; cbs.gridy = 1; pn.add(txtCalZ, cbs);
		cbs.gridx = 0; cbs.gridy = 2; pn.add(help, cbs);
		cbs.gridx = 1; cbs.gridy = 2; pn.add(calibrate, cbs);
		pnCalibration.add(pn);
		return pnCalibration;
	}
	
	private void addLine(GenericDialog dlg, String line) {
		JLabel label = new JLabel("<html><b>" + line + "</b></html>");
		label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		label.setPreferredSize(new Dimension(400, 20));
		label.setBorder(BorderFactory.createEtchedBorder());
		Panel panel = new Panel();
		panel.add(label);
		dlg.addPanel(panel);
	}
	
	private void addInfo(GenericDialog dlg) {
		JTextPane text = new JTextPane();
		text.setContentType("text/html");
		text.setEditable(false);
		text.setBorder(BorderFactory.createEtchedBorder());
		text.setMargin(new Insets(4, 4 ,4, 4));
		text.setText("<html><body style=\"font-family:arial\""+
			"<b>Single-Molecule Localization Microscopy (SMLM)</b><br>"+
			"This localization software is only designed to establish the performance baseline for the SMLM challenge."+
			"It is very fast, very inaccurate, and it only relies on two threshold parameters.<br>"+
			"<i>&copy; 2016 SMLM Challenge. Biomedical Imaging Group, EPFL.</i>"
			);

		JScrollPane scroll = new JScrollPane(text);
		scroll.setPreferredSize(new Dimension(400, 120));
		Panel panel = new Panel();
		panel.add(scroll);
		dlg.addPanel(panel);
	}

	protected void log(int frame, ArrayList<int[]> candidates, ArrayList<BaselineLocalizationParticle> particles) {
		if (mute)
			return;
		String time = IJ.d2s(((System.nanoTime() - chrono)*1e-6), 2) + "ms ";
		IJ.log(time + "Frame:" + frame + " Candidates:" + candidates.size() + " Particles:" + particles.size());
	}
	
	private void getParameters() {
		minSignal = Prefs.get(title + ".minSignal", 10);
		minSNR = Prefs.get(title + ".minSNR", 10);
		fwhm = Prefs.get(title + ".fwhm", 250);
		pixelsize = Prefs.get(title + ".pixelsize", 100);
		path = Prefs.get(title + ".path", System.getProperty("user.home"));
		framesMax = (int) Prefs.get(title + ".framesMax", 50000);
		logging = Prefs.get(title + ".logging", "Progression");
		if (mode == Mode.DH) {
			DH_distMinPairing = Prefs.get(title + ".distMinPairing", 200);
			DH_distMaxPairing = Prefs.get(title + ".distMaxPairing", 700);
			DH_A = Prefs.get(title + ".A", 10);
			DH_B = Prefs.get(title + ".B", 0);
		}
		if (mode == Mode.AS) {
			AS_A = Prefs.get(title + ".A", 10);
			AS_B = Prefs.get(title + ".B", 0);
		}
		if (mode == Mode.BP) {
			BP_distMaxPairing = Prefs.get(title + ".distMaxPairing", 700);
			BP_A = Prefs.get(title + ".A", 10);
			BP_B = Prefs.get(title + ".B", 0);
		}
		txtCalZ.setText(Prefs.get(title + ".calibration", "1 > -750; 151 > 750"));
	}

	protected void setParameters() {
		Prefs.set(title + ".minSignal", minSignal);
		Prefs.set(title + ".minSNR", minSNR);
		Prefs.set(title + ".fwhm", fwhm);
		Prefs.set(title + ".pixelsize", pixelsize);
		Prefs.set(title + ".path", path);
		Prefs.set(title + ".framesMax", framesMax);
		Prefs.set(title + ".logging", logging);
		if (mode == Mode.DH) {
			Prefs.set(title + ".distMinPairing", DH_distMinPairing);
			Prefs.set(title + ".distMaxPairing", DH_distMaxPairing);
			Prefs.set(title + ".A", DH_A);
			Prefs.set(title + ".B", DH_B);
		}
		if (mode == Mode.AS) {
			Prefs.set(title + ".A", DH_A);
			Prefs.set(title + ".B", DH_B);
		}
		if (mode == Mode.BP) {
			Prefs.set(title + ".distMaxPairing", BP_distMaxPairing);
			Prefs.set(title + ".A", BP_A);
			Prefs.set(title + ".B", BP_B);
		}
		Prefs.set(title + ".calibration", txtCalZ.getText());
	}

	protected double[] linearRegression(double x[], double[] y) {
		int n = x.length;
		double sx = 0.0;
		double sy = 0.0;
		double sxy = 0.0;
		double sxx = 0.0;
		for(int i=0; i<n; i++) {
			sx += x[i];
			sy += y[i];
			sxy += x[i]*y[i];
			sxx += x[i]*x[i];
		}
		sx /= n; 
		sy /= n; 
		sxy /= n; 
		sxx /= n; 
		double a = (sxy - sx*sy) / (sxx - sx*sx);
		double b = sy - a * sx;
		return new double[] {a, b};
	}
	
	protected static void chooseImage() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int ret = fc.showOpenDialog(null);
		
		if (ret == JFileChooser.APPROVE_OPTION) {
			Opener opener = new Opener();
			String filename = fc.getSelectedFile().getAbsolutePath();
			System.out.println(filename);
			ImagePlus imp = opener.openImage(filename);
			if (imp == null) 
				IJ.error("Unable to open " + fc.getSelectedFile().getAbsolutePath());
			else
				imp.show();
		}
	}
}
