<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<html>
	<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="<c:url value="/css/admin.css"/>" type="text/css">
	<title><s:text name="session.head.title" /></title>
</head>
<body onload="init()" align="center">
<%@ include file="/admin/menu.jsp" %>
    <table width="100%">
<tr><td align="center">
    <div id="main" >
		<div align="center">
			<font class="title"><s:text name="session.title" /></font>
			<%@ include file="/WEB-INF/jsp/popup.jsp"%>
        	<s:form method="post" action="session" namespace="/struts2" theme="css_xhtml">
           		<table class="component">
            		<tr>
                		<th style="text-align:left"><s:text name="session.parameters.title" /></th>
            		</tr>
            		<tr>
                		<td class="component" align="center">
    	            		<center><s:textfield cssClass="component" key="session.parameters.endpoint" value="%{ params.pfsUrlEndpoint } " labelposition="left" cssStyle="width:300px;" name="endPoint"/></center>
    	            		<br>
    	            		<s:submit align="center" cssClass="fieldButton" method="modifyParam" key="global.submit"/>
			    		</td>
					</tr>
        		</table>
        		
        		<br/>
        		<table class="component">
            		<tr>
                		<th style="text-align:left"><s:text name="session.midlet.title" /></th>
            		</tr>
            		<tr>
                		<td class="component">
                    		<font class="text"><a href="<s:url action="ota"/>"><s:text name="session.midlet.list" /></a> <s:text name="session.midlet.and" /> <a href="<s:url action="ota_upload"/>"> <s:text name="session.midlet.add" /></a> <s:text name="session.midlet.txt1" /></font>
                    		<br/><br/>
                    		<font class="text"><s:text name="session.midlet.txt2" /></font>
                		</td>
            		</tr>
        		</table>
        		
        		<br/>
        		<table class="component">
            		<tr>
                		<th style="text-align:left"><s:text name="session.mobile.title" /></th>
            		</tr>
		            <tr>
		                <td class="component">
		                    <font class="text"><a href="<s:url action="mobile_input"/>"/><s:text name="session.mobile.list" /></a> <s:text name="session.mobile.txt1" /></font>
		                <br/><br/>
		                <font class="text"><s:text name="session.mobile.txt2" /></font>
		                <ul class="text">
		                    <li><b><s:text name="session.mobile.key" /> </b><s:text name="session.mobile.key.descr" /></li>
		                    <li><b><s:text name="session.mobile.ua" /> </b><s:text name="session.mobile.ua.descr" /></li>
		                    <li><b><s:text name="session.mobile.style" /> </b><s:text name="session.mobile.style.descr" /></li>
		                    <li><b><s:text name="session.mobile.midlettype" /> </b><s:text name="session.mobile.midlettype.descr" /></li>
		                </ul>
		                </td>
		            </tr>
		        </table>
        		
        	</s:form>
        </div>
    </div>
	</td>
	</tr>
	</table>
</body>
</html>