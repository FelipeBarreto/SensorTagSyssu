package br.ufc.great.iot.networklayer.wifi;

public interface WifiListener {

	void onMesssageReceived(String deviceAddress, String message);

	void onDeviceConnected(String sourceAddress);

	void onDeviceDisconnected(String sourceIpAddress);

}
