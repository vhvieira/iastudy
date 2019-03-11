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
package de.dfki.mycbr.model.similaritymeasures;

import java.awt.Frame;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.dfki.mycbr.CBRProject;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.similaritymeasures.smftypes.transformers.SMFTransformer;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.model.vocabulary.ModelSlot;

/**
 * @author myCBR Team
 *
 * NOTE:
 * Always use a String (smfName) as key and a EditorSMFunction object as value.
 * Please avoid object manipulating methods e.g. such as put()
 * This class only inherits from HashMap because of its convenient options to read its data.
 */
public class SMFHolder extends java.util.HashMap<String, AbstractSMFunction>  implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(SMFHolder.class.getName());

	/** denotes whether this holder object has been changed or not. */
	private boolean hasChanged = false;
	/**
	 the active SMFunction is the one which is currently used in retrieval.
	 */
	private AbstractSMFunction activeSMF;

	private ModelInstance inst;

	
	public SMFHolder(ModelInstance inst) {
		this.inst = inst;
	}
	
	/**
	 confirms the edited smf.
	 
	 */
	public void confirmSMFunction(AbstractSMFunction smf) {
//		smf.hasBeenAccepted();
		put(smf.getSmfName(), smf);
		smf.setHasChanged(false);
		setHasChanged(true);
		if (activeSMF == null || activeSMF.getSmfName().equals(smf.getSmfName()))
		{
			// if no activeSMF has been set
			// or the old activeSMF object has been changed
			activeSMF = smf;
		}
	}

	/**
	 * @return active EditorSMFunction
	 
	 */
	public AbstractSMFunction getActiveSMF() {
		return activeSMF;
	}

	public void setActiveSMF(final AbstractSMFunction activeSMF) {
		this.activeSMF = activeSMF;
		confirmSMFunction(activeSMF);
	}
	
	/**
	 * get a certain smfunction from this holder which may not necessarily be the active smf.
	 * @param smfName String name of this smfunction
	 * @return EditorSMFunction the requested smfunction object
	 */
	public AbstractSMFunction getCertainSMFunctionFromHolder(String smfName) {
		return get(smfName);
	}
	
	@Override
	public void clear() {
		if (size()>0) setHasChanged(true);
		activeSMF = null;
		super.clear();
	}	
	
	/**
	 * Protege and myCBR share common data. Because of redundancies inconsistencies may occur. 
	 * This method is used to check all inconsistencies concerning the Holder object.
	 * Just pass through.
	 * 
	 * If inconsitencies are found repair them.
	 * Parent is the application frame. So you can open modal dialogs to handle repair operations.
	 * 
	 *  @return boolean true if changes have been made. Maybe one has to perform sth like a refresh.
	 */
	public boolean checkConsistency(Frame parent) {
		checkInitSMF();
		// check value types
		if (size()==0) return false;
		boolean changesMade = false;
		if (activeSMF.getModelInstance() instanceof ModelSlot) {
			for (String smfName: new Vector<String>(keySet())) {
				AbstractSMFunction smf = get(smfName);
				
				ModelSlot slot = (ModelSlot) smf.getModelInstance();
				if (slot.getValueType() != smf.getValueType()) {
					ValueType newValueType = slot.getValueType();
					log.fine("Value Type of slot ["+slot.getName()+"] has changed from ["+smf.getValueType()+"] to ["+newValueType+"]. transform SMF.");
	
					if (SMFTransformer.isTransformable(smf.getValueType(), newValueType)) {
						AbstractSMFunction transformedSMF = SMFTransformer.transform(smf, newValueType, smf.getSMFunctionTypeName());
						put(smfName, transformedSMF);
						if (smf==activeSMF) {
							activeSMF = transformedSMF;
						}
						log.fine("transformed successfully ["+smfName+"]");
					} else {
						log.fine("delete ["+smfName+"]");
						deleteSMF(smfName);
						checkInitSMF();
					}
					
					changesMade = true;
				}
				if (smf!=null) {
					smf.checkConsistency(parent, true);
				}
			}
		}
		return changesMade;
	}

	private void checkInitSMF() {
		if (size()>0) return;
		try {
			CBRProject.getInstance().newSMF(inst, AbstractSMFunction.DEFAULT_SMF_NAME);
		} catch (Exception e) {
			log.log(Level.FINE, "could not initialize SMF", e);
		}
	}

	// commented out before 20.10.2008
//	/**
//	 * modifies the given XMLElement to represent a serialization of this instance with all of its containing smfunctions
//	 */
//	public Element toXML()
//	{
//		Element xmlElement = new Element(XMLConstants.XML_TAG_HOLDER);
//		return xmlElement;
//	}

	public void deleteSMF(String smfName) {
		remove(smfName);
		setHasChanged(true);
		if (smfName.equals(activeSMF.getSmfName())) {
			activeSMF = null;
			// choose the next best function to be the active one.
			if (values().size() > 0) {
				activeSMF = (AbstractSMFunction) values().iterator().next();
			}
		}
	}
	
	/**
	 * 
	 * @return true if holder has been changed otherwise false.
	 */
	public boolean hasChanged() {
		return hasChanged;
	}

	/**
	 * Sets the hasChanged flag to the given boolean value.
	 * This flag denotes whether this holder object has been changed or not.
	 * @param hasChanged new value for the hasChanged flag.
	 */
	public void setHasChanged(boolean hasChanged) {
		log.fine("holder has changed ["+hasChanged+"]");
		
		if (!hasChanged) {
			// notify all internal smfs
			for (Iterator< AbstractSMFunction > it=values().iterator(); it.hasNext();) {
				AbstractSMFunction smf = it.next();
				smf.setHasChanged(false);
			}
		}
		
		this.hasChanged = hasChanged;
		if (hasChanged) CBRProject.getInstance().setHasChanged();
	}

	/**
	 * NOTE: This should be used only by CBRProject.
	 * It defines the active smf in this holder, but doesnt confirm it. (To avoid hasChanged() calls)
	 * @param smf
	 */
	public void initActiveSMF(AbstractSMFunction activeSMF) {
		this.activeSMF = activeSMF;
	}

	public void renameSMFs(String oldName, String newName) {
		log.fine("set editorSMFunctions modelInstance names from ["+oldName+"] to ["+newName+"]:  size=["+size()+"]");
		for (Iterator< AbstractSMFunction > it=values().iterator(); it.hasNext();) {
			AbstractSMFunction smf = (AbstractSMFunction) it.next();
			smf.setModelInstanceName(newName);
		}	
	}
	
}
