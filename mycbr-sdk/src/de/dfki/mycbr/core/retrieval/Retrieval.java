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

import java.util.HashMap;
import java.util.List;


import de.dfki.mycbr.core.ICaseBase;
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.similarity.Similarity;
import de.dfki.mycbr.util.Pair;

/**
 * A retrieval has a retrieval method and a retrieval engine.
 * When specifying a case base and a query you can retrieve a similarity
 * value between the query and each case in the case base.
 * 
 * @author myCBR Team
 *
 */
public class Retrieval extends HashMap<Instance, Similarity> implements Runnable {


    protected RetrievalMethod retrievalMethod = RetrievalMethod.RETRIEVE;
    List<Pair<Instance,Similarity>> l = null;
    /**
     *
     */
    private static final long serialVersionUID = 2656679620557431799L;

    /**
     *
     */
    private Instance query;

    /**
     *
     */
    private RetrievalEngine re;

    /**
     *
     */
    private Project p;

    private ICaseBase cb;
    private int k = 5;
	private boolean finished = true;
    
    /**
     *
     * @param c the query should be an instance of this concept
     */
    public Retrieval(final Concept c, ICaseBase cb) {
        p = c.getProject();
        this.cb = cb;
	    query = c.getQueryInstance();
        re = new SequentialRetrieval(p, this);
    }

    /**
    *
    * @param c the query should be an instance of this concept
    */
   public Retrieval(final Concept c, ICaseBase cb, RetrievalEngine re) {
       p = c.getProject();
       this.cb = cb;
       query = c.getQueryInstance();
       this.re = re;
   }
   
    /**
     *
     * @throws Exception if something goes wrong during retrieval
     */
    public final void start() {
    	run();
    }
    
    /**
     * @since 3.0.0 BETA 0.2
     */
    public void setRetrievalEngine(RetrievalEngine re) {
    	this.re = re;
    }
    
    /**
     * 
     */
    public RetrievalEngine getRetrievalEngine() {
    	return re;
    }
    
    /**
     * @since 3.0.0 BETA 0.3
     */
    public void setCaseBase(ICaseBase cb) {
    	this.cb = cb;
    }
    
    /**
     * @since 3.0.0 BETA 0.3
     */
    public ICaseBase getCaseBase() {
    	return cb;
    }
    
    /**
     * @since 3.0.0 BETA 0.2
     */
    public Instance getQueryInstance() {
    	return query;
    }
    
    /**
     * Set all attributes to undefined
     * @since 3.0.0 BETA 0.3
     */
    public Instance resetQuery() {
    	query.setAttsUnknown();
		return query;
    }
    
    /**
     * @since 3.0.0 BETA 0.3
     */
    public void setK(int k) {
    	this.k = k;
    }
    
    /**
     * @since 3.0.0 BETA 0.3
     */
    public int getK() {
    	return k;
    }
    
    /**
    *
    * @param m the current retrieval method
    */
    public final void setRetrievalMethod(RetrievalMethod m) {
       retrievalMethod = m;
   }

    public boolean isFinished() {
    	return finished;
    }
   /**
    *
    * @return the current case
    */
    public final RetrievalMethod getRetrievalMethod() {
       return retrievalMethod;
   }
   
    public enum RetrievalMethod {
    	RETRIEVE, RETRIEVE_SORTED, RETRIEVE_K, RETRIEVE_K_SORTED;
    }

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		finished = false;
    	
    	try {
	    	// start new retrieval
	    	switch(retrievalMethod) {
	    		case RETRIEVE: 			l = re.retrieve(cb, query); 
	    								break;
	    								
	    		case RETRIEVE_SORTED: 	l = re.retrieveSorted(cb, query);
	    								break;
	    								
	    		case RETRIEVE_K: 		l = re.retrieveK(cb, query, k);
	    								break;
	    								
	    		case RETRIEVE_K_SORTED: l = re.retrieveKSorted(cb, query, k);
	    								break;
	    								
	    		default: 				l = re.retrieve(cb, query);
	    				 				break;
	    	}

    	} catch(Exception e) {
    		System.out.println("Retrieval");
    		e.printStackTrace();
    	}
    	finished = true;
	}
    
	public List<Pair<Instance, Similarity>> getResult() {
		return l;
	}
}
