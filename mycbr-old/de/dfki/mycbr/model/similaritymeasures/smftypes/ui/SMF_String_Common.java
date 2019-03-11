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
package de.dfki.mycbr.model.similaritymeasures.smftypes.ui;

import java.awt.Frame;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.Element;
import org.jdom.JDOMException;

import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.explanation.SMExplanationContainer;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.smftypes.FakeSlot;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_String_Override;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.retrieval.Explanation;

public abstract class SMF_String_Common extends AbstractSMFunction {

	private static final long serialVersionUID = 1L;
	private final static Logger log = Logger.getLogger ( SMF_String_Common.class.getName ( ) );
	// the possible "directions" for partial match:
	// this means that query should be a substring of case: 
	public static final int DIR_QUERY_IS_SUB = 0;
	// this means that case should be a substring of query: 
	public static final int DIR_CASE_IS_SUB = 1;
	// this means that one should be a substring of the other: 
	public static final int DIR_ANY_IS_SUB = 2;

	public static final int COUNT_RELATIVE = 0;
	public static final int COUNT_ABSOLUTE = 1;
	protected static final String[] countToStr = new String[] {
		"relative", "absolute"
	};

	
	public static final String DIR_QUERY_IS_SUB_STR = "query_is_substring";
	public static final String DIR_CASE_IS_SUB_STR = "case_is_substring";
	public static final String DIR_ANY_IS_SUB_STR = "any_is_substring";

	public static final int MODE_EXACT_MATCH 	= 0;
	public static final int MODE_PARTIAL_MATCH 	= 1;
	public static final int MODE_NGRAM 		= 2;
	public static final int MODE_REGEXPR = 3;
	public static final int LAST_MODE = 3;
	
	public static final String XML_ATT_MODE = "mode";
	public static final String XML_ATT_DIR = "dir";
	public static final String XML_ATT_CASE_SENS = "case_sensitive";
	public static final String XML_ATT_NGRAM = "ngram_value";
	public static final String XML_ATT_COUNT = "count";

	public static final String XML_TAG_SMF_SYMBOLS 		= "SMF_Symbols";
	public static final String FAKESLOT_PREFIX			= "SYMBOLIC_";
	
	protected static final String[] dirToString = new String[] {
		// maps modes indexes to strings:
	  DIR_QUERY_IS_SUB_STR, DIR_CASE_IS_SUB_STR, DIR_ANY_IS_SUB_STR
	};
	
	public static final String MODE_EXACT_MATCH_STR 	= "exact_match";
	public static final String MODE_PARTIAL_MATCH_STR = "partial_match";
	public static final String MODE_NGRAM_STR = "ngram";
	public static final String MODE_REGEXPR_STR = "regular_expression";
	
	private SMF_String_Override smfSymbols = null;
	private boolean symbolicExtension = false;
		
	protected static final String[] modeToString = new String[] {
		// maps modes indexes to strings:
		MODE_EXACT_MATCH_STR, MODE_PARTIAL_MATCH_STR,
		MODE_NGRAM_STR, MODE_REGEXPR_STR
	};

	public SMF_String_Common(ModelInstance inst, Element smfElement) throws JDOMException {
		super(inst, smfElement);
		Element smfSymbolsElement = smfElement.getChild(XML_TAG_SMF_SYMBOLS);
		if (smfSymbolsElement != null) {
			try {
				FakeSlot fakeslotSymbols = new FakeSlot( FAKESLOT_PREFIX + inst.getName(), ValueType.STRING);
				smfSymbols = new SMF_String_Override(fakeslotSymbols, smfSymbolsElement);
				symbolicExtension=true;
			} catch (Exception e) {
				log.log(Level.SEVERE, "error loading internal symbol SMF!", e);
				throw new JDOMException("error loading internal symbol SMF!", e);
			}
		}
	}
	
	public SMF_String_Common(ModelInstance inst, String smfName) throws Exception {
		super(inst, smfName);
	}
	
	public abstract void setDirection(int dir);
	
	@Override
	public double getSimilarityBetween(Object query, Object cb, Explanation exp) throws Exception {
		double sim = -1d;
		if (smfSymbols != null) {
			try {
				sim = smfSymbols.getSimilarityBetween(query, cb, exp);
			} catch (Exception e) {
				// (most probably) query or case is not known as a symbol.
			}
		}
		return sim;
	}
	
	@Override
	public SMExplanationContainer getExplanationContainer() {
		if (smfSymbols == null) {
			return null;
		}
		return smfSymbols.getExplanationContainer();
	}
	
	@Override
	public void initExplanationContainer() {
		if (smfSymbols == null) {
			return;
		}
		smfSymbols.initExplanationContainer();
	}

	@Override
	public void initExplanationContainer(Element element) {
		if (smfSymbols == null) {
			return;
		}
		smfSymbols.initExplanationContainer(element);
	}
	
	@Override
	public void toXML(Element xmlElement) {
		if (symbolicExtension) {
			Element smfSymbolsElement = smfSymbols.initXMLElement();
			smfSymbols.toXML(smfSymbolsElement);
			smfSymbolsElement.setName(XML_TAG_SMF_SYMBOLS);
			xmlElement.addContent(smfSymbolsElement);
		}
	}
	
	public boolean hasSymbolicExtension() {
		return symbolicExtension;
	}
	
	public void setSymbolicExtension(boolean symbolicExtension) {
		if (this.symbolicExtension == symbolicExtension) {
			return;
		}
		this.symbolicExtension = symbolicExtension;
		if (symbolicExtension) {
			FakeSlot fakeslotSymbols = new FakeSlot( FAKESLOT_PREFIX + inst.getName(), ValueType.STRING);
			try {
				smfSymbols = new SMF_String_Override(fakeslotSymbols, FAKESLOT_PREFIX+"SMF");
			} catch (Exception e) {
				log.log(Level.SEVERE, "Could not initialize SMF for " + fakeslotSymbols, e);
			}
			return;
		}
		smfSymbols = null;
	}

	@Override
	public boolean checkConsistency(Frame parent, boolean quiet) {
		if (symbolicExtension) {
			smfSymbols.checkConsistency(parent, quiet);
		}
		return true;
	}
	
	public SMF_String_Override getSymbolsSMF() {
		return smfSymbols;
	}
	
	public void setSymbolsSMF(SMF_String_Override smfSymbols) {
		this.smfSymbols = smfSymbols;
	}
	
}
