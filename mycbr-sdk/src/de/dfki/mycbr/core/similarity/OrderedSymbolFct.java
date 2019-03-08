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
import java.util.Observable;
import java.util.Observer;

import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Attribute;
import de.dfki.mycbr.core.casebase.SymbolAttribute;
import de.dfki.mycbr.core.model.AttributeDesc;
import de.dfki.mycbr.core.model.IntegerDesc;
import de.dfki.mycbr.core.model.SymbolDesc;
import de.dfki.mycbr.util.Pair;

/**
 * This function provides a linear order of the known symbol attributes
 * for the given description. These symbol attributes are then compared
 * using the internal similarity function on the indexes. Therefore,
 * the internal similarity function is instance of IntegerSimilarityFunction.
 * 
 * @author myCBR Team
 *
 */
public class OrderedSymbolFct extends SymbolFct implements Observer {

	/**
	 * Specifies an integer for each symbol attribute resulting in a linear order
	 */
	private HashMap<SymbolAttribute, Integer> order;
	
	private IntegerFct internalFunction;
	private IntegerDesc internalDesc;
	private boolean isCyclic;
	
	private int highestOrder = 1;
	private int minOrder = 1;
	
	private int distanceLastFirst = 1;
	
	
	/**
	 * Initializes this with the given description.
	 * @param desc the description of the attributes to which this function can be applied
	 * @throws Exception 
	 */
	public OrderedSymbolFct(Project prj, SymbolDesc desc, String name) throws Exception {
		super(prj, desc, name);
		desc.addObserver(this);
		order = new HashMap<SymbolAttribute, Integer>(desc.getAllowedValues().size());
		internalDesc = new IntegerDesc(prj, name.concat("Internal"),0,desc.getAllowedValues().size());
		
		internalFunction = new IntegerFct(prj, internalDesc, name + "Internal");
		internalFunction.addObserver(this);
		this.addObserver(internalFunction);
		int i = 1;
		for(SymbolAttribute att: desc.getSymbolAttributes()) {
			order.put(att, i);
			highestOrder = i;
			i = i+1;
		} 
		internalDesc.setMin(minOrder);
		internalDesc.setMax(highestOrder);
	}

	/**
	 * Updates the similarity for each attribute combination in the underlying table according
	 * to the similarity specified by the taxonomy
	 * @throws Exception 
	 */
	public void updateTable() throws Exception {
		for (SymbolAttribute att1: desc.getSymbolAttributes()) {
			for (SymbolAttribute att2: desc.getSymbolAttributes()) {
				
				Integer queryInt = order.get(att1);
				Integer caseInt = order.get(att2);
				
				if (queryInt==null || caseInt==null) {
					System.err.println("Could not find integer representation for symbol [" 
							+ att1.getValueAsString() + "," + queryInt + "] or [" + att2.getValueAsString() + "," + caseInt + "].");
					return;
				}
				
				// care about cyclic mode
				if (isCyclic()) {
					int range = minOrder + distanceLastFirst - highestOrder;

					int diff1 = queryInt.intValue() - caseInt.intValue();
					if (diff1 < -(range/2)) {
						queryInt = new Integer(queryInt.intValue()+range);
					} else if (diff1 >(range/2)) {
						queryInt = new Integer(queryInt.intValue()-range);
					}
				}
				
				Similarity sim = internalFunction.calculateSimilarity(order.get(att1), order.get(att2));
				super.setSimilarity(att1, att2, sim);
			}
		}
		setChanged();
		notifyObservers();
	}


