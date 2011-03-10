<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="<c:url value="/css/admin.css"/>" type="text/css">
	<title><s:text name="module.head.title"/></title>
	
	<script type="text/javascript" language="javascript">
		function switchWS(name) {
			var wsTr = document.getElementById('wsInfo'+name);
			if(wsTr.className == 'hiddenWrapper'){
				wsTr.className = 'wrapper';
			} else {
				wsTr.className = 'hiddenWrapper';
			}
		}
		
	</script>
	
</head>
	<body onload="init()" align="center">
<%@ include file="/admin/menu.jsp" %>
<table width="100%">
<tr><td align="center">
	<div id="main" style="text-align:center">
		
		<font class="title"><s:text name="ws.list.title"/></font>
		<%@ include file="/WEB-INF/jsp/popup.jsp"%>
		<div align="left" style="padding-left:10px">
			<br/>
			<table class="component" width="100%" align="center">
				<tr>
					<td style="text-align:center" class="reverse">
						<s:text name="ws.list.label.name"/>						
					</td>
					<td style="text-align:center" class="reverse">
						<s:text name="ws.list.label.published"/>
					</td>
					<td style="text-align:center" class="reverse">
						<s:text name="ws.list.label.public"/>
					</td>
					<td style="text-align:center" class="reverse">
						<s:text name="ws.list.label.shared"/>
					</td>
				</tr>
				<s:iterator value="webServicesMap" status="wsMapStatus"> <!-- Iter WS -->					
					<tr>
						<td class="component" style="text-align:center">
							<a href="javascript:switchWS('<s:property value="key.name"/>')"/>
								<s:property value="key.name"/>
							</a>
						</td>
						<td class="component" style="text-align:center">
							<s:property value="key.published"/>
						</td>
						<td class="component" style="text-align:center">
							<s:property value="key.public"/>
						</td>
						<td class="component" style="text-align:center">
							<s:property value="key.shared"/>
						</td>
					</tr>
					<tr id="wsInfo<s:property value="key.name"/>" class="hiddenWrapper">
						<td class="component" style="text-align:left" colspan="4">
							<div class="scroller">
								<table class="component" align="center">
									<tr><th align="center"><s:text name="ws.list.label.api"/></th></tr>
									<s:iterator value="value" id="element"> <!-- Iter branches -->
										<tr>
											<td class="component" style="text-align:center; vertical-align:top;">
												<s:property value="element"/>
											</td>
										</tr>										
									</s:iterator> <!-- End iter branches -->
								</table>
							</div>
						</td>
					</tr>
								</s:iterator> <!-- End iter widgets -->
				</table>

		</div>
		
	</div>
		</td>
	</tr>
	</table>
</body>
</html>