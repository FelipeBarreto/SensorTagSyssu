package br.ufc.great.iot.networklayer.routing.aodv.route;

import br.ufc.great.iot.networklayer.routing.aodv.Constants;
import br.ufc.great.iot.networklayer.routing.aodv.exception.InvalidParametersException;
import br.ufc.great.iot.networklayer.routing.aodv.message.RouteRequest;

public class RouteRequestEntry extends RouteEntry {

	private static final String MINE = "mine";
	private final String sourceAddress;
	private int brodcastId;
	private int retries = 0;
	private int ttl;
	
	public RouteRequestEntry(RouteRequest rreq) throws InvalidParametersException {
		super(rreq.getDestinationAddress(), rreq.getDestinationSequenceNumber(), rreq.getHopCount());
		if(rreq.getSourceAddress() == null)
		{
			this.sourceAddress = MINE;
		}
		else
		{
			this.sourceAddress = rreq.getSourceAddress();
		}
		this.brodcastId = rreq.getBoadcastId();
		this.ttl = rreq.getTTL();
		resetAlivetimeLeft();
	}
	
	public int getBrodcastId() {
		return brodcastId;
	}
	
	public String getSourceAddress() {
		return sourceAddress;
	}
	
	public void setBroadcastId(int broacastId)
	{
		this.brodcastId = broacastId;
	}
	
	@Override
	public void resetAlivetimeLeft() {
		synchronized (aliveLock) {
			if(retries == Constants.MAX_NUMBER_OF_RREQ_RETRIES)
			{
				setAlivetimeLeft(System.currentTimeMillis() + Constants.NET_TRANSVERSAL_TIME);
			}
			else
			{
				setAlivetimeLeft(System.currentTimeMillis() + Constants.RING_TRANSVERSAL_TIME * ttl);
			}
		}		
	}
	
	public boolean resend(){
		retries++;		
		if(retries >= Constants.MAX_NUMBER_OF_RREQ_RETRIES){			
			return false;
		}
		return true;
	}
	
	/**
	 * Only used for debugging purposes!
	 * @return number of flood retries left
	 */
	public int getRetriesLeft(){
		return retries;
	}
	
	public boolean isMine()
	{
		return getSourceAddress().equalsIgnoreCase(MINE);
	}
}
