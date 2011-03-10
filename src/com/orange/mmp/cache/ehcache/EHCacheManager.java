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
package com.orange.mmp.cache.ehcache;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Status;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import com.orange.mmp.cache.Cache;
import com.orange.mmp.cache.CacheManager;
import com.orange.mmp.cache.MMPCacheException;
import com.orange.mmp.core.ApplicationListener;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.config.MMPConfig;
import com.orange.mmp.core.data.Module;
import com.orange.mmp.module.MMPModuleException;
import com.orange.mmp.module.ModuleContainerFactory;
import com.orange.mmp.module.ModuleEvent;
import com.orange.mmp.module.ModuleObserver;


/**
 * Implements CacheManager using EHCache
 * 
 * TODO Evolution P1 - Handle more options on cache config (overflow, sharing ...)
 * 
 * @author Thomas MILLET
 *
 */
public class EHCacheManager  implements ApplicationListener, CacheManager, ModuleObserver {
	
	/**
	 * Attribute name used to define a class name
	 */
	public static final QName CUCACHE_CONFIG_ATTR_MAX_ELEMENTS_IN_MEMORY = new QName("maxElementsInMemory");
	
	/**
	 * Attribute name used to define a class name
	 */
	public static final QName CUCACHE_CONFIG_ATTR_ETERNAL = new QName("eternal");
	
	/**
	 * Attribute name used to define a class name
	 */
	public static final QName CUCACHE_CONFIG_ATTR_MEMORY_STORE_EVISTION_POLICY = new QName("memoryStoreEvictionPolicy");
	
	/**
	 * Attribute name used to define a class name
	 */
	public static final QName CUCACHE_CONFIG_ATTR_TIME_TO_IDLE_SECONDS = new QName("timeToIdleSeconds");
	
	/**
	 * Attribute name used to define a class name
	 */
	public static final QName CUCACHE_CONFIG_ATTR_TIME_TO_LIVE_SECONDS = new QName("timeToLiveSeconds");
	
	
	/**
	 * EHCache main configuration file path
	 */
	private String configurationFile;
	
	/**
	 * Reference on EHCache CacheManager for delegation
	 */
	private net.sf.ehcache.CacheManager ehcacheManager;
	
	/**
	 * Modules cache configuration cache
	 */
	private Map<Module, net.sf.ehcache.Cache[]> moduleCachesCache;
	
	public Cache addCache(String cacheName)	throws MMPCacheException {
		synchronized (this.ehcacheManager) {
			net.sf.ehcache.Cache ehCache = new net.sf.ehcache.Cache(cacheName, 0, true, false, 0, 0 );
			this.ehcacheManager.addCache(ehCache);
			return new EHCache(ehCache);	
		}		
	}

	public Cache getCache(String cacheName)	throws MMPCacheException {
		net.sf.ehcache.Cache ehCache = this.ehcacheManager.getCache(cacheName);
		return new EHCache(ehCache);
	}

	public void removeCache(String cacheName) throws MMPCacheException {
		synchronized(this.ehcacheManager){
			if(this.ehcacheManager.getStatus() == Status.STATUS_ALIVE){
				this.ehcacheManager.removeCache(cacheName);
			}
		}
	}
	
	public void initialize() throws MMPException {
		try{
			this.moduleCachesCache = new ConcurrentHashMap<Module, net.sf.ehcache.Cache[]>();
			this.ehcacheManager = net.sf.ehcache.CacheManager.create(new File(this.configurationFile).toURI().toURL());
		}catch(MalformedURLException mue){
			throw new MMPCacheException(mue);
		}catch(CacheException ce){
			throw new MMPCacheException(ce);
		}
		
		//Get the ModuleContainer and add itself as a ModuleObserver
		ModuleContainerFactory.getInstance().getModuleContainer().registerModuleObserver(this);
	}
	
