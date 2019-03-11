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
package de.dfki.mycbr.modelprovider.protege;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

import de.dfki.mycbr.model.casebase.CaseInstance;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.modelprovider.ModelProvider;
import de.dfki.mycbr.modelprovider.ModelProviderProtege;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.DefaultCls;
import edu.stanford.smi.protege.model.Instance;

/**
 * 
 * @author myCBR Team
 */
public class ModelClsProtege extends ModelInstanceProtege implements ModelCls {

	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(ModelClsProtege.class.getName());
	
	private Cls cls;
	@SuppressWarnings("unchecked")
	private HashMap slotToModelSlot = new HashMap();

	private boolean updateSlotListNextTime = true;

	public ModelClsProtege(ModelProviderProtege modelProviderProtege, Instance inst) {
		super(modelProviderProtege, inst);
		cls = (Cls) inst;
		setUpdateSlotListNextTime();
	}

	@SuppressWarnings("unchecked")
	public  Collection listSlots() {
		
		synchronized(slotToModelSlot) {
			if (updateSlotListNextTime) {
					updateSlotList();
			}
		}
		return slotToModelSlot.values();
	}

	@SuppressWarnings("unchecked")
	public synchronized Collection getDirectCaseInstances() {
		
		ArrayList al = new ArrayList();
		for (Iterator<Instance> it=cls.getInstances().iterator(); it.hasNext();) {
			Instance inst = it.next();
			// dont return null values!
			CaseInstance caseInstance = ModelProvider.getInstance().getCaseInstance(inst.getName());
			if (caseInstance != null) {
				al.add(caseInstance);
			} else {
				// should not happen.
			}
		}
		return al;
	}

	@SuppressWarnings("unchecked")
	public ModelCls getSuperCls() {
		
		Collection superclasses = new ArrayList<Cls>(cls.getDirectSuperclasses());
		
		// remove all instances beginning with ':' in its name. They're kind of 'hidden' by protege. 
		for (Iterator it=superclasses.iterator(); it.hasNext();) {
			Cls cls = (Cls) it.next();
			if (cls.getName().startsWith(":")) {
				it.remove();
			}
		}
		log.fine("look up superclass of [" + getName() + "]. Found [" + superclasses.size() + "] super classes." + (superclasses.size() > 1 ? "WARNING: MORE THAN ONE SUPERCLASS FOUND !!" : ""));
		if (superclasses.size() == 0) {
			return null;
		}
		
		ModelCls superCls = (ModelCls)ModelProvider.getInstance().getModelInstance(((Cls)superclasses.iterator().next()).getName());
		return superCls;
		
	}
	
	@SuppressWarnings("unchecked")
	public Collection<Object> getDirectSubClses() {
		Collection<Object> subClses = new HashSet<Object>(cls.getDirectSubclasses());
		Collection<Object> subClsesTmp =  new HashSet<Object>();
		subClsesTmp.addAll(subClses);
		
		for (Object subClsProt : subClsesTmp) {
			if (subClsProt instanceof ModelCls){
				ModelCls subCls = (ModelCls)ModelProvider.getInstance().getModelInstance(((ModelCls)subClsProt).getName());
				subClses.add(subCls);
			} else if (subClsProt instanceof DefaultCls) {
				ModelCls subCls = (ModelCls)ModelProvider.getInstance().getModelInstance(((DefaultCls)subClsProt).getName());
				subClses.add(subCls);
			}
		}
		return subClses;
	}

	public Cls getProtegeCls() {
		return cls;
	}

	@SuppressWarnings("unchecked")
	public synchronized void updateSlotList() {
		// update slotMap
		updateSlotListNextTime = false;
		slotToModelSlot.clear();
		for (Iterator it=cls.getVisibleTemplateSlots().iterator(); it.hasNext();) {
			Instance instTmp = (Instance)it.next();
			ModelInstance modelSlot = ModelProvider.getInstance().getModelInstance(instTmp.getName());
			slotToModelSlot.put(instTmp, modelSlot);
		}
	}

	public synchronized void setUpdateSlotListNextTime() {
		updateSlotListNextTime = true;
	}

}
