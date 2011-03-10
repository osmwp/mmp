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
package com.orange.mmp.webpart;

import java.util.List;
import java.util.Map;

import com.orange.mmp.core.Constants;
import com.orange.mmp.core.MMPException;

/**
 * Factory Pattern to access current ApiContainer outside ApplicationContext
 * 
 * @author Thomas MILLET
 *
 */
public class WebPartContainerFactory {

	/**
	 * Reference to the default WebPartContainer Singleton
	 */
	private WebPartContainer defaultWebPartContainerSingleton;
	
	/**
	 * Map reference to WebPartContainer singletons
	 */
	private Map<Object, WebPartContainer> webPartContainerSingletons; 

	/**
	 * Map of content types by extension
	 */
	private Map<String, String> contentTypeByExtension; 

	/**
	 * List of extensions with support of templates
	 */
	private List<String> extensionsWithTemplateSupport;
	
	/**
	 * Reference to the WebPartContainerFactory Singleton
	 */
	private static WebPartContainerFactory webPartContainerFactorySingleton;
	
	/**
	 * Default constructor (should be called by Spring IOC Only)
	 */
	public WebPartContainerFactory(){
		if(WebPartContainerFactory.webPartContainerFactorySingleton == null){
			WebPartContainerFactory.webPartContainerFactorySingleton = this;
		}
	}
	
	/**
	 * Accessor to WebPartContainerFactory singleton
	 * 
	 * @return A WebPartContainerFactory instance
	 * @throws MMPException
	 */
	public static WebPartContainerFactory getInstance() throws MMPException{
		return WebPartContainerFactory.webPartContainerFactorySingleton;
	}
	
	/**
	 * Get default WebPartContainer instance
	 * 
	 * @return The default WebPartContainer instance of WebPartContainerFactory
	 * @throws MMPException
	 */
	public WebPartContainer getWebPartContainer() throws MMPException{
		return this.defaultWebPartContainerSingleton;
	}
	
	/**
	 * Get a WebPartContainer instance based on its type
	 * 
	 * @param type The type of the WebPartContainer
	 * @return A WebPartContainer instance
	 * @throws MMPException
	 */
	public WebPartContainer getWebPartContainer(Object type) throws MMPException{
		return this.webPartContainerSingletons.get(type);
	}

	/**
	 * Get the list of WebPartContainer types owned by this Factory 
	 * 
	 * @return An array of Objects containing types
	 */
	public Object[] getTypes(){
		return this.webPartContainerSingletons.keySet().toArray(); 
	}
	
	/**
	 * @param webPartContainerSingletons the apiContainerSingletons to set
	 */
	public void setApiContainerSingletons(Map<Object, WebPartContainer> webPartContainerSingletons) {
		this.webPartContainerSingletons = webPartContainerSingletons;
		this.defaultWebPartContainerSingleton = this.webPartContainerSingletons.get(Constants.DEFAULT_COMPONENT_KEY);
	}

	/**
	 * Define the map of content types (sort by extension).
	 * @param contentTypeByExtension Map of content types.
	 */
	public void setContentTypeByExtension(Map<String, String> contentTypeByExtension) {
		this.contentTypeByExtension = contentTypeByExtension;
	}

	/**
	 * Get map of content types (sort by extension).
	 * @param contentTypeByExtension The new map of content types.
	 */
	public Map<String, String> getContentTypeByExtension() {
		return contentTypeByExtension;
	}

	/**
	 * Modify extensions list.
	 * @param extensionsWithTemplateSupport
	 */
	public void setExtensionsWithTemplateSupport(
			List<String> extensionsWithTemplateSupport) {
		this.extensionsWithTemplateSupport = extensionsWithTemplateSupport;
	}

	/**
	 * Get extensions list.
	 * @return The extension list.
	 */
	public List<String> getExtensionsWithTemplateSupport() {
		return extensionsWithTemplateSupport;
	}

	
}
