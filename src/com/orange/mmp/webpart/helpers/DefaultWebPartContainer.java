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
package com.orange.mmp.webpart.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.orange.mmp.context.RequestContext;
import com.orange.mmp.context.ResponseContext;
import com.orange.mmp.context.UserContext;
import com.orange.mmp.core.ApplicationListener;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.config.MMPConfig;
import com.orange.mmp.core.config.MMPConfig.Homepage;
import com.orange.mmp.core.data.Module;
import com.orange.mmp.core.data.Service;
import com.orange.mmp.core.data.WebPart;
import com.orange.mmp.module.MMPModuleException;
import com.orange.mmp.module.ModuleContainerFactory;
import com.orange.mmp.module.ModuleEvent;
import com.orange.mmp.module.ModuleObserver;
import com.orange.mmp.webpart.WebPartContainer;
import com.orange.mmp.webpart.accesspoint.BundleWebAccessPoint;

/**
 * @author rmxc7111
 *
 */
public class DefaultWebPartContainer implements WebPartContainer, ModuleObserver, ApplicationListener {

	/**
	 * Default servlet class name for bundles
	 */
	private static final String DEFAULT_WEB_ACCESS_POINT_CLASS_NAME = BundleWebAccessPoint.class.getName();
	
	/**
	 * Web part by name
	 */
	final Map<String, WebPart> webPartMap = new ConcurrentHashMap<String, WebPart>();

	/**
	 * Home page URLs by service name
	 */
	final Map<String, String> homePageByServiceMap = new ConcurrentHashMap<String, String>();
	
	/**
	 * @see com.orange.mmp.webpart.WebPartContainer#addWebPart(com.orange.mmp.core.data.WebPart)
	 */
	public void addWebPart(final WebPart webPart) throws MMPException {
		if (webPart == null) {
			throw new MMPException("We cannot registered a null web part entry.");
		} else if (webPart.getName() == null || webPart.getName().length() <= 0) {
			throw new MMPException("We cannot registered a web part entry without service name.");
		} else {
			webPartMap.put(webPart.getName(), webPart);
		}
	}

	/**
	 * @see com.orange.mmp.webpart.WebPartContainer#getWebPart(java.lang.String)
	 */
	public WebPart getWebPart(final String service) throws MMPException {
		final WebPart webPart;
		if (service != null && service.length() > 0) {
			webPart = webPartMap.get(service);
		} else {
			webPart = null;
		}
		return webPart;
	}

	/**
	 * @see com.orange.mmp.webpart.WebPartContainer#invoke(com.orange.mmp.core.data.WebPart, com.orange.mmp.context.RequestContext, com.orange.mmp.context.UserContext, com.orange.mmp.context.ResponseContext)
	 */
	public void invoke(final WebPart webPart, final RequestContext requestContext,
			final UserContext userContext, final ResponseContext responseContext)
			throws MMPException {
		if (webPart == null) {
			throw new MMPException("We cannot invoke HTTP request on a null web point access.");
		} else {
			webPart.getWebAccessPoint().processRequest(requestContext, userContext, responseContext);
		}
	}

	/**
	 * @see com.orange.mmp.webpart.WebPartContainer#listWebParts()
	 */
	public List<WebPart> listWebParts() throws MMPException {
		final List<WebPart> list = new ArrayList<WebPart>(webPartMap.size());
		list.addAll(webPartMap.values());
		return list;
	}

	/**
	 * @see com.orange.mmp.webpart.WebPartContainer#removeWebPart(com.orange.mmp.core.data.WebPart)
	 */
	public void removeWebPart(final WebPart webPart) throws MMPException {
		if (webPart == null) {
			throw new MMPException("We cannot unregistered a null web part entry.");
		} else if (webPart.getName() == null || webPart.getName().length() <= 0) {
			throw new MMPException("We cannot unregistered a web part entry without service name.");
		} else {
			webPartMap.remove(webPart.getName());
		}
	}

