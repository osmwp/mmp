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
package com.orange.mmp.message;

import com.orange.mmp.core.MMPException;

/**
 * Generic base exception for CUMOB
 * 
 * @author Thomas MILLET
 */
@SuppressWarnings("serial")
public class MMPMessageException extends MMPException {

	/**
	 *	Default constructor  
	 */
	public MMPMessageException() {

	}

	/**
	 * Constructor with enclosed message
	 * @param message
	 */
	public MMPMessageException(String message) {
		super(message);
	}

	/**
	 * Constructor with enclosed root cause
	 * @param cause
	 */
	public MMPMessageException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with enclosed message and root cause
	 * @param message
	 * @param cause
	 */
	public MMPMessageException(String message, Throwable cause) {
		super(message, cause);
	}

}
