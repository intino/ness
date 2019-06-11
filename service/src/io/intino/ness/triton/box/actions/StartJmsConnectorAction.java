package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.ServiceBox;
import io.intino.ness.triton.graph.JmsService;
import org.apache.activemq.network.jms.JmsConnector;


public class StartJmsConnectorAction {

	public ServiceBox box;
	public String name;

	public String execute() {
		if (box.graph().jmsService() == null) return "Jms Service inactive";
		JmsService.JmsConnector JmsConnector = box.graph().jmsService().jmsConnectorList().stream().filter(f -> f.name$().equals(name)).findFirst().orElse(null);
		if (JmsConnector == null) return "Jms Connector not found";
		JmsConnector activeMQConnector = box.busService().jmsConnectors().stream().filter(j -> j.getName().equals(JmsConnector.name$())).findFirst().orElse(null);
		if (activeMQConnector != null && !activeMQConnector.isConnected()) {
			try {
				activeMQConnector.start();
				JmsConnector.enabled(true);
				JmsConnector.save$();
			} catch (Exception e) {
			}
		} else if (activeMQConnector == null) {
			box.busService().addJmsConnector(JmsConnector);
			JmsConnector.enabled(true);
			JmsConnector.save$();
		}
		return Action.OK;
	}
}