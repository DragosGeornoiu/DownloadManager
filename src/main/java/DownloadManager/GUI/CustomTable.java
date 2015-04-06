package DownloadManager.GUI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

/**
 * 
 * Initializes the table where the details of the files stored on the given hostname.
 *
 */
public class CustomTable extends JPanel {
	private static final long serialVersionUID = 1L;
	private List<JCheckBox> checkBoxes;
	private JTable table;

	public CustomTable(Object[] columns, String[] options, String[] types, String[] sizes) {
		checkBoxes = new ArrayList<JCheckBox>(options.length);
		setLayout(new BorderLayout());

		Object columnNames[] = columns;
		Object rowData[][] = new Object[options.length][4];

		JPanel content = new ScrollablePane(new GridBagLayout());
		content.setBackground(UIManager.getColor("List.background"));
		if (options.length > 0) {
			for (int index = 0; index <= options.length - 1; index++) {
				JCheckBox cb = new JCheckBox(options[index]);
				cb.setOpaque(false);
				checkBoxes.add(cb);
				rowData[index][0] = cb.getText();
				rowData[index][1] = types[index];
				rowData[index][2] = sizes[index];
				rowData[index][3] = false;;
			}
		}
		
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
				case 0:
					return String.class;
				case 1:
					return String.class;
				case 2:
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

	public List<JCheckBox> getCheckBoxes() {
		return checkBoxes;
	}

	public void setCheckBoxes(List<JCheckBox> checkBoxes) {
		this.checkBoxes = checkBoxes;
	}

}
