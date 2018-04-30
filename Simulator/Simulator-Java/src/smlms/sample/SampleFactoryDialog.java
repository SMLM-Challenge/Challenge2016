package smlms.sample;

import ij.IJ;
import ij.gui.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import smlms.file.PositionFile;
import smlms.tools.NormalizedVariable;
import smlms.tools.Point3D;
import additionaluserinterface.GridPanel;
import additionaluserinterface.Settings;
import additionaluserinterface.SpinnerDouble;
import additionaluserinterface.SpinnerInteger;
import additionaluserinterface.WalkBar;

public class SampleFactoryDialog extends JDialog implements ActionListener, Runnable {

	private WalkBar				walk			= new WalkBar("(c) 2016 EPFL, BIG", false, false, true);
	private Settings			settings		= new Settings("localization-microscopy", IJ.getDirectory("plugins") + "smlm-2016.txt");
	private JButton				bnBrowse		= new JButton("Browse");
	private JButton				bnLoad			= new JButton("Load");
	private JButton				bnSave			= new JButton("Save");
	private JButton				bnInfo			= new JButton("Information");
	private JButton				bnCreateBelong	= new JButton("Mask");
	private JButton				bnCreatePos		= new JButton("Create");
	private JButton				bnCreateStep	= new JButton("Create Step");
	private JButton				bnRemoveSteric	= new JButton("Steric");
	private JButton				bnCrop			= new JButton("Crop");
	private JButton				bnSavePositions	= new JButton("ZOff & Save");

	private JComboBox			cmbPredefined	= new JComboBox();

	private SpinnerInteger		spnNbFluo		= new SpinnerInteger(1000, 1, 9999999, 1);
	private SpinnerInteger		spnPixelsize	= new SpinnerInteger(100, 1, 10000, 1);
	private SpinnerInteger		spnNbSamples	= new SpinnerInteger(10000, 1, 9999999, 1000);
	private SpinnerDouble		spnZOffset		= new SpinnerDouble(-1000, -999999, 999999, 1);
	private SpinnerDouble		spnTimeOut		= new SpinnerDouble(5, 0.1, 99999, 1);
	private SpinnerDouble		spnFactorDistanceMask = new SpinnerDouble(1, 0, 1000, 0.1);
	private SpinnerDouble		spnSteric		= new SpinnerDouble(1, 0, 1000, 0.1);
	private SpinnerDouble		spnSmooth		= new SpinnerDouble(10, 0, 10000, 1);
	private SpinnerDouble		spnStep			= new SpinnerDouble(1, 0.0001, 1000, 0.1);
	private JTextField			txtFileSample	= new JTextField("-");
	private JTextField			txtOutSample	= new JTextField("-");
	private JLabel				txtName			= new JLabel("null sample");
	private JLabel				txtMask			= new JLabel("unvalid mask");
	private JLabel				txtFluo			= new JLabel("unvalid fluo");
	private JLabel				txtSteric		= new JLabel("not yet run");
	private JLabel				txtStep			= new JLabel("not yet run");
	private JLabel				txtCrop			= new JLabel("not yet run");

	private JRadioButton 		rbMask			= new JRadioButton("Mask", false);
	private JRadioButton 		rbFluos			= new JRadioButton("Fluos", true);

	private Thread				thread			= null;
	
	private Sample				sample			= null;
	private ArrayList<Sample>	samples			= new ArrayList<Sample>();

	private JButton 			job = null;
	
	private SampleFactory builder;
	
	public static void main(String args[]) {
		new SampleFactoryDialog();
	}

