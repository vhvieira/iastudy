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
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.jdom.Element;
import org.jdom.JDOMException;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel_String_Character;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMF_String_Common;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.retrieval.Explanation;

public class SMF_String_Character extends SMF_String_Common {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger ( SMF_String_Standard.class.getName ( ) );

	public static final ValueType VALUE_TYPE = ValueType.STRING;
		
	private int matchingMode = MODE_EXACT_MATCH;
	private int dir = DIR_QUERY_IS_SUB;
	private boolean caseSensitive = false;
	private int ngram_val = 3;
	private int count = COUNT_RELATIVE;

	public void setDirection(int dir) {
		setHasChanged(dir != this.dir);
		this.dir = dir;
	}
	
	public SMF_String_Character(ModelInstance inst, Element smfElement) throws JDOMException {
		super(inst, smfElement);
		int index = Helper.findIndex(modeToString,Helper.decode( smfElement.getAttributeValue(XML_ATT_MODE)));
		if (index < 0 || index > LAST_MODE) index = 0;
		matchingMode = index;
		
		index = Helper.findIndex(dirToString, 
				Helper.decode(smfElement.getAttributeValue(XML_ATT_DIR)));
		if (index < 0 || index > LAST_MODE) {
			index = 0;
		}
		dir = index;
		index = Helper.findIndex(countToStr, Helper.decode(smfElement.getAttributeValue(XML_ATT_COUNT)));
		if (index != COUNT_RELATIVE && index != COUNT_ABSOLUTE) {
			index = COUNT_RELATIVE;
		}
		this.count = index;
		
		caseSensitive = smfElement.getAttribute(XML_ATT_CASE_SENS).getBooleanValue();
		ngram_val = smfElement.getAttribute(XML_ATT_NGRAM).getIntValue();
	}

	public SMF_String_Character(ModelInstance inst, String smfName) throws Exception {
		super(inst, smfName);
	}

	public void toXML(Element xmlElement) {
		super.toXML(xmlElement);
		xmlElement.setAttribute(XML_ATT_MODE, Helper.encode(modeToString[matchingMode]));
		xmlElement.setAttribute(XML_ATT_DIR, Helper.encode(dirToString[dir]));
		xmlElement.setAttribute(XML_ATT_CASE_SENS, Helper.encode(caseSensitive + ""));
		xmlElement.setAttribute(XML_ATT_NGRAM, Helper.encode(ngram_val + ""));
		xmlElement.setAttribute(XML_ATT_COUNT, Helper.encode(countToStr[count]));
	}

	public AbstractSMFunction copy() {
		SMF_String_Character smfCopy = null;
		
		try {
			smfCopy = new SMF_String_Character(inst, smfName);
			smfCopy.matchingMode = matchingMode;
			smfCopy.dir = dir;
			smfCopy.caseSensitive = caseSensitive;
			smfCopy.ngram_val = ngram_val;
			smfCopy.count = count;
			smfCopy.setHasChanged(false);
		} catch (Exception e) {
			log.log(Level.SEVERE, "error while copying!", e);
		}
		
		return smfCopy;
	}

	protected SMFPanel createSMFPanel() {
		return new SMFPanel_String_Character(this);
	}

	public boolean checkConsistency(Frame parent, boolean quiet) {
		super.checkConsistency(parent, quiet);
		log.fine("checking consistency for ["+getClass().getName().toUpperCase()+"].");
		ModelSlot slot = (ModelSlot)inst;
//		if (!(slot.getValueType().equals(getType())))
		if (slot.getValueType() != getValueType()) {
			if (!quiet) JOptionPane.showMessageDialog(parent, 
					"This smfunction was NOT made for type ["+slot.getValueType()+
					"]. Please check it!") ;
			return false;
		}
		// nothing else to do.
		return true;
	}

	public static String getSMFunctionTypeName_static() {
		return "Character-Based";
	}

