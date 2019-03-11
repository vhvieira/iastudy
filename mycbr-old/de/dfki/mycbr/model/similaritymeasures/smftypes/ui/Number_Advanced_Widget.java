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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueAcceptorImpl;
import de.dfki.mycbr.model.similaritymeasures.HasChangedListenerSMF;
import de.dfki.mycbr.model.similaritymeasures.smftypes.Abstract_SMF_Number;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Number_Advanced;

/**
 * @author myCBR Team
 *
 */
public class Number_Advanced_Widget extends JPanel implements HasChangedListenerSMF {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(Number_Advanced_Widget.class.getName());

	private static final String[] SIMILARITYPOINTS_HEADER = new String[] { "Distance", "Similarity" }; //$NON-NLS-1$ //$NON-NLS-2$

	private class BasicSPTableModel extends DefaultTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		/** Contains the keys for Min/Zero/Max whose values are initialized to -1/0/-1 */
		private Double[] keys = new Double[]{-1d, 0d, -1d};
		
		public BasicSPTableModel(Object[][] data, String[] header) {
			super(data, header);
			refresh();
		}

		public boolean isCellEditable(int row, int column) {
			// left column is not editable
			return column > 0;
		}
		
		public void refresh() {
			double diff = smf.getDiff();
			if (smf.getDiffOrQuotientMode() == Abstract_SMF_Number.MODE_DIFFERENCE) {
				if (diff != keys[2]) {
					// value range has been changed
					keys[2] = diff;
					keys[1] = 0d;
					keys[0] = -diff;
				}
			} else {
				if (smf.getMaxValue()/smf.getMinValue() != keys[2]) {
					// value range has been changed
					keys[2] = smf.getMaxValue()/smf.getMinValue();
					keys[1] = 1d;
					keys[0] = smf.getMinValue()/smf.getMaxValue();
				}
			}
			// refresh GUI data
			TreeMap<Double,Double> samplingPoints = smf.getSamplingPoints();
			
			// call super method because ours has been customized. 
			// we want to read from smf here. we dont want to set anything in smf.
			super.setValueAt(samplingPoints.get(keys[0]), 0, 1);
			super.setValueAt(samplingPoints.get(keys[1]), 1, 1);
			super.setValueAt(samplingPoints.get(keys[2]), 2, 1);
		}
		
