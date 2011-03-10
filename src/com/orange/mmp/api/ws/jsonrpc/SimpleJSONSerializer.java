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

import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jabsorb.JSONSerializer;
import org.jabsorb.localarg.LocalArgController;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.jabsorb.serializer.impl.ArraySerializer;
import org.jabsorb.serializer.impl.BooleanSerializer;
import org.jabsorb.serializer.impl.NumberSerializer;
import org.jabsorb.serializer.impl.PrimitiveSerializer;
import org.jabsorb.serializer.impl.RawJSONArraySerializer;
import org.jabsorb.serializer.impl.RawJSONObjectSerializer;
import org.jabsorb.serializer.impl.StringSerializer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.orange.mmp.context.RequestContext;
import com.orange.mmp.context.ResponseContext;
import com.orange.mmp.context.UserContext;

/**
 * JSON Serializer specific to MMP without class hintings
 * 
 * @author Thomas MILLET
 *
 */
@SuppressWarnings("serial")
public class SimpleJSONSerializer extends JSONSerializer {

	/**
     * Register the UserContext and RequestContext arg resolver
     */
    static{
    	LocalArgController.registerLocalArgResolver(UserContext.class, HttpServletRequest.class, new UserContextArgResolver());
    	LocalArgController.registerLocalArgResolver(RequestContext.class, HttpServletRequest.class, new RequestContextArgResolver());
    	LocalArgController.registerLocalArgResolver(ResponseContext.class, HttpServletResponse.class, new ResponseContextArgResolver());
    }
    
    /* (non-Javadoc)
     * @see org.jabsorb.JSONSerializer#registerDefaultSerializers()
     */
    @Override
    public void registerDefaultSerializers() throws Exception {
	    registerSerializer(new RawJSONArraySerializer());
	    registerSerializer(new RawJSONObjectSerializer());
	    registerSerializer(new ArraySerializer());
	    registerSerializer(new SimpleListSerializer());
	    registerSerializer(new SimpleMapSerializer());
	    registerSerializer(new StringSerializer());
	    registerSerializer(new NumberSerializer());
	    registerSerializer(new BooleanSerializer());
	    registerSerializer(new PrimitiveSerializer());
	    registerSerializer(new SimpleBeanSerializer());
    }

    /* (non-Javadoc)
     * @see org.jabsorb.JSONSerializer#unmarshall(org.jabsorb.serializer.SerializerState, java.lang.Class, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
	@Override
    public Object unmarshall(SerializerState state, Class clazz, Object json) throws UnmarshallException {
    	if(clazz == null){
    		clazz = getClass(json);
    	}
    	return super.unmarshall(state, clazz, json);
    }


    /* (non-Javadoc)
     * @see org.jabsorb.JSONSerializer#marshall(org.jabsorb.serializer.SerializerState, java.lang.Object, java.lang.Object, java.lang.Object)
     */
    @Override
    public Object marshall(SerializerState state, Object parent, Object java, Object ref) throws MarshallException {
    	if(java == null || java.equals(null)){
    		return JSONObject.NULL;
    	}	
    	else return super.marshall(state, parent, java, ref);
    }

    /**
     * Replace getClassFromHint from super class to avoid using class hint
     * @param o a JSONObject or JSONArray object to get the Class type
     * @return the Class found, or null if the passed in Object is null
     *
     * @throws UnmarshallException if javaClass was not found
     */
    @SuppressWarnings("unchecked")
	private Class getClass(Object o) throws UnmarshallException{
    	if (o == null){
    		return null;
    	}

    	if (o instanceof JSONArray) {
    		JSONArray arr = (JSONArray) o;
    		if (arr.length() == 0) {
//    			throw new UnmarshallException("no type for empty array");
    			try {
    			    return Class.forName("[L" + Integer.class.getName() + ";");
    			} catch (ClassNotFoundException e) {
    			    // XXX Warning: if this block doesn't fit, just throw the following exception
    			    // This block is used by SynchronizeAPI, when an empty blocks list is provided 
    			    throw new UnmarshallException("no type for empty array");
    			}
    		}

    		Class compClazz;
    		try{
    			compClazz = getClass(arr.get(0));
    			int arrayLgth = arr.length();
    			for(int index=0; index < arrayLgth ; index++){
    				if(!getClass(arr.get(index)).isAssignableFrom(compClazz)){
    					return java.util.List.class;
    				}
    			}
    		}
    		catch (JSONException e){
    			throw (NoSuchElementException) new NoSuchElementException(e.getMessage()).initCause(e);
    		}

    		try{
    			if(compClazz.isArray()){
    				return Class.forName("[" + compClazz.getName());
    			}
    			return Class.forName("[L" + compClazz.getName() + ";");
    		}
    		catch (ClassNotFoundException e){
    			throw new UnmarshallException("problem getting array type");
    		}
    	}
    	return o.getClass();
    }

}
