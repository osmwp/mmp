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
package com.orange.mmp.midlet;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.web.multipart.MultipartFile;

import com.orange.mmp.api.ApiContainerFactory;
import com.orange.mmp.cache.Cache;
import com.orange.mmp.cache.CacheManagerFactory;
import com.orange.mmp.cache.MMPCacheException;
import com.orange.mmp.core.ApplicationListener;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.MMPRuntimeException;
import com.orange.mmp.core.data.Api;
import com.orange.mmp.core.data.DeliveryTicket;
import com.orange.mmp.core.data.Element;
import com.orange.mmp.core.data.JadAttributeAction;
import com.orange.mmp.core.data.Midlet;
import com.orange.mmp.core.data.Mobile;
import com.orange.mmp.core.data.Service;
import com.orange.mmp.core.data.Widget;
import com.orange.mmp.core.data.JadAttributeAction.ApplyCase;
import com.orange.mmp.dao.DaoManagerFactory;
import com.orange.mmp.delivery.DeliveryManager;
import com.orange.mmp.mvc.ota.Controller;
import com.orange.mmp.service.ServiceManager;
import com.orange.mmp.widget.WidgetManager;
import com.sun.midp.jadtool.AppDescriptor;

/**
 * Midlet Manager
 * 
 * @author tml
 *
 */
public class MidletManager implements ApplicationListener{

	
	/**
	 * Singleton for access outside application context
	 */
	private static MidletManager midletManagerSingleton;
	
	/**
     * Inner cache of midlets in Cache
     */
    private Cache midletCache;
    
    /**
     * The name of the midlets cache
     */
    private String midletCacheName;
	
	/**
     * Folder containing the midlets signing keystore
     */
    private String keystoreFile;

    /**
     * Alias used in keystore to sign midlets
     */
    private String keystoreAlias;

    /**
     * Key used by keystore to sign midlets
     */
    private String keystoreKey;
    
    /**
     * Fixed last modification date of JAR entries
     */
    private static final long MIDLET_LAST_MODIFICATION_DATE = 1220863782000L;
    
    /**
     * Regex Pattern compiled to replace launcher line in JAD file
     */
    private static Pattern launcherPattern = Pattern.compile("([^,]*), ([^,]*), ([^,]*)");
    
	/**
	 * Name of the bundle owning special CSS sheets
	 */
	public String cssSheetsBundleName;
    
    /**
	 * Singleton access 
	 * 
	 * @return The MidletManager singleton
	 */
	public static MidletManager getInstance(){
		return MidletManager.midletManagerSingleton;
	}
	
    /**
     * Initialize midlet manager
     */
	public void initialize() throws MMPException {
    	midletManagerSingleton = this;
    	
    	try{
			this.midletCache = CacheManagerFactory.getInstance().getCacheManager().getCache(this.midletCacheName);
		} catch (MMPCacheException mce) {
			throw new MMPRuntimeException("Failed to start MidletManager, CacheManager is unavailable",mce);
		}
	}

    /**
     * Shutdown midlet manager
     */
	public void shutdown() throws MMPException {
		midletManagerSingleton = null;
		
		if(this.midletCache != null) this.midletCache.clear();
	}
	
