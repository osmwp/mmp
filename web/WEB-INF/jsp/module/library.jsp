<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="<c:url value="/css/admin.css"/>" type="text/css">
	<title><s:text name="menu.module.manage"/></title>
	
</head>
<body onload="init()" align="center">
	<%@ include file="/admin/menu.jsp" %>
	<table width="100%">
		<tr>
			<td align="center">
				<div id="main" style="text-align:center">
					<font class="title"><s:text name="lib.list.title"/></font>
					<br><br>
					<%@ include file="/WEB-INF/jsp/popup.jsp"%>
		
					<div align="left" style="padding-left:10px">
						<br>
						<table class="component" width="100%" align="center">
							<tr>
								<td style="text-align:center" class="reverse">
									<s:text name="lib.list.label.name"/>
								</td>
								<td style="text-align:center" class="reverse">
									<s:text name="lib.list.label.packages"/>
								</td>
								<td style="text-align:center" class="reverse">
									<s:text name="lib.list.label.action"/>
								</td>
							</tr>
							<s:iterator value="librariesMap" status="librariesMapStatus"> <!-- Iter on lib -->
								<tr>
									<td class="component" style="text-align:center">
										<s:property value="key.name"/>
									</td>
									<td class="component" style="text-align:left;">
										<s:iterator value="value"> <!-- Iter on packages -->
											<s:property/><br>
										</s:iterator>
									</td>
									<td class="component" style="text-align:center">
										<s:url id="urlRemove" action="module_remove_module">
	            							<s:param name="id"><s:property value="key.id"/></s:param>
	            							<s:param name='type'>library</s:param>
	            						</s:url>
										<s:a href="%{urlRemove}"><img src="../img/poubelle.gif" border="0" alt="<s:text name="module.list.label.remove"/>"/></s:a>
									</td>
								</tr>
							</s:iterator>
						</table>
					</div>
				</div>
			</td>
		</tr>
	</table>
</body>
</html>