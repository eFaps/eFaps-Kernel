<%@page isErrorPage="true"%>
<%@taglib prefix="c"    uri="http://java.sun.com/jstl/core"%>
<%@taglib prefix="fmt"  uri="http://java.sun.com/jstl/fmt" %>

<%@page import="java.text.MessageFormat"%>

<%@page import="org.efaps.util.EFapsException"%>

<%-- /** set to request locale **/ --%>
<c:set var="locale"><%=request.getLocale()%></c:set>
<fmt:setLocale value="${locale}"/>
<c:remove var="locale"/>

<%!
  public String decode(Object _object)  {
    if (_object!=null)  {
      String text = _object.toString();
      text = text.replaceAll("\n", "<br/>");
      text = text.replaceAll("\\\\","\\\\\\\\");
      return text;
    } else  {
      return "";
    }
  }
%>


<fmt:bundle basename="StringResource">


  <%
    String errorId = "";
    String errorMessage = exception.getMessage();
    String errorAdvanced = "";
    String errorAction = "";

    if (exception instanceof EFapsException)  {
      EFapsException eFapsException = (EFapsException)exception;
      %>
        <c:set var="errorKey"><%=eFapsException.getClassName().getName()%>.<%=eFapsException.getId()%></c:set>
        <c:set var="errorId"><fmt:message><c:out value="${errorKey}"/>.Id</fmt:message></c:set>
        <c:set var="errorMessage"><fmt:message><c:out value="${errorKey}"/>.Message</fmt:message></c:set>
        <c:set var="errorAction"><fmt:message><c:out value="${errorKey}"/>.Action</fmt:message></c:set>
      <%
      errorId       = (String)pageContext.getAttribute("errorId");
      errorMessage  = (String)pageContext.getAttribute("errorMessage");
      errorAction   = (String)pageContext.getAttribute("errorAction");
      %>
        <c:remove var="errorKey"/>
        <c:remove var="errorId"/>
        <c:remove var="errorMessage"/>
        <c:remove var="errorAction"/>
      <%

      if (eFapsException.getArgs()!=null)  {
        errorMessage = MessageFormat.format(errorMessage, eFapsException.getArgs());
//        for (int i=0, j=1; i<eFapsException.getArgs().length; i++, j++)  {
//          errorMessage = errorMessage.replaceAll("<%s"+j+">", eFapsException.getArgs()[i]);
//        }
      }

      if (eFapsException.getThrowable()!=null)  {
        StackTraceElement[] traceElements = eFapsException.getThrowable().getStackTrace();
        for (int i=0; i<traceElements.length; i++)  {
          errorAdvanced += traceElements[i].toString()+"\n";
        }
      }

    } else  {
      errorMessage = exception.getMessage();
      if (errorMessage==null)  {
        errorMessage = exception.toString();
      }
      StackTraceElement[] traceElements = exception.getStackTrace();
      for (int i=0; i<traceElements.length; i++)  {
        errorAdvanced += traceElements[i].toString()+"\n";
      }
    }

    %>
      <c:set var="textTitle"><fmt:message key="JSPPage.Exception.TextTitle"/></c:set>
      <c:set var="textId"><fmt:message key="JSPPage.Exception.TextId"/></c:set>
      <c:set var="textMessage"><fmt:message key="JSPPage.Exception.TextMessage"/></c:set>
      <c:set var="textAdvanced"><fmt:message key="JSPPage.Exception.TextAdvanced"/></c:set>
      <c:set var="textAction"><fmt:message key="JSPPage.Exception.TextAction"/></c:set>
    <%

    StringBuffer buf = new StringBuffer();
    buf.append("<html><title>").append(pageContext.getAttribute("textTitle")).append("</title>");
    buf.append("<link rel=\"StyleSheet\" href=\"../styles/eFapsDefault.css\" type=\"text/css\"/>");
    buf.append("<script language=\"javascript\">");
    buf.append("function advanced()  {");
    buf.append("var obj = document.getElementById(\"advanced\");");
    buf.append("if (obj.style.display==\"none\")  {");
    buf.append("obj.style.display = \"\";");
    buf.append("} else  {");
    buf.append("obj.style.display = \"none\";");
    buf.append("}}</script>");
    buf.append("<body>");
    buf.append("<table class=\"eFapsError\">");
    buf.append("<tr>");
    buf.append("<td class=\"eFapsErrorLabel\">").append(decode(pageContext.getAttribute("textId"))).append("</td>");
    buf.append("<td class=\"eFapsErrorText\">").append(decode(errorId)).append("</td>");
    buf.append("</tr>");
    buf.append("<tr>");
    buf.append("<td class=\"eFapsErrorLabel\">");
    if (errorAdvanced.length()>0)  {
        buf.append("<a class=\"eFapsErrorLabel\" accesskey=\"a\" href=\"javascript:advanced()\">");
    }
    buf.append(decode(pageContext.getAttribute("textMessage")));
    if (errorAdvanced.length()>0)  {
      buf.append("</a>");
    }
    buf.append("</td>");
    buf.append("<td class=\"eFapsErrorText\">").append(decode(errorMessage)).append("</td>");
    buf.append("</tr>");
    if (errorAdvanced.length()>0)  {
      buf.append("<tr id=\"advanced\" style=\"display:none\">");
      buf.append("<td class=\"eFapsErrorLabel\">").append(decode(pageContext.getAttribute("textAdvanced"))).append("</td>");
      buf.append("<td class=\"eFapsErrorText\">").append(decode(errorAdvanced)).append("</td>");
      buf.append("</tr>");
    }
    buf.append("<tr>");
    buf.append("<td class=\"eFapsErrorLabel\">").append(decode(pageContext.getAttribute("textAction"))).append("</td>");
    buf.append("<td class=\"eFapsErrorText\">").append(decode(errorAction)).append("</td>");
    buf.append("</tr>");
    buf.append("</table>");
    buf.append("<div style=\"text-align:right\"><a href=\"javascript:window.close()\">Close</a></div>");
    buf.append("</body></html>");

    String text = buf.toString();
    text = text.replaceAll("<", "%3c");
    text = text.replaceAll(">", "%3e");
    text = text.replaceAll("\'", "%27");
    text = text.replaceAll("\n", " ");
    text = text.replaceAll("\r", " ");

  %>
  <c:remove var="textTitle"/>
  <c:remove var="textMessage"/>
  <c:remove var="textAdvanced"/>
  <c:remove var="textAction"/>

  <html>
    <script type="text/javascript">

      function eFapsShowError()  {
        var winleft = parseInt((screen.width - 500) / 2);
        var wintop = parseInt((screen.height - 200) / 2);
        var myWin = window.open("", "",
            "dependent=no,"+
            "location=no,"+
            "menubar=no,"+
            "titlebar=no,"+
            "hotkeys=no,"+
            "status=no,"+
            "toolbar=no,"+
            "scrollbars=yes,"+
            "resizable=yes,"+
            "height=200,"+
            "width=500,"+
            "left="+winleft+","+
            "top="+wintop);
        myWin.document.write(unescape('<%=text%>'));
        myWin.focus();
        if (parent && parent.eFapsProcessEnd)  {
          parent.eFapsProcessEnd();
        }
      }
    </script>

    <body onLoad="eFapsShowError()">
    </body>
  </html>
</fmt:bundle>