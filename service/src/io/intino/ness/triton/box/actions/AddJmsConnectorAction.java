package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.ServiceBox;
import io.intino.ness.triton.graph.ExternalBus;
import io.intino.ness.triton.graph.JMSConnector;

import java.util.Arrays;
import java.util.List;


public class AddJmsConnectorAction {

	public ServiceBox box;
	public String name;
	public String externalBus;
	public String direction;
	public String topics;

	public AddJmsConnectorAction() {
	}

	public AddJmsConnectorAction(ServiceBox box, String name, String externalBus, String direction, String topics) {
		this.box = box;
		this.name = name;
		this.externalBus = externalBus;
		this.direction = direction;
		this.topics = topics;
	}

	public String execute() {
		List<String> topics = Arrays.asList(this.topics.split(" "));
		ExternalBus bus = box.graph().externalBusList(f -> f.name$().equals(externalBus)).findFirst().orElse(null);
		if (bus == null) return "External Bus not found";
		JMSConnector connector = box.graph().jMSConnectorList(f -> f.name$().equals(name)).findFirst().orElse(null);
		if (connector != null) return "JMS Connector is already defined";
		connector = box.graph().create("jmsConnectors", name).jMSConnector(JMSConnector.Direction.valueOf(direction), bus, topics);
		connector.save$();
		box.busService().addJMSConnector(connector);
		box.restartBus(true);
		return Action.OK;
	}
}