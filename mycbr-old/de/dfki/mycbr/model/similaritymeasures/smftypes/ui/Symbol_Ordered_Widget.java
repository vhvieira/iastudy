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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Symbol_Ordered;

/**
 * 
 * @author Daniel Bahls
 */
public class Symbol_Ordered_Widget extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger ( Symbol_Ordered_Widget.class.getName ( ) );

	private JLabel jLabel = null;
	private JScrollPane scrollpane = null;
	private JTable table = null;
	private SMF_Symbol_Ordered smf;

	/** flag for DnD. True indicates that the user aborted dragging operation. */
	private boolean draggingAborted = false;
	private int draggedRow = -1;

	private JPanel paCycle = null;

	private JCheckBox cbCycle = null;

	private JLabel laDistanceLastFirst = null;

	private JTextField txtDistLastFirst = null;
	
	
	/**
	 * This is the default constructor.
	 * @param smf 
	 */
	public Symbol_Ordered_Widget(SMF_Symbol_Ordered smf) {
		super();
		this.smf = smf;
		initialize();
		
		customInit();
	}

	private void customInit() {
		initTable();
		
		//
		// add mouse behavior
		//
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				// double click on first column -> start editing second column
				if (e.getClickCount() >= 2) {
					if (table.getSelectedColumn() == 0) {
						int row = table.getSelectedRow();
						log.fine("start editing row [" + row + "] column 1."); //$NON-NLS-1$ //$NON-NLS-2$
						table.editCellAt(row, 1);
					}
				}
			}
			
			public void mouseReleased(MouseEvent e) {
				// stop dragging
				if (draggedRow >= 0) {
					draggedRow = -1;
					table.setRowSelectionAllowed(true);
					draggingAborted = false;
					table.setCursor(Cursor.getDefaultCursor());
				}
			}
		});
		
		
		//
		// drag & drop
		//
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE && draggedRow>=0) {
					// cancel drag&drop
					draggingAborted = true;
					draggedRow = -1;
					table.setCursor(Cursor.getDefaultCursor());
				}
			}
		});
		table.addMouseMotionListener(new MouseMotionAdapter() {
			@SuppressWarnings("unchecked") //$NON-NLS-1$
			public void mouseDragged(MouseEvent e) {
				// if dragging has been canceled (e.g. by 'ESCAPE'), ignore
				// dragging...
				if (draggingAborted) return;
				
				// update view
				Rectangle rect = new Rectangle(e.getX(), e.getY(), 20, 20);
				table.scrollRectToVisible(rect);
				
				// show position in tree while dragging ... more beautiful
				if (draggedRow >= 0) {
					int currentRow = table.getSelectedRow();
					
					if (currentRow == draggedRow) return;
					
					// swap
					DefaultTableModel dtm = (DefaultTableModel)table.getModel();

//					TODO 
//					if Thomas & Armin accept this kind of DnD -> improve it
//					if not --> delete it.
					
					if (currentRow > draggedRow) {
						// read concerned items
						Vector concernedItems = new Vector(currentRow-draggedRow);
						for (int i=draggedRow; i<=currentRow; i++) {
							concernedItems.add(dtm.getValueAt(i, 0));
						}
						Object tmp = concernedItems.get(0);
						concernedItems.remove(0);
						concernedItems.add(tmp);
						
						// now change symbols in table
						boolean decendingly = draggedRow!=0;
						swapRowsDnD(concernedItems, decendingly, draggedRow);

						// commented out before 20.10.2008
//						for (int i=concernedItems.size()-1; i>=0; i--)
//						{
//							table.setValueAt(concernedItems.get(i), draggedRow+i, 0);
//						}
						draggedRow = currentRow;
						
					}

					if (currentRow < draggedRow) {
						// read concerned items
						Vector concernedItems = new Vector(draggedRow-currentRow);
						for (int i=currentRow; i<=draggedRow; i++) {
							concernedItems.add(dtm.getValueAt(i, 0));
						}
						int lastIndex=draggedRow-currentRow;
						Object tmp = concernedItems.get(lastIndex);
						concernedItems.remove(lastIndex);
						concernedItems.insertElementAt(tmp,0);
						
						// now change symbols in table
						boolean decendingly = draggedRow==table.getRowCount()-1;
						swapRowsDnD(concernedItems, decendingly, currentRow);
						
						// commented out before 20.10.2008
//						for (int i=0; i<concernedItems.size(); i++)
//						{
//							table.setValueAt(concernedItems.get(i), currentRow+i, 0);
//						}
						draggedRow = currentRow;
						
					}
					
					table.revalidate();
					table.repaint();
					return;
				}
				log.fine("init currentlyDraggedNodes"); //$NON-NLS-1$

				table.setRowSelectionAllowed(false);
//				selectLeftTableSide();
				draggedRow = table.getSelectedRow();
				if (draggedRow < 0) return;
				
				table.setCursor(new Cursor(Cursor.HAND_CURSOR));
				
			}

			@SuppressWarnings("unchecked") //$NON-NLS-1$
			private void swapRowsDnD(Vector concernedItems, boolean decendingly, int lowIndex) {
				if (decendingly) {
					for (int i=concernedItems.size()-1; i>=0; i--) {
						table.setValueAt(concernedItems.get(i), lowIndex+i, 0);
					}
				} else {
					for (int i=0; i<concernedItems.size(); i++) {
						table.setValueAt(concernedItems.get(i), lowIndex+i, 0);
					}
				}
				
			}
		});
