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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.jdom.Element;
import org.jdom.JDOMException;

import de.dfki.mycbr.CBRProject;
import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.retrieval.Explanation;

public class MMF_Standard extends AbstractSMFunction implements HasChangedListenerSMF {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String XML_ATT_FCT_ONEORMULTI_MODE 	= "oneOrMulti";
	private static final String XML_ATT_FCT_MULTISELECTION_MODE = "multiSelection";
	private static final String XML_ATT_FCT_SINGLESIM_MODE 		= "singleSim";
	private static final String XML_ATT_FCT_REUSE_MODE 			= "reuse";
	private static final String XML_ATT_FCT_NOMATCH_MODE 		= "noMatch";
	private static final String XML_ATT_FCT_TOTAL_MODE 			= "totalFct";

	public static final int FUNCTION_ONEORMULTI_ONE 	= 10;
	public static final int FUNCTION_ONEORMULTI_MULTI 	= 11;

	public static final int FUNCTION_MULTISELECTION_BYQUERY 		= 20;
	public static final int FUNCTION_MULTISELECTION_BYCASE 			= 21;
	public static final int FUNCTION_MULTISELECTION_BYQUERYANDCASE 	= 22;

	public static final int FUNCTION_SINGLESIM_BEST 	= 30;
	public static final int FUNCTION_SINGLESIM_WORST 	= 31;

	public static final int FUNCTION_REUSE_REUSE 		= 40;
	public static final int FUNCTION_REUSE_DONTREUSE 	= 41;

	public static final int FUNCTION_NOMATCH_SETZERO	= 45;
	public static final int FUNCTION_NOMATCH_IGNORE 	= 46;
	
	public static final int FUNCTION_TOTAL_AVERAGE = 50;
	public static final int FUNCTION_TOTAL_MAXIMUM = 51;
	public static final int FUNCTION_TOTAL_MINIMUM = 52;

	
	// default settings
	private int currentFunction_OneOrMulti 		= FUNCTION_ONEORMULTI_MULTI;
	private int currentFunction_MultiSelection 	= FUNCTION_MULTISELECTION_BYQUERY;
	private int currentFunction_SingleSim 		= FUNCTION_SINGLESIM_BEST;
	private int currentFunction_Reuse 			= FUNCTION_REUSE_REUSE;
	private int currentFunction_NoMatch 		= FUNCTION_NOMATCH_SETZERO;
	private int currentFunction_Total 			= FUNCTION_TOTAL_AVERAGE;

	AbstractSMFunction internalSMF;

	private String provenance;


	public MMF_Standard(AbstractSMFunction internalSMF) {
		this(internalSMF.getModelInstance(), internalSMF.getSmfName());
		this.internalSMF = internalSMF;
		addHasChangedListener(this, true);
		initProvenanceStr();
	}

	public MMF_Standard(ModelInstance inst, String smfName) {
		super(inst, smfName);
		addHasChangedListener(this, true);
		initProvenanceStr();
	}

	public MMF_Standard(ModelInstance inst, Element element) throws JDOMException {
		super(inst, element);
		setCurrentFunction_MultiSelection(element.getAttribute(XML_ATT_FCT_MULTISELECTION_MODE).getIntValue());
		setCurrentFunction_OneOrMulti(element.getAttribute(XML_ATT_FCT_ONEORMULTI_MODE).getIntValue());
		setCurrentFunction_NoMatch(element.getAttribute(XML_ATT_FCT_NOMATCH_MODE).getIntValue());
		setCurrentFunction_ReuseQ(element.getAttribute(XML_ATT_FCT_REUSE_MODE).getIntValue());
		setCurrentFunction_SingleSim(element.getAttribute(XML_ATT_FCT_SINGLESIM_MODE).getIntValue());
		setCurrentFunction_Total(element.getAttribute(XML_ATT_FCT_TOTAL_MODE).getIntValue());
		addHasChangedListener(this, true);
		initProvenanceStr();
	}

