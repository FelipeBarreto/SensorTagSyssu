package br.ufc.great.iot.networklayer.base;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import br.ufc.great.iot.networklayer.routing.message.UserDataMessage;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class NetworkEventHandler extends Handler {
	
	static final int MESSAGE_RECEIVED = 1;
	static final int DEVICE_CONNECTED = 2;
	static final int DEVICE_DISCONNECTED = 3;
	static final int DEVICE_FOUND = 4;
	static final int EXCEPTION_OCURRED = 5;
	static final int CONNECTION_STATE_CHANGED = 6;
	static final int NOT_NEIGHBOR_FOUND = 7;
	
	static final String DEVICE_ADDRESS = "device_address";
	static final String DEVICE_NAME = "device_name";
	static final String ERROR_MESSAGE = "error_message";
	static final String MESSAGE = "message";
	
	private List<NetworkEventListener> messageListeners;
	
	public NetworkEventHandler() {
		messageListeners = new ArrayList<NetworkEventListener>();
	}
	
	@Override
	public void handleMessage(Message msg) {
		Bundle data = msg.getData();
		synchronized(messageListeners)
		{
			switch (msg.what) {
			
				case MESSAGE_RECEIVED:
					for (NetworkEventListener c : messageListeners) {
						c.onReceiveMessage(getDeviceAddress(data), getUserData(data));
					}
				break;
		
				case DEVICE_CONNECTED:
					for (NetworkEventListener c : messageListeners) {
						c.onDeviceConnected(getDeviceName(data), getDeviceAddress(data));
					}
				break;
					
				case DEVICE_FOUND:
					for (NetworkEventListener c : messageListeners) {
						c.onDeviceFound(getDeviceName(data), getDeviceAddress(data));
					}
				break;	
					
				case DEVICE_DISCONNECTED:
					for (NetworkEventListener c : messageListeners) {
						c.onDeviceDisconnected(getDeviceAddress(data));
					}
				break;
					
				case EXCEPTION_OCURRED:
					for (NetworkEventListener c : messageListeners) {
						c.onExceptionOccurred(msg.obj.toString());
					}
				break;
					
				case CONNECTION_STATE_CHANGED:
					for (NetworkEventListener c : messageListeners) {
						c.onStateConnectionChanged(msg.arg1, msg.arg2);
					}
				break;
					
				case NOT_NEIGHBOR_FOUND:
					for (NetworkEventListener c : messageListeners) {				
						c.onNotNeighborFound(msg.obj.toString());			
				}
				break;
				
				default:
				break;
			}
		}
	}

	private JSONObject getUserData(Bundle data) {
		return((UserDataMessage) data.getSerializable(MESSAGE)).getContent();
	}

	private String getDeviceName(Bundle data) {
		return data.getString(DEVICE_NAME);
	}

	private String getDeviceAddress(Bundle data) {
		return data.getString(DEVICE_ADDRESS);
	}
	
	public void subscribe(NetworkEventListener listener) {
		synchronized(messageListeners)
		{
			messageListeners.add(listener);
		}
	}
	
	public void unsubscribe(NetworkEventListener listener) {
		synchronized(messageListeners)
		{
			messageListeners.remove(listener);
		}
	}

	public void unsubscribeAll() {
		synchronized(messageListeners)
		{
			messageListeners.clear();
		}
	}

}
