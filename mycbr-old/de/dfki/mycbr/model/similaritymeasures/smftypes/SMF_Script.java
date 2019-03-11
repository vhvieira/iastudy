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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.casebase.CaseInstanceScript;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel_Script;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.retrieval.Explanation;

public class SMF_Script extends AbstractSMFunction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(SMF_Script.class.getName());
	
	private static final String XML_ATTR_LANGUAGE = "Language";
	private static final String XML_ATTR_SCRIPT = "Script";
	
	private static final String DEFAULT_SCRIPT_INT = 
		"# Scripting system: Jython\n" +
		"# the local similarity measure has to be called 'sim'\n" +
		"\n" +
		"def sim(q, c):\n" + 
		"  return 1.0 - q / (c+1)\n" +
		"\n";
	
	private static final String DEFAULT_SCRIPT_STR = 
		"# Scripting system: Jython\n" +
		"# the local similarity measure has to be called 'sim'\n" +
		"\n" +
		"def editDistance(a, b):\n" +
		"    n, m = len(a), len(b)\n" +
		"    if n > m:\n" +
		"        # Make sure n <= m, to use O(min(n, m)) space\n" +
		"        a, b = b, a\n" +
		"        n, m = m, n\n" +
		"\n" +
		"    current = range(n+1)\n" +
		"    for i in range(1, m+1):\n" +
		"        previous, current = current, [i] + [0] * n\n" +
		"        for j in range(1,n+1):\n" +
		"            add, delete = previous[j] + 1, current[j - 1] + 1\n" +
		"            change = previous[j - 1]\n" +
		"            if a[j - 1] != b[i - 1]:\n" +
		"                change = change + 1\n" +
		"            current[j] = min(add, delete, change)\n" +
		"\n"             +
		"    return current[n]\n" +
		"\n" +
		"def sim(q, c):\n" +
		"    if q == '' and c == '':\n" +
		"        return 1.0\n" +
		"    elif q == '' or c == '':\n" +
		"        return 0.0\n" +
		"    else:\n" +
		"        return 1.0 - editDistance(q, c) / float(max(len(q), len(c))) # normalize\n" + 
		"\n";
		
	private ModelSlot slot;	
	private String script = ""; 
	private String language = "jython"; // the scripting language
	
	private PythonInterpreter interp;
	private PyObject sim; 
	
	protected String getDefaultScript(ModelInstance inst) {
		ModelSlot slot = (ModelSlot) inst;
		ValueType v = (slot.getValueType());
		if (v == ValueType.FLOAT || v == ValueType.INTEGER) {
			return DEFAULT_SCRIPT_INT;
		} else if (v == ValueType.STRING || v == ValueType.SYMBOL) {
			return DEFAULT_SCRIPT_STR;
		} 
		return null;
	}
	
	public SMF_Script(ModelInstance inst, Element smfElement) throws JDOMException {
		super(inst, smfElement);
		slot = (ModelSlot) inst;
		language = Helper.decode(smfElement.getAttributeValue(XML_ATTR_LANGUAGE));
		script = Helper.decode(smfElement.getAttributeValue(XML_ATTR_SCRIPT));
	}
	
	public SMF_Script(ModelInstance inst, String smfName) throws Exception {
		super(inst, smfName);
		slot = (ModelSlot) inst;
		this.script = getDefaultScript(slot);
	}
	
	public static String getSMFunctionTypeName_static() {
		return "Script";
	}

	public void toXML(Element xmlElement) {
		xmlElement.setAttribute(XML_ATTR_LANGUAGE, Helper.encode(language));
		xmlElement.setAttribute(XML_ATTR_SCRIPT, Helper.encode(script));
	}

	/**
	 * copies this similarity function object.
	 */
	public AbstractSMFunction copy() {
		SMF_Script newSmf = null;
		try {
			newSmf = new SMF_Script(inst, smfName);
			// copy things
			newSmf.script = this.script;
			newSmf.language = this.language;
			newSmf.setHasChanged(false);
		} catch (Exception e) {
			log.log(Level.SEVERE, "error while copying", e);
		}
		return newSmf;
	}

	/**
	 * returns a new editor panel.
	 */
	protected SMFPanel createSMFPanel()	{
		// initialize a new editor panel representing this smfunction object.
		return new SMFPanel_Script(this);
	}
	
	public void startRetrieval() throws Exception {
		interp = new PythonInterpreter();
		interp.exec(script);
		sim = interp.get("sim");
		if (sim == null) {
			throw new IOException("script does not contain a sim() routine");
		}
	}
	
	public void finishRetrieval() throws Exception {
		interp.cleanup();
	}

	
	public double getSimilarityBetween(Object query, Object cb, Explanation exp)
						throws Exception {
		if (sim == null) {
			startRetrieval(); // XXX: this is a quick fix that may not be correct
		}
		PyObject q = CaseInstanceScript.toPyObject(slot, query);
		PyObject c = CaseInstanceScript.toPyObject(slot, cb);
		PyObject res = sim.__call__(new PyObject[]{q, c});
		Object x = res.__tojava__(Double.class);
		
		if (x == Py.NoConversion) {
			throw new IOException("cannot convert Python object to a Java Double");
		}
		if (exp != null) {
			exp.setSimilarity((Double) x);
		}
		return (Double)x;
	}
	
	
	/**
	 * Check consistency to Slot. 
	 * If something doesn't match prompt user for some options and 
	 * make it consistent.
	 * @return boolean isConsistent
	 */
	public boolean checkConsistency(Frame parent, boolean quiet) {
		// Luckily this cannot become inconsistent; at least I cannot see
		// any reasons.
		return true;
	}
	
	public ValueType getValueType() {
		return slot.getValueType();
	}

	public ModelSlot getSlot() {
		return slot;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		setHasChanged(!script.equals(this.script));
		this.script = script;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		setHasChanged(!language.equals(this.language));
		this.language = language;
	}
}
