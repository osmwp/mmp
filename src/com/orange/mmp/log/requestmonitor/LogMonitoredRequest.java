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
package com.orange.mmp.log.requestmonitor;

import com.orange.mmp.log.LogManager;
import com.orange.mmp.log.LogManagerFactory;
import com.orange.mmp.log.MMPLogException;
import com.orange.mmp.util.requestmonitor.MonitoredRequest;
import com.orange.mmp.util.requestmonitor.MonitoredRequestStatus;
import com.orange.mmp.util.requestmonitor.MonitoredRequestWriter;

/**
 * @author rmxc7111
 *
 */
public class LogMonitoredRequest {

	/** Instance of logger for monitored request */ 
	private static LogMonitoredRequest instance = null;
	
	/** Writer for error */
	private MonitoredRequestWriter errorWriter = null;
	/** Writer for statistics */
	private MonitoredRequestWriter statsWriter = null;
	
	/**
	 * Constructor
	 */
	public LogMonitoredRequest() {
		if (instance == null) {
			instance = this;
		}
	}
	
	/**
	 * Print the request
	 */
	public void log(final MonitoredRequest request) {
		try {
			if (request.getStatus() != MonitoredRequestStatus.OK) {
				StringBuffer buffer = new StringBuffer();
				errorWriter.write(buffer, request);
				LogManagerFactory.getInstance().getLogManager().log("errorlogs", LogManager.LEVEL_INFO, buffer.toString());
			}
	
			StringBuffer buffer = new StringBuffer();
			statsWriter.write(buffer, request);
			LogManagerFactory.getInstance().getLogManager().log("statslogs", LogManager.LEVEL_INFO, buffer.toString());
			
		} catch (MMPLogException exception) {
		}
	}

	/** Get instance */
	public static LogMonitoredRequest getInstance() {
		return instance;
	}
	

	/**
	 * @return the errorWriter
	 */
	public MonitoredRequestWriter getErrorWriter() {
		return errorWriter;
	}

	/**
	 * @param errorWriter the errorWriter to set
	 */
	public void setErrorWriter(MonitoredRequestWriter errorWriter) {
		this.errorWriter = errorWriter;
	}

	/**
	 * @return the statsWriter
	 */
	public MonitoredRequestWriter getStatsWriter() {
		return statsWriter;
	}

	/**
	 * @param statsWriter the statsWriter to set
	 */
	public void setStatsWriter(MonitoredRequestWriter statsWriter) {
		this.statsWriter = statsWriter;
	}
}
