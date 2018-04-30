package smlms.rendering;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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

public class RenderingTable extends JTable implements MouseListener {

	private Color			colorOddRow		= new Color(245, 245, 250);
	private Color			colorEvenRow	= new Color(232, 232, 237);
	private String[]		headers			= new String[] { 
		"Name", "Method", "Pixelsize", "FWHM", "X1", "X2", "Y1", "Y2", "Z1", "Z2", "Frame1", "Frame2", "Photons1", "Photons2" };
	
	private ArrayList<RenderingParameters> params;
	private RenderingDialog dialog;
	
	public RenderingTable(ArrayList<RenderingParameters> params, RenderingDialog dialog) {
		super();
		this.params = params;
		this.dialog = dialog;
			
		DefaultTableModel
		tableModel = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}

			@Override
			public Class<?> getColumnClass(int column) {
				if (column <= 2)
					return Integer.class;
				else if (column <= 6)
					return Double.class;
				return super.getColumnClass(column);
			}
		};

		setModel(tableModel);
		DefaultTableModel model = (DefaultTableModel) getModel();
		model.setColumnIdentifiers(headers);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setRowSelectionAllowed(true);
		setAutoCreateRowSorter(true);
		addMouseListener(this);

		for (int i = 0; i < headers.length; i++) {
			TableColumn tc = getColumnModel().getColumn(i);
			tc.setCellRenderer(new AlternatedRowRenderer());
		}

	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
		Component component = super.prepareRenderer(renderer, row, column);
		TableColumn tableColumn = getColumnModel().getColumn(column);
		if (column == 0)
			tableColumn.setPreferredWidth(200);
		return component;
	}

	public JScrollPane getPane(int width, int height) {
		JScrollPane scrollpane = new JScrollPane(this);
		scrollpane.setPreferredSize(new Dimension(width, height));
		return scrollpane;
	}
	
	public void show(int width, int height) {
		JScrollPane scrollpane = getPane(width, height);
		JFrame frame = new JFrame("History");
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
		for (RenderingParameters param : params)
			model.addRow(param.getObjectAsArray());
		repaint();
	}

	public RenderingParameters getSelected() {
		int row = getSelectedRow();
		
		if (row < 0) {
			setRowSelectionInterval(getRowCount()-1, getRowCount());
			row = getSelectedRow();
			if (row < 0)
				return null;
		}

		String name = (String)this.getModel().getValueAt(row, 0);
		ImagePlus imp = WindowManager.getImage(name);
		imp.setActivated();
		if (row >= 0)
			if (row < params.size())
				return params.get(row);
		return params.get(0);
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			JTable target = (JTable) e.getSource();
			int row = target.getSelectedRow();
			String name = (String)this.getModel().getValueAt(row, 0);
			ImagePlus imp = WindowManager.getImage(name);
			IJ.selectWindow(name);
			imp.setActivated();
			if (row >= 0)
				if (row < params.size())
					dialog.setParameters(params.get(row));
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
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
