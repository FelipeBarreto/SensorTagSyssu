package br.ufc.great.iot.networklayer.routing.flooding;

import org.json.JSONObject;

import br.ufc.great.iot.networklayer.base.NetworkManager;
import br.ufc.great.iot.networklayer.routing.IRouter;
import br.ufc.great.iot.networklayer.routing.RoutingEventsNotifier;
import br.ufc.great.iot.networklayer.routing.exception.DuplicatedMessageException;
import br.ufc.great.iot.networklayer.routing.exception.RoutingException;
import br.ufc.great.iot.networklayer.routing.exception.TimeToLiveExpiredException;
import br.ufc.great.iot.networklayer.routing.flooding.message.FloodingMessage;
import br.ufc.great.iot.networklayer.routing.flooding.message.TrackingFlooding;
import br.ufc.great.iot.networklayer.routing.message.RoutingMessage;

public class Flooding extends IRouter {

	private static final int DEFAULT_TTL = 3;
	
	
	private int sequenceNumber;
	private MessageTable messageTable;
	
	public Flooding(String applicationID, RoutingEventsNotifier notifier) {
		super(applicationID, notifier);
		messageTable = new MessageTable(MessageTable.DEFAULT_TABLE_SIZE,
				MessageTable.DEFAULT_PURGE_TIME);
	}	
	
	@Override
	public void routeMessage(RoutingMessage message)throws RoutingException
	{
		super.routeMessage(message);
		
		if(message.getType().equalsIgnoreCase(FloodingMessage.FLOODING_MESSAGE))
		{
			
		
			FloodingMessage flodMessage = ((FloodingMessage) message);
			notifier.onNewNodeReachable(flodMessage.getSourceAddress());
			
			if(messageTable.addMessage(flodMessage) == true)
			{
				if(NetworkManager.SIMULATION)
				{
					if ( !getMyAddresses().contains(flodMessage.getSourceAddress())) {
						simulation.log(message, false);
					}
					
				}
				
				boolean destinationIsBroadcast = 
						IRouter.destinationIsBroacast(flodMessage.getDestinationAddress());
				boolean isMine = getMyAddresses().contains(message.getDestinationAddress());
				try {
					// FIXME Remove to better place. Probably IRouter
					if ( getMyAddresses().contains(flodMessage.getSourceAddress())) {
						System.out.println("removeu mensagem propria");
						return;
					}
					flodMessage.decrementTtl();
					if(destinationIsBroadcast || !isMine)
					{
						getSender().queueRoutingMessage(flodMessage);
					}
				} catch (TimeToLiveExpiredException e) {
				//	e.printStackTrace();
				} finally {
					if(isMine || destinationIsBroadcast)
					{
						getReceiver().queueDataMesssage(flodMessage);
					}
				}
			}
		}
		
	}

	@Override
	public void routeMessage(String destination, JSONObject message)
			throws RoutingException {
		sequenceNumber++;
		//FIXME change flooding message 
		getSender().queueRoutingMessage(new FloodingMessage(destination, message, DEFAULT_TTL, sequenceNumber));
	}



}
