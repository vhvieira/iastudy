/*
 * myCBR License 3.0
 * 
 * Copyright (c) 2006-2015, by German Research Center for Artificial Intelligence (DFKI GmbH), Germany
 * 
 * Project Website: http://www.mycbr-project.net/
 * 
 * This library is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.
 * 
 * endOfLic */

package de.dfki.mycbr.core.similarity;

import java.util.Observable;

import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Attribute;
import de.dfki.mycbr.core.casebase.DoubleAttribute;
import de.dfki.mycbr.core.casebase.DoubleRange;
import de.dfki.mycbr.core.casebase.MultipleAttribute;
import de.dfki.mycbr.core.casebase.SimpleAttribute;
import de.dfki.mycbr.core.casebase.SpecialAttribute;
import de.dfki.mycbr.core.model.AttributeDesc;
import de.dfki.mycbr.core.model.DoubleDesc;
import de.dfki.mycbr.core.similarity.config.DistanceConfig;
import de.dfki.mycbr.core.similarity.config.NumberConfig;

/**
 * Calculates the similarity of DoubleAttribute objects. There are four modes
 * which can be chosen for the similarity function:
 * <ul>
 * <li>CONSTANT: the similarity is always a constant value</li>
 * <li>STEP AT: for a distance smaller than the specified value the similarity
 * is 0.00. For the other values it is 1.00</li>
 * <li>POLINOMIAL WITH: stretches/compresses the line described by y =
 * x/(max-min) + 1 ( for case < query ) resp. y = - (x/(max-min)) +1 ( for case
 * > query ) by taking y to the power of the given parameter</li>
 * <li>SMOOTH STEP AT: similar to STEP AT but with a smooth step, the function
 * is computed as the logistic function 1/(1 + exp((x-param)*100/(max-min))) (
 * cp. sigmoid-curve or s-curve)</li>
 * </ul>
 * You have to configure the similarity for two cases:
 * <ul>
 * <li>query < case: referred to as the right part of the function</li>
 * <li>case < query: referred to as the left part of the function</li>
 * </ul>
 * If the function is symmetric, the configurations should be the same. The
 * similarity of a value to itself is assumed to be 1.00 in any case.
 * 
 * @author myCBR Team
 */
public class DoubleFct extends NumberFct {

	private final double constantValue = 1.0;
	private final double stepAtValue = 0;
	private final double polynomialWithValue = 1;
	private final double smoothStepAtValue = 0;

	// if you have a double function that uses QUOTIENT as DISTANCE_CONFIG
	// then we need a point, from which on the function should return 0
	private double maxForQuotient = 10;
	
	/**
	 * query < case
	 */
	private NumberConfig functionTypeR = NumberConfig.CONSTANT;
	private double functionParameterR = constantValue;

	/**
	 * case < query
	 */
	private NumberConfig functionTypeL = NumberConfig.CONSTANT;
	private double functionParameterL = constantValue;

	protected DoubleDesc desc;
	protected DoubleRange range;

	/**
	 * Constructor should only be called by FunctionContainer. Initializes this
	 * with the given description.
	 * 
	 * @param desc
	 *            the description of the attributes this function can be applied
	 *            to
	 */
	public DoubleFct(Project prj, DoubleDesc desc, String name) {
		super(prj, desc, name);
		this.prj = prj;
		this.range = desc.getRange();
		this.desc = desc;

		max = desc.getMax();
		min = desc.getMin();
		diff = max - min;
		this.name = name;
	}

