/*
 * Copyright (C) 2010 France Telecom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orange.mmp.context;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.orange.mmp.api.MMPApiException;
import com.orange.mmp.core.MMPException;

/**
 * Request Context Accessor for widgets and modules
 * 
 * TODO Must be serializable for Messaging purpose
 * 
 * @author tml
 *
 */
public class RequestContext implements HttpServletRequest {

	/**
	 * Maximum size of uploaded file in bytes
	 */
	private static final int MAX_UPLOAD_FILESIZE =  5242880;
	
	/**
	 * Inner pointer on the HttpServletRequest
	 */
	transient private HttpServletRequest httpServletRequest;
	
	/**
	 * Inner pointer on the ServletFileUpload
	 */
	transient private ServletFileUpload servletFileUpload;
	
	/**
	 * Inner pointer on multipart items
	 */
	transient private List<FileItem> multipartItems;
	
	/**
	 * Indicates if current request is multipart
	 */
	private boolean isMultipart = false;
	
	/**
	 * Default constructor using httpServletRequest
	 * 
	 * @param httpServletRequest The original httpServletRequest
	 */
	@SuppressWarnings("unchecked")
	public RequestContext(HttpServletRequest httpServletRequest) throws MMPException{
		this.httpServletRequest = httpServletRequest;
		this.isMultipart = ServletFileUpload.isMultipartContent(httpServletRequest);
		if(this.isMultipart){
			DiskFileItemFactory factory = new DiskFileItemFactory(MAX_UPLOAD_FILESIZE,new File(System.getProperty("java.io.tmpdir")));
			servletFileUpload = new ServletFileUpload(factory);
			try{
				this.multipartItems = servletFileUpload.parseRequest(httpServletRequest);
			}catch(FileUploadException fue){
				throw new MMPApiException("Failed to parse multipart request",fue);
			}
		}		
	}
	
	/**
	 * Get an uploaded file in current request context (if multipart only) 
	 * 
	 * @return A File instance pointing on uploaded file
	 */
	public List<File> getFiles() throws MMPException{
		List<File> fileList = new ArrayList<File>();
		if(this.isMultipart 
				&& this.multipartItems != null){
			try{
				
				for(FileItem item : this.multipartItems){
					if(!item.isFormField()){
						File itemFile = new File(System.getProperty("java.io.tmpdir")+"/"+item.getName());
						item.write(itemFile);
						fileList.add(itemFile);
					}
				}	
			}catch(Exception e){
				throw new MMPException(e);
			}
		}
		return fileList;
	}
	
	/**
	 * HttpServletRequest getHeader delegation method
	 * 
	 * @param name The name of the header to get
	 * @return The value of the header
	 */
	public String getHeader(String name){
		return this.httpServletRequest.getHeader(name);
	}
	
	/**
	 * HttpServletRequest getHeaders delegation method
	 * 
	 * @param name The name of the header to get
	 * @return The values of the header
	 */
	@SuppressWarnings("unchecked")
	public Enumeration<String> getHeaders(String name){
		return this.httpServletRequest.getHeaders(name);
	}
	
	/**
	 * HttpServletRequest getHeaderNames delegation method
	 * 
	 * @return The list of availbale headers
	 */
	@SuppressWarnings("unchecked")
	public Enumeration<String> getHeaderNames(){
		return this.httpServletRequest.getHeaderNames();
	}
	
	/**
	 * HttpServletRequest getParameter delegation method
	 * 
	 * @param name The name of the parameter to get
	 * @return The value of the parameter
	 */
	public String getParameter(String name){
		if(!this.isMultipart)return this.httpServletRequest.getParameter(name);
		else if (this.multipartItems != null){
			for(FileItem item : this.multipartItems){
				if(item.isFormField() && item.getFieldName().equals(name)){
					return item.getString();
				}
			}
		}
		return null;
	}
	
	/**
	 * HttpServletRequest getParameterNames delegation method
	 * 
	 * @return The list of available parameters
	 */
	@SuppressWarnings("unchecked")
	public Enumeration<String> getParameterNames(){
		if(!this.isMultipart) return this.httpServletRequest.getParameterNames();
		else if (this.multipartItems != null){
			Vector<String> itemNames = new Vector<String>();
			for(FileItem item : this.multipartItems){
				if(item.isFormField()) itemNames.add(item.getFieldName());
			}
			return itemNames.elements();
		}
		return null;
	}
	
	/**
	 * HttpServletRequest getParameterValues delegation method
	 * 
	 * @param name The name of the parameter to get
	 * @return The values of the parameter
	 */
	public String[] getParameterValues(String name){
		if(!this.isMultipart) return this.httpServletRequest.getParameterValues(name);
		else if (this.multipartItems != null){
			List<String> itemValues = new ArrayList<String>();
			for(FileItem item : this.multipartItems){
				if(item.isFormField()) itemValues.add(item.getString());
			}
			String[] result = new String[itemValues.size()];
			return itemValues.toArray(result);
		}
		return null;
	}
	
	/**
	 * HttpServletRequest getParameterMap delegation method
	 * 
	 * @return A Map abstraction of parameters
	 */
	@SuppressWarnings("unchecked")
	public Map getParameterMap(){
		if(!this.isMultipart) return this.httpServletRequest.getParameterMap();
		else if (this.multipartItems != null){
			Map<String,String> parameterMap = new HashMap<String, String>();
			for(FileItem item : this.multipartItems){
				if(item.isFormField()) parameterMap.put(item.getFieldName(), item.getString());
			}
			return parameterMap;
		}
		return null;
	}
	
	/**
	 * HttpServletRequest getQueryString delegation method
	 * 
	 * @return The request query String
	 */
	public String getQueryString(){
		return this.httpServletRequest.getQueryString();		
	}
	
