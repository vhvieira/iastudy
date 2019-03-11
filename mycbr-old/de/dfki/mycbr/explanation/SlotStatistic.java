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
package de.dfki.mycbr.explanation;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class SlotStatistic implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger( SlotStatistic.class.getName());

//	HashMap attvalueToAmount = new HashMap();
	TreeMap< Object, Integer > attvalueToAmount = new TreeMap< Object, Integer >();

	private long maxAppearance;

	private long usage;
	
	public SlotStatistic(HashMap<Object, Integer> attvalueToAmount, long maxAppearance, long usage) {
		try {
		this.attvalueToAmount = new TreeMap<Object, Integer>(attvalueToAmount);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		this.maxAppearance = maxAppearance;
		this.usage = usage;
	}
	
	public Collection<Object> keyset() {
		return attvalueToAmount.keySet();
	}
	
	public Collection<Entry<Object, Integer>> entryset() {
		return attvalueToAmount.entrySet();
	}
	
	public int getAttributeAmount(Object attribute) {
		Integer amount = (Integer) attvalueToAmount.get(attribute);
		return (amount == null ? 0 : amount.intValue());
	}

	public long getMaxAppearance() {
		return maxAppearance;
	}

	public long getUsage() {
		return usage;
	}

	public void show() {
		StringBuffer sb = new StringBuffer();
		for (Iterator<Entry<Object, Integer>> it = attvalueToAmount.entrySet().iterator(); it.hasNext();) {
			Entry<Object, Integer> e = it.next();
			sb.append(e.getKey().toString() + " : " + e.getValue() + "\n");
		}
		log.info("\n" + sb.toString());
	}

	public double integral(Number val1, Number val2) {
		double v1 = val1.doubleValue();
		double v2 = val2.doubleValue();
		
		double result = 0;
		
		for (Iterator<Object> it = attvalueToAmount.keySet().iterator(); it.hasNext();) {
			Number key = (Number) it.next();
			double k = key.doubleValue();
//			if (k >= v2) break;
			if (k > v2) break;
			if (v1 <= k) {
				result += ((Integer)attvalueToAmount.get(key)).intValue();
			}
		}
		return result;
	}
	
	
}
