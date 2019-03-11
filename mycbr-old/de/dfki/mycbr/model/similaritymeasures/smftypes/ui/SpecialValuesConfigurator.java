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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.dfki.mycbr.model.vocabulary.SpecialValue;
import de.dfki.mycbr.model.vocabulary.SpecialValueHandler;

public class SpecialValuesConfigurator extends JPanel implements ListSelectionListener {
	
	private static final long serialVersionUID = 1L;
	private JPanel paValues = null;
	private JPanel paSMF = null;
	private JPanel paControl = null;
	private JScrollPane scrollpaneValues = null;
	private JList liValues = null;
	private JButton buAdd = null;
	private JButton buRemove = null;
	private JButton buClose = null;
	
	private SpecialValueHandler specialValueHandler;

	/**
	 * This is the default constructor
	 */
	public SpecialValuesConfigurator() {
		super();
		this.specialValueHandler = SpecialValueHandler.getInstance();
		
		initialize();
		customInit();
	}
	
	
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private void customInit() {
		GridBagConstraints gbc = new GridBagConstraints(0,0, 1,1, 1d,1d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0);
		paSMF.add(specialValueHandler.getInternalSMF().getEditorPanel(), gbc);
		
		liValues.setListData(new Vector(specialValueHandler.getAllSpecialValues()));
		liValues.addListSelectionListener(this);
		liValues.setCellRenderer(new DefaultListCellRenderer() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				label.setEnabled(!isNonRemovableSpecialValue(value));
				return label;
			}
		});
		
		buAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Object o = JOptionPane.showInputDialog(getTopLevelAncestor(), Messages.getString("SpecialValuesConfigurator.Add_new_spacial_value")); //$NON-NLS-1$
				if (o == null) return;
				
				if (specialValueHandler.createSpecialValue(o.toString())) {
					updateList();
					specialValueHandler.updateFakeSlot();
//					specialValueHandler.getInternalSMF().insertNewSymbol(o.toString());
					specialValueHandler.getInternalSMF().checkConsistency(null, true);
				}
				
			}
		});
		
		buRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SpecialValue sv = (SpecialValue) liValues.getSelectedValue();
				if (sv==null || isNonRemovableSpecialValue(sv)) {
					return;
				}
				specialValueHandler.removeSpecialValue(sv);

				updateList();
				specialValueHandler.updateFakeSlot();
//				specialValueHandler.getInternalSMF().removeOldSymbol(sv.getName());
				specialValueHandler.getInternalSMF().checkConsistency(null, true);
			}
		});
		
		buClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				((JDialog) getTopLevelAncestor()).dispose();
			}
		});
		
		updateButtons();
	}

	public void valueChanged(ListSelectionEvent arg0) {
		updateButtons();
	}
	
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	protected void updateList() {
		Vector vector = new Vector(specialValueHandler.getAllSpecialValues());
		Collections.sort(vector, new Comparator() {
			public int compare(Object o1, Object o2) {
				SpecialValue sv1 = (SpecialValue) o1;
				SpecialValue sv2 = (SpecialValue) o2;
				return sv1.toString().compareTo(sv2.toString());
			}
		});
		liValues.setListData(vector);
	}

	private void updateButtons() {
//		int i = liValues.getSelectedIndex();
		SpecialValue sv = (SpecialValue) liValues.getSelectedValue();
		buRemove.setEnabled(sv!=null && !isNonRemovableSpecialValue(sv));
	}

	private boolean isNonRemovableSpecialValue(Object sv) {
//		return sv==SpecialValueHandler.SPECIAL_VALUE_UNDEFINED || sv==SpecialValueHandler.SPECIAL_VALUE_UNKNOWN;
		return sv==SpecialValueHandler.SPECIAL_VALUE_UNDEFINED;
	}

	//
	// 
	// GUI setup
	//
	//
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 1;
		gridBagConstraints1.fill = GridBagConstraints.BOTH;
		gridBagConstraints1.weightx = 1.0D;
		gridBagConstraints1.weighty = 1.0D;
		gridBagConstraints1.gridy = 0;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.0D;
		gridBagConstraints.weighty = 1.0D;
		gridBagConstraints.insets = new Insets(5,5,5,0);
		gridBagConstraints.gridy = 0;
		this.setSize(300, 200);
		this.setLayout(new GridBagLayout());
		this.add(getPaValues(), gridBagConstraints);
		this.add(getPaSMF(), gridBagConstraints1);
		this.add(getBuClose(), new GridBagConstraints(0,1, 2,1, 1d,0d, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0));
	}

	private JButton getBuClose() {
		if (buClose == null) {
			buClose = new JButton(Messages.getString("SpecialValuesConfigurator.Close")); //$NON-NLS-1$
		}
		return buClose;
	}


	/**
	 * This method initializes paValues	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPaValues() {
		if (paValues == null) {
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.fill = GridBagConstraints.BOTH;
			gridBagConstraints3.gridy = 1;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.weighty = 1.0;
			gridBagConstraints3.insets = new Insets(5,0,0,0);
			gridBagConstraints3.gridx = 0;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.fill = GridBagConstraints.BOTH;
			gridBagConstraints2.gridy = 0;
			paValues = new JPanel();
			paValues.setLayout(new GridBagLayout());
			paValues.add(getPaControl(), gridBagConstraints2);
			paValues.add(getScrollpaneValues(), gridBagConstraints3);
		}
		return paValues;
	}

	/**
	 * This method initializes paSMF	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPaSMF() {
		if (paSMF == null) {
			paSMF = new JPanel();
			paSMF.setLayout(new GridBagLayout());
		}
		return paSMF;
	}

	/**
	 * This method initializes paControl	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPaControl() {
		if (paControl == null) {
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.insets = new Insets(5, 5, 5, 0);
			gridBagConstraints5.weightx = 1.0D;
			gridBagConstraints5.anchor = GridBagConstraints.EAST;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.insets = new Insets(5, 5, 5, 5);
			paControl = new JPanel();
			paControl.setLayout(new GridBagLayout());
			paControl.add(getBuAdd(), gridBagConstraints5);
			paControl.add(getBuRemove(), gridBagConstraints4);
			paControl.setBorder(new EtchedBorder());
		}
		return paControl;
	}

	/**
	 * This method initializes scrollpaneValues	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getScrollpaneValues() {
		if (scrollpaneValues == null) {
			scrollpaneValues = new JScrollPane();
			scrollpaneValues.setViewportView(getLiValues());
		}
		return scrollpaneValues;
	}

	/**
	 * This method initializes liValues	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getLiValues() {
		if (liValues == null) {
			liValues = new JList();
		}
		return liValues;
	}

	/**
	 * This method initializes buAdd	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBuAdd() {
		if (buAdd == null) {
			buAdd = new JButton();
			buAdd.setText(Messages.getString("SpecialValuesConfigurator.Add")); //$NON-NLS-1$
		}
		return buAdd;
	}

	/**
	 * This method initializes buRemove	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBuRemove() {
		if (buRemove == null) {
			buRemove = new JButton();
			buRemove.setText(Messages.getString("SpecialValuesConfigurator.Remove")); //$NON-NLS-1$
		}
		return buRemove;
	}

}
