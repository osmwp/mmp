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
package com.orange.mmp.delivery;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import com.orange.mmp.core.ApplicationListener;
import com.orange.mmp.core.MMPException;
import com.orange.mmp.core.MMPRuntimeException;
import com.orange.mmp.core.data.DeliveryTicket;
import com.orange.mmp.core.data.Midlet;
import com.orange.mmp.core.data.Mobile;
import com.orange.mmp.core.data.Service;
import com.orange.mmp.core.data.Version;
import com.orange.mmp.core.data.Widget;
import com.orange.mmp.dao.DaoManagerFactory;
import com.orange.mmp.dao.MMPDaoException;
import com.orange.mmp.mvc.ota.Controller;
import com.orange.mmp.service.ServiceManager;
import com.orange.mmp.widget.WidgetManager;

/**
 * Bean handling Midlets delivery
 * @author milletth
 *
 */
public class DeliveryManager implements Runnable,ApplicationListener{

	/**
	 * Singleton for access outside application context
	 */
	private static DeliveryManager deliveryManagerSingleton = null;
	
    /**
     * The delay in minutes to destroy TMP files handling new token
     */
    private int destroyTokenDelay = 60;

    /**
     * Indicates if update handler is running
     */
    private boolean isRunning;
    
	/**
	 * Singleton access 
	 * 
	 * @return The DeliveryManager singleton
	 */
	public static DeliveryManager getInstance(){
		return DeliveryManager.deliveryManagerSingleton;
	}

    /**
     * Starts the Delivery Manager
     */
	public void initialize() throws MMPException{
    	if(!this.isRunning){
    		deliveryManagerSingleton = this;
    					
			if(this.destroyTokenDelay > 0){
				 Thread ticketTread = new Thread(this);
				 ticketTread.setDaemon(true);
				 ticketTread.start();
			}			
    	}
    }

    /**
     * Stops the Delivery Manager
     */
	public void shutdown() throws MMPException{
        if(this.isRunning){
        	deliveryManagerSingleton = null;
            this.isRunning=false;
        }
    }

    /********************************
     * DELIVERY
     *******************************/

	/**
	 * Creates/updates a temporary file with specifed DeliveryTicket stub
	 * @param deliveryTicket The DeliveryTicket bind to this URL
	 * @param token The User's Token
	 * @param mobileNumber The user's mobile number (null for no SMS notification)
	 * @param uaKey The User agent key (workaround for Windows Mobile)
	 * @param service The curent used service
	 * @return An URL for PFM download
	 * @throws IOException
	 */
	public URL getDeliveryTicketURL(DeliveryTicket deliveryTicket) throws MMPException{
		if(deliveryTicket == null || deliveryTicket.getId() == null){
			throw new MMPException("Missing ticket or ticketId in parameter");
		}

         //Search existing ticket
       	deliveryTicket = this.getDeliveryTicket(deliveryTicket);
       	if(deliveryTicket == null){
       	    throw new MMPException("Failed to find ticket");
       	}

       	Service deliveryTicketService = ServiceManager.getInstance().getServiceById(deliveryTicket.getServiceId());
       	
       	try{
         	return new URL("http",deliveryTicketService.getHostname(),Controller.getUrlMapping().concat("/").concat(deliveryTicket.getId()));
         }catch(MalformedURLException mue){
         	throw new MMPException(mue);
         }
 }
	
 	/**
 	 * Creates a delivery ticket
	 * @return The new delivery ticket
	 */
	@SuppressWarnings("unchecked")
	public DeliveryTicket createDeliveryTicket() throws MMPException{
		 return (DeliveryTicket)DaoManagerFactory.getInstance().getDaoManager().getDao("ticket").createOrUdpdate(new DeliveryTicket());
	 }
	
    /**
     * Gets a delivery ticket from stub
     * @param deliveryTicket The ticket to get 
     * @return The delivery ticket or NULL if not found
     */
    @SuppressWarnings("unchecked")
	public DeliveryTicket getDeliveryTicket(DeliveryTicket deliveryTicket) throws MMPException{
       	DeliveryTicket[] tickets = (DeliveryTicket[])DaoManagerFactory.getInstance().getDaoManager().getDao("ticket").find(deliveryTicket);
       	if(tickets != null && tickets.length > 0) return tickets[0];
        return null;
    }
    
    /**
     * Update a delivery ticket
     * @param deliveryTicket The ticket to update
     * @return The delivery ticket or NULL if not found
     */
    @SuppressWarnings("unchecked")
	public void updateDeliveryTicket(DeliveryTicket deliveryTicket) throws MMPException{
    	DaoManagerFactory.getInstance().getDaoManager().getDao("ticket").createOrUdpdate(deliveryTicket);
    }

