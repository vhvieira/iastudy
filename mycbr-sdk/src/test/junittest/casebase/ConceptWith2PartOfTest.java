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

import java.util.Arrays;
import java.util.HashSet;

import junit.framework.TestCase;
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.model.ConceptDesc;
import de.dfki.mycbr.core.model.SymbolDesc;
import de.dfki.mycbr.core.similarity.AmalgamationFct;
import de.dfki.mycbr.core.similarity.SymbolFct;
import de.dfki.mycbr.core.similarity.config.AmalgamationConfig;

/**
 * tests functionality for a c having 2 attributes of
 * the same c (as part of relations)
 * 
 * @author myCBR Team
 *
 */
public class ConceptWith2PartOfTest extends TestCase {

	Project p;
	Concept A;
	Concept B;
	SymbolDesc a1;
	SymbolDesc a2;
	ConceptDesc b1;
	ConceptDesc b2;
	Double[][] results = {{1.0, .76, .93, .69, .53, .69, .45, .85, .61, .38, .76, .93, .69, .69, .45, .61},
			              {.78, 1.0, .70, .93, .76, .46, .69, .63, .85, .61, .54, .70, .46, .93, .69, .39},
			              {.83, .59, 1.0, .76, .35, .76, .53, .93, .69, .45, .59, .75, .51, .51, .28, .69},
			              {.60, .83, .78, 1.0, .59, .54, .76, .70, .93, .69, .36, .53, .29, .75, .51, .46},
			              {.55, .78, .48, .70, 1.0, .70, .93, .40, .63, .85, .78, .48, .70, .70, .93, .63},
			              {.60, .36, .78, .54, .59, 1.0, .76, .70, .46, .69, .83, .53, .75, .29, .51, .93},
			              {.38, .60, .55, .78, .83, .78, 1.0, .48, .70, .93, .60, .30, .53, .53, .75, .70},
			              {.65, .41/*43*/, .83, .59, .18/*20*/, .59/*60*/, .35/*36*/, 1.0, .76, .53, .41/*43*/, .83, .59, .59/*60*/, .35/*36*/, .76},
			              {.43, .65, .60, .83, .41/*43*/, .36/*38*/, .59/*60*/, .78, 1.0, .76, .19/*20*/, .60, .36, .83, .59, .54},
			              {.20, .43, .38, .60, .65, .60, .83, .55, .78, 1.0, .43, .38, .60, .60, .83, .78},
			              {.78, .54, .70, .46, .76, .93, .69, .63, .39, .61, 1.0, .70, .93, .46, .69, .85},
			              {.83, .59, .75, .51, .35/*36*/, .51/*53*/, .28/*29*/, .93, .69, .45, .59/*60*/, 1.0, .76, .76, .53, .69},
			              {.60, .36, .53, .29, .59, .75, .51, .70, .46, .69, .83, .78, 1.0, .54, .76, .93},
			              {.60, .83, .53, .75, .59/*60*/, .29/*30*/, .51/*53*/, .70, .93, .69, .36/*38*/, .78, .54, 1.0, .76, .46},
			              {.38, .60, .30, .53, .83, .53, .75, .48, .70, .93, .60, .55, .78, .78, 1.0, .70},
			              {.43, .19/*20*/, .60, .36, .41/*44*/, .83/*75*/, .59, .78/*48*/, .54, .76, .65, .60, .83, .36/*38*/, .59/*60*/, 1.0},
			              };
	
