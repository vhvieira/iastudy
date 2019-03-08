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

package de.dfki.mycbr.core.similarity.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import de.dfki.mycbr.core.casebase.Attribute;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.casebase.SimpleAttribute;
import de.dfki.mycbr.core.casebase.MultipleAttribute;
import de.dfki.mycbr.core.model.ConceptDesc;
import de.dfki.mycbr.core.similarity.AmalgamationFct;
import de.dfki.mycbr.core.similarity.ISimFct;
import de.dfki.mycbr.core.similarity.Similarity;

/**
 * Configuration on how to compute similarity of multiple values.
 * Such a configuration consists of three parts: <code>MainType</code>, <code>Reuse</code>, and 
 * <code>Type</code>.
 * 
 * @author myCBR Team
 *
 */
public final class MultipleConfig {

    /**
     *
     */
    public static final MultipleConfig DEFAULT_CONFIG = new MultipleConfig(
            MainType.PARTNER_QUERY, Reuse.REUSE, Type.MAX);
    /**
     *
     */
    private MainType mt;

    /**
     *
     */
    private Reuse r;

    /**
     *
     */
    private Type t;

    /**
     *
     * @param mtc the main type config
     * @param rc the reuse config
     * @param tc the type config
     */
    public MultipleConfig(final MainType mtc, final Reuse rc, final Type tc) {
        this.mt = mtc;
        this.r = rc;
        this.t = tc;
    }

    /**
     *
     * @param mtc the new main type config
     */
    public void setMainType(final MainType mtc) {
        if (this.mt == mtc) {
            return;
        }
        if ((mtc != MainType.BEST_MATCH) && (mtc != MainType.WORST_MATCH)) {
            if (this.r == Reuse.NONE) {
                r = Reuse.REUSE;
            } else if (this.t == Type.NONE) {
                t = Type.AVG;
            }
        } else {
            r = Reuse.NONE;
            t = Type.NONE;
        }
        this.mt = mtc;
    }

    /**
     *
     * @param rc the new reuse type
     */
    public void setReuse(final Reuse rc) {
        if ((rc == Reuse.NONE) || (this.r == rc)) {
            return;
        }
        if ((mt != MainType.BEST_MATCH) && (mt != MainType.WORST_MATCH)) {
            this.r = rc;
        }
        // else do nothing because reuse is not supported
        // for BEST_MATCH or WORST_MATCH
    }

    /**
     *
     * @param tc the new type config
     */
    public void setType(final Type tc) {
        if ((tc == Type.NONE) || (this.t == tc)) {
            return;
        }
        if ((mt != MainType.BEST_MATCH) && (mt != MainType.WORST_MATCH)) {
            this.t = tc;
        }
        // else do nothing because type is not supported
        // for BEST_MATCH or WORST_MATCH
    }

    /**
     *
     * @return the main type
     */
    public MainType getMainType() {
        return mt;
    }

    /**
     *
     * @return the reuse config
     */
    public Reuse getReuse() {
        return r;
    }

    /**
     *
     * @return the type config
     */
    public Type getType() {
        return t;
    }

    /**
     *
     * @author myCBR Team
     *
     */
    public enum MainType {
        /**
         *
         */
        BEST_MATCH, WORST_MATCH, PARTNER_MAX, PARTNER_CASE, PARTNER_QUERY
    }

    /**
     *
     * @author myCBR Team
     *
     */
    public enum Reuse {
        /**
         *
         */
        REUSE, ZERO_SIM, IGNORE, NONE
    }

