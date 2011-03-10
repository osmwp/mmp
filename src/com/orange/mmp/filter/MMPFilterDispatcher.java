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
package com.orange.mmp.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import com.orange.mmp.context.RequestContext;
import com.orange.mmp.context.ResponseContext;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.ApplicationListener;
import com.orange.mmp.core.config.MMPConfig;
import com.orange.mmp.core.data.Element;
import com.orange.mmp.core.data.Module;
import com.orange.mmp.core.data.Service;
import com.orange.mmp.filter.helpers.MMPFilterWrapper;
import com.orange.mmp.module.ModuleContainerFactory;
import com.orange.mmp.module.ModuleEvent;
import com.orange.mmp.module.ModuleObserver;
import com.orange.mmp.service.ServiceManager;

/**
 * Filter Dispatcher of MMP used to extends Servlet Filters Features
 * 
 * @author tml
 */
public class MMPFilterDispatcher implements Filter, ApplicationListener, ModuleObserver  {
    
	/**
	 * Attribute name used to define filter attached service
	 */
	public static final QName CUFILTER_CONFIG_ATTR_SERVICE = new QName("service");
	
	/**
	 * Attribute name used to define filter class
	 */
	public static final QName CUFILTER_CONFIG_ATTR_CLASSNAME = new QName("classname");
	
	/**
	 * Attribute name used to define filter priority in filters chain
	 */
	public static final QName CUFILTER_CONFIG_ATTR_PRIORITY = new QName("priority");
	
	/**
   	 * An inner Lock for filter settings
   	 */
   	private final Lock lock = new ReentrantLock();
   	
