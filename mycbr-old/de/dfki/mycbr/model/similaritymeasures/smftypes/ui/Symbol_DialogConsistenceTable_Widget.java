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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import de.dfki.mycbr.Helper;

/**
 * @author myCBR Team
 *
 */
public class Symbol_DialogConsistenceTable_Widget extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(Symbol_DialogConsistenceTable_Widget.class.getName());

	public static final String ABORT_OPTION = "abort"; //$NON-NLS-1$

	public static final String OK_OPTION = "ok"; //$NON-NLS-1$

	JPanel panel1 = new JPanel();

	JScrollPane scrollpane = new JScrollPane();

	JTable table = new JTable();

	boolean isDragging = false;

	int selectedRow = -1;

	int selectedCol = -1;

	private String result = null;

	private class ComplexTableModel extends DefaultTableModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private static final String LABEL_CREATE = "<------   CREATE NEW"; //$NON-NLS-1$

		private static final String LABEL_DELETE = "DELETE THIS   ------>"; //$NON-NLS-1$

		@SuppressWarnings("unchecked") //$NON-NLS-1$
		private Vector data = new Vector(2);

		@SuppressWarnings("unchecked") //$NON-NLS-1$
		private Vector protElements;

		@SuppressWarnings("unchecked") //$NON-NLS-1$
		private Vector smfElements;

		@SuppressWarnings("unchecked") //$NON-NLS-1$
		public ComplexTableModel(Vector protElements, Vector smfElements) {
			super();
			this.protElements = protElements;
			this.smfElements = smfElements;

			int cnt1 = protElements.size();
			int cnt2 = smfElements.size();
			for (int i = 0; i < cnt2; i++) {
				protElements.add("");//LABEL_DELETE); //$NON-NLS-1$
			}
			for (int i = 0; i < cnt1; i++) {
				smfElements.add("");//LABEL_CREATE); //$NON-NLS-1$
			}

			data.add(protElements);
			data.add(smfElements);

			// make proposition
			tryToMatch();//protElements, smfElements);

			refresh();
		}

		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			if (protElements == null || smfElements == null) {
				return 0;
			}
			return protElements.size();
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@SuppressWarnings("unchecked") //$NON-NLS-1$
		public Class getColumnClass(int columnIndex) {
			return String.class;
		}

		@SuppressWarnings("unchecked") //$NON-NLS-1$
		public Object getValueAt(int y, int x) {
			Object val = ""; //$NON-NLS-1$

			try {
				val = ((Vector) data.get(x)).get(y);
			} catch (Throwable e) {
				val = "!error!"; //$NON-NLS-1$
			}

			return val;
		}

		@SuppressWarnings("unchecked") //$NON-NLS-1$
		public void setValueAt(Object aValue, int y, int x) {
			try {
				((Vector) data.get(x)).set(y, aValue);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) {
				return "Protege Values"; //$NON-NLS-1$
			}
			return "SMFunction Values"; //$NON-NLS-1$
		}

		public void addTableModelListener(TableModelListener l) {
			super.addTableModelListener(l);
		}

		public void removeTableModelListener(TableModelListener l) {
			super.removeTableModelListener(l);
		}

		@SuppressWarnings("unchecked") //$NON-NLS-1$
		public void refresh() {
			for (int i = 0; i < protElements.size(); i++) {
				Object protEl = protElements.get(i);
				Object smfEl = smfElements.get(i);
				if ((LABEL_DELETE.equals(protEl) || "".equals(protEl)) && (LABEL_CREATE.equals(smfEl) || "".equals(smfEl))) { //$NON-NLS-1$ //$NON-NLS-2$
					smfElements.set(i, ""); //$NON-NLS-1$
					protElements.set(i, ""); //$NON-NLS-1$
				} else {
					if ("".equals(smfEl) && !LABEL_DELETE.equals(protEl) && !"".equals(protEl)) { //$NON-NLS-1$ //$NON-NLS-2$
						smfElements.set(i, LABEL_CREATE);
					} else if ("".equals(protEl) && !LABEL_CREATE.equals(smfEl) && !"".equals(smfEl)) { //$NON-NLS-1$ //$NON-NLS-2$
						protElements.set(i, LABEL_DELETE);
					}
				}
			}
		}

		public boolean isSlotName(int y, int x) {
			Object val = getValueAt(y, x);
			if ("".equals(val) || LABEL_DELETE.equals(val) || LABEL_CREATE.equals(val)) { //$NON-NLS-1$
				return false;
			}
			return true;
		}

		@SuppressWarnings("unchecked") //$NON-NLS-1$
		public void finish() {
			// prepare the vectors. We want only the matching values in the lists.
			// Kick all other rows
			for (int i = 0; i < protElements.size(); i++) {
				Object smfVal = smfElements.get(i);
				Object protVal = protElements.get(i);

				if (LABEL_DELETE.equals(smfVal) || LABEL_CREATE.equals(smfVal)) {
					smfElements.set(i, ""); //$NON-NLS-1$
				}
				if (LABEL_DELETE.equals(protVal) || LABEL_CREATE.equals(protVal)) {
					protElements.set(i, ""); //$NON-NLS-1$
				}
			}
		}

		@SuppressWarnings("unchecked") //$NON-NLS-1$
		public void tryToMatch() {
			Vector newElements = protElements;
			Vector idleElements = smfElements;
			for (int i = 0; i < newElements.size(); i++) {
				String newEl = (String) newElements.get(i);
				double maxSim = -1;
				int index = -1;
				for (int j = 0; j < idleElements.size(); j++) {
					String idleEl = (String) idleElements.get(j);
					double sim = Helper.ngram(newEl, idleEl, 3, true);
					if (sim > 0 && sim > maxSim) {
						maxSim = sim;
						index = j;
					}
				}

				if (index >= 0) {
					// swap elements in idleElements
					log.fine("__ swapping : sim = " + maxSim + ", newEl = " + newEl + ", idleEl = " + idleElements.get(index)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

					Object tmp = idleElements.get(i);
					idleElements.set(i, idleElements.get(index));//.toString()+"_["+newEl+":"+maxSim+"]");
					idleElements.set(index, tmp);
				} else if (!"".equals(newEl) && !"".equals(idleElements.get(i))) { //$NON-NLS-1$ //$NON-NLS-2$
					log.fine("__ no similar value found."); //$NON-NLS-1$
					// propose CREATE NEW
					for (int k = 0; k < smfElements.size(); k++) {
						if (!isSlotName(k, 1)) {
							// swap
							Object tmp = idleElements.get(i);

							log.fine(" propose CREATE_NEW for [" + newEl + "], put " + tmp + " in row " + k); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

							idleElements.set(i, idleElements.get(k));//.toString()+"_["+newEl+":"+maxSim+"]");
							idleElements.set(k, tmp);
							break;
						}
					}
				}
			}

		}
	}

	private ComplexTableModel tableModel;

	GridBagLayout gridBagLayout1 = new GridBagLayout();

	JButton buOk = new JButton();

	JButton buAbort = new JButton();

	JPanel paInfo = new JPanel();

	BorderLayout borderLayout1 = new BorderLayout();

