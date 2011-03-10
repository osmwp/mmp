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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.orange.mmp.bind.BindingException;
import com.orange.mmp.bind.XMLBinding;
import com.orange.mmp.bind.data.mobile.MobileCatalog;
import com.orange.mmp.core.data.JadAttributeAction;
import com.orange.mmp.core.data.Mobile;

import com.orange.mmp.dao.MMPDaoException;

/**
 * Implementation of MobileDao using properties files
 * @author milletth
 *
 */
public class MobileDaoFlfImpl extends FlfDao<Mobile> {

	/**
	 * An inner Lock for shared resources
	 */
	private final Lock lock = new ReentrantLock();
	
	/**
	 * JAXB Mobile binding package
	 */
	private static final String MOBILE_XML_BINDING_PACKAGE = "com.orange.mmp.bind.data.mobile";
	
	/**
	 * Mobiles file path
	 */
	private String path;
	
	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.Dao#createOrUdpdate(java.lang.Object)
	 */
	public Mobile createOrUdpdate(Mobile mobile) throws MMPDaoException {
		if(mobile == null
				|| mobile.getFinalName() == null
				|| mobile.getKey() == null
				|| mobile.getShortUserAgent() == null){
			throw new MMPDaoException("Missing or bad data access object");
		}
		
		try{
			this.lock.lock();
			MobileCatalog mobileCatalog = this.loadMobileCatalog();
			for(MobileCatalog.Mobile currentMobile : mobileCatalog.getMobile()){
				if(currentMobile.getUserAgentKey().equals(mobile.getKey())){
					mobileCatalog.getMobile().remove(currentMobile);
					break;
				}
			}
			mobileCatalog.getMobile().add(this.fromMobilePOJOToMobileXML(mobile));
			this.saveMobileCatalog(mobileCatalog);
			return mobile;
		}finally{
			this.lock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.Dao#delete(java.lang.Object)
	 */
	public void delete(Mobile mobile) throws MMPDaoException {
		if(mobile == null){
			throw new MMPDaoException("Missing or bad data access object");
		}
		
		try{
			this.lock.lock();
			MobileCatalog mobileCatalog = new MobileCatalog();
			List<Mobile> mobileList = Arrays.asList(this.list());
			Mobile []mobilesToDelete = this.find(mobile);
			mobileCatalog.getMobile().clear();
			for(Mobile currentMobileToDelete : mobilesToDelete){
				for(Mobile currentMobile : mobileList){
					if(!currentMobileToDelete.getKey().equals(currentMobile.getKey())){
						mobileCatalog.getMobile().add(this.fromMobilePOJOToMobileXML(currentMobile));
					}
				}
			}
			this.saveMobileCatalog(mobileCatalog);						
		}finally{
			this.lock.unlock();
		}
		
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.Dao#find(java.lang.Object)
	 */
	public Mobile[] find(Mobile mobile) throws MMPDaoException {
		if(mobile == null){
			throw new MMPDaoException("Missing or bad data access object");
		}
		
		List<Mobile> mobileList = new ArrayList<Mobile>();
		for(Mobile currentMobile : this.list()){
			//Exclusive criteria
			if(mobile.getKey() != null){
				if(currentMobile.getKey().equals(mobile.getKey())){
					mobileList.add(currentMobile);
					break;
				}
				else continue;
			}
			if(mobile.getShortUserAgent() != null){
				if(currentMobile.getShortUserAgent().equals(mobile.getShortUserAgent())){
					mobileList.add(currentMobile);
					break;
				}
				else continue;
			}
			if(mobile.getUserAgent() != null){
				if(mobile.getUserAgent().contains(currentMobile.getShortUserAgent()) ){
					mobileList.add(currentMobile);
					break;
				}
				else continue;
			}
			if(mobile.getCodeName() != null){
				if(currentMobile.getCodeName().equals(mobile.getCodeName())){
					mobileList.add(currentMobile);
					break;
				}
				else continue;
			}
			if(mobile.getFinalName() != null){
				if(currentMobile.getFinalName().equals(mobile.getFinalName())){
					mobileList.add(currentMobile);
					break;
				}
				else continue;
			}
			//Non Exclusive criteria
			if(mobile.getBranchId() != null){
				if(!currentMobile.getBranchId().equals(mobile.getBranchId())) continue;
			}
			if(mobile.getMidletType() != null){
				if(!currentMobile.getMidletType().equals(mobile.getMidletType())) continue;
			}
			if(mobile.getDropDate() != null){
				if(!currentMobile.getDropDate().equals(mobile.getDropDate())) continue;
			}
			if(mobile.getOtaEnabled()){
				if(!currentMobile.getOtaEnabled()) continue;
			}
			//No exclusion, add it
			mobileList.add(currentMobile);
		}
		
		Mobile[] mobiles = new Mobile[mobileList.size()];
		return mobileList.toArray(mobiles);
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
	public Mobile[] list() throws MMPDaoException {
		Mobile[] mobiles = null;
		MobileCatalog mobileCatalog = this.loadMobileCatalog();
		if(mobileCatalog.getMobile() != null){
			mobiles = new Mobile[mobileCatalog.getMobile().size()];
			int mobileIndex = 0;
			for(com.orange.mmp.bind.data.mobile.MobileCatalog.Mobile currentMobile : mobileCatalog.getMobile()){
				mobiles[mobileIndex++] = this.fromMobileXMLToMobilePOJO(currentMobile);
			}
		}
		else mobiles = new Mobile[0];
		
		return mobiles;
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.flf.FlfDao#checkDaoConfig()
	 */
	@Override
	public void checkDaoConfig() throws MMPDaoException {
		if(this.path == null) throw new MMPDaoException("Missing Path for Mobile DAO FLF configuration");
		File file = new File(this.path);
		if(!file.exists()) throw new MMPDaoException("Missing configuration file for Mobile DAO FLF configuration");
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}
	
	/**
	 * Inner method used to load Mobile Catalog file
	 * 
	 * @return The MobileCatalog instance based on file
	 * @throws MMPDaoException
	 */
	private MobileCatalog loadMobileCatalog() throws MMPDaoException{
		InputStream in = null;
		try{
			in = new FileInputStream(this.path);
			return (MobileCatalog)new XMLBinding().read(in, MOBILE_XML_BINDING_PACKAGE, null);
		}catch(FileNotFoundException fne){
			throw new MMPDaoException(fne);
		}catch(BindingException be){
			throw new MMPDaoException(be);
		}finally{
			if(in != null){
				try{
					in.close();
				}catch(IOException ioe){
					//NOP
				}
			}
		}		
	}
	
	/**
	 * Inner method used to save Mobile Catalog file
	 * 
	 * @param mobileCatalog The MobileCatalog instance to save
	 * @throws MMPDaoException
	 */
	private void saveMobileCatalog(MobileCatalog mobileCatalog) throws MMPDaoException{
		OutputStream out = null;
		try{
			out = new FileOutputStream(this.path);
			new XMLBinding().write(mobileCatalog, out, MOBILE_XML_BINDING_PACKAGE, null);
		}catch(FileNotFoundException fne){
			throw new MMPDaoException(fne);
		}catch(BindingException be){
			throw new MMPDaoException(be);
		}finally{
			if(out != null){
				try{
					out.close();
				}catch(IOException ioe){
					//NOP
				}
			}
		}
	}
	
	/**
	 * Builds a Mobile POJO object from a Mobile XML object (JAXB)
	 * 
	 * @param mobileXML	The Mobile XML Object
	 * @return	The corresponding Mobile POJO
	 */
	private Mobile fromMobileXMLToMobilePOJO(MobileCatalog.Mobile mobileXML) {
		Mobile mobilePOJO = new Mobile();
		mobilePOJO.setKey(mobileXML.getUserAgentKey());
		mobilePOJO.setShortUserAgent(mobileXML.getShortUA());
		mobilePOJO.setCodeName(mobileXML.getCodeName());
		mobilePOJO.setFinalName(mobileXML.getFinalName());
		mobilePOJO.setMidletType(mobileXML.getMidletType());
		mobilePOJO.setBranchId(mobileXML.getBranchId());
		mobilePOJO.setMobileDropped(mobileXML.isDropped());
		if(mobileXML.isDropped()) mobilePOJO.setDropDate(mobileXML.getDropDate());
		mobilePOJO.setOtaEnabled(mobileXML.isOTAEnabled() != null && mobileXML.isOTAEnabled());
		// JAD Attributes
		if(mobileXML.getJadAttributes() != null) {
			List<JadAttributeAction> jadAttributeActionsPOJO = new ArrayList<JadAttributeAction>();
			for(MobileCatalog.Mobile.JadAttributes.JadAction currentJadActionXML : mobileXML.getJadAttributes().getJadAction()) {
				JadAttributeAction jadAttributeActionPOJO = new JadAttributeAction();
				jadAttributeActionPOJO.setAction(JadAttributeAction.actionFromString(currentJadActionXML.getAction()));
				jadAttributeActionPOJO.setAttribute(currentJadActionXML.getAttribute());
				jadAttributeActionPOJO.setValue(currentJadActionXML.getValue());
				jadAttributeActionPOJO.setStrict(currentJadActionXML.isStrict());
				jadAttributeActionPOJO.setInJad(JadAttributeAction.applyCaseFromString(currentJadActionXML.getInJad()));
				jadAttributeActionPOJO.setInManifest(JadAttributeAction.applyCaseFromString(currentJadActionXML.getInMF()));
				jadAttributeActionsPOJO.add(jadAttributeActionPOJO);
			}
			mobilePOJO.setJADAttributesActions(jadAttributeActionsPOJO);
		}
		
		return mobilePOJO;
	}
	
	/**
	 * Builds a Mobile XML object (JAXB) from a Mobile POJO
	 * @param mobilePOJO The Mobile POJO instance
	 * @return	The corresponding Mobile XML instance (JAXB)
	 */
	private MobileCatalog.Mobile fromMobilePOJOToMobileXML(Mobile mobilePOJO){
		MobileCatalog.Mobile mobileXML = new MobileCatalog.Mobile();
		mobileXML.setUserAgentKey(mobilePOJO.getKey());
		mobileXML.setShortUA(mobilePOJO.getShortUserAgent());
		mobileXML.setCodeName(mobilePOJO.getCodeName());
		mobileXML.setFinalName(mobilePOJO.getFinalName());
		mobileXML.setMidletType(mobilePOJO.getMidletType());
		mobileXML.setBranchId(mobilePOJO.getBranchId());
		mobileXML.setDropped(mobilePOJO.isMobileDropped());
		mobileXML.setDropDate(mobilePOJO.getDropDate());
		mobileXML.setOTAEnabled(mobilePOJO.getOtaEnabled());
		// JAD Attributes
		mobileXML.setJadAttributes(new MobileCatalog.Mobile.JadAttributes());
		for(JadAttributeAction currentJadActionPOJO : mobilePOJO.getJadAttributeActions()) {
			MobileCatalog.Mobile.JadAttributes.JadAction currentJadActionXML = new MobileCatalog.Mobile.JadAttributes.JadAction();
			currentJadActionXML.setAction(currentJadActionPOJO.getAction().action());
			currentJadActionXML.setAttribute(currentJadActionPOJO.getAttribute());
			currentJadActionXML.setValue(currentJadActionPOJO.getValue());
			currentJadActionXML.setInJad(currentJadActionPOJO.getInJad().applyCase());
			currentJadActionXML.setInMF(currentJadActionPOJO.getInManifest().applyCase());
			currentJadActionXML.setStrict(currentJadActionPOJO.isStrict());
			mobileXML.getJadAttributes().getJadAction().add(currentJadActionXML);
		}
		
		return mobileXML;
	}
}
