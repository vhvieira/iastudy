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
package de.dfki.mycbr.model.casebase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;

import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.model.vocabulary.SpecialValue;
import de.dfki.mycbr.model.vocabulary.SpecialValueHandler;
import de.dfki.mycbr.retrieval.Query;

/**
 *
 * Wrapper for a CaseInstance. Used for the scripting interface.
 * 
 * @author myCBR Team
 */
public class CaseInstanceScript extends PyObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final static Logger log = Logger.getLogger(CaseInstanceScript.class.getName());
	protected CaseInstance inst; 
	
	public CaseInstanceScript(CaseInstance c) {
		assert(c != null);
		this.inst = c;
	}
	
	public CaseInstance getCaseInstance() {
		return this.inst;
	}
	
	public CaseInstanceScript(Query q) {
		assert(q != null);
		this.inst = q;
	}
	
	@Override
	public String toString() {
		return inst.toString();
	}
	
	protected static PyObject toPyObjectAtom(ModelSlot slot, Object value) {
		ValueType v = (slot.getValueType());
		if (SpecialValueHandler.getInstance().isSpecialValue(value)) {
			return (SpecialValue) value;
		} else if (v == ValueType.FLOAT) {
			return new PyFloat((Float)value);
		} else if (v == ValueType.INTEGER) {
			return new PyInteger((Integer)value);
		} else if (v == ValueType.STRING || v == ValueType.SYMBOL) {
			return new PyString((String) value);
		} else if (v == ValueType.BOOLEAN) {
			return new PyInteger( ((Boolean) value).booleanValue() ?  1 : 0);
		} else if (v == ValueType.INSTANCE) {
			return new CaseInstanceScript((CaseInstance) value);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static PyObject toPyObject(ModelSlot slot, Object value) {
		if (SpecialValueHandler.getInstance().isSpecialValue(value)) {
			return (SpecialValue) value;
		} else if (slot.isMultiple()) {
			PyList result = new PyList();
			for (Object v : (Collection)value) {
				result.add(toPyObjectAtom(slot, v));
			}
			return result;
		} else {
			return toPyObjectAtom(slot, value);
		}
	}
	
	protected static Object fromPyObjectAtom(ModelSlot slot, PyObject value) throws Exception {
		ValueType v = (slot.getValueType());
		if (SpecialValueHandler.getInstance().isSpecialValue(value)) {
			return value;
		} else if (v == ValueType.FLOAT) {
			return new Float(((PyFloat) value).getValue());
		} else if (v == ValueType.INTEGER) {
			return new Integer(((PyInteger) value).getValue());
		} else if (v == ValueType.STRING) {
			return ((PyString) value).toString();
		} else if (v == ValueType.BOOLEAN) {
			return new Boolean( ((PyInteger) value).getValue() != 0 ? true : false);
		} else if (v == ValueType.SYMBOL) {
			// check if it is a valid symbol: 
			String sym = ((PyString) value).toString();
			if (slot.getAllowedValues().contains(sym)) {
				return sym; 
			} else {
				throw new Exception(sym + " is not a valid value");
			}
		} else if (v == ValueType.INSTANCE) {
			return ((CaseInstanceScript) value).inst;
		}
		return null;
	}

	public static Object fromPyObject(ModelSlot slot, PyObject value) throws Exception {
		if (SpecialValueHandler.getInstance().isSpecialValue(value)) {
			return (SpecialValue) value;
		} else if (slot.isMultiple()) {
			if (value instanceof PyList) {
				Collection<Object> result = new ArrayList<Object>();
				PyList list = ((PyList)value);
				for (int i = 0; i < list.__len__(); i++) {
					result.add(fromPyObjectAtom(slot, list.__getitem__(i)));
				}
				return result;
			} else {
				throw new Exception("need a list of values for a multiple value slot");
			}
		} else {
			return fromPyObjectAtom(slot, value);
		}
	}

	@Override
	public PyObject __findattr__(String name) {
		ModelCls cls = inst.getModelCls();
		while (cls != null) {
			for (ModelSlot slot : cls.listSlots()) {
				if (slot.getName().equals(name)) return toPyObject(slot, inst.getSlotValue(slot));
			}
			cls = cls.getSuperCls();
		}
		return null;
	}
	
	@Override 
	public void __setattr__(String name, PyObject value) {
		ModelCls cls = inst.getModelCls();
		while (cls != null) {
			for (ModelSlot slot : cls.listSlots()) {
				if (slot.getName().equals(name)) {
					try {
						inst.setSlotValue(slot, fromPyObject(slot, value));
					} catch (Exception e) {
						e.printStackTrace();
					}
					return;
				}
			}
			cls = cls.getSuperCls();
		}
	}
	
	@Override
	public PyObject __getitem__(PyObject idx) {
		if (idx instanceof PyString) {
			PyString x = (PyString) idx;
			return __findattr__(x.toString());
		}
		return SpecialValueHandler.SPECIAL_VALUE_UNDEFINED;
	}
	
	@Override
	public void __setitem__(PyObject idx, PyObject value) {
		if (idx instanceof PyString) {
			PyString x = (PyString) idx;
			__setattr__(x.toString(), value);
		} else {
			log.warning("index has to be a string");
		}
	}
}
