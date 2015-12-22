package br.ufc.great.iot.networklayer.bluetooth;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class BluetoothEventHandler extends Handler {

	static final int STATE_SCAN_MODE = 11;
	static final int STATE_OBSERVING_CHANGE = 12;
	static final int STATE_DISCOVERY_CHANGE = 13;
	static final int STATE_CHANGE = 14;
	static final int NOT_NEIGHBOR_FOUND = 15;
	static final String DEVICE_ADDRESS = "device_address";
	static final String DEVICE_NAME = "device_name";

	protected List<BluetoothEventsListener> optionalNotifiers;

	public BluetoothEventHandler() {
		optionalNotifiers = new ArrayList<BluetoothEventsListener>();
	}

	/**
	 * Use to subscribe for network optional events
	 * 
	 * @param optionalNotifier
	 */
	public void subscribeOptionalNotifier(
			BluetoothEventsListener optionalNotifier) {
		synchronized (optionalNotifiers) {
			this.optionalNotifiers.add(optionalNotifier);
		}
	}

	/**
	 * Use to subscribe for network optional events
	 * 
	 * @param optionalNotifier
	 */
	public void unsubscribeOptionalNotifier(
			BluetoothEventsListener optionalNotifier) {
		synchronized (optionalNotifiers) {
			this.optionalNotifiers.remove(optionalNotifier);
		}
	}

	public void unsubscribeAll() {
		optionalNotifiers.clear();
	}

	@Override
	public void handleMessage(Message msg) {
		synchronized (optionalNotifiers) {
			switch (msg.what) {
			case STATE_SCAN_MODE:
				for (BluetoothEventsListener optionalNotifier : optionalNotifiers) {
					optionalNotifier.onStateScanModeChanged(msg.arg1, msg.arg2);
				}
				break;

			case STATE_OBSERVING_CHANGE:
				for (BluetoothEventsListener optionalNotifier : optionalNotifiers) {
					optionalNotifier
							.onStateObservingChanged(msg.arg1, msg.arg2);
				}
				break;

			case STATE_DISCOVERY_CHANGE:
				for (BluetoothEventsListener optionalNotifier : optionalNotifiers) {
					optionalNotifier
							.onStateDiscoveryChanged(msg.obj.toString());
				}
				break;

			case STATE_CHANGE:
				for (BluetoothEventsListener optionalNotifier : optionalNotifiers) {
					optionalNotifier.onStateChange(msg.arg1, msg.arg2);
				}
				break;

			case NOT_NEIGHBOR_FOUND:
				for (BluetoothEventsListener optionalNotifier : optionalNotifiers) {
					Bundle data = msg.getData();
					optionalNotifier.onNotNeighborDeviceFound(
							getDeviceName(data), getDeviceAddress(data));
				}
				break;

			default:
				break;
			}
		}
	}

	private String getDeviceName(Bundle data) {
		return data.getString(DEVICE_NAME);
	}

	private String getDeviceAddress(Bundle data) {
		return data.getString(DEVICE_ADDRESS);
	}

}