	/**
	 * Module event (add/remove/...).
	 * Registered web access point in module.
	 * @param moduleEvent Event.
	 */
	public void onModuleEvent(ModuleEvent moduleEvent) {
		try{
			if(moduleEvent.getType() == ModuleEvent.MODULE_ADDED){
				addModuleWebPart(moduleEvent.getModule());
			}
			else if(moduleEvent.getType() == ModuleEvent.MODULE_REMOVED){
				removeModuleWebPart(moduleEvent.getModule());
			}
		}catch(MMPException mmpe){
			//NOP - Just Log
		}
	}

	/**
	 * Add module.
	 * Registered web access point of module.
	 * @param module Module
	 * @throws MMPException Error during process
	 */
	protected void addModuleWebPart(Module module) throws MMPException{
		try{
			MMPConfig moduleConfiguration = ModuleContainerFactory.getInstance().getModuleContainer().getModuleConfiguration(module);
			if(moduleConfiguration != null){
				
				//Add web part access point
				if (moduleConfiguration.getWeb() != null) {
					for(MMPConfig.Web webPartConfig : moduleConfiguration.getWeb()){
						final String webAccessPointClassname;
						if(webPartConfig.getAccesspoint() != null 
						&& webPartConfig.getAccesspoint().getClassname() != null){
							webAccessPointClassname = webPartConfig.getAccesspoint().getClassname();
						} else {
							webAccessPointClassname = DEFAULT_WEB_ACCESS_POINT_CLASS_NAME;
						}
						
						//Load class of web access point in module/bundle
						Class<?> accessPointClass = ModuleContainerFactory.getInstance().getModuleContainer().loadModuleClass(module, webAccessPointClassname);
						//Create bundle web access point implementation
						Object accessPointImpl = accessPointClass.newInstance();
						if (accessPointImpl != null && accessPointImpl instanceof BundleWebAccessPoint) {
							((BundleWebAccessPoint)accessPointImpl).setModule(module);
							((BundleWebAccessPoint)accessPointImpl).setWebPartFolder(webPartConfig.getFolder());
							((BundleWebAccessPoint)accessPointImpl).setMessageSourceName(webPartConfig.getMessagesource());
							((BundleWebAccessPoint)accessPointImpl).setDefaultResource(webPartConfig.getDefault());
						}
						
						//Create the web access point
						WebPart wp = new WebPart();
						wp.setName(webPartConfig.getName());
						wp.setWebAccessPointClassName(webAccessPointClassname);
						wp.setWebAccessPoint((BundleWebAccessPoint)accessPointImpl);
						
						//Add web access point to current container
						this.addWebPart(wp);
					}
				}
				
				//Add home pages
				if (moduleConfiguration.getHomepage() != null) {
					for(MMPConfig.Homepage homepage : moduleConfiguration.getHomepage()){
						//Service
						final Service service = new Service();
						service.setId(homepage.getService());
						//Home page URL
						final String url = buildHomePageUrl(homepage);
						//Add home page
						this.addHomePageUrl(service, url);
					}
				}
			}
		}catch(InstantiationException ie){
			throw new MMPException(ie);
		}catch(IllegalAccessException iae){
			throw new MMPException(iae);
		}catch(MMPModuleException ce){
			throw new MMPException(ce);	
		}
	}
	
	/**
	 * Remove / unregistered web point access when module is unregistered.
	 * @param module Module
	 * @throws MMPException Error during remove access point.
	 */
	protected void removeModuleWebPart(Module module) throws MMPException {
		try{
			MMPConfig moduleConfiguration = ModuleContainerFactory.getInstance().getModuleContainer().getModuleConfiguration(module);
			if(moduleConfiguration != null){
				if ( moduleConfiguration.getWeb() != null ) {
					for(MMPConfig.Web webPartConfig : moduleConfiguration.getWeb()){
						WebPart webPart = new WebPart();
						webPart.setName(webPartConfig.getName());
						this.removeWebPart(webPart);
					}
				}
				if ( moduleConfiguration.getHomepage() != null ) {
					for(MMPConfig.Homepage homepageConfig : moduleConfiguration.getHomepage()){
						final Service service = new Service();
						service.setId(homepageConfig.getService());
						final String url = buildHomePageUrl(homepageConfig);
						this.removeHomePageUrl(service, url);
					}
				}
			}		
		}catch(MMPModuleException ce){
			throw new MMPException(ce);	
		}
	}

