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
package de.dfki.mycbr.model.vocabulary;

import java.io.Serializable;


public class FilterHandler implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static Filter FILTER_SIMILAR = new Filter("~", Messages.getString("Nothing_filtered")) { //$NON-NLS-1$ //$NON-NLS-2$
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public boolean accept(Object queryVal, Object caseVal) {
			return true;
		}
	};

	public static Filter FILTER_EQUAL = new Filter("=", Messages.getString("Not_equal_filtered")) { //$NON-NLS-1$ //$NON-NLS-2$
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public boolean accept(Object queryVal, Object caseVal) {
			return queryVal.equals(caseVal);
		}
	};

	public static Filter FILTER_LESS_THAN = new Filter("\u2264", Messages.getString("Less_than_filtered")) { //$NON-NLS-1$ //$NON-NLS-2$
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unchecked") //$NON-NLS-1$
		public boolean accept(Object queryVal, Object caseVal) {
			if (!(queryVal instanceof Comparable)) return true;
			return ((Comparable)queryVal).compareTo((Comparable)caseVal) >= 0;
		}
	};
	
	public static Filter FILTER_GREATER_THAN = new Filter("\u2265", Messages.getString("Greater_than_filtered")) { //$NON-NLS-1$ //$NON-NLS-2$
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unchecked") //$NON-NLS-1$
		public boolean accept(Object queryVal, Object caseVal) {
			if (!(queryVal instanceof Comparable)) return true;
			return ((Comparable)queryVal).compareTo((Comparable)caseVal) <= 0;
		}
	};
	
	
	public static Filter[] getFilters() {
		return new Filter[]{FILTER_SIMILAR, FILTER_EQUAL, FILTER_LESS_THAN, FILTER_GREATER_THAN};
	}
	
}
