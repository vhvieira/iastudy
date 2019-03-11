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

import de.dfki.mycbr.model.casebase.CaseInstance;

/**
 * @author myCBR Team
 * 
 * Interface for Classes.
 */
public interface ModelCls extends ModelInstance {

	/**
	 * Get all slots/attributes that belong to this class.
	 * @return Collection of ModelSlots
	 */
	public Collection<ModelSlot> listSlots();

	/**
	 * Getter for all cases from the case base that are of this type.
	 * NOTE: 
	 * The returned collection may be empty but not null.
	 * @return Collection of CaseInstance objects.
	 */
	public Collection<CaseInstance> getDirectCaseInstances();

	
	/**
	 * Getter for its super class.
	 * @return ModelCls super class.
	 */
	public ModelCls getSuperCls();

	/**
	 * Getter for all sub classes
	 * @return Collection<ModelCls> of sub classes
	 */
	public Collection<Object> getDirectSubClses();
}


