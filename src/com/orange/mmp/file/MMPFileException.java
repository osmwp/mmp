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
package com.orange.mmp.file;

import com.orange.mmp.core.MMPException;

/**
 * Generic base exception for CUIO package
 * 
 * @author Thomas MILLET
 *
 */
@SuppressWarnings("serial")
public class MMPFileException extends MMPException {

	/**
	 * Default constructor
	 */
	public MMPFileException() {

	}

	/**
	 * Constructor with enclosed message
	 * @param message
	 */
	public MMPFileException(String message) {
		super(message);
	}

	/**
	 * Constructor with enclosed parent cause
	 * @param cause
	 */
	public MMPFileException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with enclosed message and parent cause
	 * @param message
	 * @param cause
	 */
	public MMPFileException(String message, Throwable cause) {
		super(message, cause);
	}

}
