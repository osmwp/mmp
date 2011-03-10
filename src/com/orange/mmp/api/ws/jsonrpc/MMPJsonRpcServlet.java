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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOExceptionWithCause;
import org.jabsorb.JSONRPCBridge;
import org.jabsorb.JSONRPCResult;
import org.jabsorb.JSONRPCServlet;
import org.json.JSONException;
import org.json.JSONObject;

import com.orange.mmp.context.ExecutionContext;
import com.orange.mmp.core.Constants;
import com.orange.mmp.log.requestmonitor.LogMonitoredRequest;
import com.orange.mmp.util.requestmonitor.MonitoredRequest;
import com.orange.mmp.util.requestmonitor.MonitoredRequestManager;

/**
 * JSON-RPC Servlet specific to MMP using SimpleJSONBridge
 * 
 * @author Thomas MILLET
 * 
 */
@SuppressWarnings("serial")
public class MMPJsonRpcServlet extends JSONRPCServlet {

	/**
	 * Used to override static init of superclass
	 */
	public static boolean isInitialized = false;

	/**
	 * Default JSON-RPC method to use if no parameter found (GET only)
	 */
	public static final String DEFAULT_METHOD = "system.listMethods";

	/**
	 * HTTP parameter for "id" parameter of JSON-RPC
	 */
	public static final String HTTP_PARAM_ID = "id";

	/**
	 * HTTP parameter for "id" parameter of JSON-RPC
	 */
	public static final String HTTP_PARAM_METHOD = "method";

	/**
	 * HTTP parameter for "id" parameter of JSON-RPC
	 */
	public static final String HTTP_PARAM_PARAM = "params";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jabsorb.JSONRPCServlet#service(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		
		ExecutionContext executionContext = ExecutionContext.newInstance(request);
		executionContext.setName("JSON-RCP Request");

		executionContext.executionStart();
		String requestInfo = "";
		try {
			
			// Use protected method in case someone wants to override it
			JSONRPCBridge json_bridge = findBridge(request);
	
			// Encode using UTF-8, although We are actually ASCII clean as
			// all unicode data is JSON escaped using backslash u. This is
			// less data efficient for foreign character sets but it is
			// needed to support naughty browsers such as Konqueror and Safari
			// which do not honour the charset set in the response
			response.setContentType("application/json");
			response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
			OutputStream out = response.getOutputStream();
	
			// Decode using the charset in the request if it exists otherwise
			// use UTF-8 as this is what all browser implementations use.
			// The JSON-RPC-Java JavaScript client is ASCII clean so it
			// although here we can correctly handle data from other clients
			// that do not escape non ASCII data
			String charset = request.getCharacterEncoding();
			if (charset == null) {
				charset = Constants.DEFAULT_ENCODING;
			}
	
			String receiveString = null;
	
			// Test HTTP GET
			if (request.getQueryString() != null) {
				String id = request.getParameter(HTTP_PARAM_ID);
				if (id != null) {
					executionContext.setApiName(id);
					
					StringBuilder receiveStringBuilder = new StringBuilder(
							"{\"id\":").append(id).append(",\"method\":\"");
					String method = request.getParameter(HTTP_PARAM_METHOD);
					// Get params
					if (method != null) {
						executionContext.setMethodName(method);
						
						receiveStringBuilder.append(method);
						String param = request.getParameter(HTTP_PARAM_PARAM);
						// There is parameters
						if (param != null) {
							receiveStringBuilder.append("\",\"params\":")
									.append(param).append("}");
						}
						// Empty params
						else {
							receiveStringBuilder.append("\",\"params\":[]}");
						}
					}
					// Default method (list API)
					else {
						receiveStringBuilder
								.append("system.listMethods\",\"params\":[]}");
					}
	
					// Set JSON-RPC call string
					receiveString = receiveStringBuilder.toString();

					//Trace request
					executionContext.setName("JSON-RCP Request: " + receiveString);
				}
			}
	
			// Test HTTP POST
			if (receiveString == null) {
				BufferedReader in = new BufferedReader(new InputStreamReader(
						request.getInputStream(), charset));
	
				// Read the request
				CharArrayWriter data = new CharArrayWriter();
				char buf[] = new char[4096];
				int ret;
				while ((ret = in.read(buf, 0, 4096)) != -1) {
					data.write(buf, 0, ret);
				}
	
				receiveString = data.toString();
				requestInfo = receiveString;
			}
	
			// Process the request
			JSONObject json_req;
			JSONRPCResult json_res;
			try {
				json_req = new JSONObject(receiveString);
				json_res = json_bridge.call(new Object[] { request, response },
						json_req);
			} catch (JSONException e) {
				json_res = new JSONRPCResult(JSONRPCResult.CODE_ERR_PARSE, null,
						JSONRPCResult.MSG_ERR_PARSE);
			}
	
			String sendString = json_res.toString();
	
			// Write the response
			byte[] bout = sendString.getBytes(Constants.DEFAULT_ENCODING);
	
			// if the request header says that the browser can take gzip compressed
			// output, then gzip the output
			// but only if the response is large enough to warrant it and if the
			// resultant compressed output is
			// actually smaller.
			String ae = request.getHeader("accept-encoding");
			if (ae != null && ae.indexOf("gzip") != -1) {
				byte[] gzippedOut = gzip(bout);
	
				// if gzip didn't actually help, abort
				if (bout.length > gzippedOut.length) {
					bout = gzippedOut;
					response.addHeader("Content-Encoding", "gzip");
				}
			}
	
			response.setIntHeader("Content-Length", bout.length);
	
			out.write(bout);
		
		} catch (Throwable error) {
			//Catch exception
			throw new IOExceptionWithCause("Error during request processing", error);
		} finally{
			executionContext.executionStop();
			printMonitoredRequest(requestInfo);
			executionContext.close();
		}
	}

	/**
	 * Gzip something.
	 * 
	 * @param in
	 *            original content
	 * @return size gzipped content
	 */
	private byte[] gzip(byte[] in) throws IOException {
		if (in != null && in.length > 0) {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			GZIPOutputStream gout = new GZIPOutputStream(bout);
			gout.write(in);
			gout.flush();
			gout.close();
			return bout.toByteArray();
		}
		return new byte[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jabsorb.JSONRPCServlet#findBridge(javax.servlet.http.HttpServletRequest
	 * )
	 */
	@Override
	protected JSONRPCBridge findBridge(HttpServletRequest request) {
		return SimpleJSONRPCBridge.getGlobalBridge();
	}
	
	/**
	 * Print the request
	 */
	private void printMonitoredRequest(String requestInfo) {
		MonitoredRequest request = MonitoredRequestManager.getInstance().getCurrentRequest();
		
		try {
			JSONObject requestJSON = new JSONObject(requestInfo);
			String[] apiInfo = requestJSON.getString("method").split("\\.");
			
			if(apiInfo.length > 1) {
				request.setApiName(apiInfo[0]);
				request.setMethodName(apiInfo[1]);
			}
			request.setInformation(requestJSON.getString("params"));
			
		} catch (Exception e) {
			// Do nothing
		}
		
		LogMonitoredRequest.getInstance().log(request);
	}
}
