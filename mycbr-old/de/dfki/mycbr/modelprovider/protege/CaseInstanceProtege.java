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

import java.util.Collection;
import java.util.HashMap;

import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.casebase.CaseInstance;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.model.vocabulary.SpecialValueHandler;
import de.dfki.mycbr.modelprovider.ModelProvider;
import edu.stanford.smi.protege.model.Instance;

/**
 * 
 * @author myCBR Team
 */
public class CaseInstanceProtege implements CaseInstance {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Instance caseInstance;
	
	private ModelCls type;
	
	/**
	 * keyset contains all slots (ModelSlot) that have special values. values
	 * are special values defined in CaseInstance interface
	 */
	private HashMap<ModelSlot, Object> specialValues = new HashMap<ModelSlot, Object>();

	public CaseInstanceProtege(Instance caseInstance) {
		this.caseInstance = caseInstance;
		type = findType();
	}
	
	// start interface implementation
	public String getName() {
		return caseInstance.getName();
	}

	public Object getSlotValue(ModelSlot slot) {
		ModelSlotProtege slotProt = (ModelSlotProtege) slot;
		Object value = caseInstance.getOwnSlotValue(slotProt.getSlotProtege());
		
		// special value?
		if (value == null) {
			value = specialValues.get(slot);
			if (value == null) {
				// initialize -> special value 'undefined'.
				value = SpecialValueHandler.SPECIAL_VALUE_UNDEFINED;
				specialValues.put(slot, value);
			}
			return value;
		}
		
		//multiple?
		if (slot.isMultiple()) {
			value = caseInstance.getOwnSlotValues(slotProt.getSlotProtege());
		}
		
		// object oriented?
		if (slot.getValueType()==ValueType.INSTANCE) {
			Instance tmpInst = (Instance) value;
			value = ModelProvider.getInstance().getCaseInstance(tmpInst.getName());
		}
		
		return value; 
	}

	public ModelCls getModelCls() {
		
		// commented out before 20.10.2008
//		String typeName = caseInstance.getDirectType().getName();
////		log.fine("get type for ["+caseInstance.getName()+"], typeName = ["+typeName+"]");
//		ModelCls cls = (ModelCls)cbrProject.getModelInstanceByName(typeName);
//		return cls;
		return type;
	}

	@SuppressWarnings("unchecked")
	public Collection listSlots() {
		return getModelCls().listSlots();
	}
	// end interface

	public Instance getCaseInstanceProtege() {
		return caseInstance;
	}

	public String toString() {
		return getName();
	}

	private ModelCls findType() {
		String typeName = caseInstance.getDirectType().getName();
		ModelCls cls = (ModelCls)ModelProvider.getInstance().getModelInstance(typeName);
		return cls;
	}

	public void setSlotValue(ModelSlot slot, Object value) {
		if (SpecialValueHandler.getInstance().isSpecialValue(value)) {
			specialValues.put(slot, value); // TODO: test if this is the solution to Bug #27
			value = null;
		}
		caseInstance.setDirectOwnSlotValue(((ModelSlotProtege)slot).getSlotProtege(), value);
	}

}
