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
package com.orange.mmp.api;

import java.util.Map;

import com.orange.mmp.core.Constants;

/**
 * Factory Pattern to access current ApiContainer outside ApplicationContext
 * 
 * @author Thomas MILLET
 *
 */
public class ApiContainerFactory {

	/**
	 * Reference to the default ApiContainer Singleton
	 */
	private ApiContainer defaultApiContainerSingleton;
	
	/**
	 * Map reference to ApiContainer singletons
	 */
	private Map<Object, ApiContainer> apiContainerSingletons; 
	
	/**
	 * Reference to the ApiContainerFactory Singleton
	 */
	private static ApiContainerFactory apiContainerFactorySingleton;
	
	/**
	 * Default constructor (should be called by Spring IOC Only)
	 */
	public ApiContainerFactory(){
		if(ApiContainerFactory.apiContainerFactorySingleton == null){
			ApiContainerFactory.apiContainerFactorySingleton = this;
		}
	}
	
	/**
	 * Accessor to ApiContainerFactory singleton
	 * 
	 * @return A ApiContainerFactory instance
	 * @throws MMPApiException
	 */
	public static ApiContainerFactory getInstance() throws MMPApiException{
		return ApiContainerFactory.apiContainerFactorySingleton;
	}
	
	/**
	 * Get default ApiContainer instance
	 * 
	 * @return The default ApiContainer instance of ApiContainerFactory
	 * @throws MMPApiException
	 */
	public ApiContainer getApiContainer() throws MMPApiException{
		return this.defaultApiContainerSingleton;
	}
	
	/**
	 * Get a ApiContainer instance based on its type
	 * 
	 * @param type The type of the ApiContainer
	 * @return A ApiContainer instance
	 * @throws MMPApiException
	 */
	public ApiContainer getApiContainer(Object type) throws MMPApiException{
		return this.apiContainerSingletons.get(type);
	}

	/**
	 * Get the list of ApiContainer types owned by this Factory 
	 * 
	 * @return An array of Objects containing types
	 */
	public Object[] getTypes(){
		return this.apiContainerSingletons.keySet().toArray(); 
	}
	
	/**
	 * @param apiContainerSingletons the apiContainerSingletons to set
	 */
	public void setApiContainerSingletons(Map<Object, ApiContainer> apiContainerSingletons) {
		this.apiContainerSingletons = apiContainerSingletons;
		this.defaultApiContainerSingleton = this.apiContainerSingletons.get(Constants.DEFAULT_COMPONENT_KEY);
	}	
	
}
