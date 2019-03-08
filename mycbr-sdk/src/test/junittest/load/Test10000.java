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

package test.junittest.load;


import de.dfki.mycbr.core.Project;
/**
 * tests the import functionality of XML files generated by myCBR v2.6.4 to < v3.0
 * 
 * @author myCBR Team
 * 
 */
public class Test10000 {

	public static void main(String args[]) {
		try {
			new Project("/data/workspace/mycbr/samples/Used Cars (flat)/used_cars_flat10000_CBR_SMF.zip");
//		Concept car = project.getConceptByID("Car");
//		Retrieval r = new Retrieval(car);
//		SequentialRetrieval sr = new SequentialRetrieval(project,r);
//		double total = 0;
//		for(int i =0; i<1000; i++) {
//			Date start = new Date();
//			sr.retrieve((DefaultCaseBase) project.getCaseBases().values().iterator().next(), car.getInstance("Car_5521"));
//
//			Date end = new Date();
//			
//			long duration = end.getTime()
//			- start.getTime();
//	
//			SimpleDateFormat dateFormat = new SimpleDateFormat();
//			dateFormat.applyPattern("h:mm:ss");
//	
//			double dur = ((double) duration) / 1000;
//			DecimalFormat decFormat = new DecimalFormat("###,###,##0.000");
//			decFormat.format(dur);
//			
//			System.out.println(i + ": " + dur );
//			total += dur;
//		}
//		System.out.println("sdk avg: " + total/1000);
//		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
