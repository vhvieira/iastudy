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
package de.dfki.mycbr.modelprovider.protege;

import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.modelprovider.ModelProviderProtege;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.event.FrameListener;
import edu.stanford.smi.protege.model.Instance;

/**
 * 
 * @author myCBR Team
 */
public class ModelInstanceProtege implements ModelInstance, FrameListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Instance inst;
	
	public ModelInstanceProtege(ModelProviderProtege modelProviderProtege, Instance inst) {
		this.inst = inst;
		inst.addFrameListener(this);
	}

	public String getName() {
		return inst.getName();
	}

	public String toString() {
		return getName();
	}

	public void browserTextChanged(FrameEvent event) {
		// not of interest
	}

	public void deleted(FrameEvent event) {
		// not of interest
	}

	public void ownFacetAdded(FrameEvent event) {
	}

	public void ownFacetRemoved(FrameEvent event) {
	}

	public void ownFacetValueChanged(FrameEvent event) {
	}

	public void ownSlotAdded(FrameEvent event) {
	}

	public void ownSlotRemoved(FrameEvent event) {
	}

	public void ownSlotValueChanged(FrameEvent event) {
	}

	public void visibilityChanged(FrameEvent event) {
	}

	public Instance getProtegeInstance() {
		return inst;
	}
	
	public void nameChanged(FrameEvent arg0) {
	}
	
}
