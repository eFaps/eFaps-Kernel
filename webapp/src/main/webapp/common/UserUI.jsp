
<%@page import="org.efaps.db.SearchQuery"%>
<%@include file = "../common/StdTop.inc"%>
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
		  if (getParameter("Name")!=null){

  	
  	
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

