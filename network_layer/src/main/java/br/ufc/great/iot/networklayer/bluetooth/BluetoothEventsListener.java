package br.ufc.great.iot.networklayer.bluetooth;


/**
 * Callback Interface for Bluetooth Optional Events requests.
 */
public interface BluetoothEventsListener {

	/**
	 * 	Notify when the bluetooth behaviour change. 
	 *  The possible states are 
	 *  	SCAN_MODE_CONNECTABLE
	 *  		Indicates that inquiry scan is disabled, but page scan is enabled on the local Bluetooth adapter.
	 *  	SCAN_MODE_CONNECTABLE_DISCOVERABLE
	 *  		Indicates that both inquiry scan and page scan are enabled on the local Bluetooth adapter.
	 *  	SCAN_MODE_NONE
	 *  	Indicates that both inquiry scan and page scan are disabled on the local Bluetooth adapter.
	 * @param state
	 * @param previousState
	 */
	void onStateScanModeChanged(int state, int previousState);
	
	/**
	 *  Notify when the bluetooth adapter has started/finished an discovery action
	 *  The possible actions are
	 *  	ACTION_DISCOVERY_STARTED 
	 *  		Broadcast Action: The local Bluetooth adapter has started the remote device discovery process.
	 *  	ACTION_DISCOVERY_FINISHED
	 *  		Broadcast Action: The local Bluetooth adapter has finished the device discovery process.
	 * @param action
	 */
	void onStateDiscoveryChanged(String action);

	/**
	 *  Notify the current search action for the bluetooth network
	 *  The possible states are
	 *  	BLUETOOTH_SEARCH_WAIT_TIME
	 *  		Indicates the start wait time for discovering action.
	 *  	BLUETOOTH_UPDATE_SEACH_WAIT_TIME
	 *  		Updates the timer during the discovering.
	 *  	BLUETOOTH_STOPPED_OBSERVING   
	 *  		Indicates that the timer ended.
	 * @param state
	 * @param currentTime
	 */
	void onStateObservingChanged(int state, int currentTime);
	
	
	/**
	 * 	Notify when the bluetooth state change
	 * @param state
	 *            Used as an int extra field in ACTION_STATE_CHANGED intents to
	 *            request the previous power state. Possible values are:
	 *            STATE_OFF, STATE_TURNING_ON, STATE_ON, STATE_TURNING_OFF,
	 * @param previousState
	 */
	void onStateChange(int state, int previousState);
	
	void onNotNeighborDeviceFound(String deviceName, String deviceAddress);
}
