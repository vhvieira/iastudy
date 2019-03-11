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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

/**
 * Provides a dialog to notify the user, when the current version of myCBR
 * is higher than the version specified by the xml files
 * 
 * @author myCBR Team
 */
public class VersionUpdateDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private JScrollPane scrollpane;
	private JTextPane txtPane;
	private JButton buOk;
	
	// flag when user has already been notified
	// in this case, do not notify again
	private static boolean userAlreadyNotified = false;
	
	/**
	 * Constructor initializing this dialog with parent frame owner.
	 * The current project is set to project.
	 *  
	 * @param owner the owner of this dialog
	 */
	public VersionUpdateDialog(Frame owner) {
		super(owner);
		initialize();
		setTitle(Messages.getString("Changes_on_project_because_of_mycbr_version")); //$NON-NLS-1$
		txtPane.setText(CBRProject.getInstance().getVersionHandlerMessage());
		buOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
	}
	
	/**
	 * Shows this dialog in case the user has not been notified already.
	 * @param owner the owner of this dialog
	 * @param project the current myCBR project
	 */
	public static void showDialogMaybe(Frame owner) {
		
		if (userAlreadyNotified) {
			return; 	// user has already been informed
		}
		if (CBRProject.getInstance().getVersionHandlerMessage() == null) {
			return; 	// no new version message available
		}
		
		JDialog d = new VersionUpdateDialog(owner);
		d.setSize(500, 500);
		Helper.centerWindow(d);
		d.setVisible(true);
	}

	/**
	 * Initializes this.
	 * 
	 * @see #getBuOk()
	 */
	private void initialize() {
		this.setSize(300, 200);
		this.setLayout(new BorderLayout());
		this.add(getScrollpane(), BorderLayout.CENTER);
		this.add(getBuOk(), BorderLayout.SOUTH);
	}

	/**
	 * Initializes the scrollpane	
	 * 	
	 * @return javax.swing.JScrollPane the scrollpane containing the field for displaying the changes	
	 * @see #getTxtPane()
	 */
	private JScrollPane getScrollpane() {
		if (scrollpane == null) {
			scrollpane = new JScrollPane();
			scrollpane.setViewportView(getTxtPane());
		}
		return scrollpane;
	}

	/**
	 * Initializes txtPane	
	 * 	
	 * @return javax.swing.JTextPane for displaying the changes	
	 */
	private JTextPane getTxtPane() {
		if (txtPane == null) {
			txtPane = new JTextPane();
		}
		return txtPane;
	}

	/**
	 * This method initializes buOk	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBuOk() {
		if (buOk == null) {
			buOk = new JButton(Messages.getString("Ok")); //$NON-NLS-1$
		}
		return buOk;
	}

}
