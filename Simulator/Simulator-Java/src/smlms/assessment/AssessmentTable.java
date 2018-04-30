package smlms.assessment;

import ij.IJ;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class AssessmentTable extends JTable {

	private Color			colorOddRow		= new Color(245, 245, 250);
	private Color			colorEvenRow	= new Color(232, 232, 237);
	private String			name = "Untitled";
	
	public AssessmentTable(String headers[]) {
		super();
		
		DefaultTableModel tableModel = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		setModel(tableModel);
		DefaultTableModel model = (DefaultTableModel) getModel();
		model.setColumnIdentifiers(headers);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setRowSelectionAllowed(true);
		setAutoCreateRowSorter(true);

		for (int i = 0; i < headers.length; i++)
			getColumnModel().getColumn(i).setCellRenderer(new AlternatedRowRenderer());
	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
		Component component = super.prepareRenderer(renderer, row, column);
		TableColumn tableColumn = getColumnModel().getColumn(column);
		if (column == 0)
			tableColumn.setPreferredWidth(10);
		return component;
	}

	public JScrollPane getPane(int width, int height) {
		JScrollPane scrollpane = new JScrollPane(this);
		scrollpane.setPreferredSize(new Dimension(width, height));
		return scrollpane;
	}
	
	public void show(int width, int height) {
		JScrollPane scrollpane = getPane(width, height);
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

	public void update(ArrayList<CompareResult> results) {
		DefaultTableModel model = (DefaultTableModel) getModel();
		model.getDataVector().removeAllElements();
		int count = 1;
		for(CompareResult result : results) 
			model.addRow(result.getResultsValues(count++));
	}
	
	public void update(CompareResult results[]) {
		DefaultTableModel model = (DefaultTableModel) getModel();
		model.getDataVector().removeAllElements();
		for(int i=0; i<results.length; i++) 
			model.addRow(results[i].getResultsValues(i));
	}

	public void update(CompareResult results[], CompareResult result) {
		DefaultTableModel model = (DefaultTableModel) getModel();
		model.getDataVector().removeAllElements();
		for(int i=0; i<results.length; i++) 
			model.addRow(results[i].getResultsValues(i));
			name = "Frame-to-frame-" + result.dataset + "-" +  result.software + "J:" + 
			IJ.d2s(result.jaccard, 4) + " XY:" + IJ.d2s(result.rmseLateral, 4) + " Z:" + IJ.d2s(result.rmseAxial, 4);
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

	class ColumnHeaderToolTips extends MouseMotionAdapter {
		TableColumn						curCol;
		HashMap<TableColumn, String>	tips	= new HashMap<TableColumn, String>();

		public void setToolTip(TableColumn col, String tooltip) {
			if (tooltip == null)
				tips.remove(col);
			else
				tips.put(col, tooltip);
		}

		@Override
		public void mouseMoved(MouseEvent evt) {
			JTableHeader header = (JTableHeader) evt.getSource();
			JTable table = header.getTable();
			TableColumnModel colModel = table.getColumnModel();
			int vColIndex = colModel.getColumnIndexAtX(evt.getX());
			TableColumn col = null;
			if (vColIndex >= 0) {
				col = colModel.getColumn(vColIndex);
			}
			if (col != curCol) {
				header.setToolTipText((String) tips.get(col));
				curCol = col;
			}
		}
	}

}
