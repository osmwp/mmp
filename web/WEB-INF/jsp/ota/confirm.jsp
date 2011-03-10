<%@ include file="/WEB-INF/jsp/include.jsp" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title><fmt:message key="ota.confirm.title"/></title>
</head>
<body>
	<c:if test="${errormsg != null}">
		<br/><br/><fmt:message key="${errormsg}"/><br/>
	</c:if>

	<c:if test="${errormsg == null}">
		<fmt:message key="ota.confirm.label.begin"/>&nbsp;<a href="<c:out value="${url}"/>"><fmt:message key="ota.confirm.link.label"/></a>&nbsp;<fmt:message key="ota.confirm.label.end"/>
		<c:if test="${noSignOption == true}">
			<fmt:message key="ota.confirm.label.nosign"/>&nbsp;<a href="<c:out value="${url}"/>&sign=false"><fmt:message key="ota.confirm.link.label"/></a>
		</c:if>
	</c:if>
</body>
</html>