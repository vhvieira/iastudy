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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.model.vocabulary.SpecialValueHandler;

/**
 * 
 * This implements the common Test Panel, which is used by 
 * SMFPanel_String_Character, SMFPanel_String_Word and some
 * others. 
 */
public class SMFPanel_SMF_Test extends SMFPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JLabel laHeader = null;
	private JTextField tfQuery = null;
	private JTextField tfCase = null;
	private JButton buTestSimilarity = null;
	private JTextField txtTestResult = null;
	private JCheckBox buTestWhileEditing = null;
	private DecimalFormat doubleformatter = new DecimalFormat( "###,##0.00#####" ); //$NON-NLS-1$
	
	private String getExampleValue(ModelSlot slot) {
		ValueType v = slot.getValueType();
		if (v == ValueType.STRING) {
			return Messages.getString("Example_text"); //$NON-NLS-1$
		} else if (v == ValueType.FLOAT) {
			return slot.getMinimumValue().toString();
		} else if (v == ValueType.INTEGER) {
			return slot.getMinimumValue().toString();
		} else if (v == ValueType.SYMBOL) {
			String result = (String) slot.getAllowedValues().iterator().next();
			if (result == null) {
				return "";  //$NON-NLS-1$
			} else {
				return result;
			}
		}
		return ""; //$NON-NLS-1$
	}
	
	public SMFPanel_SMF_Test(AbstractSMFunction smf) {
		super(smf);
		init();
		customInit();
		if (smf.getModelInstance() instanceof ModelSlot) {
			tfCase.setText(getExampleValue((ModelSlot) smf.getModelInstance()));
			tfQuery.setText(getExampleValue((ModelSlot) smf.getModelInstance()));
		} else {
			tfCase.setText(""); //$NON-NLS-1$
			tfQuery.setText(""); //$NON-NLS-1$
		}
	}
	
	private void customInit() {
		buTestSimilarity.addActionListener(this);
		KeyAdapter kl = new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (buTestWhileEditing.isSelected()) {
					actionPerformed(null);
				}
			}
		};
		tfQuery.addKeyListener(kl);
		tfCase.addKeyListener(kl);
	}
	
	/**
	 * Returns the value s converted to a proper slot value object.
	 * Returns null on error.
	 * 
	 */
	public Object slotValueFromStr(ModelSlot slot, String s) {
		SpecialValueHandler svh = SpecialValueHandler.getInstance();
		Object x = svh.getSpecialValueFromStr(s);
		if (x != null) {
			return x; // it is a special value
		}
		ValueType v = slot.getValueType();
		if (v == ValueType.INTEGER) {
			try {
				int i = Integer.parseInt(s);
				if (i >= (Integer)slot.getMinimumValue() && i <= (Integer)slot.getMaximumValue()) 
					return i;
			} catch (Exception e) {
				return null;
			}
		} else if (v == ValueType.FLOAT) {
			try {
				float f = Float.parseFloat(s);
				if (f >= (Float)slot.getMinimumValue() && f <= (Float)slot.getMaximumValue())
					return f;
			} catch (Exception e) {
				return null;
			}
		} else if (v == ValueType.BOOLEAN) {
			return Boolean.parseBoolean(s);
		} else if (v == ValueType.STRING) {
			return s;
		} else if (v == ValueType.SYMBOL) {
			for (Object sym : slot.getAllowedValues()) {
				if (sym.equals(s)) {
					return sym;
				}
			}
			return null; // not in symbol list
		}
		return null; // return null on error
	}
	
	public void actionPerformed(ActionEvent e) {
		try {
			Object q = slotValueFromStr((ModelSlot)_smf.getModelInstance(), tfQuery.getText());
			Object c = slotValueFromStr((ModelSlot)_smf.getModelInstance(), tfCase.getText());
			SpecialValueHandler svh = SpecialValueHandler.getInstance();
			if (q == null) {
				txtTestResult.setText(Messages.getString("Query_invalid")); //$NON-NLS-1$
			} else if (c == null) {
				txtTestResult.setText(Messages.getString("Case_invalid")); //$NON-NLS-1$
			} else {	// Special Values:
				double sim = 0.0;
				if (svh.isSpecialValue(q) || svh.isSpecialValue(c)) {
					sim = svh.getSimilarityBetween(q, c, null);
				} else {
					try {
						_smf.startRetrieval();
					} catch (Exception e2) {
						String msg = e2.getMessage();
						for (StackTraceElement x : e2.getStackTrace()) {
							msg = msg + "\n" + x.toString(); //$NON-NLS-1$
						}
						JOptionPane.showMessageDialog(this, msg);
						txtTestResult.setText(Messages.getString("Error")); //$NON-NLS-1$
					}
					sim = _smf.getSimilarityBetween(q, c, null);
					_smf.finishRetrieval();
				}
				txtTestResult.setText(doubleformatter.format(sim));
			}
		} catch (Exception e1) {
			txtTestResult.setText(Messages.getString("Error")); //$NON-NLS-1$
		}
	}
	
	public void updateSim() {
		if (buTestWhileEditing.isSelected()) {
			actionPerformed(null);
		}
	}
	
	/**
	 * This method initializes paTest	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private void init() {
		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
		gridBagConstraints4.fill = java.awt.GridBagConstraints.NONE;
		gridBagConstraints4.gridy = 2;
		gridBagConstraints4.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints4.weighty = 0;
		gridBagConstraints4.insets = new java.awt.Insets(5,5,5,5);
		gridBagConstraints4.gridx = 0;
		GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
		gridBagConstraints5.gridy = 1;
		gridBagConstraints5.fill = java.awt.GridBagConstraints.NONE;
		gridBagConstraints5.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints5.weighty = 0;
		gridBagConstraints5.insets = new java.awt.Insets(5,5,5,5);
		gridBagConstraints5.gridx = 0;
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		gridBagConstraints3.gridy = 3;
		gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints3.weighty = 1.0D;
		gridBagConstraints3.insets = new java.awt.Insets(5,0,5,5);
		gridBagConstraints3.gridx = 0;
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints2.weighty = 0.0D;
		gridBagConstraints2.gridy = 2;
		gridBagConstraints2.gridx = 0;
		gridBagConstraints2.insets = new java.awt.Insets(25,5,5,5);
		gridBagConstraints2.gridwidth = 4;
		gridBagConstraints2.weightx = 1.0;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints1.gridy = 1;
		gridBagConstraints1.weightx = 1.0;
		gridBagConstraints1.weighty = 0.0D;
		gridBagConstraints1.insets = new java.awt.Insets(25,5,5,5);
		gridBagConstraints1.gridwidth = 4;
		gridBagConstraints1.gridx = 0;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.gridwidth = 4;
		gridBagConstraints.insets = new java.awt.Insets(5,5,5,5);
		gridBagConstraints.gridy = 0;
		laHeader = new JLabel();
		laHeader.setText(Messages.getString("Compare_these_values")); //$NON-NLS-1$
		this.setLayout(new GridBagLayout());
		this.add(laHeader, gridBagConstraints);
		this.add(new JLabel(Messages.getString("Query")), gridBagConstraints5); //$NON-NLS-1$
		this.add(getTfQuery(), gridBagConstraints1);
		this.add(new JLabel(Messages.getString("Case")), gridBagConstraints4); //$NON-NLS-1$
		this.add(getTfCase(), gridBagConstraints2);
		JPanel p = new JPanel();
		p.add(getBuTestSimilarity());
		p.add(getTxtTestResult());
		p.add(getBuTestWhileEditing());
		this.add(p, gridBagConstraints3);
	}

	/**
	 * This method initializes txtString1	
	 * 	
	 * @return javax.swing.JTextPane	
	 */
	private JTextField getTfQuery() {
		if (tfQuery == null) {
			tfQuery = new JTextField();
			tfQuery.setPreferredSize(new java.awt.Dimension(100, 20));
			tfQuery.setText(Messages.getString("Query_example_test")); //$NON-NLS-1$
		}
		return tfQuery;
	}

	/**
	 * This method initializes txtString2	
	 * 	
	 * @return javax.swing.JTextPane	
	 */
	private JTextField getTfCase() {
		if (tfCase == null) {
			tfCase = new JTextField();
			tfCase.setPreferredSize(new java.awt.Dimension(100, 20));
		}
		return tfCase;
	}

	/**
	 * This method initializes buTestSimilarity	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBuTestSimilarity() {
		if (buTestSimilarity == null) {
			buTestSimilarity = new JButton();
			buTestSimilarity.setText(Messages.getString("Test")); //$NON-NLS-1$
		}
		return buTestSimilarity;
	}

	/**
	 * This method initializes txtTestResult	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTxtTestResult() {
		if (txtTestResult == null) {
			txtTestResult = new JTextField();
			txtTestResult.setPreferredSize(new java.awt.Dimension(70,22));
			txtTestResult.setEditable(false);
			txtTestResult.addFocusListener(Helper.focusListener);
		}
		return txtTestResult;
	}
	
	/**
	 * This method initializes buTestWhileEditing	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JCheckBox getBuTestWhileEditing() {
		if (buTestWhileEditing == null)	{
			buTestWhileEditing = new JCheckBox(Messages.getString("Test_while_edition_test_values")); //$NON-NLS-1$
			buTestWhileEditing.setSelected(true);
		}
		return buTestWhileEditing;
	}
}
