/*
 * myCBR License 1.1
 *
 * Copyright (c) 2008
 * Thomas Roth-Berghofer, Armin Stahl & Deutsches Forschungszentrum f&uuml;r K&uuml;nstliche Intelligenz DFKI GmbH
 * Further contributors: myCBR Team (see http://mycbr-project.net/contact.html for further information 
 * about the myCBR Team). 
 * All rights reserved.
 *
 * myCBR is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Since myCBR uses some modules, you should be aware of their licenses for
 * which you should have received a copy along with this program, too.
 * 
 * endOfLic */
package de.dfki.mycbr;

import java.util.logging.Logger;

/**
 * There are value acceptors to check whether a value entered by the user fits the 
 * given assumptions (e.g. similarity has to be a floating point number in the range 0.0 to 1.0)
 * Value acceptors for INTEGER, DOUBLE, FLOAT and Similarity values are implemented here.
 * 
 * @author myCBR Team
 */
public class ValueAcceptorImpl {
	
	private static ValueAcceptor VALUEACCEPTOR_INTEGER = null;
	private static ValueAcceptor VALUEACCEPTOR_DOUBLE = null;
	private static ValueAcceptor VALUEACCEPTOR_FLOAT  = null;
	private static ValueAcceptor VALUEACCEPTOR_SIMILARITY_VALUE = null;
	
	
	/**
	 * Getter for a value acceptor of INTEGERS.
	 * 
	 * @return value acceptor for INTEGER values.
	 */
	public static ValueAcceptor getValueAcceptor_Integer() {
		if (VALUEACCEPTOR_INTEGER == null) {
			VALUEACCEPTOR_INTEGER = new ValueAcceptor() {
				public boolean accept(String intStr) {
					int val = Helper.parseInt(intStr);
					return val != Integer.MIN_VALUE;
				}
			};
		}
		return VALUEACCEPTOR_INTEGER;
	}
	
	/**
	 * Getter for a value acceptor of DOUBLES.
	 * 
	 * @return value acceptor for DOUBLE values.
	 */
	public static ValueAcceptor getValueAcceptor_Double() {
		if (VALUEACCEPTOR_DOUBLE == null) {
			VALUEACCEPTOR_DOUBLE = new ValueAcceptor() {
				public boolean accept(String doubleStr) {
					Number val = Helper.parseDouble(doubleStr);

					return val.doubleValue() != -Double.MIN_VALUE;
				}
			};
		}
		return VALUEACCEPTOR_DOUBLE;
	}
	
	/**
	 * Getter for a value acceptor of FLOATS.
	 * 
	 * @return value acceptor for FLOAT values.
	 */
	public static ValueAcceptor getValueAcceptor_Float() {
		if (VALUEACCEPTOR_FLOAT == null) {
			VALUEACCEPTOR_FLOAT = new ValueAcceptor() {
				public boolean accept(String floatStr) {
					double val = Helper.parseFloat(floatStr);

					return val != -Float.MIN_VALUE;
				}
			};
		}
		return VALUEACCEPTOR_FLOAT;
	}

	/**
	 * Getter for a value acceptor of similarity values.
	 * A similarity value is accepted if it is a double vlaue in the range 0.0 to 1.0
	 * 
	 * @return value acceptor for similarity values.
	 */
	public static ValueAcceptor getValueAcceptor_SimilarityValue() {
		if (VALUEACCEPTOR_SIMILARITY_VALUE == null) {
			VALUEACCEPTOR_SIMILARITY_VALUE = new ValueAcceptor() {
				private final Logger log = Logger.getLogger(ValueAcceptor.class.getName());
				public boolean accept(String simStr) {
					Number val = Helper.parseDouble(simStr);
					// value has to be in the range [0,1]
					boolean result = (val.intValue() >= 0 && val.intValue() <= 1);
					log.fine("simStr ["+simStr+"] is to accept? "+result);
					return result;
				}
			};
		}
		return VALUEACCEPTOR_SIMILARITY_VALUE;
	}

}
