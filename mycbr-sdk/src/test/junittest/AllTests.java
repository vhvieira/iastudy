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

import junit.framework.Test;
import junit.framework.TestSuite;
import test.junittest.casebase.ConceptWith2PartOfTest;
import test.junittest.casebase.OverrideAttributeTest;
import test.junittest.casebase.OverrideAttributeTest2;
import test.junittest.model.ConceptDescTest;
import test.junittest.model.ConceptTest;
import test.junittest.similarity.*;

/**
 * Runs all jUnit tests 
 * 
 * @author myCBR Team
 *
 */
public class AllTests {

	public static Test suite() {
		
		TestSuite suite = new TestSuite("myCBR SDK Tests");
		//$JUnit-BEGIN$
		// package
        suite.addTestSuite(ImportTest.class);
		suite.addTestSuite(MultipleAttTest.class);
		suite.addTestSuite(MyCBRImportTest.class);
		suite.addTestSuite(RetrievalTest.class);
		suite.addTestSuite(TrigramTest.class);

		// case base package
		suite.addTestSuite(ConceptWith2PartOfTest.class); 
		suite.addTestSuite(OverrideAttributeTest.class); 
		suite.addTestSuite(OverrideAttributeTest2.class); 
		
		// model package
		// suite.addTestSuite(BooleanDescTest.class); TODO
		// suite.addTestSuite(ChangeDescTest.class); TODO
		suite.addTestSuite(ConceptDescTest.class);
		suite.addTestSuite(ConceptTest.class);
//		suite.addTestSuite(IntervalDescTest.class); TODO
//		suite.addTestSuite(NumberDescTest.class); TODO
//		suite.addTestSuite(StringDescTest.class); TODO
//		suite.addTestSuite(SymbolDescTest.class); TODO 

		// similarity package
		suite.addTestSuite(AdvancedNumberTest.class);
		suite.addTestSuite(AdvancedNumberTestDouble.class);
		suite.addTestSuite(OrderedSymbolTest.class);
		suite.addTestSuite(StandardNumberTest.class);
		suite.addTestSuite(StringFunctionTest.class);
		suite.addTestSuite(SymbolFunctionTest.class);
		suite.addTestSuite(TaxonomyTest.class);
		suite.addTestSuite(TrigramTest.class);
		suite.addTestSuite(DateFunctionTest.class);

        // load package
        //suite.addTestSuite(Test1000.class);

        //$JUnit-END$
		return suite;
	}

}
