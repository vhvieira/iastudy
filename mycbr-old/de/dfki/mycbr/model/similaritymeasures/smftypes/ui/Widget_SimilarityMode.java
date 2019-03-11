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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author myCBR Team
 *
 */
public class Widget_SimilarityMode extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	JLabel laSimilarityMode = new JLabel();

	JComboBox cbSimMode = new JComboBox();

	HelpButton buHelp = new HelpButton(HelpManager.KEY_SIMILARITY_MODE);

	public Widget_SimilarityMode() {
		try {
			jbInit();
			customInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void customInit() {
		cbSimMode.setEditable(false);
	}

	void jbInit() throws Exception {
		laSimilarityMode.setText("Similarity mode:");
		this.setBorder( BorderFactory.createEmptyBorder( 10, 0, 0, 0 ));
		this.setLayout( new GridBagLayout() );
		cbSimMode.setMinimumSize(new Dimension(150, 19));
		cbSimMode.setPreferredSize(new Dimension(150, 19));
		this.add(laSimilarityMode, new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(cbSimMode, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 0), 0, 0));
		this.add(buHelp, new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	}

	public JComboBox getComboBox_SimMode() {
		return cbSimMode;
	}

	public JLabel getLabel() {
		return laSimilarityMode;
	}
}
