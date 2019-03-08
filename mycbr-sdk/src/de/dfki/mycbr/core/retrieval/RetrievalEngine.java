/*
 * myCBR License 3.0
 * 
 * Copyright (c) 2006-2015, by German Research Center for Artificial Intelligence (DFKI GmbH), Germany
 * 
 * Project Website: http://www.mycbr-project.net/
 * 
 * This library is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.
 * 
 * endOfLic */

package de.dfki.mycbr.core.retrieval;

import java.util.List;

import de.dfki.mycbr.core.ICaseBase;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.similarity.Similarity;
import de.dfki.mycbr.util.Pair;

/**
 * Retrieval methods retrieve the most similar cases to a query from a given
 * case base. There can be more efficient methods for retrieving k cases than
 * retrieving all cases and then return only k. Implements Strategy Pattern.
 *
 * @author myCBR Team
 *
 */
public abstract class RetrievalEngine {

    /**
     *
     */
    protected Instance currentCase;
    
    /**
     * @param cb the case base this retrieval should be run on
     * @param q the current query
     * @return list of retrieval results including pairs of case and similarity
     * @throws Exception if something goes wrong during similarity computations
     */
    public abstract List<Pair<Instance, Similarity>> retrieve(
            final ICaseBase cb,
            final Instance q) throws Exception;


    /**
     * @param cb the case base this retrieval should be run on
     * @param q the current query
     * @return list of pairs of case and similarity
     *  sorted with respect to the similarities in descending order
     * @throws Exception if something goes wrong during similarity computations
     */
    public abstract List<Pair<Instance, Similarity>> retrieveSorted(
            final ICaseBase cb, final Instance q) throws Exception;

    /**
     * @param cb the case base this retrieval should be run on
     * @param q the current query
     * @param k the number of cases to be returned
     * @return list of k pairs of case and similarity (if there are enough)
     * @throws Exception if something goes wrong during similarity computations
     */
    public abstract List<Pair<Instance, Similarity>> retrieveK(
            final ICaseBase cb,
            final Instance q, int k) throws Exception;

    /**
     * @param cb the case base this retrieval should be run on
     * @param q the current query
     * @param k the number of cases to be returned
     * @return list of k pairs of case and similarity (if there are enough)
     *  sorted with respect to the similarities in descending order
     * @throws Exception if something goes wrong during similarity computations
     */
    public abstract List<Pair<Instance, Similarity>> retrieveKSorted(
            final ICaseBase cb, final Instance q,
            final int k) throws Exception;

    /**
     *
     * @param c the new current case
     */
    public final void setCurrentCase(Instance c) {
        currentCase = c;
    }

    /**
     *
     * @return the current case
     */
    public final Instance getCurrentCase() {
        return currentCase;
    }
}
