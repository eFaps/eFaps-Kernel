<%@taglib prefix="c"  uri="http://java.sun.com/jstl/core"%>
<%@taglib prefix="un" uri="http://jakarta.apache.org/taglibs/unstandard-1.0"%>

<%@page import="java.util.HashMap"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>

<%@page import="org.apache.commons.fileupload.DiskFileUpload"%>
<%@page import="org.apache.commons.fileupload.FileItem"%>

<%@page import="org.efaps.admin.user.Person"%>
<%@page import="org.efaps.db.Context"%>

<%!

  public String getParameter(String _name)  {
    String ret = null;

    String[] values = parameters.get(_name);
    if (values!=null)  {
      ret = values[0];
    }
    return ret;
  }


  Map<String, String[]> parameters = null;
  Map<String, FileItem> fileParameters = new HashMap<String, FileItem>();
%>

<%

  // set context variable
  Context context = Context.getThreadContext();


  // set parameters map
  String contentType = request.getContentType();
  if(contentType != null && contentType.toLowerCase().startsWith("multipart/form-data"))  {
    // multipart request
    DiskFileUpload dfu = new DiskFileUpload();
// TODO: global setting!! + temp variable! 
// dfu.setRepositoryPath("s:\\temp");	also works without this *jul*

    List files = dfu.parseRequest(request);
    parameters = new HashMap<String, String[]>();
    for (Object obj : files)  {
      FileItem file = (FileItem)obj;
      if (file.isFormField())  {
        parameters.put(file.getFieldName(), new String[]{file.getString()});
      } else  {
        fileParameters.put(file.getFieldName(), file);
      }
    }
  } else  {
    // normal request
    parameters = request.getParameterMap();
  }
%>
