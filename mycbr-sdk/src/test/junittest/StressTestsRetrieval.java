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
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.retrieval.Retrieval;
import org.junit.Test;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kerstin on 3/28/14.
 */
public class StressTestsRetrieval {

    // Attributes grow exponentially
    @Test
    public void testRetievalIncreasingCases (){
        int noAtts = 20;
        int cases = 1;
        int noCaseStep = cases;
        int maxNoCases = 200;
        long duration = 0L;

        SimpleDateFormat dateFormat = new SimpleDateFormat();
        dateFormat.applyPattern("h:mm:ss");

        FileWriter fw = null;

        try {
            fw = new FileWriter(System.getProperty("user.dir") + "/src/test/projects/StressTest/testRetievalIncreasingCasesAndAttributes"+maxNoCases+".csv");

            PrintWriter pw = new PrintWriter(fw);
            pw.print("noAtts");
            pw.print(",");
            pw.print("noCases");
            pw.print(",");
            pw.print("duration");
            pw.print("\n");


            StressTestFramework stf = new StressTestFramework();
            String projectPath = stf.initStressTestFramework(noAtts, cases);


            Project p = new Project(projectPath);
            while (p.isImporting()){
                Thread.sleep(1);
            }

            cases++;
            Concept mainDesc = p.getConceptByID("main");
            DefaultCaseBase cb = (DefaultCaseBase) p.getCaseBases().get("casebase");

            // create query
            Retrieval r = new Retrieval(mainDesc, cb);
            Instance query = r.getQueryInstance();
            do{
                // add Case
                String caseName = "case" + String.valueOf(cases);
                Instance instance = mainDesc.addInstance(caseName);
                for (String attName : mainDesc.getAttributeDescs().keySet()){
                    instance.addAttribute(mainDesc.getAttributeDesc(attName), (int) (Math.random() * 100));
                }
                cb.addCase(instance);


                for (String attName : mainDesc.getAttributeDescs().keySet()){
                    query.addAttribute(mainDesc.getAttributeDesc(attName), (int) (Math.random() * 100));
                }

                // do retrieval
                Date start = new Date();
                r.setRetrievalMethod(Retrieval.RetrievalMethod.RETRIEVE_SORTED);
                r.start();
                Date end = new Date();
                duration = end.getTime() - start.getTime();

                // adding results to writer
                double dur = ((double) duration) / 1000;
                DecimalFormat decFormat = new DecimalFormat("###,###,##0.000");
                decFormat.format(dur);
                pw.print(noAtts);
                pw.print(",");
                pw.print(r.getResult().size());
                pw.print(",");
                pw.print(dur);
                pw.print("\n");

                // counting
                cases++;
            } while (cases < maxNoCases);
            pw.flush();
            pw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
