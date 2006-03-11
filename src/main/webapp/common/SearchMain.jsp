<%@page errorPage="Exception.jsp"%>
<html>
  <head/>
  <frameset cols="205,*" frameborder="yes" framespacing="5">
    <frame name="navigator" src="SearchNavigator.jsp?<%=request.getQueryString()%>" marginheight="8" marginwidth="8" scrolling="auto" frameborder="no"/>
    <frame name="content" src="" marginwidth="8" marginheight="8"/>
  </frameset>
</html>"
