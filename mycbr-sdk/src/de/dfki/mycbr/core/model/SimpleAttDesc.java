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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.dfki.mycbr.core.similarity.AmalgamationFct;
import de.dfki.mycbr.core.similarity.ISimFct;

/**
 * You can either define simple attribute descriptions, or
 * concept descriptions. The latter ones define part-of relations.
 * Simple attribute descriptions are descriptions of strings, integers, floats, doubles
 * symbols, etc.
 * 
 * @author myCBR Team
 *
 */
public abstract class SimpleAttDesc extends AttributeDesc {

    /**
     *
     */
    protected HashMap<String, ISimFct> simFcts;

    /**
     *
     * @param owner the owner of this
     * @param name the name of this
     * @throws Exception if the owner has another description with this name
     */
    public SimpleAttDesc(final Concept owner, final String name)
                                                        throws Exception {
        super(owner, name);
        simFcts = new HashMap<String, ISimFct>();
    }

    /**
     * Returns the available similarity functions for the specified description.
     *
     * @return the available similarity functions for the given description
     */
    public final List<ISimFct> getSimFcts() {
        return new LinkedList<ISimFct>(simFcts.values());
    }

    /**
     *
     * @param name the name of the function to be returned
     * @return the function of name name or null if there is no function
     *  known for this having this name
     */
    public final ISimFct getFct(final String name) {
        return simFcts.get(name);
    }

    /**
     * Adds the given function to the list of functions maintained for the given
     * description. 
     * 
     * @param f the new function for description desc
     * @param active if true, the new function will be used in the active
     *  amalgamation function known for the owner of this
     */
    protected final void addFunction(final ISimFct f, final boolean active) {

        simFcts.put(f.getName(), f);
        if (active) {
            setFctActive(f);
        }
        setChanged();
        notifyObservers();
    }

    /**
     * @param f the function which should be used in the active amalgamation
     *   function of the owner of this
     */
    private void setFctActive(final ISimFct f) {
        AmalgamationFct amalgam = getOwner().getActiveAmalgamFct();
        if (amalgam != null) {
            amalgam.setActiveFct(this, f);
            setChanged();
            notifyObservers();
        }
    }

    /**
     * Deletes the given function from the list of known functions for the given
     * description.
     *
     * @param f the function to be deleted
     */
    public final void deleteSimFct(final ISimFct f) {
    	
    	if (f == null) {
    		return;
    	} 
    	
        simFcts.remove(f.getName());
        // TODO what if this was active somewhere?
        // TODO update amalgamation functions that use this function
        if (simFcts.size() == 0) {
            // add default function
            addDefaultFct();
        }
        setChanged();
        notifyObservers();
    }

    /**
     *
     */
    abstract void addDefaultFct();

    /**
     *
     * @param f the function to be added to this
     */
    public final void addFct(final ISimFct f) {
        simFcts.put(f.getName(),f);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.dfki.mycbr.core.model.AttributeDesc#canOverride(de.dfki.mycbr.core
     * .model.AttributeDesc)
     */
    @Override
    /**
     * @param desc the description which should be overridden by this
     * @return true, if this can override the given function, false otherwise
     */
    public abstract boolean canOverride(final AttributeDesc desc);

    /**
     *
     */
    public final void deleteAllFcts() {
        simFcts.clear();
    }

	/**
	 * @param nameOLD
	 * @param nameNEW
	 */
	public void renameFct(String nameOLD, String nameNEW) {
		ISimFct fct = simFcts.remove(nameOLD);
		if (fct != null) {
			simFcts.put(nameNEW, fct);
			setChanged();
			notifyObservers();
		}
	}
}
