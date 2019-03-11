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
package de.dfki.mycbr;

import java.util.Collection;
import java.util.HashMap;

/**
 * Provides getter and setter methods for a classes raw data.
 * @author myCBR Team
 */
public class ClsDataRaw {

	private String clsName;
	private String superClsName;
	
	/** stores all case instances of this type. objects are of type CaseDataRaw */
	private Collection<CaseDataRawImpl> caseInstances;
	
	/** maps slot names (String) to its raw data (SlotDataRaw) */
	private HashMap<String, SlotDataRaw> slotnamesToSlotDataRaw;
	
	/**
	 * Build up the whole object in this constructor. All provided methods are read-only.
	 * @param caseName String name of this cls.
	 * @param superClsName String name of super cls.
	 * @param slots 
	 * @param caseInstances Collection of case instance data sets (HashMap). Maps slotname (String) to its value (serialized as String).
	 */
	protected ClsDataRaw(String caseName, String superClsName, HashMap<String, SlotDataRaw> slotnamesToSlotDataRaw, Collection<CaseDataRawImpl> caseInstances) {
		this.clsName = caseName;
		this.superClsName = superClsName;
		this.caseInstances = caseInstances;
		this.slotnamesToSlotDataRaw = slotnamesToSlotDataRaw;
	}
	
	/**
	 * Getter for its case instance data sets.
	 * @return Collection of case instance data sets (CaseDataRaw).
	 */
	public Collection<CaseDataRawImpl> getCaseInstances() {
		return caseInstances;
	}

	/**
	 * Getter for the name of the class
	 * @return the name of the class
	 */
	public String getClsName() {
		return clsName;
	}

	/**
	 * Getter for the name of the superclass
	 * @return the name of the superclass
	 */
	public String getSuperClsName() {
		return superClsName;
	}

	/**
	 * Getter for HashMap mapping slot names (String) to its raw data (SlotDataRaw)
	 * @return map for names to raw data
	 */
	public HashMap<String, SlotDataRaw> getSlotsAndValueTypes(){
		return slotnamesToSlotDataRaw;
	}
	
}
