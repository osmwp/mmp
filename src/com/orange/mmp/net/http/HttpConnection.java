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
package com.orange.mmp.net.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpHost;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;

import com.orange.mmp.net.Connection;
import com.orange.mmp.net.MMPNetException;

/**
 * Implementation of Connection for HTTP/HTTPS
 * 
 * @author Thomas MILLET
 *
 */
public class HttpConnection implements Connection {
		
	/**
	 * The implementation of the HTTP client
	 */
	protected org.apache.commons.httpclient.HttpClient currentHttpClient;
	
	/**
	 * The default timeout in milliseconds
	 */
	private static final int DEFAULT_TIMEOUT = 60000;
	
	/**
	 * The connection timeout in milliseconds
	 */
	protected int timeout;
	
	/**
	 * Indicate the current HTTP status
	 */
	private int currentStatusCode = -1;
		
	/**
	 * Pointer on the input stream coming from the connection
	 */
	private InputStream inDataStream = null;
	
	/**
	 * Endpoint URL of current connection
	 */
	private URL endPointUrl = null;
	
	/**
	 * Store properties of HttpConnection
	 */
	@SuppressWarnings("unchecked")
	private Map httpConnectionProperties = null;
	
	/**
	 * The HTTP method impl.
	 */
	protected HttpMethodBase method = null;
	
  	/**
	 * Proxy host configuration
	 */
	protected static String proxyHost = null;

	/**
	 * Proxy port configuration
	 */
	protected static int proxyPort = 80;

	/**
	 * Indicates if the HTTP client must pass through defined proxy
	 */
	protected boolean useProxy = true;
	
	/**
	 * Default constructor
	 * 
	 * @param httpClient The bound HttpClient
	 */
	public HttpConnection(HttpClient httpClient) {
		this.currentHttpClient = httpClient;
		this.httpConnectionProperties = new HashedMap();
	}
	
	public void close() throws MMPNetException {
		try {
			if(this.method != null) {
				this.method.releaseConnection();
				this.currentHttpClient.getHttpConnectionManager().closeIdleConnections(-1000);
				HttpConnectionManager.httpClientPool.offer(this.currentHttpClient);
			}
			if(this.inDataStream != null) {
				this.inDataStream.close();
			}
		} catch(IOException ioe) {
			throw new MMPNetException(ioe);
		}		
	}

	public void init(String endPoint, int timeout) throws MMPNetException {
		this.close();
		//Build the endpoint URL
		try{
			this.endPointUrl = new URL(endPoint);
		}catch(MalformedURLException mue){
			throw new MMPNetException(mue);
		}
		//Set timeout
		if(timeout > 0) {
			this.currentHttpClient.getParams().setParameter("http.connection.timeout", timeout*1000);
			this.currentHttpClient.getParams().setParameter("http.socket.timeout", timeout*1000);
		} else {
			this.currentHttpClient.getParams().setParameter("http.connection.timeout", DEFAULT_TIMEOUT);
			this.currentHttpClient.getParams().setParameter("http.socket.timeout", DEFAULT_TIMEOUT);
		}
	    
	}

	public InputStream getData() throws MMPNetException {
		//Execute REQUEST
		if(this.inDataStream == null)
			this.doExecute(null);
			
		//Get Response
		if(this.inDataStream != null) {
			return this.inDataStream;
		}
		else throw new MMPNetException("no HTTP response found");
	}

	public Object getProperty(String name) throws MMPNetException {
		//Seek in Connection Properties
		Object property = this.httpConnectionProperties.get(name.toLowerCase());
		if(property == null)
			property = this.httpConnectionProperties.get(name);
		//Not found, Seek in HTTPResponse
		if(property == null){
			//HTTP Status code
			if(name.equals(HttpConnectionParameters.PARAM_OUT_HTTP_STATUS_CODE)){
				return this.currentStatusCode;
			}
			else return null;
		}
		else return property;
	}

	@SuppressWarnings("unchecked")
	public void sendData(InputStream dataStream) throws MMPNetException {	
		//Execute REQUEST
		if(!this.httpConnectionProperties.containsKey(HttpConnectionParameters.PARAM_IN_HTTP_METHOD)){
			this.httpConnectionProperties.put(HttpConnectionParameters.PARAM_IN_HTTP_METHOD, HttpConnectionParameters.HTTP_METHOD_POST);
		}
		this.doExecute(dataStream);
	}

	@SuppressWarnings("unchecked")
	public void setProperty(String name, Object value) throws MMPNetException {
		this.httpConnectionProperties.put(name.toLowerCase(), value);
	}
		
