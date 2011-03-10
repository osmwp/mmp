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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.xml.namespace.QName;

import org.springframework.core.task.TaskExecutor;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;

import com.orange.mmp.core.ApplicationListener;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.config.MMPConfig;
import com.orange.mmp.core.data.Message;
import com.orange.mmp.core.data.Module;
import com.orange.mmp.message.MMPMessageException;
import com.orange.mmp.message.MessageBroker;
import com.orange.mmp.message.MessageListener;
import com.orange.mmp.module.MMPModuleException;
import com.orange.mmp.module.ModuleContainerFactory;
import com.orange.mmp.module.ModuleEvent;
import com.orange.mmp.module.ModuleObserver;

/**
 * JMS Implementation of MessageBroker
 * 
 * @author Thomas MILLET
 *
 */
public class JMSMessageBroker implements ApplicationListener, MessageBroker, MessageListener, ModuleObserver {
	
	/**
	 * Attribute name used to define a class name
	 */
	public static final QName CUMOB_CONFIG_ATTR_CLASSNAME = new QName("classname");
	
	/**
	 * TaskExecutor used to handle message aynchronously on permanent MessageListeners
	 */
	private TaskExecutor messageListenerTaskExecutor;
	
	/**
	 * Helper for JMS access
	 */
	private JmsTemplate jmsTemplate;
	
	/**
	 * Default Topic bound to the broker
	 */
	private Topic defaultTopic;
	
	/**
	 * Default Queue bound to the broker
	 */
	private Queue defaultQueue;
	
	/**
	 * MessageListener Cache
	 */
	private Map<Serializable, MessageListener> messageListenersMap;
	
	/**
	 * Cache for module message listeners
	 */
	@SuppressWarnings("unchecked")
	private Map<Class, MessageListener> moduleListenersCache;
	
	/**
	 * List of current messages listener 
	 */
	private List<MessageListener> messageListeners;
	
	/**
	 * Define the MMP instanceId on JMS Broker to avoid reentrant messages
	 */
	private String instanceId;
	
	/**
	 * Blocking queue to store Synchronous message listeners
	 */
	private BlockingQueue<JMSSyncMessageListener> syncListenerPool;
	
	/**
	 * Timeout in milliseconds for synchronous requests
	 */
	private long synchronousMessageTimeout;
	
	/**
	 * Maximum amount of synchronous message listeners
	 */
	private int maxSynchronousListeners;
	
	public void sendMessageTo(Message message, URI destination, MessageListener callback) throws MMPMessageException {
		JMSMessageDecorator messageDecorator = new JMSMessageDecorator(message,this.instanceId,this.defaultQueue);
		//Store message listener in cache
		if(callback != null){
			this.messageListenersMap.put(message.getId(),callback);
		}
		//Send message
		try{
			if(destination == null) this.jmsTemplate.send(this.defaultTopic, messageDecorator);
			else this.jmsTemplate.send(destination.toString(), messageDecorator);
		}catch(JmsException je){
			throw new MMPMessageException("Failed to send message",je);
		}
	}

	public void sendMessageToAll(Message message, MessageListener callback) throws MMPMessageException {
		this.sendMessageTo(message, null, callback);	
	}
	
	public Message sendMessageTo(Message message, URI destination) throws MMPMessageException {
		JMSSyncMessageListener messageListener = null;
		Message responseMessage = null;
		try{
			//Get Sync Listener in blocking mode
			messageListener = new JMSSyncMessageListener();
			
			//Send message
			this.sendMessageTo(message, destination, messageListener);
			
			//Get response
			synchronized(messageListener){			
				while(messageListener.getMessage() == null){
					messageListener.wait(this.synchronousMessageTimeout);
				}
				responseMessage = messageListener.getMessage();
				
				//Timeout ?
				if(responseMessage == null ) throw new MMPMessageException("null response or request timeout");
			}
			
		}catch(InterruptedException ie){
			throw new MMPMessageException(ie);
		}
		
		return responseMessage;
	}
	
	/**
	 * Helper method used to send an error based on a Throwable 
	 * 
	 * @param e The Throwable instance to send (only root cause is sent)
	 * @param destination The destination of the error
	 * @param sourceId The source ID of the message error (null for no source)
	 * @throws MMPMessageException
	 */
	public void sendError(Throwable e, URI destination, Serializable sourceId) throws MMPMessageException {
		Message errorMessage = new Message();
		errorMessage.setId(sourceId);
		while(e.getCause() != null) e = e.getCause();
		errorMessage.setData(e);
		this.sendMessageTo(errorMessage, destination, null);
	}
	
