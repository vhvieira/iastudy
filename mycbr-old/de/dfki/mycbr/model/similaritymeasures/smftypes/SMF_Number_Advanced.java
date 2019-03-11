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
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.XMLConstants;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel_Number_Advanced;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.Widget_Symmetry.SymmetryModeListener;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.retrieval.Explanation;

/**
 * @author myCBR Team
 *
 */
public class SMF_Number_Advanced extends Abstract_SMF_Number implements SymmetryModeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger ( SMF_Number_Advanced.class.getName ( ) );
	
	public static final String XML_TAG_SAMPLINGPOINT = "SamplingPoint";
	public static final String XML_ATT_XVALUE = "xValue";
	public static final String XML_ATT_YVALUE = "yValue";
	
	private ValueType currentValueType;
	
	private TreeMap<Double, Double> samplingPoints = new TreeMap<Double, Double>();

	
	@SuppressWarnings("unchecked")
	public SMF_Number_Advanced(ModelInstance inst, Element smfElement) throws JDOMException {
		super(inst, smfElement);
		
		currentValueType = ValueType.getValueType(Helper.decode(smfElement.getAttributeValue(XMLConstants.XML_ATT_TYPE)));
		
		// read sampling points
		for (Iterator it=smfElement.getDescendants(new ElementFilter(XML_TAG_SAMPLINGPOINT)); it.hasNext();) {
			Element spElement = (Element)it.next();
			Double key = spElement.getAttribute(XML_ATT_XVALUE).getDoubleValue();
			Double val  = spElement.getAttribute(XML_ATT_YVALUE).getDoubleValue();
			samplingPoints.put(key, val);
		}
		
		symmetryMode = isSymmetric();
		log.fine("smfunction is symmetric = ["+symmetryMode+"]");
	}
	
	public SMF_Number_Advanced (ModelInstance inst, String smfName) throws Exception {
		super(inst, smfName);
		currentValueType = slot.getValueType();
		
		if (slot.getMinimumValue()==null || slot.getMaximumValue()==null) {
			// throw exception
			log.fine("throw exception: max / min values are not set for this slot!");
			throw new Exception("max/min values are not set for this slot.");
		}
		
		// initialize default sampling points
		samplingPoints.put(new Double(-diff), 	new Double(0));
		samplingPoints.put(new Double(diff), 	new Double(0));
		samplingPoints.put(new Double(0), 		new Double(1));
	}

	public boolean isSymmetric() {
		Vector<Entry<Double,Double>> xVals = new Vector<Entry<Double,Double>>(samplingPoints.entrySet());
		
		int cnt = xVals.size()/2;
		int j=xVals.size()-1;
		for (int i=0; i<cnt; i++) {
			Entry<Double,Double> eLeft  = xVals.get(i);
			Entry<Double,Double> eRight = xVals.get(j--);
			
			double iLeft  = eLeft.getKey();
			double iRight = eRight.getKey();
			
			if (Math.abs(iLeft)!= Math.abs(iRight)) {
				return false;
			}
			
			double symLeft  = ((Double)eLeft.getValue()).doubleValue();
			double symRight = ((Double)eRight.getValue()).doubleValue();
			
			if (symLeft != symRight) {
				return false;
			}
		}
		
		return true;
	}

	
	public void toXML(Element xmlElement) {
		super.toXML(xmlElement);

		// save sampling points
		for (Iterator<Entry<Double,Double>> it=samplingPoints.entrySet().iterator(); it.hasNext();) {
			Entry<Double,Double> e = it.next();
			Element spElement = new Element(XML_TAG_SAMPLINGPOINT);
			spElement.setAttribute(XML_ATT_XVALUE, Double.toString(e.getKey()));
			spElement.setAttribute(XML_ATT_YVALUE, Double.toString(e.getValue()));
			
			xmlElement.addContent(spElement);
		}
	}

	/**
	 * copies this similarity function object.
	 * It copies the TreeMap sampling points (all containing objects are made fresh).
	 * 
	 */
	public AbstractSMFunction copy() {
		SMF_Number_Advanced newSmf = null;
		
		try {
			newSmf = new SMF_Number_Advanced(inst, smfName);

			// copy sampling points
			newSmf.samplingPoints.clear();
			for (Iterator<Entry<Double,Double>> it=samplingPoints.entrySet().iterator(); it.hasNext();) {
				Entry<Double,Double> e = it.next();
				newSmf.samplingPoints.put(e.getKey(), e.getValue());
			}
			
			newSmf.maxValue = maxValue;
			newSmf.minValue = minValue;
			newSmf.diff = diff;
			newSmf.symmetryMode = symmetryMode;
			newSmf.diffOrQuotientMode = diffOrQuotientMode;
			newSmf.currentValueType = currentValueType;
			
			newSmf.setHasChanged(false);
		} catch (Exception e) {
			log.log(Level.SEVERE, "error while copying", e);
		}

		return newSmf;
	}

	/**
	 * returns a new editor panel.
	 */
	protected SMFPanel createSMFPanel() {
		// initialize a new editor panel representing this smfunction object.
		return new SMFPanel_Number_Advanced(this);
	}

	/**
	 * Check consistency to Slot. 
	 * If something doesn't match prompt user for some options and make it consistent.
	 * @return boolean isConsistent
	 */
	public boolean checkConsistency(Frame parent, boolean quiet) {
		log.fine("checking consistency for ["+getClass().getName().toUpperCase()+"].");
		
		if (slot.getValueType() != getValueType()) {
			if (!quiet) {
				JOptionPane.showMessageDialog(parent, "This smfunction was NOT made for type ["+slot.getValueType()+"]. Please check it!") ;
			}
			return false;
		}
		if ((slot.getMaximumValue()== null || slot.getMinimumValue()==null)) {
			if (!quiet) {
				JOptionPane.showMessageDialog(parent, "Min/Max values are not set any more. Please check it!") ;
			}
			return false;
		}
		
		// get the new/current range of the slot
		double newMaxValue = slot.getMaximumValue().doubleValue();
		double newMinValue = slot.getMinimumValue().doubleValue();
		if (newMinValue > newMaxValue) {
			log.warning("Slot [" + slot.getName() + "]: minVal > maxVal. No Update on similarity measure.");
			return false;
		}
		
		if (!checkQuotientModeAllowed(newMinValue, newMaxValue) && getDiffOrQuotientMode()==MODE_QUOTIENT) {
			setDiffOrQuotientMode(MODE_DIFFERENCE);
		}
		
		log.fine("new maxVal = [" + newMaxValue + "], new minVal = [" + newMinValue + "]");

		if (this.maxValue!= newMaxValue || this.minValue != newMinValue) {
    		log.fine("inconsistent : smf [" + getSmfName() + "]  to slot [" + getModelInstanceName() + "].");
    		show();
    		
    		int result = JOptionPane.YES_OPTION;
    		if (!quiet && samplingPoints.size()>3) {
    			result = JOptionPane.showConfirmDialog(parent, "The range of this function has changed. Do you want to spread the sampling points proportionally along the x-axis?", "rescale sampling points?", JOptionPane.YES_NO_OPTION);
    		}
    		
    		resetMinMax(newMinValue, newMaxValue, (result == JOptionPane.YES_OPTION));
		}
		return true;
	}

	/**
	 * Sets new minimum and maximum values (range for query and case values).
	 * Thereby, it re-organizes its sampling points.
	 * It ensures that there are sampling points at -diff and diff.
	 * @param newMinValue double new minimum value
	 * @param newMaxValue double new maximum value
	 * @param proportional boolean if true, it spreads all sampling points proportionally, otherwise they keep their absolute x-position.
	 */
	private void resetMinMax(double newMinValue, double newMaxValue, boolean proportional) {
		double newDiff = newMaxValue-newMinValue;
		double oldDiff = diff;
		
		// create new Map
		if (proportional) {
			TreeMap<Double,Double> newSamplingPoints = new TreeMap<Double,Double>();

			double ratio = newDiff / oldDiff;

			for (Iterator<Entry<Double,Double>> it = samplingPoints.entrySet().iterator(); it.hasNext();) {
				Entry<Double,Double> e = it.next();
				double key = e.getKey();
				
				if (getDiffOrQuotientMode()==MODE_QUOTIENT) {
					key = getDifferenceForQuotient(key);
				}
				
				double newKey = roundIfIntegerMode(key * ratio);
				if (key == oldDiff) {
					newKey=newDiff;
				}
				if (key == -oldDiff) {
					newKey=-newDiff;
				}
				
				if (getDiffOrQuotientMode() == MODE_QUOTIENT) {
					newKey = getQuotientForDifference(newMinValue, newMaxValue, newKey);
				}
				newSamplingPoints.put(newKey, e.getValue());
			}
			samplingPoints = newSamplingPoints;

		} else {
			// just reset the border sampling points
			if (getDiffOrQuotientMode() == MODE_DIFFERENCE) {
    			samplingPoints.put(newDiff, samplingPoints.get(diff));
    			samplingPoints.put(-newDiff, samplingPoints.get(-diff));
    			samplingPoints.remove(diff);
    			samplingPoints.remove(-diff);
    			
    			// and remove the sampling points beyond the range
    			Iterator<Double> it = samplingPoints.keySet().iterator();
    			while(it.hasNext()) {
    				Double key = it.next();
    				if (Math.abs(key) > newDiff) {
    					it.remove();
    				}
    			}
			} else {
				double tmpDiff = getQuotientForDifference(diff);
				double tmpNewDiff = getQuotientForDifference(newDiff);
    			samplingPoints.put(tmpNewDiff, samplingPoints.get(tmpDiff));
    			samplingPoints.put(1d/tmpNewDiff, samplingPoints.get(1d/tmpDiff));
    			samplingPoints.remove(tmpDiff);
    			samplingPoints.remove(1d/tmpDiff);
    			
    			// and remove the sampling points beyond the range
    			Iterator<Double> it=samplingPoints.keySet().iterator();
    			while(it.hasNext()) {
    				Double key = it.next();
    				if (key < 1d/tmpNewDiff || key > tmpNewDiff) {
    					it.remove();
    				}
    			}
			}
		}
			
		maxValue = newMaxValue;
		minValue = newMinValue;
		diff = maxValue-minValue;
		setHasChanged(true);
		log.fine("finished consistency check.");
	}
	
	
	/**
	 * Value type is INTEGER.
	 */
	public ValueType getValueType() {
		return currentValueType;
	}

	/**
	 * Name of this smfunction is "Advanced" because it describes the advanced mode we know from old software
	 * Cbrworks.
	 */
	public static String getSMFunctionTypeName_static() {
		return "Advanced";
	}

	/**
	 * sets a sampling point at x= dist and y= sim.  
	 * @param x Integer xvalue for sampling point.
	 * @param sim Double similarity value (should be element of [0,1]). 
	 */
	public boolean setSamplingPoint(double x, Double sim) {
		double tmpDist = (getDiffOrQuotientMode() == MODE_QUOTIENT ? getDifferenceForQuotient(x) : x);
		if (Math.abs(tmpDist) > diff) {
			log.fine("value [" + x + "] out of range [" + (-diff) + "," + diff + "].");
			return false;
		}
		
		x = Helper.parseDouble(Helper.formatDoubleAsString(x));
		
		log.fine("putting [(" + x + "," + sim + ")] to smf");
		samplingPoints.put(x, sim);

		if (isSymmetryMode()) {
			// mirror this point
			x = (diffOrQuotientMode==MODE_DIFFERENCE? -x: 1d/x);
			log.fine("symmetric mode --> set mirror value x=[" + x + "], y=[" + sim + "]");
			samplingPoints.put(x, sim);
		}
		
		setHasChanged(true);
		return true;
	}


	/**
	 * NOTE: This returns the original samplingPoint object. Changes to it will effect this smfunction.
	 * Please use this method for read-only purposes only.
	 * @return TreeMap original samplingPoints object. (Key is Double, value is Double).
	 */
	public TreeMap<Double,Double> getSamplingPoints() {
		return samplingPoints;
	}

	/**
	 * removes the sampling point at x position xVal.
	 * @param xVal Integer the given x value where to remove the sampling point.
	 */
	public void removeSamplingPoint(double xVal) {
		samplingPoints.remove(xVal);
		if (symmetryMode) {
			xVal = (diffOrQuotientMode == MODE_DIFFERENCE ? -xVal : 1d/xVal);
			samplingPoints.remove(xVal);
		}
		setHasChanged(true);
	}

	/**
	 * Getter for difference maxValue - minValue
	 * @return double diff = maxValue - minValue 
	 */
	public double getDiff() {
		return diff;
	}

	public double getMinValue() {
		return minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public double getSimilarityBetween(Object query, Object cb, Explanation exp) {

		Double qNumber = Double.parseDouble(query.toString());

		double d = 0;
		if (diffOrQuotientMode == MODE_DIFFERENCE) {
			d = Double.parseDouble(cb.toString()) - qNumber;
		} else {  // MODE_QUOTIENT
			if (qNumber == 0) {
				return Double.NaN;
			}
			d = Double.parseDouble(cb.toString()) / (double) qNumber;
		}
		
		// maybe we hit a sampling point
		Double result = (Double) samplingPoints.get(new Double(d));
		if (result != null) {
			Helper.addExplanation(exp, this.inst, query, cb, this, null, result.doubleValue());
			return result.doubleValue();
		}
		
		// d lies between two sampling points. Get them...
		Double lsp = null;
		Double rsp = null;

		Iterator<Double> it = samplingPoints.keySet().iterator();

		// commented out before 20.10.2008
		// maybe this cannot happen, but what if we have no sampling points?
//        if (!it.hasNext()) {
//    			Helper.addExplanation(exp, this.inst, query, cb, this, null, -Double.MIN_VALUE);
//        	return -Double.MIN_VALUE;        	
//        }
		
		do {
			rsp = it.next();
			if (rsp.doubleValue() > d) {
				break;
			}
			lsp = rsp;
		} while (it.hasNext());

		// now interpolate (linear)
		double lspVal = (lsp != null? lsp.doubleValue() : 0);
		double ld = ((Double) samplingPoints.get(lsp)).doubleValue();
		double rd = ((Double) samplingPoints.get(rsp)).doubleValue();

		double res = 0d;

		if (diffOrQuotientMode == MODE_DIFFERENCE) {
	        double x = d - lspVal;
	        double width = rsp.doubleValue() - lspVal;
			res = ld + (x * (rd - ld)) / width;
		} else {  // MODE_QUOTIENT
			double x = getDifferenceForQuotient(d)/diff;
			double lspx = getDifferenceForQuotient(lsp)/diff;
			double rspx = getDifferenceForQuotient(rsp)/diff;
			
	        //double width = rsp.doubleValue() - lspVal;
			res = ld + ((x-lspx) * (rd - ld)) / (rspx-lspx);			
		}

		//
		// Explanation
		if (exp != null) {
			Helper.addExplanation(exp, this.inst, query, cb, this, null, res);
			String provenance = slot.getValueType() + "_" + getSMFunctionTypeName() + "_" + getSmfName();
			String txt = (diffOrQuotientMode == MODE_DIFFERENCE ? cb.toString() + " - " + qNumber + " = " + d : cb.toString() + " / " + qNumber + " = " + d);
			exp.addComment(provenance, txt +  " = " + d);
		}
		//
		
        return res;
	}
	
	private void show() {
		Element e = new Element("SHOW");
		toXML(e);
		XMLOutputter xmlOutputter = new XMLOutputter();
		xmlOutputter.setFormat(Format.getPrettyFormat());
		log.fine("\n"+xmlOutputter.outputString(e));
	}


	/**
	 * Implemented by Interface SymmetryListener
	 */
	public boolean setSymmetryMode(boolean symmetryMode) {
		if (!this.symmetryMode && symmetryMode && !isSymmetric()) {
			// we switch from asymmetric mode to symmetric mode,
			// but this smfunction is not symmetric
			if (editorPanel != null) {
				String[] options = new String[]{"Copy Left to Right", "Copy Right to Left", "Cancel"};
				int result = JOptionPane.showOptionDialog(editorPanel.getTopLevelAncestor(), "Function is NOT symmetric. These are your options:", "SMF is not symmetric", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
				if (result == 2) {
					return false;
				}
				
				Vector<Double> keys = new Vector<Double>(samplingPoints.keySet());
				int size = keys.size();

				// remove idle side
				for (int i=0; i<size; i++) {
					Double key = keys.get(i);
					Double tmpKey = (getDiffOrQuotientMode() == MODE_QUOTIENT ? getDifferenceForQuotient(key) : key);
					if ((tmpKey.intValue()>0 && result==0) || (tmpKey.intValue()<0 && result==1)) {
						samplingPoints.remove(key);
					}
				}
				
				// now copy left/right to right/left
				for (int i=0; i<size; i++) {
					Double key = (Double)keys.get(i);
					if ((key.intValue()<0 && result==0) || (key.intValue()>0 && result==1)) {
						Double simVal = samplingPoints.get(key);
						key = (getDiffOrQuotientMode()==MODE_QUOTIENT? 1d/key: -key.doubleValue());
						samplingPoints.put(key, simVal);
					}
				}
			}
		}
		
		this.symmetryMode = symmetryMode; 
		setHasChanged(true);
		
		return true;
	}

	/**
	 * Changes the similarity calculation mode.
	 * Diff mode calculates c-q, while quotient mode calculates c/q.
	 * A semantically correct transformation between both modes is not possible.
	 * Though, we want to keep the "shape" of the SMF.
	 * The sampling points must be transformed. 
	 */
	public boolean setDiffOrQuotientMode(int diffOrQuotientMode) {
		if (this.diffOrQuotientMode == diffOrQuotientMode) {
			return true;
		}
		this.diffOrQuotientMode = diffOrQuotientMode;
		
		// re-organize sampling points
		TreeMap<Double, Double> newSamplingPoints = new TreeMap<Double, Double>();
		if (diffOrQuotientMode == MODE_QUOTIENT) {
			for (Entry<Double,Double> e : samplingPoints.entrySet()) {
				Double xPos = e.getKey();
				double newPos = getQuotientForDifference(xPos);
				if (xPos == diff)  {
					newPos = maxValue/minValue; // avoid rounding errors at the boarder SPs
				}
				if (xPos == -diff) {
					newPos = minValue/maxValue;
				}
				newSamplingPoints.put(newPos, e.getValue());
			}
		} else {
			for (Entry<Double,Double> e : samplingPoints.entrySet()) {
				Double xPos = e.getKey();
				double newPos = roundIfIntegerMode(getDifferenceForQuotient(xPos));
				if (xPos == maxValue/minValue) {
					newPos = diff; // avoid rounding errors at the boarder SPs
				}
				if (xPos == minValue/maxValue) {
					newPos = -diff;
				}
				newSamplingPoints.put(newPos, e.getValue());
			}
		}
		
		samplingPoints = newSamplingPoints;
		setHasChanged(true);
		return true;
	}

	public void changeValueType(ValueType newValueType) {
		currentValueType = newValueType;
		resetMinMax(slot.getMinimumValue().doubleValue(), slot.getMaximumValue().doubleValue(), true);
		setHasChanged(true);
	}
	
}
