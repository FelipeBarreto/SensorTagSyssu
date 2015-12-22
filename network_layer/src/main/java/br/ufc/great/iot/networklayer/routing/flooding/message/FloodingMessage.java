package br.ufc.great.iot.networklayer.routing.flooding.message;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import br.ufc.great.iot.networklayer.routing.IRouter;
import br.ufc.great.iot.networklayer.routing.exception.TimeToLiveExpiredException;
import br.ufc.great.iot.networklayer.routing.message.UserDataMessage;

public class FloodingMessage extends UserDataMessage implements Serializable{

	/**
	 * 
	 */
	public static final String FLOODING_MESSAGE = "flooding_message"; 
	private static final long serialVersionUID = -4208617032628271753L;
	private static final String SEQUENCE_NUMBER = "sequenceNumber";
	private static final String TTL = "ttl";
	
	
	private int ttl;
	private int sequenceNumber;
	
	public FloodingMessage() {
	}
	
	public FloodingMessage(String destination, JSONObject content, int ttl, int sequenceNumber)
	{		
		super(content);
		this.ttl = ttl;
		this.sequenceNumber = sequenceNumber;
		setDestinationAddress(destination);		
		setNextHop(IRouter.BROADCAST_EXCLUDING_SOURCE);
		
	}

	public int getTtl() {
		return ttl;
	}

	public void decrementTtl() throws TimeToLiveExpiredException {
		this.ttl--;
		if(ttl == 0)
		{
			throw new TimeToLiveExpiredException("Flood");
		}
	
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}
			
	@Override
	public String getType() {
		return FLOODING_MESSAGE;
	}
		
	@Override
	protected JSONObject toJsonObject(JSONObject json) {
		super.toJsonObject(json);
		try {
			json.put(SEQUENCE_NUMBER, sequenceNumber);
			json.put(TTL, ttl);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
	
	@Override
	protected void instanceFromJson(JSONObject json) {
		super.instanceFromJson(json);
		setContent(json.optJSONObject(UserDataMessage.CONTENT));
		sequenceNumber = json.optInt(SEQUENCE_NUMBER, -1);
		ttl = json.optInt(TTL, -1);
		
	}
}

