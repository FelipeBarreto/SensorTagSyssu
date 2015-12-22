package br.ufc.great.iot.networklayer.routing.aodv;

public interface Constants {
	
	//Broadcast ID
	public static final int MAX_BROADCAST_ID = Integer.MAX_VALUE;
	public static final int FIRST_BROADCAST_ID = 0;
	
	//Sequence Numbers
	public static final int MAX_SEQUENCE_NUMBER = Integer.MAX_VALUE;
    public static final int INVALID_SEQUENCE_NUMBER = -1;
    public static final int UNKNOWN_SEQUENCE_NUMBER = 0;
    public static final int FIRST_SEQUENCE_NUMBER = 1;
    public static final int SEQUENCE_NUMBER_INTERVAL = (Integer.MAX_VALUE / 2);
	
	//alive time for a route 
	public static final int ACTIVE_ROUTE_TIMEOUT = 3000;
	public static final int MY_ROUTE_TIMEOUT = 2 * ACTIVE_ROUTE_TIMEOUT;
	
	public static final int MAX_NUMBER_OF_RREQ_RETRIES = 2;

	public static final int NODE_TRANVERSAL_TIME = 300;
	public static final int NET_DIAMETER = 15;
	public static final int NET_TRANSVERSAL_TIME = 2 * NODE_TRANVERSAL_TIME * NET_DIAMETER;
	public static final int RING_TRANSVERSAL_TIME = 2 * NODE_TRANVERSAL_TIME;
		
	public static final int TTL_START = 5;
	public static final int TTL_INCREMENT = 2;
	public static final int TTL_THRESHOLD = 7;
	
	public static final int K = 5;
	public static final int HELLO_INTERVAL = 2000;
	public static final int ALLOWED_HELLO_LOSS = 2;
	public static final int DELETE_PERIOD = K * Math.max(ACTIVE_ROUTE_TIMEOUT, HELLO_INTERVAL);

	

}
