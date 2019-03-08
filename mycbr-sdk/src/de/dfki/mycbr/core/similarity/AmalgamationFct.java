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

package de.dfki.mycbr.core.similarity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Attribute;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.casebase.MultipleAttribute;
import de.dfki.mycbr.core.casebase.SpecialAttribute;
import de.dfki.mycbr.core.model.AttributeDesc;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.model.ConceptDesc;
import de.dfki.mycbr.core.model.SimpleAttDesc;
import de.dfki.mycbr.core.similarity.config.AmalgamationConfig;

/**
 * This function is used to compute similarity of to ConceptAttribute objects.
 * First the similarity of the attributes of the concepts is computed and then
 * these values are amalgamated using this function. Possible types of
 * amalgamation functions are enumerated in AmalgamationConfig.
 * 
 * @author myCBR Team
 * 
 */
public class AmalgamationFct extends Observable implements Observer {

	/**
	 * Attributes may be inactive, meaning that they appear in the model but
	 * should not be used for similarity computations. A typical example is the
	 * attribute ID to identify an object. It usually does not make sense to
	 * define a similarity function for this attribute.
	 */
	HashMap<AttributeDesc, Boolean> active = new HashMap<AttributeDesc, Boolean>();

	/**
	 * specifies the active similarity function for this attribute description.
	 * the attribute itself knows its description and therefore knows how to
	 * compute its similarity to other values of the same description
	 */
	HashMap<AttributeDesc, Object> activeFcts = new HashMap<AttributeDesc, Object>();

	/**
	 * Specifies how the similarity of the single attributes of a given c should
	 * be amalgamated
	 */
	private AmalgamationConfig type;

	private String name;

	/**
	 * The description of the c attributes this function can be applied to.
	 */
	private Concept concept;

	/**
	 * Weights of the c's attributes. Are not taken into account for all
	 * possible amalgamation types. Initially all set to 1
	 */
	private HashMap<String, Number> weights;

	/**
	 * Initializes this with the given type, description, weights and
	 * inheritance similarity. Accessible from inside this package because only
	 * the FunctionContainer instance should create AmalgamationFct objects.
	 * 
	 * @param type
	 *            the amalgamation type (e.g. minimum, maximum, euclidean or
	 *            weighted sum)
	 * @param desc
	 *            the description of the c to which this function can be
	 *            applied.
	 * @param weights
	 *            the weights of c's attributes
	 * @param inheritanceSimilarity
	 *            similarity of the concepts appearing as descendants of this c
	 *            in the inheritance hierarchy.
	 */
	AmalgamationFct(AmalgamationConfig type, Concept c,
			HashMap<String, Number> weights, String name) {
		this.type = type;
		this.concept = c;
		c.addObserver(this);
		this.name = name;
		this.weights = weights;

		// check whether all attributes have an initial weight
		for (Iterator<AttributeDesc> it = concept.getAllAttributeDescs()
				.values().iterator(); it.hasNext();) {
			AttributeDesc att = it.next();
			if (!weights.containsKey(att)) {
				this.weights.put(att.getName(), 1.0); // get weights for inherited
													// atts
				this.active.put(att, true); // get active from inherited atts
				// take first available fct as active fct!
				if (!(att instanceof ConceptDesc)) {
					activeFcts.put(att, ((SimpleAttDesc)att).getSimFcts().get(0));
				} else {
					activeFcts.put(att, ((ConceptDesc)att).getConcept()
							.getAvailableAmalgamFcts().get(0));
				}

			}
		}

	}

	public Concept getConcept() {
		return concept;
	}
	
	/**
	 * Initializes this with the given type, description and inheritance
	 * similarity. Sets the weights of the c's attributes to 1.00. Accessible
	 * from inside this package because only the FunctionContainer instance
	 * should create AmalgamationFct objects.
	 * 
	 * @param type
	 *            the amalgamation type (e.g. minimum, maximum, euclidean or
	 *            weighted sum)
	 * @param c
	 *            the concept to which this function can be
	 *            applied.
	 * @param name the name of this function
	 */
	public AmalgamationFct(AmalgamationConfig type, Concept c, String name) {
		this.type = type;
		this.concept = c;
		this.name = name;
		this.weights = new HashMap<String, Number>();
		c.addObserver(this);

		// check whether all attributes have an initial weight
		for (Iterator<AttributeDesc> it = concept.getAllAttributeDescs()
				.values().iterator(); it.hasNext();) {
			AttributeDesc att = it.next();
			if (!weights.containsKey(att)) {
				this.weights.put(att.getName(), 1); 
				this.active.put(att, true);
				// take first available fct as active fct!
				if (!(att instanceof ConceptDesc)) {
					List<ISimFct> list = ((SimpleAttDesc)att).getSimFcts();
					if (list != null && list.size() != 0) {
						// take first function as active fct
						activeFcts.put(att, list.get(0));
					}
				} else {
					List<AmalgamationFct> list = ((ConceptDesc) att).getConcept().getAvailableAmalgamFcts();
					if (list != null && list.size() != 0) {
						// take first function as active fct
						activeFcts.put(att, list.get(0));
					}
				}

			}
		}
	}

