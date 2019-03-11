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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_String_Character;

public class SMFPanel_String_Character extends SMFPanel 
	implements ChangeListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private SMF_String_Character smf;
		private SMFPanel_SMF_Test testPanel;
		
		private JPanel paN;
		private JPanel panelCountMethod;
		
		public SMFPanel_String_Character(SMF_String_Character smf) {
			super(smf);
			this.smf = smf;
			testPanel = new SMFPanel_SMF_Test(this.smf);
			
			initMatchPanel();
			initialize();
		}
		
		private NumberTextField nuNGramVal;
		
		private ButtonGroup algoSelection;
		
		private JRadioButton rbExact; 
		private JRadioButton rbPartial; 
		private JRadioButton rbNGram; 
		private JRadioButton rbRegEx; 
	
		private JPanel paContent;
		
		private JLabel labelDirPartial;
		private JLabel labelDirRegex;
		private JLabel labelCountMethod;
		
		private JPanel panelDirPartial;
		private JPanel panelDirRegex;
		
		private JPanel getDirPanel(String a, String b, String c) {
			JPanel dirPanel = new JPanel();
			dirPanel.setLayout(new BoxLayout(dirPanel, BoxLayout.X_AXIS));
			// TODO clean up
			JRadioButton[] dirRadiobuttons = new JRadioButton[3];
			dirRadiobuttons[SMF_String_Character.DIR_QUERY_IS_SUB] = 
				new JRadioButton(a); //, SMF_String_Character.DIR_QUERY_IS_SUB);
			dirRadiobuttons[SMF_String_Character.DIR_CASE_IS_SUB] = 
				new JRadioButton(b); //, SMF_String_Character.DIR_CASE_IS_SUB);
			if (c!=null) {
				dirRadiobuttons[SMF_String_Character.DIR_ANY_IS_SUB] = 
					new JRadioButton(c); //, SMF_String_Character.DIR_ANY_IS_SUB);
			}
			dirRadiobuttons[smf.getDir()].setSelected(true);
			
			ButtonGroup dirGroup = new ButtonGroup();
			
			dirRadiobuttons[SMF_String_Character.DIR_QUERY_IS_SUB].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					smf.setDirection(SMF_String_Character.DIR_QUERY_IS_SUB);
					    testPanel.updateSim();
				}});
			dirGroup.add(dirRadiobuttons[SMF_String_Character.DIR_QUERY_IS_SUB]);
			dirPanel.add(dirRadiobuttons[SMF_String_Character.DIR_QUERY_IS_SUB]);
			dirPanel.add( Box.createRigidArea( new Dimension(5,5)));
			
			dirRadiobuttons[SMF_String_Character.DIR_CASE_IS_SUB].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						smf.setDirection(SMF_String_Character.DIR_CASE_IS_SUB);
   					    testPanel.updateSim();
					}});
			dirGroup.add(dirRadiobuttons[SMF_String_Character.DIR_CASE_IS_SUB]);
			dirPanel.add(dirRadiobuttons[SMF_String_Character.DIR_CASE_IS_SUB]);
			dirPanel.add( Box.createRigidArea( new Dimension(5,5)));
			
			if (c!=null) {
				dirRadiobuttons[SMF_String_Character.DIR_ANY_IS_SUB].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						smf.setDirection(SMF_String_Character.DIR_ANY_IS_SUB);
						testPanel.updateSim();
					}});
				dirGroup.add(dirRadiobuttons[SMF_String_Character.DIR_ANY_IS_SUB]);
				dirPanel.add(dirRadiobuttons[SMF_String_Character.DIR_ANY_IS_SUB]);
			}
			return dirPanel;
		}

		private JPanel getCountMethodPanel() {
			if (panelCountMethod == null){
				panelCountMethod = new JPanel();
				panelCountMethod.setLayout(new BoxLayout(panelCountMethod, BoxLayout.X_AXIS));
				
				JRadioButton rb0 = new JRadioButton(Messages.getString("Relative")); //$NON-NLS-1$
				JRadioButton rb1 = new JRadioButton(Messages.getString("Absolute")); //$NON-NLS-1$
				
				if (smf.getCount() == 0){
					rb0.setSelected(true);
				} else {
					rb1.setSelected(true);	
				}
				
				ButtonGroup g = new ButtonGroup();
				
				rb0.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								smf.setCount(0);
							testPanel.updateSim();
							}	
						});
				g.add(rb0);
				panelCountMethod.add(rb0);
				panelCountMethod.add( Box.createRigidArea( new Dimension(5,5)));
				rb1.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						smf.setCount(1);
					testPanel.updateSim();
					}	
				});
				g.add(rb1);
				panelCountMethod.add(rb1);
			}
				
			return panelCountMethod;
		}
		
		private void initMatchPanel() {
			
			rbExact = new JRadioButton(Messages.getString("Exact_match"));  //$NON-NLS-1$
			rbPartial = new JRadioButton(Messages.getString("partial_match"));  //$NON-NLS-1$
			rbNGram = new JRadioButton(Messages.getString("NGram_match"));  //$NON-NLS-1$
			rbRegEx = new JRadioButton(Messages.getString("Regular_expression_match"));  //$NON-NLS-1$
			
			// N-Gram also has a number input field:
			paN = new JPanel();
			JLabel laN = new JLabel(Messages.getString("N"), JLabel.RIGHT); //$NON-NLS-1$
			nuNGramVal = new NumberTextField(smf.getNgram_val());
			nuNGramVal.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					smf.setNgram_val(((NumberTextField)e.getSource()).getValue());
					testPanel.updateSim();
				}
			});
			paN.add(laN);
			paN.add(new JPanel().add(nuNGramVal)); // TODO clean up
			
			labelDirPartial = new JLabel(Messages.getString("Direction")); //$NON-NLS-1$
			labelDirRegex = new JLabel(Messages.getString("Direction")); //$NON-NLS-1$
			panelDirRegex = getDirPanel(
					"query \u2291 case", //"regular expression query matches case", //$NON-NLS-1$
					"query \u2292 case", //"regular expression case matches query", //$NON-NLS-1$
					null
					);
			
			labelCountMethod = new JLabel(Messages.getString("Count_method")); //$NON-NLS-1$
			panelDirPartial = getDirPanel(
					"query \u2291 case", //$NON-NLS-1$
					"query \u2292 case", //$NON-NLS-1$
					"query \u2292\u2291 case"); //$NON-NLS-1$
			
			getCountMethodPanel();
			
			// create combobox with the same labels:
			algoSelection = new ButtonGroup();
			rbExact.addChangeListener(this);
			rbNGram.addChangeListener(this);
			rbPartial.addChangeListener(this);
			rbRegEx.addChangeListener(this);
			
			if ( this.smf.getMatchingMode() == SMF_String_Common.MODE_EXACT_MATCH ){
				rbExact.setSelected(true);
			} else if ( this.smf.getMatchingMode() == SMF_String_Common.MODE_NGRAM ) {
				rbNGram.setSelected(true);
			} else if ( this.smf.getMatchingMode() == SMF_String_Common.MODE_PARTIAL_MATCH ) {
				rbPartial.setSelected(true);
			} else if ( this.smf.getMatchingMode() == SMF_String_Common.MODE_REGEXPR ) {
				rbRegEx.setSelected(true);
			}
		}

		/**
		 * This method initializes this
		 * 
		 */
		private void initialize() {
			this.setLayout(new GridBagLayout());
			this.add(getPaContent(), 					new GridBagConstraints(0,1, 1,1, 1d,1d, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0));	
			this.add(testPanel, 						new GridBagConstraints(0,2, 1,1, 1d,0d, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0));
		}

		/**
		 * This method initializes paContent	
		 * 	
		 * @return javax.swing.JPanel	
		 */
		private JPanel getPaContent() {
			if (paContent == null) {
				JLabel laChooseFunction = new JLabel();
				laChooseFunction.setText(Messages.getString("Matching_mode")); //$NON-NLS-1$
				
				paContent = new JPanel();
				
				JCheckBox cbCaseSensitive = new JCheckBox(Messages.getString("Case_sensitive"), smf.isCaseSensitive()); //$NON-NLS-1$
				cbCaseSensitive.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						smf.setCaseSensitive(((JCheckBox)e.getSource()).isSelected());
						testPanel.updateSim();
					}
				});
			
				algoSelection.add(rbExact);
				algoSelection.add(rbNGram);
				algoSelection.add(rbPartial);
				algoSelection.add(rbRegEx);
				
				paContent = new JPanel();
				paContent.setLayout(new GridBagLayout());
				
				GridBagConstraints gridBagConstraints = new GridBagConstraints();
				gridBagConstraints.gridx = 0;
				gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
				gridBagConstraints.insets = new java.awt.Insets(5,5,5,5);
				gridBagConstraints.gridy = 0;
				paContent.add(laChooseFunction, gridBagConstraints);
				
				gridBagConstraints.gridy = 1;
				paContent.add(cbCaseSensitive, gridBagConstraints);
				
				gridBagConstraints.gridy = 2;
				paContent.add(rbNGram, gridBagConstraints);
				
				gridBagConstraints.gridy = 4;
				paContent.add(rbPartial, gridBagConstraints);
				
				gridBagConstraints.insets = new java.awt.Insets(5,25,5,5);
				gridBagConstraints.gridy = 3;
				paContent.add(paN, gridBagConstraints);
				
				gridBagConstraints.gridy = 5;
				paContent.add(labelDirPartial, gridBagConstraints);
				
				gridBagConstraints.gridx = 1;
				gridBagConstraints.insets = new java.awt.Insets(5,5,5,5);
				paContent.add(panelDirPartial, gridBagConstraints);
				
				gridBagConstraints.gridx = 0;
				gridBagConstraints.insets = new java.awt.Insets(5,25,5,5);
				gridBagConstraints.gridy = 6;
				paContent.add(labelCountMethod, gridBagConstraints);
				
				gridBagConstraints.gridx = 1;
				gridBagConstraints.insets = new java.awt.Insets(5,5,5,5);
				paContent.add(getCountMethodPanel(), gridBagConstraints);
				
				gridBagConstraints.gridx = 0;
				gridBagConstraints.gridy = 7;
				paContent.add(rbExact, gridBagConstraints);

				gridBagConstraints.gridy = 8;
				paContent.add(rbRegEx, gridBagConstraints);	
				
				gridBagConstraints.insets = new java.awt.Insets(5,25,5,5);
				gridBagConstraints.gridy = 9;
				paContent.add(labelDirRegex, gridBagConstraints);
				
				gridBagConstraints.gridx = 1;
				gridBagConstraints.gridy = 9;
				paContent.add(panelDirRegex, gridBagConstraints);		
			}
			return paContent;
		}
		
		private class NumberTextField extends JTextField {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public NumberTextField(int defaultValue) {
				super(defaultValue+"", Math.max((defaultValue+"").length(), 4)); //$NON-NLS-1$ //$NON-NLS-2$
				this.addKeyListener(new java.awt.event.KeyAdapter() {
					public void keyTyped(java.awt.event.KeyEvent e) {
						if (!(e.getKeyChar() >= '0' && e.getKeyChar() <= '9')) {
							e.consume();
						}
					}
				});
			}
			
			public int getValue() throws NumberFormatException {
				try {
					return Integer.parseInt(this.getText());
				} catch (java.lang.NumberFormatException e) {
					return 0;
				}
			}
		}

		/**
		 * One can choose between the matching modes:
		 * 	Exact match, partial match, ngram match and regex match.
		 * 
		 *  In case one changes the matching mode, 
		 *  the matching mode, the fields for the user inputs
		 *  of the other similarity functions have to be disabled
		 *  and the similarity for the test panel has to be updated.
		 */
		public void stateChanged(ChangeEvent e) {
			
			if (((JRadioButton)e.getSource()).isSelected()) {
				
				if (e.getSource().equals(rbExact)) {
					
					this.smf.setMatchingMode( SMF_String_Common.MODE_EXACT_MATCH );
					
					setPanelComponentsEnabled(paN, false);
					
					labelCountMethod.setEnabled(false);
					setPanelComponentsEnabled(panelCountMethod, false);
					
					labelDirPartial.setEnabled(false);
					setPanelComponentsEnabled(panelDirPartial, false);
					
					labelDirRegex.setEnabled(false);
					setPanelComponentsEnabled(panelDirRegex, false);
					
				} else if (e.getSource().equals(rbNGram)) { 
					
					this.smf.setMatchingMode( SMF_String_Common.MODE_NGRAM );
					
					setPanelComponentsEnabled(paN, true);
					
					labelCountMethod.setEnabled(false);
					setPanelComponentsEnabled(panelCountMethod, false);
					
					labelDirPartial.setEnabled(false);
					setPanelComponentsEnabled(panelDirPartial, false);
					
					labelDirRegex.setEnabled(false);
					setPanelComponentsEnabled(panelDirRegex, false);
					
				} else if (e.getSource().equals(rbPartial)) {
					
					this.smf.setMatchingMode( SMF_String_Common.MODE_PARTIAL_MATCH );
					setPanelComponentsEnabled(paN, false);
					
					labelCountMethod.setEnabled(true);
					setPanelComponentsEnabled(panelCountMethod, true);
					
					labelDirPartial.setEnabled(true);
					setPanelComponentsEnabled(panelDirPartial, true);
					
					labelDirRegex.setEnabled(false);
					setPanelComponentsEnabled(panelDirRegex, false);
					
				} else if (e.getSource().equals(rbRegEx)) {
					
					this.smf.setMatchingMode( SMF_String_Common.MODE_REGEXPR );
					setPanelComponentsEnabled(paN, false);
					
					labelCountMethod.setEnabled(false);
					setPanelComponentsEnabled(panelCountMethod, false);
					
					labelDirPartial.setEnabled(false);
					setPanelComponentsEnabled(panelDirPartial, false);
					
					labelDirRegex.setEnabled(true);
					setPanelComponentsEnabled(panelDirRegex, true);
				}	
				testPanel.updateSim();
			}	

		}
		
		/**
		 * Calls this panel's and all its components' setEnabled function.
		 * Used by the state changed method, in case the similarity function is changed.  
		 * @param panel the panel to be enabled/disabled
		 * @param enabled true to enable the panel and its components, false to disable them
		 */
		public void setPanelComponentsEnabled(JPanel panel, boolean enabled) {
			
			panel.setEnabled(enabled);
			
			for (java.awt.Component comp : panel.getComponents()) {
				comp.setEnabled(enabled);
			}
			
		}
}
