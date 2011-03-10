<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="<c:url value="/css/admin.css"/>" type="text/css">
	<title><s:text name="menu.service.manage"/></title>
	
	<script type="text/javascript">
		function validate() {
			document.getElementById('id').disabled = false;
		}

		function addEntry() {
			if(document.getElementById('jadKey').value != ""){
				var currentEntry = document.getElementById('jadKey').value
					+ '=' + document.getElementById('jadValue').value + '\r\n';
				document.getElementById('jadEntries').value += currentEntry;
				document.getElementById('jadKey').value = "";
				document.getElementById('jadValue').value = "";
			}
		}
		
	</script>
	
</head>
	<body onload="init()" align="center">
<%@ include file="/admin/menu.jsp" %>
<table width="100%">
<tr><td align="center">
	<div id="main" style="text-align:center">
		<font class="title"><s:text name="menu.service.manage"/></font>
		<%@ include file="/WEB-INF/jsp/popup.jsp"%>
		<s:form method="post" action="service" namespace="/struts2" theme="css_xhtml" name="myForm">
		<table align="center" width="90%" >
				<tr>
					<td>
					<fieldset>
							<legend class="fieldsetLegend"><s:text name="service.configuration.title"/></legend>
								<s:hidden name="actionType" id="actionType"/>
								<table width="100%">
									<tr>
										<td align="left" colspan="3"><font class="label"><s:text name="service.label.id"/></font></td>
										<s:if test="%{actionType == 'edit'}">
											<td align="left" colspan="2"><s:textfield id="id" name="id" disabled="true"/></td>
										</s:if>
										<s:else>
											<td align="left" colspan="2"><s:textfield id="id" name="id"/></td>
										</s:else>
									</tr>
									<tr>
										<td align="left" colspan="3">
											<font class="label"><s:text name="service.label.endpoint"/></font>
										</td>
										<td align="left" colspan="2">
											<s:textfield id="hostname" name="hostname"/>
										</td>
									</tr>
									<tr>
										<td align="left" colspan="3">
											<font class="label"><s:text name="service.label.dedicated"/></font>
										</td>
										<td align="left" class="component" colspan="2">
											<s:radio list="#{'true':'True', 'false':'False'}" name="homepage" value="%{homepage}"/>
										</td>
									</tr>
									<tr>
										<td align="left" colspan="3">
											<font class="label"><s:text name="service.label.version"/></font>
										</td>
										<td align="left" class="component" colspan="2">
											<s:radio list="#{'true':'True', 'false':'False'}" name="usedef" value="%{usedef}"/>
										</td>
									</tr>
									<tr>
										<td align="left" colspan="3">
											<font class="label"><s:text name="service.label.signing"/></font>
										</td>
										<td align="left" class="component" colspan="2">
											<s:radio list="#{'true':'True', 'false':'False'}" name="signing" value="%{signing}"/>
										</td>
									</tr>
									<tr>
										<td align="left" colspan="3">
											<font class="label"><s:text name="service.label.headers"/></font>
										</td>
										<td align="left" class="component" colspan="2">
											<s:radio list="#{'true':'True', 'false':'False'}" name="wtheaders" value="%{wtheaders}"/>
										</td>
									</tr>
									<tr>
										<td align="left" colspan="3">
											<font class="label"><s:text name="service.label.jar"/></font>
										</td>
										<td align="left" class="component" colspan="2">
											<s:radio list="#{'true':'True', 'false':'False'}" name="compactjad" value="%{compactjad}"/>
										</td>
									</tr>
									
									<tr>
										<td align="left" colspan="3">
											&nbsp;
										</td>
										<td align="left" class="component" colspan="2">
											<br>
											<s:checkbox name="isDefault" id="isDefault" key="service.label.default" labelposition="left"/>
										</td>
									</tr>
									
								</table>	
								</fieldset>
								</td>
							</tr>
							<tr>
							 <td>	
								<fieldset>
								<legend class="fieldsetLegend"><s:text name="service.attributes.title"/></legend>
								<table width="100%">
									<!-- JadEntries -->
									<tr>
										<td align="left" class="component">
											<font class="label"><s:text name="service.jadentry.new"/></font>
										</td>
										<td align="left" class="component">
											<s:textfield id="jadKey"/>
										</td>
										<td align="left" class="component">
											<font class="label"><s:text name="service.jadentry.value"/></font>
										</td>
										<td align="left" class="component">
											<s:textfield id="jadValue"/>
										</td>
										<td align="left" class="component">
											<input type="button" onclick="addEntry()" class="fieldButton" value="<s:text name="service.jadentry.button"/>">
										</td>
									</tr>
									<tr>
										<td align="left" colspan="1" class="component">
											<font class="label"><s:text name="service.jadentry.label"/></font>
										</td>
										<td align="left" colspan="4" class="component">
											<s:textarea id="jadEntries" name="jadLine" style="width:100%" rows="5"/>
										</td>
									</tr>
								</table>
						</fieldset>
					</td>
				</tr>
				<tr>
					<td align="center">
						<br/>
						<center>
							<s:if test="%{actionType == 'edit'}">
								<s:submit cssClass="fieldButton" method="edit" key="service.button.modify" align="center" onclick="javascript: validate();"/>
							</s:if>
							<s:else>
								<s:submit cssClass="fieldButton" method="add" key="service.button.add" align="center"/>
							</s:else>
						</center>
						<br/>
					</td>
				</tr>
			</table>				
		</s:form>
		<table class="component" align="center">
			<thead>
				<tr>
					<th><s:text name="service.list.key"/></th>
					<th><s:text name="service.list.action"/></th>
				</tr>
			</thead>
			<s:iterator value="serviceList">
				<tr>
					<td class="component" style="text-align:center">
						<s:if test="%{ isDefault == true }">
							<b><s:property value="id"/></b>&nbsp;<i>(default)</i>
						</s:if>
						<s:else>
							<s:property value="id"/>
						</s:else>
					</td>
					<td class="component" style="text-align:center">
						<s:url id="urlModify" action="service_display">
            				<s:param name="id"><s:property value="id"/></s:param>
            			</s:url>
						<s:a href="%{urlModify}"><s:text name="service.action.modify"/></s:a>
													
						<s:url id="urlRemove" action="service_remove">
            				<s:param name="id"><s:property value="id"/></s:param>
            			</s:url>
						<s:a href="%{urlRemove}" onclick="javascript: return confirm('%{getText('service.confirm.remove')}');"><s:text name="service.action.remove"/></s:a>
					</td>
				</tr>
			</s:iterator>
		</table>
	</div>
		</td>
	</tr>
	</table>
</body>
</html>