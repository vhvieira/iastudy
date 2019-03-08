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

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Observable;

import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.model.DateDesc;

/**
 * Holds DateAttributes for a given DateDescr. Each time a date is used in a
 * case the DateRange returns a reference to the corresponding DateAttribute
 * object or creates a new one. SpecialAttributes are also handled for each
 * SpecialAttribute used in the current project
 *
 * @author myCBR Team
 *
 */
public final class DateRange extends Range {

    /**
     * Gives restrictions (allowed values) to this range and tells us how to
     * compute similarity of attributes of this description.
     */
    private DateDesc desc;

    /**
     * Associates a <code>DateAttribute</code> to each date used in a case for
     * the given attribute description.
     */
    private HashMap<Date, DateAttribute> atts;

    /**
     * Initializes internal data structures.
     *
     * @param p the project this range belongs to
     * @param d
     *            the date description for attributes maintained by this range
     */
    public DateRange(final Project p, final DateDesc d) {
        super(p);
        this.desc = d;
        desc.addObserver(this);
        atts = new HashMap<Date, DateAttribute>();
    }

    /**
     * Returns the DateAttribute associated with the given date. Creates a new
     * DateAttribute if there is no DateAttribute for the given date yet.
     *
     * @param date
     *            the value for which the corresponding DateAttribute should be
     *            returned
     * @return the DateAttribute specified by date
     */
    public DateAttribute getDateValue(final Date date) {
        DateAttribute att = atts.get(date);

        Date min = desc.getMinDate();
        Date max = desc.getMaxDate();

        if ((att == null)
                && ((date.after(min) || date.equals(min))
                        && (date.before(max) || date.equals(max)))) {
            att = new DateAttribute(this.desc, date);
            atts.put(date, att);
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
     * obj is expected to be of type <code>Date</code> or <code>String</code>.
     * Returns result of {@link #getDateValue(Date)} if obj is of type
     * <code>Date</code>, result of {@link Project#getSpecialAttribute(String)} if obj is
     * of type <code>SpecialAttribute</code>, else returns null. Is needed for
     * MultipleRange.
     *
     * @param obj
     *            representing Date or SpecialAttribute
     * @return SimpleAttribute that corresponds to obj, null if there is no such
     *         SimpleAttribute
     * @throws ParseException if obj does not fit the description
     */
    public Attribute getAttribute(final Object obj) throws ParseException {
        if (getProject().isSpecialAttribute(obj.toString())) {
            return getProject().getSpecialAttribute(obj.toString());
        } else if (obj instanceof Date) {
            return getDateValue((Date) obj);
        } else {
            return parseValue(obj.toString());
        }
    }

    /**
     * @param string the string to be parsed
     * @return the attribute associated with string
     * @throws ParseException if string does not fit description
     */
    public Attribute parseValue(final String string) throws ParseException {
        Date d = desc.getFormat().parse(string);
        return getDateValue(d);
    }

    /**
     * deletes all attributes that do no longer fit in the range specified by
     * minimum and maximum value of desc.
     */
    private void cleanData() {
        LinkedList<Date> deleteList = new LinkedList<Date>();
        Date min = desc.getMinDate();
        Date max = desc.getMaxDate();

        // find values which should be deleted
        for (DateAttribute att : atts.values()) {
            if (att.getDate().before(min) || att.getDate().after(max)) {
                deleteList.add(att.getDate());
            }
        }

        // delete values
        for (Date d : deleteList) {
            atts.remove(d);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
    /**
     * @param ob the object which has been changed
     * @param o additional information
     */
    public void update(final Observable ob, final Object o) {
        if (desc.equals(ob)) {
            cleanData();
        }
    }
}
