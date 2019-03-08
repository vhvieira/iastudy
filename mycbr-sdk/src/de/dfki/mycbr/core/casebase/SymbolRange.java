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

import java.util.Collection;
import java.util.HashMap;
import java.util.Observable;
import java.util.Vector;

import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.model.SymbolDesc;

/**
 * Holds SymbolAttributes for a given SymbolDesc. The SymbolAttributes are
 * initialized during construction. SpecialAttributes have to be handled separately,
 * so that you can have SpecialAttribute instead of SymbolAttributes.
 *
 * @author myCBR Team
 *
 */
public class SymbolRange extends Range {

    /**
     * Associates a <code>SymbolAttribute</code> to each allowed value.
     */
    protected HashMap<String, SymbolAttribute> symbols;

    /**
     * @param syms the symbols to set
     */
    final void setSymbols(final HashMap<String, SymbolAttribute> syms) {
        this.symbols = syms;
    }

    /**
     * Specifies a linear order for indexing symbol attributes. By default, the
     * first attribute specified in the given symbol description is the smallest
     * element in the linear order.
     */
    private HashMap<SymbolAttribute, Integer> indexes;

    /**
     * Counts the highest index for maintenance reasons.
     */
    private int highestIndex = 0;

    /**
     * Gives restrictions (allowed values) to this range and tells us how to
     * compute similarity of attributes of this description.
     */
    private SymbolDesc desc;

    /**
     * Creates one SymbolAttribute object for each String specified by
     * allowedValues.
     *
     * @param prj the project this range belongs to
     * @param description
     *            the symbol description for this attribute
     * @param allowedValues
     *            the allowed values for this attribute
     */
    public SymbolRange(final Project prj, final SymbolDesc description,
            final Collection<String> allowedValues) {
        super(prj);
        this.desc = description;
        this.desc.addObserver(this);
        // init SymbolAttributes
        if (allowedValues == null) {
            initSymbolAttributes(new Vector<String>());
        } else {
            initSymbolAttributes(allowedValues);
        }
        initIndexes();
    }

    /**
     * Initializes the private field symbols with the given allowedValues.
     *
     * @param values
     *            the symbols maintained by this range
     */
    void initSymbolAttributes(final Collection<String> values) {
        symbols = new HashMap<String, SymbolAttribute>(values.size());
        for (String value : values) {
            if (value.trim() != "") { // do not add empty values
                SymbolAttribute att = new SymbolAttribute(getDesc(), value);
                symbols.put(value, att);
            }
        }
    }

    /**
     * Initializes the private files indexes. This field specifies a linear
     * order for indexing symbol attributes. By default, the first attribute
     * specified in the given symbol description is the smallest element in the
     * linear order. Has to be called after
     * {@link #initSymbolAttributes(Collection)}
     */
    public void initIndexes() {
        setIndexes(new HashMap<SymbolAttribute, Integer>(symbols.size()));
        int i = 0;
        for (SymbolAttribute att : symbols.values()) {
            getIndexes().put(att, i++);
        }
        this.setHighestIndex(symbols.size());
    }

    /**
     * Returns the SymbolAttribute associated with the given String. Returns
     * null if the symbol does not specify an allowed value for this attribute.
     * Creates a new SymbolAttribute if there is no SymbolAttribute for the
     * given symbol yet. Updates index structure if a new SymbolAttribute object
     * is added.
     *
     * @param symbol
     *            the value for which the corresponding SymbolAttribute should
     *            be returned
     * @return the SymbolAttribute specified by symbol, null if symbol is not an
     *         allowed value
     *
     * @see SymbolDesc#isAllowedValue(String)
     */
    public final SymbolAttribute getSymbolValue(final String symbol) {
        SymbolAttribute att = symbols.get(symbol);
        return att;
    }

    /**
     * Get a map which associates a <code>SymbolAttribute</code> with each
     * allowed value for the given attribute description.
     *
     * @return map which associates <code>SymbolAttribute</code> with string
     */
    public final HashMap<String, SymbolAttribute> getSymbols() {
        return symbols;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.dfki.mycbr.core.casebase.Range#getAttribute(java.lang.Object)
     */
    /**
     * Gets the attribute associated with the specified <code>Object</code> obj.
     * obj is expected to be of type <code>String</code>. Returns result of
     * {@link #getSymbolValue(String)} if obj is of type <code>String</code>,
     * result of {@link Project#getSpecialAttribute(String)} if obj is of type
     * <code>SpecialAttribute</code>, else returns null. Is needed for
     * MultipleRange.
     *
     * @param obj
     *            representing String or SpecialAttribute
     * @return SimpleAttribute that corresponds to obj, null if there is no such
     *         SimpleAttribute
     */
    public Attribute getAttribute(final Object obj) {

        if (obj instanceof String) {
            if (getProject().isSpecialAttribute((String) obj)) {
                return getProject().getSpecialAttribute((String) obj);
            } else {
                return getSymbolValue((String) obj);
            }
        }
        return null;
    }

    /**
     * Returns the index of the given attribute. Returns null, If the attribute
     * is not maintained by this range.
     *
     * @param att
     *            the attribute whose index should be returned
     * @return index of the given att, null if this attribute is unknown
     */
    public final Integer getIndexOf(final SimpleAttribute att) {
        return getIndexes().get(att);
    }

    /**
     * Removes the SymbolAttribute associated with the given string from this
     * range. Returns the corresponding SymbolAttribute or null if there is no
     * attribute that corresponds to symbol. Updates index structures.
     *
     * @param symbol
     *            the symbol to be removed from this range
     * @return the attribute corresponding to this symbol, null if there is none
     */
    public final SymbolAttribute removeAttribute(final String symbol) {
        SymbolAttribute att = symbols.remove(symbol);
        initIndexes();
        return att;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
    public void update(final Observable o, final Object arg) {
        // does not have to do anything because symbol desc calls
        // removeSymbol if symbol gets deleted
    }

    /**
     * @param value the old name of the symbol
     * @param value2 the new name of the symbol
     */
    public final void renameSymbol(final String value, final String value2) {
        SymbolAttribute att = symbols.remove(value);
        symbols.put(value2, att);
    }

    /**
     * @param value the value of the new symbol attribute
     * @return the new symbol attribute with value value
     */
    public SymbolAttribute addSymbolValue(final String value) {

        SymbolAttribute att = null;
        if (value.trim() != "") { // do not add empty values
            att = symbols.get(value);
            if (att == null) {
                att = new SymbolAttribute(this.getDesc(), value);
                symbols.put(value, att);
                getIndexes().put(att, highestIndex++);
            }
        }
        return att;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.dfki.mycbr.core.casebase.Range#parseValue(java.lang.String)
     */
    @Override
    final Attribute parseValue(final String string) {
        return getSymbolValue(string);
    }

    /**
     * @param d the desc to set
     */
    final void setDesc(final SymbolDesc d) {
        this.desc = d;
    }

    /**
     * @return the desc
     */
    final SymbolDesc getDesc() {
        return desc;
    }

    /**
     * @param i the indexes to set
     */
    public final void setIndexes(final HashMap<SymbolAttribute, Integer>
                                                                i) {
        this.indexes = i;
    }

    /**
     * @return the indexes
     */
    public final HashMap<SymbolAttribute, Integer> getIndexes() {
        return indexes;
    }

    /**
     * @param i the highestIndex to set
     */
    public final void setHighestIndex(final int i) {
        this.highestIndex = i;
    }

    /**
     * @return the highestIndex
     */
    public final int getHighestIndex() {
        return highestIndex;
    }

}
