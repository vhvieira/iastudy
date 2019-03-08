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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.dfki.mycbr.core.ICaseBase;
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.casebase.SymbolAttribute;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.similarity.Similarity;
import de.dfki.mycbr.util.Pair;

/**
 * Sequential retrieval computes the similarity of the query and all cases in
 * the case base to get the most similar cases.
 *
 * @author myCBR Team
 *
 */
public class SequentialRetrieval extends RetrievalEngine {

    /**
     *
     */
    private Project prj;

    /**
     *
     */
    private Retrieval retrieval;

    /**
     *
     * @param p the project this retrieval belongs to
     * @param r the underlying retrieval object
     */
    public SequentialRetrieval(final Project p, final Retrieval r) {
        this.retrieval = r;
        this.prj = p;
    }

    /**
     * @param cb the case base this retrieval should be run on
     * @param q the current query
     * @return list of retrieval results including pairs of case and similarity
     * @throws Exception if something goes wrong during similarity computations
     */
    public final List<Pair<Instance, Similarity>> retrieve(final ICaseBase cb,
            final Instance q)
            throws Exception {
        Collection<Instance> cases = cb.getCases();

        List<Pair<Instance, Similarity>> result = new
                                        LinkedList<Pair<Instance, Similarity>>();
        
        for (Instance c : cases) {
            setCurrentCase(c);
            // get names of concepts these cases belong to
            String qID = q.getConcept().getName();
            String cID = c.getConcept().getName();

            // if the cases belong to the same c
            // the inheritanceSim is 1.0
            // else it has to be computed via the inheritanceFct
            Similarity inheritanceSim = Similarity.get(1.0);
            if (!qID.equals(cID)) {
                inheritanceSim = prj.getInhFct().calculateSimilarity(
                        qID, cID);
            }
            // get the common parent node of the given case and query
            SymbolAttribute qAtt = (SymbolAttribute) prj.getInhFct().getDesc()
                    .getAttribute(qID);
            SymbolAttribute cAtt = (SymbolAttribute) prj.getInhFct().getDesc()
                    .getAttribute(cID);
            Object first = prj.getInhFct().getCommonAncestor(qAtt, cAtt);

            Similarity localSim = Similarity.get(0.0);
            if (first instanceof SymbolAttribute) {
                localSim = calculateLocalSim((SymbolAttribute) first, q, c);
            }
            Similarity sim = Similarity.get(localSim.getValue()
                    * inheritanceSim.getValue());
            retrieval.put(c, sim);
            result.add(new Pair<Instance, Similarity>(c, sim));
        }
        return result;
    }

    /**
     * Calculates the similarity of q and c according to amalgamationFct of
     * parent desc. Uses only attributes q and c have in common. These are
     * exactly the attributes described by first
     *
     * @param first the query attribute
     * @param q the whole query
     * @param c the case
     * @return the similarity of the given attribute to the corresponding case
     *  attribute
     * @throws Exception if something goes wrong during similarity computations
     */
    private Similarity calculateLocalSim(final SymbolAttribute first,
            final Instance q,
            final Instance c) throws Exception {
    	Similarity res = Similarity.INVALID_SIM;
        Concept concept = prj.getConceptByID(first.getValue());
        
        res = concept.getActiveAmalgamFct().calculateSimilarity(q,
                c);
        
        return res;
    }

    /**
     * @param cb the case base this retrieval should be run on
     * @param q the current query
     * @param k the number of cases to be returned
     * @return list of k pairs of case and similarity (if there are enough)
     * @throws Exception if something goes wrong during similarity computations
     */
    public final List<Pair<Instance, Similarity>> retrieveK(
            final ICaseBase cb,
            final Instance q, final int k) throws Exception {
        List<Instance> cases = new LinkedList<Instance>(cb.getCases());
        int numberOfCases = k;
        if (k > cases.size()) {
            numberOfCases = cases.size();
        }

        List<Pair<Instance, Similarity>> result =
            new LinkedList<Pair<Instance, Similarity>>();

        Similarity lowestSim = Similarity.get(1.00);
        int counter = 0;

        Iterator<Instance> it = cases.iterator();

        for (; it.hasNext();) {
            setCurrentCase(it.next());
            Similarity currentSim = q.getConcept().getActiveAmalgamFct()
                    .calculateSimilarity(q, getCurrentCase());
            if (currentSim.getValue() < lowestSim.getValue()) {
                lowestSim = currentSim;
            }

            addSorted(result, new Pair<Instance, Similarity>(getCurrentCase(),
                    currentSim));
            if (++counter == numberOfCases) {
                break;
            }
        }

        for (; it.hasNext();) {
            setCurrentCase(it.next());

            Similarity currentSim = q.getConcept().getActiveAmalgamFct()
                    .calculateSimilarity(q, getCurrentCase());
            if (currentSim.getValue() > lowestSim.getValue()) {
                result.remove(result.size() - 1);

                addSorted(result, new Pair<Instance, Similarity>(getCurrentCase(),
                        currentSim));
                lowestSim = Similarity.get(1.00);
                for (Pair<Instance, Similarity> p : result) {
                    if (p.getSecond().getValue() < lowestSim.getValue()) {
                        lowestSim = p.getSecond();
                    }
                }
            }
        }

        // add current retrieval results to cases
        for (Pair<Instance, Similarity> p : result) {
            retrieval.put(p.getFirst(), p.getSecond());
        }

        return result;
    }

    /**
     * @param cb the case base this retrieval should be run on
     * @param q the current query
     * @param k the number of cases to be returned
     * @return list of k pairs of case and similarity (if there are enough)
     *  sorted with respect to the similarities in descending order
     * @throws Exception if something goes wrong during similarity computations
     */
    public final List<Pair<Instance, Similarity>> retrieveKSorted(
            final ICaseBase cb,
            final Instance q, final int k) throws Exception {
        return retrieveK(cb, q, k);
    }

    /**
     * @param cb the case base this retrieval should be run on
     * @param q the current query
     * @return list of pairs of case and similarity
     *  sorted with respect to the similarities in descending order
     * @throws Exception if something goes wrong during similarity computations
     */
    public final List<Pair<Instance, Similarity>> retrieveSorted(
            final ICaseBase cb,
            final Instance q) throws Exception {

        List<Pair<Instance, Similarity>> result = retrieve(cb, q);

        Comparator<Pair<Instance, Similarity>> comparator =
            new Comparator<Pair<Instance, Similarity>>() {

            public int compare(final Pair<Instance, Similarity> arg0,
                    final Pair<Instance, Similarity> arg1) {

                if (arg0.getSecond().getValue() < arg1.getSecond().getValue()) {
                    return 1;
                } else if (arg0.getSecond().getValue() > arg1.getSecond()
                        .getValue()) {
                    return -1;
                }
                return 0;
            }

        };

        Collections.sort(result, comparator);

        return result;
    }

    /**
     *
     * @param list list of sorted retrieval result
     * @param pair pair to be added to the list such that result is still sorted
     */
    private void addSorted(final List<Pair<Instance, Similarity>> list,
            final Pair<Instance, Similarity> pair) {

        int index = 0;

        for (Iterator<Pair<Instance, Similarity>> it = list.iterator(); it
                .hasNext();) {
            Pair<Instance, Similarity> current = it.next();
            if (pair.getSecond().getValue() >= current.getSecond().getValue()) {
                break;
            }
            index++;
        }

        list.add(index, pair);
    }

}
