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
package de.dfki.mycbr.model.similaritymeasures.smftypes;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;

import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.vocabulary.ModelSlot;

/**
 * 
 * @author myCBR Team
 */
public class FakeSlot implements ModelSlot, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger ( FakeSlot.class.getName ( ) );

	private String fakeSlotName = null;
	
	private Integer maxValue = null;
	private Integer minValue = null;
	private ValueType vt;
	@SuppressWarnings("unchecked")
	private Collection allowedValues = new HashSet();

	public FakeSlot(String fakeSlotName, ValueType vt) {
		this.fakeSlotName = fakeSlotName;
		this.vt = vt;
		
		log.fine("Created FakeSlot ["+fakeSlotName+"].");
	}
	
	//
	// implementation of interface ModelSlot
	//
	public ValueType getValueType() {
//		return ValueType.INTEGER;
		return vt;
	}

	@SuppressWarnings("unchecked")
	public Collection getAllowedValues() {
		return allowedValues;
	}

	public Number getMinimumValue() {
		return minValue;
	}

	public Number getMaximumValue() {
		return maxValue;
	}

	public String getName() {
		return fakeSlotName;
	}

	//
	// additional functionality
	//
	public void setMaximumValue(Integer maxValue) {
		log.fine("new max [" + fakeSlotName + "]: [" + maxValue + "]");
		this.maxValue = maxValue;
	}

	public void setMinimumValue(Integer minValue) {
		log.fine("new min [" + fakeSlotName + "]: [" + minValue + "]");
		this.minValue = minValue;
	}

	public void setRange(Integer minValue, Integer maxValue) {
		log.fine("changing range of FakeSlot [" + fakeSlotName + "]: [" + this.minValue +
				"," + this.maxValue + "] --> [" + minValue+  "," + maxValue + "]");
		this.minValue = minValue;
		this.maxValue = maxValue;
	}
	
	@SuppressWarnings("unchecked")
	public void setAllowedValues(Collection allowedValues) {
		this.allowedValues = allowedValues;
	}
	
	public String toString() {
		return "FAKESLOT [" + getName() + "]: max[" + getMaximumValue() + "] min[" + getMinimumValue() + "]";
	}

	public boolean isMultiple() {
		return false;
	}

}
