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
package de.dfki.mycbr.model.similaritymeasures;

import org.jdom.Element;
import org.jdom.JDOMException;

import de.dfki.mycbr.model.casebase.CaseInstance;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.model.vocabulary.SpecialValueHandler;
import de.dfki.mycbr.retrieval.Explanation;
import de.dfki.mycbr.retrieval.Query;

/**
 * 
 * @author myCBR Team
 *
 */
public abstract class AbstractClassSM extends AbstractSMFunction {
	

	private static final long serialVersionUID = 1L;
	protected ModelCls cls;
	protected SpecialValueHandler specialValueHandler;


	public AbstractClassSM(ModelInstance inst, Element smfElement) throws JDOMException {
		super(inst, smfElement);
		this.cls = (ModelCls) inst;
		this.specialValueHandler = SpecialValueHandler.getInstance();
	}
	
	public AbstractClassSM(ModelInstance inst, String smfName) {
		super(inst, smfName);
		this.cls = (ModelCls) inst;
		this.specialValueHandler = SpecialValueHandler.getInstance();
	}
	
	public int getFlags() {
		return 0;
	}
	
	public abstract double compareModelCls(Query query, CaseInstance caseInst, 
											Explanation thisExp, SimMap modelToSMF, 
											int flags) throws Exception;

	public SimMap getSimMap() { 
		SimMap result = new SimMap();
		result.put(inst, this);
		return result;
	}
	
}
