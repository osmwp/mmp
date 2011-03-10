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
package com.orange.mmp.module.osgi;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.springframework.dao.DataAccessException;

import com.orange.mmp.core.ApplicationController;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.MMPRuntimeException;
import com.orange.mmp.core.data.Module;
import com.orange.mmp.core.data.Widget;
import com.orange.mmp.dao.DaoManagerFactory;

/**
 * MMP Module container based on DAO for input
 * 
 * @author tml
 *
 */
public class MMPOSGiContainer extends FelixOSGiContainer implements Runnable {
	
	/**
	 * Maximum attempts before restarting OSGI framework in case of error
	 */
    private static final int FAIL_MAX_COUNT = 20;

	/**
	 * Maximum attempts to restart OSGI Framework if broken
	 */
   	private static final int RESTART_MAX_COUNT = 20;

   	/**
   	 * An inner Lock for shared resources
   	 */
   	private final Lock lock = new ReentrantLock();

	/**
	 * Indicates if update handler is running
	 */
	private boolean isRunning;

	/**
	 * Indicates the delay between 2 refresh tests
	 */
	private int refreshDelay;
	
	/**
	 * Keep the last update in memory for performances
	 */
	private long lastUpdate = 0;
	
	/**
	 * List of current Running Modules
	 */
	private Module[] runningModules;
	
	/* (non-Javadoc)
	 * @see com.orange.mmp.module.osgi.FelixOSGiContainer#initialize()
	 */
	@Override
	public void initialize() throws MMPException {
		if(!this.isRunning){
			//Initialize parent (Felix container)
			super.initialize();
		
			this.runningModules = new Module[0];
			this.isRunning = true;
			try{
	    	    Thread m4mThread = new Thread(this); 
	    	    m4mThread.setDaemon(true);	
	    	    m4mThread.start();
	    	}catch(IllegalStateException ise){
	    	    throw new MMPRuntimeException("Failed to start WidgetManager daemon",ise);
	    	}
		
		}		
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.module.osgi.FelixOSGiContainer#shutdown()
	 */
	@Override
	public void shutdown() throws MMPException {
		super.shutdown();
	}
	
	/**
	 * Reload the list of widgets
	 *
	 */
	public void refresh() throws MMPException{
		try{
			//Lock Thread
			this.lock.lock();
			long lastDAOUpdate = DaoManagerFactory.getInstance().getDaoManager().getDao("module").getLastUpdateTimestamp();
			if(this.lastUpdate < lastDAOUpdate){
				this.lastUpdate = lastDAOUpdate;
				//Get list of installed modules
				Module[] installedModules = (Module[])DaoManagerFactory.getInstance().getDaoManager().getDao("module").list();
				Arrays.sort(installedModules);
				
				//Search removed modules
				for(Module runningModule : this.runningModules){
					//Module removed
					if(Arrays.binarySearch(installedModules, runningModule) < 0) {
						this.removeModule(runningModule);
					}
				}
				
				//Search added and updates modules
				for(Module installedModule : installedModules){
					int moduleIndex = Arrays.binarySearch(runningModules, installedModule); 
					//Module added
					if(moduleIndex < 0){
						this.addModule(installedModule);
					}
					else{
						//Get current running module
						Module runningModule = runningModules[moduleIndex];
						//Check if module has been updated
						boolean mustUpdate = false;
						//Check version
						if(installedModule.getVersion() != null
								&& runningModule.getVersion() != null){
							mustUpdate = (installedModule.getVersion().compareTo(runningModule.getVersion()) != 0);
						}
						
						//Make update if needed
						if(mustUpdate){
							this.removeModule(runningModule);
							this.addModule(installedModule);
						}
					}
				}
				this.runningModules = installedModules;
			}
		}finally{
			//Unlock thread
			this.lock.unlock();
		}
	}

	/**
	 * Deploy a module bundle on MMP server
	 * @param moduleFile The module file (JAR file)
	 */
	@SuppressWarnings("unchecked")
	public Module deployModule(File moduleFile) throws MMPException {
	    try {
	    	JarFile jarFile = new JarFile(new File(moduleFile.toURI()));
    		Manifest manifest = jarFile.getManifest();
    		if(manifest == null) {
    			throw new MMPException("invalid module archive, MANIFEST file not found");
    		}
    		// Get Bundle category
    		Attributes attributes = manifest.getMainAttributes();
    		String category = attributes.getValue(BUNDLE_CATEGORY_HEADER);
    		// Test if the module is a widget
    		if (category != null && category.equals(com.orange.mmp.core.Constants.MODULE_CATEGORY_WIDGET)) {
    			Widget widget = new Widget();
    			String symbName = attributes.getValue(BUNDLE_SYMBOLICNAME_HEADER);
    			String branch = symbName.split(com.orange.mmp.widget.Constants.BRANCH_SUFFIX_PATTERN)[1];
    			widget.setLocation(moduleFile.toURI());
    			widget.setBranchId(branch);
    			DaoManagerFactory.getInstance().getDaoManager().getDao("module").createOrUdpdate(widget);
    	    	this.lastUpdate = 0;
    	    	this.refresh();
    			return widget;
    		} else {
    			Module module = new Module();
    	    	module.setLocation(moduleFile.toURI());
    	    	DaoManagerFactory.getInstance().getDaoManager().getDao("module").createOrUdpdate(module);
    	    	this.lastUpdate = 0;
    	    	this.refresh();
    	    	return module;
    		}
	    	
	    }catch(IOException ioe){
	    	throw new MMPException("Failed to deploy module", ioe);
	    }
	}

	/**
	 * Undeploy a module (asynchronous with refresh())
	 * @param name The name of the application
	 */
	@SuppressWarnings("unchecked")
	public void undeployModule(String moduleId) throws MMPException{
	    try {
	    	Module module = new Module();
	    	module.setId(moduleId);
	    	DaoManagerFactory.getInstance().getDaoManager().getDao("module").delete(module);
	    	this.lastUpdate = 0;
	    	this.refresh();
	    } catch(DataAccessException dae) {
	    	throw new MMPException("Failed to undeploy widget",dae);
	    }
	}

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
    	int restartCount = 0;
    	int failCount = 0;
    	while(this.isRunning){
    		try{
    			if(ApplicationController.getInstance().isRunning()) this.refresh();
    			failCount=0;
    		}catch(Exception e){
    			failCount++;
    		}
    		
    		//Cache should be corrupted, try to restart OSGI framework
   			if(restartCount >= RESTART_MAX_COUNT){
   				throw new MMPRuntimeException("Failed to restart ModuleContainer, max attempts count reached");
   			}
   			else{
   				if(failCount >= FAIL_MAX_COUNT){
   					//Retry to restart ModuleContainer until RESTART_MAX_COUNT reached
   					restartCount++;
   					try{
   						this.shutdown();
   						this.initialize();
   						restartCount=0;
   					}catch(Exception e){
   						//NOP
   					}   					
   				}
   			}
   			try{
   				Thread.sleep(this.refreshDelay);
   			}catch(InterruptedException ie){
   				throw new MMPRuntimeException("Failed to idle WidgetManager",ie);
   			}
   		}
   	}

	/**
	 * @param refreshDelay the refreshDelay to set
	 */
	public void setRefreshDelay(int refreshDelay) {
	    this.refreshDelay = refreshDelay*1000;
	}
}
