<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<html>
	<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="<c:url value="/css/admin.css"/>" type="text/css">
	<title><s:text name="menu.mobile.manage" /></title>

<script type="text/javascript">

	wmtt = null;

	var paramTypes = new Array("jadp");

	document.onmousemove = updateWMTT;

	function branchChange() {
		var branch = document.getElementById('branch').options[document.getElementById('branch').selectedIndex].value;
		var i = document.getElementById('i').value;
		for ( var u = 1; u <= i; u++) {
			if(branch == document.getElementById('id'+u).value) {
				document.getElementById('mobilesFromBranch'+u).style.display = 'block';
			} else {
				document.getElementById('mobilesFromBranch'+u).style.display = 'none';
			}
		}
	}

	function checkDrop() {
		// TMP
		document.getElementById('otaEnabled').checked = true;
		document.getElementById('otaEnabled').disabled = true;
		var dropped = document.getElementById('dropped').checked;
		if(dropped) {
			document.getElementById('dropdate').disabled = true;
			document.getElementById('otaEnabled').disabled = true;
			document.getElementById('dropped').disabled = true;
			document.getElementById('branch').disabled = true;
		}
	}
	
	function updateWMTT(e) {
		x = (document.all) ? window.event.x + document.body.scrollLeft : e.layerX;
		y = (document.all) ? window.event.y + document.body.scrollTop  : e.layerY;
		if (wmtt != null) {
			wmtt.style.left = (x + 20) + "px";
			wmtt.style.top   = (y - 20) + "px";
		}
	}

	function showWMTT(id) {
		// Keep if X-Tags are back
		//wmtt = document.getElementById(id);
		//wmtt.style.display = "block";
	}

	function hideWMTT() {
		// Keep if X-Tags are back
		//wmtt.style.display = "none";
	}

    function showOrHideSpecialParams(id) {
        var objLabel = document.getElementById(id+'label');
        
        for(var i=0; i<paramTypes.length; i++) {
            var objId = document.getElementById(id+paramTypes[i]);
            var disp = objId.className;
            if(disp == "component") {

                objId.className = 'hiddenWrapper';
                if(i == 0) 
                    objLabel.innerHTML = document.getElementById('show').value;
            } else {
                objId.className = 'component';
                if(i == 0)
                    objLabel.innerHTML = document.getElementById('hide').value;
            }
        }
    }

    function addJadAttr() {
		if(document.getElementById('attribute').value != '' && document.getElementById('value').value != '') {
			var currentJad = document.getElementById('action').value + ':' + document.getElementById('attribute').value
			+ '=' + document.getElementById('value').value + ';';
			currentJad += 'injad=' + document.getElementById('injad').value + ';';
			currentJad += 'inmf=' + document.getElementById('inmf').value + ';';

			if(document.getElementById('strict').checked) {
				currentJad += 'strict;';
			}
			
			currentJad += "\n";
			
			document.getElementById('jadAttrs').value += currentJad;
			
			document.getElementById('action').options[0].selected = true;
			document.getElementById('attribute').value = "";
			document.getElementById('value').value = "";
			document.getElementById('strict').checked = false;
			document.getElementById('injad').options[0].selected = true;
			document.getElementById('inmf').options[0].selected = true;
		}
		
	}

	function undrop() {
		document.getElementById('dropped').checked = false;
		document.getElementById('dropped').value = false;
		document.getElementById('dropdate').disabled = false;
		document.getElementById('dropped').disabled = false;
		document.getElementById('branch').disabled = false;
		document.getElementById('undropbutton').disabled = true;
	}

    function validate() {
    	document.getElementById('key').disabled = false;
    	document.getElementById('otaEnabled').disabled = false;
    	var dropped = document.getElementById('dropped').checked;
		if(dropped) {
			document.getElementById('dropdate').disabled = false;
			document.getElementById('dropped').value = true;
			document.getElementById('dropped').disabled = false;
			document.getElementById('branch').disabled = false;
		}
    }
	
</script>

</head>
<body onload="init()">
   	<%@ include file="/admin/menu.jsp" %>
   	<table width="100%">
