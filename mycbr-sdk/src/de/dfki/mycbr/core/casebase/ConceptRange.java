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
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.model.AttributeDesc;
import de.dfki.mycbr.core.model.Concept;

/**
 * Holds ConceptAttribute objects for a given ConceptDesc object.
 *
 * @author myCBR Team
 *
 */
public final class ConceptRange extends Range {

    /**
     * Tells us how to compute similarity for values maintained by this range.
     */
    private Concept concept;

    /**
     * Associates a <code>ConceptAttribute</code> to each identifier.
     */
    private HashMap<String, Instance> instances;
    
    /**
     *
     */
    private Project prj;

    /**
     * Initializes map for special attributes.
     *
     * @param p the project this range belongs to
     * @param c
     *            the concept for values maintained by this range
     */
    public ConceptRange(final Project p, final Concept c) {
        super(p);
        concept = c;
        this.prj = p;
        instances = new HashMap<String, Instance>();
    }

    /**
     * Returns the ConceptAttribute associated with the given String. Creates a
     * new ConcpetAttribute if there is no ConceptAttribute for the given string
     * yet.
     *
     * @param name
     *            the identifier for which the corresponding ConceptAttribute
     *            should be returned
     * @return the ConceptAttribute specified by name
     * @throws Exception if name is empty
     */
    public Instance getInstance(final String name) throws Exception {
        Instance att = instances.get(name);
        if (att == null) {
        	att = new Instance(concept, name);
        }
        return att;
    }

    /**
     *
     * @param i the instance to be added to this range
     * @return true, if instance has been successfully added, else false.
     */
    public boolean add(final Instance i) {
        if (instances.get(i.getName()) == null) {
            instances.put(i.getName(), i);
            return true;
        }
        return false;
    }

    /**
     * Returns the ConceptAttribute associated with the given String.
     *
     * @param name
     *            the name of the instance to be returned
     * @return ConceptAttribute with name name or null if there is none with
     *         this name
     */
    public Instance contains(final String name) {
        Instance att = instances.get(name);
        return att;
    }

    /**
     * Gets the current c attributes maintained by this range.
     *
     * @return the existing c attributes of the given description
     */
    public Collection<Instance> getInstances() {
        return instances.values();
    }
    
    /*
     * (non-Javadoc)
     *
     * @see de.dfki.mycbr.core.casebase.Range#getAttribute(java.lang.Object)
     */
    /**
     * Gets the attribute associated with the specified <code>Object</code> obj.
     * obj is expected to be of type <code>String</code>. Returns result of
     * {@link #getInstance(String)} if obj is of type <code>String</code>,
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
            if (prj.isSpecialAttribute((String) obj)) {
                return prj.getSpecialAttribute((String) obj);
            } else {
                try {
					Instance i = concept.getInstance((String)obj);
					if (i == null) {
						i = getInstance((String)obj);
					}
					return i;
				} catch (Exception e) {
					e.printStackTrace();
				}
            }
        }
        return null;
    }

    /**
     * @return the concept this range belongs to
     */
    public Concept getConcept() {
        return concept;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
    /**
     * @param o the object that has changed
     * @param arg additional information
     */
    public void update(final Observable o, final Object arg) {
        // nothing to do
    }

    /**
     * @param name the old name of the instance
     * @param name2 new name of the instance
     */
    public void renameInstance(final String name, final String name2) {
        Instance i = this.instances.remove(name);
        instances.put(name2, i);
    }

    /*
     * (non-Javadoc)
     *
     * @see de.dfki.mycbr.core.casebase.Range#parseValue(java.lang.String)
     */
    @Override
    /**
     * @param s the string which should be parsed
     */
    public Attribute parseValue(final String s) throws Exception {
        return getInstance(s);
    }


	/**
	 * Called when changing the description from having multiple to single values.
	 */
	public void setAllInstancesSingle(AttributeDesc d) {
		
		for(Instance i: instances.values()) {
			Attribute a = i.getAttForDesc(d);
			if (a instanceof MultipleAttribute<?>) {
				MultipleAttribute<?> multiAtt = (MultipleAttribute<?>)a;
				List<Attribute> list = multiAtt.getValues();
				Attribute newAtt = null;
				if (list.size() != 0) {
					newAtt = list.get(0);
				} else {
					newAtt = prj.getSpecialAttribute(Project.UNDEFINED_SPECIAL_ATTRIBUTE);
				}
				i.addAttribute(d, newAtt);
			}
			
		}
	}

	/**
	 * Called when changing the description from having single to multiple values.
	 */
	public void setAllInstancesMultiple(AttributeDesc d) {
		
			for(Instance i: instances.values()) {
				Attribute a = i.getAttForDesc(d);
				if (!prj.isSpecialAttribute(a.getValueAsString())) {
					LinkedList<Attribute> l1 = new LinkedList<Attribute>();
					l1.add(a);
					MultipleAttribute<?> att = new MultipleAttribute<AttributeDesc>(d,l1);
					i.addAttribute(d, att);
				}
			}
		
	}

	/**
	 * 
	 */
	public void clear() {
		instances.clear();
	}

	/**
	 * @param name
	 */
	public void remove(String name) {
		// TODO Auto-generated method stub
		instances.remove(name);
		prj.removeCase(name);

	}
}
