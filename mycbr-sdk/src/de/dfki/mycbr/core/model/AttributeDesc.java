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

import java.text.ParseException;
import java.util.Observable;
import java.util.Observer;

import de.dfki.mycbr.core.casebase.Attribute;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.casebase.MultipleAttribute;
import de.dfki.mycbr.core.casebase.Range;
import de.dfki.mycbr.core.casebase.SimpleAttribute;
import de.dfki.mycbr.core.casebase.SpecialAttribute;
import de.dfki.mycbr.core.explanation.Explainable;
import de.dfki.mycbr.core.explanation.IExplainable;
import de.dfki.mycbr.core.similarity.AmalgamationFct;

/**
 * The vocabulary of a myCBR project consists of several concepts
 * which describe a tree like structure as a class hierarchy (with composition 
 * and inheritance). The classes in this hierarchy are represented by objects of 
 * type concept. Each concept contains several  
 * attribute descriptions (like integer, float, boolean descriptions ...).
 * Concepts can have sub/super concepts (representing 
 * inheritance) and can have concept descriptions as attribute descriptions 
 * themselves (representing composition).
 * 
 * The structure containing the values (representing object diagrams) is 
 * introduced in the package <code>de.dfki.mycbr.core.casebase</code>.
 * This class contains properties that all of these descriptions have in common.
 * 
 * @author myCBR Team
 */
public abstract class AttributeDesc extends Observable implements Observer, IExplainable {
	
	protected static final String DELETE_NOTIFICATION = "deleted";
	
	/**
	 * Used to identify the attribute within the
	 * local attributes of owner. 
	 */
	protected String name;
	
	/**
	 * The Concept that has this attribute
	 * as a description 
	 */
	protected Concept owner;
	
	/**
	 * value can be a set of corresponding attribute objects
	 * instead of a single value
	 */
	protected boolean isMultiple;

	/**
	 * each description has its own range which maintains the
	 * attributes (values) for this description. 
	 */
	protected Range range;
	
	public AttributeDesc(Concept owner, String name) throws Exception {
		if (owner == null) {
			throw new Exception("owner for description \"" + name + "\" null");
		} 
		if (name == null || name.trim().equals("")) {
			throw new Exception("Cannot add description with empty name!");
		}
		this.owner = owner;
		addObserver(owner);
		this.name = name;
	}
	
	/**
	 * Set the value of isMultiple.
	 * The value for this attribute description can be a set of corresponding 
	 * attribute objects instead of a single value.
	 * 
	 * @param isMultiple the new value of isMultiple
	 */
	public void setMultiple(boolean isMultiple) {
		if (isMultiple == this.isMultiple) {
			return;
		}
		this.isMultiple = isMultiple;
		if (isMultiple) {
			owner.setAllInstancesMultiple(this);
		} else {
			owner.setAllInstancesSingle(this);
		}
		setChanged();
		notifyObservers(isMultiple);
	}

	/**
	 * Get the value of isMultiple.
	 * Possible values for this attribute description can be a set of 
	 * corresponding attribute objects instead of a single value.
	 * 
	 * @return the value of isMultiple
	 */
	public boolean isMultiple() {
		return isMultiple;
	}
	
	public void setOwner(Concept owner) throws Exception {
		if (this.owner != owner && owner != null) {
			this.owner.removeAttributeDesc(name);
			owner.addAttributeDesc(this);
			this.owner = owner;
			for (AmalgamationFct f: owner.getAvailableAmalgamFcts()) {
				f.setWeight(getName(), 1.0);
				f.setActive(this,true);
				if (this instanceof SimpleAttDesc) {
					f.setActiveFct(this, ((SimpleAttDesc)this).getSimFcts().get(0)); // there is at least one function
				} else {
					f.setActiveFct(this, ((ConceptDesc)this).getOwner().getActiveAmalgamFct()); // there is at least one function
				}
			}
			setChanged();
			notifyObservers();
		}
	}

	/**
	 * Returns the owner of this description.
	 * An attribute description belongs to exactly one concept (the owner)
	 * at a time. However, it is inherited to the sub concepts
	 * of the owner.
	 * 
	 * @return the unique owner of this description
	 */
	public Concept getOwner() {
		return owner;
	}
	
