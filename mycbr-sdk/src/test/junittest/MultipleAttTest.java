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

package test.junittest;

import java.util.LinkedList;

import junit.framework.TestCase;
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Attribute;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.casebase.MultipleAttribute;
import de.dfki.mycbr.core.model.BooleanDesc;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.model.ConceptDesc;
import de.dfki.mycbr.core.model.SymbolDesc;
import de.dfki.mycbr.core.similarity.AmalgamationFct;
import de.dfki.mycbr.core.similarity.ISimFct;
import de.dfki.mycbr.core.similarity.Similarity;
import de.dfki.mycbr.core.similarity.config.AmalgamationConfig;
import de.dfki.mycbr.core.similarity.config.MultipleConfig;
import de.dfki.mycbr.core.similarity.config.MultipleConfig.MainType;
import de.dfki.mycbr.core.similarity.config.MultipleConfig.Reuse;
import de.dfki.mycbr.core.similarity.config.MultipleConfig.Type;

/**
 * Tests the similarity measure on attributes with multiple values.
 * 
 * @author myCBR Team
 *
 */
public class MultipleAttTest extends TestCase {

	/**
	 * test the similarity measure function
	 * for symbolic attributes with multiple values
	 */
	public void testSymbol() {
		try {
				
			TestFramework fw = new TestFramework();
			ISimFct innerFct = fw.equipDesc.addSymbolFct("EquipFct", true);
			innerFct.setMultipleConfig(new MultipleConfig(MainType.PARTNER_QUERY, Reuse.REUSE, Type.AVG));
					
			fw.amalgam.setActiveFct(fw.equipDesc, innerFct);

			
			Instance i = fw.carDesc.addInstance("Instance1");
			Instance c1 = i;
			LinkedList<Attribute> l1 = new LinkedList<Attribute>();
			l1.add(fw.equipDesc.getAttribute("radio"));
			l1.add(fw.equipDesc.getAttribute("sunroof"));
			MultipleAttribute<SymbolDesc> m1 = new MultipleAttribute<SymbolDesc>(fw.equipDesc,l1);
			c1.addAttribute(fw.equipDesc.getName(), m1);
			
			i = fw.carDesc.addInstance("Instance2");
			Instance c2 = i;
			LinkedList<Attribute> l2 = new LinkedList<Attribute>();
			l2.add(fw.equipDesc.getAttribute("radio"));
			l2.add(fw.equipDesc.getAttribute("air_conditioning"));
			MultipleAttribute<SymbolDesc> m2 = new MultipleAttribute<SymbolDesc>(fw.equipDesc,l2);
			assertTrue(c2.addAttribute(fw.equipDesc.getName(),m2));
			
			i = fw.carDesc.addInstance("Instance3");
			Instance c3 = i;
			LinkedList<Attribute> l3 = new LinkedList<Attribute>();
			l3.add(fw.equipDesc.getAttribute("radio"));
			l3.add(fw.equipDesc.getAttribute("sunroof"));
			l3.add(fw.equipDesc.getAttribute("air_conditioning"));
			l3.add(fw.equipDesc.getAttribute("electric_window_lift"));
			MultipleAttribute<SymbolDesc> m3 = new MultipleAttribute<SymbolDesc>(fw.equipDesc,l3);
			c3.addAttribute(fw.equipDesc.getName(),m3);
			
			i = fw.carDesc.addInstance("query");
			Instance q = i;
			LinkedList<Attribute> l4 = new LinkedList<Attribute>();
			l4.add(fw.equipDesc.getAttribute("radio"));
			l4.add(fw.equipDesc.getAttribute("electric_window_lift"));
			MultipleAttribute<SymbolDesc> m4 = new MultipleAttribute<SymbolDesc>(fw.equipDesc,l4);
			assertTrue(q.addAttribute(fw.equipDesc.getName(),m4));
			
			fw.amalgam.setType(AmalgamationConfig.EUCLIDEAN);
			Similarity s1 = fw.amalgam.calculateSimilarity(q,c1);
			assertTrue("sim is " + s1.getRoundedValue() + " but should be 0.97",s1.getRoundedValue() == 0.97);
						

			fw.amalgam.setType(AmalgamationConfig.WEIGHTED_SUM);
			Similarity s2 = fw.amalgam.calculateSimilarity(q,c2);
			assertTrue("sim is " + s2 + " but should be 0.95",s2.getRoundedValue() == 0.95);
			
			Similarity s3 = fw.amalgam.calculateSimilarity(q,c3);
			assertTrue("sim is " + s3.getRoundedValue() + " but should be 1.0",s3.getRoundedValue() == 1.00);
				
		} catch (Exception exp) {
			exp.printStackTrace();
			assertTrue("Excpetion in MultipleAttTest: testmyCBRImport",false);
		}

	}	
	
