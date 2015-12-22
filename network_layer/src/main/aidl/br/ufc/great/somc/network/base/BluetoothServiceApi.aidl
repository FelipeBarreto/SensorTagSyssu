package br.ufc.great.somc.network.base;

import br.ufc.great.somc.network.base.BluetoothListener;
import android.bluetooth.BluetoothDevice;
import java.util.List;


interface BluetoothServiceApi
{ 
	void addListener(BluetoothListener listener);
	void removeListener(BluetoothListener listener);
	void sendBroadcastMessage(String jsonMessage, String avoidAddress);
	void sendUnicastMessage(String jsonMessage, String destinationAddress);
	int getCurrentState();
	List<String> getNeighboord();
	String getMyAddress();
	
	
	void startDiscovery();
	boolean ensureDiscoverable();	
	BluetoothDevice getRemoteDevice(String address);
	void manualConnect(String address);	
	int getObservingTime();
	boolean startObserver();
	void stopObserver();	
}