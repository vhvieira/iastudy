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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Logger;

import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.casebase.CaseInstance;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.modelprovider.ModelProvider;
import de.dfki.mycbr.modelprovider.protege.CaseInstanceProtege;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;

/**
 * @author myCBR Team
 * 
 */
public class InstanceGenerator {
	
	private final static Logger log = Logger.getLogger(InstanceGenerator.class.getName());

	private static Random rand = new Random();
		
	private static String generateRandomString() {
        long r1 = rand.nextLong();
        long r2 = rand.nextLong();
        return Long.toHexString(r1) + Long.toHexString(r2);
	}
	
	@SuppressWarnings("unchecked")
	private static boolean shouldFill(ArrayList probabilities, int index) {
		int prob = ((Integer)probabilities.get(index)).intValue();
		return (prob >= rand.nextInt(100)+1);
	}
	
	public static ModelSlot slotToModelSlot(Slot slot) {
		return (ModelSlot) 
			ModelProvider.getInstance().getModelInstance(slot.getName());
	}
	
	@SuppressWarnings("unchecked")
	public static Object generateSlotValue(ModelSlot slot) {
		ValueType valueType = slot.getValueType();
		if (valueType == ValueType.INTEGER) {		
			long min = slot.getMinimumValue().intValue();
			long max = slot.getMaximumValue().intValue();
			return new Long(Math.abs(rand.nextLong()%(max-min+1)) + min).intValue();					
		} else if (valueType == ValueType.FLOAT) {
			double min = slot.getMinimumValue().floatValue();
			double max = slot.getMaximumValue().floatValue();
			return new Double(rand.nextFloat() * (max-min) + min).floatValue();
		} else if (valueType == ValueType.STRING) {
			return generateRandomString();
		} else if (valueType == ValueType.INSTANCE) {
			if (!slot.getAllowedValues().isEmpty()) {
				//TODO preliminary solution
				// should be optional. May not always be desired. But I need it now...
				
				// return reference to existing Case Instance
				ModelCls subCls = (ModelCls) slot.getAllowedValues().iterator().next();
				Vector<CaseInstance> cases = new Vector<CaseInstance>(subCls.getDirectCaseInstances());
				int index = Math.abs(rand.nextInt()%cases.size());
				return ((CaseInstanceProtege) cases.get(index)).getCaseInstanceProtege();
			}
			
		} else if (valueType == ValueType.SYMBOL) {
			Collection symbols = slot.getAllowedValues();
			return symbols.toArray()[rand.nextInt(symbols.size())];
				// don't blame me; it is no list, but a collection, hence it has to
				// be converted to an array first (we need random access)
		} else if (valueType.equals(ValueType.BOOLEAN.toString())) {
			return new Boolean(rand.nextInt(2) == 0);
		} 
		log.info("ignored slot type " + valueType.toString() 
					+ " " + slot.getName() );
		return null;
	}

	@SuppressWarnings("unchecked")
	private static void generateOneInstanceAux(Instance inst, Cls cls, 
											   ArrayList probabilities) {
		Collection slots = cls.getTemplateSlots(); 
		int j = 0;
		for (Iterator i = slots.iterator(); i.hasNext(); ) {
			Slot slot = (Slot) i.next();
			Object val = generateSlotValue(slotToModelSlot(slot));
			if (val != null) {
				if (shouldFill(probabilities, j)) {
					if (slot.getAllowsMultipleValues()) {
						//TODO preliminary solution...
						int amount = 1 + Math.abs(rand.nextInt() % 3);
						for (int k = 0; k < amount; k++) {
							inst.addOwnSlotValue(slot, generateSlotValue(slotToModelSlot(slot)));
						}
					} else {
						inst.setOwnSlotValue(slot, val);
					}
				}
			}
			j++;
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void generateOneInstance(Cls cls, ArrayList probabilities) {
		KnowledgeBase kb = cls.getKnowledgeBase();
		String name = cls.getName() + "_" + (cls.getDirectInstanceCount()+1);
		generateOneInstanceAux(kb.createInstance(name, cls), 
				cls, probabilities);
	}
	
	@SuppressWarnings("unchecked")
	public static void generate(int amount, Cls cls, ArrayList probabilities)  {
		log.info("generating " + amount + " instances");
		for (int i = 0; i < amount; i++) {
			generateOneInstance(cls, probabilities);
		}
	}
}
