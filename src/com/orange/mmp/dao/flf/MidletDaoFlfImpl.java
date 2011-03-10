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
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import com.orange.mmp.core.data.Midlet;
import com.orange.mmp.core.data.Version;
import com.orange.mmp.midlet.Constants;
import com.orange.mmp.midlet.JadFile;

import com.orange.mmp.dao.MMPDaoException;

/**
 * Implementation of PFM DAO using shared repoistory (files)
 * @author milletth
 *
 */
public class MidletDaoFlfImpl extends FlfDao<Midlet> {

	/**
	 * PFMs files folder path
	 */
	private String path;
	
	/**
	 * An inner Lock for shared resources
	 */
	private final Lock lock = new ReentrantLock();
	
	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.Dao#createOrUdpdate(java.lang.Object)
	 */
	public Midlet createOrUdpdate(Midlet midlet) throws MMPDaoException {
		if(midlet == null
    			|| midlet.getJadLocation() == null
    			|| midlet.getJarLocation() == null
    			|| midlet.getType() == null){
    		throw new MMPDaoException("missing or bad data access object");
    	}

    	try{
    		this.lock.lock();
    		File typeFolder = new File(this.path,midlet.getType());
    		if(!typeFolder.isDirectory()) FileUtils.forceMkdir(typeFolder);
    		File jadFile = new File(new URI(midlet.getJadLocation()));
    		FileUtils.copyFileToDirectory(jadFile, typeFolder);
    		File jarFile = new File(new URI(midlet.getJarLocation()));
    		FileUtils.copyFileToDirectory(jarFile, typeFolder);
    		FileUtils.touch(new File(this.path));
    	}catch(IOException ioe){
    		throw new MMPDaoException("failed to add PFM : "+ioe.getMessage());
    	}catch(URISyntaxException use){
    		throw new MMPDaoException("failed to add PFM : "+use.getMessage());
    	}finally {
			this.lock.unlock();
		}

    	return midlet;
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.Dao#delete(java.lang.Object)
	 */
	public void delete(Midlet midlet) throws MMPDaoException {
		Midlet[] pfmList = this.find(midlet);
    	try{
    		this.lock.lock();
    		for(Midlet toDelete : pfmList){
    			FileUtils.deleteDirectory(new File(this.path,toDelete.getType()));
    		}
    		FileUtils.touch(new File(this.path));
    	}catch(IOException ioe){
    		throw new MMPDaoException("failed to delete PFM : "+ioe.getMessage());
    	}finally{
    		this.lock.unlock();
    	}    	
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.Dao#find(java.lang.Object)
	 */
	public Midlet[] find(Midlet midlet) throws MMPDaoException {
		if(midlet == null){
    		throw new MMPDaoException("missing or bad data access object");
    	}

    	FilenameFilter jadFilter = new SuffixFileFilter(".jad");
    	FilenameFilter jarFilter = new SuffixFileFilter(".jar");
    	Midlet []midlets = null;
    	ArrayList<Midlet> midletList = new ArrayList<Midlet>();

    	try{
    		File midletFolder = new File(this.path);
    		for(File typeFolder : midletFolder.listFiles()){
    			if(midlet.getType() == null || midlet.getType().equals(typeFolder.getName())){
    				File[] jadFiles = typeFolder.listFiles(jadFilter);
    				File[] jarFiles = typeFolder.listFiles(jarFilter);
    				if(jadFiles != null && jadFiles.length > 0 && jarFiles != null && jarFiles.length > 0){
    					JadFile jadFile = new JadFile();
    					jadFile.load(jadFiles[0]);
    					if(midlet.getVersion() == null || jadFile.getValue(Constants.JAD_PARAMETER_VERSION).equals(midlet.getVersion())){
    						Midlet newPFM = new Midlet();
    						newPFM.setType(typeFolder.getName());
    						newPFM.setVersion(new Version(jadFile.getValue(Constants.JAD_PARAMETER_VERSION)));
    						newPFM.setJadLocation(jadFiles[0].toURI().toString());
    						newPFM.setJarLocation(jarFiles[0].toURI().toString());
    						midletList.add(newPFM);
    					}
    				}
    			}	
    		}
    	}catch(IOException ioe){
    		throw new MMPDaoException("failed to find PFM : "+ioe.getMessage());
    	}

    	midlets = new Midlet[midletList.size()];
    	return midletList.toArray(midlets);
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
	public Midlet[] list() throws MMPDaoException {
		Midlet midlet = new Midlet();
		return this.find(midlet);
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.flf.FlfDao#checkDaoConfig()
	 */
	@Override
	public void checkDaoConfig() throws MMPDaoException {
		if(this.path == null) throw new MMPDaoException("Missing Path for Midlet DAO FLF configuration");
		File file = new File(this.path);
		if(!file.exists()){
			try{
				FileUtils.forceMkdir(file);
			}catch(IOException ioe){
				throw new MMPDaoException("Failed to create Midlet DAO FLF folder : "+ioe.getMessage());
			}
		}
		else if(!file.isDirectory()){
			throw new MMPDaoException("Path used for Midlet DAO FLF must be a directory");
		}		
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}
}
