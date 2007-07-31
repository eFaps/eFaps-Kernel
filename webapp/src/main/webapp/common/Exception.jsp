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

<%@page isErrorPage="true"%>
<%@page import="java.text.MessageFormat"%>
<%@page import="org.efaps.admin.dbproperty.DBProperties"%>
<%@page import="org.efaps.util.EFapsException"%>


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
<%  
  String errorId = "";
  String errorMessage = exception.getMessage();
  String errorAdvanced = "";
  String errorAction = "";
  String errorKey = "";
  if (exception instanceof EFapsException)  {
    EFapsException eFapsException = (EFapsException)exception;
    errorKey = eFapsException.getClassName().getName() + "." + eFapsException.getId();
    errorId = DBProperties.getProperty(errorKey + ".Id");
    errorMessage = DBProperties.getProperty(errorKey + ".Message");
    errorAction = DBProperties.getProperty(errorKey + ".Action");
    

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

    StringBuffer buf = new StringBuffer();
    buf.append("<html><title>");
    buf.append(DBProperties.getProperty("JSPPage.Exception.TextTitle"));
    buf.append("</title>");
    buf.append("<link rel=\"StyleSheet\" href=\"../styles/eFapsDefault.css\" type=\"text/css\"/>");
    buf.append("<script language=\"javascript\">");
    buf.append("function advanced()  {");
    buf.append("var obj = document.getElementById(\"advanced\");");
    buf.append("if (obj.style.display==\"none\")  {");
    buf.append("obj.style.display = \"\";");
    buf.append("} else  {");
    buf.append("obj.style.display = \"none\";");
    buf.append("}}</script>");
    buf.append("<body><center>");
    buf.append("<table class=\"eFapsError\">");
    buf.append("<tr>");
    buf.append("<td class=\"eFapsErrorLabel\">");
    buf.append(DBProperties.getProperty("JSPPage.Exception.TextId"));
    buf.append("</td>");
    buf.append("<td class=\"eFapsErrorText\">").append(decode(errorId)).append("</td>");
    buf.append("</tr>");
    buf.append("<tr>");
    buf.append("<td class=\"eFapsErrorLabel\">");
    if (errorAdvanced.length()>0)  {
        buf.append("<a class=\"eFapsErrorLabel\" accesskey=\"a\" href=\"javascript:advanced()\">");
    }
    buf.append(decode(DBProperties.getProperty("JSPPage.Exception.TextMessage")));
    if (errorAdvanced.length()>0)  {
      buf.append("</a>");
    }
    buf.append("</td>");
    buf.append("<td class=\"eFapsErrorText\">");
    buf.append(decode(errorMessage));
    buf.append("</td>");
    buf.append("</tr>");
    if (errorAdvanced.length()>0)  {
      buf.append("<tr id=\"advanced\" style=\"display:none\">");
      buf.append("<td class=\"eFapsErrorLabel\">");
      buf.append(decode(DBProperties.getProperty("JSPPage.Exception.TextAdvanced")));
      buf.append("</td>");
      buf.append("<td class=\"eFapsErrorText\">").append(decode(errorAdvanced)).append("</td>");
      buf.append("</tr>");
    }
    buf.append("<tr>");
    buf.append("<td class=\"eFapsErrorLabel\">");
    buf.append(decode(DBProperties.getProperty("JSPPage.Exception.TextAction")));
    buf.append("</td>");
    buf.append("<td class=\"eFapsErrorText\">").append(decode(errorAction)).append("</td>");
    buf.append("</tr>");
    buf.append("</table>");
    buf.append("<div style=\"text-align:center\"><a href=\"javascript:window.close()\">Close</a></div>");
    buf.append("</body></html>");

    String text = buf.toString();
    text = text.replaceAll("<", "%3c");
    text = text.replaceAll(">", "%3e");
    text = text.replaceAll("\'", "%27");
    text = text.replaceAll("\n", " ");
    text = text.replaceAll("\r", " ");

    
    String errorID = decode(errorId);
    String errorMsg = decode(errorMessage);
    String errorAct = decode(errorAction);
  %>
  <html>
    <script type="text/javascript">

      function eFapsShowError()  {
        parent.errorPage('<%=errorID%>','<%=errorMsg%>','<%=errorAct%>');
      }
    </script>

    <body onLoad="eFapsShowError()">
    </body>
  </html>
