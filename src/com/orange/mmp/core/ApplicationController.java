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
package com.orange.mmp.core;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;


/**
 * Main entry point of MMP application this component
 * is aimed to control components behavior depending on
 * application state.
 * 
 * TODO Evolution P3 - Implement true orchestration features
 * 
 * @author tml
 *
 */
public class ApplicationController implements DestructionAwareBeanPostProcessor, org.springframework.context.ApplicationListener {

	/**
	 * Indicate if MMP application is started
	 */
	private boolean isRunning = false;
	
	/**
	 * ApplicationController Singleton
	 */
	private static ApplicationController applicationControllerSingleton;
	
	/**
	 * Saves the current instance ID
	 */
	private String instanceId = null;
	
	/**
	 * Indicates if current instance is a master one (admin)
	 */
	private boolean isMaster = false;
	
	
	/**
	 * Default constructor
	 */
	public ApplicationController(){
		ApplicationController.applicationControllerSingleton = this;
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	public void onApplicationEvent(ApplicationEvent applicationEvent) {
		if(applicationEvent instanceof ContextRefreshedEvent
				|| applicationEvent instanceof ContextStartedEvent){
			ApplicationController.applicationControllerSingleton.isRunning = true;
		}
		else if(applicationEvent instanceof ContextStoppedEvent){
			ApplicationController.applicationControllerSingleton.isRunning = false;
		}
	}	
				
	/**
	 * ApplicationController Singleton accessor
	 * 
	 * @return The ApplicationController Singleton
	 */
	public static ApplicationController getInstance(){
		return ApplicationController.applicationControllerSingleton;
	}
	
	/**
	 * @return the isStarted
	 */
	public boolean isRunning() {
		return this.isRunning;
	}

	/**
	 * Called to initialize ApplicationListener instances
	 */
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if(bean instanceof ApplicationListener){
			try{
				((ApplicationListener)bean).initialize();
			}catch(MMPException ce){
				throw new MMPRuntimeException("Failed to initialize '"+beanName+"'",ce);
			}
		}
		return bean;
	}

	/**
	 * Nothing done before initialization, can be overridden
	 */
	public Object postProcessBeforeInitialization(Object bean, String beanName)	throws BeansException {
		return bean;
	}

	/**
	 * Called to shutdown ApplicationListener instances
	 */
	public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
		if(bean instanceof ApplicationListener){
			try{
				((ApplicationListener)bean).shutdown();
			}catch(MMPException ce){
				throw new MMPRuntimeException("Failed to shutdown '"+beanName+"'",ce);
			}
		}
	}

	/**
	 * @return the instanceId
	 */
	public String getInstanceId() {
		return this.instanceId;
	}

	/**
	 * @param instanceId the instanceId to set
	 */
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	/**
	 * @return the isMaster
	 */
	public boolean isMaster() {
		return this.isMaster;
	}

	/**
	 * @param isMaster the isMaster to set
	 */
	public void setMaster(boolean isMaster) {
		this.isMaster = isMaster;
	}	
}
