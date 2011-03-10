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
package com.orange.mmp.i18n;

import java.util.Map;

import com.orange.mmp.core.Constants;

/**
 * Factory to access InternationalizationManager instances outside ApplicationContext
 * 
 * @author Thomas MILLET
 *
 */
public class InternationalizationManagerFactory {
	
	/**
	 * Reference to the InternationalizationManager Singleton
	 */
	private InternationalizationManager defaultInternationalizationManagerSingleton;
	
	/**
	 * References to the InternationalizationManager Singletons
	 */
	private Map<Object, InternationalizationManager> internationalizationManagerSingletons;
	
	/**
	 * Reference to the InternationalizationManagerFactory Singleton
	 */
	private static InternationalizationManagerFactory internationalizationManagerFactorySingleton;
	
	/**
	 * Default constructor (should be called by Spring IOC Only)
	 */
	public InternationalizationManagerFactory(){
		if(InternationalizationManagerFactory.internationalizationManagerFactorySingleton == null){
			InternationalizationManagerFactory.internationalizationManagerFactorySingleton = this;
		}
	}
	
	/**
	 * Accessor to InternationalizationManagerFactory singleton
	 * 
	 * @return A InternationalizationManagerFactory instance
	 * @throws MMPI18NException
	 */
	public static InternationalizationManagerFactory getInstance() throws MMPI18NException{
		return InternationalizationManagerFactory.internationalizationManagerFactorySingleton;
	}
	
	/**
	 * Get InternationalizationManager instance based on its type
	 * 
	 * @param type The type of the InternationalizationManager to get
	 * @return The default FileManager instance of InternationalizationManagerFactory
	 * @throws MMPI18NException
	 */
	public InternationalizationManager getInternationalizationManager(Object type) throws MMPI18NException{
		return this.internationalizationManagerSingletons.get(type);
	}
	
	/**
	 * Get default InternationalizationManager instance
	 * 
	 * @return The default InternationalizationManager instance of InternationalizationManagerFactory
	 * @throws MMPI18NException
	 */
	public InternationalizationManager getInternationalizationManager() throws MMPI18NException{
		return this.defaultInternationalizationManagerSingleton;
	}
	
	/**
	 * Get the list of InternationalizationManager types owned by this Factory 
	 * 
	 * @return An array of Objects containing types
	 */
	public Object[] getTypes(){
		return this.internationalizationManagerSingletons.keySet().toArray(); 
	}

	/**
	 * @param InternationalizationManagerSingletons the InternationalizationManagerSingletons to set
	 */
	public void setInternationalizationManagerSingletons(Map<Object, InternationalizationManager> internationalizationManagerSingletons) {
		this.internationalizationManagerSingletons = internationalizationManagerSingletons;
		this.defaultInternationalizationManagerSingleton = this.internationalizationManagerSingletons.get(Constants.DEFAULT_COMPONENT_KEY);
	}

	
	
}
