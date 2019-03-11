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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.table.DefaultTableModel;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.model.vocabulary.SpecialValueHandler;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;

public class CreateSlotsPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String[] TABLE_HEADER = new String[]{Messages.getString("Attribute"), Messages.getString("Value_type"), Messages.getString("Comment")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	private Vector<ValueType> valueTypes;
	private Map<String,ValueType> proposedValueTypes = new HashMap<String,ValueType>();
	private Map<String,String> attributeComments = new HashMap<String,String>();
	private Vector<Vector<Object>> data = new Vector<Vector<Object>>();

	private boolean flagHasBeenConfirmed = false;

	private JTable table;
	private DefaultTableModel tableModel;

	private int threshold;
	
	private JPanel paHeader = null;
	private JPanel paButtons = null;
	private JLabel laDescription = new JLabel();
	private JLabel laSize = new JLabel();
	private JLabel laSizeTotal = new JLabel();
	private ButtonGroup buttongroup= new ButtonGroup();
	private JButton buPropose = new JButton(Messages.getString("Propose_value_types")); //$NON-NLS-1$
	private JButton buOK = new JButton(Messages.getString("Ok")); //$NON-NLS-1$
	private JButton buCancel = new JButton(Messages.getString("Cancel")); //$NON-NLS-1$
	private JTextPane txtDescriptionRelAbs = null;
	private JRadioButton rbAbsolute = null;
	private JTextField txtAbsolute = null;
	private JRadioButton rbRelative = null;
	private JTextField txtRelative = null;

	private int totalAmount;

	private Collection<String> missingAttributes;

	private Vector<String> header;

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private Vector csvData;

	private SpecialValueHandler specialValueHandler;

	private boolean parseMultiple;

	private Cls domainCls;

	private Project protegeProject;
	

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public CreateSlotsPanel(Collection<String> missingAttributes, Vector<String> header, Vector csvData, SpecialValueHandler specialValueHandler, boolean parseMultiple, Cls domainCls, Project protegeProject) {
		setLayout(new BorderLayout());
		this.totalAmount = csvData.size();
		this.missingAttributes = missingAttributes;
		this.header = header;
		this.csvData = csvData;
		this.specialValueHandler = specialValueHandler;
		this.parseMultiple = parseMultiple;
		this.domainCls = domainCls;
		this.protegeProject= protegeProject;
		laDescription.setText(Messages.getString("Listed_attributes_are_unknown")); //$NON-NLS-1$
		
		valueTypes = new Vector<ValueType>();
		valueTypes.add(ValueType.INTEGER);
		valueTypes.add(ValueType.FLOAT);
		valueTypes.add(ValueType.SYMBOL);
		valueTypes.add(ValueType.STRING);
		
		table = new JTable();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		for (String attribute: missingAttributes) {
			proposedValueTypes.put(attribute, ValueType.STRING);
		}
		updateTableModel();
		
		JComboBox box = new JComboBox(valueTypes);
		DefaultCellEditor tableCellEditor = new DefaultCellEditor(box);
		table.setDefaultEditor(ValueType.class, tableCellEditor);
		
		initHeaderPanel();
		laSizeTotal.setText(String.format(Messages.getString("Total_amount_of_imports"), totalAmount)); //$NON-NLS-1$
		initListeners();
		paButtons = new JPanel(new GridBagLayout());
		paButtons.add(buOK, 		new GridBagConstraints(1,0, 1,1, 0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0));
		paButtons.add(buCancel, 	new GridBagConstraints(0,0, 1,1, 1.0,0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0));
		
		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane, 	BorderLayout.CENTER);
		add(paHeader, 		BorderLayout.NORTH);
		add(paButtons, 		BorderLayout.SOUTH);
		
