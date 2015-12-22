package br.ufc.great.iot.networklayer.routing.message;

import org.json.JSONException;
import org.json.JSONObject;

import br.ufc.great.iot.networklayer.routing.exception.TimeToLiveExpiredException;

public class UserDataMessage extends RoutingMessage {

	
	private static final int MAX_TTL = 5;
	protected static final String CONTENT = "content";
	private static final String TTL = "ttl";
	
	public UserDataMessage() {
		
	}
	
	public UserDataMessage(JSONObject content)
	{
		this.content = content;
		this.ttl = MAX_TTL;
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4078351328652905792L;
	
	private JSONObject content;
	private int ttl;
	
	public JSONObject getContent() {
		return content;
	}

	public void setContent(JSONObject content) {
		this.content = content;
	}
	
	public void decrementTtl() throws TimeToLiveExpiredException {
		this.ttl--;
		if(ttl == 0)
		{
			throw new TimeToLiveExpiredException("userDataMessage");
		}
	
	}
	
	@Override
	protected JSONObject toJsonObject(JSONObject json) {
		super.toJsonObject(json);
		
		try {
			json.put(CONTENT, content);
			json.put(TTL, ttl);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json;
	}
	
	@Override
	protected void instanceFromJson(JSONObject json) {
		super.instanceFromJson(json);
		content = json.optJSONObject(CONTENT);
		ttl = json.optInt(TTL, -1);
	}

	@Override
	public String toString() {
		StringBuffer messageString = new StringBuffer(super.toString());
		try{
				messageString.insert(0, " -id " + getContent().get("id"));
		} catch (JSONException e) {
				e.printStackTrace();
		}
		return messageString.toString();
	}
}
