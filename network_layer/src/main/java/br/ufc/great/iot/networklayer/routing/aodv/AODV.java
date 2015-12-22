package br.ufc.great.iot.networklayer.routing.aodv;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.util.Log;
import br.ufc.great.iot.networklayer.routing.IRouter;
import br.ufc.great.iot.networklayer.routing.Receiver;
import br.ufc.great.iot.networklayer.routing.aodv.exception.AODVException;
import br.ufc.great.iot.networklayer.routing.aodv.exception.InvalidRouteException;
import br.ufc.great.iot.networklayer.routing.aodv.exception.NoSuchRouteException;
import br.ufc.great.iot.networklayer.routing.aodv.message.AODVMessage;
import br.ufc.great.iot.networklayer.routing.aodv.message.Hello;
import br.ufc.great.iot.networklayer.routing.aodv.message.RouteError;
import br.ufc.great.iot.networklayer.routing.aodv.message.RouteReply;
import br.ufc.great.iot.networklayer.routing.aodv.message.RouteRequest;
import br.ufc.great.iot.networklayer.routing.aodv.route.ForwardRouteEntry;
import br.ufc.great.iot.networklayer.routing.exception.RoutingException;
import br.ufc.great.iot.networklayer.routing.exception.TimeToLiveExpiredException;
import br.ufc.great.iot.networklayer.routing.message.RoutingMessage;
import br.ufc.great.iot.networklayer.routing.message.UserDataMessage;
import br.ufc.great.iot.networklayer.util.Debug;
import br.ufc.great.somc.networklayer.BuildConfig;

public class AODV extends IRouter{

	private RouteTableManager routingTable;	
	private AODVSender sender;
	private Node node;
	private AODVEventsNotifier notifier;
	private HelloBroadcaster helloBroadcaster;
	
	public AODV(String applicationID, AODVEventsNotifier notifier, boolean periodicMessages) {		
		super(applicationID, notifier);
		
		this.notifier = notifier;
		node = Node.getInstance();
		
		sender = new AODVSender(notifier, this);
		setSender(sender);
		
		routingTable = new RouteTableManager(this);
		if(periodicMessages == true)
		{
			helloBroadcaster = new HelloBroadcaster();
		}
		
	}

	@Override
	public void start() {
		super.start();
		routingTable.startTimerThread();	
		if(helloBroadcaster != null) helloBroadcaster.start();
	}	
	
	@Override
	public void stop() {
		super.stop();
		if(helloBroadcaster != null) helloBroadcaster.stopBroadcast();
		routingTable.stopTimerThread();
	}
	
	@Override
	public void routeMessage(RoutingMessage message) throws RoutingException {
		super.routeMessage(message);
		try {
			if(message.getType().equalsIgnoreCase(AODVMessage.AODV_MESSAGE))
			{
				AODVMessage aodvMessage = (AODVMessage) message;
				switch (aodvMessage.getMessageType()) {
					case RREQ:
						receiveRREQ((RouteRequest) aodvMessage);
					break;
					
					case RREP:
						receiveRREP((RouteReply) aodvMessage);
					break;
					
					case RRER:
						receiveRRER((RouteError) aodvMessage);
					break;
					
					case HELLO:
						receiveHello((Hello) aodvMessage);
					break;
			
					default:
					break;
				}
			} else {
				if(message.getType().equalsIgnoreCase(UserDataMessage.class.getCanonicalName()))
				{
					Debug.debugMessage((UserDataMessage)  message, getMyAddresses(), true, false);
					if(getMyAddresses().contains(message.getDestinationAddress())){
						getReceiver().queueDataMesssage((UserDataMessage) message);
					} else {
						if(isBrodcastMessage(message))
						{
							((UserDataMessage) message).decrementTtl();
							getReceiver().queueDataMesssage((UserDataMessage) message);
						}
						forwardData(message);						
					}
				}
			}
		}  catch (TimeToLiveExpiredException e) {
			e.printStackTrace();			
		}  catch (NoSuchRouteException e) {			
			e.printStackTrace();			
		} catch (InvalidRouteException e) {
			e.printStackTrace();			
		}
		
	}