		public void setValueAt(Object aValue, int row, int column) {
			String val = aValue.toString();
			if (!ValueAcceptorImpl.getValueAcceptor_SimilarityValue().accept(val)) {
				return;
			}
			super.setValueAt(aValue, row, column);
			smf.setSamplingPoint((double) keys[row], Helper.parseDouble(Helper.formatDoubleAsString(Helper.parseDouble(val))));
		}

	}

	/**
	 * Special TableModel for 'Additional Sampling Points' table.
	 * @author Daniel Bahls
	 */
	private class AditionalSPTableModel extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		/** stores all x values for the additional sampling points */
		private Vector<Double> xValues_additional;
		
		public AditionalSPTableModel() {
			updateXValues();
		}

		public String getColumnName(int column) {
			return SIMILARITYPOINTS_HEADER[column];
		}

		public int getColumnCount() {
			// we have 2 columns
			return 2;
		}

		public int getRowCount() {
			return xValues_additional.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			Double xVal = xValues_additional.get(rowIndex);
			if (columnIndex == 0) {
				return xVal;
			}
			return smf.getSamplingPoints().get(xVal);
		}

		public void addNewSP(double dist, double sim) {
			smf.setSamplingPoint(dist, Helper.parseDouble(Helper.formatDoubleAsString(sim)));
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			String val = aValue.toString();
			if (columnIndex == 0) {
				// x
				// is input correct?
				if (!ValueAcceptorImpl.getValueAcceptor_Integer().accept(val)) return;
				// changing x value -> move sampling point
				log.fine("moving x value to [" + val + "]"); //$NON-NLS-1$ //$NON-NLS-2$
				Double xVal = xValues_additional.get(rowIndex);
				Double yVal = (Double)smf.getSamplingPoints().get(xVal);
				smf.removeSamplingPoint(xVal);
				smf.setSamplingPoint((double) Helper.parseInt(val), yVal);

			}
			// y
			// is input correct?
			if (!ValueAcceptorImpl.getValueAcceptor_SimilarityValue().accept(val)) {
				return;
			}
			log.fine("changing y value to [" + val + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			// setting new value for y
			smf.setSamplingPoint(xValues_additional.get(rowIndex), Helper.parseDouble(Helper.formatDoubleAsString(Helper.parseDouble(val))));
		}

		@SuppressWarnings("unchecked") //$NON-NLS-1$
		public void updateXValues() {
			xValues_additional = new Vector(smf.getSamplingPoints().keySet());
			
			// commented out before 20.10.2008
			// remove basic sampling points
//			xValues_additional.remove(-diff);
//			xValues_additional.remove(0d);
//			xValues_additional.remove(diff);
			xValues_additional.remove(taModelBasicSP.keys[0]);
			xValues_additional.remove(taModelBasicSP.keys[1]);
			xValues_additional.remove(taModelBasicSP.keys[2]);
		}

		public void removeSP(int index) {
			smf.removeSamplingPoint(xValues_additional.get(index));
		}

		public int getXValueIndexOf(Double draggedObject) {
			int index = (xValues_additional.contains(draggedObject)?xValues_additional.indexOf(draggedObject):-1);
			return index;
		}

	}

	HelpButton buHelpASP = new HelpButton(HelpManager.KEY_INTEGER_ADVANCED_ASP);

	JPanel jPanel1 = new JPanel();

	TitledBorder titledBorder1;

	JTable taBasicSP = new JTable();

	GridBagLayout gridBagLayout1 = new GridBagLayout();

	JPanel jPanel2 = new JPanel();

	TitledBorder titledBorder2;

	GridBagLayout gridBagLayout3 = new GridBagLayout();

	JScrollPane jScrollPane1 = new JScrollPane();

	JTable taAdditionalSP = new JTable();

	GridBagLayout gridBagLayout2 = new GridBagLayout();

	HelpButton buHelpBSP = new HelpButton(HelpManager.KEY_INTEGER_ADVANCED_BSP);

	JPanel paHeader = new JPanel();

	BorderLayout borderLayout1 = new BorderLayout();

	JButton buAdd = new JButton();

	JButton buDelete = new JButton();

	JPanel paSpacer = new JPanel();

	private BasicSPTableModel taModelBasicSP;

	private AditionalSPTableModel taModelAditionalSP;

	private SMF_Number_Advanced smf;


	public Number_Advanced_Widget(SMF_Number_Advanced smf) {
		this.smf = smf;
		try {
			jbInit();
			customInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void customInit() {
		// always notify this when haschanged flag is set
		smf.addHasChangedListener(this, true);

		// basic similarity points
		taModelBasicSP = new BasicSPTableModel(new Object[][] { new String[] { "Min", "-1" }, new String[] { "0", "-1" }, new String[] { "Max", "-1" } }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				SIMILARITYPOINTS_HEADER);
		taBasicSP.setModel(taModelBasicSP);
		paHeader.add(taBasicSP.getTableHeader(), BorderLayout.CENTER);

		// additional similarity points
		taModelAditionalSP = new AditionalSPTableModel();
		taAdditionalSP.setModel(taModelAditionalSP);

		// commented out before 20.10.2008
//		taAdditionalSP.setDefaultEditor(Object.class, new DefaultCellEditor(new MyJTextField()));
//		taBasicSP.setDefaultEditor(Object.class, new DefaultCellEditor(new MyJTextField()));
		Helper.addFocusListener_ConfirmChanges(taBasicSP);
		Helper.addFocusListener_ConfirmChanges(taAdditionalSP);
		
		// buttons
		buAdd.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked") //$NON-NLS-1$
			public void actionPerformed(ActionEvent e) {
				Widget_EnterValuesDialog d = new Widget_EnterValuesDialog((Frame) getTopLevelAncestor(), smf);
				d.setVisible(true);

				Map result = d.getResultMap();
				if (result == null) {
					return;
				}

				double dist = Helper.parseDouble((String)result.get("Distance")); //$NON-NLS-1$
				double sim = Helper.parseDouble((String)result.get("Similarity")); //$NON-NLS-1$
				log.fine("added new sampling point at dist=[" + dist + "] : [" + sim + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				taModelAditionalSP.addNewSP(dist, sim);
			}
		});
		
		buDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = taAdditionalSP.getSelectedRow();
				if (index < 0) return;
				
				taModelAditionalSP.removeSP(index);
			}
		});
		
	}

	private void jbInit() throws Exception {
		titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)), Messages.getString("Basic_similarity_points")); //$NON-NLS-1$
		titledBorder2 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)), Messages.getString("Additional_similarity_points")); //$NON-NLS-1$
		this.setLayout(gridBagLayout3);
		jPanel1.setBorder(titledBorder1);
		jPanel1.setLayout(gridBagLayout1);
		jPanel2.setBorder(titledBorder2);
		jPanel2.setLayout(gridBagLayout2);
		paHeader.setLayout(borderLayout1);
		buAdd.setMaximumSize(new Dimension(70, 23));
		buAdd.setMinimumSize(new Dimension(70, 23));
		buAdd.setPreferredSize(new Dimension(70, 23));
		buAdd.setText(Messages.getString("Add")); //$NON-NLS-1$
		buDelete.setMaximumSize(new Dimension(70, 23));
		buDelete.setMinimumSize(new Dimension(70, 23));
		buDelete.setPreferredSize(new Dimension(70, 23));
		buDelete.setText(Messages.getString("Delete")); //$NON-NLS-1$
		this.add(jPanel1, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jPanel1.add(taBasicSP, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 15, 5, 10), 0, 0));
		jPanel1.add(buHelpBSP, new GridBagConstraints(1, 0, 1, 2, 0.0, 0.0
            ,GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
		jPanel1.add(paHeader, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 15, 0, 10), 0, 0));
    jPanel1.add(paSpacer, new GridBagConstraints(0, 2, 1, 1, 0.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(jPanel2, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jPanel2.add(jScrollPane1, new GridBagConstraints(0, 0, 1, 3, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 15, 5, 10), 0, 0));
		jPanel2.add(buHelpASP, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
		jPanel2.add(buAdd, new GridBagConstraints(1, 1, 1, 1, 0.0, 1.0, GridBagConstraints.SOUTH, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		jScrollPane1.getViewport().add(taAdditionalSP, null);
		jPanel2.add(buDelete, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		
	}

	/**
	 * implemented from interface HasChangedListener.
	 * Will be called whenever the hasChanged flag of the smfunction object
	 * has been set to a new value.
	 * @param hasChanged
	 */
	public void smfHasChanged(boolean hasChanged) {
		log.fine("smf has changed -> update GUI"); //$NON-NLS-1$
		if (hasChanged) {
			taModelBasicSP.refresh();
			taModelAditionalSP.updateXValues();
			taModelAditionalSP.fireTableChanged(new TableModelEvent(taModelAditionalSP, TableModelEvent.HEADER_ROW));
			taModelBasicSP.fireTableChanged(new TableModelEvent(taModelBasicSP, TableModelEvent.HEADER_ROW));
		}
	}

	public void selectSamplingPoint(Double draggedObject) {
		int index = taModelAditionalSP.getXValueIndexOf(draggedObject);
		if (index < 0) return;
		taAdditionalSP.changeSelection(index, 1, false, false);
	}

}
