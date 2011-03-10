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
package com.orange.mmp.api.ws.jsonrpc;

import org.jabsorb.JSONRPCBridge;
import org.jabsorb.JSONRPCResult;
import org.json.JSONObject;

import com.orange.mmp.api.MMPApiException;
import com.orange.mmp.api.ApiContainerFactory;
import com.orange.mmp.api.ApiEvent;
import com.orange.mmp.api.ApiObserver;
import com.orange.mmp.core.ApplicationListener;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.MMPRuntimeException;
import com.orange.mmp.core.data.Api;

/**
 * JSON-RPC bridge specific to MMP using SimpleJSONSerializer
 * Even if remains empty, this class override static calls of JSONRPCBridge
 * 
 * @author Thomas MILLET
 *
 */
@SuppressWarnings("serial")
public class SimpleJSONRPCBridge extends org.jabsorb.JSONRPCBridge implements ApplicationListener, ApiObserver {
    
    /**
     * Global Public bridge
     */
    private static JSONRPCBridge publicGlobalBridge ;
    
    /**
     * Global Private bridge
     */
    private static JSONRPCBridge privateGlobalBridge ;
    
    /* (non-Javadoc)
     * @see org.jabsorb.JSONRPCBridge#call(java.lang.Object[], org.json.JSONObject)
     */
    @Override
    public JSONRPCResult call(Object[] context, JSONObject jsonReq) {
    	JSONRPCResult jsonRpcResult = super.call(context, jsonReq);
    	if(jsonRpcResult.getErrorCode() == JSONRPCResult.CODE_REMOTE_EXCEPTION){
    		return new JSONRPCResult(500,jsonRpcResult.getId(),jsonRpcResult.getResult());
    	}
    	return jsonRpcResult;
    }
    
    /**
     * Get the public global Bridge
     * 
     * @return The Public JSONRPCBridge
     */
    public static JSONRPCBridge getPublicGlobalBridge(){
    	return publicGlobalBridge;
    }
    
    /**
     * Get the private global Bridge
     * 
     * @return The Private JSONRPCBridge
     */
    public static JSONRPCBridge getPrivateGlobalBridge(){
    	return privateGlobalBridge;
    }
    
    /**
     * Override JSONRPCBridge to get thje private Bridge
     * 
     * @return The Private JSONRPCBridge
     */
    public static JSONRPCBridge getGlobalBridge(){
    	return getPrivateGlobalBridge();
    }
    
	public void initialize() throws MMPException {
		try{
			SimpleJSONRPCBridge.setSerializer(new SimpleJSONSerializer());
			SimpleJSONRPCBridge.getSerializer().registerDefaultSerializers();
		}catch(Exception e){
			throw new MMPRuntimeException("Failed to intialize JSON-RPC bridge",e);
		}
		
    	publicGlobalBridge = new SimpleJSONRPCBridge();
    	privateGlobalBridge = new SimpleJSONRPCBridge();
    	ApiContainerFactory.getInstance().getApiContainer().registerApiObserver(this);
	}

	public void shutdown() throws MMPException {
		ApiContainerFactory.getInstance().getApiContainer().unregisterApiObserver(this);
		publicGlobalBridge = null;
		privateGlobalBridge = null;
	}
    
    /**
     * Add/remove api to JSONBridge singleton
     */
	public void onApiEvent(ApiEvent apiEvent) {
		Api api = apiEvent.getApi();
		switch(apiEvent.getType()){
			case ApiEvent.API_ADDED :
				//If api declare JSON-RPC, add it to SimpleJSONRPCBridge
				if(api.isPublished()){
					try{
						Object apiImpl = ApiContainerFactory.getInstance().getApiContainer().getApiImplementation(api);
						if(api.isPublic()) SimpleJSONRPCBridge.getPublicGlobalBridge().registerObject(api.getName(), apiImpl, api.getDefinitionClass());
						if(api.isPrivate()) SimpleJSONRPCBridge.getPrivateGlobalBridge().registerObject(api.getName(), apiImpl, api.getDefinitionClass());
					}catch(MMPApiException ce){
						//NOP Logged
					}
				}
			break;
			case ApiEvent.API_REMOVED :
				//If api declare JSON-RPC, remove it from SimpleJSONRPCBridge
				if(api.isPublished()){
					if(api.isPublic()) SimpleJSONRPCBridge.getPublicGlobalBridge().unregisterObject(api.getName());
					if(api.isPrivate()) SimpleJSONRPCBridge.getPrivateGlobalBridge().unregisterObject(api.getName());
				}
			break;
		}
	}
}
