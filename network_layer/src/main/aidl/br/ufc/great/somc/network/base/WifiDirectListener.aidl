package br.ufc.great.somc.network.base;

interface WifiDirectListener {

	void onStateConnectionChanged(int state, int previousState);
	void onDeviceFound(String name, String address);
	void onDeviceConnect(String name, String address);
	void onDeviceDisconnect(String name, String address);
	void onMessageReceived(String jsonMessage);
	void onExceptionOccurred(int code, String message);
	
	void onWifiStateChanged(int state); 
	void onConnectionInfo(String groupInfo);
	
}