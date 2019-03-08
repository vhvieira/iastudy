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

package test.junittest.model;


import junit.framework.TestCase;
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.model.StringDesc;

/**
 * @author myCBR Team
 *
 */
public class ConceptDescTest extends TestCase {

	public void test() {
		
		try {
			Project prj = new Project();	
			Concept topConcept = prj.createTopConcept("topConcept");	
			Concept desc = new Concept("desc", prj, topConcept);

			new Concept("desc1", prj, desc);
			new Concept("desc2", prj, desc);
			new Concept("desc3", prj, desc);
			
			new StringDesc(desc, "a1");
			new StringDesc(desc, "a2");
			new StringDesc(desc, "a3");

			new StringDesc(topConcept, "a4");
			new StringDesc(topConcept, "a5");
			new StringDesc(topConcept, "a6");
			
			/////////////////////////////////////
			/////// subConceptDescriptions //////
			/////////////////////////////////////
			// initially added 3 sub concepts
			int size = desc.getSubConcepts().size();
			assertTrue(desc.getName() + " should have 3 subconcepts but has " + size,size == 3);
			
			// try to remove one successfully
			desc.removeSubConcept("desc3");
			size = desc.getSubConcepts().size();
			assertTrue(desc.getName() + " should have 2 subconcepts but has " + size,size == 2);
			
			// try to add a sub c with the same name as another sub c
			Exception e = null;
			try {
				new Concept("desc2", prj, desc);
			} catch(Exception exp) {
				e = exp;
			}
			assertTrue("There should be an exception when trying to add a c with an ambiguous name", e != null);
			
			size = desc.getSubConcepts().size();
			assertTrue(desc.getName() + " should have 2 subconcepts but has " + size,size == 2);
			
			// add a c description with the same name as the description deleted before
			new Concept("desc3", prj, desc);
			size = desc.getSubConcepts().size();
			assertTrue(desc.getName() + " should have 3 subconcepts but has " + size,size == 3);
			
			// try to add the c as a sub c of itself
			desc.addSubConcept(desc, false);
			size = desc.getSubConcepts().size();
			assertTrue(desc.getName() + " should have 3 subconcepts but has " + size,size == 3);
			
			// try to add a sub c with the same name as super
			e = null;
			try {
				new Concept("desc", prj, desc);
			} catch(Exception exp) {
				e = exp;
			}
			assertTrue("There should be an exception when trying to add a c with an ambiguous name", e != null);
			size = desc.getSubConcepts().size();
			assertTrue(desc.getName() + " should have 3 subconcepts but has " + size,size == 3);
			
			
			///////////////////////////////////////////
			/////////// attributeDescriptions /////////
			///////////////////////////////////////////
			// initially added 3 attributes
			size = desc.getAttributeDescs().size();
			assertTrue(desc.getName() + " should have 3 direct attribute descs but has " + size,size == 3);
			
			// parent description also contains 3 attributes
			// which are also visible here
			size = desc.getAllAttributeDescs().size();
			assertTrue(desc.getName() + " should have 6 attribute descs but has " + size,size == 6);
			
			// try to successfully remove attribute description
			assertTrue("Tried to remove adesc3 but it was not found as attribute desc in " + desc.getName(), desc.removeAttributeDesc("a3")!=false);
			size = desc.getAttributeDescs().size();
			assertTrue(desc.getName() + " should have 2 direct attribute descs but has " + size,size == 2);
			size = desc.getAllAttributeDescs().size();
			assertTrue(desc.getName() + " should have 5 attribute descs but has " + size,size == 5);
			
			// try to add an attribute with a name of
			// an attribute already contained in this description
			try{
				new StringDesc(desc, "a2");
			} catch (Exception exp){
				// nothing to do
			}
			size = desc.getAttributeDescs().size();
			assertTrue(desc.getName() + " should have 2 direct attribute descs but has " + size,size == 2);
			size = desc.getAllAttributeDescs().size();
			assertTrue(desc.getName() + " should have 5 attribute descs but has " + size,size == 5);
			
			// add an attribute with the same name as the attribute 
			// which was deleted before
			new StringDesc(desc, "a3");
			size = desc.getAttributeDescs().size();
			assertTrue(desc.getName() + " should have 3 direct attribute descs but has " + size,size == 3);
			size = desc.getAllAttributeDescs().size();
			assertTrue(desc.getName() + " should have 6 attribute descs but has " + size,size == 6);
			
			// try to add description as attribute with the same name as parent
			e = null;
			try {
				new Concept("desc", prj, desc);
			} catch(Exception exp) {
				e = exp;
			}
			assertTrue("There should be an exception when trying to add a c with an ambiguous name", e != null);
			
			size = desc.getAttributeDescs().size();
			assertTrue(desc.getName() + " should have 3 direct attribute descs but has " + size,size == 3);
			size = desc.getAllAttributeDescs().size();
			assertTrue(desc.getName() + " should have 6 attribute descs but has " + size,size == 6);

		} catch(Exception e) {
			e.printStackTrace();
			assertTrue("Excpetion in ConceptDescTest: test",false);
		}
	}

}
