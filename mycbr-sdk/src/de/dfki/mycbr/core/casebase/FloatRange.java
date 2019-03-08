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
import de.dfki.mycbr.core.model.FloatDesc;

/**
 * Holds FloatAttributes for a given FloatDescription. Each time a float is used
 * in a case the FloatRange returns a reference to the corresponding
 * FloatAttribute object or creates a new one. SpecialAttributes are also
 * handled for each SpecialAttribute used in the current project
 *
 * @author myCBR Team
 *
 */
public final class FloatRange extends Range {

    /**
     * Gives restrictions (allowed values) to this range and tells us how to
     * compute similarity of attributes of this description.
     */
    private FloatDesc desc;

    /**
     * Associates a <code>FloatAttribute</code> to each float used in a case for
     * the given attribute description.
     */
    private HashMap<Float, FloatAttribute> floatAtts;

    /**
     * Initializes internal data structures according to maximal number of
     * integer attributes (given by {@link FloatDesc#getMax()} -
     * {@link FloatDesc#getMin()}).
     *
     * @param prj the project this range belongs to
     * @param floatDesc
     *            the float description for attributes maintained by this range
     */
    public FloatRange(final Project prj, final FloatDesc floatDesc) {
        super(prj);
        this.desc = floatDesc;
        floatDesc.addObserver(this);
        floatAtts = new HashMap<Float, FloatAttribute>();
    }

    /**
     * Gets the float description for attributes maintained by this range.
     *
     * @return the description of values contained in this range
     */
    public FloatDesc getDesc() {
        return desc;
    }

    /**
     * Returns true, if there is a FloatAttribute contained in this range which
     * has the specified value.
     *
     * @param value
     *            the float to be checked
     * @return true, if value is contained in this range, false otherwise
     */
    public boolean containsFloat(final Float value) {
        return floatAtts.containsKey(value);
    }

    /**
     * Returns the FloatAttribute associated with the given float. Creates a new
     * FloatAttribute if there is no FloatAttribute for the given float yet.
     *
     * @param value
     *            the value for which the corresponding FloatAttribute should be
     *            returned
     * @return the FloatAttribute specified by value
     */
    public FloatAttribute getFloatValue(final float value) {
        FloatAttribute att = floatAtts.get(value);
        float min = (Float) desc.getMin();
        float max = (Float) desc.getMax();
        if ((att == null) && (value >= min) && (value <= max)) {
            att = new FloatAttribute(this.desc, value);
            floatAtts.put(value, att);
        }
        return att;
    }
    
    /**
     * Returns the FloatAttribute associated with the given double. Creates a new
     * FloatAttribute if there is no FloatAttribute for the given double yet.
     *
     * @param dValue
     *            the value for which the corresponding FloatAttribute should be
     *            returned
     * @return the FloatAttribute specified by value
     */
    public FloatAttribute getDoubleValue(final double dValue) {
    	final float value = (float) dValue;
        FloatAttribute att = floatAtts.get(value);
        float min = (Float) desc.getMin();
        float max = (Float) desc.getMax();
        if ((att == null) && (value >= min) && (value <= max)) {
            att = new FloatAttribute(this.desc, value);
            floatAtts.put(value, att);
        }
        return att;
    }

    /**
     * Gets all values contained in this range.
     *
     * @return Collection of FloatAttribute objects representing the values
     */
    public Collection<FloatAttribute> getFloats() {
        return floatAtts.values();
    }

    /*
     * (non-Javadoc)
     *
     * @see de.dfki.mycbr.core.casebase.Range#getAttribute(java.lang.Object)
     */
    /**
     * Gets the attribute associated with the specified <code>Object</code> obj.
     * obj is expected to be of type <code>Float</code>, <code>Double</code> or <code>String</code>.
     * Returns result of {@link #getFloatValue(float)} if obj is of type
     * <code>Float</code>, <code>Double</code> result of {@link Project#getSpecialAttribute(String)} if obj is
     * of type <code>SpecialAttribute</code>, else returns null. Is needed for
     * MultipleRange.
     *
     * @param obj
     *            representing Float or SpecialAttribute
     * @return SimpleAttribute that corresponds to obj, null if there is no such
     *         SimpleAttribute
     */
    public Attribute getAttribute(final Object obj) {
        if (getProject().isSpecialAttribute(obj.toString())) {
            return getProject().getSpecialAttribute(obj.toString());
        } else if (obj instanceof Float) {
            return getFloatValue((Float) obj);
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
     * {@link #getFloatValue(float)}.
     *
     * @param value the value of the float attribute to be returned
     * @return the float attribute corresponding to value
     */
    public SimpleAttribute getValue(final float value) {
        return getFloatValue(value);
    }

    /**
     * deletes all attributes that do no longer fit in the range specified by
     * minimum and maximum value of desc.
     */
    private void cleanData() {
        LinkedList<Float> deleteList = new LinkedList<Float>();
        float min = (Float) desc.getMin();
        float max = (Float) desc.getMax();

        // find values which should be deleted
        for (FloatAttribute att : floatAtts.values()) {
            Float value = att.getValue();
            if ((value < min) || (value > max)) {
                deleteList.add(value);
            }
        }

        // delete values
        for (Float f : deleteList) {
            floatAtts.remove(f);
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
        return getFloatValue(Float.parseFloat(string));
    }
}
