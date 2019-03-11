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
package de.dfki.mycbr.model.similaritymeasures.smftypes;

import java.awt.Frame;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.jdom.Element;
import org.jdom.filter.ElementFilter;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.HasChangedListenerSMF;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel_Symbol_Taxonomy;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.retrieval.Explanation;

/**
 * 
 * @author myCBR Team
 */
public class SMF_Symbol_Taxonomy extends AbstractSMFunction implements
		HasChangedListenerSMF {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger
			.getLogger(SMF_Symbol_Taxonomy.class.getName());

	public static final ValueType VALUE_TYPE = ValueType.SYMBOL;

	/**
	 * XML constants for serialization.
	 */
	private static final String XML_TAG_ROOTSYMBOL = "RootSymbol";
	private static final String XML_TAG_CHILDSYMBOL = "ChildSymbol";
	private static final String XML_TAG_CONFIG = "InnerNodesConfig";
	private static final String XML_TAG_INTERNAL_TABLE = "InternalTable";

	private static final String XML_ATT_SYMBOL = "symbol";
	private static final String XML_ATT_SIMVAL = "simVal";
	private static final String XML_ATT_SCOPE = "scope";

	/**
	 * Here come the configuration keys and values for the semantic aspects of
	 * the inner nodes. So this is the vocabulary to describe the taxonomys
	 * meaning. The configuration will be stored in the Properties objects.
	 */
	public static final String VALUE_YES = "yes";
	public static final String VALUE_NO = "no";
	public static final String VALUE_ANY_VALUE = "any_value";
	public static final String VALUE_UNCERTAIN = "uncertain";
	public static final String VALUE_PESSIMISTIC = "pessimistic";
	public static final String VALUE_OPTIMISTIC = "optimistic";
	public static final String VALUE_AVERAGE = "average";

	public static final String KEY_QUERY = "query";
	public static final String KEY_CASE = "case";

	public static final String KEY_HAS_INNER_VALUES = "has_inner_values";
	public static final String KEY_INNER_SEMANTIC = "inner_semantic";
	public static final String KEY_UNCERTAIN = "uncertain";

	private Properties configInnerNodes_Query = new Properties();
	private Properties configInnerNodes_Case = new Properties();

	/** similarity calculation is done by an internal table. */
	private SMF_Symbol_Table table = null;
	/**
	 * lazy refreshing of the table. this flag indicates the state of the table.
	 * If changes have been done in the meantime, the table has to be
	 * recalculated.
	 */
	private boolean tableIsObsolete = true;

	/**
	 * Key is symbol (String), value is a similarity value (Double). This is
	 * used to register all taxonomy similarity values.
	 */
	@SuppressWarnings("unchecked")
	private HashMap simVals = new HashMap();

	/**
	 * Our taxonomy is represented by the root node of the taxonomy tree.
	 */
	private DefaultMutableTreeNode taxonomy = new DefaultMutableTreeNode();

	/** Flag for symmetry mode. */
	private boolean isSymmetryMode = true;

	/** Just for conveniance. */
	private ModelSlot slot;

	// commented out before 20.10.2008
	// /**
	// * Stores certain critical symbols (String).
	// * If the internal smf table has been changed in 'table mode' and is not
	// consistent with its taxonomy anymore,
	// * there are several symbols to mark whose similarity values do not
	// correspond to the taxonomy.
	// * All these symbols are called critical.
	// */
	// private Collection criticalSymbols = new HashSet();

	/**
	 * Stores all critical similarities. Key are query symbols (String), values
	 * are case base symbols (String). If the internal smf table has been
	 * changed in 'table mode' and is not consistent with its taxonomy anymore,
	 * there are several symbols to mark whose similarity values do not
	 * correspond to the taxonomy. All these symbols are called critical. And
	 * the similarity between a key (query) and its value (case) is such a
	 * critical similarity which differs from taxonomy similarity.
	 */
	@SuppressWarnings("unchecked")
	private HashMap criticalSimilarities_q_cb = new HashMap();
	@SuppressWarnings("unchecked")
	private HashMap criticalSimilarities_cb_q = new HashMap();

	public SMF_Symbol_Taxonomy(ModelInstance inst, String smfName)
			throws Exception {
		super(inst, smfName);
		this.slot = (ModelSlot) inst;

		// init root of taxonomy
		taxonomy.setUserObject(getModelInstanceName());
		setSimilarityValue((String) taxonomy.getUserObject(), new Double(0));

		this.addHasChangedListener(this, true);

		// init internal table smf
		this.table = new SMF_Symbol_Table(inst, smfName + "_internal_table");
		this.table.setBackedSMF(this);
		this.table.setSymmetryMode(false);

		//
		// initialize taxonomy
		//
		// for (Iterator it=slot.getAllowedValues().iterator(); it.hasNext();)
		Collection<Object> tmp = slot.getAllowedValues();
		List<String> allowedValues = new Vector<String>();

		for (Object allowedValue : tmp) {
			allowedValues.add(allowedValue.toString());
		}

		Collections.sort(allowedValues);
		for (String symbol : allowedValues) {

			// commented out before 20.10.2008
			// String symbol = (String) it.next();
			// DefaultMutableTreeNode node = new DefaultMutableTreeNode(new
			// Symbol_Sim_Tupel(symbol, 1), true);

			DefaultMutableTreeNode node = new DefaultMutableTreeNode(symbol,
					true);
			setSimilarityValue(symbol, new Double(1));

			log.fine("init tree with new root node [" + node.getUserObject()
					+ "]");
			taxonomy.add(node);
		}

		// initialize inner nodes configuration
		configInnerNodes_Query.setProperty(KEY_HAS_INNER_VALUES, VALUE_NO);
		configInnerNodes_Query.setProperty(KEY_INNER_SEMANTIC, VALUE_ANY_VALUE);
		configInnerNodes_Query.setProperty(KEY_UNCERTAIN, VALUE_PESSIMISTIC);

		configInnerNodes_Case.setProperty(KEY_HAS_INNER_VALUES, VALUE_NO);
		configInnerNodes_Case.setProperty(KEY_INNER_SEMANTIC, VALUE_ANY_VALUE);
		configInnerNodes_Case.setProperty(KEY_UNCERTAIN, VALUE_PESSIMISTIC);
	}

	@SuppressWarnings("unchecked")
	public SMF_Symbol_Taxonomy(ModelInstance inst, Element smfElement)
			throws Exception {
		super(inst, smfElement);
		this.slot = (ModelSlot) inst;

		// init root of taxonomy
		taxonomy.setUserObject(getModelInstanceName());
		setSimilarityValue((String) taxonomy.getUserObject(), new Double(0));

		this.addHasChangedListener(this, true);

		// init internal table smf
		this.table = new SMF_Symbol_Table(inst, smfName + "_internal_table");
		this.table.setBackedSMF(this);
		this.table.setSymmetryMode(false);

		//
		// read xml element, now.
		//
		taxonomy.removeAllChildren();
		for (Iterator it = smfElement.getDescendants(new ElementFilter(
				XML_TAG_ROOTSYMBOL)); it.hasNext();) {
			Element rootSymbol = (Element) it.next();

			// DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new
			// Symbol_Sim_Tupel(rootSymbol.getAttributeValue(XML_ATT_SYMBOL),Helper.parseDouble(rootSymbol.getAttributeValue(XML_ATT_SIMVAL))),
			// true);
			String symbol = Helper.decode(rootSymbol
					.getAttributeValue(XML_ATT_SYMBOL));
			DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(
					symbol, true);
			setSimilarityValue(symbol, new Double(rootSymbol
					.getAttributeValue(XML_ATT_SIMVAL)));

			log.fine("now creating root node [" + rootNode.getUserObject()
					+ "]");

			loadTaxonomy(rootSymbol, rootNode);
			taxonomy.add(rootNode);
		}

		// load inner node semantic configuration
		for (Iterator it = smfElement.getDescendants(new ElementFilter(
				XML_TAG_CONFIG)); it.hasNext();) {
			Element configElement = (Element) it.next();

			Properties currentProps = (Helper.decode(
					configElement.getAttributeValue(XML_ATT_SCOPE)).equals(
					KEY_CASE) ? configInnerNodes_Case : configInnerNodes_Query);
			currentProps.setProperty(KEY_HAS_INNER_VALUES, Helper
					.decode(configElement
							.getAttributeValue(KEY_HAS_INNER_VALUES)));
			currentProps.setProperty(KEY_INNER_SEMANTIC,
					Helper.decode(configElement
							.getAttributeValue(KEY_INNER_SEMANTIC)));
			currentProps.setProperty(KEY_UNCERTAIN, Helper.decode(configElement
					.getAttributeValue(KEY_UNCERTAIN)));

			log
					.fine("these are the loaded inner nodes semantic config properties:\n"
							+ currentProps);
		}

		// recalculate table next time.
		setTableObsolete(true);

		// 
		// look for internal table.
		// If there is an internal table, load it.
		//
		Element tableElement = smfElement.getChild(XML_TAG_INTERNAL_TABLE);
		if (tableElement != null) {
			log.fine("found internal table. Load it.");
			table = new SMF_Symbol_Table(inst, tableElement);
			table.setSymmetryMode(false);
			table.setBackedSMF(this);

			// dont recalculate table next time.
			setTableObsolete(false);

			// we need this to init critical similarities map.
			// Frame parent =
			// (Frame)(editorPanel==null?null:editorPanel.getTopLevelAncestor());
			// checkConsistency(parent);
			checkCriticalSymbols();
		}

		this.isSymmetryMode = isSymmetric();
	}

	/**
	 * Needed by constructor (with xml element). The tree data structure
	 * suggests recursive method calls for de-serialization...
	 * 
	 * @param parentElement
	 * @param parentNode
	 */
	@SuppressWarnings("unchecked")
	private void loadTaxonomy(Element parentElement,
			DefaultMutableTreeNode parentNode) {
		for (Iterator it = parentElement.getDescendants(new ElementFilter(
				XML_TAG_CHILDSYMBOL)); it.hasNext();) {
			Element childElement = (Element) it.next();
			if (!parentElement.getChildren().contains(childElement))
				continue;
			// DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new
			// Symbol_Sim_Tupel(childElement.getAttributeValue(XML_ATT_SYMBOL),
			// Helper.parseDouble(childElement.getAttributeValue(XML_ATT_SIMVAL))),
			// true);

			String symbol = Helper.decode(childElement
					.getAttributeValue(XML_ATT_SYMBOL));
			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(
					symbol, true);
			setSimilarityValue(symbol, new Double(Helper.decode(childElement
					.getAttributeValue(XML_ATT_SIMVAL))));

			log.fine("now creating child node [" + childNode.getUserObject()
					+ "] of parent node [" + parentNode.getUserObject() + "]");
			parentNode.insert(childNode, 0);

			// recursive
			loadTaxonomy(childElement, childNode);
		}

	}

	@SuppressWarnings("unchecked")
	public void toXML(Element xmlElement) {
		log.fine("taxonomy has [" + taxonomy.getChildCount() + "] root nodes");

		// check critical symbols. reason: maybe this smf will be saved in table
		// mode
		// TODO comment next line!
		if (!tableIsObsolete) {
			checkCriticalSymbols();
		}

		for (Enumeration en = taxonomy.children(); en.hasMoreElements();) {
			DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) en
					.nextElement();
			log.fine("now serialize root node [" + rootNode.getUserObject()
					+ "]");

			Element rootElement = new Element(XML_TAG_ROOTSYMBOL);

			String symbol = (String) rootNode.getUserObject();
			rootElement.setAttribute(XML_ATT_SYMBOL, Helper.encode(symbol));
			rootElement.setAttribute(XML_ATT_SIMVAL, Double
					.toString(getSimilarityValue(symbol)));

			serializeChildren(rootElement, rootNode);

			xmlElement.addContent(rootElement);
		}

		// now serialize inner nodes semantic configuration
		Element elementConfigQuery = new Element(XML_TAG_CONFIG);
		elementConfigQuery.setAttribute(XML_ATT_SCOPE, KEY_QUERY);
		elementConfigQuery.setAttribute(KEY_HAS_INNER_VALUES, Helper
				.encode(configInnerNodes_Query
						.getProperty(KEY_HAS_INNER_VALUES)));
		elementConfigQuery
				.setAttribute(KEY_INNER_SEMANTIC, Helper
						.encode(configInnerNodes_Query
								.getProperty(KEY_INNER_SEMANTIC)));
		elementConfigQuery.setAttribute(KEY_UNCERTAIN, Helper
				.encode(configInnerNodes_Query.getProperty(KEY_UNCERTAIN)));

		Element elementConfigCase = new Element(XML_TAG_CONFIG);
		elementConfigCase.setAttribute(XML_ATT_SCOPE, KEY_CASE);
		elementConfigCase.setAttribute(KEY_HAS_INNER_VALUES,
				Helper.encode(configInnerNodes_Case
						.getProperty(KEY_HAS_INNER_VALUES)));
		elementConfigCase.setAttribute(KEY_INNER_SEMANTIC, Helper
				.encode(configInnerNodes_Case.getProperty(KEY_INNER_SEMANTIC)));
		elementConfigCase.setAttribute(KEY_UNCERTAIN, Helper
				.encode(configInnerNodes_Case.getProperty(KEY_UNCERTAIN)));

		xmlElement.addContent(elementConfigQuery);
		xmlElement.addContent(elementConfigCase);

		// in case taxonomy does not completely match its internal table
		// (inconsistency) save table, too.
		log.fine("table will be recalculated before serialization: ["
				+ tableIsObsolete + "]");
		getTable();
		if (criticalSimilarities_q_cb.size() > 0
				|| criticalSimilarities_cb_q.size() > 0) {
			log.fine("serialize internal table, too.");
			Element tableElement = table.initXMLElement();
			tableElement.setName(XML_TAG_INTERNAL_TABLE);
			table.setBackedSMF(null);
			table.toXML(tableElement);
			table.setBackedSMF(this);
			xmlElement.addContent(tableElement);
		}

	}

	/**
	 * Needed by toXML() method. The tree data structure suggests recursive
	 * method calls for serialization...
	 * 
	 * @param parentElement
	 *            Element to fill with serialization data.
	 * @param parentNode
	 *            DefaultMutableTreeNode currently examining tree node.
	 */
	@SuppressWarnings("unchecked")
	private void serializeChildren(Element parentElement,
			DefaultMutableTreeNode parentNode) {
		for (Enumeration en = parentNode.children(); en.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) en
					.nextElement();
			log.fine("now serialize child [" + node.getUserObject()
					+ "] of parent node [" + parentNode.getUserObject() + "]");

			Element childElement = new Element(XML_TAG_CHILDSYMBOL);

			// Symbol_Sim_Tupel tupel = (Symbol_Sim_Tupel) node.getUserObject();
			String symbol = (String) node.getUserObject();
			childElement.setAttribute(XML_ATT_SYMBOL, Helper.encode(symbol));
			childElement.setAttribute(XML_ATT_SIMVAL, Double
					.toString(getSimilarityValue(symbol)));

			// recursive
			serializeChildren(childElement, node);

			parentElement.addContent(childElement);
		}
	}

	@SuppressWarnings("unchecked")
	public AbstractSMFunction copy() {
		SMF_Symbol_Taxonomy newSmf = null;

		try {
			newSmf = new SMF_Symbol_Taxonomy(inst, smfName);

			newSmf.taxonomy = copyTree(taxonomy);

			// table should NOT be null!
			newSmf.table = (SMF_Symbol_Table) table.copy();
			newSmf.table.setBackedSMF(newSmf);

			newSmf.simVals = (HashMap) simVals.clone();

			// newSmf.criticalSymbols = new ArrayList(criticalSymbols);
			newSmf.criticalSimilarities_q_cb = (HashMap) criticalSimilarities_q_cb
					.clone();
			newSmf.criticalSimilarities_cb_q = (HashMap) criticalSimilarities_cb_q
					.clone();

			newSmf.isSymmetryMode = this.isSymmetryMode;
			newSmf.configInnerNodes_Case = new Properties(configInnerNodes_Case);
			newSmf.configInnerNodes_Query = new Properties(
					configInnerNodes_Query);

			newSmf.setHasChanged(false);

		} catch (Exception e) {
			log.log(Level.SEVERE, "error while copying!", e);
		}

		return newSmf;
	}

	/**
	 * Copies the taxonomy tree by recursion.
	 * 
	 * @param parentNode
	 * @return DefaultMutableTreeNode copy of the given root node whose children
	 *         are copies of the originals.
	 */
	@SuppressWarnings("unchecked")
	private DefaultMutableTreeNode copyTree(DefaultMutableTreeNode parentNode) {
		DefaultMutableTreeNode newParentNode = (DefaultMutableTreeNode) parentNode
				.clone();
		log.fine("copying [" + newParentNode.getUserObject() + "] with ["
				+ parentNode.getChildCount() + "] children.");

		for (Enumeration en = parentNode.children(); en.hasMoreElements();) {
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) en
					.nextElement();

			// recursion
			DefaultMutableTreeNode newChildNode = copyTree(childNode);

			newParentNode.add(newChildNode);
		}

		return newParentNode;
	}

	protected SMFPanel createSMFPanel() {
		return new SMFPanel_Symbol_Taxonomy(this);
	}

	@SuppressWarnings("unchecked")
	public boolean checkConsistency(Frame parent, boolean quiet) {
		log.fine("checking consistency for ["
				+ getClass().getName().toUpperCase() + "].");

		if (slot.getValueType() != getValueType()) {
			if (!quiet) {
				JOptionPane.showMessageDialog(parent,
						"This smfunction was NOT made for type ["
								+ slot.getValueType() + "]. Please check it!");
			}
			return false;
		}

		if (slot.getAllowedValues() == null) {
			if (!quiet) {
				JOptionPane.showMessageDialog(parent,
						"Symbol values are not set any more. Please check it!");
			}
			return false;
		}

		//
		// first identify changes in symbol set
		//
		Collection idleSymbols = new TreeSet();
		Collection newSymbols = new TreeSet();
		ConsistencyChecker_Symbols.findNewAndIdleSymbols(slot
				.getAllowedValues(), simVals.keySet(), newSymbols, idleSymbols);
		idleSymbols.remove(taxonomy.getUserObject());

		//
		// then let the user decide which symbols just have a new name
		//
		HashMap matchingSymbols = new HashMap();
		if (!ConsistencyChecker_Symbols.matchSymbols(parent, newSymbols,
				idleSymbols, matchingSymbols, quiet)) {
			return false;
		}
		newSymbols.removeAll(matchingSymbols.keySet());
		idleSymbols.removeAll(matchingSymbols.values());

		//
		// insert new symbols in smf.
		//
		for (Iterator it = newSymbols.iterator(); it.hasNext();) {
			String newSymbol = (String) it.next();
			insertNewSymbol(newSymbol);
		}

		//
		// delete idle symbols in smf.
		//
		for (Iterator it = idleSymbols.iterator(); it.hasNext();) {
			String oldSymbol = (String) it.next();
			removeOldSymbol(oldSymbol);
		}

		//
		// rename symbols in smf.
		//
		for (Iterator it = matchingSymbols.entrySet().iterator(); it.hasNext();) {
			Entry e = (Entry) it.next();
			renameSymbol((String) e.getValue(), (String) e.getKey());
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	public void checkCriticalSymbols() {
		//
		// the idea:
		// copy current table,
		// construct a new one
		// and check whether or not both are equal.
		//

		// clear old critical similarities
		criticalSimilarities_q_cb.clear();
		criticalSimilarities_cb_q.clear();

		SMF_Symbol_Table table_copy = (SMF_Symbol_Table) table.copy();

		// now rebuild table
		setTableObsolete(true);
		getTable();
		// table.show("taxonomy table (fresh calculation)");
		// table_copy.show("modified table");

		boolean isConsistent = true;
		Collection symbols = new HashSet(simVals.keySet());
		symbols.remove(taxonomy.getUserObject());
		for (Iterator it1 = symbols.iterator(); it1.hasNext();) {
			String q = (String) it1.next();
			for (Iterator it2 = symbols.iterator(); it2.hasNext();) {
				String cb = (String) it2.next();

				double table_sim = table.getSimilarityBetween(q, cb, null);
				double table_copy_sim = table_copy.getSimilarityBetween(q, cb,
						null);
				if (table_copy_sim != table_sim) {
					isConsistent = false;

					// register critical similarity
					Collection q_cb = (Collection) criticalSimilarities_q_cb
							.get(q);
					if (q_cb == null) {
						q_cb = new ArrayList();
					}
					q_cb.add(cb);
					criticalSimilarities_q_cb.put(q, q_cb);

					Collection cb_q = (Collection) criticalSimilarities_cb_q
							.get(cb);
					if (cb_q == null) {
						cb_q = new ArrayList();
					}
					cb_q.add(q);
					criticalSimilarities_cb_q.put(cb, cb_q);

					// okay, now set modified value again
					table.setValueAt(table_copy_sim, q, cb);
				}

			}
		}

		// commented out before 20.10.2008
		// log.info("criticals are:\n"+criticalSimilarities_cb_q.keySet()+"\n"+criticalSimilarities_q_cb.keySet());
		// table.show("DEBUG!! table");
		// table_copy.show("DEBUG!! table_copy");

		log
				.fine("finally leaving consistency check. Is smf_taxonomy consistent to smf_standard ? ["
						+ (isConsistent ? "yes" : "no")
						+ "] return "
						+ isConsistent);
	}

	@SuppressWarnings("unchecked")
	public void renameSymbol(String oldSymbol, String newSymbol) {
		log.fine("rename symbol [" + oldSymbol + "] to [" + newSymbol + "]");
		simVals.put(newSymbol, simVals.get(oldSymbol));
		simVals.remove(oldSymbol);

		// check taxonomy tree
		for (Enumeration en = taxonomy.depthFirstEnumeration(); en
				.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) en
					.nextElement();
			String currentSymbol = (String) node.getUserObject();
			if (!oldSymbol.equals(currentSymbol))
				continue;

			// now rename node
			node.setUserObject(newSymbol);
		}

		// and apply changes to internal table
		table.renameSymbol(oldSymbol, newSymbol);

		// update GUI
		if (editorPanel != null) {
			((SMFPanel_Symbol_Taxonomy) editorPanel).refreshTreeModel();
		}
		setHasChanged(true);
	}

	@SuppressWarnings("unchecked")
	private void removeOldSymbol(String oldSymbol) {
		log.fine("remove old symbol [" + oldSymbol + "]");

		simVals.remove(oldSymbol);
		// // maybe the range has changed.
		// changeRange(Helper.getMinimum(symbolMapping.values()).intValue(),
		// Helper.getMaximum(symbolMapping.values()).intValue(), isCyclic,
		// distLastFirst);

		// check taxonomy tree
		for (Enumeration en = taxonomy.depthFirstEnumeration(); en
				.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) en
					.nextElement();
			String currentSymbol = (String) node.getUserObject();
			if (!oldSymbol.equals(currentSymbol))
				continue;

			// now remove node
			node.removeFromParent();
		}

		// and apply changes to internal table
		table.removeOldSymbol(oldSymbol);

		// update GUI
		if (editorPanel != null) {
			((SMFPanel_Symbol_Taxonomy) editorPanel).refreshTreeModel();
		}
		setHasChanged(true);
	}

	@SuppressWarnings("unchecked")
	public void insertNewSymbol(String newSymbol) {
		log.fine("insert new symbol [" + newSymbol + "]");

		simVals.put(newSymbol, new Double(1));
		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newSymbol);
		taxonomy.add(newNode);

		// and apply changes to internal table
		table.insertNewSymbol(newSymbol);

		// update GUI
		if (editorPanel != null) {
			((SMFPanel_Symbol_Taxonomy) editorPanel).refreshTreeModel();
		}
		setHasChanged(true);
	}

	public static String getSMFunctionTypeName_static() {
		return "Taxonomy";
	}

	/**
	 * Just look it up in our internal table. So we pass it through to the
	 * table. If the table is obsolete (which means that it is not representing
	 * the current taxonomy), it will be calculated. (Lazy behavior)
	 */
	public double getSimilarityBetween(Object query, Object cb, Explanation exp) {
		return getTable().getSimilarityBetween(query, cb, exp);
	}

	/**
	 * Returns the value type of the ModelInstance.
	 */
	public ValueType getValueType() {
		return VALUE_TYPE;
	}

	/**
	 * By HasChangedListener.
	 * 
	 * @see HasChangedListenerSMF
	 */
	public void smfHasChanged(boolean hasChanged) {
		if (hasChanged) {
			setTableObsolete(true);
		}
	}

	/**
	 * Set flag tableIsObsolete. For more details see above.
	 * 
	 * @param isObsolete
	 */
	private void setTableObsolete(boolean isObsolete) {
		tableIsObsolete = isObsolete;
	}

	/**
	 * If smf has been changed in the meantime, it will be calculated from
	 * scratch.
	 * 
	 * @return SMF_Symbol_Standard internal table.
	 */
	public SMF_Symbol_Table getTable() {
		if (tableIsObsolete) {
			// recalculate table
			calculateTable();

			// and reset flag.
			setTableObsolete(false);
		}
		return table;
	}

	@SuppressWarnings("unchecked")
	private void calculateTable() {
		log.fine("(re-) calculate smf table");

		boolean inner_nodes_as_values_query = configInnerNodes_Query
				.getProperty(KEY_HAS_INNER_VALUES).equals(VALUE_YES);
		boolean inner_nodes_as_values_case = configInnerNodes_Case.getProperty(
				KEY_HAS_INNER_VALUES).equals(VALUE_YES);
		// boolean inner_semantic_anyvalue_query =
		// configInnerNodes_Query.getProperty(KEY_INNER_SEMANTIC).equals(VALUE_ANY_VALUE);
		// boolean inner_semantic_anyvalue_case =
		// configInnerNodes_Case.getProperty(KEY_INNER_SEMANTIC).equals(VALUE_ANY_VALUE);

		for (Enumeration en1 = taxonomy.depthFirstEnumeration(); en1
				.hasMoreElements();) {
			DefaultMutableTreeNode node1 = (DefaultMutableTreeNode) en1
					.nextElement();

			String symbolNode1 = (String) node1.getUserObject();
			// double simValNode1 = node1Tupel.getSimVal();

			Enumeration en2 = taxonomy.depthFirstEnumeration();

			// commented out before 20.10.2008
			// DefaultMutableTreeNode node2 = null;
			// log.fine("node1 = ["+node1.getUserObject()+"]");
			// do
			// {
			// node2 = (DefaultMutableTreeNode) en2.nextElement();
			// }
			// while (node2!=node1);

			while (en2.hasMoreElements()) {
				DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) en2
						.nextElement();

				String symbolNode2 = (String) node2.getUserObject();
				// double simValNode2 = node2Tupel.getSimVal();

				// nearest common predecessor
				DefaultMutableTreeNode ncp = (DefaultMutableTreeNode) node1
						.getSharedAncestor(node2);

				if (node1 == node2) {
					// nodes are the same
					setTableValueNoCriticals(1, symbolNode1, symbolNode2);
				} else if (node2.isNodeAncestor(node1)) {
					// node1 is ancestor of node2
					setTableValueNoCriticals(1, symbolNode1, symbolNode2);
					setTableValueNoCriticals(1, symbolNode2, symbolNode1);
					if ((node1.isLeaf() || inner_nodes_as_values_query)
							&& (node2.isLeaf() || inner_nodes_as_values_case)) {
						setTableValueNoCriticals(1, symbolNode1, symbolNode2);
					} else {
						setTableValueNoCriticals(0, symbolNode1, symbolNode2);
					}
					if ((node2.isLeaf() || inner_nodes_as_values_query)
							&& (node1.isLeaf() || inner_nodes_as_values_case)) {
						setTableValueNoCriticals(1, symbolNode2, symbolNode1);
					} else {
						setTableValueNoCriticals(0, symbolNode2, symbolNode1);
					}
				} else if (ncp != null) {
					// there exists a nearest common precedessor
					String ncpSymbol = (String) ncp.getUserObject();
					double simValNCP = getSimilarityValue(ncpSymbol)
							.doubleValue();

					setTableValueNoCriticals(simValNCP, symbolNode1,
							symbolNode2);
					setTableValueNoCriticals(simValNCP, symbolNode2,
							symbolNode1);
					if ((node1.isLeaf() || inner_nodes_as_values_query)
							&& (node2.isLeaf() || inner_nodes_as_values_case)) {
						setTableValueNoCriticals(simValNCP, symbolNode1,
								symbolNode2);
					} else {
						setTableValueNoCriticals(0, symbolNode1, symbolNode2);
					}
					if ((node2.isLeaf() || inner_nodes_as_values_query)
							&& (node1.isLeaf() || inner_nodes_as_values_case)) {
						setTableValueNoCriticals(simValNCP, symbolNode2,
								symbolNode1);
					} else {
						setTableValueNoCriticals(0, symbolNode2, symbolNode1);
					}
				}

				// node2 = (DefaultMutableTreeNode) en2.nextElement();
			}
		}

		setTableObsolete(false);

		boolean calculateUncertainCase = configInnerNodes_Case.getProperty(
				KEY_INNER_SEMANTIC).equals(VALUE_UNCERTAIN)
				&& configInnerNodes_Case.getProperty(KEY_HAS_INNER_VALUES)
						.equals(VALUE_YES);
		boolean calculateUncertainQuery = configInnerNodes_Query.getProperty(
				KEY_INNER_SEMANTIC).equals(VALUE_UNCERTAIN)
				&& configInnerNodes_Query.getProperty(KEY_HAS_INNER_VALUES)
						.equals(VALUE_YES);

		//
		// Uncertain: Calculate for pessimistic/average/optimistic
		//
		if (calculateUncertainCase || calculateUncertainQuery)
			try {
				log.fine("Calculate min/max/average values for 'uncertain'");

				Method operationCase = null;
				Method operationQuery = null;

				String uncertainCase = configInnerNodes_Case
						.getProperty(KEY_UNCERTAIN);
				String uncertainQuery = configInnerNodes_Query
						.getProperty(KEY_UNCERTAIN);

				if (uncertainCase.equals(VALUE_OPTIMISTIC)) {
					operationCase = SMF_Symbol_Taxonomy.class
							.getDeclaredMethod("getMaximum", new Class[] {
									Collection.class, Collection.class });
				} else if (uncertainCase.equals(VALUE_PESSIMISTIC)) {
					operationCase = SMF_Symbol_Taxonomy.class
							.getDeclaredMethod("getMinimum", new Class[] {
									Collection.class, Collection.class });
				} else if (uncertainCase.equals(VALUE_AVERAGE)) {
					operationCase = SMF_Symbol_Taxonomy.class
							.getDeclaredMethod("getAverage", new Class[] {
									Collection.class, Collection.class });
				} else {
					assert (false); // should not happen
				}

				if (uncertainQuery.equals(VALUE_OPTIMISTIC)) {
					operationQuery = SMF_Symbol_Taxonomy.class
							.getDeclaredMethod("getMaximum", new Class[] {
									Collection.class, Collection.class });
				} else if (uncertainQuery.equals(VALUE_PESSIMISTIC)) {
					operationQuery = SMF_Symbol_Taxonomy.class
							.getDeclaredMethod("getMinimum", new Class[] {
									Collection.class, Collection.class });
				} else if (uncertainQuery.equals(VALUE_AVERAGE)) {
					operationQuery = SMF_Symbol_Taxonomy.class
							.getDeclaredMethod("getAverage", new Class[] {
									Collection.class, Collection.class });
				} else {
					assert (false); // should not happen
				}

				//
				// now calculate 'uncertain' similarities
				// 
				for (Enumeration en1 = taxonomy.depthFirstEnumeration(); en1
						.hasMoreElements();) {
					DefaultMutableTreeNode node1 = (DefaultMutableTreeNode) en1
							.nextElement();

					if (node1 == taxonomy)
						continue;
					// if (!node1.isLeaf() || node1==taxonomy)
					// continue;

					String symbolNode1 = (String) node1.getUserObject();

					for (Enumeration en2 = taxonomy.depthFirstEnumeration(); en2
							.hasMoreElements();) {
						DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) en2
								.nextElement();
						if (node2 == taxonomy)
							continue;

						String symbolNode2 = (String) node2.getUserObject();

						Collection M1 = new ArrayList();
						// collect all leaf symbols from node1
						for (Enumeration tmpEn = node1.depthFirstEnumeration(); tmpEn
								.hasMoreElements();) {
							DefaultMutableTreeNode tmpNode = (DefaultMutableTreeNode) tmpEn
									.nextElement();
							if (!tmpNode.isLeaf())
								continue;
							M1.add(tmpNode.getUserObject());
						}

						Collection M2 = new ArrayList();
						// collect all leaf symbols from node2
						for (Enumeration tmpEn = node2
								.breadthFirstEnumeration(); tmpEn
								.hasMoreElements();) {
							DefaultMutableTreeNode tmpNode = (DefaultMutableTreeNode) tmpEn
									.nextElement();
							if (!tmpNode.isLeaf())
								continue;
							M2.add(tmpNode.getUserObject());
						}

						if (calculateUncertainQuery) {
							// find max / min / avg
							Double sim1 = (Double) operationQuery.invoke(this,
									new Object[] { M1, M2 });
							if ((inner_nodes_as_values_query || node1.isLeaf())
									&& (inner_nodes_as_values_case || node2
											.isLeaf())) {
								setTableValueNoCriticals(sim1.doubleValue(),
										symbolNode1, symbolNode2);
							}
						}

						if (calculateUncertainCase) {
							// find max / min / avg
							Double sim2 = (Double) operationCase.invoke(this,
									new Object[] { M1, M2 });
							if ((inner_nodes_as_values_query || node2.isLeaf())
									&& (inner_nodes_as_values_case || node1
											.isLeaf())) {
								setTableValueNoCriticals(sim2.doubleValue(),
										symbolNode2, symbolNode1);
							}
						}

					}
				}
			} catch (Exception e) {
				log
						.log(
								Level.SEVERE,
								"Error occurred during table calculation in taxonomy SMF",
								e);
				e.printStackTrace();
			}

	}

	/**
	 * Returns the taxonomys similarity value which is detatched to the given
	 * symbol.
	 * 
	 * @param symbol
	 *            String the symbol.
	 * @return Double similarity value.
	 */
	public Double getSimilarityValue(String symbol) {
		return (Double) simVals.get(symbol);
	}

	/**
	 * Sets the taxonomys similarity value which is detatched to the given
	 * symbol.
	 * 
	 * @param symbol
	 *            String the symbol.
	 * @param simVal
	 *            Double similarity value.
	 */
	@SuppressWarnings("unchecked")
	public void setSimilarityValue(String symbol, Double simVal) {
		simVals.put(symbol, simVal);
		setHasChanged(true);
	}

	/**
	 * Enters a value in the inner table. If the similarity between querySymbol
	 * and caseSymbol is a manually changed value (in table mode) no changes
	 * will be made.
	 * 
	 * @param simVal
	 *            double similarity value.
	 * @param querySymbol
	 *            String query symbol.
	 * @param caseSymbol
	 *            String case symbol.
	 */
	private void setTableValueNoCriticals(double simVal, String querySymbol,
			String caseSymbol) {
		boolean isCritical = isCriticalSimilarity(querySymbol, caseSymbol);

		if (isCritical)
			return;
		table.setValueAt(simVal, querySymbol, caseSymbol);
		// getSmfTable().setValueAt(simVal, querySymbol, caseSymbol);
	}

	/**
	 * @param querySymbol
	 *            String query symbol.
	 * @param caseSymbol
	 *            String case symbol.
	 * @return boolean true if this similarity has been changed manually in
	 *         table mode.
	 */
	@SuppressWarnings("unchecked")
	public boolean isCriticalSimilarity(String querySymbol, String caseSymbol) {

		// commented out before 20.10.2008
		// return criticalSymbols.contains(encodeQueryAndCaseSymbol(querySymbol,
		// caseSymbol));
		// return criticalSimilarities.get(querySymbol) == caseSymbol &&
		// (caseSymbol!=null);

		Collection c = (Collection) criticalSimilarities_q_cb.get(querySymbol);
		return c != null && c.contains(caseSymbol);
	}

	/**
	 * Returns true if there exists at least one other symbol to which its
	 * similarity does not correspond to taxonomy. Therefore the symbol may
	 * appear as a query symbol or a case symbol.
	 * 
	 * @param symbol
	 *            String the symbol to be checked.
	 * @return true if there exists at least one other symbol to which its
	 *         similarity does not correspond to taxonomy.
	 */
	public boolean isCriticalSymbol(String symbol) {
		// return criticalSymbols.contains(symbol);
		// return criticalSimilarities_q_cb.keySet().contains(symbol) ||
		// criticalSimilarities_q_cb.values().contains(symbol);
		return criticalSimilarities_q_cb.keySet().contains(symbol)
				|| criticalSimilarities_cb_q.keySet().contains(symbol);
	}

	/**
	 * Checks symmetry of the smfunction.
	 * 
	 * @return boolean true if symmetric.
	 */
	public boolean isSymmetric() {
		boolean isSymmetric = true;

		// commented out before 20.10.2008
		// if table is not symmetric, then nothing is symmetric.
		// if (table !=null && !table.isSymmetric()) return false;

		// this is a weird thing! It's not my fault, it's just the nature of
		// this extraordinary option thing.
		isSymmetric &= configInnerNodes_Case.getProperty(KEY_HAS_INNER_VALUES)
				.equals(
						configInnerNodes_Query
								.getProperty(KEY_HAS_INNER_VALUES));
		if (!isSymmetric)
			return false;
		isSymmetric &= configInnerNodes_Case.getProperty(KEY_INNER_SEMANTIC)
				.equals(configInnerNodes_Query.getProperty(KEY_INNER_SEMANTIC));
		if (!isSymmetric)
			return false;
		isSymmetric &= configInnerNodes_Case.getProperty(KEY_UNCERTAIN).equals(
				configInnerNodes_Query.getProperty(KEY_UNCERTAIN));
		if (!isSymmetric)
			return false;

		return true;
	}

	/**
	 * Switch symmetryMode.
	 * 
	 * @see de.dfki.mycbr.model.similaritymeasures.smftypes.ui.Widget_Symmetry.SymmetryModeListener
	 */
	@SuppressWarnings("unchecked")
	public boolean setSymmetryMode(boolean symmetryMode) {
		if (!this.isSymmetryMode && symmetryMode && !isSymmetric()) {
			// we switch from asymmetric mode to symmetric mode,
			// but this smfunction is not symmetric
			if (editorPanel != null) {
				int result = JOptionPane.showConfirmDialog(editorPanel
						.getTopLevelAncestor(),
						"Function is NOT symmetric. Force it?",
						"SMF is not symmetric", JOptionPane.YES_NO_OPTION,
						JOptionPane.INFORMATION_MESSAGE);
				if (result == JOptionPane.NO_OPTION) {
					return false;
				}

				for (Enumeration en = configInnerNodes_Query.propertyNames(); en
						.hasMoreElements();) {
					String key = (String) en.nextElement();
					configInnerNodes_Case.setProperty(key,
							configInnerNodes_Query.getProperty(key));
				}
			}
		}

		this.isSymmetryMode = symmetryMode;
		setHasChanged(true);

		return true;
	}

	/**
	 * @return boolean isSymmetryMode
	 */
	public boolean isSymmetryMode() {
		return isSymmetryMode;
	}

	/**
	 * Configure semantic of inner nodes here.
	 * 
	 * @param keyQueryCase
	 *            String this key determines the used Properties object.
	 * @param keyOption
	 *            String this key determines the option which is about to get
	 *            modified.
	 * @param value
	 *            String the configuration value.
	 */
	public void configureInnerNodes(String keyQueryCase, String keyOption,
			String value) {
		log.fine("configure semantic of inner nodes: [" + keyQueryCase + "]["
				+ keyOption + "] = [" + value + "]");

		boolean changes = false;
		if (KEY_QUERY.equals(keyQueryCase)) {
			changes = !configInnerNodes_Query.getProperty(keyOption).equals(
					value);
			configInnerNodes_Query.setProperty(keyOption, value);
		}

		if (isSymmetryMode() || KEY_CASE.equals(keyQueryCase)) {
			log.fine("&& configure semantic of inner nodes: [" + KEY_CASE
					+ "][" + keyOption + "] = [" + value + "]");
			changes = changes
					|| !configInnerNodes_Case.getProperty(keyOption).equals(
							value);
			configInnerNodes_Case.setProperty(keyOption, value);
		}

		// setTableObsolete(true);
		if (changes) {
			setHasChanged(true);
		}
	}

	/**
	 * Read configuration of inner node semantic.
	 * 
	 * @param keyQueryCase
	 *            String this key determines the used Properties object.
	 * @param keyOption
	 *            String this key determines the option which is about to get
	 *            modified.
	 * @return value String the configuration value.
	 */
	public String getConfigurationInnerNodes(String keyQueryCase,
			String keyOption) {
		log.fine("request for configuration of inner nodes [" + keyQueryCase
				+ "][" + keyOption + "]");

		if (KEY_QUERY.equals(keyQueryCase)) {
			return configInnerNodes_Query.getProperty(keyOption);
		}
		return configInnerNodes_Case.getProperty(keyOption);
	}

	/**
	 * Returns the root node of the taxonomy whose label is equals to the Slot
	 * name.
	 * 
	 * @return DefaultMutableTreeNode root node of taxonomy.
	 */
	public DefaultMutableTreeNode getTaxonomy() {
		return taxonomy;
	}

	/**
	 * 
	 * @param newChildNode
	 *            DefaultMutableTreeNode the node to move
	 * @param newParentNode
	 *            DefaultMutableTreeNode the node to be the new parent of
	 *            newChildNode
	 * @return boolean true if successfully applied changes.
	 */
	public boolean moveNode(DefaultMutableTreeNode newChildNode,
			DefaultMutableTreeNode newParentNode) {
		if (newParentNode == null || newChildNode == null) {
			return false;
		}

		if (newChildNode.getParent() == newParentNode) {
			return false;
		}

		if (newParentNode.isNodeAncestor(newChildNode)) {
			log.fine("can't move node=[" + newChildNode.getUserObject()
					+ "] to parent=[" + newParentNode.getUserObject()
					+ "]. child is ancester of parent.");
			return false;
		}

		// check similarity hierarchy
		//
		String childSymbol = (String) newChildNode.getUserObject();
		DefaultMutableTreeNode newGrampa = (DefaultMutableTreeNode) newParentNode
				.getParent();
		double grampaSim = (newGrampa == null ? 0 : getSimilarityValue(
				(String) newGrampa.getUserObject()).doubleValue());
		double childSim = getSimilarityValue(childSymbol).doubleValue();

		// if
		// (getSimilarityValue(childSymbol).doubleValue()<getSimilarityValue(parentSymbol).doubleValue())
		if (childSim < grampaSim) {
			log
					.fine("can't move node=["
							+ newChildNode.getUserObject()
							+ "] to parent=["
							+ newParentNode.getUserObject()
							+ "]. childs similarity value is lower than its new grandfathers.");
			if (editorPanel != null) {
				JOptionPane
						.showMessageDialog(
								editorPanel,
								"can't move node=["
										+ newChildNode.getUserObject()
										+ "] to parent=["
										+ newParentNode.getUserObject()
										+ "].\nchilds similarity value is lower than its new parent's.");
			}
			return false;
		}

		log.fine("move node=[" + newChildNode.getUserObject() + "] to parent=["
				+ newParentNode.getUserObject() + "]");
		((MutableTreeNode) newChildNode.getParent()).remove(newChildNode);
		newParentNode.insert(newChildNode, 0);

		// commented out before 20.10.2008
		// DefaultMutableTreeNode grampa = (DefaultMutableTreeNode)
		// parent.getParent();
		// double grampaSim =
		// (grampa==null?0:smf.getSimilarityValue((String)grampa.getUserObject()).doubleValue());

		double minSim = getSimilarityValue(
				(String) newChildNode.getUserObject()).doubleValue();
		log.fine("grampa " + newGrampa + "," + grampaSim + "   minsim "
				+ minSim);
		if (newGrampa != null) {
			setSimilarityValue((String) newParentNode.getUserObject(), Helper
					.parseDouble(Helper
							.formatDoubleAsString((grampaSim + minSim) / 2)));
		}

		setTableObsolete(true);
		setHasChanged(true);
		return true;
	}

	@SuppressWarnings("unchecked")
	public HashMap getCriticalSimilarities_QueryToCase() {
		return criticalSimilarities_q_cb;
	}

	@SuppressWarnings("unchecked")
	public HashMap getCriticalSimilarities_CaseToQuery() {
		return criticalSimilarities_cb_q;
	}

	/**
	 * Confirmes, that taxonomy similarity is valid for this symbol. This has an
	 * affect to critical symbols (see above).
	 * 
	 * @param symbol
	 */
	@SuppressWarnings("unchecked")
	public void removeCriticalSymbol(String symbol) {
		// remove critical entries
		StringBuffer logInfo = new StringBuffer();
		logInfo.append("cases: ");
		for (Iterator it = criticalSimilarities_cb_q.entrySet().iterator(); it
				.hasNext();) {
			Entry e = (Entry) it.next();
			Collection c = (Collection) e.getValue();
			c.remove(symbol);
			if (c.size() == 0) {
				it.remove();
			}
			logInfo.append(e.getKey());
		}
		logInfo.append("\nqueries: ");
		for (Iterator it = criticalSimilarities_q_cb.entrySet().iterator(); it
				.hasNext();) {
			Entry e = (Entry) it.next();
			Collection c = (Collection) e.getValue();
			c.remove(symbol);
			if (c.size() == 0) {
				it.remove();
			}
			logInfo.append(e.getKey());
		}
		criticalSimilarities_cb_q.remove(symbol);
		criticalSimilarities_q_cb.remove(symbol);
		log.fine("remove symbol [" + symbol + "] from criticalsymbols\n"
				+ logInfo);
		setHasChanged(true);
		// in case of a toXML() call, there may appear some data loss. So we
		// have to do this, unfortunately
		getTable();
	}

	/**
	 * Used for table calculation. Will be envoked by introspection.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	private Double getMaximum(Collection Q, Collection C) {
		double result = 0;
		// SMF_Symbol_Standard table = getSmfTable();

		for (Iterator it1 = Q.iterator(); it1.hasNext();) {
			String symbolQ = (String) it1.next();

			for (Iterator it2 = C.iterator(); it2.hasNext();) {
				String symbolC = (String) it2.next();

				result = Math.max(result, table.getSimilarityBetween(symbolQ,
						symbolC, null));
			}
		}

		return new Double(result);
	}

	/**
	 * Used for table calculation. Will be envoked by introspection.
	 */
	@SuppressWarnings({ "unused", "unchecked" })
	private Double getMinimum(Collection Q, Collection C) {
		double result = 1;

		// SMF_Symbol_Standard table = getSmfTable();

		for (Iterator it1 = Q.iterator(); it1.hasNext();) {
			String symbolQ = (String) it1.next();

			for (Iterator it2 = C.iterator(); it2.hasNext();) {
				String symbolC = (String) it2.next();

				result = Math.min(result, table.getSimilarityBetween(symbolQ,
						symbolC, null));
			}
		}

		return new Double(result);
	}

	/**
	 * Used for table calculation. Will be envoked by introspection.
	 */
	@SuppressWarnings({ "unused", "unchecked" })
	private Double getAverage(Collection Q, Collection C) {
		int n = 0;
		double result = 0;
		// SMF_Symbol_Standard table = getSmfTable();

		for (Iterator it1 = Q.iterator(); it1.hasNext();) {
			String symbolQ = (String) it1.next();

			for (Iterator it2 = C.iterator(); it2.hasNext();) {
				String symbolC = (String) it2.next();

				n++;
				result += table.getSimilarityBetween(symbolQ, symbolC, null);
			}
		}

		result /= n;
		return new Double(Helper.getSimilarityStr(result));
	}
}
