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
package com.orange.mmp.core.loader;

import org.apache.commons.collections.map.HashedMap;

/**
 * Factory providing DynamicClassLoaders based on predefined key
 * 
 * @author Thomas MILLET
 *
 */
public class DynamicClassLoaderFactory {

	/**
	 * Inner map to store ClassLoaders
	 */
	private HashedMap dynamicClassLoadersMap;
	
	/**
	 * Factory Singleton
	 */
	private static DynamicClassLoaderFactory dynamicClassLoaderFactorySingleton = new DynamicClassLoaderFactory();
	
	/**
	 * Default private constructor
	 */
	private DynamicClassLoaderFactory(){
		this.dynamicClassLoadersMap = new HashedMap();
	}
	
	/**
	 * Singleton Accessor
	 * 
	 * @return The RemoteClassLoaderFactory singleton
	 */
	public static DynamicClassLoaderFactory getInstance(){
		return DynamicClassLoaderFactory.dynamicClassLoaderFactorySingleton;
	}
	
	/**
	 * Get a DynamicClassLoader instance bound to the specified key
	 * 
	 * @param key The identifying the classloader
	 * @parama renew If set, the classloader is first flushed and reloaded 
	 * @return A DynamicClassLoader bound to specified key (never null)
	 */
	public DynamicClassLoader getDynamicClassLoader(Object key, ClassLoader parent, boolean renew){
		DynamicClassLoader dynamicClassLoader = null;
		if(renew){
			this.removeDynamicClassLoader(key);
		}
		else{
			dynamicClassLoader = (DynamicClassLoader)this.dynamicClassLoadersMap.get(key);
		}
		
		if(dynamicClassLoader == null){
			dynamicClassLoader = new DynamicClassLoader(parent);
			this.dynamicClassLoadersMap.put(key, dynamicClassLoader);
		}
		
		return dynamicClassLoader;
	}
	
	/**
	 * Get a DynamicClassLoader instance bound to the specified key
	 * without reload action using specified classloader as parent 
	 * 
	 * @param key The identifying the classloader
	 * @param parent The classloader parent
	 * @return A DynamicClassLoader bound to specified key (never null)
	 */
	public DynamicClassLoader getDynamicClassLoader(Object key, ClassLoader parent){
		return this.getDynamicClassLoader(key,parent,false);
	}
	
	/**
	 * Get a DynamicClassLoader instance bound to the specified Service
	 * using system classloader as parent 
	 * 
	 * @param key The identifying the classloader
	 * @parama renew If set, the classloader is first flushed and reloaded 
	 * @return A DynamicClassLoader bound to specified key (never null)
	 */
	public DynamicClassLoader getDynamicClassLoader(Object key, boolean renew){
		return this.getDynamicClassLoader(key,null,renew);
	}
	
	/**
	 * Get a DynamicClassLoader instance bound to the specified Service
	 * without reload action using system classloader as parent 
	 * 
	 * @param key The identifying the classloader
	 * @return A DynmaicClassLoader bound to specified key (never null)
	 */
	public DynamicClassLoader getDynamicClassLoader(Object key){
		return this.getDynamicClassLoader(key,null,false);
	}
	
	/**
	 * Remove a DynamicClassLoader
	 * 
	 * @param key The identifying the classloader
	 */
	public void removeDynamicClassLoader(Object key){
		DynamicClassLoader serviceClassLoader = (DynamicClassLoader)this.dynamicClassLoadersMap.get(key);
		if(serviceClassLoader != null){
			this.dynamicClassLoadersMap.remove(key);
			serviceClassLoader = null;
		}		
	}
}
