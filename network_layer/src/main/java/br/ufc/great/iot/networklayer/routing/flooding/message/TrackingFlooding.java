package br.ufc.great.iot.networklayer.routing.flooding.message;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.ufc.great.iot.networklayer.base.NetworkManager;
import br.ufc.great.iot.networklayer.routing.exception.TimeToLiveExpiredException;
import br.ufc.great.iot.networklayer.util.Simulation;

public class TrackingFlooding extends FloodingMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7289920453197922555L;

	private static final String PATH = "path";
	
	private ArrayList<String> path = new ArrayList<String>();
	
	public TrackingFlooding()
	{
	}
	
	public TrackingFlooding(String destination, JSONObject content, int ttl, int sequenceNumber)
	{
		super(destination, content, ttl, sequenceNumber);		
	}
	
	@Override
	public void decrementTtl() throws TimeToLiveExpiredException {
		super.decrementTtl();
		updatePath();
	}
	
	private void updatePath()
	{
		String previousHop = getPreviousHop();
		if(previousHop != null)
		{
			path.add(previousHop);
		}
	}

	public List<String> getPath() {
		return path;
	}
	
	@Override
	protected void instanceFromJson(JSONObject json) {
		super.instanceFromJson(json);
		JSONArray pathArray = json.optJSONArray(PATH);
		for(int i = 0; i < pathArray.length(); i++)
		{
			path.add(pathArray.optString(i, null));
		}
	}
	
	@Override
	protected JSONObject toJsonObject(JSONObject json) {
		super.toJsonObject(json);
		try {
			json.put(PATH, new JSONArray(path));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
	
	@Override
	public String toString() {
		StringBuffer messageString = new StringBuffer(super.toString());
		messageString.append(" -path ");
		for (String deviceName : path) {
			messageString.append(Simulation.somcDevices.get(deviceName)+", ");
		}
		
		return messageString.toString();
	}
	
	

}
