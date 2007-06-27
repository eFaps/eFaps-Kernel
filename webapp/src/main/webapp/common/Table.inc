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

<%@page import="org.efaps.beans.FieldValue"%>
<%@page import="org.efaps.beans.TableBean"%>

<%@taglib prefix="c"    uri="http://java.sun.com/jstl/core"%>
<%@taglib prefix="h"    uri="http://java.sun.com/jsf/html"%>
<%@taglib prefix="f"    uri="http://java.sun.com/jsf/core"%>
<%@taglib prefix="t"    uri="http://myfaces.apache.org/tomahawk"%>

<%
  String cacheKey  = request.getParameter("cacheKey");

  if (cacheKey==null)  {
    cacheKey = Long.toString(System.currentTimeMillis());
System.out.println("create new cacheKey = "+cacheKey);
  }

  TableBean uiObject = (TableBean) request.getAttribute("uiObject");
  if (uiObject==null)  {
    uiObject = (TableBean) cache.getTableBean(cacheKey, request.getParameter("command"));
    request.setAttribute("uiObject", uiObject);
  }
%>

<c:choose>
  <c:when test="${param.mode eq 'edit' or uiObject.editMode}">
    <c:set var="bodyClass" value="eFapsFrameEdit"/>
  </c:when>
  <c:otherwise>
    <c:set var="bodyClass" value="eFapsFrameView"/>
  </c:otherwise>
</c:choose>
<html>

<%-- /** normal table **/ --%>
    
  <head>

    <link rel="stylesheet" type="text/css" href="../styles/eFapsDefault.css"/>
    
    <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>

<script type="text/javascript">

    function eFapsTableProcessEnd() {
      eFapsPositionTable();
      window.onresize = function() { eFapsPositionTable();}
    }

    /**
     */
    function eFapsPositionTable() {

var div = document.getElementById("eFapsFrameForm");
//alert("div="+div);
var menu = document.getElementById("eFapsFrameToolbarMenu");
var headerTable = document.getElementById("eFapsTableHeader");
//alert("menu="+menu);


var newHeight = eFapsCommonGetWindowHeight()
                      - menu.offsetTop
                      - menu.offsetHeight
                      - headerTable.offsetHeight;
//alert("newHeight="+newHeight);

<%-- /** if create / edit / search mode, a button line is shown */ --%>
<c:if test="${uiObject.createMode or uiObject.editMode or uiObject.searchMode}">
  newHeight = newHeight - 40;
</c:if>

if (newHeight > 0)  {
  div.style.maxHeight = newHeight + 'px';
  div.style.minHeight = newHeight + 'px';
} else  {
  div.style.maxHeight = newHeight + '0px';
  div.style.minHeight = newHeight + 'px';
}

      var bodyTable = document.getElementById("eFapsTableBody");

//alert(""+div.offsetLeft);
//alert(""+div.scrollLeft);
/*
      if (isIE)  {
        headerTable.style.top = document.body.scrollTop + "px";
        headerTable.style.left = bodyTable.offsetLeft + "px";
      } else  {
        headerTable.style.position = "fixed";
        headerTable.style.left = (bodyTable.offsetLeft - document.body.scrollLeft)  + "px";
      }
    
      headerTable.style.width = bodyTable.offsetWidth + "px";
*/    
      var header = headerTable.rows[0];

//headerTable.style.marginLeft= "10px";
//alert("" + (- div.scrollLeft));
var headerDiv = document.getElementById("eFapsFrameTableHeader");
headerDiv.style.left = - div.scrollLeft;

      var row = bodyTable.rows[0];
      var width = bodyTable.offsetWidth + "px";
      headerTable.style.width = width;
      headerTable.style.minWidth = width;
      headerTable.style.maxWidth = width;
      if (row)  {
        for (var i=0; i<row.cells.length; i++)  {
          if (row.cells[i].offsetWidth)  {
            var width = (row.cells[i].offsetWidth-2) + "px";
            header.cells[i].style.width = width;
            header.cells[i].style.maxWidth = width;
            header.cells[i].style.minWidth = width;
          }
        }
      }
    }
