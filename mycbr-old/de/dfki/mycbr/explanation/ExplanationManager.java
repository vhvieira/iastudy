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
package de.dfki.mycbr.explanation;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import de.dfki.mycbr.CBRProject;
import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.casebase.CaseInstance;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.model.vocabulary.SpecialValueHandler;

public class ExplanationManager implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger( ExplanationManager.class.getName());

	public static boolean ENABLED_ON_INIT = false;
	
	public static final int MODE_SORTING_BY_LOCAL_SIM = 1;
	public static final int MODE_SORTATION_BY_WEIGHTED_LOCAL_SIM = 2;
	
	private boolean enabled = ENABLED_ON_INIT;
	private boolean isInitialized = false;
	private boolean breakInit = false;
	
	private HashMap<ModelSlot, SlotStatistic> slotToMap = new HashMap<ModelSlot, SlotStatistic>();
	private HashMap<ModelCls, Long> clsToMap = new HashMap<ModelCls, Long>();

	private int casebaseSize;

	private JProgressBar progressbar;

	private JPanel panel;
	private static ExplanationManager instance;
	
	/**
	 * A set of ActionsListeners, which were informed when the "enabled" attribute is changed
	 */
	private HashSet<ActionListener> enableExplanationListeners = new HashSet<ActionListener>();
	
	public static final ExplanationManager getInstance() {
		return instance;
	}

	public static ExplanationManager initInstance() {
		instance = new ExplanationManager();	
		return instance;
	}
	
	public static void resetInstance() {
		instance = null;
	}
	
	private ExplanationManager() {}

	/**
	 * Initializes the explanation manager. It also shows a frame containing a progress bar.
	 * 
	 * @return true if no error occurred (for what reason ever) and process was not canceled.
	 */
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private boolean init() {
		log.info("initialize explanation manager..."); //$NON-NLS-1$
		
		Collection< ModelCls > allModelCls = CBRProject.getInstance().getAllModelCls();
		SpecialValueHandler specialValueHandler = SpecialValueHandler.getInstance();
		
		//
		// init progress dialog
		//
		JDialog d = new JDialog();
		d.setTitle(Messages.getString("Init_Explanation_Manager")); //$NON-NLS-1$
		panel = new JPanel(new GridBagLayout());
		JLabel label = new JLabel(Messages.getString("Init_Explanation_Manager")); //$NON-NLS-1$
		label.setHorizontalAlignment(SwingConstants.CENTER);
		progressbar = new JProgressBar();

		JButton buCancel = new JButton(Messages.getString("Cancel")); //$NON-NLS-1$
		buCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				breakInit = true;
			}
		});
		d.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				breakInit = true;
			}
		});
		
		panel.add(label, 		new GridBagConstraints(0,0, 2,1, 1d,1d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0, 0));
		panel.add(progressbar, 	new GridBagConstraints(0,1, 2,1, 1d,0d, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0), 0, 0));
		panel.add(buCancel, 	new GridBagConstraints(1,2, 1,1, 0d,0d, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0, 0));
		d.setSize(400,150);
		Helper.centerWindow(d);
		d.getContentPane().add(panel);
		d.setVisible(true);
		panel.revalidate();
		panel.paintImmediately(panel.getBounds());
		
		// calculate max and init progress counter
		long max = 0;
		for (Iterator<ModelCls> itMax = allModelCls.iterator(); itMax.hasNext();) {
			ModelCls cls = itMax.next();
			long instCnt = cls.getDirectCaseInstances().size();
			long slotCnt = cls.listSlots().size();
			max += instCnt * slotCnt;
		}
		long progressCounter = 0;
		initProgressBar(max);
		long startMillis = System.currentTimeMillis();
		
		// get distribution
		for (Iterator<ModelCls> itCls=allModelCls.iterator(); itCls.hasNext();) {
			if (breakInit) {
				break;
			}
			
			ModelCls cls = itCls.next();
			casebaseSize = cls.getDirectCaseInstances().size();
			long instCnt = cls.getDirectCaseInstances().size();

			for (Iterator<ModelSlot> itSlots = cls.listSlots().iterator(); itSlots.hasNext();) {
				if (breakInit) break;
				
				ModelSlot slot = itSlots.next();
				boolean isMultiple = slot.isMultiple();
				label.setText(String.format(Messages.getString("Slot_Label"), cls.getName(), slot.getName())); //$NON-NLS-1$
				
				HashMap<Object, Integer> valueToAmount = new HashMap<Object, Integer>();
				long maxAppearance = -1;
				long slotUsage = 0;
				
				if (slot.getValueType() == ValueType.SYMBOL) {
					// get statistics for valuetype symbol
					
					Collection<Object> allowedValues = slot.getAllowedValues();
					int valCnt = allowedValues.size();
					for (Iterator<Object> itSymbols=allowedValues.iterator(); itSymbols.hasNext();) {
						progressCounter += (instCnt/valCnt);
						updateProgressbar(progressCounter);

						String symbol = (String) itSymbols.next();
						int counter = 0;
						for (Iterator<CaseInstance> itCases = cls.getDirectCaseInstances().iterator(); itCases.hasNext();) {
							CaseInstance c = (CaseInstance) itCases.next();
							Object value = c.getSlotValue(slot);
							
							if (isMultiple) {
								if (value != null && !specialValueHandler.isSpecialValue(value)) {
									for (Iterator< Object > itCol = ((Collection) value).iterator(); itCol.hasNext();) {
										if (symbol.equals(itCol.next())) {
											counter++;
										}
									}
								}
							} else {
								if (symbol.equals(value)) {
									counter++;
								}
							}
						}
						
						if (maxAppearance < counter) {
							maxAppearance = counter;
						}
						valueToAmount.put(symbol, new Integer(counter));
						slotUsage += counter;
					}
				}
				else if (slot.getValueType() == ValueType.INTEGER || slot.getValueType() == ValueType.FLOAT) {
					int counter = 0;
					for (Iterator itCases = cls.getDirectCaseInstances().iterator(); itCases.hasNext();) {
						progressCounter++;
						updateProgressbar(progressCounter);
						
						CaseInstance c = (CaseInstance) itCases.next();
						Object value = c.getSlotValue(slot);
						
						Integer am = (Integer) valueToAmount.get(value);
						am = (am == null ? new Integer(1) : new Integer(am.intValue()+1));
						
						if (value != null && !specialValueHandler.isSpecialValue(value)) {
							valueToAmount.put(value, am);
							counter++;
						}
					}
					slotUsage+=counter;
				}
				else if (slot.getValueType() == ValueType.STRING) {
					int counter = 0;
					for (Iterator itCases = cls.getDirectCaseInstances().iterator(); itCases.hasNext();) {
						progressCounter++;
						updateProgressbar(progressCounter);
						
						CaseInstance c = (CaseInstance) itCases.next();
						Object value = c.getSlotValue(slot);
						
						if (value != null && !specialValueHandler.isSpecialValue(value)) {
							counter++;
						}
					}
					slotUsage+=counter;
				}
				else if (slot.getValueType() == ValueType.INSTANCE) {
					int counter = 0;
					for (Iterator itCases = cls.getDirectCaseInstances().iterator(); itCases.hasNext();) {
						progressCounter++;
						updateProgressbar(progressCounter);
						
						CaseInstance c = (CaseInstance) itCases.next();
						Object value = c.getSlotValue(slot);
						
						if (value != null && !specialValueHandler.isSpecialValue(value)) {
							counter++;
						}
					}
					slotUsage += counter;
				} else {
					progressCounter += instCnt;
					updateProgressbar(progressCounter);
				}
				
				SlotStatistic slotStat = new SlotStatistic(valueToAmount, maxAppearance, slotUsage);
				slotToMap.put(slot, slotStat);
			}
			
			clsToMap.put(cls, instCnt);
		}
		
		if (breakInit) {
			breakInit = false;
			log.fine("cancel initialization of explanation manager"); //$NON-NLS-1$
			d.dispose();
			return false;
		}
		
		updateProgressbar(max);
		isInitialized=true;
		long finishMillis = System.currentTimeMillis();

		log.info("explanation manager intialized successfully. Elapsed time [" + (finishMillis-startMillis) + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		
		d.dispose();
		return true;
	}
	
	long max;
	int lastValue = -1;

	private boolean enabled_symbol_showOccurrences 	= true;
	private boolean enabled_symbol_showCBasQuery 	= false;
	
	private boolean enabled_retrieval_showLocalSimilarities 		= true;
	private boolean enabled_retrieval_showWeightedLocalSimilarities = false;
	
	private boolean enabled_conceptualExp 			= true;
	
	private int sortingMode = MODE_SORTING_BY_LOCAL_SIM;

	
	private void initProgressBar(long max) {
		this.max = max;
	}

	private void updateProgressbar(long progressCounter) {
		if (max != 0) {
			int value = (int) ((progressCounter*100)/max);
			if (value != lastValue) {
				lastValue = value;
				progressbar.setValue(value);
				panel.paintImmediately(panel.getBounds());
			} 			
		}
	}

	public Long getClsStatistic(ModelCls cls) {
		if (!isInitialized) {
			init();
		}
		return clsToMap.get(cls);
	}
		
	public SlotStatistic getSlotStatistic(ModelSlot slot) {
		if (!isInitialized) {
			init();
		}
		return (SlotStatistic) slotToMap.get(slot);
	}
	
	public int getCaseBaseSize() {
		if (!isInitialized) {
			init();
		}
		return casebaseSize;
	}

	public void setEnabled(boolean enabled) {
		log.fine("Explanation support is [" + (enabled ? "enabled" : "disabled") + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		boolean successful = true;
		if (enabled && !isInitialized) {
			successful = init();
		}
		if (successful) {
			this.enabled = enabled;
			// sends an Action to all registered listeners
			ActionEvent setEnabledEvent = new ActionEvent(
					enabled, ActionEvent.ACTION_PERFORMED, "ExplanationEnabledEvent");
			for (ActionListener al : enableExplanationListeners) {
				al.actionPerformed(setEnabledEvent);
			}
		}
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public void reInit() {
		slotToMap.clear();
		clsToMap.clear();
		init();
	}

	public void setEnabled_Symbol_ShowOccurrences(boolean enabled) {
		this.enabled_symbol_showOccurrences = enabled;
	}

	public void setEnabled_Symbol_ShowCBasQuery(boolean enabled) {
		this.enabled_symbol_showCBasQuery = enabled;
	}

	public boolean isEnabled_symbol_showCBasQuery() {
		return enabled_symbol_showCBasQuery;
	}

	public boolean isEnabled_symbol_showOccurrences() {
		return enabled_symbol_showOccurrences;
	}

	public boolean isEnabled_retrieval_showLocalSimilarities() {
		return enabled_retrieval_showLocalSimilarities;
	}

	public void setEnabled_retrieval_showLocalSimilarities(boolean enabled_retrieval_showLocalSimilarities) {
		this.enabled_retrieval_showLocalSimilarities = enabled_retrieval_showLocalSimilarities;
	}

	public boolean isEnabled_retrieval_showWeightedLocalSimilarities() {
		return enabled_retrieval_showWeightedLocalSimilarities;
	}

	public void setEnabled_retrieval_showWeightedLocalSimilarities(boolean enabled_retrieval_showWeightedLocalSimilarities) {
		this.enabled_retrieval_showWeightedLocalSimilarities = enabled_retrieval_showWeightedLocalSimilarities;
	}

	public int getSortationMode() {
		return sortingMode;
	}

	public void setSortingMode(int sortingMode) {
		this.sortingMode = sortingMode;
	}

	public boolean isEnabled_conceptualExp() {
		return enabled_conceptualExp && ConceptExplanationProvider.getInstance().isProvided();
	}

	public void setEnabled_conceptualExp(boolean enabled_conceptualExp) {
		this.enabled_conceptualExp = enabled_conceptualExp;
	}
	
	
	/**
	 * Adds an ActionListener for being informed about changes of the 
	 * Explanation Managers "enabled" attribute
	 * @param al the ActionListener
	 */
	public void addEnabledExplanationListener(ActionListener al) {
		this.enableExplanationListeners.add(al);
	}
	
}
