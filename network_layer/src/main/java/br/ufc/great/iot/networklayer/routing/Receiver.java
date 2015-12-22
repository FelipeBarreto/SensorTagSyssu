package br.ufc.great.iot.networklayer.routing;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import br.ufc.great.iot.networklayer.routing.message.HelloMessage;
import br.ufc.great.iot.networklayer.routing.message.RoutingMessage;
import br.ufc.great.iot.networklayer.routing.message.UserDataMessage;

public class Receiver implements Runnable{

	private RoutingEventsNotifier notifier;
	private volatile boolean keepRunning = true;
	private Queue<RoutingMessage> receivedRoutingMessages;
	private Queue<UserDataMessage> receivedDataMessages;
	private Thread receiverThread;
	protected final Object queueLock = new Integer(0);
	private final String applicationID;
	
	public Receiver(String applicationID, RoutingEventsNotifier notifier) {
		super();
		this.applicationID = applicationID;
		this.notifier = notifier;
		this.receivedRoutingMessages = new ConcurrentLinkedQueue<RoutingMessage>();
		this.receivedDataMessages = new ConcurrentLinkedQueue<UserDataMessage>();
	}

	
	public void startThread(){
    	keepRunning = true;
    	receiverThread = new Thread(this);
    	receiverThread.setName("Receiver");
    	receiverThread.start();
    }
    
    public void stopThread(){
    	keepRunning = false;
       	receiverThread.interrupt();
    }

	@Override
	public void run() {
		while(keepRunning)
		{
			try {
				
					synchronized(queueLock){
						while(receivedRoutingMessages.isEmpty() && receivedDataMessages.isEmpty())
						{
							queueLock.wait();
						}
					}
			
					if(receivedDataMessages.isEmpty() != true)
					{
						UserDataMessage dataMsg = receivedDataMessages.poll();
						dataReceived(dataMsg);
					}
					
					if(receivedRoutingMessages.isEmpty() != true)
					{
						RoutingMessage msg = receivedRoutingMessages.poll();
						if(msg.getType().equalsIgnoreCase(HelloMessage.class.getCanonicalName()))
						{
							helloMessageReceived((HelloMessage) msg);
						} else {
							routingDataReceived(msg);
						}
					}
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
			
	}


	
	private void dataReceived(UserDataMessage message) {
		notifier.onUserDataReceived(message);
	}
	
	private void helloMessageReceived(HelloMessage hello) {
		notifier.onHelloMessageReceived(hello);
	}
	
	private void routingDataReceived(RoutingMessage message)
	{
		
	}
	
	public void queueMesssage(RoutingMessage message)
	{
		receivedRoutingMessages.add(message);
		synchronized (queueLock) {
			queueLock.notify();
		}
	}
	
	public void queueDataMesssage(UserDataMessage message)
	{
		receivedDataMessages.add(message);
		synchronized (queueLock) {
			queueLock.notify();
		}
	}
	



}
