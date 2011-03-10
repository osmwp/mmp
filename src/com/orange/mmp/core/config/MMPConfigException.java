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
package com.orange.mmp.core.config;

/**
 * Generic Exception for configuration loading errors
 * 
 * @author tml
 *
 */
@SuppressWarnings("serial")
public class MMPConfigException extends Exception {

	/**
	 * Default constructor
	 */
	public MMPConfigException() {
		super();
	}

	/**
	 * Constructor using error message
	 * 
	 * @param message The error message
	 */
	public MMPConfigException(String message) {
		super(message);
	}

	/**
	 * Constructor using error cause
	 * 
	 * @param cause The error cause 
	 */
	public MMPConfigException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor using error message and error cause
	 * 
	 * @param message The error message
	 * @param cause The error cause
	 */
	public MMPConfigException(String message, Throwable cause) {
		super(message, cause);
	}

}
