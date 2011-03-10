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
 * Generic abstraction class used to encapsulate a message.
 * 
 * @author Thomas MILLET
 *
 */
@SuppressWarnings("serial")
public class Message implements Serializable{
		
	/**
	 * The message ID (hashcode base)
	 */
	private Serializable id;
	
	/**
	 * The message data
	 */
	private Serializable data;
	
	/**
	 * Default constructor
	 */
	public Message() {
		super();
	}

	/**
	 * @return the data
	 */
	public Serializable getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(Serializable data) {
		this.data = data;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Message){
			return this.id.equals(((Message)obj).id);
		}
		else return false;
	}

	/**
	 * @return the id
	 */
	public Serializable getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Serializable id) {
		this.id = id;
	}
}
