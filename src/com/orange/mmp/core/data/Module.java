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
import java.net.URI;
import java.util.Comparator;

/**
 * Abstraction class for module definition.
 * 
 * @author Thomas MILLET
 *
 */
@SuppressWarnings("serial")
public class Module implements Serializable, Comparable<Module>, Comparator<Module> {

	/**
	 * Unique identifier
	 */
	private String id;
	
	/**
	 * Name of the module
	 */
	private String name;
	
	/**
	 * Category of the module
	 */
	private String category;
	
	/**
	 * Version of the module
	 */
	private Version version;
	
	/**
	 * Source location of the module
	 */
	private URI location;
	
	/**
	 * Timestamp with the last modification date
	 */
	private long lastModified;
	
	/**
	 * Constructor using id
	 * @param id The module id
	 */
	public Module() {
		super();
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
		if(obj instanceof Module) {
			return (this.id).equals(((Module)obj).id);
		}
		else return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Module module) {
		if(this.getId() == null && module.getId() == null){
			return 0;
		}
		if(this.getId() == null){
			return -1;
		}
		else if(module.getId() == null){
			return 1;
		}
		else{
			return this.getId().compareTo(module.getId());
		}
	}

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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the version
	 */
	public Version getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(Version version) {
		this.version = version;
	}

	/**
	 * @return the location
	 */
	public URI getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(URI location) {
		this.location = location;
	}

	/**
	 * @return the lastModified
	 */
	public long getLastModified() {
		return lastModified;
	}

	/**
	 * @param lastModified the lastModified to set
	 */
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public int compare(Module module1, Module module2) {
		if(module1 instanceof Widget && module2 instanceof Widget) {
			if(module1.getName().toLowerCase().compareTo(module2.getName().toLowerCase()) == 0) {
				return ((Widget)module2).getBranchId().compareTo(((Widget)module1).getBranchId());
			}
		}
		return module2.getName().toLowerCase().compareTo(module1.getName().toLowerCase());
	}
		
}
