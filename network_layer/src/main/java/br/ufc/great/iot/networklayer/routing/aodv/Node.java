package br.ufc.great.iot.networklayer.routing.aodv;

public class Node {

	private int sequenceNumber = Constants.FIRST_SEQUENCE_NUMBER;
	private int broadcastId = Constants.FIRST_BROADCAST_ID;
	private static Node node;
	
	private Node()
	{
		
	}
	
	static Node getInstance()
	{
		if(node == null)
		{
			node = new Node();
		}
		return node;
	}
	
	int getNextBroadcastID() {
		if(broadcastId == Constants.MAX_BROADCAST_ID)
		{
			broadcastId = Constants.FIRST_BROADCAST_ID;
		} else {
			broadcastId++;
		}
		return broadcastId;
	}

	int getCurrentSequenceNumber()
	{
		return sequenceNumber;
	}
	
	int getCurrentBroadcastID()
	{
		return broadcastId;
	}
	
	 int getNextSequenceNumber(){
		if(sequenceNumber == Constants.MAX_SEQUENCE_NUMBER ||
				sequenceNumber == Constants.UNKNOWN_SEQUENCE_NUMBER)
		{
			sequenceNumber = Constants.FIRST_SEQUENCE_NUMBER;
		} else {
			sequenceNumber++;
		}
		return sequenceNumber;
	}	
}
