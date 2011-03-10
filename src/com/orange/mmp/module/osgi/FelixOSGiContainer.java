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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;

import com.orange.mmp.core.ApplicationListener;
import com.orange.mmp.core.Constants;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.config.MMPConfig;
import com.orange.mmp.core.config.MMPConfigException;
import com.orange.mmp.core.config.MMPConfigLoader;
import com.orange.mmp.core.data.Element;
import com.orange.mmp.core.data.Module;
import com.orange.mmp.core.data.Version;
import com.orange.mmp.module.MMPModuleException;
import com.orange.mmp.module.ModuleContainer;
import com.orange.mmp.module.ModuleEvent;
import com.orange.mmp.module.ModuleObserver;

/**
 * Implementation of a ModuleContainer based on OSGi and Felix
 * 
 * @author Thomas MILLET
 *
 */
public class FelixOSGiContainer  implements ApplicationListener, ModuleContainer, BundleListener {

	
	/**
	 * Filter on module event to bypass none handled events
	 */
	public static final int CUMA_EVT_FILTER = ModuleEvent.MODULE_ADDED 
														| ModuleEvent.MODULE_STARTED 
														| ModuleEvent.MODULE_STOPPED 
														| ModuleEvent.MODULE_UPDATED 
														| ModuleEvent.MODULE_REMOVED;
	
	/**
     * Headers Containing the Bundle Manfiest version
     */
    public static final String BUNDLE_MANIFESTVERSION_HEADER = "Bundle-ManifestVersion";
    /**
     * Headers Containing the Bundle Activator
     */
    public static final String BUNDLE_ACTIVATOR_HEADER = "Bundle-Activator";
    /**
     * Headers Containing the Bundle description
     */
    public static final String BUNDLE_DESCRIPTION_HEADER = "Bundle-Description";
    /**
     * Headers Containing the Bundle category type
     */
    public static final String BUNDLE_CATEGORY_HEADER = "Bundle-Category";
    /**
     * Headers Containing the Bundle version
     */
    public static final String BUNDLE_VERSION_HEADER = "Bundle-Version";

    /**
     * Headers Containing the Bundle Symbolic name
     */
    public static final String BUNDLE_SYMBOLICNAME_HEADER = "Bundle-SymbolicName";

    /**
     * Headers Containing the widget name
     */
    public static final String BUNDLE_NAME_HEADER = "Bundle-Name";
	
	/**
	 * Single entry point on OSGi framework
	 */
	private HostActivator hostActivator;
	
	/**
	 * Set of Felix properties
	 */
	private Map<String,Object> felixProperties;
	
	/**
	 * Additional system packages to add to OSGi framework
	 */
	private List<String> systemPackages;
	
	/**
	 * List of module observers to register
	 */
	private List<ModuleObserver> moduleObservers;
	
	/**
	 * Reference on the embedded felix framework
	 */
	private Felix felixFramework;
	
	/**
	 * Cache for modules
	 */
	private Map<Module, Element> modulesCache;
	
	/**
	 * Cache for modules configuration
	 */
	private Map<Module, MMPConfig> modulesConfigurationCache;
	
	/**
   	 * An inner Lock for shared resources
   	 */
   	private final Lock lock = new ReentrantLock();
	
	public void initialize() throws MMPException {		
		//Initialize module observers
		this.moduleObservers = Collections.synchronizedList(new ArrayList<ModuleObserver>());
		//Initialize modules caches
		this.modulesCache = new ConcurrentHashMap<Module, Element>();
		//Initializes module configuration cache
		this.modulesConfigurationCache = new HashMap<Module, MMPConfig>();
		//Build system packages list in CSV format
		StringBuilder systemPackagesStr = new StringBuilder(); 
		for(String systemPackage : this.systemPackages){
			systemPackagesStr.append(systemPackage).append(",");
		}
		if(this.systemPackages.size() > 0){
			this.felixProperties.put(FelixConstants.FRAMEWORK_SYSTEMPACKAGES, systemPackagesStr.toString());
		}
	
		//Adds default Bundle activators
		List<BundleActivator> systemBundleActivators = new ArrayList<BundleActivator>();
		this.hostActivator = new HostActivator(this);
		systemBundleActivators.add(this.hostActivator);
		this.felixProperties.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, systemBundleActivators);
	
