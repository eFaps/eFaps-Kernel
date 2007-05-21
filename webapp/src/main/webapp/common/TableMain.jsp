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
<%@taglib prefix="c"    uri="http://java.sun.com/jstl/core"%>
<%@taglib prefix="fmt"  uri="http://java.sun.com/jstl/fmt" %>
<%@taglib prefix="str"  uri="http://jakarta.apache.org/taglibs/string"%>
<%@taglib prefix="un"   uri="http://jakarta.apache.org/taglibs/unstandard-1.0"%>

<%-- /** set to request locale **/ --%>
<c:set var="locale"><%=request.getLocale()%></c:set>
<fmt:setLocale value="${locale}"/>
<c:remove var="locale"/>

<jsp:useBean id="cache" class="org.efaps.beans.CacheSessionBean" scope="session"/>

<fmt:bundle basename="StringResource">

  <%
    String cacheKey =   request.getParameter("cacheKey");

    org.efaps.beans.TableBeanInterface uiObject = (org.efaps.beans.TableBeanInterface)request.getAttribute("uiObject");
    if (uiObject==null)  {
      uiObject = (org.efaps.beans.TableBeanInterface)cache.getTableBean(cacheKey, request.getParameter("command"));
      request.setAttribute("uiObject", uiObject);
    }
    uiObject.setResponse(response);
  %>


<c:choose>

  <%-- /** initialise the user interface object **/ --%>
  <c:when test="${uiObject.initialised eq false}">
    <%-- /** set the title of the ui object **/ --%>
    <c:set var="title"><str:replace replace="'" with="\\'"><fmt:message><c:out value="${param.command}"/>.Title</fmt:message></str:replace></c:set>
    <jsp:setProperty name="uiObject" property="title" value="<%=pageContext.getAttribute("title")%>"/>
    <c:remove var="title"/>
    <%-- /** set other parameters **/ --%>
    <jsp:setProperty name="uiObject" property="commandName" param="command"/>
    <jsp:setProperty name="uiObject" property="oid" param="oid"/>
    <jsp:setProperty name="uiObject" property="nodeId" param="nodeId"/>
    <%
System.out.println(".............................START:"+new java.util.Date());
      uiObject.execute();
System.out.println("............................MIDDLE:"+new java.util.Date());
    %>
  </c:when>

  <%-- /** **/ --%>
  <c:when test="${uiObject.selectedFilter ne param.filter and param.filter ne null and param.filter ne '' and param.filter ne '0'}">
    <jsp:setProperty name="uiObject" property="selectedFilter" param="filter"/>
    <%
System.out.println(".............................START:"+new java.util.Date());
      uiObject.execute();
System.out.println("............................MIDDLE:"+new java.util.Date());
    %>
  </c:when>

</c:choose>

<%-- /** set the sort key and sort direction **/ --%>
<c:if test="${param.sortKey ne null}">
  <jsp:setProperty name="uiObject" property="sortKey" param="sortKey"/>
  <jsp:setProperty name="uiObject" property="sortDirection" value=""/>
  <jsp:setProperty name="uiObject" property="sortDirection" param="sortDir"/>
</c:if>

<%-- /** make the sort of the key **/ --%>
<un:invoke target="${uiObject}" var="bck" method="sort"/>

<%-- /** sets the target of the form process **/ --%>
<c:choose>
  <c:when test="${param.targetProcess ne null}">
    <c:set var="targetProcess" value="${param.targetProcess}"/>
  </c:when>
  <c:otherwise>
    <c:set var="targetProcess" value=""/>
  </c:otherwise>
</c:choose>

<%-- /** sets the dom context where the title, the header and footer menu must be set **/ --%>
<c:choose>
  <c:when test="${param.domContext ne null}">
    <c:set var="domContext" value="${param.domContext}"/>
  </c:when>
  <c:otherwise>
    <c:set var="domContext" value="parent."/>
  </c:otherwise>
</c:choose>

<%-- /** sets the commit url for the delete actions **/ --%>
<c:choose>
  <c:when test="${param.commitUrl ne null}">
    <c:set var="commitUrl" value="${param.commitUrl}"/>
  </c:when>
  <c:otherwise>
    <c:set var="commitUrl" value="../common/TableProcess.jsp"/>
  </c:otherwise>
