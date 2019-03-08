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

import de.dfki.mycbr.core.explanation.Explainable;
import de.dfki.mycbr.core.explanation.IExplainable;
import de.dfki.mycbr.core.model.AttributeDesc;

/**
 * Represents values occurring in query and or cases. Each attribute knows its
 * description and therefore knows how similar it is to other attributes of the
 * same description. Attributes for a fixed description are maintained by ranges
 * (classes implementing interface {@link Range}).
 *
 * @author myCBR Team
 *
 */
public abstract class SimpleAttribute extends Attribute implements
                                                             IExplainable {

    /**
     * Gives restrictions to values for this attribute and tells us how to
     * compute similarity of this attribute and other attributes of this
     * description.
     */
    private AttributeDesc desc;

    /**
     * Creates new value for the specified description.
     *
     * @param d
     *            description of this attribute
     */
    SimpleAttribute(final AttributeDesc d) {
        this.desc = d;
    }

    /**
     * Gets the description for this attribute to determine similarity functions
     * or restrictions for the value of this attribute.
     *
     * @return the description of this attribute
     */
    public final AttributeDesc getAttributeDesc() {
        return getDesc();
    }

    @Override
    public final String getName() {
        return getValueAsString();
    }

    /**
     * @return the type of this explainable object
     */
    public final Explainable getExpType() {
        return Explainable.SimpleAttribute;
    }

    /**
     * @param d the desc to set
     */
    public final void setDesc(final AttributeDesc d) {
        this.desc = d;
        setChanged();
        notifyObservers();
    }

    /**
     * @return the desc
     */
    public final AttributeDesc getDesc() {
        return desc;
    }
}
