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
package de.dfki.mycbr.model.similaritymeasures;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.logging.Logger;

import org.jdom.Element;

import de.dfki.mycbr.CBRProject;
import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.XMLConstants;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Boolean;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Class_Script;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Class_Standard;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_External;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Number_Advanced;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Number_Std;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Script;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_String_Character;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_String_Standard;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_String_Word;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Symbol_Ordered;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Symbol_Table;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Symbol_Taxonomy;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.model.vocabulary.ModelSlot;

/**
 * @author myCBR Team
 *
 * NOTE: Register your new SMFunction classes here!
 * 
 * creates new EditorSMFunction objects. 
 * In here, all ValueType -> SMFunction mappings are registered.
 */
public class SMFunctionFactory implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final static Logger log = Logger.getLogger ( SMFunctionFactory.class.getName ( ) );

	/**
	 * Used to check for registered SMFunction plugins.
	 * To offer more SMFunction types for one Instance type
	 * you can use a comma separated String containing the class names of several EditorSMFunction. 
	 * @param inst Instance (either Slot or Cls, usually)
	 * @return class name for smfunction type of inst 
	 */
	public static String[] getSMFClassesForInstance(ModelInstance inst) {
		String className = null;

		if (inst instanceof ModelSlot) {
			ModelSlot slot = (ModelSlot) inst;
			
			// commented out before 20.10.2008
//			if (ValueType.SYMBOL.toString().equals(slot.getValueType())) className=SMF_Symbol_Standard.class.getName()+","+SMF_Symbol_Taxonomy.class.getName();
//			else if (ValueType.INTEGER.toString().equals(slot.getValueType())) className=SMF_Integer_Standard.class.getName()+","+SMF_Integer_Advanced.class.getName();
//			else if (ValueType.FLOAT.toString().equals(slot.getValueType())) className=SMF_Float_Standard.class.getName()+","+SMF_Float_Advanced.class.getName();
//			else if (ValueType.STRING.toString().equals(slot.getValueType())) className=SMF_String_Standard.class.getName();


			ValueType vt = slot.getValueType();
			if (ValueType.SYMBOL == vt) {
				className=SMF_Symbol_Table.class.getName() + 
				      "," + SMF_Symbol_Taxonomy.class.getName() + 
				      "," + SMF_Symbol_Ordered.class.getName() +
				      "," + SMF_External.class.getName() +
				      "," + SMF_Script.class.getName();
			} else if (ValueType.INTEGER == vt) { 
				className=SMF_Number_Std.class.getName() +
				   		"," + SMF_Number_Advanced.class.getName() +
				   		"," + SMF_External.class.getName() +
					      "," + SMF_Script.class.getName();
			} else if (ValueType.FLOAT == vt) { 
				className=SMF_Number_Std.class.getName()+
				  ","+SMF_Number_Advanced.class.getName() +
				  ","+SMF_External.class.getName() +
			      "," + SMF_Script.class.getName();
			} else if (ValueType.STRING == vt){ 
				className=SMF_String_Standard.class.getName() +
					"," + SMF_String_Character.class.getName() +
					"," + SMF_String_Word.class.getName() +
				  "," + SMF_External.class.getName() +
			      "," + SMF_Script.class.getName();
			} else if (ValueType.BOOLEAN == vt) {
				className=SMF_Boolean.class.getName();
			}
		}
		else if (inst instanceof ModelCls) {
			
			// commented out before 20.10.2008
//			Cls cls = (Cls) inst;
//			className = SMF_Class_Standard.class.getName();// +	"," + SMF_Class_Script.class.getName();
			
			className = SMF_Class_Standard.class.getName() 
					+	"," + SMF_Class_Script.class.getName();
		}

		if (className == null) return null;
		return className.split(",");
	}
	
	/**
	 * returns a new EditorSMFunction object for the given instance.
	 * @throws Exception 
	 */
	public static AbstractSMFunction createNewSMFunction (ModelInstance inst, String smfName) throws Exception {
		AbstractSMFunction smf = null;
		
		String[] classNames = getSMFClassesForInstance(inst);
		if (classNames == null) {
			log.severe("No smfunction type defined for model instance ["+inst.getName()+"]");
			throw new Exception("No smfunction type defined for model instance ["+inst.getName()+"]");
		}

		smf = createSMFunction(classNames[0], inst, smfName);
		
		// commented out before 20.10.2008
//			if (classNames.length==1)
//			{
//				// only one SMFunction type available
//				smf = createSMFunction(classNames[0], inst, smfName);
//			}
//			else
//			{
//				// several SMFunction types available
//				smf = new SMFContainer(inst, smfName, classNames);
//			}
		
		//logging
		String instname = null;
		String smfname = null;
		if (inst != null) {
			instname = inst.getName();
		}
		if (smf != null) {
			smfname = smf.getSmfName();
		}
		log.fine("created new smfunction (smfname= ["+smfname+"]) for ["+instname+"]. (smf==null)=="+(smf==null));

		return smf;
	}

	public static AbstractSMFunction loadSMFunction(Element smfElement, ModelInstance inst) throws Exception {
		AbstractSMFunction smf = null;
		String[] classNames = getSMFClassesForInstance(inst);
		if (classNames == null) {
			log.severe("No smfunction type defined for instance ["+(inst==null?null:inst.getName())+"]");
			throw new Exception("No smfunction type defined for instance ["+(inst==null?null:inst.getName())+"]");
		}

			if (classNames.length == 1) {
				log.fine("only one SMFunction type available ["+(inst==null?null:inst.getName())+":"+classNames[0]+"]");
				smf = loadSMFunction(inst, classNames[0], smfElement);
			} else {
				log.fine("more than one SMFunction type available ["+(inst==null?null:inst.getName())+":"+classNames+"]");
				
				// several SMFunction types available
				String currentSmfType = Helper.decode(smfElement.getAttributeValue(XMLConstants.XML_ATT_SIMMODE));
				HashMap<String, String> functions = createFunctionsMap(classNames);

				smf = loadSMFunction(inst, functions.get(currentSmfType), smfElement);
			}
		
		//logging
		String instname = null;
		String smfname = null;
		if (inst != null) {
			instname = inst.getName(); 
		} else {
			log.severe("instance is null!");
		}
		if (smf != null) {
			smfname = smf.getSmfName(); 
		} else {
			log.severe("smfunction for ["+instname+"] is null!");
		}
		log.fine("successfully loaded smfunction (smfname= ["+smfname+"]) for ["+instname+"]. (smf==null)=="+(smf==null));

		return smf;
	}
	
	@SuppressWarnings("unchecked")
	protected static AbstractSMFunction loadSMFunction(ModelInstance inst, String className, Element smfElement) throws Exception {
		log.fine("loading SMF ["+className+"] by introspection for class/slot ["+inst+"]");
		try {
		Constructor cnstr = CBRProject.class.getClassLoader().loadClass(className).getDeclaredConstructor(new Class[] { ModelInstance.class, Element.class });
		return (AbstractSMFunction)cnstr.newInstance(new Object[]{inst, smfElement});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static AbstractSMFunction createSMFunction(String className, ModelInstance inst, String smfName) throws Exception {
		log.fine("creating SMF ["+className+"] by introspection for class/slot ["+inst+"]");
		Constructor cnstr = CBRProject.class.getClassLoader().loadClass(className).getDeclaredConstructor(new Class[] { ModelInstance.class, String.class });
		return (AbstractSMFunction)cnstr.newInstance(new Object[]{inst, smfName});
	}

	public static MMF_Standard createNewMMFunction(ModelInstance modelInstance, String newSmfName) {
		MMF_Standard newMMF = new MMF_Standard(modelInstance, newSmfName);
		return newMMF;
	}

	public static AbstractSMFunction loadMMFunction(Element smfElement, ModelInstance inst) throws Exception {
		MMF_Standard mmf = new MMF_Standard(inst, smfElement);
		return mmf;
	}

	public static IMF_Standard createNewIMFunction(ModelCls cls) {
		IMF_Standard newIMF = new IMF_Standard(cls);
		return newIMF;
	}
	
	public static IMF_Standard loadIMFunction(Element smfElement, ModelInstance inst) throws Exception {
		IMF_Standard imf = new IMF_Standard(inst, smfElement);
		return imf;
	}
	
	public static HashMap<String,String> createFunctionsMap(String[] classNames) throws Exception {
		HashMap<String, String> functions = new HashMap<String,String>();
		for (int i=classNames.length-1; i>=0; i--) {
			String className = classNames[i];
			// unfortunately, we need an instance of this class to get its smfunctiontype,
			// because getSMFunctionType cannot be static, because its abstract in super class. btw: why??
			String type = (String) CBRProject.class.getClassLoader().loadClass(className).getDeclaredMethod("getSMFunctionTypeName_static").invoke(null);
			if (AbstractSMFunction.DEFAULT_SMF_NAME.equals(type)) {
				log.severe("Could not load ["+className+"]. SMFunction type name not set! Every SMF class needs a label. Please override the method 'getSMFFunctionTypeName()' in class ["+className+"]");
				continue;
			}
			functions.put(type, className);
		}
		return functions;
	}

}

