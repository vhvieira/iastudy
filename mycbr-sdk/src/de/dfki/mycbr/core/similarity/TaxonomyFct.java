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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.SymbolAttribute;
import de.dfki.mycbr.core.model.AttributeDesc;
import de.dfki.mycbr.core.model.SymbolDesc;
import de.dfki.mycbr.core.similarity.config.TaxonomyConfig;

/**
 * Similarity function for symbol descriptions. The allowed values are
 * structured using a tree. The root is the symbol's description. The children
 * are maintained using a hash map. For each node in the tree (represented by
 * pairs) we save a SymbolAttribute or SymbolDesc respectively and a similarity
 * value of the children
 * 
 * @author myCBR Team
 */
public class TaxonomyFct extends SymbolFct {

	/**
	 * Defines semantics of inner nodes as values in query
	 */
	private TaxonomyConfig queryConfig = TaxonomyConfig.INNER_NODES_ANY;

	/**
	 * Defines semantics of inner nodes as values in case
	 */
	private TaxonomyConfig caseConfig = TaxonomyConfig.INNER_NODES_ANY;

	/**
	 * Tree like structure on which similarity is computed
	 */
	private Taxonomy taxonomy;
	private Project prj;

	/**
	 * Initializes this with a new taxonomy as internal data structure.
	 * 
	 * @param topSymbol
	 *            the description to which this function belongs to
	 * @param values
	 *            the allowed symbol for which similarity computations should be
	 *            done
	 */
	public TaxonomyFct(Project prj, SymbolDesc topSymbol,
			List<SymbolAttribute> values, String name) {
		super(prj, topSymbol, name);
		topSymbol.addObserver(this);
		
		this.prj = prj;
		taxonomy = new Taxonomy(topSymbol);
		//taxonomy.addObserver(this);
	}

	/**
	 * Updates the similarity for each attribute combination in the underlying
	 * table according to the similarity specified by the taxonomy
	 * 
	 * @throws Exception
	 */
	public void updateTable() {
		// update all values
		if ((queryConfig != TaxonomyConfig.NO_INNERNODES)
				&& (caseConfig != TaxonomyConfig.NO_INNERNODES)) {
			for (TaxonomyNode att1 : this.taxonomy.getTopSymbol()
					.getNodes()) {
				for (TaxonomyNode att2 : this.taxonomy.getTopSymbol()
						.getNodes()) {
					Similarity sim = computeSimilarityByTaxonomy(att1, att2);
					this.setSimilarity(att1.toString(), att2.toString(), sim);
				}
			}
		} else if (queryConfig != TaxonomyConfig.NO_INNERNODES) { 
			// update value only if case att is leaf
			for (TaxonomyNode att1 : this.taxonomy.getTopSymbol()
					.getNodes()) {
				for (TaxonomyNode att2 : this.taxonomy.getTopSymbol()
						.getNodes()) {
					if (taxonomy.isLeaf(att2)) {
						Similarity sim = computeSimilarityByTaxonomy(att1, att2);
						this.setSimilarity(att1.toString(), att2.toString(), sim);
					}
				}
			}
		} else if (caseConfig != TaxonomyConfig.NO_INNERNODES) { 
			// update only if query att is leaf
			for (TaxonomyNode att1 : this.taxonomy.getTopSymbol()
					.getNodes()) {
				for (TaxonomyNode att2 : this.taxonomy.getTopSymbol()
						.getNodes()) {
					if (taxonomy.isLeaf(att1)) {
						Similarity sim = computeSimilarityByTaxonomy(att1, att2);
						this.setSimilarity(att1.toString(), att2.toString(), sim);
					}
				}
			}
		} else { // update value only if query and case att are leaves
			for (TaxonomyNode att1 : this.taxonomy.getTopSymbol()
					.getNodes()) {
				for (TaxonomyNode att2 : this.taxonomy.getTopSymbol()
						.getNodes()) {
					if (taxonomy.isLeaf(att1) && taxonomy.isLeaf(att2)) {
						Similarity sim = computeSimilarityByTaxonomy(att1, att2);
						this.setSimilarity(att1.toString(), att2.toString(), sim);
					}
				}
			}
		}
		setChanged();
		notifyObservers();
	}

