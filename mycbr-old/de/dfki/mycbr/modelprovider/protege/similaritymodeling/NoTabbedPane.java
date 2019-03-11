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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;

import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.similaritymeasures.SMFunctionFactory;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.model.vocabulary.ModelSlot;

public class NoTabbedPane extends JPanel implements ComponentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(NoTabbedPane.class.getName());

	/** this is the currently active panel */
	private static HolderPanel currentHolderPanel = null;
	
	private static final String MESSAGE_VALUE_TYPE_NOT_PROVIDED	= Messages.getString("No_similarity_measure_provided_for_this_value");  //$NON-NLS-1$
	private static final String MESSAGE_RANGE_NOT_SET			= Messages.getString("Set_minimum_and_maximum_values");  //$NON-NLS-1$
	private static final String MESSAGE_OO_INVALID_CLASSES		= Messages.getString("Amount_of_allowed_classes_must_be_one");  //$NON-NLS-1$
	
	private JLabel laMessage = new JLabel("", JLabel.CENTER); //$NON-NLS-1$
	
	public NoTabbedPane() {
		setLayout(new BorderLayout());
	}
	
	/**
	 * Call this to display the similarity functions for an Instance object in the tabbingpane.
	 * @param inst Instance to be displayed.
	 */
	public void select(ModelInstance inst) {
		if (currentHolderPanel!=null && inst==currentHolderPanel.getModelInstance()) {
			return;
		}
		if (!closeHolderPanel()) {
			return;
		}

		if (inst == null) {
			return;
		}
		
		// now create holder panel

		if (inst instanceof ModelSlot) {
			ModelSlot slot = (ModelSlot) inst;
			if (slot.getValueType()==ValueType.INTEGER || slot.getValueType()==ValueType.FLOAT) {
				if (slot.getMinimumValue()==null || slot.getMaximumValue()==null) {
					log.info(MESSAGE_RANGE_NOT_SET + ". slot [" + slot + "]"); //$NON-NLS-1$ //$NON-NLS-2$
					laMessage.setText(MESSAGE_RANGE_NOT_SET);
					add(laMessage, BorderLayout.SOUTH);
					return;
				}
			}
			if (slot.getValueType() == ValueType.INSTANCE) {
				// this slot has instance for value type
				// so we redirect to the global similarity measure for it
				if (slot.getAllowedValues().size() != 1) {
					log.info(MESSAGE_RANGE_NOT_SET + ". slot [" + slot + "]"); //$NON-NLS-1$ //$NON-NLS-2$
					laMessage.setText(MESSAGE_OO_INVALID_CLASSES);
					add(laMessage, BorderLayout.SOUTH);
					return;
				}
				inst = (ModelCls) slot.getAllowedValues().iterator().next();
			}
		}
		
		if (SMFunctionFactory.getSMFClassesForInstance(inst) == null) {
			log.info("No SMFunction class defined for this type."); //$NON-NLS-1$
			laMessage.setText(MESSAGE_VALUE_TYPE_NOT_PROVIDED);
			add(laMessage, BorderLayout.SOUTH);
			return;
		}
		log.fine("create new HolderPanel for instance [" + inst.getName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		HolderPanel paHolder = new HolderPanel(inst);
		currentHolderPanel = paHolder;
		add(currentHolderPanel, BorderLayout.CENTER);
		currentHolderPanel.checkConsistency();

	}

	public void refresh() {
		if (currentHolderPanel != null) {
			currentHolderPanel.refresh();
		}
	}
	
	//
	// from interface ComponentListener
	//
	public void componentHidden(ComponentEvent arg0) {
		// we need the method 'componentShown()'
		// all other methods of the ComponentListener
		// are listed here because Java doesnt allow
		// polymorphic inheritance (to ComponentAdapter...)
	}

	public void componentMoved(ComponentEvent arg0) {
		// we need the method 'componentShown()'
		// all other methods of the ComponentListener
		// are listed here because Java doesnt allow
		// polymorphic inheritance (to ComponentAdapter...)
	}

	public void componentResized(ComponentEvent arg0) {
		// we need the method 'componentShown()'
		// all other methods of the ComponentListener
		// are listed here because Java doesnt allow
		// polymorphic inheritance (to ComponentAdapter...)
	}

	
	/**
	 * will be called when the displayed content of tabbingpane changes.
	 * we check consistency then. 
	 */
	public void componentShown(ComponentEvent e) {
		log.fine("selected currentPanel by GUI. currentPanel is now [" + ((currentHolderPanel == null) ? "null" : currentHolderPanel.getName()) + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		log.fine("checking consistency (COMPSHOWN)"); //$NON-NLS-1$
		currentHolderPanel.checkConsistency();
	}
	
	/**
	 * Protege and myCBR share common data. Because of redundancies
	 * inconsistencies may occur. This method is used to check for this
	 * inconsistency to everything that is currently displayed.
	 */
	public void checkConsistency() {
		if (currentHolderPanel != null) {
			currentHolderPanel.checkConsistency();
		}
	}

	public boolean closeHolderPanel() {
		if (currentHolderPanel != null) {
			// close current panel
			remove(currentHolderPanel);
			currentHolderPanel=null;
		}
		remove(laMessage);
		revalidate();
		repaint();
		return true;
	}
	
}
