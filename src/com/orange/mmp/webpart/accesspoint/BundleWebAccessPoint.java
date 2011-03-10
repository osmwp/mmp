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
package com.orange.mmp.webpart.accesspoint;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpSession;

import com.orange.mmp.context.RequestContext;
import com.orange.mmp.context.ResponseContext;
import com.orange.mmp.context.UserContext;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.data.Module;
import com.orange.mmp.i18n.InternationalizationManager;
import com.orange.mmp.i18n.InternationalizationManagerFactory;
import com.orange.mmp.i18n.MMPI18NException;
import com.orange.mmp.module.MMPModuleException;
import com.orange.mmp.module.ModuleContainerFactory;
import com.orange.mmp.webpart.WebPartContainerFactory;
import com.orange.mmp.webpart.template.ModuleTemplateLoader;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;


/**
 * Bundle access point for web part
 * 
 * @author rmxc7111
 *
 */
public class BundleWebAccessPoint {
	
	/**
	 * Web access point attribute name (in request context) 
	 */
	public static final String BUNDLE_WEB_ACCESS_POINT_RESOURCE_PATH = "bundleWebAccessPoint_ResourcePath";
	
	/**
	 * Web files folder (base to load a web resource in bundle)
	 */
	private String webPartFolder = null;

	/**
	 * The default resource
	 */
	private String defaultResource = null;

	/**
	 * Module of this web part
	 */
	private Module module;

	/**
	 * Message source
	 */
	private String messageSource;

	/**
	 * Process a request on web part of bundle. 
	 * @param requestContext Request context
	 * @param userContext User context
	 * @param responseContext Response
	 * @throws MMPException Error during process
	 */
	public void processRequest(final RequestContext requestContext,
			final UserContext userContext, final ResponseContext responseContext)
			throws MMPException {
		
		//Get resource path
		String resourceName = getResourcePath(requestContext);
		
		//If not resource, use default resource
		if (resourceName == null || resourceName.trim().length() <= 0) {
			resourceName = defaultResource;
		}
		
		//By default, forward request to the file in URL
		forward(resourceName, requestContext, userContext, responseContext);
	}
	
	/**
	 * Forward to another file/url
	 * @param requestContext Request context
	 * @param userContext User context
	 * @param responseContext Response
	 * @throws MMPException Error during process
	 */
	protected void forward(final String fileName, final RequestContext requestContext,
			final UserContext userContext, final ResponseContext responseContext)
			throws MMPException {
		
		try {
			
			//Compute content type
			final String contentType = getContentType(fileName);
			responseContext.setContentType(contentType);
			
			//Access to resource/file and write it to response
			if (isContentSupportTemplate(fileName)) {		
				Map<?, ?> map = getTemplateValuesMapping(requestContext, userContext);
				writeTemplate(fileName, map, responseContext);
			} else {
				writeFile(fileName, responseContext);
			}
			
		} catch (IOException ioexception) {
			
			//Error during read resource on bundle and/or write resource on response 
			throw new MMPException ("Error during send a web resource", ioexception);
			
		}
	}
	
	/**
	 * Complete a template document and write it to HTTP response.
	 * @param resourcePath Path of the template document in bundle.
	 * @param templateProperties Properties to complete the template document
	 * @param response HTTP response (to write completed template)
	 * @throws IOException Error during process
	 */
	protected void writeTemplate(final String resourcePath,final Map<?,?> templateProperties, final ResponseContext responseContext) throws IOException {
		//final InputStream in = this.getClass().getResourceAsStream(resourcePath);

		//Create template loader to load data from the bundle
		//ClassTemplateLoader templateLoader = new ClassTemplateLoader(getClass(), "");
		ModuleTemplateLoader templateLoader = new ModuleTemplateLoader();
		templateLoader.setModule(module);
		
		//Initialize configuration for the template file
		Configuration conf = new Configuration();
		conf.setTemplateLoader(templateLoader);
		
		//Get the template document
		final String finalPath = (webPartFolder + "/" + resourcePath.trim()).replace("///","/").replace("//","/");
		Template template = conf.getTemplate(finalPath);
		
		//Get output writer for HTTP response
	    PrintWriter out = responseContext.getWriter();
	    try {
		    //Apply properties to the template document 
		    try {
				template.process(templateProperties, out);
			} catch (TemplateException e) {
				//Error - Log via AspectJ
			}
	    } finally {
			//Close output
			if (out != null) out.close();
		}
	}

