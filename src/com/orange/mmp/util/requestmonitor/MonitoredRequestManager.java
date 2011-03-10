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
package com.orange.mmp.util.requestmonitor;

/**
 * Manage the monitor context for the requestS.
 * @author rmxc7111
 */
public class MonitoredRequestManager {

	/** Instance of manager */
	private static MonitoredRequestManager instance = null;
	/**
	 * Get manager instance
	 * @return The manager instance
	 */
	public static MonitoredRequestManager getInstance() {
		if(instance == null) {
			instance = new MonitoredRequestManager();
		}
		return instance;
	}
	
	/**
	 * Thread local to get the request in the current thread 
	 */
	private static final ThreadLocal<MonitoredRequest> threadLocal = new ThreadLocal<MonitoredRequest>() {
		/**
		 * Define the initial value. by default, value is null.
		 * @see java.lang.ThreadLocal#initialValue()
		 */
		protected MonitoredRequest initialValue() {
			return null;
		}
	};

	
	/**
	 * Create a context for the current request
	 */
	public MonitoredRequest createRequest() {
		final MonitoredRequest request = new MonitoredRequest();
		threadLocal.set(request);
		return request;
	}
	
	/**
	 * Get the the current request
	 */
	public MonitoredRequest getCurrentRequest() {
		final MonitoredRequest request;
		request = threadLocal.get();
		return request;
	}
	
	/**
	 * Remove the the current request
	 */
	public MonitoredRequest removeCurrentRequest() {
		final MonitoredRequest request;
		request = threadLocal.get();
		threadLocal.set(null);
		return request;
	}
	
}
