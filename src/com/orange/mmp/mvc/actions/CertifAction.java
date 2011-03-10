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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.IOUtils;

import com.opensymphony.xwork2.ActionSupport;
import com.orange.mmp.midlet.MidletManager;
import com.orange.mmp.mvc.Constants;
import com.sun.midp.jadtool.AppDescriptor;

public class CertifAction extends ActionSupport {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Certificates list
	 */
	private List<X509Certificate> certifList;
	
	/**
	 * Uploaded certificate file
	 */
	private File uploadedFile;

	private String uploadedFileContentType;// The content type of the file

	private String uploadedFileFileName;// The uploaded file name and path
	
	
	/**
	 * ACTIONS
	 */
	
	@SuppressWarnings("unchecked")
	/* (non-Javadoc)
	 * @see com.opensymphony.xwork2.ActionSupport#execute()
	 */
	@Override
	public String execute() throws Exception {
		// List certificates
		try {
			Vector certs = MidletManager.getInstance().getCertificates();
			this.certifList = new ArrayList<X509Certificate>();
        	if(certs.size() > 0) {
        	    for (int i = 0; i < certs.size(); i++) {
	        		Object aobj[] = (Object[])(Object[])certs.elementAt(i);
	        		X509Certificate thisCert = (X509Certificate)aobj[AppDescriptor.CERT];
	        		certifList.add(thisCert);
        	    }
        	}
		} catch (Exception e) {
			addActionError(getText("error.certif.list", new String[] {e.getLocalizedMessage()}));
		}
		
		return super.execute();
	}
	
	public String input() throws Exception {
		if(getUploadedFile() == null) {
			addActionError(getText("error.certif.upload", new String[] {getText("error.certif.nofile", new String[] {})}));
			return this.execute();
		}
		
		if(!getUploadedFileContentType().equals(Constants.MIMETYPE_CERTIF)) {
			addActionError(getText("error.certif.upload", new String[] {getText("error.certif.format", new String[] {})}));
			return this.execute();
		}
		
		File dstFile = new File(MidletManager.getInstance().getKeystoreFile());
        // Try to create file
    	InputStream input = new FileInputStream(getUploadedFile());
        OutputStream output = new FileOutputStream(dstFile);
        try {
            IOUtils.copy(input, output);
            addActionMessage(getText("message.certif.upload", new String[] {}));
        } catch(IOException ioe) {
        	addActionError(getText("error.certif.upload", new String[] {ioe.getLocalizedMessage()}));
        } finally {
            input.close();
            output.close();
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
	 * @return the certifList
	 */
	public List<X509Certificate> getCertifList() {
		return certifList;
	}

	/**
	 * @param certifList the certifList to set
	 */
	public void setCertifList(List<X509Certificate> certifList) {
		this.certifList = certifList;
	}

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

}
