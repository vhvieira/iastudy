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

/**
 * Slots created using XML files first are SlotDataRawImpl objects.
 * Then they are passed to ClsDataRaw objects which then can be used by the modelprovider to build CaseInstance objects
 * Implements the interface SlotDataRaw 
 * 
 * @author myCBR Team
 */
public class SlotDataRawImpl implements SlotDataRaw {

	private static final long serialVersionUID = 1L;
	private Number maxVal;
	private Number minVal;
	private Collection< String > allowedValues;
	private ValueType valueType;
	private String slotName;
	private boolean multiple;

	/**
	 * Constructor for this SlotDataRawImpl object, initializing it with a name, a value type,
	 * a collection of allowed values, a minimum/maximum value and a flag for being multiple.
	 * All these values can later be entered by getters.
	 * 
	 * @param slotName the name of this slot
	 * @param valueType the value type of this slot
	 * @param allowedValues the allowed values of this slot (for value type SYMBOL)
	 * @param minVal the minimum value of this slot (for value type INTEGER or FLOAT)
	 * @param maxVal the maximum value of this slot (for value type INTEGER or FLOAT)
	 * @param multiple true, if the slot allowes multiple values. false otherwise
	 */
	protected SlotDataRawImpl(String slotName, ValueType valueType, Collection< String > allowedValues, Number minVal, Number maxVal, boolean multiple) {
		this.slotName = slotName;
		this.valueType = valueType;
		this.allowedValues = allowedValues;
		this.minVal = minVal;
		this.maxVal = maxVal;
		this.multiple = multiple;
	}
	
	/**
	 * Getter for the name of this slot
	 * @return the name of this slot
	 */
	public String getName() {
		return slotName;
	}
	
	/**
	 * Getter for the value type of this slot 
	 * @return the value type of this slot
	 * @see ValueType
	 */
	public ValueType getValueType() {
		return valueType;
	}

	/**
	 * Getter for the allowed values of this slot.
	 * Used for slots havin SYMBOL as value type.
	 * @return a collection of the allowd values of this slot
	 */
	public Collection< String > getAllowedValues() {
		return allowedValues;
	}
	
	/**
	 * Getter for the minimum value of this slot.
	 * Used for slots having INTEGER or FLOAT as value type. 
	 * @return the minimum value of this slot
	 */
	public Number getMinimumValue() {
		return minVal;
	}

	/**
	 * Getter for the maximum value of this slot.
	 * Used for slots having INTEGER or FLOAT as value type. 
	 * @return the maximum value of this slot
	 */
	public Number getMaximumValue() {
		return maxVal;
	}

	/**
	 * A slot can be multiple, meaning that you are allowed to specify
	 * more than one value (of the slots value type) for this slot.
	 * @return true, if this slot is multiple. false, otherwise.
	 */
	public boolean isMultiple() {
		return multiple;
	}

}
