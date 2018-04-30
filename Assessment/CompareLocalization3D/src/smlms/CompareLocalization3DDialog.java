//=========================================================================================
//
// Single-Molecule Localization Microscopy Challenge 2016
// http://bigwww.epfl.ch/smlm/
//
// Author: 
// Daniel Sage, http://bigwww.epfl.ch/sage/
// Biomedical Imaging Group (BIG)
// Ecole Polytechnique Federale de Lausanne (EPFL), CH-1015 Lausanne, Switzerland
//
// Reference: 
// D. Sage, H. Kirshner, T. Pengo, N. Stuurman, J. Min, S. Manley, M. Unser
// Quantitative Evaluation of Software Packages for Single-Molecule Localization Microscopy 
// Nature Methods 12, August 2015.
// 
// Conditions of use: 
// You'll be free to use this software for research purposes, but you 
// should not redistribute it without our consent. In addition, we expect you to include a
// citation or acknowledgment whenever you present or publish results that are based on it.
//
//=========================================================================================

package smlms;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import additionaluserinterface.GridPanel;
import additionaluserinterface.Settings;
import additionaluserinterface.SpinnerDouble;
import additionaluserinterface.SpinnerInteger;
import additionaluserinterface.WalkBar;

public class CompareLocalization3DDialog extends JDialog implements WindowListener, ActionListener {

	private WalkBar 			walk 			= new WalkBar("(c) Biomedical Imaging Group, EPFL 2016", false, false, false);
	private String[]			listUnit		= new String[] { "pixel", "nm" };
	private Settings			settings		= new Settings("CompareLocalization", "CompareLocalizationSettings.txt");
	private JButton				bnClose			= new JButton("Close");
	private JButton				bnCompare		= new JButton("Run (8 assessments)");
	private JButton				bnBrowseWobble	= new JButton("Browse");

	private JButton				bnBrowse[]		= new JButton[] { new JButton("Browse"), new JButton("Browse") };
	private JButton				bnLoad[]		= new JButton[] { new JButton("Load"), new JButton("Load") };
	private JTextField			txtFile[]		= new JTextField[] { new JTextField("filename.csv"), new JTextField("filename.csv") };
	private JLabel				lblFile[]		= new JLabel[] { new JLabel("..."), new JLabel("...") };

	private SpinnerDouble		shiftX[]		= new SpinnerDouble[] { new SpinnerDouble(0, -1000000, 100000, 1), new SpinnerDouble(0, -1000000, 100000, 1) };
	private SpinnerDouble		shiftY[]		= new SpinnerDouble[] { new SpinnerDouble(0, -1000000, 100000, 1), new SpinnerDouble(0, -1000000, 100000, 1) };
	private SpinnerDouble		shiftZ[]		= new SpinnerDouble[] { new SpinnerDouble(0, -1000000, 100000, 1), new SpinnerDouble(0, -1000000, 100000, 1) };
	private SpinnerInteger		shiftF[]		= new SpinnerInteger[] { new SpinnerInteger(0, -1000000, 100000, 1), new SpinnerInteger(0, -1000000, 100000, 1) };
	private JComboBox			cmbUnit[]		= new JComboBox[] { new JComboBox(listUnit), new JComboBox(listUnit) };
	private SpinnerInteger		colF[]			= new SpinnerInteger[] { new SpinnerInteger(0, -1, 1000, 1), new SpinnerInteger(0, -10, 1000, 1) };
	private SpinnerInteger		colX[]			= new SpinnerInteger[] { new SpinnerInteger(0, -1, 1000, 1), new SpinnerInteger(0, -10, 1000, 1) };
	private SpinnerInteger		colY[]			= new SpinnerInteger[] { new SpinnerInteger(0, -1, 1000, 1), new SpinnerInteger(0, -10, 1000, 1) };
	private SpinnerInteger		colZ[]			= new SpinnerInteger[] { new SpinnerInteger(0, -1, 1000, 1), new SpinnerInteger(0, -10, 1000, 1) };
	private SpinnerInteger		colI[]			= new SpinnerInteger[] { new SpinnerInteger(0, -1, 1000, 1), new SpinnerInteger(0, -10, 1000, 1) };
	private SpinnerInteger		firstRow[]		= new SpinnerInteger[] { new SpinnerInteger(1, 0, 1000, 1), new SpinnerInteger(1, 0, 1000, 1) };

