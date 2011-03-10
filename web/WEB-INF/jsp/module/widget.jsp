<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="<c:url value="/css/admin.css"/>" type="text/css">
	<title><s:text name="module.head.title"/></title>
	
	<script type="text/javascript" language="javascript">
		function switchResources(widgetId) {
			var linkElement = document.getElementById('viewLink'+widgetId);
			var trElement = document.getElementById('viewTr'+widgetId);
			if(trElement.className == 'hiddenWrapper'){
				trElement.className = 'wrapper';
				linkElement.innerHTML = document.getElementById('hide').value;
			} else{
				trElement.className = 'hiddenWrapper';
				linkElement.innerHTML = document.getElementById('show').value;
			}
		}

		function switchWidget(id) {
			var widgetTr = document.getElementById('widgetInfo'+id);
			if(widgetTr.className == 'hiddenWrapper'){
				widgetTr.className = 'wrapper';
			} else {
				widgetTr.className = 'hiddenWrapper';
			}
		}

	</script>
	
</head>
	<body onload="init()" align="center">
<%@ include file="/admin/menu.jsp" %>
<table width="100%">
<tr><td align="center">
	<div id="main" style="text-align:center">
		<s:hidden id="hide" value="%{getText('mobile.action.hide')}"/>
		<s:hidden id="show" value="%{getText('mobile.action.see')}"/>
		
		<font class="title"><s:text name="widget.list.title"/></font>
		<br><br>
		<%@ include file="/WEB-INF/jsp/popup.jsp"%>
		<A href="<s:url action='module_widget'><s:param name='sort'>a</s:param></s:url>">A</A>&nbsp;
		<A href="<s:url action='module_widget'><s:param name='sort'>b</s:param></s:url>">B</A>&nbsp;
		<A href="<s:url action='module_widget'><s:param name='sort'>c</s:param></s:url>">C</A>&nbsp;
		<A href="<s:url action='module_widget'><s:param name='sort'>d</s:param></s:url>">D</A>&nbsp;
		<A href="<s:url action='module_widget'><s:param name='sort'>e</s:param></s:url>">E</A>&nbsp;
		<A href="<s:url action='module_widget'><s:param name='sort'>f</s:param></s:url>">F</A>&nbsp;
		<A href="<s:url action='module_widget'><s:param name='sort'>g</s:param></s:url>">G</A>&nbsp;
		<A href="<s:url action='module_widget'><s:param name='sort'>h</s:param></s:url>">H</A>&nbsp;
		<A href="<s:url action='module_widget'><s:param name='sort'>i</s:param></s:url>">I</A>&nbsp;
		<A href="<s:url action='module_widget'><s:param name='sort'>j</s:param></s:url>">J</A>&nbsp;
		<A href="<s:url action='module_widget'><s:param name='sort'>k</s:param></s:url>">K</A>&nbsp;
		<A href="<s:url action='module_widget'><s:param name='sort'>l</s:param></s:url>">L</A>&nbsp;
		<A href="<s:url action='module_widget'><s:param name='sort'>m</s:param></s:url>">M</A>&nbsp;
		<A href="<s:url action='module_widget'><s:param name='sort'>n</s:param></s:url>">N</A>&nbsp;
		<A href="<s:url action='module_widget'><s:param name='sort'>o</s:param></s:url>">O</A>&nbsp;
		<A href="<s:url action='module_widget'><s:param name='sort'>p</s:param></s:url>">P</A>&nbsp;
		<A href="<s:url action='module_widget'><s:param name='sort'>q</s:param></s:url>">Q</A>&nbsp;
		<A href="<s:url action='module_widget'><s:param name='sort'>r</s:param></s:url>">R</A>&nbsp;
		<A href="<s:url action='module_widget'><s:param name='sort'>s</s:param></s:url>">S</A>&nbsp;
		<A href="<s:url action='module_widget'><s:param name='sort'>t</s:param></s:url>">T</A>&nbsp;
		<A href="<s:url action='module_widget'><s:param name='sort'>u</s:param></s:url>">U</A>&nbsp;
		<A href="<s:url action='module_widget'><s:param name='sort'>v</s:param></s:url>">V</A>&nbsp;
		<A href="<s:url action='module_widget'><s:param name='sort'>w</s:param></s:url>">W</A>&nbsp;
		<A href="<s:url action='module_widget'><s:param name='sort'>x</s:param></s:url>">X</A>&nbsp;
		<A href="<s:url action='module_widget'><s:param name='sort'>y</s:param></s:url>">Y</A>&nbsp;
		<A href="<s:url action='module_widget'><s:param name='sort'>z</s:param></s:url>">Z</A>&nbsp;
		<A href="<s:url action='module_widget'/>"><s:text name="widget.list.sort.all"/></A>
		
		<div align="left" style="padding-left:10px">
			<br/>
			<table class="component" width="100%" align="center">
				<tr>
					<td style="text-align:center" class="reverse">
						
						<s:if test="%{sort == 'descend'}">
							<s:url action='module_widget' id='urlSort'>
								<s:param name='sort'>ascend</s:param>
							</s:url>
						</s:if>
						<s:else>
							<s:url action='module_widget' id='urlSort'>
								<s:param name='sort'>descend</s:param>
							</s:url>
						</s:else>
						
						<s:a href="%{urlSort}" cssClass="reverse">
							<font color="white"><s:text name="widget.list.label.name"/></font>
						</s:a>
						<s:if test="%{sort == 'ascend'}">
							<img src="../img/ascend.gif"/>
						</s:if>
						<s:if test="%{sort == 'descend'}">
							<img src="../img/descendarrow.gif"/>
						</s:if>
					</td>
					<td style="text-align:center" class="reverse">
						<s:text name="widget.list.label.version"/>
					</td>
					<td style="text-align:center" class="reverse">
						<s:text name="widget.list.label.modified"/>
					</td>
				</tr>
				<s:iterator value="widgetsMap" status="widgetMapStatus"> <!-- Iter on widgets -->
					<s:set name="currentWidget" value="key"/>
					
					<tr>
						<td class="component" style="text-align:center">
							<a href="javascript:switchWidget('<s:property value="key.id"/>')"/>
								<s:property value="key.name"/>
							</a>
						</td>
						<td class="component" style="text-align:center">
							<s:property value="key.version"/>
						</td>
						<td class="component" style="text-align:center">
							<s:property value="key.lastModified"/>
						</td>
					</tr>
					<tr id="widgetInfo<s:property value="key.id"/>" class="hiddenWrapper">
						<td class="component" style="text-align:left" colspan="3">
							<div class="scroller">
								
								<table class="component" align="center">
								
									<tr><th colspan="5" align="center"><s:text name="widget.list.label.branches"/></th></tr>
									<tr>
										<td class="significantComponent" align="center"><s:text name="widget.list.label.branchid"/></td>
										<td class="significantComponent" align="center"><s:text name="widget.list.label.name"/></td>
										<td class="significantComponent" align="center"><s:text name="widget.list.label.mobiles"/></td>
										<td class="significantComponent" align="center"><s:text name="widget.list.label.resources"/></td>
									</tr>
									
									<s:iterator value="value"> <!-- Iter on branches -->
										
										<tr>
											<td class="component" style="text-align:center; vertical-align:top;">
												<s:property value="key.id"/>
											</td>
											<td class="component" style="text-align:center; vertical-align:top;">
												<s:property value="key.name"/>
											</td>
											<td class="notSignificantComponent" style="text-align:center; vertical-align:top;">
											    <div class="mobiles">
											    	<s:iterator value="key.mobiles"> <!-- Iter mobiles -->
											    		<s:property value="key"/><br>
											    	</s:iterator>
											    </div>
											</td>
											<td class="component" style="text-align:center; vertical-align:top;">
												<s:url id="urlRemove" action="module_remove_module">
            										<s:param name="id"><s:property value="#currentWidget.id"/>_Branch_<s:property value="key.id"/></s:param>
            										<s:param name='type'>widget</s:param>
            									</s:url>
												<a id="viewLink<s:property value="#currentWidget.id"/>;<s:property value="key.id"/>" href="javascript:switchResources('<s:property value="#currentWidget.id"/>;<s:property value="key.id"/>')"/><s:text name="widget.list.label.view"/></a>
												<s:a href="%{urlRemove}"><img src="../img/poubelle.gif" border="0" alt="<s:text name="widget.list.label.remove"/>"/></s:a>
											</td>
										</tr>
										<tr id="viewTr<s:property value="#currentWidget.id"/>;<s:property value="key.id"/>" class="hiddenWrapper">
											<td colspan="5" class="component" style="text-align:left">
												<div class="scroller">
													<ul>
														<s:iterator value="value"> <!-- Iter resources map -->
															<li><a target="_blank" href="<s:property/>"><s:property/></a></li>
														</s:iterator>
													</ul>
												</div>
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