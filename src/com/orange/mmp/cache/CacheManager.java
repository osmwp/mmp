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


/**
 * Generic interface used to manage Cache instances
 * 
 * TODO Evolution P1 - Implements MemCache (http://www.danga.com/memcached/)
 * 
 * @author Thomas MILLET
 *
 */
public interface CacheManager {
		
	/**
	 * Add a Cache to the Cache Manager
	 * 
	 * @param cacheName The name of the Cache
	 * @throws MMPCacheException
	 */
	public Cache addCache(String cacheName) throws MMPCacheException;
	
	/**
	 * Get a Cache from the Cache Manager
	 * 
	 * @param cacheName The name of the Cache
	 * @return A cache instance or null if not found
	 * @throws MMPCacheException
	 */
	public Cache getCache(String cacheName) throws MMPCacheException;
	
	/**
	 * Remove a Cache from the Cache Manager
	 * 
	 * @param cacheName The name of the Cache
	 * @throws MMPCacheException
	 */
	public void removeCache(String cacheName) throws MMPCacheException;

}
