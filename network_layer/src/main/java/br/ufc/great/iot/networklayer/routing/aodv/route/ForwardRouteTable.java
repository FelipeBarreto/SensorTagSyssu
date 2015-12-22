package br.ufc.great.iot.networklayer.routing.aodv.route;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;

import android.util.Log;
import br.ufc.great.iot.networklayer.routing.aodv.exception.InvalidParametersException;
import br.ufc.great.iot.networklayer.routing.aodv.exception.InvalidRouteException;
import br.ufc.great.iot.networklayer.routing.aodv.exception.NoSuchRouteException;
import br.ufc.great.somc.networklayer.BuildConfig;

public class ForwardRouteTable {

	private Hashtable<String, ForwardRouteEntry> forwardTable;
	private LinkedList<ForwardRouteEntry> sortedByTimeEntry;
	private Object tableLock = Integer.valueOf(0);
	
	public ForwardRouteTable()
	{
		forwardTable = new Hashtable<String, ForwardRouteEntry>();
		sortedByTimeEntry = new LinkedList<ForwardRouteEntry>();
	}

	public boolean createForwardRoute(String destinationAddress, int destinationSequenceNumber,
			String nextHop, int hopCount) {		
		
	
		ForwardRouteEntry forwardPath;
		try {
			forwardPath = new ForwardRouteEntry(destinationAddress, destinationSequenceNumber,
					nextHop, hopCount, new ArrayList<String>());
			synchronized (tableLock) {
				forwardTable.put(destinationAddress, forwardPath);
				sortedByTimeEntry.addLast(forwardPath);
			}
			return true;
		} catch (InvalidParametersException e) {
			e.printStackTrace();
			return false;
		}
	
		
	}
	
	public boolean removeEntry(ForwardRouteEntry forwardRoute) {
		synchronized (tableLock) {
			ForwardRouteEntry entry = forwardTable.remove(forwardRoute.getDestinationAddress());
			if(entry != null)
			{
				sortedByTimeEntry.remove(entry);
				return true;
			}
			return false;
		}
	}

	public boolean updateForwardRoute(String destinationAddress,
			int destinationSequenceNumber, String nextHop, int hopCount)
					throws NoSuchRouteException, InvalidRouteException {
		
			
		ForwardRouteEntry oldRoute = forwardTable.get(destinationAddress);
		
		if(oldRoute == null)
		{
			throw new NoSuchRouteException();
		}
		
		ForwardRouteEntry newRoute = null;	
		
		try {
			newRoute = new ForwardRouteEntry(destinationAddress, destinationSequenceNumber,
					nextHop, hopCount, new ArrayList<String>());
		} catch (InvalidParametersException e) {
			e.printStackTrace();
			return false;
		}
		
		if (!updateEntry(newRoute, oldRoute))
		{
			updateEntryTime(destinationAddress, true);
			return false;
		}
		return true;
	}

	private boolean updateEntry(ForwardRouteEntry newRoute, ForwardRouteEntry oldRoute) throws InvalidRouteException {
		
		if(oldRoute.isValid()){
			if(newRoute.getDestinationSequenceNumber() < oldRoute.getDestinationSequenceNumber() ||
			   (newRoute.getDestinationSequenceNumber() == oldRoute.getDestinationSequenceNumber() &&
				newRoute.getNumberHops() >= oldRoute.getNumberHops()))
			{
				return false;
			}
			
		}
		
		synchronized (tableLock) {
			newRoute.setPrecursors(oldRoute.getPrecursors());
			forwardTable.put(newRoute.getDestinationAddress(), newRoute);
			sortedByTimeEntry.remove(oldRoute);
			sortedByTimeEntry.addLast(newRoute);			
		}
		
		if(!oldRoute.isValid()) throw new InvalidRouteException(newRoute.getDestinationSequenceNumber());
		
		return true;
	}
	
	
	public boolean activeRouteToNodeExist(String destinationAddress, int destinationSequenceNumber) {
		ForwardRouteEntry route = forwardTable.get(destinationAddress);
		
		if (route != null && route.isValid())
		{
			if (BuildConfig.DEBUG) {
				StringBuffer message = new StringBuffer("route to " +  destinationAddress + "is " + route.isValid());
				Log.d("invalidating", message.toString());
			}
			if(destinationSequenceNumber > route.getDestinationSequenceNumber())
			{
				return false;
			}
			
			return true;
		}
		return false;
	}

	
	public ForwardRouteEntry routeToNodeExist(String destinationAddress)
			throws NoSuchRouteException, InvalidRouteException {
		
		ForwardRouteEntry path = updateEntryTime(destinationAddress, null);

		if(!path.isValid()){
			throw new InvalidRouteException(path.getDestinationSequenceNumber());
		}
		
		return path;
		
	}
	
