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
package com.orange.mmp.widget;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

import com.orange.mmp.cache.Cache;
import com.orange.mmp.cache.CacheManagerFactory;
import com.orange.mmp.cache.MMPCacheException;
import com.orange.mmp.core.ApplicationListener;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.MMPRuntimeException;
import com.orange.mmp.core.data.Branch;
import com.orange.mmp.core.data.Element;
import com.orange.mmp.core.data.Module;
import com.orange.mmp.core.data.Service;
import com.orange.mmp.core.data.Version;
import com.orange.mmp.core.data.Widget;
import com.orange.mmp.dao.DaoManagerFactory;
import com.orange.mmp.module.MMPModuleException;
import com.orange.mmp.module.ModuleContainerFactory;
import com.orange.mmp.module.ModuleEvent;
import com.orange.mmp.module.ModuleObserver;
import com.orange.mmp.module.osgi.FelixOSGiContainer;
import com.orange.mmp.module.osgi.MMPOSGiContainer;
import com.orange.mmp.mvc.bundle.Controller;


/**
 * Contains the catalog of widgets provided by the PFS.
 */
public class WidgetManager implements ModuleObserver, ApplicationListener{

	/**
	 * Precompiled pattern used to detect branches
	 */
	public static Pattern branchPattern = Pattern.compile("^(.*)"+Constants.BRANCH_SUFFIX_PATTERN+"("+Constants.BRANCH_ID_PATTERN+")$");
	
	/**
	 * Singleton for access outside application context
	 */
	private static WidgetManager widgetManagerSingleton;
   	
   	/**
   	 * Pointer on the widgets Cache
   	 */
   	private Cache widgetsCache;   	
   	
	/**
	 * The name of the Cache for widgets
	 */
	private String widgetCacheName;	
	
	/**
	 * Indicates if WidgetManager is running
	 */
	private boolean isRunning;
	
	
	/**
	* Singleton access 
	* 
	* @return The WidgetManager singleton
	*/
	public static WidgetManager getInstance(){
		return WidgetManager.widgetManagerSingleton;
	}
	
	/**
	 * Starts the M4M catalog
	 */
	public void initialize() throws MMPException{
		if(!this.isRunning){
			widgetManagerSingleton = this;
			
			try {
				this.widgetsCache = CacheManagerFactory.getInstance().getCacheManager().getCache(this.widgetCacheName);
			} catch (MMPCacheException mce) {
				throw new MMPRuntimeException("Failed to start WidgetManager, CacheManager is unavailable",mce);
			}
			
			try{
				ModuleContainerFactory.getInstance().getModuleContainer().registerModuleObserver(this);
			}catch(MMPModuleException mme){
				throw new MMPRuntimeException("Failed to start WidgetManager, ModuleContainer is unavailable",mme);
			}
		}
	}

	/**
	 * Stops the M4M catalog
	 */
	public void shutdown() throws MMPException{
		if(this.isRunning){
			this.isRunning=false;
			ModuleContainerFactory.getInstance().getModuleContainer().unregisterModuleObserver(this);
			this.widgetsCache.clear();
			widgetManagerSingleton = null;
		}
	}
	
	/**
	 * ModuleObserver implementation
	 * 
	 * @param moduleEvent The module event
	 */
	@SuppressWarnings("unchecked")
	public void onModuleEvent(ModuleEvent moduleEvent) {
		Module module = moduleEvent.getModule();
		if(module !=  null && module.getCategory() != null && 
				module.getCategory().equalsIgnoreCase(com.orange.mmp.core.Constants.MODULE_CATEGORY_WIDGET)){
			try{
				Matcher branchMatcher = WidgetManager.branchPattern.matcher(module.getId());
				String widgetId = null;
				String widgetBranchId = null;
				//Get WIdget branch or default one if no indicated
				if(branchMatcher.matches()){
					widgetId = branchMatcher.group(1);
					widgetBranchId = branchMatcher.group(2);					
				}
				else{
					Branch branch = new Branch();
					branch.setDefault(true);
					Branch []branches = (Branch[])DaoManagerFactory.getInstance().getDaoManager().getDao("branch").find(branch);
					if(branches.length == 0) throw new MMPRuntimeException("No Default branch configured, check configuration");
					widgetId = module.getId();
					widgetBranchId = branches[0].getId();					
				}
				if(moduleEvent.getType() == ModuleEvent.MODULE_STARTED){
					Widget widgetToAdd = new Widget();
					widgetToAdd.setId(widgetId);
					widgetToAdd.setBranchId(widgetBranchId);
					widgetToAdd.setCategory(com.orange.mmp.core.Constants.MODULE_CATEGORY_WIDGET);
					widgetToAdd.setLocation(module.getLocation());
					widgetToAdd.setName(module.getName());
					widgetToAdd.setVersion(module.getVersion());
					widgetToAdd.setLastModified(module.getLastModified());
					this.widgetsCache.set(new Element(widgetId + Constants.BRANCH_SUFFIX_PATTERN + widgetBranchId, widgetToAdd));
				}
				else if(moduleEvent.getType() == ModuleEvent.MODULE_STOPPED){
					this.widgetsCache.remove(widgetId + Constants.BRANCH_SUFFIX_PATTERN + widgetBranchId);
				}
			}catch(MMPException me){
				try{
					ModuleContainerFactory.getInstance().getModuleContainer().removeModule(module);
				}catch(MMPModuleException mme){
					//NOP
				}
			}
		}
	}
	
