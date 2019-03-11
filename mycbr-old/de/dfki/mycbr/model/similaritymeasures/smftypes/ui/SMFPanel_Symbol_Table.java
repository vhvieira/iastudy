/*
 * myCBR License 1.1
 *
 * Copyright (c) 2008
 * Thomas Roth-Berghofer, Armin Stahl & Deutsches Forschungszentrum f&uuml;r K&uuml;nstliche Intelligenz DFKI GmbH
 * Further contributors: myCBR Team (see http://mycbr-project.net/contact.html for further information 
 * about the myCBR Team). 
 * All rights reserved.
 *
 * myCBR is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Since myCBR uses some modules, you should be aware of their licenses for
 * which you should have received a copy along with this program, too.
 *
 * endOfLic**/
package de.dfki.mycbr.model.similaritymeasures.smftypes.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.MatteBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import de.dfki.mycbr.CBRProject;
import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueAcceptorImpl;
import de.dfki.mycbr.explanation.DefaultSMExplanation;
import de.dfki.mycbr.explanation.SMExplanationContainer;
import de.dfki.mycbr.explanation.ExplanationManager;
import de.dfki.mycbr.explanation.SlotStatistic;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.HasChangedListenerSMF;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Symbol_Table;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.Widget_SortableTable.DefaultHeaderTableCellRenderer;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.Widget_SortableTable.DefaultSortableTableModel;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.Widget_Symmetry.SymmetryModeListener;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelSlot;

/**
 * @author myCBR Team
 *
 */
