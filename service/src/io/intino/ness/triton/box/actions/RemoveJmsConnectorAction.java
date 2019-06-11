package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.ServiceBox;
import io.intino.ness.triton.graph.JmsService.JmsConnector;
import io.intino.ness.triton.graph.TritonGraph;

import java.util.List;


public class RemoveJmsConnectorAction {

	public ServiceBox box;
	public String name;

	public String execute() {
		if (box.graph().jmsService() == null) return "Jms Service inactive";
		List<JmsConnector> connectors = ness().jmsService().jmsConnectorList(t -> t.name$().equals(name));
		if (connectors.isEmpty()) return "Jms Connector not found";
		for (JmsConnector tank : connectors) tank.delete$();
		return Action.OK;
	}

	private TritonGraph ness() {
		return box.graph();
	}

}