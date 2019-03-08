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

import java.util.Date;

import de.dfki.mycbr.core.model.DateDesc;

/**
 * Represents date attributes. The format of this date attribute is given by the
 * range that maintains this attribute.
 *
 * @author myCBR Team
 *
 */
public final class DateAttribute extends SimpleAttribute {

    /**
     *
     */
    private Date date;

    /**
     * Initializes this.
     *
     * @param desc
     *            the date description of this attribute
     * @param d
     *            the value of this attribute
     */
    DateAttribute(final DateDesc desc, final Date d) {
        super(desc);
        this.date = d;
    }

    /**
     * Returns the value of this attribute.
     *
     * @return the date representing the value
     */
    public Date getDate() {
        return this.date;
    }

    /**
     * Returns a string representation of this date.
     * @return the string representation of this
     */
    @Override
    public String getValueAsString() {
        return getDate().toString();
    }

}
