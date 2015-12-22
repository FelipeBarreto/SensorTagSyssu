package br.ufc.great.iot.networklayer.routing.message;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import org.json.JSONException;
import org.json.JSONObject;

import br.ufc.great.iot.networklayer.util.Simulation;

import com.google.gson.JsonSyntaxException;

public class RoutingMessage extends AbstractMessage implements Serializable {

	/**
	 * 
	 */
	protected static final long serialVersionUID = -7156171251311565393L;

	protected static final String APPLICATION_ID = "appID";
	protected static final String SRC_ADDRESS = "sourceAddress";
	protected static final String DEST_ADDRESS = "destinationAddress";
	protected static final String NEXT_HOP = "nextHop";
	protected static final String PREVIOUS_HOP = "previousHop";
	protected static final String TYPE = "type";

	// TODO: Remover
	protected static final String COUNT = "hopCount";
	private int hopCount = 0;

	public void incrementHops() {
		hopCount++;
	}

	public int getHopCount() {
		return hopCount;
	}

	private String applicationID;

	private String sourceAddress;

	private String destinationAddress;

	private String nextHop;

	private String previousHop;

	private transient String sourceID;

	protected String jsonType = this.getClass().getCanonicalName();

	public RoutingMessage() {
	}

	public String getSourceAddress() {
		return sourceAddress;
	}

	public void setSourceAddress(String sourceAddress) {
		this.sourceAddress = sourceAddress;
	}

	public String getNodeID() {
		return sourceID;
	}

	public void setNodeID(String nodeID) {
		this.sourceID = nodeID;
	}

	public String getDestinationAddress() {
		return destinationAddress;
	}

	public void setDestinationAddress(String destinationAddres) {
		this.destinationAddress = destinationAddres;
	}

	public String getNextHop() {
		return nextHop;
	}

	public void setNextHop(String nextHop) {
		this.nextHop = nextHop;
	}

	public String getPreviousHop() {
		return previousHop;
	}

	public void setPreviousHop(String sourceAddress) {
		previousHop = sourceAddress;
	}

	final String getJsonType() {
		return jsonType;
	}

	public String getApplicationID() {
		return applicationID;
	}

	public void setApplicationID(String applicationID) {
		this.applicationID = applicationID;
	}

	public static String toJson(RoutingMessage message) {

		JSONObject routingJSON = null;
		routingJSON = new JSONObject();

		return message.toJsonObject(routingJSON).toString();
	}

	public static RoutingMessage getFromJson(String json) {
		String type = null;
		RoutingMessage message = null;
		try {
			JSONObject rMessage = new JSONObject(json);
			type = rMessage.get(TYPE).toString();
			message = (RoutingMessage) Class.forName(type).getConstructor()
					.newInstance();
			message.instanceFromJson(rMessage);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		return message;
	}

	protected JSONObject toJsonObject(JSONObject json) {
		try {
			json.put(APPLICATION_ID, applicationID);
			json.put(TYPE, jsonType);
			json.put(SRC_ADDRESS, sourceAddress);
			json.put(DEST_ADDRESS, destinationAddress);
			json.put(NEXT_HOP, nextHop);
			json.put(PREVIOUS_HOP, previousHop);

			json.put(COUNT, hopCount);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}

	protected void instanceFromJson(JSONObject json) {
		applicationID = json.optString(APPLICATION_ID, null);
		sourceAddress = json.optString(SRC_ADDRESS, null);
		destinationAddress = json.optString(DEST_ADDRESS, null);
		nextHop = json.optString(NEXT_HOP, null);
		previousHop = json.optString(PREVIOUS_HOP, null);

		hopCount = json.optInt(COUNT, -1);
	}

	@Override
	public String toString() {
		StringBuffer messageString = new StringBuffer();
		messageString.append(" -src "
				+ Simulation.somcDevices.get(getSourceAddress()));
		messageString.append(" -dest "
				+ Simulation.somcDevices.get(getDestinationAddress()));
		messageString.append(" -nh " + getNextHop());
		messageString.append(" -pre "
				+ Simulation.somcDevices.get(getPreviousHop()));
		messageString.append(" -hc " + getHopCount());
		return messageString.toString();
	}
}
