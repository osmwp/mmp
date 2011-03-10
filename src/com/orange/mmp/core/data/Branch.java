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

import java.util.List;

/**
 * Abtsraction class for Branch definition
 * 
 * @author kedingau
 *
 */
public class Branch {
	
	/**
	 * Indicates if current branch is the default one
	 */
	private boolean isDefault;
	
	/**
	 * The branch id
	 */
    private String id;

	/**
	 * The branch name
	 */
	private String name;

    /**
     * The branch human readable definition
     */
    private String description;
    
    /**
     * The mobiles bound to this branch
     */
    private List<Mobile> mobiles;
    

    /**
     * Default constructor
     */
	public Branch() {
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
		if(obj instanceof Branch) {
			return (this.id).equals(((Branch)obj).id);
		}
		else return false;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the mobiles
	 */
	public List<Mobile> getMobiles() {
		return mobiles;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param mobiles the mobiles to set
	 */
	public void setMobiles(List<Mobile> mobiles) {
		this.mobiles = mobiles;
	}

	/**
	 * @return the isDefault
	 */
	public boolean isDefault() {
		return isDefault;
	}

	/**
	 * @param isDefault the isDefault to set
	 */
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
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

}
