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
package de.dfki.mycbr.modelprovider.protege.explanation;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import de.dfki.mycbr.explanation.KnowledgeSource;

public class KnowledgeSourceEditor extends JPanel implements KeyListener, ActionListener {
	
	private static final long serialVersionUID = 1L;
	private JLabel laName;
	private JTextField txtName;
	private JLabel laPattern;
	private JTextArea txtPattern;
	private JRadioButton rbWebResource;
	private JRadioButton rbFile;
	private JRadioButton rbSysCall;
	private KnowledgeSource currentSource;
	private ButtonGroup buttonGroup;
	private ConceptExplanationSchemePanel paConceptExpScheme;

	
	public KnowledgeSourceEditor(ConceptExplanationSchemePanel paConceptExpScheme) {
		this.paConceptExpScheme = paConceptExpScheme;
		setLayout(new GridBagLayout());
//		add(createHeaderPanel(),	new GridBagConstraints(0,0, 3,1, 1.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		
		laName 				= new JLabel(Messages.getString("Knowledge_source_name")); //$NON-NLS-1$
		txtName 			= new JTextField();
		add(laName,			new GridBagConstraints(0,1, 3,1, 0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
		add(txtName,		new GridBagConstraints(0,2, 3,1, 1.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
		
		rbWebResource 		= new JRadioButton(Messages.getString("Url")); //$NON-NLS-1$
		rbFile 				= new JRadioButton(Messages.getString("File")); //$NON-NLS-1$
		rbSysCall	 		= new JRadioButton(Messages.getString("System_call")); //$NON-NLS-1$
		add(rbWebResource,	new GridBagConstraints(0,3, 1,1, 0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
		add(rbFile,			new GridBagConstraints(1,3, 1,1, 0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
		add(rbSysCall,		new GridBagConstraints(2,3, 1,1, 0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
		
		laPattern 			= new JLabel(Messages.getString("Pattern")); //$NON-NLS-1$
		txtPattern 			= new JTextArea();
		add(laPattern,		new GridBagConstraints(0,4, 3,1, 0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
		add(new JScrollPane(txtPattern),		new GridBagConstraints(0,5, 3,1, 1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
		
		buttonGroup = new ButtonGroup();
		buttonGroup.add(rbFile);
		buttonGroup.add(rbSysCall);
		buttonGroup.add(rbWebResource);
		rbWebResource.setSelected(true);
		
		rbFile.addActionListener(this);
		rbSysCall.addActionListener(this);
		rbFile.addActionListener(this);
		
		txtName.addKeyListener(this);
		txtPattern.addKeyListener(this);
	}
	
	private void setEditable(boolean editable) {
		laName.setEnabled(editable);
		txtName.setEnabled(editable);
		laPattern.setEnabled(editable);
		txtPattern.setEnabled(editable);
		rbFile.setEnabled(editable);
		rbSysCall.setEnabled(editable);
		rbWebResource.setEnabled(editable);
	}

	public void setSelectedSource(KnowledgeSource selectedSource) {
		this.currentSource = selectedSource;
		
		if (selectedSource == null) {
			setEditable(false);
			txtName.setText(""); //$NON-NLS-1$
			txtPattern.setText(""); //$NON-NLS-1$
			return;
		}
		setEditable(true);
		txtName.setText(selectedSource.getTitle());
		txtPattern.setText(selectedSource.getPattern());
	}

	public void keyReleased(KeyEvent e) {
		Object txtField = e.getSource();
		if (txtField == txtName) {
			currentSource.setTitle(txtName.getText());
		} else if (txtField == txtPattern) {
			currentSource.setPattern(txtPattern.getText());
		}
		paConceptExpScheme.repaintList();
	}
	
	public void keyPressed(KeyEvent arg0) {
		// ignore
	}
	public void keyTyped(KeyEvent arg0) {
		// ignore
	}

	public void actionPerformed(ActionEvent e) {
		Object button = e.getSource();
		if (button == rbFile) {
			currentSource.setSourceType(KnowledgeSource.TYPE_FILE);
		} else if (button == rbSysCall) {
			currentSource.setSourceType(KnowledgeSource.TYPE_SYS_CALL);
		} else if (button == rbWebResource) {
			currentSource.setSourceType(KnowledgeSource.TYPE_WEB_RESOURCE);
		}
	}

}
