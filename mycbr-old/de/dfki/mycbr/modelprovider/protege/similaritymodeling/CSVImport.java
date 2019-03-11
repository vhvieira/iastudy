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
package de.dfki.mycbr.modelprovider.protege.similaritymodeling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.casebase.CaseInstance;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.model.vocabulary.SpecialValueHandler;
import de.dfki.mycbr.modelprovider.ModelProvider;

/**
 * 
 * @author Daniel
 */
public class CSVImport {
	private final static Logger log = Logger.getLogger ( CSVImport.class.getName ( ) );

	private static String SEPERATOR = ";"; //$NON-NLS-1$
	private static String INTERNAL_SEPERATOR = ":"; //$NON-NLS-1$
	private static String TOKEN_UNDEFINED = "?"; //$NON-NLS-1$
	
	/**
	 * @param file File with CSV entries.
	 * @param entries The list the entries are put into
	 * @throws IOException
	 * @return String[] The Header
	 */
	public static String[] readTable(File file, ArrayList<String[]> entries) throws IOException {
		if (!file.exists()) {
			JOptionPane.showMessageDialog(null, Messages.getString("File_not_found"), Messages.getString("Error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}

		// we parse by line
		BufferedReader br = new BufferedReader(new FileReader(file));

		// first read table header
		String line = br.readLine();
		String[] header = line.split(SEPERATOR);

		// header contains duplicates?
		HashSet<String> headerVals = new HashSet<String>();
		for (int i=0; i<header.length; i++) {
			if (headerVals.contains(header[i])) {
				JOptionPane.showMessageDialog(null, Messages.getString("File_contains_dublicate_attribute_names"), Messages.getString("Error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
				return null;
			}
			headerVals.add(header[i]);
		}

		//ArrayList<String> rowList = new ArrayList<String>();
		while (line != null) {
			line = br.readLine();
			if (line != null && !line.equals("")) { //$NON-NLS-1$
				entries.add(line.split(SEPERATOR,-1));
				//rowList.add(line);
			}
		}
		br.close();
		return header;
	}

	/**
	 * @param row
	 * @param header Vector header titles (String)
	 * @return String errorMessage. null if everything went fine.
	 */
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public static ErrorMessageCSV checkImportInstance(ModelCls mainClass, Vector row, Vector header) {
		//KnowledgeBase kb = mainClass.getProject().getKnowledgeBase();

		SpecialValueHandler specialValueHandler = SpecialValueHandler.getInstance();

		// check sanity
		//boolean importable = true; // importable is not needed any longer!
		String currentAttribute = null;
		try {
			for (int i = 0; i < header.size(); i++) {
				currentAttribute = (String) header.get(i);
				ModelSlot slot = null;
				for (ModelSlot s : mainClass.listSlots()) {
					if (s.getName().equals(currentAttribute)) {
						slot = s;
						break;
					}
				}
				if (slot == null) {
					return new ErrorMessageCSV(Messages.getString("Slot__not_found_in_knowledge_base"), null); //$NON-NLS-1$
				} 
				
				// commented out before 20.10.2008
				/*else if (!mainClass.getDirectTemplateSlots().contains(slot))
				{
					return new ErrorMessageCSV("Class [ " + mainClass.getName() + " ] is not a domain of slot [ " + slot.getName() + " ]", slot.getName());
				}*/

				String v = (String) row.get(i);

				// now check value
				Collection<String> values = new ArrayList<String>();
				if (!slot.isMultiple()) {
					values.add(v);
				} else {
					String[] split = v.split(getInternalSeperator());
					for (int j=0; j<split.length; j++) {
						values.add(split[j]);
					}
				}
				for (String val : values) {
					if ("".equals(val) || TOKEN_UNDEFINED.equals(val) || specialValueHandler.isSpecialValueStr(val)) { //$NON-NLS-1$
						continue;
					}

					ValueType valueType = slot.getValueType();
					if (valueType == ValueType.SYMBOL) {
						if (!slot.getAllowedValues().contains(val)) {
							return new ErrorMessageCSV(String.format(Messages.getString("Not_allowed_value"), val), slot.getName()); //$NON-NLS-1$
						}
					} else if (valueType == ValueType.INTEGER) {
						int value = Integer.parseInt(val);
						if (slot.getMinimumValue().intValue()>value || slot.getMaximumValue().intValue()<value) {
							return new ErrorMessageCSV(String.format(Messages.getString("Not_in_range"), val, slot.getMinimumValue(), slot.getMaximumValue()), slot.getName()); //$NON-NLS-1$
						}
					}
				}
			}
		} catch (Throwable e) {
			log.log(Level.FINE, Messages.getString("Error"), e); //$NON-NLS-1$
			return new ErrorMessageCSV(e.toString(), currentAttribute);
		}
		return null;
	}

	public static void importInstance(ModelCls mainClass, Vector<String> row, Vector<String> header, String name) {

		SpecialValueHandler specialValueHandler = SpecialValueHandler.getInstance();

		try {
			CaseInstance inst = ModelProvider.getInstance().createCaseInstance(name, mainClass);

			for (int i = 0; i < header.size(); i++) {
				String slotName = (String) header.get(i);
				ModelSlot slot = null;
				for (ModelSlot s : mainClass.listSlots()) {
					if (s.getName().equals(slotName)) {
						slot = s;
						break;
					}
				}                
				String tmp = row.get(i);
				if ("".equals(tmp) || TOKEN_UNDEFINED.equals(tmp) || specialValueHandler.isSpecialValueStr(tmp)) { //$NON-NLS-1$
					tmp = null;
				}
				de.dfki.mycbr.ValueType valueType = slot.getValueType();
				if (!slot.isMultiple()) {
					Object value = valueType.newInstance(tmp);
					inst.setSlotValue(slot, value);
				} else {
					String[] split = tmp.split(getInternalSeperator());
					Collection<Object> objects = new ArrayList<Object>();
					for (int j=0; j<split.length; j++) {
						objects.add(valueType.newInstance(split[i]));
					}
					inst.setSlotValue(slot, objects);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static String getSeperator() {
		return SEPERATOR;
	}

	public static void setSeperator(String seperator) {
		SEPERATOR = seperator;
	}

	public static String getInternalSeperator() {
		return INTERNAL_SEPERATOR;
	}

	public static void setInternalSeperator(String internal_seperator) {
		INTERNAL_SEPERATOR = internal_seperator;
	}

}
