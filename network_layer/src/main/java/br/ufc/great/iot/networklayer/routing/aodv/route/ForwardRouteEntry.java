package br.ufc.great.iot.networklayer.routing.aodv.route;

import java.util.ArrayList;

import android.util.Log;
import br.ufc.great.iot.networklayer.routing.aodv.Constants;
import br.ufc.great.iot.networklayer.routing.aodv.exception.InvalidParametersException;
import br.ufc.great.somc.networklayer.BuildConfig;

public class ForwardRouteEntry extends RouteEntry {

	private String nextHop;
	private ArrayList<String> precursors;
	private boolean valid;
	
	

	public ForwardRouteEntry(String destinationAddress, int destinationSequenceNumber, String nextHop, int numberHops,
			ArrayList<String> precursors) throws InvalidParametersException {
		super(destinationAddress, destinationSequenceNumber, numberHops);
		this.nextHop = nextHop;
		this.precursors = precursors;
		this.valid = true;
		resetAlivetimeLeft();
	}

	public ArrayList<String> getPrecursors() {
		ArrayList<String> copy = new ArrayList<String>();
    	synchronized (precursors) {
    		for(String address: precursors){
    			copy.add(address);
    		}
		}
    	return copy;
	}

	
	public String getNextHop() {
		return nextHop;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {		
		this.valid = valid;
		if (BuildConfig.DEBUG) {
			StringBuffer message = new StringBuffer("updating to " +  this.getDestinationAddress() + " -> " + valid);
			Log.d("invalidating", message.toString());
		}
		/*if(valid)
		{
			this.resetAlivetimeLeft();
		}
		else
		{
			this.updateInvalidRouteAlivetime();	
		}*/
	}

	@Override
	public void resetAlivetimeLeft() {		
		synchronized (aliveLock) {
//			if (BuildConfig.DEBUG) {
//				Log.d("updatetime", "atualizando reset entrada " + getDestinationAddress());
//				Log.d("updatetime", "De " + getAlivetimeLeft());	
//			}
			if(isValid())
			{
				setAlivetimeLeft(Constants.ACTIVE_ROUTE_TIMEOUT + System.currentTimeMillis());
			}
			else
			{
				setAlivetimeLeft(Constants.DELETE_PERIOD + System.currentTimeMillis());
			}
//			if (BuildConfig.DEBUG) {
//				Log.d("updatetime", "Para " + getAlivetimeLeft());	
//			}
		}
		
	}
	
	public void updateAlivetimeLeft(long currentAlivetime, int hopCount) {		
		/*synchronized (aliveLock) {
			if (BuildConfig.DEBUG) {
				Log.d("updatetime", "atualizando reply entrada " + getDestinationAddress());
				Log.d("updatetime", "De " + getAlivetimeLeft());	
			}
			long currentTime = System.currentTimeMillis();
			setAlivetimeLeft(Math.max(
					currentAlivetime, 
					currentTime + 2*(Constants.NET_TRANSVERSAL_TIME -
							Constants.NODE_TRANVERSAL_TIME*hopCount))
					);
			if (BuildConfig.DEBUG) {
				Log.d("updatetime", "Para " + getAlivetimeLeft());
			}
		}*/
		
	}
	
	private void updateInvalidRouteAlivetime() {		
		synchronized (aliveLock) {
			if (BuildConfig.DEBUG) {
				Log.d("updatetime", "atualizando invalid entrada " + getDestinationAddress());
				Log.d("updatetime", "De " + getAlivetimeLeft());	
			}
			long currentTime = System.currentTimeMillis();
			if (BuildConfig.DEBUG) {
				Log.d("updatetime", "Para " + currentTime + Constants.DELETE_PERIOD);
			}
			setAlivetimeLeft(currentTime + Constants.DELETE_PERIOD);
		}
	}

	
	public boolean addPrecursor(String nodeAddress)
	{
		synchronized (precursors) {
			return precursors.add(nodeAddress);	
		}
		
	}
	
	@Override
	public void setDestinationSequenceNumber(int destinationSequenceNumber) {
		super.setDestinationSequenceNumber(
				Math.max(getDestinationSequenceNumber(), destinationSequenceNumber));
		
	}

	public void setPrecursors(ArrayList<String> oldPrecursors) {
		synchronized (precursors) {
			for (String precursorAddress : oldPrecursors) {
				precursors.add(precursorAddress);
			}
		}		
	}
		
	
}
