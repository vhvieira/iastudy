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
 * endOfLic */
package de.dfki.mycbr;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;

import org.jdom.Element;

import de.dfki.mycbr.model.vocabulary.SpecialValueHandler;
import de.dfki.mycbr.retrieval.Query;

public class ProjectOptions implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(ProjectOptions.class.getName());
	
	public static final String XML_ATT_CASES_TO_DISPLAY			= "CasesToDisplay"; //$NON-NLS-1$
	public static final String XML_ATT_IGNORE_UNDEFINED_QUERY	= "IgnoreUndefined_Query"; //$NON-NLS-1$
	public static final String XML_ATT_IGNORE_UNDEFINED_CASE	= "IgnoreUndefined_Case"; //$NON-NLS-1$
	public static final String XML_ATT_SHOW_FILTERS				= "showFilters"; //$NON-NLS-1$
	public static final String XML_ATT_SHOW_QUERY_WEIGHTS		= "showQueryWeights"; //$NON-NLS-1$
	public static final String XML_ATT_RETRIEVAL_ENGINE			= "retrievalEngine"; //$NON-NLS-1$

	private static final String[] WEIGHT_MODES = new String[]{Messages.getString("predefined_weights"), Messages.getString("query_weights"), Messages.getString("multiply")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	private ExplanationOptions explanationOptions;
	private int retrieval_casesToDisplay = 3;
	private HashSet<OptionsListener> _listeners = new HashSet<OptionsListener>();
	
	private static boolean hasChanged 	= false;

	private boolean ignoreUndefinedInRetrieval_Query 	= true;
	private boolean ignoreUndefinedInRetrieval_Case 	= true;
	private boolean showFilters 						= false;
	private int weightMode								= Query.WEIGHT_MODE_CLASS_ONLY;
	
	public interface OptionsListener extends Serializable {
		public void optionsChanged(ProjectOptions options);
	}
	
	public ProjectOptions() {
		explanationOptions = ExplanationOptions.getInstance();
		addOptionsListener(explanationOptions);
	}
	
	//
	// provided options
	//
	public int getCasesToDisplay() {
		return retrieval_casesToDisplay;
	}

	public void setCasesToDisplay(int retrieval_casesToDisplay) {
		setHasChanged();
		this.retrieval_casesToDisplay = retrieval_casesToDisplay;
	}

	public boolean isShowFilters() {
		return showFilters;
	}

	public void setShowFilters(boolean showFilters) {
		setHasChanged();
		this.showFilters = showFilters;
	}
	
	public int getWeightMode() {
		return weightMode;
	}

	public void setWeightMode(int weightMode) {
		setHasChanged();
		this.weightMode = weightMode;
	}

	//
	// technical stuff
	//
	public boolean hasChanged() {
		return hasChanged;
	}
	
	public static void setHasChanged() {
		hasChanged = true;
	}

	public void toXML(Element element) {
		element.setAttribute(XML_ATT_CASES_TO_DISPLAY, 		Integer.toString(retrieval_casesToDisplay));
		element.setAttribute(XML_ATT_IGNORE_UNDEFINED_QUERY,Boolean.toString(ignoreUndefinedInRetrieval_Query));
		element.setAttribute(XML_ATT_IGNORE_UNDEFINED_CASE, Boolean.toString(ignoreUndefinedInRetrieval_Case));
		element.setAttribute(XML_ATT_SHOW_FILTERS, 			Boolean.toString(showFilters));
		element.setAttribute(XML_ATT_SHOW_QUERY_WEIGHTS,	Integer.toString(weightMode));
		element.setAttribute(ExplanationOptions.XML_ATT_HIGHLIGHT_SIMILARITY, 	Boolean.toString(ExplanationOptions.getInstance().isHighlightSimilarity()));
		explanationOptions.toXML(element);

	}
	
	public void load(Element element) throws Exception {
		retrieval_casesToDisplay = element.getAttribute(XML_ATT_CASES_TO_DISPLAY).getIntValue();
		if (element.getAttribute(XML_ATT_IGNORE_UNDEFINED_QUERY) != null) {
			ignoreUndefinedInRetrieval_Query = element.getAttribute(XML_ATT_IGNORE_UNDEFINED_QUERY).getBooleanValue();
		}
		if (element.getAttribute(XML_ATT_IGNORE_UNDEFINED_CASE) != null) {
			ignoreUndefinedInRetrieval_Case = element.getAttribute(XML_ATT_IGNORE_UNDEFINED_CASE).getBooleanValue();
		}
		if (element.getAttribute(XML_ATT_SHOW_FILTERS) != null) {
			showFilters	= element.getAttribute(XML_ATT_SHOW_FILTERS).getBooleanValue();
		}
		if (element.getAttribute(XML_ATT_SHOW_QUERY_WEIGHTS) != null) {
			weightMode = element.getAttribute(XML_ATT_SHOW_QUERY_WEIGHTS).getIntValue();
		}
		if (element.getAttribute(ExplanationOptions.XML_ATT_HIGHLIGHT_SIMILARITY) != null) {
			ExplanationOptions.getInstance().setHighlightSimilarity(element.getAttribute(ExplanationOptions.XML_ATT_HIGHLIGHT_SIMILARITY).getBooleanValue());
		}

		explanationOptions.load(element);

		fireOptionsChanged();
	}
	
	public void fireOptionsChanged() {
		for (Iterator<OptionsListener> it=_listeners.iterator(); it.hasNext();) {
			OptionsListener listener = it.next();
			if (listener == null) {
				continue;
			}
			
			try {
				listener.optionsChanged(this);
			}
			catch (Throwable e) {
				log.info("Listener [" + listener.getClass().getName() + "] could not update options."); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
		}
	}
	public void addOptionsListener(OptionsListener listener) {
		_listeners.add(listener);
	}
	public void removeOptionsListener(OptionsListener listener) {
		_listeners.remove(listener);
	}

	public boolean isIgnoreUndefinedInRetrieval_Query() {
		return ignoreUndefinedInRetrieval_Query;
	}
	
	public void setIgnoreUndefinedInRetrieval_Query(boolean b) {
		ignoreUndefinedInRetrieval_Query = b;
	}

	public boolean isIgnoreUndefinedInRetrieval_Case() {
		return ignoreUndefinedInRetrieval_Case;
	}
	
	public void setIgnoreUndefinedInRetrieval_Case(boolean b) {
		ignoreUndefinedInRetrieval_Case = b;
	}
		
	private JPanel paGeneralTab;
	private JLabel laCasesToDisplay;
	private JSpinner spCasesToDisplay;
	private JPanel paIgnoreUndefined;
	private JCheckBox cbIgnoreUndefinedQuery;
	private JCheckBox cbIgnoreUndefinedCase;
	private JCheckBox cbShowFilters;
	private JComboBox cbWeightMode;
	private JButton buOK;
	private JButton buCancel;
	private JTabbedPane tabbedPane;
	private JDialog d;
	private JPanel buttonsPanel;
	private JPanel mainPanel;
	
	public void showOptions(Frame parentFrame) {

		if (d == null) {
			paGeneralTab = new JPanel(new GridBagLayout());
	
			laCasesToDisplay 	= new JLabel(Messages.getString("Amount_of_displayed_cases")); //$NON-NLS-1$
			spCasesToDisplay 	= new JSpinner(new SpinnerNumberModel(getCasesToDisplay(), 1, 10, 1));
			
			paIgnoreUndefined 	= new JPanel(new GridBagLayout());
			paIgnoreUndefined.setBorder(BorderFactory.createTitledBorder(String.format(Messages.getString("Ignore__in_retrieval"), SpecialValueHandler.SPECIAL_VALUE_UNDEFINED.toString()))); //$NON-NLS-1$
			
			cbIgnoreUndefinedQuery = new JCheckBox(Messages.getString("In_query"), isIgnoreUndefinedInRetrieval_Query()); //$NON-NLS-1$
			cbIgnoreUndefinedCase  = new JCheckBox(Messages.getString("In_case"), isIgnoreUndefinedInRetrieval_Case()); //$NON-NLS-1$
			paIgnoreUndefined.add(cbIgnoreUndefinedQuery, 	new GridBagConstraints(0,0, 1,1, 0d,0d, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0,5,0,5), 0,0));
			paIgnoreUndefined.add(cbIgnoreUndefinedCase, 	new GridBagConstraints(1,0, 1,1, 1d,0d, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0,5,0,5), 0,0));
			
			cbShowFilters 	= new JCheckBox(Messages.getString("Enable_filters_for_query_specification"), isShowFilters()); //$NON-NLS-1$
			cbWeightMode	= new JComboBox(WEIGHT_MODES);
	
			int wmIndex = 0;
			switch (weightMode) {
				case Query.WEIGHT_MODE_CLASS_ONLY: wmIndex = 0;  break;
				case Query.WEIGHT_MODE_QUERY_ONLY: wmIndex = 1;  break; 
				case Query.WEIGHT_MODE_MULTIPLY: wmIndex = 2;   break;
			}
			cbWeightMode.setSelectedIndex(wmIndex);
			
			buOK = new JButton(Messages.getString("Ok")); //$NON-NLS-1$
			buOK.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//
					// read new options
					//
					int newCasesToDisplay = Helper.parseInt(spCasesToDisplay.getValue().toString());
					if (newCasesToDisplay>0 && newCasesToDisplay <=10) setCasesToDisplay(newCasesToDisplay);

					int weightMode = 0;
					switch (cbWeightMode.getSelectedIndex()) {
						case 0: weightMode = Query.WEIGHT_MODE_CLASS_ONLY; break;
						case 1: weightMode = Query.WEIGHT_MODE_QUERY_ONLY; break;
						case 2: weightMode = Query.WEIGHT_MODE_MULTIPLY; break;
					}
					setWeightMode(weightMode);
					
					setIgnoreUndefinedInRetrieval_Query(cbIgnoreUndefinedQuery.isSelected());
					setIgnoreUndefinedInRetrieval_Case(cbIgnoreUndefinedCase.isSelected());
					setShowFilters(cbShowFilters.isSelected());
					ExplanationOptions.getInstance().setHighlightSimilarity(
							ExplanationOptions.getInstance().getCBHighlightSim().isSelected());
					fireOptionsChanged();
	
					((JDialog) ((JButton)e.getSource()).getTopLevelAncestor()).dispose();
				}
			});
			buCancel = new JButton(Messages.getString("Cancel")); //$NON-NLS-1$
			buCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					((JDialog) ((JButton)e.getSource()).getTopLevelAncestor()).dispose();
				}
			});
			
			paGeneralTab.add(laCasesToDisplay, 	new GridBagConstraints(0,1, 1,1, 1d,0d, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
			paGeneralTab.add(spCasesToDisplay, 	new GridBagConstraints(2,1, 2,1, 0d,0d, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
	
			paGeneralTab.add(paIgnoreUndefined, 	new GridBagConstraints(0,2, 4,1, 0d,0d, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
			paGeneralTab.add(cbShowFilters, 		new GridBagConstraints(0,3, 4,1, 0d,0d, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
			paGeneralTab.add(new JLabel(Messages.getString("Weight_mode")), 	new GridBagConstraints(0,5, 2,1, 0d,0d, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0)); //$NON-NLS-1$
			paGeneralTab.add(cbWeightMode, 	new GridBagConstraints(1,5, 3,1, 1d,0d, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
			
			tabbedPane = new JTabbedPane();
			tabbedPane.addTab(Messages.getString("General"), paGeneralTab); //$NON-NLS-1$
			//tabbedPane.addTab(Messages.getString("Nearest_neighbor"), nearestNeighborOptions); //$NON-NLS-1$
			tabbedPane.addTab(Messages.getString("Explanations"), explanationOptions); //$NON-NLS-1$
			
			buttonsPanel = new JPanel();
			mainPanel = new JPanel(new BorderLayout());
			mainPanel.add(tabbedPane, BorderLayout.CENTER);
			mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
			buttonsPanel.add(buOK, 				new GridBagConstraints(2,8, 1,1, 1d,1d, GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0));
			buttonsPanel.add(buCancel, 			new GridBagConstraints(3,8, 1,1, 0d,0d, GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0));
			
			d = new JDialog(parentFrame, Messages.getString("Project_options"), true); //$NON-NLS-1$
			d.getContentPane().add(mainPanel);
	
			d.setSize(500, 400);
			Helper.centerWindow(d);
			
		}
		explanationOptions.refresh();
		d.setVisible(true);
	}
	
	/**
	 * 
	 * @return the ExplanationOptions object
	 * @since myCBR v2.6.3
	 */
	public ExplanationOptions getExplanationOptions() {
		return explanationOptions;
	}
}
