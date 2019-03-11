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
package de.dfki.mycbr.model.similaritymeasures.smftypes;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import de.dfki.mycbr.CBRProject;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.SMFContainer;
import de.dfki.mycbr.model.vocabulary.ModelCls;

public class ClsToSMFTableInheritance implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private HashMap<ModelCls, HashMap<ModelCls, AbstractSMFunction>> clsToMap = new HashMap<ModelCls, HashMap<ModelCls, AbstractSMFunction>>();
	

	public ClsToSMFTableInheritance() {
		
		Collection<ModelCls> allModelCls = CBRProject.getInstance().getAllModelCls();
		for (ModelCls cls1 : allModelCls) {
			HashMap<ModelCls, AbstractSMFunction> map = new HashMap<ModelCls, AbstractSMFunction>();
			
			for (ModelCls cls2 : allModelCls) {
				AbstractSMFunction smf = getCommonAncestorsSMF(cls1, cls2);
				map.put(cls2, smf);
			}
			clsToMap.put(cls1, map);
		}	
	}
	
	public AbstractSMFunction getCommonSMF(ModelCls cls1, ModelCls cls2) {
		HashMap<ModelCls, AbstractSMFunction> map = clsToMap.get(cls1);
		if (map == null) {
			return getCommonAncestorsSMF(cls1, cls2);
		}
		AbstractSMFunction smf = map.get(cls2);
		if (smf == null) {
			return getCommonAncestorsSMF(cls1, cls2);
		}
		return smf;
	}

	
	private AbstractSMFunction getCommonAncestorsSMF(ModelCls cls1, ModelCls cls2) {
		ModelCls commonAncestorCls = null;
		if (cls1 == cls2) {
			commonAncestorCls = cls2;
		} else {
			Vector<ModelCls> allAncestorsQ = new Vector<ModelCls>();
			ModelCls superClsQ = cls1;
			while (superClsQ != null) {
				allAncestorsQ.add(superClsQ);
				superClsQ = superClsQ.getSuperCls();
			}
			ModelCls superClsC = cls2;
			while (superClsC != null && !allAncestorsQ.contains(superClsC)) {
				superClsC = superClsC.getSuperCls();
			}
			commonAncestorCls = superClsC;
		}
//		if (smf.getModelInstance()==commonAncestorCls) return smf;
		
		AbstractSMFunction commonSMF = null;//cbrProject.getSMFHolderForModelInstance(commonAncestorCls).getActiveSMF();
		while (commonSMF==null && commonAncestorCls!=null) {
			commonSMF = SMFContainer.getInstance().getSMFHolderForModelInstance(commonAncestorCls).getActiveSMF();
			commonAncestorCls = commonAncestorCls.getSuperCls();
		}
		
		return commonSMF;
	}
}
