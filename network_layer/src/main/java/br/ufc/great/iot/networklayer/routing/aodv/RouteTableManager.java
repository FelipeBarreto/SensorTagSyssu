package br.ufc.great.iot.networklayer.routing.aodv;

import java.util.ArrayList;

import android.util.Log;
import br.ufc.great.iot.networklayer.routing.aodv.exception.InvalidParametersException;
import br.ufc.great.iot.networklayer.routing.aodv.exception.InvalidRouteException;
import br.ufc.great.iot.networklayer.routing.aodv.exception.NoSuchRouteException;
import br.ufc.great.iot.networklayer.routing.aodv.message.RouteRequest;
import br.ufc.great.iot.networklayer.routing.aodv.route.ForwardRouteEntry;
import br.ufc.great.iot.networklayer.routing.aodv.route.ForwardRouteTable;
import br.ufc.great.iot.networklayer.routing.aodv.route.RouteRequestEntry;
import br.ufc.great.iot.networklayer.routing.aodv.route.RouteRequestTable;
import br.ufc.great.iot.networklayer.util.Debug;
import br.ufc.great.somc.networklayer.BuildConfig;

public class RouteTableManager {

	private RouteRequestTable requestTable;
	private ForwardRouteTable forwardTable;
	private boolean keepRunning = true;
	private Object timeoutLocker = new Integer(0);
	private TimeoutNotifier timeoutNotifier;
	private Node node;
	private AODV aodv;
	
	public RouteTableManager(AODV aodv)
	{
		requestTable = new RouteRequestTable();
		forwardTable = new ForwardRouteTable();
		this.node = Node.getInstance();
		this.aodv = aodv;
		
	}
	// REQUEST TABLE
	
	public boolean routeRequestExist(RouteRequest rreq)
	{		
		return requestTable.routeRequestEntryExist(rreq);
	}
	
