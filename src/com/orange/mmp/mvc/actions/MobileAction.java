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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionSupport;
import com.orange.mmp.bind.data.mobile.MobileCatalog;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.data.Branch;
import com.orange.mmp.core.data.JadAttributeAction;
import com.orange.mmp.core.data.Midlet;
import com.orange.mmp.core.data.Mobile;
import com.orange.mmp.dao.DaoManagerFactory;
import com.orange.mmp.dao.MMPDaoException;
import com.orange.mmp.midlet.MidletManager;

@SuppressWarnings("serial")
public class MobileAction extends ActionSupport {
		
	/**
	 * Settings Fields for Mobile creation/update
	 */
	private String key;
	
	private String userAgentKey;
	
	private String ua;
	
	private String branchId;
	
	private boolean dropped;
	
	private String dropDate;
	
	private String midletType;
	
	private boolean otaEnabled;
	
	private String uakey;
	
	private String finalname;
	
	private String codename;
	
	private String jadAttrs;
	
	private List<MobileCatalog.Mobile> mobileExtraList;
	
	/**
	 * Fields for input form initialization
	 */	
	private String[] types;
	
	/**
	 * Branch list
	 */
	private Branch[] branches;

	/**
	 * Mobile list
	 */
	private Mobile[] mobiles;
	
	/**
	 * Current branch
	 */
	private Branch currentBranch;
	
	/**
	 * Current Mobile
	 */
	private Mobile currentMobile;
	
	
	/**
	 * Zip mobile
	 */
	private String zipType;
	
	/**
	 * Current action
	 */
	private String actionType;
		

	
	/**
	 * ACTIONS
	 */

	/* (non-Javadoc)
	 * @see com.opensymphony.xwork2.ActionSupport#input()
	 */
	@Override
	public String input() throws Exception {
		initInput();
		return execute();
	}

	@Override
	public String execute() throws Exception {
		initInput();		
		return super.execute();
	}
		
