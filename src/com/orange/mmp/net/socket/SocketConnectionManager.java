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

import com.orange.mmp.core.ApplicationListener;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.net.Connection;
import com.orange.mmp.net.ConnectionManager;
import com.orange.mmp.net.MMPNetException;

/**
 * ConnectionManager Implementation for Socket connections
 * 
 * TODO Implements SocketConnection
 * 
 * @author Thomas MILLET
 *
 */
public class SocketConnectionManager implements ApplicationListener, ConnectionManager {

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
	
	public void initialize() throws MMPException {

	}

	public void shutdown() throws MMPException {

	}

}
