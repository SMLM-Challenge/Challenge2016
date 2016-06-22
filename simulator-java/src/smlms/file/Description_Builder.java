package smlms.file;

import ij.IJ;
import ij.gui.GUI;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import additionaluserinterface.GridPanel;

public class Description_Builder extends JDialog implements ActionListener {

	private JButton					bnNew			= new JButton("New");
	private JButton					bnSave			= new JButton("Save");
	private JButton					bnClose			= new JButton("Close");
	private JButton					bnReload		= new JButton("Reload");

	private JList					lstFields;

	private JLabel					lblPath			= new JLabel(Description.getDescriptionPath());
	private JTable					table 			= new JTable();
	
	public static void main(String args[]) {
		new Description_Builder();
	}

	public Description_Builder() {
		super(new JFrame(), "Description Builder");

		DefaultTableModel model = ((DefaultTableModel)table.getModel());
		model.setColumnIdentifiers(new String[] {"File", "Description"});
		table.setAutoCreateRowSorter(true);
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		load();

		DefaultListModel modelList = new DefaultListModel();
		for (int i = 0; i < Fields.values().length; i++)
			modelList.addElement(Fields.values()[i].name());
		lstFields = new JList(modelList);
		lstFields.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstFields.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent ev) {
				if (ev.getClickCount() == 2) 
					IJ.log((String) lstFields.getSelectedValue());
			}
		});
		
		lblPath.setBorder(BorderFactory.createEtchedBorder());
		JScrollPane scroll = new JScrollPane(table);
		scroll.setPreferredSize(new Dimension(800, 200));
		GridPanel pn = new GridPanel("Description File", 1);
		pn.place(1, 0, 5, 1, lblPath);
		pn.place(2, 0, bnClose);
		pn.place(2, 1, bnNew);
		pn.place(2, 3, bnReload);
		pn.place(2, 4, bnSave);
		pn.place(3, 0, 5, 1, scroll);

		JScrollPane scFields = new JScrollPane(lstFields);
		scFields.setPreferredSize(new Dimension(100, 100));

		GridPanel pn1 = new GridPanel("Fields");
		pn1.place(0, 0, 1, 1, scFields);

		GridPanel main = new GridPanel(false);
		main.place(0, 0, pn);
		main.place(2, 0, pn1);
		add(main);
		
		bnNew.addActionListener(this);
		bnSave.addActionListener(this);
		bnClose.addActionListener(this);
		bnReload.addActionListener(this);
		
		pack();
		GUI.center(this);
		setModal(true);
		setVisible(true);
	}

	private void load() {
		String path = Description.getDescriptionPath();
		String desc[] = Description.getRegisteredDescription();
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		model.getDataVector().removeAllElements();
		for(int i=0; i<desc.length; i++) {
			String line;
			try {
				BufferedReader buffer = new BufferedReader(new FileReader(path + desc[i]));
				line = buffer.readLine();
				buffer.close();
			}
			catch(Exception ex) {
				line ="Error in reading " + path + desc[i];
			}
			model.addRow(new String[] {desc[i], line});
		}
	}
	
	public void save() {
		DefaultTableModel model = ((DefaultTableModel)table.getModel());
		
		for(int i=0; i<table.getRowCount(); i++) {
			IJ.log("saev " + i);
			String filename = (String)model.getValueAt(i, 0);
			String desc = (String)model.getValueAt(i, 1);
			try {
				BufferedWriter buffer = new BufferedWriter(new FileWriter(Description.getDescriptionPath() + filename));
				buffer.write(desc + "\n");
				buffer.close();
			}
			catch (Exception ex) {
				IJ.log("Error saving " + filename);
			}
		}
	}
	
	private void create() {
		int row = table.getSelectedRow();
		String filename = "Untitled";
		String desc = "# X Y FRAME;";
		if (row >= 0) {
			DefaultTableModel model = ((DefaultTableModel)table.getModel());
			filename = (String)model.getValueAt(row, 0) + "-copy";
			desc = (String)model.getValueAt(row, 1);
		}
		try {
			BufferedWriter buffer = new BufferedWriter(new FileWriter(Description.getDescriptionPath() + filename));
			buffer.write(desc + "\n");
			buffer.close();
		}
		catch (Exception ex) {
			IJ.log("Error saving " + filename);
		}
		save();
		load();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == bnReload)
			load();
		else if (e.getSource() == bnClose) {
			dispose();
		}
		else if (e.getSource() == bnSave) {
			save();
		}
		else if (e.getSource() == bnNew) 
			create();
	}

}
