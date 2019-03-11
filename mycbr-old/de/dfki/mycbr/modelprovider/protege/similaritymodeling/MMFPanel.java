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
package de.dfki.mycbr.modelprovider.protege.similaritymodeling;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import de.dfki.mycbr.CBRProject;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.HasChangedListenerSMF;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel;
import de.dfki.mycbr.model.vocabulary.ModelInstance;

/**
 * @author myCBR Team
 *
 */

public class MMFPanel extends javax.swing.JPanel implements HasChangedListenerSMF {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(HolderPanel.class.getName());

	JPanel paMMF = new JPanel();
	//TitledBorder titledBorder1;
	GridBagLayout gridBagLayout1 = new GridBagLayout();

	private ModelInstance modelInstance;
	private AbstractSMFunction currentMMFunction;
	JPanel paButtons = new JPanel();
	JButton buClose = new JButton();
	GridBagLayout gridBagLayout2 = new GridBagLayout();
	BorderLayout borderLayout2 = new BorderLayout();
	GridBagLayout gridBagLayout3 = new GridBagLayout();

	public ModelInstance getModelInstance() {
		return modelInstance;
	}

	public MMFPanel(ModelInstance inst) {
		modelInstance = inst;
		try {
			jbInit();
			customInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void customInit() {
		paButtons.setVisible(true);

		//
		// buttons
		//
		buClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				log.fine("pressed CLOSE");

				// close tab now
				((Dialog)getTopLevelAncestor()).dispose();
			}
		});

		// initialize selection
		AbstractSMFunction mmf = CBRProject.getInstance().getMMFforModelInstance(modelInstance);
		currentMMFunction = mmf;
		currentMMFunction.removeHasChangedListener(this);
		currentMMFunction.addHasChangedListener(this, false);
		paMMF.removeAll();
		// then show its panel
		SMFPanel panel = currentMMFunction.getEditorPanel();
		paMMF.add(panel, BorderLayout.CENTER);

	}


	// commented out before 20.10.2008
//	/**
//	 * Protege and Cbrtool share common data. Because of redundancies
//	 * inconsistencies may occure. This method is used to check for this
//	 * inconsistency concerning the Holder data.
//	 */
//	public void checkConsistency()
//	{
//		// get parent frame
//		Frame frame = (Frame) MyCbr_Similarities_Tab.instance().getTopLevelAncestor();
//
//		if (currentMMFunction != null)
//		{
//			currentMMFunction.checkConsistency(frame, false);
//		}
//	}

	/**
	 Refresh GUI data.
	 Check for consistent representation of the data.
	 */
	public void refresh() {
		revalidate();
		repaint();
	}


	private void jbInit() throws Exception {
		//titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)), "Available functions");
		this.setLayout(gridBagLayout3);
		paMMF.setBorder(BorderFactory.createEtchedBorder());
		paMMF.setLayout(borderLayout2);
		//			Dimension buttonSize = new Dimension(80, 23); 
		//Dimension buttonSize = new Dimension(90, 23);
		paButtons.setLayout(gridBagLayout2);
		buClose.setText("Close");
		this.add(paMMF, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 396, 96));

		this.add(paButtons, new GridBagConstraints(0, 2, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 268, 0));
		paButtons.add(buClose, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
	}


	/**
	 * Called when smf has changed.
	 * Implemented from Interface HasChangedListener.
	 */
	public void smfHasChanged(boolean hasChanged) {
		// dont know what to do..
	}

	protected AbstractSMFunction getCurrentSMFunction() {
		return currentMMFunction;
	}

	public String toString() {
		return "holderPanel for [" + currentMMFunction + "]";
	}

}
