package br.ufc.great.iot.networklayer.routing.aodv.message;

import br.ufc.great.iot.networklayer.routing.message.RoutingMessage;

/**
 * Represent a generic AODV message
 * @author bruno
 *
 */
public abstract class AODVMessage extends RoutingMessage {

	public static final String AODV_MESSAGE = "AODV_MESSAGE";	

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6842260528153473885L;

	private int hopCount;
	
	public enum AODV_MESSAGE_TYPE { RREQ, RREP, RRER, HELLO}

	public AODVMessage()
	{
		
	}
	
	public int getHopCount() {
		return hopCount;
	}

	public void setHopCount(int hopCount) {
		this.hopCount = hopCount;
	}	
	
	public abstract AODV_MESSAGE_TYPE getMessageType();
	
	@Override
	public final String getType() {
		return AODV_MESSAGE;
	}
	
	
	public void incrementHopCount()
	{
		hopCount++;
	}
	
}
