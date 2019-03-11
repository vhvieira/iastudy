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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableModel;

/**
 * 
 * @author myCBR Team
 */
public class ImportInstancesNameChooser extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(ImportInstancesNameChooser.class.getName());

	private JPanel jContentPane = null;
	private JScrollPane scrollpane = null;
	private JTable taContent = null;
	
	private Vector<Integer> nameIndices = new Vector<Integer>();
	private Vector<String> header;
	private JButton buOk = null;
	private JButton buDontCare = null;

	private JLabel laHintOrder = null;

	private Vector<Vector<String>> data;

	private Vector<Vector<String>> examples;

	private JList liExamples;

	private Random random = new Random();

	private int randomIndex;

	/**
	 * This is the default constructor
	 * @param data 
	 */
	public ImportInstancesNameChooser(JDialog parent, boolean modal, Vector<String> header, Vector<Vector<String>> data) {
		super(parent, modal);
		this.header = header;
		this.data = data;
		initialize();
		customInit();
	}

	private void customInit() {
		initExamples();
		
		Vector<Vector<Boolean>> tableData = new Vector<Vector<Boolean>>();
		Vector<Boolean> cb = new Vector<Boolean>();
		for (int i=0; i<header.size(); i++) {
			cb.add(false);
		}
		tableData.add(cb);
		taContent.setModel(new DefaultTableModel(tableData, header) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked") //$NON-NLS-1$
			public Class getColumnClass(int arg0) {
				return Boolean.class;
			}
			
			public void setValueAt(Object arg0, int arg1, int arg2) {
				super.setValueAt(arg0, arg1, arg2);
				refreshExamples();
			}
		});
		taContent.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		taContent.setRowSelectionAllowed(false);
		taContent.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
			public void columnMoved(TableColumnModelEvent arg0) {
				// you can move the headers of the table.
				// if you do so the name pattern changes, as do the examples
				refreshExamples();
			}

			public void columnAdded(TableColumnModelEvent arg0) {
			}

			public void columnRemoved(TableColumnModelEvent arg0) {
			}
			
			public void columnMarginChanged(ChangeEvent arg0) {
			}

			public void columnSelectionChanged(ListSelectionEvent arg0) {
			}
		});
		taContent.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		taContent.doLayout();
		
		
		// buttons
		buDontCare.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				nameIndices.clear();
				dispose();
			}
		});
		
		buOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		
		refreshExamples();
	}

	public void refreshExamples() {
		// refresh nameIndices
		nameIndices.clear();
		for (int i=0; i<header.size(); i++) {
			log.fine(header.get(i) + ": " + taContent.getValueAt(0, i));//$NON-NLS-1$
			
			if (((Boolean) taContent.getValueAt(0, i)).booleanValue()) {
				String hName= (String)taContent.getColumnModel().getColumn(i).getHeaderValue();
				
				nameIndices.add(header.indexOf(hName));
			}
		}

		
		// refresh examples
		Vector<String> exDisplay = new Vector<String>(examples.size());
		for (int i=0; i<examples.size(); i++) {
			Vector<String> row = (Vector<String>) examples.get(i);
			String generatedName = ImportInstancesDialog.generateName(getNameIndices(), row);
			if (generatedName == null) {
				generatedName = Messages.getString("Generated_by_Protege") + (randomIndex+i); //$NON-NLS-1$
			}
			exDisplay.add(generatedName);
		}
		liExamples.setListData(exDisplay);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(397, 215);
		this.setTitle(Messages.getString("How_to_generate_its_names")); //$NON-NLS-1$
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.weightx = 1.0D;
			gridBagConstraints11.gridy = 2;
			laHintOrder = new JLabel();
			laHintOrder.setText(Messages.getString("Move_columns_to_define_the_order")); //$NON-NLS-1$
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridy = 2;
			gridBagConstraints2.insets = new java.awt.Insets(5,5,5,5);
			gridBagConstraints2.gridx = 2;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridy = 2;
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.EAST;
			gridBagConstraints1.insets = new java.awt.Insets(5,5,5,5);
			gridBagConstraints1.weightx = 1.0D;
			gridBagConstraints1.gridx = 1;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.gridwidth = 3;
			gridBagConstraints.gridx = 0;
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(getScrollpane(), gridBagConstraints);
			jContentPane.add(getExamplePanel(), new GridBagConstraints(0,1, 3,1, 1.0,1.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
			jContentPane.add(getBuOk(), gridBagConstraints1);
			jContentPane.add(getBuDontCare(), gridBagConstraints2);
			jContentPane.add(laHintOrder, gridBagConstraints11);
		}
		return jContentPane;
	}

	private JPanel getExamplePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel(Messages.getString("Example_names")), BorderLayout.NORTH); //$NON-NLS-1$
		liExamples = new JList();
		panel.add(liExamples, BorderLayout.CENTER);
		
		return panel;
	}
	
	private void initExamples() {
		// find some examples
		randomIndex = Math.abs(random .nextInt())%data.size();
		int amount = Math.min(3, data.size());
		examples = new Vector<Vector<String>>(amount);
		for (int i=0; i<amount; i++) {
			examples.add(data.get((randomIndex+i)%data.size()));
		}
	}

	/**
	 * This method initializes scrollpane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getScrollpane() {
		if (scrollpane == null) {
			scrollpane = new JScrollPane();
			scrollpane.setViewportView(getTaContent());
		}
		return scrollpane;
	}

	/**
	 * This method initializes taContent	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getTaContent() {
		if (taContent == null) {
			taContent = new JTable();
		}
		return taContent;
	}

	public Vector<Integer> getNameIndices() {
		return nameIndices;
	}

	/**
	 * This method initializes buOk	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBuOk() {
		if (buOk == null) {
			buOk = new JButton();
			buOk.setText(Messages.getString("Ok")); //$NON-NLS-1$
		}
		return buOk;
	}

	/**
	 * This method initializes buDontCare	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBuDontCare() {
		if (buDontCare == null) {
			buDontCare = new JButton();
			buDontCare.setText(Messages.getString("Dont_care")); //$NON-NLS-1$
		}
		return buDontCare;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
