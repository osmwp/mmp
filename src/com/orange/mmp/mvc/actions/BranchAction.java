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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.opensymphony.xwork2.ActionSupport;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.data.Branch;
import com.orange.mmp.core.data.Mobile;
import com.orange.mmp.dao.DaoManagerFactory;

@SuppressWarnings("serial")
public class BranchAction extends ActionSupport {
	
	/**
	 * Fields for Branch creation/update
	 */
	private String name;
	
	private String id;
	
	private String description;
	
	/**
	 * The current branch
	 */
	private Branch currentBranch;
	
	/**
	 * List of branches
	 */
	private Branch[] branches;
	
	/**
	 * The mobiles associated to the default branch
	 */
	private Mobile[] defaultMobiles;
	
	/**
	 * The mobiles associated to the current branch
	 */
	private Mobile[] branchMobiles;
	
	/**
	 * Mobiles of the default branch to associate or not to the new/updated branch
	 */
	
	private String mobiles;
	
	private String mobilesChk;
	
	private String mobilesStr;
		
	private String branchMobilesChk;
	
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
	
	public String execute() throws Exception {
		initInput();		
		return super.execute();
	}
	
	@SuppressWarnings("unchecked")
	public String add() throws Exception {
		String name = getName();
		String id = getId();
		String description = getDescription();
				
		// Check values
		boolean hasErrors = false;
		if(id == null || id.equals("")) {
			hasErrors = true;
			addActionError(getText("error.branch.id.missing", new String[] {}));
		} else if(!Pattern.compile(com.orange.mmp.widget.Constants.BRANCH_ID_PATTERN).matcher(id).matches()) {
			hasErrors = true;
			addActionError(getText("error.branch.id.match", new String[] {com.orange.mmp.widget.Constants.BRANCH_ID_PATTERN}));
		} else {
			Branch branch = new Branch();
			branch.setId(id);
			Branch branchesResult[] = (Branch[])DaoManagerFactory.getInstance().getDaoManager().getDao("branch").find(branch);
			if(branchesResult.length > 0) {
				hasErrors = true;
				addActionError(getText("error.branch.id.existing", new String[] {id}));
			}
		}
		if(name == null || name.equals("")) {
			hasErrors = true;
			addActionError(getText("error.branch.name.missing", new String[] {}));
		}
		this.currentBranch = new Branch();
		this.currentBranch.setName(name);
		this.currentBranch.setId(id.toUpperCase());
		this.currentBranch.setDescription(description);

		// Get mobiles
		String[] mobilesChk = getMobilesChk().split(";");
		String[] mobiles = getMobiles().split(";");
		List<Mobile> branchMobiles = new ArrayList<Mobile>();
		if((mobilesChk != null && mobiles != null) 
				&& (mobilesChk.length > 0 && mobiles.length > 0)) {
			for (int i = 1; i < mobiles.length; i++) {
				if(mobilesChk[i].equals("1")) {
					// Update mobile with new branch
					try {
						Mobile mobile = new Mobile();
						mobile.setKey(mobiles[i]);
						Mobile[] mobilesResult = (Mobile[])DaoManagerFactory.getInstance().getDaoManager().getDao("mobile").find(mobile);
						if(mobilesResult.length > 0)
							branchMobiles.add(mobilesResult[0]);						
					} catch (MMPException mmpe) {
						// This mobile won't be associated to the branch, continue
						addActionError(getText("error.branch.addmobile", new String[] {mobiles[i], id, mmpe.getLocalizedMessage()}));
					}
				}
			}
			this.currentBranch.setMobiles(branchMobiles);
		}
		
		if(hasErrors) {
			return execute();
		}	
		
		try {
			DaoManagerFactory.getInstance().getDaoManager().getDao("branch").createOrUdpdate(this.currentBranch);
		} catch (MMPException mmpe) {
			addActionError(getText("error.branch.add", new String[] {}));
			return execute();
		}
		
		addActionMessage(getText("message.branch.add", new String[] {id}));
		
		// Empty form
		setCurrentBranch(null);
		
		return execute();
	}
	
