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

import java.util.List;

import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.jabsorb.serializer.impl.ListSerializer;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Subclass of ListSerializer without Class hintings
 * 
 * @author Thomas MILLET
 *
 */
@SuppressWarnings("serial")
public class SimpleListSerializer extends ListSerializer {

    /**
     * The class that this serialises to
     */
    @SuppressWarnings("unchecked")
	private final static Class[] _overrideJSONClasses = new Class[] { JSONArray.class };

    /* (non-Javadoc)
     * @see org.jabsorb.serializer.impl.ListSerializer#getJSONClasses()
     */
    @SuppressWarnings("unchecked")
	@Override
    public Class[] getJSONClasses() {
    	// This serializer is based on JSONArray and not JSONObject
    	return _overrideJSONClasses;
    }

    /* (non-Javadoc)
     * @see org.jabsorb.serializer.impl.ListSerializer#marshall(org.jabsorb.serializer.SerializerState, java.lang.Object, java.lang.Object)
     */
    @Override
    public Object marshall(SerializerState state, Object p, Object o) throws MarshallException {
    	return o;
    }

    /* (non-Javadoc)
     * @see org.jabsorb.serializer.impl.ListSerializer#unmarshall(org.jabsorb.serializer.SerializerState, java.lang.Class, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
	@Override
    public Object unmarshall(SerializerState state, Class clazz, Object o) throws UnmarshallException {
		List list = null;
		try{
		    try{
			try{
			    if(clazz.isInterface()){
				list = new java.util.ArrayList();
			    }
			    else list = (List)clazz.newInstance();
			}catch(ClassCastException cce){
			    throw new UnmarshallException("invalid unmarshalling Class "+cce.getMessage());
			}
		    }catch(IllegalAccessException iae){
			throw new UnmarshallException("no access unmarshalling object "+ iae.getMessage());
		    }
		}catch(InstantiationException ie){
		    throw new UnmarshallException("unable to instantiate unmarshalling object "+ ie.getMessage());
		}
		JSONArray jsa = (JSONArray)o;
	
		state.setSerialized(o, list);
		int i = 0;
		try{
		    for (; i < jsa.length(); i++){
			list.add(ser.unmarshall(state, null, jsa.get(i)));
		    }
		}
		catch (UnmarshallException e){
		    throw new UnmarshallException("element " + i + " " + e.getMessage());
		}
		catch (JSONException e){
		    throw new UnmarshallException("element " + i + " " + e.getMessage());
		}
		return list;
    }


}
