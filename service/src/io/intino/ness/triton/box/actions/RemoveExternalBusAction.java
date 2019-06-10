package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.ServiceBox;
import io.intino.ness.triton.graph.ExternalBus;
import io.intino.ness.triton.graph.TritonGraph;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class RemoveExternalBusAction {

	public ServiceBox box;
	public String name;

	public String execute() {
		List<ExternalBus> buses = ness().externalBusList(t -> t.name$().equals(name)).collect(toList());
		if (buses.isEmpty()) return "External bus not found";
		for (ExternalBus externalBus : buses) externalBus.delete$();
		return Action.OK;
	}

	private TritonGraph ness() {
		return box.graph();
	}
}