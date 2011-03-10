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
package com.orange.mmp.message;

import java.net.URI;

import com.orange.mmp.core.data.Message;

/**
 * Callback reference used to get message response if available
 * 
 * @author Thomas MILLET
 */
public interface MessageListener {

	/*
	 *	Called on message reception after sending message
	 *	if a response is received
	 *
	 *	@param message The message response
	 *	@param replyTo The replyTo destination (null for no reply)
	 */
	public void onMessage(Message message, URI replyTo)  throws MMPMessageException;
}
