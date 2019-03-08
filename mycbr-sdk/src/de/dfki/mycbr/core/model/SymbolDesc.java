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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Attribute;
import de.dfki.mycbr.core.casebase.MultipleAttribute;
import de.dfki.mycbr.core.casebase.SimpleAttribute;
import de.dfki.mycbr.core.casebase.SpecialAttribute;
import de.dfki.mycbr.core.casebase.SymbolAttribute;
import de.dfki.mycbr.core.casebase.SymbolRange;
import de.dfki.mycbr.core.similarity.ISimFct;
import de.dfki.mycbr.core.similarity.OrderedSymbolFct;
import de.dfki.mycbr.core.similarity.SymbolFct;
import de.dfki.mycbr.core.similarity.TaxonomyFct;
import de.dfki.mycbr.core.similarity.TaxonomyNode;
import de.dfki.mycbr.util.Pair;

/**
 * Description for symbol attributes. Gives restrictions to the allowed values
 * for this attribute.
 *
 * @author myCBR Team
 */
public class SymbolDesc extends SimpleAttDesc implements TaxonomyNode {

    /**
     * Initializes this with the given name and the allowed values. The name
     * should be unique within the attributes of the c description containing
     * this description. Creates a new range for this description.
     *
     * @param owner the owner of this description
     * @param name
     *            the name to be used for this description.
     * @param allowedValues
     *            the values which can be used for attributes of this
     *            description
     * @throws Exception if owner already has a description with that name
     */
    public SymbolDesc(final Concept owner, final String name,
            final Set<String> allowedValues)
            throws Exception {
        super(owner, name);
        range = new SymbolRange(owner.getProject(), this, allowedValues);
        
        if (owner != null && owner != owner.getProject()) {
            owner.addAttributeDesc(this);
        }
        addDefaultFct();
    }

