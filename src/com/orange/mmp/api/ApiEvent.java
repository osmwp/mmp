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
package com.orange.mmp.api;

import com.orange.mmp.core.data.Api;

/**
 * Class handling API events
 * 
 * @author Thomas MILLET
 *
 */
public class ApiEvent {

	/**
	 * Event type for API addition
	 */
	public static final int API_ADDED = 0x01;
	
	/**
	 * Event type for API removal
	 */
	public static final int API_REMOVED = 0x02;
	
	/**
	 * The event type
	 */
	private int type;
	
	/**
	 * The api bound to this event
	 */
	private Api api;
	
	/**
	 * Default constructor
	 * 
	 * @param type The api event type
	 * @param api The api linked to this event
	 * @param apiContainer The API container of the service
	 */
	public ApiEvent(int type, Api api) {
		super();
		this.type = type;
		this.api = api;
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

	/**
	 * @return the api
	 */
	public Api getApi() {
		return api;
	}

	/**
	 * @param api the api to set
	 */
	public void setApi(Api api) {
		this.api = api;
	}

}
