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

<%@include file = "../common/StdTop.inc"%>

<c:set var="locale"><%=request.getLocale()%></c:set>
<fmt:setLocale value="${locale}"/>
<c:remove var="locale"/>
<fmt:bundle basename="StringResource">
  <tr>
    <td class="eFapsLogo">
      <div class="eFapsLogoWelcome">
        <fmt:message key="LogoRowInclude.Welcome.Label"/>
        <%=context.getPerson().getFirstName()%>
        <%=context.getPerson().getLastName()%>
      </div>
      <div class="eFapsLogoVersion">
        <fmt:message key="LogoRowInclude.Version.Label"/>
      </div>
    </td>
  </tr>
</fmt:bundle>

<%@include file = "../common/StdBottom.inc"%>
