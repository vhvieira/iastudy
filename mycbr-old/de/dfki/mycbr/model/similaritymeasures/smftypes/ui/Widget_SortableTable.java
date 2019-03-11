/**
 MyCBR License 1.1

 Copyright (c) 2008
 Thomas Roth-Berghofer, Armin Stahl & Deutsches Forschungszentrum f&uuml;r K&uuml;nstliche Intelligenz DFKI GmbH
 Further contributors: myCBR Team (see http://mycbr-project.net/contact.html for further information 
 about the mycbr Team). 
 All rights reserved.

 MyCBR is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 Since MyCBR uses some modules, you should be aware of their licenses for
 which you should have received a copy along with this program, too.
 
 endOfLic**/
package de.dfki.mycbr.model.similaritymeasures.smftypes.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import de.dfki.mycbr.Helper;

/**
 * @author myCBR Team
 *
 */
public class Widget_SortableTable extends JPanel implements TableColumnModelListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(Widget_SortableTable.class.getName());

	
	public abstract class AbstractHeaderTableCellRenderer implements TableCellRenderer {
		private int selectedRow = -1;
		private int selectedCol = -1;
		
		public AbstractHeaderTableCellRenderer() {
			init();
		}
		
		public final Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (isSelected) {
				if (this.selectedRow!=row || this.selectedCol!=column) {
					// new selection
					this.selectedRow = row;
					this.selectedCol = column;
					
					selectedHeaderAt(this, row, selectedCol);
				}
			}
			
			isSelected = isSelected || (row==selectedRow && column==selectedCol);
			Component comp = getInternalTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			return comp;
		}

		public void clearSelection() {
			selectedRow = -1;
			selectedCol = -1;
		}
		
		public void updateSelection(int row, int col) {
			this.selectedRow = row;
			this.selectedCol = col;
		}
		
		public void init() {}
		
		public abstract Component getInternalTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int visualRow, int visualCol);
		
		public abstract int getPreferredCellHeight();
		public abstract int getPreferredCellWidth();
	}
	
	
	public class DefaultHeaderTableCellRenderer extends AbstractHeaderTableCellRenderer {
		JButton button = new JButton();
		
		public Component getInternalTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
			button.setMargin(new Insets(0,0,0,0));
			if (isSelected) {
				button.setSelected(true);
				ButtonModel model = button.getModel();
				model.setArmed(true);
				model.setPressed(true);
			} else {
				ButtonModel model = button.getModel();
				model.setArmed(false);
				model.setPressed(false);
				button.setSelected(false);
			}
			button.setText(value.toString());

			return button;
		}

		public int getPreferredCellHeight() {
			return -1;
		}

		public int getPreferredCellWidth() {
			return -1;
		}
	}

	/**
	 * Special TableModel to implement. 
	 * @author Daniel Bahls
	 */
	public class DefaultSortableTableModel extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private DefaultTableModel realTableModel = null;

		private Vector<Integer> tmpltIndexMapColumns = new Vector<Integer>();
		private Vector<Integer> tmpltIndexMapRows = new Vector<Integer>();

		/**
		 * the element at index i returns the real index j.
		 * index i is here the index in the 'visual' table.
		 * index j is here the index in the realtablemodel.
		 */
		private Vector<Integer> indexMapColumns = new Vector<Integer>();
		private Vector<Integer> indexMapRows = new Vector<Integer>();

		private int realSelectedColumnIndex = -1;
		private int realSelectedRowIndex = -1;
		
		private Vector<Object> colHeader;
		private Vector<Object> rowHeader;
		
		/** compares the column indices */
		private Comparator<Object> compCols = new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				// these objects are real indices of type Integer
				// for columns
				Integer int1 = (Integer) o1;
				Integer int2 = (Integer) o2;
				
				int realSelectedRowIndex = getRealSelectedRowIndex();
				Object concernedO1 = getRealValueAt(realSelectedRowIndex, int1.intValue());
				Object concernedO2 = getRealValueAt(realSelectedRowIndex, int2.intValue());
				
				return compareForColumnSort(int1.intValue(), int2.intValue(), concernedO1, concernedO2);
			}
		};
		
		/** compares the row indices */
		private Comparator<Object> compRows = new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				// these objects are real indices of type Integer
				// for rows
				Integer int1 = (Integer) o1;
				Integer int2 = (Integer) o2;
				
				int realSelectedColumnIndex = getRealSelectedColumnIndex();
				Object concernedO1 = getRealValueAt(int1.intValue(), realSelectedColumnIndex);
				Object concernedO2 = getRealValueAt(int2.intValue(), realSelectedColumnIndex);
				
				return compareForRowSort(int1.intValue(), int2.intValue(), concernedO1, concernedO2);
			}
		};
		
		
		
		@SuppressWarnings("unchecked") //$NON-NLS-1$
		public DefaultSortableTableModel(Vector data, Vector<Object> colHeader, Vector<Object> rowHeader) {
			realTableModel = new DefaultTableModel(data, colHeader);
			this.colHeader = new Vector<Object>(colHeader);
			this.rowHeader = new Vector<Object>(rowHeader);
			
			// init template index maps
			for (int i=0; i<colHeader.size(); i++) {
				tmpltIndexMapColumns.add(i);
			}
			for (int i=0; i<rowHeader.size(); i++) {
				tmpltIndexMapRows.add(i);
			}
			
			initIndexMaps();
		}

		public int getRowCount() {
			return realTableModel.getRowCount();
		}

		public int getColumnCount() {
			return realTableModel.getColumnCount();
		}

		public Object getValueAt(int row, int col) {
			int realRow = visualToRealRowIndex(row);
			int realCol = visualToRealColumnIndex(col);
			Object value = getRealValueAt(realRow, realCol);
			return value;
		}
		
		public void setValueAt(Object value, int row, int col) {
			int realRow = visualToRealRowIndex(row);
			int realCol = visualToRealColumnIndex(col);
			setRealValueAt(value, realRow, realCol);
			if (realCol==realSelectedColumnIndex || realRow==realSelectedRowIndex) {
				// sort again and update view
				sort();
				refresh();
			}
		}
		
		public String getColumnName(int index) {
			// NOTE: this one returns the visual columname!
//			return colHeader.get(realToVisualColumnIndex(index)).toString();
			return colHeader.get(visualToRealColumnIndex(index)).toString();
		}
		
		//
		//
		public int getRealSelectedColumnIndex() {
			return realSelectedColumnIndex;
		}

		public int getRealSelectedRowIndex() {
			return realSelectedRowIndex;
		}

		public void setRealSelectedColumnIndex(int realSelectedColumnIndex) {
//			log.info("["+realSelectedColumnIndex+"]");
			this.realSelectedColumnIndex = realSelectedColumnIndex;
			sort();
			fireTableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
			scrollSmart();
		}

		public void setRealSelectedRowIndex(int realSelectedRowIndex) {
			this.realSelectedRowIndex = realSelectedRowIndex;
			sort();
			fireTableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
			scrollSmart();
		}

		public Object getRealValueAt(int realRow, int realCol) {
			Object value = realTableModel.getValueAt(realRow, realCol);
			return value;
		}

		public void setRealValueAt(Object value, int realRow, int realCol) {
			realTableModel.setValueAt(value, realRow, realCol);
		}

		public int visualToRealColumnIndex(int visualColumnIndex) {
			if (visualColumnIndex < 0) {
				return -1;
			}
			return ((Integer) indexMapColumns.get(visualColumnIndex)).intValue();
		}
		
		public int visualToRealRowIndex(int visualRowIndex) {
			if (visualRowIndex < 0) {
				return -1;
			}
			return ((Integer) indexMapRows.get(visualRowIndex)).intValue();
		}
		
		public int realToVisualColumnIndex(int realColumnIndex) {
			for (int i=0; i<indexMapColumns.size(); i++) {
				if (((Integer) indexMapColumns.get(i)).intValue() == realColumnIndex) {
					return i;
				}
			}
			return -1;
		}
		
		public int realToVisualRowIndex(int realRowIndex) {
			for (int i=0; i<indexMapRows.size(); i++) {
				if (((Integer) indexMapRows.get(i)).intValue() == realRowIndex) {
					return i;
				}
			}
			return -1;
		}
		
		private void sort() {
			log.fine("Sort table"); //$NON-NLS-1$
			//
			// sort column indices
			//
			if (getRealSelectedRowIndex() >= 0) {
				Collections.sort(indexMapColumns, compCols);
			}
			
			//
			// sort row indices
			//
			if (getRealSelectedColumnIndex() >= 0) {
				Collections.sort(indexMapRows, compRows);
			}
			
		}
		
		@SuppressWarnings("unchecked") //$NON-NLS-1$
		protected int compareForColumnSort(int realCol1, int realCol2, Object concernedO1, Object concernedO2) {
			if (concernedO1.getClass().equals(concernedO2.getClass()) && concernedO1 instanceof Comparable) return ((Comparable)concernedO1).compareTo(concernedO2);
			log.info("cannot compare different classes... use String compare: val1=[" + concernedO1 + "," + concernedO1.getClass().getName() + "] val2=[" + concernedO2 + "," + concernedO2.getClass().getName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			return concernedO1.toString().compareTo(concernedO2.toString());
		}

		@SuppressWarnings("unchecked") //$NON-NLS-1$
		protected int compareForRowSort(int realRow1, int realRow2, Object concernedO1, Object concernedO2) {
			if (concernedO1.getClass().equals(concernedO2.getClass()) && concernedO1 instanceof Comparable) {
				return ((Comparable)concernedO1).compareTo(concernedO2);
			}
			log.info("cannot compare different classes... use String compare: val1=[" + concernedO1 + "," + concernedO1.getClass().getName() + "] val2=[" + concernedO2 + "," + concernedO2.getClass().getName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			return concernedO1.toString().compareTo(concernedO2.toString());
		}

		@SuppressWarnings("unchecked") //$NON-NLS-1$
		private void initIndexMaps() {
			indexMapColumns.clear();
			indexMapRows.clear();
			realSelectedColumnIndex=-1;
			realSelectedRowIndex=-1;
			
			indexMapColumns=(Vector) tmpltIndexMapColumns.clone();
			indexMapRows=(Vector) tmpltIndexMapRows.clone();
			fireTableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
		}

		@SuppressWarnings("unchecked") //$NON-NLS-1$
		public Vector getColumnHeader() {
			return colHeader;
		}

		@SuppressWarnings("unchecked") //$NON-NLS-1$
		public Vector getRowHeader() {
			return rowHeader;
		}

		public void addColumn(Object columnObject) {
			realTableModel.addColumn(columnObject);
			// add to header
			colHeader.add(columnObject);
			// give it an index
			tmpltIndexMapColumns.add(new Integer(tmpltIndexMapColumns.size()));
			initIndexMaps();
//			sort();
			setModel(this);
			refresh();
			fireTableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
		}

		public void setColumnHeaderObject(int i, Object o) {
			colHeader.set(i, o);
			realTableModel.setColumnIdentifiers(colHeader);
			refresh();
		}
		
		public void removeColumns(int amount) {
			if (amount <= 0) return;
			int index = colHeader.size()-amount;
			realTableModel.setColumnCount(index);
			for (int i=0; i<amount; i++) {
				colHeader.remove(index);
				tmpltIndexMapColumns.remove(index);
			}
			initIndexMaps();
			setModel(this);
			refresh();
			fireTableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
		}
		
		public void quickMoveNow(int from, int to) {
			// swap
			int tmp = indexMapColumns.get(to);
			indexMapColumns.set(to, indexMapColumns.get(from));
			indexMapColumns.set(from, tmp);
		}
		
		@SuppressWarnings("unchecked") //$NON-NLS-1$
		public void movedColumn(int from, int to) {
			
			// commented out before 20.10.2008
//			if (e.getFromIndex()== e.getToIndex()) return;
//			log.info("MOVED COLUMN! "+from+" "+to);
			
			// now, current order is the normal order
			tmpltIndexMapColumns = (Vector) indexMapColumns.clone();
			
//			tmpltIndexMapColumns = newTmpltIndexMapCols;
			
			initIndexMaps();
			setModel(this);
			refresh();
			fireTableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
		}

		@SuppressWarnings("unchecked") //$NON-NLS-1$
		public void addRow(Object rowObject, Vector row) {
			realTableModel.addRow(row);
			// add to header
			rowHeader.add(rowObject);
			// give it an index
			tmpltIndexMapRows.add(new Integer(tmpltIndexMapRows.size()));
			initIndexMaps();
//			sort();
			setModel(this);
			refresh();
			fireTableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
		}

		@SuppressWarnings("unchecked") //$NON-NLS-1$
		public void insertRow(Object rowObject, Vector row, int rowIndex) {
			realTableModel.addRow(row);
			// add to header
//			rowHeader.insertElementAt(rowObject, rowIndex);
			rowHeader.add(rowObject);
		// give it an index
			tmpltIndexMapRows.insertElementAt(new Integer(tmpltIndexMapRows.size()), rowIndex);
			initIndexMaps();
//			sort();
			setModel(this);
			refresh();
			fireTableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
		}

	}

	private JScrollPane scrollpaneTaData = null;

	private JPanel paContent = null;
	private JTable taHeaderRows = null;
	private JTableHeader taHeaderColumns = null;
	private JTable taData = null;
	private JButton buClear;
	
	private AbstractHeaderTableCellRenderer headerTableCellRendererColumns  = new DefaultHeaderTableCellRenderer();
	private AbstractHeaderTableCellRenderer headerTableCellRendererRows   = new DefaultHeaderTableCellRenderer();
	
	private DefaultSortableTableModel tableModel = null;

	private int moveColumnFrom = -1;
	private int moveColumnTo   = -1;
	
	/**
	 * This is the default constructor
	 */
	public Widget_SortableTable() {
		super();
		initialize();
		
		taHeaderColumns.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				doMoveColumnModifications();
			}
		});
		
		Helper.addFocusListener_ConfirmChanges(taData);
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setLayout(new BorderLayout());
		this.setSize(300, 200);
		this.add(getPaContentVert(), java.awt.BorderLayout.CENTER);
	}


	public void scrollSmart() {
		int x = (headerTableCellRendererColumns.selectedCol < 0 ? 0 : headerTableCellRendererColumns.selectedCol);
		int y = (headerTableCellRendererRows.selectedRow < 0 ? 0 : headerTableCellRendererRows.selectedRow);
		taData.scrollRectToVisible(taData.getCellRect(y, x, true));
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public void setModel(DefaultSortableTableModel tm) { //, Vector headerData) {
		log.fine("set model to sortableTable"); //$NON-NLS-1$
		this.tableModel = tm;

		// HEADER HORIZONTAL
		Vector tableDataHeaderHoriz = new Vector();
		tableDataHeaderHoriz.add((tm.getColumnHeader() == null ? null : tm.getColumnHeader().clone()));
		
		// HEADER VERTICAL
		Vector headerDataVert = tableModel.getRowHeader();
		Vector tableDataHeaderVert = new Vector();
		Vector vizHeaderDataVert = new Vector();
		for (int i = 0; i < headerDataVert.size(); i++) {
			Vector tmp = new Vector();
			tmp.add(headerDataVert.get(i));
			tableDataHeaderVert.add(tmp);
		}
		vizHeaderDataVert.add(""); //$NON-NLS-1$
		taHeaderRows.setModel(new DefaultTableModel(tableDataHeaderVert, vizHeaderDataVert) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {
				return false;
			}
		});

		// DATA TABLE
		taData.setModel(tm);
		taData.setRowSelectionAllowed(false);
		
		refresh();
		revalidate();
		repaint();
		
		taData.createDefaultColumnsFromModel();
		taData.getColumnModel().addColumnModelListener(this);
	}

	protected void clearSelection() {
		tableModel.initIndexMaps();
		refreshHeaderData();
		taHeaderRows.clearSelection();
		
		headerTableCellRendererColumns.clearSelection();
		headerTableCellRendererRows.clearSelection();
		
		revalidate();
		repaint();
	}

	/**
	 * This method initializes paContentVert	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPaContentVert() {
		if (paContent == null) {
			paContent = new JPanel();
			paContent.setLayout(new BorderLayout());
			paContent.add(getScrollpaneTaData(), BorderLayout.CENTER);
		}
		return paContent;
	}

	/**
	 * This method initializes taHeaderVert	
	 * 	
	 * @return javax.swing.JTable	
	 */
	public JTable getTaHeaderRows() {
		if (taHeaderRows == null) {
			taHeaderRows = new JTable();
			taHeaderRows.setDefaultRenderer(Object.class, headerTableCellRendererRows);
			taHeaderRows.setRowSelectionAllowed(false);
			taHeaderRows.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			taHeaderRows.setCellSelectionEnabled(true);
		}
		return taHeaderRows;
	}

	/**
	 * This method initializes taHeaderHoriz	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTableHeader getTaHeaderColumns() {
		if (taHeaderColumns == null) {
			taHeaderColumns = getTable().getTableHeader();
			
			taHeaderColumns.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					int visualCol = taHeaderColumns.columnAtPoint(e.getPoint());
					headerTableCellRendererColumns.updateSelection(-1, visualCol);
					selectedHeaderAt(headerTableCellRendererColumns, -1, visualCol);
				}
			});
		}
		return taHeaderColumns;
	}

	
	private JScrollPane getScrollpaneTaData() {
		if (scrollpaneTaData == null) {
			scrollpaneTaData = new JScrollPane();
			
			// work around:
			// if we initialize the scrollpane directly with the jtable,
			// the table does some special configuring work which is NOT OF OUR INTEREST.
			// so we switch a JPanel in between.
			JPanel tmp = new JPanel();
			tmp.setLayout(new BorderLayout());
			tmp.add(getTable(), BorderLayout.CENTER);
			scrollpaneTaData.setViewportView(tmp);
			
			scrollpaneTaData.setCorner(JScrollPane.UPPER_LEFT_CORNER, createCornerComponent());
			JPanel tmp1 = new JPanel();
			tmp1.setLayout(new BorderLayout());
			tmp1.add(getTaHeaderColumns());
			scrollpaneTaData.setColumnHeaderView(tmp1);
			
			JPanel tmp2 = new JPanel();
			tmp2.setLayout(new BorderLayout());
			tmp2.add(getTaHeaderRows());
			tmp2.setPreferredSize(new Dimension(200, 400));
			scrollpaneTaData.setRowHeaderView(tmp2);
			
			scrollpaneTaData.setBorder(null);
			scrollpaneTaData.getVerticalScrollBar().setUnitIncrement(7);
			
		}
		return scrollpaneTaData;
	}
	
	protected Component createCornerComponent() {
		return getResetViewButton();
	}

	protected JButton getResetViewButton() {
		if (buClear == null) {
			buClear = new JButton(Messages.getString("Reset")); //$NON-NLS-1$
			buClear.setSize(100, 25);
			buClear.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					clearSelection();
				}
			});
		}
		return buClear;
	}

	/**
	 * This method initializes taData	
	 * 	
	 * @return javax.swing.JTable	
	 */
	public JTable getTable() {
		if (taData == null) {
			taData = createTable();
			taData.getTableHeader().setReorderingAllowed(false);
		}
		return taData;
	}
	
	protected JTable createTable() {
		return new JTable();
	}
	
	private void refresh() {
		log.fine("refresh sortable table"); //$NON-NLS-1$
		refreshHeaderData();
	}
	
	public void selectedHeaderAt(AbstractHeaderTableCellRenderer renderer, int row, int col) {
		log.fine("selected header at [" + row + "," + col + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (renderer == headerTableCellRendererColumns) {
			int realColumnIndex = tableModel.visualToRealColumnIndex(col);
			rowsWillBeSorted(realColumnIndex);
			tableModel.setRealSelectedColumnIndex(realColumnIndex);
		} else if (renderer == headerTableCellRendererRows) {
			int realRowIndex = tableModel.visualToRealRowIndex(row);
			columnsWillBeSorted(realRowIndex);
			tableModel.setRealSelectedRowIndex(realRowIndex);
		}
		refresh();
		
		revalidate();
		repaint();
	}

	public void rowsWillBeSorted(int realColumnIndex) {
		log.fine("rows will be sorted"); //$NON-NLS-1$
		// override this method if you want to do something before this sort.
	}

	public void columnsWillBeSorted(int realRowIndex) {
		log.fine(Messages.getString("30")); //$NON-NLS-1$
		// override this method if you want to do something before this sort.
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private void refreshHeaderData() {
		if (taData.isEditing()) {
			log.fine("cell editor is a [" + taData.getCellEditor(taData.getEditingRow(), taData.getEditingColumn()) + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			taData.getCellEditor(taData.getEditingRow(), taData.getEditingColumn()).cancelCellEditing();
		}

		Vector rowHeader = tableModel.getRowHeader();
		for (int i=0; i<rowHeader.size(); i++) {
			taHeaderRows.setValueAt(rowHeader.get(tableModel.visualToRealRowIndex(i)), i, 0);
		}
		
		headerTableCellRendererColumns.updateSelection(-1, tableModel.realToVisualColumnIndex(tableModel.getRealSelectedColumnIndex()));
		headerTableCellRendererRows.updateSelection(tableModel.realToVisualRowIndex(tableModel.getRealSelectedRowIndex()), 0);
		taHeaderRows.changeSelection(tableModel.realToVisualRowIndex(tableModel.getRealSelectedRowIndex()), 0, false, false);
		taData.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		taData.doLayout();
	}

	public void setHeaderTableCellRendererColumns(AbstractHeaderTableCellRenderer headerTableCellRendererColumns) {
		this.headerTableCellRendererColumns = headerTableCellRendererColumns;
		taHeaderColumns.setDefaultRenderer(headerTableCellRendererColumns);
		int prefHeight = headerTableCellRendererColumns.getPreferredCellHeight();
		if (prefHeight >= 0) {
			taHeaderColumns.setPreferredSize(new Dimension(taHeaderColumns.getPreferredSize().width, prefHeight));
		}
	}

	public void setHeaderTableCellRendererRows(AbstractHeaderTableCellRenderer headerTableCellRendererRows) {
		this.headerTableCellRendererRows = headerTableCellRendererRows;
		taHeaderRows.setDefaultRenderer(Object.class, headerTableCellRendererRows);
		int prefWidth = headerTableCellRendererRows.getPreferredCellWidth();
		if (prefWidth >= 0) {
			taHeaderRows.getColumnModel().getColumn(0).setPreferredWidth(prefWidth);
		}
	}

	public void setRealValueAt(Object value, int realRow, int realColumn) {
		tableModel.setRealValueAt(value, realRow, realColumn);
	}

	public boolean isDraggingColumns() {
		return moveColumnFrom >= 0;
	}
	

	//
	// IMPL of interface TableColumnModelListener
	//
	public void columnMoved(TableColumnModelEvent e) {
		if (e.getFromIndex() == e.getToIndex()) {
			return;
		}
		if (e.getToIndex() == moveColumnTo) {
			return;
		}
//		tableModel.quickMoveNow(e.getFromIndex(), e.getToIndex());
		if (moveColumnFrom < 0) {
			moveColumnFrom = e.getFromIndex();
		}
		moveColumnTo = e.getToIndex();
	}

	protected void doMoveColumnModifications() {
		if (moveColumnFrom < 0) {
			return;
		}
		log.fine("move column [" + moveColumnFrom + "] to [" + moveColumnTo + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		tableModel.quickMoveNow(moveColumnFrom, moveColumnTo);
		tableModel.movedColumn(moveColumnFrom, moveColumnTo);
		moveColumnFrom = -1;
		moveColumnTo = -1;
	}

	public void columnAdded(TableColumnModelEvent arg0) {
	}
	
	public void columnRemoved(TableColumnModelEvent arg0) {
	}
	
	public void columnMarginChanged(ChangeEvent arg0) {
	}
	
	public void columnSelectionChanged(ListSelectionEvent arg0) {
	}
	//
	// END interface impl

}