	/**
	 * Gets a running widget from the default branch
	 * @param widgetId The id of the widget
	 * @return A widget instance
	 */
	@SuppressWarnings("unchecked")
	public Widget getWidget(String widgetId) throws MMPException {
		Branch branch = new Branch();
		branch.setDefault(true);
		Branch branches[] = (Branch[])DaoManagerFactory.getInstance().getDaoManager().getDao("branch").find(branch);
		if(branches.length > 0)	return this.getWidget(widgetId,branches[0].getId());
		else throw new MMPException("No default branch found, unable to find widget");
	}
	
	/**
	 * Gets a running widget
	 * @param widgetId The id of the widget
	 * @param branchId The id of the branch
	 * @return A widget instance
	 */
	public Widget getWidget(String widgetId, String branchId) throws MMPException {
		String widgetCacheId =  widgetId + Constants.BRANCH_SUFFIX_PATTERN + branchId;
		if(this.widgetsCache.isKeyInCache(widgetCacheId)) {
			return (Widget)this.widgetsCache.get(widgetCacheId).getValue(); 
		}
		else return null;
	}
	
	/**
	 * Gets a list running widgets
	 * @return A list of widgets
	 */
	@SuppressWarnings("unchecked")
	public Widget[] getWidgetsList() throws MMPException {
		List<Element> widgetElements = this.widgetsCache.getElements();
		List<Widget> widgetList = new ArrayList<Widget>();
		for(Element widgetElement : widgetElements){
			widgetList.add((Widget)widgetElement.getValue());
		}
		Widget[] widgets = new Widget[widgetElements.size()];
		return widgetList.toArray(widgets);
	}

	/**
	 * Gets a list running widgets from  a specific branch
	 * 
	 * @param branchId The Widget branch ID
	 * @return A list of widgets
	 */
	public Widget[] getWidgetsList(String branchId) throws MMPException{
		List<Element> widgetElements = this.widgetsCache.getElements();
		List<Widget> widgetList = new ArrayList<Widget>();
		for(Element widgetElement : widgetElements){
			Widget widget = (Widget)widgetElement.getValue();
			if(widget.getBranchId().equals(branchId)) widgetList.add(widget);
		}
		
		Widget[] widgets = new Widget[widgetList.size()];
		return widgetList.toArray(widgets);
	}

	/**
     * Used to build an URL for static resources in a widget using default branch 
     * @param currentService The current detected service
     * @param resourcePath	The resource path (ex: /m4m/icon.png)
     * @param widgetId	The widget id
     * @return		An URL string to download M4M resources through AppDownloadController
     */
    @SuppressWarnings("unchecked")
	public URL getWidgetResourceUrl(Service currentService, String resourcePath, String widgetId) throws MMPException{
    	Branch branch = new Branch();
		branch.setDefault(true);
		Branch branches[] = (Branch[])DaoManagerFactory.getInstance().getDaoManager().getDao("branch").find(branch);
		if(branches.length > 0){
			return this.getWidgetResourceUrl(currentService, resourcePath, widgetId, branches[0].getId());
		}
		else throw new MMPException("No default branch found, unable to access widget");
    }
	
