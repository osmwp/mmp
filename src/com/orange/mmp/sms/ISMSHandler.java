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

public interface ISMSHandler {

    /**
     * Sends SMS API
     * @param ND Mobile phone number
     * @param text SMS content
     * @param useUCS2Support Indicate the Enabler to use UCS2 for encoding (default GSM 7-bits)
     * @return true if SMS has been sent, false otherwise
     */
    public boolean sendSMS(String ND, String text, boolean useUCS2Support);

    /**
     * Return this list of supported countries for current handler
     * @return A list of country with associated country codes
     */
    public Properties getCountryCodesList();

}
