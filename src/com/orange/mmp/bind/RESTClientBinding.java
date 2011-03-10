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

import com.orange.mmp.net.MMPNetException;

/**
 * Generic client for RESTFul WebService.<br>
 * Based on JAXB, this client is used to map Objects from XML response.
 * Bindings can be done from XML Schema descriptor (xsd), use of JAXB plugin for Eclipse
 * and METRO API is encouraged : see https://jax-ws.dev.java.net/ for more details.
 *
 * @author Thomas MILLET
 *
 * @param <T> The Binding CLASS
 */

public class RESTClientBinding<T> extends ClientBinding{

	/**
	 * The package in which Java object bindings can be found
	 */
	protected String bindingsPackage;

	/**
	 * Used when a specific ClassLoader for binded classes must be defined
	 */
	protected ClassLoader bindingsPackageLoader;


	/**
	 * Simple constructor used by IoC
	 */
	public RESTClientBinding(){

	}

	/**
	 * Default constructor dedicated to one interface endpoint
	 * @param epr The WebService endpoint
	 * @param bindingsPackage The package in which Java object bindings can be found
	 */
	public RESTClientBinding(String epr, String bindingsPackage){
	    this.epr = epr;
	    this.setBindingsPackage(bindingsPackage);
	}

	/**
	 * 	Sends the request to WebService and return an object built from
	 * 	XML response using JAXB API.
	 * 	@return T A binding object
	 */
	@SuppressWarnings("unchecked")
	public T getResponse(InputStream postData) throws BindingException {
	    T response=null;
	    try{
   		    try{
   		    	this.getHttpConnection().init(this.buildURL(), this.timeout);
   		    	if(postData != null) this.getHttpConnection().sendData(postData);
   		    	InputStream httpIn = this.getHttpConnection().getData();
   		    	if(httpIn != null){
   		    		response = (T)new XMLBinding().read(httpIn, bindingsPackage, this.bindingsPackageLoader);
   		    	}
   		    	else return null;
   		    }catch(UnsupportedEncodingException uee){
   		    	throw new BindingException("Unsupported encoding from "+this.epr+" : "+uee.getMessage());
   		    }catch(IOException ioe){
		    	throw new BindingException("Failed to build URL",ioe);
   		    }
	    } catch(MMPNetException mne) {
	    	throw new BindingException("Failed to get response : "+mne.getMessage());
	    } finally {
	    	try{
	    		this.releaseConnection();
	    	}catch(MMPNetException mne){
	    		//NOP - Just log
	    	}
	    }
	    return response;
	}


	/**
	 * @return the bindingsPackage
	 */
	public String getBindingsPackage() {
		return bindingsPackage;
	}

	/**
     * @param bindingsPackageLoader the bindingsPackageLoader to set
     */
    public void setBindingsPackageLoader(ClassLoader bindingsPackageLoader) {
        this.bindingsPackageLoader = bindingsPackageLoader;
    }

	/**
	 * @param bindingsPackage the bindingsPackage to set
	 */
	public void setBindingsPackage(String bindingsPackage) {
		this.bindingsPackage = bindingsPackage;
	}

}