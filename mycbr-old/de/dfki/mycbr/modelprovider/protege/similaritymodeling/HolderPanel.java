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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListCellRenderer;
import javax.swing.FocusManager;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.dfki.mycbr.CBRProject;
import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.explanation.ExplanationManager;
import de.dfki.mycbr.explanation.ui.Widget_Explanation_Num;
import de.dfki.mycbr.explanation.ui.Widget_Explanation_Symbols;
import de.dfki.mycbr.explanation.ui.Widget_PostIt;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.HasChangedListenerSMF;
import de.dfki.mycbr.model.similaritymeasures.SMFContainer;
import de.dfki.mycbr.model.similaritymeasures.SMFHolder;
import de.dfki.mycbr.model.similaritymeasures.SMFunctionFactory;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFContainerPanel;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.model.vocabulary.ModelSlot;

/**
 * @author myCBR Team
 *
 */
public class HolderPanel extends javax.swing.JPanel implements ListSelectionListener, HasChangedListenerSMF {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(HolderPanel.class.getName());
	
	JPanel paSMF = new JPanel();

	JPanel paHolder = new JPanel();

	//TitledBorder titledBorder1;

	JScrollPane scrollpane = new JScrollPane();

	JList liSMFnames = new JList();

	JButton buNew = new JButton();

	JButton buDelete = new JButton();

	JButton buActive = new JButton();
	
	JButton buDuplicate = new JButton();

	JButton buMultiple 		= new JButton(Messages.getString("Configure_set_similarity")); //$NON-NLS-1$
	JButton buInheritance	= new JButton(Messages.getString("Configure_inheritance_similarity")); //$NON-NLS-1$

	GridBagLayout gridBagLayout1 = new GridBagLayout();

	private ModelInstance modelInstance;

	/** the holder of the instance */
	private SMFHolder holder;
	
	private AbstractSMFunction currentSMFunction;

	GridBagLayout gridBagLayout2 = new GridBagLayout();

	BorderLayout borderLayout2 = new BorderLayout();

	GridBagLayout gridBagLayout3 = new GridBagLayout();

	private Widget_PostIt paPostIt;
	
	
	JLabel laActiveSmf = new JLabel();

	public ModelInstance getModelInstance() {
		return modelInstance;
	}

	public HolderPanel(ModelInstance inst) {
		modelInstance = inst;
		if (inst!= null) {
			this.holder =SMFContainer.getInstance().getSMFHolderForModelInstance(inst);
			try {
				jbInit();
				customInit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void customInit() {
		
		// commented out before 20.10.2008
		// inconsistency label
//		laInconsistent.setText("This smf is NOT consistent to the Model!");
//		laInconsistent.setVisible(true);
		
		// customize list
		
		updateList();
		liSMFnames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		liSMFnames.addListSelectionListener(this);
		liSMFnames.setAutoscrolls(true);
		liSMFnames.setCellRenderer(new DefaultListCellRenderer() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			Font boldFont;
			Font normalFont;
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasfocus) {
				JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasfocus);
				if (boldFont == null) {
					boldFont = label.getFont();
					normalFont = boldFont.deriveFont(Font.PLAIN);
				}
				
				AbstractSMFunction smf = holder.getActiveSMF();
				if (smf != null && smf.getSmfName().equals(value)) {
					label.setFont(boldFont);
				} else {
					label.setFont(normalFont);
				}
				return label;
			}
		});
		
		
		//
		// buttons
		//
		buMultiple.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JDialog dialog = Helper.createDialog((Window)getTopLevelAncestor(), buMultiple.getText(), true);
				
				MMFPanel mmfPanel = new MMFPanel(modelInstance);
				dialog.getContentPane().add(mmfPanel);
				
//				dialog.setTitle(buMultiple.getText());
				dialog.setSize(600,600);
				Helper.centerWindow(dialog);
				dialog.setVisible(true);
			}
		});
		
		buInheritance.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JDialog dialog = Helper.createDialog((Window)getTopLevelAncestor(), buInheritance.getText(), true);
				
				IMFPanel imfPanel = new IMFPanel((ModelCls) modelInstance);
				dialog.getContentPane().add(imfPanel);
				
