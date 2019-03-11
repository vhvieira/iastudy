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
package de.dfki.mycbr.model.vocabulary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Logger;

import de.dfki.mycbr.model.casebase.CaseInstance;
import de.dfki.mycbr.retrieval.DefaultQuery;
import de.dfki.mycbr.retrieval.RetrievalEngine;

public abstract class Filter implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final static Logger log = Logger.getLogger(Filter.class.getName());
	private String label;
	private String description;
	private de.dfki.mycbr.retrieval.RetrievalEngine retrievalEngineToNotify;
	private double counter;
	private double amount;

	public Filter(String label, String description) {
		this.label = label;
		this.description = description;
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getDescription() {
		return description;
	}
	
	@Override
	public String toString() {
		return getLabel();
	}
	
	public Collection<CaseInstance> doFilter(ModelSlot slot, DefaultQuery q, Collection<CaseInstance> ciSet) {
		Collection<CaseInstance> filteredOut = new ArrayList<CaseInstance>();
		
		// find slot path (think of composition in object oriented domains)
		Vector<ModelSlot> slotPath = new Vector<ModelSlot>();
		boolean found = findPathToSlot(slot, q, slotPath);
		if (!found) {
			log.warning("Could not filter! Slot path to [" + slot + "] not found!");
			return ciSet;
		}
		int spl = slotPath.size();

		// find query value
		for (int i=0; i<spl; i++) {
			if (q == null) break;
			q = (DefaultQuery) q.getSlotValue(slotPath.get(i)); 
		}
		if (q == null)  {
			log.fine("Cannot filter on slot [" + slot + "]. Query value not defined");
		}
		Object queryVal = q.getSlotValue(slot);
		
		SpecialValueHandler svh = SpecialValueHandler.getInstance();
		if (svh.isSpecialValue(queryVal)) {
			return filteredOut;
		}
		
		for (Iterator<CaseInstance> it= ciSet.iterator(); it.hasNext();) {
			CaseInstance ci = it.next();
			CaseInstance ciPart = ci;
			for (int i=0; i<spl; i++) {
				if (ciPart == null) break;
				ciPart = (CaseInstance) ciPart.getSlotValue(slotPath.get(i)); 
			}
			if (ciPart == null) continue;
			
			Object caseVal = ciPart.getSlotValue(slot);
			if (svh.isSpecialValue(caseVal)) continue;
			if (!accept(queryVal, caseVal)) {
				filteredOut.add(ci);
				if (retrievalEngineToNotify!=null && ((counter++)%RetrievalEngine.STATE_LISTENERS_NOTICE_STEP==0)) {
					counter++;
					retrievalEngineToNotify.updateStateListeners(counter/amount);
				}
			}
			
		}
		return filteredOut;
	}
	
	private boolean findPathToSlot(ModelSlot slot, DefaultQuery q, Vector<ModelSlot> slotPath) {
		if (q.containsKey(slot)) {
			return true;
		}
		for (Iterator<Entry<ModelSlot,Object>> it=q.entrySet().iterator(); it.hasNext();) {
			Entry<ModelSlot,Object> e = it.next();
			Object value = e.getValue();
			if (value instanceof DefaultQuery) {
				if (findPathToSlot(slot, (DefaultQuery) value, slotPath)) {
					slotPath.insertElementAt((ModelSlot) e.getKey(), 0);
					return true;
				}
			}
		}
		return false;
	}

	public abstract boolean accept(Object queryVal, Object caseVal);

	public void setUpdateFilterState(RetrievalEngine re, double counter, double amount) {
		this.retrievalEngineToNotify = re;
		this.counter = counter;
		this.amount = amount;
	}

}