	public SampleFactoryDialog() {
		super(new JFrame(), "Sample Factory");
		samples.add(getSample1());
		samples.add(getSample2());
		samples.add(getSample3());
		
		ButtonGroup group = new ButtonGroup();
		group.add(rbMask);
		group.add(rbFluos);
		
		cmbPredefined.addItem("None");
		for (Sample sample : samples)
			cmbPredefined.addItem(sample.name);
		settings.record("sample-spnFactorDistanceMask", spnFactorDistanceMask, "1");
		settings.record("sample-txtFileSample", txtFileSample, "-");
		settings.record("sample-spnPixelsize", spnPixelsize, "100");
		settings.record("sample-spnZOffset", spnZOffset, "-100");
		settings.record("sample-spnNbSamples", spnNbSamples, "1000");
		settings.record("sample-spnTimeOut", spnTimeOut, "5");
		settings.record("sample-spnNbFluo", spnNbFluo, "1000");
		settings.record("sample-spnStep", spnStep, "1");
		settings.record("sample-spnSmooth", spnSmooth, "800");
		settings.loadRecordedItems();

		GridPanel samp = new GridPanel("Sample Definition");
		samp.place(0, 0, txtFileSample);
		samp.place(0, 1, bnBrowse);
		samp.place(1, 0, txtName);
		samp.place(1, 1, bnLoad);
		samp.place(2, 0, cmbPredefined);
		samp.place(2, 1, bnSave);

		GridPanel pn1 = new GridPanel(false);
		pn1.place(3, 0, new JLabel("NbSample"));
		pn1.place(3, 1, spnNbSamples);
		pn1.place(3, 2, txtMask);
		pn1.place(3, 3, bnCreateBelong);	
		pn1.place(4, 0, new JLabel("Factor Distance"));
		pn1.place(4, 1, spnFactorDistanceMask);
		pn1.place(4, 2, txtFluo);
		pn1.place(4, 3, bnCreatePos);

		JTabbedPane tab = new JTabbedPane();
		tab.add("Masked", pn1);
		
		GridPanel fact = new GridPanel("Sample Factory");
		fact.place(1, 0, new JLabel("Pixelsize"));
		fact.place(1, 1, spnPixelsize);
		fact.place(1, 2, new JLabel("TimeOut"));
		fact.place(1, 3, spnTimeOut);
		fact.place(2, 0, new JLabel("Nb Pos."));
		fact.place(2, 1, spnNbFluo);
		fact.place(2, 2, new JLabel("ZOffset"));
		fact.place(2, 3, spnZOffset);
		//fact.place(3, 0, 4, 1, tab);
		fact.place(4, 0, new JLabel("Step (nm)"));
		fact.place(4, 1, spnStep);
		fact.place(4, 2, txtStep);
		fact.place(4, 3, bnCreateStep);
		fact.place(6, 0, new JLabel("Smooth"));
		fact.place(6, 1, spnSmooth);

		fact.place(6, 2, txtCrop);
		fact.place(6, 3, bnCrop);
		fact.place(7, 0, new JLabel("Steric (nm)"));
		fact.place(7, 1, spnSteric);
		fact.place(7, 2, txtSteric);
		fact.place(7, 3, bnRemoveSteric);
		fact.place(8, 0, 3, 1, txtOutSample);
		fact.place(8, 3, bnSavePositions);

		txtMask.setBorder(BorderFactory.createEtchedBorder());
		txtName.setBorder(BorderFactory.createEtchedBorder());
		txtFluo.setBorder(BorderFactory.createEtchedBorder());
		txtSteric.setBorder(BorderFactory.createEtchedBorder());
		txtStep.setBorder(BorderFactory.createEtchedBorder());
		txtCrop.setBorder(BorderFactory.createEtchedBorder());
		
		GridPanel main = new GridPanel(false);
		main.place(0, 0, samp);
		main.place(1, 0, fact);
		main.place(6, 0, walk);

		add(main);
		pack();
		setModal(false);
		GUI.center(this);
		setVisible(true);

		bnCrop.addActionListener(this);
		bnRemoveSteric.addActionListener(this);
		bnBrowse.addActionListener(this);
		bnLoad.addActionListener(this);
		walk.getButtonClose().addActionListener(this);
		bnInfo.addActionListener(this);
		bnCreateStep.addActionListener(this);
		bnCreateBelong.addActionListener(this);
		bnCreatePos.addActionListener(this);
		bnSave.addActionListener(this);
		bnSavePositions.addActionListener(this);
		cmbPredefined.addActionListener(this);

		updateInterface();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == walk.getButtonClose()) {
			settings.storeRecordedItems();
			dispose();
		}
		else if (e.getSource() == bnBrowse) {
			sample = null;
			JFileChooser fc = new JFileChooser();
			int ret = fc.showOpenDialog(null);
			if (ret == JFileChooser.APPROVE_OPTION) {
				String filename = fc.getSelectedFile().getAbsolutePath();
				txtFileSample.setText(filename);
			}
		}
		else if (e.getSource() == bnLoad) {
			sample = null;
			String filename = txtFileSample.getText();
			setSample(Sample.load(filename));
			
			
		}
		else if (e.getSource() == bnSave && sample != null) {
			JFileChooser fc = new JFileChooser(IJ.getDirectory("plugins") + File.separator + sample + "-" + sample.name + ".txt");
			int ret = fc.showSaveDialog(null);
			if (ret == JFileChooser.APPROVE_OPTION)
				sample.save(fc.getSelectedFile().getAbsolutePath());
		}
		else if (e.getSource() == bnInfo && sample != null) {
			IJ.log("\n Information on :" + sample.name);
			for (Item item : sample)
				IJ.log(item.getInfo());
		}
		else if (e.getSource() == bnCreateBelong || e.getSource() == bnCreatePos 
				|| e.getSource() == bnCreateStep || e.getSource() == bnCrop
				|| e.getSource() == bnRemoveSteric 
				|| e.getSource() == bnSavePositions) {
			job = (JButton)e.getSource();
			if (thread == null) {
				thread = new Thread(this);
				thread.setPriority(Thread.MIN_PRIORITY);
				thread.start();
			}
		}

