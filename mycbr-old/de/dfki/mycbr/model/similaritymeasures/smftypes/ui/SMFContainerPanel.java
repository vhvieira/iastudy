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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.SMFContainer;
import de.dfki.mycbr.model.similaritymeasures.SMFunctionFactory;
import de.dfki.mycbr.model.similaritymeasures.smftypes.transformers.SMFTransformer;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.modelprovider.protege.similaritymodeling.MyCbr_Similarities_Tab;

/**
 * @author myCBR Team
 *
 */
public class SMFContainerPanel extends SMFPanel implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(SMFContainerPanel.class.getName());

	BorderLayout borderLayout1 = new BorderLayout();
	BorderLayout borderLayout2 = new BorderLayout();

	JPanel paContent = new JPanel();
	
	SMFPanel currentSmfPanel = null; 

	protected Widget_SimilarityMode paSimMode = new Widget_SimilarityMode();
	
	Vector<String> functionTypeNames = new Vector<String>(); 
	
	AbstractSMFunction selectedSMF;
	private HashMap<String,String> functions = new HashMap<String,String>();
	private ModelInstance inst;
	
	public SMFContainerPanel(AbstractSMFunction smf) {
		super(smf);
		inst = smf.getModelInstance();
		try {
			functions = SMFunctionFactory.createFunctionsMap(SMFunctionFactory.getSMFClassesForInstance(inst));
		} catch (Exception e) {
			log.log(Level.WARNING, "Could not collect smf types for model instance ["+inst+"]", e); //$NON-NLS-1$ //$NON-NLS-2$
		}
		selectedSMF = smf;
		log.fine("initialize ContainerPanel."); //$NON-NLS-1$
		try {
			jbInit();
			customInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void customInit() {
		// set names for similarity mode box
		functionTypeNames = new Vector<String>(functions.keySet());
		Collections.reverse(functionTypeNames);
		paSimMode.getComboBox_SimMode().setModel(new DefaultComboBoxModel(functionTypeNames));
		
		paSimMode.getComboBox_SimMode().addActionListener(this);
		
		setCurrentSMF(selectedSMF);
	}

	void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		this.add(paContent, BorderLayout.CENTER);
		this.add(paSimMode, BorderLayout.NORTH);
		paContent.setLayout(borderLayout2);
	}

	public void refresh() {
		if (selectedSMF != null) {
			paSimMode.getComboBox_SimMode().setSelectedItem(selectedSMF.getSMFunctionTypeName());
		}
		revalidate();
		repaint();
	}

	public void actionPerformed(ActionEvent e) {
		// called by combo box 'similarity mode'
		String smfTypeName = (String)paSimMode.getComboBox_SimMode().getSelectedItem();
		selectCurrentSMF(smfTypeName);
		SMFContainer.getInstance().getSMFHolderForModelInstance(selectedSMF.getModelInstance()).confirmSMFunction(selectedSMF);

		MyCbr_Similarities_Tab.instance().checkConsistency();
	}

	/**
	 * select a certain smf.
	 * @param smf EditorSMFunction to display its editor panel. 
	 */
	public void selectSMF(AbstractSMFunction smf) {
		paContent.removeAll();
		
		if (!paSimMode.getComboBox_SimMode().getSelectedItem().equals(smf.getSMFunctionTypeName())) {
			paSimMode.getComboBox_SimMode().removeActionListener(this);
			paSimMode.getComboBox_SimMode().setSelectedItem(smf.getSMFunctionTypeName());
			paSimMode.getComboBox_SimMode().addActionListener(this);
		}
		currentSmfPanel = smf.getEditorPanel();
		paContent.add(currentSmfPanel, BorderLayout.CENTER);
		refresh();
	}
	
	
	public void selectCurrentSMF(String newSmfType) {
		String currentSmfType = (selectedSMF == null ? null : selectedSMF.getSMFunctionTypeName());
		if (currentSmfType.equals(newSmfType)) {
			return;
		}

		// check for transformations
		// try to retain the 'shape' of this function.
		AbstractSMFunction smfTransformed = (AbstractSMFunction) SMFTransformer.transform(selectedSMF, selectedSMF.getValueType(), newSmfType);
		if (smfTransformed == selectedSMF) {
			// this means, the function cannot be transformed. So we return the original smf
			// Then the user will be prompted whether he wants to start from scratch with a new smf or not.
			if (showWarningMessage(selectedSMF.getEditorPanel())) {
				smfTransformed = null;
			} else {
				refresh();
				return;
			}
		}
		if (smfTransformed!=null) {
			log.fine("successfully transformed ["+currentSmfType+"] to ["+newSmfType+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//			this.functions.put(newSmfType, smfTransformed);
			selectedSMF = smfTransformed;
//			currentSmf.setHasChanged(false);
		} else {
			try {
				// init a new fresh smfunction
				log.fine("could NOT transform [" + currentSmfType + "] to [" + newSmfType + "]. So init a new one."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				String className = (String) functions.get(newSmfType);
				selectedSMF = SMFunctionFactory.createSMFunction(className, inst, selectedSMF.getSmfName());
			} catch (Exception e) {
				e.printStackTrace();
				log.log(Level.SEVERE, "error when trying to initialize new SMFunction.", e); //$NON-NLS-1$
			}
		}
		
		setCurrentSMF(selectedSMF);
	}
	
	public void setCurrentSMF(AbstractSMFunction smf) {
		selectedSMF = smf;
		selectSMF(selectedSMF);
		
		// we must change the hasChanged flag in the currentSMFunction
		// to keep this flag consistent with ours
		log.fine("current hasChangedFlag should be [false] and is ["+selectedSMF.hasChanged()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
		selectedSMF.setHasChanged(true);
	}

	private boolean showWarningMessage(Component parent) {
		int result = JOptionPane.showConfirmDialog(parent, Messages.getString("Function_cant_be_transformed"), Messages.getString("Ignore_data_loss"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
		return result == JOptionPane.YES_OPTION;
	}

}

