top.frameMenu = new eFapsToolbar(<c:out value="${domContext}"/>window, 'frameMenu', 0, 40);
var topMenu = top.frameMenu.createCommandHolder();
with (topMenu)  {

  <c:forEach items="${uiObject.menuHolder.subs}" var="one1">
    <c:set var="one" value="${one1}"/>

    with (addCommand('<c:out value="${one.sub.name}"/>', '<fmt:message><c:out value="${one.sub.label}"/></fmt:message>'))  {
      <%@include file="TreeOneCommand.inc"%>
      <c:if test="${one.menu}">
        with (createCommandHolder())  {
          <c:forEach items="${one1.subs}" var="one2">
            <c:set var="one" value="${one2}"/>
            with (addCommand('<c:out value="${one.sub.name}"/>', '<fmt:message><c:out value="${one.sub.label}"/></fmt:message>'))  {
              <%@include file="TreeOneCommand.inc"%>
              <c:if test="${one.menu}">
                with (createCommandHolder())  {
                  <c:forEach items="${one2.subs}" var="one3">
                    <c:set var="one" value="${one3}"/>
                    with (addCommand('<c:out value="${one.sub.name}"/>', '<fmt:message><c:out value="${one.sub.label}"/></fmt:message>'))  {
                      <%@include file="TreeOneCommand.inc"%>
                    }
                  </c:forEach>
                }
              </c:if>
            }
          </c:forEach>
        }
      </c:if>
    }
  </c:forEach>
}