	/**
	 * HttpServletRequest getPathInfo delegation method
	 * 
	 * @return The request path info
	 */
	public String getPathInfo(){
		return this.httpServletRequest.getPathInfo();
	}
	
	/**
	 * Specific MMP method used to handle request and session contexts through
	 * an interface object UserContext. 
	 * 
	 * @param userContext The UserContext to set in current request context
	 * @param statefull Indicates if the UserContext must be stored in inner session
	 */
	public void setUserContext(UserContext userContext, boolean statefull){
		if(userContext != null){
			if(statefull){
				HttpSession session = this.httpServletRequest.getSession();
				session.setAttribute(UserContext.USER_CTX_STR_KEY, userContext);
			}
			else this.httpServletRequest.setAttribute(UserContext.USER_CTX_STR_KEY, userContext);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getAuthType()
	 */
	public String getAuthType() {
		return this.httpServletRequest.getAuthType();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getContextPath()
	 */
	public String getContextPath() {
		return this.httpServletRequest.getContextPath();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getCookies()
	 */
	public Cookie[] getCookies() {
		return this.httpServletRequest.getCookies();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
	 */
	public long getDateHeader(String name) {
		return Long.parseLong(this.getHeader(name));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
	 */
	public int getIntHeader(String name) {
		return Integer.parseInt(this.getHeader(name));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getMethod()
	 */
	public String getMethod() {
		return this.httpServletRequest.getMethod();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
	 */
	public String getPathTranslated() {
		return this.httpServletRequest.getPathTranslated();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
	 */
	public String getRemoteUser() {
		return this.httpServletRequest.getRemoteUser();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
	 */
	public String getRequestedSessionId() {
		return this.httpServletRequest.getRequestedSessionId();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURI()
	 */
	public String getRequestURI() {
		return this.httpServletRequest.getRequestURI();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURL()
	 */
	public StringBuffer getRequestURL() {
		return this.httpServletRequest.getRequestURL();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getServletPath()
	 */
	public String getServletPath() {
		return this.httpServletRequest.getServletPath();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession()
	 */
	public HttpSession getSession() {
		return this.httpServletRequest.getSession(false);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
	 */
	public HttpSession getSession(boolean create) {
		return this.httpServletRequest.getSession(false);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
	 */
	public Principal getUserPrincipal() {
		return this.httpServletRequest.getUserPrincipal();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
	 */
	public boolean isRequestedSessionIdFromCookie() {
		return this.httpServletRequest.isRequestedSessionIdFromCookie();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
	 */
	@Deprecated
	public boolean isRequestedSessionIdFromUrl() {
		return this.httpServletRequest.isRequestedSessionIdFromUrl();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
	 */
	public boolean isRequestedSessionIdFromURL() {
		return this.httpServletRequest.isRequestedSessionIdFromURL();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
	 */
	public boolean isRequestedSessionIdValid() {
		return this.httpServletRequest.isRequestedSessionIdValid();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
	 */
	public boolean isUserInRole(String role) {
		return this.httpServletRequest.isUserInRole(role);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String name) {
		return this.httpServletRequest.getAttribute(name);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttributeNames()
	 */
	@SuppressWarnings("unchecked")
	public Enumeration getAttributeNames() {
		return this.httpServletRequest.getAttributeNames();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		return this.httpServletRequest.getCharacterEncoding();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentLength()
	 */
	public int getContentLength() {
		return this.httpServletRequest.getContentLength();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentType()
	 */
	public String getContentType() {
		return this.httpServletRequest.getContentType();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getInputStream()
	 */
	public ServletInputStream getInputStream() throws IOException {
		return this.httpServletRequest.getInputStream();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalAddr()
	 */
	public String getLocalAddr() {
		return this.httpServletRequest.getLocalAddr();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocale()
	 */
	public Locale getLocale() {
		return this.httpServletRequest.getLocale();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocales()
	 */
	@SuppressWarnings("unchecked")
	public Enumeration getLocales() {
		return this.httpServletRequest.getLocales();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalName()
	 */
	public String getLocalName() {
		return this.httpServletRequest.getLocalName();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalPort()
	 */
	public int getLocalPort() {
		return this.httpServletRequest.getLocalPort();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getProtocol()
	 */
	public String getProtocol() {
		return this.httpServletRequest.getProtocol();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getReader()
	 */
	public BufferedReader getReader() throws IOException {
		return this.httpServletRequest.getReader();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
	 */
	@Deprecated
	public String getRealPath(String path) {
		return this.httpServletRequest.getRealPath(path);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteAddr()
	 */
	public String getRemoteAddr() {
		return this.httpServletRequest.getRemoteAddr();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteHost()
	 */
	public String getRemoteHost() {
		return this.httpServletRequest.getRemoteHost();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemotePort()
	 */
	public int getRemotePort() {
		return this.httpServletRequest.getRemotePort();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
	 */
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return this.httpServletRequest.getRequestDispatcher(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getScheme()
	 */
	public String getScheme() {
		return this.httpServletRequest.getScheme();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerName()
	 */
	public String getServerName() {
		return this.httpServletRequest.getServerName();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerPort()
	 */
	public int getServerPort() {
		return this.httpServletRequest.getServerPort();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#isSecure()
	 */
	public boolean isSecure() {
		return this.httpServletRequest.isSecure();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name) {
		this.httpServletRequest.removeAttribute(name);		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String name, Object value) {
		this.httpServletRequest.setAttribute(name,value);		
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String charset) throws UnsupportedEncodingException {
		this.httpServletRequest.setCharacterEncoding(charset);		
	}	
}
