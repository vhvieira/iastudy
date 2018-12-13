<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN"
    "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">

<%@ page import="java.util.*" %>
<%@ page import="de.dfki.mycbr.*" %>
<%@ page import="de.dfki.mycbr.retrieval.*" %>
<%@ page import="de.dfki.mycbr.model.vocabulary.*" %>
<%@ page import="de.dfki.mycbr.model.casebase.*" %>
<%@ page import="de.dfki.mycbr.modelprovider.*" %>

<head>
<title>myCBR</title>

<meta http-equiv="content-type" content="application/xhtml+xml; charset=utf-8" />
<meta http-equiv="Content-Style-Type" content="text/css" />
<meta name="Author" content="Laura Zilles" />
<meta name="Description" content="web application for mycbr's retrieval engine" />
<meta name="keywords" content="mycbr, retrieval" />
<script type="text/javascript">

function searchRankings(value){
	
  // value == first if the user clicked on the first arrow 
  // to display the ranking 1 to 5
  if ( value == "first" ) {
    
    // set the first ranking to be displayed = 0
    document.getElementById("begin").value = 0;

    if ( document.getElementById("maximum").value > 5 ) {
      document.getElementById("end").value = 5;
    } else {
      document.getElementById("end").value = 
        document.getElementById("maximum").value;
    }
    
    // submit the form in "index.jsp"
    document.forms[0].submit();
    
  } else if ( value == "back" ) {

    document.getElementById("begin").value = 
      --document.getElementById("begin").value;
    document.getElementById("end").value = 
      --document.getElementById("end").value;
    
    // submit the form in "index.jsp"
    document.forms[0].submit();
    
  } else if ( value == "forward" ) {

    document.getElementById("begin").value = 
      ++document.getElementById("begin").value;
    document.getElementById("end").value = 
      ++document.getElementById("end").value;
    
    // submit the form in "index.jsp"
    document.forms[0].submit();
    
  } else if ( value == "last" ) {

    document.getElementById("end").value = 
      document.getElementById("maximum").value;

    if ( document.getElementById("maximum").value > 4 ) {
      document.getElementById("begin").value = 
        document.getElementById("maximum").value - 5;
    } else {
      document.getElementById("begin").value = 0;
    }
    
    // submit the form in "index.jsp"
    document.forms[0].submit();
  }
}

function openCase(){
	
  // get the selected index
  var caseIndex = document.getElementById("fromCase").value;
	
  if ( caseIndex != "0" ) {
    window.open("index.jsp?case=" + caseIndex, "_self");
  } else {
    window.open("index.jsp", "_self");
  }
}

</script>

