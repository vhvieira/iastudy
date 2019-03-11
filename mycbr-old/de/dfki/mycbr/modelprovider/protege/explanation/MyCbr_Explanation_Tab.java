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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.FocusManager;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import de.dfki.mycbr.CBRProject;
import de.dfki.mycbr.explanation.ConceptExplanationProvider;
import de.dfki.mycbr.modelprovider.protege.MyCBRMenu;
import de.dfki.mycbr.modelprovider.protege.similaritymodeling.MyCbr_Similarities_Tab;
import edu.stanford.smi.protege.widget.AbstractTabWidget;

public class MyCbr_Explanation_Tab extends AbstractTabWidget implements
		TreeSelectionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String TAB_LABEL = Messages
			.getString("Explanation_editor"); //$NON-NLS-1$

	public static ImageIcon ICON_MYCBR = null;
	{
		try {
			ICON_MYCBR = new ImageIcon(MyCbr_Similarities_Tab.class
					.getResource("logo_mycbr.png")); //$NON-NLS-1$
		} catch (Throwable ex) {
			// ignore
		}
	}

	private ConceptTreePanel paConceptTree;

	private ConceptExplanationProvider conceptExpProvider;

	private ConceptExplanationSchemePanel paExpScheme;

	public void initialize() {

		CBRProject.getInstance(this);

		setLabel(TAB_LABEL);
		if (ICON_MYCBR != null)
			setIcon(ICON_MYCBR);
		setLayout(new GridBagLayout());
		conceptExpProvider = ConceptExplanationProvider.getInstance();

		paConceptTree = new ConceptTreePanel(this);
		paConceptTree.setRoot(conceptExpProvider.getRoot());

		paExpScheme = new ConceptExplanationSchemePanel(conceptExpProvider);

		JSplitPane splitter = createLeftRightSplitPane(Messages
				.getString("Mycbr.explanation.tab.left"), 250); //$NON-NLS-1$
		splitter.setLeftComponent(new JScrollPane(paConceptTree));
		splitter.setRightComponent(paExpScheme);
		add(splitter, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						5, 5, 5, 5), 0, 0));

		TreePath path = paConceptTree.getTree().getSelectionPath();
		DefaultMutableTreeNode node = null;
		if (path != null) {
			node = (DefaultMutableTreeNode) path.getLastPathComponent();
		}
		paExpScheme.setSelectedNode(node);

	}

	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = null;
		if (e.getPath() != null && e.getPath().getLastPathComponent() != null) {
			node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
		}
		paExpScheme.setSelectedNode(node);
	}

	@Override
	public void dispose() {
		super.dispose();
		MyCBRMenu.removeMenuFrom(getMainWindowMenuBar());
	}

	/**
	 * Implemented from Protege fw.
	 * Protege calls this method before save().
	 * Checks all opened smfunctions whether they have been changed or not. 
	 */
	public boolean canSave() {
		return true;
	}

	/**
	 * Saves all data.
	 * Method canSave() has been called before this. So we dont have to check
	 * for problems that might occur.
	 */
	public void save() {
			FocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
			
			String projectDir = new File(getProject().getProjectDirectoryURI()).getAbsolutePath();
			String projectName = getProject().getName();
			CBRProject.getInstance().save(projectName, projectDir);
		
	}

	@Override
	public boolean canClose() {
		return true;
	}
	
	@Override
	public void close() {
		CBRProject.resetInstance();
	}
}
