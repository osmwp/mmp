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
package com.orange.mmp.mvc.ota;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.orange.mmp.context.ExecutionContext;
import com.orange.mmp.core.data.Service;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.data.DeliveryTicket;
import com.orange.mmp.core.data.Mobile;
import com.orange.mmp.core.data.Widget;
import com.orange.mmp.dao.Dao;
import com.orange.mmp.dao.DaoManagerFactory;
import com.orange.mmp.delivery.DeliveryManager;
import com.orange.mmp.log.LogManager;
import com.orange.mmp.log.LogManagerFactory;
import com.orange.mmp.log.requestmonitor.LogMonitoredRequest;
import com.orange.mmp.midlet.MidletManager;
import com.orange.mmp.mvc.Constants;
import com.orange.mmp.service.ServiceManager;
import com.orange.mmp.util.requestmonitor.MonitoredRequest;
import com.orange.mmp.util.requestmonitor.MonitoredRequestManager;
import com.orange.mmp.widget.WidgetManager;

/**
 * Servlet used for PFM delivery and update
 * @author milletth
 *
 */
public class Controller extends AbstractController {
    
    /**
     * The view used to display Internal error
     */
    private static String internalErrorView = null;

    /**
     * The view used to display GONE error
     */
    private static String goneView = null;

    /**
     * The view used to display CONFIRM page
     */
    private static String confirmView = null;

    /**
     * The view used to display NOT FOUND error
     */
    private static String notFoundView = null;

    /**
     * The view used to display DEFAULT DOWNLOAD page
     */
    private static String defaultView = null;

    /**
     * The URL mapping for OTA
     */
    private static String urlMapping;
    
    /**
     * The Locale resolver to handle messages bundles
     */
    @SuppressWarnings("unused")
    private LocaleResolver localeResolverBean;

    /* (non-Javadoc)
     * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	ModelAndView modelAndView = null;
    	
		ExecutionContext executionContext = ExecutionContext.newInstance(request);
		executionContext.setName("OTA Request");
		executionContext.executionStart();
		
		try {
			
	    	String userAgent = request.getHeader(Constants.HTTP_HEADER_USERAGENT);
	    	int pathInfoStart = request.getRequestURI().indexOf(Controller.urlMapping);
	    	String pathInfo = request.getRequestURI().substring(pathInfoStart+Controller.urlMapping.length());
	    	String[] pathTokens = pathInfo.split("/");
	    	
	    	Service service = ServiceManager.getInstance().getServiceByHostname(request);
	    	if(service == null) service = ServiceManager.getInstance().getDefaultService();
	    	
	    	String downloadKey = "";
	    	boolean useRedirect = false;
	
	    	//INTIALIZE MVC
	    	if(pathTokens.length > 0) {
	    		downloadKey = pathTokens[1];
	    		//Check if a redirect must be done before download
	    		if(downloadKey.indexOf(com.orange.mmp.midlet.Constants.JAD_FILE_EXTENSION) > 0){
	    			useRedirect = false;
	    			downloadKey = downloadKey.replaceAll(com.orange.mmp.midlet.Constants.JAD_FILE_EXTENSION, "");
	    		}
	    		else if(!pathInfo.endsWith(com.orange.mmp.midlet.Constants.JAR_FILE_EXTENSION)) useRedirect = true;
	    	}
	
	    	//MAIN MVC CONTROL
	    	try{
	    		//Check if a confirmation page must be displayed before any treatment
	    		if(request.getParameter(Constants.HTTP_PARAMETER_CONFIRM) != null){
	    			modelAndView = this.showConfirmationPage(downloadKey, service, request);
	    		}
	    		//Download JAR for installation or update
	    		else if(pathInfo.endsWith(com.orange.mmp.midlet.Constants.JAR_FILE_EXTENSION)){
	    			modelAndView = this.downloadJar(service, downloadKey, request, response);
	    		}
	    		//Must redirect before delivery (Windows Mobile workaround)
	    		else if(useRedirect){
	    			modelAndView = this.redirectJadDownload(service, downloadKey, userAgent, request, response);
	    		}
	    		//Download JAD File (or display default page if allowed)
	    		else{
	    			modelAndView = this.downloadJad(service, downloadKey, userAgent, request, response);
	    		}
	    	}catch(MMPException ioe){
	    		//Any IOException is interpreted as Not FOUND 404
	    		modelAndView = new ModelAndView(internalErrorView);
	    	}

		} catch (final Exception exception) {
			executionContext.addErrorMsg("Error during OTA download process.", exception);
			//Error : Return the error view
			modelAndView = new ModelAndView(goneView);
		} finally {
	    	executionContext.addInfoMsg("Return view name: " + (modelAndView!=null?modelAndView.getViewName():"-"));
			executionContext.executionStop();
			
			final MonitoredRequest monitoredRequest;
			monitoredRequest = MonitoredRequestManager.getInstance().getCurrentRequest();
			LogMonitoredRequest.getInstance().log(monitoredRequest);
			
			executionContext.close();
		}

		return modelAndView;
    }

    /**
     * Display a confirmation page for download
     * @param ticketId The ID of the delivery ticket
     * @param service The current sercice
     * @param request The incoming HTTP request
     * @return
     */
    @SuppressWarnings("unchecked")
    private ModelAndView showConfirmationPage(String ticketId, Service service, HttpServletRequest request){
    	ExecutionContext executionContext = ExecutionContext.getInstance();
    	executionContext.addInfoMsg("Show confirmation page. Ticket ID = " + ticketId + ", service = " + (service!=null?service.getId():"null"));
    	
    	StringBuilder newURL = new StringBuilder(ticketId);
        Enumeration<String> paramEnum = request.getParameterNames();
        boolean paramAdded = false;
        while(paramEnum.hasMoreElements()){
            String paramNam = paramEnum.nextElement();
            if(!paramNam.equals(Constants.HTTP_PARAMETER_CONFIRM)){
    	    	newURL.append((paramAdded)?"&":"?").append(paramNam).append("=").append(request.getParameter(paramNam));
    	    	paramAdded=true;
            }
        }

        ModelAndView modelAndView = new ModelAndView(confirmView);
        modelAndView.addObject("noSignOption", service.getSigned());
        modelAndView.addObject("url", newURL.toString());

        return modelAndView;
    }

