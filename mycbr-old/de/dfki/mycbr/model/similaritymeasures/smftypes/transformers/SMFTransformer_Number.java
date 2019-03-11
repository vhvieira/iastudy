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
package de.dfki.mycbr.model.similaritymeasures.smftypes.transformers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.smftypes.Abstract_SMF_Number;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Number_Advanced;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Number_Std;

/**
 * 
 * @author myCBR Team
 */
@SuppressWarnings("unchecked")
public class SMFTransformer_Number {
	private final static Logger log = Logger.getLogger ( SMFTransformer_Number.class.getName ( ) );

	// this defines the number of sampling points per side.
	private static int SP_CNT = 10;
	// useful constant
	private static Double DOUBLE_ZERO = 0d;

	private static SMF_Number_Std smf_std;
	private static SMF_Number_Advanced smf_adv;
	private static double lastSim;
	private static double m;
	private static double step;
	private double MAX_ASCENT_DIFFERENCE = 0.05;
	
	
	private static HashSet<String> possibleTransactions = new HashSet(); 
	{
		possibleTransactions.add("Advanced,Standard");
		possibleTransactions.add("Standard,Advanced");
	}
	

	public AbstractSMFunction transform(AbstractSMFunction smfOrig, String newSmfType) {
		String trxKey = smfOrig.getSMFunctionTypeName() + "," + newSmfType;

		if (!possibleTransactions.contains(trxKey)) {
			// this means, the function cannot be transformed. So we return the original smf
			// Then the user will be prompted whether he wants to start from scratch with a new smf or not.
			return smfOrig;
		}
		
		log.fine("transform smf[" + smfOrig.getSMFunctionTypeName() + "] -> smf[" + newSmfType + "]");
		
		try {
			// switch to difference mode first
			int diffOrQuotientMode = ((Abstract_SMF_Number) smfOrig).getDiffOrQuotientMode();
			((Abstract_SMF_Number) smfOrig).setDiffOrQuotientMode(Abstract_SMF_Number.MODE_DIFFERENCE);
			
			if ("Advanced,Standard".equals(trxKey)) {
				smf_adv = (SMF_Number_Advanced) smfOrig;
				smf_std = new SMF_Number_Std(smf_adv.getModelInstance(), smf_adv.getSmfName());
				smf_std.setSymmetryMode(false);

				double diff = smf_adv.getDiff();
				double step = smf_adv.roundIfIntegerAndDiffMode(diff / SP_CNT);
				if (step == 0) {
					step=1;
				}
				
				
				//
				// seperate both sides of sampling points
				//
				HashMap<Number,Number> spLeft = new HashMap<Number,Number>();
				HashMap<Number,Number> spRight= new HashMap<Number,Number>();
				for (Iterator<Entry<Double,Double>> it = smf_adv.getSamplingPoints().entrySet().iterator(); it.hasNext();) {
					Entry<Double,Double> e = it.next();
					
					double key = e.getKey();
					if (key < 0) {
						spLeft.put(key, e.getValue()); 
					} else {
						spRight.put(key, e.getValue());
					}
				}
				spLeft.put(DOUBLE_ZERO, smf_adv.getSamplingPoints().get(DOUBLE_ZERO));
				
				//
				// check left side
				//
				{
					// set up const fct.
					double constVal = Helper.parseDouble(Helper.formatDoubleAsString(Helper.getMinimum(spLeft.values()))
							+ (Helper.getMaximum(spLeft.values()).doubleValue() - Helper.getMinimum(spLeft.values()).doubleValue()) / 2);
					smf_std.setValue(SMF_Number_Std.FCT_CONST, constVal, true);

					// set up polynomial fct.
					double xVal = smf_adv.roundIfIntegerAndDiffMode(-diff / 2);
					double ratio = 0.5;
					double simVal = smf_adv.getSimilarityBetween(DOUBLE_ZERO, xVal, null);
					double p = Math.abs(Math.log(simVal) / Math.log(ratio));
					double power = Helper.roundDouble(p, 2);
					smf_std.setValue(SMF_Number_Std.FCT_POLYNOMIAL, power, true);

					// now compare the error of both functions
					double errorConst = 0;
					double errorPoly = 0;
					for (double d = -diff; d <= 0; d += step) {
						double tmp = d;

						smf_std.setCurrentFctLeft(SMF_Number_Std.FCT_CONST);
						errorConst += Math.abs(smf_std.getSimilarityBetween(DOUBLE_ZERO, tmp, null) - smf_adv.getSimilarityBetween(DOUBLE_ZERO, tmp, null));

						smf_std.setCurrentFctLeft(SMF_Number_Std.FCT_POLYNOMIAL);
						errorPoly += Math.abs(smf_std.getSimilarityBetween(DOUBLE_ZERO, tmp, null) - smf_adv.getSimilarityBetween(DOUBLE_ZERO, tmp, null));
					}

					if (errorPoly > errorConst || power == 0) {
						smf_std.setCurrentFctLeft(SMF_Number_Std.FCT_CONST);
					}
				}

				//
				// check right side
				//
				{
					// set up const fct.
					double constVal = Helper.parseDouble( Helper.formatDoubleAsString(Helper.getMinimum(spRight.values()))
							+ (Helper.getMaximum(spRight.values()).doubleValue() - Helper.getMinimum(spRight.values()).doubleValue()) / 2);
					smf_std.setValue(SMF_Number_Std.FCT_CONST, constVal, false);

					// set up polynomial fct.
					double xVal = smf_adv.roundIfIntegerAndDiffMode(diff / 2);
					double ratio = 0.5;
					double simVal = smf_adv.getSimilarityBetween(DOUBLE_ZERO, xVal, null);
					double p = Math.abs(Math.log(simVal) / Math.log(ratio));
					double power = Helper.roundDouble(p, 2);
					smf_std.setValue(SMF_Number_Std.FCT_POLYNOMIAL, power, false);

					// now compare the error of both functions
					double errorConst = 0;
					double errorPoly = 0;
					for (double d = 0; d <= diff; d += step) {
						double tmp = d;

						smf_std.setCurrentFctRight(SMF_Number_Std.FCT_CONST);
						errorConst += Math.abs(smf_std.getSimilarityBetween(DOUBLE_ZERO, tmp, null) - smf_adv.getSimilarityBetween(DOUBLE_ZERO, tmp, null));

						smf_std.setCurrentFctRight(SMF_Number_Std.FCT_POLYNOMIAL);
						errorPoly += Math.abs(smf_std.getSimilarityBetween(DOUBLE_ZERO, tmp, null) - smf_adv.getSimilarityBetween(DOUBLE_ZERO, tmp, null));
					}

					if (errorPoly > errorConst || power == 0) {
						smf_std.setCurrentFctRight(SMF_Number_Std.FCT_CONST);
					}
				}
				
				smf_std.setSymmetryMode(smf_std.isSymmetric());

				// possibly, switch back to quotient mode
				smf_std.setDiffOrQuotientMode(diffOrQuotientMode);
				return smf_std;
				
			} else if ("Standard,Advanced".equals(trxKey)) {
				smf_std = (SMF_Number_Std) smfOrig;
				smf_adv = new SMF_Number_Advanced(smf_std.getModelInstance(), smf_std.getSmfName());
				smf_adv.setSymmetryMode(false);
				
				double diff = smf_std.getDiff();
				step = diff / SP_CNT;
				if (step == 0) {
					step = 1;
				}
				
				// special
				double dQuery = -diff; 
				double sim = smf_std.getSimilarityBetween(DOUBLE_ZERO, dQuery, null);

				lastSim= sim;
				m = -1;
				for (double d = -diff+step; d <= 0; d += step) {
					doInternal_StandardAdvanced(d);
				}
				
				dQuery = diff; 
				sim = smf_std.getSimilarityBetween(DOUBLE_ZERO, dQuery, null);

				lastSim= sim;
				m = -1;
				step *= -1;
				for (double d = diff+step; d >= 0; d += step) {
					doInternal_StandardAdvanced(d);
				}
				
				// set the very last value
				smf_adv.setSamplingPoint(DOUBLE_ZERO, new Double(smf_std.getSimilarityBetween(DOUBLE_ZERO, DOUBLE_ZERO, null)));
				
				smf_adv.setSymmetryMode(smf_adv.isSymmetric());
				
				// possibly, switch back to quotient mode
				smf_adv.setDiffOrQuotientMode(diffOrQuotientMode);
				return smf_adv;
			} 

			log.fine("the following transformation is not implemented: [" + trxKey + "]");
			
		} catch (Throwable e) {
			log.log(Level.FINE, "Could not transform the following [" + smfOrig.getSmfName() + "]: [" + trxKey + "]", e);
		}
		
		return null;
	}


	private void doInternal_StandardAdvanced(double dQuery) {
		double sim = smf_std.getSimilarityBetween(DOUBLE_ZERO, dQuery, null);
		double difference = Math.abs(lastSim + m - sim);
		if (difference < MAX_ASCENT_DIFFERENCE ) {
			// dont set a sampling point
			lastSim = sim;
		} else {
			log.fine("we do need a sampling point. difference=[" + difference + "].lastsim[" + lastSim + "] m[" + (m) + "]  [" + (lastSim+m) + "] != sim[" + sim + "]");

			double newM = sim - lastSim;
			if (newM != m) {
				// the ascent of the function has changed.
				// so set a sampling point at the last simval spot.
				// and reset m to the new ascent.
				smf_adv.setSamplingPoint(dQuery-step, Helper.parseDouble( Helper.formatDoubleAsString(lastSim)));
				m = newM;
			}
			
			lastSim = sim;
		}
		
	}

}