	private double partialMatch(String a, String b) {
		// a should be substring of b
		// is the short one part of the other?
		if (caseSensitive) {
			if (b.indexOf(a) >= 0) {
				if (count == COUNT_RELATIVE) {
				// similarity is now the ratio of string length.
					return ((double)a.length()) / ((double)b.length());
				} else {
					return 1.0;
				}
			}
		} else {
			if (b.toLowerCase().indexOf(a.toLowerCase()) >= 0) {
				if (count == COUNT_RELATIVE) {
					return ((double)a.length()) / ((double)b.length());
				} else {
					return 1.0;
				}
			}
		}
		return 0.0;
	}
	
	private double myMatch(String s, String regexpr) {
		try {
			Pattern p;
			if (caseSensitive) {
				p = Pattern.compile(regexpr);
			} else {
				p = Pattern.compile(regexpr, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
			}
			return p.matcher(s).matches() ? 1.0 : 0.0;
			//return (s.matches(regexpr) ? 1.0 : 0.0);
		} catch (java.util.regex.PatternSyntaxException e) {
			return 0.0;
		}
	}
	
	public double getSimilarityBetween(Object query, Object cb, Explanation exp) throws Exception {
		double sim = super.getSimilarityBetween(query, cb, exp);
		if (sim >= 0d) {
			return sim;
		}
		String q = query.toString();
		String c = cb.toString();
		
		switch (matchingMode) {
			case MODE_EXACT_MATCH: {
				if (caseSensitive) {
					sim = (q.equals(c) ? 1.0 : 0.0);
				} else {
					sim = (q.equalsIgnoreCase(c) ? 1.0 : 0.0);
				}
				break;
			}
			case MODE_PARTIAL_MATCH: {
				switch (dir) {
					case DIR_QUERY_IS_SUB:
						sim = partialMatch(q, c);
						break;
					case DIR_CASE_IS_SUB:
						sim = partialMatch(c, q);
						break;
					case DIR_ANY_IS_SUB: 
						if (q.length() <= c.length()) {
							sim = partialMatch(q, c);
						} else {
							sim = partialMatch(c, q);
						}
						break; 
				}
				break;
			}
			case MODE_NGRAM: {
				sim = Helper.ngram(q, c, ngram_val, caseSensitive);
				break;
			}
			case MODE_REGEXPR: {
				switch (dir) {
					case DIR_QUERY_IS_SUB:
						sim = myMatch(c, q);
						break;
					case DIR_CASE_IS_SUB:
						sim = myMatch(q, c);
						break;
					case DIR_ANY_IS_SUB: 
						sim = myMatch(c, q);
						if (sim == 0.0)	sim = myMatch(q, c);
						break;
				}
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

	public int getMatchingMode() {
		return matchingMode;
	}

	public void setMatchingMode(int matchingMode) {
		log.fine("set matching mode = [" + matchingMode + "]");
		setHasChanged(true);
		int index = matchingMode;
		if (index < 0 || index > LAST_MODE) {
			index = 0;
		}
		this.matchingMode = index;
	}

	/**
	 * @return the caseSensitive
	 */
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	/**
	 * @param caseSensitive the caseSensitive to set
	 */
	public void setCaseSensitive(boolean caseSensitive) {
		setHasChanged(caseSensitive != this.caseSensitive);
		this.caseSensitive = caseSensitive;
	}

	/**
	 * @return the ngram_val
	 */
	public int getNgram_val() {
		return ngram_val;
	}

	/**
	 * @param ngram_val the ngram_val to set
	 */
	public void setNgram_val(int ngram_val) {
		setHasChanged(ngram_val != this.ngram_val);
		this.ngram_val = ngram_val;
	}

	/**
	 * @return the subDirection
	 */
	public int getDir() {
		return dir;
	}

	/**
	 * @param subDirection the subDirection to set
	 */
	public void setDir(int dir) {
		setHasChanged(dir != this.dir);
		this.dir = dir;
	}
	

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		setHasChanged(count != this.count);
		this.count = count;
	}
}
