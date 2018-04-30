package smlms.rendering;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GUI;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import ij.process.LUT;
import imageware.ImageWare;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import smlms.file.FluorophoreComponent;
import smlms.file.Fluorophores;
import smlms.file.Statistics;
import smlms.tools.Point3D;
import smlms.tools.PsRandom;
import smlms.tools.Volume;
import additionaluserinterface.GridPanel;
import additionaluserinterface.Settings;
import additionaluserinterface.SpinnerDouble;
import additionaluserinterface.SpinnerInteger;
import additionaluserinterface.WalkBar;

public class RenderingDialog extends JDialog implements ActionListener, ChangeListener, Runnable {

	private WalkBar							walk			= new WalkBar("(c) 2016 EPFL, BIG", false, false, true);
	private Settings						settings		= new Settings("localization-microscopy",
			IJ.getDirectory("plugins") + "smlm-2016.txt");
	private JButton							bnRender3D		= new JButton("Render 3D");
	private JButton							bnRenderProj	= new JButton("Render 2D");
	private JButton							bnGetRoi		= new JButton("Get ROI");
	private JButton							bnAutoRoi		= new JButton("Full FOV");
	private JComboBox						cmbMethod		= new JComboBox(
			new String[] { "Gaussian", "Histogram", "Triangle" });

	private SpinnerInteger					spnTimeOut		= new SpinnerInteger(10, 0, 9999999, 1);
	private SpinnerDouble					spnPixelsize	= new SpinnerDouble(10, 0.01, 10000, 1);
	private SpinnerDouble					spnFWHM			= new SpinnerDouble(10, 0.01, 10000, 1);
	private JLabel							lblSize			= new JLabel("-");

	public String							names[]			= new String[] { "X", "Y", "Z", "Frame", "Photons" };
	public SpinnerDouble					spnOrigin[]		= new SpinnerDouble[5];
	public SpinnerDouble					spnEnd[]		= new SpinnerDouble[5];
	private FluorophoreComponent				channels[]		= new FluorophoreComponent[4];
	private JComboBox						cmbAmplitude[]	= new JComboBox[channels.length];
	private Thread							thread			= null;
	private JButton							job;
	private PsRandom						rand			= new PsRandom(1234);
	private ArrayList<RenderingParameters>	params			= new ArrayList<RenderingParameters>();
	private RenderingTable					table			= new RenderingTable(params, this);
	private JTabbedPane						tab				= new JTabbedPane();

	public static void main(String args[]) {
		new RenderingDialog();
	}

