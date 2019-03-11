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
package de.dfki.mycbr.modelprovider.standalone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import de.dfki.mycbr.CBRProject;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.modelprovider.ModelProviderStandalone;

/**
 * 
 * @author myCBR Team
 */
public class ModelClsStandalone extends ModelInstanceStandalone implements ModelCls {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ModelCls superCls;
	@SuppressWarnings("unchecked")
	private Collection slots;
	private ModelProviderStandalone modelProviderStandalone;

	@SuppressWarnings("unchecked")
	public ModelClsStandalone(String clsName, Collection slots, ModelProviderStandalone modelProviderStandalone) {
		super(clsName, modelProviderStandalone);
		this.slots = slots;
		this.modelProviderStandalone = modelProviderStandalone;
	}

	
	// start interface(ModelCls) methods
	@SuppressWarnings("unchecked")
	public Collection listSlots() {
		return slots;
	}

	@SuppressWarnings("unchecked")
	public Collection getDirectCaseInstances() {
		Collection c = modelProviderStandalone.getDirectCaseInstancesFor(this);
		return (c == null ? new ArrayList() : c);
	}

	public ModelCls getSuperCls() {
		return superCls;
	}
	
	public Collection<Object> getDirectSubClses() {
		Collection<Object> subClses = new HashSet<Object>();
		Collection<ModelCls> allModelCls = CBRProject.getInstance().getAllModelCls();
		for (ModelCls cls : allModelCls) {
			if (cls.getSuperCls() == this) {
				subClses.add(cls);
			}
		}
		
		return subClses;
	}

	
	// end interface methods
	
	
	public void setSuperCls(ModelCls superCls) {
		this.superCls = superCls;
	}

}
