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

import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.List;

import com.orange.mmp.api.MMPApiException;
import com.orange.mmp.api.ApiContainerFactory;
import com.orange.mmp.api.ApiEvent;
import com.orange.mmp.api.ApiObserver;
import com.orange.mmp.core.ApplicationListener;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.data.Message;
import com.orange.mmp.core.data.Api;
import com.orange.mmp.message.MMPMessageException;
import com.orange.mmp.message.MessageBrokerFactory;
import com.orange.mmp.message.MessageListener;
import com.orange.mmp.message.data.ServiceRegistrationMessage;

/**
 * MessageListener handling ServiceRegistrationMessage
 * 
 * @author Thomas MILLET
 *
 */
public class JMSServiceRegistrationHandler implements ApplicationListener, MessageListener, ApiObserver {
	
	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.message.MessageListener#onMessage(com.orange.mmp.core.data.Message, java.net.URI)
	 */
	public void onMessage(Message message, URI replyTo) throws MMPMessageException {
		if(message instanceof ServiceRegistrationMessage){
			this.handleServiceRegistrationMessage((ServiceRegistrationMessage)message, replyTo);
		}
	}
	
	public void initialize() throws MMPException {
		//Add new ServiceObserver to ServiceContainer
		ApiContainerFactory.getInstance().getApiContainer().registerApiObserver(this);

		//Add new MessageListener to MessageBroker
		MessageBrokerFactory.getInstance().getMessageBroker().registerMessageListener(this);
		
		//Asks for remote services
		ServiceRegistrationMessage message = new ServiceRegistrationMessage(null,ServiceRegistrationMessage.GET);
		MessageBrokerFactory.getInstance().getMessageBroker().sendMessageToAll(message, null);
	}
	
