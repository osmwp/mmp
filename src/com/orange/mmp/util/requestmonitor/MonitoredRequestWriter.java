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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Writer for the monitored request.
 * 
 * @author rmxc7111
 */
public abstract class MonitoredRequestWriter {

	/**
	 * Write a request on a stream.
	 * @param stream Stream
	 * @param request Request
	 */
	public void write(final OutputStream stream, final MonitoredRequest request) {
		StringBuffer buffer = new StringBuffer();
		write (buffer, request);
		try {
			stream.write(buffer.toString().getBytes());
		} catch (IOException e) {
			//Error during write buffer on stream...
		}
	}

	/**
	 * Write a request on a buffer.
	 * @param buffer Buffer
	 * @param request Request
	 */
	public abstract void write(final StringBuffer buffer, final MonitoredRequest request);

}
