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

package de.dfki.mycbr.core.explanation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.casebase.SimpleAttribute;
import de.dfki.mycbr.core.model.AttributeDesc;
import de.dfki.mycbr.core.model.Concept;

/**
 * Class ExplanationManager.
 */
public final class ExplanationManager extends Observable implements Observer {

    /**
     *
     */
    private boolean isEnabled = true;

    /**
     * explanations for model.
     */
    private HashMap<Concept, ConceptExplanation> conceptExp;
    /**
     *
     */
    private HashMap<AttributeDesc, ConceptExplanation> attributeDescExp;
    /**
     *
     */
    private HashMap<SimpleAttribute, ConceptExplanation> attributeExp;
    /**
     *
     */
    private HashMap<Instance, ConceptExplanation> instanceExp;

    /**
     *
     */
    public ExplanationManager() {
        conceptExp = new HashMap<Concept, ConceptExplanation>();
        attributeDescExp = new HashMap<AttributeDesc, ConceptExplanation>();
        attributeExp = new HashMap<SimpleAttribute, ConceptExplanation>();
        instanceExp = new HashMap<Instance, ConceptExplanation>();
    }

    /**
     *
     * @param c
     *            the concept to be explained
     * @return concept explanation for the given concept
     */
    public ConceptExplanation getExplanation(final Concept c) {
        ConceptExplanation exp = conceptExp.get(c);
        if (exp == null) {
            exp = new ConceptExplanation(c, this);
            conceptExp.put(c, exp);
            setChanged();
            notifyObservers();
        }
        return exp;
    }

    /**
     *
     * @param desc the attribute description to be explained
     * @return the concept explanation for the description
     */
    public ConceptExplanation getExplanation(final AttributeDesc desc) {
        ConceptExplanation exp = attributeDescExp.get(desc);
        if (exp == null) {
            exp = new ConceptExplanation(desc, this);
            attributeDescExp.put(desc, exp);
            setChanged();
            notifyObservers();
        }
        return exp;
    }

    /**
     *
     * @param att the simple attribute to be explained
     * @return the explanation for the given attribute
     */
    public ConceptExplanation getExplanation(final SimpleAttribute att) {
        ConceptExplanation exp = attributeExp.get(att);
        if (exp == null) {
            exp = new ConceptExplanation(att, this);
            attributeExp.put(att, exp);
            setChanged();
            notifyObservers();
        }
        return exp;
    }

    /**
     *
     * @param i the instance to be explained
     * @return the explanation for the given instance
     */
    public ConceptExplanation getExplanation(final Instance i) {
        ConceptExplanation exp = instanceExp.get(i);
        if (exp == null) {
            exp = new ConceptExplanation(i, this);
            instanceExp.put(i, exp);
            setChanged();
            notifyObservers();
        }
        return exp;
    }

    /**
     *
     * @param exp the new explanation for the given attribute
     * @param att the attribute to be explained
     */
    public void setExplanation(final ConceptExplanation exp,
            final SimpleAttribute att) {
        attributeExp.put(att, exp);
        setChanged();
        notifyObservers();
    }

    /**
     *
     * @param exp the explanation for the given instance
     * @param i the instance to be explained
     */
    public void setExplanation(final ConceptExplanation exp,
            final Instance i) {
        instanceExp.put(i, exp);
        setChanged();
        notifyObservers();
    }

    /**
    *
    * @param exp the explanation for the given concept
    * @param c the concept to be explained
    */
    public void setExplanation(final ConceptExplanation exp,
            final Concept c) {
        conceptExp.put(c, exp);
        setChanged();
        notifyObservers();
    }

    /**
    *
    * @param exp the explanation for the given attribute desc
    * @param desc the description to be explained
    */
    public void setExplanation(final ConceptExplanation exp,
            final AttributeDesc desc) {
        attributeDescExp.put(desc, exp);
        setChanged();
        notifyObservers();
    }

    /**
     *
     * @param c the concept to be explained
     * @param desc the text for the explanation
     * @return explanation for the given concept with description desc
     */
    public ConceptExplanation explain(final Concept c,
            final String desc) {
        ConceptExplanation res = getExplanation(c);
        res.setDescription(desc);
        return res;
    }


    /**
     *
     * @param desc the attribute description to be explained
     * @param description the text for the explanation
     * @return explanation for the given attribute description with text desc
     */
    public ConceptExplanation explain(final AttributeDesc desc,
            final String description) {
        ConceptExplanation res = getExplanation(desc);
        res.setDescription(description);
        return res;
    }

    /**
     *
     * @param att the simple attribute to be explained
     * @param description the text for the explanation
     * @return explanation for the given attribute with text desc
     */
    public ConceptExplanation explain(final SimpleAttribute att,
            final String description) {
        ConceptExplanation res = getExplanation(att);
        res.setDescription(description);
        return res;
    }

    /**
     *
     * @param i the instance to be explained
     * @param description the text for the explanation
     * @return explanation for the given attribute with text desc
     */
    public ConceptExplanation explain(final Instance i,
            final String description) {
        ConceptExplanation res = getExplanation(i);
        res.setDescription(description);
        return res;
    }

    /**
     *
     * @return list containing the known explanations
     */
    public LinkedList<ConceptExplanation> getExplanations() {
        LinkedList<ConceptExplanation> res
                        = new LinkedList<ConceptExplanation>();
        res.addAll(conceptExp.values());
        res.addAll(attributeDescExp.values());
        res.addAll(attributeExp.values());
        res.addAll(instanceExp.values());
        return res;
    }


    /**
     *
     * @param exp the object to be explained
     * @param desc the text for the explanation
     * @return explanation for the given object with text desc
     */
    public ConceptExplanation explain(final IExplainable exp,
                                            final String desc) {
        if (exp instanceof Concept) {
            return explain((Concept) exp, desc);
        } else if (exp instanceof AttributeDesc) {
            return explain((AttributeDesc) exp, desc);
        } else if (exp instanceof SimpleAttribute) {
            return explain((SimpleAttribute) exp, desc);
        } else if (exp instanceof Instance) {
            return explain((Instance) exp, desc);
        }
        return null;
    }

    /**
     *
     * @param enabled true, when the explanation manager should be enabled,
     *                                              false otherwise
     */
    public void setEnabled(final boolean enabled) {
        this.isEnabled = enabled;
    }

    /**
     *
     * @return true, when the explanation manager is enabled,
     *                                              false otherwise
     */
    public boolean isEnabeled() {
        return this.isEnabled;
    }
    
    public void removeExplanation(IExplainable exp) {
        if (exp instanceof Concept) {
            conceptExp.remove(exp);
        } else if (exp instanceof AttributeDesc) {
            attributeDescExp.remove(exp);
        } else if (exp instanceof SimpleAttribute) {
        	attributeExp.remove(exp);
        } else if (exp instanceof Instance) {
            instanceExp.remove(exp);
        }
        setChanged();
        notifyObservers();
    }

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		setChanged();
		notifyObservers();
	}
}
