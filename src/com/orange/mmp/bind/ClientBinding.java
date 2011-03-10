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
package com.orange.mmp.bind;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;

import com.orange.mmp.core.Constants;
import com.orange.mmp.net.Connection;
import com.orange.mmp.net.ConnectionManagerFactory;
import com.orange.mmp.net.MMPNetException;
import com.orange.mmp.net.http.HttpConnectionParameters;

/**
 * Superclass for remote data binding (through HTTP)
 * 
 * @author nmtv3386
 *
 */
public abstract class ClientBinding {

    /**
     * The WebService endpoint (see WS Addressing core)
     */
    protected String epr;

    /**
     * The connection timeout in seconds
     */
    protected int timeout;

    /**
     * HTTP connection hander
     */
    protected Connection httpConnection;

    /**
	 * Contains the parameters of the interface
	 */
	protected HashMap<String, String> parameters;
	    
	/**
     * Adds a parameter to WebService request
     * @param name The name of the parameter
     * @param value The value of the parameter
     */
    public void setParameter(String name, String value){
    	if(this.parameters == null) this.parameters = new HashMap<String, String>();
    	this.parameters.put(name, value);
    }

    /**
     * Remove a parameter from WebService request
     * @param name The name of the parameter
     */
    public void removeParameter(String name){
    	if(this.parameters != null){
    		this.parameters.remove(name);
    	}
    }

    /**
     * Remove all parameters of WebService request
     */
    public void clearParameters(){
    	if(this.parameters != null){
    		this.parameters.clear();
    	}
    }
    
    /**
     * Set the HTTP method of the client 
     * 
     * @param method The HTTP method to use (PUT, GET, POST)
     * @throws MMPNetException
     */
    public void setMethod(String method) throws MMPNetException{
    	if(method.equalsIgnoreCase(HttpConnectionParameters.HTTP_METHOD_GET)){
    		this.getHttpConnection().setProperty(HttpConnectionParameters.PARAM_IN_HTTP_METHOD, HttpConnectionParameters.HTTP_METHOD_GET);
    	}
    	else if(method.equalsIgnoreCase(HttpConnectionParameters.HTTP_METHOD_POST)){
    		this.getHttpConnection().setProperty(HttpConnectionParameters.PARAM_IN_HTTP_METHOD, HttpConnectionParameters.HTTP_METHOD_POST);
    	}
    	else if(method.equalsIgnoreCase(HttpConnectionParameters.HTTP_METHOD_PUT)){
    		this.getHttpConnection().setProperty(HttpConnectionParameters.PARAM_IN_HTTP_METHOD, HttpConnectionParameters.HTTP_METHOD_PUT);
    	}
    	else if(method.equalsIgnoreCase(HttpConnectionParameters.HTTP_METHOD_DEL)){
    		this.getHttpConnection().setProperty(HttpConnectionParameters.PARAM_IN_HTTP_METHOD, HttpConnectionParameters.HTTP_METHOD_DEL);
    	}
    	else throw new MMPNetException("Unsupported HTTP method '"+method+"'");
    }

    /**
     * Adds a header to WebService request
     * @param name The name of the header
     * @param value The value of the header
     * @throws MMPNetException 
     */
    public void setHeader(String name, String value) throws MMPNetException{
    	this.getHttpConnection().setProperty(name, value);
    }
    
    /**
     * Set an HttpConnectionParameters in the underlying client
     * @param name The name of the property
     * @param value The value of the property
     * @throws MMPNetException 
     */
    public void setProperty(String name, Object value) throws MMPNetException{
    	this.getHttpConnection().setProperty(name, value);
    }

    /**
     * Get the HTTP status
     * @throws MMPNetException 
     */
    public int getHttpCode() throws MMPNetException{
    	return (Integer)this.getHttpConnection().getProperty(HttpConnectionParameters.PARAM_OUT_HTTP_STATUS_CODE);
    }

    /**
     * Get a response HTTP header from current request
     * @param headerName The name of the header
     * @return The value of the header
     * @throws MMPNetException 
     */
    public String getResponseHeader(String headerName) throws MMPNetException{
    	return (String)this.getHttpConnection().getProperty(headerName);
    }

    /**
     * Get the inner HTTP connection
     * @return The instance of http connection
     * @throws MMPNetException
     */
    protected Connection getHttpConnection() throws MMPNetException{
        if(this.httpConnection == null){
       		this.httpConnection = ConnectionManagerFactory.getInstance().getConnectionManager("http").getConnection();
       		this.httpConnection.setProperty(HttpConnectionParameters.PARAM_IN_HTTP_METHOD, HttpConnectionParameters.HTTP_METHOD_GET);
        }
        return this.httpConnection;
    }
    
    /**
     * Release the inner HTTP connection
     * @throws MMPNetException 
     */
    protected void releaseConnection() throws MMPNetException{
    	ConnectionManagerFactory.getInstance().getConnectionManager("http").releaseConnection(this.httpConnection);
    }
    
    /**
	 * Build the request URL
	 * @return The URL request
	 */
	protected String buildURL() throws IOException {
	    StringBuilder urlValue = new StringBuilder(this.epr);
	    if(this.parameters != null) {
			if(urlValue.toString().contains("?"))
			    urlValue.append("&");
			else
			    urlValue.append("?");
			Iterator <String>iterator = this.parameters.keySet().iterator();
			try{
			    while(iterator.hasNext()){
	    			String paramName = URLEncoder.encode(iterator.next(),Constants.DEFAULT_ENCODING);
	    			String value = URLEncoder.encode(this.parameters.get(paramName),Constants.DEFAULT_ENCODING);
	    			urlValue.append(paramName).append("=").append(value);
	    			if(iterator.hasNext()) urlValue.append("&");
			    }
	    	}catch(UnsupportedEncodingException uee){
	    		    throw new IOException(uee.getMessage());
	    	}
	    }
	    return urlValue.toString();
	}

    /**
     * @return the epr
     */
    public String getEpr() {
    	return this.epr;
    }

    /**
     * @param epr the epr to set
     */
    public void setEpr(String epr) throws MMPNetException  {
        this.epr = epr;
    }

    /**
     * @param authenticationRealm the authenticationRealm to set
     */
    public void setAuthenticationRealm(String username, String password) throws MMPNetException {
       	this.getHttpConnection().setProperty(HttpConnectionParameters.PARAM_IN_CREDENTIALS_USER, username);
       	this.getHttpConnection().setProperty(HttpConnectionParameters.PARAM_IN_CREDENTIALS_PASSWORD, password);
    }

    /**
     * @param timeout the timeout to set in seconds
     */
    public void setTimeout(int timeout) throws MMPNetException {
        this.timeout = timeout;
    }

    /**
     * Main method used to get the object bound to the request
     * @param postData The data to post (null for GET)
     * @return An abraction of the WS response in an Object
     * @throws BindingException
     */
    public abstract Object getResponse(InputStream postData) throws BindingException;
}
