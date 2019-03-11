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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import de.dfki.mycbr.model.vocabulary.ModelInstance;

/**
 * @author myCBR Team
 * 
 * One object of this class is a mapping from slots/classes to their SMFunction.
 * 
 */
public class SimMap implements Serializable{	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public HashMap<ModelInstance, AbstractSMFunction> m; 
	
	public void put(ModelInstance inst, AbstractSMFunction func) {
		assert(inst != null);
		assert(func != null);
		m.put(inst, func);		
	}
	
	public AbstractSMFunction get(ModelInstance inst) {
		assert(inst != null);
		return m.get(inst);
	}
	
	/*
	 * for debugging purposes only
	 */
	public String toString() {
		StringBuilder b = new StringBuilder("{");
		for (Iterator< Entry< ModelInstance, AbstractSMFunction> > i = m.entrySet().iterator(); i.hasNext(); ) {
			Entry< ModelInstance, AbstractSMFunction> e = i.next();
			ModelInstance inst = (ModelInstance) e.getKey();
			AbstractSMFunction smf = (AbstractSMFunction) e.getValue();
			b.append(inst.getName());
			b.append(": ");
			b.append(smf.toString());
			b.append("\n");
		}
		b.append("}\n");
		return b.toString();
	}
	
	public SimMap(SimMap s) {
		this.m = new HashMap<ModelInstance, AbstractSMFunction>(s.m);
	}
	
	public void putAll(SimMap s) {
		this.m.putAll(s.m);
	}
	
	public Collection<AbstractSMFunction> values() {
		return m.values();
	}
	
	public SimMap() {
		this.m = new HashMap<ModelInstance, AbstractSMFunction>();
	}
	
	public SimMap clone() {
		return new SimMap(this);
	}

	public HashMap<ModelInstance, AbstractSMFunction> getInnerMap() {
		return m;
	}

	public void setInnerMap(HashMap<ModelInstance, AbstractSMFunction> innerMap) {
		this.m = innerMap;
	}
}
