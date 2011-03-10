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
package com.orange.mmp.log.log4j;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

import com.orange.mmp.core.ApplicationListener;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.config.MMPConfig;
import com.orange.mmp.core.data.Module;
import com.orange.mmp.log.MMPLogException;
import com.orange.mmp.log.LogManager;
import com.orange.mmp.module.MMPModuleException;
import com.orange.mmp.module.ModuleContainerFactory;
import com.orange.mmp.module.ModuleEvent;
import com.orange.mmp.module.ModuleObserver;

/**
 * Log4J LogManager implementation
 *  
 * @author Thomas MILLET
 *
 */
public class Log4jLogManager implements ApplicationListener, LogManager, ModuleObserver {

	/**
	 * Attribute name used to define logger layout
	 */
	public static final QName CULOG_CONFIG_ATTR_LAYOUT = new QName("layout");
	
	/**
	 * Attribute name used to define logger level
	 */
	public static final QName CULOG_CONFIG_ATTR_lEVEL = new QName("level");
	
	/**
	 * Default layout
	 */
	public static final String DEFAULT_LAYOUT = "%d{ABSOLUTE};%m%n";
	
	/**
	 * Containes the modules loggers
	 */
	private HashMap<String, Logger> loggerCache;
	
	/**
	 * Path of the Logger configuration file
	 */
	private String mainConfigurationFile;
	
	/**
	 * The root folder in which logs are stored
	 */
	private String logsRootFolder;
	
	/**
	 * The server instance ID to allow loggin in shared folder
	 */
	private String serverInstanceId;