	/**
	 * Returns similarity of att1 and att2. Returns null if the attributes do
	 * not fit the given taxonomy configurations or if they are not of type
	 * SymbolAttribute or SpecialValueAttribute
	 * 
	 * @param att1
	 *            first attribute
	 * @param att2
	 *            second attribute
	 */
	private Similarity computeSimilarityByTaxonomy(TaxonomyNode att1,
			TaxonomyNode att2) {

		if (att1.equals(att2)) {
			return Similarity.get(1.00);
		} else if ((att1 instanceof SymbolAttribute)
				&& (att2 instanceof SymbolAttribute)) {

			SymbolAttribute symbolAtt1 = (SymbolAttribute) att1;
			SymbolAttribute symbolAtt2 = (SymbolAttribute) att2;

			if (!(prj.isSpecialAttribute(symbolAtt1.getValue()))
					&& !(prj.isSpecialAttribute(symbolAtt2.getValue()))) {
				boolean isAtt1Leaf = taxonomy.isLeaf(symbolAtt1);
				boolean isAtt2Leaf = taxonomy.isLeaf(symbolAtt2);

				if ((isAtt1Leaf) && (isAtt2Leaf)) {
					/* first case together with first if */

					// att1 and att2 are leaves in our taxonomy
					// the similarity is the according similarity of the parent
					// node
					TaxonomyNode ancestor = taxonomy.getCommonAncestor(symbolAtt1, symbolAtt2);
					return taxonomy.getSimilarity(ancestor);
				} else if (isAtt1Leaf) {

					// att1 is a leave and att2 is an inner node
					return getSimilarityForInnerNode(symbolAtt2, symbolAtt1,
							caseConfig);

				} else if (isAtt2Leaf) {

					// att2 is a leave and att1 is an inner node
					return getSimilarityForInnerNode(symbolAtt1, symbolAtt2,
							queryConfig);

				} else {
					// both attributes represent inner nodes in our taxonomy
					if (queryConfig == caseConfig) {
						/* fourth case or seventh (optimistic) case */
						if ((queryConfig == TaxonomyConfig.
								                         INNER_NODES_OPTIMISTIC)
								|| (queryConfig == TaxonomyConfig.
										                     INNER_NODES_ANY)) {
							if ((taxonomy.containsAsSubnode(symbolAtt1,
									symbolAtt2))
									|| (taxonomy.containsAsSubnode(symbolAtt2,
											symbolAtt1))) {
								return Similarity.get(1.00);
							} else {
								// the similarity is the according similarity of
								// the parent node
								TaxonomyNode ancestor = taxonomy.getCommonAncestor(symbolAtt1, symbolAtt2);
								return taxonomy.getSimilarity(ancestor);
							}
						} else if (queryConfig == TaxonomyConfig.
								                     INNER_NODES_PESSIMISTIC) {
							TaxonomyNode ancestor = taxonomy.getCommonAncestor(symbolAtt1, symbolAtt2);
							return taxonomy.getSimilarity(ancestor);
						} else {
							if ((!taxonomy.containsAsSubnode(symbolAtt1,
									symbolAtt2))
									&& (!taxonomy.containsAsSubnode(symbolAtt2,
											symbolAtt1))) {
								TaxonomyNode ancestor = taxonomy.getCommonAncestor(symbolAtt1, symbolAtt2);
								return taxonomy.getSimilarity(ancestor);
							} else if (symbolAtt1 == symbolAtt2) {
								// same as average for leave and inner node
								// combination
								int childCount = taxonomy.countLeaves(
										symbolAtt1);
								double sim = (1.0 + (childCount - 1)
										* taxonomy.getSimilarity(
												symbolAtt1).getValue())
										/ childCount;
								return Similarity.get(sim);
							} else {
								double factor = (1 / taxonomy.countLeaves(
										symbolAtt1))
										* (1 / (taxonomy.countLeaves(
												symbolAtt2)));
								double result = 0.00;
								for (TaxonomyNode child1 : taxonomy
										  .getLeaves(symbolAtt1)) {
									for (TaxonomyNode child2 : taxonomy
										  .getLeaves(symbolAtt2)) {
										result = result
												+ calculateSimilarity((SymbolAttribute)child1,
														(SymbolAttribute)child2).getValue();
									}
								}

								return Similarity.get(result * factor);
							}
						}

					}
				}
			} else {
				return prj.calculateSpecialSimilarity(
						(SymbolAttribute)att1, (SymbolAttribute)att2);
			}
		}
		return Similarity.INVALID_SIM;
	}