	/**
	 * Returns the similarity of the given SimpleAttribute objects. These
	 * attributes are assumed to be instance of ConceptAttribute and should have
	 * the given description, else an invalid similarity is returned. Currently,
	 * there are four amalgamation types supported by this method.
	 * @param value1 the query attribute
	 * @param value2 the case attribute
	 * 
	 * @return similarity of the given attributes according to the amalgamation
	 *         type, invalid similarity if an error occurs
	 */
	public Similarity calculateSimilarity(Attribute value1, Attribute value2)
			throws Exception {
		double result = -1.0;
		if ((value1 instanceof SpecialAttribute)
				|| (value2 instanceof SpecialAttribute)) {
			// only possible for part of relation
			result = concept.getProject()
					.calculateSpecialSimilarity(value1, value2).getValue();
		} else if (value1 instanceof MultipleAttribute<?>
				&& value2 instanceof MultipleAttribute<?>) {
			result = concept.getProject()
					.calculateMultipleAttributeSimilarity(this,
							((MultipleAttribute<?>) value1),
							(MultipleAttribute<?>) value2).getValue();
		} else {
			Instance instance1 = (Instance) value1;
			Instance instance2 = (Instance) value2;
			Similarity maxSim = Similarity.get(0.0);
			Similarity minSim = Similarity.get(1.0);

			HashMap<AttributeDesc, Similarity> sims = new HashMap<AttributeDesc, Similarity>();
			double normalize = 0.0;
			// This only compares concepts based on common
			// attributes.
			for (AttributeDesc attDesc : concept.getAllAttributeDescs()
					.values()) {

				if (active.get(attDesc) != null && active.get(attDesc) != null) {

					Attribute qAtt = instance1.getAttForDesc(attDesc);
					Attribute cAtt = instance2.getAttForDesc(attDesc);
					Object f = activeFcts.get(attDesc);

					Similarity sim = Similarity.INVALID_SIM;
					if (f != null) {
						if (f instanceof ISimFct) {
							sim = ((ISimFct) f).calculateSimilarity(qAtt, cAtt);
						} else { // attDesc is ConceptDesc
							if (qAtt!=null) {
								sim = ((AmalgamationFct) f).calculateSimilarity(
									qAtt, cAtt);
							}/* else { // TODO: should not happen
								sim = ((AmalgamationFct) f).calculateSimilarity(
										concept.getProject().getSpecialAttribute(Project.UNDEFINED_SPECIAL_ATTRIBUTE), cAtt);
							}*/
						}
						
						double tmp = sim.getValue()
								* weights.get(attDesc.getName()).doubleValue();

						minSim = (tmp < minSim.getValue()) ? Similarity.get(tmp)
								: minSim;
						maxSim = (tmp > maxSim.getValue()) ? Similarity.get(tmp)
								: maxSim;
						sims.put(attDesc, sim);
						normalize += weights.get(attDesc.getName())
								.doubleValue();
					} else {
						System.err.println("Fct of att " + attDesc.getName() + " is null!");
					}
				} else {
					sims.put(attDesc, Similarity.get(0.00));
				}
			}
			switch (type) {
			case MAXIMUM:
				result = maxSim.getValue() / normalize;
				break;
			case MINIMUM:
				result = minSim.getValue() / normalize;
				break;
			case EUCLIDEAN:
				double tmp = 0.0;

				// sum the squared similarities
				for (Iterator<Map.Entry<AttributeDesc, Similarity>> it = sims
						.entrySet().iterator(); it.hasNext();) {
					Map.Entry<AttributeDesc, Similarity> currentEntry = it
							.next();

					if (active.get(currentEntry.getKey()) != null
							&& active.get(currentEntry.getKey())) {

						tmp = tmp
								+ (weights.get(currentEntry.getKey().getName())
										.doubleValue() / normalize)
								* Math.pow(currentEntry.getValue().getValue(),
										2);
					}
				}
				// and take the square root
				result = Math.sqrt(tmp);

				break;
			case WEIGHTED_SUM:
				tmp = 0.0;
				normalize = 0.0;

				for (Iterator<Map.Entry<AttributeDesc, Similarity>> it = sims
						.entrySet().iterator(); it.hasNext();) {
					Map.Entry<AttributeDesc, Similarity> currentEntry = it
							.next();
					if (active.get(currentEntry.getKey()) != null
							&& active.get(currentEntry.getKey())) {
						// sum the similarities and multiply with weights
						Number weight = weights.get(currentEntry.getKey()
								.getName());
						tmp = tmp + weight.doubleValue()
								* sims.get(currentEntry.getKey()).getValue();
						// remember to normalize with the sum of the weights
						// afterwards
						normalize = normalize + weight.doubleValue();
					}
				}
				// normalize with the sum of the weights
				if (tmp != 0.0 && normalize != 0.0)
					tmp = tmp / normalize;
				result = tmp;
				break;
			default:
				throw new Exception("AmalgamationConfig value unknown:" + type);
			}
		}
		Similarity res = Similarity.INVALID_SIM;
		if (result != -1.0) {
			res = Similarity.get(result);
		}
		return res;
	}

