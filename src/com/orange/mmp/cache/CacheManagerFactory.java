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
package com.orange.mmp.cache;

import java.util.Map;

import com.orange.mmp.core.Constants;

/**
 * Factory Pattern to access CacheManager instances outside ApplicationContext
 * 
 * @author Thomas MILLET
 *
 */
public class CacheManagerFactory {

	/**
	 * References to the CacheManager Singletons
	 */
	private Map<Object, CacheManager> cacheManagerSingletons;
	
	/**
	 * Reference to the default CacheManager Singleton
	 */
	private CacheManager defaultCacheManagerSingleton;
	
	/**
	 * Reference to the CacheManagerFactory Singletons
	 */
	private static CacheManagerFactory cacheManagerFactorySingleton;
	
	/**
	 * Default constructor (should be called by Spring IOC Only)
	 */
	public CacheManagerFactory(){
		if(CacheManagerFactory.cacheManagerFactorySingleton == null){
			CacheManagerFactory.cacheManagerFactorySingleton = this;
		}
	}
	
	/**
	 * Accessor to CacheManagerFactory singleton
	 * 
	 * @return A CacheManagerFactory instance
	 * @throws MMPCacheException
	 */
	public static CacheManagerFactory getInstance() throws MMPCacheException{
		return CacheManagerFactory.cacheManagerFactorySingleton;
	}
	
	/**
	 * Get CacheManager instances based on type
	 * 
	 * @param type The CacheManager key in Factory
	 * @return The CacheManager instance of CacheManagerFactory
	 * @throws MMPCacheException
	 */
	public CacheManager getCacheManager(Object type) throws MMPCacheException{
		return this.cacheManagerSingletons.get(type);
	}
	
	/**
	 * Get default CacheManager instance
	 * 
	 * @return The default CacheManager instance of CacheManagerFactory
	 * @throws MMPCacheException
	 */
	public CacheManager getCacheManager() throws MMPCacheException{
		return this.defaultCacheManagerSingleton;
	}
	
	/**
	 * Get the list of CacheManager types owned by this Factory 
	 * 
	 * @return An array of Objects containing types
	 */
	public Object[] getTypes(){
		return this.cacheManagerSingletons.keySet().toArray(); 
	}

	/**
	 * @param cacheManagerSingletons the cacheManagerSingletons to set
	 */
	public void setCacheManagerSingletons(Map<Object, CacheManager> cacheManagerSingletons) {
		this.cacheManagerSingletons = cacheManagerSingletons;
		this.defaultCacheManagerSingleton = this.cacheManagerSingletons.get(Constants.DEFAULT_COMPONENT_KEY);
	}	
	
}
