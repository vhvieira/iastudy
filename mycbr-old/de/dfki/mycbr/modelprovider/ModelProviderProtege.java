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

import javax.swing.JOptionPane;

import de.dfki.mycbr.CBRProject;
import de.dfki.mycbr.CaseDataRaw;
import de.dfki.mycbr.CaseDataRawImpl;
import de.dfki.mycbr.ClsDataRaw;
import de.dfki.mycbr.SlotDataRaw;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.casebase.CaseInstance;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.model.vocabulary.SpecialValueHandler;
import de.dfki.mycbr.modelprovider.protege.CaseInstanceProtege;
import de.dfki.mycbr.modelprovider.protege.Messages;
import de.dfki.mycbr.modelprovider.protege.ModelClsProtege;
import de.dfki.mycbr.modelprovider.protege.ModelSlotProtege;
import de.dfki.mycbr.modelprovider.protege.similaritymodeling.MyCbr_Similarities_Tab;
import edu.stanford.smi.protege.event.ClsAdapter;
import edu.stanford.smi.protege.event.ClsEvent;
import edu.stanford.smi.protege.event.ClsListener;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.event.FrameListener;
import edu.stanford.smi.protege.event.KnowledgeBaseEvent;
import edu.stanford.smi.protege.event.KnowledgeBaseListener;
import edu.stanford.smi.protege.event.SlotEvent;
import edu.stanford.smi.protege.event.SlotListener;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;

/**
 * 
 * @author myCBR Team
 */
public class ModelProviderProtege extends ModelProvider implements FrameListener, KnowledgeBaseListener, SlotListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger ( ModelProviderProtege.class.getName ( ) );
	
	/** Cache for model instances. Maps name of ModelInstance (String) to ModelInstance object */
	private HashMap<String, ModelInstance> cacheModelInstances = new HashMap<String, ModelInstance>();
	/** Cache for case instances. Maps name of CaseInstance (String) to CaseInstance object */
	private HashMap<String, CaseInstance> cacheCaseInstances = new HashMap<String, CaseInstance>();
	
	
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private Collection allModelCls = new ArrayList();


