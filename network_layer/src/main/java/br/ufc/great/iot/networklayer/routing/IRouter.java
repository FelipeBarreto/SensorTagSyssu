package br.ufc.great.iot.networklayer.routing;

import java.util.List;

import org.json.JSONObject;

import br.ufc.great.iot.networklayer.base.MessageEventListener;
import br.ufc.great.iot.networklayer.base.NetworkManager;
import br.ufc.great.iot.networklayer.routing.exception.RoutingException;
import br.ufc.great.iot.networklayer.routing.message.HelloMessage;
import br.ufc.great.iot.networklayer.routing.message.RoutingMessage;
import br.ufc.great.iot.networklayer.util.Simulation;

public abstract class IRouter {
	
	public static String BROADCAST = "broadcast";
	public static String BROADCAST_EXCLUDING_SOURCE = "broadcast_excluding_source";	
	protected RoutingEventsNotifier notifier;
	private Sender sender;
	private Receiver receiver;
	private List<String> myAddresses;
	
	//TODO: remover
	protected Simulation simulation;
	
	public IRouter(String applicationID, RoutingEventsNotifier notifier) {
		this.notifier = notifier;		
		this.sender = new Sender(notifier);
		this.receiver = new Receiver(applicationID, notifier);
		simulation = Simulation.getInstance();
	}
	
	public void start()
	{
		sender.startThread();
		receiver.startThread();
	}

	public abstract void routeMessage(String destination, JSONObject message) throws RoutingException;
	public void routeMessage(RoutingMessage message) throws RoutingException {
		if(message.getType().equals(HelloMessage.class.getCanonicalName()))
		{
			HelloMessage hello = ((HelloMessage) message);
			receiver.queueMesssage(hello);
			hello.decrementTtl();	
			sender.queueRoutingMessage(hello);
			return;
		}
	}

	public void notifyNeighborhood() {
		sender.queueRoutingMessage(new HelloMessage());
	}
	
	protected Sender getSender() {
		return sender;
	}

	protected Receiver getReceiver() {
		return receiver;
	}
	
	public void stop()
	{
		sender.stopThread();
		receiver.stopThread();
	}

	public void setNotifier(RoutingEventsNotifier notifier) {
		this.notifier = notifier;
	}

	protected void setSender(Sender sender) {
		this.sender = sender;
	}

	protected void setReceiver(Receiver receiver) {
		this.receiver = receiver;
	}
	
	public static boolean destinationIsBroacast(String destinationAddress)
	{
		return (destinationAddress != null && 
	    (destinationAddress.equalsIgnoreCase(BROADCAST) ||
	    		destinationAddress.equalsIgnoreCase(BROADCAST_EXCLUDING_SOURCE)));
		
	}
	public static boolean isBrodcastMessage(RoutingMessage message)
	{
		String nextHop = message.getNextHop();
		return destinationIsBroacast(nextHop);
	}
	
	private MessageEventListener messageNotifier = new MessageEventListener() {
		
		@Override
		public void onMessageReceived(String jsonMessage) {
			try {
				RoutingMessage message = RoutingMessage.getFromJson(jsonMessage);
				
				routeMessage(message);
			} catch (RoutingException e) {
				e.printStackTrace();
			}
		}
	};


	public MessageEventListener getMessageNotifier() {
		return messageNotifier;
	}

	protected List<String> getMyAddresses() {
		return myAddresses;
	}
	
	public void setMyAddresses(List<String> addresses)
	{
		this.myAddresses = addresses;
	}
	
	

	
}
