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
package com.orange.mmp.file;

import java.util.Map;

import com.orange.mmp.core.Constants;

/**
 * Factory to access FileManager instances outside ApplicationContext
 * 
 * @author Thomas MILLET
 *
 */
public class FileManagerFactory {
	
	/**
	 * Reference to the FileManager Singleton
	 */
	private FileManager defaultFileManagerSingleton;
	
	/**
	 * References to the FileManager Singletons
	 */
	private Map<Object, FileManager> fileManagerSingletons;
	
	/**
	 * Reference to the FileManagerFactory Singleton
	 */
	private static FileManagerFactory fileManagerFactorySingleton;
	
	/**
	 * Default constructor (should be called by Spring IOC Only)
	 */
	public FileManagerFactory(){
		if(FileManagerFactory.fileManagerFactorySingleton == null){
			FileManagerFactory.fileManagerFactorySingleton = this;
		}
	}
	
	/**
	 * Accessor to FileManagerFactory singleton
	 * 
	 * @return A FileManagerFactory instance
	 * @throws MMPFileException
	 */
	public static FileManagerFactory getInstance() throws MMPFileException{
		return FileManagerFactory.fileManagerFactorySingleton;
	}
	
	/**
	 * Get FileManager instance based on its type
	 * 
	 * @param type The type of the FileManager to get
	 * @return The default FileManager instance of FileManagerFactory
	 * @throws MMPFileException
	 */
	public FileManager getFileManager(Object type) throws MMPFileException{
		return this.fileManagerSingletons.get(type);
	}
	
	/**
	 * Get default FileManager instance
	 * 
	 * @return The default FileManager instance of FileManagerFactory
	 * @throws MMPFileException
	 */
	public FileManager getFileManager() throws MMPFileException{
		return this.defaultFileManagerSingleton;
	}
	
	/**
	 * Get the list of FileManager types owned by this Factory 
	 * 
	 * @return An array of Objects containing types
	 */
	public Object[] getTypes(){
		return this.fileManagerSingletons.keySet().toArray(); 
	}

	/**
	 * @param fileManagerSingletons the fileManagerSingletons to set
	 */
	public void setFileManagerSingletons(Map<Object, FileManager> fileManagerSingletons) {
		this.fileManagerSingletons = fileManagerSingletons;
		this.defaultFileManagerSingleton = this.fileManagerSingletons.get(Constants.DEFAULT_COMPONENT_KEY);
	}

	
	
}
