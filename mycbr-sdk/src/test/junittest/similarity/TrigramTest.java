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

import java.util.Arrays;
import java.util.LinkedList;

import de.dfki.mycbr.util.Pair;
import junit.framework.TestCase;

import org.junit.Test;

import de.dfki.mycbr.core.DefaultCaseBase;
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.model.FloatDesc;
import de.dfki.mycbr.core.model.IntegerDesc;
import de.dfki.mycbr.core.model.StringDesc;
import de.dfki.mycbr.core.model.SymbolDesc;
import de.dfki.mycbr.core.retrieval.Retrieval;
import de.dfki.mycbr.core.retrieval.Retrieval.RetrievalMethod;
import de.dfki.mycbr.core.similarity.AmalgamationFct;
import de.dfki.mycbr.core.similarity.Similarity;

/**
 * Test the trigram similarity by importing the myCBR project trigramTest.
 * @author myCBR Team
 *
 */
public class TrigramTest extends TestCase {


	@Test
	public void testMyCBRImport() {
		
		try {
			// set up the project
			//Project p = new Project(File.separator + "home"  + File.separator + "zilles" + File.separator + "worhspace" + File.separator + "myCBR2" + File.separator + "projects/UsedCarsFlat/trigramTest_CBR_SMF.XML");
			Project p = new Project(System.getProperty("user.dir") + "/src/test/projects/UsedCarsFlat/trigramTest_CBR_SMF.XML");
			Concept car = p.getConceptByID("Car");

			// disable other attributes so there is only the
			// string attribute taken into account
			SymbolDesc body = (SymbolDesc)car.getAttributeDescs().get("Body");
			AmalgamationFct f = car.getActiveAmalgamFct();
			f.setActive(body,false);
			IntegerDesc carCode = (IntegerDesc)car.getAttributeDescs().get("Car Code");
			f.setActive(carCode,false);
			IntegerDesc CCM = (IntegerDesc)car.getAttributeDescs().get("CCM");
			f.setActive(CCM,false);
			SymbolDesc color = (SymbolDesc)car.getAttributeDescs().get("Color");
			f.setActive(color,false);
			IntegerDesc doors = (IntegerDesc)car.getAttributeDescs().get("Doors");
			f.setActive(doors,false);
			SymbolDesc gas = (SymbolDesc)car.getAttributeDescs().get("Gas");
			f.setActive(gas,false);
			SymbolDesc manufacturer = (SymbolDesc)car.getAttributeDescs().get("Manufacturer");
			f.setActive(manufacturer,false);
			IntegerDesc miles = (IntegerDesc)car.getAttributeDescs().get("Miles");
			f.setActive(miles,false);
			SymbolDesc model = (SymbolDesc)car.getAttributeDescs().get("Model");
			f.setActive(model,false);
			IntegerDesc power = (IntegerDesc)car.getAttributeDescs().get("Power");
			f.setActive(power,false);
			FloatDesc price = (FloatDesc)car.getAttributeDescs().get("Price");
			f.setActive(price,false);
			IntegerDesc speed = (IntegerDesc)car.getAttributeDescs().get("Speed");
			f.setActive(speed,false);
			IntegerDesc year = (IntegerDesc)car.getAttributeDescs().get("Year");
			f.setActive(year,false);
			IntegerDesc zip = (IntegerDesc)car.getAttributeDescs().get("ZIP");
			f.setActive(zip,false);
			
			DefaultCaseBase cb = (DefaultCaseBase)p.getCaseBases().get("CaseBase0");
			Retrieval r = new Retrieval(car, cb);
			
			Instance q = r.getQueryInstance();
			Instance c1 = car.addInstance("c1");
			Instance c2 = car.addInstance("c2");
			
			StringDesc s1 = (StringDesc) car.getAttributeDescs().get("used_cars_flat_Class0");
			StringDesc s2 = (StringDesc) car.getAttributeDescs().get("used_cars_flat_Class1");
			f.setActive(s2,false);
			q.addAttribute(s1.getName(),s1.getStringAttribute("RECEIEVE"));
			
			// add 2 cases
			Instance case1 = c1;
			Instance case2 = c2;
			// other attributes are automatically set to _undefined_
			case1.addAttribute(s1.getName(),s1.getStringAttribute("RECIEVE"));
			case2.addAttribute(s1.getName(),s1.getStringAttribute("RECEIEVE"));

            cb.addCase(case1);
            cb.addCase(case2);

//			System.out.println("\n--------------------------- query ---------------------------------");
//			q.print();
			r.setRetrievalMethod(RetrievalMethod.RETRIEVE_K_SORTED);
			r.setK(3);
			r.start();
			LinkedList<Double> results = printResult(r);
			assertTrue("result is: " + results.toString() + " but should be [1.0,0.38,0.33]", results.equals(Arrays.asList(new Double[]{1.0d, 0.38d, 0.33d})));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue("Excpetion in UsedCarsFlatTest: testMyCBRImport",false);
		}
	}
	
	/**
	 * prints the retrieval result to standard output
	 * and returns a list of similarities given 
	 * @param result retrieval result
 	 * @return
	 */
    private LinkedList<Double> printResult(Retrieval result) {
        LinkedList<Double> sims = new LinkedList<Double>();
        for (Pair<Instance, Similarity> pair : result.getResult()) {
//			System.out.println("\nSimilarity: " + pair.getSecond() + " to case:");
//			pair.getFirst();
            sims.add(pair.getSecond().getRoundedValue());
        }
        return sims;
    }
	
}
