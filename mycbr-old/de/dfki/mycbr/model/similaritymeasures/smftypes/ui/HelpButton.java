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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import de.dfki.mycbr.Helper;


public class HelpButton extends JButton {

	private static final long serialVersionUID = 1L;
	private final Dimension dimension = new Dimension(21,21);
	
	static java.net.URL imageURL = HelpButton.class.getResource("help.png");
	private String key;
	
	public HelpButton(String key) {
		super(new ImageIcon(imageURL));
		
		this.key = key;
		
		setContentAreaFilled(false);
		setBorder( BorderFactory.createEmptyBorder() );
		setPreferredSize(dimension);
		setMinimumSize(dimension);
		setMaximumSize(dimension);
		
		addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() instanceof HelpButton) {
					HelpButton bu = (HelpButton) e.getSource();
					
					HelpManager helpManager = HelpManager.getInstance(bu.getKey());
					Helper.centerWindow(helpManager);
					helpManager.setVisible(true);
				}
			}
		});
	}

	public String getKey() {
		return this.key;
	}
}
