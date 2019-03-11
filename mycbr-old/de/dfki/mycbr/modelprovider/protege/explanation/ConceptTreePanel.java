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
package de.dfki.mycbr.modelprovider.protege.explanation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import de.dfki.mycbr.Helper;
import edu.stanford.smi.protege.util.ComponentFactory;

public class ConceptTreePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	
	private JTree tree;

	public ConceptTreePanel(MyCbr_Explanation_Tab myCbr_Explanation_Tab) {
		setLayout(new GridBagLayout());
		tree = new JTree();
		
		add(createHeaderPanel(), 	new GridBagConstraints(0,0, 1,1, 1.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		add(tree, 					new GridBagConstraints(0,1, 1,1, 0.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
		
		tree.addTreeSelectionListener(myCbr_Explanation_Tab);
	}


	public void setRoot(DefaultMutableTreeNode root) {
		tree.setModel(new DefaultTreeModel(root));
	}
	
	private JPanel createHeaderPanel() {
		String editorLabel = Messages.getString("ConceptTreePanel.Concept_browser"); //$NON-NLS-1$
		JPanel paHeader = new JPanel(new BorderLayout());
		JPanel titlePanel = new JPanel(new BorderLayout());
		JLabel titleLabel = ComponentFactory.createTitleFontLabel(editorLabel.toUpperCase());
		titleLabel.setForeground(Color.white);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 2));
		titlePanel.add(titleLabel);
		paHeader.add(titlePanel, BorderLayout.NORTH);
		titlePanel.setBackground(Helper.COLOR_RED_MYCBR);
		return paHeader;
	}

	public JTree getTree() {
		return tree;
	}

}
