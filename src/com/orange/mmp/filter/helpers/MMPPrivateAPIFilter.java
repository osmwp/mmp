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

import com.orange.mmp.core.MMPException;
import com.orange.mmp.filter.MMPFilter;
import com.orange.mmp.filter.MMPFilterDispatcher;

/**
 * Specialization of MMPFilterDispatcher for Private MMP API
 * 
 * @author tml
 *
 */
public class MMPPrivateAPIFilter extends MMPFilterDispatcher {

	/**
	 * Attribute name used to define if filter is on private Web API
	 */
	public static final String FILTER_SETTINGS_PRIVATE = "private";
	
	/* (non-Javadoc)
	 * @see com.orange.mmp.filter.MMPFilterDispatcher#supportFilter(com.orange.mmp.filter.MMPFilter)
	 */
	@Override
	protected boolean supportFilter(MMPFilter filter) throws MMPException {
		return filter.getSettings() != null 
				&& filter.getSettings().get(FILTER_SETTINGS_PRIVATE) != null
				&& ((String)filter.getSettings().get(FILTER_SETTINGS_PRIVATE)).equalsIgnoreCase("true");
	}
}
