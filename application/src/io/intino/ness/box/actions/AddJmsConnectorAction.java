package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.ExternalBus;
import io.intino.ness.graph.JMSConnector;

import java.util.List;

import static io.intino.ness.box.actions.Action.OK;
import static java.util.Collections.emptyList;


public class AddJmsConnectorAction {

	public NessBox box;
	public String name;
	public String externalBus;
	public String direction;
	public List<String> topics;

	public AddJmsConnectorAction() {
	}

	public AddJmsConnectorAction(NessBox box, String name, String externalBus, String direction, List<String> topics) {
		this.box = box;
		this.name = name;
		this.externalBus = externalBus;
		this.direction = direction;
		this.topics = topics;
	}

	public String execute() {
		ExternalBus bus = box.graph().externalBusList(f -> f.name$().equals(externalBus)).findFirst().orElse(null);
		if (bus == null) return "External Bus not found";
		JMSConnector connector = box.graph().jMSConnectorList(f -> f.name$().equals(name)).findFirst().orElse(null);
		if (connector != null) return "JMS Connector is already defined";
		connector = box.graph().create("jmsConnectors", name).jMSConnector(io.intino.ness.graph.JMSConnector.Direction.valueOf(direction), bus, topics);
		connector.save$();
		box.busService().addJMSConnector(connector.name$(),
				connector.bus().originURL(),
				connector.bus().user(),
				connector.bus().password(),
				direction.equals("incoming") ? topics : emptyList(),
				!direction.equals("incoming") ? topics : emptyList());
		return OK;
	}
}