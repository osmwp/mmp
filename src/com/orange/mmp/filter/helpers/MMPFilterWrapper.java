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
package com.orange.mmp.filter.helpers;

import java.util.HashMap;
import java.util.Map;

import com.orange.mmp.context.RequestContext;
import com.orange.mmp.context.ResponseContext;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.filter.MMPFilter;

/**
 * Simple MMPFilter wrapping an inner MMPFilter and simple config
 * 
 * @author tml
 *
 */
public class MMPFilterWrapper implements MMPFilter {

	/**
	 * The wrapped filter
	 */
	private MMPFilter innerFilter;
	
	/**
	 * Settings mapped on an xml File
	 */
	private Map<String, Object> settings;
	
	/**
	 * Default wrapping constructor
	 * 
	 * @param innerFilter The mapped MMPFilter
	 * @param settings The mapped settings
	 */
	public MMPFilterWrapper(MMPFilter innerFilter, Map<String, Object> settings){
		this.innerFilter = innerFilter;
		if(settings != null)  this.settings = settings;
		else this.settings = new HashMap<String, Object>();
		
		if(this.innerFilter != null){
			Map<String, Object> innerSettings = this.innerFilter.getSettings();
			if(innerSettings != null){
				for(String settingName : innerSettings.keySet()){
					this.settings.put(settingName, innerSettings.get(settingName));
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.orange.mmp.filter.MMPFilter#doFilter(com.orange.mmp.context.RequestContext, com.orange.mmp.context.ResponseContext)
	 */
	public boolean doFilter(RequestContext request, ResponseContext response) throws MMPException {
		if(this.innerFilter != null){
			return this.innerFilter.doFilter(request, response);
		}
		else return true;
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.filter.MMPFilter#getSettings()
	 */
	public Map<String, Object> getSettings() {
		return this.settings;
	}
}