	private void receiveHello(Hello hello) {
		try {
			routingTable.updateByHello(hello.getSourceAddress(), hello.getSourceSequenceNumber());
		} catch (NoSuchRouteException e) {
			routingTable.createForwardRouteEntry(hello.getSourceAddress(),
												 hello.getSourceSequenceNumber(),
												 hello.getSourceAddress(),
												 1);
		}
		Debug.debugMessage(new StringBuffer("Receiver: received hello pdu from: "+hello.getSourceAddress()));
	}

	private void forwardData(RoutingMessage message) throws NoSuchRouteException, InvalidRouteException {
		getSender().queueDataMessageToForward((UserDataMessage) message);
	}


	
	

	@Override
	public void routeMessage(String destinationAddress, JSONObject message)
			throws RoutingException {
			List<String> myAddresses = null;
			if(BuildConfig.DEBUG)
			{
				myAddresses = new ArrayList<String>();
				myAddresses.add("Prop Node");
			}
		
			UserDataMessage dataMessage = new UserDataMessage(message);
			dataMessage.setDestinationAddress(destinationAddress);
			
			if(IRouter.destinationIsBroacast(destinationAddress))
			{
				dataMessage.setNextHop(destinationAddress);
			}
		
			sender.queueMyDataMessage(dataMessage);
	}
	
	private boolean iAmTheDestination(RouteRequest routeRequest) {
		return getMyAddresses().contains(routeRequest.getDestinationAddress());
	}

	private boolean createOrUpdatePathTo(String destinationAddress, int destinationSequenceNumber,
			String nextHop, int hopCount){
		
		
		try {
			
				return routingTable.updateRouteTo(destinationAddress, destinationSequenceNumber,
						nextHop, hopCount);
		} catch (InvalidRouteException e) {
			e.printStackTrace();
			sender.onNewNodeReachable(destinationAddress);
			return true;
		} catch (NoSuchRouteException e) {
			//e.printStackTrace();
			if(routingTable.createForwardRouteEntry(destinationAddress, destinationSequenceNumber,
					nextHop, hopCount))
			{
				sender.onNewNodeReachable(destinationAddress);
				return true;
			}
		}
		return false;
		
		
	}

	private boolean sendRREQ(String destinationAddress, int lastDestinationSequenceNumber) {
		node.getNextSequenceNumber();
		node.getNextBroadcastID();
		RouteRequest routeRequest = new RouteRequest(node.getCurrentSequenceNumber(),
				node.getCurrentBroadcastID(), destinationAddress, lastDestinationSequenceNumber);
		
		if(routingTable.addRouteRequest(routeRequest, true))
		{
			getSender().queueRoutingMessage(routeRequest);
			return true;
		}		
		return false;		
	}
	
	private void receiveRREQ(RouteRequest routeRequest)
	{			
		if(BuildConfig.DEBUG)
		{
			Debug.debugMessage(routeRequest, getMyAddresses(), true, false);
		}
		
		createOrUpdatePathTo(routeRequest.getPreviousHop(), Constants.UNKNOWN_SEQUENCE_NUMBER,
				routeRequest.getPreviousHop(), 1);
		
		if(routingTable.routeRequestExist(routeRequest))
		{
			return;
		} 
		
		routingTable.addRouteRequest(routeRequest, false);
		
		routeRequest.incrementHopCount();
		
		createOrUpdatePathTo(routeRequest.getSourceAddress(), routeRequest.getSourceSequenceNumber(),
				routeRequest.getPreviousHop(), routeRequest.getHopCount());

		RouteReply rrep = null;
		try {
			rrep = sendRREP(routeRequest);
		} catch (InvalidRouteException e) {
			routeRequest.setDestinationSequenceNumber(Math.max(e.getLastSequenceNumber(),
					routeRequest.getDestinationSequenceNumber()));
		} catch (NoSuchRouteException e) {
		} finally {
			if(rrep != null)
			{
				sender.queueRoutingMessage(rrep);
			} else {				
				sender.queueRoutingMessage(routeRequest);
			}
		}
	}

