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

import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.data.Service;
import com.orange.mmp.service.ServiceManager;


/**
 * Public exposition of MMPRestServlet
 * 
 * @author Thomas MILLET
 *
 */
@SuppressWarnings("serial")
public class MMPHomePageServlet extends HttpServlet {
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try {
			
			//Extract the service
			Service service = null; 
			service = ServiceManager.getInstance().getServiceByHostname(request);
	    	
			//Get the home page for the service
			final WebPartContainer webPartContainer;
	    	webPartContainer = WebPartContainerFactory.getInstance().getWebPartContainer();
	    	String url = webPartContainer.getHomePageUrl(service);

	    	if (url != null) {
	    		//Call the page URL page
	    		
	    		//Verify URL
	    		if (! url.startsWith("http")) {
	    			//Local URL
	    			//Add context path if necessary
	    			String contextPath = request.getContextPath();
	    			if ( contextPath != null && contextPath.length() > 0 ) {
	    				url =
	    					contextPath
	    					+ ((!contextPath.endsWith("/") && !url.startsWith("/")) ? "/" : "")
	    					+ url;
	    			}
	    		}
	    		
	    		//Call URL
	    		response.sendRedirect(url);
	    		//response.sendRedirect("http://www.google.com/");
	    	} else {
				// Error - resource not found
	    		if (service != null) {
	    			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found in the " + service.getId() + " service." );
	    		} else {
	    			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found. Service in not define." );
	    		}
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

