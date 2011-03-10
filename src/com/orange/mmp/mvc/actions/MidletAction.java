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


import com.opensymphony.xwork2.ActionSupport;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.data.Midlet;
import com.orange.mmp.dao.DaoManagerFactory;
import com.orange.mmp.midlet.MidletManager;

public class MidletAction extends ActionSupport {

	private static final long serialVersionUID = 1L;
	
	/**
	 * The uploaded PFM
	 */
	private File uploadedFile;
	
	private String uploadedFileContentType;// The content type of the file

	private String uploadedFileFileName;// The uploaded file name and path
	
	/**
	 * The Midlet list
	 */
	private Midlet[] midlets;
	
	/**
	 * PFM type
	 */
	private String type;
	
	
	/**
	 * ACTIONS
	 */
	
	/*
	 * (non-Javadoc)
	 * @see com.opensymphony.xwork2.ActionSupport#input()
	 */
	public String input() {
		if(getUploadedFile() == null) {
			addActionError(getText("error.midlet.add", new String[] {getText("error.midlet.nofile", new String[] {})}));
			return this.execute();
		}
		
		try {
			MidletManager.getInstance().deployMidlet(this.uploadedFile);
			addActionMessage(getText("message.midlet.add", new String[] {}));
		} catch (MMPException mmpe) {
			addActionError(getText("error.midlet.add", new String[] {mmpe.getLocalizedMessage()}));
		}
		
		return this.execute();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.opensymphony.xwork2.ActionSupport#execute()
	 */
	public String execute() {
		try {
			this.midlets = (Midlet[])DaoManagerFactory.getInstance().getDaoManager().getDao("midlet").list();
		} catch (MMPException mmpe) {
			addActionError(getText("error.midlet.list", new String[] {}));
		}
		
		return SUCCESS;
	}
	
	/**
	 * Remove a PFM
	 */
	@SuppressWarnings("unchecked")
	public String remove() {
		if(getType() == null) {
			addActionError(getText("error.midlet.remove.null", new String[] {}));
			return this.execute();
		}
		
		try {
			Midlet midletToDelete = new Midlet();
			midletToDelete.setType(type);
			DaoManagerFactory.getInstance().getDaoManager().getDao("midlet").delete(midletToDelete);
			addActionMessage(getText("message.midlet.remove", new String[] {}));
		} catch (MMPException mmpe) {
			addActionError(getText("error.midlet.remove", new String[] {mmpe.getLocalizedMessage()}));
		}
		return this.execute();
	}

	/**
	 * END ACTIONS
	 */
	
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
	 * @return the uploadedFileContentType
	 */
	public String getUploadedFileContentType() {
		return uploadedFileContentType;
	}

	/**
	 * @return the uploadedFileFileName
	 */
	public String getUploadedFileFileName() {
		return uploadedFileFileName;
	}

	/**
	 * @param uploadedFileContentType the uploadedFileContentType to set
	 */
	public void setUploadedFileContentType(String uploadedFileContentType) {
		this.uploadedFileContentType = uploadedFileContentType;
	}

	/**
	 * @param uploadedFileFileName the uploadedFileFileName to set
	 */
	public void setUploadedFileFileName(String uploadedFileFileName) {
		this.uploadedFileFileName = uploadedFileFileName;
	}

	/**
	 * @return the midlets
	 */
	public Midlet[] getMidlets() {
		return midlets;
	}

	/**
	 * @param midlets the midlets to set
	 */
	public void setMidlets(Midlet[] midlets) {
		this.midlets = midlets;
	}



}
