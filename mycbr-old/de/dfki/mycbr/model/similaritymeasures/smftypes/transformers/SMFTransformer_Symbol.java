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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Symbol_Ordered;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Symbol_Table;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Symbol_Taxonomy;

/**
 * 
 * @author myCBR Team
 */
@SuppressWarnings("unchecked")
public class SMFTransformer_Symbol {
	
	private final static Logger log = Logger.getLogger(SMFTransformer_Symbol.class.getName());

	private HashSet<String> possibleTransactions = new HashSet<String>();
	
	public SMFTransformer_Symbol() {
		possibleTransactions.add("Taxonomy,Table");
		possibleTransactions.add("Table,Taxonomy");
		possibleTransactions.add("Ordered,Table");
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
			if ("Taxonomy,Table".equals(trxKey)) {
				log.fine("just get internal table of taxonomy smf");
				SMF_Symbol_Table smfM = ((SMF_Symbol_Taxonomy) smfOrig).getTable();

				smfM.setSymmetryMode(((SMF_Symbol_Taxonomy)smfOrig).isSymmetryMode() && smfM.isSymmetric());
				
				return smfM;
				
			} else if ("Table,Taxonomy".equals(trxKey)) {
				SMF_Symbol_Table smf_table = (SMF_Symbol_Table) smfOrig;

				SMF_Symbol_Taxonomy smf_tax = null;
				AbstractSMFunction smf_backed = smf_table.getBackedSMF();
				if (smf_backed != null && smf_backed instanceof SMF_Symbol_Taxonomy) {
					log.fine("the backed taxonomy of the given table is already a taxonomy. Changes have to be registered during consistency check.");
					smf_table.setSymmetryMode(false);
					smf_tax = (SMF_Symbol_Taxonomy) smf_backed;
					// now let the taxonomy check its critical symbols.
					smf_tax.checkCriticalSymbols();
				} else {
					log.fine("cannot use the backed taxonomy [" + (smf_backed == null ? null : smf_backed.getSMFunctionTypeName()) + "]. Create a new one.");
					return smfOrig;
				}
				return smf_tax;

			} else if ("Ordered,Table".equals(trxKey)) {
				SMF_Symbol_Ordered smf_ordered = (SMF_Symbol_Ordered) smfOrig;
				SMF_Symbol_Table smf_table = new SMF_Symbol_Table(smf_ordered.getModelInstance(), smf_ordered.getSmfName());

				final HashMap symbolMapping = smf_ordered.getSymbolMapping();

				ArrayList keyset = new ArrayList(symbolMapping.keySet());
				Collections.sort(keyset, new Comparator() {
					public int compare(Object o1, Object o2) {
						Integer int1 = (Integer)symbolMapping.get(o1);
						Integer int2 = (Integer)symbolMapping.get(o2);
						
						return int1.compareTo(int2);
					}
				});
				HashMap symbolOrder = smf_table.getSymbolOrder();
				symbolOrder.clear();
				for (int i=0; i<keyset.size(); i++) {
					String symbol = (String) keyset.get(i);
					symbolOrder.put(symbol, new Integer(i));	
				}
				
				// now fill table
				for (Iterator it1 = symbolMapping.keySet().iterator(); it1.hasNext();) {
					String symbolQ = (String) it1.next();
					for (Iterator it2 = symbolMapping.keySet().iterator(); it2.hasNext();) {
						String symbolC = (String) it2.next();

						double sim = Helper.parseDouble( Helper.formatDoubleAsString(smf_ordered.getSimilarityBetween(symbolQ, symbolC, null)));
						smf_table.setValueAt(sim, symbolQ, symbolC);
					}
				}

				return smf_table;
			}

			log.fine("the following transformation is not implemented: [" + trxKey + "]");
			
		} catch (Exception e) {
			e.printStackTrace();
			log.log(Level.SEVERE, "Could not transform the following [" + smfOrig.getSmfName() + "]: [" + trxKey + "]", e);
		}	
		return null;
	}
}
