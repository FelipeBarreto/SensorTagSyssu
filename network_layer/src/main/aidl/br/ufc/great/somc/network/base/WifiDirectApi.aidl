package br.ufc.great.somc.network.base;

import br.ufc.great.somc.network.base.WifiDirectListener;

interface WifiDirectApi {
	void addListener(WifiDirectListener listener);
	void removeListener(WifiDirectListener listener);
	
	void sendBroadcastMessage(String jsonMessage, String avoidAddress);
	void sendUnicastMessage(String jsonMessage, String destinationAddress);
	
	void startDiscovery();
	void createGroup();
	void removeGroup();
	
	void requestGroupIno();
	void manualConnect(String deviceAddress);

	int getCurrentState();
	List<String> getNeighborhood();
	String getMyAddress();
	
}
