package br.ufc.great.iot.networklayer.base;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import br.ufc.great.iot.networklayer.bluetooth.BluetoothCommunicable;
import br.ufc.great.iot.networklayer.bluetooth.BluetoothEventsListener;
import br.ufc.great.iot.networklayer.routing.IRouter;
import br.ufc.great.iot.networklayer.routing.RoutingEventsNotifier;
import br.ufc.great.iot.networklayer.routing.aodv.AODVEventsNotifier;
import br.ufc.great.iot.networklayer.routing.exception.RoutingException;
import br.ufc.great.iot.networklayer.routing.flooding.Flooding;
import br.ufc.great.iot.networklayer.routing.message.HelloMessage;
import br.ufc.great.iot.networklayer.routing.message.UserDataMessage;
import br.ufc.great.iot.networklayer.wifi.WifiCommunicable;
import br.ufc.great.iot.networklayer.wifidirect.WifiDirectCommunicable;
import br.ufc.great.somc.networklayer.BuildConfig;

public class NetworkManager{


	/**
	 * The device is idle, preparing to start do listen for connection.
	 */
	public static final int STATE_NONE = 0; 	
	/**
	 * The device is listing for connection.
	 */
	private static final int STATE_LISTEN = 1;
	/**
	 * The device is iniating a outgoing connection.
	 */
	private static final int STATE_CONNECTING = 2;	
	/**
	 * The device is now connect to a remote device
	 */
	public static final int STATE_CONNECTED = 3; 
	
	public enum ConnectionEvent{ DEVICE_CONNECTED, DEVICE_DISCONNECTED};
	
	private IRouter router;

	private String TAG = "NetworkManager";
	private Context mContext;

	private NetworkContainer netwoksAvaliable;
	
	private BluetoothCommunicable bluetoothApi;
	private WifiDirectCommunicable wifiDirectApi;
	private NetworkEventHandler networkEventHandler;
	private List<String> myAddresses;
	
	private int state;
	private WifiCommunicable wifiApi;
	
	public static boolean SIMULATION = false;
	
	private static NetworkManager networkManager = null;
	
	private int connectedDevices = 0;
	
	public static NetworkManager getInstance()
	{
		if(networkManager == null)
		{
			networkManager = new NetworkManager();
		}
		return networkManager;
	}

	public void init(Context context)
	{
		state = STATE_NONE;
		mContext = context;
		netwoksAvaliable.setApplicationID(context.getPackageName());
		//router = new AODV(context.getPackageName(), (AODVEventsNotifier) routingNotifier, true);
		router = new Flooding(context.getPackageName(), routingNotifier);
		//addBluetoothNetwork();
		//addWifiDirectNetwork();
		addWifiNetwork();
	}
	
	private NetworkManager() {
		
		networkEventHandler = new NetworkEventHandler();
		netwoksAvaliable = NetworkContainer.getInstance();		
	}
	