	/**
	 * Returns the similarity of att1 and att2 according to config. If the
	 * configuration does not correspond to the given SymbolAttributes, null is
	 * returned
	 * 
	 * @param att1
	 *            inner node
	 * @param att2
	 *            leaf node
	 * @param config
	 *            configuration of semantic of att1
	 * @return the similarity of att1 and att2 according to config, null if
	 *         attributes do not fit config
	 */
	private Similarity getSimilarityForInnerNode(SymbolAttribute att1,
			SymbolAttribute att2, TaxonomyConfig config) {
		if ((config == TaxonomyConfig.INNER_NODES_ANY)
				|| (config == TaxonomyConfig.INNER_NODES_OPTIMISTIC)) {

			/*
			 * second, third, fifth (optimistic), sixth (optimistic) case
			 * respectively
			 */
			if (taxonomy.containsAsSubnode(att1, att2)) {
				return Similarity.get(1.00);
			} else {
				// the similarity is the according similarity of the parent node
				TaxonomyNode ancestor = taxonomy.getCommonAncestor(att1, att2);
				return taxonomy.getSimilarity(ancestor);
			}

		} else if (config == TaxonomyConfig.INNER_NODES_PESSIMISTIC) {
			// the similarity is the according similarity of the parent node
			TaxonomyNode ancestor = taxonomy.getCommonAncestor(att1, att2);
			return taxonomy.getSimilarity(ancestor);
		} else if (config == TaxonomyConfig.INNER_NODES_AVERAGE) {
			/* fifth, sixth case (average) ? */
			if (!taxonomy.containsAsSubnode(att1, att2)) {
				TaxonomyNode ancestor = taxonomy.getCommonAncestor(att1, att2);
				return taxonomy.getSimilarity(ancestor);
			} else {
				int childCount = taxonomy.countLeaves(att1);
				double sim = (1.0 + (childCount - 1)
						* taxonomy.getSimilarity(att1).getValue())
						/ childCount;
				return Similarity.get(sim);
			}
		}
		return Similarity.INVALID_SIM;
	}

	public void setNodeSimilarity(TaxonomyNode att, Similarity sim) {
		this.taxonomy.setSimilarity(att, sim);
		setChanged();
		notifyObservers();
	}

	/**
	 * Gets the internal data structure of this function. All the values known
	 * to this function are located somewhere in the taxonomy and have
	 * similarities for their children associated.
	 * 
	 * @return the taxonomy underlying this similarity function
	 */
	public Taxonomy getTaxonomy() {
		return taxonomy;
	}

	/**
	 * @param queryConfig
	 *            the queryConfig to set
	 * @throws Exception
	 */
	public void setQueryConfig(TaxonomyConfig queryConfig) throws Exception {
		if (queryConfig!=this.queryConfig) {
			setChanged();
			if (isSymmetric()) {
				caseConfig = queryConfig;
			}
		}
		this.queryConfig = queryConfig;
		updateTable();
		notifyObservers();	
	}

	/**
	 * @return the queryConfig
	 */
	public TaxonomyConfig getQueryConfig() {
		return queryConfig;
	}

	/**
	 * @param caseConfig
	 *            the caseConfig to set
	 * @throws Exception
	 */
	public void setCaseConfig(TaxonomyConfig caseConfig) throws Exception {
		if (caseConfig != this.caseConfig) {
			setChanged();
			this.caseConfig = caseConfig;
			if (isSymmetric()) {
				queryConfig = caseConfig;
			}
			updateTable();
		}
		notifyObservers();
	}

	
	/**
	 * @return the caseConfig
	 */
	public TaxonomyConfig getCaseConfig() {
		return caseConfig;
	}

