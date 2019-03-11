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
package de.dfki.mycbr.model.similaritymeasures.smftypes;

import java.awt.Frame;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;

import de.dfki.mycbr.CBRProject;
import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.casebase.CaseInstance;
import de.dfki.mycbr.model.similaritymeasures.AbstractClassSM;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.IMF_Standard;
import de.dfki.mycbr.model.similaritymeasures.SMFContainer;
import de.dfki.mycbr.model.similaritymeasures.SMFHolder;
import de.dfki.mycbr.model.similaritymeasures.SimMap;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel_Class_Standard;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.model.vocabulary.SpecialValueHandler;
import de.dfki.mycbr.modelprovider.ModelProvider;
import de.dfki.mycbr.retrieval.DefaultQuery;
import de.dfki.mycbr.retrieval.Explanation;
import de.dfki.mycbr.retrieval.Query;
import de.dfki.mycbr.retrieval.ui.RetrievalWidget;

/**
 * @author myCBR Team
 *
 */
public class SMF_Class_Standard extends AbstractClassSM {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(SMF_Class_Standard.class.getName());

	/**
	 * Slot info: name, weight, comment, discriminant, owning cls. 
	 * @author Daniel Bahls
	 */
	@SuppressWarnings("unchecked")
	public class SlotAmalgamation implements Comparable,Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private double weight;

		private boolean enabled;

		private String comment = "";

		private ModelSlot slot;

		private SMF_Class_Standard owner;

		private boolean inherit = false;

		public SlotAmalgamation(SMF_Class_Standard owner, ModelSlot slot) {
			weight = 1;
			enabled = true;
			inherit = false;
			this.slot = slot;
			this.owner = owner;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			hasChanged();
			this.enabled = enabled;
			RetrievalWidget.notifyWhenActiveSlotsChanged();
			
		}

		public double getWeight() {
			return weight;
		}