	/**
	 * Returns the similarity of the given attributes. Returns invalid
	 * similarity if an error occurs. The similarity computation depends on the
	 * type of the function and it's parameter. We refer to the right part of
	 * the function if case < query and to the left part otherwise. The possible
	 * computations are:
	 * <ul>
	 * <li>CONSTANT: the similarity is always a constant value</li>
	 * <li>STEP AT: for a distance smaller than the specified value the
	 * similarity is 0.00. For the other values it is 1.00</li>
	 * <li>POLINOMIAL WITH: stretches/compresses the line described by y =
	 * x/(max-min) + 1 ( for case < query ) resp. y = - (x/(max-min)) +1 ( for
	 * case > query ) by taking y to the power of the given parameter</li>
	 * <li>SMOOTH STEP AT: similar to STEP AT but with a smooth step, the
	 * function is computed as the logistic function 1/(1 +
	 * exp((x-param)*100/(max-min))) ( cp. sigmoid-curve or s-curve)</li>
	 * </ul>
	 * 
	 * @return Similarity of the given attributes, invalid similarity if an
	 *         error occurs.
	 * @throws Exception
	 */
	public Similarity calculateSimilarity(Attribute a1, Attribute a2)
			throws Exception {
		Similarity result = Similarity.INVALID_SIM;
		if (a1 instanceof SpecialAttribute || a2 instanceof SpecialAttribute) {
			result = prj.calculateSpecialSimilarity(a1, a2);
		} else if (a1 instanceof MultipleAttribute<?>
				&& a2 instanceof MultipleAttribute<?>) {
			result = prj.calculateMultipleAttributeSimilarity(this,
					((MultipleAttribute<?>) a1), (MultipleAttribute<?>) a2);
		} else if (a1 instanceof DoubleAttribute && a2 instanceof DoubleAttribute) {

			DoubleAttribute att1 = (DoubleAttribute) a1;
			DoubleAttribute att2 = (DoubleAttribute) a2;

			// get query and case values
			double q = att1.getValue();
			double c = att2.getValue();

			double d = c - q;
			if (distanceFunction == DistanceConfig.QUOTIENT) {
				if (q == 0.0) {
					return Similarity.INVALID_SIM;
				}
				d = c / q;
			}

			if (att1.getAttributeDesc() == desc
					&& att2.getAttributeDesc() == desc) {
				if ((d < 0 && distanceFunction == DistanceConfig.DIFFERENCE)
						|| (d < 1 && distanceFunction == DistanceConfig.QUOTIENT)) { // "left side" of the graph 

					switch (functionTypeL) {
					case STEP_AT:
						result = stepAt(d, functionParameterL, true);
						break;
					case POLYNOMIAL_WITH:
						result = polinomialWith(d, functionParameterL, diff);
						break;
					case SMOOTH_STEP_AT:
						result = Similarity
								.get(1 / (1 + Math
										.exp((-d + functionParameterL)
												* (100 / diff))));
						break;
					case CONSTANT:
						result = Similarity.get(functionParameterL);
						break;
					}
				} else if ((d > 0 && distanceFunction == DistanceConfig.DIFFERENCE)
						|| (d > 1 && distanceFunction == DistanceConfig.QUOTIENT)) { // "left side" of the graph
					switch (functionTypeR) {
					case STEP_AT:
						result = stepAt(d, functionParameterR, false);
						break;
					case POLYNOMIAL_WITH:
						result = polinomialWith(d, functionParameterR, -diff);
						break;
					case SMOOTH_STEP_AT:
						result = Similarity.get(1 / (1 + Math
								.exp((d - functionParameterR) * (100 / diff))));
						break;
					case CONSTANT:
						result = Similarity.get(functionParameterR);
						break;
					}
				} else {
					return Similarity.get(1.00); // value at 0 (1 for quotient) is always 1.0
				}
			}
		}

		return result;
	}

	/**
	 * Returns the similarity for value according to step. If value is less than
	 * step, returns 0.00, else 1.00
	 * 
	 * @param value
	 *            the value for which the similarity value should be returned
	 * @param step
	 *            the step for which similarity switches from 0.00 to 1.00
	 * @return 0.00 if value is less than step, else 1.00
	 */
	private Similarity stepAt(double value, double step, boolean left) {
		if (left) {
			return (value < step) ? Similarity.get(0.00) : Similarity.get(1.00);
		} else {
			return (value > step) ? Similarity.get(0.00) : Similarity.get(1.00);
		}
	}

	/**
	 * Returns the similarity computed by pow(value/diff+1, exponent).
	 * 
	 * @param value
	 *            the value for which the similarity value should be returned
	 * @param exponent
	 *            double value describing how the line given by x/diff+1 should
	 *            be stretched/compressed
	 * @return pow(value/diff+1, exponent)
	 */
	private Similarity polinomialWith(double value, double exponent, double diff) {
		return Similarity.get(Math.pow(value / diff + 1, exponent));
	}

	/**
	 * Calculates the similarity of the given Floats. Calls
	 * {@link #calculateSimilarity(Attribute, Attribute)}. Be aware that
	 * <code>calculateSimilarity(value1, values2)</code> might differ from
	 * <code>calculateSimilarity(value2, values1)</code>. When calculating
	 * similarities, the first attribute is always referred to as the query
	 * value, the second parameter as the case value.
	 * 
	 * @param value1
	 *            the first attribute to be compared
	 * @param value2
	 *            the second attribute to be compared
	 * @return similarity of the given attributes
	 * @throws Exception
	 */
	public Similarity calculateSimilarity(Double value1, Double value2)
			throws Exception {
		return calculateSimilarity(
				(SimpleAttribute) range.getAttribute(value1),
				(SimpleAttribute) range.getAttribute(value2));
	}

