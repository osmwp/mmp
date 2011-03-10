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
package com.orange.mmp.mvc;

/**
 * Defines main PFS constants
 * @author milletth
 *
 */
public class Constants {

    /***************************
     * 	   HTTP HEADERS
     ****************************/

    /**
     * HTTP header indicating to update player (after v1.4) and location to get
     */
    public static final String HTTP_HEADER_UPDATE_PLAYER = "X-Update-Player";
    
    /**
     * HTTP header indicating to update service
     */
    public static final String HTTP_HEADER_UPDATE_SERVICE = "X-Update-Service";
    
    /**
     *  HTTP header to indicate update type
     */
    public static final String HTTP_HEADER_UPDATE_TYPE = "X-Update-Type";

    /**
     * HTTP header indicating the mobile User Agent
     */
    public static final String HTTP_HEADER_USERAGENT = "User-Agent";

    /**
     * HTTP header indicating the content type
     */
    public static final String HTTP_HEADER_CONTENTTYPE = "Content-Type";

    /**
     * HTTP header indicating the content length
     */
    public static final String HTTP_HEADER_CONTENTLENGTH = "Content-Length";

    /**
     * HTTP header indicating the content disposition
     */
    public static final String HTTP_HEADER_CONTENDISPOSITION = "Content-Disposition";

    /**
     * HTTP header If-Modified-Since
     */
    public static final String HTTP_HEADER_IFMODIFIEDSINCE = "If-Modified-Since";

    /**
     * HTTP header Last-Modified
     */
    public static final String HTTP_HEADER_LASTMODIFIED = "Last-Modified";

    /**
     * HTTP header Date
     */
    public static final String HTTP_HEADER_DATE = "Date";

    /**
     * HTTP header Expires
     */
    public static final String HTTP_HEADER_EXPIRES = "Expires";

    /**
     * HTTP header for connection type
     */
    public static final String HTTP_HEADER_CONNECTION = "Connection";

    /**
     * HTTP header for connection type
     */
    public static final String HTTP_HEADER_PROXYCONNECTION = "Proxy-Connection";

    /**
     *  HTTP header for forwarded host using reverse proxy
     */
    public static final String HTTP_HEADER_X_FORWARDED_HOST = "X-Forwarded-Host";

    /**
     *  HTTP header for forwarded server using reverse proxy
     */
    public static final String HTTP_HEADER_X_FORWARDED_SERVER = "X-Forwarded-Server";
    
    /**
     *  HTTP header to indicate minor update type
     */
    public static final int HTTP_HEADER_UPDATE_TYPE_MINOR = 2;
    
    /**
     *  HTTP header to indicate major update type
     */
    public static final int HTTP_HEADER_UPDATE_TYPE_MAJOR = 1;
    
    /**
     * HTTP header indicating origin country (Orange only)
     */
    public static final String HTTP_HEADER_COUNTRY = "COUNTRY";
    
    /**
     * HTTP header indicating origin country set by MEMO player
     */
    public static final String HTTP_HEADER_COUNTRY_MEMO = "X-Country-Memo";
    
    /**
     * HTTP header indicating origin language set by MEMO player
     */
    public static final String HTTP_HEADER_LANGUAGE_MEMO = "X-Language-Memo";
    
    /**
     * HTTP header indicating the ZIP content type
     */
    public static final String HTTP_HEADER_ZIPCONTENT = "application/zip";
    
   
    /***************************
     * 	   HTTP PARAMETERS
     ****************************/

    /**
     * Mobile Phone Number HTTP parameter
     */
    public static final String HTTP_PARAMETER_ND = "ND";


    /**
     * Mobile Client password HTTP parameter
     */
    public static final String HTTP_PARAMETER_PWD = "pwd";

    /**
     * Mobile Client password HTTP parameter
     */
    public static final String HTTP_PARAMETER_BADGE_URL = "badgeurl";


    /**
     * Mobile Client style HTTP parameter
     */
    public static final String HTTP_PARAMETER_STYLE = "style";

    /**
     * Confirmation used display confirmation page
     */
    public static final String HTTP_PARAMETER_CONFIRM = "confirm";

    /**
     * 	PFM type HTTP parameter
     */
    public static final String HTTP_PARAMETER_TYPE= "type";

    /**
     * Main application ID HTTP parameter
     */
    public static final String HTTP_PARAMETER_APPID = "appid";
    
    /**
     * Parameter indicating if the request is actually only tested
     */
    public static final String HTTP_PARAMETER_TEST = "test";

    /**
     * Mobile key HTTP parameter
     */
    public static final String HTTP_PARAMETER_KEY = "key";

    /**
     * Mobile user-agent (short) HTTP parameter
     */
    public static final String HTTP_PARAMETER_UA = "ua";

    /**
     * Generic ID HTTP parameter
     */
    public static final String HTTP_PARAMETER_ID = "id";

    /**
     * HTTP parameter used to simulate the hostname (testing/specific purpose)
     */
    public static final String HTTP_PARAMETER_HOST = "host";

    /**
     * 	Servlet request parameter for action
     */
    public static final String HTTP_PARAMETER_ACTION = "action";

    /**
     * 	Servlet request parameter for url (in cadap)
     */
    public static final String HTTP_PARAMETER_URL = "url";

    /**
     * 	Servlet request parameter for width (in cadap)
     */
    public static final String HTTP_PARAMETER_WIDTH = "width";

    /**
     * 	Servlet request parameter for height (in cadap)
     */
    public static final String HTTP_PARAMETER_HEIGHT = "height";

    /**
     * 	Servlet request parameter for zoom (in cadap)
     */
    public static final String HTTP_PARAMETER_ZOOM = "zoom";

    /**
     * 	Servlet request parameter for default download
     */
    public static final String HTTP_PARAMETER_DEFAULT = "default";

    /**
     * 	Servlet request parameter for msisdn
     */
    public static final String HTTP_PARAMETER_MSISDN = "msisdn";

    /**
     * 	Servlet request parameter to force/bypass midlet signature
     */
    public static final String HTTP_PARAMETER_SIGN = "sign";

    /***************************
     * 	    	MVC
     ****************************/

    /**
     * 	Servlet request action parameter value for deletion
     */
    public static final String HTTP_ACTION_REMOVE = "remove";

    /**
     * 	Servlet request action parameter value for add
     */
    public static final String HTTP_ACTION_ADD = "add";

    /**
     * 	Servlet request action parameter value for modification
     */
    public static final String HTTP_ACTION_MODIFY = "modify";

    /**
     * 	Servlet request action parameter value for validation
     */
    public static final String HTTP_ACTION_VALIDATE = "validate";
    
    /****************************
     * 	   FILES TYPES
     ****************************/
    public static final String MIMETYPE_PFM = "application/zip";
    
    public static final String MIMETYPE_WIDGET = "application/java-archive";
        
    public static final String MIMETYPE_CERTIF = "application/octet-stream";
    
    /****************************
     * 	   URL MAPPINGS
     ****************************/
    
    /**
     * 	OTA base url mapping
     */
    public static final String URL_MAPPING_OTA = "/OTA";
    
    /**
     * 	JSON bridge base url mapping (without JSON-RPC)
     */
    public static final String URL_MAPPING_JSONRPC = "/";
    
    /**
     * 	Ressource base path
     */
    public static final String WIDGET_RESOURCE_BASE = "/m4m";
    
}
