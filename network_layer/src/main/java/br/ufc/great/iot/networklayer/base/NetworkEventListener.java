package br.ufc.great.iot.networklayer.base;

import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;

/**
 *  Callback Interface to notify Views for these specific network events.
 * @author sauloaguiar
 *
 */
public interface NetworkEventListener {

	/**
	 * 	Notify when the device has received a message
	 * @param deviceName 
	 * 	Device Name
	 * @param deviceAddress
	 *  Device MAC Address  
	 * @param message
	 * 	The received message
	 */
	public void onReceiveMessage(String deviceAddress, JSONObject message);

	/**
	 * 	Notify when a connection is established
	 * @param deviceName
	 * 	Device Name
	 * @param deviceAddress 
	 * 	 Device MAC Address
	 */
	void onDeviceConnected(String deviceName, String deviceAddress);
	
	/**
	 * Notify the device connection state
	 * @param state
	 *  STATE_NONE  		the device is idle
	 *  STATE_LISTEN 		listening for incoming connections
	 *  STATE_CONNECTING    initiating an outgoing connection
	 *  STATE_CONNECTED     now connected to a remote device
	 * @param previousState
	 * @see BluetoothAdapter
	 */
	void onStateConnectionChanged(int state, int previousState);

	/**
	 * 	Notify when a connection has been lost
	 * @param deviceName
	 * @param deviceAddress 
	 */
	public void onDeviceDisconnected(String deviceAddress);
	
	/**
	 *  Notify when a device was discovered thru scanning
	 * @param name 
	 * 	Device Name
	 * @param address 
	 * 	 Device MAC Address
	 */
	public void onDeviceFound(String name, String address);

	/**
	 * @param message 
	 */	
	void onExceptionOccurred(String message);
	
	void onNotNeighborFound(String macAddress);
}
