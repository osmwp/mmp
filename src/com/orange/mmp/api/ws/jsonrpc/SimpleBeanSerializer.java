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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.jabsorb.serializer.impl.BeanSerializer;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Subclass of ListSerializer without Class hintings
 * 
 * @author Thomas MILLET
 *
 */
@SuppressWarnings("serial")
public class SimpleBeanSerializer extends BeanSerializer {

	/**
     * The class that can be unserialized from JSON (no class)
     */
    @SuppressWarnings("unchecked")
	private final static Class[] _overrideJSONClasses = new Class[] {};
	
	/**
     * The class that can be serialized to JSON
     */
    @SuppressWarnings("unchecked")
	private final static Class[] _overrideSerializableClasses = new Class[] { Serializable.class };

    /* (non-Javadoc)
     * @see org.jabsorb.serializer.impl.ListSerializer#getJSONClasses()
     */
    @SuppressWarnings("unchecked")
	@Override
    public Class[] getJSONClasses() {
    	return _overrideJSONClasses;
    }
    
    /* (non-Javadoc)
	 * @see org.jabsorb.serializer.impl.ListSerializer#getSerializableClasses()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class[] getSerializableClasses() {
		return _overrideSerializableClasses;
	}

    /* (non-Javadoc)
     * @see org.jabsorb.serializer.impl.ListSerializer#marshall(org.jabsorb.serializer.SerializerState, java.lang.Object, java.lang.Object)
     */
    @Override
    public Object marshall(SerializerState state, Object p, Object o) throws MarshallException {
    	JSONObject jsonObj = new JSONObject();
    	try{
    		BeanInfo beanInfo = Introspector.getBeanInfo(o.getClass(), Object.class);
    		for(PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()){
    			Method readMethod = propertyDescriptor.getReadMethod();
    			if(readMethod != null){
    				Object propValue = readMethod.invoke(o);
    				Object json = ser.marshall(state, o, propValue, propertyDescriptor.getName());
    				jsonObj.put(propertyDescriptor.getName(),json);
    			}
    		}
    	}catch(JSONException jse){
    		throw new MarshallException("Failed to marshall Bean");
    	}catch(IllegalAccessException iae){
    		throw new MarshallException("Failed to analyse Bean");
    	}catch(InvocationTargetException ite){
    		throw new MarshallException("Failed to analyse Bean");    		
    	}catch(IntrospectionException ie){
    		throw new MarshallException("Failed to analyse Bean");
    	}
    	return jsonObj;
    }

    /* (non-Javadoc)
     * @see org.jabsorb.serializer.impl.ListSerializer#unmarshall(org.jabsorb.serializer.SerializerState, java.lang.Class, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
	@Override
    public Object unmarshall(SerializerState state, Class clazz, Object o) throws UnmarshallException {
    	return null;
   	}


}