<tr><td align="center">
   	<div id="main" style="text-align:center">
   		<font class="title"><s:text name="menu.mobile.manage"/></font>
		<%@ include file="/WEB-INF/jsp/popup.jsp"%>
		<s:form method="post" action="mobile" namespace="/struts2" theme="css_xhtml">
			<s:hidden id="hide" value="%{getText('mobile.action.hide')}"/>
			<s:hidden id="show" value="%{getText('mobile.action.see')}"/>
			
			<table align="center" width="90%">
				<tr>
					<td>
							<fieldset>
							<legend class="fieldsetLegend"><s:text name="mobile.model.title"/></legend>
								<table width="100%" cellpadding="5" cellspacing="0">
								<tr>
									<td align="left" class="label"><font class="label"><s:text name="mobile.label.key" /></font></td>
									<td align="left">
										<s:if test="%{actionType == 'edit'}">
											<s:textfield id="key" name="key" disabled="true"/>
										</s:if>
										<s:else>
											<s:textfield id="key" name="key"/>
										</s:else>
									</td>
									<td align="left"><font class="label"><s:text name="mobile.label.ua.discrpart"/></font></td>
									<td align="left"><s:textfield id="ua" name="ua"/></td>
								</tr>
								<tr>
									<td align="left" class="label">
										<font class="label"><s:text name="extra.mobiles.codename"/></font>
									</td>
									<td align="left">
										<s:textfield id="codename" name="codename"/>
									</td>
									<td align="left" class="label">
										<font class="label"><s:text name="extra.mobiles.finalname"/></font>
									</td>
									<td align="left" colspan="3">
										<s:textfield id="finalname" name="finalname"/>
									</td>
								</tr>
								<tr>
									<td align="left"><font class="label"><s:text name="mobile.label.midlet.type"/></font></td>
									<td align="left">
										<s:select list="types" name="midletType">
											
										</s:select>
									</td>
									<td></td><td></td>
								</tr>
							</table>
						</fieldset>
					</td>
				</tr>
				<tr>
					<td>
					<fieldset>
					<legend class="fieldsetLegend"><s:text name="mobile.label.branch.title"/></legend>
						<table width="100%" cellpadding="5" cellspacing="0">
						<tr>
							<td align="left" valign="top"><font class="label"><s:text name="mobile.label.branch"/></font></td>
							<td align="left" valign="top">
								<s:select list="branches" id="branch" name="branchId" onchange="branchChange()" listKey="id" listValue="name">
								</s:select>
							</td>
							<td width="50%" colspan="2">
							    <table cellspacing="0" cellpadding="0" border="0" width="100%">
							        <tr>
										<td align="left" valign="top"><font class="label"><s:text name="mobile.label.mobiles"/></font></td>
										<td align="left">
											<div id="mobilesFromBranch" class="mobiles" style="display: block;">
												<font class="text">
													<c:set var="i" value="1"/>
														<s:iterator value="mobiles">
															<s:if test="%{currentBranch.id == branchId}">
															<div id="mobilesFromBranch<c:out value="${i}"/>" style="display: block;">
															</s:if>
															<s:else>
															<div id="mobilesFromBranch<c:out value="${i}"/>" style="display: none;">
															</s:else>
