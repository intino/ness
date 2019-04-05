package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.TritonBox;
import io.intino.ness.triton.graph.JMSConnector;
import org.apache.activemq.network.jms.JmsConnector;


public class StartJmsConnectorAction {

	public TritonBox box;
	public String name;

	public String execute() {
		JMSConnector JMSConnector = box.graph().jMSConnectorList(f -> f.name$().equals(name)).findFirst().orElse(null);
		if (JMSConnector == null) return "JMS Connector not found";
		JmsConnector activeMQConnector = box.busService().jmsConnectors().stream().filter(j -> j.getName().equals(JMSConnector.name$())).findFirst().orElse(null);
		if (activeMQConnector != null && !activeMQConnector.isConnected()) {
			try {
				activeMQConnector.start();
				JMSConnector.enabled(true);
				JMSConnector.save$();
			} catch (Exception e) {
			}
		} else if (activeMQConnector == null) {
			box.busService().addJMSConnector(JMSConnector);
			JMSConnector.enabled(true);
			JMSConnector.save$();
		}
		return Action.OK;
	}
}