    /**
     * Builds a ZIP archive with the appropriate name
     * @param mobile
     * @param signMidlet
     * @param output
     * @return	The ZIP filename
     * @throws IOException
     * @throws MMPException 
     */
    public String computeZip(Mobile mobile, Boolean signMidlet, OutputStream output) throws IOException, MMPException {
    	//Compute Zip
    	ZipOutputStream zipOS = new ZipOutputStream(output);
    	try {
	    	//Get default service
			Service service = ServiceManager.getInstance().getDefaultService();
		
			//Create fake ticket
			DeliveryTicket ticket = new DeliveryTicket();
			ticket.setServiceId(service.getId());
		
			//Get navigation widget (main scene)
			Widget appWidget = WidgetManager.getInstance().getWidget(ticket.getServiceId(),mobile.getBranchId());
			if(appWidget == null) appWidget = WidgetManager.getInstance().getWidget(ticket.getServiceId());
			if(appWidget == null) throw new IOException("application "+ticket.getServiceId()+" not found");

			ByteArrayOutputStream tmpOS = null;
		
			//Add JAD
			zipOS.putNextEntry(new ZipEntry(appWidget.getName()+ com.orange.mmp.midlet.Constants.JAD_FILE_EXTENSION));
			tmpOS = getJad(ticket.getId(), mobile, signMidlet, service);
			tmpOS.writeTo(zipOS);
			zipOS.closeEntry();

			//Add JAR
			zipOS.putNextEntry(new ZipEntry(appWidget.getName()+ com.orange.mmp.midlet.Constants.JAR_FILE_EXTENSION));
			tmpOS = getJar(service.getId(), mobile, signMidlet);
			tmpOS.writeTo(zipOS);
			zipOS.closeEntry();

			zipOS.flush();

			String[] tmpVersion = appWidget.getVersion().toString().split("\\.");
			if(tmpVersion.length > 2) {
			    return appWidget.getName() + "_V" + tmpVersion[0] + "." + tmpVersion[1] + appWidget.getBranchId() + "." + tmpVersion[2];
			}
			return appWidget.getName() + "_V" + appWidget.getVersion() + "_" + appWidget.getBranchId();
		} finally{
			try { zipOS.close(); } catch(IOException ioe) {}
		}
	}
	
