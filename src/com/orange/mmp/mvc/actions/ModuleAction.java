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
package com.orange.mmp.mvc.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.opensymphony.xwork2.ActionSupport;
import com.orange.mmp.api.ApiContainerFactory;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.data.Api;
import com.orange.mmp.core.data.Branch;
import com.orange.mmp.core.data.Mobile;
import com.orange.mmp.core.data.Module;
import com.orange.mmp.core.data.Widget;
import com.orange.mmp.dao.DaoManagerFactory;
import com.orange.mmp.module.ModuleContainerFactory;
import com.orange.mmp.module.osgi.MMPOSGiContainer;
import com.orange.mmp.service.ServiceManager;
import com.orange.mmp.widget.WidgetManager;

public class ModuleAction extends ActionSupport {

	private static final long serialVersionUID = 1L;
	
	/**
	 * The uploaded Widget
	 */
	private File uploadedFile;
	
	/**
	 * The branch ID of the Widget to add
	 */
	private String branchId;

	/**
	 * The Branch list
	 */
	private Branch[] branch;
	
	/**
	 * The ID of the Widget to delete
	 */
	private String id;

	/**
	 * Bean to display Modules MAP 
	 */
	private HashMap<Module, String> modulesMap;
	
	/**
	 * Bean to display Widget MAP 
	 */
	private HashMap<Widget, HashMap<Branch, String[]>> widgetsMap;
	
	/**
	 * Bean to display Web Services MAP 
	 */
	private HashMap<Api, String[]> webServicesMap;
	
	/**
	 * Bean to display Libraries MAP 
	 */
	private HashMap<Module, String[]> librariesMap;
	
    /**
     * Header Containing the Bundle category type
     */
    public static final String BUNDLE_CATEGORY_HEADER = "Bundle-Category";

    /**
     * Header Containing the Bundle Symbolic name
     */
    public static final String BUNDLE_SYMBOLICNAME_HEADER = "Bundle-SymbolicName";
    
    /**
     * Header Containing the Bundle Export Packages
     */
    public static final String BUNDLE_EXPORT_PACKAGES = "Export-Package";
    
    /**
	 * Header Containing the Module name
	 */
    public static final String BUNDLE_NAME_HEADER = "Bundle-Name";
    
	/**
	 * Var used to sort widgets
	 */
	private String sort;
	
	/**
	 * Var used to sort modules (ascend/descend)
	 */
	private String sortOrder;
	
	/**
	 * Var used to sort modules (name/type)
	 */
	private String sortParam;
	
	/**
	 * WidgetManager instance
	 */
	private WidgetManager widgetManager = WidgetManager.getInstance();
	
	/**
	 * Current type of module
	 */
	private String type;
	
	private InputStream inputStream;
	private String fileName;
	private String fileSize;
	  
	
	/**
	 * ACTIONS
	 */
	
	@SuppressWarnings("unchecked")
	public String execute() throws Exception {
		try {
			branch = (Branch[])DaoManagerFactory.getInstance().getDaoManager().getDao("branch").find(new Branch());
		} catch (MMPException mmpe) {
			addActionError(getText("error.module.list.branches", new String[] {mmpe.getLocalizedMessage()}));
		}
		if(sortOrder != null && !sortOrder.equals("")
				&& sortParam != null && !sortParam.equals(""))
			buildModulesMap(sortOrder, sortParam);
		else
			buildModulesMap("descend", "name");
		
		return super.execute();
	}
	