	public void setName(String name) throws Exception {
		if (this.name.equals(name)) {
			return; // nothing to do if description is already called "name"
		}
		// check whether there is an attribute description in the sub concept
		// of owner, which has this name
		if (owner.getAttributesOfSubDescsForName(name).size() > 0) {
			// cannot rename attribute, because other descriptions would have to inherit from this one.
			// too much constraints to be checked, such that operation hardly possible in any case
			throw new Exception("There are sub concepts which have an attribute description of the given name!");
		}
		
		AttributeDesc d = owner.getAttributeDesc(name);
		if (d != null) {
			if (d.getOwner() != owner) { // description is inherited from d.getOwner()
				// check whether this attribute description can override d
				if (!this.canOverride(d)) {
					throw new Exception("This attribute description cannot override the description cinherited from " + d.getOwner().getName()); 
				}
			} else {
				throw new Exception("The concept " + owner.getName() + " already has an attribute with the name " + name + " !");
			}
		}
		owner.renameAttDesc(this.name, name);
		this.name = name; 
		setChanged();
		notifyObservers();
		
	}

	public String getName() {
		return name;
	}
	
	/**
	 * Checks whether this attribute description
	 * can override the given attribute description
	 */
	public abstract boolean canOverride(AttributeDesc desc);

	/**
	 * @param value
	 * @return the attribute associated with the given value
	 * @throws ParseException 
	 */
	public Attribute getAttribute(Object value) throws ParseException {
		return range.getAttribute(value);
	}

	
	/**
	 * @param owner
	 * @param activeSim
	 */
	protected void updateAmalgamationFcts(Concept c, Object activeSim) {
		for (AmalgamationFct f: c.getAvailableAmalgamFcts()){
			f.setActive(this, true);
			f.setActiveFct(this, activeSim);
			f.setWeight(this, 1);
		}
		for (Concept sub: c.getSubConcepts().values()) {
			updateAmalgamationFcts(sub, activeSim);
		}
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		// a description observes another description in case
		// it overrides this other description
		if (o instanceof AttributeDesc && arg instanceof Boolean) {
			// the description which is overridden by this one changed its multiple flag
			// -> this description has to be adapted, too
			setMultiple((Boolean)arg);
		} else if (DELETE_NOTIFICATION.equals(arg)) {
			// the attribute description overridden by this has been removed!
			// we have to add this as observer, if there is another attribute 
			// overridden by this
			Concept parent = owner.getSuperConcept();
			AttributeDesc desc = parent.getAllAttributeDescs().get(name);
			if (desc != null) {
				desc.addObserver(this);
			}
		}
	}

	/**
	 * Checks whether the given attribute 
	 * fits this.
	 * Be aware that if this is multiple then single values will not fit.
	 * For checking whether single values fit this description without taking
	 * multiple values into account call {@link #fitsSingle(Attribute)}
	 * @param att the attribute to be checked
	 * @return true if the attribute fits this, false otherwise
	 */
	public boolean fits(Attribute att) {
	    if (att == null) {
	        return false;
	    }
		if (att instanceof SpecialAttribute) {
		    if (((SpecialAttribute)att).getAttributeDesc() == null) {
		        return false;
		    }
			if (!((SpecialDesc)((SpecialAttribute) att).getAttributeDesc()).isAllowedValue(att.getValueAsString())){
				return false;
			}
		} else if (att instanceof SimpleAttribute) {
			SimpleAttribute s = (SimpleAttribute)att;
			if (s.getAttributeDesc() != this || this.isMultiple()) {
				return false;
			}
		} else if (att instanceof Instance) {
			if (this.isMultiple()) {
				return false;
			}
		} else if (att instanceof MultipleAttribute<?>) {
			if (!isMultiple()) {
				return false;
			}			
		}
		return true;
	}

	/**
	 * Checks whether a single attribute fits the constraints for this.
	 * Returns true, if att is a valid SpecialAttribute or if att can be used
	 * as a single value of a multiple attribute. If this is not multiple calls {@link #fits(Attribute)}
	 * 
	 * @param att the attribute which should be checked
	 * @return true, when att can be used as value of this or in multiple attribute
	 */
	public boolean fitsSingle(Attribute att) {
		if (isMultiple) {
		    if (att == null) {
		        return false;
		    }
			if (att instanceof SpecialAttribute) {
			    if (((SpecialAttribute)att).getAttributeDesc() == null) {
			        return false;
			    }
				if (!((SpecialDesc)((SpecialAttribute) att).getAttributeDesc()).isAllowedValue(att.getValueAsString())){
					return false;
				}
			} else if (att instanceof SimpleAttribute) {
				SimpleAttribute s = (SimpleAttribute)att;
				if (s.getAttributeDesc() != this) {
					return false;
				}
			} 
			return true;
		} else { 
			return fits(att);
		} 
	}
	
	public void delete() {
		// delete all functions
		deleteAllFcts();
		owner.removeAttributeDesc(this.name);
		this.setChanged();
		this.notifyObservers(AttributeDesc.DELETE_NOTIFICATION);
		this.deleteObservers();
	}
	
	abstract public void deleteAllFcts();

	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public Explainable getExpType() {
		return Explainable.AttributeDesc;
	}
}
