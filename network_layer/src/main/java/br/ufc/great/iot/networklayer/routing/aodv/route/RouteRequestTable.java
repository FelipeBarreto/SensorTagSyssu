package br.ufc.great.iot.networklayer.routing.aodv.route;

import java.util.Hashtable;
import java.util.LinkedList;

import br.ufc.great.iot.networklayer.routing.aodv.exception.NoSuchRouteException;
import br.ufc.great.iot.networklayer.routing.aodv.message.RouteRequest;


public class RouteRequestTable {

	private Hashtable<RouteRequestKey,RouteRequestEntry> requestTable; 
	private LinkedList<RouteRequestEntry> sortedByTimeEntry;
	private Object tableLock = Integer.valueOf(0);
		
	public RouteRequestTable()
	{
		requestTable = new Hashtable<RouteRequestKey, RouteRequestEntry>();
		sortedByTimeEntry = new LinkedList<RouteRequestEntry>();
	}
	

	
	public boolean routeRequestEntryExist(RouteRequest rreq)
	{
		if (requestTable.containsKey(new RouteRequestKey(rreq.getSourceAddress(), rreq.getBoadcastId())))
		{
			return true;
		}
		return false;
	}
	
	public boolean addRouteRequestEntry(RouteRequestEntry requestEntry, boolean expires) {
		synchronized (tableLock) {			
			
			RouteRequestKey requestKey = new RouteRequestKey(requestEntry.getSourceAddress(), requestEntry.getBrodcastId());
			if(!requestTable.containsKey(requestKey))
			{
				requestTable.put(requestKey, requestEntry);
				if(expires == true)
				{
					sortedByTimeEntry.addLast(requestEntry);
				}
			}
		}
		return true;
	}
	
	public boolean removeRouteRequestEntry(String sourceAddress, int broadcastId)
	{
		RouteRequestKey requestKey = new RouteRequestKey(sourceAddress, broadcastId);
		synchronized (tableLock) {
			RouteRequestEntry requestEntry = requestTable.remove(requestKey);			
			if(requestEntry != null)
			{
				sortedByTimeEntry.remove(requestEntry);
				return true;
			}
		}
		return false;		
	}
	



	
	public RouteRequestEntry getNextToExpire() throws NoSuchRouteException
	{
		synchronized (tableLock) {
			RouteRequestEntry requestEntry = sortedByTimeEntry.peek();
			if(requestEntry != null)
			{
				return requestEntry;
			}			
		}
		throw new NoSuchRouteException();
	}
	

	public long getNextTimeToExpire() throws NoSuchRouteException {
		return getNextToExpire().getAlivetimeLeft();
	}
	
	public boolean isEmpty()
	{
		return sortedByTimeEntry.isEmpty();				 
	}
	
	private class RouteRequestKey {
		
		private String sourceAddress;
		private int broadcastId;
		
		public RouteRequestKey(String sourceAddress, int brodcastId) {
			super();
			this.sourceAddress = sourceAddress;
			this.broadcastId = brodcastId;
		}
		
		public String getSourceAddress() {
			return sourceAddress;
		}


		public int getBrodcastId() {
			return broadcastId;
		}	
		
		
		@Override
		public boolean equals(Object o) {
			RouteRequestKey anotherKey = (RouteRequestKey) o;
			if(anotherKey.getBrodcastId() == broadcastId && 
					anotherKey.getSourceAddress().equalsIgnoreCase(sourceAddress))
			{
				return true;
			}
			return false;
		}
		
		@Override
		public int hashCode(){
			return (sourceAddress+";"+Integer.toString(broadcastId)).hashCode();
		}


		
	}



}
