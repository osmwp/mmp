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
package com.orange.mmp.util.requestmonitor;

/**
 * Status of request and message.
 * @author rmxc7111
 */
public enum RequestMessageStatus {

	/** Message is a critical error. Request is stopped */
	CRITICAL,
	/** Message is an error. Request is failed */
	ERROR,
	/** Message is a warning. Request is not failed, but a problem is found */
	WARN,
	/** Message is an information */
	INFO,
	/** Message is a debug information */
	DEBUG;
	
}
