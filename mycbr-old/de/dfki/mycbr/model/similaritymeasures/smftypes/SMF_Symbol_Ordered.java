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

import java.awt.Frame;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.HasChangedListenerSMF;
import de.dfki.mycbr.model.similaritymeasures.SMFunctionFactory;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel_Symbol_Ordered;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.retrieval.Explanation;

/**
 * 
 * @author myCBR Team
 */
public class SMF_Symbol_Ordered extends AbstractSMFunction implements HasChangedListenerSMF {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger ( SMF_Symbol_Ordered.class.getName ( ) );
	
	public static final ValueType VALUE_TYPE = ValueType.SYMBOL;
	
	private static final String XML_TAG_MAP 			= "MAP";
	private static final String XML_TAG_INTERNAL_SMF 	= "InternalSMF";
	private static final String XML_ATT_SYMBOL			= "symbol";
	private static final String XML_ATT_INTEGER 		= "integer";
	private static final String XML_ATT_ISCYCLIC	 	= "isCyclic";
	private static final String XML_ATT_DISTLASTFIRST 	= "distLastFirst";
	
	private static final String NAME_SUFFIX_MODELSLOT 	= "_FAKE";
	private static final String NAME_SUFFIX_SMFNAME		= "_INTEGER";
	
	private static final int INT_STEP = 10;
	
	/** Each symbol will be mapped to an integer value. String to Integer. */
	private HashMap<Object, Number> symbolMapping = new HashMap<Object, Number>();
	
	/** After we mapped each symbol to an integer value, we create an integer slot for which we create an smf. */
	private FakeSlot fakeSlot = null;
	
	/** This is the original symbol slot. */
	private ModelSlot realSlot = null;
	
	/** internal smf. The internal similarity function for the fakeSlot. */ 
	private AbstractSMFunction smfInternal = null;

	/** these values define the range of the fake slot and of the internal integer smfunction. */
	private int maxInt = Integer.MIN_VALUE;
	private int minInt = Integer.MAX_VALUE;
	
	/** true if cyclic. */
	private boolean isCyclic = false;
	private int distLastFirst = INT_STEP;
	
	public SMF_Symbol_Ordered(ModelInstance inst, String smfName) throws Exception
	{
		super(inst, smfName);
		this.realSlot = (ModelSlot) inst;
		
		initMapping();
	}

	@SuppressWarnings("unchecked")
	public SMF_Symbol_Ordered(ModelInstance inst, Element smfElement) throws JDOMException {
		super(inst, smfElement);
		this.realSlot = (ModelSlot) inst;
		
		// load symbol mapping
		for (Iterator it=smfElement.getDescendants(new ElementFilter(XML_TAG_MAP)); it.hasNext();) {
			Element mapElement = (Element) it.next();
			String symbol = Helper.decode(mapElement.getAttributeValue(XML_ATT_SYMBOL));
			int symbolInt = mapElement.getAttribute(XML_ATT_INTEGER).getIntValue();
		
			if (symbolInt > maxInt) {
				maxInt = symbolInt; 
			}
			if (symbolInt < minInt) {
				minInt = symbolInt; 
			}
			
			symbolMapping.put(symbol, new Integer(symbolInt));
		}
		
		// load cyclic attributes
		isCyclic = smfElement.getAttribute(XML_ATT_ISCYCLIC).getBooleanValue();
		distLastFirst = smfElement.getAttribute(XML_ATT_DISTLASTFIRST).getIntValue();
		
		// get internal table
		Element internalSmfElement = smfElement.getChild(XML_TAG_INTERNAL_SMF);
		try {
			smfInternal = SMFunctionFactory.loadSMFunction(internalSmfElement, getFakeSlot());
			smfInternal.addHasChangedListener(this, true);
			log.fine("internalSMF uses a fake slot:\n" + smfInternal.getModelInstance());
		} catch (Exception e) {
			e.printStackTrace();
			log.log(Level.SEVERE, "could not load internal smf.", e);
		}
		
	}

