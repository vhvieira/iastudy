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
package de.dfki.mycbr.explanation.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import de.dfki.mycbr.explanation.ExplanationManager;

public class StandardExplanationPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JButton buRefreshExp = new JButton(Messages.getString("StandardExplanationPanel.Refresh_explanation_manager")); //$NON-NLS-1$

	public StandardExplanationPanel() {
		setLayout(new BorderLayout());
		
		buRefreshExp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//
				// run it in an extra thread
				// if the explanation manager has to initialize, all the work is done buy the java GUI thread
				// which means that the GUI freezes. The following construction avoids that.
				//
				Thread initThread = new Thread() {
					@Override
					public void run() {
						ExplanationManager.getInstance().reInit();
					}
				};
				initThread.start();
			}
		});
		
		add(buRefreshExp, BorderLayout.CENTER);
	}
}