    /**
     * Used to build an URL for static resources in a widget using a specific branch
     * @param currentService The current detected service
     * @param resourcePath	The resource name (ex: /m4m/icon.png)
     * @param widgetId	The widget id
     * @param branchId	The branch id
     * @return		An URL string to download M4M resources through AppDownloadController
     */
    public URL getWidgetResourceUrl(Service currentService, String resourcePath, String widgetId, String branchId) throws MMPException{
    	String widgetCacheId =  widgetId + Constants.BRANCH_SUFFIX_PATTERN + branchId;
    	if(this.widgetsCache.isKeyInCache(widgetCacheId)) {
           	try{
           		StringBuilder resourceUrl = new StringBuilder(Controller.getUrlMapping()).append("/").append(URLEncoder.encode(widgetId,com.orange.mmp.core.Constants.DEFAULT_ENCODING)).append(resourcePath);
           		return new URL("http",currentService.getHostname(),resourceUrl.toString());
           	}catch(UnsupportedEncodingException uee){
           		throw new MMPException("Failed to build resource URL",uee);
           	}catch(MalformedURLException mue){
           		throw new MMPException("Failed to build resource URL",mue);
           	}
   		}
   		else throw new MMPException("Widget "+widgetId+" not found in branch "+branchId);
    }

    /**
     * Used to get an static resources of a widget using default branch
     * 
     * @param resourcePath The full resource path (ex : /m4m/2/en/icon.png)
     * @param widgetId The widget id
     * 
     * @return An InputStream on that resource
     */
    @SuppressWarnings("unchecked")
	public InputStream getWidgetResource(String resourcePath, String widgetId) throws MMPException{
    	Branch branch = new Branch();
		branch.setDefault(true);
		Branch branches[] = (Branch[])DaoManagerFactory.getInstance().getDaoManager().getDao("branch").find(branch);
		if(branches.length > 0){
			return this.getWidgetResource(resourcePath, widgetId, branches[0].getId());
		}
		else throw new MMPException("No default branch found, unable to access widget");
    }
    
    /**
     * Used to get an static resources of a widget using a specific branch
     * 
     * @param resourcePath The full resource path (ex : /m4m/1/en/icon.png)
     * @param widgetId The widget id
     * @param branchId The branch id
     * 
     * @return An InputStream on that resource
     */
    @SuppressWarnings("unchecked")
	public InputStream getWidgetResource(String resourcePath, String widgetId, String branchId) throws MMPException{
    	try{
    		Module module = new Module();
    		module.setId(widgetId + Constants.BRANCH_SUFFIX_PATTERN + branchId);
    		Module[] tmpModules = (Module[])DaoManagerFactory.getInstance().getDaoManager().getDao("module").find(module);
    		if(tmpModules != null && tmpModules.length > 0)
    			module = tmpModules[0];
    		else throw new MMPException("Widget "+widgetId+" not found");
    		URL resourceUrl = ModuleContainerFactory.getInstance().getModuleContainer().getModuleResource(module, resourcePath);

    		if(resourceUrl != null){
    			return resourceUrl.openConnection().getInputStream();
    		}
    		else throw new MMPException("Resource "+resourcePath+" for widget "+widgetId+" not found");
    	}catch(IOException ioe){
    		throw new MMPException("Failed to get "+resourcePath+" for widget "+widgetId+" not found", ioe);
    	}
    }
    
    /**
     * Used to find static resources from a widget and matching XTags using default branch
     * 
     * @param resourcePath The base path for the research 
     * @param resourcePattern	The pattern for seeking resource (* is supported)
     * @param widgetId 		The widget id
     * @param recurse 		Indicates to make a recursive search
     * 
     * @return 			A List of String paths
     */
    @SuppressWarnings("unchecked")
	public List<URL> findWidgetResources(String resourcePath, String resourcePattern, String widgetId, boolean recurse) throws MMPException{
    	Branch branch = new Branch();
		branch.setDefault(true);
		Branch branches[] = (Branch[])DaoManagerFactory.getInstance().getDaoManager().getDao("branch").find(branch);
		if(branches.length > 0){
			return this.findWidgetResources(resourcePath, resourcePattern, branches[0].getId(), recurse);
		}
		else throw new MMPException("No default branch found, unable to access widget");
    }

    /**
     * Used to find static resources from a widget and matching XTags using specific branch
     * 
     * @param resourcePath The base path for the research 
     * @param resourcePattern	The pattern for seeking resource (* is supported)
     * @param widgetId 		The widget id
     * @param branchId 		The branch id
     * @param recurse 		Indicates to make a recursive search
     * 
     * @return 			A List of String paths
     */
    public List<URL> findWidgetResources(String resourcePath, String resourcePattern, String widgetId, String branchId, boolean recurse) throws MMPException{
    	String widgetCacheId =  widgetId + Constants.BRANCH_SUFFIX_PATTERN + branchId;
    	if(this.widgetsCache.isKeyInCache(widgetCacheId)) {
			if(resourcePath == null) resourcePath = "/";
			else if(!resourcePath.endsWith("/")) resourcePath += "/";
			Module module = new Module();
			module.setId(widgetCacheId);
			return ModuleContainerFactory.getInstance().getModuleContainer().findModuleResource(module, resourcePath, resourcePattern, recurse);
    	}
    	else throw new MMPException("Widget "+widgetCacheId+" not found");
    }
    