	/**
	 * first save mapping. Then save internal smf.
	 */
	public void toXML(Element xmlElement) {
		// serialize symbol mapping.
		for (Iterator<Entry<Object, Number>> it = symbolMapping.entrySet().iterator(); it.hasNext();) {
			Entry<Object, Number> e = it.next();
			
			Element mapElement = new Element(XML_TAG_MAP);
			mapElement.setAttribute(XML_ATT_SYMBOL, Helper.encode(e.getKey().toString()));
			mapElement.setAttribute(XML_ATT_INTEGER, Helper.encode(e.getValue().toString()));
			
			xmlElement.addContent(mapElement);
		}
		
		// set cyclic attributes
		xmlElement.setAttribute(XML_ATT_ISCYCLIC, Helper.encode(Boolean.toString(isCyclic())));
		xmlElement.setAttribute(XML_ATT_DISTLASTFIRST, Helper.encode(Integer.toString(getDistLastFirst())));
		
		// now serialize internal smf
		Element internalSmfElement = smfInternal.initXMLElement();
		internalSmfElement.setName(XML_TAG_INTERNAL_SMF);
		smfInternal.toXML(internalSmfElement);
		xmlElement.addContent(internalSmfElement);
	}

	@SuppressWarnings("unchecked")
	public AbstractSMFunction copy() {
		SMF_Symbol_Ordered newSmf = null;
		
		try {
			newSmf = new SMF_Symbol_Ordered(inst, smfName);
			
			newSmf.fakeSlot = getFakeSlot();
			newSmf.smfInternal = getInternalSMF().copy();
			newSmf.smfInternal.addHasChangedListener(newSmf, true);
			newSmf.symbolMapping = (HashMap) symbolMapping.clone();
			
			newSmf.isCyclic = isCyclic;
			newSmf.distLastFirst = distLastFirst;
		} catch (Exception e) {
			e.printStackTrace();
			log.log(Level.SEVERE, "error while copying SMF", e);
		}
		
		return newSmf;
	}

	protected SMFPanel createSMFPanel() {
		return new SMFPanel_Symbol_Ordered(this);
	}

	@SuppressWarnings("unchecked")
	public boolean checkConsistency(Frame parent, boolean quiet) {
		log.fine("checking consistency for [" + getClass().getName().toUpperCase() + "].");

		if (realSlot.getValueType() != getValueType()) {
			if (!quiet) {
				JOptionPane.showMessageDialog(parent, "This smfunction was NOT made for type ["+realSlot.getValueType()+"]. Please check it!") ;
			}
			return false;
		}
		
		if (realSlot.getAllowedValues() == null) {
			if (!quiet) {
				JOptionPane.showMessageDialog(parent, "Symbol values are not set any more. Please check it!") ;
			}
			return false;
		}
		
		//
		// first identify changes in symbol set
		//
		Collection<?> idleSymbols = new TreeSet();
		Collection newSymbols = new TreeSet();
		ConsistencyChecker_Symbols.findNewAndIdleSymbols(realSlot.getAllowedValues(), symbolMapping.keySet(), newSymbols, idleSymbols);
		
		//
		// then let the user decide which symbols just have a new name
		//
		HashMap matchingSymbols = new HashMap();
		if (!ConsistencyChecker_Symbols.matchSymbols(parent, newSymbols, idleSymbols, matchingSymbols, quiet)) {
			return false;
		}
		newSymbols.removeAll(matchingSymbols.keySet());
		idleSymbols.removeAll(matchingSymbols.values());
		
		//
		// insert new symbols in smf.
		//
		for (Iterator it=newSymbols.iterator(); it.hasNext();) {
			insertNewSymbol((String) it.next());
		}
		
		//
		// delete idle symbols in smf.
		//
		for (Iterator it=idleSymbols.iterator(); it.hasNext();) {
			removeOldSymbol((String) it.next());
		}
		
		//
		// rename symbols in smf.
		//
		for (Iterator it=matchingSymbols.entrySet().iterator(); it.hasNext();) {
			Entry e = (Entry) it.next();
			renameSymbol((String) e.getValue(), (String) e.getKey());
		}
		
		return true;
	}

