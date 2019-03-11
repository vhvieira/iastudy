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
package de.dfki.mycbr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultCellEditor;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import de.dfki.mycbr.model.casebase.CaseInstance;
import de.dfki.mycbr.model.similaritymeasures.AbstractClassSM;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.SMFContainer;
import de.dfki.mycbr.model.similaritymeasures.SMFHolder;
import de.dfki.mycbr.model.similaritymeasures.SimMap;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.retrieval.DefaultQuery;
import de.dfki.mycbr.retrieval.Explanation;
import edu.stanford.smi.protege.util.BrowserLauncher;

/**
 * Helper class for static helper methods.
 * 
 * @author myCBR Team
 *
 */
public class Helper {
	
	private static final Logger log = Logger.getLogger(Helper.class.getName());
	public static final Color COLOR_RED_MYCBR = new Color(196,0,9);
    
	public static void browse(String url) throws IOException {
		BrowserLauncher.openURL(url);
	}
	
	public static JDialog createDialog(Window parent, String title, boolean modal) {
		JDialog dialog = null;
		if (parent instanceof Frame) {
			dialog =  new JDialog((Frame) parent, title, false);
			dialog.setAlwaysOnTop(true);
		}
		
		if (parent instanceof Dialog) {
			dialog =  new JDialog((Dialog) parent, title, false);
			dialog.setAlwaysOnTop(true);
		}
		return dialog;
	}
	
	public static String formatDoubleAsString( Object value ) {
		Double d = 0.0;
		if (value instanceof String) {
			try { 
				d = Double.parseDouble((String)value);
			} catch (NumberFormatException e) {
				log.info("Unable to parse String " + value + " to Double");
			}
		} else if (value instanceof Double) {
			d = (Double) value;
		} else if (value instanceof Integer) {
			d = (1.0 * (Integer)value);
		} else {
			log.severe("Unable to parse " + Object.class + " " + value + " to Double");
		}
		
		NumberFormat nf = NumberFormat.getNumberInstance( new Locale("en", "US") );
		DecimalFormat df = (DecimalFormat)nf;
		df.applyPattern( "###,##0.##");
		
		return df.format(d);
	}
	
