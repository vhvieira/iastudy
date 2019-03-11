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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ConceptualExpPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private JPanel paDescription;
	private JCheckBox cbDescription;
	private JCheckBox cbWeblink;
	private JPanel paWeblink;
	private JCheckBox cbExternal;
	private JPanel paExternal;

	public ConceptualExpPanel() {
		setName(Messages.getString("Conceptual_explanation")); //$NON-NLS-1$
		setLayout(new GridBagLayout());
		
		cbDescription = new JCheckBox(Messages.getString("Protege_description")); //$NON-NLS-1$
		add(cbDescription, 			new GridBagConstraints(0,0, 1,1, 0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		add(getPaDescription(), 	new GridBagConstraints(1,0, 1,1, 1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		
		cbWeblink 	= new JCheckBox(Messages.getString("Web_link")); //$NON-NLS-1$
		add(cbWeblink, 				new GridBagConstraints(0,1, 1,1, 0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		add(getPaWebLink(), 		new GridBagConstraints(1,1, 1,1, 1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));

		cbExternal	= new JCheckBox(Messages.getString("External_application")); //$NON-NLS-1$
		add(cbExternal, 			new GridBagConstraints(0,2, 1,1, 0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		add(getPaExternal(),	 	new GridBagConstraints(1,2, 1,1, 1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));

	}

	private Component getPaExternal() {
		if (paExternal == null) {
			paExternal = new JPanel(new GridBagLayout());
			JTextField txtDescription = new JTextField();
			paExternal.add(txtDescription, new GridBagConstraints(0,0, 1,1, 1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		}
		return paDescription;
	}

	private Component getPaWebLink() {
		if (paWeblink == null) {
			paWeblink = new JPanel(new GridBagLayout());
			JTextField txtDescription = new JTextField();
			paWeblink.add(txtDescription, new GridBagConstraints(0,0, 1,1, 1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		}
		return paDescription;
	}

	private JPanel getPaDescription() {
		if (paDescription == null) {
			paDescription = new JPanel(new GridBagLayout());
			JTextArea txtDescription = new JTextArea();
			paDescription.add(txtDescription, new GridBagConstraints(0,0, 1,1, 1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		}
		return paDescription;
	}

}
