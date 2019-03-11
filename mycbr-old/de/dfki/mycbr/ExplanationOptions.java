/*
 * myCBR License 1.1
 * 
 * Copyright (c) 2008
 * Thomas Roth-Berghofer, Armin Stahl & Deutsches Forschungszentrum f&uuml;r K&uuml;nstliche Intelligenz DFKI GmbH
 * Further contributors: myCBR Team (see http://mycbr-project.net/contact.html for further information 
 * about the myCBR Team). 
 * All rights reserved.
 *
 * myCBR is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Since myCBR uses some modules, you should be aware of their licenses for
 * which you should have received a copy along with this program, too.
 * 
 * endOfLic**/
package de.dfki.mycbr;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdom.Element;

import de.dfki.mycbr.explanation.ExplanationManager;
import de.dfki.mycbr.retrieval.ui.RetrievalWidget;

/**
 * Represents tab for options of explanation functionality
 * @author myCBR Team
 * @since myCBR v2.6.3
 */
public class ExplanationOptions extends JPanel implements ProjectOptions.OptionsListener, ChangeListener {

	private static final long serialVersionUID = 1L;
	public static final String XML_ATT_EXPLANATIONS	= "explanations"; //$NON-NLS-1$
	public static final String XML_ATT_CONCEPT_EXPLANATIONS	= "concept_explanations"; //$NON-NLS-1$
	public static final String XML_ATT_LS = "ls"; //$NON-NLS-1$
	public static final String XML_ATT_WLS = "wls"; //$NON-NLS-1$
	public static final String XML_ATT_HIGHLIGHT_SIMILARITY		= "highlightSimilarity"; //$NON-NLS-1$

	private boolean highlightSimilarity				= true;

	private boolean isExplanationEnabled;
	private boolean isConceptExplanations;
	private boolean isLS;
	private boolean isWLS;
	
	private JCheckBox cbExplanations;
	private JCheckBox cbConceptExplanation	= new JCheckBox("Enable concept explanations");
	private JCheckBox cbLS  				= new JCheckBox("Show local similarities");
	private JCheckBox cbWLS					= new JCheckBox("Show weights");

	private JCheckBox cbHighlightSim;
	public static ExplanationOptions instance;
	
	/**
	 * Constructor for the explanation options panel.
	 * A CBRProject Object is needed to get an instance of the current
	 * explanation manager.
	 * 
	 * @param cbrProject the current cbr project 
	 */
	private ExplanationOptions() {
		initialize();
	}
	
	public static ExplanationOptions getInstance() {
		return instance;
	}

	public static ExplanationOptions initInstance() {
		instance = new ExplanationOptions();	
		return instance;
	}
	
	public static void resetInstance() {
		instance = null;
	}
	
	public JCheckBox getCBHighlightSim() {
		if (cbHighlightSim == null) {
			cbHighlightSim 	= new JCheckBox(Messages.getString("Similarity_colouring_in_retrieval_widget"), isHighlightSimilarity()); //$NON-NLS-1$
		}
		return cbHighlightSim;
	}
	public boolean isHighlightSimilarity() {
		return highlightSimilarity;
	}

	public void setHighlightSimilarity(boolean highlightSimilarity) {
		ProjectOptions.setHasChanged();
		this.highlightSimilarity = highlightSimilarity;
	}
	
	/**
	 * initializes the options tab for explanations
	 */
	private void initialize() {
		if (cbExplanations == null) {
			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			cbExplanations = new JCheckBox(Messages.getString("Use_Explanations"));
			cbExplanations.addChangeListener(this);
			
			this.add(cbExplanations);
			this.add(cbConceptExplanation);	
			this.add(cbLS);
			this.add(cbWLS);
			this.add(getCBHighlightSim());
			
			this.add(Box.createRigidArea(new Dimension(3,3)));
			this.add(Box.createVerticalGlue());
		}
	}
	
	/**
	 * Sets the selection of the combo box according to the internal value for this option.
	 * Be aware that the internal value may differ from the displayed value, because of the cancel button.
	 * The internal option values are equal to the displayed ones when the user presses the ok button.
	 */
	public void refresh() {
		cbExplanations.setSelected(isExplanationEnabled);
		cbConceptExplanation.setSelected(isConceptExplanations);
		cbLS.setSelected(isLS);
		cbWLS.setSelected(isWLS);		
		cbHighlightSim.setSelected(this.highlightSimilarity);
	}
	
