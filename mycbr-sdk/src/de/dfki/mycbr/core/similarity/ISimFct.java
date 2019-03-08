/*
 * myCBR License 3.0
 * 
 * Copyright (c) 2006-2015, by German Research Center for Artificial Intelligence (DFKI GmbH), Germany
 * 
 * Project Website: http://www.mycbr-project.net/
 * 
 * This library is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.
 * 
 * endOfLic */

package de.dfki.mycbr.core.similarity;

import java.util.Observer;

import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Attribute;
import de.dfki.mycbr.core.model.AttributeDesc;
import de.dfki.mycbr.core.similarity.config.MultipleConfig;

/**
 * Has to be implemented by each similarity function such that one can always
 * compute similarity of two attributes without casting the ISimFct to a
 * specific object.
 * 
 * @author myCBR Team
 */
public interface ISimFct extends Observer {

	/**
	 * Computes the similarity of value1 and value2. Be aware that this function
	 * does not have to be symmetric. The specified values have to be of same
	 * type and must belong to the same description, else null is returned.
	 * 
	 * @param value1
	 *            the first value
	 * @param value2
	 *            the second value
	 * @return similarity of value1 and value2, invalid similarity if the values
	 *         do not have the same type or do belong to different attribute
	 *         descriptions
	 * @throws Exception
	 */
	Similarity calculateSimilarity(Attribute value1, Attribute value2)
			throws Exception;

	/**
	 * Specifies whether this function is symmetric or asymmetric
	 * 
	 * @return true, if similarity function is symmetric, false otherwise
	 */
	boolean isSymmetric();

	/**
	 * Returns the name of this function
	 * @return the name of this function
	 */
	public String getName();

	/**
	 * Sets the name of this function to name
	 * @param name the new name of this function
	 */
	public void setName(String name);

	/**
	 * Returns the description of the attributes which can be compared
	 * using this function
	 * @return the description this function belongs to
	 */
	public AttributeDesc getDesc();

	/**
	 * Specifies whether this function is symmetric or not.
	 * If a function <code>sim</code> is symmetric, <code>sim(q,c) = sim(c,q)</code>
	 * should hold for all queries q and cases c this function is defined on.
	 * Else the function is asymmetric.
	 * @param symmetric specifies whether this function is symmetric or not.
	 */
	public void setSymmetric(boolean symmetric);

	public MultipleConfig getMultipleConfig();
	public void setMultipleConfig(MultipleConfig mc);
	public Project getProject();

	/**
	 * Creates a new function which is the same function
	 * as this but with a different description, namely descNEW.
	 * Assumption: this function fits the description of descNEW.
	 * @param descNEW
	 * @param active
	 */
	public void clone(AttributeDesc descNEW, boolean active);
}
