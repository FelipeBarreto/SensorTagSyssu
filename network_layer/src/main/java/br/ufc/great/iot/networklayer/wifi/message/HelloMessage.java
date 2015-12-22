package br.ufc.great.iot.networklayer.wifi.message;

public class HelloMessage extends WifiMessage {

	private static final long serialVersionUID = -4570126701829718284L;

	@Override
	public WifiTypeMessage getType() {
		return WifiTypeMessage.HELLO;
	}

}
