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

import de.dfki.mycbr.core.casebase.Attribute;
import de.dfki.mycbr.core.casebase.SimpleAttribute;
import de.dfki.mycbr.core.casebase.SymbolAttribute;
import de.dfki.mycbr.core.similarity.SymbolFct;

/**
 * Description for boolean attributes. This description does not define
 * restrictions for its values, since there is only one attribute for true and
 * one attribute representing false. BooleanDesc extends SymbolDesc in order to
 * use the table function defined for symbol attributes for boolean similarity
 * computations.
 * 
 * @author myCBR Team
 */
public class BooleanDesc extends SymbolDesc {
	
	private SymbolAttribute symbolTrue;
	private SymbolAttribute symbolFalse;
	
	/**
	 * Initializes this with the given name. The name should be unique within
	 * the attributes of the c description containing this description.
	 * Creates a new range for this description.
	 * @throws Exception 
	 * 
	 */
	public BooleanDesc(Concept owner, String name) throws Exception {
		super(owner, name, new HashSet<String>());
		//range = new BooleanRange(owner.getProject(), this);
		symbolTrue = new SymbolAttribute(this, "true");
		symbolFalse = new SymbolAttribute(this, "false");
		
	}

	/**
	 * Returns <code>BooleanAttribute</code> object representing the specified
	 * value.
	 * 
	 * @param value
	 *            the boolean representing the value that should be returned
	 * @return value representing the specified boolean.
	 */
	public SymbolAttribute getBooleanAttribute(Boolean value) {
		if (new Boolean("true").equals(value)) {
			return symbolTrue;
		} else if (new Boolean("false").equals(value)) {
			return symbolFalse;
		} else {
			return null;
		}
	}

	/**
	 * Creates a new SymbolFct for the given description.
	 * 
	 * @param name
	 *            the name of the description for which a new function should be created
	 * @return the new SymbolFct for description desc
	 */
	public SymbolFct addBooleanFct(String name, boolean active) {
		SymbolFct f = new SymbolFct(owner.getProject(), this, name);
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
		return desc instanceof BooleanDesc;
	}
	
	public boolean isAllowedValue(String v) {
		return ("true".equals(v) || "false".equals(v));
	}
	
	/**
     * Returns <code>SymbolAttribute</code> object representing the specified
     * value.
     *
     * @param value
     *            the string representing the value that should be returned
     * @return value representing the specified string, null if there is none.
     */
    public Attribute getAttribute(final Object value) {
    	
    	if (value == null) {
    		return null;
    	} else if (owner.getProject().isSpecialAttribute(value.toString())) {
            return owner.getProject().getSpecialAttribute(value.toString());
        } else {
            return getBooleanAttribute(Boolean.parseBoolean(value.toString()));
        }
        
    }
    
    public final Integer getIndexOf(final SimpleAttribute att) {
    	if (symbolTrue.equals(att)) {
    		return 0;
    	} else if (symbolFalse.equals(att)) {
    		return 1;
    	} else {
    		return null;
    	}
    }
    
    /**
     * Does nothing because you cannot delete a boolean value
     *
     * @param value the value of the symbol attribute to be removed
     */
    public void removeSymbol(final String value) {
    	
    }
    
    /**
     * Does nothing because you cannot rename a boolean value.
     * @param oldValue the old value
     * @param newValue the new value
     */
    public final boolean renameValue(final String oldValue,
            final String newValue) {
	        return false;
    }
    
    /**
     * Does nothing because you cannot add other values than true and false
     * to a boolean description
     * @param value the value to be added to this description
     * @return null 
     */
    public final SymbolAttribute addSymbol(final String value) {
        return null;
    }
}
