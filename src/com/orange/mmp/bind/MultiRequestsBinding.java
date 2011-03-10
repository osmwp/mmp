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
package com.orange.mmp.bind;

import java.util.Hashtable;
import java.util.HashMap;
import java.util.Iterator;

import com.orange.mmp.net.MMPNetException;

/**
 * This class allows to use multiple clients binding asynchronously<br>
 * The responses should be returned once the last binding has been made.<br>
 * To use this class :<br>
 * $nbps;- builds ClientBinding and set parameters in calling class<br>
 * &nbsp;- adds some ClientBinding with MultiRequestsBinding.addClientBinding<br>
 * &nbsp;- gets responses with  MultiRequestsBinding.getResponses(int timeout)<br>
 *
 * Each response can be obtained using the same Key passed in MultiRequestsBinding.addRESTClientBinding.
 *
 * @author milletth
 */
public class MultiRequestsBinding implements MultiRequestsListener{

    private Hashtable<String, ClientBinding> clients;

    private HashMap<String, Object> responses;

    /**
     * Use to add a configured ClientBinding to the requests set
     * @param name The RESTClientBinding name (used to retrieve response)
     * @param clientBinding The ClientBinding instance (must be configured before)
     */
    public void addClientBinding(String name, ClientBinding clientBinding){
    	if(this.clients == null) this.clients = new Hashtable<String, ClientBinding>();
    	this.clients.put(name, clientBinding);
    }

    /**
     * Main method launching the requests asynchronously, when timeout is reached, requests which
     * has failed are aborted but responses already received are returned in the Collection.<br>
     * Each response can be obtained using the name parameter passed in  addRESTClientBinding.
     * @param timeout
     * @return
     */
    public synchronized HashMap<String, Object> getResponses(int timeout){
    	Iterator<String> clientsName = this.clients.keySet().iterator();
    	while(clientsName.hasNext()){
    		String currentName = clientsName.next();
    		ClientBinding currentClient = this.clients.get(currentName);
    		try{
    			currentClient.setTimeout(timeout);
    		}catch(MMPNetException mne){
    			//Nop - Just log
    		}
    		new AsyncClientBinding(currentName, currentClient,this);
    	}
    	try{
    		wait();
    	}catch(InterruptedException ie){
    		return null;
    	}
    	return responses;
    }


    /* (non-Javadoc)
     * @see com.francetelecom.rd.bbtm.pfs.bind.MultiRequestListener#onResponse(String, java.lang.Object)
     */
    public synchronized void onResponse(String reponseName, Object responseObject) {
    	if(this.responses == null) this.responses = new HashMap<String, Object>();
    	this.responses.put(reponseName, responseObject);
    }



    /* (non-Javadoc)
     * @see com.francetelecom.rd.bbtm.pfs.bind.MultiRequestsListener#isComplete()
     */
    public synchronized boolean isComplete() {
    	return (this.responses != null && this.clients != null && (this.responses.size() == this.clients.size()));
    }



    /**
     * Thread laubching the requests asynchronously
     * Notifitication is made on MultiRequestListener passed in constructor
     * @author milletth
     *
     */
    private class AsyncClientBinding extends Thread{

    	private String clientName;
    	private ClientBinding clientBinding;
    	private MultiRequestsListener listener;

		/**
		 * Default constructor
		 * @param clientName The name of the ClientBinding to execute
		 * @param clientBinding The ClientBinding linked to the clientName
		 * @param listener The container receiving the responses notification
		 */
		public AsyncClientBinding(String clientName, ClientBinding clientBinding, MultiRequestsListener listener){
		    this.clientName=clientName;
		    this.clientBinding=clientBinding;
		    this.listener=listener;
		    this.start();
		}

		/* (non-Javadoc)
		* @see java.lang.Thread#run()
		*/
		@Override
		public void run() {
		    Object response;
		    try{
		      response = this.clientBinding.getResponse(null);
		    }catch(BindingException e){
		      //Adds null to not blok others responses
		      response = null;
		    }
		    synchronized(this.listener) {
			this.listener.onResponse(this.clientName, response);
			if(this.listener.isComplete()) this.listener.notifyAll();
		    }
		}
    }
}
