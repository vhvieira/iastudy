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
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.casebase.CaseInstance;
import de.dfki.mycbr.model.casebase.CaseInstanceScript;
import de.dfki.mycbr.model.similaritymeasures.AbstractClassSM;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.SimMap;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel_Class_Script;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.model.vocabulary.ModelSlotScript;
import de.dfki.mycbr.model.vocabulary.SpecialValue;
import de.dfki.mycbr.model.vocabulary.SpecialValueHandler;
import de.dfki.mycbr.retrieval.Explanation;
import de.dfki.mycbr.retrieval.Query;


/**
 * @author myCBR Team
 *
 */
public class SMF_Class_Script extends AbstractClassSM {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(SMF_Script.class.getName());
	
	public static final ValueType VALUE_TYPE = ValueType.CLS;

	private ModelCls cls;
	
	private static final String XML_ATTR_LANGUAGE = "Language";
	private static final String XML_ATTR_SCRIPT = "Script";
	
	private String script = ""; 
	private String language = "jython"; // the scripting language
	
	private static final String DEFAULT_SCRIPT_STR_START = 
		"# Scripting system: Jython\n" +
		"# the global similarity measure has to be called 'sim'\n" +
		"\n" + 
		"def sim(q, c): \n" +
		"  result = 0.0\n";
	
	private static final String DEFAULT_SCRIPT_STR_END = 
		"  return result / %d.0\n";

	private PythonInterpreter interp;
	private PyObject sim;
	private PyDictionary namespace;

	private static final String[] PYTHON_KEYWORDS = new String[] {
		"and", "del", "from", "not", "while",    
		"as", "elif", "global", "or", "with",
		"assert", "else", "if", "pass", "yield",
		"break", "except", "import", "print",   
		"class", "exec", "in", "raise",              
		"continue", "finally", "is", "return",
		"def", "for", "lambda", "try"
	};
	private static final HashSet<String> PYTHON_KEYWORDSET = new HashSet<String> ();
	
	protected static boolean needsEscaping(String s) {
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if (ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z' 
						|| ch >= '0' && ch <= '9' || ch == '_'){
			} else {
				return true;
			}
		}
		return PYTHON_KEYWORDSET.contains(s);
	}
	
	protected static String escapeString(String s) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if (ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z' 
						|| ch >= '0' && ch <= '9' || ch == '_'){
				result.append(ch);
			} else if (ch == '\\') {
				result.append("\\\\");
			} else {
				result.append(String.format("%c", ch));
			}
		}
		return result.toString();
	}
	
	protected static String getPythonFieldAccess(String s) {
		if (!needsEscaping(s)) {
			return "." + s;
		} else {
			return String.format("[\"%s\"]", escapeString(s));
		}
	}

	protected static String getPythonGlobalAccess(String s) {
		if (!needsEscaping(s)) {
			return s;
		} else {
			return String.format("globals()[\"%s\"]", escapeString(s));
		}
	}
	
	protected void setNamespace(ModelCls cls) {
		for (int i = 0; i < PYTHON_KEYWORDS.length; i++) {
			PYTHON_KEYWORDSET.add(PYTHON_KEYWORDS[i]);
		}
		namespace = new PyDictionary();
		// declare the special values:
		SpecialValueHandler svh = SpecialValueHandler.getInstance();
		for (SpecialValue v : svh.getAllSpecialValues()) {
			namespace.__setitem__(new PyString(v.getName()), v);
		}
		// set slot functions
		ModelCls c = cls;
		while (c != null) {
			for (ModelSlot slot : cls.listSlots()) {
				namespace.__setitem__(new PyString(slot.getName()), 
						new ModelSlotScript(slot));
			}
			c = c.getSuperCls();
		}
	}
	
	private String getDefaultScript(ModelCls cls) {
		StringBuilder s = new StringBuilder(DEFAULT_SCRIPT_STR_START);
		ModelCls c = cls;
		int counter = 0;
		while (c != null) {
			for (ModelSlot slot : cls.listSlots()) {
				s.append(String.format(
						"  result += %1$s(q%2$s, c%2$s)\n", 
						getPythonGlobalAccess(slot.getName()),
						getPythonFieldAccess(slot.getName())));
				++counter;
			}
			c = c.getSuperCls();
		}
		s.append(String.format(DEFAULT_SCRIPT_STR_END, counter));
		return s.toString();
	}

	public SMF_Class_Script(ModelInstance inst, String smfName) throws Exception {
		super(inst, smfName);
		this.cls = (ModelCls) inst;
		setNamespace(cls);
		this.script = getDefaultScript(cls);
	}
	
	public SMF_Class_Script(ModelInstance inst, Element smfElement) throws JDOMException {
		super(inst, smfElement);
		cls = (ModelCls) inst;
		language = Helper.decode(smfElement.getAttributeValue(XML_ATTR_LANGUAGE));
		script = Helper.decode(smfElement.getAttributeValue(XML_ATTR_SCRIPT));
		setNamespace(cls);
	}
	
	public static String getSMFunctionTypeName_static() {
		return "Script";
	}
	
	@Override
	public boolean checkConsistency(Frame parent, boolean quiet) {
		return false;
	}

	@Override
	public AbstractSMFunction copy() {
		SMF_Class_Script newSmf = null;
		try {
			newSmf = new SMF_Class_Script(inst, smfName);
			// copy things
			newSmf.script = this.script;
			newSmf.language = this.language;
			newSmf.setHasChanged(false);
		} catch (Exception e)	{
			log.log(Level.SEVERE, "error while copying", e);
		}
		return newSmf;
	}
	
	@Override
	protected SMFPanel createSMFPanel() {
		return new SMFPanel_Class_Script(this);
	}
	
	public void startRetrieval() throws Exception {
		interp = new PythonInterpreter(namespace);
		interp.exec(script);
		sim = interp.get("sim");
		if (sim == null)
			throw new IOException("script does not contain a sim() routine");
	}
	
	public void finishRetrieval() throws Exception {
		interp.cleanup();
	}

	@Override
	public double getSimilarityBetween(Object query, Object cb, Explanation exp)
			throws Exception {
		startRetrieval();
		PyObject q = new CaseInstanceScript((Query) query);
		PyObject c = new CaseInstanceScript((CaseInstance)cb);
		PyObject res = sim.__call__(new PyObject[]{q, c});
		Object x = res.__tojava__(Double.class);
		
		if (x == Py.NoConversion) {
			throw new IOException("cannot convert Python object to a Java Double");
		}
		if (exp!=null) {
			exp.setSimilarity((Double) x);
		}
		
		finishRetrieval();
		return (Double)x;
	}
	
	@Override
	public double compareModelCls(Query query, CaseInstance caseInst, Explanation thisExp, 
			  SimMap modelToSMF, int flags) throws Exception {
		return getSimilarityBetween(query, caseInst, thisExp);
	}
	
	@Override
	public ValueType getValueType() {
		return VALUE_TYPE;
	}

	@Override
	public void toXML(Element xmlElement) {
		xmlElement.setAttribute(XML_ATTR_LANGUAGE, Helper.encode(language));
		xmlElement.setAttribute(XML_ATTR_SCRIPT, Helper.encode(script));
	}
	
	public ModelCls getCls() {
		return cls;
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
