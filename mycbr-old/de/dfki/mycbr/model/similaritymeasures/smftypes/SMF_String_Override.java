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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.explanation.SMExplanationContainer;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel_String_Override;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.retrieval.Explanation;

public class SMF_String_Override extends AbstractSMFunction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger ( SMF_String_Override.class.getName ( ) );

	public static final String XML_TAG_QUERYSYMBOL = "QuerySymbol";
	public static final String XML_TAG_CBSYMBOL 	= "CBSymbol";
	public static final String XML_ATT_SIMILARITY 	= "sim";
	public static final String XML_ATT_SYMBOL		= "symbol";
	
	HashSet<String> symbolOrder = new HashSet<String>();
	HashMap<String, HashMap<String,Double>> map = new HashMap<String, HashMap<String,Double>>();

	private boolean isSymmetricMode;
	public SMF_String_Override(ModelInstance inst, String smfName) {
		super(inst, smfName);
	}

	@SuppressWarnings("unchecked")
	public SMF_String_Override(ModelInstance inst, Element smfElement) throws JDOMException {
		super(inst, smfElement);
		// load similarity data
		for (Iterator it1 = smfElement.getDescendants(new ElementFilter(XML_TAG_QUERYSYMBOL)); it1.hasNext();) {
			Element querySymbols = (Element)it1.next();
			String symbolQ = Helper.decode(querySymbols.getAttributeValue(XML_ATT_SYMBOL));
			HashMap<String,Double> queryMap = map.get(symbolQ);
			if (queryMap == null) {
				queryMap = new HashMap<String, Double>();
				map.put(symbolQ, queryMap);
			}
			for (Iterator it2 = querySymbols.getDescendants(new ElementFilter(XML_TAG_CBSYMBOL)); it2.hasNext();) {
				Element cbSymbols = (Element)it2.next();

				String symbolCb = Helper.decode(cbSymbols.getAttributeValue(XML_ATT_SYMBOL));
				double sim = cbSymbols.getAttribute(XML_ATT_SIMILARITY).getDoubleValue();
				queryMap.put(symbolCb, sim);
			}
		}
		isSymmetricMode = isSymmetric();
	}

	public void setSimilarity(Double sim, String qString, String cString) {
		setSimilarity_internal(sim, qString, cString);
		if (isSymmetricMode) {
			setSimilarity_internal(sim, cString, qString);
		}
	}
	
	private void setSimilarity_internal(Double sim, String qString, String cString) {
		HashMap<String, Double> map2 = map.get(qString);
		if (map2 == null) {
			map2 = new HashMap<String, Double>();
			map.put(qString, map2);
		}
		map2.put(cString, sim);
	}
	
	private boolean isSymmetric() {
		for (Entry<String, HashMap<String,Double>> e1: map.entrySet()) {
			String s1 = e1.getKey();
			for (Entry<String,Double> e2 : e1.getValue().entrySet()) {
				String s2 = e2.getKey();
				Double sim1 = e2.getValue();

				HashMap<String, Double> m2 = map.get(s2);
				if (m2 == null) {
					return false;
				}
				if (m2.get(s1) != sim1) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean checkConsistency(Frame parent, boolean quiet) {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public AbstractSMFunction copy() {
		SMF_String_Override newSmf = null;
		try {
			newSmf = new SMF_String_Override(inst, smfName);
			
			for (Entry<String, HashMap<String,Double>> e1 : map.entrySet()) {
				String s1 = e1.getKey();
				newSmf.map.put(s1, (HashMap<String, Double>) e1.getValue().clone());
			}
			newSmf.isSymmetricMode = isSymmetricMode;
			newSmf.setHasChanged(false);
			
		} catch (Exception e) {
			log.log(Level.SEVERE, "error while copying!", e);
		}

		return newSmf;
	}

	@Override
	protected SMFPanel createSMFPanel() {
		return new SMFPanel_String_Override(this);
	}

	@Override
	public double getSimilarityBetween(Object query, Object cb, Explanation exp) throws Exception {
		String symbolQ = (String) query;
		String symbolC = (String) cb;
		
		HashMap<String, Double> map2 = map.get(symbolQ);
		double result = -1d;
		
		if (map2 != null) {
			Double sim = map2.get(symbolC);
			if (sim != null) {
				result = sim;
			}
		}
		
		if (exp != null) {
			exp.setSimilarity(result);
		}
		return result;
	}

	@Override
	public ValueType getValueType() {
		return ValueType.STRING;
	}

	@Override
	public void toXML(Element xmlElement) {
		for (Entry<String, HashMap<String,Double>> e1 : map.entrySet()) {
			String q = e1.getKey();
			Element queryElement = new Element(XML_TAG_QUERYSYMBOL);
			queryElement.setAttribute(XML_ATT_SYMBOL, Helper.encode(q));
			for (Entry<String,Double> e2 : e1.getValue().entrySet()) {
				String c = e2.getKey();
				Double sim = e2.getValue();
				Element cbElement = new Element(XML_TAG_CBSYMBOL);
				cbElement.setAttribute(XML_ATT_SIMILARITY, Double.toString(sim));
				cbElement.setAttribute(XML_ATT_SYMBOL, Helper.encode(c));
				queryElement.addContent(cbElement);
			}
			xmlElement.addContent(queryElement);
		}
	}

	public HashMap<String, HashMap<String, Double>> getMap() {
		return map;
	}

	@Override
	public void initExplanationContainer() {
		explanationContainer = new SMExplanationContainer(getValueType());
	}
	
	@Override
	public void initExplanationContainer(Element element) {
		explanationContainer = new SMExplanationContainer(getValueType());
		explanationContainer.init(element);
	}
	
}
