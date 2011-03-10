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
package com.orange.mmp.net;

/**
 * Interface ConnectionManager to get a connection on any type
 * of protocol (HTTP, HTTPS, FTP ...)
 * 
 * TODO Evolution P2 - Implements others protocols (FTP, SMTP ...)
 * 
 * @author Thomas MILLET
 *
 */
public interface ConnectionManager {

	/**
	 * HTTP protocol scheme
	 */
	public static final String PROTOCOL_SCHEME_HTTP = "http";
	
	/**
	 * HTTPS protocol scheme
	 */
	public static final String PROTOCOL_SCHEME_HTTPS = "https";
	
	/**
	 * SOCKET protocol scheme
	 */
	public static final String PROTOCOL_SCHEME_SOCKET = "socket";
	
	/**
	 * Get a new Connection instance based on current ConnectionManager handled
	 * protocol. Once this connection is no longer used, remember to release it
	 * using releaseConnection().
	 * 
	 * @return A new Connection instance
	 * @throws MMPNetException
	 */
	public Connection getConnection() throws MMPNetException;
	
	/**
	 * Release connection and linked resources (sockets, pool ...)
	 * 
	 * @param connection The connection to release
	 * @throws MMPNetException
	 */
	public void releaseConnection(Connection connection) throws MMPNetException;
	
}
