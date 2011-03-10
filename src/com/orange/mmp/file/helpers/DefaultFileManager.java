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
package com.orange.mmp.file.helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.orange.mmp.core.ApplicationController;
import com.orange.mmp.core.ApplicationListener;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.MMPRuntimeException;
import com.orange.mmp.core.config.MMPConfig;
import com.orange.mmp.core.data.Module;
import com.orange.mmp.file.FileManager;
import com.orange.mmp.file.MMPFileException;
import com.orange.mmp.module.MMPModuleException;
import com.orange.mmp.module.ModuleContainerFactory;
import com.orange.mmp.module.ModuleEvent;
import com.orange.mmp.module.ModuleObserver;

/**
 * Default FileManager implementation
 * 
 * @author tml
 *
 */
public class DefaultFileManager implements ApplicationListener, ModuleObserver,	FileManager {

	/**
	 * Attribute name used to define files from module (CSV and wildcards)
	 */
	public static final QName CULOG_CONFIG_ATTR_FILES = new QName("files");
	
	/**
	 * Attribute name used to define files destination directory
	 */
	public static final QName CULOG_CONFIG_ATTR_DESTDIR = new QName("destdir");
	
	/**
	 * Attribute name used to define files CHMOD (Unix only)
	 */
	public static final QName CULOG_CONFIG_ATTR_CHMOD = new QName("chmod");
	
	/**
	 * The root path of shared directory
	 */
	private String sharedRepositoryRoot;
	
	/**
	 * The CHMOD command path
	 */
	private String chmodCommand;
	
	/**
	 * Containes the modules filesets
	 */
	private HashMap<String, File[]> filesetCache;
	