		public void setWeight(double weight) {
			hasChanged();
			this.weight = weight;
		}

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			hasChanged();
			this.comment = comment;
		}

		public int compareTo(Object o) {
			return slot.getName().compareTo(o.toString());
		}

		public String toString() {
			return slot.getName();
		}

		public ModelSlot getSlot() {
			return slot;
		}

		public void hasChanged() {
			log.fine("SlotAmalgamation : has changed");
			owner.setHasChanged(true);
		}

		public SlotAmalgamation copy() {
			SlotAmalgamation saCopy = new SlotAmalgamation(owner, slot);
			saCopy.enabled = this.enabled;
			saCopy.comment = this.comment;
			saCopy.weight = this.weight;
			saCopy.owner = this.owner;
			saCopy.inherit = this.inherit;

			return saCopy;
		}

		public void setInherit(boolean b) {
			this.inherit = b;
		}
		
		public boolean isInherit() {
			return inherit;
		}

	}

	private static String COMMENT_NO_LOCAL_SMF = "no local similarity measure available.";

	private static final String XML_TAG_SLOT_AMALGAMATION = "Slot"; 
	private static final String XML_ATT_AMALGAMATION_MODE = "amalgamation"; 
	private static final String XML_ATT_SLOTNAME = "slotname"; 
	private static final String XML_ATT_ENABLED  = "enabled"; 
	private static final String XML_ATT_WEIGHT   = "weight"; 
	private static final String XML_ATT_COMMENT  = "comment"; 
	private static final String XML_ATT_LOCALSMF = "localSMF"; 

	public static final String AMALGAMATION_MODE_EUCLIDEAN = "euclidean";

	public static final String AMALGAMATION_MODE_MAXIMUM = "maximum";

	public static final String AMALGAMATION_MODE_MINIMUM = "minimum";

	public static final String AMALGAMATION_MODE_WEIGHTEDSUM = "weighted_sum";

	public static final String AMALGAMATION_MODE_INHERIT = "inherit_from_superclass";

	// integer values for the above strings are used in the loops for performance;
	// a string table translates between them; order is important!
	private static final int AMAL_EUCL = 0;
	private static final int AMAL_MAX = 1;
	private static final int AMAL_MIN = 2;
	private static final int AMAL_SUM = 3;
	private static final int AMAL_INHERIT = 4;
	private static int amalStrToInt(String mode) {
		if (AMALGAMATION_MODE_EUCLIDEAN.equals(mode)) return AMAL_EUCL;
		if (AMALGAMATION_MODE_MAXIMUM.equals(mode)) return AMAL_MAX;
		if (AMALGAMATION_MODE_MINIMUM.equals(mode)) return AMAL_MIN;
		if (AMALGAMATION_MODE_WEIGHTEDSUM.equals(mode)) return AMAL_SUM;
		if (AMALGAMATION_MODE_INHERIT.equals(mode)) return AMAL_INHERIT;
		assert(false);
		return -1;
	}

	private static String amalIntToStr(int mode) {
		String[] lookup = { AMALGAMATION_MODE_EUCLIDEAN, AMALGAMATION_MODE_MAXIMUM,
				AMALGAMATION_MODE_MINIMUM, AMALGAMATION_MODE_WEIGHTEDSUM,
				AMALGAMATION_MODE_INHERIT
		};
		return lookup[mode];
	}

	public static final ValueType VALUE_TYPE = ValueType.CLS;

	private int amalgamationMode;

	/** contains SlotAmalgamation objects */
	private ArrayList<SlotAmalgamation> slotList = new ArrayList<SlotAmalgamation>();

	private Map<ModelInstance, String> modelToSMFname = new HashMap<ModelInstance, String>(); // maps 

	private ClsToSMFTableInheritance cachedInheritanceTableSMF;

	private class SpecialComparator implements Comparator<SlotAmalgamation>, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public int compare(SlotAmalgamation sa1, SlotAmalgamation sa2) {
			if (sa1.isInherit() && sa2.isInherit()) return sa1.compareTo(sa2);
			if (sa1.isInherit() && !sa2.isInherit()) return -1;
			if (!sa1.isInherit() && sa2.isInherit()) return 1;
			
			return sa1.compareTo(sa2);
		}
	}

	private Comparator<SlotAmalgamation> slotListComparator = new SpecialComparator();
	
	private boolean hasSetupInheritanceConfig = false;
	private ArrayList<SlotAmalgamation> slotList_retrieval = null;
	
	public SMF_Class_Standard(ModelInstance inst, String smfName) throws Exception {
		super(inst, smfName);

		slotList = new ArrayList<SlotAmalgamation>();

		for (Iterator<ModelSlot> it = cls.listSlots().iterator(); it.hasNext();) {
			ModelSlot ms = it.next();

			SlotAmalgamation sa = new SlotAmalgamation(this, ms);
			SMFHolder holder = SMFContainer.getInstance().getSMFHolderForModelInstance(ms);
			if (holder == null || holder.size() == 0) {
				sa.setEnabled(false);
				sa.setComment(COMMENT_NO_LOCAL_SMF);
			}

			slotList.add(sa);
		}

		checkInheritance();
		
	}
	
	private void checkInheritance() {
		log.fine("check inheritance for class ["+cls+"], smf ["+getSmfName()+"]");
		// connect to superclass
		if (inheritable()) {
			ModelCls superCls = cls.getSuperCls();
			for (Iterator<SlotAmalgamation> itSA = slotList.iterator(); itSA.hasNext();) {
				SlotAmalgamation sa = itSA.next();

				boolean canInherit = false;
				for (Iterator<ModelSlot> itSlot = superCls.listSlots().iterator(); itSlot.hasNext();) {
					ModelSlot slot = itSlot.next();
					if (sa.getSlot()==slot) {
						canInherit=true;
							break;
					}
				}
				log.fine("Cls [" + cls + "]. Can inherit [" + sa.getSlot() + "] from superCls [" + superCls + "] : " + canInherit);
				sa.setInherit(canInherit);
			}
		} else {
			// we cannot inherit smfunction from super concept.
			for (Iterator<SlotAmalgamation> itSA = slotList.iterator(); itSA.hasNext();) {
				SlotAmalgamation sa = itSA.next();
				sa.setInherit(false);
			}
		}

		Collections.sort(slotList, slotListComparator);
	}

	@SuppressWarnings("unchecked")
	public SMF_Class_Standard(ModelInstance inst, Element smfElement) throws JDOMException {
		super(inst, smfElement);

		amalgamationMode = amalStrToInt(smfElement.getAttributeValue(XML_ATT_AMALGAMATION_MODE));

		// read slot amalgamation (table entries)
		for (Iterator it=smfElement.getDescendants(new ElementFilter(XML_TAG_SLOT_AMALGAMATION)); 
				it.hasNext();) {
			Element saElement = (Element)it.next();

			String slotName = Helper.decode(saElement.getAttributeValue(XML_ATT_SLOTNAME));
			SlotAmalgamation sa = new SlotAmalgamation(this, (ModelSlot)ModelProvider.getInstance().getModelInstance(slotName));
			sa.setComment(Helper.decode(saElement.getAttributeValue(XML_ATT_COMMENT)));
			sa.setEnabled(saElement.getAttribute(XML_ATT_ENABLED).getBooleanValue());
			sa.setWeight(saElement.getAttribute(XML_ATT_WEIGHT).getDoubleValue());

			slotList.add(sa);

			if (saElement.getAttribute(XML_ATT_LOCALSMF) != null) {
				String locSmfName = Helper.decode(saElement.getAttributeValue(XML_ATT_LOCALSMF));
				setLocalSMF(sa.getSlot(), locSmfName);
			}

		}
		
		checkInheritance();
		
		setHasChanged(false);
	}

	public void toXML(Element xmlElement) {
		xmlElement.setAttribute(XML_ATT_AMALGAMATION_MODE, Helper.encode(amalIntToStr(amalgamationMode)));

		for (Iterator< SlotAmalgamation > it=slotList.iterator(); it.hasNext();) {
			SlotAmalgamation sa = it.next();
			Element saElement = new Element(XML_TAG_SLOT_AMALGAMATION);

			saElement.setAttribute(XML_ATT_SLOTNAME, Helper.encode(sa.getSlot().getName()));
			saElement.setAttribute(XML_ATT_ENABLED,  Boolean.toString(sa.isEnabled()));
			saElement.setAttribute(XML_ATT_WEIGHT,   Double.toString(sa.getWeight()));
			saElement.setAttribute(XML_ATT_COMMENT,  Helper.encode(sa.getComment()));

			String localSMFname = getLocalSMF(sa.getSlot());
			if (localSMFname != null) {
				saElement.setAttribute(XML_ATT_LOCALSMF, Helper.encode(localSMFname));
			}

			xmlElement.addContent(saElement);
		}
	}

	@SuppressWarnings("unchecked")
	public AbstractSMFunction copy() {
		SMF_Class_Standard newSmf = null;

		try {
			log.fine("COPY :: cls is [" + cls + "]");
			newSmf = new SMF_Class_Standard(cls, smfName);
			newSmf.amalgamationMode = amalgamationMode;

			ArrayList<SlotAmalgamation> slCopy = new ArrayList<SlotAmalgamation>();
			for (Iterator<SlotAmalgamation> it = slotList.iterator(); it.hasNext();) {
				SlotAmalgamation sa = it.next();
				SlotAmalgamation sacopy = sa.copy();
				sacopy.owner = newSmf;
				slCopy.add(sacopy);
			}
			Collections.sort(slCopy);
			newSmf.slotList = slCopy;
			newSmf.modelToSMFname = new HashMap<ModelInstance, String>(modelToSMFname);

			newSmf.setHasChanged(false);

		} catch (Exception e) {
			log.log(Level.SEVERE, "error while copying!", e);
		}

		return newSmf;
	}

	protected SMFPanel createSMFPanel() {
		return new SMFPanel_Class_Standard(this);
	}


	@SuppressWarnings("unchecked")
	public boolean checkConsistency(Frame parentframe, boolean quiet) {
		ArrayList< SlotAmalgamation > deletedSlots = (ArrayList<SlotAmalgamation>) slotList.clone();

		// check for new or deleted slots.
		for (Iterator< ModelSlot > it=cls.listSlots().iterator(); it.hasNext();) {
			ModelSlot slot = (ModelSlot)it.next();

			boolean isNew = true;
			// try to find this slot in slot amalgamation list
			for (Iterator<SlotAmalgamation> saIt = slotList.iterator(); saIt.hasNext();) {
				SlotAmalgamation sa = saIt.next();
				if (slot.equals(sa.getSlot())) {
					// remove slot from deleted slots list
					deletedSlots.remove(sa);
					isNew = false;
					break;
				}
			}
			if (isNew) {
				slotList.add(new SlotAmalgamation(this, slot));
			}
		}

		// deleted slots
		if (deletedSlots.size() > 0) {

			// delete idle slot amalgamations.
			for (Iterator delSlotIt = deletedSlots.iterator(); delSlotIt.hasNext();) {
				SlotAmalgamation slot = (SlotAmalgamation) delSlotIt.next();
				slotList.remove(slot);
			}
		}

		// check for similarity functions for each slot.
		ArrayList slotsWithoutSMF = new ArrayList();
		for (Iterator it = slotList.iterator(); it.hasNext();) {
			SlotAmalgamation sa = (SlotAmalgamation) it.next();
			ModelSlot slot = sa.getSlot();

			SMFHolder holder = CBRProject.getInstance().getSpecialSMFHolderForOOComposition(slot);
			if (holder == null) {
				holder = SMFContainer.getInstance().getSMFHolderForModelInstance(slot);
			}

			if (holder == null || holder.getActiveSMF() == null) {
				log.fine("no smf available for [" + slot.getName() + "]. so disable it.");
				if (sa.isEnabled()) {
					sa.setEnabled(false);
					sa.setComment(COMMENT_NO_LOCAL_SMF);
					slotsWithoutSMF.add(slot);
				}
			} else {
				if (sa.getComment().indexOf(COMMENT_NO_LOCAL_SMF) >= 0) {
					sa.setComment("");
					sa.setEnabled(true);
				}
			}
		}
		if (slotsWithoutSMF.size() > 0) {
			StringBuffer sb = new StringBuffer();
			for (Iterator it=slotsWithoutSMF.iterator(); it.hasNext();) {
				sb.append(" - " + ((ModelSlot)it.next()).getName() + "\n");
			}

			if (parentframe != null && !quiet) {
				JOptionPane.showMessageDialog(parentframe, "No similarity function defined for:\n" + sb.toString(), "Consistency check for class ["+cls.getName()+"]", JOptionPane.INFORMATION_MESSAGE);
			}
		}

		checkInheritance();
		
		if (editorPanel != null && editorPanel instanceof SMFPanel_Class_Standard) {
			SMFPanel_Class_Standard panel = (SMFPanel_Class_Standard)editorPanel;
			panel.updateTableModel();
			panel.refresh();
		}
		
		return true;
	}

	public static String getSMFunctionTypeName_static() {
		return "Standard";
	}

	// since SMF_Class_Standard is used directly by the retrieval engine, we provide a method
	// that computes the similarity and that supports different options: 
	// flags that are supported by compareModelCls: 
	public static final int CMP_IGNORE_UNDEFINED_IN_QUERY 	= 2;
	public static final int CMP_IGNORE_UNDEFINED_IN_CASE 	= 8;
	public static final int CMP_IGNORE_INHERITANCE 			= 4; // set this flag to disable
	  // computation that takes complicated inheritance rules into account
	public double compareModelCls(Query query, CaseInstance caseInst, Explanation thisExp, 
																SimMap modelToSMF, int flags) throws Exception {
		if (!hasSetupInheritanceConfig)	{
			//SMF_Class_Standard superClsSMF = null;
			if (cls.getSuperCls()!=null) {
				HashMap<ModelSlot, SlotAmalgamation> slotList_retrievalMap = findInheritedSlotAmalgamations(cls, modelToSMF); 
				slotList_retrieval = new ArrayList<SlotAmalgamation>(slotList_retrievalMap.values());
			} else {
				slotList_retrieval = slotList;
			}
			hasSetupInheritanceConfig = true;
		}

		//
		// first, find class based similarity (without inheritance structure)
		//
		double allWeights = 0.0;
		double allWeightsSquare = 0.0;
		double result_amalgamation = 0.0;

		double res = 0.0; // temporary result; has to be divided by something later
		switch (amalgamationMode) {
			case AMAL_EUCL: res = 0.0; break;
			case AMAL_MAX:  res = Double.MIN_VALUE; break;
			case AMAL_MIN:  res = Double.MAX_VALUE; break;
			case AMAL_SUM:  res = 0.0; break;
		}

		for (Iterator<SlotAmalgamation> it = slotList_retrieval.iterator(); it.hasNext();) {
			SlotAmalgamation sa = it.next();

			ModelSlot slot = sa.getSlot();
			Object queryVal = query.getSlotValue(slot);
			Object caseVal  = caseInst.getSlotValue(slot);
			AbstractSMFunction smf = (AbstractSMFunction) modelToSMF.get(slot);

			//
			// Explanation
			Explanation childExp = null;
			if (thisExp != null) {
				childExp = new Explanation(slot, queryVal, caseVal, smf);
				childExp.setWeight(sa.getWeight());
				thisExp.addExplanationChild(slot, childExp);
			}

			if (!caseInst.getModelCls().listSlots().contains(slot))	{
				if (childExp != null) {
					childExp.addComment("amalgamation", "The type of this case [" + caseInst.getModelCls() + "] has no such slot [" + slot + "]");
					childExp.setSimilarity(Double.NaN);
				}
				continue;
			}


			double queryWeight = query.getWeight(slot);
			if (queryWeight <= 0d) {
				childExp.addComment("amalgamation", "Query weight is zero.");
				childExp.setSimilarity(Double.NaN);
			} else if (!sa.isEnabled() && childExp != null) {
				childExp.addComment("amalgamation", "The local similarity measure for this attribute is not available / enabled.");
				childExp.setSimilarity(Double.NaN);
			} else {
				double weight = 1d;
				switch (query.getWeightMode()) {
					case Query.WEIGHT_MODE_CLASS_ONLY: 	weight = sa.getWeight(); break;
					case Query.WEIGHT_MODE_QUERY_ONLY: 	weight = query.getWeight(slot); break;
					case Query.WEIGHT_MODE_MULTIPLY: 	weight = sa.getWeight() * query.getWeight(slot); break;
				}

				double tmpSim = 0;
				if (weight>0 && sa.isEnabled())	{
					//
					// Special Values
					//
					if (specialValueHandler.isSpecialValue(queryVal) || specialValueHandler.isSpecialValue(caseVal)
							|| queryVal == null || caseVal == null) {
						// if undefined values should be ignored
						// just continue and check next slot values.
						if ((flags & CMP_IGNORE_UNDEFINED_IN_QUERY) != 0 && queryVal==SpecialValueHandler.SPECIAL_VALUE_UNDEFINED 
								|| (flags & CMP_IGNORE_UNDEFINED_IN_CASE) != 0 && caseVal==SpecialValueHandler.SPECIAL_VALUE_UNDEFINED) {
							if (childExp != null) {
								childExp.addComment("Project Options", "Undefined values in query or case are ignored.");
								childExp.setSimilarity(Double.NaN);
							}
							continue;
						}

						Object tmpQuery = (queryVal instanceof DefaultQuery && !((DefaultQuery)queryVal).isDefined()? 
								SpecialValueHandler.SPECIAL_VALUE_UNDEFINED : queryVal);
						tmpSim = specialValueHandler.getSimilarityBetween(tmpQuery, caseVal, childExp);
					} else {
						//
						// get slot similarity
						//
						if (smf.getValueType() == ValueType.CLS) {
							smf = getCommonAncestorsSMF((DefaultQuery)queryVal, (CaseInstance) caseVal, smf);
						}
						tmpSim = smf.getSimilarityBetween(queryVal, caseVal, childExp);
						
						if (Double.isNaN(tmpSim)) continue;

					}
					double attSim = tmpSim * weight;

					allWeights += weight;
					allWeightsSquare += weight * weight;
					//allConsideredSlots++;
					switch (amalgamationMode) {
						case AMAL_EUCL: res += tmpSim * tmpSim * weight; break;
						case AMAL_MAX:  if (res < tmpSim) res = tmpSim; break;
						case AMAL_MIN:	if (res > tmpSim) res = tmpSim; break;
						case AMAL_SUM:  res += attSim; break;
					}
				} else {
					// ignore, because weight = 0
				}
			}

			// then apply the mode
			switch (amalgamationMode) {
				case AMAL_SUM:
					result_amalgamation = res / allWeights;
					break;
				case AMAL_EUCL: 
					result_amalgamation = Math.sqrt(res / allWeights);	        	
					break;
				case AMAL_MAX: case AMAL_MIN:
					result_amalgamation = res;
					break;
			}	        
		}

		if (allWeights == 0.0) {
			result_amalgamation = Double.NaN;
		}

		//
		// now, find structural similarity (by inheritance structure)
		// but only, if not special flag set
		double result_total = result_amalgamation;
		if ((flags & CMP_IGNORE_INHERITANCE) == 0) {
			IMF_Standard imf = CBRProject.getInstance().getIMFforModelCls(cls.getName());
			
			if (imf != null) {
				double result_structure = imf.getSimilarityBetween(query.getModelCls(), caseInst.getModelCls(), thisExp);
				result_total *= result_structure;
			}
		}
		//
		// decorate explanation
		//
		if (thisExp != null) {
			thisExp.addComment("amalgamation", "Class = ["+cls.getName()+"], mode = "+amalIntToStr(amalgamationMode));
			if (allWeights == 0)  {
				thisExp.addComment("amalgamation", "Could not determine a similarity value.");
			} else {
				thisExp.addComment("amalgamation", "similarity = ["+Helper.formatDoubleAsString(result_amalgamation)+"]");
			}
			thisExp.setSimilarity(result_total);
		}
		return result_total;
	}
	
	private AbstractSMFunction getCommonAncestorsSMF(DefaultQuery query, CaseInstance ci, AbstractSMFunction smf) {
		ModelCls clsQ = query.getModelCls();
		ModelCls clsC = ci.getModelCls();
		return cachedInheritanceTableSMF.getCommonSMF(clsQ, clsC);
	}

	public int getFlags() {
		int flags = 0;
		if (CBRProject.getInstance().getProjectOptions().isIgnoreUndefinedInRetrieval_Query()) {
			flags |= CMP_IGNORE_UNDEFINED_IN_QUERY;
		}
		if (CBRProject.getInstance().getProjectOptions().isIgnoreUndefinedInRetrieval_Case()) {
			flags |= CMP_IGNORE_UNDEFINED_IN_CASE;
		}
		return flags;
	}
	
	public double getSimilarityBetween(Object q, Object cb, Explanation thisExp) throws Exception {
		return compareModelCls((Query) q, (CaseInstance) cb, thisExp, this.getSimMap(), getFlags()); 
	}

	private HashMap<ModelSlot, SlotAmalgamation> findInheritedSlotAmalgamations(ModelCls cls, SimMap modelToSMF) {
		ModelCls superCls = cls.getSuperCls();
		HashMap<ModelSlot, SlotAmalgamation> slotListMap = null;
		if (superCls != null) {
			slotListMap = findInheritedSlotAmalgamations(superCls, modelToSMF);
		} else {
			slotListMap = new HashMap<ModelSlot, SlotAmalgamation>();
		}

		SMF_Class_Standard smf = (SMF_Class_Standard) modelToSMF.get(cls);
		if (smf == null) {
			return slotListMap;
		}
		
		for (Iterator<SlotAmalgamation> it = smf.slotList.iterator(); it.hasNext();) {
			SlotAmalgamation sa = it.next();
			if (!slotListMap.containsKey(sa.getSlot())) {
				slotListMap.put(sa.getSlot(), sa);
			}
		}
		return slotListMap;
	}

	public ValueType getValueType() {
		return VALUE_TYPE;
	}

	public void setAmalgamationMode(String mode) {
		amalgamationMode = amalStrToInt(mode);
		setHasChanged(true);
	}

	public String getAmalgamationMode() {
		return amalIntToStr(amalgamationMode);
	}

	public boolean inheritable() {
		ModelCls superCls = cls.getSuperCls();

		SMFHolder superClsHolder = SMFContainer.getInstance().getSMFHolderForModelInstance(superCls);
		return (superClsHolder != null && superClsHolder.keySet().size() > 0);
	}

	public String getSuperAmalgamationMode() {
		ModelCls superCls = cls.getSuperCls();

		SMFHolder superClsHolder = SMFContainer.getInstance().getSMFHolderForModelInstance(superCls);
		SMF_Class_Standard superSmf = (SMF_Class_Standard) superClsHolder.getActiveSMF();

		int superMode = superSmf.amalgamationMode;
		if (superMode == AMAL_INHERIT) {
			superMode = amalStrToInt(superSmf.getSuperAmalgamationMode());
		}
		
		return amalIntToStr(superMode);
	}

	public ArrayList<SlotAmalgamation> getSlotList() {
		return slotList;
	}

	public SlotAmalgamation getSlotAmalgamation(ModelSlot slot) {
		return getSlotAmalgamationRecursive(slot, this);
	}
	
	private SlotAmalgamation getSlotAmalgamationRecursive(ModelSlot slot, SMF_Class_Standard smf) {
		for (SlotAmalgamation sa : smf.getSlotList()) {
			if (sa.getSlot().equals(slot)) { // == slot) {
				return sa;
			}
		}
		System.out.println(3);
		ModelInstance cls = smf.getModelInstance();
		if (cls instanceof ModelCls) {
			System.out.println("is cls");
			ModelCls superCls = ((ModelCls)cls).getSuperCls();
			if (superCls != null) {
				System.out.println("has super cls");
				SMFHolder superClsHolder = SMFContainer.getInstance().getSMFHolderForModelInstance(superCls);
				SMF_Class_Standard superSmf = (SMF_Class_Standard) superClsHolder.getActiveSMF();
				return getSlotAmalgamationRecursive(slot, superSmf);
			}
		}
		return null;
	}
	
	public double getTotalWeightAmalgamationWeight() {
		double total = 0d;
		for (SlotAmalgamation sa : slotList) {
			if (sa.isEnabled()) {
				total += sa.getWeight();
			}
		}
		return total;
	}
	
	public void setLocalSMF(ModelSlot slot, String localSMFname) {
		modelToSMFname.put(slot, localSMFname);
		if (localSMFname == null) {
			modelToSMFname.remove(slot);
		}
	}

	public String getLocalSMF(ModelSlot slot) {
		assert(modelToSMFname != null);
		assert(slot != null);
		return (String) modelToSMFname.get(slot);
	}

	public SimMap getSimMap() {
		// we have to lookup here the SMF belonging to its name: 
		SimMap result = new SimMap();
		
		CBRProject cbrProject = CBRProject.getInstance();
		
		// use default's:
		for (Iterator<ModelSlot> it = this.cls.listSlots().iterator(); it.hasNext(); ) {
			ModelSlot slot = (ModelSlot) it.next();
			SMFHolder holder = null;
			AbstractSMFunction esmf = null;
			String chosenSMFname = modelToSMFname.get(slot);
			if (slot.getValueType() == ValueType.INSTANCE) {
				holder = cbrProject.getSpecialSMFHolderForOOComposition(slot);
				esmf = (chosenSMFname==null? holder.getActiveSMF(): holder.getCertainSMFunctionFromHolder(chosenSMFname));
				if (esmf != null) {
					result.putAll(((AbstractClassSM)esmf).getSimMap());
					result.put(slot, cbrProject.checkWrapMMF(esmf));
					result.put(esmf.getModelInstance(), cbrProject.checkWrapMMF(esmf));
				}
			} else {
				holder = SMFContainer.getInstance().getSMFHolderForModelInstance(slot);
				esmf = (chosenSMFname==null ? holder.getActiveSMF() : holder.getCertainSMFunctionFromHolder(chosenSMFname));
				if (esmf != null) {
					result.put(slot, cbrProject.checkWrapMMF(esmf));
				}
			}						
		}
		
		result.put(this.cls, this); 
		
		cachedInheritanceTableSMF = new ClsToSMFTableInheritance();
		return result;
	}
	
}
