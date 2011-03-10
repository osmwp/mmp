<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Request headers</title>
</head>
<body>
<%
    out.println("QUERY <b>" + request.getMethod() + "</b>: " + request.getRequestURL() + "<br/>");
    java.text.DateFormat format = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    javax.servlet.http.Cookie[] cookies = request.getCookies();
    if(cookies != null){
	    for( javax.servlet.http.Cookie cookie : cookies ) {
	    	boolean nodata = 
	    		cookie.getDomain() == null &&
	    		cookie.getMaxAge() == -1 &&
	    		cookie.getPath() == null &&
	    		cookie.getComment() == null;
	    	
	    	out.println("COOKIE <b>" + cookie.getName() + "</b>: " + cookie.getValue());
	    	if(! nodata)
	    		out.println(" (" +
	    			(cookie.getDomain() == null ? "" : ", domain:" + cookie.getDomain()) +
	                (cookie.getMaxAge() == -1 ? "" : ", maxAge:" + format.format(new java.util.Date(cookie.getMaxAge()*1000))) +
	                (cookie.getPath() == null ? "" : ", path:" + cookie.getPath()) +
	                (cookie.getComment() == null ? "" : ", comment:" + cookie.getComment()) + ")");
	    	out.println("<br/>");
	    }
    }

    java.util.Enumeration names = request.getHeaderNames();
    if(names != null){
        while (names.hasMoreElements()) {
        	  String name = (String) names.nextElement();
        	  String value = request.getHeader(name);
        	  out.println("HEADER <b>" + name + "</b>: " + value + "<br/>");
     	    }
    }
    %>
</body>
</html>