	@SuppressWarnings("unchecked")
	public String display() throws Exception {
		String id = getId();
		if(id == null) {
			addActionError(getText("error.branch.display", new String[] {}));
			return this.execute();
		}
		
		this.currentBranch = new Branch();
		try {
			this.currentBranch.setId(id);
			Branch[] branchesResult = (Branch[])DaoManagerFactory.getInstance().getDaoManager().getDao("branch").find(this.currentBranch);
			if(branchesResult.length > 0)
				this.currentBranch = branchesResult[0];
			else
				throw new MMPException();
						
			// Mobiles without branch
			Branch defaultBranch = new Branch();
			defaultBranch.setDefault(true);
			Branch defaultbranchResult[] = (Branch[])DaoManagerFactory.getInstance().getDaoManager().getDao("branch").find(defaultBranch);
			if(defaultbranchResult.length == 0)
				throw new MMPException("No default branch found");
			defaultBranch = defaultbranchResult[0]; 
			Mobile mobile = new Mobile();
			mobile.setBranchId(defaultBranch.getId());
			this.defaultMobiles = (Mobile[])DaoManagerFactory.getInstance().getDaoManager().getDao("mobile").find(mobile);
			
			// Mobiles from this branch
			mobile.setBranchId(id);
			this.branchMobiles = (Mobile[])DaoManagerFactory.getInstance().getDaoManager().getDao("mobile").find(mobile);
			
		} catch (MMPException mmpe) {
			addActionError(getText("error.branch.display1", new String[] {id}));
		}
		setActionType("edit");
		
		return execute();
	}
	
	@SuppressWarnings("unchecked")
	public String edit() throws Exception {
		String prefix = getId();
		String name = getName();
		String description = getDescription();
		
		// Check
		if(name == null || name.equals("")) {
			addActionError(getText("error.branch.name.missing", new String[] {}));
			return this.execute();
		}
		
		// Delete and re-create branch if needed
		Branch checkBranch = new Branch();
		checkBranch.setId(id);
		Branch branchesResult[] = (Branch[])DaoManagerFactory.getInstance().getDaoManager().getDao("branch").find(checkBranch);
		if(branchesResult.length > 0)
			checkBranch = branchesResult[0];
		if(!checkBranch.getName().toLowerCase().equals(name.toLowerCase())) {
			// Check if this branch name already exists
			checkBranch = new Branch();
			checkBranch.setName(name);
			branchesResult = (Branch[])DaoManagerFactory.getInstance().getDaoManager().getDao("branch").find(checkBranch);
			if(branchesResult.length > 0){
				addActionError(getText("error.branch.name.existing", new String[] {name}));
				return this.execute();
			}
		}
		
		
		Branch defaultBranch = new Branch();
		defaultBranch.setDefault(true);
		Branch defaultBranchesResult[] = (Branch[])DaoManagerFactory.getInstance().getDaoManager().getDao("branch").find(defaultBranch);
		if(defaultBranchesResult.length == 0)
			throw new MMPException("No default branch found");
		defaultBranch = defaultBranchesResult[0];
		
		Branch modifiedBranch = new Branch();
		modifiedBranch.setName(name);
		modifiedBranch.setId(id);
		modifiedBranch.setDescription(description);
		List<Mobile> branchMobiles = new ArrayList<Mobile>();
		
		// Get Mobiles
		String[] mobilesChk = getMobilesChk().split(";");
		String[] mobiles = getMobiles().split(";");
		if((mobilesChk != null && mobiles != null) 
				&& (mobilesChk.length > 0 && mobiles.length > 0)) {
			for (int i = 1; i < mobiles.length; i++) {
				if(mobilesChk[i].equals("1")) {
					// Update mobile with new branch
					try {
						Mobile mobile = new Mobile();
						mobile.setKey(mobiles[i]);
						Mobile[] mobilesResult = (Mobile[])DaoManagerFactory.getInstance().getDaoManager().getDao("mobile").find(mobile);
						if(mobilesResult.length > 0)
							branchMobiles.add(mobilesResult[0]);						
					} catch (MMPException mmpe) {
						// This mobile won't be associated to the branch, continue
						addActionError(getText("error.branch.addmobile", new String[] {mobiles[i], id, mmpe.getLocalizedMessage()}));
					}
				}
			}
		}

		modifiedBranch.setMobiles(branchMobiles);

		// Get Mobiles previously associated to this Branch
		String[] branchMobilesStr = getMobilesStr().split(";");
		String[] branchMobilesChk = getBranchMobilesChk().split(";");
		for (int i = 1; i < branchMobilesStr.length; i++) {
			if(branchMobilesChk[i].equals("0")) {
				// Remove mobile from branch
				Mobile mobile = new Mobile();
				mobile.setKey(branchMobilesStr[i]);
				Mobile[] mobilesResult = (Mobile[])DaoManagerFactory.getInstance().getDaoManager().getDao("mobile").find(mobile);
				if(mobilesResult.length > 0) {
					mobile = mobilesResult[0];
					mobile.setBranchId(defaultBranch.getId());
					DaoManagerFactory.getInstance().getDaoManager().getDao("mobile").createOrUdpdate(mobile);
				}
			}
		}
				
		try {
			DaoManagerFactory.getInstance().getDaoManager().getDao("branch").createOrUdpdate(modifiedBranch);
		} catch (MMPException mmpe) {
			addActionError(getText("error.branch.modify", new String[] {prefix}));
			return this.execute();
		}
		
		addActionMessage(getText("message.branch.update", new String[] {prefix}));
		
		setActionType("add");
		
		return execute();
	}
	