</c:choose>

<c:if test="${param.mode ne null and param.mode eq 'print'}">
  <script language="Javascript">
    function eFapsProcessEndPrint() {

      <%-- /** title **/ --%>
      eFapsSetTitle('<c:out value="${uiObject.title}" escapeXml="false"/>');

      <%-- /** filter **/ --%>
      var filter = eFapsGetFrameFilter();
      if (filter)  {
        filter.clean();
        filter.setForm(document.forms[1]);
        <c:forEach items="${uiObject.command.targetTableFilters}" var="oneFilter" varStatus="oneFilterIndex">
          filter.add("<fmt:message key="${uiObject.command.name}.Filter.${oneFilterIndex.count}"/>", "<c:out value="${oneFilterIndex.count}"/>"
          <c:if test="${oneFilterIndex.count eq uiObject.selectedFilter}">
            ,true
          </c:if>
          );
        </c:forEach>
      }

      eFapsProcessEnd();
    }
  </script>
</c:if>

<c:if test="${empty param.mode or param.mode ne 'print'}">
<html>
  <head>
    <jsp:include page="StdHeaderMetaInfo.inc"/>
  
    <link rel="StyleSheet" href="../styles/eFapsDefault.css" type="text/css"/>
    <script type="text/javascript" src="../javascripts/eFapsToolbar.js"></script>
    <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>
  

  <%-- /* test, if a special bottom heigth is set. if yes set it to the new height */ --%>
  <c:if test="${uiObject.command.targetBottomHeight gt 0}">
    <script language="Javascript">
      eFapsPositionMainBottomHeight = <c:out value="${uiObject.command.targetBottomHeight}"/>;
    </script>
  </c:if>

  <script language="Javascript">
    function eFapsProcessEnd() {

      <%-- /** title **/ --%>
      <c:out value="${domContext}"/>eFapsSetTitle('<c:out value="${uiObject.title}" escapeXml="false"/>');

      <%-- /** menu **/ --%>
      <%@include file="StdMenu.inc"%>
      with (topMenu)  {
        addSeparator();

        <%-- /* export button */ --%>
        with (addCommand('', ''))  {
          setJavaScript("eFapsCommonPrint('../common/TableExport.jsp?command=<%=request.getParameter("command")%>&cacheKey=<%=cacheKey%>&oid=<%=request.getParameter("oid")%>&mode=print')");
          setIcon("../images/eFapsMiniToolbarExport.gif");
        }

        <%-- /* print button */ --%>
        with (addCommand('', ''))  {
          setJavaScript("eFapsCommonPrint('../common/Table.jsp?command=<%=request.getParameter("command")%>&oid=<%=request.getParameter("oid")%>&mode=print')");
          setIcon("../images/eFapsMiniToolbarPrint.gif");
        }

        addSeparator();
      }
      top.frameMenu.doMenu(<c:out value="${domContext}"/>document);

      <%-- /** footer action menu:
        - show edit button only if mode is connect
        - show connect button only if mode is connect
        - show button cancel only if target is popup **/ --%>
      var footerAction = <c:out value="${domContext}"/>eFapsGetFrameFooterAction();
      footerAction.clean();
      footerAction.setForm(document.forms[0]);
      <c:if test="${uiObject.popup}">
        <c:choose>
          <c:when test="${uiObject.editMode}">
            footerAction.add(
                  "<fmt:message key="Standard.Button.Edit"/>",
                  "../common/TableEditProcess.jsp",
                  true,
                  false,
                  false,
                  '',
                  '',
                  '',
                  '',
                  '../images/eFapsButtonDone.gif');
          </c:when>
          <c:when test="${uiObject.connectMode}">
            footerAction.add(
                  "<fmt:message key="Standard.Button.Connect"/>",
                  "../common/SearchConnect.jsp",
                  true,
                  false,
                  false,
                  '',
                  '',
                  '',
                  '',
                  '../images/eFapsButtonDone.gif');
          </c:when>
        </c:choose>
        footerAction.add(
              "<fmt:message key="Standard.Button.Cancel"/>",
              "javascript:eFapsCommonCloseWindow()",
              false,
              false,
              false,
              '',
              '',
              '',
              '',
              '../images/eFapsButtonCancel.gif');
      </c:if>

      <%-- /** filter **/ --%>
      var filter = <c:out value="${domContext}"/>eFapsGetFrameFilter();
      if (filter)  {
        filter.clean();
        filter.setForm(document.forms[0]);
        <c:forEach items="${uiObject.command.targetTableFilters}" var="oneFilter" varStatus="oneFilterIndex">
          filter.add("<fmt:message key="${uiObject.command.name}.Filter.${oneFilterIndex.count}"/>", "<c:out value="${oneFilterIndex.count}"/>"
          <c:if test="${oneFilterIndex.count eq uiObject.selectedFilter}">
            ,true
          </c:if>
          );
        </c:forEach>
      }

      eFapsPositionTable();

      <c:out value="${domContext}"/>eFapsProcessEnd();
	
    }

