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

import java.io.Serializable;
import java.util.Date;
import java.util.Vector;


/**
 * 
 * @author myCBR Team
 */
public class RetrievalResults implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Query originalQuery;
	private Vector<AssessedInstance> ranking;
	/** a vector of assessed instances */
	private Date timeStart;
	private Date timeFinish;
	private RetrievalEngine re;

	public RetrievalResults(Query originalQuery,
			Vector<AssessedInstance> ranking, de.dfki.mycbr.retrieval.RetrievalEngine re,
			Date timeStart, Date timeFinish) {
		this.originalQuery = originalQuery;
		this.ranking = ranking;
		this.timeStart = timeStart;
		this.timeFinish = timeFinish;
		this.re = re;
	}

	public final Vector<Explanation> getExplanations() {
		Vector<Explanation> result = new Vector<Explanation>();
		if (!hasExplanations())
			return result;
		for (int i = 0; i < ranking.size(); i++) {
			result.add(ranking.get(i).explanation);
		}
		return result;
	}

	public final Query getOriginalQuery() {
		return originalQuery;
	}

	public final Vector<AssessedInstance> getRanking() {
		return ranking;
	}

	public final Date getTimeFinish() {
		return timeFinish;
	}

	public final Date getTimeStart() {
		return timeStart;
	}

	public final RetrievalEngine getRetrievalEngine() {
		return re;
	}

	public boolean hasExplanations() {
		return ranking != null && ranking.size() > 0
				&& ((AssessedInstance) ranking.get(0)).explanation != null;
	}
}