	public String getNextHopToDestionation(String destinationAddress) throws NoSuchRouteException, InvalidRouteException {
		ForwardRouteEntry path = routeToNodeExist(destinationAddress);		
		return path.getNextHop();
	}

	public int getEntryDestinationSequenceNumber(String destinationAddress) throws NoSuchRouteException{
		ForwardRouteEntry path = forwardTable.get(destinationAddress);
		
		if(path == null)
		{
			throw new NoSuchRouteException(destinationAddress);
		}
		return path.getDestinationSequenceNumber();
	}

	public int getEntryHopCount(String destinationAddress) throws NoSuchRouteException, InvalidRouteException {
		return routeToNodeExist(destinationAddress).getNumberHops();
	}
	
	public long getEntryAlivetime(String destinationAddress) throws NoSuchRouteException, InvalidRouteException {
		return routeToNodeExist(destinationAddress).getAlivetimeLeft();
	}
	
	public void updateByHello(String destinationAddress, int destinationSequenceNumber) throws NoSuchRouteException {

		setValid(destinationAddress, destinationSequenceNumber, true);
	}
	
	private ForwardRouteEntry updateEntryTime(String previousHop, Boolean valid) throws NoSuchRouteException {
		
		synchronized (tableLock) {
			ForwardRouteEntry entry = forwardTable.get(previousHop);
			
			if(entry != null)
			{
				if(valid != null && entry.isValid() != valid)
				{
					entry.setValid(valid);
				}
				entry.resetAlivetimeLeft();
				sortedByTimeEntry.remove(entry);
				sortedByTimeEntry.addLast(entry);
				
				return entry;
			}
		}
		
		throw new NoSuchRouteException();
		
				
	}

	public boolean addEntryPrecursorNode(String destinationAddress,	String precursor) throws NoSuchRouteException, InvalidRouteException {
		return routeToNodeExist(destinationAddress).addPrecursor(precursor);
	}
	
	public ArrayList<String> getPrecusors(String destinationAddress) throws NoSuchRouteException, InvalidRouteException {
		return routeToNodeExist(destinationAddress).getPrecursors();
	}
	

	public boolean isEmpty()
	{
		return sortedByTimeEntry.isEmpty();
	}
	
	public ForwardRouteEntry getNextToExpire() throws NoSuchRouteException
	{
		ForwardRouteEntry entry = sortedByTimeEntry.peek();
		
		if(entry != null)
		{
			return entry;
		}
		throw new NoSuchRouteException();
		
	}

	public long getNextTimeToExpire() throws NoSuchRouteException {
		return getNextToExpire().getAlivetimeLeft();
	}

	public void setInvalid(String destinationAddress, int destinationSequenceNumber) throws NoSuchRouteException {
		if (BuildConfig.DEBUG) {
			Log.d("Invalidating", "Invaliding RouteTo "+ destinationAddress);
			//Debug.debugMessage(new StringBuffer("InvalidRouteTo "+ destinationAddress));
			
		}
		setValid(destinationAddress, destinationSequenceNumber, false);		
	}
	
	public void setValid(String destinationAddress, int destinationSequenceNumber) throws NoSuchRouteException {
		/*if (BuildConfig.DEBUG) {
			Log.d("Invalidating", "Re-Valid RouteTo "+ destinationAddress);
			Debug.debugMessage(new StringBuffer("Re-validRouteTo "+ destinationAddress));
			
		}*/
		setValid(destinationAddress, destinationSequenceNumber, true);		
	}

	private void setValid(String destinationAddress,
			int destinationSequenceNumber, boolean valid) throws NoSuchRouteException {
		
		ForwardRouteEntry entry = updateEntryTime(destinationAddress, valid);
		//entry.setValid(valid);
		entry.setDestinationSequenceNumber(destinationSequenceNumber);
	}

	public ArrayList<ArrayList<String>> findBronkenLinks(String destinationAddress)
	{
		ArrayList<ArrayList<String>> brokenLinks = new ArrayList<ArrayList<String>>();
		ArrayList<ForwardRouteEntry> currentEntries = new ArrayList<ForwardRouteEntry>();
		synchronized (tableLock) {
			for (ForwardRouteEntry forwardRoute : sortedByTimeEntry) {
				currentEntries.add(forwardRoute);
			}
			
			for (ForwardRouteEntry forwardRoute : currentEntries) {
				if(forwardRoute.getNextHop().equalsIgnoreCase(destinationAddress))
				{
					brokenLinks.add(forwardRoute.getPrecursors());
					try {						
						setInvalid(forwardRoute.getDestinationAddress(),
								forwardRoute.getDestinationSequenceNumber());
					} catch (NoSuchRouteException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		return brokenLinks;
	}

}
