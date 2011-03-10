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

import com.orange.mmp.core.data.Message;
import com.orange.mmp.core.data.Api;


/**
 * A Message Subclass used to implement a Remote Service Initialization 
 * 
 * @author Thomas MILLET
 *
 */
@SuppressWarnings("serial")
public class ServiceRegistrationMessage extends Message {

	/**
	 * Message type to indicate a service request to get all available services
	 */
	public static final int GET = 8;
	
	/**
	 * Message type to indicate a service request to set new a service on service bus
	 */
	public static final int SET = 16;
	
	/**
	 * Message type to indicate a service request to remove a service on service bus
	 */
	public static final int DEL = 32;

	/**
	 * Contains the message type
	 */
	private int type;
	
	/**
	 * Default constructor to set a new service of service bus
	 */
	public ServiceRegistrationMessage(Api service, int type){
		super();
		this.setType(type);
		this.setService(service);
	}
	
	
	/**
	 * A Service instance
	 *
	 * @param service A Service instance
	 */
	public void setService(Api service){
		this.setData(service);
	}

	/**
	 * Get the Service instance
	 * 
	 * @return A Service instance
	 */
	@SuppressWarnings("unchecked")
	public Api getService(){
		return (Api)this.getData();
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
