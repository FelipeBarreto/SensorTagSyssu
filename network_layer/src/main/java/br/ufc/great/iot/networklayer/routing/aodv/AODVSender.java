package br.ufc.great.iot.networklayer.routing.aodv;


import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.util.Log;
import br.ufc.great.iot.networklayer.routing.IRouter;
import br.ufc.great.iot.networklayer.routing.RoutingEventsNotifier;
import br.ufc.great.iot.networklayer.routing.Sender;
import br.ufc.great.iot.networklayer.routing.aodv.exception.AODVException;
import br.ufc.great.iot.networklayer.routing.aodv.exception.NoSuchRouteException;
import br.ufc.great.iot.networklayer.routing.aodv.message.AODVMessage;
import br.ufc.great.iot.networklayer.routing.aodv.message.RouteRequest;
import br.ufc.great.iot.networklayer.routing.exception.TimeToLiveExpiredException;
import br.ufc.great.iot.networklayer.routing.message.RoutingMessage;
import br.ufc.great.iot.networklayer.routing.message.UserDataMessage;
import br.ufc.great.iot.networklayer.util.Debug;
import br.ufc.great.somc.networklayer.BuildConfig;

public class AODVSender extends Sender{

	private Queue<UserDataMessage> myDataMessage;
	private boolean isRREQsent = false;
	private AODV aodv;
	
	
	public AODVSender(RoutingEventsNotifier  notifier, AODV aodv) {
		super(notifier);
		myDataMessage = new ConcurrentLinkedQueue<UserDataMessage>();
		this.aodv = aodv;
	}


	@Override
	public void run() {
		while(keepRunning)
		{
			try
			{
				synchronized (queueLock)
				{
					while(routingMessageToForward.isEmpty() && dataMessageToForward.isEmpty() && 
							(isRREQsent || myDataMessage.isEmpty()))
					{
						queueLock.wait();
					}
				}
				
				
				//Handle user data messages that is to be sent from this node
	        	if(!isRREQsent ){
		        	if(!myDataMessage.isEmpty()){
			    		UserDataMessage userData =  myDataMessage.peek();
			    		while(userData != null){
			    			
			    			if(!sendDataPacket(userData)){
			    				isRREQsent = true;	
			    				break;
			    			}	else {
				    			notifier.onSentMessage();
			    			}

			    			//it is expected that the queue still has the same userDataHeader object as head
			    			myDataMessage.poll();
			    			userData = myDataMessage.peek();
			    		}
		        	}
	        	}
	        	
	        	
	        	//Handles messages user data messages (received by other nodes) that are to be forwarded
	    		UserDataMessage userData = dataMessageToForward.peek();
	    		while(userData != null){
	    			if(!sendDataPacket(userData))
	    			{
	    				break;
	    			}
	    			//it is expected that the queue still has the same userDataHeader object as head
	    			dataMessageToForward.poll();
	    			userData = dataMessageToForward.peek();
	    		}
	    		
	    		// Handle protocol messages
	    		RoutingMessage packet = routingMessageToForward.poll();
	    		while(packet != null){
	    			sendAODVPacket(packet);
		    		packet = routingMessageToForward.poll();
	    		}
			}
			catch (InterruptedException e) {

			}
		}
	}	
	
	public void queueMyDataMessage(UserDataMessage message)
	{
		myDataMessage.add(message);
		synchronized (queueLock) {
			queueLock.notify();
		}
	}
	
	public void onNewNodeReachable(String destinationAddress)
	{		
		notifier.onNewNodeReachable(destinationAddress);
		UserDataMessage userPacket = myDataMessage.peek();
				
		if(userPacket != null &&
				userPacket.getDestinationAddress().equalsIgnoreCase(destinationAddress)){
			isRREQsent = false;
			synchronized (queueLock) {
				queueLock.notify();
			}
		}
	}
	
	public void onRouteEstablishmentFailure(String destinationAddress)
	{	
		if (BuildConfig.DEBUG) {
			StringBuffer message = new StringBuffer("Route Establishment Faliure - D ");
			message.append(destinationAddress);			
			Debug.debugMessage(message);
			Log.d(this.getClass().getName(), message.toString());
		}
		cleanMyUserDataPackets(destinationAddress);
		isRREQsent = false;
		synchronized (queueLock) {
			queueLock.notify();
		}		
	}
	
