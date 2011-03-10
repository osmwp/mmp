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

import java.io.IOException;
import java.util.List;

import com.orange.mmp.core.data.Service;
import com.orange.mmp.context.RequestContext;
import com.orange.mmp.context.ResponseContext;
import com.orange.mmp.context.UserContext;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.data.WebPart;

/**
 * @author rmxc7111
 *
 */
public interface WebPartContainer {

	/**
	 * Adds a web access into a bundle
	 * 
	 * @param webPart The new web part
	 * @throws MMPException Error during operation.
	 */
	public void addWebPart(WebPart webPart) throws MMPException;
	
	/**
	 * Removes a registered web access
	 * @param webPart The web part
	 * @throws MMPException Error during operation.
	 */
	public void removeWebPart(WebPart webPart) throws MMPException;
	
	/**
	 * Gets a web access point to web resources access into bundle.
	 * @param service Service (web part access name)
	 * @return The web part for the service.
	 * @throws MMPException Error during operation.
	 */
	public WebPart getWebPart(String service) throws MMPException;
	
	/**
	 * Gets the list of bundle web access points.
	 * @return List of web parts
	 * @throws MMPException Error during operation.
	 */
	public List<WebPart> listWebParts() throws MMPException;
	
	/**
	 * Allow to access at web resources in bundle (process an http request).
	 * 
	 * @param webPart Access point.
	 * @param requestContext Request context (the request completed with MMP data) 
	 * @param userContext User context (the request completed with MMP data) 
	 * @param responseContext response (contains the response)
	 * @throws MMPException Error during operation.
	 * @throws ServletException Error during operation.
	 * @throws IOException Error during operation.
	 */
	public void invoke(WebPart webPart, RequestContext requestContext, UserContext userContext, ResponseContext responseContext) throws MMPException;

	/**
	 * Gets an URL for a service home page.
	 * @param service Service (service defines on MMP server).
	 * @return The home page URL (or null if service not found or if service has not URL).
	 * @throws MMPException Error during operation.
	 */
	public String getHomePageUrl(Service service) throws MMPException;
	/**
	 * Add an URL for a service home page.
	 * @param service Service (service defines on MMP server).
	 * @param homePageUrl The home page URL for this service.
	 * @throws MMPException Error during operation.
	 */
	public void addHomePageUrl(Service service, String homePageUrl) throws MMPException;
	/**
	 * Remove an URL for a service home page.
	 * @param service Service (service defines on MMP server).
	 * @param homePageUrl The home page URL for this service.
	 * @throws MMPException Error during operation.
	 */
	public void removeHomePageUrl(Service service, String homePageUrl) throws MMPException;
	
}
