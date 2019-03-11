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
package de.dfki.mycbr.modelprovider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.dfki.mycbr.CaseDataRaw;
import de.dfki.mycbr.ClsDataRaw;
import de.dfki.mycbr.SlotDataRaw;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.casebase.CaseInstance;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.model.vocabulary.SpecialValueHandler;
import de.dfki.mycbr.modelprovider.standalone.CaseInstanceStandalone;
import de.dfki.mycbr.modelprovider.standalone.ModelClsStandalone;
import de.dfki.mycbr.modelprovider.standalone.ModelSlotStandalone;

/**
 * 
 * @author myCBR Team
 */
public class ModelProviderStandalone extends ModelProvider {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(ModelProviderStandalone.class.getName());

	
	/** maps cls names (String) to ModelCls objects */
	private HashMap<String, ModelCls> allModelCls   = new HashMap<String, ModelCls>();
	
	/** maps slot names (String) to ModelSlot objects */
	private HashMap<String, ModelSlot> allModelSlots = new HashMap<String, ModelSlot>();
	
	/** maps case names (String) to CaseInstance objects */
	private HashMap<String, CaseInstance> allCaseInstances = new HashMap<String, CaseInstance>();
	
	/** maps classes (ModelCls) to a collection of CaseInstances */
	private HashMap<ModelCls, Collection<CaseInstance>> clsToItsCasInstances = new HashMap<ModelCls, Collection<CaseInstance>>();

	public ModelProviderStandalone() {}
	
	@SuppressWarnings("unchecked")
	public void init(Collection rawData) {
		log.info("initializing STANDALONE MODEL PROVIDER.");
		
		// process raw data
		if (rawData==null || rawData.size()==0) {
			// no data available. There is no project to run by itself!
			log.log(Level.SEVERE, "no data available. There is no project to run by itself!");
			return;
		}
		
		log.fine("size of raw data = [" + rawData.size() + "]");
		for (Iterator clsIt=rawData.iterator(); clsIt.hasNext();) {
			ClsDataRaw clsDataRaw = (ClsDataRaw) clsIt.next();
			String clsName = clsDataRaw.getClsName();
			log.fine("building model cls [" + clsName + "]");
			
			HashMap slotsAndValueTypes = clsDataRaw.getSlotsAndValueTypes();
			
			// collect all slots that belong to this cls
			Collection slots = new ArrayList();
			
			//
			// first create Slots
			//
			for (Iterator slotIt=slotsAndValueTypes.entrySet().iterator(); slotIt.hasNext();) {
				Entry entry = (Entry) slotIt.next();
				
				String slotName = (String) entry.getKey();
				if (!allModelSlots.containsKey(slotName)) {
					// create ModelSlot
					log.fine("building model slot [" + slotName + "]");
					SlotDataRaw slotDataRaw = (SlotDataRaw)entry.getValue();
					
					ModelSlot slot = new ModelSlotStandalone(slotDataRaw, this);
					allModelSlots.put(slotName, slot);
					slots.add(slot);
				} else {
					slots.add(allModelSlots.get(slotName));
				}
			}
			
			//
			// now create Cls
			//
			ModelCls modelCls = new ModelClsStandalone(clsName, slots, this);
			allModelCls.put(clsName, modelCls);
			// all case instances of type modelCls

			//
			// then process case instances 
			// 
			for (Iterator caseIt=clsDataRaw.getCaseInstances().iterator(); caseIt.hasNext();) {
				CaseDataRaw caseRaw = (CaseDataRaw) caseIt.next();
				
				if (!createNewCaseInstance(modelCls, caseRaw)) {
					log.warning("could not instantiate case [" + caseRaw.getCaseName() + "]");
				}
				
			}

		}
		
		//
		// after buid-up of the models shape, we have to update 
		// super cls references 
		// and subcomponents of caseInstances
		//
		for (Iterator rawIt=rawData.iterator(); rawIt.hasNext();) {
			ClsDataRaw clsDataRaw = (ClsDataRaw) rawIt.next();
			String clsName = clsDataRaw.getClsName();
			
			ModelClsStandalone cls = (ModelClsStandalone) allModelCls.get(clsName);
			cls.setSuperCls((ModelCls)allModelCls.get(clsDataRaw.getSuperClsName()));
		}
		
		for (Iterator clsIt=allModelCls.values().iterator(); clsIt.hasNext();) {
			ModelClsStandalone cls = (ModelClsStandalone) clsIt.next();
			for (Iterator slotIt=cls.listSlots().iterator(); slotIt.hasNext();) {
				ModelSlotStandalone slot = (ModelSlotStandalone) slotIt.next();
				if (slot.getValueType() == ValueType.INSTANCE) {
					// check instances and setup real references
					for (Iterator ciIt=cls.getDirectCaseInstances().iterator(); ciIt.hasNext();) {
						CaseInstanceStandalone ci = (CaseInstanceStandalone) ciIt.next();
						Object val = ci.getSlotValue(slot);
						
						if (SpecialValueHandler.getInstance().isSpecialValue(val)) {
							continue;
						}
						
						String subCiName = (String) val;
						CaseInstanceStandalone subCi = (CaseInstanceStandalone) getCaseInstance(subCiName);
						ci.setSlotValue(slot, subCi);
					}
					
					ArrayList al = new ArrayList();
					for (Iterator valIt=slot.getAllowedValues().iterator(); valIt.hasNext();) {
//						String clsName = (String) valIt.next();
						Object clsName = valIt.next();

						ModelClsStandalone subCls = (ModelClsStandalone) getModelInstance(clsName.toString());
						al.add(subCls);
					}
					slot.setAllowedValues(al);
				}
				
			}
			
		}
		log.info("init of STANDALONE MODEL PROVIDER completed.");
	}

