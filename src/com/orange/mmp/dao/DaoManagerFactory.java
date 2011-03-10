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

import java.util.Map;

import com.orange.mmp.core.Constants;

/**
 * Factory to access DaoManager instances outside ApplicationContext
 * 
 * @author Thomas MILLET
 *
 */
public class DaoManagerFactory {
	
	/**
	 * Reference to the DaoManager Singleton
	 */
	private DaoManager defaultDaoManagerSingleton;
	
	/**
	 * References to the DaoManager Singletons
	 */
	private Map<Object, DaoManager> daoManagerSingletons;
	
	/**
	 * Reference to the DaoManagerFactory Singleton
	 */
	private static DaoManagerFactory daoManagerFactorySingleton;
	
	/**
	 * Default constructor (should be called by Spring IOC Only)
	 */
	public DaoManagerFactory(){
		if(DaoManagerFactory.daoManagerFactorySingleton == null){
			DaoManagerFactory.daoManagerFactorySingleton = this;
		}
	}
	
	/**
	 * Accessor to DaoManagerFactory singleton
	 * 
	 * @return A DaoManagerFactory instance
	 * @throws MMPDaoException
	 */
	public static DaoManagerFactory getInstance() throws MMPDaoException{
		return DaoManagerFactory.daoManagerFactorySingleton;
	}
	
	/**
	 * Get DaoManager instance based on its type
	 * 
	 * @param type The type of the DaoManager to get
	 * @return The default DaoManager instance of DaoManagerFactory
	 * @throws MMPDaoException
	 */
	public DaoManager getDaoManager(Object type) throws MMPDaoException{
		return this.daoManagerSingletons.get(type);
	}
	
	/**
	 * Get default DaoManager instance
	 * 
	 * @return The default DaoManager instance of DaoManagerFactory
	 * @throws MMPDaoException
	 */
	public DaoManager getDaoManager() throws MMPDaoException{
		return this.defaultDaoManagerSingleton;
	}
	
	/**
	 * Get the list of DaoManager types owned by this Factory 
	 * 
	 * @return An array of Objects containing types
	 */
	public Object[] getTypes(){
		return this.daoManagerSingletons.keySet().toArray(); 
	}

	/**
	 * @param daoManagerSingletons the daoManagerSingletons to set
	 */
	public void setDaoManagerSingletons(Map<Object, DaoManager> fileManagerSingletons) {
		this.daoManagerSingletons = fileManagerSingletons;
		this.defaultDaoManagerSingleton = this.daoManagerSingletons.get(Constants.DEFAULT_COMPONENT_KEY);
	}

	
	
}