	/**
	 * Changes the range of the internal similarity measure.
	 * @param minimum new minimum
	 * @param maximum new maximum
	 */
	private void changeRange(int minimum, int maximum, boolean isCyclic, int distLastFirst) {
		
		// set new values
		minOrder = minimum;
		highestOrder = maximum;
		this.isCyclic = isCyclic;
		this.distanceLastFirst = distLastFirst;
		
		if (isCyclic) {
			// change range of internal function
			// because range is only half as wide as before
			
			int range = highestOrder+distLastFirst-minOrder;
			int rh = range/2;
			int maxDist = 0;
			for (Iterator<Integer> it1 = order.values().iterator(); it1.hasNext();) {
				Number v1 = it1.next();
				for (Iterator<Integer> it2 = order.values().iterator(); it2.hasNext();) {
					Number v2 = it2.next();
					
					int diff = Math.abs(v1.intValue()-v2.intValue());
					if (diff > rh) {
						diff -= rh;
					}
					
					if (diff > maxDist) {
						maxDist = diff;
					}
				}
					
			}
			
			internalDesc.setMax(minOrder+highestOrder);
			internalDesc.setMin(minOrder);
			
		} else {
			internalDesc.setMax(highestOrder);
			internalDesc.setMin(minOrder);
		}
		
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	/**
	 * For the similarity computations the values 
	 * in internal table are relevant. It might be that they
	 * are inconsistent with the internal function.
	 * If there are changes made to the internal similarity function
	 * then this function gets informed via the observable pattern
	 * and updates the internal table in case that the flag <code>keepTableConsistent</code>
	 * is set. Else sets the flag <code>consistent</code> to false.
	 */
	public void update(Observable o, Object arg1) {
		
		if (o.equals(internalFunction)) {
			try {
				updateTable();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (o.equals(desc)) {
			if (arg1 instanceof Pair<?,?>) {
				Pair<SymbolAttribute,Integer> removedAtt = (Pair<SymbolAttribute,Integer>)arg1;
				removeSymbol(removedAtt.getFirst());
			} else if (arg1 instanceof SymbolAttribute) { // attribute has been added
				addSymbol((SymbolAttribute)arg1);
			}
		}
		setChanged();
		notifyObservers();
	}

	public HashMap<SymbolAttribute, Integer> getOrder() {
		return order;
	}
	/**
	 * Gets the internal similarity function.
	 * For the similarity computations the values 
	 * in internal table are relevant. It might be that they
	 * are inconsistent with the internal function.
	 * If there are changes made to the internal similarity function
	 * then this function gets informed via the observable pattern
	 * and updates the internal table in case that the flag <code>keepTableConsistent</code>
	 * is set. Else sets the flag <code>consistent</code> to false.
	 * 
	 * @return internal similarity function
	 */
	public IntegerFct getInternalFunction() {
		return internalFunction;
	}
	
	/**
	 * Removes the given attribute from the list of 
	 * ordered symbols and updates the internal tabel
	 * 
	 * @param att the attribute to be removed
	 */
	public void removeSymbol(SymbolAttribute att) {
		order.remove(att);
	}
	
	/**
	 * Adds the given attribute to the list of ordered symbols.
	 * This symbols gets the order <code>highestOrder + 10</code>.
	 * If this attribute is already contained, does nothing.
	 * Adds this attribute to the internal table, too.
	 *  
	 * @param att the attribute to be added
	 */
	public void addSymbol(SymbolAttribute att) {
		if (desc.isAllowedValue(att.getValue())) {
			if (!order.containsKey(att)) {
				highestOrder = highestOrder+1;
				order.put(att, highestOrder);
				addAttribute(att);
				internalDesc.setMax(highestOrder);
			}
		}
	}
	
	/**
	 * Sets the index of att used to linearly order the attributes to index.
	 * @param att the attribute whose index should be set
	 * @param index the index to set
	 * @throws Exception 
	 */
	public void setOrderIndexOf(SymbolAttribute att, int index) throws Exception {
		boolean updateHighestOrder = order.get(att).equals(highestOrder);
		boolean updateMinOrder = order.get(att).equals(minOrder);
		
		if (desc.isAllowedValue(att.getValue())) {
			highestOrder = index > highestOrder ? index : highestOrder;
			minOrder = index < minOrder ? index : minOrder;
			
			order.put(att, index);
			if (updateHighestOrder) {
				highestOrder = index;
				for (Integer i: order.values()) {
					highestOrder = Math.max(highestOrder, i);
				}
			}
			if (updateMinOrder) {
				minOrder = index;
				for (Integer i: order.values()) {
					minOrder = Math.min(minOrder, i);
				}
			}
			if (updateMinOrder || updateHighestOrder) {
				changeRange(minOrder, highestOrder, isCyclic, distanceLastFirst);
			}
			updateTable();
		}
	}

	/**
	 * Sets the index of att used to linearly order the attributes to index.
	 * @param att the attribute whose index should be set
	 * @param index the index to set
	 * @throws Exception 
	 */
	public void setOrderIndexOf(String att, int index) throws Exception {
		Attribute att1 = desc.getAttribute(att);
		if (att1 instanceof SymbolAttribute) {
			setOrderIndexOf((SymbolAttribute)att1, index);
		} // else SpecialValueAttribute
	}
	
	/**
	 * Returns whether this function is cyclic or not.
	 * @return true if this function is cyclic, false otherwise.
	 */
	public boolean isCyclic() {
		return isCyclic;
	}
	
	/**
	 * Specifies whether this function is cyclic or not.
	 * @param cyclic true if this function is cyclic, false otherwise.
	 */
	public void setCyclic(boolean cyclic) {
		this.isCyclic = cyclic;
		changeRange(minOrder, highestOrder, cyclic, distanceLastFirst);
		setChanged();
		notifyObservers();
	}
	
	// override methods of SymbolFct that should do nothing here
	@Override
	public boolean setSimilarity(SymbolAttribute att1, SymbolAttribute att2, double sim) {return false;}
	@Override
	public boolean setSimilarity(String symbol1, String symbol2, double sim) {return false;}
	@Override
	public boolean setSimilarity(String symbol1, String symbol2, Similarity sim) {return false;}
	@Override
	public boolean setSimilarity(Attribute att1, Attribute att2, Similarity sim) { return false; }
	
	/* (non-Javadoc)
	 * @see de.dfki.mycbr.core.similarity.ISimFct#clone(de.dfki.mycbr.core.model.AttributeDesc)
	 */
	@Override
	public void clone(AttributeDesc descNEW, boolean active) {
		if (descNEW.getClass().equals(desc.getClass()) && !name.equals(Project.DEFAULT_FCT_NAME)) {
			OrderedSymbolFct f = ((SymbolDesc)descNEW).addOrderedSymbolFct(name, active);
			f.sims = this.sims.clone();
			f.isSymmetric = this.isSymmetric;
			f.mc = this.mc;
			f.highestOrder = this.highestOrder;
			f.internalDesc = this.internalDesc;
			f.internalFunction = this.internalFunction;
			f.isCyclic = this.isCyclic;
			f.minOrder = this.minOrder;
			f.order = this.order;
			f.distanceLastFirst = this.distanceLastFirst;
		}
	}

	/**
	 * @param distanceLastFirst the distanceLastFirst to set
	 */
	public void setDistanceLastFirst(int distanceLastFirst) {
		this.distanceLastFirst = distanceLastFirst;
		changeRange(minOrder, highestOrder, isCyclic, distanceLastFirst);
		setChanged();
		notifyObservers();
	}

	/**
	 * @return the distanceLastFirst
	 */
	public int getDistanceLastFirst() {
		return distanceLastFirst;
	}
}