	@SuppressWarnings("unchecked")
	private boolean createNewCaseInstance(ModelCls modelCls, CaseDataRaw caseRaw) {
		String caseName = caseRaw.getCaseName();

		Collection caseInstancesOfCls = getAllCaseInstancesForCls(modelCls);
		
		HashMap caseValues = new HashMap();
		for (Iterator slotIt=modelCls.listSlots().iterator(); slotIt.hasNext();) {
			ModelSlot slot = (ModelSlot) slotIt.next();
			Object value = caseRaw.getSlotValue(slot.getName());
			caseValues.put(slot, value);
		}
		
		CaseInstanceStandalone caseInst = new CaseInstanceStandalone(caseName, modelCls ,caseValues);
		allCaseInstances.put(caseName, caseInst);
		caseInstancesOfCls.add(caseInst);
		
		return true;
	}

	public ModelInstance getModelInstance(String name) {
		ModelInstance inst = (ModelInstance) allModelCls.get(name);
		if (inst == null) {
			inst = (ModelInstance) allModelSlots.get(name);
		}
		if (inst==null) {
			// oh.. probably fatal.
			log.info("could not find model instance with name [" + name + "]");
//			show();
		}
		return inst;
	}

	public CaseInstance getCaseInstance(String name) {
		return (CaseInstance)allCaseInstances.get(name);
	}

	@SuppressWarnings("unchecked")
	public Collection getDirectCaseInstancesFor(ModelCls cls) {
		return getAllCaseInstancesForCls(cls);
	}


	@SuppressWarnings("unchecked")
	public CaseInstance createCaseInstance(String name, ModelCls cls) {
		if (name == null) {
			name = "ID_" + allCaseInstances.size()+1;
		}
		Collection caseInstancesOfCls = getAllCaseInstancesForCls(cls);
		CaseInstanceStandalone caseInst = new CaseInstanceStandalone(name, cls, new HashMap());
		allCaseInstances.put(name, caseInst);
		caseInstancesOfCls.add(caseInst);
		return caseInst;
	}
	
