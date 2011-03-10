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
package com.orange.mmp.module;

import java.util.Map;

import com.orange.mmp.core.Constants;

/**
 * Factory to access ModuleContainer instances outside ApplicationContext
 * 
 * @author Thomas MILLET
 *
 */
public class ModuleContainerFactory {

	/**
	 * Reference to the default ModuleContainer Singleton
	 */
	private ModuleContainer defaultModuleContainerSingleton;
	
	
	/**
	 * References to the ModuleContainer instances
	 */
	private Map<Object, ModuleContainer> moduleContainerSingletons;
	
	/**
	 * Reference to the ModuleContainerFactory Singleton
	 */
	private static ModuleContainerFactory moduleContainerFactorySingleton;
	
	/**
	 * Default constructor (should be called by Spring IOC Only)
	 */
	public ModuleContainerFactory(){
		if(ModuleContainerFactory.moduleContainerFactorySingleton == null){
			ModuleContainerFactory.moduleContainerFactorySingleton = this;
		}
	}
	
	/**
	 * Accessor to ModuleContainerFactory singleton
	 * 
	 * @return A ModuleContainerFactory instance
	 * @throws MMPModuleException
	 */
	public static ModuleContainerFactory getInstance() throws MMPModuleException{
		return ModuleContainerFactory.moduleContainerFactorySingleton;
	}
	
	/**
	 * Factory to get ModuleContainer instance
	 * 
	 * @return The default ModuleContainer instance of ModuleContainerFactory
	 * @throws MMPModuleException
	 */
	public ModuleContainer getModuleContainer() throws MMPModuleException{
		return this.defaultModuleContainerSingleton;
	}
	
	/**
	 * Factory to get ModuleContainer instance based on its type
	 * 
	 * @param type The type of the ModuleContainer
	 * @return The ModuleContainer instance of ModuleContainerFactory
	 * @throws MMPModuleException
	 */
	public ModuleContainer getModuleContainer(Object type) throws MMPModuleException{
		return this.moduleContainerSingletons.get(type);
	}
	
	/**
	 * Get the list of ModuleContainer types owned by this Factory 
	 * 
	 * @return An array of Objects containing types
	 */
	public Object[] getTypes(){
		return this.moduleContainerSingletons.keySet().toArray(); 
	}

	/**
	 * @param moduleContainerSingletons the moduleContainerSingletons to set
	 */
	public void setModuleContainerSingletons(Map<Object, ModuleContainer> moduleContainerSingletons) {
		this.moduleContainerSingletons = moduleContainerSingletons;
		this.defaultModuleContainerSingleton = this.moduleContainerSingletons.get(Constants.DEFAULT_COMPONENT_KEY);
	}	
	
}