	/**
	 * Returns the selection of the combo box.
	 * Be aware that the internal value may differ from the displayed value, because of the cancel button.
	 * The internal option values are equal to the displayed ones when the user presses the ok button.
	 * 
	 * @return true, if cbExplanations is selected, false otherwise
	 */
	public boolean isExplanationsEnabled() {
		return cbExplanations.isSelected();
	}
	
	/**
	 * Sets the selection of cbExplanations to value of enabled.
	 * Be aware that the internal value may differ from the displayed value, because of the cancel button.
	 * The internal option values are equal to the displayed ones when the user presses the ok button.
	 * 
	 * @param enabled specifying whether the combo box should be enabled or not.
	 */
	public void enableExplanations(boolean enabled) {
		cbExplanations.setSelected(enabled);
	}

	/**
	 * Returns the selection of the combo box.
	 * Be aware that the internal value may differ from the displayed value, because of the cancel button.
	 * The internal option values are equal to the displayed ones when the user presses the ok button.
	 * 
	 * @return true, if cbConceptExplanations is selected, false otherwise
	 */
	public boolean isConceptExplanationsEnabled() {
		return cbConceptExplanation.isSelected();
	}
	
	/**
	 * Sets the selection of cbConceptExplanation to value of enabled.
	 * Be aware that the internal value may differ from the displayed value, because of the cancel button.
	 * The internal option values are equal to the displayed ones when the user presses the ok button.
	 * 
	 * @param enabled specifying whether the combo box should be enabled or not.
	 */
	public void enableConceptExplanations(boolean enabled) {
		cbConceptExplanation.setSelected(enabled);
	}

	/**
	 * Returns the selection of the combo box.
	 * Be aware that the internal value may differ from the displayed value, because of the cancel button.
	 * The internal option values are equal to the displayed ones when the user presses the ok button.
	 * 
	 * @return true, if cbLS is selected, false otherwise
	 */
	public boolean isLocalSimilarityEnabled() {
		return cbLS.isSelected();
	}
	
	/**
	 * Sets the selection of cbLS to value of enabled.
	 * Be aware that the internal value may differ from the displayed value, because of the cancel button.
	 * The internal option values are equal to the displayed ones when the user presses the ok button.
	 * 
	 * @param enabled specifying whether the combo box should be enabled or not.
	 */
	public void enableLocalSimilarity(boolean enabled) {
		cbLS.setSelected(enabled);
	}

	/**
	 * Returns the selection of the combo box.
	 * Be aware that the internal value may differ from the displayed value, because of the cancel button.
	 * The internal option values are equal to the displayed ones when the user presses the ok button.
	 * 
	 * @return true, if cbLS is selected, false otherwise
	 */
	public boolean isWeightedLocalSimilarityEnabled() {
		return cbWLS.isSelected();
	}
	
	/**
	 * Sets the selection of cbWLS to value of enabled.
	 * Be aware that the internal value may differ from the displayed value, because of the cancel button.
	 * The internal option values are equal to the displayed ones when the user presses the ok button.
	 * 
	 * @param enabled specifying whether the combo box should be enabled or not.
	 */
	public void enableWeightedLocalSimilarity(boolean enabled) {
		cbWLS.setSelected(enabled);
	}
		
	/**
	 * For printing the options to XML format. Adds the information of these options to element.
	 * @param element the JDOM element node
	 */
	public void toXML(Element element) {
		element.setAttribute(XML_ATT_EXPLANATIONS, Boolean.toString(isExplanationsEnabled()));
		element.setAttribute(XML_ATT_CONCEPT_EXPLANATIONS, Boolean.toString(isConceptExplanationsEnabled()));
		element.setAttribute(XML_ATT_LS, Boolean.toString(isLocalSimilarityEnabled()));
		element.setAttribute(XML_ATT_WLS, Boolean.toString(isWeightedLocalSimilarityEnabled()));
	}
	
