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
package de.dfki.mycbr.retrieval;

import de.dfki.mycbr.model.casebase.CaseInstance;
import de.dfki.mycbr.model.vocabulary.ModelSlot;

/**
 * 
 * @author myCBR Team
 */
public interface Query extends CaseInstance
{
	/**
	 * Weight modes:
	 * The slot weights for the amalgamation function can be either determined by
	 *  * the class' slot weights only: 	WEIGHT_MODE_CLASS_ONLY
	 *  * the query's slot weights only:	WEIGHT_MODE_QUERY_ONLY
	 *  * multiplication of class' and query's slot weights:	WEIGHT_MODE_MULTIPLY
	 */
	public static final int WEIGHT_MODE_CLASS_ONLY 	= 0;
	public static final int WEIGHT_MODE_QUERY_ONLY 	= 1;
	public static final int WEIGHT_MODE_MULTIPLY 	= 2;
	
	public int getWeightMode();
	
	public Double getWeight(ModelSlot slot);
}