	private void initProvenanceStr() {
		provenance = ((ModelSlot)inst).getValueType()+"_"+getSMFunctionTypeName()+"_"+getSmfName();
	}

	public boolean checkConsistency(Frame parent, boolean quiet) {
		// no inconsistencies possible
		return true;
	}

	public AbstractSMFunction copy() {
		MMF_Standard newMMF = new MMF_Standard(inst, smfName);

		newMMF.setCurrentFunction_MultiSelection(currentFunction_MultiSelection);
		newMMF.setCurrentFunction_OneOrMulti(currentFunction_OneOrMulti);
		newMMF.setCurrentFunction_NoMatch(currentFunction_NoMatch);
		newMMF.setCurrentFunction_ReuseQ(currentFunction_Reuse);
		newMMF.setCurrentFunction_SingleSim(currentFunction_SingleSim);
		newMMF.setCurrentFunction_Total(currentFunction_Total);
		newMMF.setHasChanged(false);
		
		return newMMF;
	}

	protected SMFPanel createSMFPanel() {
		return new MMFPanel_Standard(this);
	}

	public static String getSMFunctionTypeName_static() {
		return "Standard";
	}

	public void toXML(Element xmlElement) {
		xmlElement.setAttribute(XML_ATT_FCT_MULTISELECTION_MODE, 	Integer.toString(currentFunction_MultiSelection));
		xmlElement.setAttribute(XML_ATT_FCT_ONEORMULTI_MODE, 		Integer.toString(currentFunction_OneOrMulti));
		xmlElement.setAttribute(XML_ATT_FCT_NOMATCH_MODE, 			Integer.toString(currentFunction_NoMatch));
		xmlElement.setAttribute(XML_ATT_FCT_REUSE_MODE, 			Integer.toString(currentFunction_Reuse));
		xmlElement.setAttribute(XML_ATT_FCT_SINGLESIM_MODE, 		Integer.toString(currentFunction_SingleSim));
		xmlElement.setAttribute(XML_ATT_FCT_TOTAL_MODE, 			Integer.toString(currentFunction_Total));
	}

	public double getSimilarityBetween(Object query, Object cb, Explanation exp) throws Exception {
//		MMF_Explanation expObject = null;
		if (exp != null)
		{
			Helper.addExplanation(exp, this.inst, query, cb, this, null, 0);
			
			// commented out before 20.10.2008
//			expObject = new MMF_Explanation();
//			expObject.cOrder = new Vector<Object>();
//			expObject.qOrder = new Vector<Object>();
//			expObject.similarities = new Vector<Double>();
//			exp.setAdditionalObject(expObject);
		}

		StringBuffer sb = new StringBuffer();
		Collection<?> q = (Collection<?>) query;
		Collection<?> c = (Collection<?>) cb;

		double finalSim = -1;
		if (currentFunction_OneOrMulti==FUNCTION_ONEORMULTI_ONE) {
			// select one similarity (from all)
			
			// first collect all similarities for all combinations
			// Note: complexity for this is q.size * c.size
			double[] sims = new double[q.size() * c.size()];
			int cnt = 0;
			for (Iterator<?> itQ = q.iterator(); itQ.hasNext();) {
				Object currentQueryValue = itQ.next();
				for (Iterator<?> itC = c.iterator(); itC.hasNext();) {
					Object currentCaseValue = itC.next();
					sims[cnt] = internalSMF.getSimilarityBetween(currentQueryValue, currentCaseValue, null);
					sb.append("local :: q=" + currentQueryValue + " : c=" + currentCaseValue + " --> " + sims[cnt] + "\n");
					cnt++;
				}
			}

			// now select max or min
			switch (currentFunction_SingleSim) {
				case FUNCTION_SINGLESIM_BEST:
					finalSim = calculateTotalMaximum(sims);
					break;
				case FUNCTION_SINGLESIM_WORST:
					finalSim = calculateTotalMinimum(sims);
					break;
			}
			
		} else {
			//
			// calculate from multiple
			//

			double[] sims = null;
			if (currentFunction_MultiSelection == FUNCTION_MULTISELECTION_BYQUERY) {
				//
				// by Query
				//
				sims = calculateMultiple(q, c, exp);
			} else if (currentFunction_MultiSelection == FUNCTION_MULTISELECTION_BYCASE) {
				//
				// by Case
				//
				sims = calculateMultiple(c, q, exp);
			} else if (currentFunction_MultiSelection == FUNCTION_MULTISELECTION_BYQUERYANDCASE) {
				//
				// by Query and Case
				//
				double[] sims1 = calculateMultiple(q, c, exp);
				double[] sims2 = calculateMultiple(c, q, exp);
				sims = new double[sims1.length+sims2.length];
				
				for (int i = 0; i < sims1.length; i++) {
					sims[i] = sims1[i];
				}
				for (int i = 0; i < sims2.length; i++) {
					sims[i+sims1.length] = sims2[i];
				}	
			}			
			
			//
			// now calculate similarity by total fct
			//
			switch (currentFunction_Total) {
				case FUNCTION_TOTAL_AVERAGE:
					finalSim = calculateTotalAverage(sims);
					break;
				case FUNCTION_TOTAL_MAXIMUM:
					finalSim = calculateTotalMaximum(sims);
					break;
				case FUNCTION_TOTAL_MINIMUM:
					finalSim = calculateTotalMinimum(sims);
					break;
			}
		}
		
		sb.append("total = " + Helper.formatDoubleAsString(finalSim) + "\n");

		if (exp != null) {
			exp.setSimilarity(finalSim);
			exp.addComment(provenance, sb.toString());
		}

		return finalSim;
	}