	private RouteReply sendRREP(RouteRequest rreq) throws NoSuchRouteException, InvalidRouteException
	{	
		if(iAmTheDestination(rreq))
		{
			if(node.getCurrentSequenceNumber() + 1 == rreq.getDestinationSequenceNumber())
			{
				node.getNextSequenceNumber();
			}
			return new RouteReply(rreq.getDestinationAddress(), node.getCurrentSequenceNumber(),
					rreq.getSourceAddress(), rreq.getSourceSequenceNumber());
			
		} else {
				ForwardRouteEntry entry = routingTable.getForwardEntry(rreq.getDestinationAddress());
				int destinationSequenceNumber = entry.getDestinationSequenceNumber();
				sendGratuitousReply(rreq, destinationSequenceNumber);
				int hops = entry.getNumberHops();
				long lifetime = entry.getAlivetimeLeft();
				
				return new RouteReply(rreq.getDestinationAddress(), destinationSequenceNumber,
						rreq.getSourceAddress(), rreq.getSourceSequenceNumber(), hops, lifetime);
		}
	}
	
	private void receiveRREP(RouteReply routeReply) throws NoSuchRouteException, InvalidRouteException {
		
		if(BuildConfig.DEBUG)
		{
			Debug.debugMessage(routeReply, getMyAddresses(), true, false);
		}
		
		createOrUpdatePathTo(routeReply.getPreviousHop(), Constants.UNKNOWN_SEQUENCE_NUMBER,
				routeReply.getPreviousHop(), 1);
		
		routeReply.incrementHopCount();		
		
		if(createOrUpdatePathTo(routeReply.getDestinationAddress(), routeReply.getDestinationSequenceNumber(),
				routeReply.getPreviousHop(), routeReply.getHopCount()) == true)
		{
			if(!getMyAddresses().contains(routeReply.getSourceAddress()))
			{							
					String nextHop = routingTable.getPathToDestination(routeReply.getSourceAddress());					
					routingTable.addPrecursorNode(routeReply.getDestinationAddress(), nextHop);
					routingTable.addPrecursorNode(routeReply.getPreviousHop(), nextHop);
					
					getSender().queueRoutingMessage(routeReply);					
			}
			
		}
	}

	private void receiveRRER(RouteError routeError) {
		if (BuildConfig.DEBUG) {
			Debug.debugMessage(routeError, getMyAddresses(), true, false);
		}
		try {
			int lastKnowSequenceNumber = 
					routingTable.getDestinationSequenceNumberTo(routeError.getUnreachableNodeAddress());
			if(routeError.getUnreachableNodeSequenceNumber() >= lastKnowSequenceNumber)
			{
				queueRouteErrorMessage(routeError.getUnreachableNodeAddress(),
						routeError.getUnreachableNodeSequenceNumber(),
						routingTable.getPrecusors(routeError.getUnreachableNodeAddress()));
				routingTable.setInvalid(routeError.getUnreachableNodeAddress(),
						routeError.getUnreachableNodeSequenceNumber());
			}
		} catch (AODVException e){
			e.printStackTrace();
		}
	}

	private void sendGratuitousReply(RouteRequest rreq, int destinationSequenceNumber) throws NoSuchRouteException, InvalidRouteException{
		int gratuitousHops = routingTable.getDistanceToDestination(rreq.getSourceAddress());
		long lifetime = routingTable.getRouteAlivetime(rreq.getSourceAddress());
		RouteReply rrepGratuitous = new RouteReply(rreq.getSourceAddress(), rreq.getSourceSequenceNumber(),
		rreq.getDestinationAddress(), destinationSequenceNumber, gratuitousHops, lifetime);
		getSender().queueRoutingMessage(rrepGratuitous);
	}	

	

	public String getNextHopTo(String destinationAddress) throws NoSuchRouteException, InvalidRouteException {
		
		return routingTable.getPathToDestination(destinationAddress);
		
	}

	public void onRouteEstablishmentFailure(String destinationAddress) {
		sender.onRouteEstablishmentFailure(destinationAddress);
		notifier.notifyAboutDestinationUnreachable(destinationAddress);
		
	}
	
