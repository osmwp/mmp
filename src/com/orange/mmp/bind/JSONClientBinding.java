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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONTokener;

import com.orange.mmp.core.Constants;
import com.orange.mmp.net.MMPNetException;

/**
 * Generic client for JSON WebService.<br>
 * @author Thomas MILLET
 */

public class JSONClientBinding extends ClientBinding{


	/**
	 * Simple constructor used by IoC
	 */
	public JSONClientBinding(){

	}

	/**
	 * Default constructor dedicated to one interface endpoint
	 * @param epr The WebService endpoint
	 */
	public JSONClientBinding(String epr){
		this.epr = epr;
	}
	
	/**
	 * 	Sends the request to WebService and return an object built from
	 * 	JSON response.
	 * 	@return A binding object in a JSONObject, JSONArray or String
	 */
	public Object getResponse(InputStream postData) throws BindingException{
	    StringWriter responseContent = new StringWriter();
	    BufferedReader reader = null;
	    BufferedWriter writer = null;
	    //Get response
	    try{
	    	this.getHttpConnection().init(this.buildURL(), this.timeout);
	    	if(postData != null) this.getHttpConnection().sendData(postData);
			InputStream httpIn = this.getHttpConnection().getData();
			if(httpIn != null){
			    writer = new BufferedWriter(responseContent);
			    reader = new BufferedReader(new InputStreamReader(httpIn, Constants.DEFAULT_ENCODING));
			    String line = null;
			    while((line = reader.readLine()) != null){
					writer.write(line);
					writer.newLine();
					writer.flush();
			    }
			}
			else return null;
	
	    }catch(UnsupportedEncodingException uee){
			throw new BindingException("Unsupported encoding from "+this.epr+" : "+uee.getMessage());
		}catch(IOException ioe){
			throw new BindingException("Failed to get response : "+ioe.getMessage());
		}catch(MMPNetException mne){
			throw new BindingException("Failed to get response : "+mne.getMessage());
		} finally {
	    	try{
	    		if(reader != null) reader.close();
	    		if(writer != null) writer.close();
	    		this.releaseConnection();
	    	}catch(MMPNetException mne){
	    		//Nop, logs in AOP
	    	}catch(IOException ioe){
	    		//Nop, logs in AOP
	    	}
	    }

	    //Parse response
	    if(responseContent == null) throw new BindingException("Failed to parse response : empty result");
	    String jsonString = responseContent.toString();
	    if(jsonString.length() == 0) throw new BindingException("Failed to parse response : empty result");
    	try{
    		JSONTokener tokener = new JSONTokener(jsonString);
    		return tokener.nextValue();
    	}catch(JSONException je){
    		throw new BindingException("Failed to parse response : "+je.getMessage());
    	}
	}
}