	public RenderingDialog() {
		super(new JFrame(), "Rendering");
			
		settings.record("rendering-spnTimeOut", spnTimeOut, "20");
		settings.record("rendering-spnPixelsize", spnPixelsize, "10");
		settings.record("rendering-spnFWHM", spnFWHM, "10");
		settings.record("rendering-cmbMethod", cmbMethod, (String) cmbMethod.getSelectedItem());
		
		lblSize.setBorder(BorderFactory.createEtchedBorder());
		for(int nc=0; nc<channels.length; nc++) {
			cmbAmplitude[nc]	= new JComboBox(new String[] {"None", "Unit", "Photons", "Frame", "X", "Y", "Z"});
			settings.record("rendering-cmbAmplitude"+nc, cmbAmplitude[nc], (String) cmbAmplitude[nc].getSelectedItem());
			channels[nc] = new FluorophoreComponent(settings, "Channel" + (nc+1));
			GridPanel pnF = new GridPanel(false, 1);
			pnF.place(0, 0, channels[nc]);
			pnF.place(1, 0, cmbAmplitude[nc]);
			tab.addTab("Channel" + (nc+1), pnF);
		}
		settings.loadRecordedItems();
			
		GridPanel pn1 = new GridPanel("Settings", 1);
		pn1.place(0, 0, new JLabel("Pixelsize"));
		pn1.place(0, 1, spnPixelsize);
		pn1.place(0, 2, lblSize);
		
		pn1.place(1, 0, new JLabel("FWHM"));
		pn1.place(1, 1, spnFWHM);
		pn1.place(1, 2, cmbMethod);
		pn1.place(3, 0, new JLabel("TimeOut"));
		pn1.place(3, 1, spnTimeOut);
		pn1.place(7, 0, bnRender3D);
		pn1.place(7, 1, bnRenderProj);

		GridPanel pn2 = new GridPanel("Selection", 1);
		for (int i = 0; i < names.length; i++) {
			pn2.place(1 + i, 0, new JLabel(names[i]));
			spnOrigin[i] = new SpinnerDouble(0, -999999, 999999, 100);
			spnEnd[i] = new SpinnerDouble(0, -999999, 999999, 100);
			pn2.place(1 + i, 1, spnOrigin[i]);
			pn2.place(1 + i, 2, spnEnd[i]);
			spnOrigin[i].addChangeListener(this);
			spnEnd[i].addChangeListener(this);
		}
		pn2.place(7, 1, bnGetRoi);
		pn2.place(7, 2, bnAutoRoi);
		pn2.place(8, 0, 3, 1, table.getPane(400, 200));

		GridPanel main = new GridPanel(false);
		main.place(0, 0, tab);
		main.place(2, 0, pn1);
		main.place(1, 0, pn2);
		main.place(3, 0, walk);

		add(main);
		pack();
		setModal(false);
		GUI.center(this);
		setVisible(true);

		spnPixelsize.addChangeListener(this);
		bnGetRoi.addActionListener(this);
		bnAutoRoi.addActionListener(this);
		bnRender3D.addActionListener(this);
		walk.getButtonClose().addActionListener(this);
		bnRenderProj.addActionListener(this);
		updateInterface();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == walk.getButtonClose()) {
			settings.storeRecordedItems();
			dispose();
		}
		else if (e.getSource() == bnRender3D || e.getSource() == bnRenderProj) {
			job = (JButton) e.getSource();
			if (thread == null) {
				thread = new Thread(this);
				thread.setPriority(Thread.MIN_PRIORITY);
				thread.start();
			}
		}
		else if (e.getSource() == bnGetRoi) {
			RenderingParameters params = table.getSelected();
			ImagePlus imp = WindowManager.getImage(params.name);
			if (imp == null) return;
			setParameters(params);
			double pixelsize = spnPixelsize.get();
			Roi roi = imp.getRoi();
			if (roi == null) {
				IJ.error("Select a ROI");
				return;
			}
			Rectangle rect = roi.getBounds();
			double x1 = rect.x * pixelsize + spnOrigin[0].get();
			double y1 = rect.y * pixelsize + spnOrigin[1].get();
			double z1 = spnOrigin[2].get();
			double x2 = x1 + rect.width * pixelsize;
			double y2 = y1 + rect.height * pixelsize;
			double z2 = spnEnd[2].get();
			double dx = spnEnd[0].get() + spnOrigin[0].get();
			double dy = spnEnd[1].get() + spnOrigin[1].get();
			double dz = z2 - z1;
			double diag1 = Math.sqrt(dx * dx + dy * dy);
			double diag2 = Math.sqrt(rect.width * rect.width + rect.height * rect.height) * pixelsize;
			spnPixelsize.set(round2(pixelsize * diag2 / diag1));
			spnOrigin[0].set(x1);
			spnOrigin[1].set(y1);
			spnEnd[0].set(x2);
			spnEnd[1].set(y2);

			if (imp.getStackSize() > 1) {
				int z = (int) Math.round(imp.getSlice() * pixelsize + z1);
				spnOrigin[2].set(z - dz / 4);
				spnEnd[2].set(z + dz / 4);
			}
		}
		else if (e.getSource() == bnAutoRoi) {
			for(int nc=0; nc<channels.length; nc++) {
				if (tab.getSelectedIndex() == nc) 
					resetLimits(channels[nc].getFluorophores());
			}
		}