	/**
	 * test the similarity measure function
	 * for c attributes with multiple values
	 */
	public void testConcept() {
		try {
				
			Project p = new Project();
			Concept car = p.createTopConcept("car");
			Concept equipment = p.createTopConcept("equipment");
			
			ConceptDesc equipDesc = new ConceptDesc(car, "equip", equipment);
			equipDesc.setMultiple(true);
			
			BooleanDesc ac = new BooleanDesc(equipment, "hasAC");
			BooleanDesc sunroof = new BooleanDesc(equipment, "hasSunroof");
			BooleanDesc windowLift = new BooleanDesc(equipment, "hasWindowLift");
			
			AmalgamationFct innerFct = equipment.addAmalgamationFct(AmalgamationConfig.WEIGHTED_SUM, "f", true);
			equipDesc.setMultipleConfig(innerFct, new MultipleConfig(MainType.PARTNER_QUERY, Reuse.REUSE, Type.AVG));
			//p.getFctContainer().getActiveAmalgamFcts().put(equipment, innerFct);
			
			innerFct.setActiveFct(ac, ac.addBooleanFct("acFct", true));
			innerFct.setActiveFct(sunroof, sunroof.addBooleanFct("sunroofFct", true));
			innerFct.setActiveFct(windowLift, windowLift.addBooleanFct("windowLiftFct", true));
			
			
			AmalgamationFct amalgam = car.addAmalgamationFct(AmalgamationConfig.WEIGHTED_SUM, "g", true);
			//p.getFctContainer().getActiveAmalgamFcts().put(car, amalgam);
			amalgam.setActiveFct(equipDesc, innerFct);
			// setup case base
			
			Instance a = (Instance)equipment.addInstance("a");
			a.addAttribute("hasAC", true);
			a.addAttribute("hasSunroof", true);
			a.addAttribute("hasWindowLift", true);
			
			Instance b = (Instance)equipment.addInstance("b");
			b.addAttribute("hasAC", false);
			b.addAttribute("hasSunroof", true);
			b.addAttribute("hasWindowLift", true);
			
			Instance c = (Instance)equipment.addInstance("c");
			c.addAttribute("hasAC", true);
			c.addAttribute("hasSunroof", false);
			c.addAttribute("hasWindowLift", true);
			
			Instance d = (Instance)equipment.addInstance("d");
			d.addAttribute("hasAC", true);
			d.addAttribute("hasSunroof", true);
			d.addAttribute("hasWindowLift", false);
			
			Instance e = (Instance)equipment.addInstance("e");
			e.addAttribute("hasAC", false);
			e.addAttribute("hasSunroof", false);
			e.addAttribute("hasWindowLift", true);
			
			Instance f = (Instance)equipment.addInstance("f");
			f.addAttribute("hasAC", false);
			f.addAttribute("hasSunroof", true);
			f.addAttribute("hasWindowLift", false);
			
			Instance g = (Instance)equipment.addInstance("g");
			g.addAttribute("hasAC", true);
			g.addAttribute("hasSunroof", false);
			g.addAttribute("hasWindowLift", false);
			
			Instance h = (Instance)equipment.addInstance("h");
			assertTrue(h.addAttribute("hasAC", false));
			assertTrue(h.addAttribute("hasSunroof", false));
			assertTrue(h.addAttribute("hasWindowLift", false));
			
			Instance i = car.addInstance("Instance1");
			Instance c1 = i;
			LinkedList<Attribute> l1 = new LinkedList<Attribute>();
			l1.add(a);
			l1.add(b);
			l1.add(c);
			MultipleAttribute<ConceptDesc> m1 = new MultipleAttribute<ConceptDesc>(equipDesc,l1);
			assertTrue(c1.addAttribute(equipDesc.getName(),m1));
			
			i = car.addInstance("Instance2");
			Instance c2 = i;
			LinkedList<Attribute> l2 = new LinkedList<Attribute>();
			l2.add(d);
			l2.add(e);
			l2.add(f);
			MultipleAttribute<ConceptDesc> m2 = new MultipleAttribute<ConceptDesc>(equipDesc,l2);
			assertTrue(c2.addAttribute(equipDesc.getName(),m2));
			
			i = car.addInstance("Instance3");
			Instance c3 = i;
			LinkedList<Attribute> l3 = new LinkedList<Attribute>();
			l3.add(g);
			l3.add(h);
			MultipleAttribute<ConceptDesc> m3 = new MultipleAttribute<ConceptDesc>(equipDesc,l3);
			assertTrue(c3.addAttribute(equipDesc.getName(),m3));
			
			i = car.addInstance("q");
			Instance q = i;
			LinkedList<Attribute> l4 = new LinkedList<Attribute>();
			l4.add(a);
			l4.add(d);
			l4.add(g);
			MultipleAttribute<ConceptDesc> m4 = new MultipleAttribute<ConceptDesc>(equipDesc,l4);
			assertTrue(q.addAttribute(equipDesc.getName(),m4));
		
			/////////////////////////////////////////////////////////
			/////////////////// PARTNER QUERY REUSE AVG /////////////
			/////////////////////////////////////////////////////////
//			System.out.println("==================== INHERITANCE FCT ========================");
//			p.getFctContainer().getInheritanceFct().print();
//			System.out.println("=============================================================");
//			
			Similarity s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 0.78", s1.getRoundedValue() == 0.78);
			
			Similarity s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 0.78", s2.getRoundedValue() == 0.78);
			
			Similarity s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 0.67", s3.getRoundedValue() == 0.67);
			
			/////////////////////////////////////////////////////////
			/////////////////// PARTNER QUERY REUSE MIN /////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, 
					new MultipleConfig(MainType.PARTNER_QUERY, Reuse.REUSE, Type.MIN));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 0.67", s1.getRoundedValue() == 0.67);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 0.67", s2.getRoundedValue() == 0.67);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 0.33", s3.getRoundedValue() == 0.33);
			
