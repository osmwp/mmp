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
package com.orange.mmp.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.orange.mmp.bind.BindingException;
import com.orange.mmp.bind.XMLBinding;
import com.orange.mmp.core.Constants;


/**
 * Generic Loader for MMP configuration.
 * 
 * This loader is based on mmp-config.xsd template and should load
 * specific configuration settings as DOM Elements and attributes. 
 * 
 * @author tml
 *
 */
public class MMPConfigLoader{

	/**
	 * The configuration file URL
	 */
	URL configurationURL;
	
	/**
	 * Default constructor
	 * @param configurationURL The configuration file URL
	 */
	public MMPConfigLoader(URL configurationURL) {
		this.configurationURL = configurationURL;
	}
	
	/**
	 * Get a MMPConfig configuration instance from the configuration file
	 * 
	 * @return A MMPConfig instance
	 * @throws MMPConfigException
	 */
	public MMPConfig getConfiguration() throws MMPConfigException {
		MMPConfig configuration = null;
		InputStream is = null;
		try{
			is = this.configurationURL.openConnection().getInputStream();
			configuration = (MMPConfig) new XMLBinding().read(is,Constants.MODULE_CONFIGURATION_PACKAGE, null);
		}catch(IOException ioe){
			throw new MMPConfigException("Failed to load configuration",ioe);
		}catch(BindingException be){
			throw new MMPConfigException("Failed to load configuration",be);
		}finally{
			try{
				if(is != null) is.close();
			}catch(IOException ioe){
				//NOP
			}
		}
		return configuration;
	}

}