	/**
	 * Upload a module
	 * @return
	 * @throws Exception
	 */
	public String upload() throws Exception {
		if(uploadedFile == null) {
			addActionError(getText("error.module.upload", new String[] {getText("error.module.nofile", new String[] {})}));
			return execute();
		}
		
		Module module = new Module();
		module.setLocation(uploadedFile.toURI());
		// Parser le Manifest � l'upload
		JarFile jarFile = null;
    	try {
    		jarFile = new JarFile(new File(uploadedFile.toURI()));
    		Manifest manifest = jarFile.getManifest();
    		if(manifest == null) {
    			addActionError(getText("error.module.upload", new String[] {" invalid module archive, MANIFEST file not found"}));
    			return execute();
    		}
    		// Get Bundle category
    		Attributes attributes = manifest.getMainAttributes();
    		String category = attributes.getValue(BUNDLE_CATEGORY_HEADER);

    		if (category != null && category.equals(com.orange.mmp.core.Constants.MODULE_CATEGORY_WIDGET)) {
    			Widget widget = this.widgetManager.deployWidget(uploadedFile, branchId);
    			addActionMessage(getText("message.module.upload", new String[] {widget.getName()}));
    		} else {
    			MMPOSGiContainer moduleContainer = (MMPOSGiContainer) ModuleContainerFactory.getInstance().getModuleContainer();
        		module = moduleContainer.deployModule(uploadedFile);	
        		addActionMessage(getText("message.module.upload", new String[] {module.getName()}));
    		}
    		
    	} catch (IOException ioe) {
    		addActionError(getText("error.module.upload", new String[] {" failed to add module : "+ioe.getMessage()}));
			return execute();
    	} catch (MMPException mmpe) {
    		addActionError(getText("error.module.upload", new String[] {" failed to add module : "+mmpe.getMessage()}));
			return execute();
    	}
    	
		return execute();
	}

	/**
	 * Display widgets list
	 */
	public String widget() {
		try {
			if(sort != null && !sort.equals(""))
				buildWidgetsMap(sort);
			else
				buildWidgetsMap(null);
		} catch (MMPException mmpe) {
			addActionError(getText("error.widget.init", new String[] {mmpe.getLocalizedMessage()}));
		}
		return "widget";
	}
	
	/**
	 * Display web services list
	 */
	public String webservice() {
		try {
			buildWebServicesMap();
		} catch (MMPException mmpe) {
			addActionError(getText("error.webservice.init", new String[] {mmpe.getLocalizedMessage()}));
		}
		return "webservice";
	}
	
	/**
	 * Display libraries list
	 */
	public String library() {
		try {
			buildLibrariesMap();
		} catch (MMPException mmpe) {
			addActionError(getText("error.library.init", new String[] {mmpe.getLocalizedMessage()}));
		} catch (IOException ioe) {
			addActionError(getText("error.library.init", new String[] {ioe.getLocalizedMessage()}));
		}
		return "library";
	}
	
	/**
	 * Remove a module
	 * @throws Exception 
	 */
	public String remove_module() throws Exception {
		if(getId() == null || getId().equals("")) {
			addActionError(getText("error.module.remove", new String[] {getText("error.module.notfound", new String[] {})}));
			return execute();
		}
		
		try {
			MMPOSGiContainer moduleContainer = (MMPOSGiContainer) ModuleContainerFactory.getInstance().getModuleContainer();
    		moduleContainer.undeployModule(getId());
		} catch(MMPException me) {
			addActionError(getText("error.module.remove", new String[] {me.getLocalizedMessage()}));
			if(getType() != null && getType().equals("widget"))
				return widget();
			if(getType() != null && getType().equals("library"))
				return library();
			return execute();
		} catch (Exception e) {
			if(getType() != null && getType().equals("widget"))
				return widget();
			if(getType() != null && getType().equals("library"))
				return library();
			return execute();
		}
		
		addActionMessage(getText("message.module.remove", new String[] {getId()}));
		
		if(getType() != null && getType().equals("widget"))
			return widget();
		if(getType() != null && getType().equals("library"))
			return library();
		return execute();
	}
	
	/**
	 * Download a module. Return JAR file of module.
	 * @throws Exception 
	 */
	public String download_module() throws Exception {
		if(getId() == null || getId().equals("")) {
			addActionError(getText("error.module.download", new String[] {getText("error.module.notfound", new String[] {})}));
			return execute();
		}
		
		try {
			MMPOSGiContainer moduleContainer = (MMPOSGiContainer) ModuleContainerFactory.getInstance().getModuleContainer();
    		//moduleContainer.undeployModule(getId());
			Module module = new Module();
			module.setId(getId());
			module = moduleContainer.getModule(module);

			File file = new File(module.getLocation());
			
			fileName = file.getName();
			fileSize = String.valueOf(file.length());
			inputStream = new FileInputStream(file);
			
		} catch(MMPException me) {
			addActionError(getText("error.module.download", new String[] {me.getLocalizedMessage()}));
			if(getType() != null && getType().equals("widget"))
				return widget();
			if(getType() != null && getType().equals("library"))
				return library();
			return execute();
		} catch (Exception e) {
			if(getType() != null && getType().equals("widget"))
				return widget();
			if(getType() != null && getType().equals("library"))
				return library();
			return execute();
		}
		
		return "download";
		
	}
	
