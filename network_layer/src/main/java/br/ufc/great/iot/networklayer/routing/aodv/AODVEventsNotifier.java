package br.ufc.great.iot.networklayer.routing.aodv;

import br.ufc.great.iot.networklayer.routing.RoutingEventsNotifier;

public interface AODVEventsNotifier extends RoutingEventsNotifier {
	
	void notifyAboutDestinationUnreachable(String destinationAddress);
	void notifyAboutInvalidRouteTo(String destinationAddress);
	
}
