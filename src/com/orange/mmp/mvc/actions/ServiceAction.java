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

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import com.opensymphony.xwork2.ActionSupport;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.data.Service;
import com.orange.mmp.dao.DaoManagerFactory;
import com.orange.mmp.service.ServiceManager;

public class ServiceAction extends ActionSupport {

	private static final long serialVersionUID = 1L;

	
	/**
	 * Form input fields
	 */
	private String id;
		
	private String hostname;
	
	private boolean homepage;
	
	private boolean usedef;
	
	private boolean signing;
	
	private boolean wtheaders;
	
	private boolean compactjad;
		
	private String jadLine;
	
	private boolean isDefault;
	
	/**
	 * The Service list
	 */
	private Service[] serviceList;
	
	/**
	 * Other fields
	 */
	// Current action
	private String actionType;
	
	/**
	 * ACTIONS
	 */
	
	/* (non-Javadoc)
	 * @see com.opensymphony.xwork2.ActionSupport#execute()
	 */
	@Override
	public String execute() throws Exception {
		this.serviceList = (Service[])DaoManagerFactory.getInstance().getDaoManager().getDao("service").list();
		
		return SUCCESS;
	}
	
	@SuppressWarnings("unchecked")
	public String add() throws Exception {
		try {
			Service newService = new Service();
			newService.setId(getId());
			newService.setHostname(getHostname());
			newService.setUsedefault(isUsedef());
			newService.setHomepage(isHomepage());
			newService.setSigned(isSigning());
			newService.setWtheadersbymemo(isWtheaders());
			newService.setCompactjadentries(isCompactjad());
			newService.setIsDefault(isIsDefault());
			
			// JadEntries
			String[] jadLineList = getJadLine().split("\r\n");
			Map<String, String> jadEntries = new HashMap<String, String>();
			for (String currentJadEntry : jadLineList) {
				String[] completeJad = currentJadEntry.split("=");
				if(completeJad.length == 2) {
					jadEntries.put(completeJad[0], completeJad[1]);
				}
			}
			newService.setJadEntries(jadEntries);
							
			if (getHostname() == null && !getHostname().equals("")) {
				addActionError(getText("error.service.add", new String[] {getText("error.service.badhostname", new String[] {})}));
				return this.execute();
			} 
			if(getId().equals("") || getHostname().equals("")) {
				addActionError(getText("error.service.add", new String[] {getText("error.service.endpoint", new String[] {})}));
				return this.execute();
			}	
						
			DaoManagerFactory.getInstance().getDaoManager().getDao("service").createOrUdpdate(newService);
			
			addActionMessage(getText("message.service.add", new String[] {newService.getId()}));
			
		} catch (MalformedURLException mue) {
			addActionError(getText("error.service.add", new String[] {mue.getLocalizedMessage()}));
		} catch (MMPException mmpe) {
			addActionError(getText("error.service.add", new String[] {mmpe.getLocalizedMessage()}));
		}
		
		// Empty all the fields
		emptyFields();
		
		return this.execute();
	}
	
	public String display() throws Exception {
		if(getId() == null || getId().equals("")) {
			addActionError(getText("error.service.display", new String[] {"ID = \"\""}));
		}
		
		try {
			Service service = ServiceManager.getInstance().getServiceById(getId());
			// Initialize form fields
			setId(getId());
			setHostname(service.getHostname());
			setIsDefault(service.getIsDefault());
			setUsedef(service.getUsedefault());
			setHomepage((service.getHomepage() != null) ? service.getHomepage() : false );
			setSigning(service.getSigned());
			setWtheaders((service.getWtheadersbymemo() != null) ? service.getWtheadersbymemo() : false );
			setCompactjad((service.getCompactjadentries() != null) ? service.getCompactjadentries() : false );
					
			// JadEntries
			StringBuffer jadBuffer = new StringBuffer("");
			if (service.getJadEntries() != null) {
				for(String key : service.getJadEntries().keySet()){
					jadBuffer.append(key).append("=");
					jadBuffer.append(service.getJadEntries().get(key)).append("\r\n");
				}
			}
			setJadLine(jadBuffer.toString());
			
		} catch (MMPException mmpe) {
			addActionError(getText("error.service.display", new String[] {mmpe.getLocalizedMessage()}));
			emptyFields();
		}
		setActionType("edit");
		return this.execute();
	}
	