			/////////////////////////////////////////////////////////
			/////////////////// PARTNER QUERY REUSE MAX /////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, new MultipleConfig(MainType.PARTNER_QUERY, Reuse.REUSE, Type.MAX));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getValue() + " but should be 1.0", s1.getValue() == 1.0);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getValue() + " but should be 1.0", s2.getValue() == 1.0);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getValue() + " but should be 1.0", s3.getValue() == 1.0);

			/////////////////////////////////////////////////////////
			////////////// PARTNER QUERY ZERO AVG ///////////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, 
					new MultipleConfig(MainType.PARTNER_QUERY, Reuse.ZERO_SIM, Type.AVG));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 0.67", s1.getRoundedValue() == 0.67);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 0.56", s2.getRoundedValue() == 0.56);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 0.22", s3.getRoundedValue() == 0.22);

			/////////////////////////////////////////////////////////
			////////////// PARTNER QUERY ZERO MIN ///////////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, 		new MultipleConfig(MainType.PARTNER_QUERY, Reuse.ZERO_SIM, Type.MIN));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 0.33", s1.getRoundedValue() == 0.33);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 0.33", s2.getRoundedValue() == 0.33);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 0.0", s3.getRoundedValue() == 0.0);
			
			/////////////////////////////////////////////////////////
			////////////// PARTNER QUERY ZERO MAX ///////////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, new MultipleConfig(MainType.PARTNER_QUERY, Reuse.ZERO_SIM, Type.MAX));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 1.0", s1.getRoundedValue() == 1.0);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 0.67", s2.getRoundedValue() == 0.67);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 0.33", s3.getRoundedValue() == 0.33);
			
			/////////////////////////////////////////////////////////
			////////////// PARTNER QUERY IGNORE AVG /////////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, new MultipleConfig(MainType.PARTNER_QUERY, Reuse.IGNORE, Type.AVG));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 0.67", s1.getRoundedValue() == 0.67);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 0.56", s2.getRoundedValue() == 0.56);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 0.33", s3.getRoundedValue() == 0.33);
			
			/////////////////////////////////////////////////////////
			////////////// PARTNER QUERY IGNORE MIN /////////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, new MultipleConfig(MainType.PARTNER_QUERY, Reuse.IGNORE, Type.MIN));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 0.33", s1.getRoundedValue() == 0.33);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 0.33", s2.getRoundedValue() == 0.33);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 0.33", s3.getRoundedValue() == 0.33);
			
			/////////////////////////////////////////////////////////
			////////////// PARTNER QUERY IGNORE MAX /////////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, new MultipleConfig(MainType.PARTNER_QUERY, Reuse.IGNORE, Type.MAX));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 1.0", s1.getRoundedValue() == 1.0);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 0.67", s2.getRoundedValue() == 0.67);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 0.33", s3.getRoundedValue() == 0.33);
			
			/////////////////////////////////////////////////////////
			////////////// PARTNER CASE REUSE AVG ///////////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, new MultipleConfig(MainType.PARTNER_CASE, Reuse.REUSE, Type.AVG));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 0.78", s1.getRoundedValue() == 0.78);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 0.67", s2.getRoundedValue() == 0.67);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 0.83", s3.getRoundedValue() == 0.83);

			/////////////////////////////////////////////////////////
			////////////// PARTNER CASE REUSE MIN ///////////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, new MultipleConfig(MainType.PARTNER_CASE, Reuse.REUSE, Type.MIN));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 0.67", s1.getRoundedValue() == 0.67);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 0.33", s2.getRoundedValue() == 0.33);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 0.67", s3.getRoundedValue() == 0.67);
			
			/////////////////////////////////////////////////////////
			////////////// PARTNER CASE REUSE MAX ///////////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, new MultipleConfig(MainType.PARTNER_CASE, Reuse.REUSE, Type.MAX));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 1.0", s1.getRoundedValue() == 1.0);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 1.0", s2.getRoundedValue() == 1.0);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 1.0", s3.getRoundedValue() == 1.0);
			
			/////////////////////////////////////////////////////////
			/////////////////// PARTNER MAX REUSE AVG /////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, new MultipleConfig(MainType.PARTNER_MAX, Reuse.REUSE, Type.AVG));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 0.78", s1.getRoundedValue() == 0.78);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 0.78", s2.getRoundedValue() == 0.78);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 0.67", s3.getRoundedValue() == 0.67);
			
			/////////////////////////////////////////////////////////
			/////////////////// PARTNER MAX REUSE MIN ///////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, new MultipleConfig(MainType.PARTNER_MAX, Reuse.REUSE, Type.MIN));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 0.67", s1.getRoundedValue() == 0.67);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 0.67", s2.getRoundedValue() == 0.67);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 0.33", s3.getRoundedValue() == 0.33);
			
			/////////////////////////////////////////////////////////
			/////////////////// PARTNER MAX REUSE MAX ///////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, new MultipleConfig(MainType.PARTNER_MAX, Reuse.REUSE, Type.MAX));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 1.0", s1.getRoundedValue() == 1.0);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 1.0", s2.getRoundedValue() == 1.0);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 1.0", s3.getRoundedValue() == 1.0);

			/////////////////////////////////////////////////////////
			////////////// PARTNER MAX ZERO AVG /////////////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, new MultipleConfig(MainType.PARTNER_MAX, Reuse.ZERO_SIM, Type.AVG));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 0.67", s1.getRoundedValue() == 0.67);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 0.56", s2.getRoundedValue() == 0.56);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 0.22", s3.getRoundedValue() == 0.22);

			/////////////////////////////////////////////////////////
			////////////// PARTNER MAX ZERO MIN /////////////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, new MultipleConfig(MainType.PARTNER_MAX, Reuse.ZERO_SIM, Type.MIN));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 0.33", s1.getRoundedValue() == 0.33);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 0.33", s2.getRoundedValue() == 0.33);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 0.0", s3.getRoundedValue() == 0.0);
			
			/////////////////////////////////////////////////////////
			////////////// PARTNER MAX ZERO MAX /////////////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, new MultipleConfig(MainType.PARTNER_MAX, Reuse.ZERO_SIM, Type.MAX));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 1.0", s1.getRoundedValue() == 1.0);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 0.67", s2.getRoundedValue() == 0.67);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 0.33", s3.getRoundedValue() == 0.33);
			
			/////////////////////////////////////////////////////////
			////////////// PARTNER MAX IGNORE AVG ///////////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, new MultipleConfig(MainType.PARTNER_MAX, Reuse.IGNORE, Type.AVG));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 0.67", s1.getRoundedValue() == 0.67);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 0.56", s2.getRoundedValue() == 0.56);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 0.33", s3.getRoundedValue() == 0.33);
			
			/////////////////////////////////////////////////////////
			////////////// PARTNER MAX IGNORE MIN ///////////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, new MultipleConfig(MainType.PARTNER_MAX, Reuse.IGNORE, Type.MIN));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 0.33", s1.getRoundedValue() == 0.33);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 0.33", s2.getRoundedValue() == 0.33);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 0.33", s3.getRoundedValue() == 0.33);
			
			/////////////////////////////////////////////////////////
			////////////// PARTNER MAX IGNORE MAX ///////////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, new MultipleConfig(MainType.PARTNER_MAX, Reuse.IGNORE, Type.MAX));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 1.0", s1.getRoundedValue() == 1.0);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 0.67", s2.getRoundedValue() == 0.67);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 0.33", s3.getRoundedValue() == 0.33);
			
			/////////////////////////////////////////////////////////
			////////////// PARTNER CASE ZERO AVG ////////////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, new MultipleConfig(MainType.PARTNER_CASE, Reuse.ZERO_SIM, Type.AVG));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 0.67", s1.getRoundedValue() == 0.67);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 0.56", s2.getRoundedValue() == 0.56);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 0.67", s3.getRoundedValue() == 0.67);
			
			/////////////////////////////////////////////////////////
			////////////// PARTNER CASE ZERO MIN ////////////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, new MultipleConfig(MainType.PARTNER_CASE, Reuse.ZERO_SIM, Type.MIN));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 0.33", s1.getRoundedValue() == 0.33);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 0.33", s2.getRoundedValue() == 0.33);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 0.33", s3.getRoundedValue() == 0.33);
			
			/////////////////////////////////////////////////////////
			////////////// PARTNER CASE ZERO MAX ////////////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, new MultipleConfig(MainType.PARTNER_CASE, Reuse.ZERO_SIM, Type.MAX));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 1.0", s1.getRoundedValue() == 1.0);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 1.0", s2.getRoundedValue() == 1.0);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 1.0", s3.getRoundedValue() == 1.0);

			/////////////////////////////////////////////////////////
			////////////// PARTNER CASE IGNORE AVG //////////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, new MultipleConfig(MainType.PARTNER_CASE, Reuse.IGNORE, Type.AVG));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 0.67", s1.getRoundedValue() == 0.67);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 0.56", s2.getRoundedValue() == 0.56);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 0.67", s3.getRoundedValue() == 0.67);
			
			/////////////////////////////////////////////////////////
			////////////// PARTNER CASE ZERO MIN ////////////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, new MultipleConfig(MainType.PARTNER_CASE, Reuse.IGNORE, Type.MIN));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 0.33", s1.getRoundedValue() == 0.33);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 0.33", s2.getRoundedValue() == 0.33);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 0.33", s3.getRoundedValue() == 0.33);
			
			/////////////////////////////////////////////////////////
			////////////// PARTNER CASE ZERO MAX ////////////////////
			/////////////////////////////////////////////////////////
			
			equipDesc.setMultipleConfig(innerFct, new MultipleConfig(MainType.PARTNER_CASE, Reuse.IGNORE, Type.MAX));
			
			s1 = amalgam.calculateSimilarity(q,c1);
			assertTrue("Similarity to case1 is " + s1.getRoundedValue() + " but should be 1.0", s1.getRoundedValue() == 1.0);
			
			s2 = amalgam.calculateSimilarity(q,c2);
			assertTrue("Similarity to case2 is " + s2.getRoundedValue() + " but should be 1.0", s2.getRoundedValue() == 1.0);
			
			s3 = amalgam.calculateSimilarity(q,c3);
			assertTrue("Similarity to case3 is " + s3.getRoundedValue() + " but should be 1.0", s3.getRoundedValue() == 1.0);
						
		} catch (Exception exp) {
			exp.printStackTrace();
			assertTrue("Excpetion in MultipleAttTest: testmyCBRImport",false);
		}

	}	
	
}
