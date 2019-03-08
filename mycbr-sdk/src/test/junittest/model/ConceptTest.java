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

/**
 * @author myCBR Team
 *
 */
public class ConceptTest extends TestCase {

public void test() {
		
		try {
			Project prj = new Project();	
			Concept topConcept = prj.createTopConcept("topConcept");
			Exception exp = null;
			try {
				prj.createTopConcept("topConcept");	
			} catch (Exception e) {
				exp = e;
			}
			assertTrue("There should have been an exception when adding a concept with a name already used", exp != null);
			exp = null;
			
			Concept c = new Concept("C", prj, topConcept);
			try {
				c.setName("topConcept");	
			} catch (Exception e) {
				exp = e;
			}
			assertTrue("There should have been an exception when adding a concept with a name already used", exp != null);
			exp = null;
			
		} catch(Exception e) {
			e.printStackTrace();
			assertTrue("Excpetion in ConceptDescTest: test",false);
		}
	}

}
