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
package de.dfki.mycbr.model.vocabulary;

import org.python.core.PyFloat;
import org.python.core.PyObject;
import org.python.core.PyString;

import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunctionScript;
import de.dfki.mycbr.model.similaritymeasures.SMFContainer;
import de.dfki.mycbr.model.similaritymeasures.SMFHolder;
/**
 * 
 * @author myCBR Team
 *
 * Wrapper for a ModelSlot. Used for the scripting interface.
 */
public class ModelSlotScript extends PyObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected ModelSlot s; // the model slot that is wrapped
	
	public ModelSlotScript(ModelSlot s) {
		super();
		assert(s != null);
		this.s = s;
	}
	
	@Override
	public String toString() {
		return s.toString();
	}
	
	@Override
	/**
	 * calls the active similarity function
	 */
	public PyObject __call__(PyObject[] args, String[] keywords) {
		SMFHolder holder = SMFContainer.getInstance().getSMFHolderForModelInstance(s);
		AbstractSMFunction f = holder.getActiveSMF();
		try {
			return AbstractSMFunctionScript.callSMF(f, args);
		} catch (Exception e) {
			e.printStackTrace();
			return new PyFloat(0.0);
		}
	}
	
	@Override
	public PyObject __findattr__(String name) {
		SMFHolder holder = SMFContainer.getInstance().getSMFHolderForModelInstance(s);
		AbstractSMFunction f = holder.getCertainSMFunctionFromHolder(name);
		if (f == null) return null;
		return new AbstractSMFunctionScript(f);
	}
	
	@Override
	public PyObject __getitem__(PyObject idx) {
		if (idx instanceof PyString) {
			PyString x = (PyString) idx;
			return __findattr__(x.toString());
		}
		return SpecialValueHandler.SPECIAL_VALUE_UNDEFINED;
	}
}
