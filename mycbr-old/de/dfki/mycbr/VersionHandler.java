/*
 * myCBR License 1.1
 *
 * Copyright (c) 2008
 * Thomas Roth-Berghofer, Armin Stahl & Deutsches Forschungszentrum f&uuml;r K&uuml;nstliche Intelligenz DFKI GmbH
 * Further contributors: myCBR Team (see http://mycbr-project.net/contact.html for further information 
 * about the myCBR Team). 
 * All rights reserved.
 *
 * myCBR is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Since myCBR uses some modules, you should be aware of their licenses for
 * which you should have received a copy along with this program, too.
 * 
 * endOfLic */
package de.dfki.mycbr;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;

import de.dfki.mycbr.model.similaritymeasures.IMF_Standard;
import de.dfki.mycbr.model.similaritymeasures.SMFunctionFactory;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Class_Standard;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Number_Advanced;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Number_Std;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Symbol_Table;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.SpecialValueHandler;

/**
 * Handler for different version of myCBR projects. If one opens an old project with a newer version of myCBR,
 * the file format is tried to be updated to fit the constraints of the new myCBR version.
 * 
 * @author myCBR Team
 *
 */
public class VersionHandler {
	
	private final static Logger log = Logger.getLogger ( VersionHandler.class.getName ( ) );
	private StringBuffer comments = new StringBuffer();
	
