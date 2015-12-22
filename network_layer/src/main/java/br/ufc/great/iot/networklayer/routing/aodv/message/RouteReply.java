package br.ufc.great.iot.networklayer.routing.aodv.message;

import org.json.JSONException;
import org.json.JSONObject;

import br.ufc.great.iot.networklayer.routing.aodv.Constants;

public class RouteReply extends AODVMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3718506202552660351L;
	
	private static final String LIFETIME = "lifetime";
	private static final String DESTINATION_SEQUENCE_NUMBER = "destinationSequenceNumber";
		
	private int sourceSequenceNumber;
	private long lifetime;
	private int destinationSequenceNumber;

	public RouteReply()
	{		
	}

	public RouteReply(String destinationAddress, int destinationSequenceNumber, String sourceAddress, int sourceSequenceNumber) {
		super();
		buildRREP(destinationAddress, destinationSequenceNumber,
				sourceAddress, sourceSequenceNumber, null, 0,
				System.currentTimeMillis() + Constants.MY_ROUTE_TIMEOUT);
	}

	public RouteReply(String destinationAddress, int destinationSequenceNumber, String sourceAddress, int sourceSequenceNumber,
			int hops, long lifetime) {
		super();
		buildRREP(destinationAddress, destinationSequenceNumber,
				sourceAddress, sourceSequenceNumber, null, hops, lifetime);
	}

	

	

	private void buildRREP(String destinationAddress, int destinationSequenceNumber, String sourceAddress,
			int sourceSequenceNumber, String nextHop, int hopCount, long lifetime) {
		setSourceAddress(sourceAddress);
		this.sourceSequenceNumber = sourceSequenceNumber;
		setDestinationAddress(destinationAddress);
		this.destinationSequenceNumber = destinationSequenceNumber;
		setNextHop(nextHop);
		setHopCount(hopCount);
		setLifetime(lifetime);
		
	}
	
	public int getSourceSequenceNumber() {
		return sourceSequenceNumber;
	}
	
	public void setSourceSequenceNumber(int sourceSequenceNumber) {
		this.sourceSequenceNumber = sourceSequenceNumber;
	}

	public long getLifetime() {
		return lifetime;
	}

	public void setLifetime(long lifetime) {
		this.lifetime = lifetime;
	}

	@Override
	public AODV_MESSAGE_TYPE getMessageType() {
		return AODV_MESSAGE_TYPE.RREP;
	}
	
	
	public int getDestinationSequenceNumber() {
		return destinationSequenceNumber;
	}

	public void setDestinationSequenceNumber(int destinationSequenceNumber) {
		this.destinationSequenceNumber = destinationSequenceNumber;
	}
	
	@Override
	protected JSONObject toJsonObject(JSONObject json) {
		super.toJsonObject(json);
		try {
			json.put(DESTINATION_SEQUENCE_NUMBER, destinationSequenceNumber);
			json.put(LIFETIME, lifetime);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json;
	}
	
	@Override
	protected void instanceFromJson(JSONObject json) {
		super.instanceFromJson(json);
		destinationSequenceNumber = json.optInt(DESTINATION_SEQUENCE_NUMBER, -1);
		lifetime = json.optLong(LIFETIME, -1);
	}
	
}
