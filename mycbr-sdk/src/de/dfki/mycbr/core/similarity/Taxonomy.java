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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import de.dfki.mycbr.util.Pair;

/**
 * Represents the data structure used for taxonomy similarity functions.
 * Each symbol known for a given SymbolDescription represents one node in the tree. 
 * The SymbolDescription is the root. We associate to each node a similarity which
 * tells how similar the direct children of that symbol are. 
 * By default all of the symbols are direct children of the root. The default similarity
 * for the root node is 0.00. Leaves have similarity 1.00 by default
 *   
 * @author myCBR Team
 *
 */
public class Taxonomy extends Observable implements Observer {

	/**
	 * Highest element in taxonomy
	 */
	private TaxonomyNode topSymbol;
	
	/**
	 * Similarity of children of topSymbol
	 */
	private Similarity topSimilarity = Similarity.get(0.00);
	
	private HashMap<TaxonomyNode, Similarity> sims;
	
	private HashMap<TaxonomyNode, TaxonomyNode> parents;

	/**
	 * Leaves of the tree, they appear as nodes in the filed <code>nodes</code> with
	 * <code>1.00</code> as their associated similarity
	 */
	private List<TaxonomyNode> leaves;

	/**
	 * Initializes this taxonomy by using topSymbol as root and adding 
	 * values as its children. Consequently each SymbolAttribute in values
	 * becomes a leave in this taxonomy.
	 * 
	 * @param topSymbol
	 * @param values
	 */
	Taxonomy(TaxonomyNode topSymbol) {
		this.topSymbol = topSymbol;
		if (topSymbol instanceof Observable) {
			((Observable)topSymbol).addObserver(this);
		}
		parents = new HashMap<TaxonomyNode, TaxonomyNode>();
		sims = new HashMap<TaxonomyNode, Similarity>();
		sims.put(topSymbol, topSimilarity);
		for (TaxonomyNode att: topSymbol.getNodes()) {
			sims.put(att, Similarity.get(1.00));
			parents.put(att, topSymbol);
		}
		
		leaves = new LinkedList<TaxonomyNode>(topSymbol.getNodes());	
	}
	
	/**
	 * Checks whether the tree specified by top contains node as sub node.
	 *  
	 * @param top root of tree, assumed to be of type SymbolAttribute or SymbolDesc
	 * @param node node to search for, assumed to be of type SymbolAttribute or SymbolDesc
	 * @return true, if node is contained in tree specified by top, false otherwise.
	 */
	boolean containsAsSubnode(TaxonomyNode top, TaxonomyNode node) {
		if (top == null) {
			return false;
		} else if (top == node) {
			return true;
		} else if (top == topSymbol) {
			// top is root -> check if node occurs in taxonomy
			return topSymbol.getNodes().contains(node);
		} else {
			while (!node.equals(top) && node!=topSymbol) {
				if (parents.get(node) == null) {
					System.err.println("Broken taxonomy. No parent found for " + node);
				}
				node = parents.get(node);
				
			}
			return node.equals(top);
		}
	}
	