	public void OnInvalidRouteOccur(String destinationAddress)
	{
		notifier.notifyAboutInvalidRouteTo(destinationAddress);
	}

	public int getLastKnownSequenceNumber(String destinationAddress) throws NoSuchRouteException{
		return routingTable.getDestinationSequenceNumberTo(destinationAddress);
	}

	void queueRetryRouteRequest(String destinationAddress,
			int destinationSequenceNumber, int retriesLeft) {
		RouteRequest rreq = new RouteRequest(node.getCurrentSequenceNumber(),
				node.getCurrentBroadcastID(),destinationAddress,
				destinationSequenceNumber, retriesLeft);
		sender.queueRoutingMessage(rreq);
		//debugResending(rreq);
	}

	void queueRouteErrorMessage(String unreachableNodeAddress,
			int unreachableSequenceNumber, ArrayList<String> precusors) {
		for (String destinationAdderss : precusors) {
			RouteError rrer = new RouteError(unreachableNodeAddress,
					unreachableSequenceNumber, destinationAdderss);		
			
			sender.queueRoutingMessage(rrer);
		}
				
	}

	void queueRouteErrorMessage(String unreachableNodeAddress,
			int unreachableSequenceNumber, String destinationAddress) {
		RouteError rrer = new RouteError(unreachableNodeAddress,
				unreachableSequenceNumber, destinationAddress);
		sender.queueRoutingMessage(rrer);		
	}

	void queueNewRouteRequest(String destinationAddress,
			int lastKnownSequenceNumber) {
		if(!sendRREQ(destinationAddress, lastKnownSequenceNumber))
		{
			if (BuildConfig.DEBUG) {
				StringBuffer message = new StringBuffer("Nao enviou RREQ apos RRER");
				Log.d(getClass().getName(), message.toString());
				Debug.debugMessage(message);
			}
		}
	}

//	private void debugResending(RouteRequest rreq) {
//		if (BuildConfig.DEBUG) {
//			StringBuffer message = new StringBuffer("Resending RREQ -D " + rreq.getDestinationAddress()+" ");
//			//message.append(requestEntry.getRetriesLeft());
//			message.append(" retries left, ");
//			message.append("TTL = ");
//			message.append(rreq.getTTL());
//			message.append(" aliveTimes  ");
//			//message.append(requestEntry.getAlivetimeLeft());				
//			Debug.debugMessage(message);
//			Log.d("resending", message.toString());
//		}
//	}

//	public void test(String destinationAddress) {
//		ForwardRouteEntry forwardRoute;
//		try {
//			forwardRoute = routingTable.getForwardEntry(destinationAddress);
//			if(forwardRoute.getNumberHops() == 1 && forwardRoute.isValid())
//			{
//					routingTable.setInvalid(forwardRoute.getDestinationAddress(),
//							forwardRoute.getDestinationSequenceNumber() + 1);
//				OnInvalidRouteOccur(forwardRoute.getDestinationAddress());
//				if (BuildConfig.DEBUG) {
//					StringBuffer message = new StringBuffer("Invalidando rota");
//					Debug.debugMessage(message);
//					Log.d("updatetime", message.toString());
//				}
//			}
//		} catch (NoSuchRouteException e1) {
//			e1.printStackTrace();
//		} catch (InvalidRouteException e1) {
//			e1.printStackTrace();
//		}
//
//	}
	
	private class HelloBroadcaster extends Thread {
		private boolean keepBroadcasting = true;
		
		public HelloBroadcaster() {
			setName("HelloBroadcaster");
		}
		
		void stopBroadcast(){
			keepBroadcasting = false;
			this.interrupt();
		}
		
		@Override
		public void run() {
			while(keepBroadcasting){
				try {
    				sleep(Constants.HELLO_INTERVAL);
    				getSender().queueRoutingMessage(new Hello(node.getCurrentSequenceNumber()));
	    		} catch (InterruptedException e) {
	    			
	    		}
			}
		}
		
		
	}

}
