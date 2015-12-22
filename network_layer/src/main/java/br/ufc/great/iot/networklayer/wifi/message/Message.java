package br.ufc.great.iot.networklayer.wifi.message;

public class Message extends WifiMessage {

	private static final long serialVersionUID = -4456265461837447183L;

	private String content;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public WifiTypeMessage getType() {
		return WifiTypeMessage.MSG;
	}

}
