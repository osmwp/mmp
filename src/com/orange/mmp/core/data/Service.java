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

import java.util.Map;


/***
 * Abstraction class for Service
 * @author milletth
 *
 */
public class Service {

	/**
	 * Service ID
	 */
	private String id;
	
	/**
	 * Indicates if this service is the default one
	 */
	private Boolean isDefault;
	
	/**
	 * Service hostname
	 */
	private String hostname;
	
	/**
	 * Indicates if service can use default client
	 */
	private Boolean usedefault;
	
	/**
	 * Indicates if service used signed client
	 */
	private Boolean signed;
	
	/**
	 * Indicates if client send its headers
	 */
	private Boolean wtheadersbymemo;
	
	/**
	 * Indicates if Jad entries must be compacted 
	 */
	private Boolean compactjadentries;
	
	/**
	 * List of additionnal Jad entries
	 */
	private Map<String, String> jadEntries;
	
	/**
	 * Indicates if Service has an homepage
	 */
	private Boolean homepage;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the isDefault
	 */
	public Boolean getIsDefault() {
		return isDefault;
	}

	/**
	 * @param isDefault the isDefault to set
	 */
	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	/**
	 * @return the hostname
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * @param hostname the hostname to set
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	/**
	 * @return the usedefault
	 */
	public Boolean getUsedefault() {
		return usedefault;
	}

	/**
	 * @param usedefault the usedefault to set
	 */
	public void setUsedefault(Boolean usedefault) {
		this.usedefault = usedefault;
	}

	/**
	 * @return the signed
	 */
	public Boolean getSigned() {
		return signed;
	}

	/**
	 * @param signed the signed to set
	 */
	public void setSigned(Boolean signed) {
		this.signed = signed;
	}

	/**
	 * @return the wtheadersbymemo
	 */
	public Boolean getWtheadersbymemo() {
		return wtheadersbymemo;
	}

	/**
	 * @param wtheadersbymemo the wtheadersbymemo to set
	 */
	public void setWtheadersbymemo(Boolean wtheadersbymemo) {
		this.wtheadersbymemo = wtheadersbymemo;
	}

	/**
	 * @return the compactjadentries
	 */
	public Boolean getCompactjadentries() {
		return compactjadentries;
	}

	/**
	 * @param compactjadentries the compactjadentries to set
	 */
	public void setCompactjadentries(Boolean compactjadentries) {
		this.compactjadentries = compactjadentries;
	}

	/**
	 * @return the jadEntries
	 */
	public Map<String, String> getJadEntries() {
		return jadEntries;
	}

	/**
	 * @param jadEntries the jadEntries to set
	 */
	public void setJadEntries(Map<String, String> jadEntries) {
		this.jadEntries = jadEntries;
	}

	/**
	 * @return the homepage
	 */
	public Boolean getHomepage() {
		return homepage;
	}

	/**
	 * @param homepage the homepage to set
	 */
	public void setHomepage(Boolean homepage) {
		this.homepage = homepage;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (this.id).hashCode();
	}

	

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Service) {
			return (this.id).equals(((Service)obj).id);
		}
		else return false;
	}
	
	
}
