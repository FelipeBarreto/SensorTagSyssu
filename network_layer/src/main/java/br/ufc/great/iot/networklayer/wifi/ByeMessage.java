package br.ufc.great.iot.networklayer.wifi;

import br.ufc.great.iot.networklayer.wifi.message.WifiMessage;

public class ByeMessage extends WifiMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8246747850602781854L;

	@Override
	public WifiTypeMessage getType() {
		return WifiTypeMessage.BYE;
	}

}
