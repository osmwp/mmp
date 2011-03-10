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
package com.orange.mmp.api.helpers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;

import org.apache.commons.collections.map.HashedMap;

import com.orange.mmp.api.MMPApiException;
import com.orange.mmp.api.ApiContainer;
import com.orange.mmp.api.ApiEvent;
import com.orange.mmp.api.ApiObserver;
import com.orange.mmp.core.ApplicationListener;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.config.MMPConfig;
import com.orange.mmp.core.data.Element;
import com.orange.mmp.core.data.Module;
import com.orange.mmp.core.data.Api;
import com.orange.mmp.module.MMPModuleException;
import com.orange.mmp.module.ModuleContainerFactory;
import com.orange.mmp.module.ModuleEvent;
import com.orange.mmp.module.ModuleObserver;

/**
 * Default simple ApiContainer implementation
 * 
 * @author Thomas MILLET
 *
 */
public class DefaultApiContainer implements ApplicationListener, ApiContainer, ModuleObserver {
		
	/**
	 * Attribute name used to define a class name
	 */
	public static final QName CUAPI_CONFIG_ATTR_CLASSNAME = new QName("classname");
	
	/**
	 * Attribute name used to define if a service is published on WEB API
	 */
	public static final QName CUAPI_CONFIG_ATTR_IS_PUBLISHED = new QName("published");
	
	/**
	 * Attribute name used to define if a service is shared between MMP instances
	 */
	public static final QName CUAPI_CONFIG_ATTR_IS_SHARED = new QName("shared");
	
	/**
	 * Attribute name used to define if a service is public on WEB API
	 */
	public static final QName CUAPI_CONFIG_ATTR_IS_PUBLIC = new QName("public");
	
	/**
	 * Attribute name used to define if a service is private on WEB API
	 */
	public static final QName CUAPI_CONFIG_ATTR_IS_PRIVATE = new QName("private");
	
	/**
	 * Attribute name used to define the service error type (body or http)
	 */
	public static final QName CUAPI_CONFIG_ATTR_ERROR_TYPE = new QName("errortype");
	
	/**
	 * Cache for services implementation
	 */
	private Map<Api, Element> apiCache;
	
	/**
	 * List of registered service observers
	 */
	private List<ApiObserver> apiObservers;
	
	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.api.ApiContainer#addApi(com.orange.mmp.core.data.Api, java.lang.Object)
	 */
	public Api addApi(Api api, Object apiImpl) throws MMPApiException {		
		//Check if DefinitionClass is an interface
		if(!api.getDefinitionClass().isInterface()){
			throw new MMPApiException("Definition class "+api.getDefinitionClass()+" of api "+api.getName()+" is not an interface");
		}
		//List method of service for caching purpose
		HashedMap methodsMap = new HashedMap();
		for(Method method : api.getDefinitionClass().getMethods()){
			// TODO Fix this to find a way to add multiple methods with same name
			//if(methodsMap.containsKey(method.getName())) throw new MMPApiException("Duplicated method name "+method.getName()+" in api "+api.getName());
			methodsMap.put(method.getName(), method);
		}
		
		synchronized(this.apiCache){ 
			//Add it to service cache
			this.apiCache.put(api,new Element(api,new Element(apiImpl,methodsMap)));
	
			//Notify ServiceObservers of new service availability
			ApiEvent apiEvent = new ApiEvent(ApiEvent.API_ADDED,api);
			for(ApiObserver serviceObserver : this.apiObservers){
				serviceObserver.onApiEvent(apiEvent);
			}
		}

		return api;
	}
	
