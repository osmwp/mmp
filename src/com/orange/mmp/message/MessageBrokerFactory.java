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

import java.util.Map;

import com.orange.mmp.core.Constants;

/**
 * Factory to access MessageBroker instances outside ApplicationContext
 * 
 * @author Thomas MILLET
 *
 */
public class MessageBrokerFactory {

	/**
	 * Reference to the default MessageBroker Singleton
	 */
	private MessageBroker defaultMessageBrokerSingleton;
	
	/**
	 * References to the MessageBroker instances
	 */
	private Map<Object, MessageBroker> messageBrokerSingletons;
	
	/**
	 * Reference to the MessageBrokerFactory Singleton
	 */
	private static MessageBrokerFactory messageBrokerFactorySingleton;
	
	/**
	 * Default constructor (should be called by Spring IOC Only)
	 */
	public MessageBrokerFactory(){
		if(MessageBrokerFactory.messageBrokerFactorySingleton == null){
			MessageBrokerFactory.messageBrokerFactorySingleton = this;
		}
	}
	
	/**
	 * Accessor to MessageBrokerFactory singleton
	 * 
	 * @return A MessageBrokerFactory instance
	 * @throws MMPMessageException
	 */
	public static MessageBrokerFactory getInstance() throws MMPMessageException{
		return MessageBrokerFactory.messageBrokerFactorySingleton;
	}
	
	/**
	 * Get the default MessageBroker instance
	 * 
	 * @return The default MessageBroker instance of MessageBrokerFactory
	 * @throws MMPMessageException
	 */
	public MessageBroker getMessageBroker() throws MMPMessageException{
		return this.defaultMessageBrokerSingleton;
	}
	
	/**
	 * Get a MessageBroker instance based on its type
	 * 
	 * @param type The MessageBroker type
	 * @return The MessageBroker instance of MessageBrokerFactory
	 * @throws MMPMessageException
	 */
	public MessageBroker getMessageBroker(Object type) throws MMPMessageException{
		return this.messageBrokerSingletons.get(type);
	}
	
	/**
	 * Get the list of MessageBroker types owned by this Factory 
	 * 
	 * @return An array of Objects containing types
	 */
	public Object[] getTypes(){
		return this.messageBrokerSingletons.keySet().toArray(); 
	}

	/**
	 * @param messageBrokerSingletons the messageBrokerSingletons to set
	 */
	public void setMessageBrokerSingletons(Map<Object, MessageBroker> messageBrokerSingletons) {
		this.messageBrokerSingletons = messageBrokerSingletons;
		this.defaultMessageBrokerSingleton = this.messageBrokerSingletons.get(Constants.DEFAULT_COMPONENT_KEY);
	}
}
