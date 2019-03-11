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
package de.dfki.mycbr.explanation;

import java.io.Serializable;
import java.net.URL;
import java.util.Vector;

/**
 * Representing conceptual explanation having a URL for a "see also"
 * explanation object
 * 
 * @author myCBR Team
 *
 */
public class ConceptExplanation implements Serializable {
	
	private static final long serialVersionUID = 5196601628742516931L;
	
	private String description;
	private Vector<URL> seeAlso;

	/**
	 * Initializing this conceptual explanation with a description
	 * and an URL for a "see also" explanation.
	 * @param description the description for this explanation
	 * @param seeAlso the see also URL
	 */
	public ConceptExplanation(String description, Vector<URL> seeAlso) {
		this.description = description;
		this.seeAlso = seeAlso;
	}

	/**
	 * Getter for the description of this explanation
	 * @return the description of this explanation
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Getter for the "see also" URL of this explanation
	 * @return the "see also" URL of this explanation
	 */
	public Vector<URL> getSeeAlso() {
		return seeAlso;
	}

}
