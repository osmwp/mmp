<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="<c:url value="/css/admin.css"/>" type="text/css">
	<title><s:text name="menu.security.manage"/></title>
	
</head>
<body onload="init()" align="center">	
<%@ include file="/admin/menu.jsp" %>
<table width="100%">
<tr><td align="center">
	<div id="main" style="text-align:center">
		<font class="title"><s:text name="menu.security.manage"/></font>
		<%@ include file="/WEB-INF/jsp/popup.jsp"%>
		<s:form method="post" action="certif_upload" namespace="/struts2" theme="css_xhtml" name="myForm" enctype="multipart/form-data">
		<table align="center" width="90%">
				<tr>
					<td>
					<fieldset>
					<legend class="fieldsetLegend"><s:text name="certif.title"/></legend>
						<table align="center" width="100%">
							<tr>
								<td>
								<div style="text-align:center">
										<table class="wrapper">
											<tr>
												<td class="label">
													<s:text name="certif.upload.file"/>
												</td>
												<td>
													<s:file name="uploadedFile" size="70"/>
												</td>
											</tr>
										</table>
								</div>
								</td>
							</tr>	
						</table>
					</fieldset>
				</td>
			</tr>
			<tr>
				<td align="center">
					<br>
					<s:submit cssClass="fieldButton" method="input" key="certif.upload.button" align="center"/>
				</td>
			</tr>
		</table>
		</s:form>		
		<table align="center" width="90%">
				<tr>
					<td>
						<fieldset>
						<legend class="fieldsetLegend"><s:text name="certif.current.title"/></legend>
						<table width="100%">
								<s:iterator value="certifList" status="certifListStatus">
									<tr>
										<td align="left" style="font-family:Helvetica, Arial;font-size:12px;">
											<b><s:text name="certif.label.midlet"/><s:property value="%{#certifListStatus.count}"/>:</b><br><br>
											<b><s:text name="certif.label.subject"/></b><s:property value="subjectDN"/><br>
											<b><s:text name="certif.label.issuer"/></b><s:property value="issuerDN"/><br>
											<b><s:text name="certif.label.number"/></b><s:property value="serialNumber"/><br>
											<b><s:text name="certif.label.from"/></b>&nbsp;<s:property value="notBefore"/>&nbsp;<b><s:text name="certif.label.to"/></b>&nbsp;<s:property value="notAfter"/>
										</td>
									</tr>
								</s:iterator>
						</table>
						</fieldset>
					</td>
				</tr>
			</table>	
		</div>
		</td>
	</tr>
	</table>
</body>
</html>