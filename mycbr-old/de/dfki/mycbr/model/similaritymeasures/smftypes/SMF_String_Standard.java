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

import javax.swing.JOptionPane;

import org.jdom.Element;
import org.jdom.JDOMException;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel_String_Standard;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMF_String_Common;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.retrieval.Explanation;

/**
 * @author myCBR Team
 *
 */
public class SMF_String_Standard extends SMF_String_Common {
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger ( SMF_String_Standard.class.getName ( ) );

	public static final ValueType VALUE_TYPE = ValueType.STRING;

	public static final int MODE_TRIGRAM 		= 0;
	public static final int MODE_EXACT_MATCH 	= 1;
	public static final int MODE_PARTIAL_MATCH 	= 2;
	private static final int LAST_MODE = 2;
	
	public static final String MODE_TRIGRAM_STR 		= "trigram";
	public static final String MODE_EXACT_MATCH_STR 	= "exact_match";
	public static final String MODE_PARTIAL_MATCH_STR = "partial_match";
	
	public static final String XML_ATT_MODE = "mode";
	
	private static final String[] modeToString = new String[] {
			// maps modes indexes to strings:
		  MODE_TRIGRAM_STR, MODE_EXACT_MATCH_STR, MODE_PARTIAL_MATCH_STR
	};
	
	private int matchingMode = MODE_EXACT_MATCH;

	public SMF_String_Standard(ModelInstance inst, Element smfElement) throws JDOMException {
		super(inst, smfElement);
		int index = Helper.findIndex(modeToString,Helper.decode(smfElement.getAttributeValue(XML_ATT_MODE)));
		if (index < 0 || index > LAST_MODE) {
			index = 0;
		}
		matchingMode = index;
	}

	public SMF_String_Standard(ModelInstance inst, String smfName) throws Exception {
		super(inst, smfName);
	}

	public void toXML(Element xmlElement) {
		xmlElement.setAttribute(XML_ATT_MODE, Helper.encode(modeToString[matchingMode]));
	}

	public AbstractSMFunction copy() {
		SMF_String_Standard smfCopy = null;
		
		try {
			smfCopy = new SMF_String_Standard(inst, smfName);
			smfCopy.matchingMode = matchingMode;
			
			smfCopy.setHasChanged(false);
		} catch (Exception e) {
			log.log(Level.SEVERE, "error while copying!", e);
		}
		
		return smfCopy;
	}

	protected SMFPanel createSMFPanel() {
		return new SMFPanel_String_Standard(this);
	}

	public boolean checkConsistency(Frame parent, boolean quiet) {
		log.fine("checking consistency for [" + getClass().getName().toUpperCase() + "].");
		ModelSlot slot = (ModelSlot)inst;
//		if (!(slot.getValueType().equals(getType())))
		if (slot.getValueType() != getValueType()) {
			if (!quiet) {
				JOptionPane.showMessageDialog(parent, "This smfunction was NOT made for type ["+slot.getValueType()+"]. Please check it!") ;
			}
			return false;
		}
		// nothing else to do.
		return true;
	}

	public static String getSMFunctionTypeName_static() {
		return "Standard";
	}

	public double getSimilarityBetween(Object query, Object cb, Explanation exp) {
		double sim = 0.0;
		
		switch (matchingMode) {
			case MODE_EXACT_MATCH: {
				sim = (query.equals(cb) ? 1.0 : 0.0);
				break;
			}
			case MODE_PARTIAL_MATCH: {
				String longStr  = query.toString();
				String shortStr = cb.toString();
				
				if (shortStr.length() > longStr.length()) {
					// swap strings
					String tmp = shortStr;
					shortStr = longStr;
					longStr = tmp;
				}
				
				// is the short one part of the other?
				if (longStr.indexOf(shortStr) >= 0) {
					// okay, at least there is one occurence of query in cb or vice versa.
					// similarity is now the ratio of string length.
					sim = ((double)shortStr.length()) / ((double)longStr.length());
				}				
				break;
			}
			case MODE_TRIGRAM: {
				sim = Helper.ngram(query.toString(), cb.toString(), 3, true);
				break;
			}
		}
		
		//
		// Explanation
		if (exp != null) {
			Helper.addExplanation(exp, this.inst, query, cb, this, null, sim);
		}
		
		return sim;
	}

	public ValueType getValueType() {
		return VALUE_TYPE;
	}

	public String getMatchingMode() {
		return modeToString[matchingMode];
	}

	public void setMatchingMode(String matchingMode) {
		log.fine("set matching mode = [" + matchingMode + "]");
		setHasChanged(true);
		int index = Helper.findIndex(modeToString, matchingMode);
		if (index < 0 || index > LAST_MODE) {
			index = 0;
		}

		this.matchingMode = index;
	}

	@Override
	public void setDirection(int dir) {
		// TODO Auto-generated method stub
	}

}
