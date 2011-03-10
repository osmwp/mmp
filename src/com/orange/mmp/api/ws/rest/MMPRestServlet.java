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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.orange.mmp.api.ApiContainerFactory;
import com.orange.mmp.api.MMPApiException;

import com.orange.mmp.context.ExecutionContext;
import com.orange.mmp.context.RequestContext;
import com.orange.mmp.context.ResponseContext;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.log.requestmonitor.LogMonitoredRequest;
import com.orange.mmp.util.requestmonitor.MonitoredRequest;
import com.orange.mmp.util.requestmonitor.MonitoredRequestManager;


/**
 * REST Servlet specific to MMP using ServiceContainer
 * 
 * TODO Implements RestFul servlet (POST, PUT, DELETE)
 * 
 * @author Thomas MILLET
 *
 */
@SuppressWarnings("serial")
public class MMPRestServlet extends HttpServlet {	
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("deprecation")
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String errorType = RestResponse.RETURN_TYPE_BODY;
		RestRequest restRequest = null;
		RestResponse restResponse = null;
		ResponseContext responseContext = null;
		RequestContext requestContext = null;

		ExecutionContext executionContext = ExecutionContext.newInstance(request);
		executionContext.setName("REST Request");
		
		executionContext.executionStart();
		
		try{
			if (!(request instanceof RequestContext)){
				requestContext = new RequestContext(request);
			}
			else requestContext = (RequestContext)request;
			
			if (!(response instanceof ResponseContext)){
				responseContext = new ResponseContext(response);
			}
			else responseContext = (ResponseContext)response;
			
			if(request != null) {
				String requestPath = request.getRequestURL().toString();
				String[] requestPathTokens = requestPath.split("/");
				if(requestPathTokens.length > 1) {
					executionContext.setApiName(requestPathTokens[requestPathTokens.length - 2]);
					executionContext.setMethodName(requestPathTokens[requestPathTokens.length - 1]);
				}
			}
			
			//Build request
			restRequest = RestRequest.newInstance(requestContext,responseContext);
			if(restRequest.getApi() != null) errorType = restRequest.getApi().getErrorType();
			
			//Initialize execution context
			if(restRequest.getApi() != null) {
				executionContext.setApiName(restRequest.getApi().getName());
				executionContext.setMethodName(restRequest.getMethodName());
			}
			
			this.checkRequest(restRequest);

			//Invoke service
			Object responseObject = ApiContainerFactory.getInstance().getApiContainer().invokeApi(restRequest.getApi(), restRequest.getMethodName(), restRequest.getParams());
				
			//Send response
			restResponse = RestResponse.newInstance(requestContext, responseContext);
			restResponse.send(restRequest.getMethodName(), restRequest.getReturnType(), responseObject, errorType, response);

		} catch(Exception e){
			try{
				executionContext.addErrorMsg("Bad request", e);
				
				responseContext.sendError(ResponseContext.SC_BAD_REQUEST, e.getMessage()); 
				restResponse = RestResponse.newInstance(requestContext, responseContext);
				restResponse.send(null, Throwable.class, e, errorType, response);				
			}catch(MMPException mpe){
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mpe.getMessage());
			}			
		}
		finally{
			response.flushBuffer();
			executionContext.executionStop();
			
			printMonitoredRequest("");
			
			executionContext.close();
		}
	}
	
	/**
	 * Method to override to allow or not request processing
	 * 
	 * @param restRequest The incoming request
	 * @throws MMPApiException when a request is not allowed
	 */
	protected void checkRequest(RestRequest restRequest) throws MMPApiException{
		//NOP
	}
	
	/**
	 * Print the request
	 */
	private void printMonitoredRequest(String requestInfo) {
		MonitoredRequest request = MonitoredRequestManager.getInstance().getCurrentRequest();
		LogMonitoredRequest.getInstance().log(request);
	}
}

