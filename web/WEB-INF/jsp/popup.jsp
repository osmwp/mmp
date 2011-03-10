<script language="javascript" src="<c:url value="/js/popup.js"/>"></script>
<script language="javascript">
	if (displaymode=="oncepersession" && get_cookie("fadedin")=="" || displaymode=="always" || parseInt(displaymode)!=NaN && random_num==0){
		if (window.addEventListener)
		window.addEventListener("load", initfunction, false)
		else if (window.attachEvent)
		window.attachEvent("onload", initfunction)
		else if (document.getElementById)
		window.onload=initfunction
		document.cookie="fadedin=yes"
	}
</script>
<s:if test="hasActionErrors()">
	<script language="javascript">
		boxName = "fadeinboxError";
	</script>	
	<div id="fadeinboxError" style="filter:progid:DXImageTransform.Microsoft.RandomDissolve(duration=1) progid:DXImageTransform.Microsoft.Shadow(color=gray,direction=135) ; -moz-opacity:0">
		<table width="100%">
			<tr>
				<td><img src="../img/error-icon.png"/></td>
				<td><s:actionerror cssClass="popup"/></td>
			</tr>
			<tr>
				<td align="center" colspan="2">
				<input type="button" onclick="hidefadebox()" class="fieldButton" value="<s:text name="popup.button.close"/>"/>
				</td>
			</tr>
		</table>			
	</div>
</s:if>
<s:elseif test="hasActionMessages()">
	<script language="javascript">
		boxName = "fadeinboxSuccess";
	</script>
	<div id="fadeinboxSuccess" style="filter:progid:DXImageTransform.Microsoft.RandomDissolve(duration=1) progid:DXImageTransform.Microsoft.Shadow(color=gray,direction=135) ; -moz-opacity:0">
		<table width="100%">
			<tr>
				<td><img src="../img/success-icon.png"/></td>
				<td><s:actionmessage cssClass="popup"/></td>
			</tr>
			<tr>
				<td align="center" colspan="2">
				<input type="button" onclick="hidefadebox()" class="fieldButton" value="<s:text name="popup.button.close"/>"/>
				</td>
			</tr>
		</table>			
	</div>
</s:elseif>
	
	