		else if (e.getSource() == cmbPredefined) {
			String selected = (String) cmbPredefined.getSelectedItem();
			sample = null;
			for (Sample s : samples) {
				if (selected.equals(s.name)) {
					setSample(s);
				}
			}
		}
		updateInterface();
	}

	private void updateInterface() {
		bnCreateBelong.setEnabled(sample != null);
		bnCreatePos.setEnabled(sample != null);
		bnCreateStep.setEnabled(sample != null);
		bnSave.setEnabled(sample != null);
		bnInfo.setEnabled(sample != null);
		File file = new File(txtFileSample.getText());
		txtOutSample.setText( file.getParent() + File.separator + "positions.csv");

	}
	
	public void setSample(Sample sample) {
		if (sample == null)
			return;
		this.sample = sample;
		txtName.setText(sample.getInfo());
		updateInterface();
	}
	
	public void run() {
		walk.reset();
		double chrono = System.nanoTime();
		if (job == this.bnCreateBelong) {
			Point3D dim = new Point3D(sample.nx, sample.ny, sample.nz);
			builder = new SampleFactory(dim, walk, sample, spnPixelsize.get());
			int count = builder.createBelongs(spnFactorDistanceMask.get(), spnTimeOut.get(), spnNbSamples.get());
			txtMask.setText("" + count + " voxels ");
					
			/*
			ImageWare mask = builder.createMask(dim, spnPixelsizeMask.get(), spnNbSamples.get(), factorDistanceMask);
			mask.show("Mask " + spnPixelsizeMask.get()  );
			if (sample != null) {
				double n = sample.voxels.size();
				double p = spnPixelsizeMask.get();
				double size = (sample.nx * sample.ny * sample.nz) / p;
				double ratio = n / size;
				txtMask.setText("Mask " + n  + " voxels / ratio:" + ratio );
			}
			*/
			IJ.log("Create Mask " + (System.nanoTime() - chrono)*1e-9);
		}
		else if (job == bnRemoveSteric) {
			int n = sample.fluos.size();
			builder.removeSteric(spnSteric.get());
			txtSteric.setText(" " + n + " > " + sample.fluos.size());
		}
		else if (job == bnCrop) {
			int n = sample.fluos.size();
			builder.crop();
			txtCrop.setText(" " + n + " > " + sample.fluos.size());
		}
		else if (job == bnCreatePos) {
			sample.fluos.clear();
			builder.createPositions(spnNbFluo.get(), spnTimeOut.get());
			txtFluo.setText("" + sample.fluos.size());
		}
		else if (job == bnCreateStep) {
			Point3D dim = new Point3D(sample.nx, sample.ny, sample.nz);
			builder = new SampleFactory(dim, walk, sample, spnPixelsize.get());
			builder.createPositionsStep(spnNbFluo.get(), spnTimeOut.get(), spnStep.get(), spnSmooth.get());
			txtStep.setText("" + sample.fluos.size());
		}
		else if (job == bnSavePositions) {
			double zoff = spnZOffset.get();
			for(Point3D fluo : sample.fluos) 
				fluo.z += zoff;
			new PositionFile(walk, txtOutSample.getText()).save(sample.fluos);
		}
		walk.finish();
		thread = null;
	}

	public Sample getSample1() {
		int sx = 5000;
		int sy = 4000;
		int sz = 1000;

		ArrayList<Point3D> nodes1 = new ArrayList<Point3D>();
		nodes1.add(new Point3D(-500, 1000, 500));
		nodes1.add(new Point3D(1000, 1000, 500));
		nodes1.add(new Point3D(1000, 500, 500));

		ArrayList<Point3D> nodes2 = new ArrayList<Point3D>();
		nodes2.add(new Point3D(1000, 2000, 500));
		nodes2.add(new Point3D(1500, 2000, 500));
		nodes2.add(new Point3D(1500, 2500, 500));

		ArrayList<Point3D> nodes3 = new ArrayList<Point3D>();
		nodes3.add(new Point3D(2500, 500, 0));
		nodes3.add(new Point3D(2500, 500, 1000));

		ArrayList<Point3D> nodes4 = new ArrayList<Point3D>();
		nodes4.add(new Point3D(2000, 1000, 100));
		nodes4.add(new Point3D(2500, 1000, 100));

		Sample sample = new Sample("U", sx, sy, sz);
		sample.add(new Tube("tube", nodes1, new NormalizedVariable(100), new NormalizedVariable(10), 1));
		sample.add(new Tube("tube", nodes2, new NormalizedVariable(100), new NormalizedVariable(10), 1));
		sample.add(new Tube("tube", nodes3, new NormalizedVariable(100), new NormalizedVariable(10), 1));
		sample.add(new Tube("tube", nodes4, new NormalizedVariable(100), new NormalizedVariable(10), 1));
		return sample;
	}
	
	public Sample getSample2() {
		int sx = 1000;
		int sy = 1000;
		int sz = 240;
		ArrayList<Point3D> nodes1 = new ArrayList<Point3D>();
		nodes1.add(new Point3D(100, 500, 120));
		nodes1.add(new Point3D(500, 500, 120));
		nodes1.add(new Point3D(900, 500, 120));
		Sample sample = new Sample("wavy", sx, sy, sz);
		NormalizedVariable thickness = new NormalizedVariable(5);
		thickness.addCosine(5, 2, 0);
		sample.add(new Tube("tube", nodes1, new NormalizedVariable(80), thickness, 1));
		return sample;
	}
	
	public Sample getSample3() {
		int sx = 1000;
		int sy = 1000;
		int sz = 200;
		ArrayList<Point3D> nodes1 = new ArrayList<Point3D>();
		nodes1.add(new Point3D(250, 250, 50));
		nodes1.add(new Point3D(750, 250, 150));
		nodes1.add(new Point3D(750, 750, 150));
		nodes1.add(new Point3D(250, 750, 50));
		Sample sample = new Sample("small square", sx, sy, sz);
		NormalizedVariable radius = new NormalizedVariable(40);
		radius.addCosine(5, 5, 0);
		sample.add(new Tube("tube", nodes1, radius, new NormalizedVariable(2), 1));
		return sample;
	}


}
