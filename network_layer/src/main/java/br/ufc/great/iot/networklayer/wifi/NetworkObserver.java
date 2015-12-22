package br.ufc.great.iot.networklayer.wifi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import br.ufc.great.iot.networklayer.wifi.message.HelloMessage;
import br.ufc.great.iot.networklayer.wifi.message.HelloReplyMessage;
import br.ufc.great.iot.networklayer.wifi.message.Message;
import br.ufc.great.iot.networklayer.wifi.message.WifiMessage;

public class NetworkObserver extends Thread {

	private static final int DELAY_TIME = 15000;
	private static final int DISCOVERY_PORT = 9000;
	private String TAG = WifiCommunicable.class.getCanonicalName();
	private DatagramSocket datagramSocket;
	private Context mContext;
	private WifiListener mListener;
	private String mAddress;
	private Thread observer;
	private boolean lookingForNeighboors = true;
	private Runnable runner = new Runnable() {

		@Override
		public void run() {
			while ( lookingForNeighboors && !isInterrupted()) {
				try {
					Thread.sleep(DELAY_TIME);
					sendDiscoveryRequest();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	};
	

	public synchronized String getWifiAddress() {
		if (mAddress == null) {
			mAddress = getLocalIp();
		}
		return mAddress;
	}

	public NetworkObserver(Context context, WifiListener listener) {
		mContext = context;
		mListener = listener;
		init();
	}

	/**
	 * 
	 */
	private void init() {
		try {
			datagramSocket = new DatagramSocket(DISCOVERY_PORT);
			datagramSocket.setReuseAddress(true);
			datagramSocket.setBroadcast(true);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public NetworkObserver() {
		init();
	}

	public void startObserver() {
		start();
		try {
			sendDiscoveryRequest();
			observer = new Thread(runner);
			observer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true && !isInterrupted() && !datagramSocket.isClosed()) {
			listenForObjects();
		}
	}

	/**
	 * 
	 */
	public void helloNetwork() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					sendDiscoveryRequest();
				} catch (IOException e) {
					Log.e(TAG, "Could not send discovery request", e);
				}
			}
		});
		t.start();
	}

	/**
	 * Send a broadcast UDP packet containing a request for boxee services to
	 * announce themselves.
	 * 
	 * @throws IOException
	 */
	private void sendDiscoveryRequest() throws IOException {
		final HelloMessage hello = new HelloMessage();
		(new Thread(new Runnable() {
			@Override
			public void run() {
				sendBroadcastObject(hello);
			}
		})).start();
	}

	/**
	 * @param address
	 * @param message
	 * @deprecated
	 */
	public void sendUDPUnicastMessage(final String address, final String message) {
		(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String numericAddress = address.substring(1);
					DatagramPacket packet = new DatagramPacket(
							message.getBytes(), message.length(),
							InetAddress.getByName(numericAddress),
							DISCOVERY_PORT);
					datagramSocket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		})).start();
	}
	
	/**
	 * @param message
	 * @param destinationAddress
	 */
	public void sendData(String jsonMessage, String destinationAddress){
		Message message = new Message();
		message.setContent(jsonMessage);
		sendUnicastObject(message);
	}

	private void sendUnicastObject(final WifiMessage message) {
		message.setDestinationIpAddress(getBroadcastAddress().toString());
		message.setSourceIpAddress(getLocalIp());
		(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					//String numericAddress = address.substring(1);
					ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
					ObjectOutputStream outputStream = new ObjectOutputStream(byteStream);

					outputStream.writeObject(message);
					final DatagramPacket packet = new DatagramPacket(byteStream.toByteArray(), byteStream.toByteArray().length,InetAddress.getByName(message.getDestinationIpAddress()), DISCOVERY_PORT);
					if ( datagramSocket != null ) {
						datagramSocket.send(packet);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		})).start();
	}
	/**
	 * @param string
	 */
	public void sendBroadcastData(String string) {
		Message message = new Message();
		message.setContent(string);
		sendBroadcastObject(message);
	}

	/**
	 * @param message
	 */
	private void sendBroadcastObject(final WifiMessage message) {
		String broadcastAddress = getBroadcastAddress().toString();
		message.setDestinationIpAddress(broadcastAddress.substring(1));
		message.setSourceIpAddress(getLocalIp());
		(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
					ObjectOutputStream outputStream = new ObjectOutputStream(byteStream);

					outputStream.writeObject(message);
					outputStream.flush();
					final DatagramPacket packet = new DatagramPacket(byteStream.toByteArray(), byteStream.toByteArray().length,getBroadcastAddress(), DISCOVERY_PORT);
					if ( datagramSocket != null ) {
						datagramSocket.send(packet);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		})).start();
	}

	/**
	 * 
	 */
	private void listenForObjects() {
		try {
			DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
			datagramSocket.receive(packet);
			String localIP = "/" + getLocalIp();
			if (!localIP.equalsIgnoreCase(packet.getAddress().toString())) {
				byte[] data = packet.getData();
				ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
				ObjectInputStream objectInput = new ObjectInputStream(inputStream);
				handleIncomingObject((WifiMessage)objectInput.readObject());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void handleIncomingObject(WifiMessage readObject) {
		switch (readObject.getType()) {
		case HELLO:
			sendBroadcastObject(new HelloReplyMessage());
			break;
		case HELLO_REPLY:
			mListener.onDeviceConnected(readObject.getSourceIpAddress());
			break;
		case MSG:
			Message msg = (Message) readObject;
			mListener.onMesssageReceived(msg.getSourceIpAddress(),
					msg.getContent());
			break;
		case BYE:
			mListener.onDeviceDisconnected(readObject.getSourceIpAddress());
			break;
		default:
			break;
		}
	}

	/**
	 * @return
	 */
	private String getLocalIp() {
		WifiManager wifi = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifi.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		String address = String.format("%d.%d.%d.%d", (ipAddress & 0xff),
				(ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
				(ipAddress >> 24 & 0xff));
		return address;

	}

	/**
	 * @return
	 * @throws IOException
	 */
	private InetAddress getBroadcastAddress() {
		WifiManager wifi = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcp = wifi.getDhcpInfo();

		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++)
			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
		try {
			return InetAddress.getByAddress(quads);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 */
	public void stopObserver() {
		this.interrupt();
		mListener = null;
		if (datagramSocket != null) {
			datagramSocket.disconnect();
			datagramSocket.close();
			datagramSocket = null;
		}
		if ( observer != null) {
			observer.interrupt();
		}
	}

	/**
	 * 
	 */
	public void sendBye() {
		ByeMessage bye = new ByeMessage();
		sendBroadcastObject(bye);
		sendBroadcastObject(bye);
		sendBroadcastObject(bye);
	}
}
