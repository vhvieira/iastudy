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
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.Symbol_DialogConsistenceTable_Widget;

/**
 * 
 * @author myCBR Team
 */
public class ConsistencyChecker_Symbols implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final static Logger log = Logger.getLogger ( ConsistencyChecker_Symbols.class.getName ( ) );

	/**
	 * Looks for symbols (String) which appear in protSymbols and do not appear in myCBRsymbols.
	 * They will be added to newSymbols.
	 * 
	 * Looks for symbols (String) which appear in myCBRsymbols and do not appear in protSymbols.
	 * They will be added to idleSymbols.
	 * 
	 * NOTE: 
	 * the given Collection objects protSymbols and myCBRsymbols will not be modified during this procedure,
	 * but newSymbols and idleSymbols will
	 * 
	 * @param protSymbols Collection of symbols (String).
	 * @param myCBRsymbols Collection of symbols (String).
	 * @param newSymbols Collection empty. To fill with new symbols from protSymbols.
	 * @param idleSymbols Collection empty. To fill with old symbols from myCBRsymbols.
	 */
	@SuppressWarnings("unchecked")
	public static void findNewAndIdleSymbols(Collection protSymbols, Collection myCBRsymbols, Collection newSymbols, Collection idleSymbols) {
		newSymbols.clear();
		idleSymbols.clear();
		
		newSymbols.addAll(protSymbols);
		newSymbols.removeAll(myCBRsymbols);
		
		idleSymbols.addAll(myCBRsymbols);
		idleSymbols.removeAll(protSymbols);
	}

	
	/**
	 * Fills matchingSymbols which maps newSymbols (String) to idleSymbols (String). This means, these
	 * symbols are actually the same and should be renamed.
	 * The remaining symbols in newSymbols are indeed new symbols.
	 * The remaining symbols in idleSymbols are to delete.
	 *
	 * NOTE: 
	 * the given Collection objects newSymbols and idleSymbols will not be modified during this procedure,
	 * but matchingSymbols will
	 * 
	 * @param frame Frame parent frame to which the used dialog is modal. 
	 * @param newSymbols Collection of new symbols.
	 * @param idleSymbols Collection of idle symbols.
	 * @param matchingSymbols Map empty. Will be filled. Maps newSymbols (String) to idleSymbols (String). 
	 * @return boolean false if user aborted else true.
	 */
	@SuppressWarnings("unchecked")
	public static boolean matchSymbols(Frame parent, Collection newSymbols, Collection idleSymbols, Map matchingSymbols, boolean quiet) {
		matchingSymbols.clear();

		// show idleElements
		for (Iterator itx=idleSymbols.iterator(); itx.hasNext();) {
			log.fine("idle elements: "+itx.next());
		}
		log.fine("#{new Elements}="+newSymbols.size()+ ", #{idle Elements}="+idleSymbols.size());

		if (idleSymbols.size()>0 || newSymbols.size()>0) {
			Vector protElements = new Vector(newSymbols);
			Vector smfElements = new Vector(idleSymbols);

			String result = Symbol_DialogConsistenceTable_Widget.OK_OPTION;
			Symbol_DialogConsistenceTable_Widget dct = new Symbol_DialogConsistenceTable_Widget(parent, "Make Consistent", true, protElements, smfElements);
			if (!quiet) {
				dct.setVisible(true);
				result = dct.getResult();
			} else {
				dct.finish();
			}

			log.fine("____ size prot = "+protElements.size()+" ____ size smf = "+smfElements.size());
			if (Symbol_DialogConsistenceTable_Widget.OK_OPTION.equals(result)) {
				for (int k=0; k<protElements.size(); k++) {
					String newVal = (String)protElements.get(k);
					String idleVal  = (String)smfElements.get(k);

					if (!"".equals(idleVal) && !"".equals(newVal)) {
						//
						// rename symbol
						//
						matchingSymbols.put(newVal, idleVal);
					}
				}
//        		setHasChanged(true);
			} else {
				// pressed abort
				return false;
			}
		}
		return true;
	}	
	
}