    /**
     *
     * @author myCBR Team
     *
     */
    public enum Type {
        /**
         *
         */
        AVG, MAX, MIN, NONE
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.dfki.mycbr.core.similarity.ISimFct#calculateSimilarity(de.dfki.mycbr
     * .core.casebase.Attribute, de.dfki.mycbr.core.casebase.SimpleAttribute)
     */
    /**
     * @param innerFunction the inner fct for comparing single attributes
     * @param value1 the query's multiple value to be compared
     * @param value2 the case's multiple value
     * @throws Exception if something goes wrong during similarity computations
     * @return similarity of value1 and value2
     */
    public Similarity calculateSimilarity(final ISimFct innerFunction,
            final MultipleAttribute<?> value1,
            final MultipleAttribute<?> value2)
            throws Exception {

        Similarity result = Similarity.INVALID_SIM;
        // compute similarity
        switch (getMainType()) {
        case BEST_MATCH:
            result = calculateBestMatch(innerFunction,
                    (MultipleAttribute<?>) value1,
                    (MultipleAttribute<?>) value2);
            break;
        case WORST_MATCH:
            result = calculateWorstMatch(innerFunction,
                    (MultipleAttribute<?>) value1,
                    (MultipleAttribute<?>) value2);
            break;
        case PARTNER_CASE:
            result = calculatePartnerSim(innerFunction,
                    (MultipleAttribute<?>) value2,
                    (MultipleAttribute<?>) value1);
            break;
        case PARTNER_QUERY:
            result = calculatePartnerSim(innerFunction,
                    (MultipleAttribute<?>) value1,
                    (MultipleAttribute<?>) value2);
            break;
        case PARTNER_MAX:
            if (((MultipleAttribute<?>) value1).getValues().size()
                    >= ((MultipleAttribute<?>) value2)
                    .getValues().size()) {
                result = calculatePartnerSim(innerFunction,
                        (MultipleAttribute<?>) value1,
                        (MultipleAttribute<?>) value2);
            } else {
                result = calculatePartnerSim(innerFunction,
                        (MultipleAttribute<?>) value2,
                        (MultipleAttribute<?>) value1);
            }
            break;
        default:
            break;
        }

        return result;
    }

    /**
     *
     * @param innerFct the inner function for computing similarity of single
     *  values
     * @param value1 the query's value
     * @param value2 the case's value
     * @return the similarity of value1 and value2
     * @throws Exception if something goes wrong during similarity computations
     */
    private Similarity calculatePartnerSim(final ISimFct innerFct,
            final MultipleAttribute<?> value1,
            final MultipleAttribute<?> value2)
            throws Exception {

        double[] sims = new double[value1.getValues().size()];
        boolean isReuse = getReuse() == Reuse.REUSE;

        int cnt = 0;
        Collection<Object> usedItems = new ArrayList<Object>();
        for (Iterator<Attribute> itQ = value1.getValues().iterator(); itQ
                .hasNext();) {
            Attribute currentQueryValue = itQ.next();

            if (!isReuse && usedItems.size() == value2.getValues().size()) {
                // all items have been picked
                boolean setZero = getReuse() == Reuse.ZERO_SIM;
                if (setZero) {
                    for (int i = cnt; i < sims.length; i++) {
                        sims[i] = 0;
                    }
                } else {
                    double[] simsTmp = new double[cnt];
                    for (int i = 0; i < cnt; i++) {
                        simsTmp[i] = sims[i];
                    }
                    sims = simsTmp;
                }
                break;

            }

            double simMax = -1;
            SimpleAttribute att = null;
            for (Iterator<Attribute> itC = value2.getValues().iterator(); itC
                    .hasNext();) {
                SimpleAttribute currentCaseValue = (SimpleAttribute) itC.next();

                double tmpSim = innerFct.calculateSimilarity(
                        (SimpleAttribute) currentQueryValue,
                        (SimpleAttribute) currentCaseValue).getValue();
                if (tmpSim > simMax) {
                    if (isReuse) {
                        simMax = tmpSim;
                        att = currentCaseValue;
                    } else if (!usedItems.contains(currentCaseValue)) {
                        simMax = tmpSim;
                        att = currentCaseValue;
                    }
                }
            }

            // take this item away
            usedItems.add(att);
            sims[cnt] = simMax;
            cnt++;
        }
        switch (getType()) {
        case AVG:
            return Similarity.get(calculateTotalAverage(sims));
        case MAX:
            return Similarity.get(calculateTotalMaximum(sims));
        case MIN:
            return Similarity.get(calculateTotalMinimum(sims));
        default: break;
        }

        return Similarity.INVALID_SIM;
    }

