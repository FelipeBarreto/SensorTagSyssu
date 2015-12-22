package br.ufc.great.iot.networklayer.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import android.content.Context;
import br.ufc.great.iot.networklayer.routing.aodv.message.AODVMessage;
import br.ufc.great.iot.networklayer.routing.aodv.message.RouteError;
import br.ufc.great.iot.networklayer.routing.aodv.message.RouteReply;
import br.ufc.great.iot.networklayer.routing.aodv.message.RouteRequest;
import br.ufc.great.iot.networklayer.routing.message.HelloMessage;
import br.ufc.great.iot.networklayer.routing.message.RoutingMessage;
import br.ufc.great.iot.networklayer.routing.message.UserDataMessage;

public class Debug {

	private final String path = "/sdcard/aodvdebug.txt";
	private static FileWriter logFile = null;
	private Context c;
	
	public Debug(Context c) {
		this.c = c;
		try {
			logFile = new FileWriter(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void log(String message)
	{
		if(logFile != null){
			try {
				logFile.append(message);
				logFile.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void debugMessage(StringBuffer message)
	{
		StringBuffer msg = new StringBuffer(System.currentTimeMillis()+" ");
		msg.append(message);
		msg.append("\n");
		log(msg.toString());
	}
	
	public static void debugMessage(RoutingMessage message,
			List<String> myAddresses, boolean received, boolean sucess) {
		
		if(message != null)
		{
			if(message.getType().equalsIgnoreCase(AODVMessage.AODV_MESSAGE)){
				switch (((AODVMessage) message).getMessageType()) {
				case RREQ:
					debugRouteRequest((RouteRequest) message, myAddresses, received, sucess);
				break;
					
				case RREP:
					debugRouteReply((RouteReply) message, myAddresses, received, sucess);
				break;
				
				case RRER:
					debugRouteError((RouteError) message, myAddresses, received, sucess);
				break;
	
				default:
					break;
				}
			} else {
				if(!message.getType().equals(HelloMessage.class.getCanonicalName()))
				{
					debugDataMessage((UserDataMessage) message, myAddresses, received, sucess);
				}
			}
		}
	}

	private static void debugRouteError(RouteError rrer,
			List<String> myAddresses, boolean received, boolean sucess) {
		
		StringBuffer message = new StringBuffer();
		debugInOutMessage(received, message, sucess);
		message.append(" RRER: -Iam ");
		if(myAddresses != null)
		{
			message.append(myAddresses.toString());
		}
		message.append(" -S " + rrer.getSourceAddress() + " -D " + rrer.getDestinationAddress());
		message.append(" -unrDes " + rrer.getUnreachableNodeAddress() + " -unrDestSeq " + rrer.getUnreachableNodeSequenceNumber());
		message.append(" -previous " + rrer.getPreviousHop());
		message.append(" -hop " + rrer.getHopCount());
		message.append("\n");			
		Debug.log(message.toString());
	}

	private static void debugDataMessage(UserDataMessage dataMessage, List<String> myAddresses, boolean received, boolean sucess) {
		
		
		StringBuffer message = new StringBuffer();
		debugInOutMessage(received, message, sucess);
		message.append("Data: -Iam ");
		if(myAddresses != null)
		{
			message.append(myAddresses.toString());
		} 
		message.append(" -S " + dataMessage.getSourceAddress() + " -D " + dataMessage.getDestinationAddress());
		message.append(" -previous " + dataMessage.getPreviousHop());
		message.append("\n");			
		Debug.log(message.toString());
	}

	private static void debugInOutMessage(boolean received, StringBuffer message, boolean succes) {
		message.append(System.currentTimeMillis());
		if(received)
		{
			message.append(" Received ");
		}
		else {
			message.append(" Sent ");
		}
	}
	
	private static void debugRouteRequest(RouteRequest routeRequest, List<String> myAddresses, boolean received, boolean sucess) {		
			
		/*
			Log.d(className, "----- Route Request Received -----");
			Log.d(className, "* Iam: " + myAddresses.toString() +" *");
			Log.d(className, "* From: " + routeRequest.getSourceAddress() +" *");
			Log.d(className, "* To: " + routeRequest.getDestinationAddress() +" *");
			Log.d(className, "* SrcSeq: " + routeRequest.getSourceSequenceNumber() +
					" DestSeq: " + routeRequest.getDestinationSequenceNumber());
			Log.d(className, "* NextHop: " + routeRequest.getNextHop() +" *");
			Log.d(className, "* Previous: " + routeRequest.getPreviousHop() +" *");
			Log.d(className, "-------------------------");
			*/
			
			StringBuffer message = new StringBuffer();
			debugInOutMessage(received, message, sucess);
			message.append(" RREQ: -Iam ");
			if(myAddresses != null)
			{
				message.append(myAddresses.toString());
			}
			message.append(" -S " + routeRequest.getSourceAddress() + " -D " + routeRequest.getDestinationAddress());
			message.append(" -SrcSeq " + routeRequest.getSourceSequenceNumber() + " -DestSeq " + routeRequest.getDestinationSequenceNumber());
			message.append(" -previous " + routeRequest.getPreviousHop());
			message.append(" -ttl " + routeRequest.getTTL());
			message.append(" -hop " + routeRequest.getHopCount());
			message.append("\n");			
			Debug.log(message.toString());
		
	}
	
	private static void debugRouteReply(RouteReply routeReply, List<String> myAddresses, boolean received, boolean success) {		
		
		/*
		Log.d(className, "----- Route Reply Received -----");
		Log.d(className, "* Iam: " + myAddresses.toString() +" *");
		Log.d(className, "* From: " + routeReply.getSourceAddress() +" *");
		Log.d(className, "* To: " + routeReply.getDestinationAddress() +" *");
		Log.d(className, "* SrcSeq: " + routeReply.getSourceSequenceNumber() +
				" DestSeq: " + routeReply.getDestinationSequenceNumber());
		Log.d(className, "* NextHop: " + routeReply.getNextHop() +" *");
		Log.d(className, "* Previous: " + routeReply.getPreviousHop() +" *");
		Log.d(className, "-------------------------");
		*/
		
		StringBuffer message = new StringBuffer();
		debugInOutMessage(received, message, success);
		message.append(" RREP: -Iam ");
		if(myAddresses != null)
		{
			message.append(myAddresses.toString());
		}
		message.append(" -S " + routeReply.getSourceAddress() + " -D " + routeReply.getDestinationAddress());
		message.append(" -SrcSeq " + routeReply.getSourceSequenceNumber() + " -DestSeq " + routeReply.getDestinationSequenceNumber());
		message.append(" -nextHop " + routeReply.getNextHop() + " -previous " + routeReply.getPreviousHop());
		message.append("\n");			
		log(message.toString());
	}
	
	
	public void close()
	{	
		if(logFile != null)
		{
			try {
				logFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}	
}
