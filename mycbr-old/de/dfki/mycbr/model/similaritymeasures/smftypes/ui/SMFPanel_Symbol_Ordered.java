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

import javax.swing.JPanel;

import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Symbol_Ordered;

/**
 * 
 * @author myCBR Team
 */
public class SMFPanel_Symbol_Ordered extends SMFPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//private final static Logger log = Logger.getLogger ( SMFPanel_Symbol_Ordered.class.getName ( ) );

	private JPanel paContent = null;
	
	private JPanel paSimMode = null;
	
	private Symbol_Ordered_Widget paMapping = null;
	
	private SMF_Symbol_Ordered smf;


	public SMFPanel_Symbol_Ordered(SMF_Symbol_Ordered smf) {
		super(smf);
		this.smf = smf;
		initialize();
	}

	public void refresh() {
		// pass through
		paMapping.refresh();
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 1;
		gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints2.gridy = 1;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 1;
		gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints1.gridy = 0;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridheight = 2;
		gridBagConstraints.weightx = 1;
		gridBagConstraints.weighty = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.gridy = 0;
		this.setLayout(new GridBagLayout());
		this.setSize(300, 200);
		this.add(getPaContent(), gridBagConstraints);
		this.add(getPaSimMode(), gridBagConstraints1);
		this.add(getPaMapping(), gridBagConstraints2);
	}

	/**
	 * This method initializes paContent	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPaContent() {
		if (paContent == null) {
			paContent = smf.getInternalSMFPanel();
			
			// commented out before 20.10.2008
//			if (paContent instanceof SMFContainerPanel)
//			{
//				JLabel la = ((SMFContainerPanel)paContent).paSimMode.getLabel();
//				Dimension prefSize = la.getPreferredSize();
//				la.setText("Internal mode:");
//				la.setPreferredSize(prefSize);
//			}
			
		}
		return paContent;
	}

	/**
	 * This method initializes paSimMode	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPaSimMode() {
		if (paSimMode == null)
		{
			paSimMode = new JPanel();
		}
		return paSimMode;
	}

	/**
	 * This method initializes paMapping	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPaMapping() {
		if (paMapping == null) {
			paMapping = new Symbol_Ordered_Widget(smf);
		}
		return paMapping;
	}

}
