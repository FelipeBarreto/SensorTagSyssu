package br.ufc.great.iot.networklayer.routing.message;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.ufc.great.iot.networklayer.routing.IRouter;
import br.ufc.great.iot.networklayer.routing.exception.TimeToLiveExpiredException;

public class HelloMessage extends RoutingMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3836020684298857526L;
	private static final int DEFAULT_TTL = 4; 
	private static final String PATH = "path";
	private static final String TTL = "ttl";	
	/**
	 * 
	 */
	private int ttl;
	private List<String> path;

	public HelloMessage()
	{		
		this.ttl = DEFAULT_TTL;		
		setDestinationAddress(IRouter.BROADCAST_EXCLUDING_SOURCE);		
		setNextHop(IRouter.BROADCAST_EXCLUDING_SOURCE);
		path = new ArrayList<String>();
	}

	public int getTtl() {
		return ttl;
	}

	public void decrementTtl() throws TimeToLiveExpiredException {
		this.ttl--;
		updatePath();
		if(ttl == 0)
		{
			//throw new TimeToLiveExpiredException("Hello");
		}		
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
	protected JSONObject toJsonObject(JSONObject json) {
		super.toJsonObject(json);
		try {
			json.put(TTL, this.ttl);
			json.put(PATH, new JSONArray(path));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
	
	@Override
	protected void instanceFromJson(JSONObject json) {
		super.instanceFromJson(json);
		ttl = json.optInt(TTL, -1);
		JSONArray pathArray = json.optJSONArray(PATH);
		for(int i = 0; i < pathArray.length(); i++)
		{
			path.add(pathArray.optString(i, null));
		}
		
	}
	
	//TODO: Remove
	public void setTTL(int ttl)
	{
		this.ttl = ttl;
	}
	
	//TODO: Remove
	public void setPath(List<String> path)
	{
		this.path = path;
	}
		
}
