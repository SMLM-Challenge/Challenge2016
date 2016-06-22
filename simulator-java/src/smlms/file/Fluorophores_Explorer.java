package smlms.file;

import ij.IJ;
import ij.gui.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;

import additionaluserinterface.GridPanel;
import additionaluserinterface.Settings;

public class Fluorophores_Explorer extends JDialog implements ActionListener {

	private Settings	settings		= new Settings("localization-microscopy", IJ.getDirectory("plugins") + "smlm-2016.txt");
	private JButton bnClose	 = new JButton("Close");	
	private FluorophoreComponent pane = new FluorophoreComponent(settings, "Source");

	public static void main(String args[]) {
		new Fluorophores_Explorer("name");
	}

	public Fluorophores_Explorer(String name) {
		super(new JFrame(), "Fluorophore Panel " + name);
		GridPanel main = new GridPanel(false);
		main.place(0, 0, pane);
		main.place(4, 0, bnClose);
		add(main);
		bnClose.addActionListener(this);
		pack();
		GUI.center(this);
		setModal(true);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == bnClose) 
			dispose();
	}
}
