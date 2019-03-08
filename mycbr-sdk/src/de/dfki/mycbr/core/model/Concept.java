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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import de.dfki.mycbr.core.ICaseBase;
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Attribute;
import de.dfki.mycbr.core.casebase.ConceptRange;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.casebase.SymbolAttribute;
import de.dfki.mycbr.core.explanation.Explainable;
import de.dfki.mycbr.core.explanation.IExplainable;
import de.dfki.mycbr.core.similarity.AmalgamationFct;
import de.dfki.mycbr.core.similarity.ISimFct;
import de.dfki.mycbr.core.similarity.Similarity;
import de.dfki.mycbr.core.similarity.TaxonomyFct;
import de.dfki.mycbr.core.similarity.TaxonomyNode;
import de.dfki.mycbr.core.similarity.config.AmalgamationConfig;

/**
 * Concepts define the entities in the model. You can build up 
 * inheritance hierarchies. Concepts have a set of attribute descriptions
 * and a set of amalgamation functions. The active amalgamation function defines
 * how to calculate the overall similarity of two instances of this concept.
 * 
 * There is a set of instances associated with each concept (via a concept range). 
 * These instances are used to build up the case base.
 * 
 * @author myCBR Team
 * @since myCBR v3.0.0
 * 
 */
public class Concept extends Observable implements IExplainable, Observer {
	
	/**
	 * super concept description by inheritance. The top concepts with respect
	 * to inheritance are the sub concepts of the project.
	 */
	protected Concept superConcept;
	
	/**
	 * Range of this concept. Holding all instances associated to this.
	 */
	protected ConceptRange range;
	
	/**
	 * Concepts within a project have unique names because they all appear
	 * in the same inheritance hierarchy 
	 */
	protected String ID;
	
	/**
	 * sub concepts descriptions by inheritance.
	 * empty list if this c description does not have sub concepts 
	 * in inheritance hierarchy.
	 */
	protected HashMap<String,Concept> subConcepts;
	
	/**
	 * Hash Map of attribute names together with attributes of this c. 
	 */
	protected HashMap<String, AttributeDesc> attDescs;

	protected Vector<ConceptDesc> partOfRelations = new Vector<ConceptDesc>();

	protected Project prj;

	private HashMap<String,AmalgamationFct> availableAmalgamFcts = new HashMap<String,AmalgamationFct>();

	private AmalgamationFct activeAmalgamationFct;
	
	/**
	 * Initializes this with the given name. 
	 * The name should be unique within the whole inheritance hierarchy (model)
	 * and within the attributes used for c descriptions.
	 * 
	 * Creates a new range for this description.
	 * 
	 * @param ID the ID to be used for this description.
	 * @throws Exception 
	 */
	public Concept(String ID, Project project, Concept superConcept) 
	                                                        throws Exception {
		if (ID == null || ID.trim().equals("")) {
			throw 
			 new Exception("Cannot create concepts with an empty ID!");
		}
		this.prj = project;
		this.range = new ConceptRange(project, this);
		
		if (project != null) { 	// project is only null for the case that 
								// you create a project. in the Project constructor
								// we call Concept("Project",null,null)
			if (project.hasConceptWithID(ID)) {
				throw 
				 new Exception("Concept with name \"" + ID + "\" already exists!");
			}
			this.ID = ID;

			attDescs = new HashMap<String, AttributeDesc>();
			subConcepts = new HashMap<String, Concept>();
			// inheritance hierarchy
			superConcept.addSubConcept(this,true);
			// to pass changes on to super concepts (up to project)
			this.addObserver(superConcept); 
			
		}
		if (!Project.ID_DEFAULT.equals(ID))  {
			addAmalgamationFct(AmalgamationConfig.EUCLIDEAN, Project.DEFAULT_FCT_NAME, true);
		}
	}

	/**
	 * Gets the sub c descriptions by inheritance.
	 * Empty list if this c does not have sub concepts in inheritance 
	 * hierarchy.
	 * 
	 * @return sub c descriptions of this c description
	 */
	public HashMap<String,Concept> getSubConcepts() {
		return subConcepts;
	}

