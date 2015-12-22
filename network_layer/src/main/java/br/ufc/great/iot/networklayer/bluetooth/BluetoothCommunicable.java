package br.ufc.great.iot.networklayer.bluetooth;

import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import br.ufc.great.iot.networklayer.base.Communicable;
import br.ufc.great.iot.networklayer.base.CommunicableEventListener;
import br.ufc.great.iot.networklayer.base.MessageEventListener;
import br.ufc.great.somc.network.base.BluetoothListener;
import br.ufc.great.somc.network.base.BluetoothServiceApi;
import br.ufc.great.somc.networklayer.BuildConfig;

public class BluetoothCommunicable extends Communicable {

	private static final String BLUETOOTH_SERVICE_ADDRESS = "br.ufc.great.somc.network.bluetoothservice.BluetoothNetworkManagerService";
	private BluetoothServiceApi api;
	private SemcServiceConnection serviceConnection;
	private String TAG = BluetoothCommunicable.this.getClass()
			.getCanonicalName();
	private BluetoothEventHandler handler;

	public BluetoothCommunicable(Context context,
			CommunicableEventListener notifier,
			MessageEventListener messageNotifier) {
		super(context, notifier, messageNotifier);
		handler = new BluetoothEventHandler();
		serviceConnection = new SemcServiceConnection();
	}

	/**
	 * Use to subscribe for network optional events
	 * 
	 * @param optionalNotifier
	 */
	public void subscribeOptionalNotifier(
			BluetoothEventsListener optionalNotifier) {
		handler.subscribeOptionalNotifier(optionalNotifier);
	}

	/**
	 * Use to subscribe for network optional events
	 * 
	 * @param optionalNotifier
	 */
	public void unsubscribeOptionalNotifier(
			BluetoothEventsListener optionalNotifier) {
		handler.unsubscribeOptionalNotifier(optionalNotifier);
	}

	public void unsubscribeAll() {
		handler.unsubscribeAll();
	}

	class SemcServiceConnection implements ServiceConnection {
		BluetoothListener listener = new BluetoothListener.Stub() {

			@Override
			public void onStateScanModeChanged(int state, int previousState)
					throws RemoteException {
				Message msg = handler.obtainMessage(
						BluetoothAdapter.SCAN_MODE_CONNECTABLE, state,
						previousState);
				handler.sendMessage(msg);
			}

			@Override
			public void onStateObservingChanged(int state, int previousState)
					throws RemoteException {
				Message m = handler.obtainMessage(
						BluetoothEventHandler.STATE_OBSERVING_CHANGE, state,
						previousState);
				handler.sendMessage(m);
			}

			@Override
			public void onStateDiscoveryChanged(String action)
					throws RemoteException {
				Message m = handler.obtainMessage(
						BluetoothEventHandler.STATE_DISCOVERY_CHANGE, action);
				handler.sendMessage(m);
			}

			@Override
			public void onStateChanged(int state, int previousState)
					throws RemoteException {
				Message m = handler.obtainMessage(
						BluetoothEventHandler.STATE_CHANGE, state,
						previousState);
				handler.sendMessage(m);
			}

			@Override
			public void onNotNeighborDeviceFound(String deviceName,
					String deviceAddress) throws RemoteException {
				Message m = handler
						.obtainMessage(BluetoothEventHandler.NOT_NEIGHBOR_FOUND);
				Bundle bundle = new Bundle();
				bundle.putString(BluetoothEventHandler.DEVICE_NAME, deviceName);
				bundle.putString(BluetoothEventHandler.DEVICE_ADDRESS,
						deviceAddress);
				m.setData(bundle);
				handler.sendMessage(m);
			}

			@Override
			public void onDeviceFound(String name, String address) {
				BluetoothCommunicable.this.onDeviceFound(name, address);
			}

			@Override
			public void onDeviceConnect(String name, String address) {
				BluetoothCommunicable.this.onDeviceConnect(name, address);
			}

			@Override
			public void onDeviceDisconnect(String name, String address) {
				BluetoothCommunicable.this.onDeviceDisconnect(name, address);
			}

			@Override
			public void onMessageReceived(String jsonMessage) {
				BluetoothCommunicable.this.onMessageReceived(jsonMessage);
			}

			@Override
			public void onExceptionOcurred(int code, String message) {
				BluetoothCommunicable.this.onExceptionOcurred(code, message);
			}

			@Override
			public void onMessageSent(String jsonMessage)
					throws RemoteException {
				// BluetoothCommunicable.this.onMessageSent(message);
			}

		};

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			api = BluetoothServiceApi.Stub.asInterface(service);
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "ServiceConnected");
			}
			try {
				api.addListener(listener);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			onActiveNetwork();

			try {
				api.ensureDiscoverable();
				api.startDiscovery();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "ServiceDisconnected");
			}
			try {
				api.removeListener(listener);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			onDesactiveNetwork();
		}
	}

	@Override
	public boolean sendMessage(String jsonMessage, String destinationAddress) {
		try {
			api.sendUnicastMessage(jsonMessage, destinationAddress);
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean sendBroadcastMessage(String jsonMessage, String avoidAddress) {
		try {
			api.sendBroadcastMessage(jsonMessage, avoidAddress);
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}

	@Override
	public List<String> getNeighborhood() {
		try {
			return api.getNeighboord();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean ensureDiscoverable() throws RemoteException {
		return api.ensureDiscoverable();
	}

	public void startDiscovery() throws RemoteException {
		api.startDiscovery();
	}

	public BluetoothDevice getRemoteDevice(String address)
			throws RemoteException {
		return api.getRemoteDevice(address);
	}

	public void startObserver() throws RemoteException {
		api.startObserver();
	}

	public void stopObserver() throws RemoteException {
		api.stopObserver();
	}

	public int getCurrentState() throws RemoteException {
		return api.getCurrentState();
	}

	@Override
	public void onStart() {
		Intent intent = new Intent(BLUETOOTH_SERVICE_ADDRESS);
		intent.setClassName("br.ufc.great.somc.network.bluetoothservice", BLUETOOTH_SERVICE_ADDRESS);
		context.startService(intent);
		context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onStop() {
		try {
			api.removeListener(serviceConnection.listener);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		context.unbindService(serviceConnection);
		onDesactiveNetwork();
	}

	public void connect(String address) throws RemoteException {
		api.manualConnect(address);
	}

	@Override
	public String getMyAddress() {
		try {
			return api.getMyAddress();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

}
