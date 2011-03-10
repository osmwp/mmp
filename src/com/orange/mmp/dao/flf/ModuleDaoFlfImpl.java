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
package com.orange.mmp.dao.flf;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import com.orange.mmp.core.Constants;
import com.orange.mmp.core.data.Branch;
import com.orange.mmp.core.data.Module;
import com.orange.mmp.core.data.Version;
import com.orange.mmp.core.data.Widget;
import com.orange.mmp.dao.DaoManagerFactory;
import com.orange.mmp.dao.MMPDaoException;

/**
 * Implementation of Widget DAO using shared repoistory (files)
 * @author milletth
 *
 */
public class ModuleDaoFlfImpl extends FlfDao<Module> {

	/**
	 * Headers Containing the Module category type
	 */
	public static final String MODULE_CATEGORY_HEADER = "Bundle-Category";

	/**
	 * Headers Containing the Module version
	 */
	public static final String MODULE_VERSION_HEADER = "Bundle-Version";

	/**
	 * Headers Containing the Module Symbolic name
	 */
	public static final String MODULE_ID_HEADER = "Bundle-SymbolicName";

	/**
	 * Headers Containing the Module name
	 */
	public static final String MODULE_NAME_HEADER = "Bundle-Name";
	
	/**
	 * Headers Containing the Module description
	 */
	public static final String MODULE_DESCRIPTION_HEADER = "Bundle-Description";
	
	/**
	 * Modules files folder path
	 */
	private String path;
	
	/**
	 * An inner Lock for shared resources
	 */
	private final Lock lock = new ReentrantLock();
	
	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.Dao#createOrUdpdate(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public Module createOrUdpdate(Module module) throws MMPDaoException {
		if(module == null
    			|| module.getLocation() == null) {
    		throw new MMPDaoException("missing or bad data access object");
		}

