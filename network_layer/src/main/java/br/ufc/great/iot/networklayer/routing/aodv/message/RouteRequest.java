package br.ufc.great.iot.networklayer.routing.aodv.message;

import org.json.JSONException;
import org.json.JSONObject;

import br.ufc.great.iot.networklayer.routing.IRouter;
import br.ufc.great.iot.networklayer.routing.aodv.Constants;
import br.ufc.great.iot.networklayer.routing.exception.TimeToLiveExpiredException;

/**
 * AODV Route Request Message
 * @author bruno
 */
public class RouteRequest extends AODVMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4912007210272339039L;
	private static final String HOP_COUNT = "hopCount";
	private static final String SOURCE_SEQUENCE_NUMBER = "sourceSequenceNumber";
	private static final String BROADCAST_ID = "broadcastId";
	private static final String DESTINATION_SEQUENCE_NUMBER = "destinationSequenceNumber";	
	private static final String TTL = "ttl";
	
	
	
	private int sourceSequenceNumber;
	private int broadcastId;
	private int destinationSequenceNumber;
	private int ttl = Constants.TTL_START;
	
	public RouteRequest() {
	}

	public RouteRequest(int sourceSequenceNumber, int broadcastId,
			String destinationAddress, int destinationSequenceNumber) {
		super();		
		buildRREQ(sourceSequenceNumber, broadcastId, destinationAddress,
				destinationSequenceNumber, 0);
	}
	
	public RouteRequest(int sourceSequenceNumber, int broadcastId,
			String destinationAddress, int destinationSequenceNumber,
			int retriesLeft) {
		buildRREQ(sourceSequenceNumber, broadcastId, destinationAddress,
				destinationSequenceNumber, retriesLeft);
	}

	private void buildRREQ(int sourceSequenceNumber, int broadcastId,
			String destinationAddress, int destinationSequenceNumber,
			int retriesLeft) {
		this.sourceSequenceNumber = sourceSequenceNumber;
		this.broadcastId = broadcastId;
		this.destinationSequenceNumber = destinationSequenceNumber;
		setDestinationAddress(destinationAddress);
		setNextHop(IRouter.BROADCAST_EXCLUDING_SOURCE);
		setTTL(retriesLeft);
	}
	
	public int getSourceSequenceNumber() {
		return sourceSequenceNumber;
	}
	
	public void setSourceSequenceNumber(int sourceSequenceNumber) {
		this.sourceSequenceNumber = sourceSequenceNumber;
	}
	
	public int getBoadcastId() {
		return broadcastId;
	}
	public void setBroadcastId(int boadcastId) {
		this.broadcastId = boadcastId;
	}

	@Override
	public AODV_MESSAGE_TYPE getMessageType() {
		return AODV_MESSAGE_TYPE.RREQ;
	}


	public int getDestinationSequenceNumber() {
		return destinationSequenceNumber;
	}


	public void setDestinationSequenceNumber(int destinationSequenceNumber) {
		this.destinationSequenceNumber = destinationSequenceNumber;
	}	
	
	public int getTTL() {
		return ttl;
	}

	public void setTTL(int retryNumber) {
		if(retryNumber < Constants.MAX_NUMBER_OF_RREQ_RETRIES)
		{
			ttl = Constants.TTL_START + retryNumber*Constants.TTL_INCREMENT;
		} 
		else
		{
			ttl = Constants.NET_DIAMETER;
		}
	}
	
	public void decrementTtl() throws TimeToLiveExpiredException {
		this.ttl--;
		if(ttl == 0)
		{
			throw new TimeToLiveExpiredException("AODV TTL");
		}
	
	}
	
	@Override
	protected JSONObject toJsonObject(JSONObject json) {
		super.toJsonObject(json);
		
		try {
			json.put(SOURCE_SEQUENCE_NUMBER, sourceSequenceNumber);
			json.put(DESTINATION_SEQUENCE_NUMBER, destinationSequenceNumber);
			json.put(BROADCAST_ID, broadcastId);
			json.put(HOP_COUNT, getHopCount());
			json.put(TTL, ttl);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json;
	}

	@Override
	protected void instanceFromJson(JSONObject json) {
		super.instanceFromJson(json);
		sourceSequenceNumber = json.optInt(SOURCE_SEQUENCE_NUMBER, -1);
		destinationSequenceNumber = json.optInt(DESTINATION_SEQUENCE_NUMBER, -1);
		broadcastId = json.optInt(BROADCAST_ID, -1);
		setHopCount(json.optInt(HOP_COUNT, -1));
		ttl = json.optInt(TTL, -1);
	}

}