//		table.setDefaultEditor(Object.class, new DefaultCellEditor(new MyJTextField()));
		Helper.addFocusListener_ConfirmChanges(table);
		
		//
		// cycle panel
		//
		cbCycle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				smf.setCyclic(cbCycle.isSelected());
				checkCycleButton();
			}
		});
		
		// commented out before 20.10.2008
//		txtDistLastFirst.getDocument().addDocumentListener(new DocumentListener()
//		{
//
//			public void insertUpdate(DocumentEvent arg0)
//			{
//				confirmDistLastFirst();
//			}
//
//			public void removeUpdate(DocumentEvent arg0)
//			{
//				confirmDistLastFirst();
//			}
//
//			public void changedUpdate(DocumentEvent e)
//			{
//				confirmDistLastFirst();
//			}
//		});

		txtDistLastFirst.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
//				confirmDistLastFirst();
				int i = Helper.parseInt(txtDistLastFirst.getText());
				if (i < 0) return;
				smf.setDistLastFirst(i);
				checkCycleButton();
			}
		});
		
		cbCycle.setSelected(smf.isCyclic());
		checkCycleButton();
	}

	// commented out before 20.10.2008
//	private void selectLeftTableSide()
//	{
//		log.fine("select left table side.");
////		table.getSelectedColumn();
//		int row = table.getSelectedRow();
//		table.changeSelection(row, 0, false, false);
////		table.getCellRenderer(row, 0).getTableCellRendererComponent(table, )
//		
//	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private void initTable() {
		//
		// init table model
		//
		Vector header = new Vector();
		header.add("symbol"); //$NON-NLS-1$
		header.add("int"); //$NON-NLS-1$
		Vector data = new Vector();
		
		//
		// first sort symbol map by values
		//
		HashMap symbolMapping = smf.getSymbolMapping();
		Vector entries = new Vector(symbolMapping.size());
		for (Iterator it=symbolMapping.entrySet().iterator(); it.hasNext();) {
			Entry e = (Entry) it.next();
			entries.add(e);
		}
		Collections.sort(entries, new Comparator() {
			public int compare(Object o1, Object o2) {
				Entry e1 = (Entry) o1;
				Entry e2 = (Entry) o2;
				return ((Comparable)e1.getValue()).compareTo((Comparable)e2.getValue());
			}
		});
		for (Iterator it=entries.iterator(); it.hasNext();) {
			Entry e = (Entry) it.next();
			Vector row = new Vector(2);
			row.add(e.getKey());
			row.add(e.getValue());
			data.add(row);
		}
		
		//
		// define table model
		//
		DefaultTableModel dtm = new DefaultTableModel(data, header) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			//			boolean isMovingRows = false;
			public void setValueAt(Object val, int row, int col) {
				// for DnD. set symbols
				if (col == 0) {
					super.setValueAt(val, row, col);
					smf.setSymbolMapping((String)val, (Integer) getValueAt(row, 1));
					return;
				}
				
				// int value for symbol changed.
				if (Helper.parseInt(val.toString()) == Integer.MIN_VALUE) {
					return;
				}
				val = new Integer((String)val);
				super.setValueAt(val, row, col);
				
				// commit changes to smf
				smf.setSymbolMapping((String)getValueAt(row, 0), (Integer) val);

				// commented out before 20.10.2008
//				if (isMovingRows) return;
//				log.info("do special stuff at setValueAt...");
//				log.info("class val : "+val.getClass().getName()+",  class xy: "+getValueAt(row+1,col).getClass().getName());
				
				// now sort table
				Comparable valComp = (Comparable) val;
				int rowCnt = getRowCount();
				
				// return if order is correct
				if ((row==0 || ((Comparable)getValueAt(row-1, col)).compareTo(valComp)<=0)
						&& (row==rowCnt-1 || ((Comparable)getValueAt(row+1, col)).compareTo(valComp)>0)) {
					return;
				}
				
				// now look for its correct row index
				int i=0; 
				while ((i==row) || (i<rowCnt && ((Comparable)getValueAt(i, col)).compareTo(valComp)<0)) {
					i++;
				}
				if (i > row) {
					i--;
				}
				
				log.fine("move [" + valComp + "] row [" + row + "] to [" + i + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				
//				isMovingRows = true;
				moveRow(row, row, i);
//				isMovingRows = false;
				
				table.changeSelection(i, 0, false, false);
			}
			
			public boolean isCellEditable(int row, int col) {
				return col>0;
			}
		};
		table.setModel(dtm);
		
	}
	
	/**
	 * Reloads data from smf.
	 */
	public void refresh() {
		initTable();
		checkCycleButton();
	}
	
	// commented out before 20.10.2008
