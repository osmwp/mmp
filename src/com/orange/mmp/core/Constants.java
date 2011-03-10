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
package com.orange.mmp.core;

/**
 * Global Constants
 * 
 * @author Thomas MILLET
 *
 */
public class Constants {

	/******************************************************************************************
     * 	   GLOBAL
     ******************************************************************************************/

    /**
     * Default encoding used for whole app
     */
    public static final String DEFAULT_ENCODING = "UTF-8";
    
    /**
     * Default language
     */
    public static final String DEFAULT_LANGUAGE = "en";
	
    /**
     * Empty String constant
     */
    public static final String EMPTY_STRING = "";
    
    /******************************************************************************************
     * 	   MODULE CONFIGURATION AND COMPONENTS FACTORIES
     ******************************************************************************************/
    
    /**
     * Default Component key in Factories
     */
    public static final String DEFAULT_COMPONENT_KEY = "default";
    
    /**
     * JAXB module configuration package for unmarshalling 
     */
    public static final String MODULE_CONFIGURATION_PACKAGE = "com.orange.mmp.core.config";
    
    /**
     * Key property of a module indicating the file owning the module configuration
     */
    public static final String MODULE_CONFIGURATION_FILE_PROPERTY = "MMP-ConfigurationFile";
    
    /**
     * Default Path to configuration file in modules
     * 
     * If additional configuration parameters are needed, see existing
     * code and XSD to add entries in configuration file.
     */
    public static final String DEFAULT_MODULE_CONFIGURATION_FILE="/META-INF/mmp-config.xml";
   
    /******************************************************************************************
     * 	   MODULE CONFIGURATION AND COMPONENTS FACTORIES
     ******************************************************************************************/
    
    /**
     * Module category for Widget type
     */
    public static final String MODULE_CATEGORY_WIDGET = "widget";
    
    /**
     * Module category for Service type
     */
    public static final String MODULE_CATEGORY_SERVICE = "service";
    
    /**
     * Module category for Library type
     */
    public static final String MODULE_CATEGORY_LIBRARY = "library";
    
    /**
     * Module category for unknown type
     */
    public static final String MODULE_CATEGORY_UNKNOWN = "unknown";

}
