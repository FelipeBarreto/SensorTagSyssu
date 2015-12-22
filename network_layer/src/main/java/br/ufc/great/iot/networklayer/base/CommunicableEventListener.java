package br.ufc.great.iot.networklayer.base;

public interface CommunicableEventListener {

	void onDeviceFound(String name, String address);

	void onDeviceConnect(String name, String address);

	void onDeviceDisconnect(String name, String address);

	void onExceptionOcurred(int code, String message);

	void updateNeighboorList(String name, String address, String canonicalName);

	void onActiveNetwork(String myAdresses);

	void onDesactiveNetwork(String myAddress);

}