	/**
     * Create a JAD for PFM download
     * @param ticketId The delivery ticket ID
     * @param mobile The mobile to use
     * @param signMidlet indicate if the midlet must be signed
     * @param service Indicate the hostname to use in JAD (null for default)
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
	public ByteArrayOutputStream getJad(String ticketId, Mobile mobile, boolean signMidlet, Service service) throws MMPException {
        ByteArrayOutputStream output = null;
        try {
            DeliveryTicket ticket = new DeliveryTicket();
            ticket.setId(ticketId);
            ticket = DeliveryManager.getInstance().getDeliveryTicket(ticket);
            if(ticket == null) throw new MMPException("ticket "+ticketId+" has expired");
            if(ticket.getServiceId() == null) {
            	ticket.setServiceId(service.getId());
            	if(ticket.getServiceSpecific() != null) {
            		Map<String, String> ticketMap = ticket.getServiceSpecific();
            		Map<String, String> serviceMap = service.getJadEntries();
            		Set<String> serviceKeys = serviceMap.keySet();
            		for (String key : serviceKeys) {
						ticketMap.put(key, serviceMap.get(key));
					}
            		ticket.setServiceSpecific(ticketMap);
            	} else
            		ticket.setServiceSpecific(service.getJadEntries());
            }
            ticket.setUaKey(mobile.getKey());
            ticket.setType(mobile.getMidletType());
            if(ticket.getCallback() != null && !ticket.getCallback().equals("")) {
            	Api api = new Api();
            	String[] apiArray = ticket.getCallback().split("\\.");
            	if(apiArray.length == 2){
           			api.setName(apiArray[0]);
           			String method = apiArray[1];           	
           			ApiContainerFactory.getInstance().getApiContainer().invokeApi(api, method, new Object[]{ticket});
            	}
            }
            
            Widget appWidget = WidgetManager.getInstance().getWidget(ticket.getServiceId(), mobile.getBranchId());
            if(appWidget == null) throw new MMPException("application "+ticket.getServiceId()+" not found");
            String appName = appWidget.getName();

            Midlet midlet = new Midlet();
            midlet.setType(mobile.getMidletType());
            
            Midlet midlets[] = (Midlet[])DaoManagerFactory.getInstance().getDaoManager().getDao("midlet").find(midlet);
            if(midlets.length == 0) throw new MMPException("Midlet type not found : "+mobile.getMidletType());
            else midlet = midlets[0];

            JadFile jadFile = new JadFile();
            jadFile.load(new File(new URI(midlet.getJadLocation())));

            StringBuilder midletURL = new StringBuilder();
            if(service == null) service = ServiceManager.getInstance().getDefaultService();

            midletURL.append(new URL("http",service.getHostname(),"").toString()).append(Controller.getUrlMapping()).append("/")
            	.append(mobile.getKey()).append("/")
                .append(appName).append(com.orange.mmp.midlet.Constants.JAR_FILE_EXTENSION);

            //Set default if not found
            if(ticket.getServiceId() == null) ticket.setServiceId(ServiceManager.getInstance().getDefaultService().getId());

            //Build JAD
            jadFile.setValue(Constants.JAD_PARAMETER_JAR_URL, midletURL.toString());
            jadFile.setValue(Constants.JAD_PARAMETER_APPNAME, appName);
            // Add UA key
            String uakey = mobile.getKey();
            jadFile.setValue(Constants.JAD_PARAMETER_UAKEY, uakey);

            Map<String,String> servicesSpecificMap = ticket.getServiceSpecific();
            if(servicesSpecificMap != null){
            	for(String key : servicesSpecificMap.keySet()){
            		jadFile.setValue(key,servicesSpecificMap.get(key));
            	}
            }

            String launcherLine = jadFile.getValue(Constants.JAD_PARAMETER_LAUNCHER);
            Matcher launcherLineMatcher = launcherPattern.matcher(launcherLine);
            if(launcherLineMatcher.matches()){
                jadFile.setValue(Constants.JAD_PARAMETER_LAUNCHER, appName.concat(", ").concat(launcherLineMatcher.group(2)).concat(", ").concat(launcherLineMatcher.group(3)));
            }
            else jadFile.setValue(Constants.JAD_PARAMETER_LAUNCHER, appName.concat(", ").concat(Constants.JAD_LAUNCHER_ICON).concat(", ").concat(Constants.JAD_LAUNCHER_MAINCLASS));

			//Add/Modify/Delete JAD parameters according to mobile rules
			JadAttributeAction[] jadActions = mobile.getJadAttributeActions();
			for(JadAttributeAction jadAction : jadActions) {
				// Test inJad
				if(jadAction.getInJad().equals(ApplyCase.ALWAYS)
						|| (signMidlet && jadAction.getInJad().equals(ApplyCase.SIGNED))
						|| (!signMidlet && jadAction.getInJad().equals(ApplyCase.UNSIGNED))) {
					if(jadAction.isAddAction() || jadAction.isModifyAction())
						jadFile.setValue(
								jadAction.getAttribute(), 
								jadAction.getValue(), 
								! jadAction.isStrict());
					else if(jadAction.isDeleteAction())
						jadFile.deleteValue(jadAction.getAttribute());	
				}
				
			}
            
            ByteArrayOutputStream jarContent = this.getJar(ticket.getServiceId(), mobile, signMidlet);
            jadFile.setValue(Constants.JAD_PARAMETER_JAR_SIZE, String.valueOf(jarContent.size()));

            // SIGN Midlet
            if(signMidlet) {
            	File jadTmpFile = new File(System.getProperty("java.io.tmpdir"),ticket.getId().concat(Constants.JAD_FILE_EXTENSION));

                 // Save tmp Jad file
                FileOutputStream fos = new FileOutputStream(jadTmpFile);
                jadFile.save(fos);

                // Apply commands
                BufferedInputStream jadIn = null;
                BufferedInputStream keystoreIn = null;
                ByteArrayInputStream jarIn = new ByteArrayInputStream(jarContent.toByteArray());
                output = new ByteArrayOutputStream();
                try{
                    jadIn = new BufferedInputStream(new FileInputStream(jadTmpFile));
                    keystoreIn = new BufferedInputStream(new FileInputStream(this.keystoreFile));
                    AppDescriptor midletSigner = new AppDescriptor();
                    midletSigner.load(jadIn,com.orange.mmp.core.Constants.DEFAULT_ENCODING);
                    midletSigner.loadKeyStore(keystoreIn, this.keystoreKey.toCharArray());
                    midletSigner.addCert(this.keystoreAlias, 1, 0);
                    midletSigner.addJarSignature(this.keystoreAlias, this.keystoreKey.toCharArray(),jarIn);
                    midletSigner.store(output, com.orange.mmp.core.Constants.DEFAULT_ENCODING);
                }catch(Exception e){
                    throw new MMPException(e);
                }
                finally{
                    if(jadIn != null) jadIn.close();
                    if(jarIn != null) jarIn.close();
                    if(keystoreIn != null) keystoreIn.close();
                    if(jadTmpFile != null) jadTmpFile.delete();
                }
            }
            //No SIGNING
            else{
            	output = new ByteArrayOutputStream();
            	jadFile.save(output);
            }

            return output;
        } catch(IOException ioe) {
            throw new MMPException(ioe);
        } catch(URISyntaxException use) {
            throw new MMPException(use);
        } finally {
        	try{
        		if(output != null)output.close();
        	}catch(IOException ioe){
        		//NOP
        	}
        }
    }
    
    /**
     * Create a JAD for PFM download
     * @param ticketId The delivery ticket ID
     * @param mobile The mobile to use
     * @param signMidlet indicate if the midlet must be signed
     * @param service Indicate the hostname to use in JAD (null for default)
     * @throws IOException
     */
//    @SuppressWarnings("unchecked")
//	public ByteArrayOutputStream getJad(DeliveryTicket ticket, Widget appWidget, Mobile mobile, Boolean signMidlet, Service service) throws MMPException {
//		ByteArrayOutputStream output = null;
//
//		try {
//			if(appWidget == null) {
//			    appWidget = WidgetManager.getInstance().getWidget(ticket.getService(), mobile.getBranchId());
//			    if(appWidget == null) throw new MMPException("application "+ticket.getService()+" not found");
//			}
//			String appName = appWidget.getName();
//
//			Midlet midlet = new Midlet();
//			midlet.setType(mobile.getMidletType());
//            
//			Midlet midlets[] = (Midlet[])DaoManagerFactory.getInstance().getDaoManager().getDao("midlet").find(midlet);
//            if(midlets.length == 0) throw new MMPException("PFM type not found : "+mobile.getMidletType());
//            else midlet = midlets[0];
//
//			JadFile jadFile = new JadFile();
//			jadFile.load(new File(new URI(midlet.getJadLocation())));
//
//			//Remove optional permissions if not signing
//			if(signMidlet != null && ! signMidlet)
//				jadFile.deleteValue(Constants.JAD_PARAMETER_OPT_PERMISSIONS);
//
//			//Overload parameter used to indicate if MEMO must add headers (country, msisdn, ...)
//			jadFile.setValue(Constants.JAD_PARAMETER_WASSUP_HEADERS, String.valueOf(service.getWtheadersbymemo()));
//
//			//Define JAR URL for downloading
//			StringBuilder midletURL = new StringBuilder();
//			
//			if(signMidlet == null) {
//				//ready to sign delivery case (web admin)
//				midletURL.append(appName).append(Constants.JAR_FILE_EXTENSION);
//			} else {
//				//OTA delivery directly from mobile
//				
//				midletURL
//				.append(service.getHostname())
//				.append(com.orange.mmp.mvc.Constants.URL_MAPPING_OTA).append("/")
//				.append(mobile.getKey()).append("/");
//				midletURL.append(URLEncoder.encode(appName,com.orange.mmp.core.Constants.DEFAULT_ENCODING)).append(Constants.JAR_FILE_EXTENSION)
//				.append("?").append(com.orange.mmp.mvc.Constants.HTTP_PARAMETER_ID).append("=").append(URLEncoder.encode(ticket.getService(), com.orange.mmp.core.Constants.DEFAULT_ENCODING))
//				.append("&").append(com.orange.mmp.mvc.Constants.HTTP_PARAMETER_SIGN).append("=").append(Boolean.toString(signMidlet));
//			}
//
//			//Set default if not found
//			if(ticket.getService() == null) ticket.setService(ServiceManager.getInstance().getDefaultService().getId());
//
//			//Get service referenced in ticket
//			Service serviceT = service;
//			Service tmp = ServiceManager.getInstance().getServiceById(ticket.getService());
//			if(tmp != null) serviceT = tmp;
//
//			//Build JAD
//			jadFile.setValue(Constants.JAD_PARAMETER_JAR_URL, midletURL.toString());
//			jadFile.setValue(Constants.JAD_PARAMETER_APPNAME, appName);
//
//			Map<String,String> servicesSpecificMap = ticket.getServiceSpecific();
//			if(servicesSpecificMap != null){
//				for(String key : servicesSpecificMap.keySet()){
//					jadFile.setValue(key,servicesSpecificMap.get(key));
//				}
//			}
//
//			String launcherLine = jadFile.getValue(Constants.JAD_PARAMETER_LAUNCHER);
//			Matcher launcherLineMatcher = launcherPattern.matcher(launcherLine);
//			if(launcherLineMatcher.matches()){
//				jadFile.setValue(Constants.JAD_PARAMETER_LAUNCHER, appName.concat(", ").concat(launcherLineMatcher.group(2)).concat(", ").concat(launcherLineMatcher.group(3)));
//			}
//			else jadFile.setValue(Constants.JAD_PARAMETER_LAUNCHER, appName.concat(", ").concat(Constants.JAD_LAUNCHER_ICON).concat(", ").concat(Constants.JAD_LAUNCHER_MAINCLASS));
//
//			//Add/Modify/Delete JAD parameters according to mobile rules
//			JadAttributeAction[] jadActions = mobile.getJadAttributeActions();
//			for(JadAttributeAction jadAction : jadActions) {
//				// Test inJad
//				if(jadAction.getInJad().equals(ApplyCase.ALWAYS)
//						|| (signMidlet != null && signMidlet && jadAction.getInJad().equals(ApplyCase.SIGNED))
//						|| (signMidlet != null && !signMidlet && jadAction.getInJad().equals(ApplyCase.UNSIGNED))) {
//					if(jadAction.isAddAction() || jadAction.isModifyAction())
//						jadFile.setValue(
//								jadAction.getAttribute(), 
//								jadAction.getValue(), 
//								! jadAction.isStrict());
//					else if(jadAction.isDeleteAction())
//						jadFile.deleteValue(jadAction.getAttribute());	
//				}
//				
//			}
//
//			ByteArrayOutputStream jarContent = this.getJar(serviceT.getId(), mobile, signMidlet);
//			jadFile.setValue(Constants.JAD_PARAMETER_JAR_SIZE, String.valueOf(jarContent.size()));
//
//			// SIGN Midlet
//			if(signMidlet != null && signMidlet) {
//				//File jadTmpFile = new File(System.getProperty("java.io.tmpdir"),File.ticketId.concat(Constants.JAD_FILE_EXTENSION));
//				File jadTmpFile = File.createTempFile(
//						"jad2sign_", 
//						Constants.JAD_FILE_EXTENSION, 
//						new File(System.getProperty("java.io.tmpdir")));
//
//				// Save tmp Jad file
//				FileOutputStream fos = new FileOutputStream(jadTmpFile);
//				jadFile.save(fos, service.getCompactjadentries());
//
//				// Apply commands
//				BufferedInputStream jadIn = null;
//				BufferedInputStream keystoreIn = null;
//				ByteArrayInputStream jarIn = new ByteArrayInputStream(jarContent.toByteArray());
//				output = new ByteArrayOutputStream();
//				try{
//					jadIn = new BufferedInputStream(new FileInputStream(jadTmpFile));
//					keystoreIn = new BufferedInputStream(new FileInputStream(this.keystoreFile));
//					AppDescriptor midletSigner = new AppDescriptor();
//					midletSigner.load(jadIn, com.orange.mmp.core.Constants.DEFAULT_ENCODING);
//					midletSigner.loadKeyStore(keystoreIn, this.keystoreKey.toCharArray());
//					midletSigner.addCert(this.keystoreAlias, 1, 0);
//					midletSigner.addJarSignature(this.keystoreAlias, this.keystoreKey.toCharArray(),jarIn);
//					midletSigner.store(output, com.orange.mmp.core.Constants.DEFAULT_ENCODING);
//				}catch(InvalidJadException ije) {
//					String details = ije.getExtraData() == null ? null : ije.getExtraData();
//					throw new IOException("invalid JAD error : "+ije.getMessage()+(details == null ? "" : " (\""+details+"\")"));
//				}catch(Exception e){
//					throw new IOException(e.getMessage());
//				}
//				finally{
//					if(jadIn != null) jadIn.close();
//					if(jarIn != null) jarIn.close();
//					if(keystoreIn != null) keystoreIn.close();
//					if(jadTmpFile != null) jadTmpFile.delete();
//				}
//			}
//			//No SIGNING
//			else{
//				output = new ByteArrayOutputStream();
//				jadFile.save(output, service.getCompactjadentries());
//			}
//
//			return output;
//		}catch(IOException ioe) {
//			throw new MMPException(ioe.getMessage());
//		} catch(URISyntaxException use) {
//			throw new MMPException(use.getMessage());
//		} catch (MMPException mmpe) {
//			throw new MMPException(mmpe.getMessage());
//		} finally {
//			if(output != null){
//				try{
//					output.close();
//				}catch(IOException ioe){
//					//NOP
//				}
//			}
//		}
//	}

