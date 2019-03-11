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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueAcceptorImpl;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Number_Std;

/**
 * @author myCBR Team
 *
 */
public class Number_Standard_Widget extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(Number_Standard_Widget.class.getName());

	JRadioButton rbStepL = new JRadioButton();

	JRadioButton rbPolynomialL = new JRadioButton();

	JRadioButton rbSmoothStepL = new JRadioButton();

	JRadioButton rbConstL = new JRadioButton();

	JTextField txtStepL = new JTextField();

	JTextField txtPolynomialL = new JTextField();

	JTextField txtSmoothStepL = new JTextField();

	JTextField txtConstL = new JTextField();

	JRadioButton rbPolynomialR = new JRadioButton();

	JTextField txtPolynomialR = new JTextField();

	JRadioButton rbSmoothStepR = new JRadioButton();

	JTextField txtConstR = new JTextField();

	JTextField txtSmoothStepR = new JTextField();

	JTextField txtStepR = new JTextField();

	JRadioButton rbConstR = new JRadioButton();

	JRadioButton rbStepR = new JRadioButton();

	GridBagLayout gridBagLayout1 = new GridBagLayout();

	HelpButton buHelpStep = new HelpButton(HelpManager.KEY_INTEGER_STANDARD_STEP);

	HelpButton buHelpPolynomial = new HelpButton(HelpManager.KEY_INTEGER_STANDARD_POLYNOMIAL);

	HelpButton buHelpSmooth = new HelpButton(HelpManager.KEY_INTEGER_STANDARD_SMOOTH);

	HelpButton buHelpConst = new HelpButton(HelpManager.KEY_INTEGER_STANDARD_CONST);

	ButtonGroup buttongroupL = new ButtonGroup();

	ButtonGroup buttongroupR = new ButtonGroup();
	
	private SMF_Number_Std smf;
	
	private HashMap< String, JRadioButton > key2rbMapL = new HashMap< String, JRadioButton >(); 
	private HashMap< String, JRadioButton > key2rbMapR = new HashMap< String, JRadioButton >(); 
	
	private HashMap< JRadioButton, JTextField > rb2txtMap = new HashMap< JRadioButton, JTextField >();

