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
package com.orange.mmp.util.requestmonitor.impl;

import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orange.mmp.util.requestmonitor.MonitoredRequest;
import com.orange.mmp.util.requestmonitor.MonitoredRequestWriter;
import com.orange.mmp.util.requestmonitor.RequestMessage;

/**
 * Default writer for monitored request.
 * @author rmxc7111
 */
public class DefaultMonitoredRequestWriter extends MonitoredRequestWriter {
	
	/**
	 * @see com.orange.mmp.util.requestmonitor.MonitoredRequestWriter#write(java.lang.StringBuffer, com.orange.mmp.util.requestmonitor.MonitoredRequest)
	 */
	public void write(final StringBuffer buffer, final MonitoredRequest request) {
		
		buffer.append("------------------------------------------------------\n");
		buffer.append("Request: ").append(request.getName()).append("\n");
		buffer.append("------------------------------------------------------\n");

		//HTTP request information
		HttpServletRequest httpRequest = request.getRequest();
		if (httpRequest != null) {
			//Write URL
			buffer.append("HTTP Request: ").append(httpRequest.getRequestURL().toString()).append("\n");
			//buffer.append("HTTP Request: ").append(httpRequest.getRequestURI()).append("\n");
			//Write header
			buffer.append("HTTP Header: [ ");
			Enumeration<?> headersNames = httpRequest.getHeaderNames();
			boolean isFirst = true;
			for (;headersNames.hasMoreElements();) {
				String headerName  = (String)headersNames.nextElement();
				String headerValue = httpRequest.getHeader(headerName);
				if( isFirst ) { isFirst = false; }
				else { buffer.append(" / "); }
				buffer.append(headerName).append('=').append(headerValue);
			}
			buffer.append(" ]\n");
			//Write parameters
			buffer.append("HTTP Parameters: [ ");
			Enumeration<?> paramNames = httpRequest.getParameterNames();
			isFirst = true;
			for (;paramNames.hasMoreElements();) {
				String paramName  = (String)paramNames.nextElement();
				String paramValue = httpRequest.getParameter(paramName);
				if( isFirst ) { isFirst = false; }
				else { buffer.append(" , "); }
				buffer.append(paramName).append('=').append(paramValue);
			}
			buffer.append("] \n");
		}		
		
		//API call information
		String apiName = request.getApiName();
		if (apiName != null && apiName.length() > 0) {
			buffer.append("API name: ").append(apiName).append("\n");
		}
		String methodName = request.getMethodName();
		if (methodName != null && methodName.length() > 0) {
			buffer.append("Method name: ").append(methodName).append("\n");
		}
		String otherInfo = request.getInformation();
		if (otherInfo != null && otherInfo.length() > 0) {
			buffer.append("Other: ").append(otherInfo).append("\n");
		}
		
		long time = request.getTotalTime();
		if (time >= 0) {
			buffer.append("Execution time (in ms): ").append(time).append("\n");
		}

		//Status
		buffer.append("Status: ").append(request.getStatus().toString()).append("\n");
		
		//Execution messages
		if (! request.getMessages().isEmpty()) {
			buffer.append("Execution messages: ").append("\n");
			final List<RequestMessage> messages = request.getMessages();
			for (RequestMessage message : messages) {
				buffer.append('\t').append("Message: ").append(message.getMessage()).append("\n");
				buffer.append('\t').append("- Status: ").append(message.getStatus().toString()).append('\n');
				Exception exception = message.getException();
				if (exception != null) {
					buffer.append('\t').append("- Exception: ");
					writeThrowable(buffer, exception, "\t\t", 7);
				}
			}
		}
		
		buffer.append("------------------------------------------------------\n");

	}
	
	/**
	 * Write a throwable object (for example, an Exception).
	 * @param buffer Buffer for writing thowable object.
	 * @param throwable Thowable object
	 * @param indetation Indentation
	 * @param stackSize Line number for the stack
	 */
	public void writeThrowable(final StringBuffer buffer, final Throwable throwable, final String indetation, final int stackSize) {
		//Process exception
		if (throwable != null) {
			//Display exception (class + message)
			buffer.append(throwable.toString()).append('\n');
			//Display stack trace
			StackTraceElement[] stack = throwable.getStackTrace();
			for (int index = 0; index < stack.length && index < stackSize; index++) {
				StackTraceElement elt = stack[index];
				buffer.append(indetation).append("at ").append(elt.toString()).append('\n');
			}
			buffer.append(indetation).append("...\n");
			//Display cause exception
			if (throwable.getCause() != null) {
				buffer.append(indetation).append("Cause: ");
				writeThrowable(buffer,throwable.getCause(),indetation, 5);
			}
		}
	}

}
