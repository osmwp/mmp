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
package com.orange.mmp.bind;

/**
 * Generic binding exception
 * 
 * @author nmtv3386
 *
 */
@SuppressWarnings("serial")
public class BindingException extends Exception {

	/**
	 * Default constructor
	 */
    public BindingException(){
    	super();
    }

    /**
     * Constructor with associated error message and root cause
     * 
     * @param message The error message
     * @param rootCause The error root cause
     */
	public BindingException(String message, Throwable rootCause) {
		super(message, rootCause);
	}

	/**
     * Constructor with associated error root cause
     * 
     * @param rootCause The error root cause
     */
	public BindingException(Throwable rootCause) {
		super(rootCause);
	}

	/**
     * Constructor with associated error message
     * 
     * @param message The error message
     */
    public BindingException(String message){
    	super((message == null)? "Unknown Internal Binding Error":message);
    }
}
