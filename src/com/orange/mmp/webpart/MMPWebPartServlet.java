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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.orange.mmp.context.RequestContext;
import com.orange.mmp.context.ResponseContext;
import com.orange.mmp.context.UserContext;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.data.WebPart;
import com.orange.mmp.webpart.accesspoint.BundleWebAccessPoint;


/**
 * Public exposition of MMPRestServlet
 * 
 * @author Thomas MILLET
 *
 */
@SuppressWarnings("serial")
public class MMPWebPartServlet extends HttpServlet {
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try {
			
			//Get request context
			final RequestContext requestContext;
			if (request instanceof RequestContext) {
				requestContext = (RequestContext)request;
			} else {
				requestContext = new RequestContext(request);
			}
	
			//Get response context
			final ResponseContext responseContext;
			if (response instanceof ResponseContext) {
				responseContext = (ResponseContext)response;
			} else {
				responseContext = new ResponseContext(response);
			}
			
			//Get user context
			UserContext userContext = null;
			HttpSession session = request.getSession(false);
			if(session != null){
				userContext = (UserContext)session.getAttribute(UserContext.USER_CTX_STR_KEY);
    		}
			if(userContext == null){
				userContext = (UserContext)request.getAttribute(UserContext.USER_CTX_STR_KEY);
			}
			
			//Extract request path
			final String pathInfo = requestContext.getPathInfo();
			final String[] pathTokens = pathInfo.split("/");
	    				
	    	//Get service
	    	final String service;
	    	if (pathTokens.length > 1) {
	    		service = pathTokens[1];
	    	} else {
	    		service = "Service not found...";
	    	}
	    	
	    	//Add compute resource path to request 
	    	final StringBuffer resourceUrl = new StringBuffer("");
	    	if (pathTokens.length > 2) {
	    		for (int index = 2; index< pathTokens.length; index++) {
	    			final String value = pathTokens[index];
	    			resourceUrl.append("/");
	    			resourceUrl.append(value);
	    		}
    	    	request.setAttribute(
    	    			BundleWebAccessPoint.BUNDLE_WEB_ACCESS_POINT_RESOURCE_PATH,
    	    			resourceUrl.toString());
	    	} else {
	    		request.setAttribute(
	    				BundleWebAccessPoint.BUNDLE_WEB_ACCESS_POINT_RESOURCE_PATH,
	    				"");
	    	}
	    	
	    	//Get web part access point
	    	final WebPartContainer webPartContainer;
	    	webPartContainer = WebPartContainerFactory.getInstance().getWebPartContainer();
	    	final WebPart webPart = webPartContainer.getWebPart(service);
	    	
	    	if (webPart != null) {
	    		//Call the bundle servlet
	    		webPartContainer.invoke(webPart, requestContext, userContext, responseContext);
	    	} else {
				// Error during getServiceByHostname(...)
				responseContext.sendError(HttpServletResponse.SC_NOT_FOUND, "Service not found. Please indicate a valid service. Current service: " + service);
	    	}
	    	
			// 7 - return response
	    	//Do nothing :-p
	    	
		} catch (MMPException mmpe) {
			// Error during getServiceByHostname(...)
			response.sendError(HttpServletResponse.SC_NOT_FOUND, mmpe.getMessage());
		} catch (Exception e) {
			// Error during process
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		} finally {
			response.flushBuffer();
		}
	}
	
}

