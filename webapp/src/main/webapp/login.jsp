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

<%@page import="org.efaps.admin.dbproperty.DBProperties"%>


  <html>
    <head>
      <title>eFaps</title>
    </head>
    <%-- /**  this javascript code should be on all login jsp's **/ --%>
    <script type="text/javascript">
      function test4top() {
        <%-- /**  test if previous login doesn't work **/ --%>
        if (top.wrongLogin)  {
          alert("<%=DBProperties.getProperty("Login.Wrong.Label",request.getLocale().getLanguage())%>");
        }
        <%-- /**  do we have top? no => reload to top **/ --%>
        if(top!=self)  {
          top.location=self.location;
        }
      }
    </script>
    <%-- /**  call the javascript function testing wrong login or not on top **/ --%>
    <body onload="test4top()">
      <form method="post" action="<%=request.getContextPath()%>/login">
        <center>
          <table border="0">
            <tr>
              <td><%=DBProperties.getProperty("Login.Name.Label",request.getLocale().getLanguage())%></td>
              <td><input type="text" name="name" value="" size="15"/></td>
            </tr>
            <tr>
              <td><%=DBProperties.getProperty("Login.Password.Label",request.getLocale().getLanguage())%></td>
              <td><input type="password" name="password" value="" size="15"/></td>
            </tr>
            <tr>
              <td align="right" colspan="2"><input type="submit" value="<%=DBProperties.getProperty("Login.Button.Label",request.getLocale().getLanguage())%>"/></td>
          	</tr>
          </table>
        </center>
      </form>
    </body>
  </html>