	/* (non-Javadoc)
	 * @see com.orange.mmp.core.ApplicationListener#initialize()
	 */
	public void initialize() throws MMPException {
		if(this.sharedRepositoryRoot != null) {
			File sharedRepositoryRootFile = new File(this.sharedRepositoryRoot);
			if(!sharedRepositoryRootFile.exists()) throw new MMPRuntimeException("sharedRepositoryRoot '"+this.sharedRepositoryRoot+"' not found in FileManager");
			if(!sharedRepositoryRootFile.isDirectory()) throw new MMPRuntimeException("sharedRepositoryRoot '"+this.sharedRepositoryRoot+"' is not a directory in FileManager");
		}
		else throw new MMPRuntimeException("No sharedRepositoryRoot found in FileManager");
		
		this.filesetCache = new HashMap<String, File[]>();
		
		//Get the ModuleContainer and add itself as a ModuleObserver
		ModuleContainerFactory.getInstance().getModuleContainer().registerModuleObserver(this);
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.core.ApplicationListener#shutdown()
	 */
	public void shutdown() throws MMPException {
		this.filesetCache.clear();
		this.filesetCache = null;
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.module.ModuleObserver#onModuleEvent(com.orange.mmp.module.ModuleEvent)
	 */
	public void onModuleEvent(ModuleEvent moduleEvent) {
		if(ApplicationController.getInstance().isMaster()){
			try{
				if(moduleEvent.getType() == ModuleEvent.MODULE_ADDED){
					this.addModuleFileset(moduleEvent.getModule());
				}
				else if(moduleEvent.getType() == ModuleEvent.MODULE_REMOVED){
					this.removeModuleFileset(moduleEvent.getModule());
				}
			}catch(MMPFileException cce){
				//NOP - Just Log
			}
		}
	}
	
	/**
	 * Add a module fileset to MMP
	 * 
	 * @param module The module containing the fileset
	 * @throws MMPFileException
	 */
	protected void addModuleFileset(Module module) throws MMPFileException{
		try{
			MMPConfig moduleConfiguration = ModuleContainerFactory.getInstance().getModuleContainer().getModuleConfiguration(module);
			if(moduleConfiguration != null && moduleConfiguration.getFileset() != null){
				for(MMPConfig.Fileset filesetConfig : moduleConfiguration.getFileset()){
					String filesetName = filesetConfig.getName();
					if(filesetConfig.getOtherAttributes().get(CULOG_CONFIG_ATTR_FILES) != null 
							&& filesetConfig.getOtherAttributes().get(CULOG_CONFIG_ATTR_DESTDIR) != null){
						String files = filesetConfig.getOtherAttributes().get(CULOG_CONFIG_ATTR_FILES);
						String destDir = filesetConfig.getOtherAttributes().get(CULOG_CONFIG_ATTR_DESTDIR);
						String chmod = filesetConfig.getOtherAttributes().get(CULOG_CONFIG_ATTR_CHMOD);
						
						//Parse files from fileset
						List<URL> fileList = new ArrayList<URL>();
						for(String currentFilePath : files.split(",")){
							File file = new File(currentFilePath);
							for(URL currentFileUrl : ModuleContainerFactory.getInstance().getModuleContainer().findModuleResource(module, file.getParent(), file.getName(), false)){
								fileList.add(currentFileUrl);
							}
						}
						
						
						
						//Copy files
						List<File> dstFileList = new ArrayList<File>();
						for(URL currentFileURL : fileList){
							File dstFile = this.getFile(destDir+currentFileURL.getPath(), true);
							OutputStream out = null;
							InputStream in = null;
							try{
								out = new FileOutputStream(dstFile);
								in = ModuleContainerFactory.getInstance().getModuleContainer().getModuleResource(module, currentFileURL.getPath()).openStream();
								IOUtils.copy(in, out);
								dstFileList.add(dstFile);
							}finally{
								if(out != null) out.close();
								if(in != null) in.close();
							}
							if(chmod != null){
								Runtime.getRuntime().exec(this.chmodCommand+" "+chmod+" "+dstFile.getPath());
							}
						}
						
						//Copy list of file in an array to store in cache
						File dstFiles[] = new File[dstFileList.size()];
						this.filesetCache.put(filesetName, dstFileList.toArray(dstFiles));
					}
				}
			}
		}catch(IOException ioe){
			throw new MMPFileException(ioe);
		}catch(MMPModuleException me){
			throw new MMPFileException(me);	
		}
	}
	
	/**
	 * Remove a module fileset from MMP
	 * 
	 * @param module The module containing the fileset
	 * @throws MMPFileException
	 */
	protected void removeModuleFileset(Module module) throws MMPFileException{
		try{
			MMPConfig moduleConfiguration = ModuleContainerFactory.getInstance().getModuleContainer().getModuleConfiguration(module);
			if(moduleConfiguration != null && moduleConfiguration.getFileset() != null){
				for(MMPConfig.Fileset filesetConfig : moduleConfiguration.getFileset()){
					String filesetName = filesetConfig.getName();
					File dstFiles[] = this.filesetCache.get(filesetName);
					this.filesetCache.remove(filesetName);
					if(dstFiles != null){
						for(File currentFile : dstFiles){
							FileUtils.forceDelete(currentFile);
						}
					}
				}
			}
		}catch(IOException ioe){
			throw new MMPFileException(ioe);	
		}catch(MMPModuleException me){
			throw new MMPFileException(me);	
		}
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.file.FileManager#getFile(java.lang.String, boolean)
	 */
	public File getFile(String filePath, boolean create) throws MMPFileException {
		File file = new File(this.sharedRepositoryRoot,filePath);
		if(!file.exists() && create){
			try{
				//if(!file.createNewFile()) throw new MMPFileException("Failed to get file "+filePath+" : unknown error");
				FileUtils.forceMkdir(file.getParentFile());
				if(!file.createNewFile()) throw new MMPFileException("Failed to get file "+filePath+" : unknown error");
			}catch(IOException ioe){
				throw new MMPFileException("Failed to get file "+filePath,ioe);
			}
		}
		
		return file;
	}

	/**
	 * @param sharedRepositoryRoot the sharedRepositoryRoot to set
	 */
	public void setSharedRepositoryRoot(String sharedRepositoryRoot) {
		this.sharedRepositoryRoot = sharedRepositoryRoot;
	}

	/**
	 * @param chmodCommand the chmodCommand to set
	 */
	public void setChmodCommand(String chmodCommand) {
		this.chmodCommand = chmodCommand;
	}

}