	public boolean confirmCaseNew(CaseDataRaw caseDataRaw) {
		ModelCls cls = (ModelCls) getModelInstance(caseDataRaw.getTypeName());

		if (allCaseInstances.containsKey(caseDataRaw.getCaseName())) {
			log.info("Could not instantiate new case [" + caseDataRaw.getCaseName() + "], because this name already exists in case base.");
			return false;
		}
		createNewCaseInstance(cls, caseDataRaw);
		
		log.fine("created new case for [" + caseDataRaw.getCaseName() + "]");
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean confirmCaseChanges(CaseInstance caseInstance, CaseDataRaw caseDataRaw) {
		boolean repairAgain = allCaseInstances.containsKey(caseInstance.getName());
		
		repairAgain &= confirmCaseDeletion(caseInstance);
		
		ModelCls cls = (ModelCls) getModelInstance(caseDataRaw.getTypeName());
		if (!createNewCaseInstance(cls, caseDataRaw) && repairAgain) {
			// put old case into case base again.
			String caseName = caseInstance.getName();
			Collection caseInstancesOfCls = getAllCaseInstancesForCls(caseInstance.getModelCls());
			caseInstancesOfCls.add(caseInstance);
			allCaseInstances.put(caseName, caseInstance);
			return false;
		}
		
		log.fine("successfully applied changes for case [" + caseDataRaw.getCaseName() + "]");
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean confirmCaseDeletion(CaseInstance caseInstance) {
		if (!allCaseInstances.containsKey(caseInstance.getName())) {
			log.info("Could not delete case [" + caseInstance.getName() + "], because not found in case base.");
			return false;
		}

		Collection caseInstancesOfCls = getAllCaseInstancesForCls(caseInstance.getModelCls());
		caseInstancesOfCls.remove(caseInstance);
		allCaseInstances.remove(caseInstance);
		
		log.fine("successfully deleted case [" + caseInstance.getName() + "]");
		return true;
	}

	@SuppressWarnings("unchecked")
	public Collection getAllCaseInstancesForCls(ModelCls cls) {
		Collection allCaseInstancesForCls = (Collection)clsToItsCasInstances.get(cls);
		if (allCaseInstancesForCls == null) {
			allCaseInstancesForCls = new ArrayList();
			clsToItsCasInstances.put(cls, allCaseInstancesForCls);
		}
		return allCaseInstancesForCls;
	}

	public boolean changeModelSlot_addNewSymbol(ModelSlot modelSlot, String newSymbol) {
		if (newSymbol==null || newSymbol.equals("")) {
			return false;
		}
		
		ModelSlotStandalone slotStandalone = (ModelSlotStandalone) modelSlot;
		
		slotStandalone.addNewValue(newSymbol);
		
		return true;
	}

	public boolean changeModelSlot_removeOldSymbol(ModelSlot modelSlot, String oldSymbol) {
		if (oldSymbol==null || oldSymbol.equals("")) {
			return false;
		}
		
		ModelSlotStandalone slotStandalone = (ModelSlotStandalone) modelSlot;
		
		slotStandalone.removeOldValue(oldSymbol);
		
		return true;
	}

	public void renameModelInstance(String oldName, String newName) {
		// TODO 
		Object tmp = getModelInstance(oldName);
		if (tmp == null) {
			log.fine("huh? what do you want to rename?? [" + oldName + "]");
			return;
		}
		if (tmp instanceof ModelSlot) {
			allModelSlots.remove(oldName);
			allModelSlots.put(newName, (ModelSlot) tmp);
		} else {
			allModelCls.remove(oldName);
			allModelCls.put(newName, (ModelCls) tmp);
		}
		((ModelSlotStandalone)tmp).setName(newName);
	}

	public void setHasChanged() {
		// nothing to do, so far
	}

	@SuppressWarnings("unchecked")
	public Collection getAllModelCls() {
		return allModelCls.values();
	}
	
}