	private double[] calculateMultiple(Collection<?> q, Collection<?> c, Explanation exp) throws Exception {
		
		double[] sims = new double[q.size()];
		boolean isReuse = currentFunction_Reuse == FUNCTION_REUSE_REUSE;
		Explanation expInternal = null;
		Explanation bestExp = null;

//		TODO solve optimization problem
		int cnt = 0;
		Collection<Object> usedItems = new ArrayList< Object >();
		for (Iterator<?> itQ = q.iterator(); itQ.hasNext();) {
			Object currentQueryValue = itQ.next();
			Object bestMatch = null;
			bestExp = null;
			
			double simMax = -1;
			if (!isReuse && usedItems.size() == c.size()) {
				// all items have been picked
				boolean setZero = currentFunction_NoMatch == FUNCTION_NOMATCH_SETZERO;
				if (setZero) {
					for (int i=cnt; i<sims.length; i++) {
						sims[i] = 0;
					}
				} else {
					double[] simsTmp = new double[cnt];
					for (int i=0; i<cnt; i++) {
						simsTmp[i] = sims[i];
					}
					sims = simsTmp;
				}
				if (exp != null) {
					ArrayList<Object> al = new ArrayList<Object>();
					al.add(currentQueryValue);
					while (itQ.hasNext()) {
						al.add(itQ.next());
					}
//					expObject.qOrder.add(currentQueryValue);
					exp.addComment(provenance, "no partners found for "+al+". "+(setZero? "Set similarities to Zero." : "Ignore."));
				}
				break;
				
			}
			for (Iterator<?> itC = c.iterator(); itC.hasNext();) {
				Object currentCaseValue = itC.next();

				expInternal = null;
				if (exp != null) {
					expInternal = new Explanation(inst, currentQueryValue, currentCaseValue, internalSMF);
				}
		
				double tmpSim = internalSMF.getSimilarityBetween(currentQueryValue, currentCaseValue, expInternal);

				if (tmpSim > simMax) {
					if (isReuse) {
						simMax = tmpSim;
						bestMatch = currentCaseValue;
						bestExp = expInternal;
					} else {
						if (!usedItems.contains(currentCaseValue)) {
							// take this item away
							usedItems.add(currentCaseValue);
							simMax = tmpSim;
							bestMatch = currentCaseValue;
							bestExp = expInternal;
						}
					}
				}
//				sb.append("local :: q=" + currentQueryValue + " : c=" + currentCaseValue + " --> " + sims[cnt] + "\n");
				
			}
			if (simMax < 0) {
				simMax = 0d;
			}
			if (exp != null) {
				exp.addComment(provenance, "Local sim:  "+currentQueryValue+" ~ "+bestMatch+" : "+simMax+(simMax==0? " (no partner found)" : ""));

				// commented out before 20.10.2008
//				expObject.qOrder.add(currentQueryValue);
//				expObject.similarities.add(simMax);
				
				if (bestExp != null && simMax > 0d) {
					exp.addExplanationChild(inst, bestExp);
				}
				
				// commented out before 20.10.2008
//				if (bestMatch!=null) expObject.cOrder.add(bestMatch);
			}
			sims[cnt] = simMax;
			cnt++;
		}
		
		return sims;
	}

