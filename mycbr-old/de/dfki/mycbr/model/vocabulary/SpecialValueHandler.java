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
package de.dfki.mycbr.model.vocabulary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import org.jdom.Element;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.similaritymeasures.smftypes.FakeSlot;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Symbol_Table;
import de.dfki.mycbr.retrieval.Explanation;

public class SpecialValueHandler implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String XML_TAG_SPECIAL_VALUE_HANDLER= "SpecialValueHandler";
	public static final String XML_TAG_INTERNAL_SMF 		= "InternalSMF";
	public static final String XML_TAG_SPECIALVALUE 		= "SpecialValue";
	public static final String XML_ATT_LABEL 				= "Label";

	public static final SpecialValue SPECIAL_VALUE_UNDEFINED 	= new SpecialValue("undefined");
	
	public static final String NON_SPECIAL_VALUE 			= "Non-Special Value";
	
	private FakeSlot fakeSlot;
	
	private HashSet<SpecialValue> allSpecialValues = new HashSet<SpecialValue>();
	private SMF_Symbol_Table internalSMF;
	private static SpecialValueHandler instance;
	
	private SpecialValueHandler() {
		allSpecialValues.add(SPECIAL_VALUE_UNDEFINED);
	
		fakeSlot = new FakeSlot("_SPECIAL_VALUES_", ValueType.SYMBOL);
		updateFakeSlot();
		internalSMF = new SMF_Symbol_Table(fakeSlot, "SpecialValuesSMF");

	}

	public static final SpecialValueHandler getInstance() {
		return instance;
	}

	public static SpecialValueHandler initInstance() {
		instance = new SpecialValueHandler();	
		return instance;
	}
	
	public static void resetInstance() {
		instance = null;
	}
	
	/**
	 * 
	 * @param value the value that is tested
	 * @return true iff the slot value is a special value, e.g. "undefined"
	 */
	public boolean isSpecialValue(Object value) {
		return allSpecialValues.contains(value);
	}
	
	public boolean isSpecialValueStr(String str) {
		for (Iterator<SpecialValue> it=allSpecialValues.iterator(); it.hasNext();) {
			SpecialValue sv = it.next();
			if (sv.toString().equals(str)) {
				return true;
			}
		}
		return false;
	}
	
	public SpecialValue getSpecialValueFromStr(String str) {
		for (Iterator<SpecialValue> it=allSpecialValues.iterator(); it.hasNext();) {
			SpecialValue sv = it.next();
			if (sv.getName().equals(str)) {
				return sv;
			}
		}
		return null;
	}
	
	public boolean createSpecialValue(String name) {
		for (Iterator<SpecialValue> it = allSpecialValues.iterator(); it.hasNext();) {
			SpecialValue sv = it.next();
			if (sv.getName().equals(name)) {
				return false;
			}
		}
		
		SpecialValue sv = new SpecialValue(name);
		allSpecialValues.add(sv);
		return true;
	}
	
	public void removeSpecialValue(SpecialValue value) {
		allSpecialValues.remove(value);
	}


	public double getSimilarityBetween(Object query, Object cb, Explanation exp) {
		String innerSymbolQ = (isSpecialValue(query) ? ((SpecialValue) query).getName() : NON_SPECIAL_VALUE);
		String innerSymbolC = (isSpecialValue(cb) ? ((SpecialValue) cb).getName() : NON_SPECIAL_VALUE);

		double result = internalSMF.getSimilarityBetween(innerSymbolQ, innerSymbolC, exp);

		if (exp != null) {
			exp.addComment("", "Handle Special Values:\nSim(Query[" + innerSymbolQ + "], Case[" + innerSymbolC + "]) = " + Helper.formatDoubleAsString(result));
		}
		
		return result;
	}

	@SuppressWarnings("unchecked")
	public void load(Element element) throws Exception {
		allSpecialValues.clear();
		allSpecialValues.add(SPECIAL_VALUE_UNDEFINED);
//		allSpecialValues.add(SPECIAL_VALUE_UNKNOWN);
		for (Iterator it=element.getChildren(XML_TAG_SPECIALVALUE).iterator(); it.hasNext();) {
			Element svElement = (Element) it.next();
			String label = Helper.decode(svElement.getAttributeValue(XML_ATT_LABEL));
//			if (label.equals(SPECIAL_VALUE_UNDEFINED.getName()) || label.equals(SPECIAL_VALUE_UNKNOWN.getName())) continue;
			if (label.equals(SPECIAL_VALUE_UNDEFINED.getName())) continue;
			createSpecialValue(label);
		}
		updateFakeSlot();
		
		Element smfElement = element.getChild(XML_TAG_INTERNAL_SMF);
		internalSMF = new SMF_Symbol_Table(fakeSlot, smfElement);
	}
	
	@SuppressWarnings("unchecked")
	public void toXML(Element xmlElement) {
		for (Iterator it = allSpecialValues.iterator(); it.hasNext();) {
			SpecialValue sv = (SpecialValue) it.next();
			Element svElement = new Element(XML_TAG_SPECIALVALUE);
			svElement.setAttribute(XML_ATT_LABEL, Helper.encode(sv.getName()));
			xmlElement.addContent(svElement);
		}
		
		Element smfElement = internalSMF.initXMLElement();
		internalSMF.toXML(smfElement);
		smfElement.setName(XML_TAG_INTERNAL_SMF);
		xmlElement.addContent(smfElement);
	}

	@SuppressWarnings("unchecked")
	public void updateFakeSlot() {
		Vector<String> fakeSlotSymbols = new Vector<String>();
		for (Iterator it = allSpecialValues.iterator(); it.hasNext();) {
			SpecialValue sv = (SpecialValue) it.next();
			fakeSlotSymbols.add(sv.getName());
		}
		Collections.sort(fakeSlotSymbols);
		fakeSlotSymbols.insertElementAt(NON_SPECIAL_VALUE, 0);
		fakeSlot.setAllowedValues(fakeSlotSymbols);
	}

	public SMF_Symbol_Table getInternalSMF() {
		return internalSMF;
	}

	public HashSet<SpecialValue> getAllSpecialValues() {
		return allSpecialValues;
	}

	public Collection<String> getAllSpecialValuesAsStrings() {
		ArrayList<String> al = new ArrayList<String>();
		for (Iterator<SpecialValue> it = allSpecialValues.iterator(); it.hasNext();) {
			SpecialValue sv = (SpecialValue) it.next();
			al.add(sv.toString());
		}
		return al;
	}

}
