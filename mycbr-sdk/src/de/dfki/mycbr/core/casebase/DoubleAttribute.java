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

import de.dfki.mycbr.core.model.DoubleDesc;

/**
 * Represents integers in query/cases. To avoid unnecessary objects, objects of
 * this class should only be created by integer ranges. For a fixed integer #
 * description, the integer attributes will then be referenced.
 *
 * @author myCBR Team
 */
public final class DoubleAttribute extends SimpleAttribute implements
        Comparable<DoubleAttribute> {

    /**
     *
     */
    private double value;

    /**
     * Creates a new Double attribute for the given description with the given
     * value.
     *
     * @param desc
     *            the description this attribute belongs to
     * @param value2
     *            the value of this attribute
     */
    DoubleAttribute(final DoubleDesc desc, final double value2) {
        super(desc);
        this.value = value2;
    }

    /**
     * Returns the numeric value of this attribute.
     *
     * @return the value of this attribute
     */
    public double getValue() {
        return value;
    }

    /**
     * Compares this attribute to the given one. Returns 0 if the values are
     * identical, -1 if this value is smaller than the given attribute's value
     * and 1 otherwise.
     *
     * @param o the attribute which should be compared to this
     * @return comparison result of the respective attributes
     */
    public int compareTo(final DoubleAttribute o) {
        if (this.value == o.value) {
            return 0;
        } else if (this.value < o.value) {
            return -1;
        } else {
            return 1;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see de.dfki.mycbr.core.casebase.SimpleAttribute#getValueAsString()
     */
    @Override
    /**
     * Returns a string representation of this attribute's value.
     * @return string representation of the value
     */
    public String getValueAsString() {
        return Double.toString(getValue());
    }
}
