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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URI;

import com.orange.mmp.api.ApiContainerFactory;
import com.orange.mmp.core.ApplicationListener;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.data.Message;
import com.orange.mmp.core.data.Api;
import com.orange.mmp.message.MMPMessageException;
import com.orange.mmp.message.MessageBroker;
import com.orange.mmp.message.MessageBrokerFactory;
import com.orange.mmp.message.MessageListener;
import com.orange.mmp.message.data.ServiceInvocationMessage;

/**
 * InvocationHandler used by CUMOB proxy classes to delegate
 * methods invocations to CUMOB component
 * 
 * TODO Evolution P1 - Find a way to support POJO in remote calls
 * 
 * @author Thomas MILLET
 */
public class JMSServiceInvocationHandler implements ApplicationListener, InvocationHandler, MessageListener {
	
	/**
	 * Object methods list for performance tip when looking for Object methods
	 */
	private static final String OBJECT_METHOD_LIST = "toStringhashCodegetClassnotifynotifyAllwaitequals"; 

	/**
	 * Constant used to identify "toString()" index for performances
	 */
	private static final int OBJECT_METHOD_TO_STRING_INDEX = 0;
	
	/**
	 * Constant used to identify "hashCode()" index for performances
	 */
	private static final int OBJECT_METHOD_HASH_CODE_INDEX = 8;
	
	/**
	 * Constant used to identify "getClass()" index for performances
	 */
	private static final int OBJECT_METHOD_GET_CLASS_INDEX = 16;

	/**
	 * Constant used to identify "getNotify()" index for performances
	 */
	private static final int OBJECT_METHOD_NOTIFY_INDEX = 24;
	
	/**
	 * Constant used to identify "getNotifyAll()" index for performances
	 */
	private static final int OBJECT_METHOD_NOTIFY_ALL_INDEX = 30;

	/**
	 * Constant used to identify "wait()" index for performances
	 */
	private static final int OBJECT_METHOD_WAIT_INDEX = 39;
	
	/**
	 * Constant used to identify "equals()" index for performances
	 */
	private static final int OBJECT_METHOD_EQUALS_INDEX = 43;
	
	/**
	 * Remote service
	 */
	private JMSRemoteService remoteService;
	
	/**
	 * The current MessageBroker implementation
	 */
	private MessageBroker messageBroker;
	
	/**
	 * Default constructor
	 */
	public JMSServiceInvocationHandler() {
		super();
	}
	
	/**
	 * Constructor using fields
	 * @param proxiedService
	 * @param messageBroker
	 */
	public JMSServiceInvocationHandler(Api service, URI serviceEndPoint, MessageBroker messageBroker) {
		super();
		this.messageBroker = messageBroker;
		this.setService(service, serviceEndPoint);		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.message.MessageListener#onMessage(com.orange.mmp.core.data.Message, java.net.URI)
	 */
	public void onMessage(Message message, URI replyTo) throws MMPMessageException {
		if(message instanceof ServiceInvocationMessage){
			this.handleServiceInvocationMessage((ServiceInvocationMessage)message, replyTo);
		}
	}
	
	public void initialize() throws MMPException {
		//Add new MessageListener to MessageBroker
		MessageBrokerFactory.getInstance().getMessageBroker().registerMessageListener(this);
	}
	
	public void shutdown() throws MMPException {
		//Add new MessageListener to MessageBroker
		MessageBrokerFactory.getInstance().getMessageBroker().unregisterMessageListener(this);
	}
	
	/**
	 * Manage incoming Service Call Messages to allow remoting on 
	 * services owned by the ServiceContainer 
	 * 	 
	 * @param message The incoming ServiceCallMessage
	 * @param replyTo The replyTo field for response
	 * @throws MMPMessageException
	 */
	@SuppressWarnings("all")
	public void handleServiceInvocationMessage(ServiceInvocationMessage serviceInvocationMessage, URI replyTo) throws MMPMessageException{
		switch(serviceInvocationMessage.getType()){
			//Only Handle requests here, response are handled by a synchronous listener
			case ServiceInvocationMessage.TYPE_REQUEST : 
				Api service = new Api();
				service.setName(serviceInvocationMessage.getServiceName());
				try{
					Serializable response = (Serializable)ApiContainerFactory.getInstance().getApiContainer().invokeApi(service, serviceInvocationMessage.getMethodName(), serviceInvocationMessage.getArgs());
					serviceInvocationMessage.setResponse(response);
					MessageBrokerFactory.getInstance().getMessageBroker().sendMessageTo(serviceInvocationMessage, replyTo, null);
				}catch(Exception ce){
					throw new MMPMessageException(ce);
				}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		//First get the index of the method name in Object methods list
		int methodIndex = OBJECT_METHOD_LIST.indexOf(method.getName());
		switch(methodIndex){
			//toString()
			case OBJECT_METHOD_TO_STRING_INDEX :
				return this.remoteService.getName();
			//hashCode()
			case OBJECT_METHOD_HASH_CODE_INDEX :
				return this.remoteService.hashCode();
			//getClass()
			case OBJECT_METHOD_GET_CLASS_INDEX :
				return this.remoteService.getClass();
			//notify()
			case OBJECT_METHOD_NOTIFY_INDEX :
				this.remoteService.notify();
				return null;
			//notifyAll()
			case OBJECT_METHOD_NOTIFY_ALL_INDEX :
				this.remoteService.notifyAll();
				return null;
			//wait(..)
			case OBJECT_METHOD_WAIT_INDEX :
				if(args == null || args.length == 0) this.remoteService.wait();
				else this.remoteService.wait((Long)args[0]);
				return null;
			//equals()
			case OBJECT_METHOD_EQUALS_INDEX :
				if(args != null && args.length > 0) return this.remoteService.equals(args[0]);
				else return false;
			//Handle remote Service API
			default:
				Serializable[] serializableArgs = null;
				if(args != null){
					serializableArgs = new Serializable[args.length];
					System.arraycopy(args, 0, serializableArgs, 0, args.length);
				}
				
				ServiceInvocationMessage outMessage = new ServiceInvocationMessage(this.remoteService.getName(), method.getName(), serializableArgs);
				
			
				//Send message
				Message response = this.messageBroker.sendMessageTo(outMessage, this.remoteService.getEndpoint());
			
				//Applicative error
				if(response.getData() instanceof Throwable){
					throw (Throwable)response.getData();
				}
				//OK
				else{
					return response.getData();
				}
		}
	}

	/**
	 * @return the proxiedService
	 */
	public Api getService() {
		return remoteService;
	}

	/**
	 * @param proxiedService the proxiedService to set
	 */
	public void setService(Api service, URI endpoint) {
		this.remoteService = new JMSRemoteService(service);
		this.remoteService.addEndpoint(endpoint);
	}

	/**
	 * @return the messageBroker
	 */
	public MessageBroker getMessageBroker() {
		return messageBroker;
	}

	/**
	 * @param messageBroker the messageBroker to set
	 */
	public void setMessageBroker(MessageBroker messageBroker) {
		this.messageBroker = messageBroker;
	}
}