    /**
     * List of filters by Service
     */
    private Map<Service, List<Element>> filters; 
    
    
	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		Service service = null; 
		try{	
			service = ServiceManager.getInstance().getServiceByHostname((HttpServletRequest)servletRequest);
			List<Element> serviceFilters = this.filters.get(service);
			RequestContext request = new RequestContext((HttpServletRequest)servletRequest);
			ResponseContext response = new ResponseContext((HttpServletResponse)servletResponse);
			if(serviceFilters != null){
				for(Element element : serviceFilters){
					MMPFilter filter = (MMPFilter)element.getValue();
					if(!filter.doFilter(request, response)){
						break;
					}
				}
			}
			if(response.getStatusCode() != HttpServletResponse.SC_OK){
				if(response.getStatusMessage() != null){
					((HttpServletResponse)servletResponse).sendError(response.getStatusCode(), response.getStatusMessage());
					return;
				}
				else{
					((HttpServletResponse)servletResponse).setStatus(response.getStatusCode());
					return;
				}
			}
			
			filterChain.doFilter(request, response);
			
		}catch(MMPException me){
			((HttpServletResponse)servletResponse).sendError(HttpServletResponse.SC_BAD_REQUEST, me.getMessage());
			return;
		}		
	}
	

	/* (non-Javadoc)
	 * @see com.orange.mmp.core.ApplicationListener#initialize()
	 */
	public void initialize() throws MMPException {
		this.filters = new HashMap<Service, List<Element>>();
		
		//Get the ModuleContainer and add itself as a ModuleObserver
		ModuleContainerFactory.getInstance().getModuleContainer().registerModuleObserver(this);
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.core.ApplicationListener#shutdown()
	 */
	public void shutdown() throws MMPException {
		this.filters.clear();
		
		//Get the ModuleContainer and add itself as a ModuleObserver
		ModuleContainerFactory.getInstance().getModuleContainer().unregisterModuleObserver(this);		
	}
	
	/* (non-Javadoc)
	 * @see com.orange.mmp.module.ModuleObserver#onModuleEvent(com.orange.mmp.module.ModuleEvent)
	 */
	public void onModuleEvent(ModuleEvent moduleEvent) {
		try{
			if(moduleEvent.getType() == ModuleEvent.MODULE_ADDED){
				this.addModuleFilter(moduleEvent.getModule());
			}
			else if(moduleEvent.getType() == ModuleEvent.MODULE_REMOVED){
				this.removeModuleFilter(moduleEvent.getModule());
			}
		}catch(MMPException cce){
			//NOP - Just Log
		}
	}
	
	/**
	 * Adds a filter from a module configuration
	 * 
	 * @param module The module owning the filter
	 * @throws MMPException
	 */
	protected void addModuleFilter(Module module) throws MMPException{
		MMPConfig moduleConfiguration = ModuleContainerFactory.getInstance().getModuleContainer().getModuleConfiguration(module);
		if(moduleConfiguration != null && moduleConfiguration.getFilter() != null){
			try{
				for(MMPConfig.Filter filterConfig : moduleConfiguration.getFilter()){
					if(filterConfig.getOtherAttributes() != null
							&& filterConfig.getOtherAttributes().get(CUFILTER_CONFIG_ATTR_SERVICE) != null
							&& filterConfig.getOtherAttributes().get(CUFILTER_CONFIG_ATTR_CLASSNAME) != null){
						String filterClassname = filterConfig.getOtherAttributes().get(CUFILTER_CONFIG_ATTR_CLASSNAME);
						HashMap<String, Object> filterSettings = new HashMap<String, Object>();
						for(QName attributeName : filterConfig.getOtherAttributes().keySet()){
							filterSettings.put(attributeName.getLocalPart(), filterConfig.getOtherAttributes().get(attributeName));
						}
						MMPFilterWrapper filterImpl = new MMPFilterWrapper((MMPFilter) ModuleContainerFactory.getInstance().getModuleContainer().loadModuleClass(module, filterClassname).newInstance(),filterSettings);
						
						//Try to add filter
						this.addFilter(filterConfig.getName(), filterImpl);
					}
				}
			}catch(IllegalAccessException iae){
				throw new MMPException("Failed to add filters in module "+module.getName(),iae);
			}catch(InstantiationException ie){
				throw new MMPException("Failed to add filters in module "+module.getName(),ie);
			}
		}
	}
	
	/**
	 * Removes a filter from a module configuration
	 * 
	 * @param module The module owning the filter
	 * @throws MMPException
	 */
	protected void removeModuleFilter(Module module) throws MMPException{
		MMPConfig moduleConfiguration = ModuleContainerFactory.getInstance().getModuleContainer().getModuleConfiguration(module);
		if(moduleConfiguration != null && moduleConfiguration.getFilter() != null){
			for(MMPConfig.Filter filterConfig : moduleConfiguration.getFilter()){
				if(filterConfig.getOtherAttributes() != null
						&& filterConfig.getOtherAttributes().get(CUFILTER_CONFIG_ATTR_SERVICE) != null
						&& filterConfig.getOtherAttributes().get(CUFILTER_CONFIG_ATTR_CLASSNAME) != null){
					HashMap<String, Object> filterSettings = new HashMap<String, Object>();
					for(QName attributeName : filterConfig.getOtherAttributes().keySet()){
						filterSettings.put(attributeName.getLocalPart(), filterConfig.getOtherAttributes().get(attributeName));
					}
					MMPFilterWrapper filterImpl = new MMPFilterWrapper(null,filterSettings);
					
					//Try to remove filter
					this.removeFilter(filterConfig.getName(), filterImpl);
				}
			}
		}
	}
	
	/**
	 * This method must be overidden to define supported filter by actual instance,
	 * default instance return true and supports all filters
	 * 
	 * @param filter The filter to check

	 * @return True if filter is applied to current instance, false otherwise
	 * @throws MMPException
	 */
	protected boolean supportFilter(MMPFilter filter) throws MMPException{
		return true;
	}
	
	/**
	 * Adds a filter to Dispatcher
	 * 
	 * @param name The filter name
	 * @param filter The MMPFilter to check and add
	 * @throws MMPException
	 */
	protected void addFilter(String name, MMPFilter filter) throws MMPException{
		try{
			lock.lock();
			if(filter.getSettings() == null || filter.getSettings().get(CUFILTER_CONFIG_ATTR_SERVICE.getLocalPart()) == null){
				throw new MMPException("No service found in filter settings");
			}
			Service service = ServiceManager.getInstance().getServiceById((String)filter.getSettings().get(CUFILTER_CONFIG_ATTR_SERVICE.getLocalPart()));
			int priority = 0; 
			try{	
				priority = Integer.parseInt((String)filter.getSettings().get(CUFILTER_CONFIG_ATTR_PRIORITY.getLocalPart()));
			}catch(NumberFormatException nfe){
				//NOP Not critical
			}
			if(this.supportFilter(filter)){
				Element filterElement = new Element(name,filter);
				List<Element> serviceFilters = this.filters.get(service);
				if(serviceFilters == null) serviceFilters = new LinkedList<Element>();
				else serviceFilters.remove(filterElement);
				int currentPos = 0;
				for(Element element : serviceFilters){
					MMPFilter currentFilter = (MMPFilter)element.getValue();
					int currentPriority = Integer.parseInt((String)currentFilter.getSettings().get(CUFILTER_CONFIG_ATTR_PRIORITY.getLocalPart()));
					if(currentPriority > priority){
						serviceFilters.add(currentPos,filterElement);
						break;
					}
					else currentPos++;
					
				}
				if(currentPos == serviceFilters.size()) serviceFilters.add(filterElement);
				this.filters.put(service, serviceFilters);
			}
		}finally{
			lock.unlock();
		}
	}
	
	/**
	 * Removes a filter from Dispatcher
	 * 
	 * @param name The filter name
	 * @param filter The MMPFilter to remove
	 * @throws MMPException
	 */
	protected void removeFilter(String name, MMPFilter filter) throws MMPException{
		try{
			lock.lock();
			if(filter.getSettings() == null || filter.getSettings().get(CUFILTER_CONFIG_ATTR_SERVICE.getLocalPart()) == null){
				throw new MMPException("No service found in filter settings");
			}
			Service service = ServiceManager.getInstance().getServiceById((String)filter.getSettings().get(CUFILTER_CONFIG_ATTR_SERVICE.getLocalPart()));
			List<Element> serviceFilters = this.filters.get(service);
			if(serviceFilters != null){
				serviceFilters.remove(name);
			}
			serviceFilters = this.filters.get(service);
			if(serviceFilters != null){
				serviceFilters.remove(name);
			}
			
		}finally{
			lock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		// NOP
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
		// NOP
	}
}


