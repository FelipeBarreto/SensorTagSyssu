package br.ufc.great.iot.networklayer.wifi;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.widget.Toast;
import br.ufc.great.iot.networklayer.base.Communicable;
import br.ufc.great.iot.networklayer.base.CommunicableEventListener;
import br.ufc.great.iot.networklayer.base.MessageEventListener;
import br.ufc.great.iot.networklayer.base.NetworkManager;

public class WifiCommunicable extends Communicable implements WifiListener {

	private NetworkObserver networkObserver;
	private ArrayList<String> neighborhood;
	
	public WifiCommunicable(Context context, CommunicableEventListener notifier, MessageEventListener messageNotifier) {
		super(context, notifier, messageNotifier);
	}

	@Override
	public void onStart() {
		if ( isWifiEnabled() ) {
			if ( isWifiConnected() ) {
				if ( networkObserver == null ) {
					networkObserver = new NetworkObserver(context, this);
					networkObserver.startObserver();
					neighborhood = new ArrayList<String>();
					onActiveNetwork();
				}
			}
		} else {
			Toast.makeText(context, "The wifi is off... you can turn it on :) ", Toast.LENGTH_SHORT).show();
			Intent i = new Intent(Settings.ACTION_WIFI_SETTINGS);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
	}

	
	private boolean isWifiEnabled(){
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		return wifi.isWifiEnabled();
	}
	
	private boolean isWifiConnected() {
	    ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = null;
	    if (connectivityManager != null) {
	        networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    }
	    return networkInfo == null ? false : networkInfo.isConnected();
	}
	
	
	@Override
	public void onStop() {
		networkObserver.sendBye();
		if ( networkObserver != null ) {
			networkObserver.stopObserver();
		}
		onDesactiveNetwork();
		//networkObserver = null;
	}

	@Override
	public boolean sendMessage(String jsonMessage, String destinationAddress) {
		networkObserver.sendData(jsonMessage, destinationAddress);
		return false;
	}

	@Override
	public boolean sendBroadcastMessage(String jsonMessage, String avoidAddress) {
		networkObserver.sendBroadcastData(jsonMessage);
		return true;
	}

	@Override
	public String getMyAddress() {
		return networkObserver.getWifiAddress();
	}

	@Override
	public List<String> getNeighborhood() {
		return neighborhood;
	}

	@Override
	public void onMesssageReceived(String deviceAddress, String message) {
		messageNotifier.onMessageReceived(message);
	}

	@Override
	public void onDeviceConnected(String deviceAddress) {
		if ( !neighborhood.contains(deviceAddress) ) {
			neighborhood.add(deviceAddress);
			notifier.onDeviceConnect(deviceAddress, deviceAddress);
		}
	}

	@Override
	public void onDeviceDisconnected(String deviceAddress) {
		neighborhood.remove(deviceAddress);
		notifier.onDeviceDisconnect(deviceAddress, deviceAddress);
	}

}
