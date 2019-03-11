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
package de.dfki.mycbr.model.similaritymeasures.smftypes.transformers;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Number_Advanced;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Number_Std;

/**
 * 
 * @author myCBR Team
 */
public class SMFTransformer {
	private final static Logger log = Logger.getLogger ( SMFTransformer.class.getName ( ) );

	private static SMFTransformer_Symbol 	smftrans_symbol = new SMFTransformer_Symbol();
	private static SMFTransformer_Number 	smftrans_number = new SMFTransformer_Number();
	
	/**
	 * This will be called if you change the type of your smfunction in the combo box.
	 * e.g. switch from 'standard' to 'advanced'.
	 * This will also be called automatically whenever the value type of a slot changes.
	 * 
	 * @param smfOrig EditorSMFunction original smfunction
	 * @param newValueType ValueType the new ValueType of the modelInstance the smf belongs to.
	 * @param newSmfType String the smfType of the new function.
	 * @return EditorSMFunction the new transformated smf or the original smf if no transformations could be done.
	 */
	public static AbstractSMFunction transform(AbstractSMFunction smfOrig, ValueType newValueType, String newSmfType) {
		
		if (smfOrig==null || newSmfType==null) return null;
		if (smfOrig.getSMFunctionTypeName().equals(newSmfType) && smfOrig.getValueType()==newValueType) return smfOrig;
		if(smfOrig.getValueType() == ValueType.CLS) return smfOrig;
		try {			
			if (smfOrig.getValueType()!=newValueType) {
				if (!isTransformable(smfOrig.getValueType(), newValueType)) {
					return smfOrig;
				}
				
				// commented out before 20.10.2008
//				if (smfOrig.getSMFunctionTypeName().equals("Container"))
//				{
//					log.fine("smfunction is a container");
//					try
//					{
//						SMFContainer newSmfContainer = new SMFContainer(smfOrig.getModelInstance(), smfOrig.getSmfName(), SMFunctionFactory.getSMFClassForInstance(smfOrig.getModelInstance()).split(","));
//						
//						AbstractSMFunction newInternalSMF = transform_noContainers(((SMFContainer)smfOrig).getCurrentSmf(), newValueType);
//						newSmfContainer.setCurrentSMF(newInternalSMF);
//						log.fine("type of transformed container smf is ["+newSmfContainer.getValueType()+"]");
//						return newSmfContainer;
//						
//					} catch (Exception e)
//					{
//						log.log(Level.SEVERE, "cannot transform ["+smfOrig.getValueType()+"] to ["+newValueType+"] for ["+smfOrig.getModelInstanceName()+"]");
//						e.printStackTrace();
//					}
//				}
//				else 
				return transform_noContainers(smfOrig, newValueType);
			}
			
			
			if (!smfOrig.getSMFunctionTypeName().equals(newSmfType)) {
				log.fine("transform [" + smfOrig.getSMFunctionTypeName() + "] to [" + newSmfType + "]");
				if (smfOrig.getValueType() == ValueType.SYMBOL) {
					return smftrans_symbol.transform(smfOrig, newSmfType);
				} else if (smfOrig.getValueType() == ValueType.INTEGER || smfOrig.getValueType() == ValueType.FLOAT) {
					return smftrans_number.transform(smfOrig, newSmfType);
				}
			}
			
		} catch (Throwable e) {
			log.log(Level.FINE, "could not transform  ["+smfOrig.getSMFunctionTypeName()+"] to ["+newSmfType+"]", e);
		}
		return null;
	}
	
	public static boolean isTransformable(ValueType type, ValueType newValueType) {
		if (newValueType == null) {
			return false;
		}
		
		if (type == ValueType.INTEGER && newValueType==ValueType.FLOAT) {
			return true;
		}
		if (type == ValueType.FLOAT && newValueType==ValueType.INTEGER) {
			return true;
		}
		
		return false;
	}

	private static AbstractSMFunction transform_noContainers(AbstractSMFunction smfOrig, ValueType newValueType) {
		log.fine("transform [" + smfOrig.getValueType() + "] to [" + newValueType + "] for [" + smfOrig.getModelInstanceName() + "]. TRANSFORM BY [" + smfOrig.getSMFunctionTypeName() + "]");
		if (smfOrig.getSMFunctionTypeName().equals("Standard")) {
			((SMF_Number_Std) smfOrig).changeValueType(newValueType);
			return smfOrig;
		} else if (smfOrig.getSMFunctionTypeName().equals("Advanced")) {
			((SMF_Number_Advanced) smfOrig).changeValueType(newValueType);
			return smfOrig;
		}
		log.fine("no transformation happened");
		return null;
	}
	
}