    	JarFile jarFile = null;
    	try {
    		this.lock.lock();
    		jarFile = new JarFile(new File(module.getLocation()));
    		Manifest manifest = jarFile.getManifest();
    		if(manifest == null) {
    			throw new MMPDaoException("invalid module archive, MANIFEST file not found");
    		}
    		String symbolicName = manifest.getMainAttributes().getValue(MODULE_ID_HEADER);
    		if(manifest.getMainAttributes().getValue(MODULE_ID_HEADER) != null) {
    			if(module instanceof Widget) {
    				String[] widgetId = symbolicName.split(com.orange.mmp.widget.Constants.BRANCH_SUFFIX_PATTERN);
    				module.setId(widgetId[0]);
    			} else {
    				module.setId(symbolicName);
    			}
    		}
    		else throw new MMPDaoException("invalid module archive, missing "+MODULE_ID_HEADER+" header");
    		if(manifest.getMainAttributes().getValue(MODULE_NAME_HEADER) != null){
    			module.setName(manifest.getMainAttributes().getValue(MODULE_NAME_HEADER));
    		}
    		else throw new MMPDaoException("invalid module archive, missing "+MODULE_NAME_HEADER+" header");
    		if(manifest.getMainAttributes().getValue(MODULE_VERSION_HEADER) != null){
    			module.setVersion(new Version(manifest.getMainAttributes().getValue(MODULE_VERSION_HEADER)));
    		}
    		else throw new MMPDaoException("invalid module archive, missing "+MODULE_VERSION_HEADER+" header");
    		if(manifest.getMainAttributes().getValue(MODULE_CATEGORY_HEADER) != null){
    			module.setCategory(manifest.getMainAttributes().getValue(MODULE_CATEGORY_HEADER));
    		}
    		else module.setCategory(Constants.MODULE_CATEGORY_LIBRARY);

    		File moduleFile = new File(module.getLocation());
    		File dstFile;
    		if(module instanceof Widget) {
				String[] nameAndBranch = symbolicName.split(com.orange.mmp.widget.Constants.BRANCH_SUFFIX_PATTERN);
				if(nameAndBranch.length > 1)
					dstFile = new File(this.path, nameAndBranch[0]
					        .concat(com.orange.mmp.widget.Constants.BRANCH_SUFFIX_PATTERN)
							.concat(nameAndBranch[1]).concat(".jar"));
				else {
					String defaultBranchId = null;
					Branch defaultBranch = new Branch();
					defaultBranch.setDefault(true);
					Branch defaultBranchesResult[] = (Branch[])DaoManagerFactory.getInstance().getDaoManager().getDao("branch").find(defaultBranch);
					if(defaultBranchesResult.length > 0) {
						defaultBranchId = defaultBranchesResult[0].getId();
					}
					defaultBranch = defaultBranchesResult[0];
					dstFile = new File(this.path, nameAndBranch[0]
                            .concat(com.orange.mmp.widget.Constants.BRANCH_SUFFIX_PATTERN)
					        .concat(defaultBranchId).concat(".jar"));
				}
    		} else {
    			dstFile = new File(this.path, symbolicName.concat(".jar"));
    		}
    		FileUtils.copyFile(moduleFile, dstFile);
    		module.setLocation(dstFile.toURI());
    		module.setLastModified(dstFile.lastModified());
    		jarFile.close();

    		FileUtils.touch(new File(this.path));

    		return module;
    	}catch(IOException ioe){
    		throw new MMPDaoException("failed to add module : "+ioe.getMessage());
    	}
    	finally{
    		try{
    			if(jarFile != null) jarFile.close();
    		}catch(IOException ioe){
    			//Nop just log
    		}
    		this.lock.unlock();
    	}
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.Dao#delete(java.lang.Object)
	 */
	public void delete(Module module) throws MMPDaoException {
		try{
			Module[] moduleList = this.find(module);
			this.lock.lock();
    		for(Module currentModule : moduleList){
    			File toDelete = new File(currentModule.getLocation());
    			if(!toDelete.delete()){
    				throw new MMPDaoException("failed to delete module "+currentModule.getId());
    			}
    		}

    		FileUtils.touch(new File(this.path));
    	}catch(IOException ioe){
    		throw new MMPDaoException("failed to delete module : "+ioe.getMessage());
    	}finally{
    		this.lock.unlock();
    	}		
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.Dao#find(java.lang.Object)
	 */
	public Module[] find(Module module) throws MMPDaoException {
		if(module == null){
    		throw new MMPDaoException("missing or bad data access object");
    	}

    	FilenameFilter jarFilter = new SuffixFileFilter(".jar");
    	File widgetsFiles[] = new File(this.path).listFiles(jarFilter);
    	if(widgetsFiles == null) return new Module[0];
    	ArrayList<Module> list = new ArrayList<Module>();

    	JarFile jarFile = null;
    	try {
    		for(File found : widgetsFiles) {
    			jarFile = new JarFile(found);
    			Manifest manifest = jarFile.getManifest();
    			boolean match = (module.getId() == null
    								&& module.getLocation() == null
    								&& module.getVersion() == null
    								&& module.getName() == null
    								&& module.getCategory() == null);
    			if(!match) {
    				boolean goOnchecking = true;
    				if(module.getLocation() != null && goOnchecking){
    					match = found.toURI().toString().equals(module.getLocation().toString());
    					goOnchecking = false;
    				}
    				if(module.getId() != null){
    					match = (manifest.getMainAttributes().getValue(MODULE_ID_HEADER) != null && manifest.getMainAttributes().getValue(MODULE_ID_HEADER).equals(module.getId()));
    					goOnchecking = false;
    				}
    				if(module.getVersion() != null && goOnchecking){
    					match = (manifest.getMainAttributes().getValue(MODULE_VERSION_HEADER) != null && manifest.getMainAttributes().getValue(MODULE_VERSION_HEADER).equals(module.getVersion()));
    					goOnchecking = match;
    				}
    				if(module.getName() != null && goOnchecking){
    					match = (manifest.getMainAttributes().getValue(MODULE_NAME_HEADER) != null && manifest.getMainAttributes().getValue(MODULE_NAME_HEADER).equals(module.getName()));
    					goOnchecking = match;
    				}
    				if(module.getCategory() != null && goOnchecking){
    					match = (manifest.getMainAttributes().getValue(MODULE_CATEGORY_HEADER) != null && manifest.getMainAttributes().getValue(MODULE_CATEGORY_HEADER).equals(module.getCategory()));
    					goOnchecking = match;
    				}
    			}

    			if(match){
    				Module foundModule = new Module();
    				foundModule.setId(manifest.getMainAttributes().getValue(MODULE_ID_HEADER));
    				foundModule.setName(manifest.getMainAttributes().getValue(MODULE_NAME_HEADER));
    				foundModule.setVersion(new Version(manifest.getMainAttributes().getValue(MODULE_VERSION_HEADER)));
    				foundModule.setCategory(manifest.getMainAttributes().getValue(MODULE_CATEGORY_HEADER));
    				foundModule.setLastModified(found.lastModified());
    				foundModule.setLocation(found.toURI());
    				list.add(foundModule);
    			}
    			jarFile.close();
    		}
    	} catch(IOException ioe) {
    		throw new MMPDaoException("failed to load module");
    	} finally {
    		try {
    			if(jarFile != null) jarFile.close();
    		} catch(IOException ioe) {
    			//Nop just log
    		}
    	}

    	Module[] modulesList = new Module[list.size()];
    	return list.toArray(modulesList);
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.Dao#getLastUpdateTimestamp()
	 */
	public long getLastUpdateTimestamp() throws MMPDaoException {
		return new File(this.path).lastModified();
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.Dao#list()
	 */
	public Module[] list() throws MMPDaoException {
		Module module = new Module();
		return this.find(module);
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.flf.FlfDao#checkDaoConfig()
	 */
	@Override
	public void checkDaoConfig() throws MMPDaoException {
		if(this.path == null) throw new MMPDaoException("Missing Path for Module DAO FLF configuration");
		File file = new File(this.path);
		if(!file.exists()){
			try{
				FileUtils.forceMkdir(file);
			}catch(IOException ioe){
				throw new MMPDaoException("Failed to create Module DAO FLF folder : "+ioe.getMessage());
			}
		}
		else if(!file.isDirectory()){
			throw new MMPDaoException("Path used for Module DAO FLF must be a directory");
		}		
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}
}