	/**
	 * Gets all sub c descriptions by inheritance.
	 * Empty list if this c does not have sub concepts in inheritance 
	 * hierarchy.
	 * 
	 * @return all sub c descriptions of this c description
	 */
	public HashMap<String,Concept> getAllSubConcepts() {
		HashMap<String,Concept> result 
							= new HashMap<String,Concept>(subConcepts);
		for (Concept sub: subConcepts.values()) {
			result.putAll(sub.getAllSubConcepts());
		}
		return result;
	}
	
	
	public AmalgamationFct getFct(String name) {
		return availableAmalgamFcts.get(name);
	}
	/**
	 * Removes the sub concept with name name.
	 * Does nothing if there is no such sub concept and returns null.
	 * Adapts instances and cases.
	 * 
	 * @param name the name of the sub concept to be removed
	 * @return the Concept which has been deleted.
	 */
	public Concept removeSubConcept(String name) {
		Concept sub = subConcepts.remove(name);
		if (sub != null) {
			setChanged();
		}
		notifyObservers();
		return sub;
	}

	/**
	 * Should be called when name of an sub concept changes.
	 * Renames the concept given by nameOLD to nameNEW.
	 * Does nothing if there is no sub concept with name nameOLD
	 * 
	 * @param nameOLD the name of the sub concept to be renamed 
	 * @param nameNEW the new name
	 */
	public void renameSubConcept(String nameOLD, String nameNEW) {
		Concept sub = subConcepts.remove(nameOLD);
		if (sub != null) {
			subConcepts.put(nameNEW, sub);
			setChanged();
			notifyObservers();
		}
	}
	
	public Concept getSuperConcept() {
		return superConcept;
	}

	public void removeInstance(String name) {
		range.remove(name);
	}
	
	/**
	 * Moves this concept in the inheritance hierarchy.
	 * After this operation, the this concept will be a sub concept of c.
	 * Does nothing if c is null or c is already the super concept of this.
	 * 
	 * @param c the new super concept of this
	 */
	public void setSuperConcept(Concept c, boolean isNew) { 
		if (!superConcept.equals(c) && (c != null)) {
			// addSubConcept does all the update work!
			Concept oldSuperConcept = superConcept;
			c.addSubConcept(this, isNew);
			
			superConcept = c;
			oldSuperConcept.removeSubConcept(ID);
		}
	}

	/**
	 * Adds the given concept as sub concept of this.
	 * If c is null or this does nothing.
	 * Returns false, if the action of adding c as a sub concept
	 * would lead to a cycle in the inheritance hierarchy.
	 * 
	 * @param c the concept to be added as sub concept
	 * @return true if successfully added, false otherwise
	 */
	public boolean addSubConcept(Concept c, boolean isNew) {
		if (c == null || (c == this)) {
			return false;
		}
		
		if (!isNew) {
			// check for cycles by inheritance
			Concept tmpDesc = this;
			while(tmpDesc != null && tmpDesc != prj) {
				if (tmpDesc.getName() != null && tmpDesc.getName()
						                                   .equals(c.getName())) {
					return false;
				}
				tmpDesc = tmpDesc.getSuperConcept();
			}
			
			// check for overridden attributes
			if (!checkOverriddenAtts(this, c)) {
				return false;
			}
		}
		
		
		// add new super concept
		subConcepts.put(c.getName(), c);
		c.superConcept = this;
		
		// adapt inheritance hierarchy
		TaxonomyFct inhFct = prj.getInhFct();
		SymbolDesc inhDesc = inhFct.getDesc();
		TaxonomyNode parent = null;
		if (this.equals(prj)) {
			parent = inhDesc;
		} else {
			parent = (SymbolAttribute)inhDesc.getAttribute(this.getName());
		}
		if (!isNew) {
			inhFct.getTaxonomy().getParentMap().put((SymbolAttribute)inhDesc.getAttribute(c.getName()), 
					parent);	
		} else {
			inhDesc.addSymbol(c.getName());
			SymbolAttribute a = (SymbolAttribute)inhDesc.getAttribute(c.getName());
			inhFct.getTaxonomy().getParentMap().put(a, 
					parent);	
			inhFct.getTaxonomy().getLeaves().add(a);
			inhFct.getTaxonomy().getSimilarityMap().put(a, Similarity.get(1.00));
		}
		
		// adapt cases/instances by notifying observers
		setChanged();
		notifyObservers();
		
		return true;
	}
	