	/* (non-Javadoc)
	 * @see com.orange.mmp.api.ApiContainer#getApiMethod(com.orange.mmp.core.data.Api, java.lang.String)
	 */
	public Method getApiMethod(Api api, String methodName) throws MMPApiException {
		if(this.apiCache.containsKey(api)){
			Element apiElement = ((Element)((Element)this.apiCache.get(api)).getValue());
			if(((HashedMap)apiElement.getValue()).containsKey(methodName)){
				return (Method)((HashedMap)apiElement.getValue()).get(methodName);
			}
			else throw new MMPApiException("Method '"+api.getName()+"."+methodName+"' not found");
		}
		else throw new MMPApiException("API '"+api.getName()+"' not found");
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.api.ApiContainer#invokeApi(com.orange.mmp.core.data.Api, java.lang.String, java.lang.Object[])
	 */
	public Object invokeApi(Api api, String methodName, Object... params) throws MMPApiException {
		Object apiImplementation = this.getApiImplementation(api);
		Method method = this.getApiMethod(api, methodName);
		
		try{
			return method.invoke(apiImplementation, params);
		}catch(IllegalArgumentException iarge){
			throw new MMPApiException(iarge);
		}catch(IllegalAccessException iae){
			throw new MMPApiException(iae);
		}catch(InvocationTargetException ite){
			throw new MMPApiException(ite.getCause());
		}		
	}

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.api.ApiContainer#getApi(com.orange.mmp.core.data.Api)
	 */
	public Api getApi(Api api) throws MMPApiException {
		if(this.apiCache.containsKey(api)){
			return (Api)((Element)this.apiCache.get(api)).getKey();
		}
		else throw new MMPApiException("API '"+api.getName()+"' not found");
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.api.ApiContainer#getApiImplementation(com.orange.mmp.core.data.Api)
	 */
	public Object getApiImplementation(Api api) throws MMPApiException {
		if(this.apiCache.containsKey(api)){
			return ((Element)(Element)this.apiCache.get(api).getValue()).getKey();
		}
		else return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.api.ApiContainer#listApis()
	 */
	public List<Api> listApis() throws MMPApiException {
		List<Api> apiList = new ArrayList<Api>();
		apiList.addAll(this.apiCache.keySet());
		return apiList;
	}

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.api.ApiContainer#removeApi(com.orange.mmp.core.data.Api)
	 */
	public void removeApi(Api api) throws MMPApiException {
		synchronized(this.apiCache){
			//Retrieve full API instance to build ServiceEvent
			ApiEvent apiEvent = new ApiEvent(ApiEvent.API_REMOVED, this.getApi(api));
		
			//Notify listeners
			for(ApiObserver apiObserver : this.apiObservers){
				apiObserver.onApiEvent(apiEvent);
			}
		
			//Remove api
			this.apiCache.remove(api);
		}
	}

	public void initialize() throws MMPException {
		//Initialize API observers
		this.apiObservers = Collections.synchronizedList(new ArrayList<ApiObserver>());
		
		//Initialized API cache
		this.apiCache = new ConcurrentHashMap<Api, Element>();
		
		//Get the ModuleContainer and add itself as a ModuleObserver
		ModuleContainerFactory.getInstance().getModuleContainer().registerModuleObserver(this);
	}	
	
	public void shutdown() throws MMPApiException {
		if(this.apiObservers != null) this.apiObservers.clear();
		if(this.apiCache != null) this.apiCache.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.module.ModuleObserver#onModuleEvent(com.orange.mmp.module.ModuleEvent)
	 */
	public void onModuleEvent(ModuleEvent moduleEvent) {
		try{
			if(moduleEvent.getType() == ModuleEvent.MODULE_ADDED){
				this.addModuleApi(moduleEvent.getModule());
			}
			else if(moduleEvent.getType() == ModuleEvent.MODULE_REMOVED){
				this.removeModuleApi(moduleEvent.getModule());
			}
		}catch(MMPApiException cue){
			//NOP - Just Log
		}
	}
	
	/**
	 * Inner method used to add module API from their configuration file  
	 * 
	 * @param module The module owning the API
	 * @throws MMPApiException
	 */
	@SuppressWarnings("unchecked")
	protected void addModuleApi(Module module) throws MMPApiException{
		try{
			MMPConfig moduleConfiguration = ModuleContainerFactory.getInstance().getModuleContainer().getModuleConfiguration(module);
			if(moduleConfiguration != null && moduleConfiguration.getApi() != null){
				for(MMPConfig.Api apiConfig : moduleConfiguration.getApi()){
					String definitionClassname = null;
					String implementationClassname = null;
					if(apiConfig.getDefinition() != null 
							&& apiConfig.getDefinition().getOtherAttributes() != null){
						definitionClassname = apiConfig.getDefinition().getOtherAttributes().get(CUAPI_CONFIG_ATTR_CLASSNAME);
					}
					if(apiConfig.getImplementation() != null 
							&& apiConfig.getImplementation().getOtherAttributes() != null){
						implementationClassname = apiConfig.getImplementation().getOtherAttributes().get(CUAPI_CONFIG_ATTR_CLASSNAME);
					}
					
					if(definitionClassname != null && implementationClassname != null){
						Class definitionClass = ModuleContainerFactory.getInstance().getModuleContainer().loadModuleClass(module, definitionClassname);
						if(!definitionClass.isInterface()){
							throw new MMPApiException("Failed to add Module '"+module.getName()+"' Service : '"+definitionClass.getName()+"' is not an interface");
						}
						Object implementationInstance = ModuleContainerFactory.getInstance().getModuleContainer().loadModuleClass(module, implementationClassname).newInstance();
						Api api = new Api();
						api.setDefinitionClass(definitionClass);
						api.setName(apiConfig.getName());
						api.setPublished(apiConfig.getOtherAttributes().get(CUAPI_CONFIG_ATTR_IS_PUBLISHED) == null
								|| apiConfig.getOtherAttributes().get(CUAPI_CONFIG_ATTR_IS_PUBLISHED).equalsIgnoreCase("true"));
						api.setShared(apiConfig.getOtherAttributes().get(CUAPI_CONFIG_ATTR_IS_SHARED) != null
								&& apiConfig.getOtherAttributes().get(CUAPI_CONFIG_ATTR_IS_SHARED).equalsIgnoreCase("true"));
						api.setPublic(apiConfig.getOtherAttributes().get(CUAPI_CONFIG_ATTR_IS_PUBLIC) != null
								&& apiConfig.getOtherAttributes().get(CUAPI_CONFIG_ATTR_IS_PUBLIC).equalsIgnoreCase("true"));
						api.setPrivate(apiConfig.getOtherAttributes().get(CUAPI_CONFIG_ATTR_IS_PRIVATE) != null
								&& apiConfig.getOtherAttributes().get(CUAPI_CONFIG_ATTR_IS_PRIVATE).equalsIgnoreCase("true"));
						
						// Errortype
						if(apiConfig.getOtherAttributes().get(CUAPI_CONFIG_ATTR_ERROR_TYPE) != null )
							api.setErrorType(apiConfig.getOtherAttributes().get(CUAPI_CONFIG_ATTR_ERROR_TYPE).toLowerCase());
						else
							api.setErrorType("body");
						//Add api to current ServiceContainer
						this.addApi(api, implementationInstance);
					}
				}
			}
		}catch(InstantiationException ie){
			throw new MMPApiException(ie);
		}catch(IllegalAccessException iae){
			throw new MMPApiException(iae);
		}catch(MMPModuleException ce){
			throw new MMPApiException(ce);	
		}
	}
	
	/**
	 * Inner method used to remove a module API implementation
	 * 
	 * @param module The module owning the API
	 * @param moduleContainer The module container
	 * @throws MMPApiException
	 */
	protected void removeModuleApi(Module module) throws MMPApiException {
		try{
			MMPConfig moduleConfiguration = ModuleContainerFactory.getInstance().getModuleContainer().getModuleConfiguration(module);
			if(moduleConfiguration != null && moduleConfiguration.getApi() != null){
				for(MMPConfig.Api apiConfig : moduleConfiguration.getApi()){
					Api api = new Api();
					api.setName(apiConfig.getName());
					this.removeApi(api);
				}
			}		
		}catch(MMPModuleException ce){
			throw new MMPApiException(ce);	
		}
	}


	public void registerApiObserver(ApiObserver apiObserver) throws MMPApiException {
		this.apiObservers.add(apiObserver);
	}


	public void unregisterApiObserver(ApiObserver apiObserver) throws MMPApiException {
		this.apiObservers.remove(apiObserver);
	}
	
	
}