	/**
	 * Add new mobile
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public String add() throws Exception {
		String mobileKey = getKey();
		String mobileType = getMidletType();
		String mobileShortUserAgent = getUa();
		String codeName = getCodename();
		String finalName = getFinalname();
		String branchId = getBranchId();
		boolean otaEnabled = isOtaEnabled();
		boolean dropped = isDropped();
		String dropDate = getDropDate();
		if(!dropped)
			dropDate = null;

		String[] jadAttributes = getJadAttrs().split("\n");
		
		// Check values
		boolean errors = false;
		if(mobileKey == null || mobileKey.length() == 0) {
			addActionError(getText("error.mobile.add", new String[] {"null", getText("error.mobile.uakey", new String[] {})}));
			errors = true;
		}
		if(finalName == null || finalName.length() == 0) {
			addActionError(getText("error.mobile.add", new String[] {"null", getText("error.mobile.finalname", new String[] {})}));
			errors = true;
		}
		if(mobileType == null || mobileType.length() == 0) {
			addActionError(getText("error.mobile.add", new String[] {mobileKey, getText("error.mobile.uakey", new String[] {})}));
			errors = true;
		}
		if(mobileShortUserAgent == null || mobileShortUserAgent.length() == 0) {
			addActionError(getText("error.mobile.add", new String[] {mobileKey, getText("error.mobile.uadiscr", new String[] {})}));
			errors = true;
		}
		if(!Pattern.compile("[a-zA-Z_0-9]+").matcher(mobileKey).matches()) {
			addActionError(getText("error.mobile.add", new String[] {mobileKey, getText("error.mobile.uakey.format", new String[] {})}));
			errors = true;
		}
		if(!Pattern.compile("[a-zA-Z_0-9.-]+").matcher(mobileType).matches()) {
			addActionError(getText("error.mobile.add", new String[] {mobileKey, getText("error.mobile.type.format", new String[] {})}));
			errors = true;
		}
		
		this.currentMobile = new Mobile();
		this.currentMobile.setKey(mobileKey);
		this.currentMobile.setMidletType(mobileType);
		this.currentMobile.setShortUserAgent(mobileShortUserAgent);
		this.currentMobile.setBranchId(branchId);

		if(dropDate != null && !dropDate.equals("")) {
			this.currentMobile.setMobileDropped(true);
			this.currentMobile.setDropDate(dropDate);
		} else {
			this.currentMobile.setMobileDropped(false);
			this.currentMobile.setDropDate("");
		}
		this.currentMobile.setOtaEnabled(otaEnabled);
		this.currentMobile.setCodeName((codeName != null && codeName.length() > 0) ? codeName : null);
		this.currentMobile.setFinalName(finalName);
		if(getJadAttrs().length() > 0)
			this.currentMobile.setJADAttributesActions(getJadAttrList(jadAttributes));
		else
			this.currentMobile.setJADAttributesActions(new ArrayList<JadAttributeAction>());
			
		if(errors)
			return execute();
		
		try {
			DaoManagerFactory.getInstance().getDaoManager().getDao("mobile").createOrUdpdate(this.currentMobile);
			addActionMessage(getText("message.mobile.add", new String[] {mobileKey}));
		} catch (MMPDaoException mde) {
			addActionError(getText("error.mobile.add", new String[] {mobileKey, mde.getMessage()}));
		}
		
		this.currentMobile = null;
		
		return execute();
	}
	
	/**
	 * Display mobile info
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public String display() throws Exception {
		String mobileKey = getKey();
		if(mobileKey == null) {
			addActionError(getText("error.mobile.display1", new String[] {}));
		}
		
		this.currentMobile = new Mobile();
		this.currentMobile.setKey(mobileKey);
		try {
			Mobile mobiles[] = (Mobile[])DaoManagerFactory.getInstance().getDaoManager().getDao("mobile").find(this.currentMobile);
			if(mobiles.length == 0) throw new MMPException("Mobile not found");
			this.currentMobile = mobiles[0];
		
			setActionType("edit");
			
		} catch (MMPException mmpe) {
			addActionError(getText("error.mobile.display", new String[] {mobileKey, mmpe.getMessage()}));
		}
		
		return this.execute();
	}
	
	/**
	 * Save mobile modifications
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public String edit() throws Exception {
		String mobileKey = getKey();
		String mobileType = getMidletType();
		String mobileShortUserAgent = getUa();
		String mobileCodeName = getCodename();
		String mobileFinalName = getFinalname();
		String branchId = getBranchId();
		
		boolean dropped = isDropped();
		String dropDate = getDropDate();
		if(!dropped)
			dropDate = null;
		Boolean otaEnabled = isOtaEnabled();
		
		// Check values
		boolean errors = false;
		if(mobileType == null || mobileType.length() == 0) {
			addActionError(getText("error.mobile.update", new String[] {mobileKey, getText("error.mobile.uakey", new String[] {})}));
			errors = true;
		}
		if(mobileFinalName == null || mobileFinalName.length() == 0) {
			addActionError(getText("error.mobile.add", new String[] {"null", getText("error.mobile.finalname", new String[] {})}));
			errors = true;
		}
		if(mobileShortUserAgent == null || mobileShortUserAgent.length() == 0) {
			addActionError(getText("error.mobile.update", new String[] {mobileKey, getText("error.mobile.uadiscr", new String[] {})}));
			errors = true;
		}
		if(!Pattern.compile("[a-zA-Z_0-9.-]+").matcher(mobileType).matches()) {
			addActionError(getText("error.mobile.update", new String[] {mobileKey, getText("error.mobile.type.format", new String[] {})}));
			errors = true;
		}
		

		String[] jadAttributes = getJadAttrs().split("\n");
		
		// Build mobile
		this.currentMobile = new Mobile();
		this.currentMobile.setKey(mobileKey);
		this.currentMobile.setMidletType(mobileType);
		this.currentMobile.setShortUserAgent(mobileShortUserAgent);
		this.currentMobile.setCodeName((mobileCodeName != null && mobileCodeName.length() > 0) ? mobileCodeName : null);
		this.currentMobile.setFinalName(mobileFinalName);
		this.currentMobile.setBranchId(branchId);
		if(dropDate != null && !dropDate.equals("")) {
			this.currentMobile.setMobileDropped(true);
			this.currentMobile.setDropDate(dropDate);
		} else {
			this.currentMobile.setMobileDropped(false);
			this.currentMobile.setDropDate("");
		}
		this.currentMobile.setOtaEnabled(otaEnabled);
		this.currentMobile.setJADAttributesActions(getJadAttrList(jadAttributes));
		
		if(errors)
			return execute();
		
		try {
			DaoManagerFactory.getInstance().getDaoManager().getDao("mobile").createOrUdpdate(this.currentMobile);
			addActionMessage(getText("message.mobile.update", new String[] {mobileKey}));
		} catch (MMPDaoException mde) {
			addActionError(getText("error.mobile.update", new String[] {mobileKey, mde.getMessage()}));
		}
		
		this.currentMobile = null;
		
		setActionType("add");
		return execute();
	}
	
	/**
	 * Delete mobile
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public String remove() throws Exception {
		String mobileKey = getKey();
		if(mobileKey != null) {
			try {
				Mobile mobile = new Mobile();
				mobile.setKey(mobileKey);
				Mobile mobiles[] = (Mobile[])DaoManagerFactory.getInstance().getDaoManager().getDao("mobile").find(mobile);
				if(mobiles.length == 0) throw new MMPException("Mobile not found");
				mobile = mobiles[0];
				DaoManagerFactory.getInstance().getDaoManager().getDao("mobile").delete(mobile);
				addActionMessage(getText("message.mobile.remove", new String[] {mobileKey}));
			} catch (Exception e) {
				addActionError(getText("error.mobile.remove", new String[] {mobileKey, e.getLocalizedMessage()}));
			}
		} else {
			addActionError(getText("error.mobile.remove1", new String[] {mobileKey}));
		}
		
		return execute();
	}
	
	/**
	 * Build a zip for this mobile
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public String zip() throws Exception {
		String mobileKey = getKey();
		String type = getZipType();
		if(type == null || mobileKey == null) {
			addActionError(getText("error.mobile.zip", new String[] {}));
			return this.input();
		}
		
		Boolean signing = null;
		if (type.equals("signed"))
			signing = true;
		else if (type.equals("unsigned"))
			signing = false;
		
		try {
			// Retrieving mobile
			Mobile mobile = new Mobile();
			mobile.setKey(mobileKey);
			Mobile[] mobiles = (Mobile[]) DaoManagerFactory.getInstance().getDaoManager().getDao("mobile").find(mobile);
			if(mobiles != null && mobiles.length > 0)
				mobile = mobiles[0];
			
			// Build ZIP
			ByteArrayOutputStream jadOS = new ByteArrayOutputStream();
		
			HttpServletResponse response = ServletActionContext.getResponse();
			MidletManager midletManager = MidletManager.getInstance();
			String zipFilename = midletManager.computeZip(mobile, signing, jadOS);
			response.setHeader(com.orange.mmp.mvc.Constants.HTTP_HEADER_CONTENDISPOSITION,
					"attachment; filename=\"" + zipFilename + ".zip\"");
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType(com.orange.mmp.mvc.Constants.HTTP_HEADER_ZIPCONTENT);
			response.setContentLength(jadOS.size());
			jadOS.writeTo(response.getOutputStream());
			response.reset();
		} catch (IOException ioe) {
			String details = ioe.getCause() == null ? null : ioe.getCause().getMessage();
			addActionError(getText("error.mobile.zip.ioe", new String[] {mobileKey, ioe.getMessage(), (details == null ? "" : " [" + details + "]")}));
			return this.input();
		} catch (MMPException mmpe) {
			addActionError(getText("error.mobile.zip.mmpe", new String[] {mobileKey, mmpe.getMessage()}));
			return this.input();
		}
		return SUCCESS;
	}
			
	/**
	 * END ACTIONS
	 */
	