    /**
     * Redirect client on same ticket using ".jad" extension (WM/JBed workaround)
     * @param ticketId The ID of the delivery ticket
     * @param userAgent The client UA
     * @param request The incoming HTTP request
     * @param response The outgoing HTTP response
     */
    @SuppressWarnings("unchecked")
	private ModelAndView redirectJadDownload(Service service, String ticketId, String userAgent, HttpServletRequest request, HttpServletResponse response) throws MMPException{
    	ExecutionContext executionContext = ExecutionContext.getInstance();
    	executionContext.addInfoMsg("Redirect JAD download. Ticket ID = " + ticketId + ", service = " + (service!=null?service.getId():"null"));

    	URL deliveryURL = null;
    	String signParameter = request.getParameter(Constants.HTTP_PARAMETER_SIGN);
    	Mobile currentMobile = new Mobile();
    	currentMobile.setUserAgent(userAgent);
    	Mobile mobiles[] = ((Dao<Mobile>)DaoManagerFactory.getInstance().getDaoManager().getDao("mobile")).find(currentMobile);
    	if(mobiles.length == 0) {
    		// Log UA if mobile not supported
    		LogManagerFactory.getInstance().getLogManager().log("unsupported_ua", LogManager.LEVEL_INFO, service.getId() + ";\"" + userAgent + "\"");
    		if(service.getUsedefault()){
    			try{
    				DeliveryTicket deliveryTicket = new DeliveryTicket();
    				deliveryTicket.setId(ticketId);
    				deliveryTicket.setUaKey(com.orange.mmp.midlet.Constants.DEFAULT_MOBILE);
    				deliveryTicket.setServiceId(service.getId());
    				deliveryURL = DeliveryManager.getInstance().getDeliveryTicketURL(deliveryTicket);
    			}catch(MMPException me){
    				// Ticket not found
    				ModelAndView modelAndView = new ModelAndView(goneView);
    				return modelAndView;
    			}
    			ModelAndView modelAndView =  new ModelAndView(defaultView);
    			StringBuilder newURL = new StringBuilder(ticketId).append(com.orange.mmp.midlet.Constants.JAD_FILE_EXTENSION);
    			if(newURL.indexOf("?") > 0) newURL.append("&").append(Constants.HTTP_PARAMETER_DEFAULT).append("=true");
    			else newURL.append("?").append(Constants.HTTP_PARAMETER_DEFAULT).append("=true");

    			if(signParameter != null){
    				newURL.append("&").append(Constants.HTTP_PARAMETER_SIGN).append("=").append(signParameter);
    			}
    			modelAndView.addObject("url", newURL.toString());
    			return modelAndView;
    		}
    		else{
    			ModelAndView modelAndView =  new ModelAndView(notFoundView);
    			return modelAndView;
    		}
    	}
    	else{
    		try{
    			DeliveryTicket deliveryTicket = new DeliveryTicket();
				deliveryTicket.setId(ticketId);
				deliveryTicket.setUaKey(mobiles[0].getKey());
				deliveryTicket.setServiceId(service.getId());
    			deliveryURL = DeliveryManager.getInstance().getDeliveryTicketURL(deliveryTicket);
    		}catch(MMPException me){
    			// Ticket not found
    	    	ModelAndView modelAndView = new ModelAndView(goneView);
                return modelAndView;
    		}
    	}
    	
    	try{
    		if(signParameter != null){
    			if(deliveryURL.toString().indexOf("?") > 0) response.sendRedirect(deliveryURL.toString().concat(com.orange.mmp.midlet.Constants.JAD_FILE_EXTENSION).concat("&").concat(Constants.HTTP_PARAMETER_SIGN).concat("=").concat(signParameter));
    			else response.sendRedirect(deliveryURL.toString().concat(com.orange.mmp.midlet.Constants.JAD_FILE_EXTENSION).concat("?").concat(Constants.HTTP_PARAMETER_SIGN).concat("=").concat(signParameter));
    		}
    		else response.sendRedirect(deliveryURL.toString().concat(com.orange.mmp.midlet.Constants.JAD_FILE_EXTENSION));
    	}catch(IOException ioe){
    		throw new MMPException("Failed to redirect client",ioe);
    	}

    	return null;
    }

