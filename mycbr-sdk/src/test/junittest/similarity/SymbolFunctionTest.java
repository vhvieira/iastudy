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

package test.junittest.similarity;

import junit.framework.TestCase;

import org.junit.Test;

import de.dfki.mycbr.core.similarity.Similarity;
import de.dfki.mycbr.core.similarity.SymbolFct;
import test.junittest.TestFramework;

/**
 * @author myCBR Team
 *
 */
public class SymbolFunctionTest extends TestCase {

	/**
	 * Test method for {@link de.dfki.mycbr.core.similarity.SymbolFct#calculateSimilarity(Attribute, Attribute)}.
	 */
	@Test
	public void testCalculateSimilarity() {
		
		try {
			TestFramework frame = new TestFramework();
			SymbolFct f = frame.colorDesc.addSymbolFct("f1", true);
			// same values
			assertTrue(f.calculateSimilarity(frame.colorDesc.getAttribute("green"), frame.colorDesc.getAttribute("green")).equals(Similarity.get(1.00)));
			// different values
			assertTrue(f.calculateSimilarity(frame.colorDesc.getAttribute("green"), frame.colorDesc.getAttribute("red")).equals(Similarity.get(0.00)));
			f.setSimilarity("green", "red", 0.35);
			assertTrue(f.calculateSimilarity(frame.colorDesc.getAttribute("green"), frame.colorDesc.getAttribute("red")).equals(Similarity.get(0.35)));
			if (f.isSymmetric()) {
				assertTrue(f.calculateSimilarity(frame.colorDesc.getAttribute("red"), frame.colorDesc.getAttribute("green")).equals(Similarity.get(0.35)));	
			} else {
				assertTrue(f.calculateSimilarity(frame.colorDesc.getAttribute("red"), frame.colorDesc.getAttribute("green")).equals(Similarity.get(0.00)));
			}
		} catch(Exception e) {
			assertTrue("Excpetion in SymbolFctTest: testCalculateSimilarity",false);
		}
	}

}
