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
package com.orange.mmp.mvc.bundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.orange.mmp.core.data.Branch;
import com.orange.mmp.core.data.Mobile;
import com.orange.mmp.dao.DaoManagerFactory;
import com.orange.mmp.mvc.Constants;
import com.orange.mmp.widget.WidgetManager;

/**
 * Simple JavaBean-based servlet used to download resources contained in M4M on OSGI framework
 * @author milletth
 *
 */
public class Controller extends AbstractController{
    
    /**
     * The URL mapping for Bundles
     */
    private static String urlMapping;


    /* (non-Javadoc)
     * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @SuppressWarnings("unchecked")
	@Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	int pathInfoStart = request.getRequestURI().indexOf(urlMapping);
    	if(pathInfoStart < 0){
    		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    		response.setContentLength(0);
    		return null;
    	}
    	// Get user agent to obtain branchID
    	String userAgent = request.getHeader(Constants.HTTP_HEADER_USERAGENT);
    	Mobile mobile = new Mobile();
    	mobile.setUserAgent(userAgent);
    	Mobile[] mobiles = (Mobile[]) DaoManagerFactory.getInstance().getDaoManager().getDao("mobile").find(mobile);
    	String branchId = null;//Constants.DEFAULT_BRANCH_ID;
    	if(mobiles != null && mobiles.length > 0) {
    		branchId = mobiles[0].getBranchId();
    	} else {
    		Branch branch = new Branch();
    		branch.setDefault(true);
    		Branch[] branches = (Branch[]) DaoManagerFactory.getInstance().getDaoManager().getDao("branch").find(branch);
    		if(branches != null && branches.length > 0)
    			branchId = branches[0].getId();
    	}
    	
    	String pathInfo = request.getRequestURI().substring(pathInfoStart+urlMapping.length());
    	String requestParts[] = pathInfo.split("/");
    	String widgetId = null;
    	String resourceName = null;
    	InputStream input = null;
    	
    	if(requestParts.length > 2){
    		widgetId = requestParts[1];
    		resourceName = pathInfo.substring(widgetId.length()+2);
    	}
    	else{
    		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setContentLength(0);
			return null;
    	}
    	
   		input = WidgetManager.getInstance().getWidgetResource(resourceName, widgetId, branchId);
    	
   		if(input == null) {
    		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    		response.setContentLength(0);
    	} else {
    		ByteArrayOutputStream resourceBuffer = new ByteArrayOutputStream();
    		OutputStream output = response.getOutputStream();
    		try{
    			IOUtils.copy(input, resourceBuffer);
    			response.setContentLength(resourceBuffer.size());
    			resourceBuffer.writeTo(output);
    		}catch(IOException ioe){
    			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    			response.setContentLength(0);
    		}
    		finally{
    			if(input != null)input.close();
    			if(output != null)output.close();
    			if(resourceBuffer != null)resourceBuffer.close();
    		}
    	}
   		return null;
    }
    
	/**
	 * @return the urlMapping
	 */
	public static String getUrlMapping() {
		return urlMapping;
	}

	/**
	 * @param urlMapping the urlMapping to set
	 */
	public void setUrlMapping(String urlMapping) {
		Controller.urlMapping = urlMapping;
	}
}
