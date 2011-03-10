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

import java.io.InputStream;

/**
 /**
 * Abstraction of a Connection provided by a ConnectionManager
 * 
 * @author Thomas MILLET
 *
 */
public interface Connection {

	/**
	 * Initialize the Connection object for the specified endPoint
	 * 
	 * @param endPoint The remote end point to connect
	 * @param timeout The timeout for connection
	 * @throws MMPNetException
	 */
	public void init(String endPoint, int timeout) throws MMPNetException;
	
	/**
	 * Send data to the current connected connection
	 * 
	 * @param dataStream The data to send
	 * @throws MMPNetException
	 */
	public void sendData(InputStream dataStream) throws MMPNetException;
	
	/**
	 * Get data from the current connection connection
	 * 
	 * @return The end point data in an InputStream
	 * @throws MMPNetException
	 */
	public InputStream getData() throws MMPNetException;
	
	/**
	 * Adds a generic property to current Connection, it's up to the
	 * implementation to handle the properties (networks params, headers ...)
	 * 
	 * This method should be called before connect()
	 * 
	 * @param name The name of the property
	 * @param value The value of the property 
	 * @throws MMPNetException
	 */
	public void setProperty(String name, Object value) throws MMPNetException;
	
	/**
	 * Get a generic property from current Connection
	 * 
	 * This method should be called after connect() and getData()
	 * 
	 * @param name The name to the property
	 * @return The value of the property
	 * @throws MMPNetException
	 */
	public Object getProperty(String name) throws MMPNetException;
	
	/**
	 * Close the current Connection
	 * 
	 * @throws MMPNetException
	 */
	public void close() throws MMPNetException;
}
