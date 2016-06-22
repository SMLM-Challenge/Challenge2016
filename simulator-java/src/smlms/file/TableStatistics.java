package smlms.file;

import ij.IJ;

import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class TableStatistics extends JTable {

	private Statistics[] 	stats;
	private String 			name;
	private String[] 		headers = new String[] {"Feature", "Count", "Min", "Max", "Mean", "Stdev"};
	
	public TableStatistics(String name) {
		super();
		((DefaultTableModel) getModel()).setColumnIdentifiers(headers);
		setAutoCreateRowSorter(true);
		update();
	}

	public TableStatistics(String name, Statistics[] stats) {
		super();
		this.stats = stats;
		((DefaultTableModel) getModel()).setColumnIdentifiers(headers);
		setAutoCreateRowSorter(true);
		update();
	}

	public JScrollPane pane(int width, int height) {
		JScrollPane scrollpane = new JScrollPane(this);
		scrollpane.setPreferredSize(new Dimension(width, height));
		return scrollpane;
	}
	
	public void show(int width, int height) {
		JScrollPane pane = pane(width, height);
		JFrame frame = new JFrame(name);
		frame.add(pane);
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

	public void update(Statistics[] stats) {
		this.stats = stats;
		update();
	}

	public void update(ArrayList<Statistics> astats) {
		if (astats.size() <= 0)
			return;
		this.stats = new Statistics[astats.size()];
		for(int i=0; i<astats.size(); i++) 
			this.stats[i] = astats.get(i);	
		update();
	}

	public void update() {
		DefaultTableModel model = (DefaultTableModel) getModel();
		model.getDataVector().removeAllElements();
		if (stats == null)
			return;
		int nrow = stats.length;
		for (int j=0; j<nrow; j++) {
			Object o[] = new Object[6];
			o[0] = stats[j].name;
			o[1] = stats[j].count;
			o[2] = stats[j].min;
			o[3] = stats[j].max;
			o[4] = stats[j].mean;
			o[5] = stats[j].stdev;
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
				s += model.getValueAt(row, 0) + ",";
				s += model.getValueAt(row, 1) + ",";
				for (int col = 2; col < ncols - 1; col++) {
					try {
						s += IJ.d2s(Double.parseDouble(""+model.getValueAt(row, col)), 3) + ",";
					}
					catch(Exception ex) {
						s += model.getValueAt(row, col) + ",";
					}
				}
				try {
					s += IJ.d2s(Double.parseDouble(""+model.getValueAt(row, ncols-1)), 3);
				}
				catch(Exception ex) {
					s += model.getValueAt(row, ncols-1) + ",";
				}
				buffer.write(s + "\n");
			}
			buffer.close();
		}
		catch (IOException ex) {
			System.out.println("" + ex);
		}
	}
	
}
