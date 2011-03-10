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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Data for the current monitored request.
 * @author rmxc7111
 */
public class MonitoredRequest {
	
	/**
	 * Request name
	 */
	private String name = null;

	/**
	 * HTTP request
	 */
	private HttpServletRequest request = null;
	/**
	 * Request API
	 */
	private String apiName = null;
	/**
	 * Request method
	 */
	private String methodName = null;
	/**
	 * Request user agent
	 */
	private String userAgent = null;

	/**
	 * Other request information
	 */
	private String information = null;
	/**
	 * Request messages
	 */
	private final List<RequestMessage> messages = new ArrayList<RequestMessage>();
	/**
	 * Request status
	 */
	private MonitoredRequestStatus status = MonitoredRequestStatus.OK;
	/**
	 * Request start time
	 */
	private long startTime = -1;
	/**
	 * Request stop time
	 */
	private long stopTime = -1;
	

	/**
	 * @return the apiName
	 */
	public String getApiName() {
		return apiName;
	}
	/**
	 * @param apiName the apiName to set
	 */
	public void setApiName(String apiName) {
		this.apiName = apiName;
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
	 * @return the userAgent
	 */
	public String getUserAgent() {
		return userAgent;
	}
	/**
	 * @param userAgent the userAgent to set
	 */
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	/**
	 * @return the information
	 */
	public String getInformation() {
		return information;
	}
	/**
	 * @param information the information to set
	 */
	public void setInformation(String information) {
		this.information = information;
	}
	/**
	 * @return the status
	 */
	public MonitoredRequestStatus getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(MonitoredRequestStatus status) {
		this.status = status;
	}
	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		return startTime;
	}
	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	/**
	 * @return the stopTime
	 */
	public long getStopTime() {
		return stopTime;
	}
	/**
	 * @param stopTime the stopTime to set
	 */
	public void setStopTime(long stopTime) {
		this.stopTime = stopTime;
	}
	/**
	 * @return Total request time (stop time - start time)
	 */
	public long getTotalTime() {
		return (stopTime == -1 || startTime == -1) ? -1 : stopTime - startTime;
	}
	/**
	 * Add a message.
	 * @param message The message.
	 */
	public void addMessage(final RequestMessage message) {
		messages.add(message);
	}
	/**
	 * @return the messages
	 */
	public List<RequestMessage> getMessages() {
		return messages;
	}
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}
	public HttpServletRequest getRequest() {
		return request;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
}
