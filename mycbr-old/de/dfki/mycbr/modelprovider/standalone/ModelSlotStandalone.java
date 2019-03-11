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
package de.dfki.mycbr.modelprovider.standalone;

import java.util.Collection;
import java.util.logging.Logger;

import de.dfki.mycbr.SlotDataRaw;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.modelprovider.ModelProviderStandalone;

/**
 * 
 * @author myCBR Team
 */
public class ModelSlotStandalone extends ModelInstanceStandalone implements ModelSlot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger ( ModelSlotStandalone.class.getName ( ) );

	private ValueType valueType;

	@SuppressWarnings("unchecked")
	private Collection allowedValues;

	private Number minVal;

	private Number maxVal;
	
	private boolean isMultiple = false;
	
	public ModelSlotStandalone(SlotDataRaw slotDataRaw, ModelProviderStandalone modelProviderStandalone) {
		super(slotDataRaw.getName(), modelProviderStandalone);
		this.valueType = ValueType.getValueType(slotDataRaw.getValueType().toString());
		this.allowedValues = slotDataRaw.getAllowedValues();
		this.minVal = slotDataRaw.getMinimumValue();
		this.maxVal = slotDataRaw.getMaximumValue();
		this.isMultiple = slotDataRaw.isMultiple();
	}

	// start interface(ModelSlot) methods
	public ValueType getValueType() {
		return valueType;
	}

	@SuppressWarnings("unchecked")
	public Collection getAllowedValues() {
		return allowedValues;
	}

	public Number getMinimumValue() {
		return minVal;
	}

	public Number getMaximumValue() {
		return maxVal;
	}
	// end interface

	@SuppressWarnings("unchecked")
	public void addNewValue(String newSymbol) {
		allowedValues.add(newSymbol);
	}
	
	public void removeOldValue(String oldSymbol) {
		allowedValues.remove(oldSymbol);
	}

	public boolean isMultiple() {
		return isMultiple;
	}
	
	@SuppressWarnings("unchecked")
	public void setAllowedValues(Collection allowedValues) {
		log.fine("change allowed values");
		this.allowedValues = allowedValues;
	}

}
