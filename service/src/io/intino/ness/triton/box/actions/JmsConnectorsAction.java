package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.ServiceBox;
import io.intino.ness.triton.graph.JmsService;
import org.apache.activemq.network.jms.JmsConnector;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class JmsConnectorsAction {

	public ServiceBox box;

	public List<String> execute() {
		if (box.graph().jmsService() == null) return Collections.emptyList();
		return box.graph().jmsService().jmsConnectorList().stream().map(bp -> bp.name$() + ": " + (isRunning(bp) ? "Running" : "Stopped")).collect(Collectors.toList());
	}

	private boolean isRunning(JmsService.JmsConnector JmsConnector) {
		JmsConnector connector = box.busService().jmsConnectors().stream().filter(j -> j.getName().equals(JmsConnector.name$())).findFirst().orElse(null);
		return connector != null && connector.isConnected();
	}
}