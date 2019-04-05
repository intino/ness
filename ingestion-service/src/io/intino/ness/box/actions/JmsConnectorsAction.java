package io.intino.ness.box.actions;

import io.intino.ness.box.NessServiceBox;
import io.intino.ness.graph.JMSConnector;
import org.apache.activemq.network.jms.JmsConnector;

import java.util.List;
import java.util.stream.Collectors;


public class JmsConnectorsAction {

	public NessServiceBox box;

	public List<String> execute() {
		return box.graph().jMSConnectorList().stream().map(bp -> bp.name$() + ": " + (isRunning(bp) ? "Running" : "Stopped")).collect(Collectors.toList());
	}

	private boolean isRunning(JMSConnector JMSConnector) {
		JmsConnector connector = box.busService().jmsConnectors().stream().filter(j -> j.getName().equals(JMSConnector.name$())).findFirst().orElse(null);
		return connector != null && connector.isConnected();
	}
}