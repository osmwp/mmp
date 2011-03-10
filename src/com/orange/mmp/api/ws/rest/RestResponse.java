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
package com.orange.mmp.api.ws.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import com.orange.mmp.api.MMPApiException;
import com.orange.mmp.bind.BindingException;
import com.orange.mmp.bind.XMLBinding;
import com.orange.mmp.context.RequestContext;
import com.orange.mmp.context.ResponseContext;
import com.orange.mmp.core.Constants;
import com.orange.mmp.core.MMPException;

/**
 * Abstraction class of a REST Response
 * 
 * @author tml
 *
 */
public class RestResponse {

	/**
	 * Indicate a return type HTTP
	 */
	public static final String RETURN_TYPE_HTTP = "http";
	
	/**
	 * Indicate a return type BODY
	 */
	public static final String RETURN_TYPE_BODY = "body";
	
	/**
	 * The default error code
	 */
	private static final int DEFAULT_ERROR_CODE = 500;
	
	/**
	 * The default error message
	 */
	private static final String DEFAULT_ERROR_MESSAGE = "Unknown Internal Error";
	
	/**
	 * The maximum level in error stack trace to search for error message
	 */
	private static final int MAX_RECURSIVE_SEARCH = 10;
	
	/**
	 * Contains the current request context
	 */
	private RequestContext requestContext = null;
	
	/**
	 * Contains the current response context
	 */
	private ResponseContext responseContext = null;
	
	/**
	 * Private constructor  
	 */
	private RestResponse(){
		//NOP
	}
	
	/**
	 * Get an instance of RestResponse from specified context
	 * 
	 * @param request The request context
	 * @param response The response context
	 * @return A new instance of RestResponse
	 * @throws MMPException
	 */
	public static RestResponse newInstance(RequestContext request, ResponseContext response) throws MMPException{
		RestResponse restResponse = new RestResponse();
		restResponse.requestContext = request;
		restResponse.responseContext = response;
		return restResponse;
	}
	
