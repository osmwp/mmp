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

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.TextMessage;

import com.orange.mmp.core.data.Message;
import com.orange.mmp.message.MMPMessageException;

import javax.jms.Session;

import org.springframework.jms.core.MessageCreator;

/**
 * 	Custom JMS MessageCreator used to encapsulate the MMP Message
 * 	in JMS message to hide JMS mechanism at MMP API level.
 * 
 * 	@author Thomas MILLET
 *
 */
public class JMSMessageDecorator implements MessageCreator {

	/**
	 * Selector used in JMS Message to filter self sent messages
	 */
	public static final String MESSAGE_SELECTOR_SENDER_PROPERTY = "SID";
	
	/**
	 * Indicates the MAX message ID before reseting counter (maximum awaiting messages)
	 */
	public static final int MAX_MESSAGE_ID = 100000;
	
	/**
	 * The ID counter used to identify a message 
	 */
	private volatile static int idCounter = 0;
	
	/**
	 * The instance ID of the local MMP instance
	 */
	private String instanceId;
	
	/**
	 * The queue on which messages must be sent for reply
	 */
	private Queue replyTo;
	
	/**
	 * Inner message to decorate
	 */
	private Message message;
	
	/**
	 * Inner JMS message to decorate
	 */
	private javax.jms.Message jmsMessage;
	
	/**
	 * Default Constructor using a MMP Message.
	 * This constructor is mainly used to get a JMS Message from a MMP Message
	 *  
	 * @param message The message to decorate
	 * @param instanceId The ID of the current MMP instance
	 * @param replyTo The Queue on which replies must be sent (null for notification)
	 */
	public JMSMessageDecorator(Message message, String instanceId, Queue replyTo) throws MMPMessageException {
		this.replyTo = replyTo;
		this.instanceId = instanceId;
		this.setMessage(message);
	}
	
	/**
	 * Notification message constructor using a MMP Message.
	 * This constructor is mainly used to get a JMS Message from a MMP Message
	 *  
	 * @param message The message to decorate
	 * @param instanceId The ID of the current MMP instance
	 */
	public JMSMessageDecorator(Message message, String instanceId) throws MMPMessageException {
		this(message,instanceId,null);
	}
	
	/**
	 * Notification message constructor using a JMS Message.
	 * This constructor is mainly used to get a MMP Message from a JMS Message
	 *  
	 * @param jmsMessage The message to analyse
	 * @param instanceId The ID of the current MMP instance
	 * @param replyTo The Queue on which replies must be sent (null for notification)
	 */
	public JMSMessageDecorator(javax.jms.Message jmsMessage, String instanceId) throws MMPMessageException{
		this(jmsMessage,instanceId,null);
	}
	
	/**
	 * Default Constructor using a JMS Message.
	 * This constructor is mainly used to get a MMP Message from a JMS Message
	 *  
	 * @param jmsMessage The message to analyze
	 * @param instanceId The ID of the current MMP instance
	 * @param replyTo The Queue on which replies must be sent (null for notification)
	 */
	public JMSMessageDecorator(javax.jms.Message jmsMessage, String instanceId, Queue replyTo) throws MMPMessageException{
		this.jmsMessage = jmsMessage;
		this.instanceId = instanceId;
		this.replyTo = replyTo;
	}
	
	/**
	 * Called to build a JMS Message from a MMP Message by JMSTemplate
	 */
	public javax.jms.Message createMessage(Session session) throws JMSException {
		//Create a new ObjectMessage in JMS
		if(message != null) this.jmsMessage = session.createObjectMessage(message);
		
		//Set the sender ID for loop routing issues
		this.jmsMessage.setStringProperty(MESSAGE_SELECTOR_SENDER_PROPERTY,this.instanceId);
		
		//Set replyTo field if must reply
		this.jmsMessage.setJMSReplyTo(this.replyTo);

		return this.jmsMessage;
	}
	

	/**
	 * Inner method used to build the MMP Message from the JMS Message
	 */
	@SuppressWarnings("unchecked")
	protected void buildMessage() throws JMSException {
		//Object message
		if(this.jmsMessage instanceof ObjectMessage){
			Serializable jmsObject = ((ObjectMessage)jmsMessage).getObject();
			if(jmsObject instanceof Message){
				this.message = (Message) jmsObject;
			}
			else{
				this.message = new Message();
				this.message.setData(jmsObject);
			}
		}
		//Text message
		else if(this.jmsMessage instanceof TextMessage){
			this.message = new Message();
			this.message.setData(((TextMessage)jmsMessage).getText());
		}
		//Map Message
		else if(this.jmsMessage instanceof MapMessage){
			HashMap<String, Object> messageMap = new HashMap<String, Object>(); 
			Enumeration<String> messageMapEnum = ((MapMessage)jmsMessage).getMapNames();
			while(messageMapEnum.hasMoreElements()){
				String key = messageMapEnum.nextElement();
				messageMap.put(key, ((MapMessage)jmsMessage).getObject(key));
			}
			this.message = new Message();
			this.message.setData(messageMap);
		}
		//Byte[] message
		else if(this.jmsMessage instanceof BytesMessage){
			byte[] messageBytes = new byte[(int)((BytesMessage)jmsMessage).getBodyLength()];
			((BytesMessage)jmsMessage).readBytes(messageBytes);
			this.message = new Message();
			this.message.setData(messageBytes);
		}
		else throw new JMSException("Unsupported message type '"+this.jmsMessage.getClass().getName()+"'");
	}

	/**
	 * @return the message
	 */
	public Message getMessage() throws MMPMessageException {
		try{
			this.buildMessage();
		}catch(JMSException je){
			throw new MMPMessageException(je);				
		}
		return this.message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(Message message) throws MMPMessageException {
		this.message = message;
		if(this.message.getId() == null){
			switch(JMSMessageDecorator.idCounter){
				case MAX_MESSAGE_ID:
					JMSMessageDecorator.idCounter = 0;
					break;
				default:
					JMSMessageDecorator.idCounter++;
			}
			this.message.setId(this.instanceId+JMSMessageDecorator.idCounter);
		}
	}

	/**
	 * @return the jmsMessage
	 */
	public javax.jms.Message getJmsMessage() {
		return jmsMessage;
	}

	/**
	 * @param jmsMessage the jmsMessage to set
	 */
	public void setJmsMessage(javax.jms.Message jmsMessage) throws MMPMessageException {
		this.jmsMessage = jmsMessage;
	}

	/**
	 * @return the instanceId
	 */
	public String getInstanceId() {
		return instanceId;
	}

	/**
	 * @param instanceId the instanceId to set
	 */
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	/**
	 * @return the replyTo
	 */
	public Queue getReplyTo() {
		return replyTo;
	}

	/**
	 * @param replyTo the replyTo to set
	 */
	public void setReplyTo(Queue replyTo) {
		this.replyTo = replyTo;
	}

	

	
}
