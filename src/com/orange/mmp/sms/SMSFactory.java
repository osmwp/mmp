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

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.orange.mmp.core.ApplicationListener;
import com.orange.mmp.core.MMPException;

/**
 * Abstract Factory used to send SMS 
 * 
 * @author TML
 */
public class SMSFactory implements ApplicationListener{

	/**
	 * Singleton for access outside application context
	 */
	private static SMSFactory smsFactorySingleton;
	
	/**
	 * List of SMS Handlers
	 */
    private List<ISMSHandler> smsHandlers;

    /**
     * List of languages using UCS2 SMS support
     */
    private List<String> ucs2Languages;

    /**
	* Singleton access 
	* 
	* @return The SMSFactory singleton
	*/
	public static SMSFactory getInstance(){
		return SMSFactory.smsFactorySingleton;
	}
	
    /**
     * Initialize SMSFactory
     */
    public void initialize() throws MMPException {
		smsFactorySingleton = this;
	}

    /**
     * Shutdown SMSFactory
     */
	public void shutdown() throws MMPException {
		smsFactorySingleton = null;
	}
	
    /**
     * Redirect to the correct handler and send SMS
     * @param ND	The phone number
     * @param text	The SMS text content
     * @return		True if the SMS is sent
     */
    public boolean sendSMS(String ND, String text, String lang) throws SMSException {
		String formattedND = formatND(ND);
		ISMSHandler smsHandler = findHandler(formattedND);
	
		boolean ucs2Support = this.ucs2Languages.contains(lang);
	
		return smsHandler.sendSMS(formattedND, text, ucs2Support);
    }


    /**
     * Find the first handler in the factory list whose country codes list contains a code that matches the phone number
     * @param _phoneNumber	The phone number
     * @return			The first matching handler in the list
     */
    private ISMSHandler findHandler(String _phoneNumber) throws UnsupportedCountrySMSException {
		for(ISMSHandler smsHandler : smsHandlers) {
		    Properties countryCodesList = smsHandler.getCountryCodesList();
		    for(Object countryCode : countryCodesList.keySet()){
			if(_phoneNumber.startsWith((String)countryCode)){
			    return smsHandler;
			}
		    }
		}
		throw new UnsupportedCountrySMSException();
    }

    /**
     * Format the ND : remove the first "+" character
     * @param _ND	The phone number to format
     * @return		The formatted phone number
     */
    private String formatND(String _ND) throws UnsupportedCountrySMSException {
		Pattern p = Pattern.compile("[0-9-]+");
	        Matcher m = p.matcher(_ND);
	        if(m.find())
	            _ND = m.group();
		else
		    throw new UnsupportedCountrySMSException();
	
		return _ND;
    }

    /**
     * @param smsHandlers The smsHandlers list to set
     */
    public void setSmsHandlers(LinkedList<ISMSHandler> smsHandlers) {
        this.smsHandlers = smsHandlers;
    }


    /**
     * @param ucs2Languages the ucs2Languages to set
     */
    public void setUcs2Languages(List<String> ucs2Languages) {
        this.ucs2Languages = ucs2Languages;
    }
	
}
