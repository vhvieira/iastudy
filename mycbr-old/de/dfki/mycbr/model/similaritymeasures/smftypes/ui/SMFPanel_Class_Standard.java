/*
 * MyCBR License 1.1
 *
 * Copyright (c) 2008
 * Thomas Roth-Berghofer, Armin Stahl & Deutsches Forschungszentrum f&uuml;r K&uuml;nstliche Intelligenz DFKI GmbH
 * Further contributors: myCBR Team (see http://mycbr-project.net/contact.html for further information 
 * about the myCBR Team). 
 * All rights reserved.
 *
 * MyCBR is free software; you can redistribute it and/or modify
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
 * Since MyCBR uses some modules, you should be aware of their licenses for
 * which you should have received a copy along with this program, too.
 * 
 * endOfLic */
package de.dfki.mycbr.model.similaritymeasures.smftypes.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.explanation.ExplanationManager;
import de.dfki.mycbr.explanation.SlotStatistic;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.SMFContainer;
import de.dfki.mycbr.model.similaritymeasures.SMFHolder;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Class_Standard;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Class_Standard.SlotAmalgamation;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.model.vocabulary.ModelSlot;

/**
 * @author myCBR Team
 *
 */
public class SMFPanel_Class_Standard extends SMFPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private static final String LABEL_LOCAL_SMF_ACTIVE = "Active SMF"; //$NON-NLS-1$
	
	private class ExplanationRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;


		JPanel panel = new JPanel();
		JLabel laExp = new JLabel(Messages.getString("100%")); //$NON-NLS-1$
		Dimension dim;
		JPanel paInherited = new JPanel(new BorderLayout());
		JLabel laInherited = new JLabel(Messages.getString("inherited")); //$NON-NLS-1$
		
		TableCellRenderer boolRenderer; 
		TableCellRenderer doubleRenderer; 
		
		public ExplanationRenderer() {

			panel.setLayout(new BorderLayout());
			
			laExp.setHorizontalAlignment(JLabel.RIGHT);
			panel.add(laExp, BorderLayout.EAST);
			dim = laExp.getPreferredSize();
			laInherited.setEnabled(false);
			laInherited.setHorizontalAlignment(SwingConstants.CENTER);
			laInherited.setFont(laInherited.getFont().deriveFont(Font.PLAIN));
			paInherited.add(laInherited, BorderLayout.CENTER);
			
			boolRenderer = taSlots.getDefaultRenderer(Boolean.class);
			doubleRenderer = taSlots.getDefaultRenderer(Double.class);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
			// make inherited slots gray
			SlotAmalgamation sa = (SlotAmalgamation) smf.getSlotList().get(row);
			if (sa.isInherit() && col>0) {
				if (isSelected) {
					paInherited.setBackground(table.getSelectionBackground());
				} else {
					paInherited.setBackground(table.getBackground());
				}
				return paInherited;
			}
			
			Component comp = null;
			if (value instanceof Boolean) {
				return boolRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
			} else if (value instanceof Double) {
				return doubleRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
			} else {
				comp = (Component) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
			}
			if (comp.getParent() != panel) {
				panel.add(comp, BorderLayout.CENTER);
			}
			
			comp.setEnabled(!sa.isInherit());
			
			//
			// explanations
			//

			if (value != null) {
				boolean explain = ExplanationManager.getInstance().isEnabled() && (value instanceof ModelSlot);
				laExp.setVisible(explain);
				if (explain) {
					ModelSlot slot = (ModelSlot) value;
					SlotStatistic slotStat = ExplanationManager.getInstance().getSlotStatistic(slot);
					Long clsUsage = ExplanationManager.getInstance().getClsStatistic((ModelCls) smf.getModelInstance());
					
					if ((slotStat != null) && (clsUsage != null)) {
						double usagePercentage = (clsUsage==0? Double.NaN : ((double) slotStat.getUsage()) / ((double) clsUsage));
	
						double relcol = Math.min(1, usagePercentage);
						Color color = new Color((float) (1 - relcol / 3), (float) (1 - relcol / 3), (float) (1 - relcol / 2));
						panel.setBackground(color);
	
						usagePercentage *= 100;
						laExp.setText((usagePercentage == 0 ? Messages.getString("None") : Integer.toString((int) usagePercentage) + "%"));//$NON-NLS-2$ //$NON-NLS-3$
	
						laExp.setPreferredSize(dim);
					}
				}
			}
			
			return panel;
		}
	}
	
	//
	// GUI
	//
	JLabel jLabel1 = new JLabel();
	JScrollPane scrollpane = new JScrollPane();
	JRadioButton rbWeightedSum = new JRadioButton();
	JRadioButton rbMinimum = new JRadioButton();
	JRadioButton rbMaximum = new JRadioButton();
	JRadioButton rbEuclidean = new JRadioButton();
	JTable taSlots = new JTable();
	ButtonGroup bgSimilarity = new ButtonGroup();
	GridBagLayout gridBagLayout1 = new GridBagLayout();

	//
	// model
	//
	HashMap<String, Object> amalgamationMap = new HashMap<String, Object>();
	private Class_Standard_TableModel tableModel;
	private SMF_Class_Standard smf;
	
	
	public SMFPanel_Class_Standard(AbstractSMFunction smf) {
		super(smf);
		this.smf = (SMF_Class_Standard)smf;

	    tableModel = new Class_Standard_TableModel(this.smf);
		taSlots.setModel(tableModel);
		taSlots.getTableHeader().setReorderingAllowed(false);

		try {
			jbInit();
			customInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void updateTableModel() {
		tableModel.fireTableChanged(new TableModelEvent(tableModel, TableModelEvent.HEADER_ROW));
	}

	/**
	 *  
	 */
	private void customInit() {
		//
		// configure table
		//
		ExplanationRenderer explRenderer = new ExplanationRenderer();
		
		taSlots.setDefaultRenderer(Object.class, explRenderer);
		taSlots.setDefaultRenderer(Boolean.class, explRenderer);
		taSlots.setDefaultRenderer(Double.class, explRenderer);
		taSlots.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		Helper.addFocusListener_ConfirmChanges(taSlots);
		taSlots.setDefaultRenderer(AbstractSMFunction.class, new DefaultTableCellRenderer()  {
			/**
			 * 
			 */
			private static final long serialVersionUID = -6148576733915797087L;

			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				String text = null;
				if (value == null) {
					text = LABEL_LOCAL_SMF_ACTIVE;
					label.setEnabled(false);
				} else {
					text = (String) value;
					label.setEnabled(true);
				}
				label.setText(text);
				return label;
			}
		});

		JComboBox cb = new JComboBox();
		cb.setRenderer(new DefaultListCellRenderer() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				if (value == null) {
					value = LABEL_LOCAL_SMF_ACTIVE;
				}
				return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			}
		});
		
		taSlots.setDefaultEditor(AbstractSMFunction.class, new DefaultCellEditor(cb) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
				JComboBox cb = (JComboBox) super.getTableCellEditorComponent(table, value, isSelected, row, column);
				
				ModelSlot slot = (ModelSlot) table.getValueAt(row, 0);
				
				SMFHolder holder = null;
				if (slot.getValueType() == ValueType.INSTANCE) {
					Collection<Object> c = slot.getAllowedValues();
					if (c.size() > 0) {
						holder = SMFContainer.getInstance().getSMFHolderForModelInstance((ModelInstance)c.iterator().next());
					}
					
				} else {
					holder = SMFContainer.getInstance().getSMFHolderForModelInstance(slot);
				}
				
				Vector<String> localSMFs = new Vector<String>();
				localSMFs.insertElementAt(null, 0);
				if (holder != null) {
					for (Iterator<AbstractSMFunction> it = holder.values().iterator(); it.hasNext();) {
						localSMFs.add((it.next()).getSmfName());
					}
				}
				
				cb.setModel(new DefaultComboBoxModel(localSMFs));
				
				return cb;
			}
		});
		
		
		//
		// setup amalgamation map
		//
		amalgamationMap.put(SMF_Class_Standard.AMALGAMATION_MODE_EUCLIDEAN, rbEuclidean);
		amalgamationMap.put(SMF_Class_Standard.AMALGAMATION_MODE_MAXIMUM, rbMaximum);
		amalgamationMap.put(SMF_Class_Standard.AMALGAMATION_MODE_MINIMUM, rbMinimum);
		amalgamationMap.put(SMF_Class_Standard.AMALGAMATION_MODE_WEIGHTEDSUM, rbWeightedSum);

		for (Iterator<String> it = amalgamationMap.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			AbstractButton c = (AbstractButton) amalgamationMap.get(key);
			c.setName(key);
			c.addActionListener(this);
		}
		
		refresh();
	}

	void jbInit() throws Exception {
		jLabel1.setText(Messages.getString("Attributes_(slots)")); //$NON-NLS-1$
		this.setLayout(gridBagLayout1);
		rbWeightedSum.setSelected(true);
		rbWeightedSum.setText(Messages.getString("Weighted_sum")); //$NON-NLS-1$
		rbMinimum.setText(Messages.getString("Minimum")); //$NON-NLS-1$
		rbMaximum.setText(Messages.getString("Maximum")); //$NON-NLS-1$
		rbEuclidean.setText(Messages.getString("Euclidean")); //$NON-NLS-1$
		taSlots.setMaximumSize(new Dimension(0, 0));
		taSlots.setMinimumSize(new Dimension(0, 0));
		taSlots.setAutoCreateColumnsFromModel(true);
		taSlots.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		scrollpane.setAutoscrolls(false);
		scrollpane.setMaximumSize(new Dimension(32767, 32767));
		scrollpane.setPreferredSize(new Dimension(452, 100));
		this.setMinimumSize(new Dimension(409, 100));
		this.setPreferredSize(new Dimension(462, 100));
		this.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 15, 0, 0), 0, 0));
		this.add(scrollpane, new GridBagConstraints(0, 1, 3, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 0, 5), 0, 0));
		this.add(rbMinimum, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		this.add(rbMaximum, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(rbWeightedSum, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		this.add(rbEuclidean, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		scrollpane.getViewport().add(taSlots, null);
		bgSimilarity.add(rbWeightedSum);
		bgSimilarity.add(rbMinimum);
		bgSimilarity.add(rbMaximum);
		bgSimilarity.add(rbEuclidean);
	}

	public void refresh() {
		TableColumnModel tcm = taSlots.getColumnModel();
		tcm.getColumn(0).setHeaderValue(Messages.getString("Attribute")); //$NON-NLS-1$
		tcm.getColumn(1).setHeaderValue(Messages.getString("Discriminant")); //$NON-NLS-1$
		tcm.getColumn(2).setHeaderValue(Messages.getString("Weight")); //$NON-NLS-1$
		tcm.getColumn(3).setHeaderValue(Messages.getString("Local_SMF")); //$NON-NLS-1$
		tcm.getColumn(4).setHeaderValue(Messages.getString("Comment")); //$NON-NLS-1$
		updateButtons();
	}

	private void updateButtons() {
		AbstractButton c = (AbstractButton) amalgamationMap.get(smf.getAmalgamationMode());

		c.setSelected(true);

		rbEuclidean.setEnabled(true);
		rbMaximum.setEnabled(true);
		rbMinimum.setEnabled(true);
		rbWeightedSum.setEnabled(true);
		
	}

	public void actionPerformed(ActionEvent e) {
		String value = ((Component) e.getSource()).getName();
		smf.setAmalgamationMode(value);
	}
}