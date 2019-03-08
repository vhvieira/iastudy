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

package de.dfki.mycbr.core;

import java.util.Collection;
import java.util.Observer;

import de.dfki.mycbr.core.casebase.Instance;

/**
 * Methods to be implemented by case bases of the current project.
 *
 * @author myCBR Team
 *
 */
public interface ICaseBase extends Observer {

    /**
	 * @author myCBR Team
	 *
	 */
	public enum Meta {
		AUTHOR, DATE
	}


	/**
     * Returns the name of the case base. The case base is identified by its
     * name
     *
     * @return the unique name of the case base
     */
    String getName();

    /**
     * Sets the name of this case base to name. The name has to be unique, use
     * commands to check uniqueness because case base does not know about the
     * names of other case bases
     *
     * @param name
     *            the unique name of the case base
     * @throws Exception when there is a case base with that name
     */
    void setName(String name) throws Exception;

    /**
     * Returns true, if the case base contains a case with the given name.
     *
     * @param name
     *            the name of the case to be found
     * @return the instance, if there is a case with this name, null otherwise.
     */
    Instance containsCase(String name);

    /**
     * Removes the case with the given name. Does nothing if there is none.
     * There can at most be one case with the given name, since cases are
     * identified by their name.
     *
     * @param name
     *            the name of the case to be deleted
     */
    boolean removeCase(String name);

    /**
     * Returns all cases known for this case base.
     *
     * @return all cases contained in this case base.
     */
    Collection<Instance> getCases();
   
	/**
	 * @param caze the case to be added
	 */
	void addCase(Instance caze);
	
	/**
	 * Returns the project this case base belongs to
	 * 
	 * @return the project this case base belongs to
	 */
	Project getProject();
}