	/**
     * Removes every message from the user packet queue that matches the given destination
     * @param destinationAddress the destination which to look for
     */
    private void cleanMyUserDataPackets(String destinationAddress){
    	int i = 0;
    	
    	synchronized (myDataMessage) {
			for(UserDataMessage msg: myDataMessage){
				if(msg.getDestinationAddress().equalsIgnoreCase(destinationAddress)){
					myDataMessage.remove(msg);
					i++;
				}
			}
		}
    	
    	if (BuildConfig.DEBUG) {
    		StringBuffer msg = new StringBuffer("Limpando minhas mgs -> " + i);
    		Log.d(AODVSender.class.getName(), msg.toString());
    		Debug.debugMessage(msg);
		}
    	
    }
    
    private void cleanUserDataPacketsToForward(String destinationAddress){
    	if (BuildConfig.DEBUG) {
			Log.d("invalidating", "limpando fila de pacotes para encaminhar");
		}
    	synchronized (dataMessageToForward) {
			for(UserDataMessage msg: dataMessageToForward){
				if(msg.getDestinationAddress().equalsIgnoreCase(destinationAddress)){
					dataMessageToForward.remove(msg);
					Log.d("invalidating", "pacote removed");
				}
			}
		}
    }
    
    private boolean sendAODVPacket(RoutingMessage message)
    {
    	if(message.getType().equalsIgnoreCase(AODVMessage.AODV_MESSAGE))
    	{
    		
    		String nextHop = null;
    		try {
    			String destinationAddress = null;
    			switch(((AODVMessage)message).getMessageType())
    			{
    				case RREQ:			
    					if(message.getSourceAddress() != null)
    					{
    						((RouteRequest) message).decrementTtl();
    					}
    				case RRER:
    					destinationAddress = message.getDestinationAddress();
    				break;
    				
    				case RREP:
    					destinationAddress = message.getSourceAddress();
    				break;
    				
    				
    			} 
				if(!IRouter.isBrodcastMessage(message))
				{
					nextHop = aodv.getNextHopTo(destinationAddress);
					message.setNextHop(nextHop);
				}						
    		} catch (TimeToLiveExpiredException e) {
    			Log.d("rreq", "TTL EXPIRED");
				notifier.onExceptionOcurred(-3, "TTL RREQ Exception");
				return false;
			} catch (AODVException e) {
				e.printStackTrace();
				if (BuildConfig.DEBUG) {
					Log.d(getClass().getName(), "Não deveria entrar aqui - SendAODVPacket");
				}
				return false;    	
			}
    	}
    	return sendMessage(message);
    }
    
	private boolean sendDataPacket(UserDataMessage message)
	{		
		String nextHop = null;		
		if(IRouter.isBrodcastMessage(message))
		{
			return sendMessage(message);
		}	
		
		try {
			nextHop = aodv.getNextHopTo(message.getDestinationAddress());
			message.setNextHop(nextHop);
			return sendMessage(message);
		} catch (AODVException e) {
			//e.printStackTrace();
			try {
				int lastKnownSequenceNumber = aodv.getLastKnownSequenceNumber(message.getDestinationAddress());
				if(message.getSourceAddress() == null)
				{
					aodv.queueNewRouteRequest(message.getDestinationAddress(), lastKnownSequenceNumber);
				} 
				else
				{					
					aodv.queueRouteErrorMessage(message.getDestinationAddress(),
							lastKnownSequenceNumber, message.getSourceAddress());
					cleanUserDataPacketsToForward(message.getDestinationAddress());
				}
			} catch (NoSuchRouteException e1) {
				//e1.printStackTrace();
				if(message.getSourceAddress() == null)
				{
					aodv.queueNewRouteRequest(message.getDestinationAddress(), Constants.UNKNOWN_SEQUENCE_NUMBER);
				} 
				else
				{
					aodv.queueRouteErrorMessage(message.getDestinationAddress(),
							Constants.UNKNOWN_SEQUENCE_NUMBER, message.getSourceAddress());
					cleanUserDataPacketsToForward(message.getDestinationAddress());
				}
			}
			return false;
		}
		
		
	}

	
}