/**
 */
	function eFapsPositionTable() {
	  var newHeight;
	  var newWidth;
	
	  var headerTable = document.getElementById("eFapsTableHeader");
	  var bodyTable = document.getElementById("eFapsTableBody");
	
	  if (isIE)  {
	    headerTable.style.top = document.body.scrollTop + "px";
	    headerTable.style.left = bodyTable.offsetLeft + "px";
	  } else  {
	    headerTable.style.position = "fixed";
	    headerTable.style.left = (bodyTable.offsetLeft - document.body.scrollLeft)  + "px";
	  }
	
	  headerTable.style.width = bodyTable.offsetWidth + "px";
	
	  var header = headerTable.rows[0];
	  var row = bodyTable.rows[1];
	
	  for (var i=0; i<row.cells.length; i++)  {
	    if (row.cells[i].offsetWidth)  {
	      header.cells[i].style.width = (row.cells[i].offsetWidth-2) + "px";
	    }
	  }
	
	  window.setTimeout("eFapsPositionTable()",1);
	}



    </script>
  </head>
  <body onLoad="eFapsProcessEnd()">
</c:if>

    <form method="post" target="<c:out value="${targetProcess}"/>">
      <input type="hidden" name="command" value=""/>
      <input type="hidden" name="tableCommand" value="<c:out value="${uiObject.command.name}"/>"/>
      <input type="hidden" name="eFapsTableFilter" value=""/>
      <input type="hidden" name="eFapsCacheKey" value="<c:out value="${param.cacheKey}"/>"/>

      <%-- /** show the hidden values for this user interface **/ --%>
      <c:forEach items="${uiObject.hiddenValues}" var="hiddenValue">
        <input type="hidden" name="<c:out value="${hiddenValue.name}"/>" value="<c:out value="${hiddenValue.value}"/>"/>
      </c:forEach>


    <%-- /*******************************************************************/ --%>
    <%-- /** Table Header                                                  **/ --%>
    <%-- /*******************************************************************/ --%>

    <c:if test="${empty param.mode or param.mode ne 'print'}">
      <table class="eFapsTable" id="eFapsTableHeader" style="table-layout:fixed;position:absolute;">
        <tr>
          <%-- /** show select checkbox only if not mode print and show check boxes is defined **/ --%>
          <c:if test="${(empty param.mode or param.mode ne 'print') and uiObject.showCheckBoxes}">
            <th class="eFapsTableHeader" width="1%">
              <input type="checkbox" onClick="eFapsTableSelectDeselectAll(document,'selectedRow',this.checked)"/>
            </th>
          </c:if>

          <%-- /** show table header labels **/ --%>
          <c:forEach items="${uiObject.table.fields}" var="one">
            <%-- /** show only fields if they are not hidden! **/ --%>
            <c:if test="${one.hidden eq false}">
              <th class="eFapsTableHeader">
                <%-- /** make href for the sort **/ --%>
                <c:choose>
                  <%-- /** sort dir is down if key is equal current field and direction is up **/ --%>
                  <c:when test="${uiObject.sortKey eq one.name and uiObject.sortDirection ne '-'}">
                    <a href="javascript:eFapsSortTable('<c:out value="${param.cacheKey}"/>','<c:out value="${one.name}"/>','-')">
                  </c:when>
                  <%-- /** otherwise sort dir is down **/ --%>
                  <c:otherwise>
                    <a href="javascript:eFapsSortTable('<c:out value="${param.cacheKey}"/>','<c:out value="${one.name}"/>','')">
                  </c:otherwise>
                </c:choose>
                <%-- /** show label (if exists) **/--%>
                <c:if test="${one.label ne null}">
                  <fmt:message key="${one.label}" var="label"/>
                  <c:set var="keyWithQuestionMark" value="???${one.label}???"/>
                  <%-- /** test if label exists in the property file **/ --%>
                  <c:choose>
                    <%-- /** label does not exists in the property file, this means show the original label **/ --%>
                    <c:when test="${label eq keyWithQuestionMark}">
                      <c:out value="${one.label}"/>
                    </c:when>
                    <%-- /** label exists in the property file, this means show the internationalised label **/ --%>
                    <c:otherwise>
                      <c:out value="${label}"/>
                    </c:otherwise>
                  </c:choose>
                  <c:remove var="label"/>
                </c:if>
                <%-- /** show the sort image **/ --%>
                <c:choose>
                  <%-- /** show sort up image **/ --%>
                  <c:when test="${uiObject.sortKey eq one.name and uiObject.sortDirection eq '-'}">
                    <img src="../images/eFapsTableButtonSortDown.gif"/>
                  </c:when>
                  <%-- /** show sort down image **/ --%>
                  <c:when test="${uiObject.sortKey eq one.name and uiObject.sortDirection ne '-'}">
                    <img src="../images/eFapsTableButtonSortUp.gif"/>
                  </c:when>
                  <%-- /** otherwise show a dummy image **/ --%>
                  <c:otherwise>
                    <img src="../images/eFapsTableButtonSortDummy.gif"/>
                  </c:otherwise>
                </c:choose>
                </a>
              </th>
            </c:if>
          </c:forEach>
        </tr>
      </table>
    </c:if>

    <table class="eFapsTable" id="eFapsTableBody">

    <c:if test="${empty param.mode or param.mode ne 'print'}">
      <tr style="visibility:hidden">
    </c:if>
    <c:if test="${param.mode ne null and param.mode eq 'print'}">
      <tr>
    </c:if>

    <%-- /** show select checkbox only if not mode print and show check boxes is defined **/ --%>
    <c:if test="${(empty param.mode or param.mode ne 'print') and uiObject.showCheckBoxes}">
      <th class="eFapsTableHeader" width="1%">
        <input type="checkbox" onClick="eFapsTableSelectDeselectAll(document,'selectedRow',this.checked)"/>
      </th>
    </c:if>

    <%-- /** show table header labels **/ --%>
    <c:forEach items="${uiObject.table.fields}" var="one">
      <%-- /** show only fields if they are not hidden! **/ --%>
      <c:if test="${one.hidden eq false}">
        <th class="eFapsTableHeader">
           
           <%-- /** make href for the sort **/ --%>
           <c:choose>
             <%-- /** sort dir is down if key is equal current field and direction is up **/ --%>
             <c:when test="${uiObject.sortKey eq one.name and uiObject.sortDirection ne '-'}">
               <a href="javascript:eFapsSortTable('<c:out value="${param.cacheKey}"/>','<c:out value="${one.name}"/>','-')">
             </c:when>
             <%-- /** otherwise sort dir is down **/ --%>
             <c:otherwise>
               <a href="javascript:eFapsSortTable('<c:out value="${param.cacheKey}"/>','<c:out value="${one.name}"/>','')">
             </c:otherwise>
           </c:choose>
           
           <%-- /** show label (if exists) **/--%>
           <c:if test="${one.label ne null}">
             <fmt:message key="${one.label}" var="label"/>
               <c:set var="keyWithQuestionMark" value="???${one.label}???"/>
                 <%-- /** test if label exists in the property file **/ --%>
                 <c:choose>
                   <%-- /** label does not exists in the property file, this means show the original label **/ --%>
                   <c:when test="${label eq keyWithQuestionMark}">
                     <c:out value="${one.label}"/>
                   </c:when>
                   <%-- /** label exists in the property file, this means show the internationalised label **/ --%>
                   <c:otherwise>
                     <c:out value="${label}"/>
                   </c:otherwise>
                 </c:choose>
                 <c:remove var="label"/>
           </c:if>
                
           <%-- /** show the sort image **/ --%>
           <c:choose>
             <%-- /** show sort up image **/ --%>
             <c:when test="${uiObject.sortKey eq one.name and uiObject.sortDirection eq '-'}">
               <img src="../images/eFapsTableButtonSortDown.gif"/>
             </c:when>
             <%-- /** show sort down image **/ --%>
             <c:when test="${uiObject.sortKey eq one.name and uiObject.sortDirection ne '-'}">
                <img src="../images/eFapsTableButtonSortUp.gif"/>
             </c:when>
             <%-- /** otherwise show a dummy image **/ --%>
             <c:otherwise>
               <img src="../images/eFapsTableButtonSortDummy.gif"/>
             </c:otherwise>
           </c:choose>
                </a>
         </th>
       </c:if>
     </c:forEach>
   </tr>