	/**
	 * This comparator can compare two version numbers by stepping through the columns of
	 * the version numbers, casting them to int and returning the result.
	 * The version is expected to be of the form x.y or x.y.z with x,y,z integer values.
	 * 
	 * @throws VersionNumberException if version number format is unknown
	 */
	private static Comparator<String> versionComparator = new Comparator<String>() {
		public int compare(String version1, String version2) {
			int result = 0;
			String[] splitted1 = version1.split("\\.");
			String[] splitted2 = version2.split("\\.");
			
			for(int i=0; i<Math.min(splitted1.length, splitted2.length); ++i) {
				Integer v1part = 0;
				Integer v2part = 0;
				try {
					v1part = Integer.parseInt(splitted1[i]);
					v2part = Integer.parseInt(splitted2[i]);
				} catch(NumberFormatException e) {
					throw new VersionNumberException("Version number format is unknown, can't compare version numbers.", e);
				}
				result = v1part.compareTo(v2part);
				if(result != 0) {
					return result;
				}
					
			
			}
			
			return splitted1.length - splitted2.length; // if first number has less columns it's smaller
		}
	};
	
	
	/**
	 * Perform all desired changes upon the XML documents.
	 * @param versionSMF the version of the project's SMF file.
	 * @param doc XML document of the project's SMF file.  
	 * @return true if doc could be adapted successfully, false otherwise
	 */
	@SuppressWarnings("unchecked")
	public boolean transferSMFdata(String versionSMF, Document doc) {
		log.info("MyCBR: XML document for SMF update from version [" + (versionSMF == null ? "0.?" : versionSMF) + "] to [" + CBRProject.myCBR_VERSION + "]");
		comments.append("\n-= XML document for SMF update from version [" + (versionSMF == null ? "0.?" : versionSMF) + "] to [" + CBRProject.myCBR_VERSION + "] =-\n");
		
		boolean success = true;
		if (versionSMF == null) {
			//
			// SpecialValueHandler
			//
			log("change serialization of SpecialValues in SpecialValueHandler: <<_UNDEFINED_>> to _undefined_", "so it doesn't draw too much attention in the GUI.");
			Element elementSVH = doc.getRootElement().getChild(SpecialValueHandler.XML_TAG_SPECIAL_VALUE_HANDLER);
			if (elementSVH != null) {
				HashMap<String, String> replaceMap = new HashMap<String, String>();
				replaceMap.put("UNDEFINED", 	SpecialValueHandler.SPECIAL_VALUE_UNDEFINED.getName());
//				replaceMap.put("UNKNOWN", 		SpecialValueHandler.SPECIAL_VALUE_UNKNOWN.getName());
				replaceMap.put("UNKNOWN", 		"unknown");
				
				for (Iterator it = elementSVH.getDescendants(new ElementFilter()); it.hasNext();) {
					Element element = (Element) it.next();
					if (element.getAttribute(SpecialValueHandler.XML_ATT_LABEL) != null) {
						String specialValue = Helper.decode(element.getAttributeValue(SpecialValueHandler.XML_ATT_LABEL));
						if (replaceMap.containsKey(specialValue)) {
							element.setAttribute(SpecialValueHandler.XML_ATT_LABEL, Helper.encode((String) replaceMap.get(specialValue)));
						}
					}
					if (element.getAttribute(SMF_Symbol_Table.XML_ATT_SYMBOL) != null) {
						String specialValue = Helper.decode(element.getAttributeValue(SMF_Symbol_Table.XML_ATT_SYMBOL));
						if (replaceMap.containsKey(specialValue)) {
							element.setAttribute(SMF_Symbol_Table.XML_ATT_SYMBOL, Helper.encode((String) replaceMap.get(specialValue)));
						}
					}
					
				}
			}
			
//			printElement_forDebugging(elementSVH);			
			
			//
			// Inheritance Measure (IMF)
			//
			log("remove all IMF -> init new ones in doUpdateWork()", "the very first IMFs are out-dated."); 
			success &= doc.getRootElement().removeChildren(XMLConstants.XML_TAG_INHERITANCEMEASURE);
			if (success) {
				versionSMF = "2.0";
			} else {
				log("unable to remove old Inheritance Measures from XML document");
			}
		}
		try {
			if (versionComparator.compare("2.1", versionSMF) > 0) { // if versionSMF is older than 2.1
				log("numeric SMFs: since v2.1 we calculate c-q instead of q-c. The sign of all sampling points is changed to keep the original semantic.");
				// numeric SMFs: 
				// since v2.1 we calculate c-q instead of q-c ;)
				// so, change sign of all sampling points.
				for (Iterator it = doc.getDescendants(new ElementFilter(SMF_Number_Advanced.XML_TAG_SAMPLINGPOINT)); it.hasNext();) {
					Element element = (Element) it.next();
					try {
						double xValue = element.getAttribute(SMF_Number_Advanced.XML_ATT_XVALUE).getDoubleValue();
						if (xValue!=0d) xValue = -xValue;
						element.setAttribute(SMF_Number_Advanced.XML_ATT_XVALUE, Double.toString(xValue));
					} catch (DataConversionException e) {
						success = false;
						e.printStackTrace();
					}
				}
				for (Iterator it = doc.getDescendants(new ElementFilter()); it.hasNext();) {
					Element element = (Element) it.next();
					if (SMF_Number_Std.XML_TAG_RIGHT_SIDE.equals(element.getName())) {
						element.setName(SMF_Number_Std.XML_TAG_LEFT_SIDE);
					}
					else if (SMF_Number_Std.XML_TAG_LEFT_SIDE.equals(element.getName())) {
						element.setName(SMF_Number_Std.XML_TAG_RIGHT_SIDE);
					} 
				}
				if (success) {
					versionSMF = "2.1";
				}
			}
			
			if (versionComparator.compare("2.2", versionSMF) > 0) { // if versionSMF is older than 2.2
				log("A new class similarity measure is available since v2.2: SMF_Class_Script");
				for (Iterator it = doc.getDescendants(new ElementFilter(XMLConstants.XML_TAG_SMFUNCTION)); it.hasNext();) {
					Element element = (Element) it.next();
					String type = Helper.decode(element.getAttributeValue(XMLConstants.XML_ATT_TYPE));
					if (!type.equals(SMF_Class_Standard.VALUE_TYPE.toString())) continue;
					
					element.setAttribute(XMLConstants.XML_ATT_SIMMODE, Helper.encode(SMF_Class_Standard.getSMFunctionTypeName_static()));
				}
				if (success) {
					versionSMF = CBRProject.myCBR_VERSION; // converted old SMFs up to current version (as of 2.6.1)
				}
			}
		} catch (VersionNumberException e) {
			e.printStackTrace();
			log(e.getMessage());
			success = false;
		}
			
		if(!success)
		log("Something went wrong during transferSMFdata()");
		
		return success;
	}

