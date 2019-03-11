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
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel_String_Word;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMF_String_Common;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.retrieval.Explanation;

public class SMF_String_Word extends SMF_String_Common {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(SMF_String_Standard.class.getName());

	public static final String DEFAULT_SEP = "\\W+";

	// these are the field of interest: -------------------------------
	private String sepRegExpr = DEFAULT_SEP; // what is a separator?
	private int dir = DIR_QUERY_IS_SUB;
	private int count = COUNT_RELATIVE;
	private int wordSim = MODE_EXACT_MATCH; 
	// wordSim is either MODE_EXACT_MATCH or MODE_NGRAM
	
	private boolean caseSensitive = false;
	// ----------------------------------------------------------------
	
	public static final ValueType VALUE_TYPE = ValueType.STRING;
	
	public static final String XML_ATT_SEPREGEX = "sep_regex";
	
	public SMF_String_Word(ModelInstance inst, Element smfElement) throws JDOMException	{
		super(inst, smfElement);
		this.sepRegExpr = Helper.decode(smfElement.getAttributeValue(XML_ATT_SEPREGEX));
		int index = Helper.findIndex(dirToString, Helper.decode(smfElement.getAttributeValue(XML_ATT_DIR)));
		if (index < 0 || index > LAST_MODE) {
			index = 0;
		}
		this.dir = index;
		index = Helper.findIndex(countToStr, Helper.decode(smfElement.getAttributeValue(XML_ATT_COUNT)));
		if (index != COUNT_RELATIVE && index != COUNT_ABSOLUTE) {
			index = COUNT_RELATIVE;
		}
		this.count = index;
		index = Helper.findIndex(modeToString, Helper.decode(smfElement.getAttributeValue(XML_ATT_MODE)));
		if (index < 0 || index > LAST_MODE) {
			index = 0;
		}
		this.wordSim = index;
		this.caseSensitive = smfElement.getAttribute(XML_ATT_CASE_SENS).getBooleanValue();
	}

	public SMF_String_Word(ModelInstance inst, String smfName) throws Exception	{
		super(inst, smfName);
	}

	public void toXML(Element xmlElement) {
		super.toXML(xmlElement);
		xmlElement.setAttribute(XML_ATT_SEPREGEX, Helper.encode(sepRegExpr));
		xmlElement.setAttribute(XML_ATT_DIR, Helper.encode(dirToString[dir]));
		xmlElement.setAttribute(XML_ATT_COUNT, Helper.encode(countToStr[count]));
		xmlElement.setAttribute(XML_ATT_MODE,Helper.encode( modeToString[wordSim]));
		xmlElement.setAttribute(XML_ATT_CASE_SENS, Helper.encode(caseSensitive + ""));
	}

	public AbstractSMFunction copy() {
		SMF_String_Word smfCopy = null;
		try	{
			smfCopy = new SMF_String_Word(inst, smfName);
			smfCopy.caseSensitive = caseSensitive;
			smfCopy.dir = dir;
			smfCopy.count = count;
			smfCopy.sepRegExpr = sepRegExpr;
			smfCopy.wordSim = wordSim;
			smfCopy.setHasChanged(false);
		} catch (Exception e) {
			log.log(Level.SEVERE, "error while copying!", e);
		}
		return smfCopy;
	}

	protected SMFPanel createSMFPanel()	{
		return new SMFPanel_String_Word(this);
	}

	public boolean checkConsistency(Frame parent, boolean quiet) {
		super.checkConsistency(parent, quiet);
		log.fine("checking consistency for ["+getClass().getName().toUpperCase()+"].");
		ModelSlot slot = (ModelSlot) inst;
		if (slot.getValueType() != getValueType()) {
			if (!quiet) {
				JOptionPane.showMessageDialog(parent, "This smfunction was NOT made for type ["+slot.getValueType()+"]. Please check it!") ;
			}
			return false;
		}
		// nothing else to do.
		return true;
	}

	public static String getSMFunctionTypeName_static()	{
		return "Word-Based";
	}

	private double countWords(String[] qwords, String[] cwords) {
		double result = 0.0;
		if (wordSim == MODE_EXACT_MATCH) {
			for (int i = 0; i < qwords.length; i++) {
				for (int j = 0; j < cwords.length; j++) {
					if (qwords[i].equals(cwords[j])) {
						result += 1.0;
					}
				}
			}
		} else {
			// we use the maximum similiarity:
			for (int i = 0; i < qwords.length; i++) {
				double max = 0.0;
				for (int j = 0; j < cwords.length; j++) {
					// since we already converted to lower case if needed, it
					// is safe to pass 'false' to ngram
					double n = Helper.ngram(qwords[i], cwords[j], 3, false);
					if (n > max) {
						max = n;
					}
				}
				result += max;
			}
		}
		return result;
	}
	
	public double getSimilarityBetween(Object query, Object cb, Explanation exp) throws Exception {
		double sim = super.getSimilarityBetween(query, cb, exp);
		if (sim>=0d) {
			return sim;
		}
		String q = query.toString();
		String c = cb.toString();
		if (!caseSensitive) {
			q = q.toLowerCase();
			c = c.toLowerCase();
		}
		String[] cwords = c.split(sepRegExpr);
		String[] qwords = q.split(sepRegExpr);
		
		switch (dir) {
			case DIR_QUERY_IS_SUB:
				sim = countWords(qwords, cwords);
				break;
			case DIR_CASE_IS_SUB:
				sim = countWords(cwords, qwords);
				break;
			case DIR_ANY_IS_SUB: 
				sim = Math.max(countWords(qwords, cwords), countWords(cwords, qwords));
				break;
		}
		if (qwords.length == 0) {
			sim = 1.0; // no words wanted gives 1.0 similarity
		} else if (count == COUNT_RELATIVE) {
			sim /= qwords.length;
		} else {
			if (cwords.length > 0) {
				sim /= cwords.length;
			} else {
				sim = 0.0; // no words in case gives 0.0 similarity
			}
		}
 		// Explanation
		if (exp != null) {
			Helper.addExplanation(exp, this.inst, query, cb, this, null, sim);
		}
		return sim;
	}

	public void setDirection(int dir) {
		setHasChanged(dir != this.dir);
		this.dir = dir;
	}
	
	public ValueType getValueType() {
		return VALUE_TYPE;
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

	/**
	 * @return the dir
	 */
	public int getDir() {
		return dir;
	}

	/**
	 * @param dir the dir to set
	 */
	public void setDir(int dir) {
		setHasChanged(dir != this.dir);
		this.dir = dir;
	}

	/**
	 * @return the sepRegExpr
	 */
	public String getSepRegExpr() {
		return sepRegExpr;
	}

	/**
	 * @param sepRegExpr the sepRegExpr to set
	 */
	public void setSepRegExpr(String sepRegExpr) {
		setHasChanged(!sepRegExpr.equals(this.sepRegExpr));
		this.sepRegExpr = sepRegExpr;
	}

	/**
	 * @return the wordSim
	 */
	public int getWordSim() {
		return wordSim;
	}

	/**
	 * @param wordSim the wordSim to set
	 */
	public void setWordSim(int wordSim) {
		setHasChanged(wordSim != this.wordSim);
		this.wordSim = wordSim;
	}
}