	private void renameSymbol(String oldSymbolName, String newSymbolName) {
		log.fine("rename symbol [" + oldSymbolName + "] to [" + newSymbolName + "]");
		symbolMapping.put(newSymbolName, symbolMapping.get(oldSymbolName));
		symbolMapping.remove(oldSymbolName);
		
		// update GUI
		if (editorPanel != null) {
			((SMFPanel_Symbol_Ordered) editorPanel).refresh();
		}
		setHasChanged(true);
	}

	private void removeOldSymbol(String oldSymbol) {
		log.fine("remove old symbol [" + oldSymbol + "]");

		symbolMapping.remove(oldSymbol);
		// maybe the range has changed.
		changeRange(Helper.getMinimum(symbolMapping.values()).intValue(), Helper.getMaximum(symbolMapping.values()).intValue(), isCyclic, distLastFirst);
		
		// update GUI
		if (editorPanel != null) {
			((SMFPanel_Symbol_Ordered) editorPanel).refresh();
		}
		setHasChanged(true);
	}

	private void insertNewSymbol(String newSymbol) {
		log.fine("insert new symbol [" + newSymbol + "]");
		
		int newMaxInt = maxInt + INT_STEP;
		symbolMapping.put(newSymbol, new Integer(newMaxInt));
		changeRange(minInt, newMaxInt, isCyclic, distLastFirst);
		
		// update GUI
		if (editorPanel != null) {
			((SMFPanel_Symbol_Ordered) editorPanel).refresh();
		}
		setHasChanged(true);
	}

	public static String getSMFunctionTypeName_static() {
		return "Ordered";
	}

	/**
	 * Both query and cb objects will be mapped to an Integer object.
	 * The resulting similarity will be calculated by the internal smf with these integers.
	 * @throws Exception 
	 */
	public double getSimilarityBetween(Object query, Object cb, Explanation exp) throws Exception {
		Integer queryInt = (Integer) symbolMapping.get(query);
		Integer caseInt = (Integer) symbolMapping.get(cb);
		
		if (queryInt==null || caseInt==null) {
			log.severe("Could not find integer representation for symbol [" + query + "," + queryInt + "] or [" + cb + "," + caseInt + "].");
			return 0;
		}
		
		// care about cyclic mode
		if (isCyclic()) {
			int range = maxInt + distLastFirst - minInt;

			int diff1 = queryInt.intValue() - caseInt.intValue();
			if (diff1 < -(range/2)) {
				queryInt = new Integer(queryInt.intValue()+range);
			} else if (diff1 >(range/2)) {
				queryInt = new Integer(queryInt.intValue()-range);
			}
		}
		
		// then pass through
		return smfInternal.getSimilarityBetween(queryInt, caseInt, exp);
	}

	public ValueType getValueType() {
		return VALUE_TYPE;
	}

	/**
	 * Init symbol mapping.
	 */
	private void initMapping() {
		symbolMapping.clear();
		
		int i = 0;
		for (Iterator<Object> it = realSlot.getAllowedValues().iterator(); it.hasNext();) {
			Object symbol = it.next();
			i += INT_STEP;
			symbolMapping.put(symbol, new Integer(i));
		}
		this.minInt = INT_STEP;
		this.maxInt = i;
	}


	/**
	 * The fake slot will be constructed in a lazy way. 
	 * (Actually, there is no reason for doing it lazily, but it doesnt bother either) 
	 * @return FakeSlot a ModelSlot object of type Integer.
	 */
	public FakeSlot getFakeSlot() {
		if (fakeSlot == null) {
			// init fake slot
			fakeSlot = new FakeSlot(inst.getName() + NAME_SUFFIX_MODELSLOT, ValueType.INTEGER);
			fakeSlot.setRange(new Integer(minInt), new Integer(maxInt));
		}
		return fakeSlot;
	}
	
	/**
	 * Getter for internal similarity function (which might be an SMFContainer of Integer SMFs).
	 * @return EditorSMFunction the internal function.
	 */
	public AbstractSMFunction getInternalSMF() {
		if (smfInternal == null) {
			try {
				smfInternal = SMFunctionFactory.createNewSMFunction(getFakeSlot(), getInternalSMFname(smfName)); 
				smfInternal.addHasChangedListener(this, true);
			} catch (Exception e) {
				e.printStackTrace();
				log.log(Level.SEVERE, "could not instantiate internal smf.", e);
			}
		}
		return smfInternal;
	}
	