		updateInterface();
	}

	public void run() {
		walk.reset();
		double pixelsize = spnPixelsize.get();
		double fwhm = spnFWHM.get();
		double timeout = spnTimeOut.get();
		Rendering rendering = new Rendering(walk, pixelsize, timeout);
		rendering.setLimitFrames(spnOrigin[3].get(), spnEnd[3].get());
		rendering.setLimitPhotons(spnOrigin[4].get(), spnEnd[4].get());
		Point3D origin = new Point3D(spnOrigin[0].get(), spnOrigin[1].get(), spnOrigin[2].get());
		Point3D end = new Point3D(spnEnd[0].get(), spnEnd[1].get(), spnEnd[2].get());
		Point3D dim = new Point3D(end.x - origin.x, end.y - origin.y, end.z - origin.z);
		Volume vol = new Volume(origin, dim);
		Rendering.Method method = Rendering.Method.values()[cmbMethod.getSelectedIndex()];

		ArrayList<ImageWare> images = new ArrayList<ImageWare>();
		if (job == this.bnRenderProj) {
			for(int nc = 0; nc<channels.length; nc++) {
				Fluorophores fluos = channels[nc].getFluorophores();
				Rendering.Amplitude amp = Rendering.Amplitude.values()[cmbAmplitude[nc].getSelectedIndex()];
				if (fluos != null && amp != Rendering.Amplitude.NONE)
					images.add(rendering.projection(fluos, method, amp, vol, fwhm));
			}
		}
		else {
			for(int nc = 0; nc<channels.length; nc++) {
				Fluorophores fluos = channels[nc].getFluorophores();
				Rendering.Amplitude amp = Rendering.Amplitude.values()[cmbAmplitude[nc].getSelectedIndex()];
				if (fluos != null && amp != Rendering.Amplitude.NONE)
					images.add(rendering.render(fluos, method, amp, vol, fwhm));
			}
		}

		if (images.size() == 0) 
			return;

		int nchannel = images.size();
		int nx = images.get(0).getSizeX();
		int ny = images.get(0).getSizeY();
		int nz = images.get(0).getSizeZ();
		IJ.log("nchannel " + nchannel + " " + nz);

		ImageStack stacks[] = new ImageStack[nchannel];
		for (int i = 0; i < nchannel; i++)
			stacks[i] = images.get(i).buildImageStack();
		IJ.log("stacks " + stacks.length);

		String name = "R-" + rand.nextInteger(1000) + "-" + pixelsize;
		ImagePlus imp = createComposite(nx, ny, nz, stacks);
		imp.setTitle(name);
		imp.show();

		params.add(getParameters(imp.getTitle()));
		table.update();

		walk.finish();
		thread = null;
	}

	public void setParameters(RenderingParameters param) {
		spnPixelsize.set(param.pixelsize);
		spnFWHM.set(param.fwhm);
		for (int k = 0; k < 5; k++) {
			spnOrigin[k].set(param.min[k]);
			spnEnd[k].set(param.max[k]);
		}
	}

	public RenderingParameters getParameters(String name) {
		double pixelsize = spnPixelsize.get();
		double fwhm = spnFWHM.get();
		return new RenderingParameters(name, (String) cmbMethod.getSelectedItem(), pixelsize, fwhm,
				new double[] { spnOrigin[0].get(), spnOrigin[1].get(), spnOrigin[2].get(), spnOrigin[3]
						.get(), spnOrigin[4].get() },
				new double[] { spnEnd[0].get(), spnEnd[1].get(), spnEnd[2].get(), spnEnd[3].get(), spnEnd[4].get() });
	}

	public void resetLimits(Fluorophores fluos) {
		IJ.log(" reset limits " + fluos.size());
		if (fluos == null) return;
		Statistics[] stats = fluos.getStats();
		for (int i = 0; i < 5; i++) {
			spnOrigin[i].set(stats[i + 1].min);
			spnEnd[i].set(stats[i + 1].max);
		}

	}

	private double complexity() {
		double dx = spnEnd[0].get() - spnOrigin[0].get();
		double dy = spnEnd[0].get() - spnOrigin[0].get();
		double dz = spnEnd[0].get() - spnOrigin[0].get();
		double px = spnPixelsize.get();
		return (dx / px * dy / px * dz / px) * 10e-6;
	}

	private void updateInterface() {
		lblSize.setText(IJ.d2s(complexity()) + " Mvoxels");
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		updateInterface();
	}

	private double round2(double a) {
		int e = (int) Math.log10(a);
		double m = a / Math.pow(10, e);
		int mi = ((int) Math.round(m * 100));
		return mi * Math.pow(10, e - 2);
	}

	public ImagePlus createComposite(int w, int h, int d, ImageStack[] stacks) {
		ImageStack composite = new ImageStack(w, h);
		int n = stacks.length;
		int[] index = new int[n];
		int channels = 0;
		boolean customColors = false;
		for (int i = 0; i < n; i++) {
			index[i] = 1;
			if (stacks[i] != null) {
				channels++;
				if (i > 0 && stacks[i - 1] == null) customColors = true;
			}
		}
		for (int i = 0; i < d; i++) {
			for (int j = 0; j < n; j++) {
				if (stacks[j] != null) {
					ImageProcessor ip = stacks[j].getProcessor(index[j]);
					composite.addSlice(null, ip);
					if (stacks[j] != null) stacks[j].deleteSlice(1);
				}
			}
		}
		ImagePlus imp2 = new ImagePlus("Composite", composite);
		imp2.setDimensions(channels, d, 1);
		imp2 = new CompositeImage(imp2, CompositeImage.COMPOSITE);
		if (customColors) {
			Color[] colors = { Color.red, Color.green, Color.blue, Color.white };
			CompositeImage ci = (CompositeImage) imp2;
			int color = 0;
			int c = 1;
			for (int i = 0; i < n; i++) {
				if (stacks[i] != null && c <= n) {
					ci.setPosition(c, 1, 1);
					LUT lut = ci.createLutFromColor(colors[color]);
					ci.setChannelLut(lut);
					c++;
				}
				color++;
			}
			ci.setPosition(1, 1, 1);
		}
		if (d > 1) imp2.setOpenAsHyperStack(true);
		return imp2;
	}

}
