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
package de.dfki.mycbr.modelprovider.standalone;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import de.dfki.mycbr.CBRProject;
import de.dfki.mycbr.retrieval.RetrievalResultListener;
import de.dfki.mycbr.retrieval.RetrievalResults;
import de.dfki.mycbr.retrieval.ui.RetrievalContainer;

public class RetrievalFrame extends JFrame implements RetrievalResultListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel paContent = new JPanel();
	private RetrievalContainer rc;

	public RetrievalFrame() {
		setTitle("myCBR");
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		
		paContent.setLayout(new GridBagLayout());
		
		JButton buOptions = new JButton("Options");
		buOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				CBRProject.getInstance().getProjectOptions().showOptions(getRetrievalFrame());
			}
		});
		paContent.add(buOptions, new GridBagConstraints(0,0, 1,1, 0.0,0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0));
		
		rc = RetrievalContainer.getInstance();
		paContent.add(rc, new GridBagConstraints(0,1, 1,1, 1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
		
		getContentPane().add(paContent);
		
		setSize((int)screensize.getWidth()-20, (int)screensize.getHeight()-100);
		setLocation((screensize.width - getWidth()) / 2, 20);
		
		setVisible(true);
		
	}

	protected Frame getRetrievalFrame() {
		return this;
	}

	public void setRetrievalResults(RetrievalResults results) {
		rc.setRetrievalResults(results);
	}

	public void setRetrievalState(double percentage) {
		rc.setRetrievalState(percentage);
	}
}