	/**
	 * This test tries to override attributes in subConcept B with wrong
	 * description. Exception is expected in each Instance
	 */
	public void test() {
		try {
			p = new Project();
			A = p.createTopConcept("A");
			B = p.createTopConcept("B");
			
			HashSet<String> l = new HashSet<String>();
			String[] l1 = { "a11", "a12" };
			l.addAll(Arrays.asList(l1));
			a1 = new SymbolDesc(B, "a1", l);
			
			HashSet<String> L = new HashSet<String>();
			String[] L1 = { "a21", "a22" };
			L.addAll(Arrays.asList(L1));
			a2 = new SymbolDesc(B, "a2", L);
			
			b1 = new ConceptDesc(A, "b1", B);
			b2 = new ConceptDesc(A, "b2", B);
			
			Instance B1 = B.addInstance("B1");

			B1.addAttribute(a1.getName(), a1.addSymbol("a11"));
			B1.addAttribute(a2.getName(), a2.addSymbol("a21"));
			
			Instance B2 = B.addInstance("B2");
			B2.addAttribute(a1.getName(), a1.getAttribute("a11"));
			B2.addAttribute(a2.getName(), a2.addSymbol("a22"));
			
			Instance B3 = B.addInstance("B3");
			B3.addAttribute(a1.getName(), a1.addSymbol("a12"));
			B3.addAttribute(a2.getName(), a2.getAttribute("a21"));
			
			Instance B4 = B.addInstance("B4");
			B4.addAttribute(a1.getName(), a1.getAttribute("a12"));
			B4.addAttribute(a2.getName(), a2.getAttribute("a22"));
			
			Instance i = A.addInstance("A1");
			Instance A1 = i;
			assertTrue(A1.addAttribute(b1.getName(), B1));
			assertTrue(A1.addAttribute(b2.getName(), B1));
			
			i = A.addInstance("A2");
			Instance A2 = i;
			assertTrue(A2.addAttribute(b1.getName(), B1));
			assertTrue(A2.addAttribute(b2.getName(), B2));
			
			i = A.addInstance("A3");
			Instance A3 = i;
			assertTrue(A3.addAttribute(b1.getName(), B1));
			assertTrue(A3.addAttribute(b2.getName(), B3));
			
			i = A.addInstance("A4");
			Instance A4 = i;
			assertTrue(A4.addAttribute(b1.getName(), B1));
			assertTrue(A4.addAttribute(b2.getName(), B4));
			
			i = A.addInstance("A5");
			Instance A5 = i;
			assertTrue(A5.addAttribute(b1.getName(), B2));
			assertTrue(A5.addAttribute(b2.getName(), B2));
			
			i = A.addInstance("A6");
			Instance A6 = i;
			assertTrue(A6.addAttribute(b1.getName(), B2));
			assertTrue(A6.addAttribute(b2.getName(), B3));
			
			i = A.addInstance("A7");
			Instance A7 = i;
			assertTrue(A7.addAttribute(b1.getName(), B2));
			assertTrue(A7.addAttribute(b2.getName(), B4));
			
			i = A.addInstance("A8");
			Instance A8 = i;
			assertTrue(A8.addAttribute(b1.getName(), B3));
			assertTrue(A8.addAttribute(b2.getName(), B3));
			
			i = A.addInstance("A9");
			Instance A9 = i;
			assertTrue(A9.addAttribute(b1.getName(), B3));
			assertTrue(A9.addAttribute(b2.getName(), B4));
			
			i = A.addInstance("A10");
			Instance A10 = i;
			assertTrue(A10.addAttribute(b1.getName(), B4));
			assertTrue(A10.addAttribute(b2.getName(), B4));
			
			i = A.addInstance("A11");
			Instance A11 = i;
			assertTrue(A11.addAttribute(b1.getName(), B2));
			assertTrue(A11.addAttribute(b2.getName(), B1));
			
			i = A.addInstance("A12");
			Instance A12 = i;
			assertTrue(A12.addAttribute(b1.getName(), B3));
			assertTrue(A12.addAttribute(b2.getName(), B1));
			
			i = A.addInstance("A13");
			Instance A13 = i;
			assertTrue(A13.addAttribute(b1.getName(), B4));
			assertTrue(A13.addAttribute(b2.getName(), B1));
			
			i = A.addInstance("A14");
			Instance A14 = i;
			assertTrue(A14.addAttribute(b1.getName(), B3));
			assertTrue(A14.addAttribute(b2.getName(), B2));
			
			i = A.addInstance("A15");
			Instance A15 = i;
			assertTrue(A15.addAttribute(b1.getName(), B4));
			assertTrue(A15.addAttribute(b2.getName(), B2));
			
			i = A.addInstance("A16");
			Instance A16 = i;
			assertTrue(A16.addAttribute(b1.getName(), B4));
			assertTrue(A16.addAttribute(b2.getName(), B3));
			
			SymbolFct f1 = a1.addSymbolFct("f1", true);
			f1.setSymmetric(false);
			f1.setSimilarity("a11", "a12", 0.7d);
			f1.setSimilarity("a12", "a11", 0.3d);
			
			SymbolFct f2 = a2.addSymbolFct("f2", true);
			f2.setSymmetric(false);
			f2.setSimilarity("a21", "a22", 0.05d);
			f2.setSimilarity("a22", "a21", 0.1d);
			
			AmalgamationFct amalgam1 = A.addAmalgamationFct(AmalgamationConfig.WEIGHTED_SUM, "F1", true);
			AmalgamationFct amalgam2 = B.addAmalgamationFct(AmalgamationConfig.WEIGHTED_SUM, "F2", true);
			
			amalgam2.setActiveFct(a1, f1);
			amalgam2.setActiveFct(a2, f2);
			amalgam1.setActiveFct(b1, amalgam2);
			amalgam1.setActiveFct(b2, amalgam2);
			
			Instance[] Instances = {A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16};
 			
			for (int k = 6; k<7; k++) {
				for (int j = 0; j<16; j++) {
					Double s = Instances[k].getConcept().getActiveAmalgamFct().calculateSimilarity(Instances[k],Instances[j]).getRoundedValue();
					assertTrue("sim(" + Instances[k].getName() + "," + Instances[j].getName() + ")="+ s + " should be " + results[k][j], s.equals(results[k][j]));
				}
			} 
			
//			p.setName("partOFTest");
//			p.setPath("/home/zilles/partOFTest.zip");
//			p.save();
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		
	}
	
}