</script>

  </head>  
  
  <body onLoad="eFapsTableProcessEnd()" onUnLoad="eFapsCleanUpSession('<%=cacheKey%>')" class="<c:out value="${bodyClass}"/>">


    <%-- /** filter bar **/ --%>
<%--
    <div class="eFapsFrameFilter">
      <form>
        <select name="eFapsFrameFilter" size="1" onChange="eFapsGetFrameFilter().execute(this.options[this.selectedIndex].value);" style="display:none">
        </select>
      </form>
    </div>
--%>
          <%-- /** menu bar **/ --%>

<f:view>

  <h:panelGrid id="eFapsFrameTitle">
    <h:outputText value="#{titleBean.title}"/>
  </h:panelGrid>

  <h:form id="eFapsFrameToolbarMenu">
    <h:panelGrid>
      <t:jscookMenu layout="hbr" theme="ThemeOffice" styleLocation="../styles">
        <t:navigationMenuItems value="#{menuTableToolbar.JSFMenu}"/>
      </t:jscookMenu>
    </h:panelGrid>
  </h:form>

  

<c:choose>

  <%-- /** initialise the user interface object **/ --%>
  <c:when test="${uiObject.initialised eq false}">
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




    <form id="eFapsSubmitForm" name="eFapsSubmitForm" method="post" target="" >
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
<div id="eFapsFrameTableHeader" style="position:relative;">

    <c:if test="${empty param.mode or param.mode ne 'print'}">
      <table class="eFapsTable" id="eFapsTableHeader">
        <tr>
          <%-- /** show select checkbox only if not mode print and show check boxes is defined **/ --%>
          <c:if test="${(empty param.mode or param.mode ne 'print') and uiObject.showCheckBoxes}">
            <th class="eFapsTableHeader" width="1%">
              <input type="checkbox" onClick="eFapsTableSelectDeselectAll(document,'selectedRow',this.checked)"/>
            </th>
          </c:if>

          <%-- /** show table header labels **/ --%>
          <c:forEach items="${uiObject.fieldDefs}" var="one">
            <%-- /** show only fields if they are not hidden! **/ --%>
            <c:if test="${one.field.hidden eq false}">
              <th class="eFapsTableHeader">
                <%-- /** make href for the sort **/ --%>
                <c:choose>
                  <%-- /** sort dir is down if key is equal current field and direction is up **/ --%>
                  <c:when test="${uiObject.sortKey eq one.field.name and uiObject.sortDirection ne '-'}">
                    <a href="javascript:eFapsSortTable('<c:out value="${param.cacheKey}"/>','<c:out value="${one.field.name}"/>','-')">
                  </c:when>
                  <%-- /** otherwise sort dir is down **/ --%>
                  <c:otherwise>
                    <a href="javascript:eFapsSortTable('<c:out value="${param.cacheKey}"/>','<c:out value="${one.field.name}"/>','')">
                  </c:otherwise>
                </c:choose>
                <%-- /** show label **/ --%>
                <c:out value="${one.label}"/>
                <%-- /** show the sort image **/ --%>
                <c:choose>
                  <%-- /** show sort up image **/ --%>
                  <c:when test="${uiObject.sortKey eq one.field.name and uiObject.sortDirection eq '-'}">
                    <img src="../images/eFapsTableButtonSortDown.gif"/>
                  </c:when>
                  <%-- /** show sort down image **/ --%>
                  <c:when test="${uiObject.sortKey eq one.field.name and uiObject.sortDirection ne '-'}">
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
</div>
    
    
<div id="eFapsFrameForm" style="overflow:auto;max-width:100%;" onScroll="eFapsPositionTable()">

    <table class="eFapsTable" id="eFapsTableBody">


