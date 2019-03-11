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

import java.util.Collection;

import de.dfki.mycbr.CaseDataRaw;
import de.dfki.mycbr.ClsDataRaw;
import de.dfki.mycbr.model.casebase.CaseInstance;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import edu.stanford.smi.protege.model.Project;

/**
 * 
 * @author myCBR Team
 */
public abstract class ModelProvider {
	
	protected static ModelProvider instance;
	
	ModelProvider()  {}
	
	public static synchronized ModelProvider getInstance() {
		return instance;
	}

	public static ModelProvider initInstance(Project project) {
		if (project != null) {
			instance = new ModelProviderProtege(project);
		} else {
			instance = new ModelProviderStandalone();
		}
		return instance;
	}
	
	public static void resetInstance() {
		instance = null;
	}
	
	/**
	 * Initialize. Will be called by CbrTool when initializing.
	 * The given rawData collection contains all knowledge about the project 
	 * (except similarity measure functions) in a raw format.
	 * There is a raw format at all, because the task of structuring and organizing its content
	 * is supposed to be the business of the ModelProvider implementation.
	 * 
	 * @see ClsDataRaw, CaseDataRaw.
	 * 
	 * @param cbrProject CBRProject the corresponding project.
	 * @param rawData Collection of ClsDataRaw objects.
	 */
	public abstract void init(Collection< ClsDataRaw > rawData);

	/**
	 * Getter for Cls or Slot object in the model.
	 * Results will be cached.
	 * 
	 * @see ModelInstance
	 * @param name String name of the Cls/Slot in the CBR model.
	 * @return ModelInstance object representing a cls/slot in the model.
	 */
	public abstract ModelInstance getModelInstance(String name);

	/**
	 * Getter for Case instance in the model.
	 * Results will be cached.
	 * 
	 * @see de.dfki.mycbr.casebase.CaseInstance
	 * @param name String name of the case of the case base.
	 * @return CaseInstance object representing a case of the case base.
	 */
	public abstract CaseInstance getCaseInstance(String name);

	public abstract Collection<ModelCls> getAllModelCls();

	/**
	 * Creates a new CaseInstance and adds it to the internal case list. If an instance
	 * with the given name already exists, the behaviour is undefined for performance reasons.
	 * @param cls
	 * @return new CaseInstance with the given name
	 */
	public abstract CaseInstance createCaseInstance(String name, ModelCls cls);
	
	public abstract boolean confirmCaseNew(CaseDataRaw caseDataRaw);

	public abstract boolean confirmCaseChanges(CaseInstance caseInstance, CaseDataRaw caseDataRaw);

	public abstract boolean confirmCaseDeletion(CaseInstance caseInstance);

	public abstract boolean changeModelSlot_addNewSymbol(ModelSlot modelSlot, String newSymbol);

	public abstract boolean changeModelSlot_removeOldSymbol(ModelSlot modelSlot, String oldSymbol);

	public abstract void renameModelInstance(String oldName, String newName);

	public abstract void setHasChanged();

}