	/**
	 * END ACTIONS
	 */
	
	/**
	 * Inner Methods
	 */
	
	/**
	 * Build the modules map
	 * @throws MMPException
	 */
	@SuppressWarnings("unchecked")
	private void buildModulesMap(String sortOrder, String sortType) throws MMPException {
		boolean sortByName = false;
		boolean sortByType = false;
		if(sortOrder != null) {
			if(sortType.equals("name"))
				sortByName = true;
			else if(sortType.equals("type"))
				sortByType = true;
		}
		
		Module module = new Module();
		Module[] modulesList = (Module[]) DaoManagerFactory.getInstance().getDaoManager().getDao("module").find(module);
		this.modulesMap = new LinkedHashMap<Module, String>();
		
		if(sortByName) {
			setSortOrder(sortOrder);
			setSortParam(sortType);
			Arrays.sort(modulesList, new Module());
		}
		
		if(sortByType) {
			setSortOrder(sortOrder);
			setSortParam(sortType);
			Arrays.sort(modulesList, new TypeComparator());
		}
		
		int i = 0;
		if((sortByName || sortByType) && sortOrder.equals("descend")) {
			i = modulesList.length - 1;
		}
		
		while(i < modulesList.length && i >= 0) {
			Module currentModule = modulesList[i];
			if(currentModule.getCategory() != null
					&& currentModule.getCategory().equals(com.orange.mmp.core.Constants.MODULE_CATEGORY_WIDGET)) {
				String branchId = "";
				String[] moduleId = currentModule.getId().split(com.orange.mmp.widget.Constants.BRANCH_SUFFIX_PATTERN);
				if(moduleId.length > 1)
					branchId = moduleId[1];
				this.modulesMap.put(currentModule, branchId);
			} else if (currentModule.getCategory() == null || currentModule.getCategory().equals("")) {
				currentModule.setCategory(com.orange.mmp.core.Constants.MODULE_CATEGORY_UNKNOWN);
				this.modulesMap.put(currentModule, new String());
			} else
				this.modulesMap.put(currentModule, new String());
			
			if((sortByName || sortByType) && sortOrder.equals("descend"))
				i--;
			else
				i++;
		}
	}
	
	/**
	 * Build the web services map
	 * @throws MMPException
	 */
	@SuppressWarnings("unchecked")
	private void buildWebServicesMap() throws MMPException {
		List<Api> apisList = ApiContainerFactory.getInstance().getApiContainer().listApis();
		this.webServicesMap = new HashMap<Api, String[]>();
		for(Api api : apisList) {
			Class wsClass = api.getDefinitionClass();
			Method wsMethods[] = wsClass.getMethods();
			String methods[] = new String[wsMethods.length];
			for(int index=0; index < methods.length; index++){
				methods[index] = wsMethods[index].getReturnType().getSimpleName();
				methods[index] += " "+wsMethods[index].getName();
				methods[index] +="(";
				for(int cIndex=0; cIndex < wsMethods[index].getParameterTypes().length; cIndex++){
					methods[index] += wsMethods[index].getParameterTypes()[cIndex].getSimpleName();
					if(cIndex < wsMethods[index].getParameterTypes().length-1) methods[index] += ", ";
				}
				methods[index] +=")";
			}
			this.webServicesMap.put(api, methods);
		}
	}
	
