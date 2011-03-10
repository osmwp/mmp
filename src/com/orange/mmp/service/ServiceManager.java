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
package com.orange.mmp.service;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.orange.mmp.core.ApplicationListener;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.data.Service;
import com.orange.mmp.dao.DaoManagerFactory;
import com.orange.mmp.dao.MMPDaoException;

/**
 * Manager used to configure and handle services localization
 * @author nmtv3386
 */
public class ServiceManager implements ApplicationListener{

	/**
	 * Singleton for access outside application context
	 */
	private static ServiceManager serviceManagerSingleton;
	
    /**
     * Indicates the default service key to use when no service found
     */
    private Service defaultService;

    /**
	* Singleton access 
	* 
	* @return The ServiceManager singleton
	*/
	public static ServiceManager getInstance(){
		return ServiceManager.serviceManagerSingleton;
	}
	
    /**
     * Initialise ServiceManager
     */
	public void initialize() throws MMPException {
		serviceManagerSingleton = this;
	}

    /**
     * Shutdown ServiceManager
     */
	public void shutdown() throws MMPException {
		serviceManagerSingleton = null;		
	}
	
   
    /**
     * Get a service instance from its id
     * @param id The service id
     * @return A service instance
     */
    @SuppressWarnings("unchecked")
	public Service getServiceById(String id) throws MMPException{
    	Service service = new Service();
    	service.setId(id);
    	Service services[] = (Service[])DaoManagerFactory.getInstance().getDaoManager().getDao("service").find(service);
    	if(services.length > 0) return services[0];
    	else return getDefaultService();
    }

    /**
     * Get a service from a request
     * @param request Instance of current http request
     * @return A service instance
     */
    public Service getServiceByHostname(HttpServletRequest request) throws MMPException{
    	Service service = null;
    	
    	//Search service via the X-Forwarded-Host (in HTTP header)
    	String queriedHost = request.getHeader(com.orange.mmp.mvc.Constants.HTTP_HEADER_X_FORWARDED_HOST);
    	if(queriedHost != null){
    		service = findServiceByAHostname(queriedHost);
    	}
    	
    	//If service is not found, search service via the X-Forwarded-Server
    	if(service == null) {
    		queriedHost = request.getHeader(com.orange.mmp.mvc.Constants.HTTP_HEADER_X_FORWARDED_SERVER);
    		if(queriedHost != null){
        		service = findServiceByAHostname(queriedHost);
        	}
    	}
    	
    	//If service is not found, search via the request server name
    	if(service == null) {
    		queriedHost = request.getServerName();
    		if(queriedHost != null){
        		service = findServiceByAHostname(queriedHost);
        	}
    	}
    	
    	//If service is not found, service not exist ! Use the default service.
    	if(service == null) {
    		service = getDefaultService();
    	}
    	
    	return service;
    }
    
    /**
     * Get the service via the host name
     * @param hostName host name
     * @return The service
     * @throws MMPDaoException Error during load service
     */
    @SuppressWarnings("unchecked")
	private Service findServiceByAHostname(final String hostName) throws MMPDaoException {
    	Service service = null;
    	if(hostName != null) {
    		final Pattern hostPattern = Pattern.compile("([^,]*),");
    		String queriedHost = hostName;
    		Matcher hostMatcher = hostPattern.matcher(hostName);
    		if(hostMatcher.find()) {
    			queriedHost = hostMatcher.group(1);
    		}

    		Service searchedService = new Service();
    		searchedService.setHostname(queriedHost);
    		Service services[] = (Service[])DaoManagerFactory.getInstance().getDaoManager().getDao("service").find(searchedService);
    		if(services.length > 0) {
    			service = services[0];
    		}
    	}
    	return service;
    }
    
    /**
     * Get the default service
     * @return The default service instance
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
	public Service getDefaultService() throws MMPException{
    	if(defaultService == null){
    		Service service = new Service();
    		service.setIsDefault(true);
    		Service []services = (Service[])DaoManagerFactory.getInstance().getDaoManager().getDao("service").find(service);
    		if(services.length == 0) {
    			throw new MMPException("No default service found");
    		}
    		this.defaultService = services[0];
    	}
    	return defaultService;
    }
}
