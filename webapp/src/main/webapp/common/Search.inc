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

<%@taglib prefix="c"    uri="http://java.sun.com/jstl/core"%>
<%@taglib prefix="h"    uri="http://java.sun.com/jsf/html"%>
<%@taglib prefix="f"    uri="http://java.sun.com/jsf/core"%>
<%@taglib prefix="t"    uri="http://myfaces.apache.org/tomahawk"%>

    <%-- /** normal form **/ --%>
      <html>
        <head>

<script type="text/javascript">

      function eFapsFormProcessEnd() {
        <%-- /** footer action menu:
          - in mode create show button "create"
          - in mode edit show button "update"
          - in mode search show button "show"
          - show button cancel only if target is popup **/ --%>
<%--
        <c:if test="${uiObject.createMode or uiObject.editMode or uiObject.searchMode}">
          var footerAction = eFapsGetFrameFooterAction();
          footerAction.clean();
          footerAction.setForm(document.forms[1]);
          footerAction.add(
                <c:choose>
                  <c:when test="${uiObject.createMode}">"Standard.Button.Create",</c:when>
                  <c:when test="${uiObject.editMode}">"Standard.Button.Edit",</c:when>
                  <c:when test="${uiObject.searchMode}">"Standard.Button.Search",</c:when>
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
                "Standard.Button.Cancel",
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
--%>
        eFapsPositionForm();
        window.onresize = function() { eFapsPositionForm(); }


        eFapsProcessEnd();
      }


  function eFapsPositionForm() {
    var div = document.getElementById("eFapsForm");
    var menu = document.getElementById("eFapsFrameToolbarMenu");
    var newHeight = eFapsCommonGetWindowHeight()
                          - menu.offsetTop
                          - menu.offsetHeight;

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
	}

/**
 * The current form is submitted to the given href.
 *
 * @see #execute
 */
function eFapsSubmit()  {
  var div = document.getElementById("eFapsForm");
  var form = div.getElementsByTagName("form")[0];
  
//  eFapsProcessStart();
  form.submit();


//  if (!this.submitLock)
//  {
/*    var errorMsg = checkFormForRequirements(this.getForm());
    if (errorMsg == null || errorMsg.length == 0)
    {
      this.submitLock = true;
      eFapsProcessStart();
      this.getForm().action=this.href;
      if (this.getForm().command)  {
        this.getForm().command.value=this.name;
      }
      this.getForm().submit();
    }
    else
    {
      alert(errorMsg);
    }
*/

//  }
//  else
//  {
//	  alert("Please click only once on '"+this.textHtmlElement.nodeValue+"'");
//  }
}


</script>


        </head>
        <link rel="stylesheet" type="text/css" href="../styles/eFapsDefault.css"/>
        <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>

        <script type="text/javascript">
          var eFapsTreeImagePath = '../images/';
        </script>
        <script type="text/javascript" src="../javascripts/eFapsTree.js"></script>

        <body onLoad="eFapsFormProcessEnd()" class="eFapsFrameEdit">



          <%-- /** menu bar **/ --%>

<f:view>

  <h:panelGrid id="eFapsFrameTitle">
    <h:outputText value="#{titleBean.title}"/>
  </h:panelGrid>

  <%@include file="ToolBarMenu.inc"%>

          <div class="eFapsFrameFooter">

  <h:form>
    <h:commandLink onclick="eFapsSubmit();return false;" title="Create">
      <h:graphicImage value="../images/eFapsButtonDone.gif"/>
      <h:outputText value="Search"/>
    </h:commandLink>
    <h:commandLink onclick="eFapsCommonCloseWindow();return false;" title="Create">
      <h:graphicImage value="../images/eFapsButtonCancel.gif"/>
      <h:outputText value="Close"/>
    </h:commandLink>
  </h:form>

          </div>

</f:view>
          <iframe name="eFapsFrameHidden" id="eFapsFrameHidden" class="eFapsFrameHidden" src="about:blank">
          </iframe>
          
        </body>
      </html>


<%@include file = "../common/StdBottom.inc"%>