<!--																<s:property value="key"/>-->
																<b><s:property value="key"/></b><s:if test="%{ codeName != null}">&nbsp;-&nbsp;[<i><s:property value="codeName"/></i>]</s:if><br>
																<input type="hidden" id="id<c:out value="${i}"/>" name="id<c:out value="${i}"/>" value="<s:property value="branchId"/>">
															</div>
															<c:set var="i" value="${ i+1 }"/>
														</s:iterator>
													<input type="hidden" id="i" name="i" value="<c:out value="${ i-1 }"/>">
												</font>
											</div>
										</td>
									</tr>
								</table>
							</td>
						</tr>
						</table>
				</fieldset>
				</td>
				</tr>
				<tr>
					<td>
					<fieldset>
					<legend class="fieldsetLegend"><s:text name="mobile.label.mobile.delivery"/></legend>
						<table width="100%" cellpadding="5" cellspacing="0">
						 <tr>
							<td align="left" width="50%">
								<font class="label"><s:checkbox labelposition="right" key="mobile.label.mobile.dropped" id="dropped" name="dropped"/></font>
							</td>

							<td align="left"><font class="label"><s:text name="mobile.label.drop.date"/></font>&nbsp;<font class="text"><s:text name="mobile.label.date.format"/></font></td>
							<td align="left">
								<s:textfield id="dropdate" name="dropDate" />
							</td>
						</tr>
						<tr>
							<td align="left" colspan="3">
								<font class="label"><s:checkbox labelposition="right" key="mobile.label.ota.enabled" id="otaEnabled" name="otaEnabled"/></font>
							</td>
						</tr>
				</table>
			</fieldset>
			</td>
			</tr>
				<tr>
					<td>
					<fieldset>
					<legend class="fieldsetLegend"><s:text name="extra.jad.title"/></legend>
						<table width="100%" cellpadding="5" cellspacing="0">
							<tr>
								<td width="100%">
									<table width="100%">
										<tr>
											<td align="left" class="label">
												<s:text name="extra.mobiles.action"/>
											</td>
											<td>
												<select id="action">
													<option value="add">add</option>
													<option value="modify">modify</option>
													<option value="delete">delete</option>
												</select>
											</td>
											<td align="left" class="label">
												<s:text name="extra.mobiles.attribute"/>
											</td>
											<td>
												<input type="text" id="attribute" value=""/>
											</td>
											<td align="left" class="label">
												<s:text name="extra.mobiles.value"/>
											</td>
											<td>
												<input type="text" id="value" value=""/>
											</td>
										</tr>
										<tr>
											<td align="left" class="label" colspan="2" width="33%">
												<input type="checkbox" id="strict"/><s:text name="extra.mobiles.strict"/>
											</td>
											<td align="left" class="label" colspan="2" width="33%">
												<s:text name="extra.mobiles.injad"/>
												<select id="injad">
													<option value="always">always</option>
													<option value="unsigned">signed</option>
													<option value="unsigned">unsigned</option>
													<option value="never">never</option>
												</select>
											</td>
											<td align="left" class="label" colspan="2" width="33%">
												<s:text name="extra.mobiles.inmf"/>
												<select id="inmf">
													<option value="always">always</option>
													<option value="unsigned">signed</option>
													<option value="unsigned">unsigned</option>
													<option value="never">never</option>
												</select>
											</td>
										</tr>
										<tr>
											<td colspan="6" align="center">
												<input type="button" onclick="addJadAttr()" class="fieldButton" value="<s:text name="extra.button.jad"/>">
											</td>
										</tr>
										<tr>
											<td colspan="6">
												<s:textarea rows="4" id="jadAttrs" name="jadAttrs" style="{width:100%}"/>
											</td>
										</tr>
									</table>
								</td>
							</tr>
						</table>
						</fieldset>
					</td>
				</tr>
			</table>
			<br>
			<s:hidden name="actionType" id="actionType"/>
			<s:if test="%{actionType == 'edit'}">
				<s:if test="%{ dropped == true }">
					<input id="undropbutton" type="button" value="<s:text name="mobile.button.undrop"/>" class="fieldButton" onMouseDown="undrop()"/>
				</s:if>
				<s:submit cssClass="fieldButton" method="edit" key="mobile.button.modify" align="center" onclick="javascript: validate();"/>
			</s:if>
			<s:else>
				<center>
					<s:submit cssClass="fieldButton" method="add" key="mobile.button.add" align="center"/>
				</center>
			</s:else>
			
					
		</s:form>
		
		<br>
			<table class="component" align="center" width="100%">
				<thead>
					<tr>
                        <th><s:text name="mobile.header.final"/></th>
                        <th><s:text name="mobile.header.code"/></th>
						<th><s:text name="mobile.header.key"/></th>
						<th><s:text name="mobile.header.ua"/></th>
						<th><s:text name="mobile.header.branch"/></th>
						<th><s:text name="mobile.header.drop"/></th>
						<th><s:text name="mobile.header.extra"/></th>
						<th><s:text name="mobile.header.midlet"/></th>
						<th><s:text name="mobile.header.actions"/></th>
					</tr>
				</thead>
				<s:iterator value="mobiles">
					<tr>
						<td class="keyComponent" style="text-align:center">
							<s:property value="finalName"/>
						</td>
						<td class="notSignificantComponent" style="text-align:center">
							<s:property value="codeName"/>
						</td>
						<s:if test="%{otaEnabled == null || otaEnabled == true }">
							<td class="significantComponent" style="text-align:center">
								<s:property value="key"/>
							</td>
							<td class="component" style="text-align:center">
								<s:property value="shortUserAgent"/>
							</td>
							<td class="component" style="text-align:center">
								<s:property value="branchId"/>
							</td>
							<td class="component" style="text-align:center">
								<s:property value="dropDate"/>
							</td>
							<td class="component" style="text-align:center">
								<a href="javascript:showOrHideSpecialParams('<s:property value="key"/>')" onMouseOver="showWMTT('<s:property value="key"/>')" onMouseOut="hideWMTT()"  id="<s:property value="key"/>label">
									<s:text name="mobile.action.see"/>
								</a>
								<div class="tooltip" id="<s:property value="key"/>">
									<s:property value="xtags"/>
								</div>
							</td>
							<td class="component" style="text-align:center">
								<s:property value="midletType"/>
							</td>
							<td class="component" style="text-align:center">
								<s:url id="urlRemove" action="mobile_remove">
            						<s:param name="key"><s:property value="key"/></s:param>
	            				</s:url>
								<s:a href="%{urlRemove}" onclick="javascript: return confirm('%{getText('mobile.confirm.remove')}');"><s:text name="mobile.action.remove"/></s:a>
								<s:url id="urlModify" action="mobile_display">
    	        					<s:param name="key"><s:property value="key"/></s:param>
        	    				</s:url>
								<s:a href="%{urlModify}"><s:text name="mobile.action.modify"/></s:a>
								<br>
								<s:url id="urlToSign" action="mobile_zip">
            						<s:param name="key"><s:property value="key"/></s:param>
            						<s:param name="zipType">toSign</s:param>
	            				</s:url>
								<s:a href="%{urlToSign}"><s:text name="mobile.action.tosign"/></s:a>
								&nbsp;/&nbsp;
								<s:url id="urlSigned" action="mobile_zip">
            						<s:param name="key"><s:property value="key"/></s:param>
            						<s:param name="zipType">signed</s:param>
            					</s:url>
								<s:a href="%{urlSigned}"><s:text name="mobile.action.signed"/></s:a>
								&nbsp;/&nbsp;
								<s:url id="urlUnsigned" action="mobile_zip">
        	    					<s:param name="key"><s:property value="key"/></s:param>
            						<s:param name="zipType">unsigned</s:param>
            					</s:url>
								<s:a href="%{urlUnsigned}"><s:text name="mobile.action.unsigned"/></s:a>
							</td>
						</s:if>
						<s:else>
							<td class="noOTAComponent" style="text-align:center">
								<s:property value="key"/>
							</td>
							<td class="noOTAComponent" style="text-align:center">
								<s:property value="shortUserAgent"/>
							</td>
							<td class="noOTAComponent" style="text-align:center">
								<s:property value="branchId"/>
							</td>
							<td class="noOTAComponent" style="text-align:center">
								<s:property value="dropDate"/>
							</td>
							<td class="noOTAComponent" style="text-align:center">
								<a href="javascript:showOrHideSpecialParams('<s:property value="key"/>')" onMouseOver="showWMTT('<s:property value="key"/>')" onMouseOut="hideWMTT()" id="<s:property value="key"/>label">
									<s:text name="mobile.action.see"/>
								</a>
								<div class="tooltip" id="<s:property value="key"/>">
									<s:property value="xtags"/>
								</div>
							</td>
							<td class="noOTAComponent" style="text-align:center">
								<s:property value="midletType"/>
							</td>
							<td class="noOTAComponent" style="text-align:center">
								<s:url id="urlRemove" action="mobile_remove">
            						<s:param name="key"><s:property value="key"/></s:param>
	            				</s:url>
								<s:a href="%{urlRemove}" onclick="javascript: return confirm('%{getText('mobile.confirm.remove')}');"><s:text name="mobile.action.remove"/></s:a>
								<s:url id="urlModify" action="mobile_display">
    	        					<s:param name="key"><s:property value="key"/></s:param>
        	    				</s:url>
								<s:a href="%{urlModify}"><s:text name="mobile.action.modify"/></s:a>
								<br>
								<s:url id="urlToSign" action="mobile_zip">
            						<s:param name="key"><s:property value="key"/></s:param>
            						<s:param name="zipType">toSign</s:param>
	            				</s:url>
								<s:a href="%{urlToSign}" title="%{getText('mobile.info.tosign')}"><s:text name="mobile.action.tosign"/></s:a>
								&nbsp;/&nbsp;
								<s:url id="urlSigned" action="mobile_zip">
            						<s:param name="key"><s:property value="key"/></s:param>
            						<s:param name="zipType">signed</s:param>
            					</s:url>
								<s:a href="%{urlSigned}" title="%{getText('mobile.info.signed')}"><s:text name="mobile.action.signed"/></s:a>
								&nbsp;/&nbsp;
								<s:url id="urlUnsigned" action="mobile_zip">
        	    					<s:param name="key"><s:property value="key"/></s:param>
            						<s:param name="zipType">unsigned</s:param>
            					</s:url>
								<s:a href="%{urlUnsigned}" title="%{getText('mobile.info.unsigned')}"><s:text name="mobile.action.unsigned"/></s:a>
							</td>
						</s:else>
					</tr>
					<tr id="<s:property value="key"/>xtags" class="hiddenWrapper">
						<td colspan="9" class="subcomponent" align="center">
							<s:property value="xtags"/>
						</td>
					</tr>
					
                    <tr id="<s:property value="key"/>jadp" class="hiddenWrapper">
                        <td colspan="9" class="subcomponent" align="center">
                            <c:set var="index" value="0"/>
                            
                            <s:iterator value="jadAttributeActions">
                            	
                                <s:if test="%{action.toString() == 'add'}">
                                    <font class="attrAdd">
                                        <s:text name="mobile.jad.add"/> "<b><s:property value="attribute"/>: <s:property value="value"/>"</b>
                                    </font>
                                </s:if>
                                <s:if test="%{ action.toString() == 'modify' }">
                                    <font class="attrUpdate">
	                                    <s:text name="mobile.jad.update"/> <b>"<s:property value="attribute"/>: <s:property value="value"/>"</b>
	                                    <s:if test="%{ strict == true }">
	                                        &nbsp;<s:text name="mobile.jad.exists"/>
	                                    </s:if>
                                    </font>
                                </s:if>
                                <s:if test="%{ action.toString() == 'delete' }">
                                    <font class="attrDelete">
                                        <s:text name="mobile.jad.delete"/> <b>"<s:property value="attribute"/>"</b>
                                    </font>
                                </s:if>
                                <s:if test="%{ inJad != true || inManifest != true }">
                                    (
                                    <s:if test="%{ inJad != true }">
                                        <s:text name="mobile.jad.except.jad"/>
                                    </s:if>
	                                <s:if test="%{ inManifest != true }">
	                                    <s:if test="%{ inJad != true }">
	                                        ,
	                                    </s:if>
	                                    <s:text name="mobile.jad.except.manifest"/>
	                                </s:if>
	                                )
	                            </s:if>
                                <c:set var="index" value="${index + 1}"/>
                                <br/>
                           	</s:iterator>
                           	
                            <c:if test="${ index == 0 }">
                                <i><s:text name="mobile.jad.nochange"/></i>
                            </c:if>
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