	/**
	 * Changes the parent of symbol to newParent.
	 * 
	 * @param symbol
	 *            the symbol to become a child of newParent
	 * @param newParent
	 *            the new parent of symbol
	 * @throws Exception
	 */
	public void setParent(TaxonomyNode symbol, TaxonomyNode newParent) {
		taxonomy.setParent(symbol, newParent);
		updateTable();
		setChanged();
		notifyObservers();
	}

	/**
	 * Returns the node of the taxonomy which represents the deepest ancestor of
	 * the given symbol attributes. A node of the taxonomy is represented by a
	 * pair of attribute (or description in case of root) and a similarity.
	 * 
	 * @param symbolAtt1
	 *            first attribute
	 * @param symbolAtt2
	 *            second attribute
	 * @return the common ancestor of the given attributes which is located
	 *         deepest in the taxonomy
	 */
	public Object getCommonAncestor(
			SymbolAttribute symbolAtt1, SymbolAttribute symbolAtt2) {
		return taxonomy.getCommonAncestor(symbolAtt1, symbolAtt2);
	}

	/**
	 * Updates the similarity of node root to sim
	 * 
	 * @param topSymbol
	 *            top symbol
	 * @param sim
	 *            Similarity.get
	 * @throws Exception
	 */
	void updateSimilarity(SymbolAttribute att, Similarity sim) throws Exception {
		taxonomy.setSimilarity(att, sim);
		updateTable();
		setChanged();
		notifyObservers();
	}

	/**
	 * Updates the similarity of node specified by att to sim
	 * 
	 * @param topSymbol
	 *            top symbol
	 * @param sim
	 *            Similarity.get
	 * @throws Exception
	 */
	void updateSimilarity(SymbolDesc desc, Similarity sim) throws Exception {
		taxonomy.setSimilarity(desc, sim);
		updateTable();
		setChanged();
		notifyObservers();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	/**
	 * Updates the underlying symbol table when the taxonomy changes. Calls
	 * {@link #updateTable()}
	 */
	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		try {
			if (arg == null) {
				updateTable();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the parent of the given node. Calls
	 * {@link Taxonomy#getParent(TaxonomyNode)}
	 * 
	 * @param currentNode
	 *            the node whose parent should be returned
	 * @return the parent of the given node
	 */
	public Object getParent(TaxonomyNode currentNode) {
		return taxonomy.getParent(currentNode);
	}
	
	/* (non-Javadoc)
	 * @see de.dfki.mycbr.core.similarity.ISimFct#clone(de.dfki.mycbr.core.model.AttributeDesc)
	 */
	@Override
	public void clone(AttributeDesc descNEW, boolean active) {
		if (descNEW.getClass().equals(desc.getClass()) && !name.equals(Project.DEFAULT_FCT_NAME)) {
			TaxonomyFct f = ((SymbolDesc)descNEW).addTaxonomyFct(name, active);
			f.sims = this.sims.clone();
			f.isSymmetric = this.isSymmetric;
			f.mc = this.mc;
			f.caseConfig = this.caseConfig;
			f.queryConfig = this.queryConfig;
			f.taxonomy = this.taxonomy;
		}
	}

	/**
	 * Returns the top symbol of the underlying taxonomy.
	 * @return top symbol of the underlying Taxonomy
	 */
	public TaxonomyNode getTopSymbol() {
		return taxonomy.getTopSymbol();
	}

	/**
	 * @param node the node 
	 * @return the similarity of the given node
	 */
	public Similarity getSimilarity(TaxonomyNode node) {
		return taxonomy.getSimilarity(node);
	}

	/**
	 * @return a collection of parent and child mappings
	 */
	public Collection<Map.Entry<TaxonomyNode, TaxonomyNode>> entrySet() {
		return taxonomy.getParentMap().entrySet();
	}
}
