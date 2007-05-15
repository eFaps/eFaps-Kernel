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

<%@include file = "../common/StdTop.inc"%>

<%-- /** set to request locale **/ --%>
<c:set var="locale"><%=request.getLocale()%></c:set>
<fmt:setLocale value="${locale}"/>
<c:remove var="locale"/>

<jsp:useBean id="cache" class="org.efaps.beans.CacheSessionBean" scope="session"/>

<fmt:bundle basename="StringResource">

<%-- /** constructor for the form bean **/ --%>
<c:set var="method" value="execute"/>
<%@include file = "../common/FormBeanConstructor.inc"%>
<c:remove var="method"/>


<%-- /** sets the target of the form process **/ --%>
<c:choose>
  <c:when test="${param.targetProcess ne null}">
    <c:set var="targetProcess" value="${param.targetProcess}"/>
  </c:when>
  <c:otherwise>
    <c:set var="targetProcess" value="eFapsFrameHidden"/>
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

<c:if test="${param.mode ne null or param.mode eq 'print'}">
  <script language="Javascript">
    eFapsSetTitle('<c:out value="${uiObject.title}" escapeXml="false"/>');
  </script>
</c:if>

<c:if test="${empty param.mode or param.mode ne 'print'}">
  <html>
    <head>
    </head>
    <link rel="StyleSheet" href="../styles/eFapsCalendar.css" type="text/css"/>
    <link rel="StyleSheet" href="../styles/eFapsDefault.css" type="text/css"/>
    <script type="text/javascript">var eFapsCalenderPathImages = "../images/";</script>
    <script type="text/javascript" src="../javascripts/eFapsCalendar.js"></script>
    <script type="text/javascript" src="../javascripts/eFapsToolbar.js"></script>
    <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>

    <script language="Javascript">
      function eFapsProcessEnd() {
        <%-- /** title **/ --%>
<%--
        <c:choose>
          <c:when test="${uiObject.editMode}">
            <c:out value="${domContext}"/>eFapsSetTitle('<c:out value="${uiObject.ukTitle}" escapeXml="false"/>');
          </c:when>
          <c:otherwise>
--%>
            <c:out value="${domContext}"/>eFapsSetTitle('<c:out value="${uiObject.title}" escapeXml="false"/>');
<%--
          </c:otherwise>
        </c:choose>
--%>

        <%-- /** menu **/ --%>
        <%@include file="StdMenu.inc"%>
        with (topMenu)  {
          addSeparator();

          <%-- /* export button */ --%>
          with (addCommand('', ''))  {
            setJavaScript("eFapsCommonPrint('../common/Form.jsp?command=<%=request.getParameter("command")%>&oid=<%=request.getParameter("oid")%>&mode=print')");
            setIcon("../images/eFapsMiniToolbarExport.gif");
          }

          <%-- /* print button */ --%>
          with (addCommand('', ''))  {
            setJavaScript("eFapsCommonPrint('../common/Form.jsp?command=<%=request.getParameter("command")%>&oid=<%=request.getParameter("oid")%>&mode=print')");
            setIcon("../images/eFapsMiniToolbarPrint.gif");
          }

          addSeparator();
        }
        top.frameMenu.doMenu(<c:out value="${domContext}"/>document);

        <%-- /** footer action menu:
          - in mode create show button "create"
          - in mode edit show button "update"
          - in mode search show button "show"
          - show button cancel only if target is popup **/ --%>
        var footerAction = <c:out value="${domContext}"/>eFapsGetFrameFooterAction();
        footerAction.clean();
        footerAction.setForm(document.forms[0]);
        <c:if test="${uiObject.createMode or uiObject.editMode or uiObject.searchMode}">
          footerAction.add(
                <c:choose>
                  <c:when test="${uiObject.createMode}">"<fmt:message key="Standard.Button.Create"/>",</c:when>
                  <c:when test="${uiObject.editMode}">"<fmt:message key="Standard.Button.Edit"/>",</c:when>
                  <c:when test="${uiObject.searchMode}">"<fmt:message key="Standard.Button.Search"/>",</c:when>
                </c:choose>
                <c:choose>
                  <c:when test="${param.urlProcess ne null}">
                    "<c:out value="${param.urlProcess}"/>",
                  </c:when>
                  <c:otherwise>
                    "../common/FormProcess.jsp",
                  </c:otherwise>
                </c:choose>
                true,
                false,
                false,
                '',
                '',
                '',
                '',
                <c:choose>
                  <c:when test="${uiObject.searchMode}">'../images/eFapsButtonNext.gif'</c:when>
                  <c:otherwise>'../images/eFapsButtonDone.gif'</c:otherwise>
                </c:choose>
          );
        </c:if>
        <c:if test="${uiObject.popup}">
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

        <c:out value="${domContext}"/>eFapsProcessEnd();
      }
    </script>
    <body onLoad="eFapsProcessEnd()">
</c:if>


      <form method="post" enctype="multipart/form-data" target="<c:out value="${targetProcess}"/>">
        <input type="hidden" name="command" value=""/>

        <%-- /** show the hidden values for this user interface **/ --%>
        <c:forEach items="${uiObject.hiddenValues}" var="hiddenValue">
          <input type="hidden" name="<c:out value="${hiddenValue.name}"/>" value="<c:out value="${hiddenValue.value}"/>"/>
        </c:forEach>

        <table class="eFapsForm">
          <c:if test="${uiObject.values ne null}">
            <c:if test="${uiObject.values[0] ne null}">
              <input type="hidden" name="oid" value="<c:out value="${uiObject.values[0].oid}"/>"/>
            </c:if>

            <%-- /** group count and row group count is default equal 1,
                     because only one field should be shown per row **/ --%>
            <c:set var="groupCount" value="1"/>
            <c:set var="rowGroupCount" value="1"/>