		refreshThreshold();
	}
	
	private void updateTableModel() {
		Vector<String> tableHeader = new Vector<String>();
		tableHeader.add(TABLE_HEADER[0]);
		tableHeader.add(TABLE_HEADER[1]);
		tableHeader.add(TABLE_HEADER[2]);
		data.clear();
		for (String attribute: missingAttributes) {
			Vector<Object> row = new Vector<Object>();
			row.add(attribute);
			row.add(proposedValueTypes.get(attribute));
			String comment = attributeComments.get(attribute);
			row.add((comment == null ? "" : comment)); //$NON-NLS-1$
			data.add(row);
		}
		
		tableModel = new DefaultTableModel(data, tableHeader) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				switch (columnIndex) {
					case 0: return String.class;
					case 1: return ValueType.class;
				}
				return String.class;
			}
			
			@Override
			public void setValueAt(Object value, int row, int column) {
				super.setValueAt(value, row, column);
				if (column == 1) {
					String attName = (String) getValueAt(row, 0);
					proposedValueTypes.put(attName, (ValueType) value);
				}
			}
			
			@Override
			public boolean isCellEditable(int row, int column) {
				String attName = (String) getValueAt(row, 0);
				return column == 1 && protegeProject.getKnowledgeBase().getSlot(attName)==null;
			}
		};
		table.setModel(tableModel);
		table.doLayout();
	}

	private void initListeners() {
		buPropose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				proposeValueTypes();
			}
		});
		buOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				flagHasBeenConfirmed = true;
				createSlots();
				((Window)getTopLevelAncestor()).dispose();
			}
		});
		buCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((Window) getTopLevelAncestor()).dispose();
			}
		});
		
		rbAbsolute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshThreshold();
				checkEnabled();
			}
		});
		rbRelative.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshThreshold();
				checkEnabled();
			}
		});
		txtAbsolute.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				refreshThreshold();
			};
		});
		txtRelative.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e)
			{
				refreshThreshold();
			};
		});
		
		buttongroup.add(rbAbsolute);
		buttongroup.add(rbRelative);
		checkEnabled();
	}
	
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	protected void createSlots() {
		for (String newAttName : missingAttributes) {
			int column = header.indexOf(newAttName);
			int rowCnt = totalAmount;

			ValueType vt = proposedValueTypes.get(newAttName);
			
			boolean isMultiple = false;

			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;
			for (int row = 0; row < rowCnt; row++) {
				String v = (String) ((Vector)csvData.get(row)).get(column);

				Collection<String> values = new ArrayList<String>();
				if (parseMultiple) {
					String[] split = v.split(CSVImport.getInternalSeperator());
					for (int i = 0; i < split.length; i++) {
						values.add(split[i]);
					}
					if (split.length > 1) {
						isMultiple = true;
					}
				} else {
					values.add(v);
				}

				for (String value : values) {
					if ("".equals(value)) { //$NON-NLS-1$
						continue;
					}
					if (vt == ValueType.INTEGER) {
						try {
							int tmp = Integer.parseInt(value);
							if (tmp > max) {
								max = tmp;
							}
							if (tmp < min) {
								min = tmp;
							}
						} catch (Throwable throwable) {
							continue;
						}
					} else if (vt == ValueType.FLOAT) {
						try {
							float tmp = Float.parseFloat(value);
							if (tmp > max) {
								max = tmp;
							}
							if (tmp < min) {
								min = tmp;
							}
						} catch (Throwable throwable) {
							continue;
						}
					}

				}
			}
			
			Slot slot = protegeProject.getKnowledgeBase().getSlot(newAttName);
			if (slot == null) {
				slot = protegeProject.getKnowledgeBase().createSlot(newAttName);
			}
			if (vt == ValueType.INTEGER) {
				// create Integer slot
				domainCls.addDirectTemplateSlot(slot);
				slot.setValueType(ValueType.INTEGER);
				slot.setMinimumValue(new Float(min));
				slot.setMaximumValue(new Float(max));
			} else if (vt == ValueType.FLOAT) {
				// create Integer slot
				domainCls.addDirectTemplateSlot(slot);
				slot.setValueType(ValueType.FLOAT);
				slot.setMinimumValue(new Float(min));
				slot.setMaximumValue(new Float(max));
			} else {
				// string values
				HashSet<String> allValues = new HashSet<String>();
				for (int row = 0; row < rowCnt; row++) {
					String v = (String) ((Vector)csvData.get(row)).get(column);
					if (parseMultiple) {
						String[] split = v.split(CSVImport.getInternalSeperator());
						for (int i = 0; i < split.length; i++) {
							allValues.add(split[i]);
						}
					} else {
						allValues.add(v);
					}
				}
				allValues.remove(""); //$NON-NLS-1$
				allValues.removeAll(specialValueHandler.getAllSpecialValuesAsStrings());

				if (vt == ValueType.SYMBOL) {
					// create Symbol slot
					domainCls.addDirectTemplateSlot(slot);
					slot.setValueType(ValueType.SYMBOL);
					slot.setAllowedValues(allValues);
				} else {
					// create String slot
					domainCls.addDirectTemplateSlot(slot);
					slot.setValueType(ValueType.STRING);
				}
			}
			if (isMultiple) {
				slot.setAllowsMultipleValues(true);
			}
		}
		
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public void proposeValueTypes() {
		JDialog d = Helper.createDialog((Window) getTopLevelAncestor(), Messages.getString("Please_wait"), false); //$NON-NLS-1$
		d.getContentPane().add(new JLabel(Messages.getString("Csv_data_analysed"))); //$NON-NLS-1$
		d.pack();
		Helper.centerWindow(d);
		d.setVisible(true);
		
		for (String newAttName: missingAttributes) {
			// maybe, this attribute already exists.
			Slot slot = protegeProject.getKnowledgeBase().getSlot(newAttName);
			StringBuffer comments = new StringBuffer();
			
			int column = header.indexOf(newAttName);
			int rowCnt = totalAmount;

			boolean maybeInteger = true;
			boolean maybeFloat = true;

			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;
			for (int row = 0; row < rowCnt; row++) {
				String v = (String) ((Vector)csvData.get(row)).get(column);

				Collection<String> values = new ArrayList<String>();
				if (parseMultiple) {
					String[] split = v.split(CSVImport.getInternalSeperator());
					for (int i = 0; i < split.length; i++) {
						values.add(split[i]);
					}
					if (split.length > 1) {
					}
				} else {
					values.add(v);
				}

				for (String value : values) {
					if ("".equals(value)) { //$NON-NLS-1$
						continue;
					}
					if (maybeInteger) {
						try {
							int tmp = Integer.parseInt(value);
							if (tmp > max) {
								max = tmp;
							}
							if (tmp < min) {
								min = tmp;
							}
						} catch (Throwable throwable) {
							if (specialValueHandler.isSpecialValueStr(value)) {
								continue;
							}
							maybeInteger = false;
						}
					} else
					// if (maybeFloat)
					{
						try {
							float tmp = Float.parseFloat(value);
							if (tmp > max) {
								max = tmp;
							}
							if (tmp < min) {
								min = tmp;
							}
						} catch (Throwable throwable) {
							if (specialValueHandler.isSpecialValueStr(value)) {
								continue;
							}
							maybeFloat = false;
							break;
						}
					}

				}
				if (!maybeFloat) break;
			}
			
			if (maybeInteger) {
				proposedValueTypes.put(newAttName, ValueType.INTEGER);
				comments.append(String.format(Messages.getString("Range"), min, max)); //$NON-NLS-1$
				if (slot!=null && slot.getMinimumValue()!=null && slot.getMaximumValue()!=null)  {
					if (slot.getMinimumValue().doubleValue()==min && slot.getMaximumValue().doubleValue()==max) {
						comments.append(Messages.getString("No_changes")); //$NON-NLS-1$
					} else {
						comments.append(String.format(Messages.getString("Old_range"), slot.getMinimumValue(), slot.getMaximumValue())); //$NON-NLS-1$
					}
				}
			} else if (maybeFloat) {
				proposedValueTypes.put(newAttName, ValueType.FLOAT);
				comments.append(String.format(Messages.getString("Range"), min, max)); //$NON-NLS-1$
				if (slot!=null && slot.getMinimumValue()!=null && slot.getMaximumValue()!=null)  {
					if (slot.getMinimumValue().doubleValue()==min && slot.getMaximumValue().doubleValue()==max) {
						comments.append(Messages.getString("No_changes")); //$NON-NLS-1$
					} else {
						comments.append(String.format(Messages.getString("Old_range"), slot.getMinimumValue(), slot.getMaximumValue())); //$NON-NLS-1$
					}
				}
			} else {
				// string values
				HashSet allValues = new HashSet();
				for (int row = 0; row < rowCnt; row++) {
					String v = (String) ((Vector)csvData.get(row)).get(column);
					if (parseMultiple) {
						String[] split = v.split(CSVImport.getInternalSeperator());
						for (int i = 0; i < split.length; i++) {
							allValues.add(split[i]);
						}
					} else {
						allValues.add(v);
					}
				}
				allValues.remove(""); //$NON-NLS-1$
				allValues.removeAll(specialValueHandler.getAllSpecialValuesAsStrings());
				boolean createSymbolType = allValues.size() <= threshold;
				
				comments.append(String.format(Messages.getString("Different_values"), allValues.size())); //$NON-NLS-1$
				if (slot != null) {
					comments.append(String.format(Messages.getString("Used_to_be__values"), slot.getAllowedValues().size())); //$NON-NLS-1$
				}
				
				if (createSymbolType) {
					proposedValueTypes.put(newAttName, ValueType.SYMBOL);
				} else {
					proposedValueTypes.put(newAttName, ValueType.STRING);
				}
			}

			if (slot != null) {
				proposedValueTypes.put(newAttName, slot.getValueType());
			}
			attributeComments.put(newAttName, comments.toString());
		}
		updateTableModel();
		
		d.dispose();
	}

	protected void checkEnabled() {
		txtAbsolute.setEnabled(rbAbsolute.isSelected());
		txtRelative.setEnabled(rbRelative.isSelected());
	}

	private void refreshThreshold() {
		if (rbAbsolute.isSelected()) {
			threshold = Helper.parseInt(txtAbsolute.getText());
			if (threshold < 0) {
				threshold = 0;
			}
		} else {
			threshold = Helper.parseInt(txtRelative.getText());
			if (threshold < 0) {
				threshold = 0;
			}
			if (threshold > 100) {
				threshold=100;
			} else {
				threshold = (int)((((double)threshold)/100) * totalAmount);
			}
		}
		
		laSize.setText(Messages.getString("Threshold") + threshold); //$NON-NLS-1$
	}

	
	/**
	 * This method initializes paMore	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel initHeaderPanel() {
		if (paHeader == null) {
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 1;
			gridBagConstraints11.insets = new java.awt.Insets(5,5,5,5);
			gridBagConstraints11.anchor = java.awt.GridBagConstraints.EAST;
			gridBagConstraints11.gridy = 5;
			laSizeTotal = new JLabel();
			laSizeTotal.setText(""); //$NON-NLS-1$
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 1;
			gridBagConstraints10.anchor = java.awt.GridBagConstraints.EAST;
			gridBagConstraints10.insets = new java.awt.Insets(5,5,0,5);
			gridBagConstraints10.gridy = 4;
			laSize = new JLabel();
			laSize.setText(""); //$NON-NLS-1$
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints9.gridy = 3;
			gridBagConstraints9.weightx = 1.0;
			gridBagConstraints9.insets = new java.awt.Insets(5,5,5,5);
			gridBagConstraints9.anchor = java.awt.GridBagConstraints.NORTH;
			gridBagConstraints9.gridx = 1;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.insets = new java.awt.Insets(5,5,5,5);
			gridBagConstraints8.weighty = 1.0D;
			gridBagConstraints8.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints8.gridy = 3;
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints7.gridy = 2;
			gridBagConstraints7.weightx = 1.0;
			gridBagConstraints7.insets = new java.awt.Insets(5,5,5,5);
			gridBagConstraints7.gridx = 1;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints6.insets = new java.awt.Insets(5,5,5,5);
			gridBagConstraints6.gridy = 2;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints5.gridy = 1;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.weighty = 0.0D;
			gridBagConstraints5.gridwidth = 2;
			gridBagConstraints5.gridx = 0;
			paHeader = new JPanel();
			paHeader.setLayout(new GridBagLayout());
			paHeader.add(getTxtDescriptionRelAbs(), gridBagConstraints5);
			paHeader.add(getRbAbsolute(), gridBagConstraints6);
			paHeader.add(getTxtAbsolute(), gridBagConstraints7);
			paHeader.add(getRbRelative(), gridBagConstraints8);
			paHeader.add(getTxtRelative(), gridBagConstraints9);
			paHeader.add(laSize, gridBagConstraints10);
			paHeader.add(laSizeTotal, gridBagConstraints11);

			paHeader.add(laDescription, new GridBagConstraints(0,0, 1,1, 1.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
			paHeader.add(buPropose, 	new GridBagConstraints(1,0, 1,1, 0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0));
		}
		return paHeader;
	}

	/**
	 * This method initializes txtDescriptionRelAbs	
	 * 	
	 * @return javax.swing.JTextPane	
	 */
	private JTextPane getTxtDescriptionRelAbs() {
		if (txtDescriptionRelAbs == null) {
			txtDescriptionRelAbs = new JTextPane();
			txtDescriptionRelAbs.setText(Messages.getString("Threshold_to_destinguish_between_symbol_and_string")); //$NON-NLS-1$
		}
		return txtDescriptionRelAbs;
	}

	/**
	 * This method initializes rbAbsolute	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbAbsolute() {
		if (rbAbsolute == null) {
			rbAbsolute = new JRadioButton();
			rbAbsolute.setText(Messages.getString("Absolute_threshold")); //$NON-NLS-1$
			rbAbsolute.setSelected(true);
		}
		return rbAbsolute;
	}

	/**
	 * This method initializes txtAbsolute	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTxtAbsolute() {
		if (txtAbsolute == null) {
			txtAbsolute = new JTextField();
			txtAbsolute.setText("30"); //$NON-NLS-1$
		}
		return txtAbsolute;
	}

	/**
	 * This method initializes rbRelative	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbRelative() {
		if (rbRelative == null) {
			rbRelative = new JRadioButton();
			rbRelative.setText(Messages.getString("Relative_threshold")); //$NON-NLS-1$
		}
		return rbRelative;
	}

	/**
	 * This method initializes txtRelative	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTxtRelative() {
		if (txtRelative == null) {
			txtRelative = new JTextField();
			txtRelative.setText("25"); //$NON-NLS-1$
		}
		return txtRelative;
	}

	public boolean hasBeenConfirmed() {
		return flagHasBeenConfirmed;
	}

	public int getThreshold() {
		return threshold;
	}

	public Map<String, ValueType> getProposedValueTypes() {
		return proposedValueTypes;
	}

	public void setProposedValueTypes(Map<String, ValueType> proposedValueTypes) {
		this.proposedValueTypes = proposedValueTypes;
		updateTableModel();
	}
	
}
