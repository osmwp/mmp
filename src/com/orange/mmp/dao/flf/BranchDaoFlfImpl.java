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
import com.orange.mmp.bind.data.branch.BranchCatalog;
import com.orange.mmp.core.data.Branch;
import com.orange.mmp.core.data.Mobile;
import com.orange.mmp.dao.Dao;
import com.orange.mmp.dao.DaoManagerFactory;
import com.orange.mmp.dao.MMPDaoException;

public class BranchDaoFlfImpl extends FlfDao<Branch> {
	
	/**
	 * JAXB Branch binding package
	 */
	public static final String BRANCH_XML_BINDING_PACKAGE = "com.orange.mmp.bind.data.branch";
	
	/**
	 * An inner Lock for shared resources
	 */
	private final Lock lock = new ReentrantLock();
	
	/**
	 * Branches files folder path
	 */
	private String path;
	
	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.Dao#createOrUdpdate(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public Branch createOrUdpdate(Branch branch) throws MMPDaoException {
		if(branch == null
				|| branch.getId() == null
				|| branch.getName() == null) {
			throw new MMPDaoException("Missing or bad data access object");
		}
		
		try {
			this.lock.lock();
			BranchCatalog branchCatalog = this.loadBranchCatalog();
			for(BranchCatalog.Branch currentBranch : branchCatalog.getBranch()) {
				if(currentBranch.getId().equals(branch.getId())) {
					branchCatalog.getBranch().remove(currentBranch);
					break;
				}
			}
			branchCatalog.getBranch().add(this.fromBranchPOJOToBranchXML(branch));
			this.saveBranchCatalog(branchCatalog);
			// Add mobiles
			if(branch.getMobiles() != null && branch.getMobiles().size() > 0) {
				Dao<Mobile> mobileDao = DaoManagerFactory.getInstance().getDaoManager().getDao("mobile");
				for(Mobile mobile : branch.getMobiles()) {
					mobile.setBranchId(branch.getId());
					mobileDao.createOrUdpdate(mobile);
				}
			}
			return branch;
		} finally {
			this.lock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.Dao#delete(java.lang.Object)
	 */
	public void delete(Branch branch) throws MMPDaoException {
		if(branch == null){
			throw new MMPDaoException("Missing or bad data access object");
		}
		
		try{
			this.lock.lock();
			List<Branch> branchList = new ArrayList<Branch>(Arrays.asList(this.list()));
			Branch []branchesToDelete = this.find(branch);
			for(Branch currentBranch : branchesToDelete){
				branchList.remove(currentBranch);
			}
			BranchCatalog branchCatalog = this.loadBranchCatalog();
			branchCatalog.getBranch().clear();
			for(Branch currentBranch : branchList){
				branchCatalog.getBranch().add(this.fromBranchPOJOToBranchXML(currentBranch));
			}
			this.saveBranchCatalog(branchCatalog);						
		}finally{
			this.lock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.Dao#find(java.lang.Object)
	 */
	public Branch[] find(Branch branch) throws MMPDaoException {
		if(branch == null){
			throw new MMPDaoException("Missing or bad data access object");
		}
		
		Branch []branches = this.list();
		for(Branch currentBranch : branches){
			//Exclusive criteria
			if(branch.getId() != null && currentBranch.getId().equals(branch.getId())){
				return new Branch[]{currentBranch};
			}
			if(branch.isDefault() && currentBranch.isDefault()){
				return new Branch[]{currentBranch};
			}
			if(branch.getName() != null && currentBranch.getName().equals(branch.getName())){
				return new Branch[]{currentBranch};
			}
		}
		
		//Not found return an empty array
		if(branch.getId() != null || branch.isDefault() || branch.getName() != null)
			return new Branch[0];
		
		return branches;
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
	@SuppressWarnings("unchecked")
	public Branch[] list() throws MMPDaoException {
		BranchCatalog branchCatalog = this.loadBranchCatalog();
		if(branchCatalog != null){
			Branch[] branches = new Branch[branchCatalog.getBranch().size()];
			int branchIndex = 0;
			for(BranchCatalog.Branch currentBranch : branchCatalog.getBranch()){
				Branch branch = this.fromBranchXMLToBranchPOJO(currentBranch);
				Mobile mobile = new Mobile();
				mobile.setBranchId(currentBranch.getId());
				Mobile []branchMobiles = (Mobile[])DaoManagerFactory.getInstance().getDaoManager().getDao("mobile").find(mobile);
				branch.setMobiles(Arrays.asList(branchMobiles));
				branches[branchIndex++] = branch;
			}
			return branches;
		}
		else return new Branch[0]; 
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.flf.FlfDao#checkDaoConfig()
	 */
	@Override
	public void checkDaoConfig() throws MMPDaoException {
		if(this.path == null) throw new MMPDaoException("Missing Path for Branch DAO FLF configuration");
		File file = new File(this.path);
		if(!file.exists()) throw new MMPDaoException("Missing configuration file for Branch DAO FLF configuration");
	}
	
	/**
	 * Inner method used to load Branch Catalog file
	 * 
	 * @return The BranchCatalog instance based on file
	 * @throws MMPDaoException
	 */
	private BranchCatalog loadBranchCatalog() throws MMPDaoException{
		InputStream in = null;
		try{
			in = new FileInputStream(this.path);
			return (BranchCatalog)new XMLBinding().read(in, BRANCH_XML_BINDING_PACKAGE, null);
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
	 * Inner method used to save Branch Catalog file
	 * 
	 * @param branchCatalog The branchCatalog instance to save
	 * @throws MMPDaoException
	 */
	private void saveBranchCatalog(BranchCatalog branchCatalog) throws MMPDaoException{
		OutputStream out = null;
		try{
			out = new FileOutputStream(this.path);
			new XMLBinding().write(branchCatalog, out, BRANCH_XML_BINDING_PACKAGE, null);
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
	 * Builds a Branch POJO object from a Branch XML object (JAXB)
	 * 
	 * @param branchXML	The Branch XML Object
	 * @return	The corresponding Branch POJO
	 */
	private Branch fromBranchXMLToBranchPOJO(BranchCatalog.Branch branchXML) throws MMPDaoException{
		Branch branchPOJO = new Branch();
		branchPOJO.setId(branchXML.getId());
		branchPOJO.setDefault(branchXML.isDefault());
		branchPOJO.setName(branchXML.getName());
		branchPOJO.setDescription(branchXML.getDescription());
		return branchPOJO;
	}
	
	/**
	 * Builds a Branch XML object (JAXB) from a Branch POJO
	 * @param branchPOJO The Branch POJO instance
	 * @return	The corresponding Branch XML instance (JAXB)
	 */
	private BranchCatalog.Branch fromBranchPOJOToBranchXML(Branch branchPOJO){
		BranchCatalog.Branch branchXML = new BranchCatalog.Branch();
		branchXML.setId(branchPOJO.getId());
		branchXML.setDefault(branchPOJO.isDefault());
		branchXML.setName(branchPOJO.getName());
		branchXML.setDescription(branchPOJO.getDescription());
		return branchXML;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}
}
