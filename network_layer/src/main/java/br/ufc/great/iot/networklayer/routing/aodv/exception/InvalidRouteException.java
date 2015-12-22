package br.ufc.great.iot.networklayer.routing.aodv.exception;

public class InvalidRouteException extends AODVException {

	private int lastSequenceNumber;
	
	public InvalidRouteException(int lastSequenceNumber) {
		super(String.valueOf(lastSequenceNumber));
		this.lastSequenceNumber = lastSequenceNumber;
	}


	public int getLastSequenceNumber() {
		return lastSequenceNumber;
	}
	
	
}
