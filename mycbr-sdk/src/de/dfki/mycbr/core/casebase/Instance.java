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

import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.explanation.Explainable;
import de.dfki.mycbr.core.explanation.IExplainable;
import de.dfki.mycbr.core.model.AttributeDesc;
import de.dfki.mycbr.core.model.Concept;

/**
 * Represents values occurring in query and or cases. Each attribute knows its
 * description and therefore knows how similar it is to other attributes of the
 * same description. Attributes for a fixed description are maintained by ranges
 * (classes implementing interface {@link Range}).
 *
 * @author myCBR Team
 *
 */
public final class Instance extends Attribute implements Observer,
                                                              IExplainable {

	public static final String QUERY_ID = "query";

    /**
     * The values of this c's attributes.
     */
    private HashMap<AttributeDesc, Attribute> attributes;

    /**
     * Gives restrictions to values for this attribute and tells us how to
     * compute similarity of this attribute and other attributes of this
     * description.
     */
    private Concept c;

    /**
     *
     */
    private String id;

    /**
     * Flag for adaptation
     */
    private boolean adapted = false;
    
    /**
     * Creates new value for the specified description.
     * Be aware that this instance will not belong to the concept,
     * if you call this constructor. Better call {@link Concept#addInstance(String)}
     * to create an instance properly!
     * @param concept
     *            description of this attribute
     * @param id the name of this instance
     */
     public Instance(Concept concept, String id) /*throws Exception*/ {
        this.c = concept;
        
//        if ((id == null || id.trim().equals("") || c.getProject().getInstance(id) != null) && (!"query".equals(id))) {
//        	throw new Exception("ID either null, empty or not unique!");
//        }
        
        this.id = id;
        c.addObserver(this);
        attributes = new HashMap<AttributeDesc, Attribute>();
        reset();
        if (!id.equals("query")) {
        	concept.addInstance(this);
        }
    }

    /**
     *
     * @param n new name of this
     */
    public void setName(final String n) {
    	if (id == null || id.trim().equals("") || c.getProject().getInstance(id) != null) {
        	return;
        }
        c.renameInstance(this.id, n);
        this.id = n;
        setChanged();
        notifyObservers();
    }

    /**
     * @return the name of this
     */
    public String getName() {
        return id;
    }

    /**
     * Gets the description for this attribute to determine similarity functions
     * or restrictions for the value of this attribute.
     *
     * @return the description of this attribute
     */
    public Concept getConcept() {
        return c;
    }

    /**
     * Gets all the attribute values for this c.
     *
     * @return the list of attributes of this c
     */
    public HashMap<AttributeDesc, Attribute> getAttributes() {
        return attributes;
    }

    /**
     * Removes the given attribute.
     *
     * @param desc
     *            the attribute to be removed
     */
    public void removeAttribute(final AttributeDesc desc) {
        attributes.remove(desc);
        setChanged();
        notifyObservers();
    }

    /**
     * Sets the value of the attribute specified by desc to att. First checks
     * whether this c attribute has a direct attribute description that
     * corresponds to the description of the given attribute. If not, checks
     * whether this c attribute has a super class that has a direct attribute
     * description that corresponds to the given att's description. In any of
     * these cases the given att is added to this c's attributes. Otherwise does
     * nothing.
     *
     * It is assumed that you set the values for c attributes that are contained
     * within this c attribute via composition separately by first adding the
     * values to the corresponding c attribute and then add this c attribute as
     * an attribute here.
     *
     * @param n the name of the attribute description
     * @param a
     *            the value to be added to this c attribute.
     * @return true, if the attribute was successfully added, false otherwise.
     */
    public boolean addAttribute(final String n, final Attribute a) {

        if (a == null) {
            return false;
        }

        // check whether the given description contains the given attribute
        // description as a direct or indirect (inherited) attribute
        AttributeDesc attDesc = c.getAllAttributeDescs().get(n);

        if (attDesc == null ) {
        	return false;
        } else if (attDesc.fits(a)) {
            // add the new attribute
            attributes.put(attDesc, a);

            setChanged();
            notifyObservers();
            
            return true;
        }
        return false;
    }

    /**
     *
     * @param n the name of the attribute description
     * @param value the value of the attribute to be added
     * @return true, if the attribute has been successfully added
     * @throws ParseException if the value does not fit the description
     */
    public boolean addAttribute(final String n, final Object value)
            throws ParseException {
        AttributeDesc attDesc = c.getAllAttributeDescs().get(n);
        if (attDesc != null) {
            if (!(value instanceof Attribute)) {
            	Attribute att = attDesc.getAttribute(value);
            	
                return addAttribute(n, att);
            } else {
                return addAttribute(n, (Attribute) value);
            }
        }
        return false;
    }

    /**
     *
     * @param desc the description of the attribute to be added
     * @param value the value to be added
     * @return true, if attribute has been added successfully, false otherwise
     * @throws ParseException if the value does not fit the description
     */
    public boolean addAttribute(final AttributeDesc desc, final Object value)
            throws ParseException {
        return addAttribute(desc.getName(), value);
    }

    /**
     *
     * @param desc the description of the attribute to be added
     * @param a the attribute to be added
     * @return true, if the attribute has been successfully added,
     *                                                        false otherwise
     */
    public boolean addAttribute(final AttributeDesc desc, final Attribute a) {
        return addAttribute(desc.getName(), a);
    }

    /**
     * Returns the attribute for the given description. Returns null, if there
     * is no attribute for this description.
     *
     * @param attDesc
     *            the description for which the attribute should be returned
     * @return the attribute corresponding to attDesc, null if there is none
     */
    public Attribute getAttForDesc(final AttributeDesc attDesc) {
        Attribute att = attributes.get(attDesc);

        return att;
    }

    /**
     * Returns the string representation of this c attribute.
     *
     * @return string representation of this c attribute
     */
    public String getValueAsString() {
        return id; // to save in xml and later on reload using reference by
                     // name
    }

    /**
     * @return type of explainable object.
     */
    public Explainable getExpType() {
        return Explainable.Instance;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
    public void update(final Observable o, final Object arg) {
        clean(); 

        setChanged();
        notifyObservers();
    }

    /**
     * Removes all values for attribute descriptions that do no longer exist and
     * adds value _undefined_ for each new attribute description. There might be
     * new or old descriptions if the c this case belongs to has a new super c
     */
    private void clean() {
        Collection<AttributeDesc> allowedDescs = c.getAllAttributeDescs()
                .values();
        Vector<AttributeDesc> oldDescs = new Vector<AttributeDesc>();
        for (AttributeDesc desc : attributes.keySet()) {
            if (!allowedDescs.contains(desc)) {
                oldDescs.add(desc);
            }
        }
        // delete old descs
        for (AttributeDesc desc : oldDescs) {
            attributes.remove(desc);
        }

        for (AttributeDesc attDesc : c.getAllAttributeDescs().values()) {
            if (this.getAttForDesc(attDesc) == null) {
                addAttribute(attDesc, c.getProject().getSpecialAttribute(
                        Project.UNKNOWN_SPECIAL_VALUE));
            }
        }
    }

    /**
     * @param desc the instance will be cleaned wrt this description
     */
    public void clean(final AttributeDesc desc) {
        Attribute att = getAttForDesc(desc);
        if (!desc.fits(att)) {
            addAttribute(desc, c.getProject().getSpecialAttribute(
                    Project.UNKNOWN_SPECIAL_VALUE));
        }
    }
    
    /**
     * Sets the value for every direct or inherited attribute description
     * to the default special value undefined.
     */
    public void reset() {
    	attributes.clear();
    	Attribute svAtt = c.getProject().getSpecialAttribute(
                Project.UNKNOWN_SPECIAL_VALUE);
        for (AttributeDesc desc : c.getAllAttributeDescs().values()) {
            attributes.put(desc, svAtt);
        }
    }
    
    /**
     * Sets the value for every direct or inherited attribute description
     * to the default special value undefined.
     */
    public void setAttsUnknown() {
    	attributes.clear();
    	Attribute svAtt = c.getProject().getSpecialAttribute(
                Project.UNKNOWN_SPECIAL_VALUE);
        for (AttributeDesc desc : c.getAllAttributeDescs().values()) {
            attributes.put(desc, svAtt);
        }
    }
    
    public String toString() {
    		return id;
    }

	/**
	 * @param adapted the adapted to set
	 */
	public void setAdapted(boolean adapted) {
		this.adapted = adapted;
	}

	/**
	 * @return the adapted
	 */
	public boolean isAdapted() {
		return adapted;
	}
}
