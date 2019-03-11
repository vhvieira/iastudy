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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_String_Character;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_String_Word;

public class SMFPanel_String_Word extends SMFPanel_String_Abstract {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SMF_String_Word smf;
	private SMFPanel_SMF_Test testPanel;

	public SMFPanel_String_Word(SMF_String_Word smf) {
		super(smf);
		this.smf = smf;
		testPanel = new SMFPanel_SMF_Test(this.smf);
		initialize();
	}
		
	private JPanel getPaContend() {
		JPanel paContent = new JPanel();
		JPanel paAlgo = new JPanel();
		paAlgo.setLayout(new BoxLayout(paAlgo, BoxLayout.Y_AXIS));
		customInit(paAlgo);
		paContent.add(paAlgo);
		return paContent;
	}

	// TODO clean up
	private JPanel getDirPanel(String a, String b, String c) {
		// Copied from SMFPanel_String_Character; thanks to Java, we cannot
		// reuse code here, because Java lacks closures. Java sucks.
		JPanel dirPanel = new JPanel();
		dirPanel.setBorder( BorderFactory.createEmptyBorder() );
		dirPanel.setLayout(new BoxLayout(dirPanel, BoxLayout.Y_AXIS));
		
		JRadioButton[] dirRadiobuttons = new JRadioButton[3];
		dirRadiobuttons[SMF_String_Character.DIR_QUERY_IS_SUB] = 
			new JRadioButton(a);//, SMF_String_Character.DIR_QUERY_IS_SUB);
		dirRadiobuttons[SMF_String_Character.DIR_CASE_IS_SUB] = 
			new JRadioButton(b); //, SMF_String_Character.DIR_CASE_IS_SUB);
		dirRadiobuttons[SMF_String_Character.DIR_ANY_IS_SUB] = 
			new JRadioButton(c); //, SMF_String_Character.DIR_ANY_IS_SUB);
		dirRadiobuttons[smf.getDir()].setSelected(true);

		ButtonGroup dirGroup = new ButtonGroup();
		
		dirRadiobuttons[SMF_String_Character.DIR_QUERY_IS_SUB].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				smf.setDirection(SMF_String_Character.DIR_QUERY_IS_SUB);
				testPanel.updateSim();
			}	
		});	
		dirGroup.add(dirRadiobuttons[SMF_String_Character.DIR_QUERY_IS_SUB]);
		dirPanel.add(dirRadiobuttons[SMF_String_Character.DIR_QUERY_IS_SUB]);
	
		dirRadiobuttons[SMF_String_Character.DIR_CASE_IS_SUB].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				smf.setDirection(SMF_String_Character.DIR_CASE_IS_SUB);
				testPanel.updateSim();
			}
		});
		dirGroup.add(dirRadiobuttons[SMF_String_Character.DIR_CASE_IS_SUB]);
		dirPanel.add(dirRadiobuttons[SMF_String_Character.DIR_CASE_IS_SUB]);
				
		dirRadiobuttons[SMF_String_Character.DIR_ANY_IS_SUB].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				smf.setDirection(SMF_String_Character.DIR_ANY_IS_SUB);
				testPanel.updateSim();
			}
		});
		dirGroup.add(dirRadiobuttons[SMF_String_Character.DIR_ANY_IS_SUB]);
		dirPanel.add(dirRadiobuttons[SMF_String_Character.DIR_ANY_IS_SUB]);
		
		return dirPanel;
	}
	
	// TODO clean up
	private JPanel getCountMethodPanel() {
		JPanel result = new JPanel();
		result.setBorder( BorderFactory.createEmptyBorder() );
		result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
		JRadioButton[] rb = new JRadioButton[2];
		// COUNT_RELATIVE:
		rb[0] = new JRadioButton(Messages.getString("Devide_by_querys_number_of_words")); // 0 //$NON-NLS-1$
		// COUNT_ABSOLUTE:
		rb[1] = new JRadioButton(Messages.getString("Devide_by_cases_number_of_words")); // 1 //$NON-NLS-1$
		rb[smf.getCount()].setSelected(true);
		ButtonGroup g = new ButtonGroup();
		
		
		rb[0].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					smf.setCount(0);
					testPanel.updateSim();
				}
			});
		g.add(rb[0]);
		result.add(rb[0]);
		
		rb[1].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				smf.setCount(1);
				testPanel.updateSim();
			}
		});
		g.add(rb[1]);
		result.add(rb[1]);
	
		return result;
	}

	private JPanel getWordSimPanel() {
		JPanel result = new JPanel();
		result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
		JRadioButton[] rb = new JRadioButton[4];
		// MODE_EXACT_MATCH:
		rb[SMF_String_Common.MODE_EXACT_MATCH] = new JRadioButton(Messages.getString("Exact_match")); //, SMF_String_Common.MODE_EXACT_MATCH); //$NON-NLS-1$
		// MODE_NGRAM:
		rb[SMF_String_Common.MODE_NGRAM] = new JRadioButton(Messages.getString("Maximal_trigram")); //, SMF_String_Common.MODE_NGRAM); //$NON-NLS-1$
		if (smf.getWordSim() == SMF_String_Common.MODE_EXACT_MATCH) {
			rb[SMF_String_Common.MODE_EXACT_MATCH].setSelected(true);
		} else {
			rb[SMF_String_Common.MODE_NGRAM].setSelected(true);
		}
		ButtonGroup g = new ButtonGroup();
		
			rb[SMF_String_Common.MODE_EXACT_MATCH].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					smf.setWordSim(SMF_String_Common.MODE_EXACT_MATCH);
					testPanel.updateSim();
				}
			});
			g.add(rb[SMF_String_Common.MODE_EXACT_MATCH]);
			result.add(rb[SMF_String_Common.MODE_EXACT_MATCH]);
		
			rb[SMF_String_Common.MODE_NGRAM].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					smf.setWordSim(SMF_String_Common.MODE_NGRAM);
					testPanel.updateSim();
				}
			});
			g.add(rb[SMF_String_Common.MODE_NGRAM]);
			result.add(rb[SMF_String_Common.MODE_NGRAM]);
			
		return result;
	}

	private JPanel getWordSepPanel() {
		JPanel result = new JPanel();
		result.add(new JLabel(Messages.getString("Word_seperator_reg_ex"), JLabel.RIGHT)); //$NON-NLS-1$
		JTextField txt = new JTextField(smf.getSepRegExpr(), 8);
		txt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				smf.setSepRegExpr(((JTextField)e.getSource()).getText());
				testPanel.updateSim();
			}
		});
		result.add( new JPanel().add(txt)); // TODO clean up
		return result;
	}
	
	private void customInit(JPanel pa) {
		pa.add(getWordSepPanel());
		JCheckBox cbCaseSensitive = new JCheckBox(Messages.getString("Case_sensitive"), smf.isCaseSensitive()); //$NON-NLS-1$
		cbCaseSensitive.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				smf.setCaseSensitive(((JCheckBox)e.getSource()).isSelected());
				testPanel.updateSim();
			}
		});
		pa.add(cbCaseSensitive);
		
		// TODO clean up
		JScrollPane tmp1 = new JScrollPane();
		tmp1.setViewportView(getDirPanel(
			 	"query \u2291 case", //"case should contain query's words",  //$NON-NLS-1$
				"query \u2292 case", //"query should contain case's words",  //$NON-NLS-1$
				"query \u2292\u2291 case" //"use maximum of the above two methods" //$NON-NLS-1$
				));
		
		JScrollPane tmp2 = new JScrollPane();
		tmp2.setViewportView(getCountMethodPanel());
		
		JScrollPane tmp3 = new JScrollPane();
		tmp3.setViewportView(getWordSimPanel());
		
		JPanel h = new JPanel(); // these two should be next to each other
		// to save screen space
		h.add(tmp2);
		h.add(tmp3);
		pa.add(h);
	}

	private void initialize() {
		this.setLayout(new GridBagLayout());
		this.add(getPaContend(), new GridBagConstraints(0,1, 1,1, 1d,1d, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));	
		this.add(testPanel, new GridBagConstraints(0,2, 1,1, 1d,0d, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
	}
}
