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

<%@taglib prefix="c"    uri="http://java.sun.com/jstl/core"%>
<%@taglib prefix="fmt"  uri="http://java.sun.com/jstl/fmt" %>

<%-- /** set to request locale **/ --%>
<c:set var="locale"><%=request.getLocale()%></c:set>
<fmt:setLocale value="${locale}"/>
<c:remove var="locale"/>

<fmt:bundle basename="StringResource">

  <html>
    <head>
      <title>eFaps</title>
    </head>
    <%-- /**  this javascript code should be on all login jsp's **/ --%>
    <script type="text/javascript">
      function test4top() {
        <%-- /**  test if previous login doesn't work **/ --%>
        if (top.wrongLogin)  {
          alert("<fmt:message key="Login.Wrong.Label"/>");
        }
        <%-- /**  do we have top? no => reload to top **/ --%>
        if(top!=self)  {
          top.location=self.location;
        }
      }
    </script>
    <%-- /**  call the javascript function testing wrong login or not on top **/ --%>
    <body onload="test4top()">
      <form method="post" action="<%=request.getContextPath()%>/servlet/login">
        <center>
          <table border="0">
            <tr>
              <td><fmt:message key="Login.Name.Label"/></td>
              <td><input type="text" name="name" value="" size="15"/></td>
            </tr>
            <tr>
              <td><fmt:message key="Login.Password.Label"/></td>
              <td><input type="password" name="password" value="" size="15"/></td>
            </tr>
            <tr>
              <td align="right" colspan="2"><input type="submit" value="<fmt:message key="Login.Button.Label"/>"/></td>
          </table>
        </center>
      </form>
    </body>
  </html>

</fmt:bundle>
