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

import java.util.Observable;

import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Attribute;
import de.dfki.mycbr.core.casebase.MultipleAttribute;
import de.dfki.mycbr.core.casebase.SpecialAttribute;
import de.dfki.mycbr.core.casebase.SymbolAttribute;
import de.dfki.mycbr.core.model.AttributeDesc;
import de.dfki.mycbr.core.model.SymbolDesc;
import de.dfki.mycbr.core.similarity.config.MultipleConfig;
import de.dfki.mycbr.util.Pair;

/**
 * Holds the similarity of the known attributes for the given 
 * description in a two dimensional array (table). The indexes 
 * of the attributes in this table are maintained by the corresponding range
 * (given by the description). If this function is symmetric, the table should be
 * symmetric, too.
 * 
 * @author myCBR Team
 */
public class SymbolFct extends Observable implements ISimFct {

	/**
	 * Underlying array which holds the similarities of each pair of SymbolAttributes
	 */
	protected Similarity[][] sims;
	
	protected String name;
	
	/**
	 * The description of the given attributes
	 */
	protected SymbolDesc desc;
	
	protected boolean isSymmetric = true;
	protected Project prj;
	protected MultipleConfig mc = MultipleConfig.DEFAULT_CONFIG;
	
	/**
	 * Initializes this by asking the given description
	 * for the number of allowed symbols. This number determines
	 * the size of the underlying table. Calls {@link #initTable()}
	 * to fill the table with default values.
	 * 
	 * @param desc the description of the given symbol attributes 
	 */
	public SymbolFct(Project prj,SymbolDesc desc, String name) { 
		this.prj = prj;
		this.desc = desc;
		desc.addObserver(this);
		this.name = name;
		int count = 0;
		if (desc != null) {
			count = this.desc.getAllowedValues().size();
		}
		if (count != 0) {
			sims = new Similarity[count][count];
		} else {
			sims = new Similarity[10][10];
		}
		initTable();
	}
	
	/**
	 * Initializes this by transforming the given symbol function into
	 * a similarity table. 
	 * To change f by this function you have to call
	 * {@link SymbolDesc#deleteSimFct(ISimFct)} and then {@link SymbolDesc#addFct(ISimFct)}
	 * 
	 * @param f any symbol function 
	 */
	public SymbolFct(SymbolFct f) { 
		this.prj = f.getProject();
		this.desc = f.getDesc();
		desc.addObserver(this);
		this.name = f.getName();
		this.sims = f.sims;
		this.isSymmetric = f.isSymmetric;
		this.mc = f.mc;
	}
	
	/**
	 * Initializes the underlying table with default values.
	 * The diagonal is initialized with 1.00 and everything else with 0.00.
	 */
	protected void initTable() {
		
		for (int i = 0; i<sims.length; i++) {
			for (int j = 0; j<sims[i].length; j++) {
				sims[i][j] = i == j ? Similarity.get(1.00) : Similarity.get(0.00);
			}
		}
		
	}
	
	/**
	 * Returns the similarity of the given attributes.
	 * Returns null if the attributes do not have the same description,
	 * if they are not of type SymbolAttribute or SpecialValueAttribute,
	 * or if there is something wrong with the index structure provided by 
	 * the given range.
	 * @param attribute first attribute
	 * @param attribute2 second attribute
	 * @return similarity of the given attributes, invalid similarity if an error occurs
	 * @throws Exception 
	 * 
	 */
	public Similarity calculateSimilarity(Attribute attribute, Attribute attribute2) {
		Similarity result = Similarity.INVALID_SIM;
		if ((attribute instanceof SpecialAttribute) || (attribute2 instanceof SpecialAttribute) ){
			result = prj.calculateSpecialSimilarity(attribute, attribute2);	
		} else if (attribute instanceof MultipleAttribute<?> && attribute2 instanceof MultipleAttribute<?>) {
			result = prj
			.calculateMultipleAttributeSimilarity(this,((MultipleAttribute<?>)attribute), (MultipleAttribute<?>)attribute2);
		} else if((attribute instanceof SymbolAttribute) && (attribute2 instanceof SymbolAttribute) ){
			SymbolAttribute att1 = (SymbolAttribute)attribute;
			SymbolAttribute att2 = (SymbolAttribute)attribute2;
			
			if(att1.getAttributeDesc().equals(att2.getAttributeDesc())) {
				Integer index1 = desc.getIndexOf(att1);
				Integer index2 = desc.getIndexOf(att2);
				if (index1!=null && index2!=null) {
					try {
						result = sims[index1][index2];
					} catch(ArrayIndexOutOfBoundsException e) {
						e.printStackTrace();
					}
				}
				
			}
		} 
		return result;
	}
	
