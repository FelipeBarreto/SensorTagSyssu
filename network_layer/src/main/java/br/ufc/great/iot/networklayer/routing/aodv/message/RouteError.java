package br.ufc.great.iot.networklayer.routing.aodv.message;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RouteError extends AODVMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -794723904537378260L;

	private int unreachableNodeSequenceNumber;
	private String unreachableNodeAddress;
	private ArrayList<String> unreachableDestAdderss = new ArrayList<String>();

	private static final String UNREACHABLE_NUMBER = "unreachableNodeSequenceNumber";
	private static final String UNREACHABLE_ADDRESS = "unreachableNodeAddress";
	private static final String ARRAY_UNREACHABLE = "unreachableDestAdderss";
	private static final String HOP_COUNT = "hopCount";
	
	public RouteError() {
	}
	
	
	public RouteError(String unreachableNodeAddress, int unreachableNodeSequenceNumber,
			ArrayList<String> unreachableDestAdderss) {
		this.unreachableNodeSequenceNumber = unreachableNodeSequenceNumber;
		this.unreachableNodeAddress = unreachableNodeAddress;
		this.unreachableDestAdderss = unreachableDestAdderss;
		setDestinationAddress("-1");
	}



	public RouteError(String unreachableNodeAddress, int unreachableNodeSequenceNumber,
			String destinationAddress) {
		this.unreachableNodeSequenceNumber = unreachableNodeSequenceNumber;
		this.unreachableNodeAddress = unreachableNodeAddress;
		setDestinationAddress(destinationAddress);
	
	}
	
	@Override
	public AODV_MESSAGE_TYPE getMessageType() {
		return AODV_MESSAGE_TYPE.RRER;
	}



	public int getUnreachableNodeSequenceNumber() {
		return unreachableNodeSequenceNumber;
	}



	public void setUnreachableNodeSequenceNumber(int unreachableNodeSequenceNumber) {
		this.unreachableNodeSequenceNumber = unreachableNodeSequenceNumber;
	}



	public String getUnreachableNodeAddress() {
		return unreachableNodeAddress;
	}



	public void setUnreachableNodeAddress(String unreachableNodeAddress) {
		this.unreachableNodeAddress = unreachableNodeAddress;
	}



	public ArrayList<String> getUnreachableDestAdderss() {
		return unreachableDestAdderss;
	}



	public void setUnreachableDestAdderss(ArrayList<String> unreachableDestAdderss) {
		this.unreachableDestAdderss = unreachableDestAdderss;
	}
	
	@Override
	protected JSONObject toJsonObject(JSONObject json) {
		super.toJsonObject(json);
		try {
			json.put(HOP_COUNT, getHopCount());
			json.put(UNREACHABLE_NUMBER, unreachableNodeSequenceNumber);
			json.put(UNREACHABLE_ADDRESS, unreachableNodeAddress);
			json.put(ARRAY_UNREACHABLE, new JSONArray(unreachableDestAdderss));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json;
	}
	
	@Override
	protected void instanceFromJson(JSONObject json) {
		super.instanceFromJson(json);
		setHopCount(json.optInt(HOP_COUNT, -1));
		unreachableNodeSequenceNumber = json.optInt(UNREACHABLE_NUMBER, -1);
		unreachableNodeAddress = json.optString(UNREACHABLE_ADDRESS, null);
		JSONArray pathArray = json.optJSONArray(ARRAY_UNREACHABLE);
		for(int i = 0; i < pathArray.length(); i++)
		{
			unreachableDestAdderss.add(pathArray.optString(i, null));
		}		
	}
}
