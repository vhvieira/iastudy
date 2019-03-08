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
import de.dfki.mycbr.core.casebase.StringAttribute;
import de.dfki.mycbr.core.casebase.StringRange;
import de.dfki.mycbr.core.similarity.ISimFct;
import de.dfki.mycbr.core.similarity.StringFct;
import de.dfki.mycbr.core.similarity.config.StringConfig;

/**
 * Description for string attributes. This description does not define
 * restrictions for its values, since all strings are allowed.
 *
 * @author myCBR Team
 */
public class StringDesc extends SimpleAttDesc {

    /**
     * the range managing the attributes for this.
     */
    private StringRange range;

    /**
     * Initializes this with the given name.
     *
     * @param owner the owner of this description
     * @param name the name of this description
     * @throws Exception if the owner already has a description for this name
     */
    public StringDesc(final Concept owner, final String name) throws Exception {
        super(owner, name);
        range = new StringRange(owner.getProject(), this);
        super.range = range;
        if (owner != null && owner != owner.getProject()) {
            owner.addAttributeDesc(this);
        }
        addDefaultFct();
    }

    /**
     * Returns <code>StringAttribute</code> object representing the specified
     * string.
     *
     * @param string
     *            the string representing the value that should be returned
     * @return value representing the specified string.
     */
    public final StringAttribute getStringAttribute(final String string) {
        return range.getStringValue(string);
    }

    /**
     * Creates a new StringFct for the given description.
     *
     * @param config the configuration for the function to be created
     * @param name the name of the new function
     * @param active if true, the new function will be used in all amalgamation
     *  functions known for the owner of this
     * @return the new StringFct for this
     */
    public final StringFct addStringFct(final StringConfig config,
            final String name, final boolean active) {
        StringFct f = new StringFct(owner.getProject(), config, this, name);
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
     * Checks whether this can override the given description.
     * @param desc the description to be overridden.
     * @return true, if the given description can be overridden by this, false
     * otherwise
     */
    public final boolean canOverride(final AttributeDesc desc) {
        return desc instanceof StringDesc;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.dfki.mycbr.core.model.AttributeDesc#addDefaultFct()
     */
    /**
     * Adds a default function for this.
     */
    final void addDefaultFct() {
        ISimFct activeSim = addStringFct(StringConfig.EQUALITY,
                Project.DEFAULT_FCT_NAME, false);
        updateAmalgamationFcts(owner, activeSim);
    }

}
