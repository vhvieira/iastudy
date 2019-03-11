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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.XMLConstants;
import de.dfki.mycbr.explanation.SMExplanationContainer;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel_Symbol_Table;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.Widget_Symmetry.SymmetryModeListener;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.retrieval.Explanation;

/**
 * @author myCBR Team
 *
 */
public class SMF_Symbol_Table extends AbstractSMFunction implements SymmetryModeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger ( SMF_Symbol_Table.class.getName ( ) );

	public static final ValueType VALUE_TYPE = ValueType.SYMBOL;
	
	public static final String XML_TAG_QUERYSYMBOL = "QuerySymbol";
	public static final String XML_TAG_CBSYMBOL 	= "CBSymbol";
	public static final String XML_ATT_SIMILARITY 	= "sim";
	public static final String XML_ATT_SYMBOL		= "symbol";

	private ModelSlot slot;
	
	/** maps each symbol (String) to an index (Integer) */
	HashMap<String, Integer> symbolOrder = new HashMap<String, Integer>();

	Vector<Vector<Double>> query_Rows = new Vector<Vector<Double>>();

	private boolean isSymmetricMode = true;
	
	/** maybe this table is backed by a taxonomy or an ordered smf. */
	private AbstractSMFunction backedSMF = null;
	
	@SuppressWarnings("unchecked")
	public SMF_Symbol_Table (ModelInstance inst, Element smfElement) throws JDOMException
	{
		super(inst, smfElement);
		this.slot = (ModelSlot) inst;
		
		// initialize symbol order

		int index = 0; 
		for (Iterator it = smfElement.getDescendants(new ElementFilter(XML_TAG_QUERYSYMBOL)); it.hasNext();) {
			Element querySymbols = (Element)it.next();
			String symbol = Helper.decode(querySymbols.getAttributeValue(XML_ATT_SYMBOL));
			
			symbolOrder.put(symbol, new Integer(index++));			
		}
		
		// prepare data vectors
		int size = symbolOrder.size();
		
		query_Rows = new Vector<Vector<Double>>(size);
		for (int i=0; i<size; i++) {
			Vector<Double> row = new Vector<Double>(size);
			for (int j=0; j<size; j++) {
				row.add(0d);
			}
			query_Rows.add(row);
		}
		
		// load similarity data
		for (Iterator it1 = smfElement.getDescendants(new ElementFilter(XML_TAG_QUERYSYMBOL)); it1.hasNext();) {
			Element querySymbols = (Element)it1.next();
			String symbolQ = Helper.decode(querySymbols.getAttributeValue(XML_ATT_SYMBOL));
			int indexQ = ((Integer)symbolOrder.get(symbolQ)).intValue(); 
			for (Iterator it2 = querySymbols.getDescendants(new ElementFilter(XML_TAG_CBSYMBOL)); it2.hasNext();) {
				Element cbSymbols = (Element)it2.next();

				String symbolCb = Helper.decode(cbSymbols.getAttributeValue(XML_ATT_SYMBOL));
				int indexCb = ((Integer)symbolOrder.get(symbolCb)).intValue();
				
				double sim = cbSymbols.getAttribute(XML_ATT_SIMILARITY).getDoubleValue();
				(query_Rows.get(indexQ)).set(indexCb, new Double(sim));
			}
		}
		isSymmetricMode = isSymmetric();
		
//		show();
		
	}
	
	@SuppressWarnings("unchecked")
	public SMF_Symbol_Table (ModelInstance inst, String smfName) {
		super(inst, smfName);
		this.slot = (ModelSlot) inst;

		// each symbol needs an index
		int index = 0;
		for (Iterator it = slot.getAllowedValues().iterator(); it.hasNext();) {
			// throws classcast exception if not string. just want to make sure...
			symbolOrder.put((String)it.next(), new Integer(index++));
		}
		
		log.fine("create table data");
		// now create a table model.
		for (Iterator it = symbolOrder.keySet().iterator(); it.hasNext();) {
			// add cb rows
			it.next();
			Vector colVector = new Vector(index);
			for (Iterator it2 = symbolOrder.keySet().iterator(); it2.hasNext();) {
				it2.next();
				colVector.add("");
			}
			query_Rows.add(colVector);
		}
		
		log.fine("initialize table data");
		// insert default values
		for (Iterator itQ = symbolOrder.keySet().iterator(); itQ.hasNext();) {
			String symb_Q = (String) itQ.next();
			int index_Q = ((Integer)symbolOrder.get(symb_Q)).intValue();
			for (Iterator itCb = symbolOrder.keySet().iterator(); itCb.hasNext();) {
				String symb_Cb = (String) itCb.next();
				int index_Cb = ((Integer)symbolOrder.get(symb_Cb)).intValue();
				
				double sim = symb_Cb.equals(symb_Q) ? 1 : 0;

				((Vector)query_Rows.get(index_Q)).set(index_Cb, new Double(sim));
			}
		}
		
		log.fine("finished construction.");
	}	
	
	@SuppressWarnings("unchecked")
	public void toXML(Element xmlElement) {
		if (backedSMF != null) {
			log.fine("this smf is backed by a taxonomy (SMF_Symbol_Taxonomy). So leave serialization to our master.\nNOTE: here is a little hack!");
			
			// description of the hack:
			// if this is backed by a taxonomy. We want to by totally serialized by our master (the taxonomy smf).
			// This will actually be serialized by SMFContainer.class which defines the SMFUNCTION_TYPE_NAME! (SIMMODE)
			// This has to be changed to the one of smf_symbol_taxonomy.
			xmlElement.setAttribute(XMLConstants.XML_ATT_SIMMODE, Helper.encode(backedSMF.getSMFunctionTypeName()));
			
			backedSMF.toXML(xmlElement);
			return;
		}
		
		Vector so = new Vector(symbolOrder.keySet());
		Collections.sort(so, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Integer)symbolOrder.get(o1)).compareTo((Integer)symbolOrder.get(o2));
			}
		});

		int size = query_Rows.size();
		for (int i=0; i<size; i++) {
			//Vector row = (Vector) query_Rows.get(i); // YYY
			Element queryElement = new Element(XML_TAG_QUERYSYMBOL);
			queryElement.setAttribute(XML_ATT_SYMBOL, Helper.encode(so.get(i).toString()));
			for (int j=0; j<size; j++) {
				double sim = getValueAt(i, j);
				if (sim == 0d) continue;
				Element cbElement = new Element(XML_TAG_CBSYMBOL);
				cbElement.setAttribute(XML_ATT_SIMILARITY, Double.toString(sim));
				cbElement.setAttribute(XML_ATT_SYMBOL, Helper.encode(so.get(j).toString()));
				queryElement.addContent(cbElement);
			}
			xmlElement.addContent(queryElement);
		}

	}

	@SuppressWarnings("unchecked")
	public AbstractSMFunction copy() {
		SMF_Symbol_Table newSmf = null;
		
		try {
			newSmf = new SMF_Symbol_Table(inst, smfName);
			
//			newSmf.show("Copy");
			
			Vector newQuery_Rows = new Vector(symbolOrder.size());
			log.fine("symbol order = [" + symbolOrder + "]");
			for (Iterator it = symbolOrder.keySet().iterator(); it.hasNext();) {
				newQuery_Rows.add(new Vector());
				it.next();
			}
			for (Iterator it = symbolOrder.keySet().iterator(); it.hasNext();) {
				String qVal = (String)it.next();
				// copy row vector (cb entries)
				newQuery_Rows.set(((Integer)symbolOrder.get(qVal)).intValue(), ((Vector)query_Rows.get(((Integer)symbolOrder.get(qVal)).intValue())).clone());
			}
			newSmf.query_Rows = newQuery_Rows;
			newSmf.symbolOrder = (HashMap)symbolOrder.clone();
			newSmf.isSymmetricMode = isSymmetricMode;
			
			// ??? TODO is this right? There may occure some really wicked effects! 
			newSmf.backedSMF = backedSMF;
			newSmf.setHasChanged(false);
			
		} catch (Exception e) {
			log.log(Level.SEVERE, "error while copying!", e);
		}

		return newSmf;
	}

	protected SMFPanel createSMFPanel() {
		return new SMFPanel_Symbol_Table(this);
	}

	@SuppressWarnings("unchecked")
	public boolean checkConsistency(Frame parent, boolean quiet) {
//		show("before consistency check");
		if (backedSMF != null) {
			log.fine("this smf is backed by another smfunction (maybe taxonomy). So leave consistency handling to our master.");
			boolean consistent = backedSMF.checkConsistency(parent, true);

			// update GUI.
	    	if (editorPanel != null) {
	    		((SMFPanel_Symbol_Table)editorPanel).refreshTable();
	    	}
	    	return consistent;
		}
		
		log.fine("checking consistency for ["+getModelInstanceName()+"], ["+getClass().getName().toUpperCase()+"].");
   	
		if (slot.getValueType() != getValueType()) {
			if (!quiet) {
				JOptionPane.showMessageDialog(parent, "This smfunction was NOT made for type ["+slot.getValueType()+"]. Please check it!") ;
			}
			return false;
		}
		
		if (slot.getAllowedValues() == null) {
			if (!quiet) {
				JOptionPane.showMessageDialog(parent, "Symbol values are not set any more. Please check it!") ;
			}
			return false;
		}
		
		Collection idleSymbols = new TreeSet();
		Collection newSymbols = new TreeSet();
		ConsistencyChecker_Symbols.findNewAndIdleSymbols(slot.getAllowedValues(), symbolOrder.keySet(), newSymbols, idleSymbols);
		
		HashMap matchingSymbols = new HashMap();
		if (!ConsistencyChecker_Symbols.matchSymbols(parent, newSymbols, idleSymbols, matchingSymbols, quiet)) {
			return false;
		}
		
		newSymbols.removeAll(matchingSymbols.keySet());
		idleSymbols.removeAll(matchingSymbols.values());
		
		//
		// insert new symbols in smf.
		//
		for (Iterator it = newSymbols.iterator(); it.hasNext();) {
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
		
    	log.fine("finished consistency check");
		
    	// update GUI.
    	if (editorPanel != null)  {  // && (!idleSymbols.isEmpty() || !newSymbols.isEmpty()))
    		((SMFPanel_Symbol_Table)editorPanel).refreshTable();
    	}

//    	show("after consistency check");

    	return true;
	}

	public static String getSMFunctionTypeName_static() {
		return "Table";
	}

	@SuppressWarnings("unchecked")
	public double getSimilarityBetween(Object query, Object cb, Explanation exp) {
		String symbolQ = (String) query;
		String symbolCb = (String) cb;

		// commented out before 20.10.2008
//		if (!symbolOrder.containsKey(symbolQ)) log.warning("Similarity Measure for ["+inst.getName()+"]: Unknown symbol ["+symbolQ+"]");
//		if (!symbolOrder.containsKey(symbolCb)) log.warning("Similarity Measure for ["+inst.getName()+"]: Unknown symbol ["+symbolCb+"]");
		
		int indexQ  = ((Integer)symbolOrder.get(symbolQ)).intValue();
		int indexCb = ((Integer)symbolOrder.get(symbolCb)).intValue();

		double sim = ((Double)((Vector)query_Rows.get(indexQ)).get(indexCb)).doubleValue();
		
		//
		// Explanation
		if (exp != null) {
			Helper.addExplanation(exp, this.inst, query, cb, this, null, sim);

			// commented out before 20.10.2008
//			String asp = getType()+getSMFunctionTypeName()+": active smf = ["+getSmfName()+"]";
////			Explanation newExp = new Explanation(asp, query, cb, this);
//			Explanation newExp = new Explanation(this.inst, query, cb, this);
//			newExp.addComment(asp, "similarity is [" + sim + "]");
//			newExp.setSimilarity(sim);
////			exp.addExplanationChild(asp, newExp);
//			exp.addExplanationChild(this.inst, newExp);
		}
		//
		
		return sim;
	}

	public ValueType getValueType() {
		return VALUE_TYPE;
	}

	public Vector<Vector<Double>> getData() {
		return query_Rows;
	}

	@SuppressWarnings("unchecked")
	public Vector getHeader() {
		Vector header = new Vector(symbolOrder.keySet());
		for (Iterator it = symbolOrder.entrySet().iterator(); it.hasNext();) {
			Entry e = (Entry)it.next();
			header.set(((Integer)e.getValue()).intValue(), e.getKey());
		}
			
		return header;
	}

	@SuppressWarnings("unchecked")
	public Double getValueAt(int rowIndex, int columnIndex) {
		Double sim = (Double)((Vector)query_Rows.get(rowIndex)).get(columnIndex);
		return sim;
	}

	@SuppressWarnings("unchecked")
	public void setValueAt(String val, int row, int column) {
		double d = Helper.parseDouble(Helper.formatDoubleAsString(val));
		((Vector)query_Rows.get(row)).set(column, new Double(d));
		if (isSymmetricMode) {
			((Vector)query_Rows.get(column)).set(row, new Double(d));
		}
		setHasChanged(true);
	}

	/**
	 * THIS IS A HACKY METHOD. THIS IS ONLY NEEDED BY SMF_SYMBOL_TAXONOMY.
	 * I INTENDED TO AVOID A LOT OF INTERFACE WORKAROUNDS.
	 * @param simVal
	 * @param symbolQuery
	 * @param symbolCase
	 */
	@SuppressWarnings("unchecked")
	public void setValueAt(double simVal, String symbolQuery, String symbolCase) {
		// NOTE:
		// this method is only needed by other classes (not by this one, and not by its editor panel!)
		//
		if (!symbolOrder.containsKey(symbolQuery) || !symbolOrder.containsKey(symbolCase)) {
			return;
		}
		
		int indexQ  = ((Integer)symbolOrder.get(symbolQuery)).intValue();
		int indexCb = ((Integer)symbolOrder.get(symbolCase)).intValue();

		((Vector)query_Rows.get(indexQ)).set(indexCb, new Double(simVal));
	}

	@SuppressWarnings("unchecked")
	public String show(String title) {
		StringBuffer sb = new StringBuffer();
		sb.append("\n   *****  show similarity table [" + title + "] *****\n");
		
		// find max symbol length
		int maxLength=0;
		for (Iterator itC = symbolOrder.keySet().iterator(); itC.hasNext();) {
			String symbolC = (String) itC.next();
			if (symbolC.length() > maxLength) {
				maxLength = symbolC.length();
			}
		}
		
		// symbol order
		Iterator itTmp = symbolOrder.keySet().iterator();
		int i = 0;
		while (itTmp.hasNext()) {
			itTmp.next();
			String tmpStr =  "---- NOT USED ---- Causes Crash!";
			for (String symbol : symbolOrder.keySet()) {
				int key = symbolOrder.get(symbol);
				if (key == i) {
					tmpStr = symbol;
					break;
				}
			}
			sb.append("" + i + ": " + tmpStr + "\n");
			i++;
		}
		
		// table header
		for (Iterator itC = symbolOrder.keySet().iterator(); itC.hasNext();) {
			String symbolC = (String) itC.next();
			sb.append(String.format("%" + maxLength + "s\t  | ",symbolC));
		}
		sb.append("\n");
		
		// content
		for (Iterator itQ = symbolOrder.keySet().iterator(); itQ.hasNext();) {
			String symbolQ = (String) itQ.next();
			for (Iterator itC = symbolOrder.keySet().iterator(); itC.hasNext();) {
				String symbolC = (String) itC.next();
				sb.append(String.format("%" + Math.max(1,maxLength-3)+".2f\t  | ",getSimilarityBetween(symbolQ, symbolC, null)));
			}	
			sb.append("\t\t" + symbolQ);
			sb.append("\n");
		}
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	public boolean setSymmetryMode(boolean symmetryMode) {
		if (!this.isSymmetricMode && symmetryMode && !isSymmetric()) {
			// we switch from asymmetric mode to symmetric mode,
			// but this smfunction is not symmetric
			if (editorPanel != null) {
				int result = JOptionPane.showConfirmDialog(editorPanel.getTopLevelAncestor(), "Function is NOT symmetric. Force it?", "SMF is not symmetric", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
				if (result == JOptionPane.NO_OPTION) {
					return false;
				}

				int size = query_Rows.size();
				for (int rowIndex=0; rowIndex<size; rowIndex++) {
					Vector row = (Vector)query_Rows.get(rowIndex);
					for (int colIndex=rowIndex+1; colIndex<size; colIndex++) {
						row.set(colIndex, getValueAt(colIndex, rowIndex));
					}
				}
				
			}
		}
		
		this.isSymmetricMode = symmetryMode; 
		setHasChanged(true);
		
		return true;
	}

	public boolean isSymmetric() {
		int size = query_Rows.size();
		
		for (int rowIndex=0; rowIndex<size; rowIndex++) {
			for (int colIndex=0; colIndex<size; colIndex++) {
				Double qval = getValueAt(rowIndex, colIndex);
				Double cbval = getValueAt(colIndex, rowIndex);
				if (!qval.equals(cbval))  {
					return false;
				}
			}
		}
		log.fine("issymmetric = [" + true + "]");
		return true;
	}

	public boolean isSymmetricMode() {
		return isSymmetricMode;
	}
	
	// commented out before 20.10.2008
//	/**
//	 * Needed by other classes (e.g. SMF_Symbol_Taxonomy)
//	 */
//	public Collection getSymbols()
//	{
//		return symbolOrder.keySet();
//	}

	public void renameSymbol(String oldSymbolName, String newSymbolName) {
		log.fine("rename symbol: [" + oldSymbolName + "] -> [" + newSymbolName + "]");

		Object mapValue = symbolOrder.get(oldSymbolName);
		symbolOrder.remove(oldSymbolName);
		symbolOrder.put(newSymbolName, (Integer)mapValue);
	}

	@SuppressWarnings("unchecked")
	protected void insertNewSymbol(String newSymbol) {
		if (symbolOrder.containsKey(newSymbol)) {
			log.info("already contains symbol");
			return;
		}
		
		int oldSize = symbolOrder.size();
    	
		// register new value in symbol order
        int newIndex = oldSize;
        symbolOrder.put(newSymbol, new Integer(newIndex));
        
        // add default similarity values in table
        Double doubleZero = new Double(0);
        Vector newRow = new Vector(oldSize+1);
        for (int i=0; i<oldSize; i++) {
        	// query values (new row)
        	newRow.add(doubleZero);
        	
        	// append new value to each row (symbol as cb value)
        	((Vector)query_Rows.get(i)).add(doubleZero);
        }
        // add reflexive similarity
        newRow.add(new Double(1));
        query_Rows.add(newRow);
        
        setHasChanged(true);
	}
	
	@SuppressWarnings("unchecked")
	protected void removeOldSymbol(String oldSymbol) {
		log.fine("transient: remove old symbol [" + oldSymbol + "]");

		// get index in symbol order
		int index = ((Integer)symbolOrder.get(oldSymbol)).intValue();
		log.fine("delete symbol [" + oldSymbol + "] index = [" + index + "]");
		
		// first step: remove symbol from symbol order
    	// decrease all indices larger than index
    	HashMap newOrder = new HashMap();
    	for (Iterator itSymbol=symbolOrder.entrySet().iterator(); itSymbol.hasNext();) {
    		Entry e = (Entry)itSymbol.next();
    		Integer currentIndex = (Integer)e.getValue();
    		Integer newIndex = null;
    		if (currentIndex.intValue() > index) {
    			newIndex = new Integer(currentIndex.intValue()-1);
    		} else {
    			newIndex = currentIndex;
    		}
    		newOrder.put(e.getKey(), newIndex);
    	}
    	newOrder.remove(oldSymbol);
    	log.fine("\nOld symbol order: " + symbolOrder + "\nNew symbol order: " + newOrder);
    	symbolOrder = newOrder;
		
    	// second step: 
    	// remove cb value in each row. (we delete one whole column)
    	for (Iterator rowIterator = query_Rows.iterator(); rowIterator.hasNext();) {
    		Vector row = (Vector)rowIterator.next();
    		row.remove(index);
    	}
    	// remove all entries where symbol is a query (we delete one whole row)
    	query_Rows.remove(index);
	}

	@SuppressWarnings("unchecked")
	public final HashMap getSymbolOrder() {
		return symbolOrder;
	}

	/**
	 * This method will be used by SMF_Symbol_Taxonomy.
	 * @param backedSMF
	 */
	protected void setBackedSMF(AbstractSMFunction backedSMF) {
		this.backedSMF = backedSMF;
	}
	
	/**
	 * Maybe this table is backed by a taxonomy or an ordered smf.
	 * This is the getter for this 'backed' smf.
	 * @return EditorSMFunction backed smf. Maybe null.
	 */
	public AbstractSMFunction getBackedSMF() {
		return backedSMF;
	}

	@Override
	public void initExplanationContainer() {
		explanationContainer = new SMExplanationContainer(getValueType());
	}
	
	@Override
	public void initExplanationContainer(Element element) {
		explanationContainer = new SMExplanationContainer(getValueType());
		explanationContainer.init(element);
	}
}
