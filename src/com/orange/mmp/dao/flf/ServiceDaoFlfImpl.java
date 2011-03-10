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
package com.orange.mmp.dao.flf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import com.orange.mmp.bind.BindingException;
import com.orange.mmp.bind.XMLBinding;

import com.orange.mmp.dao.MMPDaoException;

import com.orange.mmp.core.data.Service;

/**
 * Implementation of Service DAO using shared repository (files)
 * @author nmtv3386
 *
 */
public class ServiceDaoFlfImpl extends FlfDao<Service> {

	/**
	 * An inner Lock for shared resources
	 */
	private final Lock lock = new ReentrantLock();
	
	/**
	 * JAXB Binding package for Service POJO bindings
	 */
	public static final String SERVICE_XML_BINDING_PACKAGE = "com.orange.mmp.bind.data.service";
	
	/**
	 * File format of Sevices files
	 */
	private static final MessageFormat filenameFormat = new MessageFormat("{0}.xml");
	
	/**
	 * Services files folder path
	 */
	private String path;
	
	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.Dao#createOrUdpdate(java.lang.Object)
	 */
	public Service createOrUdpdate(Service service) throws MMPDaoException {
		if (service == null || service.getId() == null) {
			throw new MMPDaoException("missing or bad data access object");
		}
		this.lock.lock();
		// Find default service
		Service defaultService = new Service();
		defaultService.setIsDefault(true);
		Service[] tmpServices = this.find(defaultService);
		if(tmpServices.length > 0)
			defaultService = tmpServices[0];
		else
			defaultService = null;
		
		Service aService = new Service();
		Service[] allServices = this.find(aService);
		if(!service.getIsDefault()) {
			if(allServices.length == 0
					|| (allServices.length == 1 && allServices[0].getId().equals(service.getId()))) {
				// This is the only service: change isDefault to true
				service.setIsDefault(true);
			} else {
				if(defaultService == null) {
					// If no default service, take the first in the list and make it default
					for(int i = 0; i < allServices.length; i++) {
						Service tmpService = allServices[i];
						if(!tmpService.getId().equals(service.getId())) {
							tmpService.setIsDefault(true);
							this.createOrUdpdate(tmpService);
							break;
						}
					}
				}
			}
		}
		
		OutputStream out = null;
		try {
			// Build dedicated service XML file
			File serviceFile = new File(this.path.concat("/").concat(
					filenameFormat.format(new String[] { service.getId() })));
		
			out = new FileOutputStream(serviceFile);

			new XMLBinding().write(this.fromServicePOJOToServiceXML(service), out, SERVICE_XML_BINDING_PACKAGE, null);
		
			FileUtils.touch(new File(this.path));
			
			if(service.getIsDefault()) {
				if(defaultService != null && !defaultService.getId().equals(service.getId())) {
					// Change defaultService.isDefault to false
					defaultService.setIsDefault(false);
					this.createOrUdpdate(defaultService);
				}
			} else {
				if(defaultService != null && defaultService.getId().equals(service.getId())) {
					for(int i = 0; i < allServices.length; i++) {
						Service tmpService = allServices[i];
						if(!tmpService.getId().equals(service.getId())) {
							tmpService.setIsDefault(true);
							this.createOrUdpdate(tmpService);
							break;
						}
					}
				}
			}
			
			return service;
		} catch (IOException ioe) {
			throw new MMPDaoException("unable to add/update service [" + ioe.getMessage() + "]");
		}catch(BindingException be){
			throw new MMPDaoException("unable to add/update service [" + be.getMessage() + "]");
		} finally {
			if(out != null){
				try{
					out.close();
				}catch(IOException ioe){
					//NOP
				}
			}
			this.lock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.Dao#delete(java.lang.Object)
	 */
	public void delete(Service service) throws MMPDaoException {
		if(service == null) {
			throw new MMPDaoException("missing or bad data access object");
		}

		Service[] services = this.find(service);
		
		for(Service foundService : services) {
			this.lock.lock();
			
			if(foundService.getIsDefault()) {
				Service aService = new Service();
				Service[] allServices = this.find(aService);
				if(allServices.length < 2) {
					// If it is the only service, don't remove it
					throw new MMPDaoException("cannot remove last service");
				} else {
					for(int i = 0; i < allServices.length; i++) {
						if(!allServices[i].getId().equals(foundService.getId())) {
							// Another service must become the default service
							allServices[i].setIsDefault(true);
							this.createOrUdpdate(allServices[i]);
							break;
						}
					}
				}
			}
			
			try {
				File serviceFile = new File(this.path.concat("/").concat(
					filenameFormat.format(new String[] {foundService.getId()})));
				if(! serviceFile.delete()){
					throw new MMPDaoException("failed to delete service");
				}

				try {
					FileUtils.touch(new File(this.path));
				} catch(IOException ioe) {
					//Nop
				}
			} finally {
				this.lock.unlock();
			}
		}		
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.Dao#find(java.lang.Object)
	 */
	public Service[] find(Service service) throws MMPDaoException {
		if(service == null){
			throw new MMPDaoException("missing or bad data access object");
		}

		FilenameFilter xmlFilter = new SuffixFileFilter(".xml");
		Service []services = null;
		ArrayList<Service> serviceList = new ArrayList<Service>();

		File servicesFolder = new File(this.path);
	
		for(File serviceFile : servicesFolder.listFiles(xmlFilter)){
			InputStream in = null;
			try{
				in = new FileInputStream(serviceFile);
				com.orange.mmp.bind.data.service.Service currentService = (com.orange.mmp.bind.data.service.Service)new XMLBinding().read(in,SERVICE_XML_BINDING_PACKAGE, null);
				boolean toAdd = true;
				if(service.getId() == null || currentService.getId().equals(service.getId())){
					toAdd = true;
				}
				else toAdd = false;
				if(toAdd && (service.getHostname() == null || currentService.getHostname().equals(service.getHostname()))){
					toAdd = true;
				}
				else toAdd = false;
				if(toAdd && (service.getIsDefault() == null || currentService.isDefault() == service.getIsDefault())){
					toAdd = true;
				}
				else toAdd = false;

				if(toAdd) serviceList.add(this.fromServiceXMLToServicePOJO(currentService));
			}catch(BindingException be){
				//NOP, just skip the current service
			}
			catch(IOException ioe){
				//NOP, just skip the current service
			}
			finally{
				if(in != null){
					try {
						in.close();
					}catch(IOException ioe){
					//NOP
					}				
				}
			}
		}
		
		services = new Service[serviceList.size()];
		return serviceList.toArray(services);
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.Dao#getLastUpdateTimestamp()
	 */
	public long getLastUpdateTimestamp() throws MMPDaoException {
		return new File(this.path).lastModified();
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.Dao#list()
	 */
	public Service[] list() throws MMPDaoException {
		Service service = new Service();
		return this.find(service);
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.flf.FlfDao#checkDaoConfig()
	 */
	@Override
	public void checkDaoConfig() throws MMPDaoException {
		if(this.path == null) throw new MMPDaoException("Missing Path for Service DAO FLF configuration");
		File file = new File(this.path);
		if(!file.exists()){
			try{
				FileUtils.forceMkdir(file);
			}catch(IOException ioe){
				throw new MMPDaoException("Failed to create Service DAO FLF folder : "+ioe.getMessage());
			}
		}
		else if(!file.isDirectory()){
			throw new MMPDaoException("Path used for Service DAO FLF must be a directory");
		}
	}
	
	/**
	 * Builds a Service POJO object from a Service XML object (JAXB)
	 * 
	 * @param serviceXML The Service XML Object
	 * @return	The corresponding Service POJO
	 */
	private Service fromServiceXMLToServicePOJO(com.orange.mmp.bind.data.service.Service serviceXML) throws MMPDaoException{
		Service servicePOJO = new Service();
		servicePOJO.setId(serviceXML.getId());
		servicePOJO.setCompactjadentries(serviceXML.isCompactjadentries());
		servicePOJO.setHomepage(serviceXML.isHomepage());
		servicePOJO.setHostname(serviceXML.getHostname());
		servicePOJO.setIsDefault(serviceXML.isDefault());
		servicePOJO.setSigned(serviceXML.isSigned());
		servicePOJO.setUsedefault(serviceXML.isUsedefault());
		servicePOJO.setWtheadersbymemo(serviceXML.isWtheadersbymemo());
		if(serviceXML.getJadentries() != null && serviceXML.getJadentries().getJadentry() != null){
			Map< String, String> jadEntries = new HashMap<String, String>();
			for(com.orange.mmp.bind.data.service.Service.Jadentries.Jadentry jadEntry : serviceXML.getJadentries().getJadentry()){
				jadEntries.put(jadEntry.getKey(), jadEntry.getValue());
			}
			servicePOJO.setJadEntries(jadEntries);
		}
		return servicePOJO;
	}
	
	/**
	 * Builds a Service XML object (JAXB) from a Service POJO
	 * @param servicePOJO The Service POJO instance
	 * @return	The corresponding Service XML instance (JAXB)
	 */
	private com.orange.mmp.bind.data.service.Service fromServicePOJOToServiceXML(Service servicePOJO){
		com.orange.mmp.bind.data.service.Service serviceXML = new com.orange.mmp.bind.data.service.Service();
		serviceXML.setId(servicePOJO.getId());
		serviceXML.setCompactjadentries(servicePOJO.getCompactjadentries());
		serviceXML.setHomepage(servicePOJO.getHomepage());
		serviceXML.setHostname(servicePOJO.getHostname());
		serviceXML.setDefault(servicePOJO.getIsDefault());
		serviceXML.setSigned(servicePOJO.getSigned());
		serviceXML.setUsedefault(servicePOJO.getUsedefault());
		serviceXML.setWtheadersbymemo(servicePOJO.getWtheadersbymemo());
		if(servicePOJO.getJadEntries() != null){
			com.orange.mmp.bind.data.service.Service.Jadentries jadentries = new com.orange.mmp.bind.data.service.Service.Jadentries();
			for(String key : servicePOJO.getJadEntries().keySet()){
				com.orange.mmp.bind.data.service.Service.Jadentries.Jadentry jadEntry = new com.orange.mmp.bind.data.service.Service.Jadentries.Jadentry();
				jadEntry.setKey(key);
				jadEntry.setValue(servicePOJO.getJadEntries().get(key));
				jadentries.getJadentry().add(jadEntry);
			}
			serviceXML.setJadentries(jadentries);
		}
		return serviceXML;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}
}
