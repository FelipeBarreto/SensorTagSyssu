package br.ufc.great.iot.networklayer.base;

import java.util.List;

import android.content.Context;
import br.ufc.great.iot.networklayer.exception.AddressNotDefinedException;

/**
 *	Base class to communicate with network
 *	When instantiating, an interface is needed to be notified when network events occurs
 *	Use it to get informations about the device, the network and to send messages
 *	Extend this class if you want to provide another network implementation
 */
public abstract class Communicable {

	protected CommunicableEventListener notifier;
	protected MessageEventListener messageNotifier;
	protected Context context;
	protected boolean active = false; 
	
	/**
	 * @param context
	 * @param notifier
	 */
	public Communicable(Context context,
			CommunicableEventListener notifier, MessageEventListener messageNotifier) {
		this.notifier = notifier;
		this.context = context;
		this.messageNotifier = messageNotifier;
	}
	
	/**
	 * 
	 */
	public abstract void onStart();
	
	/**
	 * 
	 */
	public abstract void onStop();
	
	/**
	 * Send a message in a unicast way
	 * @param msg
	 */
	public abstract boolean sendMessage(String jsonMessage, String destinationAddress);

	/**
	 * Send a message in a broadcast way except to the specified address 
	 * @param message	The message
	 * @param avoidAddress 	The address that will not receive the message
	 */
	public abstract boolean sendBroadcastMessage(String jsonMessage, String avoidAddress);
	
	/**
	 * @return String containing the device address
	 */
	public abstract String getMyAddress() throws AddressNotDefinedException;

	/**
	 * @return List<String> containing the Neighborhood addresses
	 */
	public abstract List<String> getNeighborhood();
	
	
	/**
	 * @return true if the network is active
	 * 		   false otherwise
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 *  Notify when a device was discovered thru scanning
	 * @param name 
	 * 	Device Name
	 * @param address 
	 * 	 Device MAC Address
	 */
	protected void onDeviceFound(String name, String address)
	{
		notifier.onDeviceFound(name, address);
	}

	/**
	 * 	Notify when a connection is established
	 * @param deviceName
	 * 	Device Name
	 * @param deviceAddress 
	 * 	 Device MAC Address
	 */
	protected void onDeviceConnect(String name, String address)
	{
		notifier.onDeviceConnect(name, address);
		updateNeighboorList(name, address);
	}

	/**
	 * 	Notify when a connection has been lost
	 * @param deviceName
	 * @param deviceAddress 
	 */
	protected void onDeviceDisconnect(String name, String address)
	{
		notifier.onDeviceDisconnect(name, address);
	}

	/**
	 * @param code
	 * @param message
	 */
	protected void onExceptionOcurred(int code, String message)
	{
		notifier.onExceptionOcurred(code, message);
	}

	/**
	 * Notify when the network layer has received a message
	 * @param message
	 */
	protected void onMessageReceived(String jsonMessage)
	{
		messageNotifier.onMessageReceived(jsonMessage);
	}
	
	/**
	 * Notify when the network layer has sent a message
	 * @param message
	 */
	/*protected void onMessageSent(RoutingMessage message)
	{
		
	}*/
	
	/**
	 * Notify when the network layer has updated some node address
	 * @param name
	 * @param address
	 */
	protected void updateNeighboorList(String name, String address)
	{
		notifier.updateNeighboorList(name, address, this.getClass().getCanonicalName());
	}
	
	/**
	 * 
	 */
	protected void onActiveNetwork()
	{
		this.active = true;
		try {
			notifier.onActiveNetwork(getMyAddress());
		} catch (AddressNotDefinedException e) {
			e.printStackTrace();
		}
	}
	
	protected void onDesactiveNetwork()
	{	
		this.active = false;
		try {
			notifier.onDesactiveNetwork(getMyAddress());
		} catch (AddressNotDefinedException e) {
			
			e.printStackTrace();
		}
	}

	
	

}
