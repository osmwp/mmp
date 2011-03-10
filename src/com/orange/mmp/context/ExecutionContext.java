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

import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orange.mmp.mvc.Constants;
import com.orange.mmp.util.requestmonitor.MonitoredRequest;
import com.orange.mmp.util.requestmonitor.MonitoredRequestManager;
import com.orange.mmp.util.requestmonitor.MonitoredRequestStatus;
import com.orange.mmp.util.requestmonitor.RequestMessage;
import com.orange.mmp.util.requestmonitor.RequestMessageStatus;

/**
 * Current request execution context.
 * @author rmxc7111
 */
public class ExecutionContext {

	/**
	 * Instance of context.
	 * Context depend of thread context...
	 */
	private static ExecutionContext instance = null;
	
	/**
	 * Create a new instance of ExecutionContext, base on HTTP request.
	 * @param httpRequest HTTP request
	 * @return The context of execution
	 */
	public static ExecutionContext getInstance() {
		if (instance == null) {
			instance = new ExecutionContext();
		}
		return instance;
	}

	/**
	 * Create a new instance of ExecutionContext, base on HTTP request.
	 * @param httpRequest HTTP request
	 * @return The context of execution
	 */
	public static ExecutionContext newInstance(final HttpServletRequest httpRequest) {
		final MonitoredRequest request;
		if (MonitoredRequestManager.getInstance().getCurrentRequest() != null) {
			//Close the previous request
			MonitoredRequestManager.getInstance().removeCurrentRequest();
		}
		//Create the new context
		request = MonitoredRequestManager.getInstance().createRequest();
		request.setRequest(httpRequest);
		request.setStatus(MonitoredRequestStatus.OK);
		request.setUserAgent(httpRequest.getHeader(Constants.HTTP_HEADER_USERAGENT));
		
		final ExecutionContext context = getInstance();
		return context;
	}

	
	/**
	 * Initialize the context.
	 */
	private ExecutionContext() {
	}
	
	/**
	 * Define the request name
	 */
	public void setName(final String name) {
		//Initialize the new context
		final MonitoredRequest request;
		request = MonitoredRequestManager.getInstance().getCurrentRequest();
		if(request != null) {
			request.setName(name);
		}
	}
	/**
	 * Define the API name
	 */
	public void setApiName(final String apiName) {
		//Initialize the new context
		final MonitoredRequest request;
		request = MonitoredRequestManager.getInstance().getCurrentRequest();
		if(request != null) {
			request.setApiName(apiName);
		}
	}
	/**
	 * Define the method name
	 */
	public void setMethodName(final String methodName) {
		//Initialize the new context
		final MonitoredRequest request;
		request = MonitoredRequestManager.getInstance().getCurrentRequest();
		if(request != null) {
			request.setMethodName(methodName);
		}
	}

	
	/**
	 * Begin the execution.
	 */
	public void executionStart() {
		final MonitoredRequest request;
		request = MonitoredRequestManager.getInstance().getCurrentRequest();
		if(request != null) {
			request.setStartTime(Calendar.getInstance().getTimeInMillis());
		}
	}
	
	/**
	 * End of the execution.
	 */
	public void executionStop() {
		final MonitoredRequest request;
		request = MonitoredRequestManager.getInstance().getCurrentRequest();
		if(request != null) {
			request.setStopTime(Calendar.getInstance().getTimeInMillis());
		}
	}
	
	/**
	 * Add a message to the current execution context
	 * @param status Message status
	 * @param message Message
	 * @param exception Exception (if exist)
	 */
	private void addMessage(final RequestMessageStatus status, String message, Exception exception) {
		final MonitoredRequest request;
		request = MonitoredRequestManager.getInstance().getCurrentRequest();
		if(request != null) {
			final RequestMessage rqstMsg;
			
			//Update previous message if is the same exception
			if (exception != null) {
				//Exception is not null
				//Get previous message
				final List<RequestMessage> msgs = request.getMessages();
				final RequestMessage prevMsg = msgs.isEmpty() ? null : msgs.get(msgs.size()-1);
				if (prevMsg != null && prevMsg.getException() == exception) {
					//Use the previous message
					rqstMsg = prevMsg;
					rqstMsg.setMessage(message);
					rqstMsg.setStatus(status);
				} else {
					//Use a new current message
					rqstMsg = new RequestMessage(status, message, exception);
					//Add the message
					request.addMessage(rqstMsg);
				}
			} else {
				//Not exception (exception = null)
				//Use a new current message
				rqstMsg = new RequestMessage(status, message);
				//Add the message
				request.addMessage(rqstMsg);
			}
			//Change request status (depend of message status)
			if ((status == RequestMessageStatus.CRITICAL || status == RequestMessageStatus.ERROR)
			&& (request.getStatus() != MonitoredRequestStatus.FAILED)) {
				request.setStatus(MonitoredRequestStatus.FAILED);
			} else if ((status == RequestMessageStatus.WARN)
			&& (request.getStatus() == MonitoredRequestStatus.OK)) {
				request.setStatus(MonitoredRequestStatus.WARN);
			} 
		} else {
			//Problem...
			//TODO trace this problem ?
		}
	}
	
