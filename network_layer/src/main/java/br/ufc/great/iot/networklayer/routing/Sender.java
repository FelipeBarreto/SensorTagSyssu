package br.ufc.great.iot.networklayer.routing;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.util.Log;
import br.ufc.great.iot.networklayer.base.NetworkContainer;
import br.ufc.great.iot.networklayer.exception.NoActiveNetworkException;
import br.ufc.great.iot.networklayer.routing.message.RoutingMessage;
import br.ufc.great.iot.networklayer.routing.message.UserDataMessage;
import br.ufc.great.iot.networklayer.util.Debug;
import br.ufc.great.somc.networklayer.BuildConfig;

public class Sender implements Runnable{

	protected RoutingEventsNotifier notifier;
	private NetworkContainer netwoksAvaliable;
	protected Queue<UserDataMessage> dataMessageToForward;
	protected Queue<RoutingMessage> routingMessageToForward;
	protected final Object queueLock = new Integer(0);
	protected volatile boolean keepRunning = true;
	private Thread senderThread;
	
	
	public Sender(RoutingEventsNotifier  notifier) {
		super();
		this.notifier = notifier;
		this.netwoksAvaliable = NetworkContainer.getInstance();
		dataMessageToForward = new ConcurrentLinkedQueue<UserDataMessage>();
		routingMessageToForward = new ConcurrentLinkedQueue<RoutingMessage>();		
		
	}

	public void startThread(){
    	keepRunning = true;
    	senderThread = new Thread(this);
    	senderThread.setName("Sender");
    	senderThread.start();
    }
    
    public void stopThread(){
    	keepRunning = false;
       	senderThread.interrupt();
    }
    

	@Override
	public void run() {
		while(keepRunning)
		{
			try
			{
				synchronized (queueLock)
				{
					while(routingMessageToForward.isEmpty() && dataMessageToForward.isEmpty())
					{
						queueLock.wait();
					}
				}
				
				
	        	//Handles messages user data messages (received by other nodes) that are to be forwarded
	    		UserDataMessage userData = dataMessageToForward.peek();
	    		while(userData != null){
	    			sendMessage(userData);
	    			//it is expected that the queue still has the same userDataHeader object as head
	    			dataMessageToForward.poll();
	    			userData = dataMessageToForward.peek();
	    		}
	    		
	    		// Handle protocol messages
	    		RoutingMessage packet = routingMessageToForward.poll();
	    		while(packet != null){
	    			sendMessage(packet);		    		
		    		packet = routingMessageToForward.poll();
	    		}
			}
			catch (InterruptedException e) {

			}
		}
	}


	public void queueRoutingMessage(RoutingMessage message)
	{
		routingMessageToForward.add(message);
		//Log.d(getClass().getName(), "Message added " +  message.toString());
		synchronized (queueLock) {
			Log.d(getClass().getName(), "notifing about new message");
			queueLock.notify();
		}
	}
	
	public void queueDataMessageToForward(UserDataMessage message)
	{
		dataMessageToForward.add(message);
		synchronized (queueLock) {
			queueLock.notify();
		}
	}
	
	
	
	protected final boolean sendMessage(RoutingMessage routingMsg)
	{		
		if(routingMsg.getNextHop() == null)
		{	
			if (BuildConfig.DEBUG) {
				Log.d(getClass().getName(),
						"Proximo salto para "+ routingMsg.getDestinationAddress() + " vazio!!!");
			}
			return false;
		}
		
		try {
			boolean sent = netwoksAvaliable.sendMessage(routingMsg);
			Debug.debugMessage(routingMsg, null, false, sent);
			return sent;
		} catch (NoActiveNetworkException e) {
			e.printStackTrace();
			notifier.onExceptionOcurred(0, "No network avaliable");
			return false;
		}
		
		
	}
	
}
