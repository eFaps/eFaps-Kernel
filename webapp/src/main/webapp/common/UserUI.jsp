<%--
 
  Copyright 2003-2007 The eFaps Team
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
       http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 
  Author:          tmo
  Revision:        $Rev$
  Last Changed:    $Date$
  Last Changed By: $Author$
 
--%> 

<%@page import="org.efaps.db.SearchQuery"%>
<%@page import="org.efaps.db.Context"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html;"/>
		<title></title>
	</head>
	<body>
		<table>
		<tr><td>
		<form>
		<input name="Name" type="text" size="30" maxlength="30" value="*">
		<button type="submit"> Suchen</button>	</td></tr><tr><td>
	<%	
		  if (Context.getThreadContext().getParameter("Name")!=null){
  			SearchQuery query = new SearchQuery();
  			query.setQueryTypes("Admin_User_Person");
      		query.addSelect("ID");
      		query.addSelect("Name");
      		query.execute();

      while (query.next()) {
       	String name = query.get("Name").toString();
       	String ID = query.get("ID").toString();
       	%>	
  		 <input type="radio" name="radiobuttons" value="<%=ID%>"  onClick="parent.document.getElementById('UserUI').value=value"> <%=name%><br>
  			
  		<%	
      }
  	}
  			%>
	</form>	</td></tr>	
	</table>
	</body>
</html>

