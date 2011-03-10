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

import java.io.File;

/**
 * Manager used to publish Filer API to modules and services
 * 
 * @author Thomas MILLET
 *
 */
public interface FileManager {

	
	/**
	 * Get a file from the filer repository
	 * 
	 * @param filePath The absolute path of the file in filer
	 * @param create Indicates if the file must be created if not found
	 * @return A file instance, if the file does not exists and create to false, an Exception is raised
	 * @throws MMPFileException
	 */
	public File getFile(String filePath, boolean create) throws MMPFileException;	
}
