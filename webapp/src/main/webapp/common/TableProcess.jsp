<%--
 
  Copyright 2006 The eFaps Team
 
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

<%@page errorPage="Exception.jsp"%>
<%@page import="java.sql.SQLException"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.StringTokenizer"%>

<%@page import="org.efaps.admin.ui.Command"%>
<%@page import="org.efaps.admin.ui.CommandAbstract"%>
<%@page import="org.efaps.admin.ui.Menu"%>
<%@page import="org.efaps.db.Instance"%>

<%@include file = "../common/StdTop.inc"%>
<%

  String tableCommandName = request.getParameter("tableCommand");
  String actionCommandName = request.getParameter("command");
  String[] oids = (String[])request.getParameterValues("selectedRow");

  CommandAbstract command = Command.get(actionCommandName);
  if (command==null)  {
    command = Menu.get(actionCommandName);
  }

System.out.println("*************************************************");
System.out.println("tableCommandName="+tableCommandName);
System.out.println("actionCommandName="+actionCommandName);

  for (int i=0; i<oids.length; i++)  {
    String oid = oids[i];
    if (command.getDeleteIndex()>0)  {
      StringTokenizer tokens = new StringTokenizer(oid, "|");
      int count = 0;
      while (tokens.hasMoreTokens() && count<command.getDeleteIndex())  {
        oid = tokens.nextToken();
        count++;
      }
    }
    org.efaps.db.Delete del = new org.efaps.db.Delete(oid);
    del.execute();
  }
System.out.println("*************************************************");

%><%@include file = "../common/StdBottom.inc"%>
<html>
  <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>
  <body>
    <script language="Javascript">
      parent.eFapsCommonRefresh();
    </script>
  </body>
</html>
