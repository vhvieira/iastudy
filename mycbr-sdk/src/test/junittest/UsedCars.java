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

import de.dfki.mycbr.core.DefaultCaseBase;
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Attribute;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.model.AttributeDesc;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.retrieval.Retrieval;
import de.dfki.mycbr.core.retrieval.Retrieval.RetrievalMethod;
import de.dfki.mycbr.core.similarity.Similarity;
import de.dfki.mycbr.util.Pair;
import junit.framework.TestCase;

import java.util.Map;

/**
 * @author myCBR Team
 * 
 */
public class UsedCars extends TestCase {

	public void test() {
//		String projectsPath = "C:\\mycbr\\projects\\Web\\";
		String projectsPath = System.getProperty("user.dir") + "/src/test/projects/Web/";
		String projectName = "used_cars_flat";
		String conceptName = "Car";

		Project project = null;
		Concept modelClass = null;

		try {
			// load project
			double start = System.currentTimeMillis();
			project = new Project(projectsPath + projectName + ".zip");
			while (project.isImporting()) {
				
			}
			System.out.println((System.currentTimeMillis()-start)/1000);
			modelClass = project.getConceptByID(conceptName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// create case bases and assign the case bases that will be used for
		// submitting a query from a case.
		DefaultCaseBase cb = (DefaultCaseBase) project.getCaseBases().get(
				"CaseBase0");
		String caseID = "Car870";
		
		Retrieval r = new Retrieval(modelClass, cb);
		r.setRetrievalMethod(RetrievalMethod.RETRIEVE_SORTED);
		Instance query = r.getQueryInstance();

		Instance caze = modelClass.getInstance(caseID);

		for (Map.Entry<AttributeDesc, Attribute> e : caze.getAttributes()
				.entrySet()) {
			query.addAttribute(e.getKey(), e.getValue());
		}
		r.start();
		while (!r.isFinished()) {

		}

		@SuppressWarnings("unchecked")
		Pair<Instance, Similarity>[] result = new Pair[r.size()];
    	int index = 0;
    	for(Pair<Instance,Similarity> e: r.getResult()) {
    		result[index++] = e; //new Pair<Instance, Similarity>(e.getKey(), e.getValue());
    	}
		for ( int i = 0; i < 957; i++ ) {
			System.out.println(result[i].getFirst().getName() + " " + result[i].getSecond().getRoundedValue());

		}
	}
}