public class SMFPanel_Symbol_Table extends SMFPanel implements HasChangedListenerSMF, SymmetryModeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(SMFPanel_Symbol_Table.class.getName());

	private Widget_SortableTable paTable;
	private JTable innerTable;
	private JTextField cellEditorTxtField;
	private DefaultSortableTableModel tableModel;
	private Widget_Symmetry paSymmetry;
	private JCheckBoxMenuItem miCBOccurences;
	private JCheckBoxMenuItem miSimilarities;

	private SMF_Symbol_Table smf;

	//
	// for explanation purposes
	//
	private ModelCls domainCls;
	long clsUsage;

	/**
	* configure selection manager
	* Seriously, there is no way to select multiple cells. Even though, the methods
	* to do so are given: 
	* setSelectionMode, setRowSelectionAllowed, setColumnSelectionAllowed, setCellSelectionEnabled
	* See Java Bug Database: 6314530 
	*/
	private CellSelectionManager cellSelectionManager = new CellSelectionManager();

	
	private class MouseControl implements MouseListener, MouseMotionListener {
		protected Rectangle draggingRect;

		private void initDraggingRect(int row, int column) {
			draggingRect = new Rectangle();
			draggingRect.x = column;
			draggingRect.y = row;
		}

		public void mouseMoved(MouseEvent e) {
			int col = innerTable.columnAtPoint(e.getPoint());
			int row = innerTable.rowAtPoint(e.getPoint());
			
			String tooltip = null;
			if (col>-1 && row>-1) {
				col = tableModel.visualToRealColumnIndex(col);
				row = tableModel.visualToRealRowIndex(row);
				tooltip = String.format(Messages.getString("q_,c_"), tableModel.getRowHeader().get(row), tableModel.getColumnHeader().get(col)); //$NON-NLS-1$
			}
			
			innerTable.setToolTipText(tooltip);
		}
		
		public void mouseDragged(MouseEvent e) {
			if (e.isControlDown() || (e.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) {
				return;
			}
			
			Point p = e.getPoint();
			int visualRow = innerTable.rowAtPoint(p);
			int visualCol = innerTable.columnAtPoint(p);
			if (visualRow<0 || visualCol <0) {
				return;
			}
			if (draggingRect == null) {
				initDraggingRect(visualRow, visualCol);
			}
			boolean symMode = getSmf().isSymmetricMode();
			boolean updateSelection = (draggingRect.x +draggingRect.width == visualCol) || (draggingRect.y +draggingRect.height == visualRow); 
			
			if (updateSelection) {
				int tmpx = draggingRect.x;
				int tmpy = draggingRect.y;
				int tmpwidth = draggingRect.width;
				int tmpheight= draggingRect.height;
				if (tmpwidth < 0) {
					tmpwidth  = -tmpwidth;
					tmpx-=tmpwidth;
				}
				if (tmpheight < 0) {
					tmpheight = -tmpheight;
					tmpy-=tmpheight;
				}

				for (int y=0; y<=tmpheight; y++) {
					int row = tmpy + y;
					for (int x=0; x<=tmpwidth; x++) {
						int column = tmpx + x;
						cellSelectionManager.removeSelectedCell(tableModel.visualToRealRowIndex(row), tableModel.visualToRealColumnIndex(column));
					}
				}
				if (symMode) {
					for (int y=0; y<=tmpheight; y++) {
						int row = tmpy + y;
						for (int x=0; x<=tmpwidth; x++) {
							int column = tmpx + x;
							cellSelectionManager.removeSelectedCell(tableModel.visualToRealRowIndex(column), tableModel.visualToRealColumnIndex(row));
						}
					}
				}
			}
			draggingRect.width  = visualCol	-draggingRect.x;
			draggingRect.height = visualRow	-draggingRect.y;

			int tmpx = draggingRect.x;
			int tmpy = draggingRect.y;
			int tmpwidth = draggingRect.width;
			int tmpheight= draggingRect.height;
			if (tmpwidth < 0) {
				tmpwidth  = -tmpwidth;  
				tmpx -= tmpwidth;
			}
			if (tmpheight < 0) {
				tmpheight = -tmpheight;
				tmpy-=tmpheight;
			}

			if (updateSelection) {
				for (int y = 0; y <= tmpheight; y++) {
					int row = tmpy + y;
					for (int x = 0; x <= tmpwidth; x++) {
						int column = tmpx + x;
						cellSelectionManager.addSelectedCell(tableModel.visualToRealRowIndex(row), tableModel.visualToRealColumnIndex(column));
					}
				}
				if (symMode) {
					for (int y = 0; y <= tmpheight; y++) {
						int row = tmpy + y;
						for (int x = 0; x <= tmpwidth; x++) {
							int column = tmpx + x;
							cellSelectionManager.addSelectedCell(tableModel.visualToRealRowIndex(column), tableModel.visualToRealColumnIndex(row));
						}
					}
				}
			}
			innerTable.repaint();
		}

		public void mousePressed(MouseEvent e) {
			// if <right button> do nothing 
			if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) {
				return;
			}
			
			Point p = e.getPoint();
			int row = innerTable.rowAtPoint(p);
			int column = innerTable.columnAtPoint(p);
			if (row<0 || column <0) {
				return;
			}
			if (cellSelectionManager.isSelected(row, column) && innerTable.getEditingColumn()==column && innerTable.getEditingRow()==row) {
				return;
			}
			if (!e.isControlDown()) {
				cellSelectionManager.clear();
			}
			if (getSmf().isSymmetricMode()) {
				cellSelectionManager.toggleSelectedCell(tableModel.visualToRealColumnIndex(column), tableModel.visualToRealRowIndex(row));
			}
			cellSelectionManager.toggleSelectedCell(tableModel.visualToRealRowIndex(row), tableModel.visualToRealColumnIndex(column));
			
			initDraggingRect(row, column);
			innerTable.repaint();
		}
		
		public void mouseReleased(MouseEvent e) {
			// stop dragging
			draggingRect = null;
		}

		public void mouseClicked(MouseEvent e) {}

		public void mouseEntered(MouseEvent e) {}

		public void mouseExited(MouseEvent e) {}
	
	}
	
	private class ExplanationAndColoringTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;
		JPanel panel = new JPanel();
		JLabel laExp = new JLabel(Messages.getString("100%")); //$NON-NLS-1$
		Dimension dim;
		MatteBorder border = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.blue);
		
		public ExplanationAndColoringTableCellRenderer() {
			panel.setLayout(new BorderLayout());
			laExp.setHorizontalAlignment(JLabel.RIGHT);
			panel.add(laExp, BorderLayout.EAST);
			dim = laExp.getPreferredSize();
		}
		
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (comp.getParent() != panel) {
				panel.add(comp, BorderLayout.CENTER);
			}

			if (value!=null && value instanceof Number) {
				float f = ((Number)value).floatValue();
				if (f == 0) {
					comp.setEnabled(false);
					comp.setBackground(Color.white);
				} else {
					comp.setEnabled(true);
					Color col = new Color((float)1-f/2, (float)1-f/3, (float)1-f/3);
					comp.setBackground(col);
				}
			}
			int realCol = tableModel.visualToRealColumnIndex(column);
			int realRow = tableModel.visualToRealRowIndex(row);
		
			panel.setBorder((cellSelectionManager.isSelected(realRow, realCol)? border: null));
			
			ExplanationManager expManager = ExplanationManager.getInstance();
			
			boolean expEnabled = expManager.isEnabled() && expManager.isEnabled_symbol_showCBasQuery();
			laExp.setVisible(expEnabled);

			if (expEnabled) {
				
				clsUsage = expManager.getClsStatistic(domainCls);
				
				double totalAmount = clsUsage * clsUsage;
				
				SlotStatistic stat = expManager.getSlotStatistic((ModelSlot) smf.getModelInstance());
				if (stat != null) {

					Object symbolQ = tableModel.getRowHeader().get(realRow);
					Object symbolC = tableModel.getColumnHeader().get(realCol);
					
					double amntQ = stat.getAttributeAmount(symbolQ);
					double amntC = stat.getAttributeAmount(symbolC);
					
					double rel = (amntQ * amntC) / totalAmount;
					double relcol = Math.min(1, rel * ((double)clsUsage)/((double)stat.getMaxAppearance()));
					
					Color col = new Color((float)(1-relcol/3), (float)(1-relcol/3), (float)(1-relcol/2));
					panel.setBackground(col);
	
					rel *= 100;
					laExp.setText(Integer.toString((int)rel) + "%"); //$NON-NLS-2$
					
					laExp.setPreferredSize(dim);
				}
			}
			return panel;
		}
		
	}

	public SMFPanel_Symbol_Table(SMF_Symbol_Table smf) {
		super(smf);
		this.smf = smf;
		domainCls = Helper.getDomainCls((ModelSlot) smf.getModelInstance());
		
		log.fine("initialize SMFPanel_Symbol_Standard."); //$NON-NLS-1$
		smf.addHasChangedListener(this, true);
		
		paTable = new Widget_SortableTable();
		innerTable = paTable.getTable();
		
		MouseControl mouseControl = new MouseControl();
		innerTable.addMouseListener(mouseControl);
		innerTable.addMouseMotionListener(mouseControl);
		
		addPopupMenu();
		
		paSymmetry = new Widget_Symmetry();
		paSymmetry.addSymmetryModeListener(smf);
		paSymmetry.addSymmetryModeListener(this);
		paSymmetry.setSymmetrySelection(smf.isSymmetricMode());
		
		refreshTable();

		// set special table cell renderer to table (color all zeros gray)
		innerTable.setDefaultRenderer(Object.class, new ExplanationAndColoringTableCellRenderer());
		
		setLayout(new BorderLayout());
		
		JPanel paContent = new JPanel();
		paContent.setLayout(new BorderLayout());
		
		JLabel laHeaderTop = new JLabel(Messages.getString("Case_base_values"), JLabel.CENTER); //$NON-NLS-1$
		JLabel laHeaderSide = new JLabel(Messages.getString("Query_values"), JLabel.CENTER); //$NON-NLS-1$
		laHeaderSide.setUI(new VerticalLabelUI(false));
		
		paContent.add(laHeaderTop, BorderLayout.NORTH);
		paContent.add(laHeaderSide, BorderLayout.WEST);
		
		paContent.add(paTable, BorderLayout.CENTER);
		
		add(paSymmetry, BorderLayout.NORTH);
		add(paContent, BorderLayout.CENTER);
	
		
		paTable.getTaHeaderRows().addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				JTable t = (JTable) e.getSource();
				int index = t.rowAtPoint(e.getPoint());
				t.setToolTipText((index >= 0 ? t.getValueAt(index, 0).toString() : null));
			}
		});
		
		
		innerTable.getTableHeader().addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				JTableHeader t = (JTableHeader) e.getSource();
				int index = t.columnAtPoint(e.getPoint());
				t.setToolTipText((index >= 0 ? t.getColumnModel().getColumn(index).getHeaderValue().toString() : null ));
			}
		});

		// configure cell editor and editing behavior
		cellEditorTxtField = new JTextField();
		DefaultCellEditor cellEditor = new DefaultCellEditor(cellEditorTxtField);
		innerTable.setDefaultEditor(Object.class, cellEditor);

		cellEditorTxtField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object value = ((JTextField) e.getSource()).getText();
				int[] rows = cellSelectionManager.getSelectedRows();
				int[] columns = cellSelectionManager.getSelectedColumns();
				for (int i = 0; i < rows.length; i++) {
					tableModel.setRealValueAt(value, rows[i], columns[i]);
				}
				innerTable.repaint();
			}
		});
		
	}
		
	private void addPopupMenu() {
		JPopupMenu popupmenuHeader = new JPopupMenu();

		miCBOccurences = new JCheckBoxMenuItem(Messages.getString("Case_base_occurences")); //$NON-NLS-1$
		miSimilarities = new JCheckBoxMenuItem(Messages.getString("Similarities")); //$NON-NLS-1$
		
		popupmenuHeader.add(miSimilarities);
		popupmenuHeader.add(miCBOccurences);
		
		popupmenuHeader.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				boolean enabled = ExplanationManager.getInstance().isEnabled();
				if (!enabled) {
					miCBOccurences.setSelected(false);
				}
				miCBOccurences.setEnabled(enabled);
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
			}

			public void popupMenuCanceled(PopupMenuEvent arg0) {
			}
		});
		
		
		JPopupMenu popupmenuCells = new JPopupMenu();
		JMenuItem miEdit = new JMenuItem(Messages.getString("Edit")); //$NON-NLS-1$
		popupmenuCells.add(miEdit);
		miEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// start editing lastly selected cell
				int[] rows 	= cellSelectionManager.getSelectedRows();
				int[] columns = cellSelectionManager.getSelectedColumns();
				if (rows.length == 0) return;
				if (innerTable.editCellAt(tableModel.realToVisualRowIndex(rows[rows.length-1]), tableModel.realToVisualColumnIndex(columns[rows.length-1]))) {
					cellEditorTxtField.grabFocus();
				}
			}
		});
		JMenuItem miExplain = new JMenuItem(Messages.getString("Explain_similarity")); //$NON-NLS-1$
		popupmenuCells.add(miExplain);
		miExplain.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// start editing lastly selected cell
				int[] rows 	= cellSelectionManager.getSelectedRows();
				int[] columns = cellSelectionManager.getSelectedColumns();
				if (rows.length == 0) return;
				
				Object symbolQ = tableModel.getRowHeader().get(rows[0]);
				Object symbolC = tableModel.getColumnHeader().get(columns[0]);
				SMExplanationContainer expContainer = (SMExplanationContainer) smf.getExplanationContainer();
				DefaultSMExplanation exp = expContainer.getExplanation(symbolQ, symbolC);
				String rationale = (exp == null ? "" : exp.getRationale()); //$NON-NLS-1$
				
				rationale = JOptionPane.showInputDialog(innerTable, Messages.getString("Explain_similarity_value"), rationale); //$NON-NLS-1$
				if (rationale == null) return;
				
				exp = new DefaultSMExplanation(CBRProject.getInstance().getProjectAuthorName(), rationale, new Date(System.currentTimeMillis()));
				
				for (int i = 0; i < columns.length; i++) {
					symbolQ = tableModel.getRowHeader().get(rows[i]);
					symbolC = tableModel.getColumnHeader().get(columns[i]);
					expContainer.setExplanation(symbolQ, symbolC, exp);
				}
				
			}
		});
		
		innerTable.getTableHeader().setComponentPopupMenu(popupmenuHeader);
		innerTable.setComponentPopupMenu(popupmenuCells);
		
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public void refreshTable() {
		cellSelectionManager.clear();
		
		Vector<Vector<Double>> tableData = smf.getData();
		tableModel = paTable.new DefaultSortableTableModel(tableData, smf.getHeader(), smf.getHeader()) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int arg0, int arg1) {
				return true;
			}
			
			public void setRealValueAt(Object aValue, int realRow, int realCol) {
				String val = aValue.toString();
				if (!ValueAcceptorImpl.getValueAcceptor_SimilarityValue().accept(val)) {
					return;
				}
				
				getSmf().setValueAt(val, realRow, realCol);
				super.setRealValueAt(getSmf().getValueAt(realRow, realCol), realRow, realCol);
			}
			
			protected int compareForColumnSort(int realCol1, int realCol2, Object concernedO1, Object concernedO2) {
				return -super.compareForColumnSort(realCol1, realCol2, concernedO1, concernedO2);
			}
			
			protected int compareForRowSort(int realRow1, int realRow2, Object concernedO1, Object concernedO2) {
				return -super.compareForRowSort(realRow1, realRow2, concernedO1, concernedO2);
			}
			
		};
		paTable.setModel(tableModel);
		setHeaderRenderers();

		innerTable.doLayout();
	}
	
	private void setHeaderRenderers() {
		DefaultHeaderTableCellRenderer headerRendererColumns = paTable.new DefaultHeaderTableCellRenderer() {
			int prefHeight = new JTable().getRowHeight();
			public int getPreferredCellHeight() {
				return prefHeight;
			}
		};
		paTable.setHeaderTableCellRendererRows(paTable.new DefaultHeaderTableCellRenderer() {
			JPanel panel = new JPanel();
			JLabel laExp = new JLabel(Messages.getString("100%")); //$NON-NLS-1$
			Dimension dim;
			{
				panel.setLayout(new BorderLayout());
				laExp.setHorizontalAlignment(JLabel.RIGHT);
				panel.add(laExp, BorderLayout.EAST);
				dim = laExp.getPreferredSize();
			}
			
			public Component getInternalTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int visualRow, int visualCol) {

				Component comp = super.getInternalTableCellRendererComponent(table, value, isSelected, hasFocus, visualRow, visualCol);
				if (comp.getParent() != panel) {
					panel.add(comp, BorderLayout.CENTER);
				}

				AbstractSMFunction smf = getSmf();
				int row = visualRow;
				ExplanationManager expManager = ExplanationManager.getInstance();
				
				if ( expManager!= null ) {
					boolean expEnabled = expManager.isEnabled() && expManager.isEnabled_symbol_showOccurrences();
					laExp.setVisible(expEnabled);
	
					if (expEnabled) {
						// get percentage of cases having this attribute value
						String symbol = (String) tableModel.getRowHeader().get(row);
	
						if ((expManager!=null)&&(expManager.getClsStatistic(domainCls)!=null)){
							clsUsage = expManager.getClsStatistic(domainCls);
							
							SlotStatistic stat = expManager.getSlotStatistic((ModelSlot) smf.getModelInstance());
							if (stat != null) {
								if (expManager.isEnabled_symbol_showOccurrences()) {
									// Show occurrences
									double rel = ((double) stat.getAttributeAmount(symbol))/((double)clsUsage);
									double relcol = Math.min(1, rel * ((double)clsUsage)/((double)stat.getMaxAppearance()));
									
									Color col = new Color((float)(1-relcol/3), (float)(1-relcol/3), (float)(1-relcol/2));
									panel.setBackground(col);
					
									rel *= 100;
									laExp.setText(Integer.toString((int)rel) + "%"); //$NON-NLS-2$
									
									laExp.setPreferredSize(dim);
								}
							}
						}
					}
				}
				return panel;
			}

		});
		paTable.setHeaderTableCellRendererColumns(headerRendererColumns);
	}

	public SMF_Symbol_Table getSmf() {
		return smf;
	}

	public void smfHasChanged(boolean hasChanged) {
		tableModel.fireTableChanged(new TableModelEvent(tableModel, TableModelEvent.HEADER_ROW));
	}

	public boolean setSymmetryMode(boolean symmetryMode) {
		// make sure: symmetry of cell selection
		int[] rows 		= cellSelectionManager.getSelectedRows();
		int[] columns	= cellSelectionManager.getSelectedColumns();
		for (int i=0; i<rows.length; i++) {
			cellSelectionManager.addSelectedCell(columns[i], rows[i]);
		}
		
		return true;
	}

}
