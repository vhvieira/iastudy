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
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.Element;
import org.jdom.JDOMException;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.XMLConstants;
import de.dfki.mycbr.explanation.SMExplanationContainer;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.retrieval.Explanation;

/**
 * 
 * @author myCBR Team
 */
public abstract class AbstractSMFunction implements Serializable {

	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(AbstractSMFunction.class
			.getName());

	protected String smfName;

	/**
	 * name of the model instance (ModelSlot or ModelCls) for which this
	 * similarity measure function is used.
	 */
	protected String modelInstanceName;

	protected SMExplanationContainer explanationContainer = null;
	private static final String DEFAULT_SMFUNCTIONTYPENAME = "TYPE_NOT_SET";

	/**
	 * Calculates similarity between query and cb. cb means case ('case' is a
	 * pre-defined key word in java). Explanation exp may be null.
	 * 
	 * @param query
	 *            a query object (type defined by getValueType())
	 * @param cb
	 *            a case object (type defined by getValueType())
	 * @param exp
	 *            Explanation
	 * @return double similarity between query and case [0,1].
	 * @throws Exception
	 *             if query or case value is invalid.
	 */
	public abstract double getSimilarityBetween(Object query, Object cb,
			Explanation exp) throws Exception;

	public String getModelInstanceName() {
		return modelInstanceName;
	}

	protected void setModelInstanceName(String newModelInstName) {
		this.modelInstanceName = newModelInstName;
	}

	public String getSmfName() {
		return smfName;
	}

	/**
	 * value type of the instance (e.g. symbol, integer, float, ...).
	 */
	public abstract ValueType getValueType();

	/**
	 * Initializes an XML element.
	 * 
	 * @return Element xml element.
	 */
	public final Element initXMLElement() {
		Element element = new Element(XMLConstants.XML_TAG_SMFUNCTION);

		checkConsistency(null, true);
		element.setAttribute(XMLConstants.XML_ATT_SMFNAME, Helper
				.encode(getSmfName()));
		element.setAttribute(XMLConstants.XML_ATT_MODEL_INSTNAME, Helper
				.encode(getModelInstanceName()));
		String typ = getValueType() != null ? getValueType().toString() : ""; // BUGFIX
																				// for
		// removed attributes!
		element.setAttribute(XMLConstants.XML_ATT_TYPE, Helper.encode(typ));

		return element;
	}

	public static final String DEFAULT_SMF_NAME = "default";

	/**
	 * is true when changes have been made on this object
	 */
	private boolean hasChanged;

	/**
	 * HasChangedListeners are stored in here that want to be notified when
	 * hasChanged flag has been set (not necessarily changed).
	 */
	private HashSet<HasChangedListenerSMF> hasChangedListeners_allways = new HashSet<HasChangedListenerSMF>();
	/**
	 * HasChangedListeners are stored in here that want to be notified when
	 * hasChanged flag has been 'switched'.
	 */
	private HashSet<HasChangedListenerSMF> hasChangedListeners_flanks = new HashSet<HasChangedListenerSMF>();

	protected ModelInstance inst;
	protected SMFPanel editorPanel;

	/**
	 * Serialize your SMFunction in here (modify the given element). This will
	 * be called during save() method (SMManager). You will get an equal
	 * XML-Element object when the loading constructor is called.
	 */
	public abstract void toXML(Element xmlElement);

	/**
	 * Constructor used when loading from file (e.g. at startup). Here you get
	 * an XML-Element which is equal to the one you produce in the toXML()
	 * method
	 */
	public AbstractSMFunction(ModelInstance inst, Element smfElement)
			throws JDOMException {
		this.modelInstanceName = Helper.decode(smfElement
				.getAttributeValue(XMLConstants.XML_ATT_MODEL_INSTNAME));
		this.smfName = Helper.decode(smfElement
				.getAttributeValue(XMLConstants.XML_ATT_SMFNAME));
		this.inst = inst;
		hasChanged = false;
		initExplanationContainer();
	}

	/**
	 * Please call this constructor. Some initial values will be set in here
	 * (smfName, type, instanceName, ...).
	 */
	public AbstractSMFunction(ModelInstance inst, String smfName) {
		this.modelInstanceName = inst.getName();
		this.smfName = smfName;
		this.inst = inst;
		// when this constructor is called changes have been made
		hasChanged = true;
	}

	public void setSmfName(final String smfName) {
		this.smfName = smfName;
	}

	/**
	 * returns an exact copy of itself. Used for the option to discard changes.
	 * NOTE: Please make sure that the hasChanged flag of the copy is set to
	 * false.
	 */
	public abstract AbstractSMFunction copy();

	/**
	 * Returns the Editor GUI Component.
	 */
	public final SMFPanel getEditorPanel() {
		if (editorPanel == null) {
			editorPanel = createSMFPanel();
		}
		return editorPanel;
	}

	/**
	 * Initialize the Editor GUI Component here. This method will be called at
	 * most once per instance.
	 */
	protected abstract SMFPanel createSMFPanel();

