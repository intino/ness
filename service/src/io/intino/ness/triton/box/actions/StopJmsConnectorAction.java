package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.ServiceBox;
import io.intino.ness.triton.graph.JMSConnector;
import org.apache.activemq.network.jms.JmsConnector;

public class StopJmsConnectorAction {

	public ServiceBox box;
	public String name;

	public String execute() {
		JMSConnector JMSConnector = box.graph().jMSConnectorList(f -> f.name$().equals(name)).findFirst().orElse(null);
		if (JMSConnector == null) return "JMS connector not found";
		JmsConnector connector = box.busService().jmsConnectors().stream().filter(j -> j.getName().equals(JMSConnector.name$())).findFirst().orElse(null);
		if (connector != null && connector.isConnected()) {
			try {
				connector.stop();
				JMSConnector.enabled(false);
				JMSConnector.save$();
			} catch (Exception e) {
			}
		}
		return Action.OK;
	}
}