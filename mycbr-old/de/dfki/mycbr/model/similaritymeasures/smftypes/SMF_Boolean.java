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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.Element;
import org.jdom.JDOMException;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel_Boolean;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.retrieval.Explanation;

/**
 * Similarity of boolean types is trivial, however, it has to be implemented.
 * @author myCBR Team
 */
public class SMF_Boolean extends AbstractSMFunction {
	private static final long serialVersionUID = 1L;
	private final static Logger log = Logger.getLogger ( SMF_Boolean.class.getName ( ) );
	
	public static final String XML_TAG_QUERYSYMBOL = "QuerySymbol";
	public static final String XML_TAG_CBSYMBOL 	= "CBSymbol";
	public static final String XML_ATT_SIMILARITY 	= "sim";
	public static final String XML_ATT_BOOLEAN		= "boolean";
	private Object smfElement = null;
	
	public SMF_Boolean(ModelInstance inst, Element smfElement)
			throws JDOMException {
		super(inst, smfElement);
		this.smfElement = smfElement;
	}
	
	public SMF_Boolean(ModelInstance inst, String smfElement)
	throws JDOMException {
		super(inst, smfElement);
		this.smfElement = smfElement;
	}
	
	public void toXML(Element xmlElement) {
		Element queryElement = new Element(XML_TAG_QUERYSYMBOL);
		queryElement.setAttribute(XML_ATT_BOOLEAN, "false");
		
		Element cbElement = new Element(XML_TAG_CBSYMBOL);
		cbElement.setAttribute(XML_ATT_SIMILARITY, "1.0");
		cbElement.setAttribute(XML_ATT_BOOLEAN, "false");
		queryElement.addContent(cbElement);
		
		cbElement = new Element(XML_TAG_CBSYMBOL);
		cbElement.setAttribute(XML_ATT_SIMILARITY, "0.0");
		cbElement.setAttribute(XML_ATT_BOOLEAN, "true");
		queryElement.addContent(cbElement);
		
		xmlElement.addContent(queryElement);
		
		queryElement = new Element(XML_TAG_QUERYSYMBOL);
		queryElement.setAttribute(XML_ATT_BOOLEAN, "true");
		
		cbElement = new Element(XML_TAG_CBSYMBOL);
		cbElement.setAttribute(XML_ATT_SIMILARITY, "1.0");
		cbElement.setAttribute(XML_ATT_BOOLEAN, "true");
		queryElement.addContent(cbElement);
		
		cbElement = new Element(XML_TAG_CBSYMBOL);
		cbElement.setAttribute(XML_ATT_SIMILARITY, "0.0");
		cbElement.setAttribute(XML_ATT_BOOLEAN, "false");
		queryElement.addContent(cbElement);
		
		xmlElement.addContent(queryElement);
	}

	public AbstractSMFunction copy() {
		SMF_Boolean newSmf = null;
		
		try {
			newSmf = new SMF_Boolean(inst, smfElement.toString());
		} catch (Exception e) {
			log.log(Level.SEVERE, "error while copying!", e);
		}
		return newSmf;
	}
	
	public static String getSMFunctionTypeName_static() {
		return "Boolean";
	}

	protected SMFPanel createSMFPanel() {
		return new SMFPanel_Boolean(this);
	}

	public boolean checkConsistency(Frame parent, boolean quiet) {
		return true;
	}

	public double getSimilarityBetween(Object query, Object cb, Explanation exp) {
		double result = 1.0;
		// TODO != operator for strings??
		if (query.toString() != cb.toString()) {
			result = 0.0;
		}
		
		// TODO what is this explanation?
 		// Explanation
		if (exp != null) {
			Helper.addExplanation(exp, this.inst, query, cb, this, null, result);
		}
		
		return result;
	}

	public ValueType getValueType() {
		return ValueType.BOOLEAN;
	}
	
}
