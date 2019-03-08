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
import de.dfki.mycbr.core.similarity.OrderedSymbolFct;
import de.dfki.mycbr.core.similarity.Similarity;
import de.dfki.mycbr.core.similarity.config.NumberConfig;
import test.junittest.TestFramework;

/**
 * @author myCBR Team
 *
 */
public class OrderedSymbolTest extends TestCase {
	
	/**
	 * Test method for {@link de.dfki.mycbr.core.similarity.SymbolFct#calculateSimilarity(Attribute, Attribute)}.
	 */
	@Test
	public void testCalculateSimilarity() {
		
		try {
			TestFramework frame = new TestFramework();
			OrderedSymbolFct f = frame.colorDesc.addOrderedSymbolFct("f1", false);
			IntegerFct g = f.getInternalFunction();
			g.setFunctionParameterR(0.60);
				
			f.setOrderIndexOf("red", 3);
			f.setOrderIndexOf("green", 4);
			f.setOrderIndexOf("white", 2);
			
			Similarity s = f.calculateSimilarity("red", "green");
			assertTrue("sim(red,green) should be 0.6 but is " + s, s.getRoundedValue() == 0.60);
			s = f.calculateSimilarity("green", "red");
			assertTrue("sim(green,red) should be 0.6 but is " + s, s.getRoundedValue() == 0.60);
			s = f.calculateSimilarity("green", "green");
			assertTrue("sim(green,green) should be 1.0 but is " + s, s.getRoundedValue() == 1.00);
			
			g.setFunctionTypeL(NumberConfig.SMOOTH_STEP_AT);
			g.setFunctionParameterR(1.2);
			
			s = f.calculateSimilarity("red", "green");
			assertTrue("sim(red,green) should be 0.88 but is " + s, s.getRoundedValue() == 0.9);
			s = f.calculateSimilarity("green", "red");
			assertTrue("sim(green,red) should be 0.88 but is " + s,s.getRoundedValue() == 0.9);
			s = f.calculateSimilarity("green", "green");
			assertTrue("sim(green,green) should be 1.0 but is " + s, s.getRoundedValue() == 1.00);
			s = f.calculateSimilarity("white", "green");
			assertTrue("sim(white,green) should be 0.0 but is " + s, s.getRoundedValue() == 0.00);
			s = f.calculateSimilarity("green", "white");
			assertTrue("sim(green,white) should be 0.0 but is " + s, s.getRoundedValue() == 0.00);
			
			g.setFunctionTypeL(NumberConfig.POLYNOMIAL_WITH);
			g.setFunctionParameterR(0.3);
			s = f.calculateSimilarity("red", "green");
			assertTrue("sim(red,green) should be 0.97 but is " + s, s.getRoundedValue() == 0.97);
			s = f.calculateSimilarity("green", "red");
			assertTrue("sim(green,red) should be 0.97 but is " + s, s.getRoundedValue() == 0.97);
			s = f.calculateSimilarity("green", "green");
			assertTrue("sim(green,green) should be 1.0 but is " + s, s.getRoundedValue() == 1.00);
			s = f.calculateSimilarity("white", "green");
			assertTrue("sim(white,green) should be 0.94 but is " + s, s.getRoundedValue() == 0.93);
			s = f.calculateSimilarity("green", "white");
			assertTrue("sim(green,white) should be 0.94 but is " + s, s.getRoundedValue() == 0.93);
			
			g.setFunctionTypeL(NumberConfig.STEP_AT);
			g.setFunctionParameterR(1.0);
			s = f.calculateSimilarity("red", "green");
			assertTrue("sim(red,green) should be 1.0 but is " + s, s.getRoundedValue() == 1.00);
			s = f.calculateSimilarity("green","red");
			assertTrue("sim(green,red) should be 1.0 but is " + s, s.getRoundedValue() == 1.00);
			s = f.calculateSimilarity("green", "green");
			assertTrue("sim(green,green) should be 1.0 but is " + s, s.getRoundedValue() == 1.00);
			s = f.calculateSimilarity("white", "green");
			assertTrue("sim(white,green) should be 0.0 but is " + s, s.getRoundedValue() == 0.00);
			s = f.calculateSimilarity("green", "white");
			assertTrue("sim(green,white) should be 0.0 but is " + s, s.getRoundedValue() == 0.00);
			
		} catch(Exception e) {
			assertTrue("Excpetion in OrderedSymbolTest: testCalculateSimilarity",false);
		}
	}
	

}
