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
import java.util.LinkedList;
import java.util.Observable;

import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.model.IntervalDesc;
import de.dfki.mycbr.util.Pair;

/**
 * Holds IntervalAttributes for a given IntervalDesc. Each time an interval is
 * used in a case the IntervalRange returns a reference to the corresponding
 * IntervalAttribute object or creates a new one. SpecialAttribute are
 * also handled for each SpecialValue used in the current project
 *
 * @author myCBR Team
 *
 */
public final class IntervalRange extends Range {

    /**
     * Gives restrictions (allowed values) to this range and tells us how to
     * compute similarity of attributes of this description.
     */
    private IntervalDesc desc;

    /**
     * Associates a <code>IntervalAttribute</code> to each interval used in a
     * case for the given attribute description.
     */
    private HashMap<Pair<Number, Number>, IntervalAttribute> atts;

    /**
     * Initializes internal data structures.
     *
     * @param prj the project this range belongs to
     * @param d
     *            the interval description for attributes maintained by this
     *            range
     */
    public IntervalRange(final Project prj, final IntervalDesc d) {
        super(prj);
        this.desc = d;
        desc.addObserver(this);
        atts = new HashMap<Pair<Number, Number>, IntervalAttribute>();
    }

    /**
     * Returns the IntervalAttribute associated with the given interval. Creates
     * a new IntervalAttribute if there is no IntervalAttribute for the given
     * interval yet.
     *
     * @param interval
     *            the value for which the corresponding IntervalAttribute should
     *            be returned
     * @return the IntervalAttribute specified by interval
     */
    public IntervalAttribute getIntervalValue(final
                                              Pair<Number, Number> interval) {
        IntervalAttribute att = atts.get(interval);
        Number min = (Number) desc.getMin();
        Number max = (Number) desc.getMax();
        if ((att == null)
                && (interval.getFirst().doubleValue() >= min.doubleValue())
                && (interval.getSecond().doubleValue() <= max.doubleValue())) {
            att = new IntervalAttribute(this.desc, interval);
            atts.put(interval, att);
        }
        return att;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.dfki.mycbr.core.casebase.Range#getAttribute(java.lang.Object)
     */
    /**
     * Gets the attribute associated with the specified <code>Object</code> obj.
     * obj is expected to be of type <code>Interval</code> or
     * <code>String</code>. Returns result of {@link #getIntervalValue(Pair)} if
     * obj is of type <code>Interval</code>, result of
     * {@link Project#getSpecialAttribute(String)} if obj is of type
     * <code>SpecialAttribute</code>, else returns null.
     *
     * @param obj
     *            representing Interval or SpecialAttribute
     * @return SimpleAttribute that corresponds to obj, null if there is no such
     *         SimpleAttribute
     */
    @SuppressWarnings("unchecked")
    public Attribute getAttribute(final Object obj) {
        if (getProject().isSpecialAttribute(obj.toString())) {
            return getProject().getSpecialAttribute(obj.toString());
        } else if (obj instanceof Pair<?, ?>) {
            return getIntervalValue((Pair<Number, Number>) obj);
        } else {
            return parseValue(obj.toString());
        }
    }

    /**
     * deletes all attributes that do no longer fit in the range specified by
     * minimum and maximum value of desc.
     */
    private void cleanData() {
        LinkedList<Pair<Number, Number>> deleteList
                                   = new LinkedList<Pair<Number, Number>>();
        double min = desc.getMin().doubleValue();
        double max = desc.getMax().doubleValue();

        // find values which should be deleted
        for (IntervalAttribute att : atts.values()) {
            double lower = att.getInterval().getFirst().doubleValue();
            double upper = att.getInterval().getSecond().doubleValue();

            if ((lower < min) || (upper > max)) {
                deleteList.add(att.getInterval());
            }
        }

        // delete values
        for (Pair<Number, Number> i : deleteList) {
            atts.remove(i);
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
        if (string.startsWith("(") && string.endsWith(")")) {
            String tmp = string.substring(1, string.length() - 1);
            String[] pair = tmp.split(",");
            if (pair.length == 2) {
                return getIntervalValue(new Pair<Number, Number>(Float
                        .parseFloat(pair[0]), Float.parseFloat(pair[1])));
            }
        }
        return null;
    }

}
