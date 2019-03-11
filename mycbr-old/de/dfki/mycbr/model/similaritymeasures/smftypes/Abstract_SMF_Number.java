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

import org.jdom.Element;
import org.jdom.JDOMException;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.Widget_DiffOrQuotient.DiffOrQuotientModeListener;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.model.vocabulary.ModelSlot;

/**
 * @author myCBR Team
 * This class contains common functionality of SMF_Integer_Standard and 
 * SMF_Float_Standard. We cannot share much code though, because Java has
 * no templates. 
 * 
 */
public abstract class Abstract_SMF_Number extends AbstractSMFunction implements DiffOrQuotientModeListener  {

	private static final long serialVersionUID = 1L;
	public static final String XML_ATT_MINVAL 					= "minval"; 
	public static final String XML_ATT_MAXVAL 					= "maxval"; 
	public static final String XML_ATT_MODE_DIFF_OR_QUOTIENT 	= "modeDiffOrQuotient"; 
	
	public static final int MODE_DIFFERENCE 	= 0;
	public static final int MODE_QUOTIENT 		= 1;
	
	protected boolean symmetryMode = false;
	protected int diffOrQuotientMode = MODE_DIFFERENCE;

	protected ModelSlot slot;
	/** allowed minimum value defined for this slot */
	protected double minValue;
	/** allowed maximum value defined for this slot */
	protected double maxValue;
	/** difference (maxValue-minValue) */
	protected double diff;
	
	public Abstract_SMF_Number(ModelInstance inst, String smfName) throws Exception {
		super(inst, smfName);
		slot = (ModelSlot) inst;
		
		// set value range
		minValue = slot.getMinimumValue().doubleValue();
		maxValue = slot.getMaximumValue().doubleValue();
		diff = maxValue - minValue;
	}
	
	public Abstract_SMF_Number(ModelInstance inst, Element smfElement) throws JDOMException {
		super(inst, smfElement);
		slot = (ModelSlot) inst;
		
		// set value range
		maxValue = smfElement.getAttribute(XML_ATT_MAXVAL).getDoubleValue();
		minValue = smfElement.getAttribute(XML_ATT_MINVAL).getDoubleValue();
		diff = maxValue - minValue;
		if (smfElement.getAttribute(XML_ATT_MODE_DIFF_OR_QUOTIENT)!= null) {
			diffOrQuotientMode=smfElement.getAttribute(XML_ATT_MODE_DIFF_OR_QUOTIENT).getIntValue();
		}
	}
	
	public boolean checkQuotientModeAllowed() {
		return checkQuotientModeAllowed(minValue, maxValue);
	}
	
	public boolean checkQuotientModeAllowed(double minval, double maxval) {
		if (minval<=0 || maxval<=0 || (maxval-minval)<=0) {
			return false;
		}
		return true;
	}
	
	public boolean isIntegerMode() {
		return getValueType()==ValueType.INTEGER;
	}
	
	public double roundIfIntegerMode(double d) {
		if (isIntegerMode()) {
			d = new Long(Math.round(d)).doubleValue();
		}
		return d;
	}
	
	public double roundIfIntegerAndDiffMode(double d) {
		if (isIntegerMode() && diffOrQuotientMode==MODE_DIFFERENCE) {
			d = new Long(Math.round(d)).doubleValue();
		}
		return d;
	}
	
	/**
	 * Calculates a plausible quotient from a difference.
	 * In other words, it transforms a value resulted from c-q into the corresponding value c/q.
	 * In general, such a calculation is not possible. One needs another assumption to do so:
	 * if quotient is smaller than one, we assume q=maxValue -> c = quotient * maxValue. 
	 * if quotient is greater than one, we assume c=maxValue -> q = maxValue / quotient.
	 * This enables deterministic transformations. It causes no pitfalls, since all calculations
	 * based on this assumption are used for uncritical heuristics and belong to the design of this app. ;)  
	 * 
	 * E.g. used for interpolation between sampling points.
	 * @param quotient a certain c/q ratio 
	 * @return double a difference c-q.
	 */
	public double getDifferenceForQuotient(double quotient) {
		double x = 0d;
		if (quotient >= 1d) {
			x = ((quotient-1d)*maxValue)/quotient;
		}
		else {
			x = -((1-quotient)*maxValue);
		}
		return Helper.parseDouble(Helper.formatDoubleAsString(x));
	}
	
	/**
	 * Inverse function of getDifferenceForQuotient().
	 * @param difference a certain c-q difference
	 * @return double a q/c ratio.
	 */
	public double getQuotientForDifference(double difference) {
		double x = 0d;
		if (difference > 0d) {
			x = maxValue/(maxValue-difference);
		}
		else {
			x = (difference+maxValue)/maxValue;
		}
		return Helper.parseDouble(Helper.formatDoubleAsString(x));
	}

	protected double getQuotientForDifference(double minValue, double maxValue, double difference) {
		double x = 0d;
		if (difference > 0d) {
			x = maxValue/(maxValue-difference);
		}
		else {
			x = (difference+maxValue)/maxValue;
		}
		return Helper.parseDouble(Helper.formatDoubleAsString(x));
	}
	
	
	
	public int getDiffOrQuotientMode() {
		return diffOrQuotientMode;
	}

	public boolean isSymmetryMode() {
		return symmetryMode;
	}

	/**
	 * Getter for difference maxValue - minValue
	 * @return int diff = maxValue - minValue 
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

	public void toXML(Element xmlElement) {
		xmlElement.setAttribute(XML_ATT_MAXVAL, Double.toString(maxValue));
		xmlElement.setAttribute(XML_ATT_MINVAL, Double.toString(minValue));
		xmlElement.setAttribute(XML_ATT_MODE_DIFF_OR_QUOTIENT, Integer.toString(diffOrQuotientMode));
	}
	
	
	/**
	 * An SMF_Number must be able to manage both value types ValueType.INTEGER and ValueType.FLOAT
	 * All necessary modifications must be done by this method, as soon as the value type of the slot changes.
	 * @param newVT ValueType either ValueType.INTEGER or ValueType.FLOAT
	 */
	public abstract void changeValueType(ValueType newVT);
	
	// commented out before 20.10.2008
	//public abstract boolean checkConsistency(Frame parent);
	//public abstract double getSimilarityBetween(Object query, Object cb, Explanation exp);
	//protected abstract SMFPanel createSMFPanel();
	//public abstract String getSMFunctionTypeName();
	//public abstract ValueType getType();
}
