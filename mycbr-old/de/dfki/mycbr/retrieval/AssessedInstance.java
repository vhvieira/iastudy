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
package de.dfki.mycbr.retrieval;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.model.casebase.CaseInstance;

/**
 * 
 * @author myCBR Team
 * An assessed case instance. A training example consists of
 * lots of (or at least some) assessed case instances.
 * We use public attributes here because this class is really just 
 * a tuple.
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public class AssessedInstance implements Comparable< Object > {
	public CaseInstance inst;
	public double similarity; /** the assessment of this instance */
	
	public Explanation explanation; 
	  /** the explanation; may be null if no explanation is generated */
	
	public AssessedInstance(CaseInstance inst, double similarity) {
		this.inst = inst;
		this.similarity = similarity;
	}
	
	public AssessedInstance(CaseInstance inst, double similarity, 
							Explanation explanation) {
		this.inst = inst;
		this.similarity = similarity;
		this.explanation = explanation;
	}
	
	public int compareTo(Object a) {
		AssessedInstance x = ((AssessedInstance) a);
		if (Double.isNaN(this.similarity) && !Double.isNaN(x.similarity))  return 1;
		if (this.similarity < x.similarity) return 1;
		if (this.similarity > x.similarity) return -1;
		return 0; // they are of the same similarity
	}
	
	public boolean equals(Object o)
	{
		boolean equal = super.equals(o);
		if (o instanceof AssessedInstance) {
			equal |= this.inst == ((AssessedInstance)o).inst;
		} 
		return equal;
	}
	
	@Override
	public String toString()
	{
		return Helper.formatDoubleAsString(similarity)+": "+inst.getName();
	}
	
}