	/**
	 * Sets the function's parameter for query &lt; case to value. The function
	 * parameter depends on the type of the function. The parameter specifies:
	 * <ul>
	 * <li>CONSTANT: the constant similarity value</li>
	 * <li>STEP AT: the distance value where the step should be (every distance
	 * greater or equal than this value has similarity 1.00, else 0.00)</li>
	 * <li>POLINOMIAL WITH: the constant with which the function gets compressed
	 * or stretched</li>
	 * <li>SMOOTH STEP AT: the distance value where the smooth step should be</li>
	 * </ul>
	 * If the parameter for &quot;smooth step&quot; or &quot;step at&quot; does
	 * not fit the range given by max/min, the parameter will not be updated. If
	 * the parameter for &quot;constant&quot; is not within the range (0.0,1.0),
	 * the parameter will not be updated. Or if the parameter for
	 * &quot;polynomial with&quot; is smaller than 0 the parameter will not be
	 * updated. Be aware to set the function type before setting the parameter,
	 * because the parameter depends on the current function type.
	 * 
	 * @param value
	 *            the function parameter for query &lt; case
	 * @return true, if the parameter was successfully updated, false otherwise
	 */
	public boolean setFunctionParameterR(double value) {

		boolean updated = false;

		if (functionTypeR == NumberConfig.SMOOTH_STEP_AT
				|| functionTypeR == NumberConfig.STEP_AT) {
			if (distanceFunction == DistanceConfig.DIFFERENCE && value > 0
					&& value <= diff) {
				functionParameterR = value;
				updated = true;
			} else if (distanceFunction == DistanceConfig.QUOTIENT && value > 1
					&& value <= max / min) {
				functionParameterR = value;
				updated = true;
			}
		} else if (functionTypeR == NumberConfig.CONSTANT) {
			if (value >= 0 && value <= 1) {
				functionParameterR = value;
				updated = true;
			}
		} else if (functionTypeR == NumberConfig.POLYNOMIAL_WITH) {
			if (value >= 0) {
				functionParameterR = value;
				updated = true;
			}
		}

		if (updated && isSymmetric()) {
			if (functionTypeL == NumberConfig.SMOOTH_STEP_AT
					|| functionTypeL == NumberConfig.STEP_AT) {
				if (distanceFunction == DistanceConfig.DIFFERENCE) {
					functionParameterL = -value;
				} else if (distanceFunction == DistanceConfig.QUOTIENT) {
					functionParameterL = 1 / value;
				}
			} else {
				functionParameterL = value;
			}
			this.setChanged();
			notifyObservers();
		}

		return updated;
	}

	/**
	 * Sets the function's parameter for query &lt; case to value. The function
	 * parameter depends on the type of the function. The parameter specifies:
	 * <ul>
	 * <li>CONSTANT: the constant similarity value</li>
	 * <li>STEP AT: the distance value where the step should be (every distance
	 * greater or equal than this value has similarity 1.00, else 0.00)</li>
	 * <li>POLINOMIAL WITH: the constant with which the function gets compressed
	 * or stretched</li>
	 * <li>SMOOTH STEP AT: the distance value where the smooth step should be</li>
	 * </ul>
	 * If the parameter for &quot;smooth step&quot; or &quot;step at&quot; does
	 * not fit the range given by max/min, the parameter will not be updated. If
	 * the parameter for &quot;constant&quot; is not within the range (0.0,1.0),
	 * the parameter will not be updated. Or if the parameter for
	 * &quot;polynomial with&quot; is smaller than 0 the parameter will not be
	 * updated. Be aware to set the function type before setting the parameter,
	 * because the parameter depends on the current function type.
	 * 
	 * @param value
	 *            the function parameter for query &lt; case
	 * @return true, if the parameter was successfully updated, false otherwise
	 */
	public boolean setFunctionParameterL(double value) {

		boolean updated = false;

		if (functionTypeL == NumberConfig.SMOOTH_STEP_AT
				|| functionTypeL == NumberConfig.STEP_AT) {
			if (value > 0) {
				value = -value;
			}
			if (distanceFunction == DistanceConfig.DIFFERENCE && value >= -diff) {

				functionParameterL = value;
				updated = true;
			} else if (distanceFunction == DistanceConfig.QUOTIENT && value < 1
					&& value >= min / max) {
				functionParameterL = value;
				updated = true;
			}
		} else if (functionTypeL == NumberConfig.CONSTANT) {
			if (value >= 0 && value <= 1) {
				functionParameterL = value;
				updated = true;
			}
		} else if (functionTypeL == NumberConfig.POLYNOMIAL_WITH) {
			if (value >= 0) {
				functionParameterL = value;
				updated = true;
			}
		}

		if (updated && isSymmetric()) {
			if (functionTypeR == NumberConfig.SMOOTH_STEP_AT
					|| functionTypeR == NumberConfig.STEP_AT) {
				if (distanceFunction == DistanceConfig.DIFFERENCE) {
					functionParameterR = -value;
				} else if (distanceFunction == DistanceConfig.QUOTIENT) {
					functionParameterR = 1 / value;
				}
			} else {
				functionParameterR = value;
			}

			this.setChanged();
			notifyObservers();
		}

		return updated;
	}