	/**
	 * Read file from bundle (using resource path) and write the file to HTTP response.
	 * @param resourcePath Path of resource file
	 * @param response HTTP response
	 * @throws IOException Error during process
	 */
	protected void writeFile(final String resourcePath, final ResponseContext responseContext) throws IOException, MMPException {
		//Open input stream to read the file
		//final InputStream in = this.getClass().getResourceAsStream(resourcePath);
		final URL url = getResourceUrl(resourcePath);
		if (url == null) {
			throw new MMPException("Resource not found.");
		}
		
		final InputStream in = url.openStream();
		try {
			if (in == null) {
				//File not found...
				responseContext.sendError(
						ResponseContext.SC_NOT_FOUND,
						"Resource " + resourcePath + " not found for this service." 
				);
			} else {
				//Write file on HTTP response
			    PrintWriter out = responseContext.getWriter();
			    try {
					int c;
					while((c=in.read()) != -1){
						out.write(c);
					}
			    }  finally {
			    	//Close output stream
			    	if (out != null) out.close();
			    }
			}
		} finally {
			//Close input stream
			if (in != null) in.close();
		}
	}

	/**
	 * Verify if the result of request support templates.
	 * Verification is based on URL (extension).
	 * @param requestContext Request context (to extract extension via the request path)
	 * @return Support of the template (or not).
	 */
	protected boolean isContentSupportTemplate(final String resourceUrl) {
		final String extension = getUrlExtension(resourceUrl);
		final List<String> extensionsList;
		try {
			extensionsList = WebPartContainerFactory.getInstance().getExtensionsWithTemplateSupport();
			return extension != null
			&& extension.length() > 0
			&& extensionsList.contains(extension);
		} catch (MMPException e) {
			//Error - Log via AspectJ
			return false;
		}
	}
	
	/**
	 * Get the content type (MIME type) for a URL.
	 * Base on URL extension (via the request URL path).
	 * @param requestContext Request (to extract MIME type via the request path)
	 * @return The content type (MIMER type)
	 */
	protected String getContentType(final RequestContext requestContext) throws MMPException {
		final String contentType;
		contentType = getContentType(requestContext.getPathInfo());
		return contentType;
	}
	/**
	 * Get the content type (MIME type) for a file name.
	 * @param filePath File path
	 * @return the content type
	 */
	protected String getContentType(final String filename) throws MMPException {
		final String contentType;
		
		//Extract URL extension
		final String extension = getUrlExtension(filename);
		if (extension != null && extension.length() > 0) {
			//Get content type for this extension
			final Map<String, String> contentTypesMap;
			contentTypesMap = WebPartContainerFactory.getInstance().getContentTypeByExtension();
			contentType = contentTypesMap.get(extension);
		} else {
			contentType = null;
		}
		
		return contentType;
	}
	
	/**
	 * Get the content type (MIME type) for a URL.
	 * Base on URL extension (via the request URL path).
	 * @param request URL
	 * @return
	 */
	protected String getUrlExtension(final RequestContext requestContext) {
		final String extension;
		
		//Get path info of request
		final String pathInfo = requestContext.getPathInfo();
		//Compute the extension
		extension = getUrlExtension(pathInfo);
		
		return extension;
	}
	
	/**
	 * Get the extension for a URL path information.
	 * @param urlPathInfo URL path information
	 * @return the URL extension
	 */
	protected String getUrlExtension(final String urlPathInfo) {
		final String extension;
		
		//Try to catch request URL
		URI uri;
		try {
			uri = new URI(urlPathInfo);
		} catch (URISyntaxException e) {
			uri = null;
		}
		
		//Verify URI
		if (uri == null) {
			//URI is null... we cannot found content type.
			extension = null;
		} else {
			//Verify URI path
			final String path = uri.getPath().trim();
			if (path == null || path.length() <= 0) {
				//Path is null or empty... we cannot found content type.
				extension = null;
			} else {
				//Extract URL extension
				final String [] splitPath = path.split("\\."); //Regex require '\.' ('.' is reserved to replace all char)
				extension = (splitPath.length >0) ? (splitPath[splitPath.length - 1]).toLowerCase() : ("");
			}
		}
		
		return extension;
	}
	
	/**
	 * Get the resource path in web part of bundle.
	 * Base on HTTP request.
	 * 
	 * Example : Read a web resource file in bundle
	 * //Compute path (web resource path + required resource in request). 
	 * final String resourcePath = getResourcePath(hpptRequest);
	 * //Get URL to read resource in bundle (require the module) 
	 * final URL resourceURL = getResourceUrl(resourcePath);
	 * //Open input stream to read the resource.
	 * final InputStream in = resourceURL.openStream();
	 * 
	 * @param RequestContext Request context (to extract resource URL).
	 * @return The resource path in bundle.
	 */
	protected String getResourcePath(final RequestContext requestContext) {
		return (String) requestContext.getAttribute(BUNDLE_WEB_ACCESS_POINT_RESOURCE_PATH);
	}
	
