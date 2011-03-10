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

import java.util.Map;

import com.orange.mmp.context.RequestContext;
import com.orange.mmp.context.ResponseContext;
import com.orange.mmp.core.MMPException;

/**
 * Web Filters used on MMP, adaptation of javax.servlet.Filter
 * 
 * @author tml
 */
public interface MMPFilter {

	/**
	 * Main filtering method
	 *
	 * @param request The client request
	 * @param response The server response

	 * @return True if filtering chain must go on, false if other filters must be bypassed or response sent directly on error or redirect
	 * 
	 * @throws MMPException
	 */
	public boolean doFilter(RequestContext request, ResponseContext response) throws MMPException;
	
	/**
	 * Get The filter settings
	 * 
	 * @return A Map containing filter settings
	 */
	public Map<String, Object> getSettings();
}
