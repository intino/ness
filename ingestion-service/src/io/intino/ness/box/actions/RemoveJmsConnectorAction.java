package io.intino.ness.box.actions;

import io.intino.ness.box.NessServiceBox;
import io.intino.ness.graph.JMSConnector;
import io.intino.ness.graph.NessGraph;

import java.util.List;

import static io.intino.ness.box.actions.Action.OK;
import static java.util.stream.Collectors.toList;


public class RemoveJmsConnectorAction {

	public NessServiceBox box;
	public String name;

	public String execute() {
		List<JMSConnector> aqueducts = ness().jMSConnectorList(t -> t.name$().equals(name)).collect(toList());
		if (aqueducts.isEmpty()) return "Aqueduct not found";
		for (JMSConnector tank : aqueducts) tank.delete$();
		return OK;
	}

	private NessGraph ness() {
		return box.graph();
	}

}