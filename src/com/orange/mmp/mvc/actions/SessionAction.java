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
package com.orange.mmp.mvc.actions;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionSupport;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.mvc.model.SessionParams;
import com.orange.mmp.service.ServiceManager;

public class SessionAction extends ActionSupport {

	private static final long serialVersionUID = 1L;
	
	private SessionParams params;
	
	private String endPoint;
	
	/**
	 * ACTIONS
	 */
	@Override
	public String execute() throws Exception {
		//Init default session data
		this.params = new SessionParams();
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpSession session = request.getSession();
		URL httpEP = (URL)session.getAttribute("adminEndpoint");
		if(httpEP != null) {
			params.setPfsUrlEndpoint(httpEP.toString());
		} else {
			//Build default PFS URL
			try {
				URL defaultUrl = new URL("http",ServiceManager.getInstance().getDefaultService().getHostname(),"/");
				params.setPfsUrlEndpoint(defaultUrl.toString());
			}catch(MMPException me){
				addActionError(getText("error.session.params", new String[] {me.getLocalizedMessage()}));
			} catch(MalformedURLException mue) {
				addActionError(getText("error.session.params", new String[] {mue.getLocalizedMessage()}));
			}
		}
		return SUCCESS;
	}
	
	public String modifyParam() throws Exception {
		if(getEndPoint() == null || getEndPoint().equals("")) {
			addActionError(getText("error.session.params", new String[] {getText("error.session.params.noparams", new String[] {})}));
			return this.execute();
		}
		HttpServletRequest request = ServletActionContext.getRequest();
		try {
			request.getSession().setAttribute("adminEndpoint", new URL(getEndPoint())); 
		} catch (MalformedURLException murle) {
			addActionError(getText("error.session.params", new String[] {murle.getLocalizedMessage()}));
			
		}
		
		return this.execute();	
	}

	/**
	 * END ACTIONS
	 */
	
	/**
	 * GETTERS/SETTERS
	 */
	
	/**
	 * @return the params
	 */
	public SessionParams getParams() {
		return params;
	}

	/**
	 * @return the endPoint
	 */
	public String getEndPoint() {
		return endPoint;
	}

	/**
	 * @param endPoint the endPoint to set
	 */
	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}

}