	/**
	 * Init form fields
	 * @throws MMPException
	 */
	@SuppressWarnings("unchecked")
	private void initInput() throws MMPException {
		Midlet midlets[] = (Midlet[])DaoManagerFactory.getInstance().getDaoManager().getDao("midlet").list();
		this.types = new String[midlets.length];
		int typeIndex = 0;
		for (Midlet midlet: midlets){
			this.types[typeIndex++] = midlet.getType();
		}
		
		this.branches = (Branch[])DaoManagerFactory.getInstance().getDaoManager().getDao("branch").list();
		
		this.mobiles = (Mobile[])DaoManagerFactory.getInstance().getDaoManager().getDao("mobile").list();
		
		//Edit form
		if(this.currentMobile != null){
			setKey(this.currentMobile.getKey());
			setUa(this.currentMobile.getShortUserAgent());
			setCodename(this.currentMobile.getCodeName());
			setFinalname(this.currentMobile.getFinalName());
			setMidletType(this.currentMobile.getMidletType());
			setBranchId(this.currentMobile.getBranchId());
			setDropped(this.currentMobile.isMobileDropped());
			setDropDate(this.currentMobile.getDropDate());
			setOtaEnabled(this.currentMobile.getOtaEnabled());
			// Build JAD lines
			StringBuilder jadAttributes = new StringBuilder();
			JadAttributeAction[] jadAttrList = this.currentMobile.getJadAttributeActions();
			for (JadAttributeAction jadAttr : jadAttrList) {
				jadAttributes.append(jadAttr.getAction()).append(":");
				jadAttributes.append(jadAttr.getAttribute()).append("=");
				jadAttributes.append(jadAttr.getValue()).append(";");
				jadAttributes.append("injad=");
				jadAttributes.append(jadAttr.getInJad()).append(";");
				jadAttributes.append("inmf=");
				jadAttributes.append(jadAttr.getInManifest()).append(";");
				if(jadAttr.isStrict())
					jadAttributes.append("strict;");
				jadAttributes.append("\n");
			}
			setJadAttrs(jadAttributes.toString());
		
			this.currentBranch = new Branch();
			this.currentBranch.setId(this.currentMobile.getBranchId());
		}
		//Empty form
		else{
			setUa("");
			setKey("");
			setCodename("");
			setFinalname("");
			setJadAttrs("");
			setDropped(false);
			setDropDate("");
			setBranchId("");
			
			this.currentBranch = new Branch();
			this.currentBranch.setDefault(true);
		}
		
		 Branch []branchResults = (Branch[])DaoManagerFactory.getInstance().getDaoManager().getDao("branch").find(this.currentBranch);
		 this.currentBranch = branchResults[0];
				
	}
	
