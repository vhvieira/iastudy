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
import de.dfki.mycbr.core.casebase.IntervalAttribute;
import de.dfki.mycbr.core.casebase.IntervalRange;
import de.dfki.mycbr.core.casebase.MultipleAttribute;
import de.dfki.mycbr.core.casebase.SpecialAttribute;
import de.dfki.mycbr.core.similarity.ISimFct;
import de.dfki.mycbr.core.similarity.IntervalFct;
import de.dfki.mycbr.util.Pair;

/**
 * The allow computation of similarity between intervals there must be a minimal
 * and a maximal value that restrict the possible interval bounds.
 *
 * @author myCBR Team
 */
public class IntervalDesc extends SimpleAttDesc {

    /**
     * The range managing the attributes of this.
     */
    private IntervalRange range;

    /**
     * Minimal value which may appear in an interval specified for this
     * description.
     */
    private Number min;

    /**
     * Minimal value which may appear in an interval specified for this
     * description.
     */
    private Number max;

    /**
     * initializes this with the given name. The name should be unique within
     * the attributes of the c description containing this interval description.
     * Creates a new range for this description.
     *
     * @param owner the owner of this
     * @param name
     *            the name to be used for this description.
     * @param minValue number specifying the lower bound for attribute values
     * for this
     * @param maxValue number specifying the upper bound for attribute values
     * for this
     *
     * @throws Exception if min is not less than or equal to max
     */
    public IntervalDesc(final Concept owner, final String name,
            final Number minValue, final Number maxValue)
            throws Exception {
        super(owner, name);
        if (minValue.doubleValue() > maxValue.doubleValue()) {
            throw new Exception("min has to be less than or equal max");
        }
        this.min = minValue;
        this.max = maxValue;
        range = new IntervalRange(owner.getProject(), this);
        super.range = range;
        if (owner != null && owner != owner.getProject()) {
            owner.addAttributeDesc(this);
        }
        addDefaultFct();
    }

    /**
     * Returns <code>IntervalAttribute</code> object representing the specified
     * interval.
     *
     * @param minValue
     *            lower bound of the interval
     * @param maxValue
     *            upper bound of the interval
     *
     * @return value representing the specified interval.
     */
    public final IntervalAttribute getIntervalAttribute(final Number minValue,
            final Number maxValue) {
        return range.getIntervalValue(
                                 new Pair<Number, Number>(minValue, maxValue));
    }

    /**
     * Returns the minimal value which may appear in an interval specified for
     * this description.
     *
     * @return minimal value appearing in interval for this attribute
     */
    public final Number getMin() {
        return this.min;
    }

    /**
     * Sets the minimal value which may appear in an interval specified for this
     * description to min. Does nothing if min >= max
     *
     * @param minValue
     *            minimal value appearing in interval for this attribute
     */
    public final void setMin(final Number minValue) {
        if (this.min != minValue
                && minValue.doubleValue() < max.doubleValue()) {
            this.min = minValue;
            owner.getProject().cleanInstances(owner, this);
            setChanged();
            notifyObservers();
        }
    }

    /**
     * Returns the maximal value which may appear in an interval specified for
     * this description.
     *
     * @return maximal value appearing in interval for this attribute
     */
    public final Number getMax() {
        return this.max;
    }

    /**
     * Sets the maximal value which may appear in an interval specified for this
     * description. Does nothing if max <= min.
     *
     * @param maxValue
     *            maximal value appearing in interval for this attribute
     */
    public final void setMax(final Number maxValue) {
        if (this.max != maxValue
                && maxValue.doubleValue() > min.doubleValue()) {
            this.max = maxValue;
            owner.getProject().cleanInstances(owner, this);
            setChanged();
            notifyObservers();
        }
    }

    /**
     * Creates a new SymbolFct for the given description.
     *
     * @param name the name of the new function
     * @param active if true, the new function is used in the active
     * amalgamation of the owner of this
     * @return the Similarity.getFunction for description desc
     */
    public final IntervalFct addIntervalFct(final String name,
            final boolean active) {
        IntervalFct f = new IntervalFct(owner.getProject(), this, name);
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
    /**
     * @param desc the description which should be overridden by this
     * @return true, if this can override the given description, false otherwise
     */
    public final boolean canOverride(final AttributeDesc desc) {
        if (desc instanceof IntervalDesc) {
            IntervalDesc descOLD = (IntervalDesc) desc;
            if ((descOLD.getMin().doubleValue() <= this.getMin().doubleValue())
                    && (descOLD.getMax().doubleValue() >= this.getMax()
                            .doubleValue())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param att the attribute which should be checked for fitting this
     * @return true, if it is has a valid interval value or a set of valid
     * interval values (multiple attribute)
     */
    public final boolean fits(final Attribute att) {
        if (!super.fits(att)) {
            return false;
        }
        if (att instanceof IntervalAttribute) {
            return check((IntervalAttribute) att);
        } else if (att instanceof MultipleAttribute<?>) {
            MultipleAttribute<?> ma = (MultipleAttribute<?>) att;
            for (Attribute a : ma.getValues()) {
                if (!(a instanceof IntervalAttribute)
                        || !check((IntervalAttribute) a)) {
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
	        if (att instanceof IntervalAttribute) {
	            return check((IntervalAttribute) att);
	        } else {
	        	return false;
	        }
    	} else {
    		return fits(att);
    	}
    }
    
    /**
     *
     * @param i the interval attribute to be checked for allowance
     * @return true, if the given attribute is a correct interval fulfilling the
     *  restrictions of this
     */
    private boolean check(final IntervalAttribute i) {
        if (i.getInterval().getFirst().doubleValue() < min.doubleValue()
                || i.getInterval().getSecond().doubleValue() > max
                        .doubleValue()) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.dfki.mycbr.core.model.AttributeDesc#addDefaultFct()
     */
    /**
     * Adds the default function to this.
     */
    final void addDefaultFct() {
        if (owner != null && owner != owner.getProject()) {
            ISimFct activeSim = addIntervalFct(Project.DEFAULT_FCT_NAME, false);
            updateAmalgamationFcts(owner, activeSim);
        }
    }
}