	private SpinnerDouble		spnToleranceXY	= new SpinnerDouble(10, 0, 10000, 10);
	private SpinnerDouble		spnToleranceZ	= new SpinnerDouble(10, 0, 10000, 10);
	private SpinnerDouble		spnPixelsize	= new SpinnerDouble(100, 0, 10000, 10);
	private SpinnerDouble		spnZStep		= new SpinnerDouble(10, 0, 10000, 10);
	private SpinnerDouble		spnMinPhotons2	= new SpinnerDouble(1000, 0, 100000, 10);
	private SpinnerDouble		spnFieldOfViewX = new SpinnerDouble(6400, 0, 100000, 10);
	private SpinnerDouble		spnFieldOfViewY	= new SpinnerDouble(6400, 0, 100000, 10);
	private SpinnerDouble		spnBorderXY		= new SpinnerDouble(300, 0, 10000, 10);
	private JLabel				lblCorrectionX0	= new JLabel("----");
	private JLabel				lblCorrectionY0	= new JLabel("----");

	private JTextField			txtWooble		= new JTextField("no", 10);

	private JTextField			txtName[]		= new JTextField[] { new JTextField("Ground-truth"), new JTextField("Untitled") };
	private JTextField			txtDataset		= new JTextField("Dataset name");

	public CompareLocalization3DDialog() {
		super(new Frame(), "Compare Localization 3D (19.06.2016)");
		walk.setPreferredSize(new Dimension(250, 20));
		for (int i = 0; i < 2; i++) {
			settings.record("txtFile" + i, txtFile[i], "/Users/dsage/Desktop/samples");
			settings.record("shiftX" + i, shiftX[i], "0");
			settings.record("shiftY" + i, shiftY[i], "0");
			settings.record("shiftZ" + i, shiftZ[i], "0");
			settings.record("shiftF" + i, shiftF[i], "0");
			settings.record("locationUnit" + i, cmbUnit[i], "nm");
			settings.record("colF" + i, colF[i], "1");
			settings.record("colX" + i, colX[i], "2");
			settings.record("colY" + i, colY[i], "3");
			settings.record("colZ" + i, colZ[i], "4");
			settings.record("colI" + i, colI[i], "5");
			settings.record("firstRow" + i, firstRow[i], "1");
		}
		settings.record("txtWooble", txtWooble, "");
		settings.record("spnToleranceXY", spnToleranceXY, "250");
		settings.record("spnToleranceZ", spnToleranceZ, "500");
		settings.record("spnPixelsize", spnPixelsize, "100");
		settings.record("spnZStep", spnZStep, "10");
		settings.record("spnFieldoFViewX", spnFieldOfViewX, "6400");
		settings.record("spnFieldoFViewY", spnFieldOfViewY, "6400");

		settings.loadRecordedItems();

		GridPanel panels[] = new GridPanel[] { new GridPanel(false), new GridPanel(false) };
		GridPanel cols[] = new GridPanel[] { new GridPanel("Columns", 2), new GridPanel("Columns", 2) };
		GridPanel shift[] = new GridPanel[] { new GridPanel("Shift", 2), new GridPanel("Shift", 2) };

		lblCorrectionX0.setBorder(BorderFactory.createEtchedBorder());
		lblCorrectionY0.setBorder(BorderFactory.createEtchedBorder());
	
		for (int i = 0; i < 2; i++) {
			txtFile[i].setPreferredSize(new Dimension(350, 22));
			txtFile[i].setCaretPosition(txtFile[i].getText().length());
			cols[i].place(0, 0, "Header row");
			cols[i].place(0, 1, firstRow[i]);
			cols[i].place(1, 0, "Frame column");
			cols[i].place(1, 1, colF[i]);
			cols[i].place(2, 0, "X column");
			cols[i].place(2, 1, colX[i]);
			cols[i].place(3, 0, "Y column");
			cols[i].place(3, 1, colY[i]);
			cols[i].place(4, 0, "Z column");
			cols[i].place(4, 1, colZ[i]);
			cols[i].place(5, 0, "Intensity column");
			cols[i].place(5, 1, colI[i]);
			JLabel lbl1 = new JLabel("-1 if not used, col index starts at 0");
			lbl1.setBorder(BorderFactory.createEtchedBorder());
			cols[i].place(6, 0, 2, 1, lbl1);

			shift[i].place(0, 0, "Name");
			shift[i].place(0, 1, txtName[i]);
			shift[i].place(1, 0, "Frame");
			shift[i].place(1, 1, shiftF[i]);
			shift[i].place(2, 0, "X");
			shift[i].place(2, 1, shiftX[i]);
			shift[i].place(3, 0, "Y");
			shift[i].place(3, 1, shiftY[i]);
			shift[i].place(4, 0, "Z");
			shift[i].place(4, 1, shiftZ[i]);
			shift[i].place(5, 0, "Unit");
			shift[i].place(5, 1, cmbUnit[i]);
			JLabel lbl2 = new JLabel("Origin at the upper-left corner");
			lbl2.setBorder(BorderFactory.createEtchedBorder());
			shift[i].place(6, 0, 2, 1, lbl2);
	
			lblFile[i].setBorder(BorderFactory.createEtchedBorder());
			panels[i].place(1, 0, 4, 1, txtFile[i]);
			panels[i].place(2, 0, 2, 1, lblFile[i]);
			panels[i].place(2, 2, bnBrowse[i]);
			panels[i].place(2, 3, bnLoad[i]);

			panels[i].place(4, 0, 2, 1, cols[i]);
			panels[i].place(4, 2, 2, 1, shift[i]);
			bnLoad[i].addActionListener(this);
			bnBrowse[i].addActionListener(this);
		}
		
		JLabel lblPhotons1 = new JLabel("0 (all points)");
		lblPhotons1.setBorder(BorderFactory.createEtchedBorder());
		
		GridPanel pnRun = new GridPanel("Settings");
		pnRun.place(0, 0, new JLabel("Pixelsize"));
		pnRun.place(0, 1, spnPixelsize);
		pnRun.place(0, 2, new JLabel("nm"));
		pnRun.place(1, 0, new JLabel("Tolerance XY"));
		pnRun.place(1, 1, spnToleranceXY);
		pnRun.place(1, 2, new JLabel("nm"));
		pnRun.place(2, 0, new JLabel("Min. Photons 1"));
		pnRun.place(2, 1, lblPhotons1);
		pnRun.place(2, 2, new JLabel("(ref)"));
		pnRun.place(3, 0, new JLabel("Min. Photons 2"));
		pnRun.place(3, 1, spnMinPhotons2);
		pnRun.place(3, 2, new JLabel("(ref)"));
		pnRun.place(4, 0, new JLabel("FoV in X"));
		pnRun.place(4, 1, spnFieldOfViewX);
		pnRun.place(4, 2, new JLabel("nm"));
		pnRun.place(5, 0, new JLabel("FoV in Y"));
		pnRun.place(5, 1, spnFieldOfViewY);
		pnRun.place(5, 2, new JLabel("nm"));
		pnRun.place(6, 0, new JLabel("Excluded Border"));
		pnRun.place(6, 1, spnBorderXY);
		pnRun.place(6, 2, new JLabel("nm"));
	
		GridPanel pn3D = new GridPanel("3D");
		
		pn3D.place(0, 0, new JLabel("Z-step"));
		pn3D.place(0, 1, spnZStep);
		pn3D.place(1, 0, new JLabel("Tolerance Z"));
		pn3D.place(1, 1, spnToleranceZ);
		
		GridPanel pnW = new GridPanel("Wobble Correction");
		pnW.place(2, 0, 1, 1, "Wobble file");
		pnW.place(2, 1, bnBrowseWobble);
		pnW.place(3, 0, 2, 1, txtWooble);
		
		JLabel lbl3 = new JLabel("Depth-Dependent Lateral Distorsion ");
		lbl3.setBorder(BorderFactory.createEtchedBorder());
		JLabel lbl4 = new JLabel("Correction is applied only on the reference");
		lbl4.setBorder(BorderFactory.createEtchedBorder());
		pnW.place(4, 0, 2, 1, lbl3);
		pnW.place(5, 0, 2, 1, lbl4);

		GridPanel pnButton = new GridPanel(false, 1);
		pnButton.place(5, 1, txtDataset);
		pnButton.place(5, 2, bnClose);
		pnButton.place(5, 4, bnCompare);

		JTabbedPane tab = new JTabbedPane();
		tab.add("Reference", panels[0]);
		tab.add("Test", panels[1]);

		GridPanel pnMain = new GridPanel(false, 3);
		pnMain.place(2, 0, 2, 1, tab);
		pnMain.place(3, 0, 1, 2, pnRun);
		pnMain.place(3, 1, 1, 1, pn3D);
		pnMain.place(4, 1, 1, 1, pnW);
		pnMain.place(5, 0, 2, 1, pnButton);
		pnMain.place(6, 0, 2, 1, walk);

		addWindowListener(this);
		bnClose.addActionListener(this);
		bnCompare.addActionListener(this);
		bnBrowseWobble.addActionListener(this);

		add(pnMain);
		pack();
		setResizable(false);
		setVisible(true);

		// Center
		Dimension screen = getScreenSize();
		Dimension window = getSize();
		if (window.width == 0)
			return;
		int left = screen.width / 2 - window.width / 2;
		int top = (screen.height - window.height) / 4;
		if (top < 0)
			top = 0;
		setLocation(left, top);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == bnClose) {
			bnLoad[0].removeActionListener(this);
			bnBrowse[0].removeActionListener(this);
			bnLoad[1].removeActionListener(this);
			bnBrowse[1].removeActionListener(this);
			bnClose.removeActionListener(this);
			bnCompare.removeActionListener(this);
			bnBrowseWobble.removeActionListener(this);
			settings.storeRecordedItems();
			dispose();
			System.exit(0);
		}
		else if (e.getSource() == bnBrowseWobble)
			browseWooble();
		else if (e.getSource() == bnLoad[0])
			load(0, spnBorderXY.get(), spnFieldOfViewX.get()-spnBorderXY.get(), spnBorderXY.get(), spnFieldOfViewY.get()-spnBorderXY.get());
		else if (e.getSource() == bnBrowse[0])
			browseFile(0);
		else if (e.getSource() == bnLoad[1])
			load(1, spnBorderXY.get(), spnFieldOfViewX.get()-spnBorderXY.get(), spnBorderXY.get(), spnFieldOfViewY.get()-spnBorderXY.get());
		else if (e.getSource() == bnBrowse[1])
			browseFile(1);
		else if (e.getSource() == bnCompare) 
			compare();
	}

	public void compare() {
		ArrayList<String[]> results = new ArrayList<String[]>();
		results.add(CompareLocalization3D.getHeaders());
		File fileRef = new File(txtFile[0].getText());
		File fileTst = new File(txtFile[1].getText());
		Description desca = getDescription(0);
		Description descb = getDescription(1);
		double minPhotons1 = 0;
		double minPhotons2 = spnMinPhotons2.get();
		Wobble wobble = new Wobble(txtWooble.getText());
		lblCorrectionX0.setText(""+wobble.getCorrectionAt0()[0]);
		lblCorrectionY0.setText(""+wobble.getCorrectionAt0()[1]);
		
		if (fileRef.exists() && fileTst.exists()) {
			walk.reset();
			String dataset = txtDataset.getText();
			double border = spnBorderXY.get();
			double txy = spnToleranceXY.get();
			double tz = spnToleranceZ.get();
			Fluorophores[] ar = load(0, border, spnFieldOfViewX.get()-border, border, spnFieldOfViewY.get()-border);
			Fluorophores[] a = Fluorophores.crop(ar, border, spnFieldOfViewX.get()-border, border, spnFieldOfViewY.get()-border);
			
			Fluorophores[] b = load(1, border, spnFieldOfViewX.get()-border, border, spnFieldOfViewY.get()-border);
			int algo = CompareLocalization3D.ALGO_GLOBAL_SORT_NEAREST_NEIGHBORHOOR;
			
			CompareLocalization3D comparator = new CompareLocalization3D(desca.name, a);

			results.add(comparator.run(walk, descb.name, b, 1, dataset, algo, true , null,   minPhotons1, txy, tz));				
			results.add(comparator.run(walk, descb.name, b, 2, dataset, algo, false, null,   minPhotons1, txy, tz));
			results.add(comparator.run(walk, descb.name, b, 3, dataset, algo, true , wobble, minPhotons1, txy, tz));
			results.add(comparator.run(walk, descb.name, b, 4, dataset, algo, false, wobble, minPhotons1, txy, tz));
			
			results.add(comparator.run(walk, descb.name, b, 5, dataset, algo, true , null,   minPhotons2, txy, tz));				
			results.add(comparator.run(walk, descb.name, b, 6, dataset, algo, false, null,   minPhotons2, txy, tz));
			results.add(comparator.run(walk, descb.name, b, 7, dataset, algo, true , wobble, minPhotons2, txy, tz));
			results.add(comparator.run(walk, descb.name, b, 8, dataset, algo, false, wobble, minPhotons2, txy, tz));
		
			CompareTable table = new CompareTable(results, CompareLocalization3D.getHeaders(), false);
			table.show(1200, 200, "Compare " + desca.name + " vs. " + descb.name);
			walk.finish("" + desca.name + " vs " + descb.name);
		}
	}
	
	private Description getDescription(int i) {
		Description desc = new Description();
		desc.name 		= txtName[i].getText();
		desc.pixelsize 	= cmbUnit[i].getSelectedItem().equals("nm") ? 1 : spnPixelsize.get();
		desc.zstep 		= cmbUnit[i].getSelectedItem().equals("nm") ? 1 : spnZStep.get();
		desc.shiftX 	= shiftX[i].get();
		desc.shiftY 	= shiftY[i].get();
		desc.shiftZ 	= shiftZ[i].get();
		desc.shiftFrame = shiftF[i].get();
		desc.colX 		= colX[i].get();
		desc.colY 		= colY[i].get();
		desc.colZ 		= colZ[i].get();
		desc.colFrame 	= colF[i].get();
		desc.colIntensity = this.colI[i].get();
		desc.firstRow = this.firstRow[i].get();
		return desc;
	}

	private Fluorophores[] load(int i, double x1, double x2, double y1, double y2) {
		Description desc = getDescription(i);
		LocalizationFile loc = new LocalizationFile();
		Fluorophores[] fluorophoresRead = loc.read(desc, txtFile[i].getText());
		Fluorophores[] fluorophores = Fluorophores.crop(fluorophoresRead, x1, x2, y1, y2);
		int errors = loc.getNbErrors();
		double xmax = -Double.MAX_VALUE;
		double ymax = -Double.MAX_VALUE;
		double zmax = -Double.MAX_VALUE;
		int fmax = -Integer.MAX_VALUE;
		double imax = -Double.MAX_VALUE;
		double xmin = Double.MAX_VALUE;
		double ymin = Double.MAX_VALUE;
		double zmin = Double.MAX_VALUE;
		int fmin = Integer.MAX_VALUE;
		double imin = Double.MAX_VALUE;
		int count = 0;
		for (int f = 0; f < fluorophores.length; f++) {
			for (Fluorophore fluo : fluorophores[f]) {
				xmax = Math.max(xmax, fluo.xnano);
				ymax = Math.max(ymax, fluo.ynano);
				zmax = Math.max(zmax, fluo.znano);
				fmax = Math.max(fmax, fluo.frame);
				imax = Math.max(imax, fluo.photons);
				xmin = Math.min(xmin, fluo.xnano);
				ymin = Math.min(ymin, fluo.ynano);
				zmin = Math.min(zmin, fluo.znano);
				fmin = Math.min(fmin, fluo.frame);
				imin = Math.min(imin, fluo.photons);
				count++;
			}
		}
		ArrayList<String[]> data = new ArrayList<String[]>();
		data.add(new String[] { "X", "" + xmin, "" + xmax });
		data.add(new String[] { "Y", "" + ymin, "" + ymax });
		data.add(new String[] { "Z", "" + zmin, "" + zmax });
		data.add(new String[] { "Frame", "" + fmin, "" + fmax });
		data.add(new String[] { "Intensity", "" + imin, "" + imax });
		CompareTable table = new CompareTable(data, new String[] { "Feature", "Minimum", "Maximum" }, true);
		table.show(200, 200, desc.name);
		lblFile[i].setText("Fluos: " + count + " Errors:" + errors);
		return fluorophores;
	}

	
	private void browseFile(int index) {
		JFileChooser chooser = new JFileChooser(txtFile[index].getText());
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle("Open a localization file CSV, TAB, ...");
		int ret = chooser.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
			String name = chooser.getSelectedFile().getAbsolutePath();
			txtFile[index].setText(name);
			txtFile[index].setCaretPosition(name.length());
		}
	}

	private void browseWooble() {
		JFileChooser chooser = new JFileChooser(txtWooble.getText());
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle("Open a Wooble correction file *.csv");
		int ret = chooser.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
			String name = chooser.getSelectedFile().getAbsolutePath();
			txtWooble.setText(name);
			txtWooble.setCaretPosition(name.length());
		}
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		dispose();
		System.exit(0);
	}

	private Dimension getScreenSize() {
		if (GraphicsEnvironment.isHeadless())
			return new Dimension(0, 0);
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gd = ge.getScreenDevices();
		GraphicsConfiguration[] gc = gd[0].getConfigurations();
		Rectangle bounds = gc[0].getBounds();
		if (bounds.x == 0 && bounds.y == 0)
			return new Dimension(bounds.width, bounds.height);
		else
			return Toolkit.getDefaultToolkit().getScreenSize();
	}

}
