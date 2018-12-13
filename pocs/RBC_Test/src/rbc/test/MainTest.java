package rbc.test;

import java.util.Iterator;
import java.util.Vector;

import de.dfki.mycbr.CBRProject;
import de.dfki.mycbr.Helper;
import de.dfki.mycbr.MyCBR_Facade;
import de.dfki.mycbr.model.casebase.CaseInstance;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.retrieval.AssessedInstance;
import de.dfki.mycbr.retrieval.DefaultQuery;
import de.dfki.mycbr.retrieval.Explanation;
import de.dfki.mycbr.retrieval.RetrievalEngine;
import de.dfki.mycbr.retrieval.RetrievalResults;

public class MainTest {
	
	public static void main(String[] args) {
		  	  
			  long t1 = System.currentTimeMillis();
			  
			  final String projectName = "used_cars_flat";
//			  final String projectName = "rbc_test";
//			  final String projectName = "rbc_test_emagrecimento";
			  final String className = "Car";
//			  final String className = "Alimento";
			  
			  String projectsPath = MainTest.class.getResource(projectName + "_CBR_CASEBASE.XML").getPath();
			  
			  long tc_1 = System.currentTimeMillis();
			  
			  MyCBR_Facade facade = new MyCBR_Facade(projectsPath);
			  CBRProject project = facade.getProject();
			  
			  long tc_2 = System.currentTimeMillis();
			  
			  System.out.println("Tempo criação CBR: " + (tc_2 - tc_1) + " ms");
			  
			  ModelCls modelClass = facade.getModelClsByName(className);
			  DefaultQuery query = new DefaultQuery(modelClass);
//			  query = new 
			  ModelSlot slot = null;
			  RetrievalResults results = null;
			  
			  
			  // caseInstances is used for submitting a query from a case.
			  Vector<CaseInstance> caseInstances = 
			    new Vector<CaseInstance>(modelClass.getDirectCaseInstances());
			  
			  System.out.println("************** LISTANDO CASOS ***************");
			  for( Object caseInstanceObject: caseInstances ) {
				  System.out.println(caseInstanceObject);
			  }

			    // if the page has been loaded by submitting a query a value for
			    // each slot used by this query is specified by its name as POST variable
			  	CaseInstance modelInstance = caseInstances.get(1);
			  	System.out.println("Vai usar como base a instancia: " + modelInstance.getName());
			  	Iterator it_model = modelInstance.listSlots().iterator();
			    for ( Iterator it = modelClass.listSlots().iterator(); it.hasNext(); ) {
			      
			      slot = (ModelSlot) it.next();
			      ModelSlot slot_value = (ModelSlot) it_model.next();
			     		     
//			      Object valueType = slot.getValueType().newInstance(modelInstance.getSlotValue(slot_value).toString());
			      query.setSlotValue(slot_value, modelInstance.getSlotValue(slot_value));
			      System.out.println("Valor slot " + slot_value.getName() + 
				    		" = " + modelInstance.getSlotValue(slot_value) );
			    }
			  
			    long tr_1 = System.currentTimeMillis();
			    // perform retrieval
			    RetrievalEngine retrievalEngine = facade.getRetrievalEngine();
			    //project.newSMF(modelClass, "newSmfName")
			    //project.getActiveSMFs(modelClass)
			    try {
					results = retrievalEngine.retrieve(query, project.getActiveSMFs(modelClass) , true);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				long tr_2 = System.currentTimeMillis();
				
				System.out.println("Tempo retrieve cases: " + (tr_2 - tr_1) + " ms");
				
				// get the rankings which should be displayed
			    Vector ranking = results.getRanking();
			    Vector<ModelSlot> slots = new Vector<ModelSlot>(modelClass.listSlots());
			    
			    // By default, the rankings to be displayed 
			    // are the first five rankings 
			    System.out.println(" ************** TAMANHO RANKING = " + ranking.size());
			    
			    for (int j = 0; j < 5; j++) {
			    	AssessedInstance assessedInstance = (AssessedInstance) ranking.get(j);
				    Explanation explanation = assessedInstance.explanation; 
				    System.out.println("Instance = " + assessedInstance.inst.getName());
				    System.out.println("Similaridade = " + assessedInstance.similarity);
					      
				    for ( Iterator it = modelClass.listSlots().iterator(); it.hasNext(); ) {
					      
					    slot = (ModelSlot) it.next();
					    if ( slot != null ) {
					      explanation = assessedInstance.explanation.getLocalExplanation(slot);
					      
					      	System.out.println("Valor slot " + slot.getName() + 
						    		" = " + assessedInstance.inst.getSlotValue(slot) );
						          
						    String explanationTxt = "?";
						    if ( explanation != null ) {
						      explanationTxt = explanation.getComments().replaceAll("\n", "; ") 
					                       + "Similarity: " 
					                       + Helper.getSimilarityStr(explanation.getSimilarity());
						    }
						    
						    System.out.println("Explanation = " + explanationTxt);
					    }					    
					   
				    }
				}
			    long t2 = System.currentTimeMillis();
			    System.out.println("Tempo total execução = " + (t2-t1) + " ms");
	}

}
