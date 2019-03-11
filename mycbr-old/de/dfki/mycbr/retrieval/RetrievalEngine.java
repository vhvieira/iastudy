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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.model.casebase.CaseInstance;
import de.dfki.mycbr.model.similaritymeasures.AbstractClassSM;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.SimMap;
import de.dfki.mycbr.model.vocabulary.Filter;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelSlot;

/**
 * @author myCBR Team
 */
public class RetrievalEngine implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger
			.getLogger(RetrievalEngine.class.getName());

	protected Vector<CaseInstance> instances;

	private static RetrievalEngine instance;
	
	public static RetrievalEngine getInstance()  {
		return instance;
	}
	
	public static RetrievalEngine initInstance() {
		instance = new RetrievalEngine();	
		return instance;
	}
	
	public static void resetInstance() {
		instance = null;
	}
	
	public static final int STATE_LISTENERS_NOTICE_STEP = 128;
	
	protected HashSet<RetrievalResultListener> listeners = new HashSet<RetrievalResultListener>();

	protected Thread retrievalThread; 
	protected DefaultQuery query; 
	
	protected boolean useExp;
	protected SimMap modelToSMF;

	/**
	 * Because retrieval engine is supposed to run in background. 
	 * @return boolean true if a retrieval is taking place.
	 */
	public boolean isStillRunning()
	{
		boolean stillRunning = retrievalThread !=null && retrievalThread.isAlive();
		log.fine("Retrieval Engine is still running = [" + stillRunning + "]");
		return stillRunning;
	}
	

	// ----------------------- listener stuff ---------------------------------------------
	public synchronized void addRetrievalResultListener(RetrievalResultListener listener)
	{
		listeners.add(listener);
	}
	
	public synchronized void removeRetrievalResultListener(RetrievalResultListener listener)
	{
		listeners.remove(listener);
	}
	
	public void updateStateListeners(double percentage)
	{
		for (Iterator<RetrievalResultListener> it=listeners.iterator(); it.hasNext();)
		{
			it.next().setRetrievalState(percentage);
		}
	}
	protected synchronized void notifyRetrievalResultListeners(RetrievalResults results)
	{
		for (Iterator<RetrievalResultListener> it=listeners.iterator(); it.hasNext();)
		{
			RetrievalResultListener listener = it.next();
			listener.setRetrievalResults(results);
		}
	}
	
	// -------------------------------------------------------------------------------------

	/**
	 * Contains the SimMap which was used.
	 * @return SimMap lastly used SimMap
	 */
	public SimMap getSimMap()
	{
		return modelToSMF;
	}

	public synchronized RetrievalResults retrieve(DefaultQuery query,
			SimMap modelToSMF, boolean useExp) throws Exception {
		this.useExp = useExp;
		Vector<AssessedInstance> ranking = new Vector<AssessedInstance>();
		// get start time
		Date timeStart = new Date();

		// get class to iterate over:
		ModelCls cls = query.getModelCls();

		// get SMFunction:
		AbstractClassSM smfC = Helper.findSuperClsSMF(modelToSMF, cls);
		if (smfC == null) {
			log.severe("similarity function for class ["
					+ (cls == null ? null : cls.getName()) + "] is not set!");
			return null;
		}

		// iterate over any EditorSMFunction in modelToSMF to set up
		// external processes for example:
		for (Iterator<AbstractSMFunction> it = modelToSMF.values().iterator(); it
				.hasNext();) {
			AbstractSMFunction smf = it.next();
			assert (smf != null);
			try {
				smf.startRetrieval();
			} catch (Exception e) {
				e.printStackTrace();
				// BUGFIX: do not attempt to retrieve!
				return new RetrievalResults(query, ranking, this, timeStart,
						timeStart);
			}
		}

		if (instances == null)
			prepareForRetrieval(cls); // BUGFIX!
		query.putAllFiltersInOneMap();
		double counter = 0;
		double amount = instances.size();

		if (query.filtersDefined()) {
			for (Iterator<Entry<ModelSlot, Filter>> it = query.getFilterMap()
					.entrySet().iterator(); it.hasNext();) {
				Entry<ModelSlot, Filter> e = it.next();
				Filter filter = (Filter) e.getValue();
				filter.setUpdateFilterState(this, counter, amount);
				Collection<CaseInstance> filteredOut = filter.doFilter(
						(ModelSlot) e.getKey(), query, instances);

				instances.removeAll(filteredOut);
				counter += filteredOut.size();
			}
		} else {
			log.fine("no filters defined");
		}

		for (Iterator<CaseInstance> instIt = instances.iterator(); instIt
				.hasNext();) {
			CaseInstance currentInstance = instIt.next();

			Explanation exp;
			double sim;
			if (useExp) {
				exp = new Explanation(cls, query, currentInstance, smfC);
				sim = smfC.compareModelCls(query, currentInstance, exp,
						modelToSMF, smfC.getFlags());
				exp.setSimilarity(sim);
			} else {
				exp = null;
				sim = smfC.compareModelCls(query, currentInstance, null,
						modelToSMF, smfC.getFlags());
			}
			ranking.add(new AssessedInstance(currentInstance, sim, exp));
			// log.info("adding assessed instance: " +
			// currentInstance.getName());

			if ((counter++) % STATE_LISTENERS_NOTICE_STEP == 0) {
				updateStateListeners(counter / amount);
			}
		}
		updateStateListeners(1); // 100% done

		// now sort, AssessedInstance overrides a compareTo() method for this:
		Collections.sort(ranking);

		// iterate over any EditorSMFunction in modelToSMF to close
		// external processes for example:
		for (Iterator<AbstractSMFunction> it = modelToSMF.values().iterator(); it
				.hasNext();) {
			AbstractSMFunction smf = it.next();
			try {
				smf.finishRetrieval();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// get finish time
		Date timeFinish = new Date();
		return new RetrievalResults(query, ranking, this, timeStart, timeFinish);
	}

	public synchronized void prepareForRetrieval(ModelCls cls) {
		if (cls == null) {
			log
					.severe("Cannot instanciate retrieval engine properly. cls is null.");
			return;
		}
		ModelCls topCls = Helper.findHighestModelClsByInheritance(cls);
		instances = new Vector<CaseInstance>(topCls.getDirectCaseInstances());
		// maybe it is better to forbid modifying the case instances in
		// general than to copy all instances here; but only references
		// are copied anyway
		// RE: The case instances cannot change. But the collection can change
		// -> results in a ConcurrentModificationException during retrieval
	}

	public Vector<CaseInstance> getInstances() {
		return instances;
	}

	public void setInstances(Vector<CaseInstance> instances) {
		this.instances = instances;
	}
	
	/**
	 * Runs the retrieve() method concurrently. 
	 * When finished, it notifies all registered RetrievalResultListeners.
	 * @param query DefaultQuery
	 * @param modelToSMF SimMap configuration of similarity measure
	 * @param useExp boolean value for specifying whether to generate explanations or not
	 */
	public void beginRetrieval(DefaultQuery query, SimMap modelToSMF, boolean useExp)
	{
		if (isStillRunning())
		{
			log.info("Cannot run retrieval. This retrieval engine is still running.");
			return;
		}
		this.query = query;
		this.useExp = useExp;
		this.modelToSMF = modelToSMF;

		// perform a quick consistency check 
		// (maybe some smfs doesnt exist anymore but are still configured in smf_cls
		ModelCls cls = query.getModelCls();
//		SMF_Class_Standard smf_cls = (SMF_Class_Standard) modelToSMF.get(cls);
		AbstractClassSM smf_cls = Helper.findSuperClsSMF(modelToSMF, cls);
		smf_cls.checkConsistency(null, true);

		retrievalThread = new Thread()
		{
			public void run()
			{
				runInternal();
			}
		};
		
		retrievalThread.start();
	}

	private void runInternal()
	{
		RetrievalResults results = null;
		try
		{
			results = retrieveGuess(query, modelToSMF,useExp);
			notifyRetrievalResultListeners(results);
		} catch (Exception e)
		{
			log.log(Level.SEVERE, "retrieval failed", e);
			notifyRetrievalResultListeners(null);
		}
	}
	
	public RetrievalResults retrieveGuess(DefaultQuery query, SimMap modelToSMF, boolean useExp) throws Exception {
		RetrievalResults result = retrieve(query, modelToSMF, useExp);
		return result;
	}
}
