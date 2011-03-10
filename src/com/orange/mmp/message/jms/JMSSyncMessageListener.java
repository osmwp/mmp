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
package com.orange.mmp.message.jms;

import java.net.URI;

import com.orange.mmp.core.data.Message;
import com.orange.mmp.message.MMPMessageException;
import com.orange.mmp.message.MessageListener;

/**
 * A basic MessageListener for with support for synchronous message handling
 * 
 * @author Thomas MILLET
 *
 */
public class JMSSyncMessageListener implements MessageListener {

	/**
	 * The bound message
	 */
	private Message message;
	
	/**
	 * The destination for reply
	 */
	private URI replyTo;
	
	/**
	 * Default constructor
	 * 
	 * @param owner
	 */	
	public JMSSyncMessageListener(){
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.message.MessageListener#onMessage(com.orange.mmp.core.data.Message, java.net.URI)
	 */
	public void onMessage(Message message, URI replyTo) throws MMPMessageException{
		try{
			this.message = message;
			this.replyTo = replyTo;
			synchronized (this) {				
				this.notifyAll();	
			}
		}catch(IllegalMonitorStateException lmse){
			throw new MMPMessageException(lmse);
		}
	}

	/**
	 * @return the message
	 */
	public Message getMessage() {
		return message;
	}

	/**
	 * @return the replyTo
	 */
	public URI getReplyTo() {
		return replyTo;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(Message message) {
		this.message = message;
	}

	/**
	 * @param replyTo the replyTo to set
	 */
	public void setReplyTo(URI replyTo) {
		this.replyTo = replyTo;
	}

}
