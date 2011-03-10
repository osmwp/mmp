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

import java.net.URL;
import java.util.List;
import java.util.Properties;

import com.orange.mmp.core.config.MMPConfig;
import com.orange.mmp.core.data.Module;

/**
 * Main CUMA interface used to store and manage MMP Modules.<br/>
 * Implementation can be based on :
 * 	- OSGi (default)
 * 	- Filesystem/database
 * 	- Web Services
 * 	- JMX/MBeans ...
 * 
 * 
 * @author Thomas MILLET
 */
public interface ModuleContainer {

	/**
	 * Add/update a module in modules container
	 * @param module The module to add
	 * @throws MMPModuleException
	 */
	public Module addModule(Module module) throws MMPModuleException;
	
	/**
	 * Remove a module from modules container
	 * @param module The module to remove (at least module id must be set)
	 * @throws MMPModuleException
	 */
	public void removeModule(Module module) throws MMPModuleException;
	
	/**
	 * Get module based on its ID
	 * @param module the module (at least module id must be set)
	 * @return A module instance or null if not found
	 * @throws MMPModuleException
	 */
	public Module getModule(Module module) throws MMPModuleException;
	
	/**
	 * List modules owned by current container
	 * @return A List of module
	 * @throws MMPModuleException
	 */
	public List<Module> listModules() throws MMPModuleException;
	
	/**
	 * Register a new module observer	
	 * @param moduleObserver The module observer to register
	 * @throws MMPModuleException
	 */
	public void registerModuleObserver(ModuleObserver moduleObserver) throws MMPModuleException;
	
	/**
	 * Unregister a module observer 
	 * @param moduleObserver The module observer to unregister
	 * @throws MMPModuleException
	 */
	public void unregisterModuleObserver(ModuleObserver moduleObserver) throws MMPModuleException;
	
	/**
	 * Loads a class from module. Instanciation can be done using Class.newInstance()
	 * 
	 * @param module The module owning the class to load
	 * @param className The module class name
	 * @return A Class object for the requested class
	 * @throws MMPModuleException
	 */
	@SuppressWarnings("unchecked")
	public Class loadModuleClass(Module module, String className) throws MMPModuleException;
	
	/**
	 * Search method used to find resources in bundles
	 * 
	 * @param module The module owning the resource (at least module id must be set)
	 * @param path The path to the resource folder (base directory if recurse)
	 * @param filePattern The filePattern for searching, "*" is supported, null for all files
	 * @param recurse search recursively in subdirectories 
	 * @return A list of URL pointing on this resources. Resource can be retrieved using appropriate handler (ex:url.openConnection().getInputStream()) 
	 * @throws MMPModuleException
	 */
	public List<URL> findModuleResource(Module module, String path, String filePattern, boolean recurse) throws MMPModuleException;
	
	/**
	 * Get an URL pointer on a module resource. The resource can be retrieved using the appropriate URL handler.
	 * <br/>For most commons cases, the resource can be retrieved using url.openConnection().getInputStream().
	 * 
	 * @param module The module owning the resource (at least module id must be set)
	 * @param resourcePath The path to the resource 
	 * @return An URL for retrieving the resource
	 * @throws MMPModuleException
	 */
	public URL getModuleResource(Module module, String resourcePath) throws MMPModuleException;
	
	/**
	 * Get a property from a module depending on ModuleContainer implementation.
	 * 
	 * @param module The module to analyse
	 * @param propertyName The name of the property
	 * @return The property value
	 * @throws MMPModuleException
	 */
	public String getModuleProperty(Module module, String propertyName) throws MMPModuleException;	
	
	/**
	 * Get the list of available properties for this module 
	 * 
	 * @param module The module to analyse
	 * @return An array of properties names
	 * @throws MMPModuleException
	 */
	public Properties getModuleProperties(Module module) throws MMPModuleException;
	
	/**
	 * Get a module configuration based on mmp-config.xsd XML Schema
	 * 
	 * @param module The module owning to configuration
	 * @return A MMPConfig depending on module implementation and available extensions
	 * @throws MMPModuleException
	 */
	public MMPConfig getModuleConfiguration(Module module) throws MMPModuleException;	
	
}
