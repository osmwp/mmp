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
package com.orange.mmp.core.data;



/**
 * Widget abstraction class
 * 
 * @author tml
 *
 */
@SuppressWarnings("serial")
public class Widget extends Module {

	/**
	 * Branch Id
	 */
	private String branchId;
	
	public Widget() {
		super();
	}
	
	public Widget(Module module) {
		setCategory(module.getCategory());
		setId(module.getId());
		setLastModified(module.getLastModified());
		setLocation(module.getLocation());
		setName(module.getName());
		setVersion(module.getVersion());
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (this.getId()+this.branchId).hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Widget) {
			return (this.getId()+this.branchId).equals(((Widget)obj).getId()+((Widget)obj).branchId);
		}
		else return false;
	}
	
}
