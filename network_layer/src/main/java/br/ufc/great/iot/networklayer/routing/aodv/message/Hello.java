package br.ufc.great.iot.networklayer.routing.aodv.message;

import org.json.JSONException;
import org.json.JSONObject;

import br.ufc.great.iot.networklayer.routing.IRouter;

public class Hello extends AODVMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4119749284973219353L;
	
	private static final String SOURCE_SEQUENCE_NUMBER = "sourceSequenceNumber";
	private static final String TTL = "ttl";	
	
	private int sourceSequenceNumber;
	private int ttl;
	
	public Hello()
	{
	}

	public Hello(int sourceSequenceNumber) {
		setNextHop(IRouter.BROADCAST);
		setDestinationAddress(IRouter.BROADCAST);
		setSourceSequenceNumber(sourceSequenceNumber);
		ttl = 1;
	}
	
	@Override
	public AODV_MESSAGE_TYPE getMessageType() {
		return AODV_MESSAGE_TYPE.HELLO;
	}

	public int getSourceSequenceNumber() {
		return sourceSequenceNumber;
	}

	public int getTTL() {
		return ttl;
	}

	protected void setSourceSequenceNumber(int sourceSequenceNumber) {
		this.sourceSequenceNumber = sourceSequenceNumber;
	}
	
	@Override
	protected JSONObject toJsonObject(JSONObject json) {
		super.toJsonObject(json);
		try {
			json.put(SOURCE_SEQUENCE_NUMBER, sourceSequenceNumber);
			json.put(TTL, ttl);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
		
	}
	
}
