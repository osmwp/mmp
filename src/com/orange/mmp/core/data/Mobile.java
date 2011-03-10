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

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;

/**
 * Mobile handset abstraction class
 * @author milletth
 *
 */
@SuppressWarnings("serial")
public class Mobile implements Comparator<Mobile>, Serializable {

	/**
     * Mobile inner key (id)
     */
    private String key;

    /**
     * Disrciminating part of the user agent
     */
    private String shortUserAgent;

    /**
     * Full user agent description
     */
    private String userAgent;

    /**
     * The type of the midlet
     */
    private String midletType;
    
	/**
	 * Final name
	 */
	private String finalName;
	
	/**
	 * Code name
	 */
	private String codeName;
	
	/**
	 * Special JAD attributes actions
	 */
	private Collection<JadAttributeAction> jadAttrsActions;
	
	/**
	 * The prefix (major version number) of the branch to which the mobile belongs
	 */
	private String branchId;
	
	/**
	 * Boolean indicating if the Mobile has been dropped
	 */
	private boolean mobileDropped;
	
	/**
	 * The Mobile drop date
	 */
	private String dropDate;
	
	/**
	 * Boolean otaEnabled if the Mobile is enabled for OTA install
	 */
	private boolean otaEnabled;

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
     * @return the shortUserAgent
     */
    public String getShortUserAgent() {
        return shortUserAgent;
    }

    /**
     * @param shortUserAgent the shortUserAgent to set
     */
    public void setShortUserAgent(String shortUserAgent) {
        this.shortUserAgent = shortUserAgent;
    }

    /**
     * @return the userAgent
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * @param userAgent the userAgent to set
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
	 * @return the finalName
	 */
	public String getFinalName() {
		return finalName;
	}

	/**
	 * @return the codeName
	 */
	public String getCodeName() {
		return codeName;
	}

	/**
	 * @return the branchId
	 */
	public String getBranchId() {
		return branchId;
	}

	/**
	 * @return the mobileDropped
	 */
	public boolean isMobileDropped() {
		return mobileDropped;
	}

	/**
	 * @return the dropDate
	 */
	public String getDropDate() {
		return dropDate;
	}

	/**
	 * @param finalName the finalName to set
	 */
	public void setFinalName(String finalName) {
		this.finalName = finalName;
	}

	/**
	 * @param codeName the codeName to set
	 */
	public void setCodeName(String codeName) {
		this.codeName = codeName;
	}

	/**
	 * @param branchId the branchId to set
	 */
	public void setBranchId(String branchId) {
		this.branchId = branchId;
	}

	/**
	 * @param mobileDropped the mobileDropped to set
	 */
	public void setMobileDropped(boolean mobileDropped) {
		this.mobileDropped = mobileDropped;
	}

	/**
	 * @param dropDate the dropDate to set
	 */
	public void setDropDate(String dropDate) {
		this.dropDate = dropDate;
	}
	
	/**
	 * @return the otaEnabled
	 */
	public boolean getOtaEnabled() {
		return otaEnabled;
	}

	/**
	 * @param otaEnabled the otaEnabled to set
	 */
	public void setOtaEnabled(boolean otaEnabled) {
		this.otaEnabled = otaEnabled;
	}

	public JadAttributeAction[] getJadAttributeActions() {
		if(this.jadAttrsActions == null) return new JadAttributeAction[0];
		return this.jadAttrsActions.toArray(new JadAttributeAction[this.jadAttrsActions.size()]);
	}

	public void setJADAttributesActions(Collection<JadAttributeAction> jadAttrsActions) {
		this.jadAttrsActions = jadAttrsActions;
	}
	
	/*********** Comparator Impl ***********/

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Mobile mobile1, Mobile mobile2) {
		if(mobile1.getKey() != null && mobile2.getKey() != null){
		    return mobile1.getKey().compareTo(mobile2.getKey());
		}
		else return 0;
    }
}