    /**
     * Inner method used to send JAR file to client
     * @param service The service linked to the queried JAR
     * @param lang The lang of the midlet
     * @param mobileKey The mobile download key (usely the UA key)
     * @param response The HTTP response on which the file must be written
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
	private ModelAndView downloadJar(Service service, String mobileKey, HttpServletRequest request, HttpServletResponse response) throws MMPException{
    	ExecutionContext executionContext = ExecutionContext.getInstance();
    	executionContext.addInfoMsg("Download JAR. Service = " + (service!=null?service.getId():"null") + ", mobile key = " + mobileKey);

    	Mobile mobile = new Mobile();
    	mobile.setKey(mobileKey);
    	Mobile mobiles[] = ((Dao<Mobile>)DaoManagerFactory.getInstance().getDaoManager().getDao("mobile")).find(mobile);
    	if(mobiles.length == 0) mobile = null;
    	else mobile = mobiles[0];

    	ByteArrayOutputStream jarBytesBuffer = MidletManager.getInstance().getJar(service.getId(), mobile, false);
    	Widget widget = WidgetManager.getInstance().getWidget(service.getId(), mobile.getBranchId());
    	if(widget != null){
    		response.setHeader(Constants.HTTP_HEADER_CONTENDISPOSITION,"attachment; filename="+widget.getName()+".jar");
    	}
    	else{
    		ModelAndView modelAndView = new ModelAndView(internalErrorView);
    		return modelAndView;
    	}
    	response.setStatus(HttpServletResponse.SC_OK);
    	response.setContentType(com.orange.mmp.midlet.Constants.HTTP_HEADER_JARCONTENT);
    	response.setContentLength(jarBytesBuffer.size());
    	OutputStream output = null;  
    	try{
    		output = response.getOutputStream(); 
    		jarBytesBuffer.writeTo(output);
    	}catch(IOException ioe){
    		throw new MMPException("Failed to send JAR byte buffer",ioe);
    	}finally{
    		try{
    			if(jarBytesBuffer != null)jarBytesBuffer.close();
    			if(output != null)output.close();
    		}catch(IOException ioe){
    			//NOP
    		}
    	}
    	return null;
    }

    /**
     * Inner method used to send JAD file to client
     * @param service The service linked to the queried JAR
     * @param ticketId The delivery ticket ID
     * @param style The style of the midlet
     * @param lang The lang of the midlet
     * @param userAgent The mobile user agent
     * @param request The HTTp request if a page must be diplayed
     * @param response The HTTP response on which the file must be written
     * @return A ModelAndViex for Spring MVC
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
	private ModelAndView downloadJad(Service service, String ticketId, String userAgent, HttpServletRequest request, HttpServletResponse response) throws MMPException{
    	ExecutionContext executionContext = ExecutionContext.getInstance();
    	executionContext.addInfoMsg("Download JAD. Ticket ID = " + ticketId + ", service = " + (service!=null?service.getId():"null"));

    	String signParameter = request.getParameter(Constants.HTTP_PARAMETER_SIGN);

    	//Check if ticket exists
    	DeliveryTicket deliveryTicket = new DeliveryTicket();
    	try{
			deliveryTicket.setId(ticketId);
    		deliveryTicket = DeliveryManager.getInstance().getDeliveryTicket(deliveryTicket);
    		if(deliveryTicket == null) throw new IOException("Delivery ticket "+ticketId+" no found.");
    	}catch(IOException ioe){
    		//Ticket not found
    		ModelAndView modelAndView = new ModelAndView(goneView);
            return modelAndView;
    	}

    	//Test UA
    	Mobile mobile = new Mobile();
    	Mobile mobiles[] = null;
    	if(request.getParameter(Constants.HTTP_PARAMETER_DEFAULT) != null){
    		mobile.setKey(com.orange.mmp.midlet.Constants.DEFAULT_MOBILE);
    	}
    	else{
    		mobile.setUserAgent(userAgent);
    	}

    	mobiles = ((Dao<Mobile>)DaoManagerFactory.getInstance().getDaoManager().getDao("mobile")).find(mobile);
    	    	
    	//No mobile found, check in delivery ticket
    	if(mobiles.length == 0 && deliveryTicket.getUaKey() != null){
    		mobile = new Mobile();
    		mobile.setKey(deliveryTicket.getUaKey());
    	}
    	
    	mobiles = ((Dao<Mobile>)DaoManagerFactory.getInstance().getDaoManager().getDao("mobile")).find(mobile);

    	//Mobile not found
    	if(mobiles.length == 0){
    		ModelAndView modelAndView =  new ModelAndView(notFoundView);
    		return modelAndView;
    	}    	
    	//Get JAD content
    	else{
    		mobile = mobiles[0];
    		ByteArrayOutputStream jadBytesBuffer;
			boolean sign = false;
			if(service.getSigned()){
				//Bypass signature
				if(signParameter != null && signParameter.equals("false")) sign=false;
				//Active signature
				else sign = true;
			}
			else{
				//Force signature
				if(signParameter != null && signParameter.equals("true")) sign = true;
				//Bypass signature
				else sign = false;
			}
			jadBytesBuffer = MidletManager.getInstance().getJad(ticketId, mobile, sign, service);

    		//Get application metadata
			Widget widget = WidgetManager.getInstance().getWidget(deliveryTicket.getServiceId(), mobile.getBranchId());
    		if(widget != null){
    			response.setHeader(Constants.HTTP_HEADER_CONTENDISPOSITION,"attachment; filename="+widget.getName()+".jad");
    		}
    		else throw new MMPException("Application not found");  
    			
    		response.setStatus(HttpServletResponse.SC_OK);
    		response.setContentType(com.orange.mmp.midlet.Constants.HTTP_HEADER_JADCONTENT);
    		response.setContentLength(jadBytesBuffer.size());
    		OutputStream output = null;
    		try{
    			output = response.getOutputStream();
    			jadBytesBuffer.writeTo(output);
    		}catch(IOException ioe){
    			throw new MMPException("Failed to send JAD byte buffer",ioe);
    		}finally{
    			try{
    				if(jadBytesBuffer != null)jadBytesBuffer.close();
    				if(output != null)output.close();
    			}catch(IOException ioe){
    				//NOP
    			}
    		}

    		return null;
    	}
    }

    /**
     * @param localeResolverBean the localeResolverBean to set
     */
    public void setLocaleResolverBean(LocaleResolver localeResolverBean) {
        this.localeResolverBean = localeResolverBean;
    }

    /**
     * @param notFoundView the notFoundView to set
     */
    public void setNotFoundView(String notFoundView) {
	Controller.notFoundView = notFoundView;
    }

    /**
     * @param defaultView the defaultView to set
     */
    public void setDefaultView(String defaultView) {
        Controller.defaultView = defaultView;
    }

    /**
     * @param goneView the goneView to set
     */
    public void setGoneView(String goneView) {
        Controller.goneView = goneView;
    }

    /**
     * @param internalErrorView the internalErrorView to set
     */
    public void setInternalErrorView(String internalErrorView) {
        Controller.internalErrorView = internalErrorView;
    }

    /**
     * @param confirmView the confirmView to set
     */
    public void setConfirmView(String confirmView) {
        Controller.confirmView = confirmView;
    }

	/**
	 * @return the urlMapping
	 */
	public static String getUrlMapping() {
		return Controller.urlMapping;
	}

	/**
	 * @param urlMapping the urlMapping to set
	 */
	public void setUrlMapping(String urlMapping) {
		Controller.urlMapping = urlMapping;
	}

}
