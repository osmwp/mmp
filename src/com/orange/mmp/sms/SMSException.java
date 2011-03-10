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

/**
 * Generic Exception for SMS
 * 
 * @author tml
 *
 */
@SuppressWarnings("serial")
public class SMSException extends Exception {

    /**
     * Default Constructor
     */
    public SMSException() {
    	super();
    }

    /**
     * Default Constructor with message
     * 
     * @param message error message
     */
    public SMSException(String message) {
    	super(message);
    }

    /**
     * Default Constructor with root cause
     * 
     * @param cause root cause
     */
    public SMSException(Throwable cause) {
    	super(cause);
    }

    /**
     * Default Constructor with message and root cause
     * 
     * @param message error message
     * @param cause root cause
     */
    public SMSException(String message, Throwable cause) {
    	super(message, cause);
    }

}