	/**
	 * Add a critical message in the current execution context.
	 * @param message Message.
 	 */
	public void addCriticalMsg(final String message) {
		addMessage(RequestMessageStatus.CRITICAL, message, null);
	}
	/**
	 * Add a critical message in the current execution context.
	 * @param message Message.
	 * @param exception Exception.
	 */
	public void addCriticalMsg(final String message, final Exception exception) {
		addMessage(RequestMessageStatus.CRITICAL, message, exception);
	}

	/**
	 * Add an error message in the current execution context.
	 * @param message Message.
	 */
	public void addErrorMsg(final String message) {
		addMessage(RequestMessageStatus.ERROR, message, null);
	}
	/**
	 * Add an error message in the current execution context.
	 * @param message Message.
	 * @param exception Exception.
	 */
	public void addErrorMsg(final String message, final Exception exception) {
		addMessage(RequestMessageStatus.ERROR, message, exception);
	}

	/**
	 * Add a warning message in the current execution context.
	 * @param message Message.
	 */
	public void addWarningMsg(final String message) {
		addMessage(RequestMessageStatus.WARN, message, null);
	}
	/**
	 * Add a warning message in the current execution context.
	 * @param message Message.
	 * @param exception Exception.
	 */
	public void addWarningMsg(final String message, final Exception exception) {
		addMessage(RequestMessageStatus.WARN, message, exception);
	}

	/**
	 * Add an information message in the current execution context.
	 * @param message Message.
	 */
	public void addInfoMsg(final String message) {
		addMessage(RequestMessageStatus.INFO, message, null);
	}
	/**
	 * Add an information message in the current execution context.
	 * @param message Message.
	 * @param exception Exception.
	 */
	public void addInfoMsg(final String message, final Exception exception) {
		addMessage(RequestMessageStatus.INFO, message, exception);
	}
	
	/**
	 * Add a debug message in the current execution context.
	 * @param message Message.
	 */
	public void addDebugMsg(final String message) {
		addMessage(RequestMessageStatus.DEBUG, message, null);
	}
	/**
	 * Add a debug message in the current execution context.
	 * @param message Message.
	 * @param exception Exception.
	 */
	public void addDebugMsg(final String message, final Exception exception) {
		addMessage(RequestMessageStatus.DEBUG, message, exception);
	}
	
	/**
	 * Is request failed ?
	 * @return Is failed ?
	 */
	public boolean isFailed() {
		final MonitoredRequest request;
		request = MonitoredRequestManager.getInstance().getCurrentRequest();
		if(request != null) {
			return request.getStatus() == MonitoredRequestStatus.FAILED;
		} else {
			return true; //TODO How to process this case ?
		}
	}
	/**
	 * Request has a problem (warning) ?
	 * @return A problem exist ?
	 */
	public boolean isWarn() {
		final MonitoredRequest request;
		request = MonitoredRequestManager.getInstance().getCurrentRequest();
		if(request != null) {
			return request.getStatus() == MonitoredRequestStatus.WARN;
		} else {
			return true; //TODO How to process this case ?
		}
	}
	/**
	 * Request is OK ?
	 * @return No problem ?
	 */
	public boolean isOk() {
		final MonitoredRequest request;
		request = MonitoredRequestManager.getInstance().getCurrentRequest();
		if(request != null) {
			return request.getStatus() == MonitoredRequestStatus.OK;
		} else {
			return true; //TODO How to process this case ?
		}
	}

	/**
	 * Close execution context (remove MonitoredRequest)
	 */
	public void close() {
		MonitoredRequestManager.getInstance().removeCurrentRequest();
	}

	
	
}