    /**
     *
     * @param innerFct the function to compare single values
     * @param value1 the query's value
     * @param value2 the case's value
     * @return the similarity of value1 and value2
     * @throws Exception if something goes wrong during similarity calculations
     */
    private Similarity calculatePartnerSim(final AmalgamationFct innerFct,
            final MultipleAttribute<ConceptDesc> value1,
            final MultipleAttribute<ConceptDesc> value2) throws Exception {

        double[] sims = new double[value1.getValues().size()];
        boolean isReuse = getReuse() == Reuse.REUSE;

        int cnt = 0;
        Collection<Object> usedItems = new ArrayList<Object>();
        for (Iterator<Attribute> itQ = value1.getValues().iterator(); itQ
                .hasNext();) {
            Instance currentQueryValue = (Instance) itQ.next();

            if (!isReuse && usedItems.size() == value2.getValues().size()) {
                // all items have been picked
                boolean setZero = getReuse() == Reuse.ZERO_SIM;
                if (setZero) {
                    for (int i = cnt; i < sims.length; i++) {
                        sims[i] = 0;
                    }
                } else {
                    double[] simsTmp = new double[cnt];
                    for (int i = 0; i < cnt; i++) {
                        simsTmp[i] = sims[i];
                    }
                    sims = simsTmp;
                }
                break;

            }

            double simMax = -1;
            Instance att = null;
            for (Iterator<Attribute> itC = value2.getValues().iterator(); itC
                    .hasNext();) {
                Instance currentCaseValue = (Instance) itC.next();

                double tmpSim = innerFct.calculateSimilarity(currentQueryValue,
                        currentCaseValue).getValue();
                if (tmpSim > simMax) {
                    if (isReuse) {
                        simMax = tmpSim;
                        att = currentCaseValue;
                    } else if (!usedItems.contains(currentCaseValue)) {
                        simMax = tmpSim;
                        att = currentCaseValue;
                    }
                }
            }

            // take this item away
            usedItems.add(att);
            sims[cnt] = simMax;
            cnt++;
        }

        switch (getType()) {
        case AVG:
            return Similarity.get(calculateTotalAverage(sims));
        case MAX:
            return Similarity.get(calculateTotalMaximum(sims));
        case MIN:
            return Similarity.get(calculateTotalMinimum(sims));
        default: break;
        }

        return Similarity.INVALID_SIM;
    }

    /**
     *
     * @param sims array of similarities for which the avg will be returned
     * @return average similarity value of the given array
     */
    private double calculateTotalAverage(final double[] sims) {
        double result = 0;

        for (int i = 0; i < sims.length; i++) {
            result += sims[i];
        }
        result /= sims.length;

        return result;
    }

    /**
    *
    * @param sims array of similarities for which the minimum will be returned
    * @return minimum similarity value of the given array
    */
    private double calculateTotalMinimum(final double[] sims) {
        double result = 1;

        for (int i = 0; i < sims.length; i++) {
            if (sims[i] < result) {
                result = sims[i];
            }
        }

        return result;
    }

    /**
    *
    * @param sims array of similarities for which the max will be returned
    * @return maximal similarity value of the given array
    */
    private double calculateTotalMaximum(final double[] sims) {
        double result = 0;

        for (int i = 0; i < sims.length; i++) {
            if (sims[i] > result) {
                result = sims[i];
            }
        }

        return result;
    }

    /**
     * @param f the similarity function to compare single values
     * @param value1 the query's value
     * @param value2 the case's value
     * @throws Exception if something goes wrong during similarity calculations
     * @return similarity of value1 and value2
     */
    private Similarity calculateWorstMatch(final ISimFct f,
            final MultipleAttribute<?> value1,
            final MultipleAttribute<?> value2)
            throws Exception {
        Similarity result = Similarity.get(1.0);
        for (Attribute att1 : value1.getValues()) {
            for (Attribute att2 : value2.getValues()) {
                Similarity tmpSim = f.calculateSimilarity(att1, att2);
                if (tmpSim.getValue() < result.getValue()) {
                    result = tmpSim;
                }
                if (result.getValue() == 0.0) {
                    return result;
                }
            }
        }
        return result;
    }

