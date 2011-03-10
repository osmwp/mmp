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
package com.orange.mmp.net.http;

/**
 * List all parameters available in HttpConnection properties
 * 
 * @author tml
 *
 */
public class HttpConnectionParameters {

	/**
	 * IN Property key : Http Authent User
	 */
	public static final String PARAM_IN_CREDENTIALS_USER = "http.credentials.user";
	
	/**
	 * IN Property key : Http Authent Password
	 */
	public static final String PARAM_IN_CREDENTIALS_PASSWORD = "http.credentials.password";
	
	/**
	 * IN Property key : configure the HTTP Method used (default is GET)
	 */
	public static final String PARAM_IN_HTTP_METHOD= "http.method";
	
	/**
	 * IN Property key : configure the HTTP client to handle redirection (default is true)
	 */
	public static final String PARAM_IN_HTTP_HANDLE_REDIRECT= "http.redirect";
	
	/**
	 * IN Property key : configure the HTTP client to use proxy if set (default is true)
	 */
	public static final String PARAM_IN_HTTP_USE_PROXY= "http.proxy";
	
	/**
	 * IN Property key : configure the HTTP client socket timeout
	 */
	public static final String PARAM_IN_HTTP_SOCKET_TIMEOUT = "http.socket.timeout";
	
	/**
	 * OUT Property key : HTTP status code
	 */
	public static final String PARAM_OUT_HTTP_STATUS_CODE = "http.statuscode";
	
	/**
	 * Property value for HTTP_METHOD : use GET method
	 */
	public static final String HTTP_METHOD_GET = "GET";
	
	/**
	 * Property value for HTTP_METHOD : use POST method
	 */
	public static final String HTTP_METHOD_POST = "POST";
	
	/**
	 * Property value for HTTP_METHOD : use HEAD method
	 */
	public static final String HTTP_METHOD_HEAD = "HEAD";
	
	/**
	 * Property value for HTTP_METHOD : use PUT method
	 */
	public static final String HTTP_METHOD_PUT = "PUT";
	
	/**
	 * Property value for HTTP_METHOD : use DEL method
	 */
	public static final String HTTP_METHOD_DEL = "DEL";
	
	/**
	 * Property value for HTTP_METHOD : use OPTIONS method
	 */
	public static final String HTTP_METHOD_OPTIONS = "OPTIONS";
	
	/**
	 * Property value for HTTP_METHOD : use TRACE method
	 */
	public static final String HTTP_METHOD_TRACE = "TRACE";
}
