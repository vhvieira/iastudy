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

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import de.dfki.mycbr.core.DefaultCaseBase;
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.casebase.SymbolAttribute;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.model.SymbolDesc;
import de.dfki.mycbr.core.retrieval.Retrieval;
import de.dfki.mycbr.core.retrieval.Retrieval.RetrievalMethod;
import de.dfki.mycbr.core.similarity.AmalgamationFct;
import de.dfki.mycbr.core.similarity.Similarity;
import de.dfki.mycbr.core.similarity.SymbolFct;
import de.dfki.mycbr.core.similarity.TaxonomyFct;
import de.dfki.mycbr.core.similarity.config.AmalgamationConfig;
import de.dfki.mycbr.core.similarity.config.TaxonomyConfig;
import de.dfki.mycbr.util.Pair;

/**
 * Tests the standard retrieval engine based on a c with
 * two symbol attributes. Uses taxonomy and table as similarity types.
 * 
 * @author myCBR Team
 * 
 */
public class RetrievalTest extends TestCase {

	private DefaultCaseBase cb;
	private Project prj;
	private Concept car;
	private SymbolDesc manufacturerDesc;
	private SymbolDesc colorDesc;
	private TaxonomyFct f1;
	private SymbolFct f2;

	@Test
	public void testSequentialRetrieval() {
		try {
			prj = new Project();
			cb = prj.createDefaultCB("cb1");
			car = prj.createTopConcept("Car");

			HashSet<String> colors = new HashSet<String>();
			String[] colorsArray = { "green", "black", "blue", "white",
					"yellow", "red", "pink", "brown" };
			colors.addAll(Arrays.asList(colorsArray));
			colorDesc = new SymbolDesc(car,"color",colors);

			HashSet<String> manufacturers = new HashSet<String>();
			String[] manufacturersArray = { "BMW", "Audi", "VW", "Ford",
					"Mercedes", "SEAT", "FIAT" };
			manufacturers.addAll(Arrays.asList(manufacturersArray));
			manufacturerDesc = new SymbolDesc(car,"manufacturer",manufacturers);

			f1 = colorDesc.addTaxonomyFct("f1", true);
			SymbolAttribute red = (SymbolAttribute) colorDesc
					.getSymbolAttributes().toArray()[0];
			SymbolAttribute blue = (SymbolAttribute) colorDesc
					.getSymbolAttributes().toArray()[1];
			SymbolAttribute green = (SymbolAttribute) colorDesc
					.getSymbolAttributes().toArray()[2];
			SymbolAttribute white = (SymbolAttribute) colorDesc
					.getSymbolAttributes().toArray()[3];
			SymbolAttribute pink = (SymbolAttribute) colorDesc
					.getSymbolAttributes().toArray()[4];
			SymbolAttribute brown = (SymbolAttribute) colorDesc
					.getSymbolAttributes().toArray()[5];
			SymbolAttribute yellow = (SymbolAttribute) colorDesc
					.getSymbolAttributes().toArray()[6];

			f1.setParent(blue, red);
			f1.setParent(green, blue);
			f1.setParent(pink, white);
			f1.setParent(brown, pink);
			f1.setParent(yellow, brown);
			f1.setParent(red, pink);
			f1.setNodeSimilarity(colorDesc, Similarity.get(0.5));
			f1.setNodeSimilarity(white, Similarity.get(0.75));
			f1.setNodeSimilarity(pink, Similarity.get(0.88));
			f1.setNodeSimilarity(brown, Similarity.get(0.9));
			f1.setNodeSimilarity(red, Similarity.get(0.9));
			f1.setNodeSimilarity(blue, Similarity.get(0.95));
			
			f1.setQueryConfig(TaxonomyConfig.INNER_NODES_ANY);

			f2 = manufacturerDesc.addSymbolFct(
					"f2", true);
			
			f2.setSimilarity("BMW", "Audi", 0.60d);
			f2.setSimilarity("Audi", "VW", 0.20d);
			f2.setSimilarity("VW", "Ford", 0.40d);
			f2.setSimilarity("Ford", "Mercedes", 0.00d);
			f2.setSimilarity("BMW", "Mercedes", 0.60d);
			f2.setSimilarity("Audi", "Mercedes", 0.50d);
			f2.setSimilarity("Audi", "Ford", 0.20d);
			f2.setSimilarity("VW", "Mercedes", 0.10d);
			f2.setSimilarity("BMW", "VW", 0.10d);
			f2.setSimilarity("BMW", "Ford", 0.00d);
			
			AmalgamationFct amalgam = car.addAmalgamationFct(
					AmalgamationConfig.WEIGHTED_SUM, "f3", true);

			amalgam.setActiveFct(manufacturerDesc, f2);
			amalgam.setActiveFct(colorDesc, f1);
			//prj.getFctContainer().getActiveAmalgamFcts().put(carDesc, amalgam);
			
			
			
			Instance i = car.addInstance("car1");
			Instance Instance1 = i;
			cb.addCase(Instance1);
			Instance1.addAttribute(manufacturerDesc,manufacturerDesc.getAttribute("BMW"));
			Instance1.addAttribute(colorDesc,colorDesc.getAttribute("green"));

			i = car.addInstance("car2");
			Instance Instance2 = i;
			cb.addCase(Instance2);
			Instance2.addAttribute(manufacturerDesc.getName(),manufacturerDesc.getAttribute("BMW"));
			Instance2
					.addAttribute(colorDesc.getName(),colorDesc.getAttribute(
							"_undefined_"));

			i = car.addInstance("car3");
			Instance Instance3 = i;
			cb.addCase(Instance3);
			Instance3.addAttribute(manufacturerDesc.getName(),manufacturerDesc.getAttribute("Audi"));
			Instance3.addAttribute(colorDesc.getName(), colorDesc.getAttribute("green"));

			i = car.addInstance("car4");
			Instance Instance4 = i;
			cb.addCase(Instance4);
			Instance4.addAttribute(manufacturerDesc.getName(),manufacturerDesc.getAttribute(
					"_undefined_"));
			Instance4
					.addAttribute(colorDesc.getName(), colorDesc.getAttribute(
							"_undefined_"));

			Retrieval r = new Retrieval(car, cb);
			LinkedList<Double> results;

			i = car.addInstance("query");
			Instance q =  r.getQueryInstance();
			q.addAttribute(manufacturerDesc.getName(),manufacturerDesc.getAttribute("Audi"));
			q.addAttribute(colorDesc.getName(),colorDesc.getAttribute("red"));

//			System.out
//					.println("\n--------------------------- query ---------------------------------");
//			q.print();
			r.setRetrievalMethod(RetrievalMethod.RETRIEVE_SORTED);
			r.start();
			List<Pair<Instance, Similarity>> result = r.getResult(); 
			results = printResult(result);
			assertTrue(results + " should be [1.0, 0.8, 0.3, 0.0]",results.equals(Arrays.asList(new Double[] {
					1.0d, 0.8d, 0.3d, 0.0d })));

			i = car.addInstance("query");
			q =  r.getQueryInstance();
			q.addAttribute(manufacturerDesc.getName(),manufacturerDesc.getAttribute("Audi"));
			q.addAttribute(colorDesc.getName(),colorDesc.getAttribute("green"));

//			System.out
//					.println("\n--------------------------- query ---------------------------------");
//			q.print();
			r.start();
			result = r.getResult(); 
			results = printResult(result);
			assertTrue(results.equals(Arrays.asList(new Double[] { 
					1.0d, 0.8d, 0.3d, 0.0d })));

			i = car.addInstance("query");
			q =  r.getQueryInstance();
			q.addAttribute(manufacturerDesc.getName(), manufacturerDesc.getAttribute("BMW"));
			q.addAttribute(colorDesc.getName(), colorDesc.getAttribute("red"));
//			System.out
//					.println("\n--------------------------- query ---------------------------------");
//			q.print();
			r.start();
			result = r.getResult(); 
			results = printResult(result);
			assertTrue(results.equals(Arrays.asList(new Double[] { 1.0d, 0.8d,
					0.5d, 0.0d })));

			i = car.addInstance("query");
			q =  r.getQueryInstance();
			q.addAttribute(manufacturerDesc.getName(), manufacturerDesc.getAttribute("BMW"));
			q.addAttribute(colorDesc.getName(), colorDesc.getAttribute("green"));

//			System.out
//					.println("\n--------------------------- query ---------------------------------");
//			q.print();
			r.start();
			result = r.getResult(); 
			results = printResult(result);
			assertTrue(results.equals(Arrays.asList(new Double[] { 1.0d, 0.8d,
					0.5d, 0.0d })));

			i = car.addInstance("query");
			q =  r.getQueryInstance();
			q.addAttribute(manufacturerDesc.getName(), manufacturerDesc.getAttribute("VW"));
			q.addAttribute(colorDesc.getName(), colorDesc.getAttribute("white"));
//			System.out
//					.println("\n--------------------------- query ---------------------------------");
//			q.print();
			r.start();
			result = r.getResult(); 
			results = printResult(result);
			assertTrue(results.equals(Arrays.asList(new Double[] { 0.6d,
					0.55d, 0.05d, 0.0d })));

			i = car.addInstance("query");
			q = r.getQueryInstance();
			q.addAttribute(manufacturerDesc.getName(), manufacturerDesc.getAttribute("VW"));
			q.addAttribute(colorDesc.getName(), colorDesc.getAttribute("yellow"));
//			System.out
//					.println("\n--------------------------- query ---------------------------------");
//			q.print();
			r.start();
			result = r.getResult(); 
			results = printResult(result);
			assertTrue("Result should be [0.54, 0.49, 0.05, 0.0] but is "
					+ results, results.equals(Arrays.asList(new Double[] {
					0.54d, 0.49d, 0.05d, 0.0d })));

			i = car.addInstance("query");
			q =  r.getQueryInstance();
			q.addAttribute(manufacturerDesc.getName(), manufacturerDesc.getAttribute("VW"));
			q.addAttribute(colorDesc.getName(), colorDesc.getAttribute("_undefined_"));
//			System.out
//					.println("\n--------------------------- query ---------------------------------");
//			q.print();
			r.start();
			result = r.getResult(); 
			results = printResult(result);
			assertTrue("result should be [0.55,0.5,0.1,0.05] but is " + results, results.equals(Arrays.asList(new Double[] { 
					0.55d, 0.5d, 0.1d, 0.05d })));

		} catch (Exception e) {
			e.printStackTrace();
			assertTrue("Excpetion in retrievalTest: testSequentialRetrieval",
					false);
		}
	}

	private LinkedList<Double> printResult(List<Pair<Instance, Similarity>> result) {
		LinkedList<Double> sims = new LinkedList<Double>();
		for (Pair<Instance, Similarity> r : result) {
			System.out.println("\nSimilarity: " + r.getSecond().getValue()
					+ " to Instance: " + r.getFirst().getName());
			sims.add(r.getSecond().getValue());
		}
		return sims;
	}

}