	/**
	 * Perform all desired changes upon the XML.
	 * @param versionCB the version of the project's casebase file.
	 * @param doc XML document of the project's casebase file.  
	 * @return true if doc could be adapted successfully, false otherwise
	 */
	@SuppressWarnings("unchecked")
	public boolean transferCasebaseData(String versionCB, Document doc) {
		log.info("MyCBR: XML document for case base update from version [" + (versionCB == null ? "0.?" : versionCB) + "] to [" + CBRProject.myCBR_VERSION + "]");
		comments.append("\n-= XML document for case base update from version [" + (versionCB == null ? "0.?" : versionCB) + "] to [" + CBRProject.myCBR_VERSION + "] =-\n");
		
		boolean success = true;
		if (versionCB == null) {
			//
			// change special values
			//
			log("change serialization of SpecialValues: <<_UNDEFINED_>> to _undefined_", "so it doesn't draw too much attention in the GUI.");

			HashMap<String, String> replaceMap = new HashMap<String, String>();
			replaceMap.put("<<_UNDEFINED_>>", 	SpecialValueHandler.SPECIAL_VALUE_UNDEFINED.toString());
			replaceMap.put("_UNDEFINED_", 		SpecialValueHandler.SPECIAL_VALUE_UNDEFINED.toString());
			
			for (Iterator it = doc.getDescendants(new ElementFilter(XMLConstants.XML_TAG_SLOTVALUE)); it.hasNext();) {
				Element element = (Element) it.next();
				String value = Helper.decode(element.getAttributeValue(XMLConstants.XML_ATT_VALUE));
				
				if (replaceMap.containsKey(value)) {
					element.setAttribute(XMLConstants.XML_ATT_VALUE, Helper.encode((String) replaceMap.get(value)));
					continue;
				}
				
			}
				
			if (success) {
				versionCB = CBRProject.myCBR_VERSION; // converted old DB up to current version (as of 2.6.1)
			} else {
				log("unable to remove old Inheritance Measures from XML document");
			}
		}
		
		return success;
	}

	/**
	 * Perform all desired changes upon the XML.
	 * 
	 * @param versionPO the version of the project's project options file.
	 * @param doc XML document of the project's project options file.  
	 * @return true if doc could be adapted successfully, false otherwise.
	 */ 
	public boolean transferProjectOptionsData(String versionPO, Document doc) {
		log.info("MyCBR: XML document for Project Options update from version [" + (versionPO == null ? "0.?" : versionPO) + "] to [" + CBRProject.myCBR_VERSION + "]");
		comments.append("\n-= XML document for Project Options update from version [" + (versionPO == null ? "0.?" : versionPO) + "] to [" + CBRProject.myCBR_VERSION + "] =-\n");

		return true;
	}
	
	
	/**
	 * Perform all the changes that have to be done AFTER the cbr project has been loaded.
	 * NOTE: 
	 * 	The distinction between different versions of project files was estimated to be unimportant.
	 * 	The version string that is passed here, is the lowest that was found in the files 
	 * 	(currently found by < order upon strings).
	 * @param version String the version of the project.
	 * @return true if the project could be adapted successfully.
	 */
	public boolean doUpdateWork(String version) {
		log.info("MyCBR: [" + CBRProject.getInstance().getProjectName() + "] update from version [" + (version==null? "0.?" : version) + "] to [" + CBRProject.myCBR_VERSION + "]");
		comments.append("\n-= Update MyCBR project (after initialization) from version [" + (version == null ? "0.?" : version) + "] to [" + CBRProject.myCBR_VERSION + "] =-\n");

		boolean success = true;
		
		if (version == null) {
			log("init Inheritance Measures (IMF) for all ModelCls", "in the earliest versions, object oriented domains were ignored.");
			for (Iterator< ModelCls > it = CBRProject.getInstance().getAllModelCls().iterator(); it.hasNext();) {
				ModelCls cls = it.next();
				
				ModelCls topCls = Helper.findHighestModelClsByInheritance(cls);
				IMF_Standard imf = CBRProject.getInstance().getIMFforModelCls(topCls.getName());
				CBRProject.getInstance().modelClsToImf.put(cls.getName(), imf);
				if (imf!=null) continue;
				
				log.fine("Init IMF for " + topCls);
				imf = SMFunctionFactory.createNewIMFunction(topCls);
				CBRProject.getInstance().modelClsToImf.put(cls.getName(), imf);
			}
			version = CBRProject.myCBR_VERSION; // converted old CBRProject up to current version (as of 2.6.1)
		}
		
		if (!success) {
			log("Something went wrong during doUpdateWork");
		}
		return success;
	}

	/**
	 * Getter for the comments made during updating the old version
	 *
	 * @return comments as string object
	 */
	public String getComments() {
		return comments.toString();
	}
	
	/**
	 * Adding a comment to be logged. It will be of the form [action] action [reason] reason 
	 * @param action the action that should be logged
	 * @param reason the reason that should be logged
	 */
	private void log(String action, String reason) {
		String comment ="[action] " + action + "\n[reason] " + reason + "\n"; 
		comments.append(comment + "\n");
		log.fine(comment);
	}
	
	/**
	 * Adding a warning to the comments
	 * @param warning the waring to be logged and added to the comments
	 */
	private void log(String warning) {
		String comment ="[warning] "+warning; 
		comments.append(comment+"\n");
		log.fine(comment);
	}

}
