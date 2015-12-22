package br.ufc.great.iot.networklayer.routing.aodv.exception;

public class NoSuchRouteException extends AODVException {

	public NoSuchRouteException(String message) {
		super(message);
		
	}
	
	public NoSuchRouteException()
	{
		super("No such route Exception");
	}

}
