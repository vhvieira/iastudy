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

import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Attribute;
import de.dfki.mycbr.core.casebase.FloatAttribute;
import de.dfki.mycbr.core.casebase.FloatRange;
import de.dfki.mycbr.core.casebase.MultipleAttribute;
import de.dfki.mycbr.core.casebase.SimpleAttribute;
import de.dfki.mycbr.core.similarity.AdvancedFloatFct;
import de.dfki.mycbr.core.similarity.FloatFct;
import de.dfki.mycbr.core.similarity.ISimFct;

/**
 * Description for Number attributes. Restricts the values that can be used as
 * attributes of this description by minValue and maxValue
 * 
 * @author myCBR Team
 */
public class FloatDesc extends SimpleAttDesc {

	/**
	 * Minimal value which can be used as attribute specified for this
	 * description
	 */
	private float min = 0;

	/**
	 * Maximal value which can be used as attribute specified for this
	 * description
	 */
	private float max = 0;

	private FloatRange range;

	/**
	 * Initializes this with the given name. The name should be unique within
	 * the attributes of the c description containing this description.
	 * Creates a new range for this description.
	 * 
	 * @throws Exception
	 * 
	 */
	public FloatDesc(Concept owner, String name, float min, float max) throws Exception {
		super(owner, name);
		if (min > max) {
			throw new Exception("min has to be less than or equal max");
		}
		this.min = min;
		this.max = max;
		this.range = new FloatRange(owner.getProject(), this);

		super.range = range;
		if (owner != null && owner != owner.getProject()) {
			owner.addAttributeDesc(this);
		}
		addDefaultFct();
	}

	/**
	 * Returns <code>NumberAttribute</code> object representing the specified
	 * value.
	 * 
	 * @param value
	 *            the Number representing the value that should be returned
	 * @return value representing the specified float.
	 */
	public SimpleAttribute getNumberAttribute(float value) {
		return range.getValue(value);
	}

	/**
	 * Sets the minimal value which may appear as Number specified for this
	 * description to min. Does nothing if min >= max.
	 * 
	 * @param min
	 *            minimal value appearing as Number for this description
	 */
	public void setMin(float min) {
		if (this.min!=min && min < max) {
			this.min = min;
			owner.getProject().cleanInstances(owner, this);
			setChanged();
			notifyObservers();
		}
	}

	/**
	 * Gets the minimal Number that can be used as value for this description
	 * 
	 * @return minimal value for attributes of this description
	 */
	public float getMin() {
		return min;
	}

	/**
	 * Sets the max value which may appear as Number specified for this
	 * description to max.
	 * Does nothing if max <=min
	 * 
	 * @param max
	 *            max value appearing as Number for this description
	 */
	public void setMax(float max) {
		if (this.max!=max && max > min) {
			this.max = max;
			owner.getProject().cleanInstances(owner, this);
			setChanged();
			notifyObservers();
		}
	}
	
	/**
	 * Gets the maximal Number that can be used as value for this description
	 * 
	 * @return maximal value for attributes of this description
	 */
	public float getMax() {
		return max;
	}

	/**
	 * Gets the range maintaining the number attributes of this description
	 * 
	 * @return the number range of this description
	 */
	public FloatRange getRange() {
		return range;
	}

	/**
	 * Creates a new NumberFct for the given description.
	 * 
	 * @param name
	 *            the description for which a new function should be created
	 * @return the new NumberFct for description desc
	 */
	public FloatFct addFloatFct(
			String name, boolean active) {
		FloatFct f = new FloatFct(owner.getProject(),this, name);
		addFunction(f, active);

		return f;
	}
	
	/**
	 * Creates a new AdvancedNumberFct for the given description.
	 * 
	 * @param name
	 *            the description for which a new function should be created
	 * @return the new AdvancedNumberFct for description desc
	 */
	public AdvancedFloatFct addAdvancedFloatFct(
			String name, boolean active) {
		AdvancedFloatFct f = new AdvancedFloatFct(owner.getProject(), this, name);
		addFunction(f, active);

		return f;
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
		if (desc instanceof FloatDesc) {
			FloatDesc descOLD = (FloatDesc) desc;
			if ((descOLD.getMin() <= this.getMin())
					&& (descOLD.getMax() >= this.getMax())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param att
	 * @return true, if the given att fits this
	 */
	public boolean fits(Attribute att) {
		if (!super.fits(att)) {
			return false;
		}
		if (att instanceof FloatAttribute) {
			return check((FloatAttribute)att);
		} else if (att instanceof MultipleAttribute<?>) {
			MultipleAttribute<?> ma = (MultipleAttribute<?>)att;
			for (Attribute a: ma.getValues()) {
				if (!(a instanceof FloatAttribute) || !check((FloatAttribute)a) ) {
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
		if (isMultiple && !(att instanceof MultipleAttribute<?>)) {
			if (att instanceof FloatAttribute) {
				return check((FloatAttribute)att);
			} else {
				return false;
			}
		} else {
			return fits(att);
		}
	}
	
	private boolean check(FloatAttribute i) {
		if (i.getValue()< min 
			    || i.getValue() > max) {
				return false;
		} 
		return true;
	}

	/* (non-Javadoc)
	 * @see de.dfki.mycbr.core.model.AttributeDesc#addDefaultFct()
	 */
	@Override
	void addDefaultFct() {
		ISimFct activeSim = addFloatFct(Project.DEFAULT_FCT_NAME, false);
		updateAmalgamationFcts(owner, activeSim);
	}
}
