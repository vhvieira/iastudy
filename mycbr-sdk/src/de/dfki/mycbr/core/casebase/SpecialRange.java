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

import java.util.Collection;
import java.util.HashMap;

import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.model.SpecialDesc;
import de.dfki.mycbr.core.model.SymbolDesc;

/**
 * Range that holds all possible special values
 * of the given project.
 * 
 * @author myCBR Team
 *
 */
public final class SpecialRange extends SymbolRange {

    /**
     * @param prj the project this range belongs to
     * @param desc the description this range belongs to
     * @param allowedValues the allowed values for this range
     */
    public SpecialRange(final Project prj, final SymbolDesc desc,
            final Collection<String> allowedValues) {
        super(prj, desc, allowedValues);
        initSymbolAttributes(allowedValues);
        initIndexes();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    /**
     * Compares the given attributes according to a linear order. This linear
     * order is used for indexing symbol attributes. By default, the first
     * attribute specified in the given symbol description is the smallest
     * element in the linear order.
     *
     * @param att1
     *            the first attribute to be compared with the second
     * @param att2
     *            the second attribute
     * @return -2 if one of the attributes does not occur in this range, -1 if
     *         the first attribute is smaller than the second, 0 if they are
     *         equal, 1 else
     */
    public int compare(final SpecialAttribute att1,
                           final SpecialAttribute att2) {
        Integer index1 = getIndexes().get(att1);
        Integer index2 = getIndexes().get(att2);

        if (index1 == null || index2 == null) {
            return -1 - 1;
        } else {
            if (index1 < index2) {
                return -1;
            } else if (index1 == index2) {
                return 0;
            }
        }
        return 1;
    }

    /**
     * Initializes the private field symbols with the given allowedValues.
     *
     * @param values
     *            the symbols maintained by this range
     */
    final void initSymbolAttributes(final Collection<String> values) {
        symbols = new HashMap<String, SymbolAttribute>(values.size());
        for (String value : values) {
            getSymbols().put(value,
            		new SpecialAttribute(this.getProject(),
            				(SpecialDesc)this.getDesc(), value));
        }
    }
    
    /**
     * Returns the index of the given attribute. Returns null, If the attribute
     * is not maintained by this range.
     *
     * @param att
     *            the attribute whose index should be returned
     * @return index of the given att, null if this attribute is unknown
     */
    public Integer getIndexOf(final SpecialAttribute att) {
        return getIndexes().get(att);
    }

    /**
     * @param value the value of the new symbol attribute
     * @return the new symbol attribute with value value
     */
    public final SymbolAttribute addSymbolValue(final String value) {
            SymbolAttribute att = getSymbols().get(value);
            if (att == null) {
                att = new SpecialAttribute(getProject(), (SpecialDesc)getDesc(), value);
                getSymbols().put(value, att);
                getIndexes().put(att, getHighestIndex());
                setHighestIndex(getHighestIndex() + 1);
            }
            return (SpecialAttribute) att;
    }
    
    /*
     * (non-Javadoc)
     *
     * @see de.dfki.mycbr.core.casebase.Range#getAttribute(java.lang.Object)
     */
    /**
     * Gets the attribute associated with the specified <code>Object</code> obj.
     * obj is expected to be of type <code>String</code>. Returns result of
     * {@link #getSymbolValue(String)} if obj is of type <code>String</code>,
     * else returns null. Is needed for MultipleRange.
     *
     * @param obj
     *            representing String or SpecialValue
     * @return SimpleAttribute that corresponds to obj, null if there is no such
     *         SimpleAttribute
     */
    public final Attribute getAttribute(final Object obj) {

        if (getProject().isSpecialAttribute(obj.toString())) {
            return getSymbolValue((String) obj);
        }
    
        return null;
    }
    
}
