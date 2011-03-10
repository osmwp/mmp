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
package com.orange.mmp.api.ws.rest;

import com.orange.mmp.api.MMPApiException;


/**
 * Private exposition of MMPRestServlet
 * 
 * @author Thomas MILLET
 *
 */
@SuppressWarnings("serial")
public class MMPRestPrivateServlet extends MMPRestServlet {
	
	/**
	 * Check that request is for a Private API
	 */
	protected void checkRequest(RestRequest restRequest) throws MMPApiException {
		if(!restRequest.getApi().isPrivate()){
			throw new MMPApiException("API '"+restRequest.getApi().getName()+"' not found");
		}
	}
}