	@SuppressWarnings("unchecked")
	public String edit() throws Exception {
		if(getId() == null || getId().equals("")) {
			addActionError(getText("error.service.modify", new String[] {"ID = \"\""}));
			return this.execute();
		}
		try {
			Service service = new Service();
			service.setId(getId());
			service.setHostname(getHostname());
			service.setIsDefault(isIsDefault());
			service.setUsedefault(isUsedef());
			service.setHomepage(isHomepage());
			service.setSigned(isSigning());
			service.setWtheadersbymemo(isWtheaders());
			service.setCompactjadentries(isCompactjad());
			// JadEntries
			String[] jadLineList = getJadLine().split("\r\n");
			Map<String, String> jadEntries = new HashMap<String, String>();
			for (String currentJadEntry : jadLineList) {
				String[] completeJad = currentJadEntry.split("=");
				if(completeJad.length == 2) {
					jadEntries.put(completeJad[0], completeJad[1]);
				}
			}
			service.setJadEntries(jadEntries);
	
			// Endpoint analysis
			if (getHostname() == null && !getHostname().equals("")) {
				addActionError(getText("error.service.modify", new String[] {getText("error.service.badhostname", new String[] {})}));
				return this.execute();
			} 
			if(getId().equals("") || getHostname().equals("")) {
				addActionError(getText("error.service.modify", new String[] {getText("error.service.endpoint", new String[] {})}));
				return this.execute();
			}	
		
			DaoManagerFactory.getInstance().getDaoManager().getDao("service").createOrUdpdate(service);
		
		} catch (MalformedURLException mue) {
			addActionError(getText("error.service.modify", new String[] {mue.getLocalizedMessage()}));
		} catch (MMPException mmpe) {
			addActionError(getText("error.service.modify", new String[] {mmpe.getLocalizedMessage()}));
		}
		
		addActionMessage(getText("message.service.update", new String[] {getId()}));
		
		setActionType("");
		emptyFields();
		return this.execute();
	}
	
	@SuppressWarnings("unchecked")
	public String remove() throws Exception {
		if(getId() == null || getId().equals("")) {
			addActionError(getText("error.service.remove", new String[] {"ID = \"\""}));
			return this.execute();
		}
		
		Service[] services = (Service[]) DaoManagerFactory.getInstance().getDaoManager().getDao("service").list();
		if (services.length == 1) {
			// Error: this is the only service, cannot be deleted
			addActionError(getText("error.service.remove.only", new String[] {getId()}));
			setId("");
			return this.execute();
		}
		
		try {
			Service service = ServiceManager.getInstance().getServiceById(getId());
			DaoManagerFactory.getInstance().getDaoManager().getDao("service").delete(service);
		} catch (MMPException mmpe) {
			addActionError(getText("error.service.remove", new String[] {mmpe.getLocalizedMessage()}));
		}
		
		addActionMessage(getText("message.service.remove", new String[] {getId()}));
		
		setId("");
		return this.execute();
	}
	
	/**
	 * END ACTIONS
	 */
	
	/**
	 * Empty all form fields
	 */
	private void emptyFields() {
		setId("");
		setHostname("");
		setUsedef(false);
		setHomepage(false);
		setSigning(false);
		setWtheaders(false);
		setCompactjad(false);
		setHostname("");
		setJadLine("");
	}
	
	/**
	 * GETTERS/SETTERS
	 */

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the hostname
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * @return the homepage
	 */
	public boolean isHomepage() {
		return homepage;
	}

	/**
	 * @return the usedef
	 */
	public boolean isUsedef() {
		return usedef;
	}

	/**
	 * @return the signing
	 */
	public boolean isSigning() {
		return signing;
	}

	/**
	 * @return the wtheaders
	 */
	public boolean isWtheaders() {
		return wtheaders;
	}

	/**
	 * @return the compactjad
	 */
	public boolean isCompactjad() {
		return compactjad;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param hostname the hostname to set
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	/**
	 * @param homepage the homepage to set
	 */
	public void setHomepage(boolean homepage) {
		this.homepage = homepage;
	}

	/**
	 * @param usedef the usedef to set
	 */
	public void setUsedef(boolean usedef) {
		this.usedef = usedef;
	}

	/**
	 * @param signing the signing to set
	 */
	public void setSigning(boolean signing) {
		this.signing = signing;
	}

	/**
	 * @param wtheaders the wtheaders to set
	 */
	public void setWtheaders(boolean wtheaders) {
		this.wtheaders = wtheaders;
	}

	/**
	 * @param compactjad the compactjad to set
	 */
	public void setCompactjad(boolean compactjad) {
		this.compactjad = compactjad;
	}

	/**
	 * @return the serviceList
	 */
	public Service[] getServiceList() {
		return serviceList;
	}

	/**
	 * @param serviceList the serviceList to set
	 */
	public void setServiceList(Service[] serviceList) {
		this.serviceList = serviceList;
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
	 * @return the jadLine
	 */
	public String getJadLine() {
		return jadLine;
	}

	/**
	 * @param jadLine the jadLine to set
	 */
	public void setJadLine(String jadLine) {
		this.jadLine = jadLine;
	}

	/**
	 * @return the isDefault
	 */
	public boolean isIsDefault() {
		return isDefault;
	}

	/**
	 * @param isDefault the isDefault to set
	 */
	public void setIsDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

}