    /**
     * Remove a widget from its ID and Branch ID
     * 
     * @param widgetId The widget ID
     * @param branchId The widget branch ID
     * @throws MMPException
     */
    public void undeployWidget(String widgetId, String branchId) throws MMPException {
    	String moduleId = widgetId + Constants.BRANCH_SUFFIX_PATTERN + branchId;
    	MMPOSGiContainer moduleContainer = (MMPOSGiContainer) ModuleContainerFactory.getInstance().getModuleContainer();
    	moduleContainer.undeployModule(moduleId);
    }
    
    /**
     * Add branch ID to a widget Manifest before deploying it
     * @param widgetFile
     * @param branchId
     * @return a Widget instance
     * @throws IOException 
     * @throws MMPException 
     */
    public Widget deployWidget(File widgetFile, String branchId) throws MMPException {
    	Widget widget = new Widget();
    	ZipInputStream zin = null;
    	ZipOutputStream zout = null;
    	
    	try{
    		JarFile jarFile = new JarFile(new File(widgetFile.toURI()));
    		Manifest manifest = jarFile.getManifest();
    		
    		String tmpWidgetId = manifest.getMainAttributes().getValue(FelixOSGiContainer.BUNDLE_SYMBOLICNAME_HEADER); 
    		widget.setBranchId(branchId);
    		
    		if(tmpWidgetId != null){
    			widget.setName(manifest.getMainAttributes().getValue(FelixOSGiContainer.BUNDLE_NAME_HEADER));
    			widget.setId(tmpWidgetId + com.orange.mmp.widget.Constants.BRANCH_SUFFIX_PATTERN + branchId);
    			manifest.getMainAttributes().putValue(FelixOSGiContainer.BUNDLE_SYMBOLICNAME_HEADER, widget.getId());
    			
    			File tempFile = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".jar");
    			
    			zin = new ZipInputStream(new FileInputStream(widgetFile));
    		    zout = new ZipOutputStream(new FileOutputStream(tempFile));
    			
    		    ZipEntry entry = zin.getNextEntry();
    		    while (entry != null) {
    	    	    String name = entry.getName();
    	    	    zout.putNextEntry(new ZipEntry(name));
    	    	    if(!name.equals(com.orange.mmp.midlet.Constants.JAR_MANIFEST_ENTRY)) {
    	    	        IOUtils.copy(zin, zout);
    	    	    }
    	    	    else{
    	    	    	manifest.write(zout);
    	    	    }
    	    	    entry = zin.getNextEntry();
    		    }
    		    
    			widget.setLocation(tempFile.toURI());
    			widget.setId(tmpWidgetId);
    			widget.setLastModified(tempFile.lastModified());
    			widget.setCategory(com.orange.mmp.core.Constants.MODULE_CATEGORY_WIDGET);
    			widget.setVersion(new Version(manifest.getMainAttributes().getValue(FelixOSGiContainer.BUNDLE_VERSION_HEADER)));
    		}
    		else{
    			throw new MMPException("Invalid module archive, missing " + FelixOSGiContainer.BUNDLE_SYMBOLICNAME_HEADER + " header");
    		}
    	
    	}catch(IOException ioe){
    		throw new MMPException("Failed to deploy widget", ioe);
    	}
    	finally{
    		if(zin != null){
    			try{
    				zin.close();
    			}catch(IOException ioe){
    				//NOP
    			}
    		}
    		if(zout != null)
    			try{
    				zout.close();
    			}catch(IOException ioe){
    				//NOP
    			}
    	}
    	
    	MMPOSGiContainer moduleContainer = (MMPOSGiContainer) ModuleContainerFactory.getInstance().getModuleContainer();
    	moduleContainer.deployModule(new File(widget.getLocation()));
    	
    	return widget;
    }

	/**
	 * @param widgetCacheName the widgetCacheName to set
	 */
	public void setWidgetCacheName(String widgetCacheName) {
		this.widgetCacheName = widgetCacheName;
	}
}