<%-- /*******************************************************************/ --%>
<%-- /** Table Values                                                  **/ --%>
<%-- /*******************************************************************/ --%>


        <c:set var="odd" value="true"/>
<%
// store print mode
String mode = request.getParameter("mode");
boolean printMode = (mode!=null && mode.equals("print") ? true : false);


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

  for (FieldValue col : row.getValues())  {

    if (!col.getFieldDef().getField().isHidden())  {
      out.print("<td>");

      // show href
      if (col.getFieldDef().getField().getReference()!=null)  {
        String href = col.getFieldDef().getField().getReference();
        String target = "Content";
        href += "&oid=" + col.getInstance().getOid();
        if (col.getFieldDef().getField().isTargetPopup())  {
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
      if (col.getFieldDef().getField().getIcon()!=null)  {
        out.print("<img src=\"");
        out.print(col.getFieldDef().getField().getIcon());
        out.print("\"/>");
      }

      // show type icon?
      if (col.getFieldDef().getField().isShowTypeIcon())  {
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
        if (uiObject.isCreateMode() && col.getFieldDef().getField().isEditable())  {
          out.print(col.getCreateHtml());
        } else if (uiObject.isEditMode() && col.getFieldDef().getField().isEditable())  {
          out.print(col.getEditHtml());
        } else  {
          out.print(col.getViewHtml());
        }
      }

      // end href
      if (col.getFieldDef().getField().getReference()!=null)  {
        out.print("</a>");
      }

      out.print("</td>");
    }
  }

  out.print("</tr>");
}


%>
        <%-- Show header again to get correct width --%>
        <tr>
          <%-- /** show select checkbox only if not mode print and show check boxes is defined **/ --%>
          <c:if test="${(empty param.mode or param.mode ne 'print') and uiObject.showCheckBoxes}">
            <th class="eFapsTableHeader" width="1%">
            </th>
          </c:if>

          <%-- /** show table header labels **/ --%>
          <c:forEach items="${uiObject.fieldDefs}" var="one">
            <%-- /** show only fields if they are not hidden! **/ --%>
            <c:if test="${one.field.hidden eq false}">
              <th class="eFapsTableHeader">
                <%-- /** make href for the sort **/ --%>
                <c:choose>
                  <%-- /** sort dir is down if key is equal current field and direction is up **/ --%>
                  <c:when test="${uiObject.sortKey eq one.field.name and uiObject.sortDirection ne '-'}">
                    <a href="javascript:eFapsSortTable('<c:out value="${param.cacheKey}"/>','<c:out value="${one.field.name}"/>','-')">
                  </c:when>
                  <%-- /** otherwise sort dir is down **/ --%>
                  <c:otherwise>
                    <a href="javascript:eFapsSortTable('<c:out value="${param.cacheKey}"/>','<c:out value="${one.field.name}"/>','')">
                  </c:otherwise>
                </c:choose>
                <%-- /** show label **/ --%>
                <c:out value="${one.label}"/>
                <%-- /** show the sort image **/ --%>
                <c:choose>
                  <%-- /** show sort up image **/ --%>
                  <c:when test="${uiObject.sortKey eq one.field.name and uiObject.sortDirection eq '-'}">
                    <img src="../images/eFapsTableButtonSortDown.gif"/>
                  </c:when>
                  <%-- /** show sort down image **/ --%>
                  <c:when test="${uiObject.sortKey eq one.field.name and uiObject.sortDirection ne '-'}">
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

    </form>
</div>
  
</f:view>
<%
System.out.println("...............................END:"+new java.util.Date());
%>

    <%-- /** hidden frame **/ --%>
    <iframe name="eFapsFrameHidden" id="eFapsFrameHidden" class="eFapsFrameHidden" src="about:blank">
    </iframe>

    <%-- /** footer frame **/ --%>
<%--
    <div class="eFapsFrameFooter">
    </div>
--%>
  </body>
    

</html>