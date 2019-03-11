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

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_String_Standard;

/**
 * @author myCBR Team
 *
 */
public class SMFPanel_String_Standard extends SMFPanel_String_Abstract implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SMF_String_Standard smf;

	public SMFPanel_String_Standard(SMF_String_Standard smf) {
		super(smf);
		this.smf = smf;
		initialize();
		customInit();
	}

	private SMFPanel_SMF_Test paTest = null;
	private JPanel paContent = null;
	private JLabel laChooseFunction = null;
	private JRadioButton rbTrigram = null;
	private JRadioButton rbExactMatch = null;
	private ButtonGroup buttonGroup = new ButtonGroup();
	
	/** maps a matching mode (String) to a radiobutton (JRadioButton) */
	private HashMap<String, JRadioButton> modeRadiobuttonMap = new HashMap<String, JRadioButton>();
	private JRadioButton rbPartialMatch = null;
	/**
	 * This method initializes 
	 * 
	 */

	private void customInit() {
		buttonGroup.add(rbExactMatch);
		buttonGroup.add(rbTrigram);
		buttonGroup.add(rbPartialMatch);
		actionPerformed(null);
		
		// add this as actionlistener to some buttons, etc...
		rbExactMatch.addActionListener(this);
		rbTrigram.addActionListener(this);
		rbPartialMatch.addActionListener(this);
		
		modeRadiobuttonMap.put(SMF_String_Standard.MODE_EXACT_MATCH_STR, rbExactMatch);
		modeRadiobuttonMap.put(SMF_String_Standard.MODE_TRIGRAM_STR, rbTrigram);
		modeRadiobuttonMap.put(SMF_String_Standard.MODE_PARTIAL_MATCH_STR, rbPartialMatch);
		
		refresh();
	}

	private void refresh() {
		((JRadioButton)modeRadiobuttonMap.get(smf.getMatchingMode())).setSelected(true);
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setLayout(new GridBagLayout());
		this.add(getPaContent(), new GridBagConstraints(0,1, 1,1, 1d,0d, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0));	
		this.add(getPaTest(), new GridBagConstraints(0,2, 1,1, 1d,1d, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0));
	}

	/**
	 * This method initializes paTest	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPaTest() {
		if (paTest == null) {
			paTest = new SMFPanel_SMF_Test(smf);
		}
		return paTest;
	}

	/**
	 * This method initializes paContent	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPaContent() {
		if (paContent == null) {
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints8.insets = new java.awt.Insets(5,25,5,5);
			gridBagConstraints8.gridy = 2;
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints7.insets = new java.awt.Insets(5,25,0,0);
			gridBagConstraints7.gridy = 1;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints6.insets = new java.awt.Insets(5,25,0,0);
			gridBagConstraints6.gridy = 3;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.insets = new java.awt.Insets(5,5,5,5);
			gridBagConstraints5.gridy = 0;
			gridBagConstraints5.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints5.gridx = 0;
			laChooseFunction = new JLabel();
			laChooseFunction.setText(Messages.getString("Choose_similarity_function")); //$NON-NLS-1$
			paContent = new JPanel();
			paContent.setLayout(new GridBagLayout());
			paContent.add(laChooseFunction, gridBagConstraints5);
			paContent.add(getRbTrigram(), gridBagConstraints6);
			paContent.add(getRbExact(), gridBagConstraints7);
			paContent.add(getRbPartialMatch(), gridBagConstraints8);
		}
		return paContent;
	}

	/**
	 * This method initializes rbTrigram	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbTrigram()	{
		if (rbTrigram == null) {
			rbTrigram = new JRadioButton();
			rbTrigram.setText(Messages.getString("Trigram")); //$NON-NLS-1$
			rbTrigram.setName(SMF_String_Standard.MODE_TRIGRAM_STR);
		}
		return rbTrigram;
	}

	/**
	 * This method initializes rbExact	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbExact() {
		if (rbExactMatch == null) {
			rbExactMatch = new JRadioButton();
			rbExactMatch.setText(Messages.getString("Exact_match")); //$NON-NLS-1$
			rbExactMatch.setName(SMF_String_Standard.MODE_EXACT_MATCH_STR);
		}
		return rbExactMatch;
	}

	/**
	 * This method initializes rbPartialMatch	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbPartialMatch() {
		if (rbPartialMatch == null)	{
			rbPartialMatch = new JRadioButton();
			rbPartialMatch.setText(Messages.getString("Partial_match")); //$NON-NLS-1$
			rbPartialMatch.setName(SMF_String_Standard.MODE_PARTIAL_MATCH_STR);
		}
		return rbPartialMatch;
	}

	public void actionPerformed(ActionEvent e) {
		if (e!=null && e.getSource()instanceof JRadioButton) {
			smf.setMatchingMode(((JRadioButton)e.getSource()).getName());
		}
		paTest.updateSim();
	}

}