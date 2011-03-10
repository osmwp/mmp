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
package com.orange.mmp.message.data;

import java.io.Serializable;

import com.orange.mmp.core.data.Message;


/**
 * A Message Subclass used to implement a Service Invocation Procedure.
 * This class encapsulate Request and Responses using a specific message type
 * and automated ID generation.
 * 
 * Response content of service is encapsulated in Message.data
 * 
 * @author Thomas MILLET
 *
 */
@SuppressWarnings("serial")
public class ServiceInvocationMessage extends Message {

	/**
	 * Service type for request
	 */
	public final static int TYPE_REQUEST = 1;
	
	/**
	 * Service type for response
	 */
	public final static int TYPE_RESPONSE = 2;
	
	/**
	 * The type of the message (request or response)
	 */
	private int type;
	
	/**
	 * The name of the invoked service
	 */
	private String serviceName;
	
	/**
	 * The name of the invoked method
	 */
	private String methodName;
	
	/**
	 * List of arguments to pass to method
	 */
	private Serializable[] args;
	
	/**
	 * Default constructor for a request
	 * 
	 * @param serviceName name of the invoked service
	 * @param methodName name of the invoked method
	 * @param args List of arguments to pass to method
	 */
	public ServiceInvocationMessage(String serviceName, String methodName, Serializable[] args){
		super();
		this.type = TYPE_REQUEST;
		this.serviceName = serviceName;
		this.methodName = methodName;
		this.args = (args == null)? new Serializable[0]:args;
	}
	
	/**
	 * set the service response
	 * 
	 * @param response The service response
	 */
	public void setResponse(Serializable response){
		this.type = TYPE_RESPONSE;
		this.setData(response);
	}
	
	/**
	 * Get the service response
	 * @return The response in a Serializable object
	 */
	public Serializable getResponse(){
		return this.getData();
	}
	
	/**
	 * @return the serviceName
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * @param serviceName the serviceName to set
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	/**
	 * @return the methodName
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @param methodName the methodName to set
	 */
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	/**
	 * @return the args
	 */
	public Serializable[] getArgs() {
		return args;
	}

	/**
	 * @param args the args to set
	 */
	public void setArgs(Serializable[] args) {
		this.args = (args == null)? new Serializable[0]:args;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}
}