<%
((org.efaps.beans.AbstractCollectionBean)uiObject).startProcessView();
%>

            <c:forEach items="${uiObject.values}" var="value" begin="1">

              <c:choose>

                <%-- /** if the field is a group count field, store the value in the row group count variable,
                         (only if not create node or field is creatable) **/ --%>
                <c:when test="${value.field.groupCount > 0 && (uiObject.createMode ne true or value.field.creatable)  && (uiObject.searchMode ne true or value.field.searchable)}">
                  <c:set var="rowGroupCount" value="${value.field.groupCount}"/>
                </c:when>

                <%-- /* only if:
                        - in createmode field is createable
                        - in searchmode field is searchable
                        - otherwise show field */ --%>
                <c:when test="${(uiObject.createMode ne true or value.field.creatable) and (uiObject.searchMode ne true or value.field.searchable)}">

                  <%-- /** start start row tag if first column **/ --%>
                  <c:if test="${groupCount == 1}">
                      <tr>
                  </c:if>

                  <%-- /** use required style class only if defined and not in edit or create mode **/ --%>
                  <c:choose>
                    <c:when test="${value.field.required and (uiObject.editMode or uiObject.createMode)}">
                      <td class="eFapsFormLabelRequired">
                    </c:when>
                    <c:otherwise>
                      <td class="eFapsFormLabel">
                    </c:otherwise>
                  </c:choose>

                  <%-- /** show the label of the field **/ --%>
                  <fmt:message><c:out value="${value.label}"/></fmt:message></td>

                  <%-- /** make column with correct span **/ --%>
                  <td class="eFapsFormInputField" colspan="<c:out value="${2 * (uiObject.maxGroupCount - rowGroupCount) + 1}"/>">

                    <%-- /** show href START **/ --%>
                    <c:if test="${value.field.reference ne null and !(empty value.instance.oid) and uiObject.createMode ne true and uiObject.searchMode ne true}">
                      <c:set var="targetUrl" value="${value.field.reference}&oid=${value.instance.oid}"/>

                      <c:choose>
                        <c:when test="${value.field.targetPopup}">
                          <c:set var="targetWindow" value="Popup"/>
                        </c:when>
                        <c:otherwise>
                          <c:set var="targetWindow" value="Content"/>
                          <c:if test="${uiObject.nodeId ne null}">
                            <c:set var="targetUrl" value="${targetUrl}&nodeId=${uiObject.nodeId}"/>
                          </c:if>
                        </c:otherwise>
                      </c:choose>

                      <a href="javascript:eFapsCommonOpenUrl('<c:out value="${targetUrl}" escapeXml="false"/>','<c:out value="${targetWindow}" escapeXml="false"/>')">

                      <c:remove var="targetUrl"/>
                      <c:remove var="targetWindow"/>
                    </c:if>


                    <%-- /** show type icon if needed **/ --%>
                    <c:if test="${value.field.showTypeIcon and !(empty value.instance.oid) and uiObject.createMode ne true and uiObject.searchMode ne true}">
                      <img src="<c:out value="${value.instance.type.icon}"/>"/>&nbsp;
                    </c:if>

                    <%-- /** show value **/ --%>
                    <c:choose>
                      <c:when test="${uiObject.createMode and value.field.creatable}">
                        <c:out value="${value.createHtml}" escapeXml="false"/>
                      </c:when>
                      <c:when test="${uiObject.editMode and value.field.editable}">
                        <c:out value="${value.editHtml}" escapeXml="false"/>
                      </c:when>
                      <c:when test="${uiObject.searchMode and value.field.searchable}">
                        <c:out value="${value.searchHtml}" escapeXml="false"/>
                      </c:when>
                      <c:otherwise>
                        <c:out value="${value.viewHtml}" escapeXml="false"/>
                      </c:otherwise>
                    </c:choose>

                    <%-- /** show href END **/ --%>
                    <c:if test="${value.field.reference ne null and !(empty value.instance.oid) and uiObject.createMode ne true and uiObject.searchMode ne true}">
                      </a>
                    </c:if>

                  </td>

                  <%-- /** show the end row tag only if last table column **/ --%>
                  <c:if test="${groupCount == rowGroupCount}">
                    </tr>
                  </c:if>

                  <c:choose>
                    <%-- /** if group count less than row group count, increase by 1;
                             meaning the row has more than one field! **/ --%>
                    <c:when test="${groupCount < rowGroupCount}">
                      <c:set var="groupCount" value="${groupCount + 1}"/>
                    </c:when>
                    <%-- /** if last column, reset group counters **/ --%>
                    <c:when test="${groupCount == rowGroupCount}">
                      <c:set var="groupCount" value="1"/>
                      <c:set var="rowGroupCount" value="1"/>
                    </c:when>
                  </c:choose>
                </c:when>
              </c:choose>
            </c:forEach>

<%
((org.efaps.beans.AbstractCollectionBean)uiObject).endProcessView();
%>

          </c:if>
        </table>
      </form>

<c:if test="${empty param.mode or param.mode ne 'print'}">
    </body>
  </html>
</c:if>

</fmt:bundle>

<%@include file = "../common/StdBottom.inc"%>
