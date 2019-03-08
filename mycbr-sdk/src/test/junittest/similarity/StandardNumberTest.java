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

import de.dfki.mycbr.core.similarity.IntegerFct;
import de.dfki.mycbr.core.similarity.Similarity;
import de.dfki.mycbr.core.similarity.config.NumberConfig;
import test.junittest.TestFramework;

/**
 * @author myCBR Team
 *
 */
public class StandardNumberTest extends TestCase {

	/**
	 * Test method for {@link de.dfki.mycbr.core.similarity.NumberFct#calculateSimilarity(Attribute, Attribute)}.
	 */
	@Test
	public void testCalculateSimilarityAttributeAttribute() {

		try {
			TestFramework frame = new TestFramework();
			IntegerFct f = frame.doorDesc.addIntegerFct("f1", true);
			f.setSymmetric(true);
			f.setFunctionParameterR(0.60);
			Similarity s = f.calculateSimilarity(3,4);
			assertTrue("sim(3,4) should be 0.6 but is " + s, s.equals(Similarity.get(0.60)));
			s = f.calculateSimilarity(4,3);
			assertTrue("sim(4,3) should be 0.6 but is " + s, s.equals(Similarity.get(0.60)));
			s = f.calculateSimilarity(4,4);
			assertTrue("sim(4,4) should be 1.0 but is " + s, s.equals(Similarity.get(1.00)));
			f.setFunctionTypeL(NumberConfig.SMOOTH_STEP_AT);
			f.setFunctionParameterR(1.2);
			
			s = f.calculateSimilarity(3,4);
			assertTrue("sim(3,4) should be 0.88 but is " + s, s.getRoundedValue() == 0.88);
			s = f.calculateSimilarity(4,3);
			assertTrue("sim(4,3) should be 0.88 but is " + s, s.getRoundedValue() == 0.88);
			
			s = f.calculateSimilarity(4,4);
			assertTrue("sim(4,4) should be 1.0 but is " + s, s.getRoundedValue() == 1.0);
			s = f.calculateSimilarity(2,4);
			assertTrue("sim(2,4) should be 0.0 but is " + s, s.getRoundedValue() == 0.00);
			s = f.calculateSimilarity(4,2);
			assertTrue("sim(4,2) should be 0.0 but is " + s, s.getRoundedValue() == 0.00);
			
			f.setFunctionTypeL(NumberConfig.POLYNOMIAL_WITH);
			f.setFunctionParameterR(0.3);
			s = f.calculateSimilarity(3,4);
			assertTrue("sim(3,4) should be 0.97 but is " + s, s.getRoundedValue() == 0.97);
			s = f.calculateSimilarity(4,3);
			assertTrue("sim(4,3) should be 0.97 but is " + s, s.getRoundedValue() == 0.97);
			s = f.calculateSimilarity(4,4);
			assertTrue("sim(4,4) should be 1.0 but is " + s, s.getRoundedValue() == 1.00);
			s = f.calculateSimilarity(2,4);
			assertTrue("sim(2,4) should be 0.94 but is " + s, s.getRoundedValue() == 0.94);
			s = f.calculateSimilarity(4,2);
			assertTrue("sim(4,2) should be 0.94 but is " + s, s.getRoundedValue() == 0.94);
			
			f.setFunctionTypeL(NumberConfig.STEP_AT);
			f.setFunctionParameterR(1);
			s = f.calculateSimilarity(3,4);
			assertTrue("sim(3,4) should be 1.0 but is " + s, s.getRoundedValue() == 1.00);
			s = f.calculateSimilarity(4,3);
			assertTrue("sim(4,3) should be 1.0 but is " + s, s.getRoundedValue() == 1.00);
			s = f.calculateSimilarity(4,4);
			assertTrue("sim(4,4) should be 1.0 but is " + s, s.getRoundedValue() == 1.00);
			s = f.calculateSimilarity(2,4);
			assertTrue("sim(2,4) should be 0.0 but is " + s, s.getRoundedValue() == 0.00);
			s = f.calculateSimilarity(4,2);
			assertTrue("sim(4,2) should be 0.0 but is " + s, s.getRoundedValue() == 0.00);
		} catch (Exception e) {
			assertTrue("Excpetion in StandardNumberTest: testCalculateSimilarityAttributeAttribute",false);
		}
	}

}
