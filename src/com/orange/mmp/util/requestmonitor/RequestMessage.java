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
 * A message for the request. 
 * @author rmxc7111
 */
public class RequestMessage {

	/** Message status */
	private RequestMessageStatus status = RequestMessageStatus.DEBUG;
	/** Message */
	private String message = null;
	/** Exception (for error/warning/critical message) */
	private Exception exception = null;
	
	/**
	 * Constructor (DEBUG, empty message and no exception).
	 */
	public RequestMessage() {
		this(RequestMessageStatus.DEBUG, "", null);
	}
	/**
	 * Constructor for an exception message (ERROR status).
	 * @param exception The exception (if error).
	 */
	public RequestMessage(final Exception exception) {
		this(RequestMessageStatus.ERROR, exception.getMessage(), exception);
	}
	/**
	 * Constructor for an exception message.
	 * @param status The message status.
	 * @param exception The exception (if error).
	 */
	public RequestMessage(final RequestMessageStatus status, final Exception exception) {
		this(status, exception.getMessage(), exception);
	}
	/**
	 * Constructor.
	 * @param status The message status.
	 * @param message The message content.
	 */
	public RequestMessage(final RequestMessageStatus status, final String message) {
		this(status, message, null);
	}
	/**
	 * Constructor for an exception message (ERROR status).
	 * @param message The message content.
	 * @param exception The exception (if error).
	 */
	public RequestMessage(final String message, final Exception exception) {
		this(RequestMessageStatus.ERROR, message, exception);
	}
	/**
	 * Constructor.
	 * @param status The message status.
	 * @param message The message content.
	 * @param exception The exception (if error).
	 */
	public RequestMessage(final RequestMessageStatus status, final String message,
			final Exception exception) {
		super();
		this.status = status;
		this.message = message;
		this.exception = exception;
	}
	
	/**
	 * Get the status message.
	 * @return The status message.
	 */
	public RequestMessageStatus getStatus() {
		return status;
	}
	/**
	 * Change the status message.
	 * @param status The status message. 
	 */
	public void setStatus(final RequestMessageStatus status) {
		this.status = status;
	}
	
	/**
	 * Get the message.
	 * @return The message.
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * Define the message. 
	 * @param message The message.
	 */
	public void setMessage(final String message) {
		this.message = message;
	}
	
	/**
	 * Get the message exception.
	 * @return The exception.
	 */
	public Exception getException() {
		return exception;
	}
	/**
	 * Define the exception.
	 * @param exception The exception.
	 */
	public void setException(final Exception exception) {
		this.exception = exception;
	}
	
	
}
