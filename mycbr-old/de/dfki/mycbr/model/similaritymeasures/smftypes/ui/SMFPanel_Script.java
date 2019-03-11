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
import java.awt.Font;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Script;

public class SMFPanel_Script extends SMFPanel implements DocumentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SMF_Script smf;
	private SMFPanel_SMF_Test testPanel;
	private JTextArea taScript;
	
	public void insertUpdate(DocumentEvent e) {
		smf.setScript(taScript.getText());		
	}
	
	public void removeUpdate(DocumentEvent e) {
		smf.setScript(taScript.getText());		
	}

	public void changedUpdate(DocumentEvent e) {
		smf.setScript(taScript.getText());
	}

	
	public SMFPanel_Script(SMF_Script smf) {
		super(smf);
		this.smf = smf;
		testPanel = new SMFPanel_SMF_Test(this.smf); 
		taScript = new JTextArea(smf.getScript());
		taScript.setSize(405, 535);
		taScript.getDocument().addDocumentListener(this);
		taScript.setFont(new Font("Monospaced", Font.PLAIN, 12));
		
		this.setLayout(new BorderLayout());
		this.setSize(new java.awt.Dimension(605,535));
		// TODO clean up
		JScrollPane tmp = new JScrollPane();
		tmp.setViewportView(taScript);
		this.add(tmp, java.awt.BorderLayout.CENTER);
		this.add(testPanel, java.awt.BorderLayout.SOUTH);
	}

}
