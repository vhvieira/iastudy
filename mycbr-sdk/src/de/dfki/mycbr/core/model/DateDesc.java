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

package de.dfki.mycbr.core.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Attribute;
import de.dfki.mycbr.core.casebase.DateAttribute;
import de.dfki.mycbr.core.casebase.DateRange;
import de.dfki.mycbr.core.casebase.MultipleAttribute;
import de.dfki.mycbr.core.casebase.SpecialAttribute;
import de.dfki.mycbr.core.similarity.DateFct;
import de.dfki.mycbr.core.similarity.ISimFct;

/**
 * Description for date attributes. Restricts the values that can be used as
 * attributes of this description by minValue and maxValue and holds a format
 * which is used for these values
 * 
 * @author myCBR Team
 */
public class DateDesc extends SimpleAttDesc {

	/**
	 * Minimal value which can be used as attribute specified for this
	 * description
	 */
	private Date minDate;

	/**
	 * Maximal value which can be used as attribute specified for this
	 * description
	 */
	private Date maxDate;

	private DateRange range;

	/**
	 * Specifies the format for values of this description
	 */
	private SimpleDateFormat format;

	/**
	 * Initializes this with the given name, minimal/ maximal value and format.
	 * The name should be unique within the attributes of the c
	 * description containing this description. Creates a new range for this
	 * description.
	 * 
	 * @param name
	 *            the name to be used for this description.
	 * @throws Exception
	 */
	public DateDesc(Concept owner, String name, Date minDate, Date maxDate, SimpleDateFormat format)
			throws Exception {
		super(owner, name);
		if (minDate.after(maxDate)) {
			throw new Exception("min has to be before max date");
		}
		this.format = format;
		this.minDate = minDate;
		this.maxDate = maxDate;
		range = new DateRange(owner.getProject(), this);
		super.range = range;
		if (owner != null && owner != owner.getProject()) {
			owner.addAttributeDesc(this);
		}
		addDefaultFct();
	}

	/**
	 * Returns <code>DateAttribute</code> object representing the specified
	 * value.
	 * 
	 * @param value
	 *            the date representing the value that should be returned
	 * @return value representing the specified date.
	 */
	public DateAttribute getDateAttribute(Date value) {
		return range.getDateValue(value);
	}

	/**
	 * Sets the minimal date which can be used as value for this description.
	 * Does nothing if minDate is already the minimal date or
	 * if it is not before maxDate
	 * 
	 * @param minDate
	 *            the minDate to set
	 */
	public void setMinDate(Date minDate) {
		if (!this.minDate.equals(minDate) && minDate.before(maxDate)) {
			this.minDate = minDate;
			owner.getProject().cleanInstances(owner, this);
			setChanged();
			notifyObservers();
		}
	}

	/**
	 * Returns the minimal date which can be used as value for this description.
	 * 
	 * @return the minDate
	 */
	public Date getMinDate() {
		return minDate;
	}

	/**
	 * Sets the maximal date which can be used as value for this description.
	 * Does nothing if maxDate is already the maximal date or
	 * if it is not before maxDate.
	 * 
	 * @param maxDate
	 *            the maxDate to set
	 */
	public void setMaxDate(Date maxDate) {
		if (!this.maxDate.equals(maxDate) && (maxDate.after(minDate))) {
			this.maxDate = maxDate;
			owner.getProject().cleanInstances(owner, this);
			setChanged();
			notifyObservers();
		}
	}

	/**
	 * Gets the maximal date which can be used as value for this description.
	 * 
	 * @return the maxDate
	 */
	public Date getMaxDate() {
		return maxDate;
	}

	/**
	 * Gets the values' format
	 * 
	 * @return the format
	 */
	public SimpleDateFormat getFormat() {
		return format;
	}

	/**
	 * Sets the values' format
	 * @param format the new format
	 */
	public void setFormat(SimpleDateFormat format) {
		this.format = format; // TODO update attributes
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Creates a new SymbolFct for the given description.
	 * 
	 * @param name
	 *            the description for which a new function should be created
	 * @return the Similarity.getFunction for description desc
	 */
	public DateFct addDateFct(String name, boolean active, DateFct.DateFunctionPrecision adjust) {
		DateFct f = new DateFct(owner.getProject(), this, name, adjust);
		addFunction(f, active);

		return f;
	}

    public DateFct addDateFct(String name, boolean active) {
        return addDateFct(name, active, DateFct.DateFunctionPrecision.Year);
    }
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.dfki.mycbr.core.model.AttributeDesc#canOverride(de.dfki.mycbr.core
	 * .model.AttributeDesc)
	 */
	@Override
	public boolean canOverride(AttributeDesc desc) {

		if (desc instanceof DateDesc) {
			DateDesc descOLD = (DateDesc) desc;
			if (descOLD.getMinDate().before(this.getMinDate())
					|| (descOLD.getMinDate().equals(this.getMinDate()))
					&& (descOLD.getMaxDate().after(this.getMaxDate()) || descOLD
							.getMaxDate().equals(this.getMaxDate()))) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param att
	 * @return true, if the given attribute fits this
	 */
	public boolean fits(Attribute att) {
		if (!super.fits(att)) {
			return false;
		}
		if (att instanceof DateAttribute) {
			return check((DateAttribute)att);
		} else if (att instanceof MultipleAttribute<?>) {
			MultipleAttribute<?> ma = (MultipleAttribute<?>)att;
			for (Attribute a: ma.getValues()) {
				if (!(a instanceof DateAttribute) || !check((DateAttribute)a) ) {
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean fitsSingle(Attribute att) { 
		if (!super.fitsSingle(att)) {
			return false;
		}
		if (isMultiple && !(att instanceof SpecialAttribute)) {
			if (att instanceof DateAttribute) {
				return check((DateAttribute)att);
			} else {
				return false;
			}
		} else {
			return fits(att);
		}
	}
	
	private boolean check(DateAttribute a) {
		if ( a.getDate().before(minDate)
			|| a.getDate().after(maxDate)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see de.dfki.mycbr.core.model.AttributeDesc#addDefaultFct()
	 */
	@Override
	void addDefaultFct() {
		
		if (owner != null && owner != owner.getProject()) {
			ISimFct activeSim = addDateFct(Project.DEFAULT_FCT_NAME, false, DateFct.DateFunctionPrecision.Day);
			updateAmalgamationFcts(owner, activeSim);
		}
	}
}
