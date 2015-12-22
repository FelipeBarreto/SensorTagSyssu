package br.ufc.great.iot.networklayer.routing.exception;



public class TimeToLiveExpiredException extends RoutingException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5314694861232741238L;
	private String protocol;

	public TimeToLiveExpiredException(String protocol)
	{
		this.protocol = protocol;
	}
	
	@Override
	public String toString() {
		return "TTL Expired: " + protocol;
	}
}
