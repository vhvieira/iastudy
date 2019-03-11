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
package de.dfki.mycbr.model.similaritymeasures;

import org.python.core.PyFloat;
import org.python.core.PyObject;

import de.dfki.mycbr.model.casebase.CaseInstanceScript;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.model.vocabulary.SpecialValueHandler;

/**
 * Wraps an AbstractSMFunction for Jython.
 * @author myCBR Team
 * 
 */
public class AbstractSMFunctionScript extends PyObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected AbstractSMFunction smf; 
	
	@Override
	public String toString() {
		return smf.toString();
	}	
	
	public AbstractSMFunctionScript(AbstractSMFunction smf) {
		super();
		assert(smf != null);
		this.smf = smf;
	}
	
	public static PyObject callSMF(AbstractSMFunction f, PyObject[] args) throws Exception {
		if (args.length != 2) {
			return new PyFloat(0.0);
		}
		ModelSlot s = (ModelSlot) f.getModelInstance();
		Object q = CaseInstanceScript.fromPyObject(s, args[0]);
		Object c = CaseInstanceScript.fromPyObject(s, args[1]);
		SpecialValueHandler svh = SpecialValueHandler.getInstance();
		if (svh.isSpecialValue(q) || svh.isSpecialValue(c) || q == null || c == null) {
			return new PyFloat(svh.getSimilarityBetween(q, c, null));
		}
		return new PyFloat(f.getSimilarityBetween(q, c, null));
	}
	
	@Override
	public PyObject __call__(PyObject[] args, String[] keywords) {
		try {
			return callSMF(smf, args);
		} catch (Exception e) {
			e.printStackTrace();
			return new PyFloat(0.0);
		}
	}
	
}