    /**
     * Get Midlet for download.
     * @param appId				The midlet main application ID
     * @param mobile 			The mobile to use
     * @param isMidletSigned	Boolean indicating if the midlet is signed (true), unsigned (false), to sign (null)
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
	public ByteArrayOutputStream getJar(String appId, Mobile mobile, Boolean isMidletSigned) throws MMPException{
        if(appId == null) appId = ServiceManager.getInstance().getDefaultService().getId();
        
        //Search in Cache first
        String jarKey = appId+isMidletSigned+mobile.getKey();

        if(this.midletCache.isKeyInCache(jarKey)){
        	return (ByteArrayOutputStream)this.midletCache.get(jarKey).getValue();
        }
        
        Object extraCSSJadAttr = null;
        
        //Not found, build the JAR
        ByteArrayOutputStream output = null;
        ZipOutputStream zipOut = null;
        ZipInputStream zipIn = null;
        InputStream resourceStream=null;
        try {
        	Midlet midlet = new Midlet();
        	midlet.setType(mobile.getMidletType());
        	Midlet []midlets = (Midlet[])DaoManagerFactory.getInstance().getDaoManager().getDao("midlet").find(midlet);
            if(midlets.length == 0) throw new MMPException("Midlet type not found : "+mobile.getMidletType());
            else midlet = midlets[0];

			//Get navigation widget
			Widget appWidget = WidgetManager.getInstance().getWidget(appId,mobile.getBranchId());
			if(appWidget == null) {
			    // Use Default if not found
			    appWidget = WidgetManager.getInstance().getWidget(appId);
			}
            List<URL> embeddedResources = WidgetManager.getInstance().findWidgetResources("/m4m/","*", appId, mobile.getBranchId(), false);
            output = new ByteArrayOutputStream();
            zipOut = new ZipOutputStream(output);
            zipIn = new ZipInputStream(new FileInputStream(new File(new URI(midlet.getJarLocation()))));

            ZipEntry entry;
            while(( entry = zipIn.getNextEntry()) != null){
                zipOut.putNextEntry(entry);

                // Manifest found, modify it before delivery
                if(entry.getName().equals(Constants.JAR_MANIFEST_ENTRY) && appWidget != null) {
                    Manifest midletManifest = new Manifest(zipIn);
                    
                    // TODO ? Remove optional permissions if midlet is not signed
					if(isMidletSigned != null && ! isMidletSigned)
						midletManifest.getMainAttributes().remove(Constants.JAD_PARAMETER_OPT_PERMISSIONS);
                    
                    midletManifest.getMainAttributes().putValue(Constants.JAD_PARAMETER_APPNAME,appWidget.getName());
                    String launcherLine = midletManifest.getMainAttributes().getValue(Constants.JAD_PARAMETER_LAUNCHER);
                    Matcher launcherLineMatcher = launcherPattern.matcher(launcherLine);
                    if(launcherLineMatcher.matches()){
                        midletManifest.getMainAttributes().putValue(Constants.JAD_PARAMETER_LAUNCHER, appWidget.getName().concat(", ").concat(launcherLineMatcher.group(2)).concat(", ").concat(launcherLineMatcher.group(3)));
                    }
                    else midletManifest.getMainAttributes().putValue(Constants.JAD_PARAMETER_LAUNCHER, appWidget.getName());
                    
                    // Add/Modify/Delete MANIFEST parameters according to mobile rules
					JadAttributeAction[] jadActions = mobile.getJadAttributeActions();
					for(JadAttributeAction jadAction : jadActions) {
						if(jadAction.getInManifest().equals(ApplyCase.ALWAYS)
								|| (isMidletSigned != null && isMidletSigned && jadAction.getInManifest().equals(ApplyCase.SIGNED))
								|| (isMidletSigned != null && !isMidletSigned && jadAction.getInManifest().equals(ApplyCase.UNSIGNED))) {
							Attributes.Name attrName = new Attributes.Name(jadAction.getAttribute());
							boolean exists = midletManifest.getMainAttributes().get(attrName) != null;
							if(jadAction.isAddAction() || jadAction.isModifyAction()) {
								if(exists || ! jadAction.isStrict())
									midletManifest.getMainAttributes().putValue(
											jadAction.getAttribute(),
											jadAction.getValue());
							}
							else if(jadAction.isDeleteAction() && exists)
								midletManifest.getMainAttributes().remove(attrName);
						}
					}
					
					//Retrieve MeMo CSS extra attribute
					extraCSSJadAttr = midletManifest.getMainAttributes().get(
							new Attributes.Name(Constants.JAD_PARAMETER_MEMO_EXTRA_CSS));

					
                    midletManifest.write(zipOut);
                }
                //Other files of Midlet
                else{
                    IOUtils.copy(zipIn, zipOut);
                }
                zipIn.closeEntry();
                zipOut.closeEntry();
            }

            if(embeddedResources != null){
                for(URL resourceUrl : embeddedResources){
                    resourceStream = resourceUrl.openConnection().getInputStream();
                    String resourcePath = resourceUrl.getPath(); 
                    entry = new ZipEntry(resourcePath.substring(resourcePath.lastIndexOf("/")+1));
                    entry.setTime(MIDLET_LAST_MODIFICATION_DATE);
                    zipOut.putNextEntry(entry);
                    IOUtils.copy(resourceStream, zipOut);
                    zipOut.closeEntry();
                    resourceStream.close();
                }
            }
            
            //Put JAR in cache for next uses
            this.midletCache.set(new Element(jarKey,output));
            
            
			//If necessary, add special CSS file if specified in JAD attributes
			if(extraCSSJadAttr != null) {
				String extraCSSSheetName = (String) extraCSSJadAttr;
				//Get resource stream
				resourceStream = WidgetManager.getInstance().getWidgetResource(extraCSSSheetName+"/"+this.cssSheetsBundleName, mobile.getBranchId());

				if(resourceStream == null)
					throw new DataAccessResourceFailureException("no CSS sheet named "+extraCSSSheetName+" in "+this.cssSheetsBundleName+" special bundle");

				//Append CSS sheet file into JAR
				entry = new ZipEntry(new File(extraCSSSheetName).getName());
				entry.setTime(MidletManager.MIDLET_LAST_MODIFICATION_DATE);
				zipOut.putNextEntry(entry);
				IOUtils.copy(resourceStream, zipOut);
				zipOut.closeEntry();
				resourceStream.close();
			}

            return output;
            
        }catch(IOException ioe){
            throw new MMPException(ioe);
        }catch(URISyntaxException use){
            throw new MMPException(use);
        }catch(DataAccessException dae){
            throw new MMPException(dae);
        }finally{
        	try{
        		if(output != null)output.close();
        		if(zipIn != null)zipIn.close();
        		if(zipOut != null)zipOut.close();
        		if(resourceStream != null)resourceStream.close();
        	}catch(IOException ioe){
        		//NOP
        	}
        }
    }
    
    /**
     * Deploy an uploaded Midlet archive on PFS
     * @param uploadedFile The updloaded PFM file (MultipartFile to copy)
     */
    public void deployMidlet(MultipartFile uploadedFile) throws MMPException{
        String fileName = uploadedFile.getOriginalFilename();
        File dstFile = new File(System.getProperty("java.io.tmpdir"),fileName);
        //Try to create file
        InputStream input = null;
        OutputStream output = null;
        try{
        	input = uploadedFile.getInputStream();
        	output = new FileOutputStream(dstFile);
            IOUtils.copy(input, output);
        }catch(IOException ioe){
                throw new MMPException("Failed to deploy PFM",ioe);
        }finally{
        	try{
        		if(input != null) input.close();
        		if(output != null) output.close();
        	}catch(IOException ioe){
        		//NOP
        	}
        }
        this.deployMidlet(dstFile);
    }