	public void shutdown() throws MMPException {
		//Remove ServiceObserver
		ApiContainerFactory.getInstance().getApiContainer().unregisterApiObserver(this);
		
		//Send messages to unregister local services on remote instances
		for(Api service :  ApiContainerFactory.getInstance().getApiContainer().listApis()){
			if(!(service instanceof JMSRemoteService) && service.isShared()){
				ServiceRegistrationMessage message = new ServiceRegistrationMessage(service,ServiceRegistrationMessage.DEL);
				MessageBrokerFactory.getInstance().getMessageBroker().sendMessageToAll(message, null);
			}
		}
		
		//Add new MessageListener to MessageBroker
		MessageBrokerFactory.getInstance().getMessageBroker().unregisterMessageListener(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.api.ApiObserver#onApiEvent(com.orange.mmp.api.ApiEvent)
	 */
	public void onApiEvent(ApiEvent apiEvent) {
		Api service =  apiEvent.getApi();
		
		if(!(service instanceof JMSRemoteService) && service.isShared()){
			try{
				switch(apiEvent.getType()){
					case ApiEvent.API_ADDED:
						//Send GET message
						ServiceRegistrationMessage addMessage = new ServiceRegistrationMessage(new JMSRemoteService(service),ServiceRegistrationMessage.SET);
						MessageBrokerFactory.getInstance().getMessageBroker().sendMessageToAll(addMessage, null);	
					break;
					case ApiEvent.API_REMOVED:
						//Send DEL message
						ServiceRegistrationMessage delMessage = new ServiceRegistrationMessage(service,ServiceRegistrationMessage.DEL);
						MessageBrokerFactory.getInstance().getMessageBroker().sendMessageToAll(delMessage, null);
						//Send GET message (fail-over on remote service if possible)
						ServiceRegistrationMessage getMessage = new ServiceRegistrationMessage(service,ServiceRegistrationMessage.GET);
						MessageBrokerFactory.getInstance().getMessageBroker().sendMessageToAll(getMessage, null);
					break;
				}
			}catch(MMPMessageException ce){
				//NOP - Just log
			}
		}		
	}
	
	/**
	 * Manage incoming Service Registration Messages
	 * 	-GET : return all shared service of current instance
	 * - SET : receive and add a remote service to ServiceContainer
	 * - SET : remove a remote service from ServiceContainer
	 * 
	 * @param message The incoming ServiceRegistrationMessage
	 * @param replyTo The replyTo field for response
	 * @throws MMPMessageException
	 */
	private synchronized void handleServiceRegistrationMessage(ServiceRegistrationMessage message, URI replyTo) throws MMPMessageException{
		switch(message.getType()){
			//GET Message - Send services to other instances
			case ServiceRegistrationMessage.GET:
				try{
					if(replyTo != null){
						//Check if a specific service is searched
						Api askedService = message.getService();
						//Send all local service to instance
						if(askedService == null){
							List<Api> services = ApiContainerFactory.getInstance().getApiContainer().listApis();
							for(Api service : services){
								if(!(service instanceof JMSRemoteService) && service.isShared()){
									ServiceRegistrationMessage outMessage = new ServiceRegistrationMessage(new JMSRemoteService(service),ServiceRegistrationMessage.SET);
									MessageBrokerFactory.getInstance().getMessageBroker().sendMessageTo(outMessage, replyTo, null);
								}
							}
						}
						//Send the local service if found
						else{
							Api service = ApiContainerFactory.getInstance().getApiContainer().getApi(askedService);
							if(service != null && !(service instanceof JMSRemoteService) && service.isShared()){
								ServiceRegistrationMessage outMessage = new ServiceRegistrationMessage(new JMSRemoteService(service),ServiceRegistrationMessage.SET);
								MessageBrokerFactory.getInstance().getMessageBroker().sendMessageTo(outMessage, replyTo, null);
							}
						}
					}
				}catch(MMPApiException ce){
					throw new MMPMessageException(ce);
				}	
			break;
			//SET Message - Add service proxy to CUAPI
			case ServiceRegistrationMessage.SET:
				Api serviceToSet = message.getService();
				try{	
					//Check that service is not already registered
					Api service = ApiContainerFactory.getInstance().getApiContainer().getApi(serviceToSet);
					//Not found - Adds remote service
					if(service == null){						
						//Add Proxied RemoteService
						JMSServiceInvocationHandler remoteServiceInvocationHandler = new JMSServiceInvocationHandler(serviceToSet, replyTo, MessageBrokerFactory.getInstance().getMessageBroker());  
						Object serviceProxy = Proxy.newProxyInstance(serviceToSet.getDefinitionClass().getClassLoader(),
								new Class[]{serviceToSet.getDefinitionClass()},
								remoteServiceInvocationHandler);
						
						ApiContainerFactory.getInstance().getApiContainer().addApi(remoteServiceInvocationHandler.getService(), serviceProxy);
					}
					//If instance of a remote service -> adds endpoint
					else if(service instanceof JMSRemoteService){
						((JMSRemoteService)service).addEndpoint(replyTo);
					}

				}catch(NoClassDefFoundError nde){
					throw new MMPMessageException(nde);
				}catch(MMPApiException ce){
					throw new MMPMessageException(ce);
				}
			break;
			//DEL Message - Remove service from CUAPI
			case ServiceRegistrationMessage.DEL:
				Api serviceToDel = message.getService();
				try{
					//Check that service is local or not
					Api service = ApiContainerFactory.getInstance().getApiContainer().getApi(serviceToDel);
					if(service instanceof JMSRemoteService){
						//Remove endpoint
						((JMSRemoteService)service).removeEndpoint(replyTo);
						//No more endpoints, remove service
						if(((JMSRemoteService)service).getEndpoint() == null){
							ApiContainerFactory.getInstance().getApiContainer().removeApi(serviceToDel);
						}
					}
				}catch(MMPApiException ce){
					throw new MMPMessageException(ce);
				}
			break;	
		}
	}
}
