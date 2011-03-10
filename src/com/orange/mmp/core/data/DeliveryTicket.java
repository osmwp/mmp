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

import java.util.Map;

/**
 * Abstraction class for delivery ticket
 * @author milletth
 *
 */
public class DeliveryTicket {

    /**
     * The ticket ID
     */
    protected String id;

    /**
     * The user's mobile phone number
     */
    protected String msisdn;

    /**
     * The application ID linked to this ticket
     */
    protected String serviceId;

    /**
     * The timestamp of the ticket
     */
    protected long creationDate;

    /**
     * The midlet type
     */
    protected String type;

    /**
     * The service specific parameters
     */
    protected Map<String,String> serviceSpecific;

    /**
     * The user agent key for current ticket
     */
    protected String uaKey;
    
    /**
     * Callback method
     */
    protected String callback;

    /**
     * @return the nD
     */
    public String getMsisdn() {
        return msisdn;
    }

    /**
     * @param nd the nD to set
     */
    public void setMsisdn(String msisdn) {
       this.msisdn = msisdn;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the creationDate
     */
    public long getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate the creationDate to set
     */
    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @return the serviceId
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * @param serviceId the service ID to set
     */
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
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
     * @return the serviceSpecific
     */
    public Map<String, String> getServiceSpecific() {
        return serviceSpecific;
    }

    /**
     * @param serviceSpecific the serviceSpecific to set
     */
    public void setServiceSpecific(Map<String,String> serviceSpecific) {
        this.serviceSpecific = serviceSpecific;
    }

    /**
     * @return the uaKey
     */
    public String getUaKey() {
        return uaKey;
    }

    /**
     * @param uaKey the uaKey to set
     */
    public void setUaKey(String uaKey) {
        this.uaKey = uaKey;
    }

	/**
	 * @return the callback
	 */
	public String getCallback() {
		return callback;
	}

	/**
	 * @param callback the callback to set
	 */
	public void setCallback(String callback) {
		this.callback = callback;
	}
}