    /**
     * Get an URL to update a PFM based on specified parameters
     * @param service The current service
     * @param mobile The current mobile
     * @return An URL fo call for update, null if not available
     */
    public URL getPlayerUpdateUrl(Service service, Mobile mobile, Map<String,String> serviceEntries ) throws MMPException {
        if(service != null && mobile != null){
        	DeliveryTicket deliveryTicket = new DeliveryTicket();
            deliveryTicket.setUaKey(mobile.getKey());
            deliveryTicket.setServiceId(service.getId());
            deliveryTicket.setServiceSpecific(serviceEntries);
        	URL dwnUrl = this.getDeliveryTicketURL(deliveryTicket);
            StringBuilder updateUrlValue = new StringBuilder(dwnUrl.toString());
            updateUrlValue.append("?").append(com.orange.mmp.mvc.Constants.HTTP_PARAMETER_CONFIRM).append("=").append("1");
            try{
            	return new URL(updateUrlValue.toString());
            }catch(MalformedURLException mue){
            	throw new MMPException("Failed to build update URL",mue);
            }
        }
        else throw new MMPException("Missing token, service or mobile");
    }
    
    
    
    /**
     * Get an URL to update a service based on specified parameters
     * @param service	The current service
     * @param mobile The current mobile
     * @return An URL to call for update, null if not available
     */
    public URL getServiceUpdateUrl(Service service, Mobile mobile) throws MMPException {
        if(service != null && mobile != null) {
            return WidgetManager.getInstance().getWidgetResourceUrl(service,"/m4m/main.m4m",service.getId(),mobile.getBranchId());
        }
        else throw new MMPException("Misssing mobile or service");
    }
    
    /**
     * Compares version and indicates if the PFM must be updated
     * @param mobile	The current mobile
     * @param service The current service
     * @param playerVersion The current player version
     * @return true if PFM must be updated, false otherwise
     */
    @SuppressWarnings("unchecked")
	public boolean mustUpdatePlayer(Mobile mobile, Service service, Version playerVersion) throws MMPException {
        if(mobile == null 
        		|| service == null 
        		|| playerVersion == null){
            throw new MMPException("Missing information in headers");
        }
		Midlet midlets[] = null;
		Midlet midlet = new Midlet();
		midlet.setType(mobile.getMidletType());
		midlets = (Midlet[])DaoManagerFactory.getInstance().getDaoManager().getDao("midlet").find(midlet);
		if(midlets.length == 0) throw new MMPException("Midlet not found : '"+mobile.getMidletType()+"'");
		
        return midlets[0].getVersion().compareTo(playerVersion) > 0;
    }
    
    /**
     * Compares version and indicates if the service must be updated
     * @param service The current service
     * @param mobile The current mobile
     * @param serviceVersion The current service version
     * @return true if service must be updated, false otherwise
     */
    public boolean mustUpdateService(Service service, Mobile mobile, Version serviceVersion) throws MMPException {
    	if(service == null ||  serviceVersion == null){
            throw new MMPException("Missing information in headers");
        }
        Widget serviceWidget = WidgetManager.getInstance().getWidget(service.getId(), mobile.getBranchId());
        if(serviceWidget == null) return false;

        return serviceWidget.getVersion().compareTo(serviceVersion) > 0;
    }

    /********************************
     * INTERNAL
     *******************************/

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @SuppressWarnings("unchecked")
	public void run() {
        long destroyTokenDelayMS = ((long)destroyTokenDelay)*60000;

        try{
            while(this.isRunning){
                long maxTime = System.currentTimeMillis()-destroyTokenDelayMS;
                try{
                    DeliveryTicket[] allTickets = (DeliveryTicket[])DaoManagerFactory.getInstance().getDaoManager().getDao("ticket").find(new DeliveryTicket());
                    for(DeliveryTicket currentTicket : allTickets){
                    	if(currentTicket.getCreationDate() < maxTime){
                    		DaoManagerFactory.getInstance().getDaoManager().getDao("ticket").delete(currentTicket);

                    	}
                    }
                }catch(MMPDaoException mde){
                    //Nop just alert in logs
                }
                    Thread.sleep(60000);
                }
        }catch(InterruptedException ie){
            throw new MMPRuntimeException("Failed to refresh DeliveryManager",ie);
        }
    }

    /**
     * @param destroyTokenDelay the destroyTokenDelay to set
     */
    public void setDestroyTokenDelay(int destroyTokenDelay) {
        this.destroyTokenDelay = destroyTokenDelay;
    }
}
