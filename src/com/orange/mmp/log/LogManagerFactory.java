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

import java.util.Map;

import com.orange.mmp.core.Constants;

/**
 * Factory to access LogManager instances outside ApplicationContext
 * 
 * @author Thomas MILLET
 *
 */
public class LogManagerFactory {

	/**
	 * Reference to the LogManager Singleton
	 */
	private LogManager defaultLogManagerSingleton;
	
	/**
	 * References to the LogManager Singletons
	 */
	private Map<Object, LogManager> logManagerSingletons;
	
	/**
	 * Reference to the LogManagerFactory Singleton
	 */
	private static LogManagerFactory logManagerFactorySingleton;
	
	/**
	 * Default constructor (should be called by Spring IOC Only)
	 */
	public LogManagerFactory(){
		if(LogManagerFactory.logManagerFactorySingleton == null){
			LogManagerFactory.logManagerFactorySingleton = this;
		}
	}
	
	/**
	 * Accessor to LogManagerFactory singleton
	 * 
	 * @return A LogManagerFactory instance
	 * @throws MMPLogException
	 */
	public static LogManagerFactory getInstance() throws MMPLogException{
		return LogManagerFactory.logManagerFactorySingleton;
	}
	
	/**
	 * Get LogManager instance based on its type
	 * 
	 * @param type The type of the LogManager to get
	 * @return The default LogManager instance of LogManagerFactory
	 * @throws MMPLogException
	 */
	public LogManager getLogManager(Object type) throws MMPLogException{
		return this.logManagerSingletons.get(type);
	}
	
	/**
	 * Get default LogManager instance
	 * 
	 * @return The default LogManager instance of LogManagerFactory
	 * @throws MMPLogException
	 */
	public LogManager getLogManager() throws MMPLogException{
		return this.defaultLogManagerSingleton;
	}
	
	/**
	 * Get the list of LogManager types owned by this Factory 
	 * 
	 * @return An array of Objects containing types
	 */
	public Object[] getTypes(){
		return this.logManagerSingletons.keySet().toArray(); 
	}

	/**
	 * @param logManagerSingletons the logManagerSingletons to set
	 */
	public void setLogManagerSingletons(Map<Object, LogManager> logManagerSingletons) {
		this.logManagerSingletons = logManagerSingletons;
		this.defaultLogManagerSingleton = this.logManagerSingletons.get(Constants.DEFAULT_COMPONENT_KEY);
	}

	
	
}
