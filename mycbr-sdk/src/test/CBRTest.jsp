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
		String projectsPath = "/home/lzilles/apps/apache-tomcat-5.5.28/webapps/myCBRWebDemo/projects/";
		String projectName = "used_cars_flat";
		String conceptName = "Car";

		Project project = null;
		Concept modelClass = null;
		
		try {
			// load project
			project = new Project(projectsPath + projectName + ".zip");
			modelClass = project.getConceptByID(conceptName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// create case bases and assign the case bases that will be used for
		// submitting a query from a case.
		DefaultCaseBase cb = (DefaultCaseBase)project.getCaseBases().values().iterator().next();
		Retrieval r = new Retrieval(modelClass, cb);
		Instance query = r.getQuery();
		
		for (AttributeDesc d: modelClass.getAllAttributeDescs().values()) {
			Object queryValue = request.getParameter(d.getName().replaceAll(" ", "_"));
		}
		List<Pair<Case, Similarity>> results = r.getRetrievalEngine().retrieveK(cb, query, 5);
		Pair<Case,Similarity>[] ranking = (Pair[])results.toArray();
		%> 
		  
		  <!-- display general information -->
		  <p>Project: <%= project.getName() %>   &gt;   
		  Base class: <%= query.getConcept().getName() %></p>	
		  
		  <form id="form" action="index.jsp" method="post" accept-charset="utf-8">
		  
		  <table style="border:0; width:100%;">
		  
		  <%   

		  int slotId = 0;
		  int j = 0;
		  
		  if ( modelClass != null ) {
		    
		    for (AttributeDesc d: modelClass.getAllAttributeDescs().values()) {
		      slotId++;
		      Attribute queryValue = query.getAttForDesc(d);
		      String queryValueAsString = queryValue.getValueAsString();
		      
		      if ( j%4 == 0 ) { %>
		        <tr>
		      <% } %>
		      
		      <td><%= d.getName() %></td>
		      <td>
		      
		      <%
		      
		      if (d instanceof IntegerDesc) {
		    	  IntegerDesc desc = (IntegerDesc)d;
		    	  %>
		  	    <input id="min<%= (d.getName()).replaceAll(" ", "_") %>" 
		                     type="hidden" value="<%=desc.getMin() %>" />
		  	    <input id="max<%= (d.getName()).replaceAll(" ", "_") %>" 
		                     type="hidden" value="<%=desc.getMax()%>" />
		  	  
		  	  
		  	  <input title="min:<%=desc.getMin() %> 
		                   max:<%=desc.getMax() %>" 
		  	         name="<%= d.getName().replaceAll(" ", "_") %>" 
		  	         id="<%=d.getName().replaceAll(" ", "_")%>" 
		  	         type="text" 
		  	         value="<%= queryValueAsString %>" />
		  	  
		  	<% } else if ( d instanceof FloatDesc) { 
		  	 FloatDesc desc = (FloatDesc)d;
	    	  %>
	  	    <input id="min<%= (d.getName()).replaceAll(" ", "_") %>" 
	                     type="hidden" value="<%=desc.getMin() %>" />
	  	    <input id="max<%= (d.getName()).replaceAll(" ", "_") %>" 
	                     type="hidden" value="<%=desc.getMax()%>" />
	  	  
	  	  
	  	  <input title="min:<%=desc.getMin() %> 
	                   max:<%=desc.getMax() %>" 
	  	         name="<%= d.getName().replaceAll(" ", "_") %>" 
	  	         id="<%=d.getName().replaceAll(" ", "_")%>" 
	  	         type="text" 
	  	         value="<%= queryValueAsString %>" />
	  	       <% } else if ( d instanceof StringDesc) { 
		    	  %>
			  	  <input title="min: 
	                   max:" 
	  	         name="<%= d.getName().replaceAll(" ", "_") %>" 
	  	         id="<%=d.getName().replaceAll(" ", "_")%>" 
	  	         type="text" 
	  	         value="<%= queryValueAsString %>" />
			  	<% } else if ( d instanceof SymbolDesc) { 
			  		SymbolDesc desc = (SymbolDesc)d;
			  	
			    	  %>
			    	  
			    	  <select name="<%= (d.getName()).replaceAll(" ", "_") %>" >
			  	    <option <%= (UNDEFINED_SPECIAL_ATTRIBUTE.equals(queryValueString)? 
			                                          "selected=\"selected\"":"") %>>
			                <%= undefined %>
			              </option>
			  	  
			              <% for ( String allowedValueString: desc.getAllowedValues() ) {
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
			        if ( end < results.size() ) {  %>
			  	<a href="javascript:searchRankings('forward');">
			            [<%= begin + 2 %> - <%= end + 1 %>]
			          </a>
			  	<a href="javascript:searchRankings('last');">
			            [<%= results.size() - 4 %> - <%= results.size() %>]
			          </a>			
			        <% } %>
			      </p>
			  		    
			      <table style="border:0;">
			      int row = -1;
			      <tr>
			  		    
			  	    <td>&nbsp;</td>
			  	    <td>Query</td> 
			  	  for ( int i = begin; i < end; i++ ) {
				      <td title="">
				        <%= "Rank: " + (i+1) + "<br />Name: " 
			                             + ranking[i].getFirst().getName()
			                             + "<br />Similarity: " 
			                             + ranking[i].getSecond().getRoundedValue() %>
				      </td>
			  	  }
			  	</tr>
			        <% for (AttributeDesc d: modelClass.getAllAttDescs()) {
			          %>
			  		    
			  	<tr>
			  		    
			  	    <% if ( row%2 == 0 ) { %>
			  	    
			  	      <td>
			  	      <%= (d.getName()) %>
		              </td>
		  	    <td>
		                <%= (query.getAttForDesc(d)) %>
			                </td>
			  		    
			  	    <% } else { %>
			  		    
			  	      <td style="background-color : #efefef;" >
			                  <%= (d.getName()) %>
			                </td>
			  	      <td style="background-color : #efefef;" >
			                  <%= (query.getAttForDesc(d)) %>
			                </td>
			  		    
			  	  <% }
			  		    
			  	  for ( int i = begin; i < end; i++ ) {
			  		      
			  			
			  	      if ( row%2 == 0 ) { %>	
			  			
			  	        <td>
			  	          <%= ranking[i].getFirst().getAttForDesc(d).valueAsString() %> 
			  			
			  	      <% } else { %>
			  			
			  	        <td style="background-color : #efefef;" >
			  		<%= ranking[i].getFirst().getAttForDesc(d).valueAsString() %> 
			  		
			  	      <% } %> 
			  			
			  	        </td>
			  			
			  			
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
			          type="hidden" value="<%= ranking.length %>"/> 
			      </p>
			  		
			    <% } %> <!-- end outer if -->
			  	        
			    </form>
			  	        
			  </body>
			  </html>