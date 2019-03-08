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

import java.util.Observable;

import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.model.StringDesc;

/**
 * Maintains string attributes for a fixed string description. Unlike other
 * ranges (classes which implements <code>Range</code>) does not have a list of
 * the current string values, since StringAttribute objects will not be
 * referenced.
 *
 * @author myCBR Team
 *
 */
public final class StringRange extends Range {

    /**
     * Tells us how to compute similarity of attributes maintained by this
     * range.
     */
    private StringDesc desc;

    /**
     * 
     * @param prj the project this range belongs to
     * @param d
     *            the symbol description for this attribute
     */
    public StringRange(final Project prj, final StringDesc d) {
        super(prj);
        this.desc = d;
        desc.addObserver(this);
    }

    /**
     * Creates a new StringAttribute associated with the given String.
     *
     * @param s
     *            the value for which the corresponding StringAttribute should
     *            be returned
     * @return the StringAttribute specified by string
     */
    public StringAttribute getStringValue(final String s) {
        StringAttribute att = new StringAttribute(desc, s);
        return att;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.dfki.mycbr.core.casebase.Range#getAttribute(java.lang.Object)
     */
    /**
     * Gets the attribute associated with the specified <code>Object</code> obj.
     * obj is expected to be of type <code>String</code>. Returns result of
     * {@link #getStringValue(String)} if obj is of type <code>String</code>,
     * result of {@link Project#getSpecialAttribute(String)} if obj is of type
     * <code>SpecialAttribute</code>, else returns null. Is needed for
     * MultipleRange.
     *
     * @param obj
     *            representing String or SpecialAttribute
     * @return SimpleAttribute that corresponds to obj, null if there is no such
     *         SimpleAttribute
     */
    public Attribute getAttribute(final Object obj) {
        if (obj instanceof String) {
            if (getProject().isSpecialAttribute(obj.toString())) {
                return getProject().getSpecialAttribute(obj.toString());
            } else {
                return getStringValue((String) obj);
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
    public void update(final Observable arg0, final Object arg1) {
        // there is nothing to do
    }

    /*
     * (non-Javadoc)
     *
     * @see de.dfki.mycbr.core.casebase.Range#parseValue(java.lang.String)
     */
    @Override
    public Attribute parseValue(final String string) {
        return getStringValue(string);
    }
}
