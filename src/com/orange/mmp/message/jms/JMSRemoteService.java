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
package com.orange.mmp.message.jms;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Stack;

import com.orange.mmp.core.data.Api;
import com.orange.mmp.core.loader.DynamicClassException;
import com.orange.mmp.core.loader.DynamicClassLoader;
import com.orange.mmp.core.loader.DynamicClassLoaderFactory;
import com.orange.mmp.core.loader.InterfaceGenerator;

/**
 * Subclass of Service used to serialize Service instances.
 * 
 * Specially intended to use in remoting context, this class
 * must be hide by components to be treated as a Service by modules.
 * 
 * @author Thomas MILLET
 *
 */
public class JMSRemoteService extends Api implements Externalizable {

	private transient Stack<URI> endpoints;
	
	/**
	 * Default constructor
	 */
	public JMSRemoteService(){
		super();
		this.endpoints = new Stack<URI>();
	}
	
	/**
	 * Constructor from an existing Service
	 * 
	 * @param service The source Service 
	 */
	public JMSRemoteService(Api service){
		super();
		this.endpoints = new Stack<URI>();
		this.setName(service.getName());
		this.setDefinitionClass(service.getDefinitionClass());
		this.setPublished(service.isPublished());
		this.setShared(service.isShared());
	}
	
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		//name
		this.setName((String)in.readObject());
		//isShared
		this.setShared(in.readBoolean());
		//isPublished
		this.setPublished(in.readBoolean());
		//definitionClass
		String definitionClassName = (String)in.readObject();
		if(definitionClassName != null){
			InterfaceGenerator classGenerator = new InterfaceGenerator();
			classGenerator.setClassName(definitionClassName);
			classGenerator.setMethods((String[])in.readObject());
			DynamicClassLoader serviceClassLoader = DynamicClassLoaderFactory.getInstance().getDynamicClassLoader(this,true);
			try{
				this.setDefinitionClass(serviceClassLoader.loadClass(definitionClassName,classGenerator.getByteCode()));
			}catch(DynamicClassException dce){
				throw new IOException(dce.getMessage());
			}
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		//name
		out.writeObject(this.getName());
		//isShared
		out.writeBoolean(this.isShared());
		//isPulished
		out.writeBoolean(this.isPublished());
		//definitionClass
		if(this.getDefinitionClass() != null){
			String definitionClassName = this.getDefinitionClass().getName(); 
			out.writeObject(definitionClassName);
			Method []definitionClassMethods = this.getDefinitionClass().getMethods();
			String []definitionClassMethodsName = new String[definitionClassMethods.length];
			for(int index=0; index < definitionClassMethods.length; index++){
				definitionClassMethodsName[index] = definitionClassMethods[index].toString();
			}
			out.writeObject(definitionClassMethodsName);
		}
		else out.writeObject(null);
	}
	
	/**
	 * Gets the current RemoteService endpoint
	 * 
	 * @return An endpoint URI (or null if not endpoint found)
	 */
	public URI getEndpoint(){
		return this.endpoints.peek();
	}
	
	/**
	 * Adds a new endpoint to the remote service
	 * 
	 * @param endpoint The endpoint URI to add
	 */
	public void addEndpoint(URI endpoint){
		this.endpoints.push(endpoint);
	}
	
	/**
	 * Removes a new endpoint to the remote service
	 * 
	 * @param endpoint The endpoint URI to remove
	 */
	public void removeEndpoint(URI endpoint){
		this.endpoints.remove(endpoint);
	}

}