	public boolean addRouteRequest(RouteRequest rreq, boolean expires) {
		try {
			RouteRequestEntry entry = new RouteRequestEntry(rreq);
			if(requestTable.addRouteRequestEntry(entry, expires))
			{
				if(expires == true)
				{
					synchronized (timeoutLocker) {
						timeoutLocker.notify();
					}
				}				
				return true;
			}			
		} catch (InvalidParametersException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	// FORWARD TABLE
	public boolean createForwardRouteEntry(String destinationAddress, int destinationSequenceNumber,
			String nextHop, int hopCount){
		if(forwardTable.createForwardRoute(destinationAddress, destinationSequenceNumber,
				nextHop, hopCount))
		{
			if (BuildConfig.DEBUG) {
				StringBuffer message = new StringBuffer("creating route to " +  destinationAddress + " is true");
				Log.d("invalidating", message.toString());
				Debug.debugMessage(message);
			}
			synchronized (timeoutLocker) {
				timeoutLocker.notify();
			}
			return true;
		}
		return false;
	}
	
	public boolean updateRouteTo(String destinationAddress,
			int destinationSequenceNumber, String nextHop, int hopCount) 
					throws NoSuchRouteException, InvalidRouteException {
		
		if(forwardTable.updateForwardRoute(destinationAddress, destinationSequenceNumber,
				nextHop, hopCount))
		{
			synchronized (timeoutLocker) {
				timeoutLocker.notify();
			}
			return true;
		}
		return false;
	}

	public String getPathToDestination(String destinationAddress) throws NoSuchRouteException, InvalidRouteException {
		return forwardTable.getNextHopToDestionation(destinationAddress);
	}
	
	public ForwardRouteEntry getForwardEntry(String destinationAddress) throws NoSuchRouteException, InvalidRouteException
	{
		return forwardTable.routeToNodeExist(destinationAddress);
	}

	public int getDestinationSequenceNumberTo(String destinationAddress) throws NoSuchRouteException{
		return forwardTable.getEntryDestinationSequenceNumber(destinationAddress);
	}

	public int getDistanceToDestination(String destinationAddress) throws NoSuchRouteException, InvalidRouteException {
		return forwardTable.getEntryHopCount(destinationAddress);
	}
	
	public long getRouteAlivetime(String destinationAddress) throws NoSuchRouteException, InvalidRouteException {
		return forwardTable.getEntryAlivetime(destinationAddress);
	}

	public boolean addPrecursorNode(String destinationAddress, String precursor) throws NoSuchRouteException, InvalidRouteException {
		return forwardTable.addEntryPrecursorNode(destinationAddress, precursor);
	}

	public ArrayList<String> getPrecusors(String destinationAddress) throws NoSuchRouteException, InvalidRouteException {
		return forwardTable.getPrecusors(destinationAddress);
	}
	
	public void setInvalid(String destinationAddress,
			int destinationSequenceNumber) throws NoSuchRouteException {
		forwardTable.setInvalid(destinationAddress, destinationSequenceNumber);
	}
	
	public void updateByHello(String sourceAddress, int sourceSequenceNumber) throws NoSuchRouteException {
		forwardTable.updateByHello(sourceAddress, sourceSequenceNumber);
	}
	
	public void setValid(String destinationAddress,
			int destinationSequenceNumber) throws NoSuchRouteException {
		forwardTable.setValid(destinationAddress, destinationSequenceNumber);
	}

	
	public void startTimerThread()
	{
		keepRunning = true;
		timeoutNotifier = new TimeoutNotifier();		
		timeoutNotifier.setName("Timeout");
		timeoutNotifier.start();
	}
	
	public void stopTimerThread()
	{
		keepRunning = false;
		timeoutNotifier.interrupt();
	}
	
	private class TimeoutNotifier extends Thread 
	{
		
		@Override
		public void run() 
		{
			while(keepRunning)
			{
				try
				{
					synchronized (timeoutLocker) {
						if(requestTable.isEmpty() && forwardTable.isEmpty())
						{
							timeoutLocker.wait();
						}
					}
					
					long expireTime = getMinimunTime() - System.currentTimeMillis();
					if(expireTime > 0)
					{
							sleep(expireTime);
					}
					
					try {
						RouteRequestEntry requestEntry = requestTable.getNextToExpire();					
						
						while (requestEntry.getAlivetimeLeft() <= System.currentTimeMillis()) {
							requestTable.removeRouteRequestEntry(requestEntry.getSourceAddress(),
									requestEntry.getBrodcastId());
							
							if(requestEntry.isMine() == true)
							{
								if(!forwardTable.activeRouteToNodeExist(requestEntry.getDestinationAddress(),
										requestEntry.getDestinationSequenceNumber()))
								{
									if(requestEntry.resend())
									{
										
										requestEntry.setBroadcastId(node.getNextBroadcastID());
										requestEntry.resetAlivetimeLeft();
										requestTable.addRouteRequestEntry(requestEntry, true);										
										
										aodv.queueRetryRouteRequest(requestEntry.getDestinationAddress(),
												requestEntry.getDestinationSequenceNumber(),
												requestEntry.getRetriesLeft());
									} 
									else
									{
										aodv.onRouteEstablishmentFailure(requestEntry.getDestinationAddress());
									}
								}
							}
								requestEntry = requestTable.getNextToExpire();
						}

					} catch (NoSuchRouteException e) {
						//e.printStackTrace();
					}
					
					
					try {
						ForwardRouteEntry forwardRoute = forwardTable.getNextToExpire();
						while(forwardRoute.getAlivetimeLeft() <= System.currentTimeMillis())
						{
							if(forwardRoute.getNumberHops() == 1 && forwardRoute.isValid())
							{
								forwardTable.setInvalid(forwardRoute.getDestinationAddress(),
										forwardRoute.getDestinationSequenceNumber() + 1);
								
								aodv.OnInvalidRouteOccur(forwardRoute.getDestinationAddress());
								ArrayList<ArrayList<String>> broken =
										forwardTable.findBronkenLinks(forwardRoute.getDestinationAddress());
								
								for (ArrayList<String> precusors : broken) {									
									aodv.queueRouteErrorMessage(forwardRoute.getDestinationAddress(),
											forwardRoute.getDestinationSequenceNumber(), precusors);
								}
								
							} else if(forwardRoute.isValid()){
								
								forwardTable.setInvalid(forwardRoute.getDestinationAddress(),
										forwardRoute.getDestinationSequenceNumber() + 1 );
								aodv.OnInvalidRouteOccur(forwardRoute.getDestinationAddress());
								
							} else {
								
								forwardTable.removeEntry(forwardRoute);
								
								if (BuildConfig.DEBUG) {
									StringBuffer message = new StringBuffer("Removing route to " +forwardRoute.getDestinationAddress());
									Log.d("invalidating", message.toString());
									Debug.debugMessage(message.append("\n"));
								}	
							}
							forwardRoute = forwardTable.getNextToExpire();
							
						}
					} catch (NoSuchRouteException e1) {
						
					}
				
				}catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private long getMinimunTime() {
		long requestTime = Long.MAX_VALUE;
		try {
			requestTime =  requestTable.getNextTimeToExpire();		
		} catch (NoSuchRouteException e) {
			//e.printStackTrace();			
		}
		
		long forwardTime = -1;
		try {
			forwardTime =  forwardTable.getNextTimeToExpire();
		} catch (NoSuchRouteException e) {
			//e.printStackTrace();
			if(requestTime == Long.MAX_VALUE){
				return -1;
			}
			return requestTime;
		}
	
		return Math.min(requestTime, forwardTime);
	}

	

	

	
}
