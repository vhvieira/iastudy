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
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.model.StringDesc;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;


/**
 * @author myCBR Team
 *
 */
public class MoveAttDescTest {

	@Test
	public void testMoveAttDesc() {
		try {
			Project p = new Project();
			Concept A = p.createTopConcept("top1");
			Concept B = p.createTopConcept("top2");
			StringDesc a = new StringDesc(A, "a");
			new StringDesc(B, "a");
			StringDesc c = new StringDesc(B, "c");
			Exception exp = null;
			try {
				a.setOwner(B);
			} catch (Exception e) {
				exp = e;
				// tried to move attdesc but owner has attdesc with that name
			}
			assertTrue("there should have been an exception!", exp != null);
			
			exp = null;
			try {
				c = new StringDesc(B, "c");
			} catch (Exception e) {
				exp = e;
				// tried to add attdesc but owner has attdesc with that name
			}
			assertTrue("there should have been an exception!", exp != null);
			
			c.setOwner(A);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