    /**
     * Deploy a midlet archive file on PFS
     * @param midletFile The midlet file (ZIP file)
     */
    @SuppressWarnings("unchecked")
    public void deployMidlet(File midletFile) throws MMPException{
        try{
        	File tmpDeployDir = new File(System.getProperty("java.io.tmpdir"),"tmpDeployDir");
        	FileUtils.deleteDirectory(tmpDeployDir);
        	FileUtils.forceMkdir(tmpDeployDir);

        	ZipInputStream zip = new ZipInputStream(new FileInputStream(midletFile));
        	ZipEntry entry;
        	try{
        		while((entry=zip.getNextEntry())!=null) {
        			if(entry.isDirectory()) {
        				File repFile = new File(tmpDeployDir,entry.getName());
        				repFile.mkdirs();
        			}
        			else{
        				String filename = entry.getName();
        				File entryFile = new File(tmpDeployDir, filename);
        				FileOutputStream fos = new FileOutputStream( entryFile );
        				try{
        					IOUtils.copy(zip, fos);
        				}finally{
        					fos.close();
        				}
        			}
        		}
        	}finally{
        		zip.closeEntry();
        	}

        	List<Midlet> midletList = new ArrayList<Midlet>();
        	Collection<File> filesList = FileUtils.listFiles(tmpDeployDir, new String[]{"jar","jad"}, true);

        	for(File currentfile : filesList){
        		String midletType = null;
        		String midletFilename = null;
        		if(currentfile.getParentFile().getAbsolutePath().equals(tmpDeployDir.getAbsolutePath())){
        			midletType = currentfile.getName().split("\\.")[0];
        		}
        		else{
        			midletType = currentfile.getParentFile().getName();
        		}

        		midletFilename = currentfile.getName();

        		if(midletType != null){
        			boolean found = false;
        			for(Midlet midlet : midletList){
        				found = false;
        				if(midlet.getType().equals(midletType)){
        					if(midlet.getJadLocation() == null) midlet.setJadLocation(currentfile.toURI().toString());
        					else midlet.setJarLocation(currentfile.toURI().toString());
        					found = true;
        					break;
        				}
        			}
        			if(!found){
        				Midlet midlet = new Midlet();
        				midlet.setType(midletType);
        				if(midletFilename.endsWith("jad")) midlet.setJadLocation(currentfile.toURI().toString());
        				else midlet.setJarLocation(currentfile.toURI().toString());
        				midletList.add(midlet);
        			}
        		}
        	}

    		for(Midlet midlet : midletList){
    			DaoManagerFactory.getInstance().getDaoManager().getDao("midlet").createOrUdpdate(midlet);
    		}

        	FileUtils.deleteDirectory(tmpDeployDir);
        }catch(IOException ioe){
    		throw new MMPException("Failed to deploy PFM",ioe);
    	}
    }      
    
