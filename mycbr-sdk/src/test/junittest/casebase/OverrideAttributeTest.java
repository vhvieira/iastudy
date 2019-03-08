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

package test.junittest.casebase;

import java.text.SimpleDateFormat;
import java.util.HashSet;

import junit.framework.TestCase;
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.model.BooleanDesc;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.model.ConceptDesc;
import de.dfki.mycbr.core.model.DateDesc;
import de.dfki.mycbr.core.model.IntegerDesc;
import de.dfki.mycbr.core.model.IntervalDesc;
import de.dfki.mycbr.core.model.StringDesc;
import de.dfki.mycbr.core.model.SymbolDesc;

/**
 * When there is a sub c B of a given c A, then all attributes of
 * this c A will be inherited. The c B can overwrite an attribute
 * "a" inherited by A by adding a new attribute with the name "a". There are
 * special requirements to be met.
 * 
 * @author myCBR Team
 * 
 */
public class OverrideAttributeTest extends TestCase {

	Project p;
	Concept topConcept;
	Concept subConcept;
	BooleanDesc booleanDesc;
	StringDesc stringDesc;
	IntegerDesc numberDesc;
	DateDesc dateDesc;
	IntervalDesc intervalDesc;
	SymbolDesc symbolDesc;
	Concept topConcept2;
	Concept subConcept2;

	/**
	 * initialize model with at least one attribute of each type
	 */
	public void setUp() {

		try {
			p = new Project();
			p.setName("Inheritance");
			p.setPath(System.getProperty("user.dir") + "/src/test/projects/");
			topConcept = p.createTopConcept("A");

			subConcept = new Concept("B", p, topConcept);

			topConcept2 = p.createTopConcept("C");
			subConcept2 = new Concept("D", p, topConcept2);


			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			HashSet<String> hs = new HashSet<String>();
			hs.add("symbl1");
			hs.add("symbl2");
			hs.add("symbl3");
			hs.add("symbl4");
			hs.add("symbl5");
			
			///////////////////////////////////////////
			////// attributes of conceptDesc A ////////
			///////////////////////////////////////////
			dateDesc = new DateDesc(topConcept, "date", df.parse("1990-01-01"), df
						.parse("2011-01-01"), df);
			booleanDesc = new BooleanDesc(topConcept, "bool");
			stringDesc = new StringDesc(topConcept, "string");
			numberDesc = new IntegerDesc(topConcept, "number", 0, 7);
			intervalDesc = new IntervalDesc(topConcept, "interval", 0, 100);
			symbolDesc = new SymbolDesc(topConcept, "symbol", hs);
			new ConceptDesc(topConcept, "c", topConcept2);
			
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue("Exception while adding concepts and attributes", false);
		}
	}

	/**
	 * reset local variables
	 */
	public void tearDown() {
//		p.save();
		p = null;
		topConcept = null;
		subConcept = null;
		booleanDesc = null;
		stringDesc = null;
		numberDesc = null;
		dateDesc = null;
		intervalDesc = null;
		symbolDesc = null;
		topConcept2 = null;
		subConcept2 = null;
    }
	/**
	 * This test tries to override attributes in subConcept B with wrong
	 * description. Exception is expected in each case
	 */
	public void testOverrideAttributeWithWrongDescription() {
		Exception e = null;
		try {
			new StringDesc(subConcept, "bool");
		} catch (Exception exp) {
			e = exp;
		}
		assertTrue("Test should have thrown an exception!", e != null);
		e = null;

		try {
			new BooleanDesc(subConcept, "string");
		} catch (Exception exp) {
			e = exp;
		}
		assertTrue("Test should have thrown an exception!", e != null);
		e = null;

		try {
			new StringDesc(subConcept, "number");
		} catch (Exception exp) {
			e = exp;
		}
		assertTrue("Test should have thrown an exception!", e != null);
		e = null;

		try {
			new SymbolDesc(subConcept, "date", new HashSet<String>());
		} catch (Exception exp) {
			e = exp;
		}
		assertTrue("Test should have thrown an exception!", e != null);
		e = null;

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		try {
			new DateDesc(subConcept, "interval", df.parse("1990-01-01"), df
					.parse("2011-01-01"), df);
		} catch (Exception exp) {
			e = exp;
		}
		assertTrue("Test should have thrown an exception!", e != null);
		e = null;

		try {
			new ConceptDesc(subConcept, "symbol", subConcept);
		} catch (Exception exp) {
			e = exp;
		}
		assertTrue("Test should have thrown an exception!", e != null);
		e = null;

		try {
			new BooleanDesc(subConcept, "c");
		} catch (Exception exp) {
			e = exp;
		}
		assertTrue("Test should have thrown an exception!", e != null);
		e = null;
	}


