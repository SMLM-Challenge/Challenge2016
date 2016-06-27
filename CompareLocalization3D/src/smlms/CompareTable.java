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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class CompareTable extends JTable {

	private Color			colorOddRow		= new Color(245, 245, 250);
	private Color			colorEvenRow	= new Color(232, 232, 237);
	
	public CompareTable(ArrayList<String[]> data, String headers[], boolean headerVisible) {
		DefaultTableModel model = new DefaultTableModel();
		setModel(model);
		String[] h = new String[headers.length]; 
		model.setColumnIdentifiers(headerVisible ? headers : h);
		for (int i = 0; i < headers.length; i++) {
			TableColumn tc = getColumnModel().getColumn(i);
			tc.setCellRenderer(new AlternatedRowRenderer());
		}
		update(data);
	}

	public String getSelectedAtColumn(int col) {
		int row = getSelectedRow();
		if (row >= 0)
			return (String)getModel().getValueAt(row, col);
		else 
			return ""; 
	}
	
	public void setSelectedAtColumn(int col, String selection) {
		int nrows = this.getRowCount();
		for(int i=0; i<nrows; i++) {
			String name = (String)getModel().getValueAt(i, col);
			if (name.equals(selection))
				this.setRowSelectionInterval(i, i+1);
		}
	}
	
	public void update(ArrayList<String[]> data) {
		DefaultTableModel model = (DefaultTableModel) getModel();
		model.getDataVector().removeAllElements();
		for (String[] row : data)
			model.addRow(row);
		repaint();
	}
	
	public JScrollPane getPane(int width, int height) {
		setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		setPreferredScrollableViewportSize(new Dimension(width, height));
		setFillsViewportHeight(true);
		return new JScrollPane(this);
	}
	
	public void show(int width, int height, String title) {
		JFrame frame = new JFrame(title);
		frame.add(getPane(width, height));
		frame.pack();
		frame.setVisible(true);
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