		try{
			//Build new instance of framework ...
			this.felixFramework = new Felix(this.felixProperties);
		
			//... and start it
			this.felixFramework.start();
		
		}catch(BundleException be){
			throw new MMPModuleException(be);
		}
	}
	
	public void shutdown() throws MMPException {
		if(this.moduleObservers != null) this.moduleObservers.clear();
		if(this.modulesCache != null) this.modulesCache.clear();
		if(this.modulesConfigurationCache != null) this.modulesConfigurationCache.clear();
		try{
			this.felixFramework.stop();
		}catch(BundleException be){
			throw new MMPModuleException(be);
		}
	}
	
	public Module addModule(Module module) throws MMPModuleException {
		try{
			this.lock.lock();
			if(module != null && module.getLocation() != null){
				try{
					Bundle bundle = this.hostActivator.getBundleContext().installBundle(module.getLocation().toString());
					module = this.bundleToModule(bundle);
					return module;
				}catch(BundleException be){
					throw new MMPModuleException(be);
				}
			}
			else throw new MMPModuleException("Unable to add module, no location specified");
		}finally{
			this.lock.unlock();
		}
	}

	public Module getModule(Module module) throws MMPModuleException {
		return (Module)((Element)this.modulesCache.get(module)).getKey();
	}

	public void registerModuleObserver(ModuleObserver moduleObserver) throws MMPModuleException {
		try{
			this.lock.lock();
			this.moduleObservers.add(moduleObserver);
		}finally{
			this.lock.unlock();
		}
	}

	public void removeModule(Module module) throws MMPModuleException {
		try{
			this.lock.lock();
			if(this.modulesCache.get(module) != null) {
				Bundle bundle = (Bundle)((Element)this.modulesCache.get(module)).getValue();
				bundle.stop();
				synchronized (bundle) {
					bundle.wait(5000);
				}	
			}
		}catch(BundleException be){
			throw new MMPModuleException(be);
		}catch(InterruptedException ie){
			throw new MMPModuleException(ie);
		}finally{
			this.lock.unlock();
		}
	}

	public void unregisterModuleObserver(ModuleObserver moduleObserver)	throws MMPModuleException {
		try{
			this.lock.lock();
			this.moduleObservers.remove(moduleObserver);
		}finally{
			this.lock.unlock();
		}
	}
	
	public void bundleChanged(BundleEvent bundleEvent) {
		//First check if event must be handled
		int eventType = bundleEvent.getType() & CUMA_EVT_FILTER;

		if(bundleEvent.getBundle().getBundleId() > 0 && eventType > 0){
			try{
				//Build event context
				ModuleEvent moduleEvent = null;
				Module module = this.bundleToModule(bundleEvent.getBundle());
				
				//Switch on event type
				switch(eventType){
					// Bundle installed - store in cache and start bundle
					case BundleEvent.INSTALLED :
						this.modulesCache.put(module,new Element(module, bundleEvent.getBundle()));
						this.modulesConfigurationCache.put(module,this.getModuleConfiguration(module));
						// Install module ...
						moduleEvent = new ModuleEvent(ModuleEvent.MODULE_ADDED,module);					
						//... notify listener ...
						for(ModuleObserver moduleObserver : this.moduleObservers) moduleObserver.onModuleEvent(moduleEvent);
						//... and auto start bundle.
						bundleEvent.getBundle().start();
					break;
					
					// Bundle started - NOP
					case BundleEvent.STARTED :
						//Notify modules observers
						moduleEvent = new ModuleEvent(ModuleEvent.MODULE_STARTED,module);
						for(ModuleObserver moduleObserver : this.moduleObservers) moduleObserver.onModuleEvent(moduleEvent);
					break;
					
					// Bundle stopped - NOP
					case BundleEvent.STOPPED :
						//Notify modules observers ...
						moduleEvent = new ModuleEvent(ModuleEvent.MODULE_STOPPED,module);
						for(ModuleObserver moduleObserver : this.moduleObservers) moduleObserver.onModuleEvent(moduleEvent);
						//... and auto uninstall module
						bundleEvent.getBundle().uninstall();					
					break;
					
					// Bundle uninstalled - remove it from cache
					case BundleEvent.UNINSTALLED :
						try{
							//Notify modules observers ...
							moduleEvent = new ModuleEvent(ModuleEvent.MODULE_REMOVED,module);
							for(ModuleObserver moduleObserver : this.moduleObservers) moduleObserver.onModuleEvent(moduleEvent);
							synchronized (bundleEvent.getBundle()) {
								bundleEvent.getBundle().notifyAll();
							}
						}finally{
							this.modulesCache.remove(module);
							this.modulesConfigurationCache.remove(module);
						}
					break;
				}
			}catch(BundleException be){
				//NOP - Just Log and return
			}catch(MMPModuleException ce){
				//NOP - Just Log and return
			}
		}
	}
	
	/**
	 * Tools method used to get a module from a bundle form
	 * @param bundle The input bundle
	 * @return A module instance based on the bundle
	 */
	@SuppressWarnings("unchecked")
	protected Module bundleToModule(Bundle bundle) throws MMPModuleException {
		Dictionary<String, String> bundleHeaders = bundle.getHeaders();
		Module module = new Module();
		module.setId(bundle.getSymbolicName());
		module.setCategory(bundleHeaders.get(BUNDLE_CATEGORY_HEADER));
		module.setVersion(new Version(bundleHeaders.get(BUNDLE_VERSION_HEADER)));
		module.setName(bundleHeaders.get(BUNDLE_NAME_HEADER));
		module.setLastModified(bundle.getLastModified());
		try {
			module.setLocation(new URI(bundle.getLocation()));
		} catch (URISyntaxException e) {
			throw new MMPModuleException("Failed to get module location",e);
		}
		return module;
	}
	
	@SuppressWarnings("unchecked")
	public Class loadModuleClass(Module module, String className) throws MMPModuleException {
		try{
			return ((Bundle)((Element)this.modulesCache.get(module)).getValue()).loadClass(className);
		}catch(NoClassDefFoundError ncde){
			throw new MMPModuleException(ncde);
		}catch(ClassNotFoundException cnfe){
			throw new MMPModuleException(cnfe);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.module.ModuleContainer#findModuleResource(com.orange.mmp.core.data.Module, java.lang.String, java.lang.String, boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<URL> findModuleResource(Module module, String path, String filePattern, boolean recurse) throws MMPModuleException {
		Bundle bundle = (Bundle)((Element)this.modulesCache.get(module)).getValue();
		Enumeration<URL> entriesEnum = bundle.findEntries(path, filePattern, recurse);
		if(entriesEnum != null){
			return Collections.list(entriesEnum);
		}
		else return new ArrayList<URL>();
	}

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.module.ModuleContainer#getModuleResource(com.orange.mmp.core.data.Module, java.lang.String)
	 */
	public URL getModuleResource(Module module, String resourcePath) throws MMPModuleException {
		return ((Bundle)((Element)this.modulesCache.get(module)).getValue()).getEntry(resourcePath);
	}
	

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.module.ModuleContainer#listModules()
	 */
	public List<Module> listModules() throws MMPModuleException {
		List<Module> moduleList = new ArrayList<Module>();
		moduleList.addAll(this.modulesCache.keySet());
		return moduleList;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.module.ModuleContainer#getModuleProperty(com.orange.mmp.core.data.Module, java.lang.String)
	 */
	public String getModuleProperty(Module module, String propertyName) throws MMPModuleException {
		Bundle bundle = (Bundle)((Element)this.modulesCache.get(module)).getValue();
		return (String)bundle.getHeaders().get(propertyName);
	}
	
	/* (non-Javadoc)
	 * @see com.orange.mmp.module.ModuleContainer#getModuleProperties(com.orange.mmp.core.data.Module)
	 */
	@SuppressWarnings("unchecked")
	public Properties getModuleProperties(Module module) throws MMPModuleException {
		Properties properties = new Properties();
		Bundle bundle = (Bundle)((Element)this.modulesCache.get(module)).getValue();
		Dictionary dictionary = bundle.getHeaders();
		Enumeration propertyKeys = bundle.getHeaders().keys();
		while(propertyKeys.hasMoreElements()){
			String propertyKey = (String)propertyKeys.nextElement();
			properties.setProperty(propertyKey, (String)dictionary.get(propertyKey));
		}
		return properties;
	}	

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.module.ModuleContainer#getModuleConfiguration(com.orange.mmp.core.data.Module)
	 */
	public MMPConfig getModuleConfiguration(Module module) throws MMPModuleException {
		MMPConfig moduleConfiguration = null;
		synchronized (this.modulesConfigurationCache){
			//Config not in Cache, get it from module
			if(!this.modulesConfigurationCache.containsKey(module)){
				URL moduleConfigurationURL = null;
				String moduleConfigurationFile = this.getModuleProperty(module, Constants.MODULE_CONFIGURATION_FILE_PROPERTY);
				//Get configuration from moduleConfigurationFile
				if(moduleConfigurationFile != null){
					moduleConfigurationURL = this.getModuleResource(module, moduleConfigurationFile);
				}
				//No configuration found, get it from default location
				else{ 
					moduleConfigurationURL = this.getModuleResource(module, Constants.DEFAULT_MODULE_CONFIGURATION_FILE);	
				}
				//Configuration file found, load configuration
				if(moduleConfigurationURL != null) {
					try{
						MMPConfigLoader mmpConfigLoader = new MMPConfigLoader(moduleConfigurationURL);
						moduleConfiguration = mmpConfigLoader.getConfiguration();
					}catch(MMPConfigException cce){
						throw new MMPModuleException(cce);
					}
				}
			}
			else moduleConfiguration = this.modulesConfigurationCache.get(module);
		}				 
		return moduleConfiguration;
	}

	/**
	 * @param systemPackages the systemPackages to set
	 */
	public void setSystemPackages(List<String> systemPackages) {
		this.systemPackages = systemPackages;
	}
	
	/**
	 * @param felixProperties the felixProperties to set
	 */
	public void setFelixProperties(Map<String, Object> felixProperties) {
		this.felixProperties = felixProperties;
	}

	/**
	 * @return the felixProperties
	 */
	public Map<String, Object> getFelixProperties() {
		return felixProperties;
	}

	/**
	 * Inner class used as a bridge from OSGi framework to main application
	 * 
	 * @author tml
	 *
	 */
	public class HostActivator implements BundleActivator{
	    
		/**
		 * Reference on the bundle context
		 */
		private BundleContext context = null;
		
		/**
		 * Reference on the main bundle listener
		 */
		private BundleListener mainListener = null;
		
		/**
		 * Default constructor
		 * @param mainListener The listener on which events are delegated
		 */
		public HostActivator(BundleListener mainListener){
			this.mainListener = mainListener;
		}
		
		/**
		 * Called at framework start
		 */
	    public void start(BundleContext context){
	    	this.context = context;
	    	context.addBundleListener(this.mainListener);
	    }

	    /**
	     * Called at framework stop
	     */
	    public void stop(BundleContext context){
	    	this.context.removeBundleListener(this.mainListener);
	    	this.context = null;
	    }
	    
	    /**
	     * Gets the bundle context
	     * @return a BundleContext instance
	     */
	    public BundleContext getBundleContext(){
	    	return this.context;
	    }

	    /**
	     * List bundles
	     * @return an array of bundles
	     */
	    public Bundle[] getBundles(){
            return this.context.getBundles();
	    }
	}
}