	/**
	 * Build the widgets map
	 * @throws MMPException
	 */
	@SuppressWarnings("unchecked")
	private void buildWidgetsMap(String sort) throws MMPException {
		boolean sortByName = false;
		boolean sortByLetter = false;
		if(sort != null) {
			if(sort.equals("ascend") || sort.equals("descend"))
				sortByName = true;
			else if(sort.length() == 1)
				sortByLetter = true;
		}
		
		this.widgetsMap = new LinkedHashMap<Widget, HashMap<Branch, String[]>>();
		Widget[] widgetsList = this.widgetManager.getWidgetsList();
		if(sortByName) {
			setSort(sort);
			Arrays.sort(widgetsList, new Module());
		}
		
		int i = 0;
		if(sortByName && sort.equals("descend")) {
			i = widgetsList.length - 1;
		}
		
		while(i < widgetsList.length && i >= 0) {
			Widget widget = widgetsList[i];
			String[] tmpId = widget.getId().split(com.orange.mmp.widget.Constants.BRANCH_SUFFIX_PATTERN);
			String widgetId = widget.getId();
			if(tmpId.length > 0) {
				widgetId = tmpId[0];
				widget.setId(widgetId);
			}
			if((!sortByLetter) 
					|| (sortByLetter && widget.getName().toLowerCase().startsWith(sort))) {
				Branch branch = new Branch();
				branch.setId(widget.getBranchId());
				Branch[] branchesList = (Branch[]) DaoManagerFactory.getInstance().getDaoManager().getDao("branch").find(branch);
				if(branchesList != null && branchesList.length > 0) {
					branch = branchesList[0];
				}
			    
			    // Add Mobiles to Branch
				Mobile mobile = new Mobile();
				mobile.setBranchId(widget.getBranchId());
			    Mobile[] mobiles = (Mobile[]) DaoManagerFactory.getInstance().getDaoManager().getDao("mobile").find(mobile);
			    List<Mobile> mobileList = Arrays.asList(mobiles);
			    branch.setMobiles(mobileList);
			    
			    HashMap<Branch, String[]> branchMap;
			    Widget widgetFinder = isAlreadyPresent(widgetsMap, widget);
			    if(widgetFinder != null) {
			    	branchMap = widgetsMap.get(widgetFinder);
			    	widgetsMap.remove(widgetFinder);
			    } else {
			    	branchMap = new HashMap<Branch, String[]>();
			    }
			    
			    String[] resourcesList = new String[0];
			    List<URL> widgetResources = this.widgetManager.findWidgetResources("/", "*", widgetId, widget.getBranchId(), true);
			    if(widgetResources != null) {
				    resourcesList = new String[widgetResources.size()];
				
				    for(int j = 0; j < resourcesList.length; j++) {
				    	URL resourcePath = widgetResources.get(j);
				    	URL resourceURL = this.widgetManager.getWidgetResourceUrl(ServiceManager.getInstance().getDefaultService(), resourcePath.getPath(), widgetId, widget.getBranchId());

					   	resourcesList[j] = resourceURL.toString();
				    }
				}
			    branchMap.put(branch, resourcesList);
			    widgetsMap.put(widget, branchMap);
			}

			if(sortByName && sort.equals("descend"))
				i--;
			else
				i++;
		}
		
	}
	
	
	/**
	 * Build the libraries map
	 * @throws MMPException
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	private void buildLibrariesMap() throws MMPException, IOException {
		Module module = new Module();
		module.setCategory(com.orange.mmp.core.Constants.MODULE_CATEGORY_LIBRARY);
		Module[] modulesList = (Module[]) DaoManagerFactory.getInstance().getDaoManager().getDao("module").find(module);
		
		this.librariesMap = new HashMap<Module, String[]>();
		for(Module lib : modulesList) {
			// Get Export-Package from MANIFEST
			JarFile jarFile = new JarFile(new File(lib.getLocation()));
			Manifest manifest = jarFile.getManifest();
    		if(manifest == null) {
    			throw new MMPException(" invalid module archive, MANIFEST file not found");
    		}
    		// Get Bundle category
    		Attributes attributes = manifest.getMainAttributes();
    		String exportPackagesStr = attributes.getValue(BUNDLE_EXPORT_PACKAGES);
    		String[] libPackages = new String[0];
    		if(exportPackagesStr != null && !exportPackagesStr.equals("")) {
    			String[] exportPackages = exportPackagesStr.split(",");
    			libPackages = new String[exportPackages.length];
    			for(int i = 0; i < libPackages.length; i++) {
    				String exportPackage = exportPackages[i];
    				libPackages[i] = exportPackage.split(";")[0];
    			}
    		}
    		this.librariesMap.put(lib, libPackages);
		}
	}
	
    /**
     * Check if a Widget with the same ID is already present in widgetMap
     * @param widgetMap	The Widget Map to parse
     * @param widget	The current Widget
     * @return		The Widget if it is already present in the Map, null otherwise
     */
    private Widget isAlreadyPresent(HashMap<Widget, HashMap<Branch, String[]>> widgetMap, Widget widget) {
    	Set<Widget> widgets = widgetMap.keySet();
    	for (Iterator<Widget> iterator = widgets.iterator(); iterator.hasNext();) {
    		Widget tmpWidget = iterator.next();
    		if(widget.getId().equals(tmpWidget.getId())) {
    			return tmpWidget;
    		}
    	}
    	return null;
    }
	