	/* (non-Javadoc)
	 * @see com.orange.mmp.core.ApplicationListener#initialize()
	 */
	public void initialize() throws MMPException {
		PropertyConfigurator.configure(this.mainConfigurationFile);
		this.loggerCache = new HashMap<String, Logger>();
		
		//Get the ModuleContainer and add itself as a ModuleObserver
		ModuleContainerFactory.getInstance().getModuleContainer().registerModuleObserver(this);
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.core.ApplicationListener#shutdown()
	 */
	public void shutdown() throws MMPException {
		this.loggerCache.clear();
		this.loggerCache = null;
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.log.LogManager#log(java.lang.String, int, java.lang.String)
	 */
	public void log(String key, int level, String message) throws MMPLogException {
		try{
			//Get Logger
			Logger logger = this.loggerCache.get(key);
			if(logger == null) logger = Logger.getLogger(key);
			
			//And append logs
			switch(level){
				case LEVEL_DEBUG :
					Logger.getLogger(key).debug(message);		
				break;
				case LEVEL_WARN :
					Logger.getLogger(key).warn(message);
				break;
				case LEVEL_ERROR :
					Logger.getLogger(key).error(message);
				break;	
				case LEVEL_FATAL :
					Logger.getLogger(key).fatal(message);
				break;
				default:
					Logger.getLogger(key).info(message);	
			}
		}catch(Exception e){
			throw new MMPLogException(e);
		}
	}
	
	/**
	 * Add a Logger for a dedicated module
	 *  
	 * @param module The module owning the logger
	 * @throws MMPLogException
	 */
	protected void addModuleLogger(Module module) throws MMPLogException{
		try{
			MMPConfig moduleConfiguration = ModuleContainerFactory.getInstance().getModuleContainer().getModuleConfiguration(module);
			if(moduleConfiguration != null && moduleConfiguration.getLogger() != null){
				for(MMPConfig.Logger loggerConfig : moduleConfiguration.getLogger()){
					String loggerName = loggerConfig.getName();
					String loggerLayout = DEFAULT_LAYOUT;
					if(loggerConfig.getOtherAttributes().get(CULOG_CONFIG_ATTR_LAYOUT) != null){
						loggerLayout = loggerConfig.getOtherAttributes().get(CULOG_CONFIG_ATTR_LAYOUT);
					}
					if(this.loggerCache.containsKey(loggerName)){
						throw new MMPLogException("Logger "+loggerName+" already exists");
					}
					int loggerLevel = LogManager.LEVEL_INFO;
					String loggerLevelName = null;
					if(loggerConfig.getOtherAttributes() != null && loggerConfig.getOtherAttributes().get(CULOG_CONFIG_ATTR_lEVEL) != null){
						loggerLevelName = loggerConfig.getOtherAttributes().get(CULOG_CONFIG_ATTR_lEVEL);
					}
					if(loggerLevelName != null){
						if(loggerLevelName.equalsIgnoreCase("debug")) loggerLevel = LogManager.LEVEL_DEBUG;
						else if(loggerLevelName.equalsIgnoreCase("warn")) loggerLevel = LogManager.LEVEL_WARN;
						else if(loggerLevelName.equalsIgnoreCase("error")) loggerLevel = LogManager.LEVEL_ERROR;
						else if(loggerLevelName.equalsIgnoreCase("fatal")) loggerLevel = LogManager.LEVEL_FATAL;
					}
					File loggerDir = new File(logsRootFolder+"/"+module.getId());
					if(loggerConfig.isStats())
						loggerDir = new File(logsRootFolder+"/"+module.getId()+"/stats");
					if(!loggerDir.isDirectory()){
						if(!loggerDir.mkdir()){
							throw new MMPLogException("Failed to create logging folder "+loggerDir.getPath());
						}
					}
					Logger logger = Logger.getLogger(loggerName);
					DailyRollingFileAppender appender = new DailyRollingFileAppender(new PatternLayout(loggerLayout),loggerDir+"/"+this.serverInstanceId+"-"+loggerName+".log", "'.'yyyy-MM-dd");
					logger.setAdditivity(false);
					appender.activateOptions();
					logger.removeAllAppenders();
					logger.addAppender(appender);
					logger.setLevel(Level.toLevel(loggerLevel));
					this.loggerCache.put(loggerName, logger);
				}
			}
		}catch(IOException ioe){
			throw new MMPLogException(ioe);
		}catch(MMPModuleException ce){
			throw new MMPLogException(ce);	
		}
	}
	
	/**
	 * Remove a Logger for a dedicated module
	 *  
	 * @param module The module owning the logger
	 * @throws MMPLogException
	 */
	protected void removeModuleLogger(Module module) throws MMPLogException{
		try{
			MMPConfig moduleConfiguration = ModuleContainerFactory.getInstance().getModuleContainer().getModuleConfiguration(module);
			if(moduleConfiguration != null && moduleConfiguration.getLogger() != null){
				for(MMPConfig.Logger loggerConfig : moduleConfiguration.getLogger()){
					String loggerName = loggerConfig.getName();
					if(loggerName != null) this.loggerCache.remove(loggerName);
				}
			}
		}catch(MMPModuleException ce){
			throw new MMPLogException(ce);	
		}
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.module.ModuleObserver#onModuleEvent(com.orange.mmp.module.ModuleEvent)
	 */
	public void onModuleEvent(ModuleEvent moduleEvent) {
		try{
			if(moduleEvent.getType() == ModuleEvent.MODULE_ADDED){
				this.addModuleLogger(moduleEvent.getModule());
			}
			else if(moduleEvent.getType() == ModuleEvent.MODULE_REMOVED){
				this.removeModuleLogger(moduleEvent.getModule());
			}
		}catch(MMPLogException mle){
			//NOP - Just Log
		}	
	}
	
	/**
	 * @param mainConfigurationFile the mainConfigurationFile to set
	 */
	public void setMainConfigurationFile(String mainConfigurationFile) {
		this.mainConfigurationFile = mainConfigurationFile;
	}

	/**
	 * @param logsRootFolder the logsRootFolder to set
	 */
	public void setLogsRootFolder(String logsRootFolder) {
		this.logsRootFolder = logsRootFolder;
	}

	/**
	 * @param serverInstanceId the serverInstanceId to set
	 */
	public void setServerInstanceId(String serverInstanceId) {
		this.serverInstanceId = serverInstanceId;
	}

}
