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

import java.util.logging.Logger;

import javax.swing.JLabel;

import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Boolean;


/**
 * 
 * @author myCBR Team
 */
public class SMFPanel_Boolean extends SMFPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(SMFPanel_Boolean.class.getName());

	
	public SMFPanel_Boolean(SMF_Boolean smf) {
		super(smf);

		log.fine("initialize SMFPanel_Boolean."); //$NON-NLS-1$
		initialize();
	}
	
	private void initialize() {
		add(new JLabel(Messages.getString("Boolean_has_trivial_similarity_measure"))); //$NON-NLS-1$
	}

}
