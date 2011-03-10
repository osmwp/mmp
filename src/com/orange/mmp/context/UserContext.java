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
package com.orange.mmp.context;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * User context accessor for widgets and modules.
 * 
 * @author milletth
 *
 */
public class UserContext {

	/**
	 * Key used to store user context in session or requests attributes
	 */
	public static final String USER_CTX_STR_KEY = "UCSK";
	
    /**
     * The user session in private access
     */
    protected Map<Object, Object> innerMap;

    /**
     * The default constructor called at loac args resolving
     * @param session The user session
     */
    public UserContext(){
    	this.innerMap = new HashMap<Object, Object>();
    }
    /**
     * Get an attribute stored in UserContext
     * @param key The widget attribute key
     * @return An object representing the attribute
     */
    public Object getAttribute(String key){
    	return this.innerMap.get(key);
    }

    /**
     * Set an attribute stored in UserContext
     * @param key The widget attribute key
     * @param value An object representing the attribute
     */
    public void setAttribute(String key, Object value) throws NotSerializableException {
    	if(value instanceof Serializable){
    		this.innerMap.put(key, value);
    	}
    	else throw new NotSerializableException("Object "+key+" can be stored in session, not serializable.");
    }

    /**
     * Removes an attribute stored in UserContext
     * @param key The widget attribute key
     */
    public void removeAttribute(String key){
    	this.innerMap.remove(key);
    }

}
