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

import java.util.Collection;

import de.dfki.mycbr.ValueType;

/**
 * @author myCBR Team
 *
 * Interface for Slots/Attributes.
 */
public interface ModelSlot extends ModelInstance {
	/**
	 * Getter for the value type.
	 * E.g. ValueType.Integer, ValueType.Float, ValueType.Symbol, ValueType.String, etc.
	 * @return ValueType value type.
	 */
	public ValueType getValueType();

	/**
	 * Getter for the allowed values.
	 * Not used by numeric types, but important for symbol and instance(OO) type 
	 * 
	 * @return Collection symbols (Strings if valueType=Symbol / ModelCls if valueType=INSTANCE ) to be allowed for instances.
	 */
	public Collection<Object> getAllowedValues();

	/**
	 * Getter for minimum value.
	 * The range of allowed values is important to calculate similarity measures.
	 * Not used by symbol types, but important for numeric types.
	 * @return Number minimum value to be allowed for an instance.
	 */
	public Number getMinimumValue();

	/**
	 * Getter for maximum value.
	 * The range of allowed values is important to calculate similarity measures.
	 * Not used by symbol types, but important for numeric types.
	 * @return Number minimum value to be allowed for an instance.
	 */
	public Number getMaximumValue();
	
	
	/**
	 * @return true if a ModelInstance object may have more than one slot value for this slot type.
	 */
	public boolean isMultiple();
	

}
