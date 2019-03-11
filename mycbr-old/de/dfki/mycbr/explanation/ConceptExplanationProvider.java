/*
 * myCBR License 1.1
 *
 * Copyright (c) 2008
 * Thomas Roth-Berghofer, Armin Stahl & Deutsches Forschungszentrum f&uuml;r K&uuml;nstliche Intelligenz DFKI GmbH
 * Further contributors: myCBR Team (see http://mycbr-project.net/contact.html for further information 
 * about the myCBR Team). 
 * All rights reserved.
 *
 * myCBR is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Since myCBR uses some modules, you should be aware of their licenses for
 * which you should have received a copy along with this program, too.
 * 
 * endOfLic */
package de.dfki.mycbr.explanation;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.tree.DefaultMutableTreeNode;

import org.jdom.Element;
import org.jdom.filter.ElementFilter;

import de.dfki.mycbr.CBRProject;
import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import edu.stanford.smi.protege.event.KnowledgeBaseEvent;

/**
 * 
 * @author myCBR Team
 *
 */
public class ConceptExplanationProvider implements Serializable {

	private static final long serialVersionUID = 5582366092746752344L;

	private HashMap<Object,DefaultMutableTreeNode> instToNode = new HashMap<Object,DefaultMutableTreeNode>();
	private HashMap<DefaultMutableTreeNode,ConceptExplanationScheme> nodeToScheme = new HashMap<DefaultMutableTreeNode,ConceptExplanationScheme>();
	
	private DefaultMutableTreeNode root;

	private ExplainableConcept thing;
	private static ConceptExplanationProvider instance;
	
	private ConceptExplanationProvider() {
		thing = new ExplainableConcept("THING");
		initConceptTree();
		ConceptExplanationScheme thingScheme = new ConceptExplanationScheme(thing);
		nodeToScheme.put(root, thingScheme);
	}
	
	public static ConceptExplanationProvider getInstance() {
		return instance;
	}
	
	public static ConceptExplanationProvider initInstance() {
		instance = new ConceptExplanationProvider();	
		return instance;
	}
	
	public static void resetInstance() {
		instance = null;
	}
	
	public ConceptExplanationScheme getResponsibleExplanationScheme(DefaultMutableTreeNode node) {
		ConceptExplanationScheme scheme = nodeToScheme.get(node);
		if (scheme != null) {
			return scheme;
		}
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
		if (parent == null) {
			return null;
		}
		return getResponsibleExplanationScheme(parent);
	}

	public ConceptExplanationScheme getDirectExplanationScheme(DefaultMutableTreeNode node) {
		return nodeToScheme.get(node);
	}

	public void setDirectExplanationScheme(DefaultMutableTreeNode node, ConceptExplanationScheme scheme) {
		nodeToScheme.put(node, scheme);
	}
	
	public void removeDirectExplanationScheme(DefaultMutableTreeNode node) {
		nodeToScheme.remove(node);
	}
	
	private void initConceptTree() {
		instToNode.clear();
		root = new DefaultMutableTreeNode(thing);
		for (ModelCls cls: CBRProject.getInstance().getAllModelCls()) {
			createNode(root, Helper.findHighestModelClsByInheritance(cls));
		}
	}
	
	private void createNode(DefaultMutableTreeNode parent, Object inst) {
		if (instToNode.keySet().contains(inst)) return;
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new ExplainableConcept(inst));
		parent.add(node);
		instToNode.put(inst,node);
		
		if (!(inst instanceof ModelCls)) return;

		ModelCls cls = (ModelCls) inst;
		for (Object subCls : cls.getDirectSubClses()) {
			createNode(node, subCls);
		}
		for (ModelSlot slot : cls.listSlots()) {
			createNode(node, slot);
			if (slot.getValueType() == ValueType.SYMBOL) {
				DefaultMutableTreeNode slotNode = instToNode.get(slot);
				for (Object allowedValue : slot.getAllowedValues()) {
					createNode(slotNode, allowedValue);
				}
			}
		}
	}

	public DefaultMutableTreeNode getRoot() {
		return root;
	}

	public void toXML(Element rootElement) {
		toXML(rootElement, root);
	}

	@SuppressWarnings("unchecked")
	private void toXML(Element element, DefaultMutableTreeNode node) {
		ConceptExplanationScheme scheme = nodeToScheme.get(node);

		if (scheme != null) {
			Element schemeElement = scheme.toXML();
			element.addContent(schemeElement);
		}
		
		for (Enumeration en = node.children(); en.hasMoreElements();) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) en.nextElement();
			toXML(element, child);
		}
	}

	@SuppressWarnings("unchecked")
	public void load(Element rootElement) {
		nodeToScheme.clear();
		HashMap<String,DefaultMutableTreeNode> idToNode = new HashMap<String,DefaultMutableTreeNode>();
		for (Enumeration en = root.breadthFirstEnumeration(); en.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
			ExplainableConcept expConcept = (ExplainableConcept) node.getUserObject();
			idToNode.put(expConcept.getID(), node);
		}
		
		for (Iterator it=rootElement.getDescendants(new ElementFilter(ConceptExplanationScheme.XML_TAG_EXP_SCHEME)); it.hasNext();) {
			Element element = (Element) it.next();
			String conceptID = Helper.decode(element.getAttributeValue(ConceptExplanationScheme.XML_ATT_CONCEPT_ID));
			DefaultMutableTreeNode node = idToNode.get(conceptID);
			if (node != null) {
				ConceptExplanationScheme scheme = new ConceptExplanationScheme((ExplainableConcept) node.getUserObject(), element);
				nodeToScheme.put(node, scheme);
			}
		}
		
		for(DefaultMutableTreeNode node: idToNode.values()) {
			if(!nodeToScheme.containsKey(node)) {
				nodeToScheme.put(node, new ConceptExplanationScheme((ExplainableConcept) node.getUserObject()));
			}
		}
	}

	public boolean isProvided() {
		if (nodeToScheme.values().size() > 1) {
			return true;
		}
		ConceptExplanationScheme thingScheme = nodeToScheme.get(root);
		if (!"".equals(thingScheme.getDescription()) || !thingScheme.getSources().isEmpty()) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public ConceptExplanationScheme getResponsibleExplanationScheme(Object concept) {
		for (Enumeration en = root.breadthFirstEnumeration(); en.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
			ExplainableConcept expConcept = (ExplainableConcept) node.getUserObject();
			if (concept.equals(expConcept.getUserObject())) {
				return getResponsibleExplanationScheme(node);
			}
		}
		return null;
	}

	public CBRProject getProject() {
		return CBRProject.getInstance();
	}
	
	public void addScheme(DefaultMutableTreeNode node, ExplainableConcept ec) {
		ConceptExplanationScheme scheme = new ConceptExplanationScheme(ec);
		nodeToScheme.put(node, scheme);
	}
		
	public void defaultClsMetaClsChanged(KnowledgeBaseEvent arg0) {}
	public void defaultFacetMetaClsChanged(KnowledgeBaseEvent arg0) {}
	public void defaultSlotMetaClsChanged(KnowledgeBaseEvent arg0) {}
	public void facetCreated(KnowledgeBaseEvent arg0) {}
	public void facetDeleted(KnowledgeBaseEvent arg0) {}
	public void frameNameChanged(KnowledgeBaseEvent arg0) {}
	public void instanceCreated(KnowledgeBaseEvent arg0) {}
	public void instanceDeleted(KnowledgeBaseEvent arg0) {}

}
