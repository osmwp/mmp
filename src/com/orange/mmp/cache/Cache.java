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

import java.util.List;

import com.orange.mmp.core.data.Element;

/**
 * Generic Cache interface
 * 
 * @author Thomas MILLET
 *
 */
public interface Cache {

	/**
	 * Set an object in cache
	 * 
	 * @param element The element to store
	 * @throws MMPCacheException
	 */
	public void set(Element element) throws MMPCacheException;
	
	/**
	 * Get an object from cache
	 * 
	 * @param key The key used
	 * @return An Element containing key and value
	 * @throws MMPCacheException
	 */
	public Element get(Object key) throws MMPCacheException;
	
	/**
	 * Remove an object from cache
	 * 
	 * @param key The key used
	 * @throws MMPCacheException
	 */
	public void remove(Object key) throws MMPCacheException;
	
	/**
	 * Get a list of all available keys in a cache 
	 * 
	 * @return A list of keys
	 * @throws MMPCacheException
	 */
	public List<Object> getKeys() throws MMPCacheException;
	
	/**
	 * Get a list of all available elements in a cache 
	 * 
	 * @return A list of keys
	 * @throws MMPCacheException
	 */
	public List<Element> getElements() throws MMPCacheException;
	
	/**
	 * Indicates if a key is present in cache
	 * 
	 * @param key The key used
	 * @return true if key found, false otherwise
	 * @throws MMPCacheException
	 */
	public boolean isKeyInCache(Object key) throws MMPCacheException;

	/**
	 * Indicates if a value is present in cache
	 * 
	 * @param value The value used
	 * @return true if value found, false otherwise
	 * @throws MMPCacheException
	 */
	public boolean isValueInCache(Object value) throws MMPCacheException;

	/**
	 * Get the size of the current cache 
	 * 
	 * @return The number of elements in cache
	 * @throws MMPCacheException
	 */
	public int size() throws MMPCacheException;
		
	/**
	 * Clear specified cache
	 * 
	 * @param cacheName The cache to clear
	 * @throws MMPCacheException
	 */
	public void clear() throws MMPCacheException;
}