	/**
	 * A symbol attribute can be overridden, if the allowed symbols are a subset
	 * of the original ones.
	 */
	public void testOverrideSymbolAttribute() {
		Exception e = null;
		try {
			HashSet<String> hs = new HashSet<String>();
			hs.add("symbl1");
			hs.add("symbl2");
			hs.add("symbl7");
			hs.add("symbl4");
			hs.add("symbl5");
			new SymbolDesc(subConcept, "symbol", hs);
		} catch (Exception exp) {
			e = exp;
		}
		assertTrue(
				"There should be an exception when overriding a symbol attribute where the allowed values are not contained in the original ones!",
				e != null);

		try {
			HashSet<String> hs = new HashSet<String>();
			hs.add("symbl1");
			hs.add("symbl2");
			hs.add("symbl5");
			new SymbolDesc(subConcept, "symbol", hs);
		} catch (Exception exp) {
			exp.printStackTrace();
			assertTrue("exception in testOverrideSymbolAttribute", false);
		}
	}

	/**
	 * A string attribute can always be overridden.
	 */
	public void testOverrideStringAttribute() {

		try {
			new StringDesc(subConcept, "string");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(
					"Excpetion in OverrideAttributeTest: testOverrideStringAttribute",
					false);
		}
	}

	/**
	 * A boolean attribute can always be overridden.
	 */
	public void testOverrideBoolAttribute() {

		try {
			new BooleanDesc(subConcept, "bool");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(
					"Excpetion in OverrideAttributeTest: testOverrideBoolAttribute",
					false);
		}
	}

	/**
	 * A number attribute can be overridden, if the range given by min and max
	 * is contained in the original one.
	 */
	public void testOverrideNumberAttribute() {
		Exception e = null;
		try {
			new IntegerDesc(subConcept, "number", 1, 10);
		} catch (Exception exp) {
			e = exp;
		}
		assertTrue(
				"There should be an exception when overriding number attributes where the range is not contained in the original one",
				e != null);

		try {
			new IntegerDesc(subConcept, "number", 1, 6);
		} catch (Exception exp) {
			assertTrue(
					"Excpetion in OverrideAttributeTest: testOverrideNumberAttribute",
					false);
		}

	}

	/**
	 * Intervals can be overridden if the new interval boundaries are contained
	 * in the old ones!
	 */
	public void testOverrideIntervalAttribute() {
		Exception e = null;
		try {
			new IntervalDesc(subConcept, "interval", -1, 1002);
		} catch (Exception exp) {
			e = exp;
		}
		assertTrue(
				"There should be an exception when overriding interval attributes where the range is not contained in the original one",
				e != null);

		try {
			new IntervalDesc(subConcept, "interval", 1, 50);
		} catch (Exception exp) {
			assertTrue(
					"Excpetion in OverrideAttributeTest: testOverrideIntervalAttribute",
					false);
		}

	}

	/**
	 * Dates can be overridden if the new range is contained in the old one!
	 */
	public void testOverrideDateAttribute() {

		Exception e = null;
		try {
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			new DateDesc(subConcept, "date", df2.parse("1988-10-10"), df2
						.parse("2012-01-01"), df2);			
		} catch (Exception exp) {
			e = exp;
		}
		assertTrue("There should be an exception when overriding date " +
			"attributes where the range is not contained in the original one",
				e != null);

		try {
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			new DateDesc(subConcept, "date", df2.parse("1990-10-10"), df2
						.parse("2010-01-01"), df2);
		} catch (Exception exp) {
			assertTrue(
					"Excpetion in OverrideAttributeTest: testOverrideDateAttribute",
					false);
		}

	}

	/**
	 * Concept attributes can be overridden if the new c is a sub c
	 * of the old one.
	 */
	public void testOverrideConceptAttribute() {

		Exception e = null;
		try {
			new ConceptDesc(subConcept, "c", subConcept);
		} catch (Exception exp) {
			e = exp;
		}
		assertTrue(
				"There should be an exception when overriding c attributes where the c is not a sub c of the old one!",
				e != null);

		try {
			new ConceptDesc(subConcept, "c", subConcept2);
		} catch (Exception exp) {
			exp.printStackTrace();
			assertTrue(
					"Excpetion in OverrideAttributeTest: testOverrideConceptAttribute",
					false);
		}

	}

}