	/**
	 * Protege and Cbrtool share common data. Because of redundancies
	 * inconsistencies may occure. This method is used to check for all
	 * inconsistencies concerning the ModelInstance object and the data of the
	 * similarity function. E.g. maxValue, minValue, names, etc... If
	 * inconsitencies are found repair them.
	 * 
	 * @parameter quiet boolean, is true if popup dialogs are to avoid.
	 * 
	 *            Be-Aware-Of CheckList (inspired from Protege side): - user
	 *            changes the type of ModelInstance - user changes the range
	 *            (min/max value) of ModelSlot (e.g. of type Integer, Float,...)
	 *            - user changes the allowed values of ModelSlot (e.g.
	 *            Symbol,...) - user changed the name of ModelInstance
	 * 
	 *            Parent is the application frame. So you can open modal dialogs
	 *            to handle repair operations.
	 * 
	 * @return boolean true if everything is consistent when exiting the method,
	 *         false otherwise.
	 */
	public abstract boolean checkConsistency(Frame parent, boolean quiet);

	/**
	 * returns true if changes have been made on this object. call
	 * clearHasChanged() to set it to false again.
	 */
	public final boolean hasChanged() {
		return hasChanged;
	}

	/**
	 * Sets the hasChanged flag to b value. Call this when the changes have been
	 * made/saved or similar
	 */
	public final void setHasChanged(boolean b) {
		boolean tmpHasChanged_isNewValue = (hasChanged != b);
		hasChanged = b;

		fireHasChangedEvent(hasChangedListeners_allways, hasChanged);
		if (tmpHasChanged_isNewValue) {
			fireHasChangedEvent(hasChangedListeners_flanks, hasChanged);
		}
	}

	private final void fireHasChangedEvent(
			HashSet<HasChangedListenerSMF> listeners, boolean hasChanged) {
		for (Iterator<HasChangedListenerSMF> it = listeners.iterator(); it
				.hasNext();) {
			HasChangedListenerSMF hcl = it.next();
			if (hcl != null) {
				hcl.smfHasChanged(hasChanged);
			}
		}
	}

	/**
	 * NOTE: always_notify_me = true means: whenever this flag is manipulated
	 * the hcl will be called. always_notify_me = false means: hcl will be
	 * called "at the flanks"... when hasChanged flag is switching from
	 * true/false to false/true.
	 * 
	 * @param hcl
	 *            HasChangedListener to be notified when changes have been made.
	 * @param always_notify_me
	 *            boolean indicates when to notify this listener.
	 */
	public final void addHasChangedListener(HasChangedListenerSMF hcl,
			boolean always_notify_me) {
		if (always_notify_me) {
			hasChangedListeners_allways.add(hcl);
		} else {
			hasChangedListeners_flanks.add(hcl);
		}
	}

	public final void removeHasChangedListener(HasChangedListenerSMF hcl) {
		// log.finer("SMFunctionTypeName ["+getSMFunctionTypeName()+"] removing hasChangedListener["+hcl.getClass().getName()+"]");
		hasChangedListeners_allways.remove(hcl);
		hasChangedListeners_flanks.remove(hcl);
	}

	/**
	 * Calling static methods on java objects is weird! If the object was casted
	 * up to a more abstract class, the implementation of the higher class is
	 * used! Which programmer uses this 'feature' ? And static abstract methods
	 * are not allowed in java.
	 * 
	 * This method returns the smfunctiontypename of the class of this object by
	 * introspection.
	 * 
	 * @return the smfunctiontypename of this object's (lowest) class.
	 */
	public final String getSMFunctionTypeName() {
		String smfTypeName = null;
		try {
			smfTypeName = (String) getClass().getDeclaredMethod(
					"getSMFunctionTypeName_static").invoke(null);
		} catch (Exception e) {
			log
					.log(
							Level.SEVERE,
							"getSMFunctionTypeName must be overridden in ["
									+ getClass().getName()
									+ "]. Java does not support 'static abstract' methods. But this should be one.",
							e);
		}
		return smfTypeName;
	}

	public String toString() {
		return modelInstanceName + "_" + smfName;
	}

	public final ModelInstance getModelInstance() {
		return inst;
	}

	// this is overridden by SMF_External which needs a more complex
	// retrieval setup.
	public void startRetrieval() throws Exception {
	}

	// this is overridden by SMF_External which needs a more complex
	// retrieval setup.
	public void finishRetrieval() throws Exception {
	}

	public SMExplanationContainer getExplanationContainer() {
		return explanationContainer;
	}

	/**
	 * Override this method, if you want to provide your SMFunction with
	 * additional explanation capabilities.
	 * 
	 * @param element
	 */
	public void initExplanationContainer(Element element) {
		explanationContainer = null;
	}

	/**
	 * Override this method, if you want to provide your SMFunction with
	 * additional explanation capabilities.
	 */
	public void initExplanationContainer() {
		explanationContainer = null;
	}

	/**
	 * Required when using a container. This means, there are several SMFs for
	 * one value type. To make a distinction between different kinds of
	 * similarity measures, there must be a unique name for each kind. Please
	 * override this method when creating your own EditorSMFunction derivate.
	 * 
	 * Should be bound to class (not object), so this ought to be a static
	 * abstract method, but 'static abstract' is not allowed in java.
	 * 
	 * @return name of the similarity function type (should be bound to class).
	 */
	public static String getSMFunctionTypeName_static() {
		return DEFAULT_SMFUNCTIONTYPENAME;
	}
}
