package br.ufc.great.iot.networklayer.routing.flooding;

public class MessageEntry {
	
	private String sourceAddress;
	private int sequenceNumber;
	private long timeStamp;
	
	public MessageEntry(String sourceAddress, int sequenceNumber) {
		super();
		this.sourceAddress = sourceAddress;
		this.sequenceNumber = sequenceNumber;
		timeStamp = System.currentTimeMillis();
	}	 
	
	public String getSourceAddress() {
		return sourceAddress;
	}
	
	public void setSourceAddress(String sourceAddress) {
		this.sourceAddress = sourceAddress;
	}
	
	public int getSequenceNumber() {
		return sequenceNumber;
	}
	
	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	
	public long getTimeStamp()
	{
		return timeStamp;
	}
	
	@Override
	public boolean equals(Object o) {
		MessageEntry msg = ((MessageEntry) o);
		if(msg.getSequenceNumber() == this.getSequenceNumber() && 
				msg.getSourceAddress().equalsIgnoreCase(getSourceAddress()))
		{
			return true;
		}		
		return false; 
	}
}