	public static Object getCorrespondingObject(DefaultMutableTreeNode node, CaseInstance ci) {
		TreeNode[] path = node.getPath();

		Object currentObject = ci;
		
		for (int i=1; i<path.length-1 && currentObject!=null; i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) path[i];
			
			ModelSlot slot = (ModelSlot) child.getUserObject();
			currentObject = ((CaseInstance) currentObject).getSlotValue(slot);
		}
		return currentObject;
	}
	
	
	public static Object getCorrespondingObject(DefaultMutableTreeNode node, DefaultQuery query) {
		TreeNode[] path = node.getPath();

		Object currentObject = query;
		
		for (int i=1; i<path.length-1 && currentObject!=null; i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) path[i];
			
			ModelSlot slot = (ModelSlot) child.getUserObject();
			currentObject = ((DefaultQuery) currentObject).getSlotValue(slot);
		}
		
		return currentObject;
	}
	
	
	/**
	 * parses a String to an int. if String is not parsable Integer.MIN_VALUE will be returned.
	 * This is useful if you don't want to care about ParseExceptions
	 * @param intStr String representation of an int
	 * @return int the int value. Integer.MIN_VALUE if the given String is not parsable. 
	 */
	public static int parseInt(String intStr) {
		int result = Integer.MIN_VALUE;
		try {
			result = Integer.parseInt(intStr);
		} catch (Exception e) {
			log.log(Level.FINE, "cannot parse String ["+intStr+"] to int.");
		}
		return result;
	}

	/**
	 * parses a String to a double. if String is not parsable (-Double.MinValue) will be returned.
	 * This is useful if you don't want to care about ParseExceptions
	 * @param doubleStr String representation of a double
	 * @return double the double value. -Double.MIN_VALUE if the given String is not parsable. 
	 */
	public static double parseDouble(String doubleStr) {
		Number result = -Double.MIN_VALUE;
		try {
//			result = Double.parseDouble(doubleStr);
			NumberFormat nf = NumberFormat.getNumberInstance( new Locale("en", "US") );
		    DecimalFormat df = (DecimalFormat)nf;
		    result = df.parse(doubleStr);
				
			log.finer("double value for ["+doubleStr+"] is : ["+result+"]");
		} catch (Exception e) {
			log.log(Level.FINE, "cannot parse String ["+doubleStr+"] to double.");
		}
		return result.doubleValue();
	}

	/**
	 * parses a String to a float. if String is not parsable (-Float.MinValue) will be returned.
	 * This is useful if you don't want to care about ParseExceptions
	 * @param floatStr String representation of a float
	 * @return float the float value. -Float.MIN_VALUE if the given String is not parsable. 
	 */
	public static float parseFloat(String floatStr) {
		float result = -Float.MIN_VALUE;
		try {
			result = Float.parseFloat(floatStr);
		} catch (Exception e) {
			log.log(Level.FINE, "cannot parse String ["+floatStr+"] to float.");
		}
		return result;
	}

	/**
	 * checks the correct spelling for a smfunction name.
	 * @param name String the name of the function
	 * @return boolean isValid
	 */
	public static boolean checkSMFName(String name) {
		boolean isValid = name.indexOf(" ") < 0;
		isValid &= name.indexOf("'") < 0;
		return isValid;
	}

	/**
	 * center the passed JFrame or JDialog in the center of the screen.
	 * 
	 * @param comp the frame or dialog to center
	 */
	public static void centerWindow(Component comp) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = comp.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		comp.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
	}

	/**
	 * Calculates trigram similarity between two Strings.
	 * 
	 * @param s1 String string1
	 * @param s2 String string2
	 * @return double between 0 and 1.
	 */
	public static double ngram (String s1, String s2, int n, boolean caseSensitive) {
		
		if(!caseSensitive) {
			s1 = s1.toLowerCase();
			s2 = s2.toLowerCase();
		}
		
    	ArrayList<String> triples_s1 = getTuples(s1, 3);
    	ArrayList<String> triples_s2 = getTuples(s2, 3);
    	
    	int matches = 0;
    	
    	for (int i=0; i<triples_s1.size(); i++) {
    		for (int j=0; j<triples_s2.size(); j++) {
    			
    			if (triples_s1.get(i).equals(triples_s2.get(j))) {
    				matches++;
    			}
    		}
    	}
    	double sim = ((double)2*matches)/((double)(triples_s1.size()+triples_s2.size()));
    	
    	return sim;
	}
	
	/**
	 * Gets Triples for trigram matching. Includes padding.
	 * @return ArrayList triples (of type String)
	 */
	private static ArrayList<String> getTuples(String s, int length) {
    	s = "_" + s.trim() + "_";
    	while (s.length() < length) {
    		s += "_";
    	}
    	
		ArrayList< String > triples = new ArrayList< String >();
		int lastIndex = s.length()-length;
		for (int i=0; i<=lastIndex; i++) {
			String tmp = s.substring(i, i+length);
			if(!triples.contains(tmp)){
				triples.add(s.substring(i, i+length));
			}
		}
		return triples;
	}
	
    /**
     * We need this method to perform an appropriate java typing.
     * Even though Protege knows about the right slots value types, it allways returns a Float object
     * when calling getMin/Max() method from slots. Funny is here, that it returns a "Number" object.
     * @param obj
     * @return
     */
    public static Object toRightType(ValueType vt, Object obj) {
    	if (obj instanceof Number) {
    		if (vt==ValueType.INTEGER) return new Integer(((Number)obj).intValue());
    	}
    	return obj;
    }
    
    public static double roundDouble(double val, int point) {
    	double factor = Math.pow(10, point);
    	val *= factor;
    	return Math.round(val)/factor;
    }
   
    /**
     * Finds the minimum number under the given Number collection.
     * @param numbers Collections of Number objects
     * @return Number the minimum of these numbers.
     */
	public static Number getMinimum(Collection< Number > numbers) {
    	if (numbers==null || numbers.size()==0) {
    		return null;
    	}
    	
    	Iterator<Number> it = numbers.iterator(); 
    	Number result = (Number) it.next();
    	
    	while (it.hasNext()) {
    		Number currentNumber = (Number) it.next();
    		if (currentNumber.doubleValue() < result.doubleValue()) {
    			result = currentNumber;
    		}
    	}
    	return result;
    }
	
    /**
     * Finds the maximum number under the given Number collection.
     * @param numbers Collections of Number objects
     * @return Number the maximum of these numbers.
     */
	public static Number getMaximum(Collection< Number > numbers) {
		
    	if (numbers==null || numbers.size()==0) {
    		return null;
    	}
    	
    	Iterator< Number > it=numbers.iterator(); 
    	Number result = it.next();
    	
    	while (it.hasNext()) {
    		Number currentNumber = (Number) it.next();
    		if (currentNumber.doubleValue() > result.doubleValue()) {
    			result = currentNumber;
    		}
    	}
    	return result;
    }

    public static void addFocusListener_ConfirmChanges(JTable table) {
		if (!(table.getDefaultEditor(Object.class) instanceof DefaultCellEditor)) {
			return;
		}
		((DefaultCellEditor)table.getDefaultEditor(Object.class)).getComponent().addFocusListener(focusListener);
	}

	public static ModelCls findHighestModelClsByCompositionAndInheritance(Collection<ModelCls> someCls) {
		Collection<ModelCls> topClsInh = new HashSet<ModelCls>();
		for (ModelCls cls : someCls) {
			ModelCls topCls = findHighestModelClsByInheritance(cls);
			if (!topClsInh.contains(topCls)) {
				topClsInh.add(topCls);
			}
		}
		ModelCls topClsComp = findHighestModelClsByComposition(topClsInh);
		return topClsComp;
	}

	public static ModelCls findHighestModelClsByComposition(Collection<ModelCls> someCls) {
		if (someCls==null || someCls.size() == 0) {
			return null;
		}
		HashMap<ModelCls, DefaultMutableTreeNode> clsToNode = new HashMap<ModelCls, DefaultMutableTreeNode>();
		for (Iterator< ModelCls > it=someCls.iterator(); it.hasNext();) {
			ModelCls cls = it.next();
			if (!clsToNode.containsKey(cls)) {
				DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(cls);
				findHighestModelClsByCompositionInternal(treeNode, clsToNode, someCls.size());
			}
		}

		DefaultMutableTreeNode tmp = (DefaultMutableTreeNode) clsToNode.values().iterator().next();
		DefaultMutableTreeNode topParent = tmp;
		while (tmp != null) {
			tmp = (DefaultMutableTreeNode)tmp.getParent();
			if (tmp != null) {
				topParent = tmp;
			}
		} 
		return (ModelCls) topParent.getUserObject();
	}

	private static void findHighestModelClsByCompositionInternal(DefaultMutableTreeNode treeNode, HashMap< ModelCls, DefaultMutableTreeNode > clsToNode, int depth) {
		if (depth < 0) return;
		ModelCls cls = (ModelCls) treeNode.getUserObject();
		clsToNode.put(cls, treeNode);
		
		for (Iterator<ModelSlot> it = cls.listSlots().iterator(); it.hasNext();) {
			ModelSlot slot = (ModelSlot) it.next();
			if (slot.getValueType() == ValueType.INSTANCE) {
				Collection<Object> values = slot.getAllowedValues();
				if (values.size() > 0) {
					ModelCls childCls = (ModelCls) values.iterator().next();
					DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode(childCls);
					
					treeNode.add(childTreeNode);
					findHighestModelClsByCompositionInternal(childTreeNode, clsToNode, depth-1);
				}
			}
		}
	}

	public static ModelCls getSubCls(ModelSlot slot) {
		Collection<Object> allowedCls = slot.getAllowedValues();
		if (allowedCls.size() == 0) {
			return null;
		}
		return (ModelCls) allowedCls.iterator().next();
	}

	public static Explanation addExplanation(Explanation parentExp, ModelInstance inst, Object query, Object cb, AbstractSMFunction usedSMF, String comment, double similarity) {
		if (parentExp == null) {
			return null;
		}
		parentExp.setSimilarity(similarity);

		return null;
	}

	public static ModelCls getDomainCls(ModelSlot slot) {
		Collection<ModelCls> allModelCls = CBRProject.getInstance().getAllModelCls();
		for (Iterator<ModelCls> it=allModelCls.iterator(); it.hasNext();) {
			ModelCls cls = it.next();
			if (cls.listSlots().contains(slot)) return cls;
		}
		return null;
	}

	public static boolean hasInheritanceStructure(ModelCls cls) {
		if (cls == null) return false;
		
		try{
			if (cls.getSuperCls() != null) return true;
		} catch( Exception e) {
			log.severe("Exception while looking for superclass of: " + cls.getName());
			return false;
		}
		
		for (Iterator<ModelCls> it = CBRProject.getInstance().getAllModelCls().iterator(); it.hasNext();) {
			ModelCls itCls = it.next();
			if (itCls.getSuperCls() == cls) return true;
		}
		return false;
	}

	public static ModelCls findHighestModelClsByInheritance(ModelCls cls) {
		return findHighestModelClsByInheritance(cls, 1000);
	}

	public static ModelCls findHighestModelClsByInheritance(ModelCls cls, int counter) {
		if (cls == null) {
			return null;
		}
		ModelCls topCls = cls;
		while (topCls.getSuperCls() != null) {
			topCls = topCls.getSuperCls();
			if (counter-- < 0)  {
				log.warning("Could not find highest class (inheritance), because of loops");
				break;
			}
		}
		return topCls;
	}

	public static AbstractClassSM findSuperClsSMF(SimMap activeSMFs, ModelCls cls) {
		if (cls == null) {
			return null;
		}
		AbstractClassSM smfC = null;
		
		if (activeSMFs!=null)  {
			smfC = (AbstractClassSM) activeSMFs.get(cls);
		}
		
		if (smfC != null) {
			return smfC;
		}
		return findSuperClsSMF(activeSMFs, cls.getSuperCls());		
	}

	public static int findIndex(String[] searchSpace, String elem) {
		for (int i = 0; i < searchSpace.length; i++) {
			if (searchSpace[i].equals(elem)) return i;
		}
		return -1;
	}

	/**
	 * Checks the value types of smf and slot for equality.
	 * If they are not, all smfs are removed from the holder and a new one is initialized.
	 * @param smf
	 * @param slot
	 * @return true if both types were the same
	 */
	public static boolean checkValueTypeSlotAndSMF(AbstractSMFunction smf, ModelSlot slot) {
		if (slot.getValueType()!=smf.getValueType()) {
			log.fine("smf ["+smf.getSmfName()+"] for slot ["+slot.getName()+"] have different value types: ["+smf.getValueType()+", "+slot.getValueType()+"]. Create new SMF");
			SMFHolder holder = SMFContainer.getInstance().getSMFHolderForModelInstance(slot);
			holder.checkConsistency(null);
			return false;
		}
		return true;
	}

	public static String getSimilarityStr(double similarity) {
		if (Double.isNaN(similarity)) {
			return "n.a.";
		}
		return formatDoubleAsString( similarity );
	}

	public static final FocusListener focusListener = new FocusAdapter() {
		public void focusLost(FocusEvent e) {
			((JTextField)e.getComponent()).postActionEvent();
		}
	};
	
	/**
	 * Escapes special characters in the given string to make it valid XML
	 * @return
	 */
	public static String encode(String text) {
		if (text!=null) {
			text.replaceAll("&", "&amp;");
			text.replaceAll("<", "&lt;");
			text.replaceAll(">", "&gt;");
			text.replaceAll("\"", "&quot;");
			text.replaceAll("\'", "&apos;");
		}
		return text;
	}
	
	/**
	 * Decodes given string such that xml decoded symbols will become special characters again.
	 * @return
	 */
	public static String decode(String text) {
		if (text!=null) {
			text.replaceAll("&amp;", "&");
			text.replaceAll("&lt;","<");
			text.replaceAll("&gt;", ">");
			text.replaceAll("&quot;","\"");
			text.replaceAll("&apos;", "\'");
		}
		return text;
	}
}