	public ValueType getValueType() {
		return ValueType.NO_TYPE;
	}

	public AbstractSMFunction getInternalSMF() {
		return internalSMF;
	}

	public void setInternalSMF(AbstractSMFunction internalSMF) {
		this.internalSMF = internalSMF;
	}

	//
	// TOTAL FUNCTION IMPLEMENTATIONS
	//
	private double calculateTotalAverage(double[] sims) {
		// log.info("calculate AVERAGE");
		double result = 0;

		for (int i = 0; i < sims.length; i++) {
			result += sims[i];
		}
		result /= sims.length;

		return result;
	}

	private double calculateTotalMinimum(double[] sims) {
		double result = 1;

		for (int i = 0; i < sims.length; i++) {
			if (sims[i] < result)
				result = sims[i];
		}

		return result;
	}

	private double calculateTotalMaximum(double[] sims)
	{
		double result = 0;

		for (int i = 0; i < sims.length; i++) {
			if (sims[i] > result)
				result = sims[i];
		}

		return result;
	}

	public int getCurrentFunction_OneOrMulti() {
		return currentFunction_OneOrMulti;
	}

	public int getCurrentFunction_MultiSelection() {
		return currentFunction_MultiSelection;
	}

	public int getCurrentFunction_SingleSim() {
		return currentFunction_SingleSim;
	}

	public int getCurrentFunction_Reuse() {
		return currentFunction_Reuse;
	}

	public int getCurrentFunction_NoMatch() {
		return currentFunction_NoMatch;
	}

	public int getCurrentFunction_Total() {
		return currentFunction_Total;
	}
	
	public void setCurrentFunction_Total(int currentTotalFunction) {
		this.currentFunction_Total = currentTotalFunction;
		setHasChanged(true);
	}
	
	public void setCurrentFunction_MultiSelection(int currentFunction_MultiSelection) {
		this.currentFunction_MultiSelection = currentFunction_MultiSelection;
		setHasChanged(true);
	}

	public void setCurrentFunction_OneOrMulti(int currentFunction_OneOrMulti) {
		this.currentFunction_OneOrMulti = currentFunction_OneOrMulti;
		setHasChanged(true);
	}

	public void setCurrentFunction_NoMatch(int intValue) {
		this.currentFunction_NoMatch = intValue;
		setHasChanged(true);
	}

	public void setCurrentFunction_ReuseQ(int currentFunction_ReuseQ) {
		this.currentFunction_Reuse = currentFunction_ReuseQ;
		setHasChanged(true);
	}

	public void setCurrentFunction_SingleSim(int currentFunction_SingleSim) {
		this.currentFunction_SingleSim = currentFunction_SingleSim;
		setHasChanged(true);
	}

	public void smfHasChanged(boolean hasChanged) {
		if (hasChanged) {
			CBRProject.getInstance().setHasChanged();
		}
	}

}
