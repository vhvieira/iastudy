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
package de.dfki.mycbr.modelprovider.protege.similaritymodeling;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import de.dfki.mycbr.Helper;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;

/**
 * 
 * @author myCBR Team
 */
public class GeneratorDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(GeneratorDialog.class.getName());

	private JPanel jContentPane = null;
	private JPanel paContent = null;
	private JLabel jLabel = null;
	private JTextField jTextField = null;
	private JScrollPane jScrollPane = null;
	private JTable jTable = null;
	private JButton jButton = null;
	private JButton jButton1 = null;
	
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private ArrayList slotNames = null;
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private ArrayList slotValues = null;
	private Cls cls; 
	
	/**
	 * This is the default constructor
	 */
	public GeneratorDialog(Frame parent, Cls cls) {
		super(parent, true);
		initialize();
	
		customInit(cls);
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private void customInit(Cls cls) {
		this.cls = cls;
		Collection slots = cls.getTemplateSlots(); 
		slotNames = new ArrayList();
		slotValues = new ArrayList();
		for (Iterator i = slots.iterator(); i.hasNext(); ) {
			Slot slot = (Slot) i.next();
			ValueType valueType = slot.getValueType();
			if (valueType == ValueType.INTEGER || valueType == ValueType.FLOAT
					|| valueType == ValueType.BOOLEAN || valueType == ValueType.STRING
					|| valueType == ValueType.SYMBOL || valueType == ValueType.INSTANCE) {
				// it is an attribute we care about:
				slotNames.add(slot.getName());
				slotValues.add(new Integer(100)); // default is 100% probability
			} else {
				log.fine("ignored slot type " + valueType.toString()  //$NON-NLS-1$
						+ " " + slot.getName() ); //$NON-NLS-1$
			}
		}
		
		TableModel dataModel = new AbstractTableModel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			public String getColumnName(int col) {
				if (col == 0) {
					return Messages.getString("Slot"); //$NON-NLS-1$
				} else {
					return Messages.getString("Probability"); //$NON-NLS-1$
				}
			}
			public int getRowCount() {
				return slotNames.size(); 
			}
			
			public int getColumnCount() {
				return 2;
			}
			
			public Object getValueAt(int row, int col) {
				if (col == 0) {
					return slotNames.get(row);
				} else {
					return slotValues.get(row);
				}
			}
			
			public boolean isCellEditable(int row, int col) { 
				return col == 1;
			}
			
			public void setValueAt(Object value, int row, int col) {
				int i = Helper.parseInt((String) value);
				if (i >= 0 && i <= 100) {
					slotValues.set(row, new Integer(i));
				} else {
					JOptionPane.showMessageDialog(null, 
						Messages.getString("Give_an_integer_between_0_and_100"), Messages.getString("Error"),  //$NON-NLS-1$ //$NON-NLS-2$
						JOptionPane.ERROR_MESSAGE);
				}
				//fireTableCellUpdated(row, col);
			}
		};
	    jTable.setModel(dataModel);
	    //JScrollPane scrollpane = new JScrollPane(table);
	}


	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(300, 200);
		this.setTitle(Messages.getString("Generate_instances")); //$NON-NLS-1$
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getPaContent(), java.awt.BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes paContent	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPaContent() {
		if (paContent == null) {
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 2;
			gridBagConstraints4.insets = new java.awt.Insets(5,5,5,5);
			gridBagConstraints4.gridy = 2;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.weightx = 1.0D;
			gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints3.insets = new java.awt.Insets(5,5,5,5);
			gridBagConstraints3.gridwidth = 2;
			gridBagConstraints3.gridy = 2;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.weighty = 1.0;
			gridBagConstraints2.gridwidth = 3;
			gridBagConstraints2.insets = new java.awt.Insets(5,5,5,5);
			gridBagConstraints2.gridx = 0;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints1.gridy = 1;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.gridwidth = 2;
			gridBagConstraints1.insets = new java.awt.Insets(5,5,5,5);
			gridBagConstraints1.gridx = 1;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.weightx = 0.0D;
			gridBagConstraints.insets = new java.awt.Insets(5,5,5,5);
			gridBagConstraints.gridy = 1;
			jLabel = new JLabel();
			jLabel.setText(Messages.getString("Number_of_instances_to_generate")); //$NON-NLS-1$
			paContent = new JPanel();
			paContent.setLayout(new GridBagLayout());
			paContent.add(jLabel, gridBagConstraints);
			paContent.add(getJTextField(), gridBagConstraints1);
			paContent.add(getJScrollPane(), gridBagConstraints2);
			paContent.add(getJButton(), gridBagConstraints3);
			paContent.add(getJButton1(), gridBagConstraints4);
		}
		return paContent;
	}

	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
			jTextField.setText("1"); //$NON-NLS-1$
			jTextField.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyTyped(java.awt.event.KeyEvent e) {
					if (!(e.getKeyChar() >= '0' && e.getKeyChar() <= '9')) {
						e.consume();
					}
				}
			});
		}
		return jTextField;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTable());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getJTable() {
		if (jTable == null) {
			jTable = new JTable();
		}
		return jTable;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText(Messages.getString("Generate")); //$NON-NLS-1$
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (jTable.getCellEditor() != null) {
						jTable.getCellEditor().stopCellEditing();
					}
					InstanceGenerator.generate(Helper.parseInt(jTextField.getText()), 
						cls, slotValues);
					dispose();
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setText(Messages.getString("Cancel")); //$NON-NLS-1$
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dispose();
				}
			});
		}
		return jButton1;
	}

}
