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
package com.orange.mmp.core.loader;

/**
 * ClassLoader used to load and handles Classes dynamically
 * using a dedicated ClassLoader to allow easy code update.
 * 
 * This Loader uses a byte[] source code to load classes.
 * 
 * @author Thomas MILLET
 *
 */
public class DynamicClassLoader extends ClassLoader {
	
	/**
	 * Default constructor using system class loader as parent
	 */
	public DynamicClassLoader() {
		super();
	}

	/**
	 * Extended constructor using a specified classloader as parent
	 * 
	 * @param parent The current classloader parent
	 */
	public DynamicClassLoader(ClassLoader parent) {
		super(parent);
	}

	/**
	 * Get a Class from its classname and its bytecode
	 * 
	 * @param className The name of the class
	 * @param classByteCode The bytecode of the class
	 * @return A Class based on bytecode and defined name
	 */
	@SuppressWarnings("unchecked")
	public Class loadClass(String className, byte []classByteCode){
		Class clazz = this.findLoadedClass(className);
		if(clazz == null && classByteCode != null) clazz = this.defineClass(className, classByteCode, 0, classByteCode.length); 
		return clazz; 
	}	
}