	/**
	 * Gets the function's parameter for query &lt; case to value. The function
	 * parameter depends on the type of the function. The parameter specifies:
	 * <ul>
	 * <li>CONSTANT: the constant similarity value</li>
	 * <li>STEP AT: the distance value where the step should be (every distance
	 * greater or equal than this value has similarity 1.00, else 0.00)</li>
	 * <li>POLINOMIAL WITH: the constant with which the function gets compressed
	 * or stretched</li>
	 * <li>SMOOTH STEP AT: the distance value where the smooth step should be</li>
	 * </ul>
	 * 
	 * @return the function parameter for query &lt; case
	 */
	public double getFunctionParameterR() {
		return functionParameterR;
	}

	/**
	 * Gets the function's parameter for case &lt; query to value. The function
	 * parameter depends on the type of the function. The parameter specifies:
	 * <ul>
	 * <li>CONSTANT: the constant similarity value</li>
	 * <li>STEP AT: the distance value where the step should be (every distance
	 * greater or equal than this value has similarity 1.00, else 0.00)</li>
	 * <li>POLINOMIAL WITH: the constant with which the function gets compressed
	 * or stretched</li>
	 * <li>SMOOTH STEP AT: the distance value where the smooth step should be</li>
	 * </ul>
	 * 
	 * @return the function parameter for case &lt; query
	 */
	public double getFunctionParameterL() {
		return functionParameterL;
	}

	/**
	 * Sets the type of the function for query &lt; case. Updates function
	 * parameter if necessary. Be aware to set the function type before setting
	 * the parameter, because the parameter depends on the current function
	 * type.
	 * 
	 * @param type
	 *            the type of the function
	 */
	public void setFunctionTypeR(NumberConfig type) {

		if (functionTypeR == NumberConfig.POLYNOMIAL_WITH
				|| functionTypeR == NumberConfig.CONSTANT) {
			if (type == NumberConfig.STEP_AT) {
				functionParameterR = stepAtValue;
			} else if (type == NumberConfig.SMOOTH_STEP_AT) {
				functionParameterR = smoothStepAtValue;
			}
		} else if (functionTypeR == NumberConfig.STEP_AT
				|| functionTypeR == NumberConfig.SMOOTH_STEP_AT) {
			if (type == NumberConfig.POLYNOMIAL_WITH) {
				functionParameterR = polynomialWithValue;
			} else if (type == NumberConfig.CONSTANT) {
				functionParameterR = constantValue;
			}
		}

		this.functionTypeR = type;
		if (isSymmetric()) {
			this.functionTypeL = functionTypeR;
			if (functionTypeL == NumberConfig.SMOOTH_STEP_AT
					|| functionTypeL == NumberConfig.STEP_AT) {
				if (distanceFunction == DistanceConfig.DIFFERENCE) {
					functionParameterL = -functionParameterR;
				} else if (distanceFunction == DistanceConfig.QUOTIENT) {
					functionParameterL = 1 / functionParameterR;
				}
			} else {
				functionParameterL = functionParameterR;
			}
		}

		this.setChanged();
		notifyObservers();
	}

