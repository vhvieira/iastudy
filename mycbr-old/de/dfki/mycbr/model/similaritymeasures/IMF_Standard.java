/**
 MyCBR License 1.1

 Copyright (c) 2008
 Thomas Roth-Berghofer, Armin Stahl & Deutsches Forschungszentrum f&uuml;r K&uuml;nstliche Intelligenz DFKI GmbH
 Further contributors: myCBR Team (see http://mycbr-project.net/contact.html for further information 
 about the mycbr Team). 
 All rights reserved.

 MyCBR is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 Since MyCBR uses some modules, you should be aware of their licenses for
 which you should have received a copy along with this program, too.
 
 endOfLic**/
package de.dfki.mycbr.model.similaritymeasures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jdom.Element;
import org.jdom.JDOMException;

import de.dfki.mycbr.CBRProject;
import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.XMLConstants;
import de.dfki.mycbr.model.similaritymeasures.smftypes.FakeSlot;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Symbol_Taxonomy;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel_Symbol_Taxonomy;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.modelprovider.ModelProvider;
import de.dfki.mycbr.retrieval.Explanation;

/**
 * 
 * @author myCBR Team
 *
 */
public class IMF_Standard implements Serializable {
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(IMF_Standard.class.getName());

	private static final String NAME_FAKESLOT		= "_FAKESLOT_IMF_";
	private static final String NAME_INTERNAL_SMF 	= "_IMF_INTERNAL_SMF_";

	/** this IMF treats exactly those classes that are subclass of topCls (and topCls itself). */
	private ModelCls topCls;
	
	/** contains all subclasses of topCls (including topCls itself). */
	private Collection<ModelCls> affectedCls = new ArrayList<ModelCls>();

	/** class hierarchy is mapped onto a tree structure. 
	 * This is the root. User object is the name of topCls. 
	 * NOTE: this is supposed to always be the same object as in the internal smf (SMF_symbol_tax).
	 */
	private DefaultMutableTreeNode root;

	/** maps a cls name to its corresponding tree node. */
	private HashMap<String, DefaultMutableTreeNode> clsnameToNode = new HashMap<String, DefaultMutableTreeNode>();

	/** the reference to the GUI. */
	private SMFPanel_Symbol_Taxonomy imfPanel;
	
	/** since we re-use SMF_Symbol_Taxonomy, there must be a Slot on which the internal smf can work. */
	private FakeSlot fakeSlot = null;
	private SMF_Symbol_Taxonomy smf_internal = null;
	
	public IMF_Standard(ModelCls topCls) {
		this.topCls = topCls;
		affectedCls = findAffectedCls(topCls);
		initFakeSlot();
		
		try {
			root = buildUpTree(topCls, 0, true);
			smf_internal = new SMF_Symbol_Taxonomy(fakeSlot, NAME_INTERNAL_SMF);

			// replace smf_taxonomy nodes with this node
			DefaultMutableTreeNode tax = smf_internal.getTaxonomy();
			tax.removeAllChildren();
			tax.add(root);
			
			initSimilarityValues();	
		} catch (Exception e) {
			log.log(Level.SEVERE, "could not initialize internal SMF", e);
		}
	}

