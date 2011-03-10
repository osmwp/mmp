<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="<c:url value="/css/admin.css"/>" type="text/css">
	<title><s:text name="menu.midlet.manage"/></title>
	
</head>
<body onload="init()" align="center">	
<%@ include file="/admin/menu.jsp" %>
<table width="100%">
<tr><td align="center">
	<div id="main" style="text-align:center">
		<font class="title"><s:text name="menu.midlet.manage"/></font>
		<%@ include file="/WEB-INF/jsp/popup.jsp"%>
		<div align="center">
		<s:form method="post" action="midlet_upload" namespace="/struts2" theme="css_xhtml" name="myForm" enctype="multipart/form-data">		
			<table align="center" width="90%">
				<tr>
					<td>
					<fieldset>
							<legend class="fieldsetLegend"><s:text name="midlet.upload.title"/></legend>
							<div style="text-align:center">
									<table class="wrapper">
											<tr>
												<td class="label">
													<s:text name="midlet.upload.label"/>
												</td>
												<td>
													<s:file name="uploadedFile" size="70" accept="application/zip"/>
												</td>
											</tr>
									</table>
							</div>
				</fieldset>
			</td>
		</tr>
		</table>	
		<br/>
		<table align="center" width="90%">
			<tr>
				<td colspan="2" align="center">
					<s:submit cssClass="fieldButton" method="input" key="midlet.button.upload" align="center"/>
				</td>
			</tr>
		</table>	
		 </s:form>
		<br/>	
		<table class="component" align="center" width="90%">
			<thead>
				<tr>
					<th><s:text name="midlet.list.name"/></th>
					<th><s:text name="midlet.list.version"/></th>
					<th><s:text name="midlet.list.files"/></th>
					<th><s:text name="midlet.list.action"/></th>
				</tr>
			</thead>
			<s:iterator value="midlets">
				<tr>
					<td class="component" style="text-align:center">
						<b><s:property value="type"/></b>
					</td>
					<td class="component" style="text-align:center">
						<s:property value="version"/>
					</td>
					<td class="component" style="text-align:left">
						<ul>
							<li><s:property value="jadLocation"/></li>
							<li><s:property value="jarLocation"/></li>
						</ul>
					</td>
					<td class="component" style="text-align:center">
						<s:url id="urlRemove" action="midlet_remove">
    						<s:param name="type"><s:property value="type"/></s:param>
         				</s:url>
						<s:a href="%{urlRemove}"><s:text name="midlet.list.remove"/></s:a>
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