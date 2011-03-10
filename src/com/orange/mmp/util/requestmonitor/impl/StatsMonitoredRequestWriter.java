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

import javax.servlet.http.HttpServletRequest;

import com.orange.mmp.util.requestmonitor.MonitoredRequest;
import com.orange.mmp.util.requestmonitor.MonitoredRequestWriter;

/**
 * @author rmxc7111
 *
 */
public class StatsMonitoredRequestWriter extends MonitoredRequestWriter {

	/**
	 * @see com.orange.mmp.util.requestmonitor.MonitoredRequestWriter#write(java.lang.StringBuffer, com.orange.mmp.util.requestmonitor.MonitoredRequest)
	 */
	public void write(final StringBuffer buffer, final MonitoredRequest request) {
		
		buffer.append("\"").append(request.getName()).append("\";");
		
		//Time
		long time = request.getTotalTime();
		if (time >= 0) {
			buffer.append("\"").append(time).append("ms\";");
		}
		//Status
		buffer.append("\"").append(request.getStatus().toString()).append("\";");

		
		//HTTP request information
		HttpServletRequest httpRequest = request.getRequest();
		if (httpRequest != null) {
			//Write URL
			buffer.append("\"").append(httpRequest.getRequestURL().toString()).append("\";");
			//Write header
			buffer.append("\"{");
			Enumeration<?> headersNames = httpRequest.getHeaderNames();
			boolean isFirst = true;
			for (;headersNames.hasMoreElements();) {
				String headerName  = (String)headersNames.nextElement();
				String headerValue = httpRequest.getHeader(headerName);
				if( isFirst ) { isFirst = false; }
				else { buffer.append(" / "); }
				buffer.append(headerName).append('=').append(headerValue);
			}
			buffer.append("}\";");
			//Write parameters
			buffer.append("\"{");
			Enumeration<?> paramNames = httpRequest.getParameterNames();
			isFirst = true;
			for (;paramNames.hasMoreElements();) {
				String paramName  = (String)paramNames.nextElement();
				String paramValue = httpRequest.getParameter(paramName);
				if( isFirst ) { isFirst = false; }
				else { buffer.append(" , "); }
				buffer.append(paramName).append('=').append(paramValue);
			}
			buffer.append("}\";");
		}		
		
		
		//API call information
		String apiName = request.getApiName();
		if (apiName != null && apiName.length() > 0) {
			buffer.append("\"").append(apiName).append("\";");
		}
		String methodName = request.getMethodName();
		if (methodName != null && methodName.length() > 0) {
			buffer.append("\"").append(methodName).append("\";");
		}

		String otherInfo = request.getInformation();
		if (otherInfo != null && otherInfo.length() > 0) {
			buffer.append("\"").append(otherInfo).append("\";");
		}
	}
}
