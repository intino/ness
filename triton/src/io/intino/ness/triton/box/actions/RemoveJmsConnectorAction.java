package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.TritonBox;
import io.intino.ness.triton.graph.JMSConnector;
import io.intino.ness.triton.graph.TritonGraph;

import java.util.List;

import static java.util.stream.Collectors.toList;


public class RemoveJmsConnectorAction {

	public TritonBox box;
	public String name;

	public String execute() {
		List<JMSConnector> aqueducts = ness().jMSConnectorList(t -> t.name$().equals(name)).collect(toList());
		if (aqueducts.isEmpty()) return "Aqueduct not found";
		for (JMSConnector tank : aqueducts) tank.delete$();
		return Action.OK;
	}

	private TritonGraph ness() {
		return box.graph();
	}

}