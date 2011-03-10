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
				<font class="title"><s:text name="menu.module.manage"/></font>
				<%@ include file="/WEB-INF/jsp/popup.jsp"%>
				<div style="text-align:center">
					<s:form method="post" action="module_upload" namespace="/struts2" theme="css_xhtml" name="myForm" enctype="multipart/form-data">
						<table class="wrapper">
							<tr>
								<td class="label">
									<s:text name="module.upload.label"/>
								</td>
								<td>
									<s:file name="uploadedFile" size="70"/>
								</td>
							</tr>
							<tr>
								<td class="label">
									<s:text name="module.upload.branch"/>
								</td>
								<td>
									<s:select list="branch" id="branch" name="branchId" listKey="id" listValue="name">
									</s:select>
								</td>
							</tr>
							<tr>
								<td colspan="2" align="center">
									<br>
									<s:submit cssClass="fieldButton" method="upload" key="module.button.upload" align="center"/>
								</td>
							</tr>
						</table>
        			</s:form>
				</div>
				<div align="left" style="padding-left:10px">
					<br/>
					<table class="component" width="100%" align="center">
						<tr>
							<td style="text-align:center" class="reverse">
								<s:if test="%{sortOrder == 'descend'}">
									<s:if test="%{sortParam == 'type'}">
										<s:url action='module' id='urlSort'>
											<s:param name='sortOrder'>descend</s:param>
											<s:param name='sortParam'>name</s:param>
										</s:url>
									</s:if>
									<s:else>
										<s:url action='module' id='urlSort'>
											<s:param name='sortOrder'>ascend</s:param>
											<s:param name='sortParam'>name</s:param>
										</s:url>
									</s:else>
								</s:if>
								<s:else>
									<s:url action='module' id='urlSort'>
										<s:param name='sortOrder'>descend</s:param>
										<s:param name='sortParam'>name</s:param>
									</s:url>
								</s:else>
								
								<s:a href="%{urlSort}" cssClass="reverse">
									<font color="white"><s:text name="module.list.label.name"/></font>
								</s:a>
								<s:if test="%{sortParam == 'name'}">
									<s:if test="%{sortOrder == 'ascend'}">
										<img src="../img/ascend.gif"/>
									</s:if>
									<s:if test="%{sortOrder == 'descend'}">
										<img src="../img/descendarrow.gif"/>
									</s:if>
								</s:if>
							</td>
							<td style="text-align:center" class="reverse">
								<s:if test="%{sortOrder == 'descend'}">
									<s:if test="%{sortParam == 'name'}">
										<s:url action='module' id='urlSort'>
											<s:param name='sortOrder'>descend</s:param>
											<s:param name='sortParam'>type</s:param>
										</s:url>
									</s:if>
									<s:else>
										<s:url action='module' id='urlSort'>
											<s:param name='sortOrder'>ascend</s:param>
											<s:param name='sortParam'>type</s:param>
										</s:url>
									</s:else>
								</s:if>
								<s:else>
									<s:url action='module' id='urlSort'>
										<s:param name='sortOrder'>descend</s:param>
										<s:param name='sortParam'>type</s:param>
									</s:url>
								</s:else>
								
								<s:a href="%{urlSort}" cssClass="reverse">
									<font color="white"><s:text name="module.list.label.type"/></font>
								</s:a>
								<s:if test="%{sortParam == 'type'}">
									<s:if test="%{sortOrder == 'ascend'}">
										<img src="../img/ascend.gif"/>
									</s:if>
									<s:if test="%{sortOrder == 'descend'}">
										<img src="../img/descendarrow.gif"/>
									</s:if>
								</s:if>
							</td>
							<td style="text-align:center" class="reverse">
								<s:text name="module.list.label.branchid"/>
							</td>
							<td style="text-align:center" class="reverse">
								<s:text name="module.list.label.version"/>
							</td>
							<td style="text-align:center" class="reverse">
								<s:text name="module.list.label.action"/>
							</td>
						</tr>
						<s:iterator value="modulesMap" status="moduleMapStatus"> <!-- Iter on modules -->
							<tr>
								<td class="component" style="text-align:center">
									<s:property value="key.name"/>
								</td>
								<td class="component" style="text-align:center">
									<s:property value="key.category"/>
								</td>
								<s:if test="%{ value != '' }">
									<td class="component" style="text-align:center">
										<s:property value="value"/>
									</td>
								</s:if>
								<s:else>
									<td class="notSignificantComponent" style="text-align:center">
									</td>
								</s:else>
								<td class="component" style="text-align:center">
									<s:property value="key.version.value"/>
								</td>
								<td class="component" style="text-align:center">
									<s:url id="urlDownload" action="module_download_module">
            							<s:param name="id"><s:property value="key.id"/></s:param>
            						</s:url>
            						<s:a href="%{urlDownload}"><img src="../img/download.gif" border="0" alt="<s:text name="module.list.label.download"/>"/></s:a>
            						&nbsp;
									<s:url id="urlRemove" action="module_remove_module">
            							<s:param name="id"><s:property value="key.id"/></s:param>
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