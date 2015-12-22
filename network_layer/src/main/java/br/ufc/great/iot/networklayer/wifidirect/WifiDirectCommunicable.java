package br.ufc.great.iot.networklayer.wifidirect;

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import br.ufc.great.iot.networklayer.base.Communicable;
import br.ufc.great.iot.networklayer.base.CommunicableEventListener;
import br.ufc.great.iot.networklayer.base.MessageEventListener;
import br.ufc.great.somc.network.base.WifiDirectApi;
import br.ufc.great.somc.network.base.WifiDirectListener;

public class WifiDirectCommunicable extends Communicable {

	private static final String WIFI_DIRECT_SERVICE = "br.ufc.great.somc.network.wifidirectservice.WifiDirectNetworkManagerService";
	private WifiDirectApi api;
	private WifiServiceConnection wifiServiceConnection;
	private static final String TAG = WifiDirectCommunicable.class.getName();
	private int connections;
	private Object lock = new Object();

	class WifiServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			api = WifiDirectApi.Stub.asInterface(service);
			try {
				api.addListener(wifiListener);
			} catch (RemoteException e) {
				e.printStackTrace();
			}


			try {
				api.startDiscovery();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			Log.d(TAG, "onServiceConnected");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "onServiceDisconnected");
			try {
				api.removeListener(wifiListener);
				onDesactiveNetwork();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			api = null;
		}
	}

	private WifiDirectListener wifiListener = new WifiDirectListener.Stub() {

		@Override
		public void onWifiStateChanged(int state) throws RemoteException {
			Log.d(TAG, "onWifiStateChanged");
			if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
				WifiDirectCommunicable.this.onActiveNetwork();
			} else {
				WifiDirectCommunicable.this.onDesactiveNetwork();
			}
		}

		@Override
		public void onStateConnectionChanged(int state, int previousState)
				throws RemoteException {
			Log.d(TAG, "onStateConnectionChanged");
		}

		@Override
		public void onMessageReceived(String message) throws RemoteException {
			Log.d(TAG, "onMessageReceived Called: " + message);
			WifiDirectCommunicable.this.onMessageReceived(message);
		}

		@Override
		public void onExceptionOccurred(int code, String message)
				throws RemoteException {
			Log.d(TAG, "onExceptionOcurred Called");
			WifiDirectCommunicable.this.onExceptionOcurred(code, message);
		}

		@Override
		public void onDeviceFound(String name, String address)
				throws RemoteException {
			Log.d(TAG, "onDeviceFound Called");
			WifiDirectCommunicable.this.onDeviceFound(name, address);
		}

		@Override
		public void onDeviceDisconnect(String name, String address)
				throws RemoteException {
			Log.d(TAG, "onDeviceDisconnect Called");
			decrementConnections();
			WifiDirectCommunicable.this.onDeviceDisconnect(name, address);
		}

		@Override
		public void onDeviceConnect(String name, final String address)
				throws RemoteException {
			onActiveNetwork();
			incrementConnections();
			Log.d(TAG, "onDeviceConnect Called");
			WifiDirectCommunicable.this.onDeviceConnect(name, address);

		}

		@Override
		public void onConnectionInfo(String groupInfo) throws RemoteException {
			Log.d(TAG, "onConnectionInfo Called");
		}

	};

	/**
	 * 
	 */
	private void decrementConnections() {
		synchronized (lock) {
			if (connections > 0) {
				connections--;
			}
			if (connections == 0) {
				onDesactiveNetwork();
			}
		}
	}

	/**
	 * 
	 */
	private void incrementConnections() {
		synchronized (lock) {
			connections++;
		}
	}

	public WifiDirectCommunicable(Context context,
			CommunicableEventListener notifier,
			MessageEventListener messageNotifier) {
		super(context, notifier, messageNotifier);
		synchronized (lock) {
			connections = 0;
		}
		wifiServiceConnection = new WifiServiceConnection();
	}

	@Override
	public void onStart() {
		Intent intent = new Intent(WIFI_DIRECT_SERVICE);
		intent.setClassName("br.ufc.great.somc.network.wifidirectservice",
				WIFI_DIRECT_SERVICE);
		context.bindService(intent, wifiServiceConnection,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onStop() {
		try {
			if (api != null)
				api.removeListener(wifiListener);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		context.unbindService(wifiServiceConnection);
	}

	@Override
	public boolean sendMessage(String jsonMessage, String destinationAddress) {
		try {
			if (api != null)
				api.sendUnicastMessage(jsonMessage, destinationAddress);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean sendBroadcastMessage(String jsonMessage, String avoidAddress) {
		try {
			if (api != null)
				api.sendBroadcastMessage(jsonMessage, avoidAddress);
			return true;
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public String getMyAddress() {
		try {
			if (api != null)
				return api.getMyAddress();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<String> getNeighborhood() {
		try {
			if (api != null)
				return api.getNeighborhood();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void connect(String address) throws RemoteException {
		if (api != null)
			api.manualConnect(address);
	}

	public void removeGroup() throws RemoteException {
		if (api != null)
			api.removeGroup();
	}

	public void startDiscovery() throws RemoteException {
		if (api != null)
			api.startDiscovery();
	}

	public void createGroup() throws RemoteException {
		if (api != null)
			api.createGroup();
	}

	public void requestGroupInfo() throws RemoteException {
		if (api != null)
			api.requestGroupIno();
	}
}