	private List<JadAttributeAction> getJadAttrList(String[] jadAttributes) throws IOException {
		List<JadAttributeAction> jadAttrList = new ArrayList<JadAttributeAction>();
		for(String currentAttr : jadAttributes) {
			currentAttr = currentAttr.trim();
			if(currentAttr.length() > 0){
				String[] tmpAttr = currentAttr.split(";");
				String action = tmpAttr[0].split(":")[0];
				tmpAttr[0] = tmpAttr[0].substring(action.length()+1);
				String attribute = tmpAttr[0].split("=")[0];
				String value = tmpAttr[0].substring(attribute.length()+1);
				MobileCatalog.Mobile.JadAttributes.JadAction jadAction = new MobileCatalog.Mobile.JadAttributes.JadAction();
				jadAction.setAction(action);
				jadAction.setAttribute(attribute);
				jadAction.setValue(value);
				String[] inJad = tmpAttr[1].split("=");
				if(inJad.length == 2 && inJad[0].equals("injad")) {
					jadAction.setInJad(inJad[1]);
				} else {
					jadAction.setInJad(JadAttributeAction.ApplyCase.NEVER.toString());
				}
				String[] inMf = tmpAttr[2].split("=");
				if(inMf.length == 2 && inMf[0].equals("inmf")) {
					jadAction.setInMF(inMf[1]);
				} else {
					jadAction.setInMF(JadAttributeAction.ApplyCase.NEVER.toString());
				}
				
				if(tmpAttr.length > 3) {
					currentAttr = currentAttr.substring(currentAttr.indexOf(";"));
					if(currentAttr.contains("strict"))
						jadAction.setStrict(true);
					else
						jadAction.setStrict(false);
				}
				JadAttributeAction jadAttr = new JadAttributeAction();
				jadAttr.setAction(JadAttributeAction.actionFromString(jadAction.getAction()));
				jadAttr.setAttribute(jadAction.getAttribute());
				jadAttr.setValue(jadAction.getValue());
				jadAttr.setStrict(jadAction.isStrict());
				jadAttr.setInJad(JadAttributeAction.applyCaseFromString(jadAction.getInJad()));
				jadAttr.setInManifest(JadAttributeAction.applyCaseFromString(jadAction.getInMF()));
				jadAttrList.add(jadAttr);
			}
		}
		return jadAttrList;
	}
		
	/**
	 * GETTERS/SETTERS
	 */
	/**
	 * @return the ua
	 */
	public String getUa() {
		return ua;
	}