//	private void confirmDistLastFirst()
//	{
//		int i = Helper.parseInt(txtDistLastFirst.getText());
//		if (i<0) return;
//		smf.setDistLastFirst(i);
//	}

	private void checkCycleButton() {
		boolean isCyclic = smf.isCyclic();
		
		laDistanceLastFirst.setEnabled(isCyclic);
		txtDistLastFirst.setEnabled(isCyclic);
		txtDistLastFirst.setText(Integer.toString(smf.getDistLastFirst()));
	}
	
	
	//
	// GUI
	//

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
		gridBagConstraints11.gridx = 0;
		gridBagConstraints11.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints11.gridy = 2;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints1.gridy = 1;
		gridBagConstraints1.weightx = 1.0;
		gridBagConstraints1.weighty = 1.0;
		gridBagConstraints1.insets = new java.awt.Insets(5,5,5,5);
		gridBagConstraints1.gridx = 0;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new java.awt.Insets(5,5,5,5);
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.gridx = 0;
		jLabel = new JLabel();
		jLabel.setText(Messages.getString("Symbol_needs_integer_representation")); //$NON-NLS-1$
		this.setLayout(new GridBagLayout());
		this.setSize(285, 313);
		this.add(jLabel, gridBagConstraints);
		this.add(getScrollpane(), gridBagConstraints1);
		this.add(getPaCycle(), gridBagConstraints11);
	}

	/**
	 * This method initializes scrollpane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getScrollpane() {
		if (scrollpane == null) {
			scrollpane = new JScrollPane();
			scrollpane.setViewportView(getTable());
		}
		return scrollpane;
	}

	/**
	 * This method initializes table	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getTable() {
		if (table == null) {
//			table = new JTable();
			table = new JTable() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;
				//
				// special changes for DnD feature:
				// mark only the left side during drag. even though right side might be selected.
				//
				private Color defaultColor = null;
				public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
					Component c = super.prepareRenderer(renderer, row, col);
					if (defaultColor == null) {
						defaultColor = c.getBackground();
					}
					if (col==0 && row==draggedRow) {
						c.setBackground(table.getSelectionBackground());
					} else {
						c.setBackground(defaultColor);
					}
					
					return c;
				}
			};
		}
		return table;
	}

	/**
	 * This method initializes paCycle	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPaCycle() {
		if (paCycle == null) {
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.insets = new java.awt.Insets(5,5,5,5);
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints3.gridy = 0;
			gridBagConstraints3.weightx = 1.0D;
			gridBagConstraints3.insets = new java.awt.Insets(5,5,5,5);
			gridBagConstraints3.gridx = 2;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.gridy = 0;
			laDistanceLastFirst = new JLabel();
			laDistanceLastFirst.setText(Messages.getString("Distance_last_to_first")); //$NON-NLS-1$
			paCycle = new JPanel();
			paCycle.setLayout(new GridBagLayout());
			paCycle.add(getCbCycle(), gridBagConstraints4);
			paCycle.add(laDistanceLastFirst, gridBagConstraints2);
			paCycle.add(getTxtDistLastFirst(), gridBagConstraints3);
		}
		return paCycle;
	}

	/**
	 * This method initializes cbCycle	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getCbCycle() {
		if (cbCycle == null) {
			cbCycle = new JCheckBox();
			cbCycle.setText(Messages.getString("Cyclic")); //$NON-NLS-1$
		}
		return cbCycle;
	}

	/**
	 * This method initializes txtDistLastFirst	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTxtDistLastFirst() {
		if (txtDistLastFirst == null) {
			txtDistLastFirst = new JTextField();
			txtDistLastFirst.addFocusListener(Helper.focusListener);
		}
		return txtDistLastFirst;
	}

}  //  @jve:decl-index=0:visual-constraint="9,7"
