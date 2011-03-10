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
package com.orange.mmp.log;

/**
 * Manager used to publish logging API to modules and services
 * 
 * @author Thomas MILLET
 *
 */
public interface LogManager {
	/**
	 * DEBUG LOG LEVEL
	 */
	public static final int LEVEL_DEBUG = 10000;
	
	/**
	 * INFO LOG LEVEL
	 */
	public static final int LEVEL_INFO = 20000;
	
	/**
	 * WARN LOG LEVEL
	 */
	public static final int LEVEL_WARN = 30000;
	
	/**
	 * ERROR LOG LEVEL
	 */
	public static final int LEVEL_ERROR = 40000;
	
	/**
	 * FATAL LOG LEVEL
	 */
	public static final int LEVEL_FATAL = 50000;
	
	/**
	 * Logs a message in current LogManager
	 * 
	 * @param key The logging key (classname, identifier ...)
	 * @param level The log level (LEVEL_DEBUG, LEVEL_INFO ...)
	 * @param message The log message
	 * @throws MMPLogException
	 */
	public void log(String key, int level, String message) throws MMPLogException;
}
