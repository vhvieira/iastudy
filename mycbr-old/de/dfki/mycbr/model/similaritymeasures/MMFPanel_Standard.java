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
package de.dfki.mycbr.model.similaritymeasures;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel;

public class MMFPanel_Standard extends SMFPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MMF_Standard mmf;
	private JPanel paMultipleSim = null;

	private JPanel paSymmetry = null;
	private JPanel paOneOrMulti = null;
	private JPanel paSingleSim = null;
	private JPanel paMultiSelection = null;
	private JPanel paReuse = null;
	private JPanel paTotal = null;
	private JRadioButton rbAverage = null;
	private JRadioButton rbMaximum = null;
	private JRadioButton rbMinimum = null;
	private JRadioButton rbFindBestByQuery = null;
	private JRadioButton rbFindBestByCase = null;
	private JRadioButton rbFindBestByQueryAndCaseAvg = null;
	
	private JRadioButton rbQreuse = null;
	private JRadioButton rbQdontReuse = null;
	private JRadioButton rbBestMatch = null;
	private JRadioButton rbWorstMatch = null;
	private JRadioButton rbMultipleSims = null;
	private JRadioButton rbOneSim = null;
	
	// button groups
	private ButtonGroup buttonGroupOneOrMulti 		= new ButtonGroup();  //  @jve:decl-index=0:
	private ButtonGroup buttonGroupMultiSelection 	= new ButtonGroup();  //  @jve:decl-index=0:
	private ButtonGroup buttonGroupSingleSim 		= new ButtonGroup();  //  @jve:decl-index=0:
	private ButtonGroup buttonGroupReuseQ 			= new ButtonGroup();  
	private ButtonGroup buttonGroupNoMatch 			= new ButtonGroup();  //  @jve:decl-index=0:
	private ButtonGroup buttonGroupTotal 			= new ButtonGroup();  //  @jve:decl-index=0:
	private JPanel paNoMatch = null;
	private JRadioButton rbSetZero = null;
	private JLabel laNoMatch = null;
	private JRadioButton rbIgnore = null;

	
	
	public MMFPanel_Standard(MMF_Standard mmf) {
		super(mmf);
		this.mmf = mmf;
		initialize();
		customInit();
	}

	private void customInit() {
		// setup button groups
		buttonGroupOneOrMulti.add(rbOneSim);
		buttonGroupOneOrMulti.add(rbMultipleSims);

		buttonGroupSingleSim.add(rbBestMatch);
		buttonGroupSingleSim.add(rbWorstMatch);

		buttonGroupMultiSelection.add(rbFindBestByQuery);
		buttonGroupMultiSelection.add(rbFindBestByCase);
		buttonGroupMultiSelection.add(rbFindBestByQueryAndCaseAvg);
		

		buttonGroupReuseQ.add(rbQreuse);
		buttonGroupReuseQ.add(rbQdontReuse);

		buttonGroupNoMatch.add(rbSetZero);
		buttonGroupNoMatch.add(rbIgnore);
		
		buttonGroupTotal.add(rbAverage);
		buttonGroupTotal.add(rbMaximum);
		buttonGroupTotal.add(rbMinimum);

		
		
		rbBestMatch.setName(Integer.toString(MMF_Standard.FUNCTION_SINGLESIM_BEST));
		rbWorstMatch.setName(Integer.toString(MMF_Standard.FUNCTION_SINGLESIM_WORST));
		
		rbQdontReuse.setName(Integer.toString(MMF_Standard.FUNCTION_REUSE_DONTREUSE));
		rbQreuse.setName(Integer.toString(MMF_Standard.FUNCTION_REUSE_REUSE));
		
		rbIgnore.setName(Integer.toString(MMF_Standard.FUNCTION_NOMATCH_IGNORE));
		rbSetZero.setName(Integer.toString(MMF_Standard.FUNCTION_NOMATCH_SETZERO));
		
		rbFindBestByCase.setName(Integer.toString(MMF_Standard.FUNCTION_MULTISELECTION_BYCASE));
		rbFindBestByQuery.setName(Integer.toString(MMF_Standard.FUNCTION_MULTISELECTION_BYQUERY));
		rbFindBestByQueryAndCaseAvg.setName(Integer.toString(MMF_Standard.FUNCTION_MULTISELECTION_BYQUERYANDCASE));
		
		rbMultipleSims.setName(Integer.toString(MMF_Standard.FUNCTION_ONEORMULTI_MULTI));
		rbOneSim.setName(Integer.toString(MMF_Standard.FUNCTION_ONEORMULTI_ONE));
		
		rbAverage.setName(Integer.toString(MMF_Standard.FUNCTION_TOTAL_AVERAGE));
		rbMaximum.setName(Integer.toString(MMF_Standard.FUNCTION_TOTAL_MAXIMUM));
		rbMinimum.setName(Integer.toString(MMF_Standard.FUNCTION_TOTAL_MINIMUM));

		
		
		rbAverage.addActionListener(this);
		rbBestMatch.addActionListener(this);
		rbSetZero.addActionListener(this);
		rbIgnore.addActionListener(this);
		rbFindBestByCase.addActionListener(this);
		rbFindBestByQuery.addActionListener(this);
		rbFindBestByQueryAndCaseAvg.addActionListener(this);
		rbMaximum.addActionListener(this);
		rbMinimum.addActionListener(this);
		rbMultipleSims.addActionListener(this);
		rbOneSim.addActionListener(this);
		rbQdontReuse.addActionListener(this);
		rbQreuse.addActionListener(this);
		rbWorstMatch.addActionListener(this);
		
		updateButtons();
	}

	private JRadioButton getRadioButton(int id) {
		switch (id) {
			case MMF_Standard.FUNCTION_MULTISELECTION_BYCASE: 	return rbFindBestByCase;
			case MMF_Standard.FUNCTION_MULTISELECTION_BYQUERY: 	return rbFindBestByQuery;
			case MMF_Standard.FUNCTION_MULTISELECTION_BYQUERYANDCASE: 		return rbFindBestByQueryAndCaseAvg;

			case MMF_Standard.FUNCTION_ONEORMULTI_ONE: 			return rbOneSim;
			case MMF_Standard.FUNCTION_ONEORMULTI_MULTI: 		return rbMultipleSims;

			case MMF_Standard.FUNCTION_SINGLESIM_BEST: 			return rbBestMatch;
			case MMF_Standard.FUNCTION_SINGLESIM_WORST: 		return rbWorstMatch;

			case MMF_Standard.FUNCTION_REUSE_DONTREUSE: 		return rbQdontReuse;
			case MMF_Standard.FUNCTION_REUSE_REUSE: 			return rbQreuse;

			case MMF_Standard.FUNCTION_NOMATCH_IGNORE: 			return rbIgnore;
			case MMF_Standard.FUNCTION_NOMATCH_SETZERO: 		return rbSetZero;

			case MMF_Standard.FUNCTION_TOTAL_AVERAGE: 			return rbAverage;
			case MMF_Standard.FUNCTION_TOTAL_MAXIMUM: 			return rbMaximum;
			case MMF_Standard.FUNCTION_TOTAL_MINIMUM: 			return rbMinimum;
		}
		return null;
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
        gridBagConstraints19.gridx = 0;
        gridBagConstraints19.gridwidth = 2;
        gridBagConstraints19.fill = GridBagConstraints.BOTH;
        gridBagConstraints19.insets = new Insets(5, 5, 5, 5);
        gridBagConstraints19.gridy = 1;
        GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
        gridBagConstraints16.gridx = 1;
        gridBagConstraints16.fill = GridBagConstraints.BOTH;
        gridBagConstraints16.weightx = 1.0D;
        gridBagConstraints16.insets = new Insets(5, 5, 5, 5);
        gridBagConstraints16.gridy = 2;
        GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
        gridBagConstraints15.gridx = 0;
        gridBagConstraints15.fill = GridBagConstraints.BOTH;
        gridBagConstraints15.gridwidth = 2;
        gridBagConstraints15.insets = new Insets(5, 5, 5, 5);
        gridBagConstraints15.gridy = 0;
        GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
        gridBagConstraints4.gridx = 0;
        gridBagConstraints4.weightx = 1.0D;
        gridBagConstraints4.weighty = 1.0D;
        gridBagConstraints4.fill = GridBagConstraints.BOTH;
        gridBagConstraints4.insets = new Insets(5, 5, 5, 5);
        gridBagConstraints4.gridy = 2;
        this.setLayout(new GridBagLayout());
        this.setSize(new Dimension(446, 339));
        this.add(getPaMultipleSim(), gridBagConstraints4);
        this.add(getPaSymmetry(), gridBagConstraints15);
        this.add(getPaSingleSim(), gridBagConstraints16);
        this.add(getPaOneOrMulti(), gridBagConstraints19);
			
	}

	/**
	 * This method initializes paMultipleSim	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPaMultipleSim() {
		if (paMultipleSim == null) {
			GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
			gridBagConstraints22.gridx = 0;
			gridBagConstraints22.fill = GridBagConstraints.BOTH;
			gridBagConstraints22.insets = new Insets(0, 0, 5, 0);
			gridBagConstraints22.gridy = 2;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.fill = GridBagConstraints.BOTH;
			gridBagConstraints8.insets = new Insets(5, 0, 5, 0);
			gridBagConstraints8.gridy = 1;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.fill = GridBagConstraints.BOTH;
			gridBagConstraints5.weightx = 1.0D;
			gridBagConstraints5.gridy = 0;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.anchor = GridBagConstraints.WEST;
			gridBagConstraints1.fill = GridBagConstraints.BOTH;
			gridBagConstraints1.weighty = 1.0D;
			gridBagConstraints1.gridy = 3;
			paMultipleSim = new JPanel();
			paMultipleSim.setLayout(new GridBagLayout());
			paMultipleSim.add(getPaSelection(), gridBagConstraints5);
			paMultipleSim.add(getPaReuse(), gridBagConstraints8);
			
			paMultipleSim.setBorder(createBorder());
			paMultipleSim.add(getPaTotal(), gridBagConstraints1);
			paMultipleSim.add(getPaNoMatch(), gridBagConstraints22);
		}
		return paMultipleSim;
	}

	private Border createBorder() {
		Border border = BorderFactory.createEtchedBorder();
		return border;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JRadioButton) {
			JRadioButton rb = (JRadioButton) e.getSource();
			
			int value = Integer.parseInt(rb.getName());
			if (value < 20) {
				mmf.setCurrentFunction_OneOrMulti(value);
			} else if (value < 30) {
				mmf.setCurrentFunction_MultiSelection(value);
			} else if (value < 40) {
				mmf.setCurrentFunction_SingleSim(value);
			} else if (value < 45) {
				mmf.setCurrentFunction_ReuseQ(value);
			} else if (value < 50) {
				mmf.setCurrentFunction_NoMatch(value);
			} else if (value < 60) {
				mmf.setCurrentFunction_Total(value);
			} 
		}
		
		updateButtons();
	}

	private void updateButtons() {
		getRadioButton(mmf.getCurrentFunction_OneOrMulti()).setSelected(true);
		getRadioButton(mmf.getCurrentFunction_MultiSelection()).setSelected(true);
		getRadioButton(mmf.getCurrentFunction_SingleSim()).setSelected(true);
		getRadioButton(mmf.getCurrentFunction_Reuse()).setSelected(true);
//		getRadioButton(mmf.getCurrentFunction_ReuseC()).setSelected(true);
		getRadioButton(mmf.getCurrentFunction_NoMatch()).setSelected(true);
		getRadioButton(mmf.getCurrentFunction_Total()).setSelected(true);
		
		boolean isMulti = (mmf.getCurrentFunction_OneOrMulti() == MMF_Standard.FUNCTION_ONEORMULTI_MULTI);
		setEnabledAllChildren(paMultipleSim, isMulti);
		setEnabledAllChildren(paSingleSim, !isMulti);

		if (isMulti) {
			boolean isReuse = (mmf.getCurrentFunction_Reuse() == MMF_Standard.FUNCTION_REUSE_REUSE);
			setEnabledAllChildren(paNoMatch, !isReuse);
		}
		
	}

	private void setEnabledAllChildren(JPanel panel, boolean enabled) {
		Component[] comps = panel.getComponents();
		for (int i=0; i<comps.length; i++) {
			if (comps[i] instanceof JRadioButton || comps[i] instanceof JLabel) {
				comps[i].setEnabled(enabled);
			}
			if (comps[i] instanceof JPanel) {
				setEnabledAllChildren((JPanel) comps[i], enabled); 
			}
		}
	}

	/**
	 * This method initializes paTotal	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPaTotal() {
		if (paTotal == null) {
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints3.gridy = 2;
			gridBagConstraints3.weightx = 1.0D;
			gridBagConstraints3.weighty = 1.0D;
			gridBagConstraints3.gridx = 0;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.anchor = GridBagConstraints.WEST;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.gridx = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.gridx = 0;
			paTotal = new JPanel();
			paTotal.setLayout(new GridBagLayout());
			paTotal.add(getRbAverage(), gridBagConstraints);
			paTotal.add(getRbMaximum(), gridBagConstraints2);
			paTotal.add(getRbMinimum(), gridBagConstraints3);
			
			paTotal.setBorder(createBorder());
		}
		return paTotal;
	}

	/**
	 * This method initializes rbAverage	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbAverage() {
		if (rbAverage == null) {
			rbAverage = new JRadioButton();
			rbAverage.setText(Messages.getString("Average")); //$NON-NLS-1$
		}
		return rbAverage;
	}

	/**
	 * This method initializes rbMaximum	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbMaximum() {
		if (rbMaximum == null) {
			rbMaximum = new JRadioButton();
			rbMaximum.setText(Messages.getString("Maximum")); //$NON-NLS-1$
		}
		return rbMaximum;
	}

	/**
	 * This method initializes rbMinimum	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbMinimum() {
		if (rbMinimum == null) {
			rbMinimum = new JRadioButton();
			rbMinimum.setText(Messages.getString("Minimum")); //$NON-NLS-1$
		}
		return rbMinimum;
	}

	/**
	 * This method initializes paSelection	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPaSelection() {
		if (paMultiSelection == null) {
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.anchor = GridBagConstraints.WEST;
			gridBagConstraints7.weightx = 1.0D;
			gridBagConstraints7.gridy = 0;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridy = 1;
			gridBagConstraints6.anchor = GridBagConstraints.WEST;
			gridBagConstraints6.gridx = 0;
			paMultiSelection = new JPanel();
			paMultiSelection.setLayout(new GridBagLayout());
			paMultiSelection.add(getRbFindBestByQuery(), gridBagConstraints7);
			paMultiSelection.add(getRbFindBestByCase(), gridBagConstraints6);
			paMultiSelection.add(getRbFindBestByQueryAndCase(), new GridBagConstraints(0,2, 1,1, 0d,0d, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0), 0,0));
			
			paMultiSelection.setBorder(createBorder());
		}
		return paMultiSelection;
	}

	/**
	 * This method initializes rbFindBestByQuery	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbFindBestByQuery() {
		if (rbFindBestByQuery == null) {
			rbFindBestByQuery = new JRadioButton();
			rbFindBestByQuery.setText(Messages.getString("Best_partners_for_query_items")); //$NON-NLS-1$
		}
		return rbFindBestByQuery;
	}

	/**
	 * This method initializes rbFindBestByQuery	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbFindBestByQueryAndCase() {
		if (rbFindBestByQueryAndCaseAvg == null) {
			rbFindBestByQueryAndCaseAvg = new JRadioButton();
			rbFindBestByQueryAndCaseAvg.setText(Messages.getString("Do_both")); //$NON-NLS-1$
		}
		return rbFindBestByQueryAndCaseAvg;
	}

	/**
	 * This method initializes rbFindBestByCase	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbFindBestByCase() {
		if (rbFindBestByCase == null) {
			rbFindBestByCase = new JRadioButton();
			rbFindBestByCase.setText(Messages.getString("Best_partners_for_case_items")); //$NON-NLS-1$
		}
		return rbFindBestByCase;
	}

	/**
	 * This method initializes paReuse	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPaReuse() {
		if (paReuse == null) {
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.gridx = 1;
			gridBagConstraints14.anchor = GridBagConstraints.WEST;
			gridBagConstraints14.gridy = 2;
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 0;
			gridBagConstraints13.anchor = GridBagConstraints.WEST;
			gridBagConstraints13.gridy = 2;
			gridBagConstraints13.weightx = 1;
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 1;
			gridBagConstraints12.anchor = GridBagConstraints.WEST;
			gridBagConstraints12.gridy = 1;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridy = 1;
			gridBagConstraints11.anchor = GridBagConstraints.WEST;
			gridBagConstraints11.gridx = 0;
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 1;
			gridBagConstraints10.weightx = 1.0D;
			gridBagConstraints10.gridy = 0;
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 0;
			gridBagConstraints9.weightx = 1.0D;
			gridBagConstraints9.gridy = 0;
			
			// commented out before 20.10.2008
//			laCase = new JLabel();
//			laCase.setText("Case");
//			laQuery = new JLabel();
//			laQuery.setText("Query");
			
			paReuse = new JPanel();
			paReuse.setLayout(new GridBagLayout());
//			paReuse.add(laQuery, gridBagConstraints9);
//			paReuse.add(laCase, gridBagConstraints10);
			paReuse.add(getRbQreuse(), gridBagConstraints11);
//			paReuse.add(getRbCreuse(), gridBagConstraints12);
			paReuse.add(getRbQdontReuse(), gridBagConstraints13);
//			paReuse.add(getRbCdontReuse(), gridBagConstraints14);
			
			paReuse.setBorder(createBorder());
		}
		return paReuse;
	}

	/**
	 * This method initializes rbQreuse	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbQreuse() {
		if (rbQreuse == null) {
			rbQreuse = new JRadioButton();
			rbQreuse.setText(Messages.getString("Reuse_items")); //$NON-NLS-1$
		}
		return rbQreuse;
	}

	// commented out before 20.10.2008
//	/**
//	 * This method initializes rbCreuse	
//	 * 	
//	 * @return javax.swing.JRadioButton	
//	 */
//	private JRadioButton getRbCreuse()
//	{
//		if (rbCreuse == null)
//		{
//			rbCreuse = new JRadioButton();
//			rbCreuse.setText("Reuse items");
//		}
//		return rbCreuse;
//	}

	/**
	 * This method initializes rbQdontReuse	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbQdontReuse() {
		if (rbQdontReuse == null) {
			rbQdontReuse = new JRadioButton();
			rbQdontReuse.setText(Messages.getString("Dont_reuse")); //$NON-NLS-1$
		}
		return rbQdontReuse;
	}

	// commented out before 20.10.2008
//	/**
//	 * This method initializes rbCdontReuse	
//	 * 	
//	 * @return javax.swing.JRadioButton	
//	 */
//	private JRadioButton getRbCdontReuse()
//	{
//		if (rbCdontReuse == null)
//		{
//			rbCdontReuse = new JRadioButton();
//			rbCdontReuse.setText("Dont reuse");
//		}
//		return rbCdontReuse;
//	}

	/**
	 * This method initializes paSymmetry	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPaSymmetry() {
		if (paSymmetry == null) {
			paSymmetry = new JPanel();
			paSymmetry.setLayout(new GridBagLayout());
			
			paSymmetry.setBorder(createBorder());
		}
		return paSymmetry;
	}

	/**
	 * This method initializes paSingleSim	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPaSingleSim() {
		if (paSingleSim == null) {
			GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
			gridBagConstraints18.gridx = 0;
			gridBagConstraints18.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints18.weighty = 1.0D;
			gridBagConstraints18.gridy = 1;
			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
			gridBagConstraints17.gridx = 0;
			gridBagConstraints17.anchor = GridBagConstraints.WEST;
			gridBagConstraints17.weightx = 1.0D;
			gridBagConstraints17.weighty = 0.0D;
			gridBagConstraints17.gridy = 0;
			paSingleSim = new JPanel();
			paSingleSim.setLayout(new GridBagLayout());
			paSingleSim.add(getRbBestMatch(), gridBagConstraints17);
			paSingleSim.add(getRbWorstMatch(), gridBagConstraints18);
			
			paSingleSim.setBorder(createBorder());
		}
		return paSingleSim;
	}

	/**
	 * This method initializes rbBestMatch	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbBestMatch() {
		if (rbBestMatch == null) {
			rbBestMatch = new JRadioButton();
			rbBestMatch.setText(Messages.getString("Find_best_match")); //$NON-NLS-1$
		}
		return rbBestMatch;
	}

	/**
	 * This method initializes rbWorstMatch	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbWorstMatch() {
		if (rbWorstMatch == null) {
			rbWorstMatch = new JRadioButton();
			rbWorstMatch.setText(Messages.getString("Find_worst_match")); //$NON-NLS-1$
		}
		return rbWorstMatch;
	}

	/**
	 * This method initializes paOneOrMulti	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPaOneOrMulti() {
		if (paOneOrMulti == null) {
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.weightx = 1.0D;
			GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
			gridBagConstraints20.weightx = 1.0D;
			paOneOrMulti = new JPanel();
			paOneOrMulti.setLayout(new GridBagLayout());
			paOneOrMulti.add(getRbMultipleSims(), gridBagConstraints20);
			paOneOrMulti.add(getRbOneSim(), gridBagConstraints21);
			
			paOneOrMulti.setBorder(createBorder());
		}
		return paOneOrMulti;
	}

	/**
	 * This method initializes rbMultipleSims	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbMultipleSims() {
		if (rbMultipleSims == null) {
			rbMultipleSims = new JRadioButton();
			rbMultipleSims.setText(Messages.getString("Calculate_by_multiple")); //$NON-NLS-1$
		}
		return rbMultipleSims;
	}

	/**
	 * This method initializes rbOneSim	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbOneSim() {
		if (rbOneSim == null) {
			rbOneSim = new JRadioButton();
			rbOneSim.setText(Messages.getString("Select_one_similarity")); //$NON-NLS-1$
		}
		return rbOneSim;
	}

	/**
	 * This method initializes paNoMatch	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPaNoMatch() {
		if (paNoMatch == null) {
			GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
			gridBagConstraints25.gridx = 0;
			gridBagConstraints25.anchor = GridBagConstraints.WEST;
			gridBagConstraints25.gridy = 2;
			GridBagConstraints gridBagConstraints24 = new GridBagConstraints();
			gridBagConstraints24.gridx = 0;
			gridBagConstraints24.gridy = 0;
			laNoMatch = new JLabel();
			laNoMatch.setText(Messages.getString("Item_without_partner")); //$NON-NLS-1$
			GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
			gridBagConstraints23.anchor = GridBagConstraints.WEST;
			gridBagConstraints23.gridy = 1;
			gridBagConstraints23.weightx = 1.0D;
			gridBagConstraints23.gridx = 0;
			paNoMatch = new JPanel();
			paNoMatch.setLayout(new GridBagLayout());
			paNoMatch.add(getRbSetZero(), gridBagConstraints23);
			paNoMatch.add(laNoMatch, gridBagConstraints24);
			paNoMatch.add(getRbIgnore(), gridBagConstraints25);
			
			paNoMatch.setBorder(createBorder());
		}
		return paNoMatch;
	}

	/**
	 * This method initializes rbSetZero	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbSetZero() {
		if (rbSetZero == null) {
			rbSetZero = new JRadioButton();
			rbSetZero.setText(Messages.getString("Set_similarity_zero")); //$NON-NLS-1$
		}
		return rbSetZero;
	}

	/**
	 * This method initializes rbIgnore	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbIgnore() {
		if (rbIgnore == null) {
			rbIgnore = new JRadioButton();
			rbIgnore.setText(Messages.getString("Ignore")); //$NON-NLS-1$
		}
		return rbIgnore;
	}
	

}  //  @jve:decl-index=0:visual-constraint="10,10"
