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

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;

import com.orange.mmp.message.MMPMessageException;

/**
 * JMS Listener used to handle messages received on Default Queue
 * 
 * @author Thomas MILLET
 *
 */
public class JMSDefaultQueueListener implements MessageListener {

	/**
	 * Reference to the MessageBroker
	 */
	private JMSMessageBroker messageBroker;
	
	/**
	 * Current instance ID
	 */
	private String instanceId;
	
	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message jmsMessage) {
		URI replyTo = null;
		com.orange.mmp.core.data.Message message = null;
		try{
			JMSMessageDecorator jmsMessageDecorator = new JMSMessageDecorator(jmsMessage,this.instanceId);
			replyTo = (jmsMessage.getJMSReplyTo() != null)? new URI(((Queue)jmsMessage.getJMSReplyTo()).getQueueName()):null;
			message = jmsMessageDecorator.getMessage();
			this.messageBroker.onMessage(message, replyTo);
			
		}catch(Throwable e){
			//Catch all Exceptions here to reply with an error message
			if(replyTo != null && message != null){
				try{
					this.messageBroker.sendError(e, replyTo, message.getId());
				}catch(MMPMessageException ce){
					//NOP - Unable to send error, keep a trace
				}
			}
		}
	}

	/**
	 * @param messageBroker the messageBroker to set
	 */
	public void setMessageBroker(JMSMessageBroker messageBroker) {
		this.messageBroker = messageBroker;
	}

	/**
	 * @param instanceId the instanceId to set
	 */
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
}
