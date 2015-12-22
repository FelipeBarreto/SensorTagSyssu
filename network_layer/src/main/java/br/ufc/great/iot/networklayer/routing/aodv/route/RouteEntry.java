package br.ufc.great.iot.networklayer.routing.aodv.route;

import br.ufc.great.iot.networklayer.routing.aodv.Constants;
import br.ufc.great.iot.networklayer.routing.aodv.exception.InvalidParametersException;




public abstract class RouteEntry {

	private String destinationAddress;
	private int numberHops;
	private int destinationSequenceNumber;	
	private long alivetimeLeft;
	protected final Object aliveLock = new Integer(0);
	
	
	public RouteEntry(String destinationAddress, 
			int destinationSequenceNumber, int numberHops) throws InvalidParametersException {
		super();
		if(destinationSequenceNumber <= Constants.MAX_SEQUENCE_NUMBER	&& 
		   destinationSequenceNumber >= Constants.FIRST_SEQUENCE_NUMBER || 
		   destinationSequenceNumber == Constants.UNKNOWN_SEQUENCE_NUMBER){
			this.destinationAddress = destinationAddress;
			this.numberHops = numberHops;
			this.destinationSequenceNumber = destinationSequenceNumber;
		} else {
			throw new InvalidParametersException("RouteEntry: Invalid parameters given");
		}
	
	}

	public String getDestinationAddress() {
		return destinationAddress;
	}	
		
	public int getNumberHops() {
		return numberHops;
	}	
	
	public int getDestinationSequenceNumber() {
		return destinationSequenceNumber;
	}

	public long getAlivetimeLeft() {
		synchronized (aliveLock) {
			return alivetimeLeft;
		}		
	}

	abstract void resetAlivetimeLeft();

	public void setAlivetimeLeft(long aliveTime) {
		this.alivetimeLeft = aliveTime;
	}

	public void setDestinationSequenceNumber(int destinationSequenceNumber) {
		this.destinationSequenceNumber = destinationSequenceNumber;
	}

	
	
	
	
}