//	/** maps ProtegeInstance to ModelInstance */
//	private HashMap protInstToModelInst = new HashMap();
	
	
	// protege
	private Project protegeProject;
	private KnowledgeBase knowledgeBase;
	private ClsListener clsListener;

	private Slot slotValueType;
	private Slot slotNumericMinimum;
	private Slot slotNumericMaximum;
	
	/**
	 * Will be called from protege side.
	 * @param protegeProject
	 */
	ModelProviderProtege(Project protegeProject) {
		this.protegeProject = protegeProject;
		this.knowledgeBase = protegeProject.getKnowledgeBase();

		slotValueType 		= knowledgeBase.getSlot(":SLOT-VALUE-TYPE"); //$NON-NLS-1$
		slotNumericMinimum	= knowledgeBase.getSlot(":SLOT-NUMERIC-MINIMUM"); //$NON-NLS-1$
		slotNumericMaximum	= knowledgeBase.getSlot(":SLOT-NUMERIC-MAXIMUM"); //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	protected void updateAllModelCls() {
		log.fine("update all modelCls"); //$NON-NLS-1$
		allModelCls.clear();
		for (Iterator it=knowledgeBase.getClses().iterator(); it.hasNext();) {
			Cls cls = (Cls) it.next();
			allModelCls.add(getModelInstance(cls.getName()));
		}
	}

	public KnowledgeBase getKnowledgeBase() {
		return protegeProject.getKnowledgeBase();
	}
	
	/**
	 * Will be called from cbrtool side.
	 * NOTE:
	 * Our policy here is the following: All data which is not consistent with the protege model will be changed
	 * in the protege model, NOT in our cbr model.
	 */
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public void init(Collection rawData) {
		log.info("initializing PROTEGE MODEL PROVIDER."); //$NON-NLS-1$
		addProtegeListeners();

		
		if (rawData != null) {
			checkRawData(rawData);
		}

		//
		// add clsListener to all Cls
		//
		for (Iterator it=protegeProject.getKnowledgeBase().getClses().iterator(); it.hasNext();) {
			Cls cls = (Cls) it.next();
			cls.addClsListener(clsListener);
//			cls.addFrameListener(this);
		}
		
		updateAllModelCls();
	}


	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private void checkRawData(Collection rawData) {
		StringBuffer errorMsg = new StringBuffer();
		errorMsg.append(Messages.getString("Something_changed_in_Proteges_absence")); //$NON-NLS-1$
		boolean errorOccurred = false;
		ArrayList<CaseDataRawImpl> notFoundInProtege 	= new ArrayList<CaseDataRawImpl>(); 
		ArrayList<Instance> notFoundInMyCBR 			= new ArrayList<Instance>(); 
		try {
			for (Iterator clsIt = rawData.iterator(); clsIt.hasNext();) {
				ClsDataRaw clsRaw = (ClsDataRaw) clsIt.next();
				boolean localErrorOccurred = false;
				log.fine("check cls [" + clsRaw.getClsName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$

				//
				// first check slots and value types.
				//
				for (Iterator slotIt = clsRaw.getSlotsAndValueTypes().entrySet().iterator(); slotIt.hasNext();) {
					Entry entry = (Entry) slotIt.next();
					String slotName = (String) entry.getKey();

					Slot slot = (Slot) knowledgeBase.getInstance(slotName);
					SlotDataRaw slotDataRaw = (SlotDataRaw) entry.getValue();
					ValueType valueType = slotDataRaw.getValueType();

					if (slot == null) {
						errorMsg.append(String.format(Messages.getString("Slot_not_found_in_protege"), slotName)); //$NON-NLS-1$
						localErrorOccurred = true;
					} else {
						if (!slot.getValueType().toString().equals(valueType.toString())) {
							log.info(Messages.getString("Value_types_are_different") + slot.getName()); //$NON-NLS-1$
							errorMsg.append(String.format(Messages.getString("Slot__value_type_inconsistent"), slotName, valueType.toString(), slot.getValueType().toString())); //$NON-NLS-1$
							localErrorOccurred = true;
						}

						//
						// check allowed/min/max values.
						//
						if (slotDataRaw.getAllowedValues() != null) {
							// get copy of proteges allowed values
							Collection allValsCopy = new ArrayList(slot.getAllowedValues());

							for (Iterator valIt = slotDataRaw.getAllowedValues().iterator(); valIt.hasNext();) {
								String value = (String) valIt.next();

								if (allValsCopy.contains(value)) {
									allValsCopy.remove(value);
								} else {
									errorMsg.append(String.format(Messages.getString("Not_implemented_protege_slot_does_not_know_value"), slotName, value)); //$NON-NLS-1$
									localErrorOccurred = true;
								}
							}

							if (allValsCopy.size() > 0) {
								errorMsg.append(String.format(Messages.getString("Not_implemented_value_not_in_mycbr"), slotName, allValsCopy.toString())); //$NON-NLS-1$
								localErrorOccurred = true;
							}
						}
						if (slotDataRaw.getMaximumValue()!=null && slotDataRaw.getMaximumValue().doubleValue()!=slot.getMaximumValue().doubleValue()) {
							errorMsg.append(String.format(Messages.getString("Not_implemented_protege_slot_maxvalue"), slotName, slotDataRaw.getMaximumValue(), slot.getMaximumValue())); //$NON-NLS-1$
							localErrorOccurred = true;
						}
						if (slotDataRaw.getMinimumValue()!=null && slotDataRaw.getMinimumValue().doubleValue()!=slot.getMinimumValue().doubleValue()) {
							errorMsg.append(String.format(Messages.getString("Not_implemented_protege_slot_minvalue"), slotName, slotDataRaw.getMinimumValue(), slot.getMinimumValue())); //$NON-NLS-1$
							localErrorOccurred = true;
						}

					}

				}

				//
				// then check case base data
				//
				Cls cls = (Cls) knowledgeBase.getInstance(clsRaw.getClsName());
				if (cls == null) {
					log.fine("cls is NULL!"); //$NON-NLS-1$
					errorMsg.append(String.format(Messages.getString("Cls_not_found_in_protege"), clsRaw.getClsName())); //$NON-NLS-1$
					localErrorOccurred = true;
				} else {
					HashMap<String, Instance> clsInstancesProtege = new HashMap<String, Instance>();
					for (Iterator tmpIt = cls.getDirectInstances().iterator(); tmpIt.hasNext();) {
						Instance inst = (Instance) tmpIt.next();
						clsInstancesProtege.put(inst.getName(), inst);
					}

					if (clsRaw.getCaseInstances() != null) {
						Collection allInstancesCbr = new ArrayList(clsRaw.getCaseInstances());
						for (Iterator tmpIt = allInstancesCbr.iterator(); tmpIt.hasNext();) {
							CaseDataRaw cdr = (CaseDataRaw) tmpIt.next();
							if (clsInstancesProtege.containsKey(cdr.getCaseName())) {
								clsInstancesProtege.remove(cdr.getCaseName());
								tmpIt.remove();
							} else {
								errorMsg.append(String.format(Messages.getString("Could_not_find_case_instance_in_protege"), cdr.getCaseName())); //$NON-NLS-1$
								notFoundInProtege.add((CaseDataRawImpl) cdr);
							}
						}
					}

					if (clsInstancesProtege.size() > 0) {
						errorMsg.append(String.format(Messages.getString("Cound_not_find_case_in_mycbr"), clsInstancesProtege.size())); //$NON-NLS-1$
//						localErrorOccurred = true;
						notFoundInMyCBR.addAll(clsInstancesProtege.values());
					}
				}

				errorOccurred |= localErrorOccurred;
			}
		} catch (Throwable e) {
			log.log(Level.SEVERE, "initialization of model provider (protege) failed:", e); //$NON-NLS-1$
		}
		

		//
		// If Casebases of MyCBR and Protege projects are inconsistent: make consistency proposals
		//
		String note = Messages.getString("Note_during_work_only_protege_cases_will_be_used"); //$NON-NLS-1$
		if (notFoundInProtege.size() > 0) {
			int result = JOptionPane.showConfirmDialog(MyCbr_Similarities_Tab.instance(), String.format(Messages.getString("Cases_not_found_in_protege_project"), notFoundInProtege.size()), Messages.getString("Mycbr"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$ //$NON-NLS-2$
			if (result == JOptionPane.YES_OPTION)  {
				log.info("Import [" + notFoundInProtege.size() + "] instances from MyCBR project to Protege project.\n" + note); //$NON-NLS-1$ //$NON-NLS-2$
		        KnowledgeBase kb = protegeProject.getKnowledgeBase();
		        SpecialValueHandler sph = SpecialValueHandler.getInstance();
	            
				for (CaseDataRawImpl cdr : notFoundInProtege) {
					Cls cls = (Cls) kb.getInstance(cdr.getTypeName());
			        kb.createInstance(cdr.getCaseName(), cls);
				}
				for (CaseDataRawImpl cdr : notFoundInProtege) {
			        Instance newInst = kb.getInstance(cdr.getCaseName());
			        for (Iterator it=cdr.entrySet().iterator(); it.hasNext();) {
			        	Entry e = (Entry) it.next();
			        	String slotName = (String) e.getKey();
			        	Object value = e.getValue();
			        	if (sph.isSpecialValue(value)) {
			        		continue;
			        	}
			        	Slot slot = (Slot) kb.getInstance(slotName);
			        	if (slot == null) {
			        		log.warning("Slot with name [" + slotName + "] not found"); //$NON-NLS-1$ //$NON-NLS-2$
			        	}
			        	if (slot.getValueType() == edu.stanford.smi.protege.model.ValueType.INSTANCE) {
			        		value = kb.getInstance((String)value);
			        	}
			        	if (value instanceof Collection) {
					        newInst.setOwnSlotValues(slot, (Collection) value);
			        	} else {
					        newInst.setOwnSlotValue(slot, value);
			        	}
			        }
				}
			}
				
		}
		if (notFoundInMyCBR.size() > 0) {
			int result = JOptionPane.showConfirmDialog(MyCbr_Similarities_Tab.instance(),String.format(Messages.getString("ModelProviderProtege.Cases_not_found_in_mycbr_want_to_remove_them_from_protege"), notFoundInMyCBR.size(), note), Messages.getString("ModelProviderProtege.Mycbr"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$ //$NON-NLS-2$
			if (result == JOptionPane.YES_OPTION) {
		        KnowledgeBase kb = protegeProject.getKnowledgeBase();
				for (Instance inst : notFoundInMyCBR) {
					kb.deleteInstance(inst);
				}
			}
		}
		
		if (errorOccurred) {
			// error detection not implemented properly 
			
			// commented out before 20.10.2008
//			JOptionPane.showMessageDialog(MyCbr_Similarities_Tab.instance(), ""+errorMsg.toString()+"\n");
//			log.info(""+errorMsg.toString()+"\n");
		}
	}

	/**
	 * For CbrTool.
	 */
	public ModelInstance getModelInstance(String name) {
		try {
			if (cacheModelInstances.containsKey(name)) {
				log.fine("found in cache [" + name + "]"); //$NON-NLS-1$ //$NON-NLS-2$
				return (ModelInstance) cacheModelInstances.get(name);
			}
			
			Instance protInst = knowledgeBase.getInstance(name);
			log.fine("request for model instance [" + name + "]. " + (protInst==null ? "!! NOT FOUND !!" : "found it.")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	
			if (protInst == null) {
				return null;
			}
			
			ModelInstance mi = createModelInstance(protInst);
			return mi;
		} catch(Throwable t) {
			log.log(Level.SEVERE, "", t); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * For CbrTool
	 */
	public CaseInstance getCaseInstance(String name) {
		CaseInstance c = cacheCaseInstances.get(name);
		if (c != null) {
			log.fine("found in cache [" + name + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			return c;
		}
		Instance inst = knowledgeBase.getInstance(name);
		return createCaseInstance(inst);
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private ModelInstance createModelInstance(Instance protInst) {
		// protInst cannot be null.
		log.fine("creating model instance for [" + protInst.getName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		
		ModelInstance mi = null;
		if (protInst instanceof Slot) {
			// initialize ModelSlot
			mi = new ModelSlotProtege(this, protInst);
			for (Iterator it=knowledgeBase.getDomain((Slot) protInst).iterator(); it.hasNext();) {
				Cls cls = (Cls) it.next();
				ModelClsProtege modelCls = (ModelClsProtege) getModelInstance(cls.getName());
				modelCls.setUpdateSlotListNextTime();
			}
		} else {
			// initialize ModelCls
			mi = new ModelClsProtege(this, protInst);
		}
		
		cacheModelInstances.put(protInst.getName(), mi);
		return mi;
	}
	
	public CaseInstance createCaseInstance(String name, ModelCls cls) {
		Instance inst = knowledgeBase.createInstance(name, 
				knowledgeBase.getCls(cls.getName()));
		CaseInstance result =
			new CaseInstanceProtege(inst);
		cacheCaseInstances.put(name, result);
		return result;
	}
	
	private CaseInstance createCaseInstance(Instance protInstance) {
		if (protInstance == null) {
			log.fine("cannot create CaseInstance object. protege instance is null."); //$NON-NLS-1$
			return null;
		}
		log.fine("creating case instance for protege instance, name=[" + protInstance.getName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		CaseInstanceProtege caseInstance = new CaseInstanceProtege(protInstance);
		cacheCaseInstances.put(protInstance.getName(), caseInstance);
		return caseInstance;
	}

	
	public boolean confirmCaseNew(CaseDataRaw caseDataRaw) {
		ModelClsProtege cls = (ModelClsProtege) getModelInstance(caseDataRaw.getTypeName());

		if (knowledgeBase.containsFrame(caseDataRaw.getCaseName())) {
			log.info("Could not instantiate new case [" + caseDataRaw.getCaseName() + "], because this name already exists in case base."); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		createNewCaseInstance(cls, caseDataRaw);
		
		log.fine("created new case for [" + caseDataRaw.getCaseName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		return true;
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public boolean confirmCaseChanges(CaseInstance caseInstance, CaseDataRaw caseDataRaw) {
		ModelClsProtege cls = (ModelClsProtege) getModelInstance(caseDataRaw.getTypeName());
		
		if (cls == null) {
			log.severe("Could not create new case instance [" + caseDataRaw.getCaseName() + "], because class not found!"); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}

		Instance inst = knowledgeBase.getInstance(caseDataRaw.getCaseName());
		if (inst == null) {
			log.severe("Could not find case instance [" + caseDataRaw.getCaseName() + "], because of protege... i dont know!"); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		} else {
			
            for (Iterator it = cls.listSlots().iterator(); it.hasNext();) {
            	ModelSlotProtege modelSlot = (ModelSlotProtege)it.next();
            	
            	Slot slot = modelSlot.getSlotProtege();
            	
            	// commented out before 20.10.2008
//                Slot slot = kb.getSlot((String) header.get(i));
//                String valStr = (String) row.get(i);

//                ValueType valueType = ValueType.getValueType(slot.getValueType().toString());
            	
            	Object value = null;
            	if (modelSlot.getValueType()==ValueType.INSTANCE) {
            		CaseInstanceProtege ci = (CaseInstanceProtege) caseDataRaw.getSlotValue(modelSlot.getName());
            		if (ci != null) {
            			value = ((CaseInstanceProtege) ci).getCaseInstanceProtege();
            		}
            	} else {
            		value = caseDataRaw.getSlotValue(modelSlot.getName());
            	}
                
                if (value instanceof Collection) {
                	inst.setOwnSlotValues(slot, (Collection) value);
                } else {
                	inst.setOwnSlotValue(slot, value);
                }

            }
		}
		
		return true;
	}

	public boolean confirmCaseDeletion(CaseInstance caseInstance) {
		if (!knowledgeBase.containsFrame(caseInstance.getName())) {
			log.info("Could not delete case [" + caseInstance.getName() + "], because not found in case base."); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}

		knowledgeBase.deleteInstance(((CaseInstanceProtege)caseInstance).getCaseInstanceProtege());
		
		cacheCaseInstances.remove(caseInstance.getName());
		log.fine("successfully deleted case [" + caseInstance.getName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		return true;
	}
	

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private boolean createNewCaseInstance(ModelClsProtege cls, CaseDataRaw caseDataRaw) {
		if (cls == null) {
			log.severe("Could not create new case instance [" + caseDataRaw.getCaseName() + "], because class not found!"); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}

		Instance newInst = knowledgeBase.createInstance(caseDataRaw.getCaseName(), cls.getProtegeCls());
		if (newInst == null) {
			log.severe("Could not create new case instance [" + caseDataRaw.getCaseName() + "], because of protege... i dont know!"); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		} else {
            for (Iterator it = cls.listSlots().iterator(); it.hasNext();) {
            	ModelSlotProtege modelSlot = (ModelSlotProtege)it.next();
            	
            	Slot slot = modelSlot.getSlotProtege();
            	
            	// commented out before 20.10.2008
//                Slot slot = kb.getSlot((String) header.get(i));
//                String valStr = (String) row.get(i);

//                ValueType valueType = ValueType.getValueType(slot.getValueType().toString());
            	
            	Object value = null;
            	if (modelSlot.getValueType() == ValueType.INSTANCE) {
            		CaseInstanceProtege ci = (CaseInstanceProtege) caseDataRaw.getSlotValue(modelSlot.getName());
            		if (ci != null) {
            			value = ((CaseInstanceProtege) ci).getCaseInstanceProtege();
            		}
            	} else {
            		value = caseDataRaw.getSlotValue(modelSlot.getName());
            	}
                
                if (value instanceof Collection) {
                	newInst.setOwnSlotValues(slot, (Collection) value);
                }
                else {
                	newInst.setOwnSlotValue(slot, value);
                }

            }
		}
		
		return true;
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public boolean changeModelSlot_addNewSymbol(ModelSlot modelSlot, String newSymbol) {
		if (newSymbol==null || newSymbol.equals("")) { //$NON-NLS-1$
			return false;
		}
		
		Slot slotProt = ((ModelSlotProtege) modelSlot).getSlotProtege();
		
		Collection newAllowedValues = new ArrayList(slotProt.getAllowedValues());
		newAllowedValues.add(newSymbol);
		slotProt.setAllowedValues(newAllowedValues);
		
		return true;
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public boolean changeModelSlot_removeOldSymbol(ModelSlot modelSlot, String oldSymbol) {
		if (oldSymbol==null || oldSymbol.equals("")) { //$NON-NLS-1$
			return false;
		}
		
		Slot slotProt = ((ModelSlotProtege) modelSlot).getSlotProtege();
		
		Collection newAllowedValues = new ArrayList(slotProt.getAllowedValues());
		newAllowedValues.remove(oldSymbol);
		slotProt.setAllowedValues(newAllowedValues);
		
		return true;
	}

	public void renameModelInstance(String oldName, String newName) {
		cacheModelInstances.put(newName, cacheModelInstances.get(oldName));
		cacheModelInstances.remove(oldName);
		updateAllModelCls();
	}

	public void setHasChanged() {
		log.fine("TELL KNOWLEDGE BASE; IT HAS CHANGED."); //$NON-NLS-1$
		knowledgeBase.setChanged(true);
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public Collection getAllModelCls() {
		 
		// commented out before 20.10.2008
//		Collection allModelCls = new ArrayList();
//		for (Iterator it=knowledgeBase.getClses().iterator(); it.hasNext();)
//		{
//			Cls cls = (Cls) it.next();
//			allModelCls.add(getModelInstance(cls.getName()));
//		}
		
		return allModelCls;
	}
	
	private void addProtegeListeners() {
		this.clsListener = new ClsAdapter() {
			public void templateSlotRemoved(ClsEvent event) {
				Cls cls = event.getCls();
				ModelClsProtege clsProtege = (ModelClsProtege) ModelProvider.getInstance().getModelInstance(cls.getName());
				clsProtege.setUpdateSlotListNextTime();
			}
			
			public void templateSlotAdded(ClsEvent event) {
				Cls cls = event.getCls();
				ModelClsProtege clsProtege = (ModelClsProtege) ModelProvider.getInstance().getModelInstance(cls.getName());
				clsProtege.setUpdateSlotListNextTime();
			}
			
			public void directSubclassAdded(ClsEvent event) {
				Cls superCls = event.getCls();
				Cls subCls = event.getSubclass();
				CBRProject.getInstance()._subClsAdded((ModelCls) ModelProvider.getInstance().getModelInstance(superCls.getName()), (ModelCls) ModelProvider.getInstance().getModelInstance(subCls.getName()));
			}
		};
		
		knowledgeBase.addKnowledgeBaseListener(this);
		knowledgeBase.addFrameListener(this);
		knowledgeBase.addSlotListener(this);
	}
	
	
	//
	// implementation of frame listener
	//
	public void deleted(FrameEvent event) {
		//
		// NOTE:
		// In general, deletion events NEVER occur not often.
		// Slots are only 'removed' from its domain cls. Though, one can delete them...
		//
	}
	
	public void nameChanged(FrameEvent event) {
// tell CBRProject
		CBRProject.getInstance().renameModelInstance(event.getOldName(), event.getFrame().getName());
	}

	//
	// not interesting for us
	//
	public void browserTextChanged(FrameEvent event) {
		// don't care
	}
	public void ownFacetAdded(FrameEvent event) {
		// don't care
	}
	public void ownFacetRemoved(FrameEvent event) {
		// don't care
	}
	public void ownFacetValueChanged(FrameEvent event) {
		// don't care
	}
	public void ownSlotAdded(FrameEvent event) {
		// don't care
	}
	public void ownSlotRemoved(FrameEvent event) {
		// don't care
	}
	public void ownSlotValueChanged(FrameEvent event) {
		Slot slot = event.getSlot();
		if (slotValueType.equals(slot) || slotNumericMinimum.equals(slot) || slotNumericMaximum.equals(slot)) {
			CBRProject.getInstance().checkConsistencyForSlot((ModelSlot)getModelInstance(event.getFrame().getName()));
		}
	}
	public void visibilityChanged(FrameEvent event) {
		// don't care
	}
	
	
	//
	// Implementation of KnowledgeBaseListener
	//
	public void clsCreated(KnowledgeBaseEvent event) {
		CBRProject.getInstance().modelInstanceHasBeenCreated(event.getCls().getName());
		
		event.getCls().addClsListener(clsListener);
		updateAllModelCls();
	}

	public void clsDeleted(KnowledgeBaseEvent event) {
		updateAllModelCls();
		CBRProject.getInstance().modelClsHasBeenDeleted(event.getOldName());
	}

	public void defaultClsMetaClsChanged(KnowledgeBaseEvent event) {
		// don't care
	}

	public void defaultFacetMetaClsChanged(KnowledgeBaseEvent event) {
		// don't care
	}

	public void defaultSlotMetaClsChanged(KnowledgeBaseEvent event) {
		// don't care
	}

	public void facetCreated(KnowledgeBaseEvent event) {
		// don't care
	}

	public void facetDeleted(KnowledgeBaseEvent event) {
		// don't care
	}

	public void frameNameChanged(KnowledgeBaseEvent event) {
		// don't care
	}

	public void instanceCreated(KnowledgeBaseEvent event) {
		// don't care
	}

	public void instanceDeleted(KnowledgeBaseEvent event) {
		// don't care
	}

	public void slotCreated(KnowledgeBaseEvent event) {
		Slot slot = event.getSlot();
		CBRProject.getInstance().modelInstanceHasBeenCreated(slot.getName());
	}

	public void slotDeleted(KnowledgeBaseEvent event) {
		//
		// DOES NOT WORK IN PROTEGE!!
		//
		CBRProject.getInstance().modelClsHasBeenDeleted(event.getOldName());
	}
	//
	// end of KnowlegdeBaseListener implementation

	
	//
	// Begin interface SlotListener
	//
	public void directSubslotAdded(SlotEvent event) {
	}

	public void directSubslotMoved(SlotEvent event) {
	}

	public void directSubslotRemoved(SlotEvent event) {
	}

	public void directSuperslotAdded(SlotEvent event) {
	}

	public void directSuperslotRemoved(SlotEvent event) {
	}

	public void templateSlotClsAdded(SlotEvent event) {
	}

	public void templateSlotClsRemoved(SlotEvent event) {
		log.info("Slot[" + event.getSlot() + "] is beeing removed from domain cls [" + event.getCls() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ModelClsProtege cls = (ModelClsProtege) getModelInstance(event.getCls().getName());
		if (cls != null) {
			cls.updateSlotList();
			MyCbr_Similarities_Tab.instance().checkConsistency();
		}
	}
	//
	// end interface SlotListener

	public void setChanged(boolean b) {
		protegeProject.getKnowledgeBase().setChanged(false);
	}

}
