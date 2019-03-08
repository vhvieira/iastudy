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

package de.dfki.mycbr.core.casebase;

import de.dfki.mycbr.core.Project;

import java.text.ParseException;
import java.util.Observer;

/**
 * Interface to be implemented by containers of attribute values. Is of great
 * importance for multiple attributes.
 *
 * @author myCBR Team
 *
 */
public abstract class Range implements Observer {

    /**
     * the project this range belongs to.
     */
    private Project prj;

    /**
     *
     * @param p the project this range belongs to.
     */
    public Range(final Project p) {
        this.prj = p;
    }

    /**
     * Gets the attribute associated with the specified <code>Object</code> obj.
     * obj is expected to be of a certain type (dependent on <code>AttributeDesc
     * </code> object of
     * class implementing this interface) or SpecialAttribute (represented by a
     * string). Should return result of {@link Project#getSpecialAttribute(String)} if obj
     * is a special attribute, a corresponding attribute for the description if obj
     * is of the expected type, else null.
     *
     * @param obj
     *            the object for which the corresponding attribute should be
     *            returned
     * @return Attribute that corresponds to obj, null if there is no such
     *         Attribute
     * @throws ParseException if obj does not fit description
     */
    public abstract Attribute getAttribute(Object obj) throws ParseException;

    /**
     *
     * @param string the string which should be used for parsing
     * @return the attribute which corresponds to the given string
     * @throws ParseException if string does not fit description
     * @throws Exception 
     */
    abstract Attribute parseValue(String string) throws ParseException, Exception;

    /**
     * @param p the project to set
     */
    public final void setProject(final Project p) {
        this.prj = p;
    }

    /**
     * @return the prj
     */
    public final Project getProject() {
        return prj;
    }

}
