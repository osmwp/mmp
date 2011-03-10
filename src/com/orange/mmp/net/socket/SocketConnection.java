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
package com.orange.mmp.net.socket;

import java.io.InputStream;

import com.orange.mmp.net.Connection;
import com.orange.mmp.net.ConnectionManager;
import com.orange.mmp.net.MMPNetException;

/**
 * Connection implementation for Sockets
 * 
 * @author Thomas MILLET
 *
 */
public class SocketConnection implements Connection, ConnectionManager {

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.net.Connection#close()
	 */
	public void close() throws MMPNetException {

	}

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.net.Connection#getData()
	 */
	public InputStream getData() throws MMPNetException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.net.Connection#getProperty(java.lang.String)
	 */
	public Object getProperty(String name) throws MMPNetException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.net.Connection#init(java.lang.String, int)
	 */
	public void init(String endPoint, int timeout) throws MMPNetException {

	}

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.net.Connection#sendData(java.io.InputStream)
	 */
	public void sendData(InputStream dataStream) throws MMPNetException {

	}

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.net.Connection#setProperty(java.lang.String, java.lang.Object)
	 */
	public void setProperty(String name, Object value) throws MMPNetException {

	}

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.net.ConnectionManager#getConnection()
	 */
	public Connection getConnection() throws MMPNetException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.net.ConnectionManager#releaseConnection(com.orange.mmp.net.Connection)
	 */
	public void releaseConnection(Connection connection) throws MMPNetException {

	}

}
