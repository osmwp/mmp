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
package com.orange.mmp.dao;

import com.orange.mmp.core.MMPException;

/**
 * Generic base exception for DAO package
 * 
 * @author Thomas MILLET
 *
 */
@SuppressWarnings("serial")
public class MMPDaoException extends MMPException {

	/**
	 * Default constructor
	 */
	public MMPDaoException() {

	}

	/**
	 * Constructor with enclosed message
	 * @param message
	 */
	public MMPDaoException(String message) {
		super(message);
	}

	/**
	 * Constructor with enclosed parent cause
	 * @param cause
	 */
	public MMPDaoException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with enclosed message and parent cause
	 * @param message
	 * @param cause
	 */
	public MMPDaoException(String message, Throwable cause) {
		super(message, cause);
	}

}
