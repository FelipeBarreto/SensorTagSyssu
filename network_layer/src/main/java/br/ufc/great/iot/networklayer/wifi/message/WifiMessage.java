package br.ufc.great.iot.networklayer.wifi.message;

import java.io.Serializable;

public abstract class WifiMessage implements Serializable {

	private static final long serialVersionUID = 8606490731764622087L;

	private String destinationIpAddress;
	private String sourceName;
	private String sourceIpAdress;
	private long timestamp;
	private int ttl;
	private static final int TTL = 3;
	
	public enum WifiTypeMessage{HELLO, HELLO_REPLY, BYE, NEIGHBORHOOD_REQUEST, MSG}

	public WifiMessage() {
		this.ttl = TTL;
		this.timestamp = System.currentTimeMillis();
	}

	public abstract WifiTypeMessage getType();
	
	public String getDestinationIpAddress() {
		return destinationIpAddress;
	}

	public void setDestinationIpAddress(String destinationMacAddress) {
		this.destinationIpAddress = destinationMacAddress;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public String getSourceIpAddress() {
		return sourceIpAdress;
	}

	public void setSourceIpAddress(String sourceAddress) {
		this.sourceIpAdress = sourceAddress;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}
	
	
}
