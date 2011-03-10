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
package com.orange.mmp.webpart.template;

import java.net.URL;

import com.orange.mmp.core.data.Module;
import com.orange.mmp.module.MMPModuleException;
import com.orange.mmp.module.ModuleContainerFactory;

import freemarker.cache.URLTemplateLoader;

/**
 * @author rmxc7111
 *
 */
public class ModuleTemplateLoader extends URLTemplateLoader {

	/**
	 * Module (to load resources in bundle).
	 */
	private Module module = null;
	
	/**
	 * Constructor
	 */
	public ModuleTemplateLoader() {
	}

	/**
	 * Get the URL to read template (use the module to compute URL).
	 * @see freemarker.cache.URLTemplateLoader#getURL(java.lang.String)
	 */
	protected URL getURL(final String templatePath) {
		URL url = null;
		try {
			url = ModuleContainerFactory.getInstance().getModuleContainer().getModuleResource(module, templatePath);
		} catch (MMPModuleException e) {
			//Error - Log via AspectJ
		}
		return url;
	}


	/**
	 * Modify the module.
	 * Module is used to read resources in bundle.
	 * @param module Module
	 */
	public void setModule(Module module) {
		this.module = module;
	}
	/**
	 * Get the module.
	 * Module is used to read resources in bundle.
	 * @return The module
	 */
	public Module getModule() {
		return module;
	}

}
