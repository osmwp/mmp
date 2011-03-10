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
package com.orange.mmp.i18n.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import com.orange.mmp.core.ApplicationController;
import com.orange.mmp.core.ApplicationListener;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.config.MMPConfig;
import com.orange.mmp.core.data.Module;
import com.orange.mmp.i18n.InternationalizationManager;
import com.orange.mmp.i18n.MMPI18NException;
import com.orange.mmp.module.ModuleContainerFactory;
import com.orange.mmp.module.ModuleEvent;
import com.orange.mmp.module.ModuleObserver;

/**
 * I18N manager for convenience use and I18N API abstraction
 * 
 * @author tml
 *
 */
public class DefaultInternationalizationManager implements ApplicationListener, ModuleObserver, InternationalizationManager{
	
	/**
	 * Attribute name used to define location of messages source file from module
	 */
	public static final QName I18N_CONFIG_MS_LOCATION = new QName("location");
	
	/**
	 * Attribute name used to define basename of location files (ex : message for message.properties, message_fr.properties ...)
	 */
	public static final QName I18N_CONFIG_MS_BASENAME = new QName("basename");
	
	/**
	 * Pointer on the Spring MessageSource
	 */
	private MessageSource defaultMessageSource;
	
	/**
	 * Handle i18n from modules
	 */
	private Map<String, MessageSource> messageSourceMap = null; 
	/**
	 * i18n files path from modules/message resource
	 */
	private Map<String, String> i18nFilesPathByMessageSource = null; 
	
	/**
	 * The base path where module messages are located
	 */
	private String messageBasePath = null;
	
	/**
	 * Initialize component
	 */
	public void initialize() throws MMPException {
		this.messageSourceMap = new HashMap<String, MessageSource>();
		this.i18nFilesPathByMessageSource = new HashMap<String, String>();
		
		//Get the ModuleContainer and add itself as a ModuleObserver
		ModuleContainerFactory.getInstance().getModuleContainer().registerModuleObserver(this);
	}

	/**
	 * Shutdown
	 */
	public void shutdown() throws MMPException {
		this.messageSourceMap = null;
		this.i18nFilesPathByMessageSource = null;
	}

	/**
	 * @param messageSource the messageSource to set
	 */
	public void setDefaultMessageSource(MessageSource defaultMessageSource) {
		this.defaultMessageSource = defaultMessageSource;
	}
	
