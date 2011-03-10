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
package com.orange.mmp.sms;

import java.util.Properties;

import com.francetelecom.rd.bbtm.pfs.instantapi.sms.Response;
import com.orange.mmp.bind.BindingException;
import com.orange.mmp.bind.RESTClientBinding;
import com.orange.mmp.net.MMPNetException;

/**
 * SMS client for CASE Instant API
 * 
 * @author tml
 *
 */
public class InstantAPISMSHandler implements ISMSHandler {

    private Properties countryCodesList = null;
    
    private String endPointAddress = null;

    private String smsApiAccessKey = null;

    /* (non-Javadoc)
     * @see com.francetelecom.rd.bbtm.pfs.smsenabler.ISMSHandler#sendSMS(java.lang.String, java.lang.String, boolean)
     */
    public boolean sendSMS(String ND, String text, boolean useUCS2Support) {
	try{
        	RESTClientBinding<Response> responseBinding = new RESTClientBinding<Response>();
        	responseBinding.setBindingsPackage("com.francetelecom.rd.bbtm.pfs.instantapi.sms");
        	responseBinding.setEpr(this.endPointAddress);
        	responseBinding.setParameter("id", this.smsApiAccessKey);
        	responseBinding.setParameter("to", ND);
        	responseBinding.setParameter("content", text);
        	Response response = responseBinding.getResponse(null);
        	if(response != null && response.getStatus() != null && response.getStatus().getStatusCode() != null){
        	   return  response.getStatus().getStatusCode() == 200;
        	}
        	else return false;
	}catch(BindingException be) {
	    return false;
    }catch(MMPNetException mne) {
	    return false;
	}
	
    }
        
    /* (non-Javadoc)
     * @see com.francetelecom.rd.bbtm.pfs.smsenabler.ISMSHandler#getCountryCodesList()
     */
    public Properties getCountryCodesList() {
        return countryCodesList;
    }

    public void setCountryCodesList(Properties countryCodesList) {
        this.countryCodesList = countryCodesList;
    }

    public void setEndPointAddress(String endPointAddress) {
        this.endPointAddress = endPointAddress;
    }

    /**
     * @param smsApiAccessKey the smsApiAccessKey to set
     */
    public void setSmsApiAccessKey(String smsApiAccessKey) {
        this.smsApiAccessKey = smsApiAccessKey;
    }

   

}
