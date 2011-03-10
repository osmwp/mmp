<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<html>
	<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="<c:url value="/css/admin.css"/>" type="text/css">
    <script type="text/javascript">

		var publicUrl;

		var privateUrl;
	    
		function initTester() {
			var tmpUrl = document.location.pathname;
			var url = tmpUrl.substring(0, tmpUrl.indexOf("/", 1));
			publicUrl = url + "/public/services/api/JSON-RPC";
			privateUrl = url + "/services/api/JSON-RPC";
			jsonurl = publicUrl;
		}

		function changed() {
			if(document.getElementById('private').checked) {
				jsonurl = privateUrl;
			} else {
				jsonurl = publicUrl;
			}
			onLoad();
		}
	    
    </script>

	<script type="text/javascript" src="<c:url value="../js/jsonrpc.js"/>"></script>
    <script type="text/javascript" src="<c:url value="../js/test.js"/>"></script>
	<title><s:text name="json.head.title"/></title>
</head>
<body onLoad="initTester();init();onLoad()">
	<%@ include file="/admin/menu.jsp" %>
	<table width="100%">
<tr><td align="center">
	<div id="main" style="text-align:center">
	<font class="title"><s:text name="json.title"/></font>
		<br><br>
		<div class="wrapper" style="text-align:center">
		    <table align="center">
		    	<tr>
		    		<td align="right">
		    			<font class="label">
		    				<s:text name="json.label.private"/>
		    			</font>
		    		</td>
		    		<td colspan="2" align="left">
		    			<input type="checkbox" id="private" onclick="javascript:changed();"/>
		    		</td>
		    	</tr>
			    <tr>
			    	<td align="right"><font class="label"><s:text name="json.label.method"/></font></td>
			    	<td align="left">
			    		<select id="method">
						</select>
					</td>
					<td align="left"><input type="button" value="<s:text name="json.button.refresh"/>" onclick="doListMethods();" /></td>
			    </tr>
			    <tr>
			    	<td align="right"><font class="label"><s:text name="json.label.params"/></font></td>
			   		<td align="left"><input type="text" id="params" size="40" value=""></td>
			   		<td align="left"><input type="button" value="<s:text name="json.button.call"/>" onclick="doCall();" /></td>
			   	</tr>
		    </table>
		    <textarea wrap="off" id="result" cols="80" rows="24"></textarea>
		</div>
	 </div>
	 	</td>
	</tr>
	</table>
  </body>
  
</html>