    /**
     * Creates a new OrderedSymbolFct for the given description.
     *
     * @param name the name of the new function.
     * @param active the new function will be used in all amalgamation functions
     *  known for the owner
     * @return the Similarity.getFunction for description desc
     */
    public final OrderedSymbolFct addOrderedSymbolFct(final String name,
            final boolean active) {
        OrderedSymbolFct f = null;
        try {
            f = new OrderedSymbolFct(owner.getProject(), this, name);
            addFunction(f, active);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return f;
    }

    /**
     * Returns <code>SymbolAttribute</code> object representing the specified
     * value.
     *
     * @param value
     *            the string representing the value that should be returned
     * @return value representing the specified string, null if there is none.
     */
    public Attribute getAttribute(final String value) {
        if (owner.getProject().isSpecialAttribute(value)) {
            return owner.getProject().getSpecialAttribute(value);
        } else {
            return ((SymbolRange) range).getAttribute(value);
        }
    }

    /**
     * Adds value to the list of allowed symbols of this description. Returns
     * <code>SymbolAttribute</code> object representing the specified value.
     *
     * @param value
     *            the string representing the value that should be returned
     * @return value representing the specified string.
     */
    public SymbolAttribute addSymbol(final String value) {
        SymbolAttribute att = ((SymbolRange) range).addSymbolValue(value);
        if (att != null) {
            setChanged();
            notifyObservers(att);
        }
        return att;
    }

    /**
     * Returns Collection of symbol attributes representing the allowed symbols
     * for the given description.
     *
     * @return collection of allowed attributes
     */
    public final Collection<SymbolAttribute> getSymbolAttributes() {
        return ((SymbolRange) range).getSymbols().values();
    }

    /**
     * Gets the allowed values. Allowed values are those strings that can be
     * used for values of this description
     *
     * @return the allowed values for attributes of this description
     */
    public final Set<String> getAllowedValues() {
        return ((SymbolRange) range).getSymbols().keySet();
    }

    /**
     * Tells you whether the specified string is an allowed value for this
     * or not.
     *
     * @param value the string which should be checked for allowance
     * @return true, if value is an allowed value, false otherwise
     */
    public boolean isAllowedValue(final String value) {
        return ((SymbolRange) range).getSymbols().keySet().contains(value);
    }

    /**
     * Removes the given string from the list of allowed symbols.
     *
     * @param value the value of the symbol attribute to be removed
     */
    public void removeSymbol(final String value) {
        SymbolRange r = (SymbolRange) range;

        SymbolAttribute removedAtt = (SymbolAttribute) r.getAttribute(value);
        Integer i = r.getIndexOf(removedAtt);
        ((SymbolRange) range).removeAttribute(value); // has to be done before
        // observes get notified!
        setChanged();
        Pair<SymbolAttribute, Integer> notifyObj =
            new Pair<SymbolAttribute, Integer>(removedAtt, i);
        notifyObservers(notifyObj);

        owner.getProject().cleanInstances(owner, this);
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
     * @return true, if this can override the given function, false otherwise.
     */
    public boolean canOverride(final AttributeDesc desc) {
        if (desc instanceof SymbolDesc && desc != null) {
            SymbolDesc sDesc = (SymbolDesc) desc;
            if (sDesc.getAllowedValues() != null
                    && sDesc.getAllowedValues().containsAll(
                            this.getAllowedValues())) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param att the attribute whose index should be returned
     * @return the index of the given attribute, null if it does not belong to
     *  this
     */
    public Integer getIndexOf(final SimpleAttribute att) {
        return ((SymbolRange) range).getIndexOf(att);
    }

    /**
     * @param att the attribute which should be checked for fitting this
     * @return true, if att is a valid attribute for this description, false
     * otherwise
     */
    public boolean fits(final Attribute att) {
        if (!super.fits(att)) {
            return false;
        }
        if (att instanceof SymbolAttribute
                && !(att instanceof SpecialAttribute)) {
            return check((SymbolAttribute) att);
        } else if (att instanceof MultipleAttribute<?>) {
            MultipleAttribute<?> ma = (MultipleAttribute<?>) att;
            for (Attribute a : ma.getValues()) {
                if (!(a instanceof SymbolAttribute)
                		||( !check((SymbolAttribute) a))) {
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
    	
    	if (isMultiple) {
    		if (att instanceof SymbolAttribute
                    && !(att instanceof SpecialAttribute)) {
                return check((SymbolAttribute) att);
            } else {
            	return false;
            }
    	} else {
    		return fits(att);
    	}
    }
    
    /**
     * Checks whether the given symbol attribute is an allowed value for this
     * description.
     * @param i the attribute to be checked
     * @return true, if i is an allowed value, false otherwise
     */
    private boolean check(final SymbolAttribute i) {
        if (!this.isAllowedValue(i.getValue())) {
            return false;
        }
        return true;
    }

    /**
     * Changes the symbol attribute's value associated with oldValue to
     * newValue.
     * @param oldValue the old value
     * @param newValue the new value
     */
    public boolean renameValue(final String oldValue,
            final String newValue) {
        if (newValue.trim() != "") {
            ((SymbolRange) range).renameSymbol(oldValue, newValue);
            setChanged();
            notifyObservers();
            return true;
        }
        return false;
    }

    /**
     * Creates a new SymbolFct for the given description.
     *
     * @param name the name of the new function
     * @param active if true, the new function will be used in all amalgamation
     * functions known for the owner of this.
     * @return the new symbol function
     */
    public final SymbolFct addSymbolFct(final String name,
            final boolean active) {
        SymbolFct f = new SymbolFct(owner.getProject(), this, name);
        addFunction(f, active);
        return f;
    }
    
    /**
     * Creates a new TaxonomyFct for the given description.
     *
     * @param name the name of the new function
     * @param active if true, the new function will be used in all amalgamation
     * functions known for the owner of this.
     *
     * @return the new taxonomy function
     */
    public final TaxonomyFct addTaxonomyFct(final String name,
            final boolean active) {
        TaxonomyFct f = new TaxonomyFct(owner.getProject(), this,
                new LinkedList<SymbolAttribute>(getSymbolAttributes()), name);
        addFunction(f, active);
        f.updateTable();
        return f;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.dfki.mycbr.core.model.AttributeDesc#addDefaultFct()
     */
    /**
     *
     */
    final void addDefaultFct() {
        if (owner != null && owner != owner.getProject()) {
            ISimFct activeSim = addSymbolFct(Project.DEFAULT_FCT_NAME, false);
            updateAmalgamationFcts(owner, activeSim);
        }
    }

	/* (non-Javadoc)
	 * @see de.dfki.mycbr.core.similarity.TaxonomyNode#getNodes()
	 */
	@Override
	public List<TaxonomyNode> getNodes() {
		return new LinkedList<TaxonomyNode>(getSymbolAttributes());
	}

}
