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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import de.dfki.mycbr.model.casebase.CaseInstance;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.model.vocabulary.SpecialValueHandler;
import de.dfki.mycbr.modelprovider.ModelProvider;
import de.dfki.mycbr.retrieval.DefaultQuery;
import de.dfki.mycbr.retrieval.RetrievalEngine;

/**
 * This class is intended to give an easy access to a myCBR project from the code side.
 * The intention is to provide a complete interface to all the relevant functionality one may need
 * when trying to integrate a myCBR project into another software product.
 *
 * USAGE:
 * 
 *  * open a CBR project by calling the static method openProject(<project name>) in the CBRProject class.
 *    Difference between project file and project name: 
 *    'cars' is a project name - 'cars_CBR_SMF.XML' is a project file.
 *    You can also open a project in a certain path by the signature openProject(<project name>, <project dir>);
 *    
 *  * then create a MyCBR_Facade instance by passing the project instance to the constructor
 * 
 *  * do all your work over this facade object.
 *     
 * 
 * NOTE: As the name already says: this is a facade.
 * All functionality provided here is also directly available on a CBRProject instance, 
 * its derivates and the Helper class. If you need to get deeper into the internals of the
 * case-based reasoning system, we'd suggest to have a look at the classes mentioned here.
 * 
 * @author myCBR Team
 */
public class MyCBR_Facade implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private CBRProject project;

	/**
	 * @see CBRProject.openProject() for information about the instantiation of a MyCBR project.
	 * @param project the CBRProject to encapsulate in a facade.
	 */
	public MyCBR_Facade() {
		this.project = CBRProject.getInstance();
	}

	
	//
	// retrieval functionality
	//
	
	public RetrievalEngine getRetrievalEngine() {
		return RetrievalEngine.getInstance();
	}

	//
	// Access for the CBR vocabulary (the ontology of the CBR system)
	//

	/**
	 * @return all classes defined in the CBR model.
	 */
	public Collection<ModelCls> getAllModelCls() {
		return project.getAllModelCls();
	}

	/**
	 * @param slotName name of a slot defined in the CBR model.
	 * @return slot with name 'slotName' 
	 */
	public ModelSlot getModelSlotByName(String slotName) {
		return (ModelSlot) ModelProvider.getInstance().getModelInstance(slotName);
	}

	/**
	 * 
	 * @param clsName name of a class defined in the CBR model.
	 * @return class with name 'clsName'
	 */
	public ModelCls getModelClsByName(String clsName) {
		return (ModelCls) ModelProvider.getInstance().getModelInstance(clsName);
	}
	
	
	//
	// Access to the case base
	//

	/**
	 * Getter for the CaseInstance object by the name caseName
	 * @param caseName name of case 
	 * @return caseInstance object having the name caseName
	 */
	public CaseInstance getCaseInstanceByName(String caseName) {
		return ModelProvider.getInstance().getCaseInstance(caseName);
	}
	
	
	/**
	 * Converts a CaseInstance into a simple map. This maps from attribute name to value string.
	 * 
	 * @param caseInstance case of interest
	 * @return Map<String, String> a flat description of the case.
	 */
	public Map<String, String> caseDescription(CaseInstance caseInstance) {
		Map<String, String> cd = new TreeMap<String, String>();
		
		ArrayList<CaseInstance> ciList = new ArrayList<CaseInstance>();
		ciList.add(caseInstance);
		while (ciList.size()>0) {
			ArrayList<CaseInstance> al = new ArrayList<CaseInstance>(ciList);
			ciList.clear();
			for (CaseInstance ci: al) {
				for (ModelSlot slot: ci.listSlots()) {
					if (slot.getValueType()==ValueType.INSTANCE && !SpecialValueHandler.getInstance().isSpecialValue( ci.getSlotValue(slot))) {
						CaseInstance subCi = (CaseInstance) ci.getSlotValue(slot);
						if (subCi!=null) {
							ciList.add(subCi);
						}
						continue;
					}
					
					Object value = ci.getSlotValue(slot);
					String valStr = value.toString();
					cd.put(slot.getName(), valStr);
				}
			}
		}
		
		return cd;
	}

	
	/**
	 * Converts a DefaultQuery object into a simple map. This maps from attribute name to value string.
	 * This may be useful when the content of a query must be examined. 
	 *  
	 * @param query DefaultQuery the query of interest.
	 * @return Map<String, String> a flat description of the query.
	 */
	public Map<String, String> queryDescription(DefaultQuery query) {
		Map<String, String> cd = new TreeMap<String, String>();
		
		ArrayList<DefaultQuery> qList = new ArrayList<DefaultQuery>();
		qList.add(query);
		while (qList.size() > 0) {
			ArrayList<DefaultQuery> al = new ArrayList<DefaultQuery>(qList);
			qList.clear();
			for (DefaultQuery q: al) {
				for (ModelSlot slot : q.getModelCls().listSlots()) {
					if (slot.getValueType()==ValueType.INSTANCE && !SpecialValueHandler.getInstance().isSpecialValue( q.getSlotValue(slot))) {
						DefaultQuery subQ = (DefaultQuery) q.getSlotValue(slot);
						if (subQ != null) {
							qList.add(subQ);
						}
						continue;
					}
					
					Object value = q.getSlotValue(slot);
					String valStr = value.toString();
					cd.put(slot.getName(), valStr);
				}
			}
		}
		
		return cd;
	}
	
	public boolean confirmCaseNew(CaseDataRaw caseDataRaw) {
		return project.confirmCaseNew(caseDataRaw);
	}

}