	public void shutdown() throws MMPException {
		if(this.moduleCachesCache != null) this.moduleCachesCache.clear();
		if(this.ehcacheManager != null){
			this.ehcacheManager.clearAll();
			this.ehcacheManager.shutdown();
		}
	}

	
	public void onModuleEvent(ModuleEvent moduleEvent) {
		try{
			if(moduleEvent.getType() == ModuleEvent.MODULE_ADDED){
				this.addModuleCache(moduleEvent.getModule());
			}
			else if(moduleEvent.getType() == ModuleEvent.MODULE_REMOVED){
				this.removeModuleCache(moduleEvent.getModule());
			}
		}catch(MMPCacheException cce){
			//NOP - Just Log
		}
	}
	
	/**
	 * Adds a cache in current CacheManager based on cucache.xsd configuration file
	 * 
	 * @param module The module owning cache configuration
	 * @throws MMPCacheException
	 */
	protected void addModuleCache(Module module) throws MMPCacheException{
		try{
			MMPConfig moduleConfiguration = ModuleContainerFactory.getInstance().getModuleContainer().getModuleConfiguration(module);
			if(moduleConfiguration != null && moduleConfiguration.getCache() != null){
				for(MMPConfig.Cache cacheConfig : moduleConfiguration.getCache()){
					if(cacheConfig.getOtherAttributes() != null){
						String maxElementsInMemory = cacheConfig.getOtherAttributes().get(CUCACHE_CONFIG_ATTR_MAX_ELEMENTS_IN_MEMORY);
						String eternal = cacheConfig.getOtherAttributes().get(CUCACHE_CONFIG_ATTR_ETERNAL);
						String memoryStoreEvictionPolicy = cacheConfig.getOtherAttributes().get(CUCACHE_CONFIG_ATTR_MEMORY_STORE_EVISTION_POLICY);
						String timeToIdleSeconds = cacheConfig.getOtherAttributes().get(CUCACHE_CONFIG_ATTR_TIME_TO_IDLE_SECONDS);
						String timeToLiveSeconds = cacheConfig.getOtherAttributes().get(CUCACHE_CONFIG_ATTR_TIME_TO_LIVE_SECONDS);
						if(maxElementsInMemory != null && eternal != null){
							MemoryStoreEvictionPolicy memoryStoreEvictionPolicyObject = MemoryStoreEvictionPolicy.LRU;
							if(memoryStoreEvictionPolicy != null){
								if(memoryStoreEvictionPolicy.equals("LFU")) memoryStoreEvictionPolicyObject = MemoryStoreEvictionPolicy.LFU;
								else if(memoryStoreEvictionPolicy.equals("FIFO")) memoryStoreEvictionPolicyObject = MemoryStoreEvictionPolicy.FIFO;
							}
							
							net.sf.ehcache.Cache cache = new net.sf.ehcache.Cache(cacheConfig.getName(),
																					Integer.parseInt(maxElementsInMemory),
																					memoryStoreEvictionPolicyObject,
																					false,
																					null,
																					Boolean.valueOf(eternal),
																					(timeToLiveSeconds != null) ? Long.valueOf(timeToLiveSeconds):0,
																					(timeToIdleSeconds != null) ? Long.valueOf(timeToIdleSeconds):0,
																					false,
																					0,
																					null);
							this.ehcacheManager.addCache(cache);
						}
					}
				}
			}
		}catch(MMPModuleException ce){
			throw new MMPCacheException(ce);
		}	
	}
	
	/**
	 * Removes a cache from current CacheManager based on cucache.xsd configuration file
	 * 
	 * @param module The module owning the cache to remove
	 * @throws MMPCacheException
	 */
	protected void removeModuleCache(Module module) throws MMPCacheException{
		try{
			MMPConfig moduleConfiguration = ModuleContainerFactory.getInstance().getModuleContainer().getModuleConfiguration(module);
			if(moduleConfiguration != null && moduleConfiguration.getCache() != null){
				for(MMPConfig.Cache cacheConfig : moduleConfiguration.getCache()){
					this.ehcacheManager.removeCache(cacheConfig.getName());
				}
			}
		}catch(MMPModuleException ce){
			throw new MMPCacheException(ce);
		}
	}

	/**
	 * @param configurationFile the configurationFile to set
	 */
	public void setConfigurationFile(String configurationFile) {
		this.configurationFile = configurationFile;
	}
}
