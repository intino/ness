package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.JMSConnector;
import org.apache.activemq.network.jms.JmsConnector;

import static io.intino.ness.box.actions.Action.OK;

public class StopJmsConnectorAction {

	public NessBox box;
	public String name;

	public String execute() {
		JMSConnector JMSConnector = box.graph().jMSConnectorList(f -> f.name$().equals(name)).findFirst().orElse(null);
		if (JMSConnector == null) return "Bus pipe not found";
		JmsConnector connector = box.busService().jmsConnectors().stream().filter(j -> j.getName().equals(JMSConnector.name$())).findFirst().orElse(null);
		if (connector != null && !connector.isConnected()) {
			try {
				connector.stop();
			} catch (Exception e) {
			}
		}
		return OK;
	}
}