	/**
	 * Constructs a derivate of our own smfname.
	 * @param smfName String the smfName to modify.
	 * @return String the new smfname.
	 */
	private String getInternalSMFname(String smfName) {
		return smfName+NAME_SUFFIX_SMFNAME;
	}

	/**
	 * Getter for the editor panel of the internal smf.
	 * @return SMFPanel the editor panel of the internal smf.
	 */
	public SMFPanel getInternalSMFPanel() {
		return getInternalSMF().getEditorPanel();
	}

	/**
	 * Getter for the mapping of symbols (String) to its integer representation (Integer).
	 * @return HashMap the symbol-to-integer map.
	 */
	public HashMap<Object, Number> getSymbolMapping() {
		return symbolMapping;
	}

	/**
	 * Changes a symbols integer representation.
	 * @param symbol String the concerned symbol.
	 * @param newVal Integer the new integer representation.
	 */
	public void setSymbolMapping(String symbol, Integer newVal) {
		symbolMapping.put(symbol, newVal);

		// maybe the range has changed.
		changeRange(Helper.getMinimum(symbolMapping.values()).intValue(), Helper.getMaximum(symbolMapping.values()).intValue(), isCyclic, distLastFirst);
		
		setHasChanged(true);
	}

	/**
	 * Changes the range of the internal smf.
	 * @param minimum int new minimum
	 * @param maximum int new maximum
	 */
	private void changeRange(int minimum, int maximum, boolean isCyclic, int distLastFirst) {
		if (minimum==minInt && maximum==maxInt && this.isCyclic==isCyclic && this.distLastFirst==distLastFirst) {
			return;
		}
		
		// set new values
		minInt = minimum;
		maxInt = maximum;
		this.isCyclic = isCyclic;
		this.distLastFirst = distLastFirst;
		
		if (isCyclic) {
			// change fake slots range
			// DEPRECATED: because range is only half as wide as before
			// the range is something really tricky now: lets check it
//			getFakeSlot().setRange(new Integer(minInt), new Integer(minInt+(maxInt-minInt+distLastFirst)/2));
			
			int range = maxInt+distLastFirst-minInt;
			int rh = range/2;
			int maxDist = 0;
			for (Iterator<Number> it1 = symbolMapping.values().iterator(); it1.hasNext();) {
				Number v1 = it1.next();
				for (Iterator<Number> it2 = symbolMapping.values().iterator(); it2.hasNext();) {
					Number v2 = it2.next();
					
					int diff = Math.abs(v1.intValue()-v2.intValue());
					if (diff > rh) {
						diff -= rh;
					}
					
					if (diff > maxDist) {
						maxDist = diff;
					}
				}
					
			}
			
			getFakeSlot().setRange(new Integer(minInt), new Integer(minInt+maxDist));
		} else {
			getFakeSlot().setRange(new Integer(minInt), new Integer(maxInt));
		}
		
		// now check consistency
		log.fine("now check consistency for internal smf.");
		SMFPanel p = getEditorPanel();
		smfInternal.checkConsistency((Frame)(p == null ? null : p.getTopLevelAncestor()), true);
	}

	
	public boolean isCyclic() {
		return isCyclic;
	}
	
	public void setCyclic(boolean isCyclic) {
		log.fine("Set Cyclic from [" + this.isCyclic + "] to [" + isCyclic + "]");
		changeRange(minInt, maxInt, isCyclic, distLastFirst);
//		this.isCyclic = isCyclic;
		
		setHasChanged(true);
	}

	public int getDistLastFirst() {
		return distLastFirst;
	}

	public void setDistLastFirst(int distLastFirst) {
		if (this.distLastFirst == distLastFirst) {
			return;
		}
		changeRange(minInt, maxInt, isCyclic, distLastFirst);
		setHasChanged(true);
	}

	public void smfHasChanged(boolean hasChanged) {
		log.fine("internal smf of ordered smf [" + getSmfName() + "] has changed.");
		// pass through to hasChangedListeners of this smf
		setHasChanged(hasChanged);
	}
	
}