	/**
	 * Returns the similarity of the given attributes.
	 * Returns null if the attributes do not have the same description,
	 * if they are not of type SymbolAttribute or SpecialValueAttribute,
	 * or if there is something wrong with the index structure provided by 
	 * the given range.
	 * @param value1 first attribute
	 * @param value2 second attribute
	 * @return similarity of the given attributes, invalid similarity if an error occurs
	 * @throws Exception 
	 */
	public Similarity calculateSimilarity(String value1, String value2) throws Exception { 
		return calculateSimilarity(desc.getAttribute(value1), desc.getAttribute(value2));
	}
	
	/**
	 * Sets the similarity of att1 and att2 to a Similarity.get object
	 * initialized with sim. 
	 * Calls {@link #setSimilarity(Attribute, Attribute, Similarity)}
	 * 
	 * @param att1 the first attribute
	 * @param att2 the second attribute
	 * @param sim the Similarity.get
	 * @return true, if the similarity has been updated successfully, false otherwise
	 */
	public boolean setSimilarity(SymbolAttribute att1, SymbolAttribute att2, double sim) {
		return setSimilarity(att1, att2, Similarity.get(sim));
	}

	/**
	 * Sets the similarity of the given symbols to a Similarity.get object
	 * initialized with sim. 
	 * Calls {@link #setSimilarity(Attribute, Attribute, Similarity)}
	 * 
	 * @param symbol1 the first attribute's value
	 * @param symbol2 the second attribute's value
	 * @param sim the Similarity.get
	 * @return true, if the similarity has been updated successfully, false otherwise
	 */
	public boolean setSimilarity(String symbol1, String symbol2, double sim) {
		return setSimilarity(desc.getAttribute(symbol1), desc.getAttribute(symbol2), Similarity.get(sim));
	}
	
	/**
	 * Sets the similarity the given symbols to sim.
	 * Calls {@link #setSimilarity(Attribute, Attribute, Similarity)}
	 * 
	 * @param symbol1 the first attribute's value
	 * @param symbol2 the second attribute's value
	 * @param sim the Similarity.get
	 * @return true, if the similarity has been updated successfully, false otherwise
	 */
	public boolean setSimilarity(String symbol1, String symbol2, Similarity sim) {
		return setSimilarity(desc.getAttribute(symbol1), desc.getAttribute(symbol2), sim);
	}
	
	/**
	 * Sets the similarity of att1 and att2 to sim. 
	 * If this function is symmetric also sets the similarity of
	 * att2 and att1 to sim.
	 * Returns true if the similarity has been successfully updated,
	 * false if there is something wrong with the index structure provided by the given range
	 * or if the attributes are not contained in this range.
	 * 
	 * @param att1 the first attribute
	 * @param att2 the second attribute
	 * @param sim the Similarity.get
	 * @return true, if the similarity has been updated successfully, false otherwise
	 */
	public boolean setSimilarity(Attribute att1, Attribute att2, Similarity sim) {
		Integer index1 = desc.getIndexOf((SymbolAttribute)att1); 
		Integer index2 = desc.getIndexOf((SymbolAttribute)att2);
		if (index1!=null && index2!=null) {
				try {
					sims[index1][index2] = sim;
					if (isSymmetric()) {
						sims[index2][index1] = sim;
					}
					setChanged();
					notifyObservers();
					return true;
				} catch(ArrayIndexOutOfBoundsException e) {
					System.err.println("Problem with table in symbol function " + name);
				}
		}
		return false;
	}
			