	/**
	 * Define the base folder of web resources.
	 * @param webPartFolder the base folder of web resources.
	 */
	public void setWebPartFolder(final String webPartFolder) {
		this.webPartFolder = webPartFolder;
	}
	/**
	 * Get the base folder of web resources.
	 * @return The web resources folder
	 */
	public String getWebPartFolder() {
		return webPartFolder;
	}

	/**
	 * Define the module of the web part
	 * @param module
	 */
	public void setModule(Module module) {
		this.module = module;
	}
	/**
	 * Get the module of the web part
	 * @return Module
	 */
	public Module getModule() {
		return module;
	}
	
	/**
	 * Get resource URL in bundle.
	 * 
	 * Example : Read a web resource file in bundle
	 * //Compute path (web resource path + required resource in request). 
	 * final String resourcePath = getResourcePath(hpptRequest);
	 * //Get URL to read resource in bundle (require the module) 
	 * final URL resourceURL = getResourceUrl(resourcePath);
	 * //Open input stream to read the resource.
	 * final InputStream in = resourceURL.openStream();
	 * 
	 * @param resourcePath Resource path in bundle
	 * @return The URL of the resource.
	 */
	public URL getResourceUrl(final String resourcePath) {
		URL url = null;
		try {
			final String finalPath = (webPartFolder + "/" + resourcePath.trim()).replace("///","/").replace("//","/");
			url = ModuleContainerFactory.getInstance().getModuleContainer().getModuleResource(module, finalPath);
		} catch (MMPModuleException e) {
			//Error - Log via AspectJ
		}
		return url;
	}
	
	/**
	 * Create a map with value for generate template documents.
	 * @param requestContext Request context
	 * @param userContext User context
	 * @return Map of values
	 */
	protected Map<?,?> getTemplateValuesMapping(final RequestContext requestContext, final UserContext userContext) {
		Map<Object,Object> map = new HashMap<Object, Object>();
		//Add internationalized labels
		if (messageSource != null && messageSource.length() > 0) {
			String lang = requestContext.getParameter("lang");
			// Try to retrieve language in session
			if(lang == null || lang.length() == 0) {
				if (requestContext.getLocale() != null) {
					lang = requestContext.getLocale().getLanguage();
				}
			}
			// Check language
			if(lang == null || lang.length() == 0) {
				lang = "en";
			}
			else if(lang.indexOf("_") > 0) {
				lang = lang.split("_")[0];
			}
			final Locale locale = new Locale(lang);
			try {
				final InternationalizationManager i18nManager;
				i18nManager = InternationalizationManagerFactory.getInstance().getInternationalizationManager();
				map.putAll(i18nManager.getLocalizationMap(messageSource, locale));
			} catch (MMPI18NException e) {
				//AspectJ log the exception
			}
		}
		if (requestContext != null) {
			Enumeration<?> enumeration;
			//Add request context attributes
			enumeration = requestContext.getAttributeNames();
			for (;enumeration.hasMoreElements();) {
				String key = (String)enumeration.nextElement();
				map.put(key, requestContext.getAttribute(key));
			}
			//Add request context parameters
			enumeration = requestContext.getParameterNames();
			for (;enumeration.hasMoreElements();) {
				String key = (String)enumeration.nextElement();
				map.put(key, requestContext.getParameter(key));
			}
			//Add session attributes
			final HttpSession session = requestContext.getSession();
			if (session != null) {
				enumeration = session.getAttributeNames();
				for (;enumeration.hasMoreElements();) {
					String key = (String)enumeration.nextElement();
					map.put(key, session.getAttribute(key));
				}
			}
		}

		//Return the complete map of data
		return map;
	}

	/**
	 * Get the name of message source (for internationalization).
	 * @return Message source name
	 */
	public String getMessageSourceName() {
		return messageSource;
	}

	/**
	 * Define the name of message source (for internationalization).
	 * @param messageSourceName The new message source name
	 */
	public void setMessageSourceName(final String messageSourceName) {
		this.messageSource = messageSourceName;
	}

	/**
	 * Get the default resource (path and name).
	 * @return Default resource.
	 */
	public String getDefaultResource() {
		return defaultResource;
	}

	/**
	 * Modify the default resource (path and name).
	 * @param defaultResource The new default resource.
	 */
	public void setDefaultResource(String defaultResource) {
		this.defaultResource = defaultResource;
	}
}

