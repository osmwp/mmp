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


import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpHost;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.params.HttpMethodParams;

import com.orange.mmp.core.ApplicationListener;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.net.Connection;
import com.orange.mmp.net.ConnectionManager;
import com.orange.mmp.net.MMPNetException;

/**
 * Implementation of ConnectionManager for HTTP 
 * 
 * @author Thomas MILLET
 *
 */
public class HttpConnectionManager implements ApplicationListener, ConnectionManager{
	
	/**
	 * Default HTTP Proxy port
	 */
	private static final int PROXY_DEFAULT_PORT = 8080;
	
	/**
	 * Proxy hostname
	 */
	protected static String proxyHost;
	
	/**
	 * Proxy port
	 */
	protected static int proxyPort;	
	
	private static org.apache.commons.httpclient.util.IdleConnectionTimeoutThread idleConnectionCleaner = new org.apache.commons.httpclient.util.IdleConnectionTimeoutThread();
	
	/**
	 * The inner HttpClient pool size
	 */
	private static final int POOL_SIZE = 1000;

	/**
	 * Contains a pool of HttpClient already initialized
	 */
	protected static LinkedBlockingQueue<HttpClient> httpClientPool = new LinkedBlockingQueue<HttpClient>(POOL_SIZE);
		
	public Connection getConnection() throws MMPNetException {
		SimpleHttpConnectionManager shcm = new SimpleHttpConnectionManager();
        
        HttpClientParams defaultHttpParams = new HttpClientParams();
        defaultHttpParams.setParameter(HttpMethodParams.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        defaultHttpParams.setParameter(HttpConnectionParams.TCP_NODELAY, true);
        defaultHttpParams.setParameter(HttpConnectionParams.STALE_CONNECTION_CHECK, false);
        defaultHttpParams.setParameter(HttpConnectionParams.SO_LINGER, 0);
        defaultHttpParams.setParameter(HttpClientParams.MAX_REDIRECTS, 3);
        defaultHttpParams.setParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, false);

		HttpClient httpClient2 = new HttpClient(defaultHttpParams, shcm);
        
		if(HttpConnectionManager.proxyHost != null) {
			defaultHttpParams.setParameter("http.route.default-proxy", new HttpHost(HttpConnectionManager.proxyHost, HttpConnectionManager.proxyPort)); // TODO Host configuration !
			if(HttpConnectionManager.proxyHost != null/* && this.useProxy*/) {
				HostConfiguration config = new HostConfiguration();
				config.setProxy(HttpConnectionManager.proxyHost, HttpConnectionManager.proxyPort);
				httpClient2.setHostConfiguration(config);
			} else {
				HostConfiguration config = new HostConfiguration();
				config.setProxyHost(null);
				httpClient2.setHostConfiguration(config);
			}
		}
		
		return new HttpConnection(httpClient2);
	}

	public void releaseConnection(Connection connection) throws MMPNetException {
		connection.close();	
	}

	public void initialize() throws MMPException {
        // prepare parameters
        for(int i=0; i < POOL_SIZE; i++){
    		HttpClient httpClient = new HttpClient();
    		httpClient.getParams().setParameter("http.protocol.version", HttpVersion.HTTP_1_1);
    		httpClient.getParams().setParameter("http.tcp.nodelay", true);
    		httpClient.getParams().setParameter("http.connection.stalecheck", false);
    		httpClient.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
    		idleConnectionCleaner.addConnectionManager(httpClient.getHttpConnectionManager());
    		httpClientPool.add(httpClient);
        }
     	idleConnectionCleaner.setConnectionTimeout(-1000);
     	idleConnectionCleaner.setTimeoutInterval(1000);
     	idleConnectionCleaner.start();
	}
	
	public void shutdown() throws MMPException {
		//NOP
		idleConnectionCleaner.shutdown();
	    httpClientPool.clear();
	}

	/**
	 * @param proxyHost the proxyHost to set
	 */
	public void setProxy(String proxy) {
		if(proxy != null && proxy.length() > 0) {
			int portIndex = proxy.indexOf(":");
			if(portIndex > 0){
				HttpConnectionManager.proxyHost = proxy.substring(0,portIndex);
				HttpConnectionManager.proxyPort = Integer.parseInt(proxy.substring(portIndex+1));
			} else {
				HttpConnectionManager.proxyHost = proxy;
				HttpConnectionManager.proxyPort = PROXY_DEFAULT_PORT;
			}
			
			//Configure Proxy into JVM properties
			System.setProperty("http.proxyHost", HttpConnectionManager.proxyHost);
			System.setProperty("http.proxyPort", Integer.toString(HttpConnectionManager.proxyPort)); 
		}
	}

}