	/**
	 * Checks whether there are attributes description in c (or sub concepts) which 
	 * might override attributes in concept.
	 * If there are such attribute descriptions, checks whether they can override
	 * the corresponding descriptions. Returns false, if there is at least
	 * one description in c that cannot override another description with the same name 
	 * in concept
	 *  
	 * @param c concept whose attributes are to be checked 
	 * @param concept concept whose attributes might get overridden
	 * @return true if there are no problems with overridden attribute descriptions, false otherwise
	 */
	private boolean checkOverriddenAtts(Concept concept, Concept c) {
		
		// first check attribute descriptions of c
		for (AttributeDesc a: c.getAttributeDescs().values()) {
			AttributeDesc overriddenAtt = concept.getAttributeDesc(a.getName());
			
			if (overriddenAtt != null && !a.canOverride(overriddenAtt)) {
				return false;
			}
		}
		// then check all sub concepts
		for (Concept sub: c.getSubConcepts().values()) {
			if (!checkOverriddenAtts(concept,sub)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Rename this c if possible.
	 * Sets the ID to ID if there is no other
	 * c with this ID. 
	 * @param id the new ID for this concept
	 * @throws Exception 
	 */
	public void setName(String id) throws Exception {
		if (!prj.hasConceptWithID(id)) {
			if (this != prj) {
				superConcept.renameSubConcept(this.ID,id);	
			}
			this.ID = id;	
			this.setChanged();
			notifyObservers();
		} else {
			throw new Exception("Concept with id \""+ id +"\" already exists!");
		}
	}
	
	/**
	 * Gets the attribute descriptions of this c
	 * Be aware that the inherited attribute descriptions are not returned by 
	 * this method. For all attribute descriptions belonging to this description 
	 * (including inherited ones) call {@link #getAllAttributeDescs()}
	 *  
	 * @return the attribute descriptions that directly belong to this 
	 *                                                               description
	 */
	public HashMap<String, AttributeDesc> getAttributeDescs() {
		return attDescs;
	}
	
	/**
	 * Gets the attribute descriptions of this c including inherited 
	 * attributes. Inherited attributes can be overridden if there is an 
	 * attribute with the same name in this c and in one of the super 
	 * concepts.
	 * 
	 * @return all attribute descriptions that belong to this description
	 */
	public HashMap<String, AttributeDesc> getAllAttributeDescs() {
		
		HashMap<String, AttributeDesc> result = new HashMap<String, 
															   AttributeDesc>();
		Vector<Concept> path = new Vector<Concept>();
		path.add(this);
		Concept tmpDesc = superConcept;
		while(tmpDesc != prj) {
			path.add(tmpDesc);
			tmpDesc = tmpDesc.getSuperConcept();
		}
		
		for (int i=path.size()-1; i>=0; i--) {
			result.putAll(path.get(i).getAttributeDescs());
		}
		return result;
	}

	/**
	 * Adds given description as attribute description to current c
	 * description. Do not add one attribute description to several c
	 * descriptions, but create a new one for each.
	 * 
	 * @param desc
	 *            the attribute description to be added to this description
	 * @throws Exception 
	 */
	public void addAttributeDesc(AttributeDesc desc) throws Exception {
		String name = desc.getName();
		
		if (hasDirectAttributeDesc(name)) {
			throw new Exception("Cannot add attribute description. " +
					"Concept \"" + ID + "\" already has an attribute description with name \"" + name +"\"");
		}
		
		// check whether super class has another attribute with this name
		AttributeDesc descOLD = this.getAllAttributeDescs().get(name);
		boolean override = false;
		
		if ((descOLD != null) && (desc != null)) {
			// check whether attribute can be overridden!
			if (!desc.canOverride(descOLD)) {
				throw new Exception("Cannot override the attribute with " +
						                                    "name \""+ name + "\" !");
			}
			override = true;
			descOLD.addObserver(desc);
		}
		// check whether sub class has another attribute with this name
		Vector<AttributeDesc> subDescs = getAttributesOfSubDescsForName(name);

		for(AttributeDesc currentDesc: subDescs) {
			// check whether attribute can be overridden!
			if (!currentDesc.canOverride(desc)) {
				throw new Exception("Cannot add attribute! There is a " +
						"naming conflict with an attribute called \"" + name 
						                   + "\"in a sub concept!");
			}
		}
		// add new description
		this.attDescs.put(name,desc);
		// add functions 
		if (override) {
			if (!(desc instanceof ConceptDesc)) {
				List<ISimFct> l = new LinkedList<ISimFct>(((SimpleAttDesc)descOLD).getSimFcts());
				for (ISimFct f : l) {
					f.clone(desc, false);
				}
			}
			// adapt amalgamation fcts
			for (AmalgamationFct f: getAvailableAmalgamFcts()) {
				Number weight = f.getWeight(descOLD);
				Boolean active = f.isActive(descOLD);
				Object activeFct = f.getActiveFct(descOLD);
				f.remove(descOLD);
				if (activeFct != null) {
					if (!(desc instanceof ConceptDesc)) { // -> SimpleAttDesc
						// get cloned active fct 
						List<ISimFct> list = ((SimpleAttDesc)desc).getSimFcts();
						for (ISimFct currentFct : list) {
							if (currentFct.getName().equals(((ISimFct)activeFct).getName())) {
								f.setActiveFct(desc, currentFct);
								break;
							}
						}
					} else {
						f.setActiveFct(desc, activeFct);
					}
					
					f.setActive(desc, active);
				}
				if (weight != null) {
					f.setWeight(desc.getName(), weight);
				}
			}
		}
		this.setChanged();
		notifyObservers(desc);

	}
	
	public boolean hasDirectAttributeDesc(String name) {
		return (getAttributeDescs().get(name)!=null);
	}
	
	public boolean hasAttributeDesc(String name) {
		return getAllAttributeDescs().containsKey(name);
	}

	/**
	 * Searches for an attribute with the given name. Adds for each
	 * subtree the first description appearing in this tree.
	 * @return list of descriptions with given name (only first appearance) 
	 */
	public Vector<AttributeDesc> getAttributesOfSubDescsForName(String name) {
		Vector<AttributeDesc> result = new Vector<AttributeDesc>();
		for(Concept sub: subConcepts.values()) {
			AttributeDesc att = sub.getAttributeDescs().get(name);
			if (att != null) {
				result.add(att);
			} else {
				result.addAll(sub.getAttributesOfSubDescsForName(name));
			}
		}
		return result;
	}
	
	/**
	 * Removes the attribute given by name from
	 * the attributes of this c.
	 * Does nothing if there is no attribute with this name 
	 * @param name
	 * @return the attribute description deleted, null if none has been deleted
	 */
	public boolean removeAttributeDesc(String name) {
		AttributeDesc desc = this.attDescs.remove(name);
		if (desc != null) {
			
			// delete part-of relation
			if (desc instanceof ConceptDesc) {
				((ConceptDesc)desc).getConcept().getPartOfRelations().remove(desc);
			}
			
			setChanged();
			notifyObservers(desc);
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see de.dfki.mycbr.core.model.AttributeDesc
	 *                      #canOverride(de.dfki.mycbr.core.model.AttributeDesc)
	 */
	public boolean canOverride(Concept c) {
		if(c.getAllSubConcepts().containsValue(this)) {
			return true;
		} else if (c.equals(this)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns the current project.
	 * @return the project
	 */
	public Project getProject() {
		return prj;
	}

	/**
	 * Returns the instances of this concept.
	 * Mark that cases are also instances.
	 * 
	 * @return all instances of this concept
	 */
	public Collection<Instance> getDirectInstances() {
		return range.getInstances();
	}

	/**
	 * Get all instances known for this concept
	 * including instances of sub concepts.
	 * 
	 * @return collection of all instances known for this
	 */
	public Collection<Instance> getAllInstances() {
		Collection<Instance> allInstForConcept = new LinkedList<Instance>(range.getInstances());

	    for (Concept sub : getSubConcepts().values()) {
	    	if (sub.getAllInstances().size()!=0) {
	    		allInstForConcept.addAll(sub.getAllInstances());
	    	}
	    }
	    return allInstForConcept;
	}

	/**
	 * Adds desc as a part of relation of this concept.
	 * This means that desc is a concept description which
	 * concept equals this. 
	 * 
	 * @param desc the description which has a part-of relation to this
	 */
	public void addPartOfRelation(ConceptDesc desc) {
		partOfRelations.add(desc);
	}
	
	/**
	 * Returns all part-of relation known for this concept.
	 * 
	 * @return all part-of relation known for this concept.
	 */
	public Vector<ConceptDesc> getPartOfRelations() {
		return partOfRelations;
	}
	
	/**
	 * Removes the given description as part-of relation.
	 * This function should either be called if the concept of
	 * desc changes or if desc is deleted.
	 * 
	 * @param desc the part-of relation to be removed
	 */
	public void deletePartOfRelation(ConceptDesc desc) {
		partOfRelations.remove(desc);
	}
	
	/**
	 * Deletes the concept with the given ID from this model
	 */
	public void delete() {
	
		// remove all fcts!
		availableAmalgamFcts = null;
		activeAmalgamationFct = null;
		
		// clean part-of relations
		for (ConceptDesc desc: getPartOfRelations()) {
			// part of relations become string attribute descriptions
			String n = desc.getName();
			Concept o = desc.getOwner();
			desc.delete();
			try {
				new StringDesc(o, n);
			} catch (Exception e) {
				System.err.println("Error when deleting a concept. Unable to reset part-of relation " + n);
			}
		}
		
		// clean attributes
		for (AttributeDesc desc: getAttributeDescs().values()) {
			desc.delete();
		}
		
		// remove from inheritance desc and super c
		getSuperConcept().removeSubConcept(ID);
		prj.getInhFct().getDesc().removeSymbol(ID);

		for (Instance i: range.getInstances()) {
			for (ICaseBase cb: prj.getCaseBases().values()) {	
				cb.removeCase(i.getName());
			}
		}
		range.clear();
		
		// clean sub concepts
		for (Concept sub: getSubConcepts().values()) {
			sub.delete();
		}
		
	}

	/**
	 * Returns the instance of this with name name and null
	 * if there is none.
	 * @param name
	 * @return instance of this with name name
	 */
	public Instance getInstance(String name) {
		Instance res = range.contains(name);
		if (res == null) {
			for (Concept c: subConcepts.values()) {
				res = c.getInstance(name);
				if (res!=null) {
					return res;
				}
			}	
		}
		return res;
	}

	/**
	 * Returns a new instance that can be used as a query object
	 * for retrieval.
	 * 
	 * @return query instance
	 */
    public Instance getQueryInstance() {
    	Instance query = new Instance(this, "query");
    	query.setAttsUnknown();
    	return query;
    }
    
	/**
	 * Adds a new instance to this concept's range.
	 * Returns the new instance. If there is an instance
	 * with the given name, returns this and does nothing.
	 * 
	 * @param name the name of the new instance
	 * @return the instance with name name
	 * @throws Exception if name is null or empty
	 */
	public Instance addInstance(String name) throws Exception {
		Instance i = range.contains(name);
		if (i == null) {
			i = range.getInstance(name);
			setChanged();
		}
		notifyObservers();
		return i;
	}
	
	public Instance copyInstance(Instance original, String newName) throws Exception {
		// check whether original belongs to this concept
		Instance i = range.contains(original.getName());
		if (i == null || !i.equals(original)) {
			return null;
		}
		// now get new instance and start copying
		i = range.contains(newName);
		if (i == null) {
			i = range.getInstance(newName);
			for (Map.Entry<AttributeDesc, Attribute> entry: original.getAttributes().entrySet()) {
				i.addAttribute(entry.getKey(), entry.getValue());
			}
			setChanged();
		}
		notifyObservers();
		return i;
	}
	
	/**
	 * Adds the given instance to this concept's range.
	 * 
	 * @param i the new instance
	 * @return true if i is added successfully, false otherwise.
	 */
	public boolean addInstance(Instance i) {
		boolean added = range.add(i);
		if (added) {
			setChanged();
			notifyObservers();
		}
		return added;
	}

	/**
	 * Renames the instance with name name.
	 * Does nothing if there is no such instance.
	 * 
	 * @param name name of the instance to be renamed
	 * @param name2 new name of the instance
	 */
	public void renameInstance(String name, String name2) {
		this.range.renameInstance(name, name2);
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Deletes the given function from the list of
	 * known functions for the given description.
	 * @param f the function to be deleted
	 */
	public void deleteAmalgamFct(AmalgamationFct f) {
		
		if (f == null) {
			return;
		}
		
		availableAmalgamFcts.remove(f.getName());
		
		// if f was active fct take another one as active!
		if (activeAmalgamationFct.equals(f)) {
			if (availableAmalgamFcts.size() == 0) {
				addAmalgamationFct(AmalgamationConfig.WEIGHTED_SUM, Project.DEFAULT_FCT_NAME, true);
			}
			activeAmalgamationFct = availableAmalgamFcts.values().iterator().next();
		}
		setChanged();
		notifyObservers(f);
		
	}
	
	/**
	 * Creates a new AmalgamationFct for the given description.
	 * 
	 * @param name
	 *            the description for which a new function should be created
	 * @return the new AmalgamationFct for description desc
	 */
	public AmalgamationFct addAmalgamationFct(AmalgamationConfig amalgam,
			String name, boolean active) {
		AmalgamationFct f = new AmalgamationFct(amalgam, this, name);
		availableAmalgamFcts.put(f.getName(), f);
		if (active) {
			activeAmalgamationFct = f;
		}
		setChanged();
		notifyObservers(f);
		return f;
	}
	
	/**
	 * Get the value of activeSimFcts
	 * 
	 * @return the value of activeSimFcts
	 */
	public AmalgamationFct getActiveAmalgamFct() {
		return activeAmalgamationFct;
	}

	/**
	 * Set the active amalgamation function for this.
	 * @param amalgam the new amalgamation function
	 */
	public void setActiveAmalgamFct(AmalgamationFct amalgam) {
		if (getAvailableAmalgamFcts().contains(amalgam)) {
			activeAmalgamationFct = amalgam;
			setChanged();
		}
		notifyObservers();
	}
	
	/**
	 * Returns the available similarity functions for the specified description
	 * 
	 * @return the available similarity functions for this
	 */
	public List<AmalgamationFct> getAvailableAmalgamFcts() {
		return new LinkedList<AmalgamationFct>(availableAmalgamFcts.values());
	}
	
	/**
	 * Should be called when name of an sub concept changes.
	 * Renames the concept given by nameOLD to nameNEW.
	 * Does nothing if there is no sub concept with name nameOLD
	 * 
	 * @param nameOLD the name of the sub concept to be renamed 
	 * @param nameNEW the new name
	 */
	public void renameAmalgamationFct(String nameOLD, String nameNEW) {
		AmalgamationFct fct = availableAmalgamFcts.remove(nameOLD);
		if (fct != null) {
			availableAmalgamFcts.put(nameNEW, fct);
			setChanged();
			notifyObservers();
		}
	}
	
	/**
	 * Returns the attribute description with the given name visible in this concept.
	 * There might be several descriptions with the given name, but the attribute description
	 * returned either belong directly to this concept, or is inherited from a super concept.
	 * 
	 * @param name the name of the attribute description to be returned
	 * @return the attribute with name name, null if there is none.
	 */
	public AttributeDesc getAttributeDesc(String name) {
		return getAllAttributeDescs().get(name);
	}

	/**
	 * Sets the name of the attribute description given by name to name2.
	 * Does nothing if there is no attribute description with name name 
	 * @param name
	 * @param name2
	 */
	public void renameAttDesc(String name, String name2) {
		AttributeDesc att = attDescs.remove(name);
		if (att != null) {
			attDescs.put(name2, att);
			setChanged();
		}
		notifyObservers();
	}

	/* (non-Javadoc)
	 * @see de.dfki.mycbr.core.explanation.IExplainable#getName()
	 */
	@Override
	public String getName() {
		return ID;
	}

	/* (non-Javadoc)
	 * @see de.dfki.mycbr.core.explanation.IExplainable#getExpType()
	 */
	@Override
	public Explainable getExpType() {
		return Explainable.Concept;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		setChanged();
		notifyObservers();
	}

	/**
	 * @param desc the description that now uses multiple attributes as values
	 */
	public void setAllInstancesMultiple(AttributeDesc desc) {
		range.setAllInstancesMultiple(desc);
		for (Concept c: subConcepts.values()) {
			c.setAllInstancesMultiple(desc);
		}
	}

	/**
	 * @param desc the description that now uses single attributes as values
	 */
	public void setAllInstancesSingle(AttributeDesc desc) {
		range.setAllInstancesSingle(desc);
		for(Concept c: subConcepts.values()) {
			c.setAllInstancesSingle(desc);
		}
	}
}