	/**
	 * The type for the amalgamation. At the moment the common and supported
	 * types are: Minimum, Maximum, Euclidean and Weighted Sum.
	 * 
	 * @return the amalgamation type of this function
	 * @see AmalgamationConfig
	 */
	public AmalgamationConfig getType() {
		return type;
	}

	/**
	 * Sets the type of this function to type. Type can be one of: Minimum,
	 * Maximum, Euclidean and Weighted Sum.
	 * 
	 * @param type
	 * @see AmalgamationConfig
	 */
	public void setType(AmalgamationConfig type) {
		this.type = type;
		setChanged();
		notifyObservers();
	}

	/**
	 * Returns the name of this function.
	 * 
	 * @return the name of this function.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of this function to name.
	 * Does nothing if there is another function with the given name.
	 * 
	 * @param name
	 *            the new name of this function.
	 */
	public void setName(String name) {
		if (concept.getFct(name)==null) {
			concept.renameAmalgamationFct(this.name, name);
			this.name = name;
			setChanged();
			notifyObservers();
		}
	}

	/**
	 * Sets the weight of the given attribute to weight. For some configurations
	 * of this function global weights can be used.
	 * 
	 * @param attDesc
	 *            the description of the attribute whose weight should be set
	 * @param weight
	 *            the new weight of the attribute
	 */
	public void setWeight(AttributeDesc attDesc, Number weight) {
		if (attDesc != null) {
			weights.put(attDesc.getName(), weight);
			setChanged();
			notifyObservers();
		}
	}

	/**
	 * Sets the weight of the given attribute to weight. For some configurations
	 * of this function global weights can be used.
	 * 
	 * @param name
	 *            the name of the attribute description whose weight should be set
	 * @param weight
	 *            the new weight of the attribute
	 */
	public void setWeight(String name, Number weight) {
		weights.put(name, weight);
		setChanged();
		notifyObservers();
	}

	public Project getProject() {
		return concept.getProject();
	}

	/**
	 * Attributes may be inactive, meaning that they appear in the model but
	 * should not be used for similarity computations. A typical example is the
	 * attribute ID to identify an object. It usually does not make sense to
	 * define a similarity function for this attribute.
	 * 
	 * @return true, if this attribute is used for similarity computations,
	 *         false otherwise
	 */
	public Boolean isActive(AttributeDesc att) {
		return active.get(att);
	}

	/**
	 * Sets this attribute to active or inactive. Attributes may be inactive,
	 * meaning that they appear in the model but should not be used for
	 * similarity computations. A typical example is the attribute ID to
	 * identify an object. It usually does not make sense to define a similarity
	 * function for this attribute.
	 * 
	 * @param active
	 *            true, if this attribute is used for similarity computations,
	 *            false otherwise
	 */
	public void setActive(AttributeDesc att, boolean active) {
		this.active.put(att, active);
		setChanged();
		notifyObservers();
	}

	/**
	 * Sets the active similarity function for this attribute description to
	 * activeSim. There may be several similarity functions for this description
	 * . However, there is one similarity
	 * that is currently used to compute similarities between values of this
	 * description. This similarity function is called active similarity
	 * function. The attribute (value) itself knows its description and
	 * therefore knows how to compute its similarity to other values of the same
	 * description.
	 * 
	 * @param activeSim
	 *            the activeSim to set
	 */
	public void setActiveFct(AttributeDesc att, Object activeSim) {
		if (activeSim instanceof ISimFct
				|| activeSim instanceof AmalgamationFct) {
			activeFcts.put(att, activeSim);
			setChanged();
			notifyObservers();
		}
	}

	/**
	 * Gets the active similarity function for this attribute description. There
	 * may be several similarity functions for this description maintained by
	 * . However, there is one similarity that is
	 * currently used to compute similarities between values of this
	 * description. This similarity function is called active similarity
	 * function. The attribute (value) itself knows its description and
	 * therefore knows how to compute its similarity to other values of the same
	 * description.
	 * 
	 * @return the active similarity function currently used to compute
	 *         similarities between values of this description
	 */
	public Object getActiveFct(AttributeDesc att) {
		return activeFcts.get(att);
	}

	public void remove(AttributeDesc desc) {
		weights.remove(desc.getName());
		active.remove(desc);
		activeFcts.remove(desc);
		setChanged();
		notifyObservers();
	}
	
	public Number getWeight(AttributeDesc desc) {
		return weights.get(desc.getName());
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable arg0, Object arg1) {

	}
}
