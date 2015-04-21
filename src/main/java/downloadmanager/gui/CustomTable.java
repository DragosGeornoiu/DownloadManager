package downloadmanager.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import downloadmanager.constants.Constants;

/**
 * Initializes the table where the details of the files stored on the given host
 * are.
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
				rowData[index][Constants.CHECK_COLUMN_POSITION] = false;
				;
				rowData[index][Constants.PROGRESS_COLUMN_POSITION] = "";
			}
		}

		sizeOfElements = options.length;

		DefaultTableModel tableModel = new DefaultTableModel(rowData, columnNames) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				if (column == 3) {
					return true;
				}

				return false;
			}
		};

		table = new JTable(tableModel) {
			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int column) {
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

			@Override
			public boolean isCellEditable(int row, int column) {
				return super.isCellEditable(row, column);
			}

		};

		table.getTableHeader().setReorderingAllowed(false);
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table.getModel());
		table.setRowSorter(sorter);
		Comparator<String> fileSizesComparator = new Comparator<String>() {
			@Override
			public int compare(String size1, String size2) {
				int value1, value2;
				String unit1, unit2;
				value1 = Integer.parseInt(size1.split(" ")[0]);
				unit1 = size1.split(" ")[1];

				value2 = Integer.parseInt(size2.split(" ")[0]);
				unit2 = size2.split(" ")[1];

				// EB, PB, TB, GB, MB, KB or bytes
				List<String> sizes = getListWithSizes();
				if (unit1.equals(unit2)) {
					if (value1 > value2) {
						return 1;
					} else if (value1 < value2) {
						return -1;
					}
					return 0;
				} else {
					int index;
					for (index = 0; index < sizes.size(); index++) {
						if (sizes.get(index).equals(unit1)) {
							break;
						}
					}

					int index2;
					for (index2 = 0; index2 < sizes.size(); index2++) {
						if (sizes.get(index2).equals(unit2)) {
							break;
						}
					}
					if (index > index2) {
						return -1;
					} else {
						return 1;
					}
				}
			}

		};

		sorter.setComparator(2, fileSizesComparator);
		add(new JScrollPane(table));
	}

	private List<String> getListWithSizes() {
		List<String> sizes = new ArrayList<String>();
		sizes.add(Constants.EB);
		sizes.add(Constants.PB);
		sizes.add(Constants.TB);
		sizes.add(Constants.GB);
		sizes.add(Constants.MB);
		sizes.add(Constants.KB);
		sizes.add(Constants.BYTES);
		return sizes;
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
		for (int i = 0; i < sizeOfElements; i++) {
			if (retVal(i, 0).equals(name)) {
				return i;
			}
		}
		return -1;
	}

	public void setTextAt(String text, int row, int column) {
		table.setValueAt(text, row, column);
	}

	public void setAllCheckBoxes(boolean selected) {
		for (int i = 0; i < sizeOfElements; i++) {
			if (table.isCellEditable(i, Constants.CHECK_COLUMN_POSITION)) {
				table.setValueAt(selected, i, Constants.CHECK_COLUMN_POSITION);
			}
		}
	}

	public void setAllProgressesToZero() {
		for (int i = 0; i < sizeOfElements; i++) {
			table.setValueAt("", i, Constants.PROGRESS_COLUMN_POSITION);
		}
	}

}
