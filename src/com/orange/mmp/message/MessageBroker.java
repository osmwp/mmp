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
 * Main component interface of CUMOB
 * 
 * Used to send and receive messages
 * 
 * TODO Evolution P2 - Implements a MessageBroker based on SOAP exposure (SOA) and Jabber (XMPP)
 * 
 * @author Thomas MILLET
 *
 */
public interface MessageBroker{

	/**
	 * Gets the destination on which this MessageBroker is listening 
	 * 
	 * @return A URI representation of the local destination
	 * @throws MMPMessageException
	 */
	public URI getLocalDestination() throws MMPMessageException;
	
	/**
	 * Sends a Message object asynchronously (1 request -> N responses) to
	 * all instances of MMP
	 * 
	 * @param message The message instance
	 * @param callback The response callback (null for notification purpose)
	 * @throws MMPMessageException
	 */
	public void sendMessageToAll(Message message, MessageListener callback) throws MMPMessageException;

	/**
	 * Sends a Message object asynchronously (1 request -> 1 responses) to a dedicated
	 * instance based on the destination
	 * 
	 * @param message The message instance
	 * @param destination The message destination
	 * @param callback The response callback (null for notification purpose)
	 * @throws MMPMessageException
	 */
	public void sendMessageTo(Message message, URI destination, MessageListener callback) throws MMPMessageException;
	
	/**
	 * Sends a Message object synchronously (1 request -> 1 response) to a dedicated
	 * instance and return the response Message object
	 * 
	 * @param message The message instance
	 * @param destination The message destination
	 * @return The message response
	 * @throws MMPMessageException
	 */
	public Message sendMessageTo(Message message, URI destination) throws MMPMessageException;
	
	/**
	 * Adds a permanent MessageListener to the MessageBroker.
	 * When messages are treated through sendMessageTo using a callback
	 * the MessageListeners are not notified.
	 * 
	 * Adding a MessageListener to the MessageBroker is mainly used to implement
	 * a permanent listener for incoming messages.
	 * 
	 * @param messageListener The MessageListener to add
	 * @throws MMPMessageException
	 */
	public void registerMessageListener(MessageListener messageListener) throws MMPMessageException;
	
	/**
	 * Removes a MessageListener from MessageBroker
	 * 
	 * @param messageListener The MessageListener to remove
	 * @throws MMPMessageException
	 */
	public void unregisterMessageListener(MessageListener messageListener) throws MMPMessageException;
	
	
}