	/**
	 * @param ua the ua to set
	 */
	public void setUa(String ua) {
		this.ua = ua;
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
	 * @return the dropped
	 */
	public boolean isDropped() {
		return dropped;
	}

	/**
	 * @param dropped the dropped to set
	 */
	public void setDropped(boolean dropped) {
		this.dropped = dropped;
	}

	/**
	 * @return the midletType
	 */
	public String getMidletType() {
		return midletType;
	}

	/**
	 * @param midletType the midletType to set
	 */
	public void setMidletType(String midletType) {
		this.midletType = midletType;
	}

	/**
	 * @return the dropDate
	 */
	public String getDropDate() {
		return dropDate;
	}

	/**
	 * @param dropDate the dropDate to set
	 */
	public void setDropDate(String dropDate) {
		this.dropDate = dropDate;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the actionType
	 */
	public String getActionType() {
		return actionType;
	}

	/**
	 * @param actionType the actionType to set
	 */
	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	/**
	 * @return the zipType
	 */
	public String getZipType() {
		return zipType;
	}

	/**
	 * @param zipType the zipType to set
	 */
	public void setZipType(String zipType) {
		this.zipType = zipType;
	}

	/**
	 * @return the uakey
	 */
	public String getUakey() {
		return uakey;
	}

	/**
	 * @return the finalname
	 */
	public String getFinalname() {
		return finalname;
	}

	/**
	 * @return the codename
	 */
	public String getCodename() {
		return codename;
	}

	/**
	 * @param uakey the uakey to set
	 */
	public void setUakey(String uakey) {
		this.uakey = uakey;
	}

	/**
	 * @param finalname the finalname to set
	 */
	public void setFinalname(String finalname) {
		this.finalname = finalname;
	}

	/**
	 * @param codename the codename to set
	 */
	public void setCodename(String codename) {
		this.codename = codename;
	}

	/**
	 * @return the mobileExtraList
	 */
	public List<MobileCatalog.Mobile> getMobileExtraList() {
		return mobileExtraList;
	}

	/**
	 * @param mobileExtraList the mobileExtraList to set
	 */
	public void setMobileExtraList(List<MobileCatalog.Mobile> mobileExtraList) {
		this.mobileExtraList = mobileExtraList;
	}

	/**
	 * @return the userAgentKey
	 */
	public String getUserAgentKey() {
		return userAgentKey;
	}

	/**
	 * @param userAgentKey the userAgentKey to set
	 */
	public void setUserAgentKey(String userAgentKey) {
		this.userAgentKey = userAgentKey;
	}

	/**
	 * @return the jadAttrs
	 */
	public String getJadAttrs() {
		return jadAttrs;
	}

	/**
	 * @param jadAttrs the jadAttrs to set
	 */
	public void setJadAttrs(String jadAttrs) {
		this.jadAttrs = jadAttrs;
	}

	/**
	 * @return the otaEnabled
	 */
	public boolean isOtaEnabled() {
		return otaEnabled;
	}

	/**
	 * @param otaEnabled the otaEnabled to set
	 */
	public void setOtaEnabled(boolean otaEnabled) {
		this.otaEnabled = otaEnabled;
	}

	/**
	 * @return the types
	 */
	public String[] getTypes() {
		return types;
	}

	/**
	 * @param types the types to set
	 */
	public void setTypes(String[] types) {
		this.types = types;
	}

	/**
	 * @return the mobiles
	 */
	public Mobile[] getMobiles() {
		return mobiles;
	}

	/**
	 * @param mobiles the mobiles to set
	 */
	public void setMobiles(Mobile[] mobiles) {
		this.mobiles = mobiles;
	}

	/**
	 * @return the branches
	 */
	public Branch[] getBranches() {
		return branches;
	}

	/**
	 * @param branches the branches to set
	 */
	public void setBranches(Branch[] branches) {
		this.branches = branches;
	}


	/**
	 * @return the currentBranch
	 */
	public Branch getCurrentBranch() {
		return currentBranch;
	}

	/**
	 * @param currentBranch the currentBranch to set
	 */
	public void setCurrentBranch(Branch currentBranch) {
		this.currentBranch = currentBranch;
	}

	/**
	 * @return the currentMobile
	 */
	public Mobile getCurrentMobile() {
		return currentMobile;
	}

	/**
	 * @param currentMobile the currentMobile to set
	 */
	public void setCurrentMobile(Mobile currentMobile) {
		this.currentMobile = currentMobile;
	}

}
