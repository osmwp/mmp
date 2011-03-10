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
 * Base RunTime Exception for MMP
 * 
 * @author Thomas MILLET
 *
 */
@SuppressWarnings("serial")
public class MMPRuntimeException extends RuntimeException {

	/**
	 * Default constructor
	 */
	public MMPRuntimeException() {
		super();
	}

	/**
	 * Constructor with error message
	 * 
	 * @param message The error message
	 */
	public MMPRuntimeException(String message) {
		super(message);
	}

	/**
	 * Constructor with root cause
	 * 
	 * @param cause The root cause
	 */
	public MMPRuntimeException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with error message and root cause
	 * 
	 * @param message The error message
	 * @param cause The root cause
	 */
	public MMPRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

}