	/**
	 * Build the home page URL via the configuration 
	 * @param homepageConfig Home page configuration
	 * @return The URL
	 */
	private String buildHomePageUrl(Homepage homepageConfig) throws MMPException {
		String strUrl = homepageConfig.getUrl();
		String strWeb = homepageConfig.getWeb();
		if (strWeb != null && strWeb.trim().length() > 0) {
			//With web part link - Complete URL
			strUrl = "/services/web"
				+ ((!strWeb.startsWith("/")) ? "/" : "")
				+ strWeb.trim()
				+ ((!strWeb.endsWith("/") && !strUrl.startsWith("/")) ? "/" : "")
				+ strUrl.trim();
		}
		
		//Return URL
		return strUrl;
	}

	/**
	 * @see com.orange.mmp.core.ApplicationListener#initialize()
	 */
	public void initialize() throws MMPException {
		//Get the ModuleContainer and add itself as a ModuleObserver
		ModuleContainerFactory.getInstance().getModuleContainer().registerModuleObserver(this);
	}	
	
	/**
	 * @see com.orange.mmp.core.ApplicationListener#shutdown()
	 */
	public void shutdown() throws MMPException {
		//Unregistered module observer
		ModuleContainerFactory.getInstance().getModuleContainer().unregisterModuleObserver(this);
		if(webPartMap != null) webPartMap.clear();
		if(homePageByServiceMap != null) homePageByServiceMap.clear();
	}

	/**
	 * Get the home page URL.
	 * Use an internal map: service id => URL.
	 * If URL not found, return null. 
	 * @see com.orange.mmp.webpart.WebPartContainer#getHomePageUrl(com.orange.mmp.bind.data.service.Service)
	 */
	public String getHomePageUrl(final Service service) throws MMPException {
		final String result;
		if (service != null) {
			final String serviceId = service.getId();
			if (serviceId != null && serviceId.length() > 0) {
				result = homePageByServiceMap.get(serviceId);
			} else {
				//Service ID = null
				result = null;
			}
		} else {
			//Service = null
			result = null;
		}
		return result;
	}
	/**
	 * Add the URL into the internal map of URL by service id.
	 * @see com.orange.mmp.webpart.WebPartContainer#setHomePageUrl(com.orange.mmp.bind.data.service.Service, java.lang.String)
	 */
	public void addHomePageUrl(final Service service, final String homePageUrl)
			throws MMPException {
		if (service != null && homePageUrl != null) {
			final String serviceId = service.getId();
			if (serviceId != null && serviceId.length() > 0) {
				homePageByServiceMap.put(serviceId, homePageUrl);
			}
		}
	}
	/**
	 * Remove the URL.
	 * Verify if service and URL are equals before...
	 * @see com.orange.mmp.webpart.WebPartContainer#removeHomePageUrl(com.orange.mmp.bind.data.service.Service, java.lang.String)
	 */
	public void removeHomePageUrl(Service service, String homePageUrl)
			throws MMPException {
		if (service != null && homePageUrl != null) {
			final String serviceId = service.getId();
			final String url = homePageUrl.toString();
			if (serviceId != null && serviceId.length() > 0
					&& url != null && url.length() > 0) {
				//Verify URL
				final String testedUrl = homePageByServiceMap.get(serviceId);
				if (testedUrl != null && url.equals(testedUrl)) {
					homePageByServiceMap.remove(serviceId);
				}
			}
		}
	}

}