	/**
	 * Inner method used to execute request
	 * 
	 * @param dataStream The data stream to send in request (null for GET only)
	 * @throws MMPNetException 
	 */
	@SuppressWarnings("unchecked")
	protected void doExecute(InputStream dataStream) throws MMPNetException {
	    try{
			this.currentHttpClient = HttpConnectionManager.httpClientPool.take();
		} catch(InterruptedException ie) {
			throw new MMPNetException("Corrupted HTTP client pool",ie);
		}

		if(this.httpConnectionProperties.containsKey(HttpConnectionParameters.PARAM_IN_CREDENTIALS_USER)){
			this.currentHttpClient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials((String)this.httpConnectionProperties.get(HttpConnectionParameters.PARAM_IN_CREDENTIALS_USER)
			,(String)this.httpConnectionProperties.get(HttpConnectionParameters.PARAM_IN_CREDENTIALS_PASSWORD)));
			this.currentHttpClient.getParams().setAuthenticationPreemptive(true);
		}

		// Config
		HostConfiguration config = new HostConfiguration();
	    if(this.timeout > 0)
	    	this.currentHttpClient.getParams().setParameter(HttpConnectionParameters.PARAM_IN_HTTP_SOCKET_TIMEOUT, this.timeout);
	    if(HttpConnectionManager.proxyHost != null &&
	    		(this.httpConnectionProperties.get(HttpConnectionParameters.PARAM_IN_HTTP_USE_PROXY) != null
	    		&& this.httpConnectionProperties.get(HttpConnectionParameters.PARAM_IN_HTTP_USE_PROXY).toString().equals("true"))
	    		|| (this.httpConnectionProperties.get(HttpConnectionParameters.PARAM_IN_HTTP_USE_PROXY) == null 
	    		&& this.useProxy)) {
			config.setProxy(HttpConnectionManager.proxyHost, HttpConnectionManager.proxyPort);
	    } else {
			config.setProxyHost(null);
	    }
	    this.currentHttpClient.setHostConfiguration(config);
	    this.currentHttpClient.getHostConfiguration().setHost(new HttpHost(this.endPointUrl.getHost()));
				
	    String methodStr = (String)this.httpConnectionProperties.get(HttpConnectionParameters.PARAM_IN_HTTP_METHOD);
	    
	    if(methodStr == null || methodStr.equals(HttpConnectionParameters.HTTP_METHOD_GET)) {
	    	this.method = new GetMethod(endPointUrl.toString().replace(" ", "+"));
		} else if(methodStr.equals(HttpConnectionParameters.HTTP_METHOD_POST)) {
			this.method = new PostMethod((this.endPointUrl.getQuery() == null) ? this.endPointUrl.getPath()
					:this.endPointUrl.getPath()+"?"+endPointUrl.getQuery());
			if(dataStream != null) {
				InputStreamRequestEntity inputStreamRequestEntity = new InputStreamRequestEntity(dataStream);
				((PostMethod)this.method).setRequestEntity(inputStreamRequestEntity);
			}
		} else if(methodStr.equals(HttpConnectionParameters.HTTP_METHOD_PUT)) {
			this.method = new PutMethod((this.endPointUrl.getQuery() == null) ? this.endPointUrl.getPath()
					:this.endPointUrl.getPath()+"?"+endPointUrl.getQuery());
			if(dataStream != null) {
				InputStreamRequestEntity inputStreamRequestEntity = new InputStreamRequestEntity(dataStream);
				((PutMethod)this.method).setRequestEntity(inputStreamRequestEntity);
			}
		} else if(methodStr.equals(HttpConnectionParameters.HTTP_METHOD_DEL)) {
			this.method = new DeleteMethod((this.endPointUrl.getQuery() == null) ? this.endPointUrl.getPath()
					:this.endPointUrl.getPath()+"?"+endPointUrl.getQuery());
		}
		else throw new MMPNetException("HTTP method not supported");

		 //Add headers
	    if(this.httpConnectionProperties != null) {
	    	for(Object headerName : this.httpConnectionProperties.keySet()) {
	    		if(!((String)headerName).startsWith("http.")) {
	    			this.method.addRequestHeader((String) headerName, this.httpConnectionProperties.get(headerName).toString());
	    		}
        	}
	    }
		// Set Connection/Proxy-Connection close to avoid TIME_WAIT sockets
		this.method.addRequestHeader("Connection", "close");
		this.method.addRequestHeader("Proxy-Connection", "close");

		try {
			int httpCode = this.currentHttpClient.executeMethod(config, method);
			this.currentStatusCode = httpCode;
			
			for(org.apache.commons.httpclient.Header responseHeader : method.getResponseHeaders()) {
				this.httpConnectionProperties.put(responseHeader.getName(), responseHeader.getValue());
			}

			if(this.currentStatusCode >= 400) {
				throw new MMPNetException("HTTP "+this.currentStatusCode+ " on '"+endPointUrl+"'");
			} else {
				this.inDataStream = this.method.getResponseBodyAsStream();
			}
		} catch(IOException ioe) {
			throw new MMPNetException("I/O error on "+this.endPointUrl+" : "+ioe.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @return the useProxy
	 */
	public boolean isUseProxy() {
	    return useProxy;
	}

	/**
	 * @param useProxy the useProxy to set
	 */
	public void setUseProxy(boolean useProxy) {
	    this.useProxy = useProxy;
	}
	
}
