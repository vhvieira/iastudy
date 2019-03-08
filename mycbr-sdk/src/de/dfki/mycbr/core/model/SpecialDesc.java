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

import java.util.HashSet;

import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Attribute;
import de.dfki.mycbr.core.casebase.SpecialAttribute;
import de.dfki.mycbr.core.casebase.SpecialRange;
import de.dfki.mycbr.core.similarity.SpecialFct;

/**
 * Special values are values that can be used for any
 * attribute description. Here you can define the allowed 
 * values of the special value description of this project.
 * @author myCBR Team
 *
 */
public class SpecialDesc extends SymbolDesc {

    /**
     * For initializing special value desc.
     *
     * @param p
     *            the project this description belongs to
     * @param allowedValues
     *            the values which can be used for attributes of this
     *            description
     * @throws Exception
     *             if something goes wrong during creation of special value
     *             function
     */
    public SpecialDesc(final Project p,
            final HashSet<String> allowedValues)
            throws Exception {
        super(p, "specialValueDesc", allowedValues);
        range = new SpecialRange(p, this, allowedValues);
    }

    public static SpecialDesc getDefault() {
    	
        HashSet<String> svs = new HashSet<String>();
        svs.add(Project.UNDEFINED_SPECIAL_ATTRIBUTE);
        svs.add(Project.UNKNOWN_SPECIAL_VALUE);
        svs.add(Project.NO_SPECIAL_VALUE);
    	try {
    		return new SpecialDesc(null, svs);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return null;
    }
    
    /**
     * Returns <code>SymbolAttribute</code> object representing the specified
     * value.
     *
     * @param value
     *            the string representing the value that should be returned
     * @return value representing the specified string, null if there is none.
     */
    public final Attribute getAttribute(final String value) {
        if (owner.getProject().isSpecialAttribute(value)) {
            return ((SpecialRange) range).getAttribute(value);
        }
        return null;
    }

    /**
     *
     * @param att the attribute whose index should be returned
     * @return the index of the given attribute or null if it does not belong
     * to this
     */
    public final Integer getIndexOf(final SpecialAttribute att) {
        return ((SpecialRange) range).getIndexOf(att);
    }

    /**
     * Removes the given string from the list of allowed symbols.
     *
     * @param value the value which should be removed as allowed value of this
     */
    public final void removeSymbol(final String value) {
        if (!value.equals(Project.UNDEFINED_SPECIAL_ATTRIBUTE)) {
            super.removeSymbol(value);
        }
    }
    
    /**
     * Creates a new SymbolFct for the given description.
     *
     * @param name the name of the new function
     * @param active if true, the new function will be used in all amalgamation
     * functions known for the owner of this.
     * @return the new symbol function
     */
    public final SpecialFct addSpecialFct(final String name,
            final boolean active) {
        SpecialFct f = new SpecialFct(owner.getProject(), this, name);
        addFunction(f, active);
        return f;
    }
}