	/**
	 * For loading the values for this options out of an XML file.
	 * 
	 * @param element specifying the the DOM element containing the option values 
	 * @throws Exception in case an error occurs while parsing the XML information given by element
	 */
	public void load(Element element) throws Exception {
		if (element.getAttribute(XML_ATT_EXPLANATIONS) != null) {
			isExplanationEnabled = element.getAttribute(XML_ATT_EXPLANATIONS).getBooleanValue();
			enableExplanations(element.getAttribute(XML_ATT_EXPLANATIONS).getBooleanValue());
			if(!isExplanationEnabled) {
				cbConceptExplanation.setEnabled(false);
				cbLS.setEnabled(false);
				cbWLS.setEnabled(false);
			}
			// run it in an extra thread
			// if the explanation manager has to initialize, all the work is done by the java GUI thread
			// which means that the GUI freezes. The following construction avoids that.
			Thread initThread = new Thread() {
				@Override
				public void run() {
					ExplanationManager expManager = ExplanationManager.getInstance();
					expManager.setEnabled(isExplanationsEnabled());
				}
			};
			initThread.start();
		}
		if (isExplanationsEnabled()) {
			
			if (element.getAttribute(XML_ATT_CONCEPT_EXPLANATIONS) != null) {
				isConceptExplanations = element.getAttribute(XML_ATT_CONCEPT_EXPLANATIONS).getBooleanValue();
				enableConceptExplanations(element.getAttribute(XML_ATT_CONCEPT_EXPLANATIONS).getBooleanValue());
				ExplanationManager.getInstance().setEnabled_conceptualExp(isConceptExplanations);
				if (RetrievalWidget.getInstance() != null) {
					RetrievalWidget.getInstance().setConceptExplanationsVisible(isConceptExplanations);
				}
			}
			if (element.getAttribute(XML_ATT_LS) != null) {
				isLS = element.getAttribute(XML_ATT_LS).getBooleanValue();
				enableLocalSimilarity(element.getAttribute(XML_ATT_LS).getBooleanValue());
				ExplanationManager.getInstance().setEnabled_retrieval_showLocalSimilarities(isLS);
			}
			if (element.getAttribute(XML_ATT_WLS) != null) {
				isWLS = element.getAttribute(XML_ATT_WLS).getBooleanValue();
				enableWeightedLocalSimilarity(element.getAttribute(XML_ATT_WLS).getBooleanValue());
				ExplanationManager.getInstance().setEnabled_retrieval_showWeightedLocalSimilarities(isWLS);
			}
		} else {
			cbConceptExplanation.setEnabled(false);
			getCBHighlightSim().setEnabled(false);
			cbLS.setEnabled(false);
			cbWLS.setEnabled(false);
		}
		
	}

	/**
	 * Updates the internal values of the options according to the options specified 
	 * by the user input. Is called when user clicks the OK button.
	 * @param options not used in this context but specified by myCBR's OptionListener interface
	 */
	public void optionsChanged(ProjectOptions options) {
		
		if (isExplanationEnabled != cbExplanations.isSelected()) {
			isExplanationEnabled = cbExplanations.isSelected();
			// run it in an extra thread
			// if the explanation manager has to initialize, all the work is done by the java GUI thread
			// which means that the GUI freezes. The following construction avoids that.
			Thread initThread = new Thread() {
				@Override
				public void run() {
					ExplanationManager expManager = ExplanationManager.getInstance();
					expManager.setEnabled(isExplanationsEnabled());
				}
			};
			initThread.start();
		}

		if (isLocalSimilarityEnabled() != isLS) {
			isLS = isLocalSimilarityEnabled();
			ExplanationManager.getInstance().setEnabled_retrieval_showLocalSimilarities(isLS);
		}
		if(isWeightedLocalSimilarityEnabled() != isWLS) {
			isWLS = isWeightedLocalSimilarityEnabled();
			ExplanationManager.getInstance().setEnabled_retrieval_showWeightedLocalSimilarities(isWLS);
		}
		if(isConceptExplanationsEnabled() != isConceptExplanations) {
			isConceptExplanations = isConceptExplanationsEnabled();
			ExplanationManager.getInstance().setEnabled_conceptualExp(isConceptExplanations);
		}
	}
	
	/**
	 * to disable/enable other similarity options 
	 * depending on whether the explanations are enabled or not
	 */
	public void stateChanged(ChangeEvent e) {
		if(e.getSource().equals(cbExplanations)) {
			boolean enabled = false;
			if(cbExplanations.isSelected()) {
				enabled = true;
			} else {
				cbConceptExplanation.setSelected(false);
				cbLS.setEnabled(false);
				cbWLS.setEnabled(false);
				cbHighlightSim.setEnabled(false);
			}
			cbConceptExplanation.setEnabled(enabled);
			cbLS.setEnabled(enabled);
			cbWLS.setEnabled(enabled);
			cbHighlightSim.setEnabled(enabled);
		}
	}
	
}
