package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.ExternalBus;
import io.intino.ness.graph.JMSConnector;

import java.util.Arrays;
import java.util.List;

import static io.intino.ness.box.actions.Action.OK;


public class AddJmsConnectorAction {

	public NessBox box;
	public String name;
	public String externalBus;
	public String direction;
	public String topics;

	public AddJmsConnectorAction() {
	}

	public AddJmsConnectorAction(NessBox box, String name, String externalBus, String direction, String topics) {
		this.box = box;
		this.name = name;
		this.externalBus = externalBus;
		this.direction = direction;
		this.topics = topics;
	}

	public String execute() {
		List<String> topics = Arrays.asList(this.topics.split(" "));
		ExternalBus bus = box.nessGraph().externalBusList(f -> f.name$().equals(externalBus)).findFirst().orElse(null);
		if (bus == null) return "External Bus not found";
		JMSConnector connector = box.nessGraph().jMSConnectorList(f -> f.name$().equals(name)).findFirst().orElse(null);
		if (connector != null) return "JMS Connector is already defined";
		connector = box.nessGraph().create("jmsConnectors", name).jMSConnector(io.intino.ness.graph.JMSConnector.Direction.valueOf(direction), bus, topics);
		connector.save$();
		box.busService().addJMSConnector(connector);
		box.restartBus(true);
		return OK;
	}
}