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

import java.lang.reflect.Method;
import java.util.List;

import com.orange.mmp.core.data.Api;

/**
 * Generic interface to handle APIs
 * 
 * TODO Evolution P1 - Add Rest/Hesian ... Implementation
 * TODO Evolution P2 - Find a service container implementation to replace default one
 * 
 * @author Thomas MILLET
 *
 */
public interface ApiContainer {

	/**
	 * Adds an Api instance to container based on its implementation class
	 * 
	 * Remote Api can be added here too, they must be handled by underlying
	 * implementation class (in that case isLocal must be set to false).
	 * 
	 * Resulting Api instance should be based as Interface class name as API
	 * name and Interface methods as Api.
	 * 
	 * Web services can also be exposed by the ApiContainer using transformers.
	 * 
	 * @param api The service data object linked to this Api
	 * @param apiImpl The object owning the Api implementation
	 * @return An Api instance to use with current Api container
	 * @throws MMPApiException
	 */
	public Api addApi(Api api, Object serviceImpl) throws MMPApiException;
	
	/**
	 * Removes an Api from the container
	 * 
	 * @param api The Api to remove (at least name must be set)
	 * @throws MMPApiException
	 */
	public void removeApi(Api api) throws MMPApiException;
	
	/**
	 * Gets a Api instance
	 * 
	 * @param api The Api to get (at least name must be set)
	 * @return An Api instance to use with Api container
	 * @throws MMPApiException
	 */
	public Api getApi(Api api) throws MMPApiException;
	
	/**
	 * List all Apis available in current Api container
	 * 
	 * @return A List of Api instances
	 * @throws MMPApiException
	 */
	public List<Api> listApis() throws MMPApiException;
	
	/**
	 * Allow to invoke an Api
	 * 
	 * @param api The Api to invoke
	 * @param methodName The method to call
	 * @param param Parameters of the call
	 * @return An object containing the result
	 * @throws MMPApiException
	 */
	public Object invokeApi(Api api, String methodName, Object ... params) throws MMPApiException; 
	
	/**
	 * Register a new Api observer	
	 * @param apiObserver The Api observer to register
	 * @throws MMPApiException
	 */
	public void registerApiObserver(ApiObserver apiObserver) throws MMPApiException;
	
	/**
	 * Unregister a Api observer 
	 * @param apiObserver The Api observer to unregister
	 * @throws MMPApiException
	 */
	public void unregisterApiObserver(ApiObserver apiObserver) throws MMPApiException;
	
	/**
	 * Get an Api implementation object based on a Api POJO
	 * @param api The Api owning the implementation
	 * @return The Api implementation Java object
	 * @throws MMPApiException
	 */
	public Object getApiImplementation(Api api) throws MMPApiException;
	
	/**
	 *  Get an Api method
	 *  
	 * @param api The Api owning the method
	 * @param methodName The name of the method
	 * @return A method abstraction class
	 * @throws MMPApiException
	 */
	public Method getApiMethod(Api api, String methodName) throws MMPApiException;
	
}
