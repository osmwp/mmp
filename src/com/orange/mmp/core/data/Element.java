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
 * Container key/value, mainly used in Map to store
 * an object using pattern Map.put(key,Element(key,value))
 * 
 * @author Thomas MILLET
 *
 */
@SuppressWarnings("serial")
public class Element implements Serializable{
	
	/**
	 * The element key
	 */
	private Object key;
	
	/**
	 * The element value
	 */
	private Object value;

	/**
	 * No Arg constructor
	 */
	public Element(){
		super();
	}
	
	/**
	 * Constructor using fields
	 * 
	 * @param key The element key
	 * @param value The element value
	 */
	public Element(Object key, Object value) {
		super();
		this.key = key;
		this.value = value;
	}

	/**
	 * @return the key
	 */
	public Object getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(Object key) {
		this.key = key;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.key.hashCode();
	}

	

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Element){
			return this.key.equals(((Element)obj).key);
		}
		else return false;
	}

}
