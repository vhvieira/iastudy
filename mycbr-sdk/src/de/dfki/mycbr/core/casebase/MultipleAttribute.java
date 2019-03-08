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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.dfki.mycbr.core.model.AttributeDesc;

/**
 * Represents sets of values in query/case. The class Type should be a class
 * which extends. AttributeDesc class and corresponds to the description of the
 * values contained in the mentioned set. This class is a wrapper for the
 * original values which are maintained by a class implementing Range.
 *
 * @param <Type> type of attribute description describing single values
 *  contained in this collection
 * @author myCBR Team
 */
public final class MultipleAttribute<Type extends AttributeDesc>
                                                            extends Attribute {

    /**
     * Multiple attributes have a list of "simple" attributes as their value. Be
     * aware that you do not include Special attributes here, since this does not
     * make sense.
     */
    private List<Attribute> values;

    /**
     *
     */
    private Type type;

    /**
     * Initializes this with empty value.
     *
     * @param desc
     *            the description of this attribute.
     * @param l1 list of values for this collection
     */
    public MultipleAttribute(final Type desc, final LinkedList<Attribute> l1) {
        this.type = desc;
        this.values = l1;
    }

    /**
     * Gets the list of values for this attribute.
     *
     * @return the values
     */
    public List<Attribute> getValues() {
        return values;
    }

    /**
     * Adds the given attribute to the list of values for this attribute.
     *
     * @param value
     *            the attribute to be added
     */
    public void addValue(final Attribute value) {
        if (value instanceof SpecialAttribute) {
            return; // not allowed to add special attributes to this collection
        } else {
            if (value instanceof SimpleAttribute) {
                if (!((SimpleAttribute) value)
                        .getAttributeDesc().equals(type)) {
                    return; // attribute must have the given description
                }
            }
        }

        if (!values.contains(value)) {
            values.add(value); // only add value if it is not already contained
            setChanged();
            notifyObservers();
        }
    }

    /**
     * Adds the given attribute to the list of values for this attribute.
     *
     * @param value
     *            the attribute to be added
     */
    public void removeValue(final Attribute value) {
        if (values.remove(value)) {
        	setChanged();
        	notifyObservers();
        }
    }

    /**
     * Returns the string representation of this attribute as the concatenation
     * of the string representations of the single values.
     *
     * @return string representation of this attribute's value
     */
    public String toString() {
        String result = "";
        for (Iterator<Attribute> it = this.values.iterator(); it.hasNext();) {
            result = result + it.next().toString() + ";";
        }
        result = result.substring(0, result.length());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.dfki.mycbr.core.casebase.SimpleAttribute#getValueAsString()
     */
    @Override
    public String getValueAsString() {
        return toString();
    }

    /**
     *
     * @return the type of this attribute
     */
    public Type getAttributeDesc() {
        return type;
    }
}
