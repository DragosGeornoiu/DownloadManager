package DownloadManager.GUI;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import DownloadManager.Constants.Constants;

/**
 * Initializes the table where the details of the files stored on the given hostname.
 */
public class CustomTable extends JPanel {
	private static final long serialVersionUID = 1L;
	private JTable table;
	private int sizeOfElements;

	public CustomTable(Object[] columns, String[] options, String[] types, String[] sizes) {
		setLayout(new BorderLayout());

		Object columnNames[] = columns;
		Object rowData[][] = new Object[options.length][5];

		JPanel content = new ScrollablePane(new GridBagLayout());
		content.setBackground(UIManager.getColor("List.background"));
		if (options.length > 0) {
			for (int index = 0; index <= options.length - 1; index++) {
				rowData[index][Constants.NAME_COLUMN_POSITION] = options[index];
				rowData[index][Constants.TYPE_COLUMN_POSITION] = types[index];
				rowData[index][Constants.SIZE_COLUMN_POSITION] = sizes[index];
				rowData[index][Constants.CHECK_COLUMN_POSITION] = false;;
				rowData[index][Constants.PROGRESS_COLUMN_POSITION] = "";
			}
		}
		
		sizeOfElements = options.length;
		
		// all cells aren't editable except the one where the check boxes are.
		DefaultTableModel tableModel = new DefaultTableModel(rowData, columnNames) {
			private static final long serialVersionUID = 1L;

			@Override
		    public boolean isCellEditable(int row, int column) {
		    	if(column == 3) {
		    		return true;
		    	}
		    		
		       return false;
		    }
		};
		
		// Checkboxes need the Boolean type and JTable auto renders it.
		table = new JTable(tableModel) {
			private static final long serialVersionUID = 1L;
			@Override
			public Class getColumnClass(int column) {
				switch (column) {
				case Constants.NAME_COLUMN_POSITION:
					return String.class;
				case Constants.TYPE_COLUMN_POSITION:
					return String.class;
				case Constants.SIZE_COLUMN_POSITION:
					return String.class;
				case Constants.PROGRESS_COLUMN_POSITION:
					return String.class;
				default:
					return Boolean.class;
				}
			}
		};
		
		add(new JScrollPane(table));
	}
	
	public Object retVal(int row, int col) {
		return table.getValueAt(row, col);
	}

	public class ScrollablePane extends JPanel implements Scrollable {
		private static final long serialVersionUID = 1L;

		public ScrollablePane(LayoutManager layout) {
			super(layout);
		}

		public ScrollablePane() {
		}

		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return new Dimension(100, 100);
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 32;
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 32;
		}

		@Override
		public boolean getScrollableTracksViewportWidth() {
			boolean track = false;
			Container parent = getParent();
			if (parent instanceof JViewport) {
				JViewport vp = (JViewport) parent;
				track = vp.getWidth() > getPreferredSize().width;
			}
			return track;
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
			boolean track = false;
			Container parent = getParent();
			if (parent instanceof JViewport) {
				JViewport vp = (JViewport) parent;
				track = vp.getHeight() > getPreferredSize().height;
			}
			return track;
		}
	}

	public int getSizeOfElements() {
		return sizeOfElements;
	}

	public void setSizeOfElements(int sizeOfElements) {
		this.sizeOfElements = sizeOfElements;
	}
	
	public int getIndexWhere(String name) {
		for(int i=0; i<sizeOfElements; i++) {
			if(retVal(i, 0).equals(name)) {
				return i;
			}
		}
		return -1;
	}
	
	public void setTextAt(String text, int row, int column) {
		table.setValueAt(text, row, column);
	}

	public void setAllCheckBoxes(boolean selected) {
		for(int i=0; i<sizeOfElements; i++) {
			table.setValueAt(selected, i, Constants.CHECK_COLUMN_POSITION);
		}
		
	}
}
