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

import de.dfki.mycbr.core.model.StringDesc;

/**
 * Represents strings used in query/cases. Currently, each StringAttribute
 * object is used once, meaning that it is not referenced when used in several
 * cases. This is because usually one string does not appear more than once.
 *
 * @author myCBR Team
 */
public final class StringAttribute extends SimpleAttribute {

    // value of this attribute as string
    /**
     *
     */
    private String value;

    /**
     * Initializes this.
     *
     * @param desc
     *            the descriptions of this
     * @param v
     *            the value of this
     */
    StringAttribute(final StringDesc desc, final String v) {
        super(desc);
        this.value = v;
    }

    /**
     * Gets the value of this attribute.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the string representation of this attribute's value.
     *
     * @return the string representation of the value
     */
    public String toString() {
        return getValue();
    }

    /*
     * (non-Javadoc)
     *
     * @see de.dfki.mycbr.core.casebase.SimpleAttribute#getValueAsString()
     */
    @Override
    /**
     * Returns a string representation of this attribute.
     * @return string representation of this attribute.
     */
    public String getValueAsString() {
        return getValue();
    }

    /**
     * Returns true, if this attribute equals the given attribute. A string
     * attribute is equal to another if the respective attribute values equal.
     *
     * @param att
     *            the attribute to be compared with this.
     * @return true, if values of attributes equal, false otherwise.
     */
    public boolean equals(final StringAttribute att) {
        return this.value.equals(att.getValue());
    }

    /**
     * @return hash value for this
     */
    public int hashCode() {
        return super.hashCode();
    }
}
