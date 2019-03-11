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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.casebase.CaseInstance;
import de.dfki.mycbr.model.vocabulary.Filter;
import de.dfki.mycbr.model.vocabulary.FilterHandler;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.model.vocabulary.SpecialValueHandler;

/**
 * 
 * @author myCBR Team
 * 
 */
public class DefaultQuery extends HashMap<ModelSlot,Object> implements Query
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

//	private final static Logger log = Logger.getLogger ( DefaultQuery.class.getName () );

	private ModelCls cls;
	private boolean isDefined = false;
	private HashMap<ModelSlot, Filter> filterMap = null;
	private HashMap<ModelSlot, Double> weightMap = null;

	private int weightMode = Query.WEIGHT_MODE_CLASS_ONLY;
	
	
	public DefaultQuery(ModelCls cls) 
	{
		this.cls = cls; 
		for (Iterator< ModelSlot > it=cls.listSlots().iterator(); it.hasNext();)
		{
			ModelSlot slot = it.next();
			
			Object value = null;
			if (slot.getValueType()==ValueType.INSTANCE)
			{
				ModelCls subCls = Helper.getSubCls(slot);
				if (subCls!=null) value = new DefaultQuery(subCls);
			}
			else
			{
				value = SpecialValueHandler.SPECIAL_VALUE_UNDEFINED;
			}
			put(slot, value);
		}
	}

	public DefaultQuery(CaseInstance caseInstance)
	{
		this.cls = caseInstance.getModelCls();
		SpecialValueHandler specialValueHandler = SpecialValueHandler.getInstance();
		
		// copy values
		for (Iterator<ModelSlot> it=caseInstance.listSlots().iterator(); it.hasNext();)
		{
			ModelSlot slot = it.next();

			Object value = null;
			if (slot.getValueType()==ValueType.INSTANCE)
			{
				ModelCls subCls = Helper.getSubCls(slot);
				if (subCls != null)
				{
					Object slotValue = caseInstance.getSlotValue(slot);
//					value = (Helper.isSpecialValue(slotValue)? new DefaultQuery(subCls): new DefaultQuery((CaseInstance) slotValue));
					value = (specialValueHandler.isSpecialValue(slotValue)? new DefaultQuery(subCls): new DefaultQuery((CaseInstance) slotValue));
				}
			}
			else
			{
				value = caseInstance.getSlotValue(slot);
			}
			put(slot, value);
		}
	}
	
	// Copy constructor
	@SuppressWarnings("unchecked")
	public DefaultQuery(DefaultQuery query)
	{
		this.cls = query.cls;
		SpecialValueHandler specialValueHandler = SpecialValueHandler.getInstance();
		this.weightMode = query.weightMode;
		if (query.weightMap!=null) this.weightMap = new HashMap<ModelSlot, Double>(query.weightMap);
		
		// copy values
		for (Iterator<ModelSlot> it=cls.listSlots().iterator(); it.hasNext();)
		{
			ModelSlot slot = it.next();

			Object value = null;
			if (slot.getValueType()==ValueType.INSTANCE)
			{
				ModelCls subCls = Helper.getSubCls(slot);
				if (subCls != null)
				{
					Object slotValue = query.getSlotValue(slot);
//					value = (Helper.isSpecialValue(slotValue)? new DefaultQuery(subCls): new DefaultQuery((DefaultQuery) slotValue));
					value = (specialValueHandler.isSpecialValue(slotValue)? new DefaultQuery(subCls): new DefaultQuery((DefaultQuery) slotValue));
				}
			}
			else
			{
				value = query.getSlotValue(slot);
			}
			put(slot, value);
		}
		
		if (query.filterMap!=null)
		{
			filterMap= new HashMap();
			filterMap.putAll(query.filterMap);
		}
	}

	
	@SuppressWarnings("unchecked")
	public DefaultQuery(ModelCls cls, HashMap slotsToValues)
	/**
	 * @param slotsToValues maps slots (NOT their name!) to values!
	 */
	{
		this.cls = cls;
		putAll(slotsToValues);
	}

	
	public DefaultQuery(ModelCls cls, int weightMode)
	{
		this(cls);
		setWeightMode(weightMode);
	}
	public DefaultQuery(CaseInstance caseInstance, int weightMode)
	{
		this(caseInstance);
		setWeightMode(weightMode);
	}
	@SuppressWarnings("unchecked")
	public DefaultQuery(ModelCls cls, HashMap slotsToValues, int weightMode)
	{
		this(cls, slotsToValues);
		setWeightMode(weightMode);
	}
	
	public HashMap<ModelSlot, Double> getWeights() {
		return weightMap;
	}
	
	public HashMap<ModelSlot, Filter> getFilters() {
		return filterMap;
	}
	
	public void setWeights(HashMap<ModelSlot, Double> weights) {
		weightMap = weights;
	}
	
	public void setFilters(HashMap<ModelSlot, Filter> filters) {
		filterMap = filters;
	}
	
	
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (Iterator< ModelSlot > it=cls.listSlots().iterator(); it.hasNext();)
		{
			ModelSlot slot = (ModelSlot)it.next();
			result.append(slot.getName() + ": " + get(slot).toString() + "\n");
		}
		return result.toString();
	}

	public Object getSlotValue(ModelSlot slot)
	{
		return get(slot);
	}

	public void setSlotValue(ModelSlot slot, Object value)
	{
		put(slot, value);
	}
	

	public int getWeightMode()
	{
		return weightMode;
	}

	public void setWeightMode(int weightMode)
	{
		this.weightMode = weightMode;
	}
	
	public Double getWeight(ModelSlot slot)
	{
		if (weightMap==null) return 1d;
		Double weight = weightMap.get(slot);
		return (weight==null? 1d: weight);
	}
	
	public void setQueryWeight(ModelSlot slot, Double weight)
	{
		if (weightMap==null)
		{
			weightMap= new HashMap<ModelSlot, Double>();
		}
		weightMap.put(slot, weight);
	}
	

	public ModelCls getModelCls()
	{
		return cls;
	}

	public boolean isDefined()
	{
		return isDefined ;
	}

	public Object put(ModelSlot key, Object val)
	{
		if (val!=SpecialValueHandler.SPECIAL_VALUE_UNDEFINED) isDefined = true; 
		return super.put(key, val);
	}
	
	
	public void setFilter(ModelSlot slot, Filter filter)
	{
		if (filterMap==null)
		{
			filterMap = new HashMap<ModelSlot, Filter>();
		}
		if (filter == FilterHandler.FILTER_SIMILAR)
		{
			filterMap.remove(slot);
			return;
		}
		filterMap.put(slot, filter);
	}
	
	public Filter getFilter(ModelSlot slot)
	{
		if (filterMap==null) return FilterHandler.FILTER_SIMILAR;
		Filter filter = filterMap.get(slot);
		if (filter==null) filter = FilterHandler.FILTER_SIMILAR;
		return filter;
	}

	public HashMap<ModelSlot, Filter> getFilterMap()
	{
		return filterMap;
	}
	

	/**
	 * Think of composition (OO)
	 * All the filters that are defined in sub queries are put into the filter map of the parent.
	 * This is used by the retrieval engine.
	 */
	public void putAllFiltersInOneMap()
	{
		SpecialValueHandler specialValueHandler = SpecialValueHandler.getInstance();
		for (Object o: values())
		{
			if (!(o instanceof DefaultQuery) || o==null || specialValueHandler.isSpecialValue(o)) continue;
			DefaultQuery subQuery = (DefaultQuery)o;
			subQuery.putAllFiltersInOneMap();
			if (!subQuery.filtersDefined()) continue;
			if (filterMap==null) filterMap=subQuery.getFilterMap(); else filterMap.putAll(subQuery.getFilterMap());
		}
	}
	
	public boolean filtersDefined()
	{
		SpecialValueHandler specialValueHandler = SpecialValueHandler.getInstance();
		if (filterMap!=null) return true; 
		for (Object o: values())
		{
			if (!(o instanceof DefaultQuery) || o==null || specialValueHandler.isSpecialValue(o)) continue;
			if (((DefaultQuery)o).filtersDefined()) return true;
		}
		return false;
	}

	public String getName()
	{
		return "Query";
	}

	@SuppressWarnings("unchecked")
	public Collection listSlots()
	{
		return cls.listSlots();
	}
	
}