	private <T> void addBluetoothNetwork() 
	{		
		try {
			bluetoothApi = (BluetoothCommunicable) netwoksAvaliable.addNewNetwork(BluetoothCommunicable.class,
					mContext, notifier, router.getMessageNotifier());
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}	
	
	private <T> void addWifiNetwork() {
		try {
			wifiApi = (WifiCommunicable) netwoksAvaliable.addNewNetwork(WifiCommunicable.class, mContext, notifier, router.getMessageNotifier());
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	
	private <T> void addWifiDirectNetwork() {
		try {
			wifiDirectApi = (WifiDirectCommunicable) netwoksAvaliable.addNewNetwork(WifiDirectCommunicable.class, mContext, notifier, router.getMessageNotifier());
			
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public void subscribeBluetoothOptionalEvents(BluetoothEventsListener optionalNotifier)
	{
		bluetoothApi.subscribeOptionalNotifier(optionalNotifier);		
	}
	
	public void unsubscribeBluetoothOptionalEvents(BluetoothEventsListener optionalNotifier)
	{
		bluetoothApi.unsubscribeOptionalNotifier(optionalNotifier);	
	}

	public void onStart() {
		if(BuildConfig.DEBUG)
		{
			Log.d(TAG, "onStart");
		}			
	}

	/**
	 * @param listener
	 */
	public void subscribe(NetworkEventListener listener) {
		networkEventHandler.subscribe(listener);
	}
	
	public void unsubscribe(NetworkEventListener listener) {
		networkEventHandler.unsubscribe(listener);
	}

	/**
	 * @param msg
	 * @param destination
	 */
	public void sendMessage(JSONObject msg, Set<String> destinations) {		
		if (destinations.size() == 1) {
			String destinationArray[] = new String[destinations.size()];
			destinationArray = destinations.toArray(destinationArray);
			sendMessage(msg, destinationArray[0]);			
		} else {			
			for (String destination : destinations) {
				sendMessage(msg, destination);
			}	
		}
	}
	
	public void sendBroadcastMessage(JSONObject msg) 
	{
		sendMessage(msg, IRouter.BROADCAST_EXCLUDING_SOURCE);
	}

	/**
	 * @param msg
	 * @param destination
	 */
	public void sendMessage(JSONObject msg, String destination)	{		
		try {
			router.routeMessage(destination, msg);
		} catch (RoutingException e) {
			e.printStackTrace();
		}	
		
	}
	
	private void notifyNeighborhood() {
		router.notifyNeighborhood();
	}
	
	public void onCreate() {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "onCreate");	
		}
	}

	public void onResume() {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "onResume");
		}		
		myAddresses = new ArrayList<String>();
		netwoksAvaliable.activeNetworks();		
		router.start();
	}

	public void onDestroy() {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "onDestroy");
		}
	}
	
	public void onStop() {
		networkEventHandler.unsubscribeAll();
		if ( bluetoothApi != null ) {
			bluetoothApi.unsubscribeAll();
		}
		if (wifiDirectApi != null){
			wifiDirectApi.onStop();
		}
		netwoksAvaliable.disableNetworks();
		router.stop();
	}

	public void connect(String address) throws RemoteException {
		bluetoothApi.connect(address);
	}
	
	public void connectWifi(String address) throws RemoteException{
		wifiDirectApi.connect(address);
	}
	
	public void startWifiDiscovery() throws RemoteException {
		wifiDirectApi.startDiscovery();
	}
	
	public void removeWifiGroup() throws RemoteException {
		wifiDirectApi.removeGroup();
	}
	
	public boolean ensureDiscoverable() throws RemoteException {
		return bluetoothApi.ensureDiscoverable();
	}

	public void startDiscovery() throws RemoteException {
		bluetoothApi.startDiscovery();
	}

	public BluetoothDevice getRemoteDevice(String address)
			throws RemoteException {
		return bluetoothApi.getRemoteDevice(address);
	}

	public void startObserver() throws RemoteException {
		bluetoothApi.startObserver();
	}

	public void stopObserver() throws RemoteException {
		bluetoothApi.stopObserver();
	}

	public int getCurrentState() throws RemoteException {
		return state;
	}

	public void onPause() {
	}

	
	private void notifyAboutNeighbor(String destinationAddress) {
		Message m = networkEventHandler.obtainMessage(
				NetworkEventHandler.NOT_NEIGHBOR_FOUND,
				destinationAddress);
		networkEventHandler.sendMessage(m);
	}


	private synchronized void updateConnectionState(ConnectionEvent event) {
		int previousState = state;
		if(event == ConnectionEvent.DEVICE_CONNECTED)
		{
			state = STATE_CONNECTED;
			connectedDevices++;
		} else {
			connectedDevices--;
			if(connectedDevices == 0)
			{
				state = STATE_NONE;
			}
		}
		
		Message msg = networkEventHandler.obtainMessage(
				NetworkEventHandler.CONNECTION_STATE_CHANGED,
				state, previousState);
		
		networkEventHandler.sendMessage(msg);
		
		if(state == STATE_CONNECTED)
		{
			notifyNeighborhood();
		}
	}
	

	private final CommunicableEventListener notifier = new CommunicableEventListener() {
		
		// Network Events Notifier Methods

		@Override
		public void onDeviceFound(String name, String address) {
			Message m = networkEventHandler.obtainMessage(
					NetworkEventHandler.DEVICE_FOUND);
			Bundle bundle = new Bundle();
			bundle.putString(NetworkEventHandler.DEVICE_NAME, name);
			bundle.putString(NetworkEventHandler.DEVICE_ADDRESS, address);
			m.setData(bundle);
			
			networkEventHandler.sendMessage(m);
		}

		@Override
		public void onDeviceConnect(String name, String address) {
			
			Message m = networkEventHandler.obtainMessage(
					NetworkEventHandler.DEVICE_CONNECTED);
			Bundle bundle = new Bundle();
			bundle.putString(NetworkEventHandler.DEVICE_NAME, name);
			bundle.putString(NetworkEventHandler.DEVICE_ADDRESS, address);
			m.setData(bundle);

			updateConnectionState(ConnectionEvent.DEVICE_CONNECTED);
			networkEventHandler.sendMessage(m);
			
		}

		@Override
		public void onDeviceDisconnect(String name, String address) {
			Message m = networkEventHandler.obtainMessage(
					NetworkEventHandler.DEVICE_DISCONNECTED);
			Bundle bundle = new Bundle();
			bundle.putString(NetworkEventHandler.DEVICE_ADDRESS, address);
			m.setData(bundle);
			
			updateConnectionState(ConnectionEvent.DEVICE_DISCONNECTED);
			networkEventHandler.sendMessage(m);
		}

		@Override
		public void onExceptionOcurred(int code, String message) {
			switch(code)			
			{
				case -1:
					message = "Destination Unreachable " + message;
				break;
				
				case -2:
					message = "Invalid " + message;
				break;
				
			}
			
			Message m = networkEventHandler.obtainMessage(
					NetworkEventHandler.EXCEPTION_OCURRED,
					message);
			networkEventHandler.sendMessage(m);
			
		}	

		@Override
		public void onActiveNetwork(String myAddress) {
			myAddresses.add(myAddress);
			router.setMyAddresses(myAddresses);
		}
		
		@Override
		public void onDesactiveNetwork(String myAddress) {
			myAddresses.remove(myAddress);
			router.setMyAddresses(myAddresses);
		}

		
		@Override
		public void updateNeighboorList(String name, String address,
				String networkID) {
		}

		
	};
	
	private final RoutingEventsNotifier routingNotifier = new AODVEventsNotifier() {
		
		// Routing Events Notifier Methods 

		@Override
		public void onUserDataReceived(UserDataMessage message) {
			Message m = networkEventHandler.obtainMessage(
					NetworkEventHandler.MESSAGE_RECEIVED);
			Bundle bundle = new Bundle();
			bundle.putSerializable(NetworkEventHandler.MESSAGE, message);
			bundle.putString(NetworkEventHandler.DEVICE_ADDRESS, message.getSourceAddress());
			m.setData(bundle);
			networkEventHandler.sendMessage(m);		
			
		}

		@Override
		public void onHelloMessageReceived(HelloMessage hello) {
			
			onNewNodeReachable(hello.getSourceAddress());
			List<String> devices = new ArrayList<String>( hello.getPath());		
				for (String device : devices) {
					onNewNodeReachable(device);			
				}
			
		}

		@Override
		public void onNewNodeReachable(String destinationAddress) {
			if(!myAddresses.contains(destinationAddress))
			{
				notifyAboutNeighbor(destinationAddress);
			}
		}

		@Override
		public void onSentMessage() {
		}

		@Override
		public void onExceptionOcurred(int code, String message) {
			notifier.onExceptionOcurred(code, message);
		}

		@Override
		public void notifyAboutDestinationUnreachable(String destinationAddress) {
			notifier.onExceptionOcurred(-1, destinationAddress);
		}

		@Override
		public void notifyAboutInvalidRouteTo(String destinationAddress) {
			notifier.onExceptionOcurred(-2, destinationAddress);
		}

		
	};

}