	@SuppressWarnings("unchecked")
	public String remove() throws Exception {
		String id = getId();
		if (id != null) {
			try {
				Branch branch = new Branch();
				branch.setId(id);
				// List mobiles on this branch
				Mobile mobile = new Mobile();
				mobile.setBranchId(id);
				Mobile[] mobiles = (Mobile[]) DaoManagerFactory.getInstance().getDaoManager().getDao("mobile").find(mobile);
				if(mobiles != null && mobiles.length > 0) {
					Branch defaultBranch = new Branch();
					defaultBranch.setDefault(true);
					Branch[] defaultbranchResult = (Branch[])DaoManagerFactory.getInstance().getDaoManager().getDao("branch").find(defaultBranch);
					if(defaultbranchResult != null && defaultbranchResult.length > 0)
						defaultBranch = defaultbranchResult[0];
					else
						throw new MMPException("No default branch found");
					// Set branchId to Default
					for (Mobile currentMobile : mobiles) {
						currentMobile.setBranchId(defaultBranch.getId());
						DaoManagerFactory.getInstance().getDaoManager().getDao("mobile").createOrUdpdate(currentMobile);
					}
				}

				DaoManagerFactory.getInstance().getDaoManager().getDao("branch").delete(branch);
				addActionMessage(getText("message.branch.remove", new String[] {id}));
			} catch (Exception e) {
				addActionError(getText("error.branch.remove", new String[] {id, e.getLocalizedMessage()}));
			}
		} else {
			addActionError(getText("error.branch.remove1", new String[] {id}));
		}
		return execute();
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
		// Mobiles without branch
		Branch defaultBranch = new Branch();
		defaultBranch.setDefault(true);
		Branch branchesResult[] = (Branch[])DaoManagerFactory.getInstance().getDaoManager().getDao("branch").find(defaultBranch);
		if(branchesResult.length == 0)
			throw new MMPException("No default branch found");
		defaultBranch = branchesResult[0]; 
		Mobile mobile = new Mobile();
		mobile.setBranchId(defaultBranch.getId());
		this.defaultMobiles = (Mobile[])DaoManagerFactory.getInstance().getDaoManager().getDao("mobile").find(mobile);
		
		// List branches
		this.branches = (Branch[])DaoManagerFactory.getInstance().getDaoManager().getDao("branch").list();
		
		if(this.currentBranch != null){
			setName(this.currentBranch.getName());
			setId(this.currentBranch.getId());
			setDescription(this.currentBranch.getDescription());
		} else {
			setName("");
			setId("");
			setDescription("");
			
			mobile = new Mobile();
			mobile.setBranchId(id);
			this.branchMobiles = (Mobile[])DaoManagerFactory.getInstance().getDaoManager().getDao("mobile").find(mobile);
		}
	}
	
	/**
	 * GETTERS/SETTERS
	 */

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
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
	 * @return the mobileChk
	 */
	public String getMobilesChk() {
		return mobilesChk;
	}

	/**
	 * @return the branchMobileChk
	 */
	public String getBranchMobilesChk() {
		return branchMobilesChk;
	}

	/**
	 * @param mobileChk the mobileChk to set
	 */
	public void setMobilesChk(String mobileChk) {
		this.mobilesChk = mobileChk;
	}

	/**
	 * @param branchMobileChk the branchMobileChk to set
	 */
	public void setBranchMobilesChk(String branchMobileChk) {
		this.branchMobilesChk = branchMobileChk;
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
	 * @return the defaultMobiles
	 */
	public Mobile[] getDefaultMobiles() {
		return defaultMobiles;
	}

	/**
	 * @param defaultMobiles the defaultMobiles to set
	 */
	public void setDefaultMobiles(Mobile[] defaultMobiles) {
		this.defaultMobiles = defaultMobiles;
	}


	/**
	 * @return the mobilesStr
	 */
	public String getMobilesStr() {
		return mobilesStr;
	}

	/**
	 * @param mobilesStr the mobilesStr to set
	 */
	public void setMobilesStr(String mobilesStr) {
		this.mobilesStr = mobilesStr;
	}

	/**
	 * @return the branchMobiles
	 */
	public Mobile[] getBranchMobiles() {
		return branchMobiles;
	}

	/**
	 * @param branchMobiles the branchMobiles to set
	 */
	public void setBranchMobiles(Mobile[] branchMobiles) {
		this.branchMobiles = branchMobiles;
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
	 * @return the mobiles
	 */
	public String getMobiles() {
		return mobiles;
	}

	/**
	 * @param mobiles the mobiles to set
	 */
	public void setMobiles(String mobiles) {
		this.mobiles = mobiles;
	}


}
