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
package com.orange.mmp.log.aop;


import com.orange.mmp.context.ExecutionContext;

/**
 * Aspect for logging purpose
 * 
 * @author Thomas MILLET
 *
 */
public aspect Logging {
	
	/**********************************************************
     * INFO LOGGER START
     **********************************************************/
	
	/**
	 * MMPComponents START/STOP
	 */
	pointcut componentInitialize() :
		execution(void com.orange.mmp.core.ApplicationListener.initialize());

	@SuppressWarnings("unchecked")
	after() : componentInitialize(){
		try{
			String componentClassname = thisJoinPoint.getTarget().getClass().getName();
			com.orange.mmp.log.LogManagerFactory.getInstance().getLogManager().log(componentClassname, com.orange.mmp.log.LogManager.LEVEL_INFO, "COMPONENT STARTED - "+componentClassname);
		}catch(Exception ex){
			//NOP
		}
	}

	pointcut componentShutdown() :
		execution(void com.orange.mmp.core.ApplicationListener.shutdown());

	@SuppressWarnings("unchecked")
	after() : componentShutdown(){
		try{
			String componentClassname = thisJoinPoint.getTarget().getClass().getName();
			com.orange.mmp.log.LogManagerFactory.getInstance().getLogManager().log(componentClassname, com.orange.mmp.log.LogManager.LEVEL_INFO, "COMPONENT STOPPED - "+componentClassname);
		}catch(Exception ex){
			//NOP
		}
	}
	
	/**
	 * ModuleContainer
	 */
	pointcut addModule(com.orange.mmp.core.data.Module module) :
		args(module)
		&& execution (com.orange.mmp.core.data.Module com.orange.mmp.module.ModuleContainer.addModule(com.orange.mmp.core.data.Module));
	
	@SuppressWarnings("unchecked")
	after(com.orange.mmp.core.data.Module module) returning(com.orange.mmp.core.data.Module newModule) : addModule(module){
		try{
			String componentClassname = thisJoinPoint.getTarget().getClass().getName();
			com.orange.mmp.log.LogManagerFactory.getInstance().getLogManager().log(componentClassname, com.orange.mmp.log.LogManager.LEVEL_INFO, "MODULE ADDED - '"+newModule.getName()+ "' from '"+newModule.getLocation()+"'");
		}catch(Exception ex){
			//NOP
		}
	}
	
	@SuppressWarnings("unchecked")
	after(com.orange.mmp.core.data.Module module) throwing(Exception e) : addModule(module){
		try{
			String componentClassname = thisJoinPoint.getTarget().getClass().getName();
			com.orange.mmp.log.LogManagerFactory.getInstance().getLogManager().log(componentClassname, com.orange.mmp.log.LogManager.LEVEL_WARN, "MODULE ERROR - Failed to add module from '"+module.getLocation()+"'");
		}catch(Exception ex){
			//NOP
		}
	}
	
	pointcut removeModule(com.orange.mmp.core.data.Module module) :
		args(module)
		&& execution (void com.orange.mmp.module.ModuleContainer.removeModule(com.orange.mmp.core.data.Module));
	
	@SuppressWarnings("unchecked")
	after(com.orange.mmp.core.data.Module module) : removeModule(module){
		try{
			String componentClassname = thisJoinPoint.getTarget().getClass().getName();
			com.orange.mmp.log.LogManagerFactory.getInstance().getLogManager().log(componentClassname, com.orange.mmp.log.LogManager.LEVEL_INFO, "MODULE REMOVED - '"+module.getId()+ "'");
		}catch(Exception ex){
			//NOP
		}
	}
	
	@SuppressWarnings("unchecked")
	after(com.orange.mmp.core.data.Module module) throwing(Exception e) : removeModule(module){
		try{
			String componentClassname = thisJoinPoint.getTarget().getClass().getName();
			com.orange.mmp.log.LogManagerFactory.getInstance().getLogManager().log(componentClassname, com.orange.mmp.log.LogManager.LEVEL_WARN, "MODULE ERROR - Failed to remove module '"+module.getName()+"'");
		}catch(Exception ex){
			//NOP
		}
	}
	
	pointcut bundleStartError(org.osgi.framework.BundleException bundleException):
		args(bundleException) &&
		within(com.orange.mmp.module.osgi.FelixOSGiContainer) &&
		handler(org.osgi.framework.BundleException+);
	
	before(org.osgi.framework.BundleException bundleException) : bundleStartError(bundleException){
		try{
			Throwable throwable = bundleException.getCause();
			if(throwable != null){
				String errorLine = new StringBuilder(thisJoinPoint.getSourceLocation().getWithinType().getSimpleName())
					.append(":")
					.append(thisJoinPoint.getSourceLocation().getLine())
					.append(" - Bundle Error cause : ")
					.append(bundleException.getCause()).toString();
				com.orange.mmp.log.LogManagerFactory.getInstance().getLogManager().log(bundleException.getClass().getName(), com.orange.mmp.log.LogManager.LEVEL_WARN, errorLine);
			}
		}catch(Exception e){
			//NOP Catch All to avoid AOP interferences with application code
		}
	}
	
	/**
	 * ServiceContainer
	 */
	pointcut addApi(com.orange.mmp.core.data.Api api, Object apiImpl) :
		args(api,apiImpl)
		&& execution (com.orange.mmp.core.data.Api com.orange.mmp.api.ApiContainer.addApi(com.orange.mmp.core.data.Api, Object));
	
	@SuppressWarnings("unchecked")
	after(com.orange.mmp.core.data.Api api, Object apiImpl) returning(com.orange.mmp.core.data.Api newApi) : addApi(api,apiImpl){
		try{
			String componentClassname = thisJoinPoint.getTarget().getClass().getName();
			com.orange.mmp.log.LogManagerFactory.getInstance().getLogManager().log(componentClassname, com.orange.mmp.log.LogManager.LEVEL_INFO, "API ADDED - '"+api.getName()+ "'");
		}catch(Exception ex){
			//NOP Catch All to avoid AOP interferences with application code
		}
	}
	
	@SuppressWarnings("unchecked")
	after(com.orange.mmp.core.data.Api api, Object apiImpl) throwing(Exception e) : addApi(api,apiImpl){
		try{
			String componentClassname = thisJoinPoint.getTarget().getClass().getName();
			com.orange.mmp.log.LogManagerFactory.getInstance().getLogManager().log(componentClassname, com.orange.mmp.log.LogManager.LEVEL_WARN, "API ERROR - Failed to add API '"+api.getName()+ "'");
		}catch(Exception ex){
			//NOP Catch All to avoid AOP interferences with application code
		}
	}
	
	pointcut removeApi(com.orange.mmp.core.data.Api api) :
		args(api)
		&& execution (void com.orange.mmp.api.ApiContainer.removeApi(com.orange.mmp.core.data.Api));
	
	@SuppressWarnings("unchecked")
	before(com.orange.mmp.core.data.Api api) : removeApi(api){
		try{
			String componentClassname = thisJoinPoint.getTarget().getClass().getName();
			com.orange.mmp.log.LogManagerFactory.getInstance().getLogManager().log(componentClassname, com.orange.mmp.log.LogManager.LEVEL_INFO, "API REMOVED - '"+api.getName()+ "'");
		}catch(Exception ex){
			//NOP Catch All to avoid AOP interferences with application code
		}
	}
	
	@SuppressWarnings("unchecked")
	after(com.orange.mmp.core.data.Api api) throwing(Exception e) : removeApi(api){
		try{
			String componentClassname = thisJoinPoint.getTarget().getClass().getName();
			com.orange.mmp.log.LogManagerFactory.getInstance().getLogManager().log(componentClassname, com.orange.mmp.log.LogManager.LEVEL_WARN, "API ERROR - Failed to remove API '"+api.getName()+ "'");
		}catch(Exception ex){
			//NOP Catch All to avoid AOP interferences with application code
		}
	}
	/**********************************************************
     * INFO LOGGER END
     **********************************************************/
	
	/**********************************************************
     * ERROR LOGGER START
     **********************************************************/
	
    pointcut warnLog(Exception e) :
    	args(e) &&
    	!within(com.orange.mmp.log.aop.Logging) &&
    	!within(com.orange.mmp.log.log4j.Log4jLogManager) &&
    	handler(Exception+);
    before(Exception e) : warnLog(e){
    	try{
    		//Trace error in execution context (if execution context exist)
    		ExecutionContext.getInstance().addWarningMsg(e.getMessage(), e);
    		
    		//Trace error in mmp log file
    		String errorLine = new StringBuilder(thisJoinPoint.getSourceLocation().getWithinType().getSimpleName())
												.append(":")
												.append(thisJoinPoint.getSourceLocation().getLine())
												.append(" - ")
												.append(e.getMessage()).toString(); 
    		if(e instanceof RuntimeException){
    			com.orange.mmp.log.LogManagerFactory.getInstance().getLogManager().log(e.getClass().getName(), com.orange.mmp.log.LogManager.LEVEL_FATAL, errorLine);
    		}
    		else {
    			com.orange.mmp.log.LogManagerFactory.getInstance().getLogManager().log(e.getClass().getName(), com.orange.mmp.log.LogManager.LEVEL_WARN, errorLine);
    		}

    	}catch(Exception ex){
			//NOP Catch All to avoid AOP interferences with application code
		}
	}
    
    /**********************************************************
     * ERROR LOGGER END
     **********************************************************/
	
}
