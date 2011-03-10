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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;

import com.orange.mmp.core.MMPException;

/**
 * Response Context Accessor for widgets and modules
 * 
 * TODO Must be serializable for Messaging purpose
 * 
 * @author tml
 *
 */
public class ResponseContext implements HttpServletResponse{
	
	/**
	 * Inner pointer on the HttpServletRequest
	 */
	transient protected HttpServletResponse httpServletResponse;
	
	/**
	 * Inner status code
	 */
	private int statusCode = HttpServletResponse.SC_OK;
	
	/**
	 * Inner status message
	 */
	private String statusMessage;
	
	/**
	 * Default constructor mapping HttpServletResponse
	 * 
	 * @param httpServletResponse The source HttpServletResponse
	 */
	public ResponseContext(HttpServletResponse httpServletResponse){
		this.httpServletResponse = httpServletResponse;
	}
	
	/**
	 * Set a specific header (replace it if exists)
	 * 
	 * @param name The header name
	 * @param value The header value
	 */
	public void setHeader(String name, String value){
		this.httpServletResponse.setHeader(name, value);
	}
	
	/**
	 * Add a specific header (multiple values are allowed)
	 * 
	 * @param name The header name
	 * @param value The header value
	 */
	public void addHeader(String name, String value){
		this.httpServletResponse.addHeader(name, value);
	}
	
	/**
	 * Check if response contains specified header
	 * 
	 * @param name The header name
	 */
	public boolean containsHeader(String name){
		return this.httpServletResponse.containsHeader(name);
	}
	
	/**
	 * Sends temporary redirection
	 * 
	 * @param location The redirect location
	 * @throws MMPException
	 */
	public void sendRedirect(String location) throws IOException{
		this.httpServletResponse.sendRedirect(location);
	}
	
	/**
	 * Set response error
	 * 
	 * @param statusCode The response error status code
	 */
	public void sendError(int statusCode){
		this.statusCode = statusCode;
		this.statusMessage = statusCode+ " HTTP Error";
	}
	
	/**
	 * Set response error and error message
	 * 
	 * @param statusCode The response error status code
	 * @param message The response error message
	 */
	public void sendError(int statusCode, String message){
		this.statusCode = statusCode;
		this.statusMessage = message;
	}
	
	/**
	 * Sets the response status code
	 * 
	 * @param statusCode The response status code
	 */
	public void setStatus(int statusCode){
		this.statusCode = statusCode;
	}

	/**
	 * @return the statusCode
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * @return the statusMessage
	 */
	public String getStatusMessage() {
		return statusMessage;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#flushBuffer()
	 */
	public void flushBuffer() throws IOException {
		this.httpServletResponse.flushBuffer();		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getBufferSize()
	 */
	public int getBufferSize() {
		return this.httpServletResponse.getBufferSize();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		return this.httpServletResponse.getCharacterEncoding();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getContentType()
	 */
	public String getContentType() {
		return this.httpServletResponse.getContentType();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getLocale()
	 */
	public Locale getLocale() {
		return this.httpServletResponse.getLocale();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getOutputStream()
	 */
	public ServletOutputStream getOutputStream() throws IOException {
		return this.httpServletResponse.getOutputStream();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getWriter()
	 */
	public PrintWriter getWriter() throws IOException {
		return this.httpServletResponse.getWriter();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#isCommitted()
	 */
	public boolean isCommitted() {
		return this.httpServletResponse.isCommitted();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#reset()
	 */
	public void reset() {
		this.httpServletResponse.reset();		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#resetBuffer()
	 */
	public void resetBuffer() {
		this.httpServletResponse.resetBuffer();		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setBufferSize(int)
	 */
	public void setBufferSize(int size) {
		this.httpServletResponse.setBufferSize(size);		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String charset) {
		this.httpServletResponse.setCharacterEncoding(charset);		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentLength(int)
	 */
	public void setContentLength(int length) {
		this.httpServletResponse.setContentLength(length);		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
	 */
	public void setContentType(String type) {
		this.httpServletResponse.setContentType(type);		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale locale) {
		this.httpServletResponse.setLocale(locale);		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
	 */
	public void addCookie(Cookie cookie) {
		this.httpServletResponse.addCookie(cookie);		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
	 */
	public void addDateHeader(String name, long value) {
		this.httpServletResponse.addDateHeader(name, value);		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
	 */
	public void addIntHeader(String name, int value) {
		this.httpServletResponse.addIntHeader(name, value);		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
	 */
	@Deprecated
	public String encodeRedirectUrl(String url) {
		return this.httpServletResponse.encodeRedirectUrl(url);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
	 */
	public String encodeRedirectURL(String url) {
		return this.httpServletResponse.encodeRedirectURL(url);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
	 */
	@Deprecated
	public String encodeUrl(String url) {
		return this.httpServletResponse.encodeUrl(url);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
	 */
	public String encodeURL(String url) {
		return this.httpServletResponse.encodeURL(url);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
	 */
	public void setDateHeader(String name, long value) {
		this.httpServletResponse.setDateHeader(name, value);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
	 */
	public void setIntHeader(String name, int value) {
		this.httpServletResponse.setIntHeader(name, value);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
	 */
	@Deprecated
	public void setStatus(int code, String message) {
		this.statusCode = code;
		this.statusMessage = message;
		this.httpServletResponse.setStatus(code, message);		
	}
}
