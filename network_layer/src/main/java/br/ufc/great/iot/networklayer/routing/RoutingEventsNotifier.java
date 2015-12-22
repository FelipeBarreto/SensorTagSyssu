package br.ufc.great.iot.networklayer.routing;

import br.ufc.great.iot.networklayer.routing.message.HelloMessage;
import br.ufc.great.iot.networklayer.routing.message.UserDataMessage;

public interface RoutingEventsNotifier {

	void onUserDataReceived(UserDataMessage message);
	void onHelloMessageReceived(HelloMessage hello);
	void onNewNodeReachable(String destinationAddress);
	void onSentMessage();
	void onExceptionOcurred(int code, String message);
}