//	private JRadioButton currentRBleft;
//	private JRadioButton currentRBr;
	
	public Number_Standard_Widget(SMF_Number_Std smf)
	{
		this.smf = smf;
		try {
			jbInit();
			customInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void customInit() {
		// create rb2txtMap
		rb2txtMap.put(rbConstL, 	txtConstL);
		rb2txtMap.put(rbStepL, 		txtStepL);
		rb2txtMap.put(rbPolynomialL, txtPolynomialL);
		rb2txtMap.put(rbSmoothStepL, txtSmoothStepL);

		rb2txtMap.put(rbConstR, 	txtConstR);
		rb2txtMap.put(rbStepR, 		txtStepR);
		rb2txtMap.put(rbPolynomialR, txtPolynomialR);
		rb2txtMap.put(rbSmoothStepR, txtSmoothStepR);
		
		// create key2rbMapL
		key2rbMapL.put(SMF_Number_Std.FCT_STEP_STR, rbStepL);
		key2rbMapL.put(SMF_Number_Std.FCT_POLYNOMIAL_STR, rbPolynomialL);
		key2rbMapL.put(SMF_Number_Std.FCT_SMOOTH_STEP_STR, rbSmoothStepL);
		key2rbMapL.put(SMF_Number_Std.FCT_CONST_STR, rbConstL);
		
		// create key2rbMapR
		key2rbMapR.put(SMF_Number_Std.FCT_STEP_STR, rbStepR);
		key2rbMapR.put(SMF_Number_Std.FCT_POLYNOMIAL_STR, rbPolynomialR);
		key2rbMapR.put(SMF_Number_Std.FCT_SMOOTH_STEP_STR, rbSmoothStepR);
		key2rbMapR.put(SMF_Number_Std.FCT_CONST_STR, rbConstR);
		
		// add this as listener to all text fields
		txtStepL.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String valStr = txtStepL.getText();
				if (!ValueAcceptorImpl.getValueAcceptor_Double().accept(valStr)) {
					return;
				}
				
				double val = smf.roundIfIntegerAndDiffMode(Helper.parseDouble(valStr));
				if (val<0 || val > smf.getDiff()) {
					return;
				}

				log.fine("entered new value [" + valStr + "] for txtStepL ");
				smf.setValue(SMF_Number_Std.FCT_STEP, val, true);
				log.fine("CANNOT APPLY new value [" + valStr + "] for txtStepL ");
				refresh();
			}
		});
		
		txtPolynomialL.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String valStr = txtPolynomialL.getText();
				double val = Helper.roundDouble(Helper.parseDouble(valStr), 2);
				if (ValueAcceptorImpl.getValueAcceptor_Double().accept(valStr) && val>=0) {
					log.fine("entered new value ["+valStr+"] for txtPolynomialL ");
					smf.setValue(SMF_Number_Std.FCT_POLYNOMIAL, new Double(val), true);
					return;
				}
				log.fine("CANNOT APPLY new value [" + valStr + "] for txtPolynomialL ");
				refresh();
			}
		});
		
		txtSmoothStepL.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String valStr = txtSmoothStepL.getText();
				if (!ValueAcceptorImpl.getValueAcceptor_Double().accept(valStr)) {
					return;
				}
				double val = smf.roundIfIntegerAndDiffMode(Helper.parseDouble(valStr));
				if (val <0 || val>smf.getDiff()) {
					return;
				}

				log.fine("entered new value [" + valStr + "] for txtSmoothstepL ");
				smf.setValue(SMF_Number_Std.FCT_SMOOTH_STEP, val, true);
				log.fine("CANNOT APPLY new value [" + valStr + "] for txtSmoothstepL ");
				refresh();
			}
		});
		
		txtConstL.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String valStr = txtConstL.getText();
				if (ValueAcceptorImpl.getValueAcceptor_SimilarityValue().accept(valStr)) {
					log.fine("entered new value [" + valStr + "] for txtConstL ");
					smf.setValue(SMF_Number_Std.FCT_CONST, Helper.parseDouble(Helper.formatDoubleAsString(Helper.parseDouble(valStr))), true);
					return;
				}
				log.fine("CANNOT APPLY new value [" + valStr + "] for txtConstL ");
				refresh();
			}

		});
		
		txtStepR.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String valStr = txtStepR.getText();
				if (!ValueAcceptorImpl.getValueAcceptor_Double().accept(valStr)) {
					return;
				}
				double val = smf.roundIfIntegerAndDiffMode(Helper.parseDouble(valStr));
				if (val<0 || val > smf.getDiff()) {
					return;
				}

				log.fine("entered new value [" + valStr + "] for txtStepR ");
				smf.setValue(SMF_Number_Std.FCT_STEP, val, false);
				log.fine("CANNOT APPLY new value [" + valStr + "] for txtStepR ");
				refresh();
			}
		});

		txtPolynomialR.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String valStr = txtPolynomialR.getText();
				double val = Helper.roundDouble(Helper.parseDouble(valStr), 2);
				if (ValueAcceptorImpl.getValueAcceptor_Double().accept(valStr) && val >= 0) {
					log.fine("entered new value [" + valStr + "] for txtPolynomialR ");
					smf.setValue(SMF_Number_Std.FCT_POLYNOMIAL, new Double(val), false);
					return;
				}
				log.fine("CANNOT APPLY new value [" + valStr + "] for txtPolynomialR ");
				refresh();
			}
		});

		txtSmoothStepR.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String valStr = txtSmoothStepR.getText();
				if (!ValueAcceptorImpl.getValueAcceptor_Double().accept(valStr)) {
					return;
				}
				double val = smf.roundIfIntegerAndDiffMode(Helper.parseDouble(valStr));
				if (val<0 || val > smf.getDiff()) {
					return;
				}

				log.fine("entered new value [" + valStr + "] for txtSmoothstepR ");
				smf.setValue(SMF_Number_Std.FCT_SMOOTH_STEP, val, false);
				log.fine("CANNOT APPLY new value [" + valStr + "] for txtSmoothstepR ");
				refresh();
			}
		});

		txtConstR.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String valStr = txtConstR.getText();
				if (ValueAcceptorImpl.getValueAcceptor_SimilarityValue().accept(valStr)) {
					log.fine("entered new value [" + valStr + "] for txtConstR ");
					smf.setValue(SMF_Number_Std.FCT_CONST, Helper.parseDouble( Helper.formatDoubleAsString(Helper.parseDouble(valStr))), false);
					return;
				}
				log.fine("CANNOT APPLY new value [" + valStr + "] for txtConstR ");
				refresh();
			}

		});
		
		// add actionlistener this to all radio buttons
		rbStepL.addActionListener(this);
		rbPolynomialL.addActionListener(this);
		rbSmoothStepL.addActionListener(this);
		rbConstL.addActionListener(this);
		rbStepR.addActionListener(this);
		rbPolynomialR.addActionListener(this);
		rbSmoothStepR.addActionListener(this);
		rbConstR.addActionListener(this);

		txtConstL.addFocusListener(Helper.focusListener);
		txtConstR.addFocusListener(Helper.focusListener);
		txtPolynomialL.addFocusListener(Helper.focusListener);
		txtPolynomialR.addFocusListener(Helper.focusListener);
		txtSmoothStepL.addFocusListener(Helper.focusListener);
		txtSmoothStepR.addFocusListener(Helper.focusListener);
		txtStepL.addFocusListener(Helper.focusListener);
		txtStepR.addFocusListener(Helper.focusListener);
		
		updateRadioButtons();
	}
	

	public void refresh() {
		log.fine("refreshing data in the textFields.");
		double[] valsLeft = smf.getFctValsLeft();
		txtStepL.setText(Double.toString(valsLeft[SMF_Number_Std.FCT_STEP]));
		txtPolynomialL.setText(Double.toString(valsLeft[SMF_Number_Std.FCT_POLYNOMIAL]));
		txtSmoothStepL.setText(Double.toString(valsLeft[SMF_Number_Std.FCT_SMOOTH_STEP]));
		txtConstL.setText(Double.toString(valsLeft[SMF_Number_Std.FCT_CONST]));
		
		double[] valsRight = smf.getFctValsRight();
		txtStepR.setText(Double.toString(valsRight[SMF_Number_Std.FCT_STEP]));
		txtPolynomialR.setText(Double.toString(valsRight[SMF_Number_Std.FCT_POLYNOMIAL]));
		txtSmoothStepR.setText(Double.toString(valsRight[SMF_Number_Std.FCT_SMOOTH_STEP]));
		txtConstR.setText(Double.toString(valsRight[SMF_Number_Std.FCT_CONST]));
		
		updateRadioButtons();
	}

	void jbInit() throws Exception {
		rbStepL.setText("Step at:");
		this.setLayout(gridBagLayout1);
		rbPolynomialL.setSelected(true);
		rbPolynomialL.setText("Polynomial with:");
		rbSmoothStepL.setText("Smooth-Step at:");
		rbConstL.setText("Constant:");
		txtStepL.setText("-1");
		txtPolynomialL.setText("-1");
		txtSmoothStepL.setText("-1");
		txtConstL.setText("-1");
		rbPolynomialR.setSelected(true);
		rbPolynomialR.setText("Polynomial with:");
		txtPolynomialR.setText("-1");
		rbSmoothStepR.setText("Smooth-Step at:");
		txtConstR.setText("-1");
		txtSmoothStepR.setText("-1");
		txtStepR.setText("-1");
		rbConstR.setText("Constant:");
		rbStepR.setText("Step at:");
		this.add(rbStepL, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(rbSmoothStepL, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(rbConstL, new GridBagConstraints(0, 3, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(txtStepL, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(txtPolynomialL, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(txtSmoothStepL, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(txtConstL, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(rbStepR, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 20, 5, 5), 0, 0));
		this.add(rbPolynomialR, new GridBagConstraints(5, 1, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 20, 5, 5), 0, 0));
		this.add(txtPolynomialR, new GridBagConstraints(7, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(rbSmoothStepR, new GridBagConstraints(4, 2, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 20, 5, 5), 0, 0));
		this.add(txtConstR, new GridBagConstraints(7, 3, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(txtSmoothStepR, new GridBagConstraints(7, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(txtStepR, new GridBagConstraints(7, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(rbConstR, new GridBagConstraints(3, 3, 4, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 20, 5, 5), 0, 0));
		this.add(rbPolynomialL, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(buHelpStep, new GridBagConstraints(8, 0, 4, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(buHelpPolynomial, new GridBagConstraints(8, 1, 3, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(buHelpSmooth, new GridBagConstraints(8, 2, 2, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(buHelpConst, new GridBagConstraints(8, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		buttongroupL.add(rbStepL);
		buttongroupL.add(rbPolynomialL);
		buttongroupL.add(rbSmoothStepL);
		buttongroupL.add(rbConstL);
		buttongroupR.add(rbStepR);
		buttongroupR.add(rbPolynomialR);
		buttongroupR.add(rbSmoothStepR);
		buttongroupR.add(rbConstR);
	}

	private void updateRadioButtons() {
		boolean symmetric = smf.isSymmetryMode();
		
		txtConstL.setEnabled(false);
		txtPolynomialL.setEnabled(false);
		txtSmoothStepL.setEnabled(false);
		txtStepL.setEnabled(false);
		txtConstR.setEnabled(false);
		txtPolynomialR.setEnabled(false);
		txtSmoothStepR.setEnabled(false);
		txtStepR.setEnabled(false);
		
		String currFctLeft = SMF_Number_Std.fctToString(smf.getCurrentFctLeft());
		log.fine("current fct left : [" + currFctLeft + "]");
		log.fine("key2rbmap contains key [" + key2rbMapL.containsKey(currFctLeft) + "]");
		log.fine("rb2txtmap contains key [" + rb2txtMap.get(key2rbMapL.get(currFctLeft)) + "]");
		
		(rb2txtMap.get(key2rbMapL.get(currFctLeft))).setEnabled(true);
		String currFctRight = SMF_Number_Std.fctToString(smf.getCurrentFctRight());
		if (!symmetric) {
			(rb2txtMap.get(key2rbMapR.get(currFctRight))).setEnabled(true);
		}
		
		(key2rbMapL.get(currFctLeft)).setSelected(true);
		(key2rbMapR.get(currFctRight)).setSelected(true);
		
		if (symmetric) {
			// disable right side
			rbConstR.setEnabled(false);
			rbPolynomialR.setEnabled(false);
			rbSmoothStepR.setEnabled(false);
			rbStepR.setEnabled(false);	
		} else {
			// enable right side
			rbConstR.setEnabled(true);
			rbPolynomialR.setEnabled(true);
			rbSmoothStepR.setEnabled(true);
			rbStepR.setEnabled(true);
		}
	}


	public void actionPerformed(ActionEvent e) {
		// triggered by radiobuttons
		JRadioButton rb = (JRadioButton)e.getSource();
		log.fine("triggered radio button");
		if (key2rbMapL.values().contains(rb)) {
			// rb on the left
			for (Iterator<Entry<String, JRadioButton>> it = key2rbMapL.entrySet().iterator(); it.hasNext();) {
				Entry<String, JRadioButton> entry = it.next();
				if (entry.getValue() == rb) {
					smf.setCurrentFctLeft(SMF_Number_Std.stringToFct(entry.getKey()));
					return;
				}
			}
		}
		if (key2rbMapR.values().contains(rb)) {
			// rb on the right
			for (Iterator<Entry<String, JRadioButton>> it = key2rbMapR.entrySet().iterator(); it.hasNext();) {
				Entry<String, JRadioButton> entry = it.next();
				if (entry.getValue() == rb) {
					log.fine("current fct right = [" + (String)entry.getKey() + "]");
					smf.setCurrentFctRight(SMF_Number_Std.stringToFct((String)entry.getKey()));
					return;
				}
			}
		}
	}

}