	/* (non-Javadoc)
	 * @see com.orange.mmp.i18n.InternationalizationManager#getMessage(java.lang.String, java.lang.Object[], java.lang.String, java.util.Locale)
	 */
	public String getMessage(String code, Object[] args, String defaultMessage,	Locale locale) throws MMPI18NException {
		try{
			return this.defaultMessageSource.getMessage(code, args, defaultMessage, locale);
		}catch(NoSuchMessageException nsme){
			throw new MMPI18NException(nsme);
		}
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.i18n.InternationalizationManager#getMessage(java.lang.String, java.lang.String, java.lang.Object[], java.lang.String, java.util.Locale)
	 */
	public String getMessage(String messageSourceName, String code,	Object[] args, String defaultMessage, Locale locale)
			throws MMPI18NException {
		try{
			MessageSource messageSource = this.messageSourceMap.get(messageSourceName);
			if(messageSource != null){
				return messageSource.getMessage(code, args, defaultMessage, locale);
			}
			else return this.defaultMessageSource.getMessage(code, args, defaultMessage, locale);
		}catch(NoSuchMessageException nsme){
			throw new MMPI18NException(nsme);
		}
	}


	/* (non-Javadoc)
	 * @see com.orange.mmp.module.ModuleObserver#onModuleEvent(com.orange.mmp.module.ModuleEvent)
	 */
	public void onModuleEvent(ModuleEvent moduleEvent) {
		try{
			if(moduleEvent.getType() == ModuleEvent.MODULE_ADDED){
				this.addModuleMessageSource(moduleEvent.getModule());
			}
			else if(moduleEvent.getType() == ModuleEvent.MODULE_REMOVED){
				this.removeModuleMessageSource(moduleEvent.getModule());
			}
		}catch(MMPException me){
			//NOP - Just Log
		}		
	}

	/**
	 * Adds a MessageSource from a module
	 * 
	 * @param module The module owning the MessageSource
	 * @throws MMPException
	 */
	private void addModuleMessageSource(Module module) throws MMPException{
		try{	
			MMPConfig moduleConfiguration = ModuleContainerFactory.getInstance().getModuleContainer().getModuleConfiguration(module);
			if(moduleConfiguration != null && moduleConfiguration.getMessagesource() != null){
				for(MMPConfig.Messagesource messageSourceConfig : moduleConfiguration.getMessagesource()){
					if(messageSourceConfig.getOtherAttributes().get(I18N_CONFIG_MS_LOCATION) != null
							&& messageSourceConfig.getOtherAttributes().get(I18N_CONFIG_MS_BASENAME) != null){
						String messageSourceName = messageSourceConfig.getName();
						String location = messageSourceConfig.getOtherAttributes().get(I18N_CONFIG_MS_LOCATION);
						String basename = messageSourceConfig.getOtherAttributes().get(I18N_CONFIG_MS_BASENAME);
						List<URL> locations = ModuleContainerFactory.getInstance().getModuleContainer().findModuleResource(module, location, basename+"*", false);
						
						String messageSourceFolderBase = this.messageBasePath+"/"+messageSourceName;
						FileUtils.forceMkdir(new File(messageSourceFolderBase));

						//Copy I18N resources on shared repository (Only master server)
						if(ApplicationController.getInstance().isMaster()){
							String fileName = null;						
							for(URL messageUrl : locations){
								InputStream in = null;
								OutputStream out = null;
								try{
									in = messageUrl.openStream();
									String filePath = messageUrl.getPath();
									fileName = filePath.substring(filePath.lastIndexOf("/")+1);
									out = new FileOutputStream(new File(messageSourceFolderBase,fileName));
									IOUtils.copy(in, out);
								}finally{
									if(in != null){
										in.close();
									}
									if(out != null){
										out.close();
									}
								}
								
							}
						}
						
						ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
						messageSource.setDefaultEncoding("UTF-8");
						messageSource.setBasename("file://"+messageSourceFolderBase+"/"+basename);
						this.messageSourceMap.put(messageSourceName, messageSource);

						//Keep file path for the translation
						i18nFilesPathByMessageSource.put(messageSourceName, messageSourceFolderBase+"/"+basename);
					}
				}
			}
		}catch(IOException ioe){
			throw new MMPI18NException("Failed to load "+module.getName()+" I18N configuration");
		}
	}
	
	/**
	 * Removes a MessageSource from a module
	 * 
	 * @param module The module owning the MessageSource
	 * @throws MMPException
	 */
	private void removeModuleMessageSource(Module module) throws MMPException{
		MMPConfig moduleConfiguration = ModuleContainerFactory.getInstance().getModuleContainer().getModuleConfiguration(module);
		if(moduleConfiguration != null && moduleConfiguration.getMessagesource() != null){
			for(MMPConfig.Messagesource messageSourceConfig : moduleConfiguration.getMessagesource()){
				if(messageSourceConfig.getOtherAttributes().get(I18N_CONFIG_MS_LOCATION) != null
						&& messageSourceConfig.getOtherAttributes().get(I18N_CONFIG_MS_BASENAME) != null){
					String messageSourceName = messageSourceConfig.getName();					
					try{	
						FileUtils.forceDelete(new File( this.messageBasePath+"/"+messageSourceName));
					}catch(IOException ioe){
						//NOP
					}
					this.messageSourceMap.remove(messageSourceName);
				}
			}
		}
	}

	/**
	 * @param messageBasePath the messageBasePath to set
	 */
	public void setMessageBasePath(String messageBasePath) {
		this.messageBasePath = messageBasePath;
	}

	/**
	 * @see com.orange.mmp.i18n.InternationalizationManager#getLocalizationMap(java.lang.String, java.util.Locale)
	 */
	public Map<?,?> getLocalizationMap(final String messageSource, final Locale locale) {
		final Properties i18nBundle = new Properties();
		final String baseFilePath = i18nFilesPathByMessageSource.get(messageSource);
		
		//Find name for the i18n file (and verify if file exist)
		File file = null;
		if (locale.getCountry() != null && locale.getCountry().length() > 0) {
			file = new File( baseFilePath + "_" + locale.getLanguage() + "_" + locale.getCountry() + ".properties");
		}
		if (file == null || !file.exists()) {
			file = new File( baseFilePath + "_" + locale.getLanguage() + ".properties");
		}
		if (file == null || !file.exists()) {
			file = new File( baseFilePath + ".properties");
		}
		
		//Read file (event if file not exist... error is automatically processed via AspectJ and add to log
		final InputStream inStream;
		try {
			inStream = new FileInputStream(file);
			i18nBundle.load(inStream);
		} catch (FileNotFoundException e) {
			//File cannot be opened
			//Add in log via AspectJ
		} catch (IOException e) {
			//Error during read file
			//Add in log via AspectJ
		}
		
		//Return properties map
		return i18nBundle;
	}
	 
	
}