	/**
	 * Send the Rest Response based on the response Object and the HttpServletResponse 
	 * 
	 * @param rootElement The XML root element name of the response
	 * @param responseClass The Class of the response Object
	 * @param responseObject The response object
	 * @param errorType Service error type (body or http)
	 * @param httpServletResponse The HttpServletResponse to use to send response
	 * @throws MMPException
	 */
	@SuppressWarnings({ "unchecked", "deprecation" })
	protected void send(String rootElement, Class responseClass, Object responseObject, String errorType, HttpServletResponse httpServletResponse) throws MMPException{
		if(errorType == null || errorType.length() == 0) errorType = RETURN_TYPE_BODY;
		
		//Specific status code handling
		if(this.responseContext.getStatusCode() >= 300){
			try {
				if(errorType.equals(RETURN_TYPE_HTTP)) {
					httpServletResponse.setContentLength(0);
					httpServletResponse.setStatus(this.responseContext.getStatusCode(), this.responseContext.getStatusMessage());
				} else {
					String responseStr = "<?xml version=\"1.0\" encoding=\""+Constants.DEFAULT_ENCODING+"\"?>"
					+ 	"<error>"
					+ 		"<code>" + this.responseContext.getStatusCode() + "</code>"
					+ 	  "<message><![CDATA[" + this.responseContext.getStatusMessage() + "]]></message>"
					+ 	"</error>";
				
					
						responseContext.setContentLength(responseStr.getBytes(Constants.DEFAULT_ENCODING).length);
						responseContext.setContentType(com.orange.mmp.cadap.Constants.MIME_TYPE_XMLCONTENT);
						responseContext.setCharacterEncoding(Constants.DEFAULT_ENCODING);
						responseContext.getWriter().write(responseStr);
				}
			} catch(IOException ioe) {
				throw new MMPApiException(ioe);
			}
		}
		//Throwed Error handling
		else if(responseClass.isAssignableFrom(Throwable.class)){
			this.parseThrowable((Throwable)responseObject);
			try {
				if(errorType.equals(RETURN_TYPE_HTTP)) {
					httpServletResponse.setContentLength(0);
					httpServletResponse.sendError(this.responseContext.getStatusCode(), this.responseContext.getStatusMessage());
				} else {
					String responseStr = "<?xml version=\"1.0\" encoding=\""+Constants.DEFAULT_ENCODING+"\"?>"
					+ 	"<error>"
					+ 		"<code>" + this.responseContext.getStatusCode() + "</code>"
					+ 	  "<message><![CDATA[" + this.responseContext.getStatusMessage() + "]]></message>"
					+ 	"</error>";
				
					
						responseContext.setContentLength(responseStr.getBytes(Constants.DEFAULT_ENCODING).length);
						responseContext.setContentType(com.orange.mmp.cadap.Constants.MIME_TYPE_XMLCONTENT);
						responseContext.setCharacterEncoding(Constants.DEFAULT_ENCODING);
						responseContext.getWriter().write(responseStr);
				}
			} catch(IOException ioe) {
				throw new MMPApiException(ioe);
			}
		}
		//Void handling (hardcoded for perfomances but can be done with JAXB using null element marshalling)
		else if(responseClass.getName().equals("void")){
			String responseStr = "<?xml version=\"1.0\" encoding=\""+Constants.DEFAULT_ENCODING+"\"?>"
				+ 	"<"+rootElement+"/>";

			try{
				responseContext.setContentLength(responseStr.getBytes(Constants.DEFAULT_ENCODING).length);
				responseContext.setContentType(com.orange.mmp.cadap.Constants.MIME_TYPE_XMLCONTENT);
				responseContext.setCharacterEncoding(Constants.DEFAULT_ENCODING);
				responseContext.getWriter().write(responseStr);
			}catch(IOException ioe){
				throw new MMPApiException(ioe);
			}
		}
		//Others cases
		else{
			try{
				String ae = this.requestContext.getHeader("accept-encoding");
				boolean gzip = (ae != null && ae.indexOf("gzip") != -1);
				ByteArrayInputStream in = null;
				//If response is an array of byte[], handled as binary data transfer (download)
				if(responseObject != null && responseObject.getClass().isAssignableFrom(byte[].class) && 
						!responseObject.getClass().isAssignableFrom(String.class)){
					if(gzip){
						byte[] zippedResult = this.gzip((byte[])responseObject);
						responseContext.setContentLength(zippedResult.length);
						responseContext.addHeader("Content-Encoding", "gzip");
						in = new ByteArrayInputStream(zippedResult);
					}
					else{
						responseContext.setContentLength(((byte[])responseObject).length);
						in = new ByteArrayInputStream((byte[])responseObject);
					}
					if(responseContext.getContentType() == null){
						responseContext.setContentType(com.orange.mmp.cadap.Constants.MIME_TYPE_OCTETSTREAM);
					}
				}
				//XML Binding
				else{
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					responseContext.setContentType(com.orange.mmp.cadap.Constants.MIME_TYPE_XMLCONTENT);
					try{
						new XMLBinding().write(responseObject, out, responseClass, rootElement);
					}catch(BindingException be){
						throw new MMPException(be);
					}
					if(gzip){
						byte[] zippedResult = this.gzip(out.toByteArray());
						responseContext.setContentLength(zippedResult.length);
						responseContext.addHeader("Content-Encoding", "gzip");
						in = new ByteArrayInputStream(zippedResult);
					}
					else{
						responseContext.setContentLength(out.size());
						in = new ByteArrayInputStream(out.toByteArray());
					}
				}
				
				IOUtils.copy(in, responseContext.getOutputStream());
			}catch(IOException ioe){
				throw new MMPApiException(ioe);
			}
		}
	}
	
	/**
	 * Generate error code and error message from a Throwable
	 * 
	 * @param e Throwable instance linked to the error
	 */
	protected void parseThrowable(Throwable e){
		int currentLevel = 0;
		Throwable ref = e;
		while(currentLevel++ < MAX_RECURSIVE_SEARCH 
				&& ref.getMessage() == null && ref.getCause() != null){
			ref = ref.getCause();
		}
		this.responseContext.sendError(DEFAULT_ERROR_CODE, (ref.getMessage() == null) ? DEFAULT_ERROR_MESSAGE : ref.getMessage());
	}

	/**
	 * Gzip something.
	 *
	 * @param in original content
	 * @return size gzipped content
	 */
	private byte[] gzip(byte[] in) throws IOException{
	  if (in != null && in.length > 0){
	    ByteArrayOutputStream bout = new ByteArrayOutputStream();
	    GZIPOutputStream gout = new GZIPOutputStream(bout);
	    gout.write(in);
	    gout.flush();
	    gout.close();
	    return bout.toByteArray();
	  }
	  return new byte[0];
	}
}
