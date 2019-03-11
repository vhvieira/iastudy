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

import java.util.Collection;
import java.util.HashMap;

import de.dfki.mycbr.model.casebase.CaseInstance;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.model.vocabulary.SpecialValueHandler;

/**
 * 
 * @author myCBR Team
 */
public class CaseInstanceStandalone implements CaseInstance {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** maps slots (ModelSlot) to values (Object) */
	@SuppressWarnings("unchecked")
	private HashMap slotToValues;
	private String caseName;
	private ModelCls typeCls;
	@SuppressWarnings("unchecked")
	private HashMap specialValues = new HashMap();

	@SuppressWarnings("unchecked")
	public CaseInstanceStandalone(String caseName, ModelCls typeCls, HashMap slotToValues) {
		this.caseName = caseName;
		this.slotToValues = slotToValues;
		this.typeCls = typeCls;
	}
	
	// start interface implementation
	public String getName() {
		return caseName;
	}

	@SuppressWarnings("unchecked")
	public Object getSlotValue(ModelSlot slot) {
		Object value = slotToValues.get(slot);
		if (value == null) {
			value = specialValues.get(slot);
			if (value == null) {
				// initialize -> special value 'undefined'.
				value = SpecialValueHandler.SPECIAL_VALUE_UNDEFINED;
				specialValues.put(slot, value);
			}
		}
		return value;
	}
	
	@SuppressWarnings("unchecked")
	public void setSlotValue(ModelSlot slot, Object value) {
		slotToValues.put(slot, value);
	}

	public ModelCls getModelCls() {
		return typeCls;
	}

	@SuppressWarnings("unchecked")
	public Collection listSlots() {
		return slotToValues.keySet();
	}
	// end interface
	
	public String toString() {
		return getName();
	}

}