	/**
	 * Sets the parent of symbol to parent.
	 * This automatically updates the sub tree specified by symbol correctly.
	 * Parent is expected to be of type SymbolDesc, in case that parent is the topSymbol, else of type
	 * SymbolAttribute, otherwise does nothing. 
	 * 
	 * @param symbol the symbol which should be moved as a sub tree to parent
	 * @param newParent the new parent of the sub tree specified by symbol
	 */
	void setParent(TaxonomyNode symbol, TaxonomyNode newParent) {
		// we cannot add a node as child of a node
		// already contained in subtree, this would lead to infinite loop
		if (symbol == null || newParent == null || containsAsSubnode(symbol, newParent)) {
			return;
		}
		
		TaxonomyNode oldParent = parents.get(symbol);
		
		// and add it to the new parent
		parents.put(symbol, newParent);
		
		if (!parents.values().contains(oldParent)) {
			TaxonomyNode att = (TaxonomyNode)oldParent;
			leaves.add(att);
			sims.put(att, Similarity.get(1.00));
		}
		
		// the symbol has been successfully added to newParent
		// so this symbol cannot be a leave anymore
		// independent of being a leave or not
		leaves.remove(newParent);
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Gets the deepest common ancestor node of the given SymbolAttribute objects.
	 * The common parent is always the node which has both attributes as sub nodes and 
	 * is the node located deepest in the taxonomy having this property. 
	 * Two symbols contained in this taxonomy always have such a parent node.
	 * 
	 * @param symbolAtt1 the first symbol
	 * @param symbolAtt2 the second symbol
	 * @return the parent node of the two symbol attributes 
	 */
	TaxonomyNode getCommonAncestor(TaxonomyNode symbolAtt1,
			TaxonomyNode symbolAtt2) {
		
		if (symbolAtt1.equals(symbolAtt2)) {
			return symbolAtt1;
		}
		Vector<TaxonomyNode> parents1 = new Vector<TaxonomyNode>();
		Vector<TaxonomyNode> parents2 = new Vector<TaxonomyNode>();
		
		getParentsOf(symbolAtt1, parents1);
		getParentsOf(symbolAtt2, parents2);
		
		int counter1 = parents1.size()-1;
		int counter2 = parents2.size()-1;
		
		// search from the end of the vectors to the beginning
		// for the last element which is equal
		while (parents1.elementAt(counter1).equals(parents2.elementAt(counter2))) {
			counter1--;
			counter2--;
			if ((counter1==-1) || (counter2==-1) ){
				break;
			}
		}
		
		return parents1.elementAt(counter1+1);
		
	}

	/**
	 * Adds the parent of the given symbol to the specified list. 
	 * If called with an empty list, returns the path from the given
	 * node to the root.
	 * @param symbolAtt1 the symbol which parent should be added.
	 * 
	 */
	private void getParentsOf(
			TaxonomyNode node, Vector<TaxonomyNode> parents) {
		
		parents.add(node);
		
		TaxonomyNode currentParent = getParent(node);
		
		// recursive search for further parents
		if (currentParent != null) {
			getParentsOf(currentParent, parents);
		}
	}

	/**
	 * Says whether the given attribute is a leaf or not.
	 * Returns false in case that the SmybolAttribute object is not contained in this taxonomy.
	 * 
	 * @param att the symbol to be checked
	 * @return true, if att is a leave, false otherwise
	 */
	boolean isLeaf(TaxonomyNode att) {
		return leaves.contains(att);
	}
	
	/**
	 * Returns the Similarity object that corresponds to obj.
	 * In this taxonomy, each symbol attribute is represented by a node which also contains 
	 * a Similarity object. This similarity specifies how similar the leaves of this node are.
	 * obj might also be of type SymbolDesc since the top node of the taxonomy is 
	 * the symbol description.
	 * 
	 * @param obj the object for which the similarity is returned
	 * @return the similarity corresponding to obj
	 */
	public Similarity getSimilarity(TaxonomyNode obj) {
		Similarity sim = sims.get(obj);
		if (sim == null) {
			sim = Similarity.INVALID_SIM;
		}
		return sim;
	}
	
	/**
	 * Returns the list of leaves specified by the taxonomy given by top.
	 * 
	 * @param top the root of the taxonomy whose leaves should be returned
	 * @return the leaves of the taxonomy given by top
	 */
	List<TaxonomyNode> getLeaves(TaxonomyNode top) {
		List<TaxonomyNode> result = new LinkedList<TaxonomyNode>();
		if (top.equals(topSymbol)) {
			return leaves;
		} else {
			if (top != null) {
				for (TaxonomyNode leaf: leaves) {
					if (containsAsSubnode(top, leaf)) {
						result.add(leaf);
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * Returns the number of leaves specified by the subtree given by top.
	 * 
	 * @param top the node whose number of leaves should be returned
	 * @return the number of leaves of the taxonomy given by top
	 */
	int countLeaves(TaxonomyNode top) {
		if (top == topSymbol) {
			return leaves.size();
		} else {
			int counter = 0;
			for (TaxonomyNode leaf: leaves) {
				if (containsAsSubnode(top, leaf)) {
					counter++;
				}
			}
			return counter;
		}
	}
	
	/**
	 * Gets the top symbol of this taxonomy
	 * 
	 * @return the top symbol of this taxonomy
	 */
	public TaxonomyNode getTopSymbol() {
		return topSymbol;
	}
	
	/**
	 * Updates the similarity of node specified by att to sim
	 * 
	 * @param topSymbol top symbol 
	 * @param sim Similarity.get
	 */
	void setSimilarity(TaxonomyNode att, Similarity sim) {
		if (att == null || sim == null) {
			return;
		} 
		if (att.equals(topSymbol)) {
			sims.put((TaxonomyNode)att, sim);
			topSimilarity = sim;
			setChanged();
		} else if (topSymbol.getNodes().contains(att)) {
			sims.put((TaxonomyNode)att, sim);
			setChanged();
		} else {
			System.err.println("Could not update taxonomy similarity of node " + att);
		}
		notifyObservers();
	}

	/**
	 * Returns the parent node of the given node.
	 * Returns null if the given node is not a node
	 * of this taxonomy or if it is the root.
	 * @param currentNode the node whose parent should be returned
	 * @return the parent of the given node if there is one, null otherwise
	 */
	public TaxonomyNode getParent(TaxonomyNode currentNode) {
		return parents.get(currentNode);
	}
	
	/**
	 * Returns the children of the given node.
	 * A node is either topSymbol or a 
	 * symbolAttribute
	 * 
	 * @param node the node whose children should be returned
	 * @return the children of the given node, empty list if there are none
	 */
	public List<TaxonomyNode> getChildren(TaxonomyNode node) {
		List<TaxonomyNode> l = new LinkedList<TaxonomyNode>();
		for (Map.Entry<TaxonomyNode, TaxonomyNode> entry: parents.entrySet()) {
			if (entry.getValue().equals(node)) {
				l.add(entry.getKey());
			}
		}
		return l;
	}
	
	public HashMap<TaxonomyNode, Similarity> getSimilarityMap() {
		return sims;
	}
	
	public HashMap<TaxonomyNode, TaxonomyNode> getParentMap() {
		return parents;
	}
	
	public List<TaxonomyNode> getLeaves() {
		return leaves;
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void update(Observable o, Object arg) {
		if (o.equals(topSymbol)) {
			if (topSymbol.getNodes().size() < sims.size() && arg != null && arg instanceof Pair) {
			    // attribute has been removed
				Pair<TaxonomyNode,Integer> removedAtt = (Pair<TaxonomyNode,Integer>)arg;
				removeAttribute(removedAtt.getFirst());
			} else if (topSymbol.getNodes().size() > sims.size()) {
				// attribute has been added
				addAttribute((TaxonomyNode)arg);
			}

		}
	}

	/**
	 * Removes the given attribute from this taxonomy.
	 * All children of a will point to a's parent.
	 * 
	 * @param a the attribute to be removed
	 */
	private void removeAttribute(TaxonomyNode a) {

		TaxonomyNode parent = getParent(a);
		List<TaxonomyNode> l = getChildren(a);
		
		for (TaxonomyNode att: l) {
			setParent(att, parent);
		}
		parents.remove(a);
		sims.remove(a);
	}
	
	private void addAttribute(TaxonomyNode a) {
		// there is no node that corresponds to symbol
		// add a new node, it will automatically become a leave
		// so there is no similarity defined
		sims.put(a, Similarity.get(1.00));
		leaves.add(a);
	}
}
