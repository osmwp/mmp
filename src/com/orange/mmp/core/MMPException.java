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
package com.orange.mmp.core;

/**
 * Superclass of all MMP exception
 * 
 * @author Thomas MILLET
 *
 */
@SuppressWarnings("serial")
public class MMPException extends Exception {

	/**
	 * Default constructor
	 */
	public MMPException() {

	}

	/**
	 * Constructor with enclosed message 
	 * 
	 * @param message
	 */
	public MMPException(String message) {
		super(message);
	}

	/**
	 * Constructor with enclosed root cause
	 * 
	 * @param cause
	 */
	public MMPException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with enclosed message and root cause
	 * 
	 * @param message
	 * @param cause
	 */
	public MMPException(String message, Throwable cause) {
		super(message, cause);
	}

}
