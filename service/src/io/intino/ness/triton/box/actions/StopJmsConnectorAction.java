package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.ServiceBox;
import io.intino.ness.triton.graph.JmsService;
import org.apache.activemq.network.jms.JmsConnector;

public class StopJmsConnectorAction {

	public ServiceBox box;
	public String name;

	public String execute() {
		if (box.graph().jmsService() == null) return "Jms Service inactive";
		JmsService.JmsConnector JmsConnector = box.graph().jmsService().jmsConnectorList().stream().filter(f -> f.name$().equals(name)).findFirst().orElse(null);
		if (JmsConnector == null) return "Jms connector not found";
		JmsConnector connector = box.busService().jmsConnectors().stream().filter(j -> j.getName().equals(JmsConnector.name$())).findFirst().orElse(null);
		if (connector != null && connector.isConnected()) {
			try {
				connector.stop();
				JmsConnector.enabled(false);
				JmsConnector.save$();
			} catch (Exception e) {
			}
		}
		return Action.OK;
	}
}