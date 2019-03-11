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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.modelprovider.ModelProvider;
import de.dfki.mycbr.modelprovider.ModelProviderProtege;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Slot;

/**
 * 
 * @author myCBR Team
 */
public class ModelSlotProtege extends ModelInstanceProtege implements ModelSlot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Slot slot;

	public ModelSlotProtege(ModelProviderProtege modelProviderProtege, Instance inst) {
		super(modelProviderProtege, inst);
		slot = (Slot) inst;
	}

	// start interface implementation
	public ValueType getValueType() {
		return ValueType.getValueType(slot.getValueType().toString());
	}

	@SuppressWarnings("unchecked")
	public Collection getAllowedValues() {
		Collection c = null;
		if (getValueType() == ValueType.INSTANCE) {
			// list the allowed class types
			c = new ArrayList();
			for (Iterator it=slot.getAllowedClses().iterator(); it.hasNext();) {
				Cls cls = (Cls) it.next();
				ModelCls inst = (ModelCls) ModelProvider.getInstance().getModelInstance(cls.getName());
				if (inst != null) {
					c.add(inst);
				}
			}
		} else {
			c = slot.getAllowedValues();
		}
		return c;
	}

	public Number getMinimumValue() {
		return slot.getMinimumValue();
	}

	public Number getMaximumValue() {
		return slot.getMaximumValue();
	}
	// end interface
	
	public Slot getSlotProtege() {
		return slot;
	}

	public boolean isMultiple() {
		return slot.getAllowsMultipleValues();
	}

}
