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
package com.orange.mmp.cadap;

import java.util.Map;

import com.orange.mmp.core.Constants;

/**
 * Factory Pattern to access MediaApapter instances outside ApplicationContext
 * 
 * @author Thomas MILLET
 *
 */
public class MediaAdapterFactory {

	/**
	 * References to the MediaAdapter Singletons
	 */
	private Map<Object, MediaAdapter> mediaAdapterSingletons;
	
	/**
	 * Reference to the default MediaAdapter Singleton
	 */
	private MediaAdapter defaultMediaAdapterSingleton;
	
	/**
	 * Reference to the MediaAdapterFactory Singletons
	 */
	private static MediaAdapterFactory mediaAdapterFactorySingleton;
	
	/**
	 * Default constructor (should be called by Spring IOC Only)
	 */
	public MediaAdapterFactory(){
		if(MediaAdapterFactory.mediaAdapterFactorySingleton == null){
			MediaAdapterFactory.mediaAdapterFactorySingleton = this;
		}
	}
	
	/**
	 * Accessor to MediaAdapterFactory singleton
	 * 
	 * @return A MediaAdapterFactory instance
	 * @throws MediaAdapterException
	 */
	public static MediaAdapterFactory getInstance() throws MediaAdapterException{
		return MediaAdapterFactory.mediaAdapterFactorySingleton;
	}
	
	/**
	 * Get MediaAdapter instances based on type (mime type)
	 * 
	 * @param type The MediaAdapter key in Factory
	 * @return The MediaAdapter instance of MediaAdapterFactory
	 * @throws MediaAdapterException
	 */
	public MediaAdapter getMediaAdapter(Object type) throws MediaAdapterException{
		return this.mediaAdapterSingletons.get(type);
	}
	
	/**
	 * Get default MediaAdapter instance
	 * 
	 * @return The default CacheManager instance of CacheManagerFactory
	 * @throws MediaAdapterException
	 */
	public MediaAdapter getMediaAdapter() throws MediaAdapterException{
		return this.defaultMediaAdapterSingleton;
	}
	
	/**
	 * Get the list of MediaAdapter types owned by this Factory 
	 * 
	 * @return An array of Objects containing types
	 */
	public Object[] getTypes(){
		return this.mediaAdapterSingletons.keySet().toArray(); 
	}

	/**
	 * @param mediaAdapterSingletons the mediaAdapterSingletons to set
	 */
	public void setMediaAdapterSingletons(Map<Object, MediaAdapter> mediaAdapterSingletons) {
		this.mediaAdapterSingletons = mediaAdapterSingletons;
		this.defaultMediaAdapterSingleton = this.mediaAdapterSingletons.get(Constants.DEFAULT_COMPONENT_KEY);
	}	
	
}
