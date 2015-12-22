package br.ufc.great.iot.networklayer.wifi.message;


public class HelloReplyMessage extends WifiMessage {

	private static final long serialVersionUID = -7770835424949339011L;

	@Override
	public WifiTypeMessage getType() {
		return WifiTypeMessage.HELLO_REPLY;
	}

}
