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
package de.dfki.mycbr.explanation.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import de.dfki.mycbr.explanation.ExplanationManager;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;

public class Widget_Explanation_Symbols extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ExplanationManager expManager;
	private JPanel paTools = new JPanel(new GridBagLayout());

	private JCheckBox cbShowOccurrences = new JCheckBox(Messages.getString("Show_occurrences_in_casebase")); //$NON-NLS-1$
	private JCheckBox cbShowCBasQuery 	= new JCheckBox(Messages.getString("Show_comparisons_for_casebase")); //$NON-NLS-1$

	public Widget_Explanation_Symbols(AbstractSMFunction smf) {
		this.expManager = ExplanationManager.getInstance();
		setName(Messages.getString("Explanatory_design_support")); //$NON-NLS-1$
		setLayout(new GridBagLayout());

		paTools.add(cbShowOccurrences,	new GridBagConstraints(0,0, 1,1, 1d,0d, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		paTools.add(cbShowCBasQuery,	new GridBagConstraints(0,1, 1,1, 0d,1d, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		
		cbShowOccurrences.addActionListener(this);
		cbShowCBasQuery.addActionListener(this);
		
		if (expManager!=null) {
			cbShowOccurrences.setSelected(expManager.isEnabled_symbol_showOccurrences());
			cbShowCBasQuery.setSelected(expManager.isEnabled_symbol_showCBasQuery());
		}
		add(new StandardExplanationPanel(),	new GridBagConstraints(0,0, 1,1, 0d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		add(paTools, new GridBagConstraints(0,1, 1,1, 1d,1d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		
		checkCBSettings();
	}

	public void actionPerformed(ActionEvent e) {
		checkCBSettings();
		getTopLevelAncestor().repaint();
	}

	private void checkCBSettings() {
		if (expManager!= null) {
			expManager.setEnabled_Symbol_ShowOccurrences(cbShowOccurrences.isSelected());
			expManager.setEnabled_Symbol_ShowCBasQuery(cbShowCBasQuery.isSelected());
		}
	}
}
