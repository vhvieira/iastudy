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

import java.io.Serializable;

/**
 * An object of this interface represents either a Class (Cls) or Slot of 
 * the designed model.
 * 
 * NOTE: 
 * It is NOT an instance in means of a case representation, query, etc.
 * You can compare it to the Instance class of Protege. 
 * @author Daniel Bahls
 */
public interface ModelInstance extends Serializable {
	/**
	 * Getter for Instance name.
	 * @return String name of this Cls/Slot.
	 */
	public String getName();

	// commented out before 20.10.2008
//	/**
//	 * Getter for super Cls or super Slot.
//	 * @return ModelInstance super-Cls/Slot. If this is not 
//	 * a specialization of another ModelInstance return null.
//	 */
//	public ModelInstance getSuperModelInstance();
	
}
