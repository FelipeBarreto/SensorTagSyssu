package br.ufc.great.somc.network.base;

import android.os.Bundle;


interface BluetoothListener
{	
	void onDeviceFound(String name, String address);
	void onDeviceConnect(String name, String address);
	void onDeviceDisconnect(String name, String address);
	void onMessageReceived(String jsonMessage);
	void onExceptionOcurred(int code, String message);
	
	void onNotNeighborDeviceFound(String deviceName, String deviceAddress);
	void onMessageSent(String jsonMessage);
	void onStateChanged(int state, int previousState);	
	void onStateScanModeChanged(int action, int previousState);
	void onStateObservingChanged(int action, int previousState);
	void onStateDiscoveryChanged(String action);	
}