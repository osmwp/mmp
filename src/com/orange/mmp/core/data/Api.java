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

/**
 * Abstraction class for services definition.
 * 
 * @author Thomas MILLET
 *
 */
@SuppressWarnings("serial")
public class Api implements Serializable{
	
	/**
	 * Name of the service used as API entry point in calls
	 */
	private String name;
	
	/**
	 * Class defining the service API (Interface)
	 */
	@SuppressWarnings("unchecked")
	private transient Class definitionClass;
	
	/**
	 * Indicates if the service is published as a WebService
	 * (exposure depends on ServiceContainer)
	 */
	private boolean isPublished;
	
	/**
	 * Indicates if the service is shared among MMP instances
	 * (exposure depends on MessageBroker)
	 */
	private boolean isShared;
	
	/**
	 * Indicates if the service is available on Public API
	 */
	private boolean isPublic;
	
	/**
	 * Indicates if the service is available on Private API
	 */
	private boolean isPrivate;
	
	/**
	 * Indicates the service error type (body or http)
	 */
	private String errorType;
	
	/**
	 * Default Constructor for serialization purpose
	 */
	public Api() {
		super();
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

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Api){
			return this.name.equals(((Api)obj).name);
		}
		else return false;
	}


	/**
	 * @return the definitionClass
	 */
	@SuppressWarnings("unchecked")
	public Class getDefinitionClass() {
		return definitionClass;
	}

	/**
	 * @param definitionClass the definitionClass to set
	 */
	@SuppressWarnings("unchecked")
	public void setDefinitionClass(Class definitionClass) {
		this.definitionClass = definitionClass;
	}
	
	/**
	 * @param isShared the isShared to set
	 */
	public void setShared(boolean isShared){
		this.isShared = isShared;
	}
	
	/**
	 * @return true is the service is shared (false otheriwse)
	 */
	public boolean isShared(){
		return this.isShared;
	}

	/**
	 * @return the isPublished
	 */
	public boolean isPublished() {
		return isPublished;
	}

	/**
	 * @param isPublished the isPublished to set
	 */
	public void setPublished(boolean isPublished) {
		this.isPublished = isPublished;
	}

	/**
	 * @return the isPublic
	 */
	public boolean isPublic() {
		return isPublic;
	}

	/**
	 * @param isPublic the isPublic to set
	 */
	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	/**
	 * @return the isPrivate
	 */
	public boolean isPrivate() {
		return isPrivate;
	}

	/**
	 * @param isPrivate the isPrivate to set
	 */
	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	/**
	 * @return the errorType
	 */
	public String getErrorType() {
		return errorType;
	}

	/**
	 * @param errorType the errorType to set
	 */
	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}
	
}
