package smlms.tools;


import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public class TableDouble extends JTable {

	private Color			colorOddRow		= new Color(245, 245, 250);
	private Color			colorEvenRow	= new Color(232, 232, 237);
	private double[][] 		data;
	private String 			name;
	private String[]		firstcol;
	private String[]		headers;
	
	public TableDouble(String name, String[] firstcol, String[] headers, double[][] data) {
		super();
		this.name = name;
		this.data = data;
		this.firstcol = firstcol;
		this.headers = headers;
IJ.log(" firstcol " + firstcol.length);
IJ.log(" headers " + headers.length);
IJ.log(" data " + data.length);
IJ.log(" data " + data[0].length);

		DefaultTableModel
		tableModel = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		setModel(tableModel);
		DefaultTableModel model = (DefaultTableModel) getModel();
		model.setColumnIdentifiers(headers);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setRowSelectionAllowed(true);
		setAutoCreateRowSorter(true);

		for (int i = 0; i < headers.length; i++) {
			TableColumn tc = getColumnModel().getColumn(i);
			tc.setCellRenderer(new AlternatedRowRenderer());
		}
		update();
	}

	public void show(int width, int height) {
		JScrollPane scrollpane = new JScrollPane(this);
		scrollpane.setPreferredSize(new Dimension(width, height));
		JFrame frame = new JFrame(name);
		frame.add(scrollpane);
		frame.pack();
		frame.setVisible(true);
	}

	public int getRow(int id) {
		for (int row = 0; row < getRowCount(); row++) {
			if (((Integer)getValueAt(row, 0)) == id)
				return row;
		}
		return -1;
	}

	public void update() {
		DefaultTableModel model = (DefaultTableModel) getModel();
		model.getDataVector().removeAllElements();
		if (data == null)
			return;
		int ncol = data[0].length;
		int nrow = data.length;
		for (int j=0; j<nrow; j++) {
			Object o[] = new Object[ncol+1];
			o[0] = firstcol[j];
			for(int i=0; i<ncol; i++) {
				double d = data[j][i];
				o[i+1] = (Object)d;
			}
			model.addRow(o);
		}
		repaint();
	}
	
	public void saveCVS(String filename) {
		File file = new File(filename);

		try {
			BufferedWriter buffer = new BufferedWriter(new FileWriter(file));
			String s = "";
			for (int i = 0; i < headers.length; i++)
				s += headers[i] + ",";
			buffer.write(s + "\n");
			TableModel model = this.getModel(); 
			int ncols = model.getColumnCount();
			int nrows = model.getRowCount();
			for (int row = 0; row < nrows; row++) {
				s = "";
				for (int col = 0; col < ncols - 1; col++)
					s += model.getValueAt(row, col) + ",";
				s += model.getValueAt(row, ncols - 1);
				buffer.write(s + "\n");
			}
			buffer.close();
		}
		catch (IOException ex) {
			System.out.println("" + ex);
		}
	}

	public double[] getSelected() {
		int row = getSelectedRow();
		String name = (String)this.getModel().getValueAt(row, 0);
		ImagePlus imp = WindowManager.getImage(name);
		imp.setActivated();
		if (row >= 0)
			if (row < data.length)
				return data[row];
		return null;
	}
	
	public class AlternatedRowRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
			if (!isSelected)
				c.setBackground(row % 2 == 0 ? colorEvenRow : colorOddRow);
			return c;
		}
	}

}