	/**
	 * Constructor for creating IMF_Standard object out of XML file.
	 * Only used at the beginning when the myCBR Project is created.
	 * @param inst the top class
	 * @param element the jdom element specifying this IMF_Standard object
	 * @throws JDOMException if an exception occurs while parsing the xml file
	 */
	@SuppressWarnings("unchecked")
	public IMF_Standard(ModelInstance inst, Element element) throws JDOMException {
		topCls = (ModelCls) inst;
		affectedCls = findAffectedCls(topCls);
		initFakeSlot();
		
		try {
			smf_internal = new SMF_Symbol_Taxonomy(fakeSlot, element);
			root = (DefaultMutableTreeNode) smf_internal.getTaxonomy().getChildAt(0);
			for (Enumeration en=root.breadthFirstEnumeration(); en.hasMoreElements();) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
				clsnameToNode.put((String) node.getUserObject(), node);
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "could not initialize internal SMF", e);
		}
	}

	private void initFakeSlot() {
		fakeSlot = new FakeSlot(NAME_FAKESLOT, ValueType.SYMBOL);
		Collection<String> allowedValues = findAllowedValues();
		fakeSlot.setAllowedValues(allowedValues);
	}

	private DefaultMutableTreeNode buildUpTree(ModelCls cls, int depth, boolean initSims) {
		DefaultMutableTreeNode parent = new DefaultMutableTreeNode(cls.getName());
		clsnameToNode.put(cls.getName(), parent);
		
		// go the tree down to its leaves
		for (Iterator<ModelCls> it = affectedCls.iterator(); it.hasNext();) {
			ModelCls subCls = (ModelCls) it.next();
			if (subCls.getSuperCls() != cls) continue;
			parent.add(buildUpTree(subCls, depth+1, initSims));
		}
		
		return parent;
	}


	public Collection<ModelCls> findAffectedCls(ModelCls topCls) {
		Collection<ModelCls> c = new ArrayList<ModelCls>();
		c.add(topCls);
		for (Iterator<ModelCls> it = getAllModelCls().iterator(); it.hasNext();) {
			ModelCls cls = it.next();
			ModelCls superCls = cls;
			
			while (superCls != null) {
				superCls = superCls.getSuperCls();
				if (c.contains(superCls)) {
					c.add(cls);
					break;
				}
			}
		}
		return c;
	}
	
	// copied from CBRProject, because cbrProject instance might be null here
	private Collection<ModelCls> getAllModelCls() {
		Collection<ModelCls> allModelCls = ModelProvider.getInstance().getAllModelCls();

		Collection<ModelCls> finalAllModelCls = new ArrayList<ModelCls>();
		for (Iterator<ModelCls> it = allModelCls.iterator(); it.hasNext();) {
			ModelCls cls = it.next();
			if (cls.getName().startsWith(":"))
				continue;
			finalAllModelCls.add(cls);
		}
		return finalAllModelCls;
	}
	
	private Collection<String> findAllowedValues() {
		Collection<String> allowedValues = new ArrayList<String>();
		for (Iterator<ModelCls> it = affectedCls.iterator(); it.hasNext();) {
			ModelCls cls = it.next();
			allowedValues.add(cls.getName());
		}
		return allowedValues;
	}

	@SuppressWarnings("unchecked")
	private void initSimilarityValues() {
		// initialize similarity values
		DefaultMutableTreeNode tax = smf_internal.getTaxonomy();
		for (Enumeration en = tax.breadthFirstEnumeration(); en.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
			int depth = node.getLevel()-1;
			double simVal = 1d-Math.pow(0.5, depth); 
			smf_internal.setSimilarityValue((String) node.getUserObject(), simVal);
		}
	}
	
	public boolean checkConsistency(boolean forcedRefresh) {
		// get all descendants of topCls
		affectedCls = findAffectedCls(topCls);
		// build tree with topCls as root
		DefaultMutableTreeNode newRoot = buildUpTree(topCls, 0, false);
		
		// if a refresh is forced or the trees specified by root and newRoot
		// are not the same
		if (forcedRefresh || !compareTrees(root, newRoot)) {
			// the trees are not equal
			root = newRoot;

			// maybe, topCls is not the same anymore
			ModelCls newTopCls = Helper.findHighestModelClsByInheritance(topCls);
			topCls = newTopCls;
			
			// replace smf_taxonomy nodes with this node
			DefaultMutableTreeNode tax = smf_internal.getTaxonomy();
			tax.removeAllChildren();
			tax.add(root);

			// update allowed values
			fakeSlot.setAllowedValues(findAllowedValues());
			
			// use the internal checkConsistency method to keep taxonomy consistent
			smf_internal.checkConsistency(null, true);
			
			// refresh GUI
			if (imfPanel != null) {
				imfPanel.refreshTreeModel();
			}
		}
		return true;
	}
	
	/**
	 * Compares two trees specified by node1 and node2.
	 * 
	 * @param node1 the first tree that should be compared to the second one
	 * @param node2 the second three that should be compared to the first one
	 * @return true iff both trees are equal, false otherwise
	 */
	@SuppressWarnings("unchecked")
	private boolean compareTrees(DefaultMutableTreeNode node1, DefaultMutableTreeNode node2) {
		if (node1.getUserObject() != node2.getUserObject()) {
			return false;
		}
		
		HashMap<Object, DefaultMutableTreeNode> map1 = new HashMap<Object, DefaultMutableTreeNode>();
		for (Enumeration en=node1.children(); en.hasMoreElements();) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) en.nextElement();
			map1.put(child.getUserObject(), child);
		}
		
		HashMap<Object, DefaultMutableTreeNode> map2 = new HashMap<Object, DefaultMutableTreeNode>();
		for (Enumeration en=node2.children(); en.hasMoreElements();) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) en.nextElement();
			map2.put(child.getUserObject(), child);
		}
		
		if (!(map1.keySet().containsAll(map2.keySet()) && map2.keySet().containsAll(map1.keySet()))) {
			return false;
		}
		
		for (Iterator<Object> it=map1.keySet().iterator(); it.hasNext();) {
			Object key = it.next();
			DefaultMutableTreeNode child1 = (DefaultMutableTreeNode) map1.get(key);
			DefaultMutableTreeNode child2 = (DefaultMutableTreeNode) map2.get(key);
			if (!compareTrees(child1, child2)) return false;
		}
		return true;
	}

	/**
	 * Splits the taxonomy at the node labelled with the name of the ModelCls.
	 * The returned IMF contsists of the subtree beginning at the mentioned node.
	 * Similarity values of the subtree are reinitialized.
	 * This subtree is removed from the original tree (the taxonomy of this object)
	 * @param cls ModelCls that is to remove.
	 * @return IMF_Standard the new taxonomy.
	 */
	public IMF_Standard split(ModelCls cls) {
		IMF_Standard imfSplit = new IMF_Standard(cls);
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) clsnameToNode.get(cls.getName());
		if (node.getParent() == null) {
			// this is the root
			root = null;
		} else {
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
			parent.remove(node);
			if (imfPanel != null) {
				imfPanel.refresh();
			}
		}
		imfSplit.root = imfSplit.buildUpTree(cls, 0, true);
		
		affectedCls = findAffectedCls(topCls);
		fakeSlot.setAllowedValues(findAllowedValues());
		smf_internal.checkConsistency(null, true);
		
		if (imfPanel != null) {
			imfPanel.refreshTreeModel();
		}
		
		setHasChanged(true);
		return imfSplit;
	}
	
	/**
	 * Merge two trees. Exactly, the taxonomy of imfChild will be added to the children of the node labelled with
	 * the name of superCls.
	 * @param superCls
	 * @param imfChild
	 */
	@SuppressWarnings("unchecked")
	public void merge(ModelCls superCls, IMF_Standard imfChild) {
		log.fine("merge IMFs for ["+topCls+","+imfChild.topCls+"]" );
		
		topCls = Helper.findHighestModelClsByInheritance(superCls);
		affectedCls = findAffectedCls(topCls);
		Collection<String> allowedValues = findAllowedValues();
		fakeSlot.setAllowedValues(allowedValues);
		try {
			root = buildUpTree(topCls, 0, true);
			smf_internal.checkConsistency(null, true);

			// replace smf_taxonomy nodes with this node
			DefaultMutableTreeNode tax = smf_internal.getTaxonomy();
			tax.removeAllChildren();
			tax.add(root);
			
			initSimilarityValues();
			
			// now set similarity values
			for (Iterator<String> it = fakeSlot.getAllowedValues().iterator(); it.hasNext();) {
				String symbol = it.next();
				Double simVal = smf_internal.getSimilarityValue(symbol);
				if (simVal == null) {
					smf_internal.setSimilarityValue(symbol, imfChild.smf_internal.getSimilarityValue(symbol));
				}
			}

			// update GUI
			if (imfPanel != null) {
				imfPanel.refreshTreeModel();
			}
			
		} catch (Exception e) {
			log.log(Level.SEVERE, "Cannot merge Inheritance Measure Functions for classes ["+topCls+","+imfChild.topCls+"]");
		}
		setHasChanged(true);
	}
	

	public JPanel getIMFPanel() {
		if (imfPanel == null) {
			imfPanel = (SMFPanel_Symbol_Taxonomy) smf_internal.getEditorPanel();
			imfPanel.getTree().setRootVisible(false);
			imfPanel.setDraggingEnabled(false);
		}
		return imfPanel;
	}

	public Element toXML() {
		Element element = smf_internal.initXMLElement();
		smf_internal.toXML(element);
		element.setAttribute(XMLConstants.XML_ATT_TOPCLS, Helper.encode(topCls.getName()));
		
		element.setName(XMLConstants.XML_TAG_INHERITANCEMEASURE);
		
		return element;
	}


	public double getSimilarityBetween(ModelCls queryCls, ModelCls caseCls, Explanation exp) {
		double result = smf_internal.getSimilarityBetween(queryCls.getName(), caseCls.getName(), exp);

		// explanations
		if (exp != null) {
			exp.addComment("Inheritance Structure", "Similarity = ["+Helper.formatDoubleAsString(result)+"]");
			exp.setSimilarity(result);
		}
		
		return result;
	}

	public void setHasChanged(boolean hasChanged) {
		if (hasChanged) {
			CBRProject.getInstance().setHasChanged();
		}
	}

	public Collection<ModelCls> getAffectedCls() {
		return affectedCls;
	}

	public void setSimilarityFor(ModelCls cls, double simVal) {
		smf_internal.setSimilarityValue(cls.getName(), simVal);
	}

	public double getSimilarityFor(ModelCls cls) {
		return smf_internal.getSimilarityValue(cls.getName());
	}

	public void renamedCls(String oldName, String newName) {
		smf_internal.renameSymbol(oldName, newName);
	}

	@SuppressWarnings("unchecked")
	public void integrateNewModelCls(ModelCls cls) {
		log.fine("integrate new cls : "+cls);
		affectedCls = findAffectedCls(topCls);
		fakeSlot.setAllowedValues(findAllowedValues());
		String symbol = cls.getName();
		smf_internal.insertNewSymbol(symbol);
		if (cls.getSuperCls() != null) {
			// find cls symbol and superCls symbol in taxonomy
			ModelCls superCls = cls.getSuperCls();
			String superSymbol = superCls.getName();
			DefaultMutableTreeNode parent = null;
			DefaultMutableTreeNode child = null;
			for (Enumeration en = smf_internal.getTaxonomy().breadthFirstEnumeration(); en.hasMoreElements();) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
				if (node.getUserObject().equals(superSymbol)) {
					parent = node;
				}
				if (node.getUserObject().equals(symbol)) {
					child = node;
				}
			}
			
			// move child to its parent
			smf_internal.moveNode(child, parent);	
		}
	}
}