    /**
     * Comparator which allow to sort the modules by category
     *
     */
    public class TypeComparator implements Comparator<Module> {

    	public int compare(Module module1, Module module2) {
    		if(module1.getCategory().toLowerCase().compareTo(module2.getCategory().toLowerCase()) == 0) {
    			return module2.getName().toLowerCase().compareTo(module1.getName().toLowerCase());
    		}
    		
    		return module2.getCategory().toLowerCase().compareTo(module1.getCategory().toLowerCase());
    	}

    }
    
	/**
	 * GETTERS/SETTERS
	 */
	
	/**
	 * @return the uploadedFile
	 */
	public File getUploadedFile() {
		return uploadedFile;
	}

	/**
	 * @param uploadedFile the uploadedFile to set
	 */
	public void setUploadedFile(File uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

	/**
	 * @return the branch
	 */
	public Branch[] getBranch() {
		return branch;
	}

	/**
	 * @param branch the branch to set
	 */
	public void setBranch(Branch[] branch) {
		this.branch = branch;
	}

	/**
	 * @return the branchId
	 */
	public String getBranchId() {
		return branchId;
	}

	/**
	 * @param branchId the branchId to set
	 */
	public void setBranchId(String branchId) {
		this.branchId = branchId;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the modulesMap
	 */
	public HashMap<Module, String> getModulesMap() {
		return modulesMap;
	}

	/**
	 * @param modulesMap the modulesMap to set
	 */
	public void setModulesMap(HashMap<Module, String> modulesMap) {
		this.modulesMap = modulesMap;
	}

	/**
	 * @return the widgetsMap
	 */
	public HashMap<Widget, HashMap<Branch, String[]>> getWidgetsMap() {
		return widgetsMap;
	}

	/**
	 * @param widgetsMap the widgetsMap to set
	 */
	public void setWidgetsMap(
			HashMap<Widget, HashMap<Branch, String[]>> widgetsMap) {
		this.widgetsMap = widgetsMap;
	}

	/**
	 * @return the webServicesMap
	 */
	public HashMap<Api, String[]> getWebServicesMap() {
		return webServicesMap;
	}

	/**
	 * @param webServicesMap the webServicesMap to set
	 */
	public void setWebServicesMap(HashMap<Api, String[]> webServicesMap) {
		this.webServicesMap = webServicesMap;
	}

	/**
	 * @return the librariesMap
	 */
	public HashMap<Module, String[]> getLibrariesMap() {
		return librariesMap;
	}

	/**
	 * @param librariesMap the librariesMap to set
	 */
	public void setLibrariesMap(HashMap<Module, String[]> librariesMap) {
		this.librariesMap = librariesMap;
	}

	/**
	 * @return the sort
	 */
	public String getSort() {
		return sort;
	}

	/**
	 * @param sort the sort to set
	 */
	public void setSort(String sort) {
		this.sort = sort;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the sortOrder
	 */
	public String getSortOrder() {
		return sortOrder;
	}

	/**
	 * @return the sortParam
	 */
	public String getSortParam() {
		return sortParam;
	}

	/**
	 * @param sortOrder the sortOrder to set
	 */
	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	/**
	 * @param sortParam the sortParam to set
	 */
	public void setSortParam(String sortParam) {
		this.sortParam = sortParam;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileSize() {
		return fileSize;
	}

	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

}
