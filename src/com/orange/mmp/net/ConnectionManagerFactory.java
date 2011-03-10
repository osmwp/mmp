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
package com.orange.mmp.net;

import java.util.Map;

/**
 * Factory to access current LogManager outside ApplicationContext
 * 
 * @author Thomas MILLET
 *
 */
public class ConnectionManagerFactory {
	
	/**
	 * Reference to the ConnectionManager Singletons
	 */
	private Map<Object, ConnectionManager> connectionManagerSingletons;
	
	/**
	 * Reference to the ConnectionManagerFactory Singleton
	 */
	private static ConnectionManagerFactory connectionManagerFactorySingleton;
	
	/**
	 * Default constructor (should be called by Spring IOC Only)
	 */
	public ConnectionManagerFactory(){
		if(ConnectionManagerFactory.connectionManagerFactorySingleton == null){
			ConnectionManagerFactory.connectionManagerFactorySingleton = this;
		}
	}
	
	/**
	 * Accessor to ConnectionManagerFactory singleton
	 * 
	 * @return A ConnectionManagerFactory instance
	 * @throws MMPNetException
	 */
	public static ConnectionManagerFactory getInstance() throws MMPNetException{
		return ConnectionManagerFactory.connectionManagerFactorySingleton;
	}
	
	/**
	 * Get ConnectionManager instance based on its type
	 * 
	 * @param type The type (protocol) handled by the ConnectionManager to get
	 * @return The ConnectionManager instance based on type
	 * @throws MMPNetException
	 */
	public ConnectionManager getConnectionManager(Object type) throws MMPNetException{
		return this.connectionManagerSingletons.get(type);
	}
	
	/**
	 * Get the list of ConnectionManager types owned by this Factory 
	 * 
	 * @return An array of Objects containing types
	 */
	public Object[] getTypes(){
		return this.connectionManagerSingletons.keySet().toArray(); 
	}

	/**
	 * @param connectionManagerSingletons the connectionManagerSingletons to set
	 */
	public void setConnectionManagerSingletons(Map<Object, ConnectionManager> connectionManagerSingletons) {
		this.connectionManagerSingletons = connectionManagerSingletons;
	}

}
