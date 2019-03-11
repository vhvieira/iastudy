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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.XMLConstants;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel_Number_Std;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.Widget_Symmetry.SymmetryModeListener;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.retrieval.Explanation;

/**
 * 
 * @author myCBR Team
 */
public class SMF_Number_Std extends Abstract_SMF_Number implements SymmetryModeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger ( SMF_Number_Std.class.getName ( ) );

	public static final String XML_TAG_LEFT_SIDE  = "LeftSide";
	public static final String XML_TAG_RIGHT_SIDE = "RightSide";
	public static final String XML_ATT_FCT_MODE   = "fctMode";
	public static final String XML_ATT_MINVAL = "minval"; 
	public static final String XML_ATT_MAXVAL = "maxval"; 
	
	public static final int FCT_STEP 		= 0;
	public static final int FCT_POLYNOMIAL 	= 1;
	public static final int FCT_SMOOTH_STEP = 2;
	public static final int FCT_CONST 		= 3;
	public static final int FCT_MODES = 4; // number of different modes that are available

	public static final String FCT_STEP_STR 		= "step";
	public static final String FCT_POLYNOMIAL_STR 	= "polinomial";
	public static final String FCT_SMOOTH_STEP_STR  = "smooth_step";
	public static final String FCT_CONST_STR 		= "const";
	
	public static String fctToString(int fct) {
		final String[] lookup = {FCT_STEP_STR, FCT_POLYNOMIAL_STR, 
								FCT_SMOOTH_STEP_STR, FCT_CONST_STR};
		return lookup[fct];
	}
	
	public static int stringToFct(String fct) {
		if (fct.equals(FCT_STEP_STR)) return FCT_STEP;
		if (fct.equals(FCT_POLYNOMIAL_STR)) return FCT_POLYNOMIAL;
		if (fct.equals(FCT_SMOOTH_STEP_STR)) return FCT_SMOOTH_STEP;
		if (fct.equals(FCT_CONST_STR)) return FCT_CONST;
		return -1;
	}
	
	protected int currentFctLeft  = FCT_POLYNOMIAL;
	protected int currentFctRight = FCT_POLYNOMIAL;

	
	public ValueType currentValueType;

	/** maps function keys (int) to its value (double). left side of editor. */
	private double[] fctValsLeft  = new double[FCT_MODES];
	/** maps function keys (String) to its value (double). right side of editor. */
	private double[] fctValsRight = new double[FCT_MODES];
	
	
	public SMF_Number_Std(ModelInstance inst, Element smfElement) throws JDOMException {
		super(inst, smfElement);
		currentValueType = ValueType.getValueType(Helper.decode(smfElement.getAttributeValue(XMLConstants.XML_ATT_TYPE)));

		// read sampling points
		Element leftSide = (Element)smfElement.getDescendants(new ElementFilter(XML_TAG_LEFT_SIDE)).next();
		for (int i = 0; i < FCT_MODES; i++) {
			fctValsLeft[i] = leftSide.getAttribute(fctToString(i)).getDoubleValue();
		}
		currentFctLeft = stringToFct(leftSide.getAttributeValue(XML_ATT_FCT_MODE));
		
		Element rightSide = (Element)smfElement.getDescendants(new ElementFilter(XML_TAG_RIGHT_SIDE)).next();
		for (int i = 0; i < FCT_MODES; i++) {
			fctValsRight[i] = rightSide.getAttribute(fctToString(i)).getDoubleValue();
		}
		
		currentFctRight = stringToFct(rightSide.getAttributeValue(XML_ATT_FCT_MODE));
		
		symmetryMode = isSymmetric();
		log.fine("smfunction is symmetric = [" + symmetryMode + "]");
	}
	

	public SMF_Number_Std(ModelInstance inst, String smfName) throws Exception {
		super(inst, smfName);
		currentValueType = slot.getValueType();
		
		// initialize maps
		fctValsLeft[FCT_STEP] = 0;
		fctValsLeft[FCT_POLYNOMIAL] = 1;
		fctValsLeft[FCT_SMOOTH_STEP] = diff/2;
		fctValsLeft[FCT_CONST] = 1;
		
		fctValsRight = (double[])fctValsLeft.clone();
		
		currentFctLeft  = FCT_POLYNOMIAL;
		currentFctRight = FCT_POLYNOMIAL;
		
		symmetryMode = false;
	}
	
	public boolean isSymmetric() {
		if (currentFctLeft != currentFctRight) {
			return false;
		}
		for (int i = 0; i < FCT_MODES; i++) {
			if (fctValsLeft[i] != fctValsRight[i]) {
				return false;
			}
		}
		return true;	
	}
	
	public void toXML(Element xmlElement) {
		super.toXML(xmlElement);

		Element leftElement = new Element(XML_TAG_LEFT_SIDE);
		leftElement.setAttribute(FCT_STEP_STR, 			Double.toString(fctValsLeft[FCT_STEP]));
		leftElement.setAttribute(FCT_POLYNOMIAL_STR, 	Double.toString(fctValsLeft[FCT_POLYNOMIAL]));
		leftElement.setAttribute(FCT_SMOOTH_STEP_STR, 	Double.toString(fctValsLeft[FCT_SMOOTH_STEP]));
		leftElement.setAttribute(FCT_CONST_STR, 		Double.toString(fctValsLeft[FCT_CONST]));
		leftElement.setAttribute(XML_ATT_FCT_MODE, 		Helper.encode(fctToString(currentFctLeft)));
		xmlElement.addContent(leftElement);
		
		Element rightElement = new Element(XML_TAG_RIGHT_SIDE);
		rightElement.setAttribute(FCT_STEP_STR, 		Double.toString(fctValsRight[FCT_STEP]));
		rightElement.setAttribute(FCT_POLYNOMIAL_STR, 	Double.toString(fctValsRight[FCT_POLYNOMIAL]));
		rightElement.setAttribute(FCT_SMOOTH_STEP_STR, 	Double.toString(fctValsRight[FCT_SMOOTH_STEP]));
		rightElement.setAttribute(FCT_CONST_STR, 		Double.toString(fctValsRight[FCT_CONST]));
		rightElement.setAttribute(XML_ATT_FCT_MODE, 	Helper.encode(fctToString(currentFctRight)));
		xmlElement.addContent(rightElement);
	}

	public AbstractSMFunction copy() {
		SMF_Number_Std newSmf = null;
		
		try {
			newSmf = new SMF_Number_Std(inst, smfName);

			// clone HashMaps
			newSmf.fctValsLeft  = (double[])fctValsLeft.clone();
			newSmf.fctValsRight = (double[])fctValsRight.clone();
			newSmf.symmetryMode = symmetryMode;
			newSmf.currentFctLeft  = currentFctLeft;
			newSmf.currentFctRight = currentFctRight;
			
			newSmf.setHasChanged(false);
			
		} catch (Exception e) {
			log.log(Level.SEVERE, "error while copying!", e);
		}
		return newSmf;
	}

	protected SMFPanel createSMFPanel() {
		return new SMFPanel_Number_Std(this);
	}

	public boolean checkConsistency(Frame parent, boolean quiet) {
		log.fine("checking consistency for [" + getClass().getName().toUpperCase() + "].");
		
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
		double newMinValue = slot.getMinimumValue().doubleValue();
		double newMaxValue = slot.getMaximumValue().doubleValue();
		if (newMinValue > newMaxValue)  {
			log.warning("Slot [" + slot.getName() + "]: minVal > maxVal. No Update on similarity measure.");
			return false;
		} 
		if (!checkQuotientModeAllowed(newMinValue, newMaxValue) && getDiffOrQuotientMode()==MODE_QUOTIENT) {
			setDiffOrQuotientMode(MODE_DIFFERENCE);
		}

		log.fine("new maxVal = [" + newMaxValue + "], new minVal = [" + newMinValue + "]");
		
		if (newMinValue==minValue && newMaxValue==maxValue) {
			return true;
		}

		minValue = newMinValue;
		maxValue = newMaxValue;
		diff = maxValue - minValue;
		
		setHasChanged(true);
		return true;
	}

	public static String getSMFunctionTypeName_static() {
		return "Standard";
	}

	public double getSimilarityBetween(Object query, Object cb, Explanation exp) {
		double q = ((Number)query).doubleValue();
		double c = ((Number)cb).doubleValue();
		
		double d = 0;
		if (diffOrQuotientMode == MODE_DIFFERENCE) {
			d = c-q;
		} else {  // MODE_QUOTIENT
			if (q == 0) {
				return Double.NaN;
			}
			d= getDifferenceForQuotient(c/q);
		}

        if (d > diff) {
        	d = diff; 
        } else if (d < -diff) {
        	d =- diff;
        }
		double result = 0.1;
		double[] usedMap = null;

		if (d < 0) {
			// left side
			usedMap = fctValsLeft;
			
			if (currentFctLeft == FCT_STEP) {
				double step = usedMap[currentFctLeft];
				if (getDiffOrQuotientMode()==MODE_QUOTIENT) {
					step = -getDifferenceForQuotient(step);
				}
				if (d < -step) {
					result = 0;
				} else {
					result = 1;
				}
			} else if (currentFctLeft == FCT_POLYNOMIAL) {
				double power = usedMap[currentFctLeft];
				double ratio = ((double)Math.abs(d))/((double)diff);
				double x = 1 - ratio;
				result = Math.pow(x, power);
			} else if (currentFctLeft == FCT_SMOOTH_STEP) {
				double delta = Math.abs(d);
				double threshhold = usedMap[currentFctLeft];
				if (getDiffOrQuotientMode() == MODE_QUOTIENT) {
					threshhold = -getDifferenceForQuotient(threshhold);
				}
				
				double alpha = getDiff()/100;//1;
				double ratio = (((double)delta)-(threshhold)) / alpha;
				double divider = Math.exp(ratio)+1;
				result = ((double)1)/divider;
			} else if (currentFctLeft == FCT_CONST) {
				result = usedMap[currentFctLeft];
			}
			
		}  else if (d > 0) {
			// right side
			usedMap = fctValsRight;
		
			if (currentFctRight == FCT_STEP) {
				double step = usedMap[currentFctRight];
				if (getDiffOrQuotientMode() == MODE_QUOTIENT) {
					step = getDifferenceForQuotient(step);
				}
				if (d > step) {
					result = 0;
				} else {
					result = 1;
				}
			} else if (currentFctRight == FCT_POLYNOMIAL) {
				double power = usedMap[currentFctRight];
				double ratio = ((double)Math.abs(d))/((double)diff);
				double x = 1-ratio;
				result = Math.pow(x, power);
			} else if (currentFctRight == FCT_SMOOTH_STEP) {
				double delta = Math.abs(d);
				double threshhold = usedMap[currentFctRight];
				if (getDiffOrQuotientMode() == MODE_QUOTIENT) {
					threshhold = getDifferenceForQuotient(threshhold);
				}
				double alpha = getDiff()/100;//1;
				double ratio = (((double)delta)-(threshhold)) / alpha;
				double divider = Math.exp(ratio)+1;
				result = ((double)1)/divider;
			} else if (currentFctRight == FCT_CONST) {
				result = usedMap[currentFctRight];
			}
		} else {
			// d == 0
			result = 1d;
		}
		
		//
		// Explanation
		if (exp != null) {
			Helper.addExplanation(exp, this.inst, query, cb, this, null, result);

			// commented out before 20.10.2008
//			String asp = getType()+getSMFunctionTypeName()+": active smf=["+getSmfName()+"]";
////			Explanation newExp = new Explanation(asp, query, cb, this);
//			Explanation newExp = new Explanation(this.inst, query, cb, this);
//			newExp.addComment(asp, "similarity is [" + result + "]");
//			newExp.setSimilarity(result);
////			exp.addExplanationChild(asp, newExp);
//			exp.addExplanationChild(this.inst, newExp);
		}
		//
		
		return result;
	}

	public ValueType getValueType() {
		return currentValueType;
	}
	
	/**
	 * Set new parameter for this function.
	 * NOTE: this is an internal function. Make sure to use the right types for value and the right keys.
	 * @param key int defined as FCT_... in this class.
	 * @param value Object value to be set. 
	 * @param isLeftSide boolean determines whether the left side is to be set or the right side
	 */
	public void setValue(int key, double value, boolean isLeftSide) {
		setValue_internal(key, value, isLeftSide);
		if (symmetryMode)  {
			if (getDiffOrQuotientMode()==MODE_QUOTIENT && (key==FCT_STEP || key==FCT_SMOOTH_STEP)) {
				value = 1d/value; 
			}
			setValue_internal(key, value, !isLeftSide);
		} 
		setHasChanged(true);
	}

	private void setValue_internal(int key, double value, boolean isLeftSide) {
		if (isLeftSide) {
			fctValsLeft[key] = value;
		} else {
			fctValsRight[key] = value;
		}
	}
	
	/**
	 * NOTE: Use this map only for read-only purposes!. 
	 * @return double[] function key(int) to function parameter(double). Left side.
	 */
	public double[] getFctValsLeft() {
		return fctValsLeft;
	}

	/**
	 * NOTE: Use this map only for read-only purposes!. 
	 * @return HashMap function key(String) to function parameter(Float or Double). Right side.
	 */
	public double[] getFctValsRight() {
		return fctValsRight;
	}

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

				if (result == 0) {
					fctValsRight = (double[]) fctValsLeft.clone();
					currentFctRight = currentFctLeft;
					if (getDiffOrQuotientMode() == MODE_QUOTIENT) {
						fctValsRight[FCT_STEP]			= 1d/fctValsRight[FCT_STEP];
						fctValsRight[FCT_SMOOTH_STEP]	= 1d/fctValsRight[FCT_SMOOTH_STEP];
					}
				} else {
					fctValsLeft = (double[]) fctValsRight.clone();
					currentFctLeft = currentFctRight;
					if (getDiffOrQuotientMode() == MODE_QUOTIENT) {
						fctValsLeft[FCT_STEP]			= 1d/fctValsLeft[FCT_STEP];
						fctValsLeft[FCT_SMOOTH_STEP]	= 1d/fctValsLeft[FCT_SMOOTH_STEP];
					}
				}
				
			}
		}
		
		this.symmetryMode = symmetryMode; 
		setHasChanged(true);
		
		return true;
	}

	public int getCurrentFctLeft() {
		return currentFctLeft;
	}

	public void setCurrentFctLeft(int currentFctLeft) {
		log.fine("new currentFct set (left): [" + currentFctLeft + "]");
		this.currentFctLeft = currentFctLeft;
		if (symmetryMode) {
			this.currentFctRight = currentFctLeft;
		}
		setHasChanged(true);
	}

	public int getCurrentFctRight() {
		return currentFctRight;
	}

	public void setCurrentFctRight(int currentFctRight) {
		log.fine("new currentFct set (right): [" + currentFctRight + "]");
		this.currentFctRight = currentFctRight;
		setHasChanged(true);
	}

	public boolean setDiffOrQuotientMode(int diffOrQuotientMode) {
		if (this.diffOrQuotientMode == diffOrQuotientMode) {
			return true;
		}
		this.diffOrQuotientMode = diffOrQuotientMode;

		if (diffOrQuotientMode == Abstract_SMF_Number.MODE_QUOTIENT) {
			fctValsLeft[FCT_SMOOTH_STEP] = 	getQuotientForDifference(-fctValsLeft[FCT_SMOOTH_STEP]);
			fctValsLeft[FCT_STEP] = 		getQuotientForDifference(-fctValsLeft[FCT_STEP]);

			fctValsRight[FCT_SMOOTH_STEP] =	getQuotientForDifference(fctValsRight[FCT_SMOOTH_STEP]);
			fctValsRight[FCT_STEP] = 		getQuotientForDifference(fctValsRight[FCT_STEP]);
		} else {
			fctValsLeft[FCT_SMOOTH_STEP] = 	roundIfIntegerMode(-getDifferenceForQuotient(fctValsLeft[FCT_SMOOTH_STEP]));
			fctValsLeft[FCT_STEP] = 		roundIfIntegerMode(-getDifferenceForQuotient(fctValsLeft[FCT_STEP]));

			fctValsRight[FCT_SMOOTH_STEP] =	roundIfIntegerMode(getDifferenceForQuotient(fctValsRight[FCT_SMOOTH_STEP]));
			fctValsRight[FCT_STEP] = 		roundIfIntegerMode(getDifferenceForQuotient(fctValsRight[FCT_STEP]));
		}
		
		setHasChanged(true);
		return true;
	}

	public void changeValueType(ValueType newValueType) {
		currentValueType = newValueType;
		checkConsistency(null, true);
		setHasChanged(true);
	}
}