<%-- /*******************************************************************/ --%>
<%-- /** Table Values                                                  **/ --%>
<%-- /*******************************************************************/ --%>


        <c:set var="odd" value="true"/>
<%
// store print mode
String mode = request.getParameter("mode");
boolean printMode = (mode!=null && mode.equals("print") ? true : false);


((org.efaps.beans.AbstractCollectionBean)uiObject).startProcessView();

// because of performance reason, the following is a pure java implementation
boolean odd = true;
for (java.util.Iterator rowIter = uiObject.getValues().iterator(); rowIter.hasNext(); odd=!odd)  {

  org.efaps.beans.TableBean.Row row = (org.efaps.beans.TableBean.Row)rowIter.next();

  // show the tr tag depending on the odd attribute
  if (odd)  {
    out.print("<tr class=\"eFapsTableRowOdd\">");
  } else  {
    out.print("<tr class=\"eFapsTableRowEven\">");
  }

  // show select checkbox only if not mode print and show check boxes ifdefined
  //if test="${(empty param.mode or param.mode ne 'print') and uiObject.showCheckBoxes}">
  if (!printMode && uiObject.isShowCheckBoxes())  {
    out.print("<td width=\"1%\"><input type=\"checkbox\" name=\"selectedRow\" value=\"");
    out.print(row.getOids());
    out.print("\"/></td>");
  }

  for (org.efaps.beans.TableBean.Value col : row.getValues())  {

    if (!col.getField().isHidden())  {
      out.print("<td>");

      // show href
      if (col.getField().getReference()!=null)  {
        String href = col.getField().getReference();
        String target = "Content";
        href += "&oid=" + col.getInstance().getOid();
        if (col.getField().isTargetPopup())  {
          target = "Popup";
        } else if (uiObject.getNodeId()!=null) {
          href += "&nodeId=" + uiObject.getNodeId();
        }
        out.print("<a href=\"javascript:eFapsCommonOpenUrl('");
        out.print(href);
        out.print("', '");
        out.print(target);
        out.print("')\">");
      }

      // image
      if (col.getField().getIcon()!=null)  {
        out.print("<img src=\"");
        out.print(col.getField().getIcon());
        out.print("\"/>");
      }

      // show type icon?
      if (col.getField().isShowTypeIcon())  {
        String imgUrl = col.getInstance().getType().getIcon();
        if (imgUrl!=null)  {
          out.print("<img src=\"");
          out.print(imgUrl);
          out.print("\"/>");
          out.print("&nbsp;");
        };
      }

      // show column value
      if (col.getValue()!=null)  {
        if (uiObject.isCreateMode() && col.getField().isEditable())  {
          out.print(col.getCreateHtml());
        } else if (uiObject.isEditMode() && col.getField().isEditable())  {
          out.print(col.getEditHtml());
        } else  {
          out.print(col.getViewHtml());
        }
      }

      // end href
      if (col.getField().getReference()!=null)  {
        out.print("</a>");
      }

      out.print("</td>");
    }
  }

  out.print("</tr>");
}

((org.efaps.beans.AbstractCollectionBean)uiObject).endProcessView();



%>




      </table>

    </form>
<c:if test="${empty param.mode or param.mode ne 'print'}">
  </body>
</html>
</c:if>

</fmt:bundle>
<%
System.out.println("...............................END:"+new java.util.Date());
%>