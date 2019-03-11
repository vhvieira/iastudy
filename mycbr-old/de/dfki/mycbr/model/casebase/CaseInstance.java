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

import java.io.Serializable;
import java.util.Collection;

import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelSlot;

/**
 * Interface for a case instance. The class this case belongs to, the case's name
 * and the slot's should be accessable.
 * @author myCBR Team
 *
 */
public interface CaseInstance extends Serializable
{
	
	/**
	 * Getter for the (unique) name of the instance.
	 * @return String name of the instance.
	 */
	public String getName();

	/**
	 * Getter for the value of a given attribute.
	 * NOTE:
	 * This never returns null. Return one of the special values. 
	 * 'UNDEFINED' means the same like null in java.
	 * 'UNKNOWN' means: this value is explicitely not set, because it is unknown.
	 * @param slot ModelSlot the attribute.
	 * @return Object value of the given attribute.
	 */
	public Object getSlotValue(ModelSlot slot);

	/**
	 * Sets the value of slot to value
	 * @param slot the slot which should get a value
	 * @param value the value for slot
	 */
	public void setSlotValue(ModelSlot slot, Object value);

	/**
	 * Getter for the direct type. 
	 * @return ModelCls the class this case is instance of.
	 */
	public ModelCls getModelCls();

	/**
	 * Lists all attributes (ModelSlots) that belong to the case type.
	 * @return  all attributes that belong to the case type
	 */
	public Collection<ModelSlot> listSlots();
		
}
