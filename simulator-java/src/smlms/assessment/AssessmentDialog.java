package smlms.assessment;


import ij.IJ;
import ij.gui.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import smlms.file.FluorophoreComponent;
import smlms.file.Fluorophores;
import additionaluserinterface.GridPanel;
import additionaluserinterface.Settings;
import additionaluserinterface.SpinnerDouble;
import additionaluserinterface.WalkBar;

public class AssessmentDialog extends JDialog implements ActionListener, ChangeListener, Runnable {

	private WalkBar							walk			= new WalkBar("(c) 2016 EPFL, BIG", false, false, true);
	private Settings						settings		= new Settings("localization-microscopy", IJ.getDirectory("plugins") + "smlm-2016.txt");
	private JButton							bnAssessment	= new JButton("Assessment");
	public JComboBox						cmbAlgo			= new JComboBox(new String[] {"Global Sorted NN", "N.N", "Hungarian"});
	public JComboBox						cmbLogging		= new JComboBox(new String[] {"Talk", "Verbose", "Mute"});
	public SpinnerDouble					spnPixelsize	= new SpinnerDouble(100, 0.01, 10000, 1);
	public SpinnerDouble					spnToleranceXY	= new SpinnerDouble(250, 0.01, 10000, 1);
	public SpinnerDouble					spnToleranceZ	= new SpinnerDouble(500, 0.01, 10000, 1);
	private JLabel							lblSize			= new JLabel("-");
	public JTextField						txtSoftware		= new JTextField("software", 10);
	public JTextField						txtDataset		= new JTextField("dataset", 10);
	private FluorophoreComponent				channels[]		= new FluorophoreComponent[2];
	private Thread							thread			= null;

	public static void main(String args[]) {
		new AssessmentDialog();
	}

	public AssessmentDialog() {
		super(new JFrame(), "Assessment");
			
		settings.record("assessment-spnPixelsize", spnPixelsize, "100");
		settings.record("assessment-spnToleranceXY", spnToleranceXY, "250");
		settings.record("assessment-spnToleranceZ", spnToleranceZ, "250");
		
		lblSize.setBorder(BorderFactory.createEtchedBorder());
		channels[0] = new FluorophoreComponent(settings, "Ground-truth");
		channels[1] = new FluorophoreComponent(settings, "Tested software");
		settings.loadRecordedItems();
			
		GridPanel pn1 = new GridPanel("Settings", 1);
		pn1.place(0, 0, new JLabel("Pixelsize"));
		pn1.place(0, 1, spnPixelsize);
		pn1.place(1, 0, new JLabel("Tolerance XY"));
		pn1.place(1, 1, spnToleranceXY);
		pn1.place(2, 0, new JLabel("Tolerance Z"));
		pn1.place(2, 1, spnToleranceZ);
		pn1.place(3, 0, new JLabel("Matching"));
		pn1.place(3, 1, cmbAlgo);
		pn1.place(4, 0, txtSoftware);
		pn1.place(4, 1, txtDataset);
		pn1.place(7, 0, cmbLogging);
		pn1.place(7, 1, bnAssessment);
		
		GridPanel main = new GridPanel(false);
		main.place(0, 0, channels[0]);
		main.place(1, 0, channels[1]);
		main.place(2, 0, pn1);
		main.place(3, 0, walk);

		add(main);
		pack();
		setModal(false);
		GUI.center(this);
		setVisible(true);

		spnPixelsize.addChangeListener(this);
		bnAssessment.addActionListener(this);
		walk.getButtonClose().addActionListener(this);
		updateInterface();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == walk.getButtonClose()) {
			settings.storeRecordedItems();
			dispose();
		}
		else if (e.getSource() == bnAssessment) {
			if (thread == null) {
				thread = new Thread(this);
				thread.setPriority(Thread.MIN_PRIORITY);
				thread.start();
			}
		}

		updateInterface();
	}

	public void run() {
		walk.reset();
		double pixelsize = spnPixelsize.get();
		double toleranceXY = spnToleranceXY.get();
		double toleranceZ = spnToleranceZ.get();
		Fluorophores refs[] = channels[0].getFluorophoresPerFrames();
		Fluorophores tests[] = channels[1].getFluorophoresPerFrames();
		int algo = cmbAlgo.getSelectedIndex();
		String logging = (String)cmbLogging.getSelectedItem();
		String dataset = txtDataset.getText();
		String software = txtDataset.getText();
		CompareLocalization cl = new CompareLocalization(walk, dataset, software, logging, refs, tests, algo, toleranceXY, pixelsize);
		cl.run();
	
		AssessmentTable table = new AssessmentTable(CompareResult.getResultsHeader());
		table.update(cl.getResults(), cl.getResult());
		table.show(1000, 100);
	
		walk.finish();
		thread = null;
	}

	private void updateInterface() {
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		updateInterface();
	}




}