//				dialog.setTitle(buInheritance.getText());
				dialog.setSize(600,600);
				Helper.centerWindow(dialog);
				dialog.setVisible(true);
			}
		});
		
		buDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				log.fine("pressed delete"); //$NON-NLS-1$
				String selection = (String) liSMFnames.getSelectedValue();
				if (selection == null) {
					return;
				}
				
				int result = JOptionPane.showConfirmDialog(((JButton) e.getSource()).getTopLevelAncestor(), String.format(Messages.getString("Delete_similarity_measure"),selection), Messages.getString("Are_you_sure"), JOptionPane.YES_NO_OPTION, //$NON-NLS-1$ //$NON-NLS-2$
						JOptionPane.WARNING_MESSAGE);
				if (JOptionPane.YES_OPTION == result) {
					// delete selection
					currentSMFunction=null;
					holder.deleteSMF(selection);
					updateList();
					selectSMF(holder.getActiveSMF(), true);
				}
			}
		});

		buNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				log.fine("pressed NEW"); //$NON-NLS-1$

				AbstractSMFunction lastSMF = currentSMFunction;
				if (!closeCurrentSMFunction()) {
					return;
				}

				String newSmfName = JOptionPane.showInputDialog(getTopLevelAncestor(), Messages.getString("Enter_name_for_similarity_function"), AbstractSMFunction.DEFAULT_SMF_NAME); //$NON-NLS-1$
				if (newSmfName == null) {
					selectSMF(lastSMF, false);
					return;
				}
				newSmfName = newSmfName.trim();

				if (!Helper.checkSMFName(newSmfName) || holder.containsKey(newSmfName)) {
					JOptionPane.showMessageDialog(getTopLevelAncestor(), Messages.getString("Invalid_name")); //$NON-NLS-1$
					selectSMF(lastSMF, false);
					return;
				}
				
				// create new smfunction object
				AbstractSMFunction smf = null;
				try {
					smf = CBRProject.getInstance().newSMF(modelInstance, newSmfName);
					updateList();
				} catch (Exception ex) {
					if (ex.getCause() != null) {
						log.log(Level.INFO, "could not instantiate new smfunction.", ex); //$NON-NLS-1$
						JOptionPane.showMessageDialog(getTopLevelAncestor(), ex.getCause().getMessage(), Messages.getString("Cannot_instantiate_smf"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
					} else {
						log.log(Level.SEVERE, "", ex); //$NON-NLS-1$
						JOptionPane.showMessageDialog(getTopLevelAncestor(), Messages.getString("Error_occured_read_log_for_details"), Messages.getString("Cannot_instantiate_smf"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				selectSMF(smf, true);
			}
		});

		buDuplicate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (currentSMFunction == null) {
					return;
				}
				log.fine("pressed DUPLICATE"); //$NON-NLS-1$
				AbstractSMFunction lastSMF = currentSMFunction;
				String newSmfName_Proposal = currentSMFunction.getSmfName() + "_duplicate"; //$NON-NLS-1$
				if (!closeCurrentSMFunction()) {
					return;
				}

				String newSmfName = JOptionPane.showInputDialog(getTopLevelAncestor(), Messages.getString("Enter_name_for_similarity_function"), newSmfName_Proposal); //$NON-NLS-1$
				if (newSmfName == null) {
					selectSMF(lastSMF, false);
					return;
				}
				newSmfName = newSmfName.trim();

				if (!Helper.checkSMFName(newSmfName) || holder.containsKey(newSmfName)) {
					JOptionPane.showMessageDialog(getTopLevelAncestor(), Messages.getString("Invalid_name")); //$NON-NLS-1$
					selectSMF(lastSMF, false);
					return;
				}

				// create new smfunction object
				AbstractSMFunction smf = null;

				smf = lastSMF.copy();
				smf.setSmfName(newSmfName);
				holder.confirmSMFunction(smf);
				updateList();

				selectSMF(smf, true);
			}
		});

		buActive.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				log.fine("pressed ACTIVE"); //$NON-NLS-1$
				if (currentSMFunction == null) {
					return;
				}
				holder.setActiveSMF(currentSMFunction);
				updateHolderButtons();
				repaint();
			}
		});
		
		// initialize selection
		AbstractSMFunction smf = holder.getActiveSMF();
		selectSMF(smf, false);
	}

	/**
	 * closes the current smfunction editing. In case that changes have been
	 * made user will be prompted.
	 *
	 * @return true if the smfunction has been closed, false otherwise
	 */
	protected boolean closeCurrentSMFunction() {
		// close now
		selectSMF(null, false);
		return true;
	}
	
	
	protected void updateList() {
		Vector<String> smfNames = new Vector<String>(holder.keySet());
		Collections.sort(smfNames);
		liSMFnames.setListData(smfNames);
	}

	/**
	 * Protege and myCBR share common data. Because of redundancies
	 * inconsistencies may occur. This method is used to check for this
	 * inconsistency concerning the Holder data.
	 */
	public void checkConsistency() {
		// get parent frame
		Frame frame = (Frame) getTopLevelAncestor();
		if (holder != null) {
			if (holder.checkConsistency(frame)) {
				// maybe, type of slot has changed. EditorSMFunction objects have been transformed (created new).
				// reload current SMFunction
				if (currentSMFunction != null) {
					AbstractSMFunction smfNow = holder.getCertainSMFunctionFromHolder(currentSMFunction.getSmfName());
					selectSMF(smfNow, false);
				}
			}
	
			if (holder.size() != liSMFnames.getModel().getSize()) {
				updateList();
				if (currentSMFunction==null || holder.getCertainSMFunctionFromHolder(currentSMFunction.getSmfName())==null) {
					 selectSMF(holder.getActiveSMF(), true);
				}
			}
			
			// expression for visibility of multimeasure button and inheritance measure button.
			buMultiple.setVisible(currentSMFunction!=null && currentSMFunction.getModelInstance() instanceof ModelSlot && ((ModelSlot)currentSMFunction.getModelInstance()).isMultiple());
			
			buInheritance.setVisible(currentSMFunction!=null && currentSMFunction.getModelInstance() instanceof ModelCls && Helper.hasInheritanceStructure((ModelCls)currentSMFunction.getModelInstance()));

			if (currentSMFunction != null) {
				if (modelInstance instanceof ModelSlot) {
					boolean equal = Helper.checkValueTypeSlotAndSMF(currentSMFunction, (ModelSlot) modelInstance);
					if (!equal) {
						AbstractSMFunction smf = holder.getActiveSMF();
						selectSMF(smf, false);
						return;
					}
				}
				currentSMFunction.checkConsistency(frame, true);
			}
		}
	}

	/**
	 Refresh GUI data.
	 Check for consistent representation of the data.
	 */
	public void refresh() {
		revalidate();
		repaint();
		updateHolderButtons();
	}

	private void updateHolderButtons() {
		log.fine("update Holder Buttons"); //$NON-NLS-1$
		if (currentSMFunction == null) {
			buDelete.setEnabled(false);
			buActive.setEnabled(false);
			buNew.setEnabled(true);
			buDuplicate.setEnabled(false);
		} else {
			buDelete.setEnabled(true);
			if (holder.getActiveSMF() == null) {
				buActive.setEnabled(true);
			} else {
				buActive.setEnabled(!holder.getActiveSMF().getSmfName().equals(currentSMFunction.getSmfName()));
			}
			buNew.setEnabled(true);
			buDuplicate.setEnabled(true);
		}
	}

	private void jbInit() throws Exception {
		//titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)), "Available functions");
		this.setLayout(gridBagLayout3);
		paSMF.setLayout(borderLayout2);
		//paHolder.setBorder(titledBorder1);
		paHolder.setLayout(gridBagLayout1);
//		Dimension buttonSize = new Dimension(80, 23); 
		Dimension buttonSize = new Dimension(90, 23); 
		buNew.setMaximumSize(buttonSize);
		buNew.setMinimumSize(buttonSize);
		buNew.setOpaque(true);
		buNew.setPreferredSize(buttonSize);
		buNew.setText(Messages.getString("New")); //$NON-NLS-1$
		buDelete.setMaximumSize(buttonSize);
		buDelete.setMinimumSize(buttonSize);
		buDelete.setPreferredSize(buttonSize);
		buDelete.setText(Messages.getString("Delete")); //$NON-NLS-1$
		buActive.setPreferredSize(buttonSize);
		buActive.setMinimumSize(buttonSize);
		buActive.setMaximumSize(buttonSize);
		buActive.setText(Messages.getString("Active")); //$NON-NLS-1$
		buDuplicate.setPreferredSize(buttonSize);
		buDuplicate.setMinimumSize(buttonSize);
		buDuplicate.setMaximumSize(buttonSize);
		buDuplicate.setText(Messages.getString("Duplicate")); //$NON-NLS-1$
		scrollpane.setMinimumSize(new Dimension(250, 40));
		scrollpane.setPreferredSize(new Dimension(250, 40));
		this.add(paSMF, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 396, 96));
		this.add(new JLabel(Messages.getString("Available_functions")), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0)); //$NON-NLS-1$
		this.add(paHolder, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(20, 0, 0, 0), 0, 0));
		this.add(buMultiple, 	new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(buInheritance, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

		// add panel for special buttons (csv-import, export of smfs)
//		this.add(getSpecialButtonsPanel(), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		this.add(laActiveSmf, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

		paHolder.add(scrollpane, new GridBagConstraints(0, 0, 1, 2, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		scrollpane.getViewport().add(liSMFnames, null);
		paHolder.add(buNew, new GridBagConstraints(1, 0, 1, 1, 0.0, 1.0, GridBagConstraints.SOUTH, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		paHolder.add(buDelete, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTH, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		paHolder.add(buActive, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTH, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		paHolder.add(buDuplicate, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTH, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
	}

	/**
	 * select an editorSMFunction object here.
	 * If smf is null nothing will be displayed.
	 * In here, a copy of the original smfunction will be produced and
	 * further used for the currentSMFunction.
	 * Its corresponding SMFPanel will be showed.
	 * @param smf EditorSMFunction object to be edited.
	 * @param checkConsistency boolean set true if you want to perform a consistency check after new selection. 
	 */
	private void selectSMF(AbstractSMFunction smf, boolean checkConsistency) {
		FocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
		
		String laTxt = (holder.getActiveSMF() == null ? Messages.getString("HolderPanel.No_similarity_measure_defined") : String.format(Messages.getString("HolderPanel.Active_similarity_measure"), holder.getActiveSMF().getSmfName())); //$NON-NLS-1$ //$NON-NLS-2$
		laActiveSmf.setText(laTxt);
		
		currentSMFunction = smf;
		if (smf == null) {
			log.fine("selected smfunction is NULL"); //$NON-NLS-1$
			paSMF.removeAll();
			liSMFnames.clearSelection();
		} else {
			log.fine("copy smfunction object and initialize panel"); //$NON-NLS-1$

			currentSMFunction.removeHasChangedListener(this);
			currentSMFunction.addHasChangedListener(this, false);
			paSMF.removeAll();


			// show the panel
			SMFPanel panel = null;
			String[] classNames = SMFunctionFactory.getSMFClassesForInstance(modelInstance);
			if (classNames.length <= 1) {
				// only one similarity measure editor available
				panel = currentSMFunction.getEditorPanel();
			} else {
				// several similarity measure editors available
				panel = new SMFContainerPanel(currentSMFunction);
			}
			paSMF.add(panel, BorderLayout.CENTER);
			
			
			//
			// add explanation widget
			//
			ValueType vt = currentSMFunction.getValueType();
			if (vt==ValueType.INTEGER || vt==ValueType.FLOAT) {
				paPostIt = new Widget_PostIt(new Widget_Explanation_Num(smf));
				paSMF.add(paPostIt, BorderLayout.EAST);
			}
			else if (vt == ValueType.SYMBOL) {
				paPostIt = new Widget_PostIt(new Widget_Explanation_Symbols(smf));
				paSMF.add(paPostIt, BorderLayout.EAST);
			}
			checkExplanationVisibility();

			// update list
			liSMFnames.setSelectedValue(smf.getSmfName(), false);
			int tmpIndex = liSMFnames.getSelectedIndex();
			liSMFnames.scrollRectToVisible(liSMFnames.getCellBounds(tmpIndex,tmpIndex));

			if (checkConsistency) {
				checkConsistency();
			}
		}
		refresh();
	}


	public void valueChanged(ListSelectionEvent e) {
		log.fine("List value changed : [" + liSMFnames.getSelectedValue() + "]"); //$NON-NLS-1$ //$NON-NLS-2$

		AbstractSMFunction smfunction = (AbstractSMFunction)holder.get(liSMFnames.getSelectedValue());
		if ((smfunction==null && currentSMFunction==null) || (smfunction!=null && currentSMFunction!=null && smfunction.getSmfName().equals(currentSMFunction.getSmfName()))) {
			return;
		}
		if (closeCurrentSMFunction()) {
			log.fine("old smf closed"); //$NON-NLS-1$
			selectSMF(smfunction, true);
		} else {
			log.fine("old smf has not been closed"); //$NON-NLS-1$
			liSMFnames.setSelectedValue(currentSMFunction.getSmfName(), true);
		}
	}

	public SMFHolder getHolder() {
		return holder;
	}

	/**
	 * Called when smf has changed.
	 * Implemented from Interface HasChangedListener.
	 */
	public void smfHasChanged(boolean hasChanged) {
		log.fine("smf has changed"); //$NON-NLS-1$
		if (hasChanged) {
			holder.setHasChanged(true);
		}
	}

	protected AbstractSMFunction getCurrentSMFunction() {
		return currentSMFunction;
	}
	
	public String toString() {
		return String.format(Messages.getString("Holder_panel_for"), currentSMFunction); //$NON-NLS-1$
	}

	public void paint(Graphics g) {
		checkExplanationVisibility();
		super.paint(g);
	}

	public void checkExplanationVisibility() {
		if (paPostIt != null) {
			paPostIt.setVisible(ExplanationManager.getInstance().isEnabled());
		}
	}

}