    /**
     * @param f the similarity function to compare single values
     * @param value1 the query's value
     * @param value2 the case's value
     * @throws Exception if something goes wrong during similarity calculations
     * @return similarity of value1 and value2
     */
    private Similarity calculateWorstMatch(final AmalgamationFct f,
            final MultipleAttribute<?> value1,
            final MultipleAttribute<?> value2)
            throws Exception {
        Similarity result = Similarity.get(1.0);
        for (Attribute att1 : value1.getValues()) {
            for (Attribute att2 : value2.getValues()) {
                Similarity tmpSim = f.calculateSimilarity(att1, att2);
                if (tmpSim.getValue() < result.getValue()) {
                    result = tmpSim;
                }
                if (result.getValue() == 0.0) {
                    return result;
                }
            }
        }
        return result;
    }

    /**
     * @param f the similarity function to compare single values
     * @param value1 the query's value
     * @param value2 the case's value
     * @throws Exception if something goes wrong during similarity calculations
     * @return similarity of value1 and value2
     */
    private Similarity calculateBestMatch(final ISimFct f,
            final MultipleAttribute<?> value1,
            final MultipleAttribute<?> value2)
            throws Exception {
        Similarity result = Similarity.INVALID_SIM;
        for (Attribute att1 : value1.getValues()) {
            for (Attribute att2 : value2.getValues()) {
                Similarity tmpSim = f.calculateSimilarity(att1, att2);
                if (tmpSim.getValue() > result.getValue()) {
                    result = tmpSim;
                }
                if (result.getValue() == 1.0) {
                    return result;
                }
            }
        }
        return result;
    }


    /**
     * @param f the similarity function to compare single values
     * @param value1 the query's value
     * @param value2 the case's value
     * @throws Exception if something goes wrong during similarity calculations
     * @return similarity of value1 and value2
     */
    private Similarity calculateBestMatch(final AmalgamationFct f,
            final MultipleAttribute<?> value1,
            final MultipleAttribute<?> value2)
            throws Exception {
        Similarity result = Similarity.INVALID_SIM;
        for (Attribute att1 : value1.getValues()) {
            for (Attribute att2 : value2.getValues()) {
                Similarity tmpSim = f.calculateSimilarity(att1, att2);
                if (tmpSim.getValue() > result.getValue()) {
                    result = tmpSim;
                }
                if (result.getValue() == 1.0) {
                    return result;
                }
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.dfki.mycbr.core.similarity.ISimFct#calculateSimilarity(de.dfki.mycbr
     * .core.casebase.Attribute, de.dfki.mycbr.core.casebase.SimpleAttribute)
     */
    /**
     * @param innerFct the similarity function to compare single values
     * @param value1 the query's value
     * @param value2 the case's value
     * @throws Exception if something goes wrong during similarity calculations
     * @return similarity of value1 and value2
     */
    @SuppressWarnings("unchecked")
    public Similarity calculateSimilarity(final AmalgamationFct innerFct,
            final MultipleAttribute<?> value1,
            final MultipleAttribute<?> value2)
            throws Exception {

        Similarity result = Similarity.INVALID_SIM;
        // compute similarity
        switch (getMainType()) {
        case BEST_MATCH:
            result = calculateBestMatch(innerFct, value1, value2);
            break;
        case WORST_MATCH:
            result = calculateWorstMatch(innerFct, value1, value2);
            break;
        case PARTNER_CASE:
            result = calculatePartnerSim(innerFct,
                    (MultipleAttribute<ConceptDesc>) value2,
                    (MultipleAttribute<ConceptDesc>) value1);
            break;
        case PARTNER_QUERY:
            result = calculatePartnerSim(innerFct,
                    (MultipleAttribute<ConceptDesc>) value1,
                    (MultipleAttribute<ConceptDesc>) value2);

            break;
        case PARTNER_MAX:
            if (((MultipleAttribute<?>) value1).getValues().size()
                    >= ((MultipleAttribute<?>) value2)
                    .getValues().size()) {
                result = calculatePartnerSim(innerFct,
                        (MultipleAttribute<ConceptDesc>) value1,
                        (MultipleAttribute<ConceptDesc>) value2);
            } else {
                result = calculatePartnerSim(innerFct,
                        (MultipleAttribute<ConceptDesc>) value2,
                        (MultipleAttribute<ConceptDesc>) value1);
            }
            break;
        default:
            break;
        }

        return result;

    }

}
