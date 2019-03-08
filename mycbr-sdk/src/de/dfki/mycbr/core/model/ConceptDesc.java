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
import java.util.Map;
import java.util.Observable;

import de.dfki.mycbr.core.casebase.Attribute;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.casebase.MultipleAttribute;
import de.dfki.mycbr.core.casebase.SpecialAttribute;
import de.dfki.mycbr.core.similarity.AmalgamationFct;
import de.dfki.mycbr.core.similarity.config.MultipleConfig;

/**
 * The vocabulary of a myCBR project consists of several attribute descriptions
 * which describe a tree like structure as a class hierarchy (with composition 
 * and inheritance). The classes in this hierarchy are represented by objects of 
 * type c description. Each c description contains several other 
 * attribute descriptions (like integer, float, boolean descriptions ...).
 * Concept description can have sub/super c description (representing 
 * inheritance) and can have c descriptions as attribute descriptions 
 * themselves (representing composition).
 *
 * @author myCBR Team
 */
public class ConceptDesc extends AttributeDesc {

	private Concept concept; 
	private HashMap<AmalgamationFct, MultipleConfig> multipleConfigs = new HashMap<AmalgamationFct, MultipleConfig>();
	
	/**
	 * Creates a new attribute description of type concept.
	 * This description belongs to the given owner
	 * and specifies a part-of relation with respect to concept.
	 * 
	 * @param owner
	 * @param name
	 * @param concept
	 * @throws Exception
	 */
	public ConceptDesc(Concept owner, String name, Concept concept) throws Exception {
		super(owner,name);
		this.concept = concept;
		this.concept.addObserver(this);
		this.concept.addPartOfRelation(this);
		if (owner != null && owner != owner.getProject()) {
			owner.addAttributeDesc(this);
		} 
		super.range = concept.range;
		AmalgamationFct activeSim = concept.getActiveAmalgamFct();
		updateAmalgamationFcts(owner, activeSim);
		
		// init multiple configs
		for (AmalgamationFct f: concept.getAvailableAmalgamFcts()) {
			multipleConfigs.put(f, MultipleConfig.DEFAULT_CONFIG);
		}
	}
	
	public Concept getConcept() {
		return concept;
	}

	
	/* (non-Javadoc)
	 * @see de.dfki.mycbr.core.model.AttributeDesc
	 * 						#canOverride(de.dfki.mycbr.core.model.AttributeDesc)
	 */
	@Override
	public boolean canOverride(AttributeDesc desc) {
		if (desc instanceof ConceptDesc) {
			Concept c = ((ConceptDesc) desc).getConcept();
			return concept.canOverride(c);
		}
		return false;
	}
	
	public void setConcept(Concept c) {
		this.concept.deletePartOfRelation(this);
		this.concept = c;
		c.addPartOfRelation(this);
		range = c.range;
		owner.getProject().cleanInstances(owner, this);
		setChanged();
		notifyObservers();
	}
	
	/**
	 * @param att
	 * @return true if the given attribute fits this description
	 */
	public boolean fits(Attribute att) {
		if (!super.fits(att)) {
			return false;
		}
		if (att instanceof Instance) {
			return check((Instance)att);
		} else if (att instanceof MultipleAttribute<?>) {		 	
			MultipleAttribute<?> ma = (MultipleAttribute<?>)att;
			for (Attribute a: ma.getValues()) {
				if (!(a instanceof Instance) || !check((Instance)a) ) {
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
			if (att instanceof Instance) {
				return check((Instance)att);
			} else {
				return false;
			}
		} else {
			return fits(att);
		}
	}
	
	private boolean check(Instance i) {
		for (Map.Entry<AttributeDesc, Attribute> entry: i.getAttributes().entrySet()) {
			if (!entry.getKey().fits(entry.getValue())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 */
	public void deleteAllFcts() {
		if (concept == null || concept.getAvailableAmalgamFcts() == null) {
			return; // TODO
		}
		concept.getAvailableAmalgamFcts().clear();
	}
	
	public MultipleConfig getMultipleConfig(AmalgamationFct f) {
		MultipleConfig mc = multipleConfigs.get(f);
		if (mc == null) {
			mc = MultipleConfig.DEFAULT_CONFIG;
		}
		return mc;
	}

	public void setMultipleConfig(AmalgamationFct f, MultipleConfig mc) {
		if (multipleConfigs.get(f)!=mc) {
			multipleConfigs.put(f, mc);
			setChanged();
			notifyObservers();
		}
	}
	
	@Override
	public void update(Observable o, Object arg) {
		
		if (arg instanceof AmalgamationFct) { // added/ deleted amalgamation fct
			AmalgamationFct f = ((AmalgamationFct)arg);
			if (concept.getAvailableAmalgamFcts().contains(f)) { // function has been added
				multipleConfigs.put(f, MultipleConfig.DEFAULT_CONFIG);
			} else { // function has been deleted
				multipleConfigs.remove(f);
			}
		} else { // only clean instances if necessary
			// which is not the case for adding/ removing amalgamation functions
			
			// if this concept changes we have to clean the corresponding
			// instances
			owner.getProject().cleanInstances(owner, this);
	
		}
	}
}