//	JTextArea jTextArea1 = new JTextArea();
	JTextPane jTextPane = new JTextPane();

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public Symbol_DialogConsistenceTable_Widget(Frame frame, String title, boolean modal, Vector newElements, Vector idleElements) {
		super(frame, title, modal);
		try {
			jbInit();
			customInit(newElements, idleElements);
			setSize(400, 400);
//			pack();
			Helper.centerWindow(this);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private void customInit(Vector newElements, Vector idleElements) {

//		jTextPane.setWrapStyleWord(true);

		tableModel = new ComplexTableModel(newElements, idleElements);

		table.setModel(tableModel);
		table.setCellSelectionEnabled(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (!tableModel.isSlotName(table.getSelectedRow(), table.getSelectedColumn())) {
					return;
				}
				isDragging = true;
				selectedCol = table.getSelectedColumn();
				selectedRow = table.getSelectedRow();

				table.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

			public void mouseReleased(MouseEvent e) {
				if (isDragging && table.getSelectedColumn() == selectedCol) {
					// swap values
					Object o1 = table.getValueAt(selectedRow, selectedCol);
					int row = table.getSelectedRow();
					int col = table.getSelectedColumn();
					table.setValueAt(table.getValueAt(row, col), selectedRow, selectedCol);
					table.setValueAt(o1, row, col);

					tableModel.fireTableChanged(new TableModelEvent(tableModel, TableModelEvent.HEADER_ROW));
					table.setColumnSelectionInterval(col, col);
					table.setRowSelectionInterval(row, row);

					selectedCol = -1;
					selectedRow = -1;
					tableModel.refresh();
				}
				isDragging = false;
				table.setCursor(Cursor.getDefaultCursor());
			}
		});

		// set button actions
		buOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				result = OK_OPTION;
				tableModel.finish();
				dispose();
			}
		});

		buAbort.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				result = ABORT_OPTION;
				dispose();
			}
		});

		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

	}

	private void jbInit() throws Exception {
		panel1.setLayout(gridBagLayout1);
		this.setTitle(Messages.getString("Make_consistent")); //$NON-NLS-1$
		buOk.setText(Messages.getString("OK")); //$NON-NLS-1$
		buAbort.setText(Messages.getString("Abort")); //$NON-NLS-1$
		paInfo.setMinimumSize(new Dimension(100, 150));
		paInfo.setPreferredSize(new Dimension(100, 150));
		paInfo.setLayout(borderLayout1);
		//    jLabel1.setText("jLabel1");
		jTextPane.setEditable(false);
		jTextPane.setText(Messages.getString("Set_of_allowed_symbols_changed")); //$NON-NLS-1$
		getContentPane().add(panel1);
		panel1.add(scrollpane, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		panel1.add(buOk, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		scrollpane.getViewport().add(table, null);
		panel1.add(buAbort, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		panel1.add(paInfo, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		paInfo.add(jTextPane, BorderLayout.CENTER);
	}

	public String getResult() {
		return result;
	}
	
	public void finish() {
		tableModel.finish();
	}
}
