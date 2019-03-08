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

import java.util.HashMap;
import java.util.Observable;
import java.util.LinkedList;

import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.model.IntegerDesc;

/**
 * Holds IntegerAttributes for a given IntegerDescription. Each time an integer
 * is used in a case the IntegerRange returns a reference to the corresponding
 * IntegerAttribute object or creates a new one. SpecialAttributes are also
 * handled for each SpecialAttribute used in the current project
 *
 * @author myCBR Team
 *
 */
public final class IntegerRange extends Range {

    /**
     * Gives restrictions (allowed values) to this range and tells us how to
     * compute similarity of attributes of this description.
     */
    private IntegerDesc desc;

    /**
     * Associates a <code>IntegerAttribute</code> to each integer
     * used in a case.
     */
    private HashMap<Integer, IntegerAttribute> intAtts;

    /**
     *
     */
    private static final int INIT_VALUE = 10000;

    /**
     * Initializes internal data structures according to maximal Integer of
     * integer attributes (given by {@link IntegerDesc#getMax()} -
     * {@link IntegerDesc#getMin()}).
     *
     * @param prj the project this range belongs to
     * @param d
     *            the integer description for attributes maintained by this
     *            range
     */
    public IntegerRange(final Project prj, final IntegerDesc d) {
        super(prj);
        this.desc = d;
        desc.addObserver(this);
        int distance = (Integer) desc.getMax() - (Integer) desc.getMin();
        if (distance > 0 && distance < INIT_VALUE) {
            intAtts = new HashMap<Integer, IntegerAttribute>(distance);
        } else {
            intAtts = new HashMap<Integer, IntegerAttribute>();
        }
    }

    /**
     * Gets the integer description of the attributes maintained by this range.
     *
     * @return the integer description for values included in this range
     */
    public IntegerDesc getDesc() {
        return desc;
    }

    /**
     * Returns true, if there is a IntegerAttribute object contained in this
     * range, false otherwise.
     *
     * @param value
     *            the integer value to be checked
     * @return true, if value is contained in this range, false otherwise.
     */
    public boolean containsInteger(final Integer value) {
        return intAtts.containsKey(value);
    }

    /**
     * Returns the IntegerAttribute associated with the given value. Creates a
     * new IntegerAttribute if there is no IntegerAttribute for the given value
     * yet. Returns null if the specified value is not contained in the interval
     * given by {@link IntegerDesc#getMin()} and {@link IntegerDesc#getMin()} of
     * the given integer description.
     *
     * @param value
     *            the value for which the corresponding IntegerAttribute should
     *            be returned
     * @return the IntegerAttribute specified by value, null if value is not an
     *         allowed value
     */
    public IntegerAttribute getIntegerValue(final int value) {
        IntegerAttribute att = intAtts.get(value);
        int min = (Integer) desc.getMin();
        int max = (Integer) desc.getMax();
        if ((att == null) && (value >= min) && (value <= max)) {
            att = new IntegerAttribute(this.desc, value);
            intAtts.put(value, att);
        }
        return att;
    }

    /**
     * Gets the map which associates an IntegerAttribute object with each
     * integer used as value.
     *
     * @return map associating IntegerAttribuet objects with integers
     */
    public HashMap<Integer, IntegerAttribute> getIntegers() {
        return intAtts;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.dfki.mycbr.core.casebase.Range#getAttribute(java.lang.Object)
     */
    /**
     * Gets the attribute associated with the specified <code>Object</code> obj.
     * obj is expected to be of type <code>Integer</code> or <code>String</code>
     * . Returns result of {@link #getIntegerValue(int)} if obj is of type
     * <code>Integer</code>, result of {@link Project#getSpecialAttribute(String)} if obj
     * is of type <code>SpecialAttribute</code>, else returns null. Is needed for
     * MultipleRange.
     *
     * @param obj
     *            representing Integer or SpecialAttribute
     * @return SimpleAttribute that corresponds to obj, null if there is no such
     *         SimpleAttribute
     */
    public Attribute getAttribute(final Object obj) {
        if (getProject().isSpecialAttribute(obj.toString())) {
            return getProject().getSpecialAttribute(obj.toString());
        } else if (obj instanceof Integer) {
            return getIntegerValue((Integer) obj);
        } else {
            return parseValue(obj.toString());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.dfki.mycbr.core.casebase.IntegerRange
     *                                   #getIntegerValue(java.lang.Integer
     * )
     */
    /**
     * Returns the attribute for the given Integer.
     *
     * @param v an attribute with this value should be returned
     * @return integer attribute associated with the given value
     */
    public SimpleAttribute getValue(final int v) {
        return getIntegerValue(v);
    }

    /**
     * deletes all attributes that do no longer fit in the range specified by
     * minimum and maximum value of desc.
     */
    private void cleanData() {
        LinkedList<Integer> deleteList = new LinkedList<Integer>();
        int min = desc.getMin();
        int max = desc.getMax();

        // find values which should be deleted
        for (IntegerAttribute att : intAtts.values()) {
            int value = att.getValue();
            if ((value < min) || (value > max)) {
                deleteList.add(value);
            }
        }

        // delete values
        for (int i : deleteList) {
            intAtts.remove(i);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
    public void update(final Observable ob, final Object o) {
        if (desc.equals(ob)) {
            cleanData();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see de.dfki.mycbr.core.casebase.Range#parseValue(java.lang.String)
     */
    @Override
    public Attribute parseValue(final String string) {
        return getIntegerValue(Integer.parseInt(string));
    }
}
