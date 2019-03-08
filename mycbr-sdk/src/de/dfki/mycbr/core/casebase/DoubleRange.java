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
import java.util.LinkedList;
import java.util.Observable;

import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.model.DoubleDesc;

/**
 * Holds FloatAttributes for a given FloatDescription. Each time a float is used
 * in a case the FloatRange returns a reference to the corresponding
 * DoubleAttribute object or creates a new one. SpecialAttributes are also
 * handled for each SpecialAttribute used in the current project
 *
 * @author myCBR Team
 *
 */
public final class DoubleRange extends Range {

    /**
     * Gives restrictions (allowed values) to this range and tells us how to
     * compute similarity of attributes of this description.
     */
    private DoubleDesc desc;

    /**
     * Associates a <code>DoubleAttribute</code> to each double used in a case for
     * the given attribute description.
     */
    private HashMap<Double, DoubleAttribute> doubleAtts;

    /**
     * Initializes internal data structures according to maximal number of
     * integer attributes (given by {@link DoubleDesc#getMax()} -
     * {@link DoubleDesc#getMin()}).
     *
     * @param prj the project this range belongs to
     * @param doubleDesc
     *            the double description for attributes maintained by this range
     */
    public DoubleRange(final Project prj, final DoubleDesc doubleDesc) {
        super(prj);
        this.desc = doubleDesc;
        doubleDesc.addObserver(this);
        doubleAtts = new HashMap<Double, DoubleAttribute>();
    }

    /**
     * Gets the double description for attributes maintained by this range.
     *
     * @return the description of values contained in this range
     */
    public DoubleDesc getDesc() {
        return desc;
    }

    /**
     * Returns true, if there is a DoubleAttribute contained in this range which
     * has the specified value.
     *
     * @param value
     *            the double to be checked
     * @return true, if value is contained in this range, false otherwise
     */
    public boolean containsDouble(final Double value) {
        return doubleAtts.containsKey(value);
    }
    
    /**
     * Returns the DoubleAttribute associated with the given double. Creates a new
     * DoubleAttribute if there is no DoubleAttribute for the given double yet.
     *
     * @param value
     *            the value for which the corresponding DoubleAttribute should be
     *            returned
     * @return the DoubleAttribute specified by value
     */
    public DoubleAttribute getDoubleValue(final double value) {
        DoubleAttribute att = doubleAtts.get(value);
        double min = (Double) desc.getMin();
        double max = (Double) desc.getMax();
        if ((att == null) && (value >= min) && (value <= max)) {
            att = new DoubleAttribute(this.desc, value);
            doubleAtts.put(value, att);
        }
        return att;
    }

    /**
     * Gets all values contained in this range.
     *
     * @return Collection of DoubleAttribute objects representing the values
     */
    public Collection<DoubleAttribute> getDoubles() {
        return doubleAtts.values();
    }

    /*
     * (non-Javadoc)
     *
     * @see de.dfki.mycbr.core.casebase.Range#getAttribute(java.lang.Object)
     */
    /**
     * Gets the attribute associated with the specified <code>Object</code> obj.
     * obj is expected to be of type <code>Double</code>, <code>Double</code> or <code>String</code>.
     * Returns result of {@link #getDoubleValue(double)} if obj is of type
     * <code>Double</code>, <code>Double</code> result of {@link Project#getSpecialAttribute(String)} if obj is
     * of type <code>SpecialAttribute</code>, else returns null. Is needed for
     * MultipleRange.
     *
     * @param obj
     *            representing Double or SpecialAttribute
     * @return SimpleAttribute that corresponds to obj, null if there is no such
     *         SimpleAttribute
     */
    public Attribute getAttribute(final Object obj) {
        if (getProject().isSpecialAttribute(obj.toString())) {
            return getProject().getSpecialAttribute(obj.toString());
        } else if (obj instanceof Double) {
            return getDoubleValue((Double) obj);
        } else if (obj instanceof Double) {
            return getDoubleValue((Double) obj);
        } else {
            return parseValue(obj.toString());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.dfki.mycbr.core.casebase.NumberRange#getNumberValue(java.lang.Number)
     */
    /**
     * Returns an attribute representing the given number. Calls
     * {@link #getDoubleValue(double)}.
     *
     * @param value the value of the double attribute to be returned
     * @return the double attribute corresponding to value
     */
    public SimpleAttribute getValue(final double value) {
        return getDoubleValue(value);
    }

    /**
     * deletes all attributes that do no longer fit in the range specified by
     * minimum and maximum value of desc.
     */
    private void cleanData() {
        LinkedList<Double> deleteList = new LinkedList<Double>();
        double min = (Double) desc.getMin();
        double max = (Double) desc.getMax();

        // find values which should be deleted
        for (DoubleAttribute att : doubleAtts.values()) {
        	Double value = att.getValue();
            if ((value < min) || (value > max)) {
                deleteList.add(value);
            }
        }

        // delete values
        for (Double d : deleteList) {
            doubleAtts.remove(d);
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
        return getDoubleValue(Double.parseDouble(string));
    }
}
