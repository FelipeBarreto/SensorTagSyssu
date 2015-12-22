package br.ufc.great.iot.networklayer.routing.aodv.exception;

public class AODVException extends Exception {
	
	private String message;

	public AODVException(String message) {
		super();
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
	
	@Override
	public String toString() {
		return super.toString() + " " + message;
	}
}
