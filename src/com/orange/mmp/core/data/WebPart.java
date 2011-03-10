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
package com.orange.mmp.core.data;

import java.io.Serializable;

import com.orange.mmp.webpart.accesspoint.BundleWebAccessPoint;

/**
 * @author rmxc7111
 *
 */
@SuppressWarnings("serial")
public class WebPart  implements Serializable{
	
	/**
	 * Name of the service used as entry point
	 */
	private String name;
	
	/**
	 * The web entry point
	 */
	private transient BundleWebAccessPoint webAccessPoint;

	/**
	 * Servlet class name
	 */
	private String accessPointClassName;
	
	/**
	 * Default Constructor for serialization purpose
	 */
	public WebPart() {
		super();
		webAccessPoint = null;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}

	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if(obj instanceof Api){
			return this.name.equals(((WebPart)obj).name);
		}
		else return false;
	}

	/**
	 * Modify the web access point class name. It define the web entry point.
	 * @param accessPointClassName The new access point class name.
	 */
	public void setWebAccessPointClassName(final String accessPointClassName) {
		this.accessPointClassName = accessPointClassName;
	}
	/**
	 * Get the web access point class name. It define the web entry point.
	 * @return Access point class name.
	 */
	public String getServletClassName() {
		return accessPointClassName;
	}

	/**
	 * Modify the web access point.
	 * @param servlet The new web access point.
	 */
	public void setWebAccessPoint(final BundleWebAccessPoint webAccessPoint) {
		this.webAccessPoint = webAccessPoint;
	}

	/**
	 * Get the web access point.
	 * @return the access point.
	 */
	public BundleWebAccessPoint getWebAccessPoint() {
		return webAccessPoint;
	}



	
}
