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
package com.orange.mmp.api.ws.jsonrpc;

import java.util.Iterator;
import java.util.Map;

import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.jabsorb.serializer.impl.MapSerializer;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Subclass of MapSerializer without Class hintings
 * 
 * @author Thomas MILLET
 *
 */
@SuppressWarnings("serial")
public class SimpleMapSerializer extends MapSerializer {

    /* (non-Javadoc)
     * @see org.jabsorb.serializer.impl.MapSerializer#marshall(org.jabsorb.serializer.SerializerState, java.lang.Object, java.lang.Object)
     */
    @Override
    public Object marshall(SerializerState state, Object p, Object o) throws MarshallException {
    	return o;
    }

    /* (non-Javadoc)
     * @see org.jabsorb.serializer.impl.MapSerializer#unmarshall(org.jabsorb.serializer.SerializerState, java.lang.Class, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
	@Override
    public Object unmarshall(SerializerState state, Class clazz, Object o) throws UnmarshallException {
    	Map map = null;
    	try{
    		try{
    			try{
    				if(clazz.isInterface()){
    					map = new java.util.HashMap();
    				}
    				else map = (Map)clazz.newInstance();
    			}catch(ClassCastException cce){
    				throw new UnmarshallException("invalid unmarshalling Class "+cce.getMessage());
    			}
    		}catch(IllegalAccessException iae){
    			throw new UnmarshallException("no access unmarshalling object "+ iae.getMessage());
    		}
    	}catch(InstantiationException ie){
    		throw new UnmarshallException("unable to instantiate unmarshalling object "+ ie.getMessage());
    	}
    	JSONObject jso = (JSONObject)o;
    	Iterator keys = jso.keys();
    	state.setSerialized(o, map);
    	try{
    		while(keys.hasNext()){
    			String key = (String)keys.next();
    			map.put(key, ser.unmarshall(state, null, jso.get(key)));
    		}
    	}catch(JSONException je){
    		throw new UnmarshallException("Could not read map: " + je.getMessage());
    	}

    	return map;
    }
}