	public void onMessage(Message message, URI replyTo) throws MMPMessageException {
		//If the message as been sent using a callback
		MessageListener messageListenerCallback =  this.messageListenersMap.remove(message.getId());
		if(messageListenerCallback != null){
			messageListenerCallback.onMessage(message,replyTo);
		}
		//Otherwise forward message to all MessageListener
		else{
			for(MessageListener messageListener : this.messageListeners){
				this.messageListenerTaskExecutor.execute(new MessageListenerTask(messageListener,message,replyTo,this));
			}
		}	
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.module.ModuleObserver#onModuleEvent(com.orange.mmp.module.ModuleEvent)
	 */
	public void onModuleEvent(ModuleEvent moduleEvent) {
		try{
			if(moduleEvent.getType() == ModuleEvent.MODULE_ADDED){
				this.addModuleListener(moduleEvent.getModule());
			}
			else if(moduleEvent.getType() == ModuleEvent.MODULE_REMOVED){
				this.removeModuleListener(moduleEvent.getModule());
			}
		}catch(MMPMessageException ce){
			//NOP - Just Log
		}
	}
	
	/**
	 * Adds a MessageListener from Module configuration file
	 * 
	 * @param module The module owning the listener

	 * @throws MMPMessageException
	 */
	@SuppressWarnings("unchecked")
	protected void addModuleListener(Module module) throws MMPMessageException{
		try{
			MMPConfig moduleConfiguration = ModuleContainerFactory.getInstance().getModuleContainer().getModuleConfiguration(module);
			if(moduleConfiguration != null && moduleConfiguration.getMessageListener() != null){
				for(MMPConfig.MessageListener listenerConfig : moduleConfiguration.getMessageListener()){
					if(listenerConfig.getOtherAttributes() != null
							&& listenerConfig.getOtherAttributes().get(CUMOB_CONFIG_ATTR_CLASSNAME) != null){
						Class messageListenerClass = ModuleContainerFactory.getInstance().getModuleContainer().loadModuleClass(module, listenerConfig.getOtherAttributes().get(CUMOB_CONFIG_ATTR_CLASSNAME));
						if(MessageListener.class.isAssignableFrom(messageListenerClass)){
							if(!this.moduleListenersCache.containsKey(messageListenerClass)){
								MessageListener messageListener = (MessageListener)messageListenerClass.newInstance(); 
								this.registerMessageListener(messageListener);
								this.moduleListenersCache.put(messageListenerClass, messageListener);
							}
							else throw new MMPMessageException("Class '"+messageListenerClass+"' is already registered as MessageListener");
						}
						else throw new MMPMessageException("Class '"+messageListenerClass+"' from module '"+module.getName()+"' is not a MessageListener implementation");
					}
				}
			}
		}catch(InstantiationException ie){
			throw new MMPMessageException(ie);
		}catch(IllegalAccessException iae){
			throw new MMPMessageException(iae);
		}catch(MMPModuleException ce){
			throw new MMPMessageException(ce);	
		}
	}
	
	/**
	 * Removes a Module MessageListener
	 * 
	 * @param module The module owning the listener

	 * @throws MMPMessageException
	 */
	@SuppressWarnings("unchecked")
	protected void removeModuleListener(Module module) throws MMPMessageException{
		try{
			MMPConfig moduleConfiguration = ModuleContainerFactory.getInstance().getModuleContainer().getModuleConfiguration(module);
			if(moduleConfiguration != null && moduleConfiguration.getMessageListener() != null){
				for(MMPConfig.MessageListener listenerConfig : moduleConfiguration.getMessageListener()){
					if(listenerConfig.getOtherAttributes() != null
							&& listenerConfig.getOtherAttributes().get(CUMOB_CONFIG_ATTR_CLASSNAME) != null){
						Class messageListenerClass = ModuleContainerFactory.getInstance().getModuleContainer().loadModuleClass(module, listenerConfig.getOtherAttributes().get(CUMOB_CONFIG_ATTR_CLASSNAME));
						MessageListener messageListener = this.moduleListenersCache.get(messageListenerClass);
						if(messageListener != null) this.unregisterMessageListener(messageListener);
					}	
				}
			}
		}catch(MMPModuleException ce){
			throw new MMPMessageException(ce);	
		}
	}
	
	public URI getLocalDestination() throws MMPMessageException{
		try{
			return new URI(this.defaultQueue.getQueueName());
		}catch(URISyntaxException ue){
			throw new MMPMessageException(ue);
		}catch(JMSException je){
			throw new MMPMessageException(je);
		}
	}

	@SuppressWarnings("unchecked")
	public void initialize() throws MMPException {
		//Initialize the Listeners list
		this.messageListeners = Collections.synchronizedList(new ArrayList<MessageListener>());
		//Initialize the module message listeners configuration cache
		this.moduleListenersCache = new ConcurrentHashMap<Class, MessageListener>();
		
		this.syncListenerPool = new ArrayBlockingQueue<JMSSyncMessageListener>(this.maxSynchronousListeners);
		try{
			for(int i=0; i < this.maxSynchronousListeners; i++ ){
				this.syncListenerPool.put(new JMSSyncMessageListener());
			}
		}catch(InterruptedException e) {
			throw new MMPMessageException(e);
		}
		
		//Initialize Listeners Cache
		this.messageListenersMap = new ConcurrentHashMap<Serializable, MessageListener>();
		
		//Get the ModuleContainer and add itself as a ModuleObserver
		ModuleContainerFactory.getInstance().getModuleContainer().registerModuleObserver(this);
	}

	public void shutdown() throws MMPException {
		if(this.messageListenersMap != null) this.messageListenersMap.clear();
		if(this.moduleListenersCache != null) this.moduleListenersCache.clear();
	}
	
	public void registerMessageListener(MessageListener messageListener) throws MMPMessageException {
		this.messageListeners.add(messageListener);
	}

	public void unregisterMessageListener(MessageListener messageListener) throws MMPMessageException {
		this.messageListeners.remove(messageListener);
	}

	/**
	 * @param instanceId the instanceId to set
	 */
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	/**
	 * @param jmsTemplate the jmsTemplate to set
	 */
	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	/**
	 * @param defaultTopic the defaultTopic to set
	 */
	public void setDefaultTopic(Topic defaultTopic) {
		this.defaultTopic = defaultTopic;
	}

	/**
	 * @param defaultQueue the defaultQueue to set
	 */
	public void setDefaultQueue(Queue defaultQueue) {
		this.defaultQueue = defaultQueue;
	}
	
	/**
	 * @param messageListenerTaskExecutor the messageListenerTaskExecutor to set
	 */
	public void setMessageListenerTaskExecutor(TaskExecutor messageListenerTaskExecutor) {
		this.messageListenerTaskExecutor = messageListenerTaskExecutor;
	}

	/**
	 * @param synchronousMessageTimeout the synchronousMessageTimeout to set
	 */
	public void setSynchronousMessageTimeout(long synchronousMessageTimeout) {
		this.synchronousMessageTimeout = (synchronousMessageTimeout*1000);
	}

	/**
	 * @param maxSynchronousListeners the maxSynchronousListeners to set
	 */
	public void setMaxSynchronousListeners(int maxSynchronousListeners) {
		this.maxSynchronousListeners = maxSynchronousListeners;
	}
	
	/**
	 * Inner class used to execute asynchronously permanent MessageListeners treatments
	 * @author tml
	 *
	 */
	private class MessageListenerTask implements Runnable {

		/**
		 * Associated MessageBroker
		 */
		JMSMessageBroker messageBroker;
		
		/**
		 * Associated MessageListener 
		 */
		MessageListener messageListener;
		
		/**
		 * Received message
		 */
		Message message;
		
		/**
		 * Sender
		 */
		URI replyTo;
		
		/**
		 * Default constructor
		 * @param messageListener Associated MessageListener 
		 * @param message Received message
		 * @param replyTo Sender
		 */
		public MessageListenerTask(MessageListener messageListener, Message message, URI replyTo, JMSMessageBroker messageBroker){
			this.messageListener = messageListener;
			this.message = message;
			this.replyTo = replyTo;
			this.messageBroker = messageBroker;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			try{
				this.messageListener.onMessage(this.message, this.replyTo);
			}catch(Throwable e){
				if(this.replyTo != null){
					try{
						this.messageBroker.sendError(e, this.replyTo, message.getId());
					}catch(MMPException ce){
						//NOP - Unable to send error, keep a trace
					}
				}
			}
		}

	}
}