</head>
<body> 

  <%
	
  final String projectsPath = 
    "/home/lzilles/apps/apache-tomcat-5.5.28/webapps/myCBRWebDemo/projects";
  final String projectName = "used_cars_flat";
  final String className = "Car"; 
  
  MyCBR_Facade facade = new MyCBR_Facade(projectsPath + "/" + projectName + "_CBR_CASEBASE.XML");
  CBRProject project = facade.getProject();
  
  ModelCls modelClass = facade.getModelClsByName(className);
  DefaultQuery query = new DefaultQuery(modelClass);
  
  ModelSlot slot = null;
  RetrievalResults results = null;
  
  // caseInstances is used for submitting a query from a case.
  Vector<CaseInstance> caseInstances = 
    new Vector<CaseInstance>(modelClass.getDirectCaseInstances());	
  // If a case has been chosen, a parameter case with a number 
  // is specified in the request
  String index = request.getParameter("case");
  String undefined = SpecialValueHandler.SPECIAL_VALUE_UNDEFINED.toString();
  
  boolean isEmpty = true;
  if ( ( index != null ) && ( index != "" ) ) {
    
    // If a case has been chosen the current query is based on it
    CaseInstance caseInstance = caseInstances.get(Integer.parseInt(index)-1);
    query = new DefaultQuery(caseInstance);
    
  } else {
    
    // if the page has been loaded by submitting a query a value for
    // each slot used by this query is specified by its name as POST variable			 
    for ( Iterator it = modelClass.listSlots().iterator(); it.hasNext(); ) {
      
      slot = (ModelSlot) it.next();
      // Get value of POST variable
      Object queryValue = request.getParameter(slot.getName().replaceAll(" ", "_"));
      
      // if the slot was not used by the query
      // the value should be SpecialValueHandler.SPECIAL_VALUE_UNDEFINED
      if ( queryValue == null || "".equals(queryValue) ||
                                              undefined.equals(queryValue) ) {
	query.setSlotValue(slot, SpecialValueHandler.SPECIAL_VALUE_UNDEFINED);
	continue;
      }
      isEmpty = false;
      
      // set the value for the slot of the current query to the value used
      // by the submitted query, so that the last submitted query is visible
      Object valueType = slot.getValueType().newInstance(queryValue.toString());
      query.setSlotValue(slot, valueType);
    }
  }
  
  // perform retrieval
  if ( !isEmpty ) {
    RetrievalEngine retrievalEngine = facade.getRetrievalEngine();
    results = retrievalEngine.retrieve(query, project.getActiveSMFs(modelClass), 
      true);
  }%> 
  
  <!-- display general information -->
  <p>Project: <%= project.getProjectName() %>   &gt;   
  Base class: <%= query.getModelCls().getName() %></p>	
  
  <form id="form" action="index.jsp" method="post" accept-charset="utf-8">
  
  <table style="border:0; width:100%;">
  
  <%   
  
  int slotId = 0;
  int j = 0;
  
  if ( modelClass != null ) {
    
    for ( Iterator it = modelClass.listSlots().iterator(); it.hasNext(); ) {
      
      slot = (ModelSlot) it.next();
      slotId++;
      Object queryValue = query.getSlotValue(slot);
      String queryValueString = null;

      if ( queryValue == null ) {
	queryValueString = undefined;
      } else {
	queryValueString = queryValue.toString();
      }
      
      // create a new column for each fifth slot
      if ( j%4 == 0 ) { %>
        <tr>
      <% } %>
      
      <td><%= slot.getName() %></td>
      <td>
      
        <%ValueType valueType = slot.getValueType();
      
        if ( valueType == ValueType.STRING || valueType == ValueType.INTEGER || 
             valueType == ValueType.FLOAT ){
	
	  if ( ( slot.getMinimumValue() != null ) && 
             ( slot.getMaximumValue() != null ) ) { %>
	    <input id="min<%= (slot.getName()).replaceAll(" ", "_") %>" 
                   type="hidden" value="<%=slot.getMinimumValue() %>" />
	    <input id="max<%= (slot.getName()).replaceAll(" ", "_") %>" 
                   type="hidden" value="<%=slot.getMaximumValue()%>" />
	  <% } %>
	  
	  <input title="min:<%=slot.getMinimumValue() %> 
                 max:<%=slot.getMaximumValue() %>" 
	         name="<%= slot.getName().replaceAll(" ", "_") %>" 
	         id="<%=slot.getName().replaceAll(" ", "_")%>" 
	         type="text" 
	         value="<%= query.getSlotValue(slot) %>" />
	  
	<% } else if ( slot.getValueType() == ValueType.SYMBOL ) { %>
	  
	  <select name="<%= (slot.getName()).replaceAll(" ", "_") %>" >
	    <option <%= (undefined.equals(queryValueString)? 
                                        "selected=\"selected\"":"") %>>
              <%= undefined %>
            </option>
	  
            <% for ( Iterator itValues = slot.getAllowedValues().iterator(); 
                     itValues.hasNext(); ) {
	    
              Object allowedValue = itValues.next();
	  
              if (!(allowedValue instanceof String)) { // do nothing
	        continue;
	      }
	  
              String allowedValueString = (String) allowedValue;%>
	      <!-- else select the index if it was used in the last query -->
	      <option <%= (allowedValueString.equals(queryValueString)? 
                                                "selected=\"selected\"":"") %> >
                <%= allowedValueString %>
              </option>
	    <% } %>
	  </select>
	  
	<% } else { %>				
	  N.A.
	<% } %>
	  
      </td>
	 
      <% if ( j%4 == 3 ) { %>
        </tr>
      <% } 
      
      j++;
    } // end for
	
    // if last row has not been closed, since the slots are not a multiple of 4
    if ( (--j)%4 != 3 ) { %> 
      </tr>
    <% }
  } else { %>
  
    <tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr>

  <% } %>
	
    <tr>
      <td>From Case:</td>
      <td>
	<select title="select query from case" name="fromCase" id="fromCase" 
                onchange="openCase();" >
	  <option value="0"></option>
	
	  <%

	  int value = 1;

          // if a case was chosen in the last query, it should still be selected
	  int caseIndex = 0; 
	  if ( ( index != null ) && ( index != "" ) ) {
	    caseIndex = Integer.parseInt(index);
	  }
	
	  for( Object caseInstanceObject: caseInstances ) {
	    
            // if a selection was made in an earlier than the last query else
            // if a selection was made in the last query
	    if ( (Integer.toString(value)).equals(
                                        request.getParameter("fromCase")) ) { %>

	      <option value="<%= value %>" selected="selected" >
                <%= caseInstanceObject.toString() %>
              </option>

	    <% } else if ( (Integer.toString(value)).equals(
                                        request.getParameter("case")) ) { %>

	      <option value="<%= value %>" selected="selected" >
                <%= caseInstanceObject.toString() %>
              </option>

	    <% } else { %>

	      <option value="<%= value %>">
                <%= caseInstanceObject.toString() %>
              </option>

	    <% }
	    value++;
	    
	  } %>

	</select>		
      </td>
      <td>&nbsp;</td>
      <td>&nbsp;</td>
    </tr>
	  
    <tr>
      <td>
        <input class="submit" type="submit" value="submit"/>
      </td>
      <td>
        <input class="submit" type="button" value="reset" 
                 onclick="window.open('index.jsp','_self');" />
      </td>
      <td>&nbsp;</td>
      <td>&nbsp;</td>
    </tr>
	  
  </table> <%-- end table queryParams --%>
	  
  <% if ( results != null ) {
	  
    // get the rankings which should be displayed
    Vector ranking = results.getRanking();
    Vector<ModelSlot> slots = new Vector<ModelSlot>(modelClass.listSlots());
	  
    // By default, the rankings to be displayed 
    // are the first five rankings 
    int begin = 0;
    int end = Math.min(ranking.size(), 5);
    
    // if the hidden field "begin" does exist and has a value
    // use this as the first ranking to be displayed.
    // Use the value of the hidden field "end" as the 
    // last ranking to be displayed
    if((request.getParameter("begin")!=null)&&(request.getParameter("begin")!="")){
      begin = Integer.parseInt(request.getParameter("begin"));
      end = Integer.parseInt(request.getParameter("end"));
    } %>
	  
    <p>
      
      <% // if the first ranking is not shown, enable the first and second arrow 
        if ( begin != 0 ) {  %>
        <a href="javascript:searchRankings('first');">
          [1 - <%= 5 %>]
        </a>
        <a href="javascript:searchRankings('back');">
          [<%= begin %> - <%= end-1 %>]
        </a>
      <% }

      // if the last ranking is not shown, enable the third and fourth arrow
      if ( end < ranking.size() ) {  %>
	<a href="javascript:searchRankings('forward');">
          [<%= begin + 2 %> - <%= end + 1 %>]
        </a>
	<a href="javascript:searchRankings('last');">
          [<%= ranking.size() - 4 %> - <%= ranking.size() %>]
        </a>			
      <% } %>
    </p>
		    
    <table style="border:0;">
    
      <% for ( int row=-1 ; row < slots.size(); row++ ) {
		    
        slot = null;
	if ( row > -1 ) {
	  slot = slots.get(row); 
	}%>
		    
	<tr>
	  <% if ( row == -1 ) { %>
		    
	    <td>
              <%= (slot==null? "&nbsp;": slot.getName()) %>
            </td>
	    <td>
              <%= (slot==null? "Query": query.getSlotValue(slot)) %>
            </td> 	
		    
	  <% } else { %>
		    
	    <% if ( row%2 == 0 ) { %>
	    
	      <td>
                <%= (slot==null? "&nbsp;": slot.getName()) %>
              </td>
	      <td>
                <%= (slot==null? "Query": query.getSlotValue(slot)) %>
              </td>
		    
	    <% } else { %>
		    
	      <td style="background-color : #efefef;" >
                <%= (slot==null? "&nbsp;": slot.getName()) %>
              </td>
	      <td style="background-color : #efefef;" >
                <%= (slot==null? "Query": query.getSlotValue(slot)) %>
              </td>
		    
	    <% } %>
	  <% }
		    
	  for ( int i = begin; i < end; i++ ) {
		      
	    AssessedInstance assessedInstance = (AssessedInstance)ranking.get(i);
	    Explanation explanation = assessedInstance.explanation; 
		      
	    if ( slot != null ) {
	      explanation = assessedInstance.explanation.getLocalExplanation(slot);
	    }
		      
	    String explanationTxt = "?";
	    if ( explanation != null ) {
	      explanationTxt = explanation.getComments().replaceAll("\n", "; ") 
                       + "Similarity: " 
                       + Helper.getSimilarityStr(explanation.getSimilarity());
	    }
		      
	    if ( slot == null ) { %>			
	      <td title="<%= explanationTxt %>">
	        <%= "Rank: " + (i+1) + "<br />Name: " 
                             + assessedInstance.inst.getName() 
                             + "<br />Similarity: " 
                             + Helper.getSimilarityStr(assessedInstance.similarity) %>
	      </td>
			
	    <% } else if ( row != -1 ) {
			
	      if ( row%2 == 0 ) { %>	
			
	        <td title="<%= explanationTxt %>">
	          <%= assessedInstance.inst.getSlotValue(slot)%> 
			
	      <% } else { %>
			
	        <td style="background-color : #efefef;" title="<%= explanationTxt %>">
		<%= assessedInstance.inst.getSlotValue(slot)%> 
		
	      <% } %> 
			
	        </td>
			
	    <% } %>
			
	  <% } %> <!-- end for -->
			
	</tr>
			
      <% } %> <!-- end for -->
		        
    </table>
		        
    <p>
      <%-- Hidden fields to save the rankings which should be displayed --%>
      <input id="begin" name="begin" type="hidden" value="<%= begin %>"/>
      <input id="end" name="end" type="hidden" value="<%= end %>"/>
      <%-- Number of rankings available --%>
      <input id="maximum" name="maximum" 
        type="hidden" value="<%= ranking.size() %>"/> 
    </p>
		
  <% } %> <!-- end outer if -->
	        
  </form>
	        
</body>
</html>