	/**
	 * Expands the underlying table by one row and one column.
	 * The new cells are initialized with 0.00 and 1.00 respectively.
	 * Returns false if the attribute is not contained in the given range
	 * or if there is something wrong with the index returned by this range for the
	 * given attribute.
	 * 
	 * @param att the new attribute to be added for similarity computations
	 * @return true, if the table could be updated correctly, false otherwise
	 */
	protected boolean addAttribute(SymbolAttribute att) {
		Similarity[][] simsNew  = new Similarity[sims.length+1][sims.length+1];
		
		Integer index = desc.getIndexOf(att);
		if (index == null) {
			return false;
		}
		
		try {
			for (int i = 0; i<simsNew.length; i++) {
				for (int j = 0; j<simsNew[i].length; j++) {
					if (j == index) {
						simsNew[i][j] = i == j ? Similarity.get(1.00) : Similarity.get(0.00);
					} else {
						simsNew[i][j] = i == index ? Similarity.get(0.00) : sims[i][j];
					}
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
		
		sims = simsNew;
		setChanged();
		notifyObservers();

		return true;
	}
	
	/**
	 * Reduces the underlying table by one row and one column.
	 * Returns false if the attribute is not contained in the given range
	 * or if there is something wrong with the index returned by this range for the
	 * given attribute.
	 * 
	 * @param index the index of the attribute to be removed
	 * @return true, if the table could be updated correctly, false otherwise
	 */
	protected boolean removeAttribute(Integer index) {
		Similarity[][] simsNew  = new Similarity[sims.length-1][sims.length-1];
		
		if (index == null) {
			return false;
		}
		
		try {
			for (int i = 0; i<simsNew.length; i++) {
				for (int j = 0; j<simsNew[i].length; j++) {
					if (j < index) {
						simsNew[i][j] = i <= j ? sims[i][j] : sims[i+1][j];
					} else {
						simsNew[i][j] = i < index ? sims[i][j+1] : sims[i+1][j+1];
					}
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
		
		sims = simsNew;
		setChanged();
		notifyObservers();

		return true;
	}

	/* (non-Javadoc)
	 * @see de.dfki.mycbr.core.similarity.function.ISimilarityFunction#isSymmetric()
	 */
	@Override
	/**
	 * Specifies whether this function is symmetric or not.
	 * 
	 * @return true, if this function is symmetric, false otherwise
	 */
	public boolean isSymmetric() {
		return isSymmetric;
	}
	
	/**
	 * Specifies whether this function is symmetric or not.
	 * 
	 * @param symmetric true, if this function is symmetric, false otherwise
	 */
	public void setSymmetric(boolean symmetric) {
		if (symmetric != isSymmetric && symmetric) {
			// TODO: copy left right or right left...
		}
		isSymmetric = symmetric;
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Gets the symbol description of the attributes this function
	 * is defined on
	 * 
	 * @return symbol description of the given attributes
	 */
	public SymbolDesc getDesc() {
		return desc;
	}

	/* (non-Javadoc)
	 * @see de.dfki.mycbr.core.similarity.ISimFct#getName()
	 */
	@Override
	/**
	 * Returns the name of this function.
	 * @return name of this function
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see de.dfki.mycbr.core.similarity.ISimFct#setName(java.lang.String)
	 */
	@Override
	/**
	 * Sets the name of this function to name.
	 * Does nothing if there is another function with this name.
	 * @param name the name of this function
	 */
	public void setName(String name) {
		if (desc.getFct(name) == null) {
			desc.renameFct(this.name, name);
			this.name = name;
			setChanged();
			notifyObservers();
		}
	}

	/* (non-Javadoc)
	 * @see de.dfki.mycbr.core.similarity.ISimFct#getProject()
	 */
	@Override
	public Project getProject() {
		return prj;
	}

	/* (non-Javadoc)
	 * @see de.dfki.mycbr.core.similarity.ISimFct#getMultipleConfig()
	 */
	@Override
	public MultipleConfig getMultipleConfig() {
		return mc;
	}

	/* (non-Javadoc)
	 * @see de.dfki.mycbr.core.similarity.ISimFct#setMultipleConfig()
	 */
	@Override
	public void setMultipleConfig(MultipleConfig mc) {
		this.mc = mc;
		setChanged();
		notifyObservers();
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void update(Observable o, Object arg) {
		if (o.equals(desc)) {
			// attribute has been removed
			if (arg instanceof Pair<?, ?>) {
				Pair<SymbolAttribute,Integer> removedAtt = (Pair<SymbolAttribute,Integer>)arg;
				removeAttribute(removedAtt.getSecond());
			} else if (arg instanceof SymbolAttribute) { // attribute has been added
				addAttribute((SymbolAttribute)arg);
			}
		}
		setChanged();
		notifyObservers();
	}

	/* (non-Javadoc)
	 * @see de.dfki.mycbr.core.similarity.ISimFct#clone(de.dfki.mycbr.core.model.AttributeDesc)
	 */
	@Override
	public void clone(AttributeDesc descNEW, boolean active) {
		if (descNEW.getClass().equals(desc.getClass()) && !name.equals(Project.DEFAULT_FCT_NAME)) {
			SymbolFct f = ((SymbolDesc)descNEW).addSymbolFct(name, active);
			f.sims = this.sims.clone();
			f.isSymmetric = this.isSymmetric;
			f.mc = this.mc;
		}
	}
}