    /**
     * Get all certificates from keystore
     * @return	A Vector containing certificates
     */
    @SuppressWarnings("unchecked")
	public Vector getCertificates() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, Exception {
    	AppDescriptor midletSigner = new AppDescriptor();
        midletSigner.loadKeyStore(new BufferedInputStream(new FileInputStream(this.keystoreFile)), this.keystoreKey.toCharArray());
        midletSigner.addCert(this.keystoreAlias, 1, 0);
        return midletSigner.getAllCerts();
    }
    
    /**
     * @param keystoreFile the keystoreFile file path to set
     */
    public void setKeystoreFile(String keystoreFile) {
        this.keystoreFile = keystoreFile;
    }

    /**
     * @param keystoreAlias the keystoreAlias to set
     */
    public void setKeystoreAlias(String keystoreAlias) {
        this.keystoreAlias = keystoreAlias;
    }

    /**
     * @param keystoreKey the keystoreKey to set
     */
    public void setKeystoreKey(String keystoreKey) {
        this.keystoreKey = keystoreKey;
    }

    /**
     * @return the keystoreFile
     */
    public String getKeystoreFile() {
        return keystoreFile;
    }

	/**
	 * @param midletCacheName the midletCacheName to set
	 */
	public void setMidletCacheName(String midletCacheName) {
		this.midletCacheName = midletCacheName;
	}
	
	/**
	 * @param cssSheetsBundleName the cssSheetsBundleName to set
	 */
	public void setCSSSheetsBundleName(String cssSheetsBundleName) {
		this.cssSheetsBundleName = cssSheetsBundleName;
	}
}
