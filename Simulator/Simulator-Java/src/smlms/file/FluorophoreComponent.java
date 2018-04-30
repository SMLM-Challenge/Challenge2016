package smlms.file;

import ij.IJ;
import imageware.ImageWare;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import smlms.rendering.Rendering;
import smlms.tools.ArrayOperations;
import smlms.tools.Chart;
import smlms.tools.Point3D;
import smlms.tools.Volume;
import additionaluserinterface.GridPanel;
import additionaluserinterface.Settings;
import additionaluserinterface.SpinnerDouble;

public class FluorophoreComponent extends JPanel implements ActionListener, Runnable {

	private Settings						settings;
	private JButton							bnBrowse		= new JButton("Browse");
	private JButton							bnLoad			= new JButton("Load");
	private JComboBox						cmbFormat		= new JComboBox();
	private JButton							bnPreview		= new JButton("Preview");
	private JButton							bnStats			= new JButton("Stats");

	private JButton							bnHisto			= new JButton("Histo");
	private JButton							bnEvolution		= new JButton("Frames");
	private boolean							descriptionToChoose = false;
	
	private SpinnerDouble					spnPixelsize	= new SpinnerDouble(40, 0.01, 1000, 1);
	public JTextField						txtFile			= new JTextField("-", 20);
	public JLabel							lblInfo			= new JLabel("----------------");
	
	private Fluorophores					fluos			= null;
	private Thread							thread			= null;
	private JTextField						lblFormat		= new JTextField("", 20);
	
	public FluorophoreComponent(Settings settings, String name) {
		descriptionToChoose = true;
		this.settings = settings;
		doDialog(name);
	}
	
	public FluorophoreComponent(Settings settings, String name, String formatForced) {
		this.settings = settings;
		cmbFormat.setSelectedItem(formatForced);
		descriptionToChoose = false;
		doDialog(name);
	}
	
	private void doDialog(String name) {
		settings.record("fluorophores-spnPixelsize-"+name, spnPixelsize, "100");
		settings.record("fluorophores-txtFile-"+name, txtFile, "");
		settings.record("fluorophores-cmbFormat-"+name, cmbFormat, "");
	
		lblInfo.setBorder(BorderFactory.createEtchedBorder());
		lblFormat.setBorder(BorderFactory.createEtchedBorder());
		lblFormat.setEditable(false);
		lblFormat.setBackground(new Color(220, 220, 250));
		cmbFormat.setEditable(true);
		String desc[] = Description.getRegisteredDescription();
		cmbFormat.addItem("#format#");
		for(int i=0; i<desc.length; i++)
			cmbFormat.addItem(desc[i]);

		GridPanel pn = new GridPanel(name, 1);
		pn.place(1, 0, 4, 1, txtFile);
		pn.place(1, 4, bnBrowse);
		if (descriptionToChoose) {
			pn.place(3, 4, 1, 1, cmbFormat);
			pn.place(3, 0, 4, 1, lblFormat);
		}
		pn.place(2, 0, 4, 1, lblInfo);
		pn.place(2, 4, bnLoad);
		pn.place(4, 0, bnEvolution);
		pn.place(4, 1, bnHisto);
		pn.place(4, 2, bnStats);
		pn.place(4, 3, bnPreview);
		pn.place(4, 4, spnPixelsize);
	
		add(pn);
	
		bnStats.addActionListener(this);
		bnBrowse.addActionListener(this);
		bnLoad.addActionListener(this);
		bnPreview.addActionListener(this);
		bnHisto.addActionListener(this);
		bnEvolution.addActionListener(this);
		cmbFormat.addActionListener(this);
		
		update();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
IJ.log(" " + e);	
		if (e.getSource() == cmbFormat) 
			update();
		else if (e.getSource() == bnBrowse) {
			JFileChooser fc = new JFileChooser();
			int ret = fc.showOpenDialog(null);
			if (ret == JFileChooser.APPROVE_OPTION) 
				txtFile.setText(fc.getSelectedFile().getAbsolutePath());
			update();
		}
		else if (e.getSource() == bnPreview) {
			if (fluos == null)
				return;
			Rendering rendering = new Rendering(null, spnPixelsize.get(), 1000000);
			Point3D dim = new Point3D(fluos.getXMax(100), fluos.getYMax(100), fluos.getZMax(100));
			Volume vol = new Volume(new Point3D(0, 0, 0), dim);
			ImageWare image = rendering.projection(fluos, Rendering.Method.GAUSSIAN, Rendering.Amplitude.PHOTONS, vol, spnPixelsize.get()*0.5);
			image.show("Render");
		}
		else if (e.getSource() == bnHisto) {
			if (fluos == null)
				return;
			boolean activeFields[] = new Description((String)cmbFormat.getSelectedItem()).getActiveFields();
			Statistics stats[] = fluos.getStats();
			for(int i=1; i<stats.length; i++) 
				if (activeFields[i]) {
					Chart chart = new Chart(stats[i].name, "Numbers of fluorophores", stats[i].domain);
					chart.add(stats[i].name, stats[i].histo);
					chart.show(stats[i].name, 800, 400);
				}
		}
		else if (e.getSource() == bnEvolution) {
			if (fluos == null)
				return;
			boolean activeFields[] = new Description((String)cmbFormat.getSelectedItem()).getActiveFields();
			Statistics stats[] = fluos.getStats();
			for(int i=1; i<stats.length; i++) 
				if (activeFields[i]) {
					double frames[] = ArrayOperations.ramp(stats[i].evolution.length);
					Chart chart = new Chart(stats[i].name, "Sum of " + stats[i].name, frames);
					chart.add(stats[i].name, stats[i].evolution);
					chart.show(stats[i].name, 800, 400);
				}
		}
		else if (e.getSource() == bnStats) {
IJ.log(" " + (fluos == null));
			if (fluos == null)
				return;
			 TableStatistics table = new TableStatistics("Fluorophores", fluos.getStats());
			 table.show(500, 300);
		}
		else if (e.getSource() == bnLoad) {
			if (thread == null) {
				thread = new Thread(this);
				thread.setPriority(Thread.MIN_PRIORITY);
				thread.start();
			}
		}
	}

	private void update() {
		Description desc = new Description((String)cmbFormat.getSelectedItem());
		lblFormat.setText(desc.getDecription());
	}
	
	public String[] getSource() {
		return new String[] {txtFile.getText(), (String)cmbFormat.getSelectedItem(), lblInfo.getText()};
	}
	
	public Fluorophores getFluorophores() {
		return fluos;
	}

	public void setFluorophores(Fluorophores fluos) {
		this.fluos = fluos;
	}

	public Fluorophores[] getFluorophoresPerFrames() {
		return fluos.reshapeInFrames();
	}
	
	public void run() {
		Description.getDescriptionPath();
		Description desc = new Description((String)cmbFormat.getSelectedItem());
		update();
		fluos = Fluorophores.load(txtFile.getText(), desc, lblInfo);
		fluos.computeStats();
		thread = null;
	}
}
