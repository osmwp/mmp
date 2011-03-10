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
package com.orange.mmp.api.ws.rest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.orange.mmp.api.MMPApiException;
import com.orange.mmp.api.ApiContainer;
import com.orange.mmp.api.ApiContainerFactory;
import com.orange.mmp.context.RequestContext;
import com.orange.mmp.context.ResponseContext;
import com.orange.mmp.context.UserContext;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.data.Api;

/**
 * Abstraction class of a REST Request
 * 
 * @author tml
 *
 */
public class RestRequest {

	/**
	 * List of primitive types
	 */
	private static final String PRIMITIVE_TYPES = "boolean,byte,char,short,int,long,float,double";

	/**
	 * Index of Boolean type in PRIMITIVE_TYPES
	 */
	private static final int BOOLEAN_INDEX=0;
	
	/**
	 * Index of Byte type in PRIMITIVE_TYPES
	 */
	private static final int BYTE_INDEX=8;
	
	/**
	 * Index of Char type in PRIMITIVE_TYPES
	 */
	private static final int CHAR_INDEX=13;
	
	/**
	 * Index of Short type in PRIMITIVE_TYPES
	 */
	private static final int SHORT_INDEX=18;
	
	/**
	 * Index of Int type in PRIMITIVE_TYPES
	 */
	private static final int INT_INDEX=24;
	
	/**
	 * Index of Long type in PRIMITIVE_TYPES
	 */
	private static final int LONG_INDEX=28;
	
	/**
	 * Index of Float type in PRIMITIVE_TYPES
	 */
	private static final int FLOAT_INDEX=33;
	
	/**
	 * Index of Double type in PRIMITIVE_TYPES
	 */
	private static final int DOUBLE_INDEX=39;
	
	/**
	 * The name of the API
	 */
	private Api api;
	
	/**
	 * The name of the method
	 */
	private String methodName;
	
	/**
	 * The ordered list of parameters
	 */
	private Object[] params;

	/**
	 * The method return type
	 */
	@SuppressWarnings("unchecked")
	private Class returnType; 
	
	/**
	 * Private constructor
	 */
	private RestRequest(){
		//NOP
	}
	
	/**
	 * Get an instance of RestRequest from specified context
	 * 
	 * @param request The request context
	 * @param response The response context
	 * @return A new instance of RestRequest
	 * @throws MMPException
	 */
	@SuppressWarnings("unchecked")
	public static RestRequest newInstance(RequestContext request, ResponseContext response) throws MMPException{
		RestRequest restRequest = new RestRequest();
		
		ApiContainer serviceContainer = ApiContainerFactory.getInstance().getApiContainer();
		String []pathComponents = request.getPathInfo().split("/");
		
		restRequest.api = new Api();
		restRequest.api.setName(pathComponents[1]);
		restRequest.methodName = pathComponents[2];
		restRequest.api = serviceContainer.getApi(restRequest.api);
		
		List<String> paramsList = new ArrayList<String>();
		
		//Get params from path
		if(pathComponents.length < 3) throw new MMPException("Malformed REST request");
		else if(pathComponents.length > 3){
			for(int pathIndex=3; pathIndex<pathComponents.length; pathIndex++){
				paramsList.add(pathComponents[pathIndex]);
			}
		}
		
		//Find method
		Method method = serviceContainer.getApiMethod(restRequest.api, restRequest.methodName);
		Class []paramsClass = method.getParameterTypes();
		restRequest.returnType = method.getReturnType();
		
		//Check params
		restRequest.params = new Object[paramsClass.length];

		for(int classIndex = 0, paramIndex=0; classIndex < paramsClass.length; classIndex++){
			try{
				if(paramsClass[classIndex].isAssignableFrom(RequestContext.class)){
					restRequest.params[classIndex] = request;
				}
				else if(paramsClass[classIndex].isAssignableFrom(ResponseContext.class)){
					restRequest.params[classIndex] = new ResponseContext(response);
				}
				else if(paramsClass[classIndex].isAssignableFrom(UserContext.class)){
					HttpSession session = request.getSession(false);
					if(session != null){
						restRequest.params[classIndex] = (UserContext)session.getAttribute(UserContext.USER_CTX_STR_KEY);
		    		}
					if(restRequest.params[classIndex] == null){
						restRequest.params[classIndex] = request.getAttribute(UserContext.USER_CTX_STR_KEY);
					}
				}
				else if(paramIndex < paramsList.size()){
					if(paramsClass[classIndex].isPrimitive()){
						switch(PRIMITIVE_TYPES.indexOf(paramsClass[classIndex].getName())){
				 			case BOOLEAN_INDEX :
				 				restRequest.params[classIndex] = Boolean.parseBoolean(paramsList.get(paramIndex++));
				 			break;
				 			case BYTE_INDEX :
				 				restRequest.params[classIndex] = Byte.parseByte(paramsList.get(paramIndex++));
				 			break;
				 			case CHAR_INDEX :
				 				restRequest.params[classIndex] = paramsList.get(paramIndex++).charAt(0);
				 			break;
				 			case SHORT_INDEX :
				 				restRequest.params[classIndex] = Short.parseShort(paramsList.get(paramIndex++));
				 			break;
				 			case INT_INDEX :
				 				restRequest.params[classIndex] = Integer.parseInt(paramsList.get(paramIndex++));
				 			break;
				 			case LONG_INDEX :
				 				restRequest.params[classIndex] = Long.parseLong(paramsList.get(paramIndex++));
				 			break;
				 			case FLOAT_INDEX :
				 				restRequest.params[classIndex] = Float.parseFloat(paramsList.get(paramIndex++));
				 			break;
				 			case DOUBLE_INDEX :
				 				restRequest.params[classIndex] = Double.parseDouble(paramsList.get(paramIndex++));
				 			break;
						}
					}
					else restRequest.params[classIndex] = paramsList.get(paramIndex++);
				}
			}catch(ClassCastException cce){
				throw new MMPApiException("Parameter '"+paramsList.get(classIndex)+"' must be of Class '"+paramsClass[classIndex]+"'");	
			}
		}
		
		return restRequest;
	}
	
	/**
	 * @return the serviceName
	 */
	public Api getApi() {
		return api;
	}

	/**
	 * @return the methodName
	 */
	public String getMethodName() {
		return methodName;
	}


	/**
	 * @return the params
	 */
	public Object[] getParams() {
		return params;
	}

	/**
	 * @return the returnType
	 */
	@SuppressWarnings("unchecked")
	public Class getReturnType() {
		return returnType;
	}
}
