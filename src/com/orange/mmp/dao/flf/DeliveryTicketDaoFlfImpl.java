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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import com.orange.mmp.core.data.DeliveryTicket;

import com.orange.mmp.dao.MMPDaoException;

/**
 * Implementation of Delivery ticket DAO using shared repoistory (files)
 * @author milletth
 *
 */
public class DeliveryTicketDaoFlfImpl extends FlfDao<DeliveryTicket> {

    /**
     * The TMP file extension
     */
    private static final String TMP_FILE_EXT = ".tmp";

    /**
     * The Mobile Phone Number entry key in TMP File
     */
    private static final String ND_PARAMETER = "msisdn";

    /**
     * The Mobile Client service in TMP File
     */
    private static final String SERVICE_PARAMETER = "Service";

    /**
     * The Mobile Client uaKey in TMP File
     */
    private static final String UAKEY_PARAMETER = "UA-Key";
    
    /**
     * The Mobile Client callback in TMP File
     */
    private static final String CALLBACK_PARAMETER = "callback";

    /**
     * The prefix used in delivery ticket to set optionnal entries in JAD
     */
    private static final String JAD_PREFIX_ENTRY = "Jad-Entry-";
    
	/**
	 * DeliveryTickets files folder path
	 */
	private String path;
	
	/**
	 * An inner Lock for shared resources
	 */
	private final Lock lock = new ReentrantLock();
	
	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.Dao#createOrUdpdate(java.lang.Object)
	 */
	public DeliveryTicket createOrUdpdate(DeliveryTicket deliveryTicket) throws MMPDaoException {
		if(deliveryTicket == null){
		    throw new MMPDaoException("missing or bad data access object");
		}

		OutputStream outProps = null;
	
		try{
			this.lock.lock();
		    File temp ;
		    if(deliveryTicket.getId() != null){
		    	temp = new File(this.path,deliveryTicket.getId().concat(TMP_FILE_EXT));
		    	if(!temp.exists()) throw new MMPDaoException("Invalid ticket id "+deliveryTicket.getId());
		    }
		    else{
		    	temp = File.createTempFile(String.valueOf(Math.round(Math.random()*10000)), null,new File(this.path));
		    }
		    deliveryTicket.setId(temp.getName().split("\\.")[0]);
		    deliveryTicket.setCreationDate(temp.lastModified());
		    Properties clientProps = new Properties();
		    if(deliveryTicket.getMsisdn() != null) {
		    	clientProps.setProperty(ND_PARAMETER, deliveryTicket.getMsisdn());
		    }
		    if(deliveryTicket.getServiceId() != null){
		    	clientProps.setProperty(SERVICE_PARAMETER, deliveryTicket.getServiceId());
		    }
		    if(deliveryTicket.getUaKey() != null){
		    	clientProps.setProperty(UAKEY_PARAMETER, deliveryTicket.getUaKey());
		    }
		    if(deliveryTicket.getCallback() != null) {
		    	clientProps.setProperty(CALLBACK_PARAMETER, deliveryTicket.getCallback());
		    }
		    if(deliveryTicket.getServiceSpecific() != null) {
		    	Map<String, String> serviceSpecificMap = deliveryTicket.getServiceSpecific();
		    	for(String key : serviceSpecificMap.keySet()){
		    		clientProps.setProperty(JAD_PREFIX_ENTRY.concat(key), serviceSpecificMap.get(key).toString());
		    	}
		    }
	
		    outProps = new FileOutputStream(temp);
		    clientProps.store(outProps, null);
		    outProps.close();
	
		    FileUtils.touch(new File(this.path));
	
		}catch(IOException ioe){
		    throw new MMPDaoException("failed to add ticket : "+ioe.getMessage());
		}finally{
		    try{
		    	if(outProps != null) outProps.close();
		    }catch(IOException ioe){
			//Nop
		    }
		    this.lock.unlock();
		}
	
		return deliveryTicket;
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.Dao#delete(java.lang.Object)
	 */
	public void delete(DeliveryTicket deliveryTicket) throws MMPDaoException {
		DeliveryTicket deliveryTickets[] = this.find(deliveryTicket);
		try{
			this.lock.lock();
			for(DeliveryTicket currentTicket : deliveryTickets){
	    		File toDelete = new File(this.path,currentTicket.getId().concat(".tmp"));
	    		if(!toDelete.delete()){
	    			throw new MMPDaoException("failed to delete ticket "+currentTicket.getId());
	    		}
	    	}
    	
    		FileUtils.touch(new File(this.path));
    	}catch(IOException ioe){
    		throw new MMPDaoException("failed to delete ticket : "+ioe.getMessage());
    	}finally{
    		this.lock.unlock();
    	}
		
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.Dao#find(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public DeliveryTicket[] find(DeliveryTicket deliveryTicket) throws MMPDaoException {
		if(deliveryTicket == null){
    		throw new MMPDaoException("missing or bad data access object");
    	}

    	//Find by Id
    	if(deliveryTicket.getId() != null){
    		File toFind = new File(this.path,deliveryTicket.getId().concat(".tmp"));
    		if(toFind.exists()){
    			InputStream inProps = null;
    			DeliveryTicket[] ticketList = new DeliveryTicket[1];
    			ticketList[0] = new DeliveryTicket();
    			try{
    				inProps = new FileInputStream(toFind.getAbsolutePath());
    				Properties propFile = new Properties();
    				propFile.load(inProps);
    				ticketList[0].setId(deliveryTicket.getId());

    				Enumeration propNames = propFile.propertyNames();
    				HashMap<String, String> serviceSpecificMap = new HashMap<String, String>();
    				while(propNames.hasMoreElements()) {
    					String currentProp = propNames.nextElement().toString();
    					if(currentProp.equals(ND_PARAMETER))
    						ticketList[0].setMsisdn(propFile.getProperty(ND_PARAMETER));
    					else if(currentProp.equals(SERVICE_PARAMETER))
    						ticketList[0].setServiceId(propFile.getProperty(SERVICE_PARAMETER));
    					else if(currentProp.equals(UAKEY_PARAMETER))
    						ticketList[0].setUaKey(propFile.getProperty(UAKEY_PARAMETER));
    					else if(currentProp.equals(CALLBACK_PARAMETER))
    						ticketList[0].setCallback(propFile.getProperty(CALLBACK_PARAMETER));
    					else if(currentProp.startsWith(JAD_PREFIX_ENTRY)){
    						serviceSpecificMap.put(currentProp.substring(JAD_PREFIX_ENTRY.length()), propFile.getProperty(currentProp));
    					}
    				}
    				ticketList[0].setServiceSpecific(serviceSpecificMap);
    				ticketList[0].setCreationDate(toFind.lastModified());

    			}catch(IOException ioe){
    				throw new MMPDaoException("failed to load ticket");
    			}finally{
    				try{
    					if(inProps != null) inProps.close();
    				}catch(IOException ioe){
    					//Nop
    				}
    			}
    			return ticketList;
    		}
    	}
    	else{
    		FilenameFilter tmpFilter = new SuffixFileFilter(".tmp");
    		File ticketsFiles[] = new File(this.path).listFiles(tmpFilter);
    		if(ticketsFiles == null) return new DeliveryTicket[0];
    		ArrayList<DeliveryTicket> list = new ArrayList<DeliveryTicket>();
    		InputStream inProps = null;
    		Properties propFile = new Properties();

    		try{
    			for(File found : ticketsFiles){
    				inProps = new FileInputStream(found.getAbsolutePath());
    				propFile.load(inProps);
    				boolean match = (deliveryTicket.getMsisdn() == null);
    				if(!match){
    					boolean goOnchecking = true;
    					if(deliveryTicket.getMsisdn() != null){
    						match = (propFile.getProperty(ND_PARAMETER) != null && propFile.getProperty(ND_PARAMETER).equals(deliveryTicket.getMsisdn()));
    						goOnchecking = match;
    					}
    					if(deliveryTicket.getServiceId() != null && goOnchecking){
    						match = (propFile.getProperty(SERVICE_PARAMETER) != null && propFile.getProperty(SERVICE_PARAMETER).equals(deliveryTicket.getServiceId()));
    						goOnchecking = match;
    					}
    					if(deliveryTicket.getUaKey() != null && goOnchecking){
    						match = (propFile.getProperty(UAKEY_PARAMETER) != null && propFile.getProperty(UAKEY_PARAMETER).equals(deliveryTicket.getUaKey()));
    						goOnchecking = match;
    					}
    					if(deliveryTicket.getCallback() != null && goOnchecking){
    						match = (propFile.getProperty(CALLBACK_PARAMETER) != null && propFile.getProperty(CALLBACK_PARAMETER).equals(deliveryTicket.getCallback()));
    						goOnchecking = match;
    					}
    				}

    				if(match){
    					DeliveryTicket foundTicket = new DeliveryTicket();
    					foundTicket.setId(found.getName().split("\\.")[0]);

    					Enumeration propNames = propFile.propertyNames();
    					HashMap<String, String> serviceSpecificMap = new HashMap<String, String>();
    					while(propNames.hasMoreElements()) {
    						String currentProp = propNames.nextElement().toString();
    						if(currentProp.equals(ND_PARAMETER)) {
    							foundTicket.setMsisdn(propFile.getProperty(ND_PARAMETER));
    						} else if(currentProp.equals(SERVICE_PARAMETER)) {
    							foundTicket.setServiceId(propFile.getProperty(SERVICE_PARAMETER));
    						} else if(currentProp.startsWith(JAD_PREFIX_ENTRY)){
    							serviceSpecificMap.put(currentProp.substring(JAD_PREFIX_ENTRY.length()), propFile.getProperty(currentProp));
    						}
    					}
    					foundTicket.setServiceSpecific(serviceSpecificMap);
    					foundTicket.setCreationDate(found.lastModified());

    					list.add(foundTicket);
    				}
    			}
    		}catch(IOException ioe){
    			throw new MMPDaoException("failed to load ticket");
    		}finally{
    			try{
    				if(inProps != null) inProps.close();
    			}catch(IOException ioe){
    				//Nop
    			}
    		}

    		DeliveryTicket[] ticketList = new DeliveryTicket[list.size()];
    		return list.toArray(ticketList);
    	}

    	return new DeliveryTicket[0];
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
	public DeliveryTicket[] list() throws MMPDaoException {
		DeliveryTicket deliveryTicket = new DeliveryTicket();
		return this.find(deliveryTicket);
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.flf.FlfDao#checkDaoConfig()
	 */
	@Override
	public void checkDaoConfig() throws MMPDaoException {
		if(this.path == null) throw new MMPDaoException("Missing Path for Tickets DAO FLF configuration");
		File file = new File(this.path);
		if(!file.exists()){
			try{
				FileUtils.forceMkdir(file);
			}catch(IOException ioe){
				throw new MMPDaoException("Failed to create Tickets DAO FLF folder : "+ioe.getMessage());
			}
		}
		else if(!file.isDirectory()){
			throw new MMPDaoException("Path used for Tickets DAO FLF must be a directory");
		}		
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}
}
