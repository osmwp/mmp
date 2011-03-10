<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<html>
	<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="<c:url value="/css/admin.css"/>" type="text/css">
	<title><s:text name="menu.branch.manage" /></title>
	
	<script type="text/javascript">

		function getlists() {
			var mobiles = '';
			var mobilesChk = '';
			var checked = document.myForm.mobileChk;
			var i = document.getElementById('i').value;
			for ( var u = 1; u <= i; u++) {
				mobiles = mobiles + ';' + document.getElementById('mobile'+u).value;
				if (document.getElementById('mobile'+u).checked) {
					mobilesChk = mobilesChk + ';1';
				} else {
					mobilesChk = mobilesChk + ';0';
				}
			}
			document.getElementById('mobiles').value = mobiles;
			document.getElementById('mobilesChk').value = mobilesChk;
		}

		function validate() {
			getlists();
			var branchMobiles = '';
			var branchMobilesChk = '';
			var checked = document.myForm.branchMobileChk;
			var j = document.getElementById('j').value;
			for ( var u = 1; u <= j; u++) {
				branchMobiles = branchMobiles + ';' + document.getElementById('branchMobile'+u).value;
				if (document.getElementById('branchMobile'+u).checked) {
					branchMobilesChk = branchMobilesChk + ';1';
				} else {
					branchMobilesChk = branchMobilesChk + ';0';
				}
			}
			document.getElementById('mobilesStr').value = branchMobiles;
			document.getElementById('branchMobilesChk').value = branchMobilesChk;

			document.getElementById('id').disabled = false;
		}
	
	</script>
	
</head>
<body onload="init()">
<%@ include file="/admin/menu.jsp" %>
<table width="100%">
<tr><td align="center">
	<div id="main" style="text-align:center">
		<font class="title"><s:text name="menu.branch.manage" /></font>
		<%@ include file="/WEB-INF/jsp/popup.jsp"%>
		
		<s:form method="post" action="branch" namespace="/struts2" theme="css_xhtml" name="myForm">
			<s:hidden name="mobilesChk" id="mobilesChk"/>
			<s:hidden name="mobiles" id="mobiles"/>
			<s:hidden name="mobilesStr" id="mobilesStr"/>
			<s:hidden name="branchMobilesChk" id="branchMobilesChk"/>
			<s:hidden name="actionType" id="actionType"/>
			
			<table align="center" width="90%">
				<tr>
					<td>
						<fieldset>
						<legend class="fieldsetLegend"><s:text name="mobile.label.branch.title"/></legend>
						<table width="100%">
							<tr>
								<td align="left"><font class="label"><s:text name="branch.label.name"/></font></td>
								<td align="left"><s:textfield id="name" name="name"/></td>
							</tr>
							<tr>
								<td align="left"><font class="label"><s:text name="branch.label.id"/></font></td>
								<td align="left">
									<s:if test="%{actionType == 'edit'}">
										<s:textfield id="id" name="id" disabled="true"/>
									</s:if>
									<s:else>
										<s:textfield id="id" name="id"/>
									</s:else>
								</td>
							</tr>
							<tr>
								<td align="left" valign="top"><font class="label"><s:text name="branch.label.descr"/></font></td>
								<td align="left"><s:textarea id="description" name="description" rows="3" cols="100"/></td>
							</tr>
							<tr>
								<td align="left" valign="top"><font class="label"><s:text name="branch.label.mobiles"/></font></td>
								<td align="left" class="component">
								    <div class="mobiles">
								    	<c:set var="j" value="1"/>
										<s:iterator value="branchMobiles" status="branchMobileStatus">
											<s:checkbox value="true" cssClass="component" labelposition="right" fieldValue="%{key}" label="%{key}" id="branchMobile%{#branchMobileStatus.count}" name="branchMobileChk" />
											<c:set var="j" value="${ j+1 }"/>
										</s:iterator>
										<input type="hidden" id="j" name="j" value="<c:out value="${ j-1 }"/>">
								    
								    	<c:set var="i" value="1"/>
								    	<s:iterator value="defaultMobiles" status="mobileListStatus">
								    		<s:checkbox cssClass="component" labelposition="right" label="%{key}" fieldValue="%{key}" id="mobile%{#mobileListStatus.count}" name="mobileChk" />
								    		<c:set var="i" value="${ i+1 }"/>
								    	</s:iterator>
								    	<input type="hidden" id="i" name="i" value="<c:out value="${ i-1 }"/>">
									</div>
								</td>
							</tr>
						</table>
						</fieldset>
						<table width="100%">
							<tr>
								<td colspan="2" align="center">
									<br>
									<s:if test="%{actionType == 'edit'}">
										<s:submit cssClass="fieldButton" method="edit" key="branch.button.modify" align="center" onclick="javascript: validate();"/>
									</s:if>
									<s:else>
										<center>
											<s:submit cssClass="fieldButton" method="add" key="branch.button.add" align="center" onclick="javascript: getlists();"/>
										</center>
									</s:else>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
		</s:form>
			

		
		<table class="component" align="center">
			<thead>
				<tr>
            		<th><s:text name="branch.label.name"/></th>
                    <th><s:text name="branch.label.id"/></th>
					<th><s:text name="branch.label.descr"/></th>
					<th><s:text name="branch.label.mobiles.list"/></th>
					<th><s:text name="branch.label.action"/></th>
				</tr>
			</thead>
			<s:iterator value="branches">
				<tr>
					<td class="component" style="text-align:center">
						<s:property value="name"/>
					</td>
					<td class="component" style="text-align:center">
						<s:property value="id"/>
					</td>
					<td class="component" style="text-align:center">
						<s:property value="description"/>
					</td>
					<td class="component" style="text-align:center" width="10%">
						<s:if test="%{ mobiles.size() > 0 }">
							<div class="mobiles">
								<s:iterator value="mobiles">
									<b><s:property value="key"/></b><s:if test="%{ codeName != null }">&nbsp;-&nbsp;[<i><s:property value="codeName"/></i>]</s:if><br>
								</s:iterator>
							</div>
						</s:if>
					</td>
					<td class="component" style="text-align:center">
						<s:if test="name != 'Default'">
							<s:url id="urlModify" action="branch_display">
            					<s:param name="id"><s:property value="id"/></s:param>
            				</s:url>
							<s:a href="%{urlModify}"><s:text name="branch.action.modify"/></s:a>
							
							<s:if test="mobiles.size() > 0">
								<s:url id="urlRemoveMobiles" action="branch_remove">
            						<s:param name="id"><s:property value="id"/></s:param>
            					</s:url>
								<s:a href="%{urlRemoveMobiles}" onclick="javascript: return confirm('%{getText('branch.confirm.remove.mobiles')}');"><s:text name="branch.action.remove"/></s:a>
							</s:if>
							<s:else>
								<s:url id="urlRemove" action="branch_remove">
            						<s:param name="id"><s:property value="id"/></s:param>
            					</s:url>
								<s:a href="%{urlRemove}" onclick="javascript: return confirm('%{getText('branch.confirm.remove')}');"><s:text name="branch.action.remove"/></s:a>
							</s:else>
						</s:if>
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