	/**
	 * Sets the type of the function for case &lt; query. Updates function
	 * parameter if necessary. Be aware to set the function type before setting
	 * the parameter, because the parameter depends on the current function
	 * type.
	 * 
	 * @param type
	 *            the type of the function
	 */
	public void setFunctionTypeL(NumberConfig type) {
		if (functionTypeL == NumberConfig.POLYNOMIAL_WITH
				|| functionTypeL == NumberConfig.CONSTANT) {
			if (type == NumberConfig.STEP_AT) {
				functionParameterL = stepAtValue;
			} else if (type == NumberConfig.SMOOTH_STEP_AT) {
				functionParameterL = smoothStepAtValue;
			}
		} else if (functionTypeL == NumberConfig.STEP_AT
				|| functionTypeL == NumberConfig.SMOOTH_STEP_AT) {
			if (type == NumberConfig.POLYNOMIAL_WITH) {
				functionParameterL = polynomialWithValue;
			} else if (type == NumberConfig.CONSTANT) {
				functionParameterL = constantValue;
			}
		}

		this.functionTypeL = type;
		if (isSymmetric()) {
			this.functionTypeR = functionTypeL;
			if (functionTypeR == NumberConfig.SMOOTH_STEP_AT
					|| functionTypeR == NumberConfig.STEP_AT) {
				if (distanceFunction == DistanceConfig.DIFFERENCE) {
					functionParameterR = -functionParameterL;
				} else if (distanceFunction == DistanceConfig.QUOTIENT) {
					functionParameterR = 1 / functionParameterL;
				}
			} else {
				functionParameterR = functionParameterL;
			}
		}

		this.setChanged();
		notifyObservers();
	}

	public NumberConfig getFunctionTypeL() {
		return functionTypeL;
	}

	public NumberConfig getFunctionTypeR() {
		return functionTypeR;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	public double getDiff() {
		return diff;
	}

	public void setDistanceFct(DistanceConfig df) {
		if (this.distanceFunction == df) {
			return;
		}
		if (df.equals(DistanceConfig.QUOTIENT)) {
			if (min <= 0 && max >= 0) {
				return; // cannot use quotient, when 0 is included in range
			}
		}
		this.distanceFunction = df;

		if (functionTypeL == NumberConfig.SMOOTH_STEP_AT
				|| functionTypeL == NumberConfig.STEP_AT) {
			if (distanceFunction == DistanceConfig.QUOTIENT) {
				functionParameterL = 1;
			} else {
				functionParameterL = 0;
			}
		}
		if (functionTypeR == NumberConfig.SMOOTH_STEP_AT
				|| functionTypeR == NumberConfig.STEP_AT) {
			if (distanceFunction == DistanceConfig.QUOTIENT) {
				functionParameterR = 1;
			} else {
				functionParameterR = 0;
			}
		}
		setChanged();
		notifyObservers();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.dfki.mycbr.core.similarity.ISimFct#clone(de.dfki.mycbr.core.model.
	 * AttributeDesc, boolean)
	 */
	@Override
	public void clone(AttributeDesc descNEW, boolean active) {
		if (descNEW instanceof DoubleDesc
				&& !name.equals(Project.DEFAULT_FCT_NAME)) {
			DoubleFct f = ((DoubleDesc) descNEW).addDoubleFct(name, active);
			f.functionParameterL = this.functionParameterL;
			f.functionParameterR = this.functionParameterR;
			f.functionTypeL = this.functionTypeL;
			f.functionTypeR = this.functionTypeR;
			f.isSymmetric = this.isSymmetric;
			f.mc = this.mc;
			f.distanceFunction = this.distanceFunction;
			f.maxForQuotient = this.maxForQuotient;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable arg0, Object arg1) {

		if (arg0.equals(desc)) {
			super.min = desc.getMin();
			super.max = desc.getMax();
			super.diff = max - min;
			updateFunctionParams();
		}
	}
	
	public void updateFunctionParams() {
		if (functionTypeL.equals(NumberConfig.SMOOTH_STEP_AT)
				|| functionTypeL.equals(NumberConfig.STEP_AT)) {
			// check whether parameter still fits domain
			if (!fitsDistance(functionParameterL)) {
				functionParameterL = 0;
			}
		}
		if (functionTypeR.equals(NumberConfig.SMOOTH_STEP_AT)
				|| functionTypeR.equals(NumberConfig.STEP_AT)) {
			// check whether parameter still fits domain
			if (!fitsDistance(functionParameterR)) {
				functionParameterR = 0;
			}
		}
	}
	
	/**
	 * @param x a value to be checked 
	 * @return true, if x is still in the domain defined by desc wrt to the given distance function
	 */
	public boolean fitsDistance(double x) {
		if (distanceFunction.equals(DistanceConfig.DIFFERENCE)) {
			if (x < (max-min) && x > (min-max)) {
				return true;
			}
		} else {
			if (x > 0) {
				return true;
			}
		}
		return false;
	}
	
	public double getMaxForQuotient() {
		return maxForQuotient;
	}	
	
	public void setMaxForQuotient(double max) {
		if (max>0) {
			this.maxForQuotient = max;
			setChanged();
			notifyObservers();
		} 
		updateFunctionParams();
	}
}
